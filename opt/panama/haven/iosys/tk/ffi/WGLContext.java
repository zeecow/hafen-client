/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven.iosys.tk.ffi;

import java.util.*;
import java.util.function.*;
import haven.*;
import haven.ffi.*;
import haven.render.*;
import haven.render.gl.*;
import haven.iosys.tk.*;
import haven.iosys.windows.*;
import haven.iosys.gl.*;
import java.awt.image.BufferedImage;
import haven.iosys.windows.Win32.*;

@Toolkit.Available(name = "wgl")
public class WGLContext implements Toolkit.Factory {
    private final Win32 win;
    private final WGL wgl;

    private WGLContext() {
	try {
	    win = Win32.get();
	    wgl = WGL.get();
	} catch(RuntimeException e) {
	    throw(new Unavailable("Win32 environment not available", e));
	}
    }

    private static WGLContext instance = null;
    public static WGLContext get() {
	if(instance == null) {
	    synchronized(WGLContext.class) {
		if(instance == null)
		    instance = new WGLContext();
	    }
	}
	return(instance);
    }

    public Toolkit open(String... args) {
	return(new WGLToolkit());
    }

    public int priority() {
	return(System.getProperty("os.name", "").startsWith("Windows ") ? 100 : 0);
    }

    public class WGLToolkit implements Toolkit {
	private static final String WCLASSNAME = "haven-window";
	private final Handle hInstance;
	private WGL.Ext text;
	private Handle ctx;
	private OpenGL gl;
	private int pfmt;
	private PIXELFORMATDESCRIPTOR pfd;
	private Collection<String> exts;

	private WGLToolkit() {
	    hInstance = win.GetModuleHandle(null);
	    WndClassEx wc = win.WndClassEx();
	    wc.hInstance(hInstance).lpszClassName(WCLASSNAME).lpfnWndProc(this::wndproc).style(Win32.CS_OWNDC);
	    win.RegisterClassEx(wc);
	    createctx();
	}

	private void createctx() {
	    Handle hwnd = null, hdc = null, temp = null;
	    try {
		hwnd = win.CreateWindowEx(0, WCLASSNAME, null, 0, null, null, null, null, hInstance);
		hdc = win.GetDC(hwnd);
		pfd = win.PIXELFORMATDESCRIPTOR();
		pfd.nVersion(1);
		pfd.dwFlags(Win32.PFD_DRAW_TO_WINDOW | Win32.PFD_SUPPORT_OPENGL | Win32.PFD_DOUBLEBUFFER);
		pfd.cColorBits(24).cAlphaBits(8).cDepthBits(24);
		pfmt = win.ChoosePixelFormat(hdc, pfd);
		win.SetPixelFormat(hdc, pfmt, pfd);
		temp = wgl.wglCreateContext(hdc);
		wgl.wglMakeCurrent(hdc, temp);
		text = wgl.ext();
		try {
		    exts = new HashSet<>(Arrays.asList(text.wglGetExtensionsStringEXT().split(" ")));
		} catch(MissingFunction e) {
		    try {
			exts = new HashSet<>(Arrays.asList(text.wglGetExtensionsStringARB(hdc).split(" ")));
		    } catch(MissingFunction e2) {
			throw(new Unavailable("No WGL extensions available"));
		    }
		}
		if(!exts.contains("WGL_ARB_create_context_profile"))
		    throw(new Unavailable("WGL_ARB_create_context_profile not available"));
		if(!exts.contains("WGL_EXT_swap_control"))
		    throw(new Unavailable("WGL_EXT_swap_control not available"));
		ctx = text.wglCreateContextAttribsARB(hdc, null, new int[] {
			WGL.WGL_CONTEXT_PROFILE_MASK_ARB, WGL.WGL_CONTEXT_CORE_PROFILE_BIT_ARB,
			WGL.WGL_CONTEXT_MAJOR_VERSION_ARB, 4, WGL.WGL_CONTEXT_MINOR_VERSION_ARB, 6,
			0, 0,
		    });
		wgl.wglMakeCurrent(hdc, ctx);
		gl = wgl.gl();
		wgl.wglMakeCurrent(hdc, null);
	    } finally {
		if(temp != null)
		    wgl.wglDeleteContext(temp);
		if(hdc != null)
		    win.ReleaseDC(hwnd, hdc);
		if(hwnd != null)
		    win.DestroyWindow(hwnd);
	    }
	}

	private long wndproc(Handle hwnd, int umsg, long wparam, long lparam) {
	    WGLWindow wnd;
	    synchronized(mtmon) {
		wnd = windows.get(hwnd);
	    }
	    if(wnd == null) {
		// Warning.warn(String.format("message received for non-registered windows %s: %d", hwnd, umsg));
		return(win.DefWindowProc(hwnd, umsg, wparam, lparam));
	    }
	    return(wnd.message(hwnd, umsg, wparam, lparam));
	}

	public Cursor.Caps cursorcaps() {
	    return(null);
	}

	public Cursor makecursor(BufferedImage image, Coord hotspot) {
	    return(null);
	}

	private final Object mtmon = new Object();
	private Thread msgthread = null;
	private volatile int msgthreadid = 0;
	private final Map<Handle, WGLWindow> windows = new HashMap<>();
	private final Collection<Runnable> mtasks = new ArrayList<>();
	private void handlemsgs() {
	    try {
		MSG msg = win.MSG();
		win.PeekMessage(msg, null, 0, 0, Win32.PM_NOREMOVE);
		synchronized(mtmon) {
		    msgthreadid = win.GetCurrentThreadId();
		    mtmon.notifyAll();
		}
		while(true) {
		    Collection<Runnable> tasks;
		    synchronized(mtmon) {
			tasks = new ArrayList<>(mtasks);
			mtasks.clear();
			if(tasks.isEmpty() && windows.isEmpty()) {
			    msgthread = null;
			    return;
			}
		    }
		    for(Runnable task : tasks)
			task.run();
		    if(win.GetMessage(msg, null, 0, 0))
			win.DispatchMessage(msg);
		}
	    } finally {
		synchronized(mtmon) {
		    if(msgthread == Thread.currentThread())
			msgthread = null;
		}
	    }
	}

	private void ckmsgthread() {
	    if(msgthread == null) {
		msgthreadid = 0;
		Thread th = new HackThread(this::handlemsgs, "Win32 message dispatch thread");
		th.start();
		msgthread = th;
		boolean irq = false;
		while(msgthreadid == 0) {
		    try {
			mtmon.wait();
		    } catch(InterruptedException e) {
			irq = true;
		    }
		}
		if(irq)
		    Thread.currentThread().interrupt();
	    }
	}

	private void msgrun(Runnable task) {
	    synchronized(mtmon) {
		mtasks.add(task);
		ckmsgthread();
	    }
	    win.PostThreadMessage(msgthreadid, Win32.WM_USER, 0, 0);
	}

	private <T> T msgrun(Supplier<T> task) {
	    class Runner implements Runnable {
		T val;
		boolean done;

		public void run() {
		    val = task.get();
		    synchronized(this) {
			done = true;
			notifyAll();
		    }
		}
	    }
	    Runner r = new Runner();
	    msgrun(r);
	    boolean irq = false;
	    synchronized(r) {
		while(!r.done) {
		    try {
			r.wait();
		    } catch(InterruptedException e) {
			irq = true;
		    }
		}
	    }
	    if(irq)
		Thread.currentThread().interrupt();
	    return(r.val);
	}

	private void glrun(Handle hwnd, Runnable task) {
	    msgrun(() -> {
		Handle hdc = win.GetDC(hwnd);
		try {
		    wgl.wglMakeCurrent(hdc, ctx);
		    try {
			task.run();
		    } finally {
			wgl.wglMakeCurrent(hdc, null);
		    }
		} finally {
		    win.ReleaseDC(hwnd, hdc);
		}
	    });
	}

	private <T> T glrun(Handle hwnd, Supplier<T> task) {
	    return(msgrun(() -> {
		Handle hdc = win.GetDC(hwnd);
		try {
		    wgl.wglMakeCurrent(hdc, ctx);
		    T ret;
		    try {
			ret = task.get();
		    } finally {
			wgl.wglMakeCurrent(hdc, null);
		    }
		    return(ret);
		} finally {
		    win.ReleaseDC(hwnd, hdc);
		}
	    }));
	}

	private void register(WGLWindow wnd) {
	    synchronized(mtmon) {
		windows.put(wnd.hwnd, wnd);
		ckmsgthread();
	    }
	}

	private void unregister(WGLWindow wnd) {
	    synchronized(mtmon) {
		windows.remove(wnd.hwnd);
	    }
	}

	public class WGLWindow implements Windeye {
	    public final Handle hwnd;
	    private final Collection<EventListener> callbacks = new java.util.concurrent.CopyOnWriteArrayList<>();
	    private Coord size = Coord.z;
	    private WGLEnvironment renv;

	    private WGLWindow() {
		hwnd = msgrun(() -> win.CreateWindowEx(0, WCLASSNAME, null, Win32.WS_OVERLAPPEDWINDOW,
						       null, null, null, null, hInstance));
		register(this);
		msgrun(() -> {
		    Handle hdc = win.GetDC(hwnd);
		    try {
			win.SetPixelFormat(hdc, pfmt, pfd);
		    } finally {
			win.ReleaseDC(hwnd, hdc);
		    }
		});
	    }

	    public class WGLEnvironment extends FFIEnvironment {
		private WGLEnvironment() {
		    super(gl, Area.sized(size));
		}

		public void submit(Render cmd) {
		    super.submit(cmd);
		    if(renv == this)
			glrun(hwnd, () -> process(gl));
		}
	    }

	    public void add(EventListener l) {
		callbacks.add(l);
	    }

	    private void callback(Event ev) {
		for(EventListener l : callbacks)
		    l.event(ev);
	    }

	    private long message(Handle hwnd, int umsg, long wparam, long lparam) {
		switch(umsg) {
		case Win32.WM_SIZE:
		    size = Coord.of((int)((lparam & 0x0000ffff) >> 0),
				    (int)((lparam & 0xffff0000) >> 16));
		    Debug.dump(size);
		    return(0);
		default:
		    return(win.DefWindowProc(hwnd, umsg, wparam, lparam));
		}
	    }

	    public WGLWindow show(boolean vis) {
		boolean rv = msgrun(() -> win.ShowWindow(hwnd, vis ? Win32.SW_NORMAL : Win32.SW_HIDE));
		return(this);
	    }

	    public WGLWindow title(String name) {
		msgrun(() -> win.SetWindowText(hwnd, name));
		return(this);
	    }

	    public WGLWindow icon(BufferedImage icon) {
		return(this);
	    }

	    public WGLWindow cursor(Cursor cursor) {
		return(this);
	    }

	    public WGLWindow sizing(Sizing info) {
		return(this);
	    }

	    public WGLWindow state(State st) {
		return(this);
	    }

	    public Coord size() {
		return(size);
	    }

	    public State state() {
		return(State.NORMAL);
	    }

	    public Environment env() {
		if(renv == null) {
		    synchronized(this) {
			if(renv == null)
			    renv = glrun(hwnd, () -> new WGLEnvironment());
		    }
		}
		return(renv);
	    }

	    private void glswap(GL gl) {
		GLException.checkfor(gl, null);
		text.wglSwapIntervalEXT(1);
		win.SwapBuffers(wgl.wglGetCurrentDC());
		GLException.checkfor(gl, null);
	    }

	    public void swapbuffers(Render g) {
		GLRender gbuf = (GLRender)g;
		if(gbuf.env != renv)
		    throw(new IllegalArgumentException());
		gbuf.submit(this::glswap);
	    }

	    public void dispose() {
		unregister(this);
		msgrun(() -> win.DestroyWindow(hwnd));
	    }
	}

	public Windeye window() {
	    return(new WGLWindow());
	}

	public void dispose() {
	}
    }
}

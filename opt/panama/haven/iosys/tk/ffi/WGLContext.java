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
import haven.iosys.tk.Button;
import haven.iosys.windows.Win32.*;
import static haven.iosys.tk.Key.Std.*;

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
	private static int serial = 0;
	private final String wclassname = "haven-window-" + serial;
	private final Handle hInstance;
	private final WndClassEx wndclass;
	private final KeyboardState kb = new KeyboardState();
	private WGL.Ext text;
	private Handle ctx;
	private OpenGL gl;
	private int pfmt;
	private PIXELFORMATDESCRIPTOR pfd;
	private Collection<String> exts;

	private WGLToolkit() {
	    hInstance = win.GetModuleHandle(null);
	    wndclass = win.WndClassEx();
	    wndclass.hInstance(hInstance).lpszClassName(wclassname).lpfnWndProc(this::wndproc).style(Win32.CS_OWNDC);
	    win.RegisterClassEx(wndclass);
	    createctx();
	}

	private void createctx() {
	    Handle hwnd = null, hdc = null, temp = null;
	    try {
		hwnd = win.CreateWindowEx(0, wclassname, null, 0, null, null, null, null, hInstance);
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

	public Cursor.Caps cursorcaps() {
	    return(null);
	}

	public Cursor makecursor(BufferedImage image, Coord hotspot) {
	    return(null);
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

	private final Object mtmon = new Object();
	private Thread msgthread = null;
	private volatile int msgthreadid = 0;
	private final Map<Handle, WGLWindow> windows = new HashMap<>();
	private final Queue<Runnable> mtasks = new LinkedList<>();
	private void handlemsgs() {
	    try {
		MSG msg = win.MSG();
		win.PeekMessage(msg, null, 0, 0, Win32.PM_NOREMOVE);
		synchronized(mtmon) {
		    msgthreadid = win.GetCurrentThreadId();
		    mtmon.notifyAll();
		}
		while(true) {
		    Runnable task;
		    synchronized(mtmon) {
			task = mtasks.poll();
			if((task == null) && windows.isEmpty()) {
			    msgthread = null;
			    return;
			}
		    }
		    if(task != null)
			task.run();
		    while(win.PeekMessage(msg, null, 0, 0, Win32.PM_REMOVE))
			win.DispatchMessage(msg);
		    if(task == null) {
			if(win.GetMessage(msg, null, 0, 0))
			    win.DispatchMessage(msg);
		    }
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
	    private int wheelacc = 0;

	    private WGLWindow() {
		hwnd = msgrun(() -> win.CreateWindowEx(0, wclassname, null, Win32.WS_OVERLAPPEDWINDOW,
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
		// System.err.printf("%x %x %x\n", umsg, wparam, lparam);
		switch(umsg) {
		case Win32.WM_CLOSE:
		    callback(new CloseRequest() {});
		    return(0);
		case Win32.WM_SIZE:
		    size = Coord.of((int)((lparam & 0x0000ffff) >> 0),
				    (int)((lparam & 0xffff0000) >> 16));
		    return(0);

		case Win32.WM_KEYDOWN:
		    callback(new W32KeyDownEvent(wparam, lparam));
		    return(0);
		case Win32.WM_SYSKEYDOWN:
		    callback(new W32KeyDownEvent(wparam, lparam));
		    return(win.DefWindowProc(hwnd, umsg, wparam, lparam));
		case Win32.WM_KEYUP:
		    callback(new W32KeyUpEvent(wparam, lparam));
		    return(0);
		case Win32.WM_SYSKEYUP:
		    callback(new W32KeyUpEvent(wparam, lparam));
		    return(win.DefWindowProc(hwnd, umsg, wparam, lparam));
		case Win32.WM_SYSCOMMAND:
		    if((wparam & 0xfff0) == Win32.SC_KEYMENU)
			return(0);
		    return(win.DefWindowProc(hwnd, umsg, wparam, lparam));

		case Win32.WM_MOUSEMOVE:
		    callback(new W32MouseMoveEvent(wparam, lparam));
		    return(0);
		case Win32.WM_LBUTTONDOWN:
		    callback(new W32MouseDownEvent(Button.Std.LEFT, wparam, lparam));
		    return(0);
		case Win32.WM_LBUTTONUP:
		    callback(new W32MouseUpEvent(Button.Std.LEFT, wparam, lparam));
		    return(0);
		case Win32.WM_MBUTTONDOWN:
		    callback(new W32MouseDownEvent(Button.Std.MIDDLE, wparam, lparam));
		    return(0);
		case Win32.WM_MBUTTONUP:
		    callback(new W32MouseUpEvent(Button.Std.MIDDLE, wparam, lparam));
		    return(0);
		case Win32.WM_RBUTTONDOWN:
		    callback(new W32MouseDownEvent(Button.Std.RIGHT, wparam, lparam));
		    return(0);
		case Win32.WM_RBUTTONUP:
		    callback(new W32MouseUpEvent(Button.Std.RIGHT, wparam, lparam));
		    return(0);
		case Win32.WM_XBUTTONDOWN: {
		    long btn = (wparam & 0xffff0000) >> 16;
		    if(btn == 1)
			callback(new W32MouseDownEvent(Button.Std.BACK, wparam, lparam));
		    else if(btn == 2)
			callback(new W32MouseDownEvent(Button.Std.FORWARD, wparam, lparam));
		    return(0);
		}
		case Win32.WM_XBUTTONUP: {
		    long btn = (wparam & 0xffff0000) >> 16;
		    if(btn == 1)
			callback(new W32MouseUpEvent(Button.Std.BACK, wparam, lparam));
		    else if(btn == 2)
			callback(new W32MouseUpEvent(Button.Std.FORWARD, wparam, lparam));
		    return(0);
		}
		case Win32.WM_MOUSEWHEEL: {
		    int raw = (short)((wparam & 0xffff0000) >> 16);
		    wheelacc += raw;
		    int amount = wheelacc / 120;
		    if(amount != 0) {
			wheelacc -= amount * 120;
			callback(new W32MouseWheelEvent(amount, wparam, lparam));
		    }
		    return(0);
		}
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

	    public class W32KeyEvent {
		public int code, repeat, scancode;
		public boolean ext, ctx, pstate, tstate;
		public Key key;
		public Set<Key.Mod> mods;

		public W32KeyEvent(long wparam, long lparam) {
		    code     = (int)wparam;
		    repeat   = (int)((lparam & 0x0000ffff) >> 0);
		    scancode = (int)((lparam & 0x00ff0000) >> 16);
		    ext      =       (lparam & 0x01000000) != 0;
		    ctx      =       (lparam & 0x20000000) != 0;
		    pstate   =       (lparam & 0x40000000) != 0;
		    tstate   =       (lparam & 0x80000000) != 0;

		    win.GetKeyboardState(kb);
		    if((key = stdkeys.get(code)) == null) {
			synchronized(ukeys) {
			    W32Key ukey = new W32Key(win, code, scancode, ext);
			    if((key = ukeys.get(ukey)) == null) {
				ukeys.put(ukey, ukey);
				key = ukey;
			    }
			}
		    }
		    mods = xlmods(kb);
		}

		public String string() {return(null);}
		public Key key() {return(key);}
		public Set<Key.Mod> mods() {return(mods);}
	    }
	    public class W32KeyDownEvent extends W32KeyEvent implements KeyDownEvent {
		public String string;

		public W32KeyDownEvent(long wparam, long lparam) {
		    super(wparam, lparam);
		    int pst = kb.get(code);
		    kb.set(code, pst | 0x80);
		    char[] chars = win.ToUnicodeEx(code, scancode, kb, 0, win.GetKeyboardLayout(0));
		    kb.set(code, pst);
		    string = ((chars == null) || (chars.length == 0)) ? null : new String(chars);
		}

		public String string() {return(string);}
	    }
	    public class W32KeyUpEvent extends W32KeyEvent implements KeyUpEvent {
		public W32KeyUpEvent(long wparam, long lparam) {super(wparam, lparam);}
	    }

	    public class W32MouseEvent {
		public final Coord wndc;
		public final Set<Button> held = new HashSet<>();
		public final Set<Key.Mod> mods;

		public W32MouseEvent(long wparam, long lparam, boolean screen) {
		    Coord c = Coord.of((short)((lparam & 0x0000ffff) >> 0),
				       (short)((lparam & 0xffff0000) >> 16));
		    if(screen)
			wndc = win.ScreenToClient(hwnd, c);
		    else
			wndc = c;
		    if((wparam & Win32.MK_LBUTTON) != 0) held.add(Button.Std.LEFT);
		    if((wparam & Win32.MK_MBUTTON) != 0) held.add(Button.Std.MIDDLE);
		    if((wparam & Win32.MK_RBUTTON) != 0) held.add(Button.Std.RIGHT);
		    if((wparam & Win32.MK_XBUTTON1) != 0) held.add(Button.Std.BACK);
		    if((wparam & Win32.MK_XBUTTON2) != 0) held.add(Button.Std.FORWARD);
		    win.GetKeyboardState(kb);
		    mods = xlmods(kb);
		}

		public Coord wndc() {return(wndc);}
		public Set<Button> held() {return(held);}
		public Set<Key.Mod> mods() {return(mods);}
	    }
	    public class W32MouseMoveEvent extends W32MouseEvent implements MouseMoveEvent {
		public W32MouseMoveEvent(long wparam, long lparam) {super(wparam, lparam, false);}
	    }
	    public class W32MouseButtonEvent extends W32MouseEvent {
		public final Button btn;

		public W32MouseButtonEvent(Button btn, long wparam, long lparam) {
		    super(wparam, lparam, false);
		    this.btn = btn;
		}

		public Button button() {return(btn);}
	    }
	    public class W32MouseDownEvent extends W32MouseButtonEvent implements MouseDownEvent {
		public W32MouseDownEvent(Button btn, long wparam, long lparam) {super(btn, wparam, lparam);}
	    }
	    public class W32MouseUpEvent extends W32MouseButtonEvent implements MouseUpEvent {
		public W32MouseUpEvent(Button btn, long wparam, long lparam) {super(btn, wparam, lparam);}
	    }

	    public class W32MouseWheelEvent extends W32MouseEvent implements MouseWheelEvent {
		public final int amount;

		public W32MouseWheelEvent(int amount, long wparam, long lparam) {
		    super(wparam, lparam, true);
		    this.amount = amount;
		}

		public int amount() {return(amount);}
	    }
	}

	public Windeye window() {
	    return(new WGLWindow());
	}

	public void dispose() {
	    wgl.wglDeleteContext(ctx);
	    win.UnregisterClass(wclassname, hInstance);
	}

	public Set<Key.Mod> xlmods(KeyboardState kb) {
	    Set<Key.Mod> ret = EnumSet.noneOf(Key.Mod.class);
	    if((kb.get(0x10) & 0xf0) != 0)
		ret.add(Key.Mod.SHIFT);
	    if((kb.get(0x11) & 0xf0) != 0)
		ret.add(Key.Mod.CONTROL);
	    if((kb.get(0x12) & 0xf0) != 0)
		ret.add(Key.Mod.ALT);
	    if((kb.get(0xa5) & 0xf0) != 0)
		ret.add(Key.Mod.ALTGR);
	    if(((kb.get(0x5b) & 0xf0) != 0) || ((kb.get(0x5c) & 0xf0) != 0))
		ret.add(Key.Mod.SUPER);
	    return(ret);
	}
    }

    private static final Map<W32Key, W32Key> ukeys = new HashMap<>();
    public static class W32Key implements Key {
	public final int vk, sc;
	public final boolean ext;
	public final String nm, id;

	public W32Key(Win32 win, int vk, int sc, boolean ext) {
	    this.vk = vk;
	    this.sc = sc;
	    this.ext = ext;
	    this.nm = win.GetKeyNameText((sc << 16) | (ext ? (1 << 24) : 0));
	    this.id = String.format("w32:%s", nm).intern();
	}

	public String id() {return(id);}
	public String nm() {return(nm);}

	public int hashCode() {return(Objects.hash(vk, sc));}
	public boolean equals(W32Key that) {return((this.vk == that.vk) && (this.sc == that.sc) && (this.ext == that.ext));}
	public boolean equals(Object x) {return((x instanceof W32Key) && equals((W32Key)x));}

	public String toString() {
	    return(String.format("#<win32-key %s%d %d (%s)>", ext ? "ext:" : "", sc, vk, nm));
	}
    }

    public static final Map<Integer, Key> stdkeys = Utils.<Integer, Key>map()
	.put(0x0d, ENTER)         .put(0x08, BACKSPACE)     .put(0x09, TAB)
	.put(0x03, CANCEL)        .put(0x0c, CLEAR)         .put(0x10, SHIFT)
	.put(0x11, CONTROL)       .put(0x12, ALT)           .put(0x13, PAUSE)
	.put(0x14, CAPSLOCK)      .put(0x1b, ESCAPE)        .put(0x20, SPACE)
	.put(0x21, PAGEUP)        .put(0x22, PAGEDOWN)      .put(0x23, END)
	.put(0x24, HOME)          .put(0x25, LEFT)          .put(0x26, UP)
	.put(0x27, RIGHT)         .put(0x28, DOWN)          .put(0xbc, COMMA)
	.put(0xbd, MINUS)         .put(0xbe, PERIOD)        .put(0xbf, SLASH)
	.put(0xba, SEMICOLON)     .put(0xbb, EQUALS)        .put(0xdb, LEFTBRACKET)
	.put(0xdd, RIGHTBRACKET)  .put(0xdc, BACKSLASH)     .put(0x2e, DELETE)
	.put(0x90, NUMLOCK)       .put(0x91, SCROLLLOCK)    .put(0x2c, PRINTSCREEN)
	.put(0x2d, INSERT)        .put(0x2f, HELP)          .put(0xc0, BACKQUOTE)
	.put(0xde, QUOTE)

	.put(0x5b, WINDOWS)       .put(0x5c, WINDOWS)       .put(0x5d, MENU)
	.put(0x18, FINAL)         .put(0x1c, CONVERT)       .put(0x1d, NONCONVERT)
	.put(0x1e, ACCEPT)        .put(0x1f, MODECHANGE)    .put(0x15, KANA)
	.put(0x19, KANJI)

	.put(0x30, N0)            .put(0x31, N1)            .put(0x32, N2)
	.put(0x33, N3)            .put(0x34, N4)            .put(0x35, N5)
	.put(0x36, N6)            .put(0x37, N7)            .put(0x38, N8)
	.put(0x39, N9)

	.put(0x41, A)             .put(0x42, B)             .put(0x43, C)
	.put(0x44, D)             .put(0x45, E)             .put(0x46, F)
	.put(0x47, G)             .put(0x48, H)             .put(0x49, I)
	.put(0x4a, J)             .put(0x4b, K)             .put(0x4c, L)
	.put(0x4d, M)             .put(0x4e, N)             .put(0x4f, O)
	.put(0x50, P)             .put(0x51, Q)             .put(0x52, R)
	.put(0x53, S)             .put(0x54, T)             .put(0x55, U)
	.put(0x56, V)             .put(0x57, W)             .put(0x58, X)
	.put(0x59, Y)             .put(0x5a, Z)

	.put(0x70, F1)            .put(0x71, F2)            .put(0x72, F3)
	.put(0x73, F4)            .put(0x74, F5)            .put(0x75, F6)
	.put(0x76, F7)            .put(0x77, F8)            .put(0x78, F9)
	.put(0x79, F10)           .put(0x7a, F11)           .put(0x7b, F12)
	.put(0x7c, F13)           .put(0x7d, F14)           .put(0x7e, F15)
	.put(0x7f, F16)           .put(0x80, F17)           .put(0x81, F18)
	.put(0x82, F19)           .put(0x83, F20)           .put(0x84, F21)
	.put(0x85, F22)           .put(0x86, F23)           .put(0x87, F24)

	.put(0x60, NP0)           .put(0x61, NP1)           .put(0x62, NP2)
	.put(0x63, NP3)           .put(0x64, NP4)           .put(0x65, NP5)
	.put(0x66, NP6)           .put(0x67, NP7)           .put(0x68, NP8)
	.put(0x69, NP9)           .put(0x6f, NP_DIV)        .put(0x6a, NP_MUL)
	.put(0x6d, NP_SUB)        .put(0x6b, NP_ADD)        .put(0x6c, NP_SEP)
	.put(0x6e, NP_DEC)
	.map();
}

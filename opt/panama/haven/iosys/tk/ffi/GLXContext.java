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
import haven.render.*;
import haven.render.gl.*;
import haven.ffi.*;
import haven.iosys.tk.*;
import haven.iosys.posix.*;
import haven.iosys.x11.*;
import haven.iosys.gl.*;
import java.awt.image.*;
import haven.iosys.x11.XLib.*;
import haven.iosys.x11.XInput.*;
import haven.iosys.tk.Button;
import static haven.iosys.x11.XKeysym.*;
import static haven.iosys.tk.Key.Std.*;

@Toolkit.Available(name = "glx")
public class GLXContext implements Toolkit.Factory {
    private final LibC libc;
    private final XLib xlib;
    private final XInput xi;
    private final GLX glx;
    private final OpenGL gl;
    private final Xcursor xcr;

    private GLXContext() {
	try {
	    this.libc = LibC.get();
	    this.xlib = XLib.get();
	    this.xi = XInput.get();
	    this.glx = GLX.get();
	    this.gl = glx.gl();
	    this.xcr = Xcursor.get();
	} catch(RuntimeException e) {
	    throw(new Unavailable("X11 libraries not available", e));
	}
	xlib.XInitThreads();
	xlib.XSetErrorHandler();
    }

    private static GLXContext instance = null;
    public static GLXContext get() {
	if(instance == null) {
	    synchronized(GLXToolkit.class) {
		if(instance == null)
		    instance = new GLXContext();
	    }
	}
	return(instance);
    }

    public Toolkit open(String... args) {
	return(new GLXToolkit(null, -1));
    }

    public int priority() {
	return(System.getProperty("os.name", "").equals("Linux") ? 100 : 0);
    }

    public static class XCursor implements Cursor {
	public static final XCursor inherit = new XCursor(null, XID.None);
	public final XID id;
	private final Runnable clean;

	public XCursor(Display dpy, XID id) {
	    this.id = id;
	    if(dpy != null)
		clean = Finalizer.finalize(this, () -> {if(!dpy.closed) dpy.lib.XFreeCursor(dpy, id);});
	    else
		clean = null;
	}

	public void dispose() {
	    clean.run();
	}
    }

    public class GLXToolkit implements Toolkit {
	private static final int[][] glversions = {
	    {4, 6}, {4, 5}, {4, 4}, {4, 3}, {4, 2}, {4, 1}, {4, 0},
				    {3, 3}, {3, 2}, {3, 1}, {3, 0},
						    {2, 1}, {2, 0},
	};
	private static final int[] fbattribs = {
	    GLX.GLX_DRAWABLE_TYPE, GLX.GLX_WINDOW_BIT,
	    GLX.GLX_RENDER_TYPE, GLX.GLX_RGBA_BIT,
	    GLX.GLX_DOUBLEBUFFER, 1,
	    GLX.GLX_DEPTH_SIZE, 24, GLX.GLX_RED_SIZE, 8, GLX.GLX_GREEN_SIZE, 8, GLX.GLX_BLUE_SIZE, 8,
	    XLib.None
	};
	public final Display dpy;
	public final int nscreen;
	public final Screen screen;
	public final ExtensionInfo ext_xi, ext_glx;
	public final XkbExtensionInfo ext_xkb;
	public final List<String> exts;
	public final GLX.GLXFBConfig fb;
	public final XLib.XVisualInfo vis;
	public final XID colormap;
	public final GLX.GLXContext ctx;
	public final XIM im;
	public final long imstyle;
	public final int mod_alt, mod_meta, mod_altgr, mod_super;
	public final Map<Integer, Integer> rmodmap = new HashMap<>();
	public final Map<Integer, XIPointerInfo> pointers = new HashMap<>();
	public final Cursor.Caps ccaps;
	public final Atomic ATOM = new Atomic("ATOM");
	public final Atomic CARDINAL = new Atomic("CARDINAL");
	public final Atomic WINDOW = new Atomic("WINDOW");
	public final Atomic UTF8_STRING = new Atomic("UTF8_STRING");
	public final Atomic WM_NAME = new Atomic("WM_NAME");
	public final Atomic WM_PROTOCOLS = new Atomic("WM_PROTOCOLS");
	public final Atomic WM_DELETE_WINDOW = new Atomic("WM_DELETE_WINDOW");
	public final Atomic _NET_WM_NAME = new Atomic("_NET_WM_NAME");
	public final Atomic _NET_WM_ICON = new Atomic("_NET_WM_ICON");
	public final Atomic _NET_WM_STATE = new Atomic("_NET_WM_STATE");
	public final Atomic _NET_WM_STATE_HIDDEN = new Atomic("_NET_WM_STATE_HIDDEN");
	public final Atomic _NET_WM_STATE_MAXIMIZED_VERT = new Atomic("_NET_WM_STATE_MAXIMIZED_VERT");
	public final Atomic _NET_WM_STATE_MAXIMIZED_HORZ = new Atomic("_NET_WM_STATE_MAXIMIZED_HORZ");
	public final Atomic _NET_WM_STATE_FULLSCREEN = new Atomic("_NET_WM_STATE_FULLSCREEN");
	public final Atomic _NET_WM_PID = new Atomic("_NET_WM_PID");
	public final Atomic _NET_WM_PING = new Atomic("_NET_WM_PING");
	public final Atomic _NET_SUPPORTING_WM_CHECK = new Atomic("_NET_SUPPORTING_WM_CHECK");
	public final String srvvendor, wmname;
	public final int srvrelease;
	private boolean closed = false;

	public class Atomic {
	    public final String name;
	    private Atom id = null;

	    public Atomic(String name) {
		this.name = name;
	    }

	    public boolean is(Atom a) {
		if(id == null)
		    id = dpy.lib.XInternAtom(dpy, name, false);
		return(a.equals(id));
	    }
	}

	private void prepare(Atomic... atoms) {
	    String[] names = new String[atoms.length];
	    for(int i = 0; i < atoms.length; i++)
		names[i] = atoms[i].name;
	    Atom[] ret = xlib.XInternAtoms(dpy, names, false);
	    for(int i = 0; i < atoms.length; i++)
		atoms[i].id = ret[i];
	}

	private GLX.GLXContext createctx() {
	    GLX.GLXContext ret = null;
	    XException lasterr = null;
	    for(int[] ver : glversions) {
		int[] ctxattribs = {
		    GLX.GLX_CONTEXT_PROFILE_MASK_ARB, GLX.GLX_CONTEXT_CORE_PROFILE_BIT_ARB,
		    GLX.GLX_CONTEXT_MAJOR_VERSION_ARB, ver[0], GLX.GLX_CONTEXT_MINOR_VERSION_ARB, ver[1],
		    XLib.None,
		};
		try {
		    ret = glx.glXCreateContextAttribsARB(dpy, fb, null, true, ctxattribs);
		} catch(XException e) {
		    if(e.request_code != ext_glx.opcode)
			throw(e);
		    lasterr = e;
		    continue;
		}
		break;
	    }
	    if(ret == null)
		throw(new Unavailable("could not create OpenGL context", lasterr));
	    return(ret);
	}

	public class XIPointerInfo {
	    public final String[] buttons;
	    public final Map<Integer, Scroll> scroll = new HashMap<>();

	    public class Scroll {
		public final int type;
		public final double inc;
		public double lastval;

		private Scroll(XIScrollClassInfo si, XIValuatorClassInfo vi) {
		    this.type = si.scroll_type();
		    this.inc = si.increment();
		    this.lastval = vi.value();
		}
	    }

	    private XIPointerInfo(XIDeviceInfo dev, Map<Integer, XIDeviceInfo> devs) {
		String[] buttons = new String[0];
		Map<Integer, XIValuatorClassInfo> vals = new IntMap<>();
		for(XIAnyClassInfo cl : dev.classes()) {
		    if(cl.type() == XInput.XIButtonClass) {
			XIButtonClassInfo bi = cl.button();
			Atom[] labels = bi.labels();
			for(int i = 0; i < labels.length; i++) {
			    if(labels[i] == null)
				continue;
			    Atom lbl = labels[i];
			    check: for(int o = 0; o < i; o++) {
				if(lbl.equals(labels[o])) {
				    for(int u = 0; u < labels.length; u++) {
					if(lbl.equals(labels[u]))
					    labels[u] = null;
				    }
				    break check;
				}
			    }
			}
			buttons = new String[labels.length];
			for(int i = 0; i < labels.length; i++) {
			    buttons[i] = (labels[i] == null) ? null : xlib.XGetAtomName(dpy, labels[i]);
			}
		    } else if(cl.type() == XInput.XIValuatorClass) {
			XIValuatorClassInfo vi = cl.valuator();
			/* Apparently, master pointer valuators can
			 * sometimes be invalid from XIQueryDevice,
			 * even though the values from the slave are
			 * correct... */
			XIDeviceInfo src = devs.get(vi.sourceid());
			if(src != null) {
			    for(XIAnyClassInfo scl : src.classes()) {
				if(scl.type() == XInput.XIValuatorClass) {
				    XIValuatorClassInfo svi = scl.valuator();
				    if(svi.label().equals(vi.label()))
					vals.put(vi.number(), svi);
				}
			    }
			}
		    }
		}
		for(XIAnyClassInfo cl : dev.classes()) {
		    if(cl.type() == XInput.XIScrollClass) {
			XIScrollClassInfo si = cl.scroll();
			if(vals.containsKey(si.number())) {
			    if((si.scroll_type() == XInput.XIScrollTypeVertical) || (si.scroll_type() == XInput.XIScrollTypeHorizontal)) {
				scroll.put(si.number(), new Scroll(si, vals.get(si.number())));
			    }
			}
		    }
		}
		this.buttons = buttons;
	    }
	}

	public Map<Integer, XIDeviceInfo> devicemap() {
	    Map<Integer, XIDeviceInfo> ret = new IntMap<>();
	    xi.XIQueryDevice(dpy, XInput.XIAllDevices).forEach(dev -> ret.put(dev.deviceid(), dev));
	    return(ret);
	}

	public GLXToolkit(String display, int nscreen) {
	    boolean done = false;
	    try {
		if(!xlib.XkbLibraryVersion(new int[] {XLib.XkbMajorVersion, XLib.XkbMinorVersion}))
		    throw(new Unavailable("XKB client support unavailable"));
		if((this.dpy = xlib.XOpenDisplay(display)) == null)
		    throw(new Unavailable("Cannot open X11 display"));
		if(nscreen < 0)
		    nscreen = xlib.DefaultScreen(dpy);
		else if(nscreen >= xlib.ScreenCount(dpy))
		    throw(new IllegalArgumentException("no such screen: " + nscreen));
		this.nscreen = nscreen;
		this.screen = xlib.ScreenOfDisplay(dpy, nscreen);
		srvvendor = xlib.XServerVendor(dpy);
		srvrelease = xlib.XVendorRelease(dpy);

		/* Atoms */
		prepare(ATOM, CARDINAL, WINDOW, UTF8_STRING,
			WM_NAME, WM_PROTOCOLS, WM_DELETE_WINDOW,
			_NET_WM_NAME, _NET_WM_ICON, _NET_WM_PID, _NET_WM_PING,
			_NET_WM_STATE, _NET_WM_STATE_MAXIMIZED_VERT, _NET_WM_STATE_MAXIMIZED_HORZ,
			_NET_WM_STATE_HIDDEN, _NET_WM_STATE_FULLSCREEN,
			_NET_SUPPORTING_WM_CHECK
			);

		/* Keyboard input */
		{
		    if((ext_xkb = xlib.XkbQueryExtension(dpy, XLib.XkbMajorVersion, XLib.XkbMinorVersion)) == null)
			throw(new Unavailable("XKB is not supported"));
		    boolean[] supported = {false};
		    xlib.XkbSetDetectableAutoRepeat(dpy, true, supported);
		    if(!supported[0])
			Warning.warn("detectable auto-repeat is not available");
		}
		{
		    XIM im = xlib.XOpenIM(dpy, null, null);
		    long cst = 0;
		    if(im != null) {
			long[] styles = ((XIMStyles)xlib.XGetIMValues(im, Arrays.asList(XLib.XNQueryInputStyle)).get(XLib.XNQueryInputStyle)).styles();
			Arrays.sort(styles);
			if(Arrays.binarySearch(styles, XLib.XIMPreeditNothing | XLib.XIMStatusNothing) >= 0)
			    cst = XLib.XIMPreeditNothing | XLib.XIMStatusNothing;
			else if(Arrays.binarySearch(styles, XLib.XIMPreeditNone | XLib.XIMStatusNone) >= 0)
			    cst = XLib.XIMPreeditNone | XLib.XIMStatusNone;
			else
			    cst = 0;
		    } else {
			Warning.warn("no X11 input method available");
		    }
		    if((im != null) && (cst != 0)) {
			this.im = im;
			this.imstyle = cst;
			
		    } else {
			if(im != null)
			    xlib.XCloseIM(im);
			this.im = null;
			this.imstyle = 0;
		    }
		}
		{
		    int[][] modmap = xlib.XGetModifierMapping(dpy).mapping();
		    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		    for(int i = 0; i < 8; i++) {
			for(int key : modmap[i]) {
			    rmodmap.put(key, i);
			    min = Math.min(min, key);
			    max = Math.max(max, key);
			}
		    }
		    XID[][] syms = xlib.XGetKeyboardMapping(dpy, min, max + 1 - min);
		    int mod_alt = 0, mod_meta = 0, mod_altgr = 0, mod_super = 0;
		    for(int i = 3; i < 8; i++) {
			for(int key : modmap[i]) {
			    for(XID sym : syms[key - min]) {
				if(sym.equals(XK_Alt_L) || sym.equals(XK_Alt_R)) {
				    mod_alt = i;
				} else if(sym.equals(XK_Meta_L) || sym.equals(XK_Meta_R)) {
				    mod_meta = i;
				} else if(sym.equals(XK_ISO_Level3_Shift)) {
				    mod_altgr = i;
				} else if(sym.equals(XK_Super_L) || sym.equals(XK_Super_R)) {
				    mod_super = i;
				}
			    }
			}
		    }
		    if((mod_alt == 0) && (mod_meta == 0))
			Warning.warn("found no Alt or Meta modifiers on X11 display");
		    this.mod_alt = mod_alt; this.mod_meta = mod_meta; this.mod_altgr = mod_altgr; this.mod_super = mod_super;
		}

		/* XInput2 */
		if((ext_xi = xlib.XQueryExtension(dpy, "XInputExtension")) == null)
		    throw(new Unavailable("XInputExtension is not supported"));

		{
		    int ver[] = {2, 2};
		    if((xi.XIQueryVersion(dpy, ver) != XLib.Success) || ((ver[0] < 2) || ((ver[0] == 2) && (ver[1] < 2))))
			throw(new Unavailable("XInput >=2.2 not supported"));
		    XIEventMask mask = new XIEventMask(XInput.XIAllDevices);
		    mask.set(XInput.XI_DeviceChanged, XInput.XI_HierarchyChanged);
		    xi.XISelectEvents(dpy, screen.root(), Arrays.asList(mask));
		    refreshxi();
		    if(false) {
			for(XInput.XIDeviceInfo dev : xi.XIQueryDevice(dpy, XInput.XIAllDevices)) {
			    Debug.dump(dev, dev.name());
			    for(XInput.XIAnyClassInfo cl : dev.classes()) {
				if(cl.type() == XInput.XIValuatorClass) {
				    XInput.XIValuatorClassInfo v = cl.valuator();
				    Debug.dump(v, xlib.XGetAtomName(dpy, v.label()));
				} else if(cl.type() == XInput.XIScrollClass) {
				    Debug.dump(cl.scroll());
				}
			    }
			}
		    }
		}

		/* Xcursor */
		{
		    if(xcr.XcursorSupportsARGB(dpy)) {
			Coord maxcursor = xlib.XQueryBestCursor(dpy, screen.root(), Coord.of(512, 512));
			ccaps = new Cursor.Caps(Math.min(maxcursor.x, maxcursor.y), xcr.XcursorGetDefaultSize(dpy));
		    } else {
			ccaps = null;
		    }
		    emptycurs = makecursor(PUtils.rasterimg(PUtils.imgraster(Coord.of(1, 1))), Coord.z);
		}

		/* GLX */
		if((ext_glx = xlib.XQueryExtension(dpy, "GLX")) == null)
		    throw(new Unavailable("GLX is not supported"));
		exts = Arrays.asList(glx.glXQueryExtensionsString(dpy, nscreen).split(" "));
		if(!exts.contains("GLX_ARB_create_context_profile"))
		    throw(new Unavailable("GLX_ARB_create_context_profile not supported"));

		GLX.GLXFBConfig[] fbs = glx.glXChooseFBConfig(dpy, nscreen, fbattribs);
		if(fbs.length == 0)
		    throw(new Unavailable("no suitable framebuffer configuration available"));
		fb = fbs[0];
		vis = glx.glXGetVisualFromFBConfig(dpy, fb);
		colormap = xlib.XCreateColormap(dpy, screen.root(), vis.visual(), XLib.AllocNone);
		this.ctx = createctx();

		/* WM info */
		{
		    String wmname = "Unknown";
		    XProperty wmw = xlib.XGetWindowProperty(dpy, screen.root(), _NET_SUPPORTING_WM_CHECK.id, false, Atom.nil);
		    if((wmw.format == 32) && wmw.type.equals(WINDOW.id)) {
			XProperty nm = xlib.XGetWindowProperty(dpy, wmw.x(0), _NET_WM_NAME.id, false, Atom.nil);
			if(nm.format == 8) {
			    wmname = new String(nm.b(), ABI.C_CHARSET);
			}
		    }
		    this.wmname = wmname;
		}

		done = true;
	    } finally {
		if(!done)
		    dispose();
	    }
	}

	private final FileDescriptor rwake, wwake; {
	    int[] pipe = libc.pipe();
	    rwake = FileDescriptor.of(pipe[0]);
	    wwake = FileDescriptor.of(pipe[1]);
	}

	private void refreshxi() {
	    Map<Integer, XIDeviceInfo> devs = devicemap();
	    synchronized(pointers) {
		pointers.clear();
		for(XIDeviceInfo dev : devs.values()) {
		    if(dev.use() == XInput.XIMasterPointer)
			pointers.put(dev.deviceid(), new XIPointerInfo(dev, devs));
		}
	    }
	}

	private void dispatch(XIDeviceEvent ev) {
	    XID id;
	    id = ev.event();
	    GLXWindow wnd;
	    synchronized(etmon) {
		wnd = windows.get(id);
	    }
	    if(wnd == null) {
		Warning.warn(String.format("XInput event received for non-registered window %s: %d", id, ev.evtype()));
		return;
	    }
	    wnd.event(ev);
	}

	private void dispatch(XIFocusEvent ev) {
	    XID id;
	    id = ev.event();
	    GLXWindow wnd;
	    synchronized(etmon) {
		wnd = windows.get(id);
	    }
	    if(wnd == null) {
		Warning.warn(String.format("XInput event received for non-registered window %s: %d", id, ev.evtype()));
		return;
	    }
	    wnd.event(ev);
	}

	private void dispatch(XEvent ev) {
	    if(xlib.XFilterEvent(ev, XID.None))
		return;
	    if(ev.type() == XLib.GenericEvent) {
		XGenericEvent ge = ev.xgeneric();
		if(ge.extension() == ext_xi.opcode) {
		    switch(ge.evtype()) {
		    case XInput.XI_DeviceChanged, XInput.XI_HierarchyChanged:
			xrun(this::refreshxi);
			break;
		    case XInput.XI_KeyPress, XInput.XI_KeyRelease, XInput.XI_ButtonPress, XInput.XI_ButtonRelease, XInput.XI_Motion:
			dispatch(xi.XIDeviceEvent(ge));
			break;
		    case XInput.XI_Enter, XInput.XI_Leave, XInput.XI_FocusIn, XInput.XI_FocusOut:
			dispatch(xi.XIFocusEvent(ge));
			break;
		    default:
			Warning.warn(String.format("unexpected XInput event received: %d", ge.evtype()));
			break;
		    }
		} else {
		    Warning.warn(String.format("unexpected generic event received: opcode=%d, type=%d", ge.extension(), ge.evtype()));
		}
	    } else {
		XID id = ev.window();
		GLXWindow wnd;
		synchronized(etmon) {
		    wnd = windows.get(id);
		}
		if(wnd == null) {
		    Warning.warn(String.format("event received for non-registered window %s: %d", id, ev.type()));
		    return;
		}
		wnd.event(ev);
	    }
	}

	private final Object etmon = new Object();
	private Thread evthread = null;
	private int etref = 0;
	private final Map<XID, GLXWindow> windows = new HashMap<>();
	private final Queue<Runnable> xtasks = new LinkedList<>();
	private void handleevents() {
	    try {
		LibC.PollFD pfd = libc.pollfd(2);
		while(true) {
		    boolean block = true;
		    Runnable task;
		    synchronized(etmon) {
			task = xtasks.poll();
			if(!xtasks.isEmpty())
			    block = false;
			if((task == null) && block && (etref == 0) && windows.isEmpty()) {
			    evthread = null;
			    return;
			}
		    }
		    if(task != null)
			task.run();
		    while(xlib.XPending(dpy) > 0) {
			XLib.XEvent ev = xlib.XEvent();
			xlib.XNextEvent(dpy, ev);
			dispatch(ev);
		    }
		    pfd.fd(0, xlib.ConnectionNumber(dpy)).events(0, LibC.POLLIN).revents(0, 0);
		    pfd.fd(1, rwake.fileno).events(1, LibC.POLLIN).revents(1, 0);
		    try {
			int rv = libc.poll(pfd, 2, block ? -1 : 0);
		    } catch(StdError e) {
			if(e.errno == LibC.EINTR)
			    continue;
			throw(e);
		    }
		    if((pfd.revents(1) & LibC.POLLIN) != 0)
			libc.read(rwake.fileno, 1024);
		}
	    } finally {
		synchronized(etmon) {
		    if(evthread == Thread.currentThread())
			evthread = null;
		}
	    }
	}

	private class Aliveness implements AutoCloseable {
	    Aliveness() {
		synchronized(etmon) {
		    etref++;
		}
	    }

	    public void close() {
		synchronized(etmon) {
		    etref--;
		}
	    }
	}

	private void ckevthread() {
	    if(evthread == null) {
		Thread th = new HackThread(this::handleevents, "X11 dispatch thread");
		th.start();
		evthread = th;
	    }
	}

	private void xrun(Runnable task) {
	    synchronized(etmon) {
		xtasks.add(task);
		ckevthread();
	    }
	    while(libc.write(wwake.fileno, new byte[] {0}) == 0);
	}

	private <T> T xrun(Supplier<T> task) {
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
	    xrun(r);
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

	private void glrun(XID wnd, Runnable task) {
	    xrun(() -> {
		if(!glx.glXMakeCurrent(dpy, wnd, ctx))
		    throw(new RuntimeException("glXMakeCurrent failed without reason"));
		try {
		    task.run();
		} finally {
		    glx.glXMakeCurrent(dpy, XLib.XID.None, null);
		}
	    });
	}

	private <T> T glrun(XID wnd, Supplier<T> task) {
	    return(xrun(() -> {
		if(!glx.glXMakeCurrent(dpy, wnd, ctx))
		    throw(new RuntimeException("glXMakeCurrent failed without reason"));
		T ret;
		try {
		    ret = task.get();
		} finally {
		    glx.glXMakeCurrent(dpy, XLib.XID.None, null);
		}
		return(ret);
	    }));
	}

	private void register(GLXWindow wnd) {
	    synchronized(etmon) {
		windows.put(wnd.id, wnd);
		ckevthread();
	    }
	}

	private void unregister(GLXWindow wnd) {
	    synchronized(etmon) {
		windows.remove(wnd.id);
	    }
	}

	public class GLXWindow implements Windeye {
	    public final XID id;
	    public final XIC ic;
	    private final Collection<Consumer<XEvent>> listeners = new ArrayList<>();
	    private final Collection<EventListener> callbacks = new java.util.concurrent.CopyOnWriteArrayList<>();
	    private boolean showing = false;
	    private boolean mapped, focused;
	    private GLXEnvironment renv;
	    private Coord size = Coord.z;
	    private int visibility = 0;
	    private int cursi = -1;

	    public class GLXEnvironment extends FFIEnvironment {
		private int qstate;

		private GLXEnvironment() {
		    super(gl, Area.sized(size));
		}

		private void process() {
		    synchronized(this) {
			qstate = 2;
		    }
		    process(gl);
		    synchronized(this) {
			if((qstate & 1) != 0)
			    glrun(id, (Runnable)this::process);
			qstate &= ~2;
		    }
		}

		public void submit(Render cmd) {
		    super.submit(cmd);
		    synchronized(this) {
			if(renv == this) {
			    if(qstate == 0)
				glrun(id, (Runnable)this::process);
			    qstate |= 1;
			}
		    }
		}
	    }

	    public GLXWindow() {
		boolean done = false;
		try {
		    XSetWindowAttributes attrs = xlib.XSetWindowAttributes();
		    long values = XLib.CWColormap | XLib.CWEventMask;
		    attrs.colormap(colormap);
		    long evmask = XLib.StructureNotifyMask | XLib.FocusChangeMask | XLib.PropertyChangeMask;
		    evmask |= XLib.VisibilityChangeMask | XLib.KeyPressMask | XLib.KeyReleaseMask;
		    try(Aliveness _ = new Aliveness()) {
			id = xrun(() -> xlib.XCreateWindow(dpy, screen.root(), 0, 0, 1, 1, 0, vis.depth(), XLib.InputOutput, vis.visual(), values, attrs));
			register(this);
		    }
		    if(im != null) {
			ic = xrun(() -> xlib.XCreateIC(im, Utils.<String, Object>map()
						       .put(XLib.XNInputStyle, imstyle)
						       .put(XLib.XNClientWindow, id).map()));
			if(ic != null) {
			    evmask |= xrun(() -> ((Number)xlib.XGetICValues(ic, Arrays.asList(XLib.XNFilterEvents)).get(XLib.XNFilterEvents))).longValue();
			    xrun(() -> xlib.XSetICFocus(ic));
			} else {
			    Warning.warn(String.format("failed to create X11 input context"));
			}
		    } else {
			ic = null;
		    }

		    {
			XIEventMask mask = new XIEventMask(XInput.XIAllMasterDevices);
			mask.set(XInput.XI_ButtonPress, XInput.XI_ButtonRelease, XInput.XI_Motion, XInput.XI_Enter);
			xrun(() -> xi.XISelectEvents(dpy, id, Arrays.asList(mask)));
		    }

		    xrun(() -> {
			xlib.XChangeProperty(dpy, id, _NET_WM_PID.id, CARDINAL.id, XLib.PropModeReplace, new long[] {libc.getpid()});
			xlib.XChangeProperty(dpy, id, WM_PROTOCOLS.id, ATOM.id, XLib.PropModeReplace, new Atom[] {WM_DELETE_WINDOW.id, _NET_WM_PING.id});
		    });
		    long barda = evmask;
		    xrun(() -> xlib.XSelectInput(dpy, id, barda));

		    done = true;
		} finally {
		    if(!done)
			dispose();
		}
	    }

	    public void add(EventListener l) {
		callbacks.add(l);
	    }

	    private void callback(Event ev) {
		for(EventListener l : callbacks)
		    l.event(ev);
	    }

	    public GLXToolkit toolkit() {
		return(GLXToolkit.this);
	    }

	    public GLXWindow show(boolean show) {
		if(show && !showing) {
		    xrun(() -> xlib.XMapWindow(dpy, id));
		    showing = true;
		    waitfor(ev -> ev.type() == XLib.MapNotify);
		} else if(!show && showing) {
		    xrun(() -> xlib.XUnmapWindow(dpy, id));
		    showing = false;
		    waitfor(ev -> ev.type() == XLib.UnmapNotify);
		}
		return(this);
	    }

	    public GLXWindow size(Coord size) {
		xrun(() -> xlib.XResizeWindow(dpy, id, size.x, size.y));
		return(this);
	    }

	    public GLXWindow title(String title) {
		xrun(() -> {
		    xlib.XChangeProperty(dpy, id,      WM_NAME.id, UTF8_STRING.id, XLib.PropModeReplace, title.getBytes(Utils.utf8));
		    xlib.XChangeProperty(dpy, id, _NET_WM_NAME.id, UTF8_STRING.id, XLib.PropModeReplace, title.getBytes(Utils.utf8));
		});
		return(this);
	    }

	    public GLXWindow cursor(Cursor cursor) {
		if(cursor == null) {
		    xrun(() -> xlib.XDefineCursor(dpy, id, XID.None));
		    return(this);
		}
		XCursor xc;
		if(cursor instanceof Cursor.Std)
		    xc = getlibcursor((Cursor.Std)cursor);
		else
		    xc = (XCursor)cursor;
		xrun(() -> xlib.XDefineCursor(dpy, id, xc.id));
		return(this);
	    }

	    public GLXWindow icon(BufferedImage img) {
		img = PUtils.coercergba(img, false);
		Coord sz = PUtils.imgsz(img);
		long[] pixels = new long[2 + (sz.x * sz.y)];
		pixels[0] = sz.x;
		pixels[1] = sz.y;
		Raster imgd = img.getRaster();
		for(int y = 0, p = 2; y < sz.y; y++) {
		    for(int x = 0; x < sz.x; x++, p++) {
			int a = imgd.getSample(x, y, 3);
			int r = (imgd.getSample(x, y, 0) * a) / 255;
			int g = (imgd.getSample(x, y, 1) * a) / 255;
			int b = (imgd.getSample(x, y, 2) * a) / 255;
			pixels[p] = (b << 0) | (g << 8) | (r << 16) | (a << 24);
		    }
		}
		xrun(() -> xlib.XChangeProperty(dpy, id, _NET_WM_ICON.id, CARDINAL.id, XLib.PropModeReplace, pixels));
		return(this);
	    }

	    public GLXWindow sizing(Sizing info) {
		XLib.XSizeHints hints = new XLib.XSizeHints();
		Coord nsz = null;
		if(info.fixsize == null) {
		    if(info.normsize != null) {
			nsz = info.normsize;
			hints.width  = info.normsize.x;
			hints.height = info.normsize.y;
			hints.base_width  = info.normsize.x;
			hints.base_height = info.normsize.y;
			hints.flags |= XLib.PSize | XLib.PBaseSize;
		    }
		    if(info.minsize != null) {
			hints.min_width  = info.minsize.x;
			hints.min_height = info.minsize.y;
			hints.flags |= XLib.PMinSize;
		    }
		    if(info.maxsize != null) {
			hints.max_width  = info.maxsize.x;
			hints.max_height = info.maxsize.y;
			hints.flags |= XLib.PMaxSize;
		    }
		} else {
		    nsz = info.fixsize;
		    hints.width  = info.fixsize.x;
		    hints.height = info.fixsize.y;
		    hints.base_width  = info.fixsize.x;
		    hints.base_height = info.fixsize.y;
		    hints.min_width  = info.fixsize.x;
		    hints.min_height = info.fixsize.y;
		    hints.max_width  = info.fixsize.x;
		    hints.max_height = info.fixsize.y;
		    hints.flags |= XLib.PSize | XLib.PBaseSize | XLib.PMinSize | XLib.PMaxSize;
		}
		if(hints.flags != 0) {
		    Coord nsz2 = nsz;
		    xrun(() -> {
			if(nsz2 != null)
			    xlib.XResizeWindow(dpy, id, nsz2.x, nsz2.y);
			xlib.XSetWMNormalHints(dpy, id, hints);
		    });
		}
		return(this);
	    }

	    private Set<Atom> curstate = Collections.emptySet();
	    private void wmstate(Atom[] st) {
		xrun(() -> {
		    if(showing) {
			Collection<Atom> p = curstate, n = Arrays.asList(st);
			boolean mhcons = false, mvcons = false;
			for(Atom s : p) {
			    if(!n.contains(s)) {
				if((s.equals(_NET_WM_STATE_MAXIMIZED_VERT.id) && mvcons) || (s.equals(_NET_WM_STATE_MAXIMIZED_HORZ.id) && mhcons))
				    continue;
				XEvent msg = xlib.XEvent().type(XLib.ClientMessage).window(id);
				msg.xclient().message_type(_NET_WM_STATE.id).format(32).l(0, 0).a(1, _NET_WM_STATE_FULLSCREEN.id).a(2, Atom.nil).l(3, 1);
				if(s.equals(_NET_WM_STATE_MAXIMIZED_VERT.id) && p.contains(_NET_WM_STATE_MAXIMIZED_HORZ.id)) {
				    msg.xclient().a(2, _NET_WM_STATE_MAXIMIZED_HORZ.id);
				    mhcons = true;
				} else if(s.equals(_NET_WM_STATE_MAXIMIZED_HORZ.id) && p.contains(_NET_WM_STATE_MAXIMIZED_VERT.id)) {
				    msg.xclient().a(2, _NET_WM_STATE_MAXIMIZED_VERT.id);
				    mvcons = true;
				}
				xlib.XSendEvent(dpy, screen.root(), false, XLib.SubstructureNotifyMask | XLib.SubstructureRedirectMask, msg);
			    }
			}
			for(Atom s : n) {
			    if(!p.contains(s)) {
				XEvent msg = xlib.XEvent().type(XLib.ClientMessage).window(id);
				msg.xclient().message_type(_NET_WM_STATE.id).format(32).l(0, 1).a(1, _NET_WM_STATE_FULLSCREEN.id).a(2, Atom.nil).l(3, 1);
				xlib.XSendEvent(dpy, screen.root(), false, XLib.SubstructureNotifyMask | XLib.SubstructureRedirectMask, msg);
			    }
			}
		    } else {
			xlib.XChangeProperty(dpy, id, _NET_WM_STATE.id, ATOM.id, XLib.PropModeReplace,
					     new Atom[] {});
		    }
		    curstate = (st.length == 0) ? Collections.emptySet() : new HashSet<>(Arrays.asList(st));
		});
	    }

	    public GLXWindow state(State st) {
		xrun(() -> {
		    switch(st) {
		    case MINIMIZED:  xlib.XIconifyWindow(dpy, id, nscreen); break;
		    case NORMAL:     wmstate(new Atom[] {}); break;
		    case MAXIMIZED:  wmstate(new Atom[] {_NET_WM_STATE_MAXIMIZED_VERT.id, _NET_WM_STATE_MAXIMIZED_HORZ.id}); break;
		    case EXCLUSIVE:  wmstate(new Atom[] {_NET_WM_STATE_FULLSCREEN.id}); break;
		    }
		});
		return(this);
	    }

	    public Coord size() {
		return(size);
	    }

	    public State state() {
		if(curstate.contains(_NET_WM_STATE_HIDDEN.id))
		    return(State.MINIMIZED);
		if(curstate.contains(_NET_WM_STATE_FULLSCREEN.id))
		    return(State.EXCLUSIVE);
		else if(curstate.contains(_NET_WM_STATE_MAXIMIZED_VERT.id) && curstate.contains(_NET_WM_STATE_MAXIMIZED_HORZ.id))
		    return(State.MAXIMIZED);
		return(State.NORMAL);
	    }

	    public boolean focused() {
		return(focused);
	    }

	    public Visibility visible() {
		switch(visibility) {
		case XLib.VisibilityUnobscured:        return(Visibility.FULL);
		case XLib.VisibilityPartiallyObscured: return(Visibility.PARTIAL);
		case XLib.VisibilityFullyObscured:     return(Visibility.NONE);
		default:                               return(Visibility.UNKNOWN);
		}
	    }

	    public void dispose() {
		try(Aliveness _ = new Aliveness()) {
		    unregister(this);
		    xrun(() -> {
			if(ic != null)
			    xlib.XDestroyIC(ic);
			xlib.XDestroyWindow(dpy, id);
		    });
		}
	    }

	    public Environment env() {
		if(renv == null) {
		    synchronized(this) {
			if(renv == null) {
			    renv = glrun(id, () -> new GLXEnvironment());
			}
		    }
		}
		return(renv);
	    }

	    private void glswap(GL gl, int ival) {
		GLException.checkfor(gl, null);
		if(ival != cursi)
		    glx.glXSwapIntervalEXT(dpy, id, cursi = ival);
		glx.glXSwapBuffers(dpy, id);
		GLException.checkfor(gl, null);
	    }

	    public void swapbuffers(Render buf, Object mode) {
		GLRender gbuf = (GLRender)buf;
		if(gbuf.env != renv)
		    throw(new IllegalArgumentException());
		if(!(mode instanceof Boolean))
		    throw(new IllegalArgumentException());
		gbuf.submit(gl-> this.glswap(gl, ((Boolean)mode) ? 1 : 0));
	    }

	    private XEvent waitfor(Predicate<XEvent> test) {
		XEvent[] ret = {null};
		Consumer<XEvent> cb = ev -> {
		    if(test.test(ev)) {
			synchronized(ret) {
			    ret[0] = ev;
			    ret.notifyAll();
			}
		    }
		};
		synchronized(listeners) {
		    listeners.add(cb);
		}
		try {
		    boolean irq = false;
		    synchronized(ret) {
			while(ret[0] == null) {
			    try {
				ret.wait();
			    } catch(InterruptedException e) {
				irq = true;
			    }
			}
		    }
		    if(irq)
			Thread.currentThread().interrupt();
		} finally {
		    synchronized(listeners) {
			listeners.remove(cb);
		    }
		}
		return(ret[0]);
	    }

	    private void configurenotify(XConfigureEvent ev) {
		size = Coord.of(ev.width(), ev.height());
	    }

	    private void propertynotify(XPropertyEvent ev) {
		if(ev.atom().equals(_NET_WM_STATE.id)) {
		    XProperty val = xlib.XGetWindowProperty(dpy, id, ev.atom(), false, Atom.nil);
		    Set<Atom> nst = new HashSet<>();
		    for(int i = 0; i < val.len; i++)
			nst.add(val.a(i));
		    if(!nst.equals(curstate)) {
			curstate = nst.isEmpty() ? Collections.emptySet() : nst;
		    }
		}
	    }

	    private final XComposeStatus compose = xlib.XComposeStatus();
	    private boolean keylookup(GLXKeyEvent out, XKeyEvent data, boolean doim) {
		byte[] cbuf = new byte[128];
		XID[] sbuf = new XID[1];
		int l;
		if(!doim || (ic == null)) {
		    l = data.lookup(cbuf, sbuf, compose);
		    out.sym = sbuf[0];
		    if(l > 0)
			out.str = new String(Utils.splice(cbuf, 0, l), ABI.C_CHARSET);
		    out.key = sym2key(data.keycode(), sbuf[0]);
		    return(true);
		} else {
		    int[] st = {0};
		    l = data.utf8lookup(ic, cbuf, sbuf, st);
		    while(st[0] == XLib.XBufferOverflow) {
			cbuf = new byte[l + 16];
			l = data.utf8lookup(ic, cbuf, sbuf, st);
		    }
		    boolean rv = false;
		    if(l > 0) {
			if(!((st[0] == XLib.XLookupChars) || (st[0] == XLib.XLookupBoth)))
			    throw(new RuntimeException("unexpected X11 key lookup status: " + st[0]));
			out.str = new String(Utils.splice(cbuf, 0, l), Utils.utf8);
			rv = true;
		    }
		    if(!sbuf[0].equals(XID.None)) {
			if(!((st[0] == XLib.XLookupKeySym) || (st[0] == XLib.XLookupBoth)))
			    throw(new RuntimeException("unexpected X11 key lookup status: " + st[0]));
			out.sym = sbuf[0];
			out.key = sym2key(data.keycode(), sbuf[0]);
			rv = true;
		    }
		    return(rv);
		}
	    }

	    private void keypress(XKeyEvent ev) {
		GLXKeyPressEvent tke = new GLXKeyPressEvent(this, ev);
		if(keylookup(tke, ev, true))
		    callback(tke);
	    }
	    private void keyrelease(XKeyEvent ev) {
		GLXKeyReleaseEvent tke = new GLXKeyReleaseEvent(this, ev);
		if(keylookup(tke, ev, false))
		    callback(tke);
	    }

	    private void clientmsg(XClientMessageEvent ev) {
		if(WM_PROTOCOLS.is(ev.message_type()) && (ev.format() == 32)) {
		    Atom proto = ev.a()[0];
		    if(WM_DELETE_WINDOW.is(proto)) {
			callback(new CloseRequest() {});
		    } else if(_NET_WM_PING.is(proto)) {
			XEvent msg = xlib.XEvent().type(XLib.ClientMessage).window(screen.root());
			msg.xclient().message_type(WM_PROTOCOLS.id).format(32).a(0, _NET_WM_PING.id).l(1, ev.l()[1]).x(2, id);
			xlib.XSendEvent(dpy, screen.root(), false, XLib.SubstructureNotifyMask | XLib.SubstructureRedirectMask, msg);
		    } else {
			Debug.dump("unknown window-manager message received: " + xlib.XGetAtomName(dpy, ev.a()[0]));
		    }
		} else {
		    Debug.dump("unknown client message received: " + xlib.XGetAtomName(dpy, ev.message_type()));
		}
	    }

	    private void event(XEvent ev) {
		for(Consumer<XEvent> cb : listeners)
		    cb.accept(ev);
		switch(ev.type()) {
		case XLib.MapNotify:
		    mapped = true;
		    break;
		case XLib.UnmapNotify:
		    mapped = false;
		    break;
		case XLib.ReparentNotify:
		    break;
		case XLib.ConfigureNotify:
		    configurenotify(ev.xconfigure());
		    break;
		case XLib.FocusIn:
		    focused = true;
		    break;
		case XLib.FocusOut:
		    focused = false;
		    break;
		case XLib.PropertyNotify:
		    if(ev.window().equals(id))
			propertynotify(ev.xproperty());
		    break;
		case XLib.VisibilityNotify:
		    visibility = ev.xvisibility().state();
		    break;
		case XLib.ClientMessage:
		    clientmsg(ev.xclient());
		    break;
		case XLib.KeyPress:
		    keypress(ev.xkey());
		    break;
		case XLib.KeyRelease:
		    keyrelease(ev.xkey());
		    break;
		default:
		    Warning.warn(String.format("unexpected event received for window %s: %d", id, ev.type()));
		}
	    }

	    private void event(XIDeviceEvent ev) {
		XIPointerInfo ptr;
		synchronized(pointers) {
		    ptr = pointers.get(ev.deviceid());
		}
		if(ptr != null) {
		    switch(ev.evtype()) {
		    case XInput.XI_ButtonPress:
			if((ev.flags() & XInput.XIPointerEmulated) == 0) {
			    int btn = ev.detail();
			    if((btn == 4) || (btn == 5)) {
				callback(new GLXMouseWheelEvent(this, ptr, ev, MouseWheelEvent.Axis.VERT, (btn == 4) ? -1 : 1));
			    } else if((btn == 6) || (btn == 7)) {
				callback(new GLXMouseWheelEvent(this, ptr, ev, MouseWheelEvent.Axis.HORIZ, (btn == 6) ? -1 : 1));
			    } else {
				callback(new GLXMouseDownEvent(this, ptr, ev));
			    }
			}
			break;
		    case XInput.XI_ButtonRelease:
			if((ev.flags() & XInput.XIPointerEmulated) == 0)
			    callback(new GLXMouseUpEvent(this, ptr, ev));
			break;
		    case XInput.XI_Motion: {
			Map<Integer, Double> vals = ev.valuators();
			for(Integer v : ev.valuators().keySet()) {
			    XIPointerInfo.Scroll scr = ptr.scroll.get(v);
			    if(scr != null) {
				double nv = vals.get(v), nvn = nv / scr.inc, lvn = scr.lastval / scr.inc;
				long nvi = (long)Math.floor(nvn), lvi = (long)Math.floor(lvn);
				double delta = nvn - lvn;
				int idelta = (int)(nvi - lvi);
				scr.lastval = nv;
				MouseWheelEvent.Axis axis = null;
				if(scr.type == XInput.XIScrollTypeVertical)
				    axis = MouseWheelEvent.Axis.VERT;
				else if(scr.type == XInput.XIScrollTypeHorizontal)
				    axis = MouseWheelEvent.Axis.HORIZ;
				if(axis != null)
				    callback(new GLXMouseWheelEvent(this, ptr, ev, axis, idelta, delta));
			    }
			}
			if((ev.flags() & XInput.XIPointerEmulated) == 0)
			    callback(new GLXMouseMoveEvent(this, ptr, ev));
			break;
		    }
		    default:
			Warning.warn(String.format("unexpected XInput event received for window %s: %d",  ev.evtype()));
			break;
		    }
		}
	    }

	    private void event(XIFocusEvent ev) {
		switch(ev.evtype()) {
		case XInput.XI_Enter:
		    xrun(() -> {
			Map<Integer, XIDeviceInfo> devs = devicemap();
			XIDeviceInfo dev = devs.get(ev.deviceid());
			if(dev == null)
			    return;
			synchronized(pointers) {
			    pointers.put(dev.deviceid(), new XIPointerInfo(dev, devs));
			}
		    });
		    break;
		default:
		    Warning.warn(String.format("unexpected XInput event received for window %s: %d",  ev.evtype()));
		    break;
		}
	    }
	}

	public Windeye window() {
	    return(new GLXWindow());
	}

	public Cursor.Caps cursorcaps() {
	    return(ccaps);
	}

	private final Map<String, XCursor> libcursors = new HashMap<>();
	private final XCursor emptycurs, defcurs = new XCursor(null, XID.None);
	private XCursor getlibcursor(String name) {
	    synchronized(libcursors) {
		XCursor ret = libcursors.get(name);
		if(ret == null) {
		    XID id = xrun(() -> xcr.XcursorLibraryLoadCursor(dpy, name));
		    if(id.equals(XID.None))
			ret = defcurs;
		    else
			ret = new XCursor(dpy, id);
		    libcursors.put(name, ret);
		}
		return(ret);
	    }
	}
	private XCursor getlibcursor(Cursor.Std id) {
	    switch(id) {
	    case DEFAULT:   return(defcurs);
	    case NONE:      return(emptycurs);
	    case POINTER:   return(getlibcursor("default"));
	    case WAIT:      return(getlibcursor("wait"));
	    case HAND:      return(getlibcursor("pointer"));
	    case MOVE:      return(getlibcursor("all-resize"));
	    case CARET:     return(getlibcursor("text"));
	    case CROSSHAIR: return(getlibcursor("crosshair"));
	    case SIZE_N:    return(getlibcursor("n-resize"));
	    case SIZE_NE:   return(getlibcursor("ne-resize"));
	    case SIZE_E:    return(getlibcursor("e-resize"));
	    case SIZE_SE:   return(getlibcursor("se-resize"));
	    case SIZE_S:    return(getlibcursor("s-resize"));
	    case SIZE_SW:   return(getlibcursor("sw-resize"));
	    case SIZE_W:    return(getlibcursor("w-resize"));
	    case SIZE_NW:   return(getlibcursor("nw-resize"));
	    }
	    return(defcurs);
	}

	public XCursor makecursor(BufferedImage img, Coord hotspot) {
	    img = PUtils.coercergba(img, false);
	    Coord sz = PUtils.imgsz(img);
	    int[] pixels = new int[sz.x * sz.y];
	    Raster imgd = img.getRaster();
	    for(int y = 0, p = 0; y < sz.y; y++) {
		for(int x = 0; x < sz.x; x++, p++) {
		    int a = imgd.getSample(x, y, 3);
		    int r = (imgd.getSample(x, y, 0) * a) / 255;
		    int g = (imgd.getSample(x, y, 1) * a) / 255;
		    int b = (imgd.getSample(x, y, 2) * a) / 255;
		    pixels[p] = (b << 0) | (g << 8) | (r << 16) | (a << 24);
		}
	    }
	    return(new XCursor(dpy, xrun(() -> xcr.XcursorImageLoadCursor(dpy, xcr.XcursorImageCreate(sz, hotspot, pixels)))));
	}

	private Set<Key.Mod> mods(int state, int key, boolean include) {
	    if(key != 0) {
		Integer mod = rmodmap.get(key);
		if(mod != null) {
		    if(include)
			state |= 1 << mod;
		    else
			state &= ~(1 << mod);
		}
	    }
	    Set<Key.Mod> ret = EnumSet.noneOf(Key.Mod.class);
	    if((state & XLib.ShiftMask) != 0)
		ret.add(Key.Mod.SHIFT);
	    if((state & XLib.ControlMask) != 0)
		ret.add(Key.Mod.CONTROL);
	    if((mod_alt > 0) && ((state & (1 << mod_alt)) != 0))
		ret.add(Key.Mod.ALT);
	    if((mod_meta > 0) && ((state & (1 << mod_meta)) != 0))
		ret.add(Key.Mod.META);
	    if((mod_super > 0) && ((state & (1 << mod_super)) != 0))
		ret.add(Key.Mod.SUPER);
	    if((mod_altgr > 0) && ((state & (1 << mod_altgr)) != 0))
		ret.add(Key.Mod.ALTGR);
	    return(ret);
	}

	public void dispose() {
	    if(!closed) {
		synchronized(this) {
		    if(closed)
			return;
		    if(ctx != null)
			glx.glXDestroyContext(dpy, ctx);
		    if(im != null)
			xlib.XCloseIM(im);
		    if(dpy != null)
			xlib.XCloseDisplay(dpy);
		    closed = true;
		}
	    }
	}

	public String description() {
	    return(String.format("X11/GLX, %s/%d %s", srvvendor, srvrelease, wmname));
	}
    }

    private static final Map<Pair<Integer, XID>, XKey> ukeys = new HashMap<>();
    private Key sym2key(int code, XID sym) {
	Key ret = stdkeys.get(sym);
	if(ret == null) {
	    synchronized(ukeys) {
		XKey k = ukeys.get(Pair.of(code, sym));
		if(k == null)
		    ukeys.put(Pair.of(code, sym), k = new XKey(xlib, code, sym));
		ret = k;
	    }
	}
	return(ret);
    }

    public static abstract class GLXKeyEvent {
	public final GLXToolkit.GLXWindow wnd;
	public final int code, state;
	public final Set<Key.Mod> mods;
	public XID sym;
	public Key key;
	public String str;

	public GLXKeyEvent(GLXToolkit.GLXWindow wnd, XKeyEvent ev, boolean include) {
	    this.wnd = wnd;
	    this.code = ev.keycode();
	    this.state = ev.state();
	    this.mods = wnd.toolkit().mods(ev.state(), ev.keycode(), include);
	}

	public String string() {return(str);}
	public Key key() {return(key);}
	public Set<Key.Mod> mods() {return(mods);}

	public String toString() {
	    return(String.format("#<%s code=%d state=%x sym=%s key=%s str=\"%s\">", getClass().getSimpleName(), code, state, sym, key, (str == null) ? "" : Utils.bprint.enc(str.getBytes(Utils.utf8))));
	}
    }
    public static class GLXKeyPressEvent extends GLXKeyEvent implements Toolkit.KeyDownEvent {
	GLXKeyPressEvent(GLXToolkit.GLXWindow wnd, XKeyEvent ev) {super(wnd, ev, true);}
    }
    public static class GLXKeyReleaseEvent extends GLXKeyEvent implements Toolkit.KeyUpEvent {
	GLXKeyReleaseEvent(GLXToolkit.GLXWindow wnd, XKeyEvent ev) {super(wnd, ev, false);}
    }

    private static Button buttonid(GLXToolkit.XIPointerInfo ptr, int xi) {
	switch(xi) {
	case 1: return(Button.Std.LEFT);
	case 2: return(Button.Std.MIDDLE);
	case 3: return(Button.Std.RIGHT);
	case 8: return(Button.Std.BACK);
	case 9: return(Button.Std.FORWARD);
	}
	if((xi <= ptr.buttons.length) && (ptr.buttons[xi - 1] != null)) {
	    String nm = ptr.buttons[xi - 1];
	    return(new Button() {
		public String id() {return(("x11:" + nm).intern());}
		public String nm() {return(nm);}
	    });
	} else {
	    return(new Button() {
		public String id() {return(("x11:" + xi).intern());}
		public String nm() {return("Button " + xi);}
	    });
	}
    }

    public static abstract class GLXMouseEvent {
	public final GLXToolkit.GLXWindow wnd;
	public final GLXToolkit.XIPointerInfo ptr;
	public final Coord wndc, rootc;
	public final Set<Button> held;
	public final Set<Key.Mod> mods;

	public GLXMouseEvent(GLXToolkit.GLXWindow wnd, GLXToolkit.XIPointerInfo ptr, XIDeviceEvent ev) {
	    this.wnd = wnd;
	    this.ptr = ptr;
	    this.wndc = Coord.of((int)ev.event_x(), (int)ev.event_y());
	    this.rootc = Coord.of((int)ev.root_x(), (int)ev.root_y());
	    this.held = new HashSet<>();
	    ev.buttons().forEach(b -> held.add(buttonid(ptr, b)));
	    this.mods = wnd.toolkit().mods(ev.mods().effective(), 0, true);
	}

	public Coord wndc() {return(wndc);}
	public Set<Button> held() {return(held);}
	public Set<Key.Mod> mods() {return(mods);}

	public String toString() {
	    return(String.format("#<%s wnd=%s root=%s held=%s mods=%s>", getClass().getSimpleName(), wndc, rootc, held, mods));
	}
    }
    public static abstract class GLXMouseButtonEvent extends GLXMouseEvent {
	public final Button button;

	public GLXMouseButtonEvent(GLXToolkit.GLXWindow wnd, GLXToolkit.XIPointerInfo ptr, XIDeviceEvent ev, boolean include) {
	    super(wnd, ptr, ev);
	    this.button = buttonid(ptr, ev.detail());
	    if(include)
		held.add(button);
	    else
		held.remove(button);
	}

	public Button button() {return(button);}

	public String toString() {
	    return(String.format("#<%s wnd=%s root=%s btn=%d held=%s mods=%s>", getClass().getSimpleName(), wndc, rootc, button, held, mods));
	}
    }
    public static class GLXMouseDownEvent extends GLXMouseButtonEvent implements Toolkit.MouseDownEvent {
	GLXMouseDownEvent(GLXToolkit.GLXWindow wnd, GLXToolkit.XIPointerInfo ptr, XIDeviceEvent ev) {super(wnd, ptr, ev, true);}
    }
    public static class GLXMouseUpEvent extends GLXMouseButtonEvent implements Toolkit.MouseUpEvent {
	GLXMouseUpEvent(GLXToolkit.GLXWindow wnd, GLXToolkit.XIPointerInfo ptr, XIDeviceEvent ev) {super(wnd, ptr, ev, false);}
    }
    public static class GLXMouseMoveEvent extends GLXMouseEvent implements Toolkit.MouseMoveEvent {
	GLXMouseMoveEvent(GLXToolkit.GLXWindow wnd, GLXToolkit.XIPointerInfo ptr, XIDeviceEvent ev) {super(wnd, ptr, ev);}
    }
    public static class GLXMouseWheelEvent extends GLXMouseEvent implements Toolkit.MouseWheelEvent {
	public final Axis axis;
	public final int amount;
	public final double sub;

	GLXMouseWheelEvent(GLXToolkit.GLXWindow wnd, GLXToolkit.XIPointerInfo ptr, XIDeviceEvent ev, Axis axis, int amount, double sub) {
	    super(wnd, ptr, ev);
	    this.axis = axis;
	    this.amount = amount;
	    this.sub = sub;
	}
	GLXMouseWheelEvent(GLXToolkit.GLXWindow wnd, GLXToolkit.XIPointerInfo ptr, XIDeviceEvent ev, Axis axis, int amount) {
	    this(wnd, ptr, ev, axis, amount, amount);
	}

	public Axis axis() {return(axis);}
	public int amount() {return(amount);}
	public double subamount() {return(sub);}
    }

    public static class XKey implements Key {
	public final int code;
	public final XID sym;
	public final String id, nm;

	private XKey(XLib xlib, int code, XID sym) {
	    this.code = code;
	    this.sym = sym;
	    this.nm = xlib.XKeysymToString(sym);
	    this.id = String.format("x11:%s", nm).intern();
	}

	public String id() {return(id);}
	public String nm() {return(nm);}

	public int hashCode() {return(Objects.hash(code, sym));}
	public boolean equals(XKey that) {return((this.code == that.code) && this.sym.equals(that.sym));}
	public boolean equals(Object x) {return((x instanceof XKey) && equals((XKey)x));}

	public String toString() {
	    return(String.format("#<x-key %d %s(%s)>", code, nm, sym));
	}
    }

    public static final Map<XID, Key> stdkeys = Utils.<XID, Key>map()
	.put(XK_Return, ENTER)             .put(XK_BackSpace, BACKSPACE)    .put(XK_Tab, TAB)
	.put(XK_Cancel, CANCEL)            .put(XK_Clear, CLEAR)            .put(XK_Pause, PAUSE)
	.put(XK_Caps_Lock, CAPSLOCK)       .put(XK_Escape, ESCAPE)          .put(XK_space, SPACE)
	.put(XK_Page_Up, PAGEUP)           .put(XK_Page_Down, PAGEDOWN)     .put(XK_End, END)
	.put(XK_Home, HOME)                .put(XK_Left, LEFT)              .put(XK_Up, UP)
	.put(XK_Right, RIGHT)              .put(XK_Down, DOWN)              .put(XK_comma, COMMA)
	.put(XK_minus, MINUS)              .put(XK_period, PERIOD)          .put(XK_slash, SLASH)
	.put(XK_semicolon, SEMICOLON)      .put(XK_equal, EQUALS)           .put(XK_bracketleft, LEFTBRACKET)
	.put(XK_bracketright, RIGHTBRACKET).put(XK_backslash, BACKSLASH)    .put(XK_Delete, DELETE)
	.put(XK_Num_Lock, NUMLOCK)         .put(XK_Scroll_Lock, SCROLLLOCK) .put(XK_Print, PRINTSCREEN)
	.put(XK_Insert, INSERT)            .put(XK_Help, HELP)              .put(XK_grave, BACKQUOTE)

	.put(XK_Shift_L, SHIFT)            .put(XK_Shift_R, SHIFT)          .put(XK_Control_L, CONTROL)
	.put(XK_Control_R, CONTROL)        .put(XK_Alt_L, ALT)              .put(XK_Alt_R, ALT)
	.put(XK_Meta_L, META)              .put(XK_Meta_R, META)            .put(XK_Super_L, WINDOWS)
	.put(XK_Super_R, WINDOWS)          .put(XK_ISO_Level3_Shift, ALTGR)

	.put(XK_0, N0)                     .put(XK_1, N1)                   .put(XK_2, N2)
	.put(XK_3, N3)                     .put(XK_4, N4)                   .put(XK_5, N5)
	.put(XK_6, N6)                     .put(XK_7, N7)                   .put(XK_8, N8)
	.put(XK_9, N9)

	.put(XK_A, A)                      .put(XK_B, B)                    .put(XK_C, C)
	.put(XK_D, D)                      .put(XK_E, E)                    .put(XK_F, F)
	.put(XK_G, G)                      .put(XK_H, H)                    .put(XK_I, I)
	.put(XK_J, J)                      .put(XK_K, K)                    .put(XK_L, L)
	.put(XK_M, M)                      .put(XK_N, N)                    .put(XK_O, O)
	.put(XK_P, P)                      .put(XK_Q, Q)                    .put(XK_R, R)
	.put(XK_S, S)                      .put(XK_T, T)                    .put(XK_U, U)
	.put(XK_V, V)                      .put(XK_W, W)                    .put(XK_X, X)
	.put(XK_Y, Y)                      .put(XK_Z, Z)
	.put(XK_a, A)                      .put(XK_b, B)                    .put(XK_c, C)
	.put(XK_d, D)                      .put(XK_e, E)                    .put(XK_f, F)
	.put(XK_g, G)                      .put(XK_h, H)                    .put(XK_i, I)
	.put(XK_j, J)                      .put(XK_k, K)                    .put(XK_l, L)
	.put(XK_m, M)                      .put(XK_n, N)                    .put(XK_o, O)
	.put(XK_p, P)                      .put(XK_q, Q)                    .put(XK_r, R)
	.put(XK_s, S)                      .put(XK_t, T)                    .put(XK_u, U)
	.put(XK_v, V)                      .put(XK_w, W)                    .put(XK_x, X)
	.put(XK_y, Y)                      .put(XK_z, Z)

	.put(XK_ampersand, AMPERSAND)      .put(XK_asterisk, ASTERISK)      .put(XK_quotedbl, DBLQUOTE)
	.put(XK_less, LT)                  .put(XK_greater, GT)             .put(XK_braceleft, LEFTBRACE)
	.put(XK_braceright, RIGHTBRACE)    .put(XK_at, AT)                  .put(XK_colon, COLON)
	.put(XK_asciicircum, CIRCUMFLEX)   .put(XK_dollar, DOLLAR)          .put(XK_EuroSign, EUROSIGN)
	.put(XK_exclam, EXCL)              .put(XK_exclamdown, INVEXCL)     .put(XK_parenleft, LEFTPAREN)
	.put(XK_parenright, RIGHTPAREN)    .put(XK_numbersign, NUMBERSIGN)  .put(XK_plus, PLUS)
	.put(XK_underscore, UNDERSCORE)    .put(XK_Menu, MENU)              .put(XK_Undo, UNDO)
	.put(XK_Redo, AGAIN)               .put(XK_Find, FIND)              .put(XK_Multi_key, COMPOSE)
	.put(XK_Begin, BEGIN)

	.put(XK_dead_grave, DEADGRAVE)            .put(XK_dead_acute, DEADACUTE)        .put(XK_dead_circumflex, DEADCIRCUMFLEX)
	.put(XK_dead_tilde, DEADTILDE)            .put(XK_dead_macron, DEADMACRON)      .put(XK_dead_breve, DEADBREVE)
	.put(XK_dead_abovedot, DEADABOVEDOT)      .put(XK_dead_diaeresis, DEADDIAERESIS).put(XK_dead_abovering, DEADABOVERING)
	.put(XK_dead_doubleacute, DEADDOUBLEACUTE).put(XK_dead_caron, DEADCARON)        .put(XK_dead_cedilla, DEADCEDILLA)
	.put(XK_dead_ogonek, DEADOGONEK)          .put(XK_dead_iota, DEADIOTA)          .put(XK_dead_voiced_sound, DEADVOICED)
	.put(XK_dead_semivoiced_sound, DEADSEMIVOICED)

	.put(XK_KP_Left, NP_LEFT)    .put(XK_KP_Up, NP_UP)           .put(XK_KP_Right, NP_RIGHT)
	.put(XK_KP_Down, NP_DOWN)    .put(XK_KP_Multiply, NP_MUL)    .put(XK_KP_Add, NP_ADD)
	.put(XK_KP_Separator, NP_SEP).put(XK_KP_Subtract, NP_SUB)    .put(XK_KP_Decimal, NP_DEC)
	.put(XK_KP_Divide, NP_DIV)
	.put(XK_KP_0, NP0)           .put(XK_KP_1, NP1)              .put(XK_KP_2, NP2)
	.put(XK_KP_3, NP3)           .put(XK_KP_4, NP4)              .put(XK_KP_5, NP5)
	.put(XK_KP_6, NP6)           .put(XK_KP_7, NP7)              .put(XK_KP_8, NP8)
	.put(XK_KP_9, NP9)
	.put(XK_KP_Home, HOME)       .put(XK_KP_Page_Up, PAGEUP)     .put(XK_KP_Page_Down, PAGEDOWN)
	.put(XK_KP_End, END)         .put(XK_KP_Insert, INSERT)      .put(XK_KP_Enter, ENTER)

	.put(XK_Henkan_Mode, CONVERT)   .put(XK_Muhenkan, NONCONVERT).put(XK_Henkan_Mode, MODECHANGE)
	.put(XK_Hiragana_Katakana, KANA).put(XK_Kanji, KANJI)        .put(XK_Katakana, KATAKANA)
	.put(XK_Hiragana, HIRAGANA)     .put(XK_Zenkaku, FULLWIDTH)  .put(XK_Hankaku, HALFWIDTH)
	.put(XK_Romaji, ROMAN)          .put(XK_Zen_Koho, ALLCAND)   .put(XK_Mae_Koho, PREVCAND)
	.put(XK_Kana_Lock, KANALOCK)

	.put(XK_F1, F1)                    .put(XK_F2, F2)                  .put(XK_F3, F3)
	.put(XK_F4, F1)                    .put(XK_F5, F2)                  .put(XK_F3, F3)
	.put(XK_F7, F1)                    .put(XK_F8, F2)                  .put(XK_F9, F9)
	.put(XK_F10, F10)                  .put(XK_F11, F11)                .put(XK_F12, F12)
	.put(XK_F13, F13)                  .put(XK_F14, F14)                .put(XK_F15, F15)
	.put(XK_F16, F16)                  .put(XK_F17, F17)                .put(XK_F18, F18)
	.put(XK_F19, F19)                  .put(XK_F20, F20)                .put(XK_F21, F21)
	.put(XK_F22, F22)                  .put(XK_F23, F23)                .put(XK_F24, F24)
	.map();
}

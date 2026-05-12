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

package haven;

import java.util.*;
import java.io.*;
import haven.render.*;
import haven.iosys.tk.*;
import java.awt.image.BufferedImage;
import haven.iosys.tk.Button;

public class Client implements Console.Directory {
    public static final Config.Variable<String> toolkit = Config.Variable.prop("haven.toolkit", null);
    public final Toolkit tk;
    public final Windeye wnd;
    private final EventQueue queue = new EventQueue(this);
    private UILoop loop;
    private Thread mt;

    public Client(Toolkit tk) {
	this.tk = tk;
	this.wnd = tk.window();
	wnd.title("Haven & Hearth");
	Coord fsz = Utils.getprefc("mainwnd/locksize", null);
	if(fsz == null)
	    wnd.sizing(new Windeye.Sizing().minsize(UI.scale(800, 600)).normsize(Utils.getprefc("mainwnd/size", UI.scale(1024, 768))));
	else
	    wnd.sizing(new Windeye.Sizing().fixsize(fsz));
	if(Utils.getprefb("mainwnd/max", false))
	    wnd.state(Windeye.State.MAXIMIZED);
	try(InputStream icon = Client.class.getResourceAsStream("icon.png")) {
	    wnd.icon(javax.imageio.ImageIO.read(icon));
	} catch(IOException e) {
	    throw(new Error(e));
	}
	wnd.add(queue);
	wnd.show(true);
    }

    public static class EventQueue implements Toolkit.EventListener {
	public final Client cl;
	private final List<Toolkit.Event> pending = new ArrayList<>();
	private Toolkit.MouseMoveEvent mousemv;

	public EventQueue(Client cl) {
	    this.cl = cl;
	}

	public void event(Toolkit.Event ev) {
	    if(ev instanceof Toolkit.CloseRequest) {
		Thread mt = cl.mt;
		if(mt != null)
		    mt.interrupt();
	    } else {
		synchronized(this) {
		    if(ev instanceof Toolkit.MouseMoveEvent) {
			mousemv = (Toolkit.MouseMoveEvent)ev;
		    } else {
			pending.add(ev);
		    }
		}
	    }
	}

	private static int buttonid(Button btn) {
	    if(btn == Button.Std.LEFT)
		return(1);
	    else if(btn == Button.Std.MIDDLE)
		return(2);
	    else if(btn == Button.Std.RIGHT)
		return(3);
	    return(0);
	}

	public void dispatch(UI ui) {
	    List<Toolkit.Event> evs;
	    Toolkit.MouseMoveEvent mousemv;
	    synchronized(this) {
		mousemv = this.mousemv;
		this.mousemv = null;
		evs = new ArrayList<>(pending);
		pending.clear();
	    }
	    if(mousemv != null) {
		ui.mousemove(mkawt(mousemv), mousemv.wndc());
	    }
	    for(Toolkit.Event ev : evs) {
		if(ev instanceof Toolkit.MouseDownEvent) {
		    Toolkit.MouseDownEvent e = (Toolkit.MouseDownEvent)ev;
		    int btn = buttonid(e.button());
		    if(btn > 0)
			ui.mousedown(mkawt(e), e.wndc(), btn);
		} else if(ev instanceof Toolkit.MouseUpEvent) {
		    Toolkit.MouseUpEvent e = (Toolkit.MouseUpEvent)ev;
		    int btn = buttonid(e.button());
		    if(btn > 0)
			ui.mouseup(mkawt(e), e.wndc(), btn);
		} else if(ev instanceof Toolkit.MouseWheelEvent) {
		    Toolkit.MouseWheelEvent e = (Toolkit.MouseWheelEvent)ev;
		    if(e.axis() == Toolkit.MouseWheelEvent.Axis.VERT)
			ui.mousewheel(mkawt(e), e.wndc(), e.amount(), e.subamount());
		} else if(ev instanceof Toolkit.KeyDownEvent) {
		    ui.keydown(mkawt((Toolkit.KeyEvent)ev));
		} else if(ev instanceof Toolkit.KeyUpEvent) {
		    ui.keyup(mkawt((Toolkit.KeyEvent)ev));
		}
	    }
	}

	/* XXX: All the following are for backward compatibility, to be removed. */
	private static final Map<Key, Integer> revawt = new HashMap<>();
	static {
	    for(Map.Entry<Integer, Key> k : JOGLToolkit.stdkeys.entrySet())
		revawt.put(k.getValue(),k.getKey());
	}

	private static final java.awt.Component awtdummy = new java.awt.Component() {};

	private static int awtmods(Collection<Key.Mod> mods) {
	    int ret = 0;
	    for(Key.Mod mod : mods) {
		switch(mod) {
		case SHIFT: ret |= java.awt.event.InputEvent.SHIFT_DOWN_MASK; break;
		case CONTROL: ret |= java.awt.event.InputEvent.CTRL_DOWN_MASK; break;
		case META: ret |= java.awt.event.InputEvent.META_DOWN_MASK; break;
		case ALT: ret |= java.awt.event.InputEvent.ALT_DOWN_MASK; break;
		}
	    }
	    return(ret);
	}

	private static class ExtKeyEvent extends java.awt.event.KeyEvent {
	    private ExtKeyEvent(int id, long time, int mods, int code, char chr, int location) {
		super(awtdummy, id, time, mods, code, chr, location);
	    }

	    public int getExtendedKeyCode() {return(getKeyCode());}
	}

	private static java.awt.event.KeyEvent mkawt(Toolkit.KeyEvent ev) {
	    int id = 0;
	    if(ev instanceof Toolkit.KeyDownEvent)
		id = java.awt.event.KeyEvent.KEY_PRESSED;
	    else if(ev instanceof Toolkit.KeyUpEvent)
		id = java.awt.event.KeyEvent.KEY_RELEASED;
	    char c = java.awt.event.KeyEvent.CHAR_UNDEFINED;
	    if(ev.string() != null)
		c = ev.string().charAt(0);
	    return(new ExtKeyEvent(id, System.currentTimeMillis(), awtmods(ev.mods()),
				   revawt.getOrDefault(ev.key(), java.awt.event.KeyEvent.VK_UNDEFINED),
				   c, java.awt.event.KeyEvent.KEY_LOCATION_UNKNOWN));
	}

	private static java.awt.event.MouseEvent mkawt(Toolkit.MouseEvent ev) {
	    int id = 0;
	    if(ev instanceof Toolkit.MouseDownEvent)
		id = java.awt.event.MouseEvent.MOUSE_PRESSED;
	    else if(ev instanceof Toolkit.MouseUpEvent)
		id = java.awt.event.MouseEvent.MOUSE_RELEASED;
	    return(new java.awt.event.MouseEvent(awtdummy, id, System.currentTimeMillis(), awtmods(ev.mods()),
						 ev.wndc().x, ev.wndc().y, 0, false,
						 (ev instanceof Toolkit.MouseButtonEvent) ? buttonid(((Toolkit.MouseButtonEvent)ev).button()) : java.awt.event.MouseEvent.NOBUTTON));
	}
    }

    public static class ClientLoop extends UILoop {
	public final Client cl;

	private ClientLoop(Client cl) {
	    super(cl.wnd);
	    this.cl = cl;
	}

	public UI newui(UI.Runner fun) {
	    UI ui = super.newui(fun);
	    ui.cons.add(cl);
	    return(ui);
	}

	protected void dispatch(UI ui) {
	    cl.queue.dispatch(ui);
	}
    }

    private UI newui(UI.Runner fun) {
	return(loop.newui(fun));
    }

    public class Main implements UI.Runner {
	public UI.Runner run(UI ui) throws InterruptedException {
	    UI.Runner fun = null;
	    while(true) {
		if(fun == null)
		    fun = new Bootstrap();
		String t= fun.title();
		if(t == null)
		    wnd.title("Haven & Hearth");
		else
		    wnd.title("Haven & Hearth \u2013 " + t);
		fun = fun.run(newui(fun));
	    }
	}
    }

    private void savewndstate() {
	switch(wnd.state()) {
	case MAXIMIZED:
	    Utils.setprefb("mainwnd/max", true);
	    break;
	case NORMAL:
	    Utils.setprefc("mainwnd/size", wnd.size());
	    Utils.setprefb("mainwnd/max", false);
	    break;
	}
    }

    public void run(UI.Runner task) {
	if(mt != null) throw(new IllegalStateException());
	mt = Thread.currentThread();
	UILoop loop = this.loop = new ClientLoop(this);
	try {
	    try {
		while(task != null)
		    task = task.run(newui(task));
	    } catch(InterruptedException e) {
	    } finally {
		newui(null);
	    }
	    savewndstate();
	} finally {
	    loop.dispose();
	    this.loop = null;
	    mt = null;
	}
    }

    public void dispose() {
	wnd.dispose();
    }

    private Windeye.State prevfsstate = Windeye.State.NORMAL;
    private Map<String, Console.Command> cmdmap = new TreeMap<String, Console.Command>();
    {
	cmdmap.put("fs", new Console.Command() {
		public void run(Console cons, String[] args) {
		    if(args.length >= 2) {
			if(Utils.parsebool(args[1])) {
			    if(wnd.state() != Windeye.State.EXCLUSIVE) {
				prevfsstate = wnd.state();
				wnd.state(Windeye.State.EXCLUSIVE);
			    }
			} else {
			    wnd.state(prevfsstate);
			}
		    }
		}
	    });
	cmdmap.put("sz", new Console.Command() {
		public void run(Console cons, String[] args) {
		    if(args.length >= 3) {
			Coord sz = Coord.of(Integer.parseInt(args[1]),
					    Integer.parseInt(args[2]));
			if((args.length >= 4) && args[3].equals("lock")) {
			    Utils.setprefc("mainwnd/locksize", sz);
			    wnd.sizing(new Windeye.Sizing().fixsize(sz));
			} else {
			    Utils.setprefc("mainwnd/locksize", null);
			    wnd.sizing(new Windeye.Sizing().minsize(UI.scale(800, 600)).normsize(sz));
			}
		    }
		}
	    });
    }
    public Map<String, Console.Command> findcmds() {
	return(cmdmap);
    }

    private static Toolkit toolkit() {
	if(toolkit.get() != null) {
	    Toolkit.Factory f = Toolkit.toolkits().get(toolkit.get());
	    if(f == null)
		throw(new RuntimeException("no such toolkit: " + toolkit.get()));
	    return(f.open());
	}
	List<Toolkit.Factory> tks = new ArrayList<>(Toolkit.toolkits().values());
	Collections.sort(tks, Comparator.comparing(Toolkit.Factory::priority).reversed());
	Collection<Throwable> errors = new ArrayList<>();
	Toolkit tk = null;
	for(Toolkit.Factory tkt : tks) {
	    try {
		return(tkt.open());
	    } catch(Unavailable e) {
		errors.add(e);
	    }
	}
	RuntimeException exc = new RuntimeException("could find no working windowing system");
	errors.forEach(exc::addSuppressed);
	throw(exc);
    }

    private static void main2(String[] args) {
	Client cl = new Client(toolkit());
	try {
	    cl.run(cl.new Main());
	} finally {
	    cl.dispose();
	}
    }

    public static void main(String[] args) {
	/* Set up the error handler as early as humanly possible. */
	ThreadGroup g = new ThreadGroup("Haven main group");
	String ed = Utils.getprop("haven.errorurl", "");
	if(ed.equals("stderr")) {
	    g = new haven.error.SimpleHandler("Haven main group", true);
	} else if(!ed.equals("")) {
	    try {
		final haven.error.ErrorHandler hg = new haven.error.ErrorHandler(new java.net.URI(ed).toURL());
		hg.sethandler(new haven.error.ErrorGui(null) {
			public void errorsent() {
			    hg.interrupt();
			}
		    });
		g = hg;
		new DeadlockWatchdog(hg).start();
	    } catch(java.net.MalformedURLException | java.net.URISyntaxException e) {
	    }
	}
	Thread main = new HackThread(g, () -> main2(args), "Haven main thread");
	main.start();
    }
}

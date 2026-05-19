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
import haven.render.*;
import haven.iosys.tk.*;
import java.awt.image.BufferedImage;
import haven.render.gl.GLEnvironment;

public abstract class UILoop implements UI.Context {
    public final Windeye wnd;
    public final Thread th;
    public Environment env;
    public UI ui;
    private final Cursor.Caps curscaps;
    private final Object uilock = new Object();
    private UI lockedui;
    private long frameno = 0;

    public UILoop(Windeye wnd) {
	this.wnd = wnd;
	this.env = wnd.env();
	this.curscaps = wnd.toolkit().cursorcaps();
	newui(null);
	this.th = new HackThread(this::run, "Haven UI thread");
	this.th.start();
    }

    public UI newui(UI.Runner fun) {
	UI prevui, newui = new UI(this, new Coord(wnd.size()), fun);
	newui.env = this.env;
	synchronized(uilock) {
	    prevui = this.ui;
	    this.ui = newui;
	    while((this.lockedui != null) && (this.lockedui == prevui)) {
		try {
		    uilock.wait();
		} catch(InterruptedException e) {
		    Thread.currentThread().interrupt();
		    break;
		}
	    }
	}
	if(prevui != null) {
	    synchronized(prevui) {
		prevui.destroy();
	    }
	}
	return(newui);
    }

    public void setmousepos(Coord c) {
	/* XXX */
    }

    /* XXX: Move to UI? */
    private Object prevtooltip = null;
    private Indir<Tex> prevtooltex = null;
    private Disposable freetooltex = null;
    private void drawtooltip(UI ui, GOut g) {
	Object tooltip;
	synchronized(ui) {
	    tooltip = ui.tooltip(ui.mc);
	}
	Indir<Tex> tt = null;
	if(Utils.eq(tooltip, prevtooltip)) {
	    tt = prevtooltex;
	} else {
	    if(freetooltex != null) {
		freetooltex.dispose();
		freetooltex = null;
	    }
	    prevtooltip = null;
	    prevtooltex = null;
	    Disposable free = null;
	    if(tooltip != null) {
		if(tooltip instanceof Text) {
		    Tex t = ((Text)tooltip).tex();
		    tt = () -> t;
		} else if(tooltip instanceof Tex) {
		    Tex t = (Tex)tooltip;
		    tt = () -> t;
		} else if(tooltip instanceof Indir<?>) {
		    @SuppressWarnings("unchecked")
			Indir<Tex> c = (Indir<Tex>)tooltip;
		    tt = c;
		} else if(tooltip instanceof String) {
		    if(((String)tooltip).length() > 0) {
			Tex r = new TexI(Text.render((String)tooltip).img, false);
			tt = () -> r;
			free = r;
		    }
		}
	    }
	    prevtooltip = tooltip;
	    prevtooltex = tt;
	    freetooltex = free;
	}
	Tex tex = (tt == null) ? null : tt.get();
	if(tex != null) {
	    Coord sz = tex.sz();
	    Coord pos = ui.mc.sub(sz).sub(curshotspot);
	    if(pos.x < 0)
		pos.x = 0;
	    if(pos.y < 0)
		pos.y = 0;
	    Coord br = pos.add(sz);
	    Coord m = UI.scale(2, 2);
	    g.chcolor(244, 247, 21, 192);
	    g.rect2(pos.sub(m).sub(1, 1), br.add(m));
	    g.chcolor(35, 35, 35, 192);
	    g.frect2(pos.sub(m), br.add(m));
	    g.chcolor();
	    g.image(tex, pos);
	}
	ui.lasttip = tooltip;
    }

    private final Map<Resource, Cursor> cursors = new WeakHashMap<>();
    private final Map<Cursor, Coord> curshotspots = new WeakHashMap<>();
    private Object lastcursor = null;
    private Coord curshotspot = Coord.z;
    private void drawcursor(UI ui, GOut g) {
	Object curs;
	synchronized(ui) {
	    curs = ui.getcurs(ui.mc);
	}
	if(curs instanceof Resource) {
	    Resource res = (Resource)curs;
	    if(curscaps == null) {
		if(!(lastcursor instanceof Resource))
		    wnd.cursor(Cursor.Std.NONE);
		curshotspot = UI.scale(res.flayer(Resource.negc).cc);
		Coord dc = ui.mc.sub(curshotspot);
		g.image(res.flayer(Resource.imgc), dc);
	    } else {
		if(curs != lastcursor) {
		    Cursor tkc = cursors.get(res);
		    if(tkc == null) {
			Coord hotspot = res.flayer(Resource.negc).cc;
			BufferedImage img = res.flayer(Resource.imgc).img;
			Coord sz = PUtils.imgsz(img);
			Coord tsz;
			if(curscaps.pref != 0) {
			    tsz = sz.mul(curscaps.pref).div(sz.max());
			} else {
			    tsz = UI.scale(sz);
			    if((tsz.x > curscaps.max) || (tsz.y > curscaps.max))
				tsz = tsz.mul(curscaps.max).div(tsz.max());
			}
			if(!Utils.eq(tsz, sz)) {
			    img = PUtils.uiscale(img, tsz);
			    hotspot = hotspot.mul(tsz).div(sz);
			}
			cursors.put(res, tkc = wnd.toolkit().makecursor(img, hotspot));
			curshotspots.put(tkc, hotspot);
		    }
		    curshotspot = curshotspots.get(tkc);
		    wnd.cursor(tkc);
		}
	    }
	} else if(curs instanceof Cursor.Std) {
	    if(curs != lastcursor)
		wnd.cursor((Cursor.Std)curs);
	} else {
	    if(curs != lastcursor)
		Warning.warn("unexpected cursor specification: %s", curs);
	}
	lastcursor = curs;
    }

    private long prevfree = 0, framealloc = 0;
    protected void statlines(Collection<String> buf, UI ui) {
	buf.add(String.format("FPS: %d (%d%% idle, latency %d)", fps, (int)(uidle * 100.0), framelag));
	Runtime rt = Runtime.getRuntime();
	long free = rt.freeMemory(), total = rt.totalMemory();
	if(free < prevfree)
	    framealloc = ((prevfree - free) + (framealloc * 19)) / 20;
	prevfree = free;
	buf.add(String.format("Mem: %,011d/%,011d/%,011d/%,011d (%,d)", free, total - free, total, rt.maxMemory(), framealloc));
	buf.add(String.format("State slots: %d", State.Slot.numslots()));
	Environment env = ui.getenv();
	if(env instanceof GLEnvironment) {
	    GLEnvironment gl = (GLEnvironment)env;
	    buf.add(String.format("GL progs: %d", gl.numprogs()));
	    buf.add(String.format("V-Mem: %s", gl.memstats()));
	}
	@SuppressWarnings("deprecation") MapView map = ui.root.findchild(MapView.class);
	if((map != null) && (map.back != null)) {
	    buf.add(String.format("Camera: %s", map.camstats()));
	    buf.add(String.format("Mapview: %s", map.stats()));
	    // buf.add(String.format("Click: Map: %s, Obj: %s", map.clmaplist.stats(), map.clobjlist.stats()));
	}
	if((ui.sess != null) && (ui.sess.conn instanceof Connection))
	    buf.add(String.format("Connection: %s", ((Connection)ui.sess.conn).stats));
	buf.add(String.format("Async: L %s, D %s", ui.loader.stats(), Defer.gstats()));
	int rqd = Resource.local().qdepth() + Resource.remote().qdepth();
	if(rqd > 0)
	    buf.add(String.format("RQ depth: %d (%d)", rqd, Resource.local().numloaded() + Resource.remote().numloaded()));
    }

    private void drawstats(UI ui, GOut g, Render buf) {
	Collection<String> lines = new ArrayList<>();
	statlines(lines, ui);
	synchronized(Debug.framestats) {
	    Debug.framestats.forEach(s -> lines.add(String.valueOf(s)));
	}
	int y = g.sz().y - UI.scale(190), dy = FastText.h;
	for(String ln : lines)
	    FastText.aprint(g, new Coord(10, y -= dy), 0, 1, ln);
    }

    private void display(UI ui, Render buf) {
	Pipe base = new BufPipe();
	base.prep(new FragColor<>(FragColor.defcolor)).prep(new DepthBuffer<>(DepthBuffer.defdepth));
	base.prep(FragColor.blend(new BlendMode()));
	Area wnd = Area.sized(ui.root.sz);
	base.prep(new States.Viewport(wnd)).prep(new Ortho2D(wnd));
	base.prep(new FrameInfo());
	buf.clear(base, FragColor.fragcol, FColor.BLACK);
	GOut g = new GOut(buf, base, wnd.sz());
	synchronized(ui) {
	    ui.draw(g);
	}
	if(UIPanel.dbtext.get())
	    drawstats(ui, g, buf);
	drawtooltip(ui, g);
	drawcursor(ui, g);
    }

    public static class Fence implements Runnable, Abortable {
	private int state = 0;

	public void run() {
	    synchronized(this) {
		state = 1;
		notifyAll();
	    }
	}

	public void abort() {
	    synchronized(this) {
		state = 2;
		notifyAll();
	    }
	}

	public boolean waitfor() throws InterruptedException {
	    synchronized(this) {
		while(state == 0)
		    wait();
		return(state == 1);
	    }
	}
    }

    protected abstract void dispatch(UI ui);

    private final double[] frames = new double[128], waited = new double[frames.length];
    private int fps, framelag;
    private double uidle;
    protected void updstats(Frame f) {
	int fi = (int)(f.frameno % frames.length);
	frames[fi] = f.ftime;
	waited[fi] = f.wtime;
	double twait = 0;
	int i = 0, ckf = fi;
	for(; i < frames.length - 1; i++) {
	    twait += waited[ckf];
	    if(f.ftime - frames[ckf] > 1)
		break;
	    ckf = (ckf - 1 + frames.length) % frames.length;
	}
	if(f.ftime > frames[ckf]) {
	    fps = (int)Math.round(i / (f.ftime - frames[ckf]));
	    uidle = twait / (f.ftime - frames[ckf]);
	}
    }

    public static class Frame {
	public final UILoop loop;
	public final long frameno;
	public final UI ui;
	public final Render out;
	public final Fence sync = new Fence();
	public Frame prev;
	public double stime, ftime, wtime;

	public Frame(UILoop loop, UI ui, Render out, Frame prev) {
	    this.loop = loop;
	    this.frameno = loop.frameno++;
	    this.ui = ui;
	    this.out = out;
	    this.prev = prev;
	    this.stime = Utils.rtime();
	}

	protected void tick() {
	    synchronized(ui) {
		loop.dispatch(ui);
		ui.mousehover(ui.mc);
		if(ui.sess != null) {
		    ui.sess.glob.ctick();
		    ui.sess.glob.gtick(out);
		}
		ui.tick();
		ui.gtick(out);
		Coord sz = loop.wnd.size();
		if(!ui.root.sz.equals(sz))
		    ui.root.resize(sz);
	    }
	}

	protected void display() {
	    loop.display(ui, out);
	}

	protected void swapbuffers() {
	    loop.wnd.swapbuffers(out);
	    out.fence(() -> loop.framelag = (int)(loop.frameno - frameno));
	}

	protected void fin() {
	    ftime = Utils.rtime();
	}

	public void run() throws InterruptedException {
	    out.fence(sync);
	    if(prev != null) {
		double then = Utils.rtime();
		prev.sync.waitfor();
		wtime += Utils.rtime() - then;
	    }

	    tick();
	    display();
	    swapbuffers();
	    fin();
	}
    }

    protected Frame frame(UI ui, Render out, Frame prev) {
	return(new Frame(this, ui, out, prev));
    }

    private void run() {
	Render buf = null;
	try {
	    Frame prevframe = null;
	    while(true) {
		Environment env = wnd.env();
		if(env != this.env) {
		    this.env = env;
		    if(ui != null)
			ui.env = env;
		}
		buf = env.render();
		try {
		    UI ui;
		    synchronized(uilock) {
			this.lockedui = ui = this.ui;
			uilock.notifyAll();
		    }
		    Debug.cycle(ui.modflags());

		    Frame curframe = frame(ui, buf, prevframe);
		    prevframe = null;
		    curframe.run();
		    env.submit(buf);

		    buf = null;
		    updstats(curframe);
		    (prevframe = curframe).prev = null;
		} finally {
		    if(buf != null)
			buf.dispose();
		}
	    }
	} catch(InterruptedException e) {
	} finally {
	    synchronized(uilock) {
		lockedui = null;
		uilock.notifyAll();
	    }
	}
    }

    public void dispose() {
	th.interrupt();
	try {
	    th.join(5000);
	} catch(InterruptedException e) {
	    Thread.currentThread().interrupt();
	}
	if(th.isAlive())
	    Warning.warn("ui thread failed to terminate");
    }
}

package haven;

import java.awt.*;

public class WindowX extends Window {
    public static final Decorator BIG = new OldSchool();
    public static final Decorator SMALL = new Slim();
    
    protected Decorator deco;
    
    public WindowX(Coord sz, String cap, boolean lg, Coord tlo, Coord rbo) {
	super(sz, cap, lg, tlo, rbo);
    }
    
    public WindowX(Coord sz, String cap, boolean lg) {
	super(sz, cap, lg);
    }
    
    public WindowX(Coord sz, String cap) {
	super(sz, cap);
    }
    
    @Override
    public void chcap(String cap) {
	if(deco != null) {
	    rcf = deco.captionFont();
	}
	super.chcap(cap);
    }
    
    public WindowX(Coord sz, String changelog, OldSchool deco) {
	this(sz, changelog);
	setDeco(deco);
    }
    
    public void setDeco(Decorator deco) {
	this.deco = deco;
	resize2(asz);
    }
    
    @Override
    protected void drawbg(GOut g) {
	if(deco != null) {
	    deco.drawbg(this, g);
	} else {
	    super.drawbg(g);
	}
    }
    
    @Override
    protected void drawframe(GOut g) {
	if(deco != null) {
	    deco.drawframe(this, g);
	} else {
	    super.drawframe(g);
	}
    }
    
    @Override
    protected void placetwdgs() {
//	if(deco != null) {
//	    
//	} else {
	super.placetwdgs();
//	}
    }
    
    @Override
    protected void resize2(Coord sz) {
	if(deco != null) {
	    deco.resize(this, sz);
	    placetwdgs();
	    for (Widget ch = child; ch != null; ch = ch.next)
		ch.presize();
	} else {
	    super.resize2(sz);
	}
    }
    
    interface Decorator {
	
	
	void drawbg(WindowX wnd, GOut g);
	
	void drawframe(WindowX wnd, GOut g);
	
	void resize(WindowX wnd, Coord sz);
	
	Text.Furnace captionFont();
    }
    
    
    public static class Slim implements Decorator {
	private static final Tex bg = Resource.loadtex("gfx/hud/wnd/bgtex");
	private static final Tex cl = Resource.loadtex("gfx/hud/wnd/cleft");
	private static final Tex cm = Resource.loadtex("gfx/hud/wnd/cmain");
	private static final Tex cr = Resource.loadtex("gfx/hud/wnd/cright");
	private static final int capo = UI.scale(2), capio = UI.scale(2);
	private static final Coord mrgn = UI.scale(3, 3);
	private static final Text.Foundry cf = new Text.Foundry(Text.serif, 12);
	
	public static final Coord tlm = UI.scale(18, 30);
	public static final Coord brm = UI.scale(13, 22);
	public static final Coord cpo = UI.rscale(36, 12);
	
	private static final IBox wbox = new IBox("gfx/hud/wnd", "tl", "tr", "bl", "br", "extvl", "extvr", "extht", "exthb") {
	    final Coord co = UI.scale(3, 3), bo = UI.scale(2, 2);
	    
	    public Coord btloff() {return (super.btloff().sub(bo));}
	    
	    public Coord ctloff() {return (super.ctloff().sub(co));}
	    
	    public Coord bisz() {return (super.bisz().sub(bo.mul(2)));}
	    
	    public Coord cisz() {return (super.cisz().sub(co.mul(2)));}
	};
	
	
	@Override
	public void drawbg(WindowX wnd, GOut g) {
//	    Coord bgc = new Coord();
//	    for (bgc.y = wnd.cptl.y; bgc.y < wnd.cptl.y + wnd.wsz.y; bgc.y += bg.sz().y) {
//		for (bgc.x = wnd.cptl.x; bgc.x < wnd.cptl.x + wnd.wsz.x; bgc.x += bg.sz().x)
//		    g.image(bg, bgc, wnd.cptl, wnd.csz);
//	    }
	    g.chcolor(new Color(255, 57, 255,255));
	    g.frect(wnd.cptl, wnd.wsz);
	    g.chcolor();
	}
	
	@Override
	public void drawframe(WindowX wnd, GOut g) {
	    wbox.draw(g, wnd.cptl, wnd.wsz);
	    if(wnd.cap != null) {
		int w = wnd.cap.sz().x;
		int y = wnd.cptl.y + capo;
		g.aimage(cl, new Coord(wnd.cptl.x , y), 0, 0.5);
		g.aimage(cm, new Coord(wnd.cptl.x + cl.sz().x, y), 0, 0.5, new Coord(w, cm.sz().y));
		g.aimage(cr, new Coord(wnd.cptl.x  + w + cl.sz().x, y), 0, 0.5);
		g.aimage(wnd.cap.tex(), new Coord(wnd.cptl.x + cl.sz().x, y - capo - capio),0, 0.5);
	    }
	}
	
	@Override
	public void resize(WindowX wnd, Coord sz) {
	    wnd.asz = sz;
	    wnd.csz = wnd.asz.add(mrgn.mul(2));
	    wnd.wsz = wnd.csz.add(wbox.bisz()).add(0, cm.sz().y);
	    wnd.cptl = new Coord(wnd.tlo.x, Math.max(wnd.tlo.y, capo)+cm.sz().y/2);
	    wnd.sz = wnd.wsz.add(wnd.cptl).add(wnd.rbo);
	    wnd.ctl = wnd.cptl.add(wbox.btloff()).add(0, cm.sz().y / 2);
	    wnd.atl = wnd.ctl.add(mrgn);
	    wnd.cbtn.c = wnd.xlate(new Coord(wnd.ctl.x + wnd.csz.x - wnd.cbtn.sz.x, wnd.ctl.y).add(2, -2), false);
	}
	
	@Override
	public Text.Furnace captionFont() {
	    return Slim.cf;
	}
    }
    
    private static class OldSchool implements Decorator {
	
	@Override
	public void drawbg(WindowX wnd, GOut g) {
	    Coord bgc = new Coord();
	    Coord cbr = wnd.ctl.add(wnd.csz);
	    for (bgc.y = wnd.ctl.y; bgc.y < cbr.y; bgc.y += bg.sz().y) {
		for (bgc.x = wnd.ctl.x; bgc.x < cbr.x; bgc.x += bg.sz().x)
		    g.image(bg, bgc, wnd.ctl, cbr);
	    }
	    bgc.x = wnd.ctl.x;
	    for (bgc.y = wnd.ctl.y; bgc.y < cbr.y; bgc.y += bgl.sz().y)
		g.image(bgl, bgc, wnd.ctl, cbr);
	    bgc.x = cbr.x - bgr.sz().x;
	    for (bgc.y = wnd.ctl.y; bgc.y < cbr.y; bgc.y += bgr.sz().y)
		g.image(bgr, bgc, wnd.ctl, cbr);
	}
	
	@Override
	public void drawframe(WindowX wnd, GOut g) {
	    Coord mdo, cbr;
	    g.image(cl, wnd.tlo);
	    mdo = wnd.tlo.add(cl.sz().x, 0);
	    cbr = mdo.add(wnd.cmw, cm.sz().y);
	    for (int x = 0; x < wnd.cmw; x += cm.sz().x)
		g.image(cm, mdo.add(x, 0), Coord.z, cbr);
	    g.image(cr, wnd.tlo.add(cl.sz().x + wnd.cmw, 0));
	    g.image(wnd.cap.tex(), wnd.tlo.add(cpo));
	    mdo = wnd.tlo.add(cl.sz().x + wnd.cmw + cr.sz().x, 0);
	    cbr = wnd.tlo.add(wnd.wsz.add(-tr.sz().x, tm.sz().y));
	    for (; mdo.x < cbr.x; mdo.x += tm.sz().x)
		g.image(tm, mdo, Coord.z, cbr);
	    g.image(tr, wnd.tlo.add(wnd.wsz.x - tr.sz().x, 0));
	    
	    mdo = wnd.tlo.add(0, cl.sz().y);
	    cbr = wnd.tlo.add(lm.sz().x, wnd.wsz.y - bl.sz().y);
	    if(cbr.y - mdo.y >= lb.sz().y) {
		cbr.y -= lb.sz().y;
		g.image(lb, new Coord(wnd.tlo.x, cbr.y));
	    }
	    for (; mdo.y < cbr.y; mdo.y += lm.sz().y)
		g.image(lm, mdo, Coord.z, cbr);
	    
	    mdo = wnd.tlo.add(wnd.wsz.x - rm.sz().x, tr.sz().y);
	    cbr = wnd.tlo.add(wnd.wsz.x, wnd.wsz.y - br.sz().y);
	    for (; mdo.y < cbr.y; mdo.y += rm.sz().y)
		g.image(rm, mdo, Coord.z, cbr);
	    
	    g.image(bl, wnd.tlo.add(0, wnd.wsz.y - bl.sz().y));
	    mdo = wnd.tlo.add(bl.sz().x, wnd.wsz.y - bm.sz().y);
	    cbr = wnd.tlo.add(wnd.wsz.x - br.sz().x, wnd.wsz.y);
	    for (; mdo.x < cbr.x; mdo.x += bm.sz().x)
		g.image(bm, mdo, Coord.z, cbr);
	    g.image(br, wnd.tlo.add(wnd.wsz.sub(br.sz())));
	}
	
	@Override
	public void resize(WindowX wnd, Coord sz) {
	    wnd.asz = sz;
	    wnd.csz = wnd.asz.add(wnd.mrgn.mul(2));
	    wnd.wsz = wnd.csz.add(tlm).add(brm);
	    wnd.sz = wnd.wsz.add(wnd.tlo).add(wnd.rbo);
	    wnd.ctl = wnd.tlo.add(tlm);
	    wnd.atl = wnd.ctl.add(wnd.mrgn);
	    wnd.cmw = (wnd.cap == null) ? 0 : wnd.cap.sz().x;
	    wnd.cmw = Math.max(wnd.cmw, wnd.wsz.x / 4);
	    wnd.cptl = new Coord(wnd.ctl.x, wnd.tlo.y);
	    wnd.cpsz = wnd.tlo.add(cpo.x + wnd.cmw, cm.sz().y).sub(wnd.cptl);
	    wnd.cmw = wnd.cmw - (cl.sz().x - cpo.x) - UI.scale(5);
	    wnd.cbtn.c = wnd.xlate(wnd.tlo.add(wnd.wsz.x - wnd.cbtn.sz.x, 0), false);
	}
	
	@Override
	public Text.Furnace captionFont() {
	    return Window.cf;
	}
    }
}

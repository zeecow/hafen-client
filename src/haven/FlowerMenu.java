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

import java.awt.*;

import static java.lang.Math.PI;

public class FlowerMenu extends Widget {
    public static final Color pink = new Color(255, 0, 128);
    public static final Color ptc = Color.YELLOW;
    public static final Text.Foundry ptf = new Text.Foundry(Text.dfont, 12);
    public static final IBox pbox = Window.wbox;
    public static final Tex pbg = Window.bg;
    public static final int ph = UI.scale(30), ppl = 8;
    public Petal[] opts;
    private UI.Grab mg, kg;
	Petal autochoose;

    @RName("sm")
    public static class $_ implements Factory {
	public Widget create(UI ui, Object[] args) {
	    String[] opts = new String[args.length];
	    for(int i = 0; i < args.length; i++)
		opts[i] = (String)args[i];
	    return(new FlowerMenu(opts));
	}
    }

    public class Petal extends Widget {
	public String name;
	public double ta, tr;
	public int num;
	private Text text;
	private double a = 1;

	public Petal(String name) {
	    super(Coord.z);
	    this.name = name;
	    text = ptf.render(name, ptc);
	    resize(text.sz().x + UI.scale(25), ph);
	}

	public void move(Coord c) {
	    this.c = c.sub(sz.div(2));
	}

	public void move(double a, double r) {
	    move(Coord.sc(a, r));
	}

	public void draw(GOut g) {
	    g.chcolor(new Color(255, 255, 255, (int)(255 * a)));
	    g.image(pbg, new Coord(3, 3), new Coord(3, 3), sz.add(new Coord(-6, -6)), UI.scale(pbg.sz()));
	    pbox.draw(g, Coord.z, sz);
	    g.image(text.tex(), sz.div(2).sub(text.sz().div(2)));
	}

	/*public boolean mousedown(MouseDownEvent ev) {
		if (ev.b==2 && ZeeManagerItems.isFlowerMenuFromWItem()){
			ZeeManagerItems.showWindowClickAllItemPetals(this.name);
			return false;
		}
	    choose(this);
		ZeeManagerStockpile.checkChoosenPetal(this.name);
		ZeeQuickOptionsWindow.updatePetalWidget(this.name);
		ZeeManagerGobs.labelHarvestedPlant(this.name);
	    return(true);
	}*/

	@Override
	public boolean mousedown(MouseDownEvent ev) {
		return true;
	}

	@Override
	public boolean mouseup(MouseUpEvent ev) {
		// avoid clicking during menu animation
		if (ZeeThread.now() - ZeeManagerGobs.lastClickMouseDownMs > 500) { //500ms
			choose(this);
			ZeeManagerStockpile.checkChoosenPetal(this.name);
			ZeeQuickOptionsWindow.updatePetalWidget(this.name);
			ZeeManagerGobs.labelHarvestedPlant(this.name);
			return true;
		}
		return super.mouseup(ev);
	}

	public Area ta(Coord tc) {
	    return(Area.sized(tc.sub(sz.div(2)), sz));
	}

	public Area ta(double a, double r) {
	    return(ta(Coord.sc(a, r)));
	}
    }

    private static double nxf(double a) {
	return(-1.8633 * a * a + 2.8633 * a);
    }

    public class Opening extends NormAnim {
	Opening() {super(0.25);}
	
	public void ntick(double s) {
	    double ival = 0.8;
	    double off = (opts.length == 1) ? 0.0 : ((1.0 - ival) / (opts.length - 1));
	    for(int i = 0; i < opts.length; i++) {
		Petal p = opts[i];
		double a = Utils.clip((s - (off * i)) * (1.0 / ival), 0, 1);
		double b = nxf(a);
		p.move(p.ta + ((1 - b) * PI), p.tr * b);
		p.a = a;
	    }
	}
    }

    public class Chosen extends NormAnim {
	Petal chosen;
		
	Chosen(Petal c) {
	    super(0.75);
	    chosen = c;
	}
		
	public void ntick(double s) {
	    double ival = 0.8;
	    double off = ((1.0 - ival) / (opts.length - 1));
	    for(int i = 0; i < opts.length; i++) {
		Petal p = opts[i];
		if(p == chosen) {
		    if(s > 0.6) {
			p.a = 1 - ((s - 0.6) / 0.4);
		    } else if(s < 0.3) {
			double a = nxf(s / 0.3);
			p.move(p.ta, p.tr * (1 - a));
		    }
		} else {
		    if(s > 0.3) {
			p.a = 0;
		    } else {
			double a = s / 0.3;
			a = Utils.clip((a - (off * i)) * (1.0 / ival), 0, 1);
			p.a = 1 - a;
		    }
		}
	    }
	    if(s == 1.0)
		ui.destroy(FlowerMenu.this);
	}
    }

    public class Cancel extends NormAnim {
	Cancel() {super(0.25);}

	public void ntick(double s) {
	    double ival = 0.8;
	    double off = (opts.length == 1) ? 0.0 : ((1.0 - ival) / (opts.length - 1));
	    for(int i = 0; i < opts.length; i++) {
		Petal p = opts[i];
		double a = Utils.clip((s - (off * i)) * (1.0 / ival), 0, 1);
		double b = 1.0 - nxf(1.0 - a);
		p.move(p.ta + (b * PI), p.tr * (1 - b));
		p.a = 1 - a;
	    }
	    if(s == 1.0)
		ui.destroy(FlowerMenu.this);
	}
    }

    private void organize(Petal[] opts) {
	Area bounds = parent.area().xl(c.inv());
	int l = 1, p = 0, i = 0, mp = 0, ml = 1, t = 0, tt = -1;
	boolean muri = false;
	while(i < opts.length) {
	    place: {
		double ta = (PI / 2) - (p * (2 * PI / (l * ppl)));
		double tr = UI.scale(75) + (UI.scale(50) * (l - 1));
		if(!muri && !bounds.contains(opts[i].ta(ta, tr))) {
		    if(tt < 0) {
			tt = ppl * l;
			t = 1;
			mp = p;
			ml = l;
		    } else if(++t >= tt) {
			muri = true;
			p = mp;
			l = ml;
			continue;
		    }
		    break place;
		}
		tt = -1;
		opts[i].ta = ta;
		opts[i].tr = tr;
		i++;
	    }
	    if(++p >= (ppl * l)) {
		l++;
		p = 0;
	    }
	}
    }

    public FlowerMenu(String... options) {
	super(Coord.z);
	opts = new Petal[options.length];
	for(int i = 0; i < options.length; i++) {
	    add(opts[i] = new Petal(options[i]));
	    opts[i].num = i;
	}
	autoClickPetal();
    }

    protected void added() {
	if(c.equals(-1, -1))
	    c = parent.ui.lcc;
	mg = ui.grabmouse(this);
	kg = ui.grabkeys(this);
	organize(opts);
	new Opening().ntick(0);
    }

    /*public boolean mousedown(MouseDownEvent ev) {
	if(!anims.isEmpty())
	    return(true);
	if(!ev.propagate(this))
	    choose(null);
	return(true);
    }*/

	@Override
	public boolean mousedown(MouseDownEvent ev) {
		if(anims.isEmpty())
			return(true);
		return false;
	}

	public boolean mouseup(MouseUpEvent ev) {
		if(!anims.isEmpty())
			return(true);
		if(!ev.propagate(this))
			choose(null);
		return(super.mouseup(ev));
	}

	public void uimsg(String msg, Object... args) {
	if(msg == "cancel") {
	    new Cancel();
	    mg.remove();
	    kg.remove();
	} else if(msg == "act") {
	    new Chosen(opts[Utils.iv(args[0])]);
	    mg.remove();
	    kg.remove();
	}
    }

	final Object flag = new Object();
    public void draw(GOut g) {
		super.draw(g, false);
		synchronized (flag){
			if (autochoose != null) {
				choose(autochoose);
				ZeeManagerGobs.labelHarvestedPlant(autochoose.name);
				autochoose = null;
			}
		}
    }

    public boolean keydown(KeyDownEvent ev) {
	if((ev.c >= '0') && (ev.c <= '9')) {
	    int opt = (ev.c == '0') ? 9 : (ev.c - '1');
	    if(opt < opts.length) {
		choose(opts[opt]);
		ZeeManagerStockpile.checkChoosenPetal(opts[opt].name);
		kg.remove();
	    }
	    return(true);
	} else if(key_esc.match(ev)) {
	    choose(null);
	    kg.remove();
	    return(true);
	}
	return(super.keydown(ev));
    }

    public void choose(Petal option) {
	if(option == null) {
	    wdgmsg("cl", -1);
	} else {
		if (!ZeeConfig.isPetalConfirmed(option.name)){
			return;
		}
	    wdgmsg("cl", option.num, ui.modflags());
	}
    }

	@Override
	protected void attach(UI ui) {
		super.attach(ui);
	}

	@Override
	public void tick(double dt) {
		super.tick(dt);
	}

	//auto click the first match
	private void autoClickPetal(){
		if (!ZeeQuickOptionsWindow.autoPetalName.isBlank()) {
			for (int i = 0; i < opts.length; i++) {
				if (ZeeQuickOptionsWindow.autoPetalName.contentEquals(opts[i].text.text)) {
					autochoose = opts[i];
					return;
				}
			}
		}
		if(ZeeConfig.butcherMode) {
			String[] list = ZeeConfig.butcherAutoList.split(",");
			for (int i = 0; i < opts.length; i++) {// for each menu option
				for (int j = 0; j < list.length; j++) {// compare each name in saved list
					if (list[j].contentEquals(opts[i].text.text)) {
						autochoose = opts[i];
						return;
					}
				}
			}
		}
    	if(ZeeConfig.autoClickMenuOption && !ZeeConfig.isHoldingSpecialKey() && ZeeManagerGobs.isMouseUp()) {
			String[] list = ZeeConfig.autoClickMenuOptionList.split(",");
			for (int i = 0; i < opts.length; i++) {// for each menu option
				for (int j = 0; j < list.length; j++) {// compare each name in saved list
					if (list[j].contentEquals(opts[i].text.text)) {
						autochoose = opts[i];
						return;
					}
				}
			}
		}
	}

	boolean hasPetal(String name){
		if (opts==null || opts.length==0)
			return false;
		for (Petal petal : opts) {
			if (petal.name.contentEquals(name))
				return true;
		}
		return false;
	}
}

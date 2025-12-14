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
import static haven.Inventory.invsq;

public class Equipory extends Widget implements DTarget {
	WItem leftHand, rightHand;
    private static final Resource.Image bgi = Resource.loadrimg("gfx/hud/equip/bg");
    private static final int yo = Inventory.sqsz.y, sh = 11;
    private static final Tex bg = new TexI(PUtils.uiscale(bgi.img, Coord.of((sh * yo * bgi.sz.x) / bgi.sz.y, sh * yo)));
    private static int rx = getRx();
    public static final Coord bgc = new Coord(invsq.sz().x, 0);
    public static Coord[] ecoords = getEcoords();
    public static final Tex[] ebgs = new Tex[ecoords.length];
    public static final Text[] etts = new Text[ecoords.length];
    static Coord isz;
    static {
	isz = new Coord();
	for(Coord ec : ecoords) {
	    if(ec.x + invsq.sz().x > isz.x)
		isz.x = ec.x + invsq.sz().x;
	    if(ec.y + invsq.sz().y > isz.y)
		isz.y = ec.y + invsq.sz().y;
	}
	for(int i = 0; i < ebgs.length; i++) {
	    Resource bgres = Resource.local().loadwait("gfx/hud/equip/ep" + i);
	    Resource.Image img = bgres.layer(Resource.imgc);
	    if(img != null) {
		ebgs[i] = img.tex();
		etts[i] = Text.render(bgres.flayer(Resource.tooltip).t);
	    }
	}
    }
    Map<GItem, Collection<WItem>> wmap = new HashMap<>();
    private final Avaview ava;

    @RName("epry")
    public static class $_ implements Factory {
	public Widget create(UI ui, Object[] args) {
	    long gobid;
	    if(args.length < 1)
		gobid = -2;
	    else if(args[0] == null)
		gobid = -1;
	    else
		gobid = Utils.uiv(args[0]);
	    return(new Equipory(gobid));
	}
    }

    protected void added() {
	if(ava.avagob == -2)
	    ava.avagob = getparent(GameUI.class).plid;
	if(ZeeConfig.equiporyCompact)
		ava.hide();
	super.added();
	// btn toggel size
	Window win = (Window) parent;
	Window.DefaultDeco deco = (Window.DefaultDeco) win.deco;
	deco.add(
		new ZeeWindow.ZeeButton(15, ZeeWindow.ZeeButton.TEXT_TOGGLE_EQUIPORY_SIZE,"compact/expand"),
		5,	28
	);
    }

    public Equipory(long gobid) {
	super(isz);
	ava = add(new Avaview(bg.sz(), gobid, "equcam") {
		public boolean mousedown(MouseDownEvent ev) {
		    return(false);
		}

		public void draw(GOut g) {
		    g.image(bg, Coord.z);
		    super.draw(g);
		}

		{
		    basic.add(new Outlines(false));
		}

		final FColor cc = new FColor(0, 0, 0, 0);
		protected FColor clearcolor() {return(cc);}
	    }, bgc);
    }

    public static interface SlotInfo {
	public int slots();
    }

    public void addchild(Widget child, Object... args) {
	if(child instanceof GItem) {
	    add(child);
	    GItem g = (GItem)child;
	    ArrayList<WItem> v = new ArrayList<>();
	    for(int i = 0; i < args.length; i++) {
		int ep = Utils.iv(args[i]);
		WItem w = null;
		if(ep < ecoords.length)
		    v.add(add(w=new WItem(g), ecoords[ep].add(1, 1)));
			if(ep==6)
				leftHand = w;
			else if(ep==7)
				rightHand = w;
	    }
	    v.trimToSize();
	    wmap.put(g, v);
	} else {
	    super.addchild(child, args);
	}
    }

    public void cdestroy(Widget w) {
	super.cdestroy(w);
	if(w instanceof GItem) {
	    GItem i = (GItem)w;
		if(ZeeManagerItems.isTwoHandedItem(i.getres().name)) {
			leftHand = rightHand = null;
		}if(leftHand!=null && w.equals(leftHand.item)) {
			leftHand = null;
		}else if(rightHand!=null && w.equals(rightHand.item)) {
			rightHand = null;
		}
	    for(WItem v : wmap.remove(i))
		ui.destroy(v);
	}
    }

    public void uimsg(String msg, Object... args) {
	if(msg == "pop") {
	    ava.avadesc = Composited.Desc.decode(ui.sess, args);
	} else {
	    super.uimsg(msg, args);
	}
    }

    public int epat(Coord c) {
	for(int i = 0; i < ecoords.length; i++) {
	    if(c.isect(ecoords[i], invsq.sz()))
		return(i);
	}
	return(-1);
    }

    public boolean drop(Coord cc, Coord ul) {
	wdgmsg("drop", epat(cc));
	return(true);
    }

    public void drawslots(GOut g) {
	int slots = 0;
	GameUI gui = getparent(GameUI.class);
	if((gui != null) && (gui.vhand != null)) {
	    try {
		SlotInfo si = ItemInfo.find(SlotInfo.class, gui.vhand.item.info());
		if(si != null)
		    slots = si.slots();
	    } catch(Loading l) {
	    }
	}
	for(int i = 0; i < ecoords.length; i++) {
	    if((slots & (1 << i)) != 0) {
		g.chcolor(255, 255, 0, 64);
		g.frect(ecoords[i].add(1, 1), invsq.sz().sub(2, 2));
		g.chcolor();
	    }
	    g.image(invsq, ecoords[i]);
	    if(ebgs[i] != null)
		g.image(ebgs[i], ecoords[i]);
	}
    }

    public Object tooltip(Coord c, Widget prev) {
	int sl = epat(c);
	if(sl >= 0)
	    return(etts[sl]);
	return(null);
    }

    public void draw(GOut g) {
	drawslots(g);
	super.draw(g);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }


	public void dropItemByNameContains(String nameContains) {
		WItem[] equips = this.children(WItem.class).toArray(new WItem[0]);
		for (int i = 0; i < equips.length; i++) {
			if (equips[i].item.res.get().name.contains(nameContains)) {
				equips[i].item.wdgmsg("drop", Coord.z);
				break;
			}
		}
	}

	public void dropAllItemsByNameContains(String nameContains) {
		WItem[] equips = this.children(WItem.class).toArray(new WItem[0]);
		for (int i = 0; i < equips.length; i++) {
			if (equips[i].item.res.get().name.contains(nameContains)) {
				equips[i].item.wdgmsg("drop", Coord.z);
			}
		}
	}

	private static int getRx() {
		return invsq.sz().x + (ZeeConfig.equiporyCompact ? invsq.sz().x : bg.sz().x);
	}

	private static Coord[] getEcoords() {
		return new Coord[]{
                new Coord( 0,  0 * yo),
                new Coord( 0,  1 * yo),
                new Coord( 0,  3 * yo),
                new Coord(rx,  3 * yo),
                new Coord( 0,  4 * yo),
                new Coord(rx,  4 * yo),
                new Coord( 0,  5 * yo),
                new Coord(rx,  5 * yo),
                new Coord( 0,  6 * yo),
                new Coord(rx,  6 * yo),
                new Coord( 0,  7 * yo),
                new Coord(rx,  7 * yo),
                new Coord( 0,  9 * yo),
                new Coord(rx,  9 * yo),
                new Coord( 0, 10 * yo),
                new Coord(rx, 10 * yo),
                new Coord(invsq.sz().x, 0 * yo),
                new Coord(rx,  1 * yo),
                new Coord(rx,  2 * yo),
                new Coord( 0,  8 * yo),
                new Coord(rx,  8 * yo),
                new Coord(rx,  0 * yo),
                new Coord( 0,  2 * yo),
        };
	}

	public void toggleSize() {
		ZeeConfig.equiporyCompact = !ZeeConfig.equiporyCompact;
		Utils.setprefb("equiporyCompact",ZeeConfig.equiporyCompact);
		// hide
		if (ZeeConfig.equiporyCompact){
			ava.hide();
			resizew(sz.x - bg.sz().x + invsq.sz().x);
			//adjust right side item's pos
			for (Map.Entry<GItem, Collection<WItem>> col : wmap.entrySet()) {
				for (WItem wItem : col.getValue()) {
					if (wItem.c.x > bg.sz().x)
						wItem.c.x -= bg.sz().x - invsq.sz().x;
				}
			}
		}
		// show
		else{
			ava.show();
			resizew(sz.x + bg.sz().x - invsq.sz().x);
			//adjust right side item's pos
			for (Map.Entry<GItem, Collection<WItem>> col : wmap.entrySet()) {
				for (WItem wItem : col.getValue()) {
					if (wItem.c.x != 1)
						wItem.c.x += bg.sz().x - invsq.sz().x;
				}
			}
		}
		Equipory.rx = getRx();
		Equipory.ecoords = getEcoords();
		isz = new Coord();
		for(Coord ec : ecoords) {
			if(ec.x + invsq.sz().x > isz.x)
				isz.x = ec.x + invsq.sz().x;
			if(ec.y + invsq.sz().y > isz.y)
				isz.y = ec.y + invsq.sz().y;
		}
		parent.pack();
	}
}

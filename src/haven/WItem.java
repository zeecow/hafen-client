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

import haven.ItemInfo.AttrCache;
import haven.render.Pipe;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static haven.Inventory.sqsz;

public class WItem extends Widget implements DTarget {
    public static final Resource missing = Resource.local().loadwait("gfx/invobjs/missing");
    public final GItem item;
    private Resource cspr = null;
    private Message csdt = Message.nil;

    public WItem(GItem item) {
	super(sqsz);
	this.item = item;
    }

    public void drawmain(GOut g, GSprite spr) {
	spr.draw(g);
    }

    public class ItemTip implements Indir<Tex>, ItemInfo.InfoTip {
	private final List<ItemInfo> info;
	private final TexI tex;

	public ItemTip(List<ItemInfo> info, BufferedImage img) {
	    this.info = info;
	    if(img == null)
		throw(new Loading());
	    tex = new TexI(img);
	}

	public GItem item() {return(item);}
	public List<ItemInfo> info() {return(info);}
	public Tex get() {return(tex);}
    }

    public class ShortTip extends ItemTip {
	public ShortTip(List<ItemInfo> info) {super(info, ItemInfo.shorttip(info));}
    }

    public class LongTip extends ItemTip {
	public LongTip(List<ItemInfo> info) {super(info, ItemInfo.longtip(info));}
    }

    private double hoverstart;
    private ItemTip shorttip = null, longtip = null;
    private List<ItemInfo> ttinfo = null;
    public Object tooltip(Coord c, Widget prev) {
	if (this instanceof ItemDrag)//no tooltip for holding item
		return null;
	double now = Utils.rtime();
	if(prev == this) {
	} else if(prev instanceof WItem) {
	    double ps = ((WItem)prev).hoverstart;
	    if(now - ps < 1.0)
		hoverstart = now;
	    else
		hoverstart = ps;
	} else {
	    hoverstart = now;
	}
	try {
	    List<ItemInfo> info = item.info();
	    if(info.size() < 1)
		return(null);
	    if(info != ttinfo) {
		shorttip = longtip = null;
		ttinfo = info;
	    }
		// shift required for stack longtt, and for skipping shorttip delay
	    if(!ui.modshift && item.isStackByContent() || now - hoverstart < 1.0 ){
		if(shorttip == null)
		    shorttip = new ShortTip(info);
		return(shorttip);
	    } else {
		if(longtip == null) {

			// collect current iteminfo class names
			List<String> infoClasses = new ArrayList<>();
			for (ItemInfo itemInfo : new ArrayList<>(info)) {
				if (infoClasses.contains(itemInfo.getClass().getName()))
					continue;//unique class names
				infoClasses.add(itemInfo.getClass().getName());
			}

			// add blank line
			info.add(new ItemInfo.AdHoc(this.item, ""));

			// add res name
			info.add(new ItemInfo.AdHoc(this.item, this.item.resource().name));

			// add meter % maybe
			Double meter = (item.meter > 0) ? Double.valueOf(item.meter / 100.0) : itemmeter.get();
			if ((meter != null) && (meter > 0)) {
				info.add(new ItemInfo.AdHoc(this.item, (int) (meter * 100) + "% done"));
			}

			// add collected info class names
			if (!infoClasses.isEmpty()){
				info.add(new ItemInfo.AdHoc(this.item, "ItemInfo"));
				for (String infoClass : infoClasses) {
					info.add(new ItemInfo.AdHoc(this.item, "    "+infoClass));
				}
			}

			longtip = new LongTip(info);
		}
		return(longtip);
	    }
	} catch(Loading e) {
	    return("...");
	}
    }

    private List<ItemInfo> info() {return(item.info());}
    public final AttrCache<Pipe.Op> rstate = new AttrCache<>(this::info, info -> {
	    ArrayList<GItem.RStateInfo> ols = new ArrayList<>();
	    for(ItemInfo inf : info) {
		if(inf instanceof GItem.RStateInfo)
		    ols.add((GItem.RStateInfo)inf);
	    }
	    if(ols.size() == 0)
		return(() -> null);
	    if(ols.size() == 1) {
		Pipe.Op op = ols.get(0).rstate();
		return(() -> op);
	    }
	    Pipe.Op[] ops = new Pipe.Op[ols.size()];
	    for(int i = 0; i < ops.length; i++)
		ops[i] = ols.get(0).rstate();
	    Pipe.Op cmp = Pipe.Op.compose(ops);
	    return(() -> cmp);
	});
    public final AttrCache<GItem.InfoOverlay<?>[]> itemols = new AttrCache<>(this::info, info -> {
	    ArrayList<GItem.InfoOverlay<?>> buf = new ArrayList<>();
	    for(ItemInfo inf : info) {
		if(inf instanceof GItem.OverlayInfo)
		    buf.add(GItem.InfoOverlay.create((GItem.OverlayInfo<?>)inf));
	    }
	    GItem.InfoOverlay<?>[] ret = buf.toArray(new GItem.InfoOverlay<?>[0]);
	    return(() -> ret);
	});
    public final AttrCache<Double> itemmeter = new AttrCache<>(this::info, AttrCache.map1(GItem.MeterInfo.class, minf -> minf::meter));

    private Widget contparent() {
	/* XXX: This is a bit weird, but I'm not sure what the alternative is... */
	Widget cont = getparent(GameUI.class);
	return((cont == null) ? cont = ui.root : cont);
    }

    private GSprite lspr = null;
    private Widget lcont = null;
    public void tick(double dt) {
	/* XXX: This is ugly and there should be a better way to
	 * ensure the resizing happens as it should, but I can't think
	 * of one yet. */
	GSprite spr = item.spr();
	if((spr != null) && (spr != lspr)) {
	    Coord sz = new Coord(spr.sz());
	    if((sz.x % sqsz.x) != 0)
		sz.x = sqsz.x * ((sz.x / sqsz.x) + 1);
	    if((sz.y % sqsz.y) != 0)
		sz.y = sqsz.y * ((sz.y / sqsz.y) + 1);
	    resize(sz);
	    lspr = spr;
	}
    }

    public void draw(GOut g) {
	GSprite spr = item.spr();
	if(spr != null) {
	    Coord sz = spr.sz();
	    g.defstate();
	    if(rstate.get() != null)
		g.usestate(rstate.get());
	    drawmain(g, spr);
	    g.defstate();
	    GItem.InfoOverlay<?>[] ols = itemols.get();
	    if(ols != null) {
		for(GItem.InfoOverlay<?> ol : ols)
		    ol.draw(g);
	    }
	    Double meter = (item.meter > 0) ? Double.valueOf(item.meter / 100.0) : itemmeter.get();
	    if((meter != null) && (meter > 0)) {
		g.chcolor(255, 255, 255, 64);
		Coord half = sz.div(2);
		g.prect(half, half.inv(), half, meter * Math.PI * 2);
		g.chcolor();
		// witem percentage
		g.image(
			GItem.NumberInfo.font.render((int) (meter * 100) +"%",Color.lightGray).tex(),
			Coord.of(-1,half.y+6)
		);
	    }
	} else {
	    g.image(missing.layer(Resource.imgc).tex(), Coord.z, sz);
	}
    }

    public boolean mousedown(MouseDownEvent ev) {
	// middle-click item starts equipManager
	if(ev.b == 2) {
		ZeeManagerItems.clickStartMs = System.currentTimeMillis();
		return false;
	}
	// sort transfer
	if(ui.modmeta && !ui.modshift && !ui.modctrl && !item.res.get().basename().contains("gemstone")){
		if(ev.b==1) {
			wdgmsg("transfer-sort", item, false);
		}else if(ev.b==3) {
			wdgmsg("transfer-sort", item, true);
		}else {
			return false;
		}
		return true;
	}
	// default
	if(ev.b == 1) {
		if(ui.modshift) {
			int n = ui.modctrl ? -1 : 1;
			item.wdgmsg("transfer", ev.c, n);
		} else if(ui.modctrl) {
			int n = ui.modmeta ? -1 : 1;
			item.wdgmsg("drop", ev.c, n);
		} else {
			item.wdgmsg("take", ev.c);
		}
		return(true);
	} else if(ev.b == 3) {
		item.wdgmsg("iact", ev.c, ui.modflags());
		ZeeWindow.checkCloseWinDFStyle(this,ev);
		return(true);
	}
	return(super.mousedown(ev));
    }

	@Override
	public boolean mouseup(MouseUpEvent ev) {
	boolean isReallyWItem = this.getClass().getSimpleName().contentEquals("WItem");
	if (isReallyWItem){
		ZeeManagerItems.lastItemClickedMs = ZeeThread.now();
		ZeeManagerItems.lastItemClicked = this;
		ZeeManagerItems.lastItemClickedButton = ev.b;
	}
	// ignore calls by other classes other than WItem
	if (ev.b == 2 && isReallyWItem) {
		ZeeManagerItems.clickEndMs = System.currentTimeMillis();
		new ZeeManagerItems(this,c).start();
		return false;
	}
	if (ev.b==3 && ui.modshift && ZeeConfig.farmerMode) {
		ZeeConfig.println("> disabling farmer mode, start with harvesting instead of planting");
	}
	return super.mouseup(ev);
	}

    public boolean drop(Coord cc, Coord ul) {
	return(false);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
	item.wdgmsg("itemact", ui.modflags());
	return(true);
    }

    public boolean mousehover(MouseHoverEvent ev, boolean on) {
	if(on && (item.contents != null)) {
	    item.hovering(this);
	    return(true);
	}
	return(super.mousehover(ev, on));
    }

	public Coord getInvSlotCoord() {
	return c.div(33);
	}
}

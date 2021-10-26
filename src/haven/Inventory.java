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
import java.awt.image.WritableRaster;
import java.util.stream.Collectors;

public class Inventory extends Widget implements DTarget {
	public Boolean mainInv = null;
    public static final Coord sqsz = UI.scale(new Coord(33, 33));
    public static final Tex invsq;
    public boolean dropul = true;
    public Coord isz;
    Map<GItem, WItem> wmap = new HashMap<GItem, WItem>();
	public static final Comparator<WItem> ITEM_COMPARATOR_ASC = new Comparator<WItem>() {
		@Override
		public int compare(WItem o1, WItem o2) {
			double q1=-1, q2=-1;
			try {
				q1 = Inventory.getQuality(o1.item);
				q2 = Inventory.getQuality(o2.item);
				return Double.compare(q1, q2);
			}catch(Exception e){
				System.out.println("Inventory.ITEM_COMPARATOR_ASC > "+q1+" "+q2);
			}
			return 0;
		}
	};
	public static final Comparator<WItem> ITEM_COMPARATOR_DESC = new Comparator<WItem>() {
		@Override
		public int compare(WItem o1, WItem o2) {
			return ITEM_COMPARATOR_ASC.compare(o2, o1);
		}
	};

    static {
	Coord sz = sqsz.add(1, 1);
	WritableRaster buf = PUtils.imgraster(sz);
	for(int i = 1, y = sz.y - 1; i < sz.x - 1; i++) {
	    buf.setSample(i, 0, 0, 20); buf.setSample(i, 0, 1, 28); buf.setSample(i, 0, 2, 21); buf.setSample(i, 0, 3, 167);
	    buf.setSample(i, y, 0, 20); buf.setSample(i, y, 1, 28); buf.setSample(i, y, 2, 21); buf.setSample(i, y, 3, 167);
	}
	for(int i = 1, x = sz.x - 1; i < sz.y - 1; i++) {
	    buf.setSample(0, i, 0, 20); buf.setSample(0, i, 1, 28); buf.setSample(0, i, 2, 21); buf.setSample(0, i, 3, 167);
	    buf.setSample(x, i, 0, 20); buf.setSample(x, i, 1, 28); buf.setSample(x, i, 2, 21); buf.setSample(x, i, 3, 167);
	}
	for(int y = 1; y < sz.y - 1; y++) {
	    for(int x = 1; x < sz.x - 1; x++) {
		buf.setSample(x, y, 0, 36); buf.setSample(x, y, 1, 52); buf.setSample(x, y, 2, 38); buf.setSample(x, y, 3, 125);
	    }
	}
	invsq = new TexI(PUtils.rasterimg(buf));
    }

    @RName("inv")
    public static class $_ implements Factory {
	public Widget create(UI ui, Object[] args) {
	    return(new Inventory((Coord)args[0]));
	}
    }

    public void draw(GOut g) {
	Coord c = new Coord();
	for(c.y = 0; c.y < isz.y; c.y++) {
	    for(c.x = 0; c.x < isz.x; c.x++) {
		g.image(invsq, c.mul(sqsz));
	    }
	}
	super.draw(g);
    }
	
    public Inventory(Coord sz) {
	super(sqsz.mul(sz).add(1, 1));
	isz = sz;
    }
    
    public boolean mousewheel(Coord c, int amount) {
	if(ui.modshift) {
	    Inventory minv = getparent(GameUI.class).maininv;
	    if(minv != this) {
		if(amount < 0)
		    wdgmsg("invxf", minv.wdgid(), 1);
		else if(amount > 0)
		    minv.wdgmsg("invxf", this.wdgid(), 1);
	    }
	}
	return(true);
    }
    
    public void addchild(Widget child, Object... args) {
	add(child);
	Coord c = (Coord)args[0];
	if(child instanceof GItem) {
	    GItem i = (GItem)child;
	    wmap.put(i, add(new WItem(i), c.mul(sqsz).add(1, 1)));
	    if(isMainInv())
	    	ZeeConfig.addInvItem(i);
	}
    }

	public void cdestroy(Widget w) {
	super.cdestroy(w);
	if(w instanceof GItem) {
	    GItem i = (GItem)w;
	    ui.destroy(wmap.remove(i));
		if(isMainInv())
			ZeeConfig.removeInvItem(i);
	}
    }
    
    public boolean drop(Coord cc, Coord ul) {
	Coord dc;
	if(dropul)
	    dc = ul.add(sqsz.div(2)).div(sqsz);
	else
	    dc = cc.div(sqsz);
	wdgmsg("drop", dc);
	return(true);
    }
	
    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "sz") {
	    isz = (Coord)args[0];
	    resize(invsq.sz().add(UI.scale(new Coord(-1, -1))).mul(isz).add(UI.scale(new Coord(1, 1))));
	    ZeeConfig.invMainoptionsWdg.repositionLabelCount();
	} else if(msg == "mode") {
	    dropul = (((Integer)args[0]) == 0);
	} else {
	    super.uimsg(msg, args);
	}
    }


	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(msg.equals("transfer-sort")){
			process( getSame( (GItem)args[0], (Boolean)args[1]), "transfer");
		} else {
			super.wdgmsg(sender, msg, args);
		}
	}

	private void process(List<WItem> items, String action) {
		for (WItem item : items){
			item.item.wdgmsg(action, Coord.z);
		}
	}

	private List<WItem> getSame(GItem item, Boolean ascending) {
		String name = item.res.get().basename();
		GSprite spr = item.spr();
		List<WItem> items = new ArrayList<>();
		for(Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
			if(wdg.visible && wdg instanceof WItem) {
				WItem wItem = (WItem) wdg;
				GItem child = wItem.item;
				if(child.res.get().basename().equals(name)){// ((spr == child.spr()) || (spr != null && spr.same(child.spr())))) {
					items.add(wItem);
				}
			}
		}
		Collections.sort(items, ascending ? ITEM_COMPARATOR_ASC : ITEM_COMPARATOR_DESC);
		return items;
	}

	public int countItemsByName(String name){
    	int count = 0;
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				String wdgname = ((WItem) wdg).item.res.get().basename();
				if (wdgname.contains(name)) {
					count++;
					break;
				}
			}
		}
		return count;
	}

	public List<WItem> getWItemsByName(String name) {
		return ZeeClickItemManager.getInvBelt().children(WItem.class).stream().filter(wItem -> {
			return wItem.item.getres().name.contains(name);
		}).collect(Collectors.toList());
	}

	public List<WItem> getItemsByNameOrNames(String... names) {
		List<WItem> items = new ArrayList<WItem>();
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				String wdgname = ((WItem) wdg).item.getres().name;
				for (String name : names) {
					if (wdgname.contains(name)) {
						items.add((WItem) wdg);
						break;
					}
				}
			}
		}
		return items;
	}
	public static List<WItem> getItemsByNameOrNames(Inventory inv, String... names) {
    	return inv.getItemsByNameOrNames(names);
	}

	public int getNumberOfFreeSlots() {
		int feespace = isz.x * isz.y;
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem)
				feespace -= (wdg.sz.x * wdg.sz.y) / (sqsz.x * sqsz.y);
		}
		return feespace;
	}
	public static int getNumberOfFreeSlots(Inventory inv) {
		return inv.getNumberOfFreeSlots();
	}

	public List<Coord> getFreeSlots() {
		List<Coord> coords = new ArrayList<>();

		//init array with 0s
		int[][] inv = new int[isz.x][isz.y];
		for (int i = 0; i < isz.x; i++) {
			for (int j = 0; j < isz.y; j++) {
				inv[i][j] = 0;// free slot
			}
		}

		//set occupied slots to 1
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				WItem item = (WItem) wdg;
				Coord div = item.c.div(sqsz);
				inv[div.x][div.y] = 1;
			}
		}

		//collect empty coords for return list
		for (int i = 0; i < isz.x; i++) {
			for (int j = 0; j < isz.y; j++) {
				if(inv[i][j] == 0)
					coords.add(new Coord(i,j));
			}
		}

		return coords;
	}

	public void dropItemsByName(String name) {
		for (WItem wItem : getItemsByNameOrNames(name)) {
			wItem.item.wdgmsg("drop", Coord.z);
		}
	}

	public static Double getQuality(GItem item) {
		try {
			/*
			ItemInfo.Contents contents = getItemInfoContents(item.info());
			if(contents != null) {
				Double quality = getItemInfoQuality(contents.sub);
				if(quality != null) {
					return(quality);
				}
			}
			*/
			return(getItemInfoQuality(item.info()));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return Double.valueOf(0);
	}

	public static Double getItemInfoQuality(List<ItemInfo> info) throws NoSuchFieldException, IllegalAccessException {
		for(ItemInfo v : info) {
			if(v.getClass().getSimpleName().equals("Quality")) {
				return((Double) v.getClass().getField("q").get(v));
			}
		}
		return(null);
	}

	public static ItemInfo.Contents getItemInfoContents(List<ItemInfo> info) {
		for(ItemInfo v : info) {
			if(v instanceof ItemInfo.Contents) {
				return((ItemInfo.Contents) v);
			}
		}
		return(null);
	}

	private boolean isMainInv() {
		if(mainInv == null){
			Window w = this.getparent(Window.class);
			if( w != null && w.cap.text.equalsIgnoreCase("Inventory"))
				mainInv = Boolean.TRUE;
			else
				mainInv = Boolean.FALSE;
		}
		return mainInv;
	}
}

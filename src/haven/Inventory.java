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

import java.awt.image.WritableRaster;
import java.util.*;
import java.util.stream.Collectors;

public class Inventory extends Widget implements DTarget {
	private Boolean mainInv = null;
    public static final Coord sqsz = UI.scale(new Coord(32, 32)).add(1, 1);
    public static final Tex invsq;
    public boolean dropul = true;
    public Coord isz;
    public boolean[] sqmask = null;
	Map<GItem, WItem> wmap = new HashMap<GItem, WItem>();
	static final Comparator<WItem> ITEM_COMPARATOR_ASC = new Comparator<WItem>() {
		@Override
		public int compare(WItem o1, WItem o2) {
			double q1=-1, q2=-1;
			try {
				q1 = o1.item.getInfoQuality();
				q2 = o2.item.getInfoQuality();
				return Double.compare(q1, q2);
			}catch(Exception e){
				System.out.println("Inventory.ITEM_COMPARATOR_ASC > "+q1+" "+q2);
			}
			return 0;
		}
	};
	static final Comparator<WItem> ITEM_COMPARATOR_DESC = new Comparator<WItem>() {
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
	int mo = 0;
	for(c.y = 0; c.y < isz.y; c.y++) {
	    for(c.x = 0; c.x < isz.x; c.x++) {
		if((sqmask != null) && sqmask[mo++]) {
		    g.chcolor(64, 64, 64, 255);
		    g.image(invsq, c.mul(sqsz));
		    g.chcolor();
		} else {
		    g.image(invsq, c.mul(sqsz));
		}
	    }
	}
	super.draw(g);
    }
	
    public Inventory(Coord sz) {
	super(sqsz.mul(sz).add(1, 1));
	isz = sz;
    }
    
    public boolean mousewheel(MouseWheelEvent ev) {
	if(ui.modshift || ZeeConfig.scrollTransferItems) {
	    Inventory minv = getparent(GameUI.class).maininv;
	    if(minv != this) {
		int amount = 1;
		if (ui.modshift | ui.modctrl)
			amount = 5;
		if (ui.modshift && ui.modctrl)
			amount = 10;
		if(ev.a < 0)
		    wdgmsg("invxf", minv.wdgid(), amount);
		else if(ev.a > 0)
		    minv.wdgmsg("invxf", this.wdgid(), amount);
	    }
		if (ev.a != 0)
			ZeeManagerItems.playFeedbackSound();
	}
	return(true);
    }
    
    public void addchild(Widget child, Object... args) {
	add(child);
	Coord c = (Coord)args[0];
	if(child instanceof GItem) {
	    GItem i = (GItem)child;
	    wmap.put(i, add(new WItem(i), c.mul(sqsz).add(1, 1)));
	}
    }
    
    public void cdestroy(Widget w) {
	super.cdestroy(w);
	if(w instanceof GItem) {
	    GItem i = (GItem)w;
	    ui.destroy(wmap.remove(i));
		if(labelCount!=null)
			ZeeConfig.invCounterUpdate(i);
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
		ZeeConfig.inventoryPreResize(this);
	    resize(invsq.sz().add(UI.scale(new Coord(-1, -1))).mul(isz).add(UI.scale(new Coord(1, 1))));
		ZeeConfig.inventoryResized(this);
	    sqmask = null;
	} else if(msg == "mask") {
	    boolean[] nmask;
	    if(args[0] == null) {
		nmask = null;
	    } else {
		nmask = new boolean[isz.x * isz.y];
		byte[] raw = (byte[])args[0];
		for(int i = 0; i < isz.x * isz.y; i++)
		    nmask[i] = (raw[i >> 3] & (1 << (i & 7))) != 0;
	    }
	    this.sqmask = nmask;
	} else if(msg == "mode") {
	    dropul = !Utils.bv(args[0]);
	} else {
	    super.uimsg(msg, args);
	}
    }


	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if(msg.equals("transfer-sort")){
			process( getSame( (GItem)args[0], (Boolean)args[1], false), "transfer");
		}
		else if(msg.equals("transfer-ql")){
			process( getSame( (GItem)args[0], (Boolean)args[1], true), "transfer");
		}
		else {
			super.wdgmsg(sender, msg, args);
		}
	}

	private void process(List<WItem> items, String action) {
		for (WItem item : items){
			item.item.wdgmsg(action, Coord.z);
		}
	}

	private List<WItem> getSame(GItem clickedItem, boolean ascending, boolean sameQl) {
		List<WItem> items = new ArrayList<>();
		try {
			double ql = sameQl ? clickedItem.getInfoQuality() : -1;
			String clickedName = clickedItem.getres().name;
			String clickedInfoName = ZeeManagerItems.getItemInfoName(clickedItem.info()).replace(", stack of","");
			boolean isGemstone = clickedName.endsWith("/gemstone");
			if (isGemstone)
				clickedInfoName = clickedInfoName.replaceAll("^(\\S+\\s+){2}","");//simple gemstone name
			boolean useInfoName = isGemstone || clickedName.endsWith("/meat");
			if (useInfoName)
				clickedName = clickedInfoName;
			for (Widget wdg = lchild; wdg != null; wdg = wdg.prev) {
				if (wdg.visible && wdg instanceof WItem) {
					WItem w = (WItem) wdg;
					String wName = w.item.getres().name;
					if(useInfoName) {
						wName = ZeeManagerItems.getItemInfoName(w.item.info()).replace(", stack of", "");
						if (isGemstone && w.item.getres().name.endsWith("/gemstone"))
							wName = wName.replaceAll("^(\\S+\\s+){2}","");//simple gemstone name
					}
					if ( wName.contentEquals(clickedName) ) {
						if (!sameQl)
							items.add(w);
						else if (ql == w.item.getInfoQuality())
							items.add(w);
					}
				}
			}
			Collections.sort(items, ascending ? ITEM_COMPARATOR_ASC : ITEM_COMPARATOR_DESC);
		}catch (Exception e){
			e.printStackTrace();
		}
		return items;
	}

	public int countItemsByNameContains(String nameContains){
    	int count = 0;
		GItem gItem;
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				gItem = ((WItem) wdg).item;
				String wdgname = gItem.res.get().name;
				if (wdgname.contains(nameContains)) {
					if (ZeeManagerItems.isStackByAmount(gItem))
						count += ZeeManagerItems.getItemInfoAmount(gItem.info());
					else
						count++;
				}
			}
		}
		return count;
	}

	public int countItemsByNameEndsWith(String nameEndsWith){
		int count = 0;
		GItem gItem;
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				gItem = ((WItem) wdg).item;
				String wdgname = gItem.res.get().name;
				if (wdgname.endsWith(nameEndsWith)) {
					if (ZeeManagerItems.isStackByAmount(gItem))
						count += ZeeManagerItems.getItemInfoAmount(gItem.info());
					else
						count++;
				}
			}
		}
		return count;
	}

	public HashMap<String,Integer> getMapItemNameCount(){
		HashMap<String, Integer> map = new HashMap<>();
		GItem gItem;
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				gItem = ((WItem) wdg).item;
				String wdgname = gItem.res.get().name;
				Integer count = map.get(wdgname);
				if (count==null)
					count = 1;
				else if (gItem.isStackByContent() && gItem.getInfoName().contains("stack of"))
					count += gItem.getInfoAmount();
				else
					count++;
				map.put(wdgname, count);
			}
		}
		return map;
	}

	public int countItemsByNameEquals(String nameEquals){
		int count = 0;
		GItem gItem;
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				gItem = ((WItem) wdg).item;
				String wdgname = gItem.res.get().name;
				if (wdgname.contentEquals(nameEquals)) {
					if (ZeeManagerItems.isStackByAmount(gItem))
						count += ZeeManagerItems.getItemInfoAmount(gItem.info());
					else
						count++;
				}
			}
		}
		return count;
	}

	// FIXME: collect() may throw NullException if filter finds a null?
	public List<WItem> getWItemsByNameContains(String nameContains) {
		return this.children(WItem.class)
				.stream()
				.filter( wItem -> wItem.item.getres().name.contains(nameContains) )
				.collect(Collectors.toList());
	}

	public List<WItem> getWItemsByNameContains(String... nameContains) {
		List<WItem> items = new ArrayList<WItem>();
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				String wdgname = ((WItem) wdg).item.getres().name;
				for (String name : nameContains) {
					if (wdgname.contains(name)) {
						items.add((WItem) wdg);
						break;
					}
				}
			}
		}
		return items;
	}

	public List<WItem> getWItemsByInfoNameContains(String nameContains) {
		return this.children(WItem.class)
				.stream()
				.filter( wItem -> ZeeManagerItems.getItemInfoName(wItem.item.info()).toLowerCase().contains(nameContains.toLowerCase()))
				.collect(Collectors.toList());
	}
	public List<WItem> getWItemsByInfoNameEquals(String nameEquals) {
		return this.children(WItem.class)
				.stream()
				.filter( wItem -> ZeeManagerItems.getItemInfoName(wItem.item.info()).toLowerCase().contentEquals(nameEquals.toLowerCase()))
				.collect(Collectors.toList());
	}

	public List<WItem> getWItemsByNameEndsWith(String nameEndsWith) {
		return this.children(WItem.class)
				.stream()
				.filter( wItem -> wItem.item.getres().name.endsWith(nameEndsWith) )
				.collect(Collectors.toList());
	}

	public List<WItem> getItemsByNameEnd(String... namesEndsWith) {
		List<WItem> items = new ArrayList<WItem>();
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				String wdgname = ((WItem) wdg).item.getres().name;
				for (String name : namesEndsWith) {
					if (wdgname.endsWith(name)) {
						items.add((WItem) wdg);
						break;
					}
				}
			}
		}
		return items;
	}

	int getNumberOfSlots(){
		int invSlots = isz.x * isz.y;
		if (sqmask!=null){
			for(int i = 0; i < isz.x * isz.y; i++) {
				if(sqmask[i])
					invSlots--;
			}
		}
		return invSlots;
	}
	int getNumberOfFreeSlots() {
		int invSlots = getNumberOfSlots();
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				//ZeeConfig.println(invSlots+" -  "+((WItem) wdg).item.getres().name);
				invSlots -= ((wdg.sz.x * wdg.sz.y) / (sqsz.x * sqsz.y));// (66, 33) / (33,33)
				//ZeeConfig.println("      = "+invSlots);
			}
		}
		return invSlots;
	}

	//returns topleft from free slot area (w,h)
	public Coord getFreeSlotAreaSized(int w, int h) {
		// inv size is inverted? x=cols, y=rows
		int[][] inv = new int[isz.y][isz.x];
		//init array with 0s
		for (int i = 0; i < isz.y; i++) {
			for (int j = 0; j < isz.x; j++) {
				inv[i][j] = 0;// free slot
			}
		}
		//printMatrix(inv);
		//set occupied slots to 1
		int itcont = 0;
		for (Widget wdg = child; wdg != null; wdg = wdg.next) {
			if (wdg instanceof WItem) {
				WItem item = (WItem) wdg;
				itcont++;
				Coord div = item.c.div(sqsz);
				Coord itsz = item.sz.div(sqsz);
				//TODO update getFreeSlots
				for (int i = 0; i < itsz.y; i++) {
					for (int j = 0; j < itsz.x; j++) {
						if( div.x + j < isz.x  &&  div.y + i < isz.y )
							inv[div.y+i][div.x+j] = itcont;
					}
				}
			}
		}
		ZeeConfig.println("=======");
		ZeeConfig.println("itemsz("+w+","+h+")  "+((ZeeConfig.gameUI!=null&&ZeeConfig.gameUI.vhand!=null&&ZeeConfig.gameUI.vhand.item!=null)? ZeeManagerItems.getHoldingItem().item.getres().basename():""));
		printMatrix(inv);
		//search free area sized (w,h)
		boolean blocked;
		for (int i = 0; i < (inv.length); i++) {
			for (int j = 0; j < (inv[0].length); j++) {
				// occupied slot
				if (inv[i][j] != 0)
					continue;
				//ZeeConfig.println(i+","+j);
				// check if free slots fit item size
				blocked = false;
				for (int iw = 0; iw < w-1; iw++) {
					for (int jh = 0; jh < h-1; jh++) {
						if( i+iw >= inv.length || j+jh >= inv[0].length || inv[i + iw][j + jh] != 0) {
							blocked = true;
							break;
						}
					}
					if (blocked)
						break;
				}
				if (!blocked) {
					Coord ret = new Coord(j,i);
					ZeeConfig.println("free slots topleft "+ret+"  itemsz("+w+","+h+")");
					return ret;
				}
			}
		}
		ZeeConfig.println("no free slots for itemsz("+w+","+h+")");
		return null;
	}

	private void printMatrix(int[][] matrix) {
		System.out.println("printMatrix "+matrix.length+" x "+matrix[0].length + " , isz "+isz );
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
	}

	public List<Coord> getFreeSlots() {
		List<Coord> coords = new ArrayList<>();
		int[][] inv = new int[isz.x][isz.y];
		//init array with 0s
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

	public void dropItemsByNameEndsWith(String nameEndsWith) {
		List<WItem> items = getItemsByNameEnd(nameEndsWith);
		if (items.size()==0){
			ZeeConfig.println("Inventory > dropItemsByName > no item found with name = "+nameEndsWith);
			return;
		}
		WItem item = items.get(0);
		Coord itemCoord = ZeeManagerItems.getWItemCoord(item);
		// haven.GItem@65523a95 ; drop ; [(28, 28), -1]
		item.item.wdgmsg("drop",itemCoord,-1);
	}

	public boolean isMainInv() {
		if(mainInv == null){
			Window w = this.getparent(Window.class);
			if( w != null && w.cap.contentEquals("Inventory"))
				mainInv = Boolean.TRUE;
			else
				mainInv = Boolean.FALSE;
		}
		return mainInv;
	}

	public WItem getItemBySlotCoord(Coord c) {
		for (WItem item : this.children(WItem.class)) {
			if (c.compareTo(ZeeManagerItems.getWItemCoord(item)) == 0) {
				return item;
			}
		}
		return null;
	}

	public WItem getItemByCoord(Coord c) {
		for (WItem item : this.children(WItem.class)) {
			if (c.isect(xlate(item.c, true), item.sz)) {
				return item;
			}
		}
		return null;
	}

	public WItem getWItemByGItem(GItem gItem) {
		for (WItem wItem : this.children(WItem.class)) {
			if (wItem.item.equals(gItem)) {
				return wItem;
			}
		}
		return null;
	}

	public List<WItem> getItemsSelectedForCrafting() {
		List<WItem> ret = new ArrayList<>();
		for (WItem wItem : this.children(WItem.class)) {
			for(ItemInfo inf : wItem.item.info()) {
				if(inf.getClass().getSimpleName().contentEquals("CraftPrep")) {
					ret.add(wItem);
					break;//add wItem once
				}
			}
		}
		return ret;
	}

	public List<WItem> getItemsWithColorOverlay() {
		List<WItem> ret = new ArrayList<>();
		for (WItem wItem : this.children(WItem.class)) {
			for(ItemInfo inf : wItem.item.info()) {
				if(inf instanceof GItem.ColorInfo) {
					ret.add(wItem);
					break;//add wItem once
				}
			}
		}
		return ret;
	}

	public List<WItem> getItemsByInfoClass(String classNameEndsWith) {
		List<WItem> ret = new ArrayList<>();
		for (WItem wItem : this.children(WItem.class)) {
			for(ItemInfo inf : wItem.item.info()) {
				if(inf.getClass().getName().endsWith(classNameEndsWith))
					ret.add(wItem);
			}
		}
		return ret;
	}

	public Label labelCount = null;
	public void repositionLabelCount() {
		if (labelCount==null)
			return;
		Coord br = this.area().br;
		labelCount.c = new Coord(
				br.x - labelCount.sz.x,
				br.y - 3
		);
        if(isMainInv()) {//workaround main inv area.br broken by pockets?
            //ZeeConfig.println("repos maininv");
            Window  win = this.getparent(Window.class);
            if (win!=null)
                win.pack();
        }
	}
}

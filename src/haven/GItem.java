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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;

public class GItem extends AWidget implements ItemInfo.SpriteOwner, GSprite.Owner {
    public Indir<Resource> res;
    public MessageBuf sdt;
    public int meter = 0;
    public int num = -1;
    private GSprite spr;
    private ItemInfo.Raw rawinfo;
    private List<ItemInfo> info = Collections.emptyList();
	private boolean itemDropped = false;
	private boolean itemCounted = false;

    @RName("item")
    public static class $_ implements Factory {
	public Widget create(UI ui, Object[] args) {
	    int res = (Integer)args[0];
	    Message sdt = (args.length > 1)?new MessageBuf((byte[])args[1]):Message.nil;
	    return(new GItem(ui.sess.getres(res), sdt));
	}
    }

    public interface ColorInfo {
	public Color olcol();
    }

    public interface OverlayInfo<T> {
	public T overlay();
	public void drawoverlay(GOut g, T data);
    }

    public static class InfoOverlay<T> {
	public final OverlayInfo<T> inf;
	public final T data;

	public InfoOverlay(OverlayInfo<T> inf) {
	    this.inf = inf;
	    this.data = inf.overlay();
	}

	public void draw(GOut g) {
	    inf.drawoverlay(g, data);
	}

	public static <S> InfoOverlay<S> create(OverlayInfo<S> inf) {
	    return(new InfoOverlay<S>(inf));
	}
    }

    public interface NumberInfo extends OverlayInfo<Tex> {
	public int itemnum();
	public default Color numcolor() {
	    return(Color.WHITE);
	}

	public default Tex overlay() {
	    return(new TexI(GItem.NumberInfo.numrender(itemnum(), numcolor())));
	}

	public default void drawoverlay(GOut g, Tex tex) {
	    g.aimage(tex, g.sz(), 1, 1);
	}

	public static BufferedImage numrender(int num, Color col) {
		//col = Color.white;
	    return(Utils.outline2(Text.render(Integer.toString(num), col).img, Utils.contrast(col)));
	}
    }

    public interface MeterInfo {
	public double meter();
    }

    public static class Amount extends ItemInfo implements NumberInfo {
	private final int num;

	public Amount(Owner owner, int num) {
	    super(owner);
	    this.num = num;
	}

	public int itemnum() {
	    return(num);
	}
    }

    public GItem(Indir<Resource> res, Message sdt) {
	this.res = res;
	this.sdt = new MessageBuf(sdt);
    }

    public GItem(Indir<Resource> res) {
	this(res, Message.nil);
    }

    private Random rnd = null;
    public Random mkrandoom() {
	if(rnd == null)
	    rnd = new Random();
	return(rnd);
    }
    public Resource getres() {return(res.get());}
    private static final OwnerContext.ClassResolver<GItem> ctxr = new OwnerContext.ClassResolver<GItem>()
	.add(Glob.class, wdg -> wdg.ui.sess.glob)
	.add(Session.class, wdg -> wdg.ui.sess);
    public <T> T context(Class<T> cl) {return(ctxr.context(cl, this));}
    @Deprecated
    public Glob glob() {return(ui.sess.glob);}

    public GSprite spr() {
	GSprite spr = this.spr;
	if(spr == null) {
	    try {
			spr = this.spr = GSprite.create(this, res.get(), sdt.clone());
			if (!itemDropped) {
				itemDropped = true;
				onSpriteCreated();
			}
			if(!itemCounted){
				itemCounted = true;
				Window w = this.getparent(Window.class);
				if(w!=null && w.cap.text.equalsIgnoreCase("Inventory"))
					ZeeConfig.addInvItem(this);
			}
	    } catch(Loading l) {
	    }
	}
	return(spr);
    }


	/*
		check item for dropping and other actions
	 */
	private void onSpriteCreated() {

		String basename = this.resource().basename();
		Resource curs = ui.root.getcurs(Coord.z);
		ZeeConfig.lastInvItemBaseName = basename;
		ZeeConfig.lastInvItemMs = ZeeThread.now();

		if (curs != null && curs.name.equals(ZeeConfig.CURSOR_MINE)) {
			//drop mined item
			ZeeManagerMiner.lastDropItemMs = System.currentTimeMillis();
			if (ZeeConfig.dropMinedStones && ZeeManagerMiner.isStoneNotOre(basename) ||
					ZeeConfig.dropMinedOre && ZeeManagerMiner.isStoneOre(basename) ||
					ZeeConfig.dropMinedOrePrecious && ZeeManagerMiner.isStoneOrePrecious(basename) ||
					ZeeConfig.dropMinedCurios && ZeeConfig.mineablesCurios.contains(basename) )
			{
				this.wdgmsg("drop", Coord.z);
			}
		}
		else if( ZeeConfig.farmerMode ) {
			if(ZeeManagerFarmer.busy) {
				//drop non-seed crops
				if (!basename.startsWith("seed-") && ZeeConfig.isItemCrop(basename)) {
					this.wdgmsg("drop", Coord.z);
				}
			}
			else if(basename.startsWith("seed-") && this.parent instanceof Inventory) {
				//farmermode not busy
				if (ZeeConfig.lastSavedOverlayEndCoord == null) {
					//cancel farmermode
					ZeeConfig.println("seedfarmer > no tile selection, reset initial state");
					//ZeeConfig.farmerMode = false; //TODO test
					ZeeManagerFarmer.resetInitialState();
				}
				else {
					//start farmermode
					new ZeeManagerFarmer(this, basename).start();
				}
			}
		}
		else if (ZeeConfig.dropBoards && basename.startsWith("board-")){
			this.wdgmsg("drop", Coord.z);
		}
		else if( ZeeConfig.dropSeeds && basename.startsWith("seed-") && this.parent instanceof Inventory){
			//drop seeds
			Inventory inv = (Inventory) this.parent;
			if(inv.getNumberOfFreeSlots() < 3){
				inv.dropItemsByName(basename);
			}
		}
		else if( ZeeConfig.dropSoil && basename.startsWith("soil") && this.parent instanceof Inventory) {
			//drop soil
			Inventory inv = (Inventory) this.parent;
			inv.dropItemsByName(basename);
		}
	}

    public void tick(double dt) {
	GSprite spr = spr();
	if(spr != null)
	    spr.tick(dt);
    }

    public List<ItemInfo> info() {
	if(info == null)
	    info = ItemInfo.buildinfo(this, rawinfo);
	return(info);
    }

    public Resource resource() {
	return(res.get());
    }

    public GSprite sprite() {
	if(spr == null)
	    throw(new Loading("Still waiting for sprite to be constructed"));
	return(spr);
    }

    public void uimsg(String name, Object... args) {
	if(name == "num") {
	    num = (Integer)args[0];
	} else if(name == "chres") {
	    synchronized(this) {
		res = ui.sess.getres((Integer)args[0]);
		sdt = (args.length > 1)?new MessageBuf((byte[])args[1]):MessageBuf.nil;
		spr = null;
	    }
	} else if(name == "tt") {
	    info = null;
	    rawinfo = new ItemInfo.Raw(args);
	} else if(name == "meter") {
	    meter = (int)((Number)args[0]).doubleValue();
	}
    }
}

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
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ItemInfo {
    public final Owner owner;

    public interface Owner extends OwnerContext {
	public List<ItemInfo> info();
    }

    public interface ResOwner extends Owner {
	public Resource resource();
    }

    public interface SpriteOwner extends ResOwner {
	public GSprite sprite();
    }

    public static class Raw {
	public static final Raw nil = new Raw(new Object[0], 0);
	public final Object[] data;
	public final double time;

	public Raw(Object[] data, double time) {
	    this.data = data;
	    this.time = time;
	}

	public Raw(Object[] data) {
	    this(data, Utils.rtime());
	}
    }

    @Resource.PublishedCode(name = "tt", instancer = FactMaker.class)
    public static interface InfoFactory {
	public ItemInfo build(Owner owner, Raw raw, Object... args);
    }

    public static class FactMaker extends Resource.PublishedCode.Instancer.Chain<InfoFactory> {
	public FactMaker() {super(InfoFactory.class);}
	{
	    add(new Direct<>(InfoFactory.class));
	    add(new StaticCall<>(InfoFactory.class, "mkinfo", ItemInfo.class, new Class<?>[] {Owner.class, Object[].class},
				 (make) -> new InfoFactory() {
					 public ItemInfo build(Owner owner, Raw raw, Object... args) {
					     return(make.apply(new Object[]{owner, args}));
					 }
				     }));
	    add(new StaticCall<>(InfoFactory.class, "mkinfo", ItemInfo.class, new Class<?>[] {Owner.class, Raw.class, Object[].class},
				 (make) -> new InfoFactory() {
					 public ItemInfo build(Owner owner, Raw raw, Object... args) {
					     return(make.apply(new Object[]{owner, raw, args}));
					 }
				     }));
	    add(new Construct<>(InfoFactory.class, ItemInfo.class, new Class<?>[] {Owner.class, Object[].class},
				(cons) -> new InfoFactory() {
					public ItemInfo build(Owner owner, Raw raw, Object... args) {
					    return(cons.apply(new Object[] {owner, args}));
					}
				    }));
	    add(new Construct<>(InfoFactory.class, ItemInfo.class, new Class<?>[] {Owner.class, Raw.class, Object[].class},
				(cons) -> new InfoFactory() {
					public ItemInfo build(Owner owner, Raw raw, Object... args) {
					    return(cons.apply(new Object[] {owner, raw, args}));
					}
				    }));
	}
    }

    public ItemInfo(Owner owner) {
	this.owner = owner;
    }

    public static class Layout {
	public final Owner owner;
	public final CompImage cmp = new CompImage();
	public int width = 0;
	private final List<Tip> tips = new ArrayList<>();
	private final Map<TipID, Tip> itab = new HashMap<>();

	public Layout(Owner owner) {
	    this.owner = owner;
	}

	public interface TipID<T extends Tip> {
	    public T make(Owner owner);
	}

	@Deprecated
	public interface ID<T extends Tip> extends TipID<T> {
	    public T make();
	    public default T make(Owner owner) {return(make());}
	}

	@SuppressWarnings("unchecked")
	public <T extends Tip> T intern(TipID<T> id) {
	    T ret = (T)itab.get(id);
	    if(ret == null) {
		itab.put(id, ret = id.make(owner));
		add(ret);
	    }
	    return(ret);
	}

	public <T extends Tip> T intern(ID<T> id) {
	    return(intern((TipID<T>)id));
	}

	public void add(Tip tip) {
	    tips.add(tip);
	    tip.prepare(this);
	}

	public BufferedImage render() {
	    Collections.sort(tips, new Comparator<Tip>() {
		    public int compare(Tip a, Tip b) {
			return(a.order() - b.order());
		    }
		});
	    for(Tip tip : tips)
		tip.layout(this);
	    return(cmp.compose());
	}
    }

    public static abstract class Tip extends ItemInfo {
	public Tip(Owner owner) {
	    super(owner);
	}

	public BufferedImage tipimg() {return(null);}
	public BufferedImage tipimg(int w) {return(tipimg());}
	public Tip shortvar() {return(null);}
	public void prepare(Layout l) {}
	public void layout(Layout l) {
	    BufferedImage t = tipimg(l.width);
	    if(t != null)
		l.cmp.add(t, new Coord(0, l.cmp.sz.y));
	}
	public int order() {return(100);}
    }

    public static class AdHoc extends Tip {
	public final Text str;

	public AdHoc(Owner owner, String str) {
	    super(owner);
	    this.str = Text.render(str);
	}

	public BufferedImage tipimg() {
	    return(str.img);
	}
    }

    public static class Name extends Tip {
	public final Text str;

	public Name(Owner owner, Text str) {
	    super(owner);
	    this.str = str;
	}

	public Name(Owner owner, String str) {
	    this(owner, Text.render(str));
	}

	public BufferedImage tipimg() {
	    return(str.img);
	}

	public int order() {return(0);}

	public Tip shortvar() {
	    return(new Tip(owner) {
		    public BufferedImage tipimg() {return(str.img);}
		    public int order() {return(0);}
		});
	}

	public static interface Dynamic {
	    public String name();
	}

	public static class Default implements InfoFactory {
	    public static String get(Owner owner) {
		if(owner instanceof SpriteOwner) {
		    GSprite spr = ((SpriteOwner)owner).sprite();
		    if(spr instanceof Dynamic)
			return(((Dynamic)spr).name());
		}
		if(!(owner instanceof ResOwner))
		    return(null);
		Resource res = ((ResOwner)owner).resource();
		Resource.Tooltip tt = res.layer(Resource.tooltip);
		if(tt == null)
		    throw(new RuntimeException("Item resource " + res + " is missing default tooltip"));
		return(tt.t);
	    }

	    public ItemInfo build(Owner owner, Raw raw, Object... args) {
		String nm = get(owner);
		return((nm == null) ? null : new Name(owner, nm));
	    }
	}
    }

    public static class Pagina extends Tip {
	public final String str;

	public Pagina(Owner owner, String str) {
	    super(owner);
	    this.str = str;
	}

	public BufferedImage tipimg(int w) {
	    return(RichText.render(str, w).img);
	}

	public void layout(Layout l) {
	    BufferedImage t = tipimg((l.width == 0) ? Math.max(UI.scale(200), l.cmp.sz.x) : l.width);
	    if(t != null)
		l.cmp.add(t, new Coord(0, l.cmp.sz.y + UI.scale(10)));
	}

	public int order() {return(10000);}
    }

    public static class Contents extends Tip {
	public final List<ItemInfo> sub;
	static final Text.Line ch = Text.render("Contents:");
	
	public Contents(Owner owner, List<ItemInfo> sub) {
	    super(owner);
	    this.sub = sub;
	}
	
	public BufferedImage tipimg() {
	    BufferedImage stip = longtip(sub);
	    BufferedImage img = TexI.mkbuf(Coord.of(stip.getWidth(), stip.getHeight()).add(UI.scale(10, 15)));
	    Graphics g = img.getGraphics();
	    g.drawImage(ch.img, 0, 0, null);
	    g.drawImage(stip, UI.scale(10), UI.scale(15), null);
	    g.dispose();
	    return(img);
	}

	public Tip shortvar() {
	    return(new Tip(owner) {
		    public BufferedImage tipimg() {return(shorttip(sub));}
		    public int order() {return(100);}
		});
	}
    }

    public static BufferedImage catimgs(int margin, BufferedImage... imgs) {
	int w = 0, h = -margin;
	for(BufferedImage img : imgs) {
	    if(img == null)
		continue;
	    if(img.getWidth() > w)
		w = img.getWidth();
	    h += img.getHeight() + margin;
	}
	BufferedImage ret = TexI.mkbuf(new Coord(w, h));
	Graphics g = ret.getGraphics();
	int y = 0;
	for(BufferedImage img : imgs) {
	    if(img == null)
		continue;
	    g.drawImage(img, 0, y, null);
	    y += img.getHeight() + margin;
	}
	g.dispose();
	return(ret);
    }

    public static BufferedImage catimgsh(int margin, BufferedImage... imgs) {
	int w = -margin, h = 0;
	for(BufferedImage img : imgs) {
	    if(img == null)
		continue;
	    if(img.getHeight() > h)
		h = img.getHeight();
	    w += img.getWidth() + margin;
	}
	BufferedImage ret = TexI.mkbuf(new Coord(w, h));
	Graphics g = ret.getGraphics();
	int x = 0;
	for(BufferedImage img : imgs) {
	    if(img == null)
		continue;
	    g.drawImage(img, x, (h - img.getHeight()) / 2, null);
	    x += img.getWidth() + margin;
	}
	g.dispose();
	return(ret);
    }

    public static BufferedImage longtip(List<ItemInfo> info) {
	if(info.isEmpty())
	    return(null);
	Layout l = new Layout(info.get(0).owner);
	for(ItemInfo ii : info) {
	    if(ii instanceof Tip) {
		Tip tip = (Tip)ii;
		l.add(tip);
	    }
	}
	if(l.tips.size() < 1)
	    return(null);
	// add "[shift for help]" to stack shorttip
	if(l.tips.get(0).owner instanceof GItem) {
		GItem gitem = (GItem) l.tips.get(0).owner;
		if (gitem.isStackByContent())
			l.add(new AdHoc(gitem, "[shift expand info]"));
	}
	return(l.render());
    }

    public static BufferedImage shorttip(List<ItemInfo> info) {
	List<ItemInfo> sinfo = new ArrayList<>();
	for(ItemInfo ii : info) {
	    if(ii instanceof Tip) {
		Tip tip = ((Tip)ii).shortvar();
		if(tip != null)
		    sinfo.add(tip);
	    }
	}
	return(longtip(sinfo));
    }

    public static <T> T find(Class<T> cl, List<ItemInfo> il) {
	for(ItemInfo inf : il) {
	    if(cl.isInstance(inf))
		return(cl.cast(inf));
	}
	return(null);
    }

    public static List<ItemInfo> buildinfo(Owner owner, Raw raw) {
	List<ItemInfo> ret = new ArrayList<ItemInfo>();
	Resource.Resolver rr = owner.context(Resource.Resolver.class);
	for(Object o : raw.data) {
	    if(o instanceof Object[]) {
		Object[] a = (Object[])o;
		ItemInfo inf;
		if(a[0] instanceof InfoFactory) {
		    inf = ((InfoFactory)a[0]).build(owner, raw, a);
		} else {
		    Resource ttres;
		    if(a[0] instanceof Resource) {
			ttres = (Resource)a[0];
		    } else if(a[0] instanceof Indir) {
			ttres = (Resource)((Indir)a[0]).get();
		    } else {
			ttres = rr.getresv(a[0]).get();
		    }
		    InfoFactory f = ttres.getcode(InfoFactory.class, true);
		    inf = f.build(owner, raw, a);
		}
		if(inf != null)
		    ret.add(inf);
	    } else if(o instanceof String) {
		ret.add(new AdHoc(owner, (String)o));
	    } else {
		throw(new ClassCastException("Unexpected object type " + o.getClass() + " in item info array."));
	    }
	}
	return(ret);
    }

    public static List<ItemInfo> buildinfo(Owner owner, Object[] rawinfo) {
	return(buildinfo(owner, new Raw(rawinfo)));
    }
    
    private static String dump(Object arg) {
	if(arg instanceof Object[]) {
	    StringBuilder buf = new StringBuilder();
	    buf.append("[");
	    boolean f = true;
	    for(Object a : (Object[])arg) {
		if(!f)
		    buf.append(", ");
		buf.append(dump(a));
		f = false;
	    }
	    buf.append("]");
	    return(buf.toString());
	} else {
	    return(arg.toString());
	}
    }

    public static class AttrCache<R> implements Indir<R> {
	private final Supplier<List<ItemInfo>> from;
	private final Function<List<ItemInfo>, Supplier<R>> data;
	private List<ItemInfo> forinfo = null;
	private Supplier<R> save;

	public AttrCache(Supplier<List<ItemInfo>> from, Function<List<ItemInfo>, Supplier<R>> data) {
	    this.from = from;
	    this.data = data;
	}

	public R get() {
	    try {
		List<ItemInfo> info = from.get();
		if(info != forinfo) {
		    save = data.apply(info);
		    forinfo = info;
		}
		return(save.get());
	    } catch(Loading l) {
		return(null);
	    }
	}

	public static <I, R> Function<List<ItemInfo>, Supplier<R>> map1(Class<I> icl, Function<I, Supplier<R>> data) {
	    return(info -> {
		    I inf = find(icl, info);
		    if(inf == null)
			return(() -> null);
		    return(data.apply(inf));
		});
	}

	public static <I, R> Function<List<ItemInfo>, Supplier<R>> map1s(Class<I> icl, Function<I, R> data) {
	    return(info -> {
		    I inf = find(icl, info);
		    if(inf == null)
			return(() -> null);
		    R ret = data.apply(inf);
		    return(() -> ret);
		});
	}
    }

    public static interface InfoTip {
	public List<ItemInfo> info();
    }
}

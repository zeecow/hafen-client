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

import me.ender.Reflect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class ItemInfo {
    public static final Resource armor_hard = Resource.local().loadwait("gfx/hud/chr/custom/ahard");
    public static final Resource armor_soft = Resource.local().loadwait("gfx/hud/chr/custom/asoft");
    static final Pattern count_pattern = Pattern.compile("(?:^|[\\s])([0-9]*\\.?[0-9]+\\s*%?)");
    public final Owner owner;
    
    public static ItemInfo make(Session sess, String resname, Object ...args) {
	Resource res = Resource.remote().load(resname).get();
	InfoFactory f = res.layer(Resource.CodeEntry.class).get(InfoFactory.class);
	return f.build(new SessOwner(sess), args);
    }
    
    public interface Owner extends OwnerContext {
	@Deprecated
	public default Glob glob() {return(context(Glob.class));}
	public List<ItemInfo> info();
    }

    private static class SessOwner implements ItemInfo.Owner {
	private final OwnerContext.ClassResolver<SessOwner> ctxr;

	public SessOwner(Session sess) {
	    ctxr = new OwnerContext.ClassResolver<SessOwner>()
		.add(Glob.class, x -> sess.glob)
		.add(Session.class, x -> sess);
	}

	@Override
	public List<ItemInfo> info() {
	    return null;
	}

	@Override
	public <T> T context(Class<T> cl) {
	    return (ctxr.context(cl, this));
	}
    }
    
    public interface ResOwner extends Owner {
	Resource resource();
    }

    public interface SpriteOwner extends ResOwner {
	GSprite sprite();
    }
    
    @Resource.PublishedCode(name = "tt", instancer = FactMaker.class)
    public interface InfoFactory {
	ItemInfo build(Owner owner, Object... args);
    }

    public static class FactMaker implements Resource.PublishedCode.Instancer {
	public InfoFactory make(Class<?> cl) throws InstantiationException, IllegalAccessException {
	    if(InfoFactory.class.isAssignableFrom(cl))
		return(cl.asSubclass(InfoFactory.class).newInstance());
	    try {
		final Method mkm = cl.getDeclaredMethod("mkinfo", Owner.class, Object[].class);
		int mod = mkm.getModifiers();
		if(ItemInfo.class.isAssignableFrom(mkm.getReturnType()) && ((mod & Modifier.STATIC) != 0) && ((mod & Modifier.PUBLIC) != 0)) {
		    return(new InfoFactory() {
			    public ItemInfo build(Owner owner, Object... args) {
				try {
				    return((ItemInfo)mkm.invoke(null, owner, args));
				} catch(Exception e) {
				    if(e instanceof RuntimeException) throw((RuntimeException)e);
				    throw(new RuntimeException(e));
				}
			    }
			});
		}
	    } catch(NoSuchMethodException e) {
	    }
	    return(null);
	}
    }
    
    public ItemInfo(Owner owner) {
	this.owner = owner;
    }
    
    public static class Layout {
	private final List<Tip> tips = new ArrayList<Tip>();
	private final Map<ID, Tip> itab = new HashMap<ID, Tip>();
	public final CompImage cmp = new CompImage();

	public interface ID<T extends Tip> {
	    T make();
	}

	@SuppressWarnings("unchecked")
	public <T extends Tip> T intern(ID<T> id) {
	    T ret = (T)itab.get(id);
	    if(ret == null) {
		itab.put(id, ret = id.make());
		add(ret);
	    }
	    return(ret);
	}

	public void add(Tip tip) {
	    tips.add(tip);
	    tip.prepare(this);
	}

	public BufferedImage render() {
	    Collections.sort(tips, (a, b) -> (a.order() - b.order()));
	    for(Tip tip : tips)
		tip.layout(this);
	    return(cmp.compose());
	}
    }

    public static abstract class Tip extends ItemInfo {
	public Tip(Owner owner) {
	    super(owner);
	}

	@Deprecated
	public BufferedImage longtip() {return(null);}
	public BufferedImage tipimg() {return(longtip());}
	public Tip shortvar() {return(null);}
	public void prepare(Layout l) {}
	public void layout(Layout l) {
	    BufferedImage t = tipimg();
	    if(t != null)
		l.cmp.add(tipimg(), new Coord(0, l.cmp.sz.y));
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
    }

    public static class Contents extends Tip {
	public final List<ItemInfo> sub;
	private static final Text.Line ch = Text.render("Contents:");
	
	public Contents(Owner owner, List<ItemInfo> sub) {
	    super(owner);
	    this.sub = sub;
	}
	
	public BufferedImage tipimg() {
	    BufferedImage stip = longtip(sub);
	    BufferedImage img = TexI.mkbuf(new Coord(stip.getWidth() + 10, stip.getHeight() + 15));
	    Graphics g = img.getGraphics();
	    g.drawImage(ch.img, 0, 0, null);
	    g.drawImage(stip, 10, 15, null);
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
	return catimgs(margin, false, imgs);
    }

    public static BufferedImage catimgs(int margin, boolean right, BufferedImage... imgs) {
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
	    g.drawImage(img, right ? w - img.getWidth() : 0, y, null);
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
	Layout l = new Layout();
	for(ItemInfo ii : info) {
	    if(ii instanceof Tip) {
		Tip tip = (Tip)ii;
		l.add(tip);
	    }
	}
	if(l.tips.size() < 1)
	    return(null);
	return(l.render());
    }

    public static BufferedImage shorttip(List<ItemInfo> info) {
	Layout l = new Layout();
	for(ItemInfo ii : info) {
	    if(ii instanceof Tip) {
		Tip tip = ((Tip)ii).shortvar();
		if(tip != null)
		    l.add(tip);
	    }
	}
	if(l.tips.size() < 1)
	    return(null);
	return(l.render());
    }

    public static <T> T find(Class<T> cl, List<ItemInfo> il) {
	for(ItemInfo inf : il) {
	    if(cl.isInstance(inf))
		return(cl.cast(inf));
	}
	return(null);
    }

    public static <T> List<T> findall(Class<T> cl, List<ItemInfo> il) {
	List<T> ret = new LinkedList<>();
	for(ItemInfo inf : il) {
	    if(cl.isInstance(inf))
		ret.add(cl.cast(inf));
	}
	return ret;
    }

    public static List<ItemInfo> findall(String cl, List<ItemInfo> infos){
	return infos.stream()
	    .filter(inf -> Reflect.is(inf, cl))
	    .collect(Collectors.toCollection(LinkedList::new));
    }

    public static List<ItemInfo> buildinfo(Owner owner, Object[] rawinfo) {
	List<ItemInfo> ret = new ArrayList<ItemInfo>();
	for(Object o : rawinfo) {
	    if(o instanceof Object[]) {
		Object[] a = (Object[])o;
		Resource ttres;
		if(a[0] instanceof Integer) {
		    ttres = owner.glob().sess.getres((Integer)a[0]).get();
		} else if(a[0] instanceof Resource) {
		    ttres = (Resource)a[0];
		} else if(a[0] instanceof Indir) {
		    ttres = (Resource)((Indir)a[0]).get();
		} else {
		    throw(new ClassCastException("Unexpected info specification " + a[0].getClass()));
		}
		InfoFactory f = ttres.getcode(InfoFactory.class, true);
		ItemInfo inf = f.build(owner, a);
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

    public static String getCount(List<ItemInfo> infos) {
	String res = null;
	for (ItemInfo info : infos) {
	    if(info instanceof Contents) {
		Contents cnt = (Contents) info;
		res = getCount(cnt.sub);
	    } else if(info instanceof AdHoc) {
		AdHoc ah = (AdHoc) info;
		try {
		    Matcher m = count_pattern.matcher(ah.str.text);
		    if(m.find()) {
			res = m.group(1);
		    }
		} catch (Exception ignored) {
		}
	    } else if(info instanceof Name) {
		Name name = (Name) info;
		try {
		    Matcher m = count_pattern.matcher(name.str.text);
		    if(m.find()) {
			res = m.group(1);
		    }
		} catch (Exception ignored) {
		}
	    }
	    if(res != null) {
		return res.trim();
	    }
	}
	return null;
    }

    public static Pair<Integer, Integer> getWear(List<ItemInfo> infos) {
	infos = findall("Wear", infos);
	for (ItemInfo info : infos) {
	    if(Reflect.hasField(info, "m") && Reflect.hasField(info, "d")){
		return new Pair<>(Reflect.getFieldValueInt(info, "d"), Reflect.getFieldValueInt(info, "m"));
	    }
	}
	return null;
    }

    public static Pair<Integer, Integer> getArmor(List<ItemInfo> infos) {
	//loftar is wonderful sunshine and has same class name for wear and armor tooltips even though
	//they are different classes with different fields :)
	infos = findall("Wear", infos);
	for (ItemInfo info : infos) {
	    if(Reflect.hasField(info, "hard") && Reflect.hasField(info, "soft")){
		return new Pair<>(Reflect.getFieldValueInt(info, "hard"), Reflect.getFieldValueInt(info, "soft"));
	    }
	}
	return null;
    }

    @SuppressWarnings("unchecked")
    public static Map<Resource, Integer> getBonuses(List<ItemInfo> infos) {
	List<ItemInfo> slotInfos = ItemInfo.findall("ISlots", infos);
	List<ItemInfo> gilding = ItemInfo.findall("Slotted", infos);
	Map<Resource, Integer> bonuses = new HashMap<>();
	try {
	    for (ItemInfo islots : slotInfos) {
		List<Object> slots = (List<Object>) Reflect.getFieldValue(islots, "s");
		for (Object slot : slots) {
		    parseAttrMods(bonuses, (List) Reflect.getFieldValue(slot, "info"));
		}
	    }
	    for (ItemInfo info : gilding) {
		List<Object> slots = (List<Object>) Reflect.getFieldValue(info, "sub");
		parseAttrMods(bonuses, slots);
	    }
	    parseAttrMods(bonuses, ItemInfo.findall("haven.res.ui.tt.attrmod.AttrMod", infos));
	} catch (Exception ignored) {}
	Pair<Integer, Integer> wear = ItemInfo.getArmor(infos);
	if (wear != null) {
	    bonuses.put(armor_hard, wear.a);
	    bonuses.put(armor_soft, wear.b);
	}
	return bonuses;
    }


    @SuppressWarnings("unchecked")
    public static void parseAttrMods(Map<Resource, Integer> bonuses, List infos) {
	for (Object inf : infos) {
	    List<Object> mods = (List<Object>) Reflect.getFieldValue(inf, "mods");
	    for (Object mod : mods) {
		Resource attr = (Resource) Reflect.getFieldValue(mod, "attr");
		int value = Reflect.getFieldValueInt(mod, "mod");
		if (bonuses.containsKey(attr)) {
		    bonuses.put(attr, bonuses.get(attr) + value);
		} else {
		    bonuses.put(attr, value);
		}
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private static Map<Resource, Integer> parseAttrMods2(List infos) {
	Map<Resource, Integer> bonuses = new HashMap<>();
	for (Object inf : infos) {
	    List<Object> mods = (List<Object>) Reflect.getFieldValue(inf, "mods");
	    for (Object mod : mods) {
		Resource attr = (Resource) Reflect.getFieldValue(mod, "attr");
		int value = Reflect.getFieldValueInt(mod, "mod");
		if (bonuses.containsKey(attr)) {
		    bonuses.put(attr, bonuses.get(attr) + value);
		} else {
		    bonuses.put(attr, value);
		}
	    }
	}
	return bonuses;
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
}

/* Preprocessed source code */
package haven.res.ui.tt.attrmod;

import haven.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

import static haven.PUtils.convolvedown;

@Resource.PublishedCode(name = "attrmod")
@haven.FromResource(name = "ui/tt/attrmod", version = 11)
public class AttrMod extends ItemInfo.Tip {
    public final Collection<Entry> tab;

    public AttrMod(Owner owner, Collection<Entry> tab) {
	super(owner);
	this.tab = tab;
    }

    public static class Fac implements InfoFactory {
	public ItemInfo build(Owner owner, Raw raw, Object... args) {
	    Resource.Resolver rr = owner.context(Resource.Resolver.class);
	    Collection<Entry> tab = new ArrayList<Entry>();
	    for(int a = 1; a < args.length; a += 2)
		tab.add(new Mod(Attribute.get(rr.getresv(args[a]).get()), Utils.dv(args[a + 1])));
	    return(new AttrMod(owner, tab));
	}
    }

    public static void tablayout(Layout l, Collection<Entry> tabc) {
	Entry[] tab = tabc.toArray(new Entry[0]);
	BufferedImage[] icons = new BufferedImage[tab.length];
	BufferedImage[] names = new BufferedImage[tab.length];
	BufferedImage[] values = new BufferedImage[tab.length];
	int w = 0;
	for(int i = 0; i < tab.length; i++) {
	    Entry row = tab[i];
	    names[i] = Text.render(row.attr.name()).img;
	    icons[i] = row.attr.icon();
	    if(icons[i] != null)
		icons[i] = convolvedown(icons[i], Coord.of(names[i].getHeight()), CharWnd.iconfilter);
	    values[i] = RichText.render(row.fmtvalue(), 0).img;
	    w = Math.max(w, names[i].getWidth());
	}
	for(int i = 0; i < tab.length; i++) {
	    int y = l.cmp.sz.y;
	    if(icons[i] != null)
		l.cmp.add(icons[i], Coord.of(0, y));
	    int nx = names[i].getHeight() + (int)UI.scale(0.75);
	    l.cmp.add(names[i], Coord.of(nx, y));
	    l.cmp.add(values[i], Coord.of(nx + w + UI.scale(5), y));
	}
    }

    public void prepare(Layout l) {
	l.intern(Collected.id).tab.addAll(tab);
    }

    public static class Collected extends Tip {
	public static final Layout.TipID<Collected> id = Collected::new;
	public final Collection<Entry> tab = new ArrayList<>();

	public Collected(Owner owner) {
	    super(owner);
	}

	public void layout(Layout l) {
	    tablayout(l, tab);
	}
    }
}

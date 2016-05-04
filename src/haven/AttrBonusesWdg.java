package haven;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AttrBonusesWdg extends Widget implements ItemInfo.Owner {
    private boolean needUpdate = false;
    private boolean needBuild = false;
    private boolean needRedraw = false;

    private WItem[] items;
    private Map<Resource, Integer> bonuses;
    private List<ItemInfo> info = null;
    private BufferedImage tip = null;

    private CharWnd charWnd = null;

    public AttrBonusesWdg(int y) {
	super(new Coord(175, y));
    }

    public void update(WItem[] items) {
	this.items = items;
	needUpdate = true;

    }

    @Override
    public void draw(GOut g) {
	if(needRedraw) {
	    render();
	}

	if(tip != null) {
	    g.image(tip, Coord.z);
	}
    }

    private void render() {
	try {
	    if(info != null && !info.isEmpty()) {
		tip = ItemInfo.longtip(info);
	    } else {
		tip = null;
	    }

	    needRedraw = false;
	} catch (Loading ignored) {}
    }

    @Override
    public void tick(double dt) {
	super.tick(dt);
	if(needUpdate) {
	    doUpdate();
	}
	if(charWnd == null) {
	    charWnd = ui.gui.chrwdg;
	    if(charWnd != null) {needBuild = true;}
	}
	if(needBuild) {
	    build();
	}
    }

    private void doUpdate() {
	try {
	    bonuses = Arrays.stream(items)
		.filter(wItem -> wItem != null)
		.map(WItem::bonuses)
		.map(Map::entrySet)
		.flatMap(Collection::stream)
		.collect(
		    Collectors.toMap(
			Entry::getKey,
			Entry::getValue,
			Integer::sum
		    )
		);

	    needUpdate = false;
	    needBuild = true;
	} catch (Loading ignored) {}
    }

    private void build() {
	try {
	    if(bonuses != null) {
		info = Collections.singletonList(
		    make(bonuses.entrySet()
			.stream()
			.sorted(this::BY_PRIORITY)
			.collect(Collectors.toList())
		    )
		);
	    }

	    needBuild = false;
	    needRedraw = true;
	} catch (Loading ignored) {}
    }

    private ItemInfo make(Collection<Entry<Resource, Integer>> mods) {
	Resource res = Resource.remote().load("ui/tt/attrmod").get();
	ItemInfo.InfoFactory f = res.layer(Resource.CodeEntry.class).get(ItemInfo.InfoFactory.class);
	Object[] args = new Object[mods.size() * 2 + 1];
	int i = 1;
	for (Entry<Resource, Integer> entry : mods) {
	    args[i] = ui.sess.getresid(entry.getKey());
	    args[i + 1] = entry.getValue();
	    i += 2;
	}
	return f.build(this, args);
    }

    private int statIndex(Resource res) {
	if(charWnd != null) {
	    List<CharWnd.Attr> base = charWnd.base;
	    return IntStream.range(0, base.size())
		.filter(i -> base.get(i).res == res)
		.findFirst().orElse(Integer.MAX_VALUE);
	}
	return Integer.MAX_VALUE;
    }

    private int skillIndex(Resource res) {
	if(charWnd != null) {
	    List<CharWnd.SAttr> skill = charWnd.skill;
	    return IntStream.range(0, skill.size())
		.filter(i -> skill.get(i).res == res)
		.findFirst().orElse(Integer.MAX_VALUE);
	}
	return Integer.MAX_VALUE;
    }

    private int BY_PRIORITY(Entry<Resource, Integer> o1, Entry<Resource, Integer> o2) {
	Resource r1 = o1.getKey();
	Resource r2 = o2.getKey();

	int b1 = statIndex(r1);
	int b2 = statIndex(r2);

	if(b1 == b2) {
	    b1 = skillIndex(r1);
	    b2 = skillIndex(r2);
	    if(b1 == b2) {
		return r1.name.compareTo(r2.name);
	    } else {
		return Integer.compare(b1, b2);
	    }
	} else {
	    return Integer.compare(b1, b2);
	}
    }

    @Override
    public Glob glob() {
	return ui.sess.glob;
    }

    @Override
    public List<ItemInfo> info() {
	return info;
    }
}

package haven;

import java.util.*;

import static haven.FlowerMenu.AUTOCHOOSE;

public class FlowerList extends WidgetList<FlowerList.Item> {

    public static final Comparator<Item> ITEM_COMPARATOR = new Comparator<Item>() {
	@Override
	public int compare(Item o1, Item o2) {
	    return o1.name.compareTo(o2.name);
	}
    };

    public FlowerList() {
	super(new Coord(200, 25), 10);

	for(Map.Entry<String, Boolean> entry : AUTOCHOOSE.entrySet()) {
	    additem(new Item(entry.getKey()));
	}

	update();
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	switch(msg) {
	    case "changed": {
		String name = (String) args[0];
		boolean val = (Boolean) args[1];
		synchronized(AUTOCHOOSE) {
		    AUTOCHOOSE.put(name, val);
		}
		FlowerMenu.saveAutochoose();
		break;
	    }
	    case "delete": {
		String name = (String) args[0];
		synchronized(AUTOCHOOSE) {
		    AUTOCHOOSE.remove(name);
		}
		FlowerMenu.saveAutochoose();
		removeitem((Item) sender);
		ui.destroy(sender);
		update();
		break;
	    }
	    default:
		super.wdgmsg(sender, msg, args);
		break;
	}
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void add(String name) {
	if(name != null && !name.isEmpty() && !AUTOCHOOSE.containsKey(name)) {
	    synchronized(AUTOCHOOSE) {
		AUTOCHOOSE.put(name, true);
	    }
	    FlowerMenu.saveAutochoose();
	    additem(new Item(name));
	    update();
	}
    }

    private void update() {
	Collections.sort(list, ITEM_COMPARATOR);
	int n = listitems();
	for(int i = 0; i < n; i++) {
	    listitem(i).c = itempos(i);
	}
    }

    protected static class Item extends Widget {

	public final String name;
	private final CheckBox cb;
	private boolean a = false;
	private UI.Grab grab;

	public Item(String name) {
	    super(new Coord(200, 25));
	    this.name = name;

	    cb = add(new CheckBox(name), 3, 3);
	    cb.a = AUTOCHOOSE.get(name);
	    cb.canactivate = true;

	    add(new Button(24, "X"), 175, 0);
	}

	@Override
	public boolean mousedown(Coord c, int button) {
	    if(super.mousedown(c, button)) {
		return true;
	    }
	    if(button != 1)
		return (false);
	    a = true;
	    grab = ui.grabmouse(this);
	    return (true);
	}

	@Override
	public boolean mouseup(Coord c, int button) {
	    if(a && button == 1) {
		a = false;
		if(grab != null) {
		    grab.remove();
		    grab = null;
		}
		if(c.isect(new Coord(0, 0), sz))
		    click();
		return (true);
	    }
	    return (false);
	}

	private void click() {
	    cb.a = !cb.a;
	    wdgmsg("changed", name, cb.a);
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
	    switch(msg) {
		case "ch":
		    wdgmsg("changed", name, (int) args[0] > 0);
		    break;
		case "activate":
		    wdgmsg("delete", name);
		    break;
		default:
		    super.wdgmsg(sender, msg, args);
		    break;
	    }
	}
    }
}

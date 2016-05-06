package haven;

public class GildingWnd extends Window implements DTarget2, DropTarget{
    private final WItem target;
    private final WItem gild;

    public GildingWnd(WItem target, WItem gild) {
	super(new Coord(200, 100), "Gilding");
	justclose = true;

	this.target = target;
	this.gild = gild;

	add(new FWItem(target.item), 10, 10);
	add(new FWItem(gild.item), 60, 10);

	add(new Button(120, "Gild") {
	    @Override
	    public void click() {
		gild();
	    }
	}, 35, 80);
    }

    private void gild() {
	target.item.wdgmsg("itemact", ui.modflags());
    }

    @Override
    public void cdraw(GOut g) {
    }

    @Override
    public void close() {
	super.close();
    }

    @Override
    public boolean drop(WItem target, Coord cc, Coord ul) {
	return mousedown(cc, 0);
    }

    @Override
    public boolean iteminteract(WItem target, Coord cc, Coord ul) {
	return mousedown(cc, 1);
    }

    @Override
    public boolean dropthing(Coord cc, Object thing) {
	return mousedown(cc, 0);
    }

    public static void create(UI ui, WItem target, WItem gild) {
	ui.gui.add(new GildingWnd(target, gild));
    }

    private static class FWItem extends WItem {
	public FWItem(GItem item) {
	    super(item);
	}

	@Override
	public boolean iteminteract(WItem target, Coord cc, Coord ul) {
	    return false;
	}

	@Override
	public boolean mousedown(Coord c, int btn) {
	    return false;
	}
    }

}

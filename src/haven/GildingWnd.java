package haven;

import java.awt.image.BufferedImage;

public class GildingWnd extends Window {
    private final WItem target;
    private final WItem gild;
    private final BufferedImage igild;
    private final BufferedImage islots;
    private UI.Grab mg;

    public GildingWnd(WItem target, WItem gild) {
	super(new Coord(200, 100), "Gilding");
	justclose = true;

	this.target = target;
	this.gild = gild;

	gild.hide();

	igild = ItemInfo.longtip(gild.gilding.get());
	islots = ItemInfo.longtip(target.slots.get());

	int h = Math.max(igild.getHeight(), islots.getHeight());
	int w = igild.getWidth()+islots.getWidth();

	resize(new Coord(w+20, h+70));

	add(new FWItem(target.item), 10, 5);
	add(new FWItem(gild.item), asz.x-5-gild.sz.x, 5);

	add(new Button(120, "Gild") {
	    @Override
	    public void click() {
		gild();
	    }
	}, asz.x/2-60, h+50);
    }

    @Override
    protected void added() {
	super.added();
	mg = ui.grabmouse(this);
    }

    private void gild() {
	target.item.wdgmsg("itemact", ui.modflags());
    }

    @Override
    public void cdraw(GOut g) {
	g.image(islots, new Coord(5, 40));
	g.image(igild, new Coord(asz.x-5-igild.getWidth(), 40));
    }

    @Override
    public boolean mousedown(Coord c, int button) {
	if(c.isect(Coord.z, sz)){
	    super.mousedown(c, button);
	} else {
	    close();
	}
	return true;
    }

    @Override
    public void close() {
	gild.show();
	if(mg != null) {
	    mg.remove();
	}
	mg = null;
	super.close();
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

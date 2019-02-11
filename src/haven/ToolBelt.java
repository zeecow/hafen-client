package haven;

import java.awt.event.KeyEvent;

import static haven.Inventory.*;

public class ToolBelt extends DraggableWidget implements DTarget, DropTarget {
    public static final int GAP = 10;
    public static final int PAD = 2;
    public static final Coord INVSZ = invsq.sz();
    public final int[] beltkeys = {KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
	KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8,
	KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12};
    private final int group = 4;
    private final int start = 132;
    private final int size = beltkeys.length;
    private boolean vertical = false;
    
    public ToolBelt(String name) {
	super(name);
	resize();
    }
    
    private void resize() {
	sz = beltc(size - 1).add(INVSZ);
    }
    
    private Indir<Resource> belt(int i) {
	return (ui != null && ui.gui != null) ? ui.gui.belt[i] : null;
    }
    
    private Coord beltc(int i) {
	return vertical ?
	    new Coord(0, ((INVSZ.y + PAD) * i) + (GAP * (i / group))) :
	    new Coord(((INVSZ.x + PAD) * i) + (GAP * (i / group)), 0);
    }
    
    private int beltslot(Coord c) {
	for (int i = 0; i < size; i++) {
	    if(c.isect(beltc(i), invsq.sz())) {
		return slot(i);
	    }
	}
	return (-1);
    }
    
    @Override
    public void draw(GOut g) {
	for (int i = 0; i < size; i++) {
	    Coord c = beltc(i);
	    int slot = slot(i);
	    g.image(invsq, c);
	    try {
		Indir<Resource> item = belt(slot);
		if(item != null) {
		    Resource.Image img = item.get().layer(Resource.imgc);
		    if(img == null)
			throw (new NullPointerException("No image in " + item.get().name));
		    g.image(img.tex(), c.add(1, 1));
		}
	    } catch (Loading ignored) {}
	    g.chcolor(156, 180, 158, 255);
	    FastText.aprintf(g, c.add(INVSZ.sub(2, 0)), 1, 1, "F%d", i + 1);
	    g.chcolor();
	}
    }
    
    private int slot(int i) {return i + start;}
    
    @Override
    public boolean globtype(char key, KeyEvent ev) {
	if(key != 0 || ui.modflags() != 0) { return false;}
	for (int i = 0; i < beltkeys.length; i++) {
	    if(ev.getKeyCode() == beltkeys[i]) {
		keyact(slot(i));
		return true;
	    }
	}
	return false;
    }
    
    public void keyact(final int slot) {
	MapView map = ui.gui.map;
	if(map != null) {
	    Coord mvc = map.rootxlate(ui.mc);
	    if(mvc.isect(Coord.z, map.sz)) {
		map.delay(map.new Hittest(mvc) {
		    protected void hit(Coord pc, Coord2d mc, MapView.ClickInfo inf) {
			Object[] args = {slot, 1, ui.modflags(), mc.floor(OCache.posres)};
			if(inf != null) { args = Utils.extend(args, MapView.gobclickargs(inf));}
			ui.gui.wdgmsg("belt", args);
		    }
		    
		    protected void nohit(Coord pc) {
			ui.gui.wdgmsg("belt", slot, 1, ui.modflags());
		    }
		});
	    }
	}
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
	if(button == 3 && ui.modshift) {
	    reorient();
	    return true;
	}
	int slot = beltslot(c);
	if(slot != -1) {
	    if(button == 1) {
		ui.gui.wdgmsg("belt", slot, 1, ui.modflags());
	    } else if(button == 3) {
		ui.gui.wdgmsg("setbelt", slot, 1);
	    }
	    if(belt(slot) != null) {return true;}
	}
	return super.mousedown(c, button);
    }
    
    private void reorient() {
	vertical = !vertical;
	resize();
    }
    
    public boolean drop(Coord c, Coord ul) {
	int slot = beltslot(c);
	if(slot != -1) {
	    ui.gui.wdgmsg("setbelt", slot, 0);
	    return true;
	}
	return false;
    }
    
    public boolean iteminteract(Coord c, Coord ul) {return false;}
    
    public boolean dropthing(Coord c, Object thing) {
	int slot = beltslot(c);
	if(slot != -1) {
	    if(thing instanceof Resource) {
		Resource res = (Resource) thing;
		if(res.layer(Resource.action) != null) {
		    ui.gui.wdgmsg("setbelt", slot, res.name);
		    return true;
		}
	    }
	}
	return false;
    }
}

package haven;

import java.awt.*;
import java.awt.event.KeyEvent;

import static haven.Inventory.*;

public class ToolBelt extends DraggableWidget implements DTarget, DropTarget {
    public static final int GAP = 10;
    public static final int PAD = 2;
    public static final int BTNSZ = 17;
    public static final Coord INVSZ = invsq.sz();
    public static final Color BG_COLOR = new Color(43, 54, 35, 202);
    public final int[] beltkeys = {KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
	KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8,
	KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12};
    private final int group = 4;
    private final int start = 132;
    private final int size = beltkeys.length;
    private final IButton btnLock, btnULock, btnFlip;
    private boolean vertical = false, over = false, locked = false;
    
    public ToolBelt(String name) {
        //TODO: Add way to customize key binds, size, grouping, start index etc.
	super(name);
	btnULock = add(new IButton("gfx/hud/btn-ulock", "", "-d", "-h"));
	btnULock.action(this::toggle);
	btnULock.recthit = true;
	
	btnLock = add(new IButton("gfx/hud/btn-lock", "", "-d", "-h"));
	btnLock.action(this::toggle);
	btnLock.recthit = true;
	
	btnFlip = add(new IButton("gfx/hud/btn-flip", "", "-d", "-h"));
	btnFlip.action(this::flip);
	btnFlip.recthit = true;
    }
    
    @Override
    protected void initCfg() {
	super.initCfg();
	locked = (boolean) cfg.getValue("locked", locked);
	vertical = (boolean) cfg.getValue("vertical", vertical);
	resize();
	update_buttons();
    }
    
    private void update_buttons() {
	btnLock.visible = locked;
	btnULock.visible = !locked;
	btnFlip.visible = !locked;
	if(vertical) {
	    btnLock.c = btnULock.c = new Coord(BTNSZ, 0);
	    btnFlip.c = Coord.z;
	} else {
	    btnLock.c = btnULock.c = new Coord(0, BTNSZ);
	    btnFlip.c = Coord.z;
	}
    }
    
    private void resize() {
	sz = beltc(size - 1).add(INVSZ);
    }
    
    private void toggle() {
	locked = !locked;
        draggable(!locked);
	update_buttons();
	cfg.setValue("locked", locked);
	storeCfg();
    }
    
    private void flip() {
	vertical = !vertical;
	resize();
	update_buttons();
	cfg.setValue("vertical", vertical);
	storeCfg();
    }
    
    private Indir<Resource> belt(int i) {
        //TODO: Add ability to save and use custom MenuGrid actions on this belt
	return (ui != null && ui.gui != null) ? ui.gui.belt[i] : null;
    }
    
    private Coord beltc(int i) {
	return vertical ?
	    new Coord(0, BTNSZ + ((INVSZ.y + PAD) * i) + (GAP * (i / group))) :
	    new Coord(BTNSZ + ((INVSZ.x + PAD) * i) + (GAP * (i / group)), 0);
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
	if(over) {
	    if(!locked) {
		g.chcolor(BG_COLOR);
		g.frect(Coord.z, sz);
		g.chcolor();
	    }
	    super.draw(g);
	}
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
        //TODO: Make actions draggable if not locked
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
    
    @Override
    public void mousemove(Coord c) {
	over = c.isect(Coord.z, sz);
	super.mousemove(c);
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

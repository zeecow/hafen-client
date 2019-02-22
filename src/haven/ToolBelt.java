package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static haven.Inventory.*;

public class ToolBelt extends DraggableWidget implements DTarget, DropTarget {
    private static final Text.Foundry fnd = new Text.Foundry(Text.sans, 12);
    public static final int GAP = 10;
    public static final int PAD = 2;
    public static final int BTNSZ = 17;
    public static final Coord INVSZ = invsq.sz();
    public static final Color BG_COLOR = new Color(43, 54, 35, 202);
    public static final int[] FKEYS = {KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
	KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8,
	KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12};
    private static final String CONFIG_JSON = "belts.json";
    private static final Gson gson;
    private static Map<String, Map<Integer, String>> config;
    private final int[] beltkeys;
    private Map<Integer, String> usercfg;
    private final MenuGrid.Pagina[] custom;
    private final int group;
    private final int start;
    private final int size;
    private final ToggleButton btnLock;
    private final IButton btnFlip;
    private boolean vertical = false, over = false, locked = false;
    final Tex[] keys;
    private Indir<Resource> last = null;
    private Tex ttip = null;
    
    
    static {
	gson = (new GsonBuilder()).setPrettyPrinting().create();
	load();
    }
    
    private static void load() {
	try {
	    Type type = new TypeToken<Map<String, Map<Integer, String>>>() {
	    }.getType();
	    config = gson.fromJson(Config.loadFile(CONFIG_JSON), type);
	} catch (Exception ignore) {}
	if(config == null) {
	    config = new HashMap<>();
	}
    }
    
    private static void save() {
	Config.saveFile(CONFIG_JSON, gson.toJson(config));
    }
    
    public ToolBelt(String name, int start, int group, int[] beltkeys) {
	super(name);
	this.start = start;
	this.group = group;
	this.beltkeys = beltkeys;
	size = beltkeys.length;
	keys = new Tex[size];
	custom = new MenuGrid.Pagina[size];
	loadBelt();
	for (int i = 0; i < size; i++) {
	    if(beltkeys[i] != 0) {
		keys[i] = Text.renderstroked(KeyEvent.getKeyText(beltkeys[i]), fnd).tex();
	    }
	}
    
	btnLock = add(new ToggleButton("gfx/hud/btn-ulock", "", "-d", "-h", "gfx/hud/btn-lock", "", "-d", "-h"));
	btnLock.action(this::toggle);
	btnLock.recthit = true;
	
	btnFlip = add(new IButton("gfx/hud/btn-flip", "", "-d", "-h"));
	btnFlip.action(this::flip);
	btnFlip.recthit = true;
    }
    
    private void loadBelt() {
	String path = Config.userpath();
	if(!config.containsKey(path)) {
	    config.put(path, new HashMap<>());
	}
	usercfg = config.get(path);
    }
    
    @Override
    protected void attached() {
	super.attached();
	for (int i = 0; i < size; i++) {
	    String res = usercfg.get(slot(i));
	    if(res != null) {
		custom[i] = ui.gui.menu.paginafor(Resource.local().load(res));
	    }
	}
    }
    
    @Override
    protected void initCfg() {
	super.initCfg();
	locked = (boolean) cfg.getValue("locked", locked);
	vertical = (boolean) cfg.getValue("vertical", vertical);
	btnLock.state(locked);
	draggable(!locked);
	resize();
	update_buttons();
    }
    
    private void update_buttons() {
	btnFlip.visible = !locked;
	if(vertical) {
	    btnLock.c = new Coord(BTNSZ, 0);
	    btnFlip.c = Coord.z;
	} else {
	    btnLock.c = new Coord(0, BTNSZ);
	    btnFlip.c = Coord.z;
	}
    }
    
    private void resize() {
	sz = beltc(size - 1).add(INVSZ);
    }
    
    private void toggle(Boolean state) {
	locked = state != null ? state : false;
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
    
    private Indir<Resource> belt(int slot) {
	if(slot < 0) {return null;}
	Indir<Resource> res = custom[slot - start] != null ? custom[slot - start].res : null;
	if(ui != null && ui.gui != null && ui.gui.belt[slot] != null) {
	    res = ui.gui.belt[slot];
	}
	return res;
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
    
    private void setcustom(int slot, MenuGrid.Pagina p) {
	if(p != custom[slot - start]) {
	    custom[slot - start] = p;
	    usercfg.put(slot, p != null ? p.res().name : null);
	    save();
	}
    }
    
    private MenuGrid.Pagina getcustom(Resource res) {
	MenuGrid.Pagina p = ui.gui.menu.paginafor(res);
	return (p != null && p.button() instanceof MenuGrid.CustomPagButton) ? p : null;
    }
    
    private MenuGrid.Pagina getcustom(Indir<Resource> res) {
	try {
	    return res != null ? getcustom(res.get()) : null;
	} catch (Loading ignored) {}
	return null;
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
	    if(keys[i] != null) {
		g.aimage(keys[i], c.add(INVSZ.sub(2, 0)), 1, 1);
	    }
	}
    }
    
    private int slot(int i) {return i + start;}
    
    @Override
    public boolean globtype(char key, KeyEvent ev) {
	if(!visible || key != 0 || ui.modflags() != 0) { return false;}
	for (int i = 0; i < beltkeys.length; i++) {
	    if(ev.getKeyCode() == beltkeys[i]) {
		keyact(slot(i));
		return true;
	    }
	}
	return false;
    }
    
    public void keyact(final int slot) {
	MenuGrid.Pagina pagina = getcustom(belt(slot));
	if(pagina != null) {
	    pagina.button().use();
	    return;
	}
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
		MenuGrid.Pagina pagina = getcustom(belt(slot));
		if(pagina != null) {
		    pagina.button().use();
		} else {
		    ui.gui.wdgmsg("belt", slot, 1, ui.modflags());
		}
	    } else if(button == 3) {
		ui.gui.wdgmsg("setbelt", slot, 1);
		setcustom(slot, null);
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
    
    @Override
    public Object tooltip(Coord c, Widget prev) {
	int slot = beltslot(c);
	if(slot < 0) {return super.tooltip(c, prev);}
	Indir<Resource> res = belt(slot);
	if(res == null) {return super.tooltip(c, prev);}
	if(last != res) {
	    if(ttip != null) {ttip.dispose();}
	    ttip = null;
	    try {
		MenuGrid.Pagina p = ui.gui.menu.paginafor(res.get());
		if(p != null) {
		    ttip = ItemData.longtip(p, ui.sess);
		}
		last = res;
	    } catch (Loading ignored) {}
	}
	return ttip;
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
		    MenuGrid.Pagina pagina = getcustom(res);
		    if(pagina != null) {
			setcustom(slot, pagina);
			ui.gui.wdgmsg("setbelt", slot, 1); //clear default action in this slot
		    } else {
			setcustom(slot, null);
			ui.gui.wdgmsg("setbelt", slot, res.name);
		    }
		    return true;
		}
	    }
	}
	return false;
    }
}

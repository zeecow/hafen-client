package haven;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static haven.WidgetList.*;

public class KeyBinder {
    public static final int ALT = 1;
    public static final int CTRL = 2;
    public static final int SHIFT = 4;
    private List<KeyBind> binds = new ArrayList<>();
    
    public KeyBinder() {
	add(KeyEvent.VK_1, CTRL, gui -> gui.eqproxy.activate(0), "left hand");
	add(KeyEvent.VK_2, CTRL, gui -> gui.eqproxy.activate(1), "right hand");
	add(KeyEvent.VK_C, ALT, GameUI::toggleCraftList, "open craft list");
	add(KeyEvent.VK_B, ALT, GameUI::toggleBuildList, "open building list");
	add(KeyEvent.VK_A, ALT, GameUI::toggleActList, "open actions list");
	add(KeyEvent.VK_H, ALT, GameUI::toggleHand, "toggle cursor item");
	add(KeyEvent.VK_S, ALT, GameUI::toggleStudy, "toggle study window");
	add(KeyEvent.VK_F, ALT, gui -> gui.filter.toggle(), "show filter");
	add(KeyEvent.VK_I, ALT, gui -> CFG.DISPLAY_GOB_INFO.set(!CFG.DISPLAY_GOB_INFO.get(), true), "display info");
	add(KeyEvent.VK_H, CTRL, gui -> CFG.DISPLAY_GOB_HITBOX.set(!CFG.DISPLAY_GOB_HITBOX.get(), true), "display hitboxes");
	add(KeyEvent.VK_R, ALT, gui -> CFG.SHOW_GOB_RADIUS.set(!CFG.SHOW_GOB_RADIUS.get(), true), "display radius");
	add(KeyEvent.VK_G, CTRL, gui -> gui.map.togglegrid(), "show tile grid");
	add(KeyEvent.VK_Z, CTRL, gui ->
		{
		    Config.center_tile = !Config.center_tile;
		    gui.ui.message(String.format("Tile centering turned %s", Config.center_tile ? "ON" : "OFF"), GameUI.MsgType.INFO);
		}, "toggle tile centering"
	);
    }
    
    public boolean handle(UI ui, KeyEvent e) {
	int code = e.getKeyCode();
	int modflags = getModFlags(e.getModifiersEx());
    
	for (KeyBind bind : binds) {
	    if (bind.match(ui, code, modflags)) {
		return true;
	    }
	}
	
	return false;
    }
    
    public static int getModFlags(int modflags) {
	modflags = ((modflags & InputEvent.ALT_DOWN_MASK) != 0 ? ALT : 0)
	    | ((modflags & InputEvent.META_DOWN_MASK) != 0 ? ALT : 0)
	    | ((modflags & InputEvent.CTRL_DOWN_MASK) != 0 ? CTRL : 0)
	    | ((modflags & InputEvent.SHIFT_DOWN_MASK) != 0 ? SHIFT : 0);
	return modflags;
    }
    
    public void add(int code, int mods, Action action, String name) {
	binds.add(new KeyBind(code, mods, action, name));
    }
    
    public static KeyBind make(KeyEvent e, Action action, String name) {
	return new KeyBind(e.getKeyCode(), getModFlags(e.getModifiersEx()), action, name);
    }
    
    private void change(KeyBind from, KeyBind to) {
	binds.remove(from);
        binds.add(to);
    }
    
    public List<ShortcutWidget> makeWidgets() {
	List<ShortcutWidget> list = new ArrayList<>(binds.size());
	for (KeyBind bind : binds) {
	    list.add(new ShortcutWidget(bind));
	}
	return list;
    }
    
    public static class KeyBind {
	private final int code;
	private final int mods;
	private final Action action;
	public final String name;
	
	public KeyBind(int code, int mods, Action action, String name) {
	    this.code = code;
	    this.mods = mods;
	    this.action = action;
	    this.name = name;
	}
	
	public boolean match(UI ui, int code, int mods) {
	    boolean match = code == this.code && ((mods & this.mods) == this.mods);
	    if (match && ui.gui != null) {
		action.run(ui.gui);
	    }
	    return match;
	}
	
	public String shortcut() {
	    if(code == 0 && mods == 0) {return "Unbound";}
	    String key = KeyEvent.getKeyText(code);
	    if ((mods & SHIFT) != 0) {
		key = "SHIT+" + key;
	    }
	    if ((mods & ALT) != 0) {
		key = "ALT+" + key;
	    }
	    if ((mods & CTRL) != 0) {
		key = "CTRL+" + key;
	    }
	    return key;
	}
    }
    
    public interface Action {
	void run(GameUI gui);
    }
    
    public static class ShortcutWidget extends Widget implements ShortcutSelectorWdg.Result {
    
	private final Button btn;
    
	public ShortcutWidget(KeyBind bind) {
	    btn = add(new Button(75, bind.shortcut()){
		@Override
		public void click() {
		    ui.root.add(new ShortcutSelectorWdg(bind, ShortcutWidget.this), ui.mc.sub(75, 20));
		}
	    },
	    225, 0);
	    btn.autosize(true);
	    btn.c.x = 300 - btn.sz.x;
	    add(new Label(bind.name), 5, 5);
	}
    
	@Override
	public void keyBindChanged(KeyBind from, KeyBind to) {
	    btn.change(to.shortcut());
	    btn.c.x = 300 - btn.sz.x;
	    ui.root.keybinds.change(from, to);
	}
    }
    
    private static class ShortcutSelectorWdg extends Widget {
	private static final Color BGCOLOR = new Color(32, 64, 32, 196);
	private final KeyBind bind;
	private final Result listener;
    
	private UI.Grab keygrab;
	private UI.Grab mousegrab;
	
	public ShortcutSelectorWdg(KeyBind bind, Result listener) {
	    this.bind = bind;
	    this.listener = listener;
	    sz = new Coord(150, 45);
	    add(new Label("Press any key..."));
	}
    
	@Override
	public boolean keydown(KeyEvent ev) {
	    int code = ev.getKeyCode();
	    if(    code != 0
		&& code != KeyEvent.VK_CONTROL
		&& code != KeyEvent.VK_SHIFT
		&& code != KeyEvent.VK_ALT
		&& code != KeyEvent.VK_META) {
		if(code != KeyEvent.VK_ESCAPE) {
		    listener.keyBindChanged(bind, make(ev, bind.action, bind.name));
		}
		remove();
	    }
	    return true;
	}
    
	@Override
	public boolean type(char key, KeyEvent ev) {
	    
	    return true;
	}
	
	@Override
	protected void attach(UI ui) {
	    super.attach(ui);
	    keygrab = ui.grabkeys(this);
	    mousegrab = ui.grabmouse(this);
	}
	
	@Override
	public boolean mousedown(Coord c, int button) {
	    remove();
	    return true;
	}
	
	public void remove() {
	    mousegrab.remove();
	    keygrab.remove();
	    reqdestroy();
	}
	
	@Override
	public void draw(GOut g) {
	    g.chcolor(BGCOLOR);
	    g.frect(Coord.z, sz);
	    g.chcolor();
	    BOX.draw(g, Coord.z, sz);
	    super.draw(g);
	}
	
	public interface Result {
	    void keyBindChanged(KeyBind from, KeyBind to);
	}
    }
}

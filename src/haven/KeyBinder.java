package haven;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class KeyBinder {
    public static final int ALT = 1;
    public static final int CTRL = 2;
    public static final int SHIFT = 4;
    private List<KeyBind> binds = new ArrayList<>();
    
    public KeyBinder() {
	add(KeyEvent.VK_1, CTRL, gui -> gui.eqproxy.activate(0));
	add(KeyEvent.VK_2, CTRL, gui -> gui.eqproxy.activate(1));
	add(KeyEvent.VK_C, ALT, GameUI::toggleCraftList);
	add(KeyEvent.VK_B, ALT, GameUI::toggleBuildList);
	add(KeyEvent.VK_A, ALT, GameUI::toggleActList);
	add(KeyEvent.VK_H, ALT, GameUI::toggleHand);
	add(KeyEvent.VK_S, ALT, GameUI::toggleStudy);
	add(KeyEvent.VK_F, ALT, gui -> gui.filter.toggle());
	add(KeyEvent.VK_I, ALT, gui -> CFG.DISPLAY_GOB_INFO.set(!CFG.DISPLAY_GOB_INFO.get(), true));
	add(KeyEvent.VK_H, CTRL, gui -> CFG.DISPLAY_GOB_HITBOX.set(!CFG.DISPLAY_GOB_HITBOX.get(), true));
	add(KeyEvent.VK_R, ALT, gui -> CFG.SHOW_GOB_RADIUS.set(!CFG.SHOW_GOB_RADIUS.get(), true));
	add(KeyEvent.VK_G, CTRL, gui -> gui.map.togglegrid());
	add(KeyEvent.VK_Z, CTRL, gui ->
		{
		    Config.center_tile = !Config.center_tile;
		    gui.ui.message(String.format("Tile centering turned %s", Config.center_tile ? "ON" : "OFF"), GameUI.MsgType.INFO);
		}
	);
    }
    
    public boolean handle(UI ui, KeyEvent e) {
	int code = e.getKeyCode();
	int modflags = e.getModifiersEx();
	modflags = ((modflags & InputEvent.ALT_DOWN_MASK) != 0 ? ALT : 0)
		| ((modflags & InputEvent.META_DOWN_MASK) != 0 ? ALT : 0)
		| ((modflags & InputEvent.CTRL_DOWN_MASK) != 0 ? CTRL : 0)
		| ((modflags & InputEvent.SHIFT_DOWN_MASK) != 0 ? SHIFT : 0);
	
	for (KeyBind bind : binds) {
	    if (bind.match(ui, code, modflags)) {
		return true;
	    }
	}
	
	return false;
    }
    
    public void add(int code, int mods, Action action) {
	binds.add(new KeyBind(code, mods, action));
    }
    
    public static class KeyBind {
	private final int code;
	private final int mods;
	private final Action action;
	
	public KeyBind(int code, int mods, Action action) {
	    this.code = code;
	    this.mods = mods;
	    this.action = action;
	}
	
	public boolean match(UI ui, int code, int mods) {
	    boolean match = code == this.code && ((mods & this.mods) == this.mods);
	    if (match && ui.gui != null) {
		action.run(ui.gui);
	    }
	    return match;
	}
    }
    
    public interface Action {
	void run(GameUI gui);
    }
}

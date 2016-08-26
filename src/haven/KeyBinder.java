package haven;

import java.awt.event.KeyEvent;

public class KeyBinder {
    public boolean handle(UI ui, KeyEvent e) {
	int code = e.getKeyCode();
	boolean CTRL = e.isControlDown();
	boolean ALT = e.isAltDown();

	if(CTRL && code == KeyEvent.VK_1) {
	    if(ui.gui != null) {
		ui.gui.eqproxy.activate(0);
	    }
	} else if(CTRL && code == KeyEvent.VK_2) {
	    if(ui.gui != null) {
		ui.gui.eqproxy.activate(1);
	    }
	} else if(ALT && code == KeyEvent.VK_C) {
	    if(ui.gui!=null){
		ui.gui.toggleCraftList();
	    }
	} else if(ALT && code == KeyEvent.VK_B) {
	    if(ui.gui!=null){
		ui.gui.toggleBuildList();
	    }
	} else if(ALT && code == KeyEvent.VK_A) {
	    if(ui.gui!=null){
		ui.gui.toggleActList();
	    }
	} else if(ALT && code == KeyEvent.VK_H) {
	    if(ui.gui!=null){
		ui.gui.toggleHand();
	    }
	} else if(ALT && code == KeyEvent.VK_S) {
	    if(ui.gui!=null){
		ui.gui.toggleStudy();
	    }
	} else if(ALT && code == KeyEvent.VK_F) {
	    if(ui.gui!=null){
		FilterWnd filter = ui.gui.filter;
		filter.show(!filter.visible);
	    }
	} else if(ALT && code == KeyEvent.VK_I) {
	    CFG.DISPLAY_GOB_INFO.set(!CFG.DISPLAY_GOB_INFO.get(), true);
	} else if(CTRL && code == KeyEvent.VK_H) {
	    CFG.DISPLAY_GOB_HITBOX.set(!CFG.DISPLAY_GOB_HITBOX.get(), true);
	} else if(ALT && code == KeyEvent.VK_R) {
	    CFG.SHOW_GOB_RADIUS.set(!CFG.SHOW_GOB_RADIUS.get(), true);
	} else if(CTRL && code == KeyEvent.VK_G) {
	    if(ui.gui!=null){
		ui.gui.map.togglegrid();
	    }
	} else if(CTRL && code == KeyEvent.VK_Z) {
	    Config.center_tile = !Config.center_tile;
	    ui.message(String.format("Tile centering turned %s", Config.center_tile ? "ON" : "OFF"), GameUI.MsgType.INFO);
	}

	return false;
    }
}

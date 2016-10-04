package haven;

public enum Action {
    ACT_HAND_0(gui -> gui.eqproxy.activate(0), "left hand"),
    ACT_HAND_1(gui -> gui.eqproxy.activate(1), "right hand"),
    OPEN_QUICK_CRAFT(GameUI::toggleCraftList, "open craft list"),
    OPEN_QUICK_BUILD(GameUI::toggleBuildList, "open building list"),
    OPEN_QUICK_ACTION(GameUI::toggleActList, "open actions list"),
    TOGGLE_CURSOR(GameUI::toggleHand, "toggle cursor item"),
    TOGGLE_STUDY(GameUI::toggleStudy, "toggle study window"),
    FILTER(gui -> gui.filter.toggle(), "show filter"),
    TOGGLE_GOB_INFO(gui -> CFG.DISPLAY_GOB_INFO.set(!CFG.DISPLAY_GOB_INFO.get(), true), "display info"),
    TOGGLE_GOB_HITBOX(gui -> CFG.DISPLAY_GOB_HITBOX.set(!CFG.DISPLAY_GOB_HITBOX.get(), true), "display hitboxes"),
    TOGGLE_GOB_RADIUS(gui -> CFG.SHOW_GOB_RADIUS.set(!CFG.SHOW_GOB_RADIUS.get(), true), "display radius"),
    TOGGLE_TILE_GRID(gui -> gui.map.togglegrid(), "show tile grid"),
    TOGGLE_TILE_CENTERING(gui ->
    {
	Config.center_tile = !Config.center_tile;
	gui.ui.message(String.format("Tile centering turned %s", Config.center_tile ? "ON" : "OFF"), GameUI.MsgType.INFO);
    }, "toggle tile centering");
    
    public final String name;
    private final Do action;
    public final String description;
    
    Action(Do action, String name, String description) {
	this.name = name;
	this.action = action;
	this.description = description;
    }
    
    Action(Do action, String name) {
	this(action, name, null);
    }
    
    public void run(GameUI gui) {
	action.run(gui);
    }
    
    interface Do {
	void run(GameUI gui);
    }
}

package haven;

import auto.Bot;

public enum Action {
    TOGGLE_INVENTORY(GameUI::toggleInventory, "Inventory"),
    TOGGLE_EQUIPMENT(GameUI::toggleEquipment, "Equipment"),
    TOGGLE_CHARACTER(GameUI::toggleCharacter, "Character Sheet"),
    TOGGLE_KIN_LIST(GameUI::toggleKinList, "Kith & Kin"),
    TOGGLE_OPTIONS(GameUI::toggleOptions, "Options"),
    TOGGLE_CHAT(GameUI::toggleChat, "Toggle Chat"),
    TOGGLE_MAP(GameUI::toggleMap, "Toggle Map"),
    TAKE_SCREENSHOT(GameUI::takeScreenshot, "Take Screenshot"),
    
    ACT_HAND_0(gui -> gui.eqproxy.activate(0), "Left hand", "Left click on left hand slot."),
    ACT_HAND_1(gui -> gui.eqproxy.activate(1), "Right hand", "Left click on right hand slot."),
    OPEN_QUICK_CRAFT(GameUI::toggleCraftList, "Open craft list", "Opens list of items you can craft. Start typing to narrow the list. Press Enter or double-click to select recipe."),
    OPEN_QUICK_BUILD(GameUI::toggleBuildList, "Open building list", "Opens list of objects you can build. Start typing to narrow the list. Press Enter or double-click to select building."),
    OPEN_QUICK_ACTION(GameUI::toggleActList, "Open actions list", "Opens list of actions you can perform. Start typing to narrow the list. Press Enter or double-click to perform action."),
    OPEN_CRAFT_DB(GameUI::toggleCraftDB, "Open crafting DB"),
    TOGGLE_CURSOR(GameUI::toggleHand, "Toggle cursor item", "Hide/show item on a cursor. Allows you to walk with item on cursor when hidden."),
    TOGGLE_STUDY(GameUI::toggleStudy, "Toggle study window"),
    FILTER(gui -> gui.filter.toggle(), "Show item filter"),
    TOGGLE_GOB_INFO(gui -> CFG.DISPLAY_GOB_INFO.set(!CFG.DISPLAY_GOB_INFO.get(), true), "Display info", "Display crop/tree growth and object health overlay."),
    TOGGLE_GOB_HITBOX(gui -> CFG.DISPLAY_GOB_HITBOX.set(!CFG.DISPLAY_GOB_HITBOX.get(), true), "Display hitboxes"),
    TOGGLE_GOB_RADIUS(gui -> CFG.SHOW_GOB_RADIUS.set(!CFG.SHOW_GOB_RADIUS.get(), true), "Display radius", "Displays effective radius of beehives/mine supports etc."),
    TOGGLE_TILE_GRID(gui -> gui.map.togglegrid(), "Show tile grid"),
    TOGGLE_TILE_CENTERING(gui ->
    {
	Config.center_tile = !Config.center_tile;
	gui.ui.message(String.format("Tile centering turned %s", Config.center_tile ? "ON" : "OFF"), GameUI.MsgType.INFO);
    }, "Toggle tile centering"),
    TEST(Bot::pickup_herbs, ""),
    CLEAR_PLAYER_DAMAGE(GobDamageInfo::clearPlayerDamage, "Clear damage from player"),
    CLEAR_ALL_DAMAGE(GobDamageInfo::clearAllDamage, "Clear damage from everyone");
    
    
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

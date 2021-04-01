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
    TOGGLE_MINIMAP_ICONS_SETTINGS(GameUI::toggleIconSettings, "Show minimap icon settings"),
    TOGGLE_TIMERS(GameUI::toggleTimers, "Toggle Timers"),
    TAKE_SCREENSHOT(GameUI::takeScreenshot, "Take Screenshot"),
    
    ACT_HAND_0(gui -> gui.eqproxy.activate(Equipory.SLOTS.HAND_LEFT, 1), "Left hand", "Left click on left hand slot."),
    ACT_HAND_1(gui -> gui.eqproxy.activate(Equipory.SLOTS.HAND_RIGHT,1), "Right hand", "Left click on right hand slot."),
    ACT_BELT(gui -> gui.eqproxy.activate(Equipory.SLOTS.BELT, 3), "Belt", "Right click on belt slot."),
    ACT_DRINK(Bot::drink, "Drink", "Drinks water."),
    OPEN_QUICK_CRAFT(GameUI::toggleCraftList, "Open craft list", "Opens list of items you can craft. Start typing to narrow the list. Press Enter or double-click to select recipe."),
    OPEN_QUICK_BUILD(GameUI::toggleBuildList, "Open building list", "Opens list of objects you can build. Start typing to narrow the list. Press Enter or double-click to select building."),
    OPEN_QUICK_ACTION(GameUI::toggleActList, "Open actions list", "Opens list of actions you can perform. Start typing to narrow the list. Press Enter or double-click to perform action."),
    OPEN_CRAFT_DB(GameUI::toggleCraftDB, "Open crafting DB"),
    TOGGLE_CURSOR(GameUI::toggleHand, "Toggle cursor item", "Hide/show item on a cursor. Allows you to walk with item on cursor when hidden."),
    TOGGLE_STUDY(GameUI::toggleStudy, "Toggle study window"),
    FILTER(gui -> gui.filter.toggle(), "Show item filter"),
    TOGGLE_GOB_INFO(CFG.DISPLAY_GOB_INFO, "Display info", "Display crop/tree growth and object health overlay."),
    TOGGLE_GOB_HITBOX(CFG.DISPLAY_GOB_HITBOX, "Display hitboxes"),
    TOGGLE_HIDE_TREES(CFG.HIDE_TREES, "Hide trees"),
    TOGGLE_GOB_RADIUS(CFG.SHOW_GOB_RADIUS, "Display radius", "Displays effective radius of beehives/mine supports etc."),
    TOGGLE_TILE_GRID(gui -> gui.map.togglegrid(), "Show tile grid"),
    TOGGLE_TILE_CENTERING(gui ->
    {
	Config.center_tile = !Config.center_tile;
	gui.ui.message(String.format("Tile centering turned %s", Config.center_tile ? "ON" : "OFF"), GameUI.MsgType.INFO);
    }, "Toggle tile centering"),
    BOT_PICK_ALL_HERBS(Bot::pickup, "Auto-pick stuff", "Will automatically pickup all herbs/mussels/clay/frogs/grasshoppers etc. in radius that can be changed in Options->General."),
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
    
    Action(CFG<Boolean> toggle, String name, String description) {
        this(gui -> toggle.set(!toggle.get(), true), name, description);
    }
    
    Action(CFG<Boolean> toggle, String name) {
        this(toggle, name, null);
    }
    
    public void run(GameUI gui) {
	action.run(gui);
    }
    
    interface Do {
	void run(GameUI gui);
    }
}

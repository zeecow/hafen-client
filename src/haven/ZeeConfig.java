package haven;

import haven.render.*;
import haven.res.ui.obj.buddy.Buddy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static haven.OCache.posres;


public class ZeeConfig {
    static final String CATEG_PVPANDSIEGE = "PVP and siege";
    static final String CATEG_AGROCREATURES = "Agressive creatures";
    static final String CATEG_RAREFORAGE = "Rare forageables";
    static final String CATEG_LOCRES = "Localized resources";
    static final String MAP_GOB_AUDIO = "mapGobSaved";
    static final String MAP_GOB_SPEECH = "mapGobSpeech";
    static final String MAP_ANIMAL_FORMAT = "mapAnimalFormat";
    static final String MAP_ANIMAL_FORMAT_PIG = "pig";
    static final String MAP_ANIMAL_FORMAT_HORSE = "horse";
    static final String MAP_ANIMAL_FORMAT_CATTLE = "cattle";
    static final String MAP_ANIMAL_FORMAT_GOAT = "goat";
    static final String MAP_ANIMAL_FORMAT_SHEEP = "sheep";
    static final String MAP_GOB_CATEGORY = "mapGobCategory2";
    static final String MAP_CATEGORY_AUDIO = "mapCategoryAudio2";
    static final String MAP_CATEGORY_COLOR = "mapCategoryColor2";
    static final String MAP_CATEGORY_GOBS = "mapCategoryGobs2";
    static final String MAP_ACTION_USES = "mapActionUses2";
    static final String MAP_GOB_COLOR = "mapGobSettings2";
    static final String MAP_WND_POS = "mapWindowPos3";
    static final String WINDOW_NAME_CRAFT = "Makewindow";
    static final String WINDOW_NAME_XPEVT = "XpEvtWindow";

    static final String CURSOR_ARW = "gfx/hud/curs/arw";//cursor
    static final String CURSOR_ATK = "gfx/hud/curs/atk";
    static final String CURSOR_EAT = "gfx/hud/curs/eat";//feast
    static final String CURSOR_DIG = "gfx/hud/curs/dig";
    static final String CURSOR_HAND = "gfx/hud/curs/hand";//push,lift
    static final String CURSOR_HARVEST = "gfx/hud/curs/harvest";
    static final String CURSOR_MINE = "gfx/hud/curs/mine";//destroy
    static final String CURSOR_SHOOT = "gfx/hud/curs/shoot";
    static final String CURSOR_INSPECT = "gfx/hud/curs/study";
    static final String CURSOR_FISH = "gfx/hud/curs/fish";

    static final String POSE_HORSE_IDLE = "gfx/kritter/horse/idle";
    static final String POSE_HORSE_WALKING = "gfx/kritter/horse/walking";//speed 0
    static final String POSE_HORSE_PACE = "gfx/kritter/horse/pace";//speed 1
    static final String POSE_HORSE_TROT = "gfx/kritter/horse/trot";//speed 2
    static final String POSE_HORSE_GALLOP = "gfx/kritter/horse/gallop";//speed 3

    static final String POSE_PLAYER_RIDING_IDLE = "gfx/borka/riding-idle";//speed 0, 1
    static final String POSE_PLAYER_RIDING_TROT = "gfx/borka/riding-trot";//speed 2
    static final String POSE_PLAYER_RIDING_GALLOP = "gfx/borka/riding-gallop";//speed 3

    static final String POSE_PLAYER_KICKSLED_IDLE = "gfx/borka/sparkan-idle";
    static final String POSE_PLAYER_KICKSLED_ACTIVE = "gfx/borka/sparkan-sparkan";

    static final String POSE_PLAYER_CORACLE_IDLE = "gfx/borka/coracleidle";
    static final String POSE_PLAYER_CORACLE_ACTIVE = "gfx/borka/coraclerowan";
    static final String POSE_PLAYER_CORACLE_CAPE = "gfx/borka/coraclecape";
    static final String POSE_PLAYER_DUGOUT_IDLE = "gfx/borka/dugoutidle";
    static final String POSE_PLAYER_DUGOUT_ACTIVE = "gfx/borka/dugoutrowan";
    static final String POSE_PLAYER_ROWBOAT_IDLE = "gfx/borka/rowboat-d";
    static final String POSE_PLAYER_ROWBOAT_ACTIVE = "gfx/borka/rowing";

    static final String POSE_PLAYER_IDLE = "gfx/borka/idle";
    static final String POSE_PLAYER_WALK = "gfx/borka/walking";//speed 0, 1
    static final String POSE_PLAYER_RUN = "gfx/borka/running";//speed 2, 3
    static final String POSE_PLAYER_BUTCH = "gfx/borka/butcher";
    static final String POSE_PLAYER_BUILD = "gfx/borka/buildan";
    static final String POSE_PLAYER_SAWING = "gfx/borka/sawing";
    static final String POSE_PLAYER_CHIPPINGSTONE = "gfx/borka/chipping";//no pickaxe
    static final String POSE_PLAYER_CHOPBLOCK = "gfx/borka/choppan";
    static final String POSE_PLAYER_CHOPTREE = "gfx/borka/treechop";
    static final String POSE_PLAYER_DIGSHOVEL = "gfx/borka/shoveldig";
    static final String POSE_PLAYER_DIG = "gfx/borka/dig";
    static final String POSE_PLAYER_DRINK = "gfx/borka/drinkan";
    static final String POSE_PLAYER_LIFTING = "gfx/borka/banzai";
    static final String POSE_PLAYER_HARVESTING = "gfx/borka/harvesting";//reeds clearing
    static final String POSE_PLAYER_PICK = "gfx/borka/pickan";//pickaxe mining, chipping
    static final String POSE_PLAYER_BUSHPICK = "gfx/borka/bushpickan";//collect coal, pick bush
    static final String POSE_PLAYER_PICKGROUND = "gfx/borka/pickaxeanspot";
    static final String POSE_PLAYER_CARRYFLAT = "gfx/borka/carry-flat";//idle pickaxe
    static final String POSE_PLAYER_TRAVELHOMESHRUG = "gfx/borka/pointconfused";
    static final String POSE_PLAYER_TRAVELHOMEPOINT = "gfx/borka/pointhome";
    static final String POSE_PLAYER_THINK = "gfx/borka/thinkan";
    static final String POSE_PLAYER_DRIVE_WHEELBARROW = "gfx/borka/carry"; //same as pickaxe
    static final String POSE_PLAYER_CARRY_PICKAXE = "gfx/borka/carry"; //same as wheelbarrow
    static final String POSE_PLAYER_CARRY_SCYTHEARMS = "gfx/borka/scythearms";
    static final String POSE_PLAYER_PRESSINGWINE = "gfx/borka/winepressan";
    static final String POSE_PLAYER_PRESSINGWINE_IDLE = "gfx/borka/winepress-idle";
    static final String POSE_PLAYER_LOOM_IDLE = "gfx/borka/loomsit";
    static final String POSE_PLAYER_LOOM_WEAVING = "gfx/borka/weaving";
    static final String POSE_PLAYER_ROPE_WALKING = "gfx/borka/ropewalking";

    static final String TILE_WATER_FRESH_SHALLOW = "gfx/tiles/water";
    static final String TILE_WATER_FRESH_DEEP = "gfx/tiles/deep";
    static final String TILE_WATER_OCEAN_SHALLOW = "gfx/tiles/owater";
    static final String TILE_WATER_OCEAN_DEEP = "gfx/tiles/odeep";
    static final String TILE_WATER_OCEAN_DEEPER = "gfx/tiles/odeeper";

    static final String TILE_SWAMP = "gfx/tiles/swamp";
    static final String TILE_SWAMP_WATER = "gfx/tiles/swampwater";
    static final String TILE_SWAMP_BOG = "gfx/tiles/bog";
    static final String TILE_SWAMP_BOG_WATER = "gfx/tiles/bogwater";
    static final String TILE_SWAMP_FEN = "gfx/tiles/fen";
    static final String TILE_SWAMP_FEN_WATER = "gfx/tiles/fenwater";

    static final String TILE_BEACH = "gfx/tiles/beach";
    static final String TILE_SANDCLIFF = "gfx/tiles/sandcliff";
    static final String TILE_MOUNTAIN = "gfx/tiles/mountain";

    static final String DEF_LIST_MUTE_AUDIO = "Leashed horse.;Tracking is now turned;Stacking is now turned;must be empty to be unequipped";
    static final String DEF_LIST_CONFIRM_PETAL = "Empty,Swill,Clean out,Slaughter,Castrate,Unmoor,Declaim,Take possession,Renounce Lawspeaker,Become Lawspeaker,Pull hair,Disassemble";
    static final List<String> DEF_LIST_CONFIRM_BUTTON = List.of("Empty","Empty out","Abandon quest","Abandon credo");
    static final String DEF_LIST_WINDOWS_ADD_HIDE_BUTTON = "Inventory,Character Sheet,Basket,Creel,Cattle Roster,Quiver,Pickup Gobs,Tile Monitor,Switch Char";
    static final String DEF_LIST_BUTCH_AUTO = "Break,Scale,Wring neck,Kill,Skin,Flay,Pluck,Clean,Butcher,Collect bones";
    static final String DEF_LIST_AUTO_CLICK_MENU = "Pick,Harvest wax";
    static final String DEF_LIST_SHAPEICON = "stalagoomba 1,diamond 7 1 0 0,255 255 0;/amberwash 2,diamond 7 0 1 1,255 102 0;/cavepuddle 2,diamond 7 0 1 1,0 204 102;/ladder 2,triangleUp 5 0 1 1,0 204 102;/minehole 2,triangleDown 5 0 1 1,0 204 102;/burrow 2,triangleDown 6 0 1 1,204 0 255;/spark 2,square 4 0 1 0,102 102 255;/snekkja 2,square 4 0 1 0,255 255 102;/dugout 2,square 4 0 1 0,255 255 102;/wheelbarrow 2,square 4 0 1 0,0 255 255;/cart 2,square 4 0 1 0,0 153 255;/knarr 2,square 4 0 1 0,255 255 102;/rowboat 2,square 4 0 1 0,255 255 102;/horse/ 1,square 4 0 1 0,0 204 0;items/arrow 2,triangleUp 5 0 1 1,102 255 204;milestone-stone-e 2,diamond 4 0 1 1,255 255 255;milestone-wood-e 2,diamond 4 0 1 1,255 255 255;/fishingnet 2,diamond 4 0 1 1,153 153 153;wonders/wellspring 1,diamond 5 0 1 1,0 255 255;/map/starshard 2,diamond 7 0 1 1,255 255 0";
    static final Color DEF_SIMPLE_WINDOW_COLOR = new Color(55, 64, 32, 255);
    static final Color DEF_GRID_COLOR = new Color(204, 204, 255, 45);
    static final int MINIMAP_DRAG_BUTTON = 3;
    static final int DEF_GOB_MAX_REQUEUE = 500000;

    static final int PLAYER_SPEED_CRAWL = 0;
    static final int PLAYER_SPEED_WALK = 1;
    static final int PLAYER_SPEED_RUN = 2;
    static final int PLAYER_SPEED_SPRINT = 3;

    static final int LOCATION_UNDEFINED = -1;
    static final int LOCATION_OUTSIDE = 0;
    static final int LOCATION_CELLAR = 1, DEF_LIGHT_CELLAR = 32;
    static final int LOCATION_CABIN = 2, DEF_LIGHT_CABIN = 144;
    static final int LOCATION_UNDERGROUND = 3, DEF_LIGHT_UNDERGROUND = 48;
    static int playerLocation = LOCATION_UNDEFINED;

    static Color COLOR_RED = new Color(255,0,0,200);
    static Color COLOR_ORANGE = new Color(255,128,0,200);
    static Color COLOR_YELLOW = new Color(255,255,0,200);
    static Color COLOR_MAGENTA = new Color(255,0,255,200);
    static Color COLOR_LIGHTBLUE = new Color(0, 255, 255, 200);

    static GameUI gameUI;
    private static String cursorName = CURSOR_ARW;
    static Window windowEquipment,windowInvMain, toggleEquipsLastWindowClicked;
    static Makewindow makeWindow;
    static ZeeInvMainOptionsWdg invMainoptionsWdg;
    static ZeeOptionsJFrame zeecowOptions;
    static Button btnMkWndSearchInput;
    static GobIcon.SettingsWindow.IconList iconList;
    static ChatUI.Channel multiChat;
    private static Widget iconListFilterBox;
    private static Inventory mainInv;
    static Glob glob;
    static boolean keepMapViewOverlay;

    static String playingAudio = null;
    static String lastUiMsg, uiMsgTextQuality, uiMsgTextBuffer;
    static long now, lastUiQualityMsgMs=0, lastUIMsgMs, lastHafenWarningMs=0;
    static Object[] lastMapViewClickArgs;
    static Gob lastMapViewClickGob;
    static String lastMapViewClickGobName;
    static Coord lastMapViewClickPc, lastMapViewClickPcPrev;
    static Coord2d lastMapViewClickMc, lastMapViewClickMcPrev;
    static int lastMapViewClickButton;
    static long lastMapViewClickMs;
    static Coord lastSavedOverlayStartCoord, lastSavedOverlayEndCoord;
    static int lastSavedOverlayModflags;
    static long lastSavedOverlayMs;
    static MCache.Overlay lastSavedOverlay;
    static GItem lastInvItemCreated;
    static String lastInvItemCreatedBaseName, lastInvItemCreatedName;
    static long lastInvItemCreatedMs;
    static Coord lastUiClickCoord;
    static Class<?> classMSRad;

    static int aggroRadiusTiles = Utils.getprefi("aggroRadiusTiles", 11);
    static boolean alertOnPlayers = Utils.getprefb("alertOnPlayers", true);
    static boolean barterStandMidclickAutoBuy = false;
    static boolean barterAutoDisableStacking = Utils.getprefb("barterStandStackingOff",true);
    static boolean clickIconStoatAggro = Utils.getprefb("clickIconStoatAggro", true);
    static boolean autoChipMinedBoulder = Utils.getprefb("autoChipMinedBoulder", true);
    static boolean autoClickMenuOption = Utils.getprefb("autoClickMenuOption", true);
    static String autoClickMenuOptionList = Utils.getpref("autoClickMenuOptionList", DEF_LIST_AUTO_CLICK_MENU);
    static boolean autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);
    static boolean autoToggleEquips = Utils.getprefb("autoToggleEquips", true);
    static boolean autoRunLogin = Utils.getprefb("autoRunLogin", true);
    static boolean autoToggleGridLines = Utils.getprefb("autoToggleGridLines", true);
    static boolean blockAudioMsg = Utils.getprefb("blockAudioMsg", true);
    static String blockAudioMsgList = Utils.getpref("blockAudioMsgList", DEF_LIST_MUTE_AUDIO);
    static boolean butcherMode = false;
    static boolean autoStack = false;//set at checkUiMsg()
    static String butcherAutoList = Utils.getpref("butcherAutoList", DEF_LIST_BUTCH_AUTO);
    static boolean cattleRosterHeight = Utils.getprefb("cattleRosterHeight", true);
    static double cattleRosterHeightPercentage = Utils.getprefd("cattleRosterHeightPercentage", 1.0);
    static boolean confirmPetalEatReduceFoodEff = Utils.getprefb("confirmPetalEatReduceFoodEff", true);
    static boolean confirmPetal = Utils.getprefb("confirmPetal", true);
    static String confirmPetalList = Utils.getpref("confirmPetalList", DEF_LIST_CONFIRM_PETAL);
    public static boolean confirmThrowingAxeOrSpear = Utils.getprefb("confirmThrowingAxeOrSpear", true);
    static boolean debugWidgetMsgs = false;//disabled by default
    static boolean debugCodeRes = Utils.getprefb("debugCodeRes", false);
    static boolean drinkKey = Utils.getprefb("drinkKey", true);
    static boolean dropHoldingItemAltKey = Utils.getprefb("dropHoldingItemAltKey", true);
    static boolean dropMinedCurios = Utils.getprefb("dropMinedCurios", true);
    static boolean dropMinedOre = Utils.getprefb("dropMinedOre", true);
    static boolean dropMinedOrePrecious = Utils.getprefb("dropMinedOrePrecious", true);
    static boolean dropMinedStones = Utils.getprefb("dropMinedStones", true);
    static boolean dropSeeds = false;
    static boolean dropSoil = false;
    static boolean destroyingTreelogs = false;
    static boolean equiporyCompact = Utils.getprefb("equiporyCompact", false);
    static boolean equipShieldOnCombat = Utils.getprefb("equipShieldOnCombat", false);
    static boolean farmerMode = false;
    static boolean freeGobPlacement = Utils.getprefb("freeGobPlacement", true);
    static boolean fishMoonXpAlert = Utils.getprefb("fishMoonXpAlert", true);
    static int gridColorInt = Utils.getprefi("gridColorInt",ZeeConfig.colorToInt(DEF_GRID_COLOR));
    public static boolean hideFxSmoke = Utils.getprefb("hideFxSmoke", true);
    public static boolean stopSomeAnimations = Utils.getprefb("stopSomeAnimations", true);
    static boolean hideTileTransitions = Utils.getprefb("hideTileTransitions", true);
    static boolean highlightCropsReady = Utils.getprefb("highlightCropsReady", true);
    static boolean isThinClient = false;
    static boolean autoTrackScents = Utils.getprefb("autoTrackScents", true);
    public static boolean isRainLimited = Utils.getprefb("isRainLimited", false);
    public static Integer rainLimitPerc = Utils.getprefi("rainLimitPerc", 25);
    public static boolean treeAnimation = Utils.getprefb("treeAnimation", false);
    static boolean keyCamSwitchShiftC = Utils.getprefb("keyCamSwitchShiftC", true);
    static boolean keyUpDownAudioControl = Utils.getprefb("keyUpDownAudioControl", true);
    static boolean autoHideWindows = Utils.getprefb("autoHideWindows", false);
    static String listWindowsAddHideButton = Utils.getpref("listWindowsAddHideButton", DEF_LIST_WINDOWS_ADD_HIDE_BUTTON);
    static List<String> listAutoHideWindowsActive = new ArrayList<>(Utils.getprefsl("listAutoHideWindowsActive",new String[]{}));
    static List<String> listAutoHideWindowsActiveFast = new ArrayList<>(Utils.getprefsl("listAutoHideWindowsActiveFast",new String[]{}));
    public static boolean miniTrees = Utils.getprefb("miniTrees", false);
    public static Integer miniTreesSize = Utils.getprefi("miniTreesSize", 50);
    static boolean noWeather = Utils.getprefb("noWeather", false);
    static boolean noFlavObjs = Utils.getprefb("noFlavObjs", true);
    static boolean scrollTransferItems = Utils.getprefb("scrollTransferItems", true);
    static boolean notifyBuddyOnline = Utils.getprefb("notifyBuddyOnline", false);
    static boolean pilerMode = false;
    static boolean pickupGobWindowKeepOpen = Utils.getprefb("pickupGobWindowKeepOpen", true);
    static boolean pickupGobWindowAutoRefresh = Utils.getprefb("pickupGobWindowAutoRefresh", false);
    static boolean shapeIcons = Utils.getprefb("shapeIcons", false);
    static String shapeIconsList = Utils.getpref("shapeIconsList", DEF_LIST_SHAPEICON);
    static boolean showIconsZoomOut = Utils.getprefb("showIconsZoomOut", true);
    static boolean showKinNames = Utils.getprefb("showKinNames", true);
    public static boolean simpleCrops = Utils.getprefb("simpleCrops", true);
    public static boolean simpleHerbs = Utils.getprefb("simpleHerbs", true);
    static boolean slowMiniMap = Utils.getprefb("slowMiniMap", true);
    static boolean sortActionsByUses = Utils.getprefb("sortActionsByUses", true);
    static boolean rememberWindowsPos = Utils.getprefb("rememberWindowsPos", true);
    static boolean showInspectTooltip = false;
    static boolean isPlayerFeasting = false;
    static boolean isPlayerCursorMining = false;
    static boolean simpleWindows = Utils.getprefb("simpleWindows", true);
    static int simpleWindowColorInt = Utils.getprefi("simpleWindowColorInt",ZeeConfig.colorToInt(DEF_SIMPLE_WINDOW_COLOR));
    static boolean simpleWindowBorder = Utils.getprefb("simpleWindowBorder", true);
    static boolean simpleButtons = Utils.getprefb("simpleButtons", true);
    static String windowShortMidclickTransferMode = "des";//default shortMidclick transfer descending
    static boolean liftVehicleBeforeTravelHearth = Utils.getprefb("liftVehicleBeforeTravelHearth", true);
    static int minimapScale = Utils.getprefi("minimapScale",1);
    static boolean researchFoodTips = Utils.getprefb("researchFoodTips", true);
    static boolean closeTamedAnimalWindowAfterNaming = Utils.getprefb("closeTamedAnimalWindowAfterNaming", true);
    static int gobMaxRequeues = Utils.getprefi("gobMaxRequeues",DEF_GOB_MAX_REQUEUE);
    static boolean minimapSolidColor = Utils.getprefb("minimapSolidColor",true);
    static boolean terrainSolidColor = Utils.getprefb("terrainSolidColor",false);
    static boolean pavingSolidColor = Utils.getprefb("pavingSolidColor",false);
    public static boolean showOverlayPclaim = Utils.getprefb("showOverlayPclaim",true);
    public static boolean showOverlayVclaim = Utils.getprefb("showOverlayVclaim",true);
    public static boolean showOverlayProv = Utils.getprefb("showOverlayProv",true);
    static boolean showHitbox = false;
    public static boolean autoHideWindowDelay = Utils.getprefb("autoHideWindowDelay",true);
    public static int autoHideWindowDelayMs = Utils.getprefi("autoHideWindowDelayMs",1000);
    public static boolean showGobPointer = Utils.getprefb("showGobPointer",false);
    public static boolean showGobRadar = Utils.getprefb("showGobRadar",false);
    static boolean autocloseXpWindow = Utils.getprefb("autocloseXpWindow",true);

    public static boolean playMidiRadio = Utils.getprefb("playMidiRadio",false);
    static Runnable playMidiRadioRunnable = () -> ZeeMidiRadio.toggleRadio();

    static boolean hideGobs = Utils.getprefb("hideGobs",false);
    static Runnable hideGobsRunnable = () -> ZeeManagerGobClick.toggleModelsAllGobs();

    public static boolean showGrowingTreeScale = Utils.getprefb("showGrowingTreeScale", true);
    static Runnable showGrowingTreeScaleRunnable = () -> ZeeManagerGobClick.toggleAllTreeGrowthTexts();

    public final static Set<String> mineablesStone = new HashSet<String>(Arrays.asList(
            "stone","gneiss","basalt","dolomite","feldspar","flint",
            "granite","hornblende","limestone","marble","porphyry","quartz",
            "sandstone","schist","blackcoal","zincspar","apatite","fluorospar",
            "gabbro","corund","kyanite","mica","microlite","orthoclase","soapstone",
            "sodalite","olivine","alabaster","breccia","diabase","arkose",
            "diorite","slate","arkose","eclogite","jasper","greenschist","pegmatite",
            "rhyolite","pumice","chert","graywacke","serpentine","sunstone"
    ));
    public final static Set<String> mineablesOre = new HashSet<String>(Arrays.asList(
            "cassiterite","chalcopyrite","malachite","ilmenite","cinnabar",
            "limonite","hematite","magnetite","leadglance","peacockore","cuprite"
    ));
    public final static Set<String> mineablesOrePrecious = new HashSet<String>(Arrays.asList(
            "galena","argentite","hornsilver", // silver
            "petzite","sylvanite","nagyagite" // gold
    ));
    public final static Set<String> mineablesCurios = new HashSet<String>(Arrays.asList(
            "catgold","petrifiedshell","strangecrystal","quarryquartz"
    ));
    public final static Set<String> localizedResources = new HashSet<String>(Arrays.asList(
            "gfx/terobjs/saltbasin",
            "gfx/terobjs/abyssalchasm",
            "gfx/terobjs/windthrow",
            "gfx/terobjs/icespire",
            "gfx/terobjs/woodheart",
            "gfx/terobjs/jotunmussel",
            "gfx/terobjs/guanopile",
            "gfx/terobjs/geyser",
            "gfx/terobjs/claypit",
            "gfx/terobjs/caveorgan",
            "gfx/terobjs/crystalpatch",
            "gfx/terobjs/fairystone",
            "gfx/terobjs/lilypadlotus",
            "gfx/terobjs/algaeblob"
    ));
    public final static Set<String> rareForageables = new HashSet<String>(Arrays.asList(
        "gfx/terobjs/herbs/flotsam",
        "gfx/terobjs/herbs/chimingbluebell",
        "gfx/terobjs/herbs/edelweiss",
        "gfx/terobjs/herbs/bloatedbolete",
        "gfx/terobjs/herbs/glimmermoss",
        "gfx/terobjs/herbs/camomile",
        "gfx/terobjs/herbs/cavecoral",
        "gfx/terobjs/herbs/clay-cave",
        "gfx/terobjs/map/cavepuddle",
        "gfx/terobjs/herbs/mandrake",
        "gfx/terobjs/herbs/seashell",
        "gfx/kritter/stalagoomba/stalagoomba"
    ));
    public final static Set<String> aggressiveGobs = new HashSet<String>(Arrays.asList(
            "gfx/kritter/adder/adder",
            "gfx/kritter/badger/badger",
            "gfx/kritter/bat/bat",
            "gfx/kritter/bear/bear",
            "gfx/kritter/boar/boar",
            "gfx/kritter/caveangler/caveangler",
            "gfx/kritter/goldeneagle/goldeneagle",
            "gfx/kritter/lynx/lynx",
            "gfx/kritter/mammoth/mammoth",
            "gfx/kritter/moose/moose",
            "gfx/kritter/rat/caverat",
            "gfx/kritter/troll/troll",
            "gfx/kritter/walrus/walrus",
            "gfx/kritter/goat/wildgoat",
            "gfx/kritter/wolf/wolf",
            "gfx/kritter/wolverine/wolverine"
    ));
    public final static Set<String> pvpGobs = new HashSet<String>(Arrays.asList(
            "gfx/terobjs/vehicle/bram",
            "gfx/terobjs/vehicle/catapult",
            "gfx/kritter/nidbane/nidbane",
            "gfx/terobjs/vehicle/wreckingball"
    ));


    static List<String> listGobsSession = new ArrayList<>();

    static HashMap<String,String> mapTamedAnimalNameFormat = initMapTamedAnimals();
    static HashMap<String, Set<String>> mapCategoryGobs = initMapCategoryGobs();//init categs first
    static HashMap<String,String> mapGobAudio = initMapGobAudio();
    static HashMap<String,String> mapGobSpeech = initMapGobSpeech();
    static HashMap<String,String> mapGobCategory = initMapGobCategory();
    static HashMap<String,String> mapCategoryAudio = initMapCategoryAudio();
    static HashMap<String,Integer> mapActionUses = initMapActionUses();
    static HashMap<String, Color> mapGobColor = initMapGobColor();
    static HashMap<String,Color> mapCategoryColor = initMapCategoryColor();
    static HashMap<String,Coord> mapWindowPos = initMapWindowPos();


    private static boolean isSpriteKind(Gob gob, String... kind) {
        List<String> kinds = Arrays.asList(kind);
        boolean result = false;
        Class spc;
        Drawable d = gob.getattr(Drawable.class);
        Resource.CodeEntry ce = gob.getres().layer(Resource.CodeEntry.class);
        if(ce != null) {
            spc = ce.get("spr");
            result = spc != null && (kinds.contains(spc.getSimpleName()) || kinds.contains(spc.getSuperclass().getSimpleName()));
        }
        if(!result) {
            if(d instanceof ResDrawable) {
                Sprite spr = ((ResDrawable) d).spr;
                if(spr == null) {throw new Loading();}
                spc = spr.getClass();
                result = kinds.contains(spc.getSimpleName()) || kinds.contains(spc.getSuperclass().getSimpleName());
            }
        }
        return result;
    }

    private static Message getDrawableData(Gob gob) {
        Drawable dr = gob.getattr(Drawable.class);
        ResDrawable d = (dr instanceof ResDrawable) ? (ResDrawable) dr : null;
        if(d != null)
            return d.sdt.clone();
        else
            return null;
    }

    public static MixColor getHighlightDrawableColor(Gob gob) {
        if(gob==null || gob.getres()==null)
            return null;

        String gobName;

        try{
            gobName = gob.getres().name;
        }catch (Resource.Loading loading){
            loading.printStackTrace();
            return null;
        }

        //System.out.printf("gobHighlightDrawable %s ", gobName);

        //if it's a crop
        if(highlightCropsReady && isGobCrop(gobName)) {
            //System.out.printf(" CROP \n");
            if (isCropMaxStage(gob))
                return new MixColor(COLOR_LIGHTBLUE);
        }
        //else System.out.printf(" NOPE \n");

        return null;
    }

    public static boolean isCropMaxStage(Gob gob) {
        boolean ret = false;
        int maxStage = 0;
        for (FastMesh.MeshRes layer : gob.getres().layers(FastMesh.MeshRes.class)) {
            if(layer.id / 10 > maxStage) {
                maxStage = layer.id / 10;
            }
        }
        Message data = getDrawableData(gob);
        if(data != null) {
            int stage = data.uint8();
            if(stage > maxStage)
                stage = maxStage;
            if(stage==maxStage)
                ret = true;
        }
        return ret;
    }

    public static MixColor getHighlightGobColor(Gob gob) {

        //get Type and name
        String gobName = gob.getres().name;
        String categ;
        Color c;

        if(ZeeConfig.mapGobColor.size() > 0   &&   (c = ZeeConfig.mapGobColor.get(gobName)) != null){
            //highlight gob 1st priority
            return new MixColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        }
        else if((categ = ZeeConfig.mapGobCategory.get(gobName)) != null){
            //highlight category 2nd priority
            c = mapCategoryColor.get(categ);
            if(c==null) {
                //set default categ color if empty
                c = ZeeConfig.COLOR_YELLOW;
                ZeeConfig.mapCategoryColor.put(categ, c);
            }
            return new MixColor(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
        }

        return null;
    }

    public static void resetDefaultCateg(String categ) {
        if(categ.contentEquals(ZeeConfig.CATEG_LOCRES)){
            ZeeConfig.mapCategoryGobs.put(ZeeConfig.CATEG_LOCRES, ZeeConfig.localizedResources);
        }else if(categ.contentEquals(ZeeConfig.CATEG_RAREFORAGE)){
            ZeeConfig.mapCategoryGobs.put(ZeeConfig.CATEG_RAREFORAGE, ZeeConfig.rareForageables);
        }else if(categ.contentEquals(ZeeConfig.CATEG_PVPANDSIEGE)){
            ZeeConfig.mapCategoryGobs.put(ZeeConfig.CATEG_PVPANDSIEGE, ZeeConfig.pvpGobs);
        }else if(categ.contentEquals(ZeeConfig.CATEG_AGROCREATURES)){
            ZeeConfig.mapCategoryGobs.put(ZeeConfig.CATEG_AGROCREATURES, ZeeConfig.aggressiveGobs);
        }
    }

    public static boolean isDefaultCateg(String categ) {
        if(categ.contentEquals(CATEG_LOCRES) || categ.contentEquals(CATEG_AGROCREATURES) || categ.contentEquals(CATEG_PVPANDSIEGE) || categ.contentEquals(CATEG_RAREFORAGE))
            return true;
        else
            return false;
    }

    public static boolean isPlayer(Gob gob) {
        boolean isMannequim = (gob.getattr(GobHealth.class) != null);// mannequim object has health attr
        return gob.getres().name.startsWith("gfx/borka/body") && !isMannequim;
    }

    public static boolean isTree(String gobName) {
        return ZeeManagerGobClick.isGobTree(gobName);
    }

    public static boolean isBush(String gobName) {
        return gobName.contains("/bushes/");
    }

    public static boolean isGobCrop(String gobName) {
        return gobName.startsWith("gfx/terobjs/plants/") && !gobName.endsWith("trellis") && !isGobWildCrop(gobName);
    }

    public static boolean isGobWildCrop(String gobName) {
        return gobName.startsWith("gfx/terobjs/plants/") &&
                (
                        gobName.contains("/wild") ||
                        gobName.contains("/stringgrass") ||
                        gobName.contains("/cereal") ||
                        gobName.contains("/gourd") ||
                        gobName.contains("/tuber")
                );
    }

    //  gfx/invobjs/turnip , gfx/invobjs/seed-turnip
    public static boolean isItemCrop(String basename) {
        final String crops = "beetroot,seed-turnip,turnip,seed-carrot,carrot,seed-flax,seed-hemp,seed-leek,leek,seed-poppy,"
            +"seed-pipeweed,seed-cucumber,seed-barley,seed-wheat,seed-millet,seed-lettuce,"
            +"seed-pumpkin";
        return crops.contains(basename);
    }

    public static boolean isBug(String resName){
        final List<String> l = List.of(
            "/silkmoth", "/grasshopper", "/ladybug", "/dragonfly",
            "/waterstrider", "/firefly", "/sandflea",
            "/cavemoth", "/stagbeetle", "/cavecentipede", "/moonmoth",
            "/monarchbutterfly", "/grub","/springbumblebee"
        );
        for (String s : l) {
            if(resName.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDungeonIconName(String iconName){
        //todo: ant dungeon
        final List<String> listContains = List.of(
                "/batcave","/beaverdam","/badgerden","/wolfslair","/beehive","/foxhole"
        );
        for (String listName : listContains) {
            if (iconName.endsWith(listName))
                return true;
        }
        return false;
    }

    public static boolean isLocResMapIconName(String iconName){
        final List<String> listContains = List.of(
                "/amberwash","abyssalchasm","geyser","guanopile","headwaters"
                ,"woodheart","icespire","irminsul","hiddenhollow","woodheart"
                ,"jotunmussel","algaeblob","lilypadlotus","monolith","crystalpatch"
                ,"saltbasin","spawningbed","watervortex","tarpit","irminsul"
        );
        for (String listName : listContains) {
            if (iconName.endsWith(listName))
                return true;
        }
        return false;
    }

    public static boolean isAggressiveIconName(String iconName){
        if (iconName.endsWith("/plo"))
            return true;
        if (isAggressive(iconName))
            return true;
        return false;
    }

    public static boolean isAggressive(String nameContains){
        final List<String> listContains = List.of(
               "/adder","/sandflea","/boar/","/badger/","/bear/","/bat/","/boreworm/",
                "/ooze/","/cavelouse/","/caveangler/","orca","/goldeneagle/","/lynx/",
                "/mammoth/","/moose/","/troll/","/walrus/","/goat/","/wolf/","/wolverine/",
                "spermwhale"
        );
        for (String listName : listContains) {
            if (nameContains.contains(listName))
                return true;
        }
        return false;
    }

    public static boolean isFish(String nameContains){
        return nameContains.contains("/fish-");
    }

    static boolean isAnimalHideTailEtc(String name){
        return name.endsWith("hide")
                || name.endsWith("-blood")
                || name.endsWith("squirreltail");
    }

    public static boolean isButchableSmallAnimal(String nameContains){
        // skip cases ("/squirrelhide", "squirrelhide-blood", "squirreltail")
        if (isAnimalHideTailEtc(nameContains))
            return false;
        final String[] endlist = {
            "rockdove","quail","/hen","/rooster","magpie", // "/crab"
            "mallard","seagull","ptarmigan","grouse",
            "/squirrel","/hedgehog","/bogturtle",
            "/rabbit-buck","-doe","/adder","/mole",
            "-dead","-plucked","-cleaned","-clean"
        };
        for (int i = 0; i < endlist.length; i++) {
            if(nameContains.contains(endlist[i]) || isFish(nameContains))
                return true;
        }
        return false;
    }

    public static boolean isSmallAnimal(String nameFull){
        final String[] endList = {"/toad"};//TODO bring more to this list
        for (int i = 0; i < endList.length; i++) {
            if(nameFull.endsWith(endList[i]))
                return true;
        }
        final String[] containsList = {
                "rockdove","quail","/hen","/rooster","magpie",
                "mallard","seagull","ptarmigan","grouse",
                "/rat/rat","/squirrel","/hedgehog","/bogturtle",
                "/rabbit-buck","rabbit-doe","/crab","/jellyfish",
                "/frog","/forestlizard","snail",
                "/adder"
        };
        for (int i = 0; i < containsList.length; i++) {
            if(nameFull.contains(containsList[i]))
                return true;
        }
        return false;
    }

    public static boolean isScent(String name){
        if(name.contains("clue-")) {
            return true;
        }
        return false;
    }

    public static boolean isSpice(String name){
        final String[] list = {
            "kvann", "chives", "dill","thyme","sage",
            "laurel","juniper",
            "ambergris",
            "truffle"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isString(String name){
        final String[] list = {
            "stingingnettle","taproot","cattail","toadflax"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    static boolean isKritter(Gob gob){
        return isKritter(gob.getres().name);
    }

    static boolean isKritter(String name){
        return name.contains("/kritter/");
    }

    static boolean isKritterNotPickable(Gob kritter) {
        String resname = kritter.getres().name;

        final String[] list = {
            //wild
            "gfx/kritter/badger/badger",
            "gfx/kritter/bear/bear",
            "gfx/kritter/boar/boar",
            "gfx/kritter/caveangler/caveangler",
            "gfx/kritter/lynx/lynx",
            "gfx/kritter/mammoth/mammoth",
            "gfx/kritter/moose/moose",
            "gfx/kritter/rat/caverat",
            "gfx/kritter/troll/troll",
            "gfx/kritter/walrus/walrus",
            "gfx/kritter/goat/wildgoat",
            "gfx/kritter/wolf/wolf",
            "gfx/kritter/wolverine/wolverine",
            "gfx/kritter/cattle/aurochs/",
            "gfx/kritter/sheep/mouflon",
            "gfx/kritter/reindeer/",
            "gfx/kritter/reddeer/",
            "gfx/kritter/roedeer/",
            "gfx/kritter/fox/",
            "gfx/kritter/beaver",
            "gfx/kritter/boreworm",
            "gfx/kritter/cavelouse",
            "gfx/kritter/ooze",
            "gfx/kritter/otter",
            "gfx/kritter/mammoth",
            "/spermwhale",
            "/orca",
            //tamed
            "gfx/kritter/horse/",
            "gfx/kritter/goat/",
            "gfx/kritter/sheep/",
            "gfx/kritter/cattle/",
            "gfx/kritter/pig/",
            //misc
            "gfx/kritter/midgeswarm/",
            "gfx/kritter/ants/",
            "gfx/kritter/wildbees/"
        };
        for (int i = 0; i < list.length; i++) {
            if(resname.contains(list[i]))
                return true;
        }

        // kritters pickable when KO only
        final List<String> listKO = List.of(
                "gfx/kritter/bat/bat",
                "gfx/kritter/stoat/stoat"
        );
        for (String kri : listKO) {
            if (resname.contentEquals(kri) && !ZeeManagerGobClick.isGobDeadOrKO(kritter)){
                return true;
            }
        }

        // giant toad questgivers not pickable
        if ( resname.endsWith("/toad") ) {
            if (ZeeManagerGobClick.getGAttrNames(kritter).contains("ObScale"))
                return true;
        }

        return false;
    }

    static boolean isBird(String name){
        final String[] list = {
            "rockdove","quail","eagle","owl","magpie",
            "mallard","pelican","seagull","swan","ptarmigan","grouse","bullfinch"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        final String[] listEnds = {
            "/chick","/hen","/rooster"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.endsWith(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isHerb(String name){
        return name.contains("/herbs/");
    }

    public static boolean isFlower(String name){
        final String[] list = {
            "bloodstern","camomile","cavebulb","chimingbluebell","clover","coltsfoot","dandelion",
            "edelweiss","frogscrown","heartsease","marshmallow","stingingnettle","thornythistle",
            "yarrow","snapdragon","wintergreen"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isMushroom(String name){
        final String[] list = {
            "bolete","truffle","trumpet","cavelantern","chantrelle","/lorchel","fairy","blewit",
            "puffball","indigo","parasol","snowtop","yellowfoot", "herbs/stalagoom",
            "oystermushroom", "/champignon-"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isTreeLeaf(String name){
        final String[] list = {
                "trees/maple","trees/conkertree","trees/mulberry","trees/fig"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isTreeToughBark(String name){
        final String[] list = {
            "trees/linden","trees/birch","trees/wartybirch","trees/willow","trees/cedar",
            "trees/elm","trees/juniper","trees/beech","trees/mulberry","trees/wychelm"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isTreeBough(String name){
        final String[] list = {
                "trees/linden","trees/alder","trees/yew","trees/spruce",
                "trees/elm","trees/fir","trees/sweetgum","trees/grayalder"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isTreeFruit(String name){
        final String[] list = {
            "trees/cherrie","trees/fig","trees/lemon","trees/medlar","trees/mulberry",
            "trees/pear","trees/persimmon","trees/plum","trees/quince","trees/apple",
            "trees/sorb"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isTreeNuts(String name){
        final String[] list = {
            "trees/almond","trees/beech","trees/chestnut","trees/hazel",
            "trees/walnut","trees/carob","trees/king","trees/oak",
            "bushes/witherstand"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static void applyGobSettingsHighlight(Gob gob, MixColor mc) {
        if(gob==null || mc==null)
            return;
        try {
            gob.setattr(new ZeeGobHighlight(gob, mc));
        } catch (Resource.Loading e) {
            e.printStackTrace();
        }
    }

    public static void applyGobSettingsAudio(Gob gob) {
        String gobName = gob.getres().name;
        long gobId = gob.id;
        String path = "";

        // other players
        if (gob.tags.contains(Gob.Tag.PLAYER_OTHER)) {
            if (autoHearthOnStranger && !playerHasAnyPose(POSE_PLAYER_TRAVELHOMEPOINT, POSE_PLAYER_TRAVELHOMESHRUG)) {
                autoHearth();
            }
            if (alertOnPlayers && !ZeeManagerGobClick.isGobDeadOrKO(gob)) {
                String audio = mapCategoryAudio.get(CATEG_PVPANDSIEGE);
                if (audio != null && !audio.isEmpty())
                    playAudioGobId(audio, gobId);
                else
                    gameUI.ui.msg("player spotted",Color.yellow,ZeeSynth.msgsfxPlayer);
            }
        }
        //if single gob alert is saved, play alert
        else if( (path = mapGobAudio.get(gobName)) != null){
            playAudioGobId(path,gobId);
        }
        // play category audio if gob applies
        else {
            //for each category in mapCategoryGobs...
            for (String categ: mapCategoryGobs.keySet()){
                if(categ==null || categ.isEmpty())
                    continue;
                //...check if gob is in category
                if(mapCategoryGobs.get(categ).contains(gobName)){
                    // skip aggressive audio if gob dead of KO
                    if (categ.contentEquals(CATEG_AGROCREATURES) && ZeeManagerGobClick.isGobDeadOrKO(gob))
                        continue;
                    //play audio for category
                    path = mapCategoryAudio.get(categ);
                    playAudioGobId(path, gobId);
                }
            }
        }

        // gob text to speech
        String speech = null;
        if( (speech = mapGobSpeech.get(gobName)) != null){
            ZeeSynth.textToSpeakLinuxFestival(speech);
        }
    }

    static void autoHearth() {
        // cancel click some tasks, hopefully
        lastMapViewClickButton = 1;

        // unmount
        if (isPlayerMountingHorse() && getMainInventory().countItemsByNameContains("/rope") > 0) {
            new ZeeThread() {
                public void run() {
                    try {
                        unmountPlayerFromHorse(getPlayerCoord());
                        sleep(777);//waitPose could fail if unmount failed
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    gameUI.act("travel", "hearth");
                }
            }.start();
        }
        else {
            gameUI.act("travel", "hearth");
        }
    }



    // make expanded map window fit screen
    static Coord mapWndLastPos, mapWndLastSz;
    static int mapWndMinHeightBackup=350, mapWndMinHeight=350;
    public static void windowMapCompact(MapWnd mapWnd, boolean compact) {

        if(gameUI==null || gameUI.mapfile==null) {
            //println("windowMapCompact > gameUI "+gameUI+" , mapfile "+gameUI.mapfile);
            return;
        }

        Coord screenSize = gameUI.map.sz;
        Window.DefaultDeco deco = (Window.DefaultDeco) mapWnd.deco;

        // window expanded
        if(!compact){
            if (deco.ca.sz().y < mapWndMinHeight){
                mapWndMinHeightBackup = mapWnd.view.sz.y;
                mapWnd.resize(deco.ca.sz().x, mapWndMinHeight);
            }
            // used when gameUI resize
            mapWndLastPos = new Coord(mapWnd.c);
            // horizontal fit
            if(mapWnd.c.x + deco.ca.sz().x > screenSize.x){
                mapWnd.c = new Coord(screenSize.x - deco.ca.sz().x, mapWnd.c.y);
            }
            // vertical fit
            if(mapWnd.c.y + deco.ca.sz().y > screenSize.y){
                mapWnd.c = new Coord(mapWnd.c.x, screenSize.y - deco.ca.sz().y);
            }else if(mapWnd.c.y < 0){
                mapWnd.c = new Coord(mapWnd.c.x, 0);
            }
        }

        // window compacted
        else{
            if(MiniMap.scale==1){
                gameUI.mapfile.resize(compactMapSizeScale1);
            }else if(MiniMap.scale==2){
                gameUI.mapfile.resize(compactMapSizeScale2);
            }else if(MiniMap.scale==3){
                gameUI.mapfile.resize(compactMapSizeScale3);
            }
            //from minimapCompactReposition();
            MapWnd map = gameUI.mapfile;
            // adjust x pos if out of screen, or if on the right side of screen
            if ( map.c.x + map.viewf.sz.x > gameUI.sz.x  ||  map.c.x > gameUI.sz.x/2)
                map.c.x = gameUI.sz.x - map.viewf.sz.x ;

            //recenter player when map compacts
            mapWnd.recenter();
        }


        // show/hide minimap resize btns
        toggleMinimapResizeButtons(mapWnd,compact);

    }

    static Widget wdgMapResizeBtns;
    static void reposMapResizeBtns(){
        if (wdgMapResizeBtns!=null)
            wdgMapResizeBtns.c = Coord.of(0,gameUI.mapfile.sz.y-23);
    }
    private static void toggleMinimapResizeButtons(MapWnd mapWnd, boolean compact) {

        // hide map resize buttons
        if (!compact && wdgMapResizeBtns!=null){
            wdgMapResizeBtns.reqdestroy();
            wdgMapResizeBtns = null;
        }

        // show map resize buttons
        else if (compact){

            wdgMapResizeBtns = mapWnd.add(new Widget(Coord.z),0,0);
            reposMapResizeBtns();

            // button horizontal
            Button button = wdgMapResizeBtns.add(new Button(20,"↔"){
                public boolean mousedown(Coord c, int button) {
                    int change=25;
                    if (button==3)
                        change *= -1;
                    minimapPrevSize = Coord.of(gameUI.mapfile.viewf.sz);
                    gameUI.mapfile.resize(minimapPrevSize.add(change,0));
                    minimapCompactResizedMouseup();
                    minimapCompactReposition();
                    return true;
                }
                public boolean mouseup(Coord c, int button) {
                    return false;
                }
            },Coord.z);
            button.settip("left/right click");

            // button vertical
            button = wdgMapResizeBtns.add(new Button(20,"↕"){
                public boolean mousedown(Coord c, int button) {
                    int change=25;
                    if (button==3)
                        change *= -1;
                    minimapPrevSize = Coord.of(gameUI.mapfile.viewf.sz);
                    gameUI.mapfile.resize(minimapPrevSize.add(0,change));
                    minimapCompactResizedMouseup();
                    minimapCompactReposition();
                    return true;
                }
                public boolean mouseup(Coord c, int button) {
                    return false;
                }
            },Coord.z.add(20,0));
            button.settip("left/right click");

            wdgMapResizeBtns.pack();
        }
    }


    static Coord autoToggleEquipsBackupCoord;
    public static void checkAutoOpenEquips(boolean done) {
        if(!ZeeConfig.autoToggleEquips)
            return;

        if(!windowEquipment.visible) {
            //from Equipory.drawslots()
            if((gameUI != null) && (gameUI.vhand != null)) {
                try {
                    Equipory.SlotInfo si = ItemInfo.find(Equipory.SlotInfo.class, gameUI.vhand.item.info());
                    if(si != null) {
                        windowEquipment.show();
                    }
                } catch(Loading l) {
                }
            }
        }else if(done){
            windowEquipment.hide();
        }
    }

    public static void initWindowInvMain() {

        //add options interface
        windowInvMain.add(invMainoptionsWdg = new ZeeInvMainOptionsWdg("Inventory"));

        //change slots position
        Inventory invSlots = windowInvMain.getchild(Inventory.class);
        invSlots.c = new Coord(0,30);

        // add radio buttons for transfer options
        invMainoptionsWdg.addWindowTransferOptions();

        windowInvMain.pack();

        initCalendar();

        // fix switching chars with different inv sizes?
        inventoryResized(invSlots);
    }

    // Fish Moon XP alert
    private static boolean fishMoonAlertDone = false;
    private static void initCalendar() {
        Astronomy a = gameUI.ui.sess.glob.ast;
        int moonPhaseIndex = (int) Math.round(a.mp * (double) Cal.moon.f.length) % Cal.moon.f.length;
        //fish moon xp alert
        if (fishMoonXpAlert && !fishMoonAlertDone) {
            if (a.moonPhases[moonPhaseIndex].toLowerCase().contains("full moon")) {
                fishMoonAlertDone = true;
                Cal.fishMoonTex = CharWnd.attrf.render("Fish Moon XP").tex();
                Cal.fishMoonCoord = Coord.z.add(10,0);
                Cal.fishMoonShowText = true;
            }
        }
        // winter countdown
        if ( a.season().name().contains("Autumn")){
            //int nextIndex = (a.season().ordinal()+1) % Astronomy.Season.values().length;
            //Astronomy.Season nextSeason = Astronomy.Season.values()[nextIndex];
            int days = a.season().length - (a.scday + 1);
            if (days <= 5) {
                Cal.winterCountdownTex = CharWnd.attrf.render(days + "d to Winter").tex();
                Cal.winterCountdownCoord = Coord.z.add(5, 56);
                Cal.winterCountdownShow = true;
            }
        }
    }


    public static void windowAdded(Window window, String test) {
        //println(test+" > "+windowTitle);
        //println("    deco "+window.deco);

        window.zeeWinAdded = true;

        String windowTitle = window.cap.strip();

        if (windowTitle.contentEquals("Stack"))
            return;

        if (isBuildWindow(window)) {
            //tunnel helper
            if (ZeeManagerMiner.tunnelHelperStage == ZeeManagerMiner.TUNNELHELPER_STAGE5_BUILDCOL && windowTitle.contentEquals("Stone Column")){
                ZeeManagerMiner.tunnelHelperBuildColumn(window);
            }
            return;
        }

        //cupboard
        if (ZeeCupboardLabeler.isActive && windowTitle.contentEquals("Cupboard")){
            ZeeCupboardLabeler.checkCupboardContents(window);
        }
        //cheesetray
        else if(windowTitle.contentEquals("Rack")) {
            ZeeManagerItemClick.checkCheeseTray(window);
        }
        //belt
        else if (windowTitle.contentEquals("Belt")) {
            ZeeManagerItemClick.invBelt = null;
            ZeeManagerItemClick.getInvBelt();
        }
        //tamed animals manager
        else if (windowTitle.contentEquals("Cattle Roster")) {
            windowModCattleRoster(window);
        }
        // autolabel contents
        else if(ZeeManagerGobClick.autoLabelWincapContainers.contains(windowTitle) || ZeeManagerGobClick.autoLabelWincapVmeters.contains(windowTitle) ) {
            // add window fuel UI
            if (List.of("Oven", "Kiln", "Ore Smelter").contains(windowTitle)) {
                windowAddFuelGUI(window, windowTitle);
            }
            // label gob
            ZeeManagerGobClick.labelGobByContents(window);
        }
        //equips
        else if(windowTitle.contentEquals("Equipment")) {
            windowEquipment = window;
        }
        //main inventory
        else if(windowTitle.contentEquals("Inventory")) {
            windowInvMain = window;
        }
        //barter stand
        else if(windowTitle.contentEquals("Barter Stand")){
            windowModBarterStand(window);
        }
        //mod tamed animal window
        else if(isWindowAnimalStats(windowTitle)){
            windowModAnimalStats(window, windowTitle);
        }
        // auto press
        else if(windowTitle.contentEquals("Extraction Press")) {
            Button btnPress = getButtonNamed(window,"Press");
            if (btnPress==null){
                println("addWindow > winepress button not found");
            } else {
                window.add(new Button(UI.scale(60), "auto") {
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("activate")) {
                            ZeeManagerGobClick.autoPressWine(window);
                        }
                    }
                }, btnPress.c.x + btnPress.sz.x + 5 , btnPress.c.y);
            }
        }
        //fishing
        else if(windowTitle.contains("This is bait")){
            ZeeFishing.checkFishWindow(window);
        }
        // xp window
        else if (isWindowXpEvent(window)){
            // use same widow title for all xpevt windows
            windowTitle = WINDOW_NAME_XPEVT;
            modXpEventWindow(window);
            return;
        }

        // Craft window
        if(isMakewindow(window)) {

            // cheese tray
            if(windowTitle.contentEquals("Cheese Tray")){
                ZeeManagerItemClick.cheeseTrayMakeWindow(window);
            }
            // bug collection
            else if(windowTitle.contentEquals("Bug Collection")) {
                if (!ZeeManagerCraft.bugColRecipeOpen)
                    ZeeManagerCraft.bugColRecipeOpened(window);
            }
            // cloths
            else if( List.of("Linen Cloth","Hemp Cloth").contains(windowTitle) ) {
                if (!ZeeManagerCraft.clothRecipeOpen)
                    ZeeManagerCraft.clothRecipeOpened(window);
            }
            // rope
            else if (windowTitle.contentEquals("Rope")) {
                if (!ZeeManagerCraft.ropeRecipeOpen)
                    ZeeManagerCraft.ropeRecipeOpened(window);
            }
            else{
                if (ZeeManagerCraft.bugColRecipeOpen)
                    ZeeManagerCraft.bugColWindowClosed();
                else if (ZeeManagerCraft.clothRecipeOpen)
                    ZeeManagerCraft.clothWindowClosed();
                else if (ZeeManagerCraft.ropeRecipeOpen)
                    ZeeManagerCraft.ropeWindowClosed();
            }

            // checkbox auto pick irrlight
            makeWindowAddIrrlightCheckbox(window);

            // use same widow title for all craft windows
            windowTitle = WINDOW_NAME_CRAFT;
        }

        if (gameUI!=null && !gameUI.sz.equals(0,0)){
            windowApplySavedPosition(window, windowTitle);
            windowFitView(window);
        }

        //order of call is important due to window.hasOrganizeButton
        windowModOrganizeButton(window, windowTitle);
        windowModAutoHideButton(window,windowTitle);
    }

    private static void modXpEventWindow(Window window) {

        //move bottom right, after necessary delay
        new ZeeThread(){
            public void run() {
                try {
                    sleep(PING_MS);
                    Coord newPos = Coord.of(gameUI.sz.sub(window.sz));
                    newPos = newPos.add(-gameUI.menu.sz.x,-MenuGrid.bgsz.y);
                    window.move(newPos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        //add checkbox
        CheckBox cb;
        window.add(cb = new CheckBox("autoclose"){
            {a = autocloseXpWindow;}
            public void set(boolean a) {
                autocloseXpWindow = a;
                Utils.setprefb("autocloseXpWindow",a);
            }
        },0,0);

        //autoclose
        if (autocloseXpWindow) {
            new ZeeThread() {
                public void run() {
                    try {
                        long timeout = 5000;
                        do {
                            cb.setLabel("autoclose " + (timeout / 1000) + "s");
                            sleep(1000);
                            timeout -= 1000;
                        } while (timeout > 0);
                        if (autocloseXpWindow)//may be unchecked since thread start
                            getButtonNamed(window, "Okay!").click();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    static boolean isWindowXpEvent(Window window) {
        return window.cap.contains("Hey, listen!");
        //return getButtonNamed(window,"Okay!") != null;
    }

    private static void makeWindowAddIrrlightCheckbox(Window window) {
        new ZeeThread(){
            public void run() {
                try {
                    // find tools icons
                    sleep(500);//wait res loading
                    List<Indir<Resource>> list = window.getchild(Makewindow.class).tools;
                    boolean addCheckbox = false;
                    for (Indir<Resource> tool : list) {
                        String name = tool.get().name;
                        if(name.endsWith("/crucible") || name.endsWith("/smithshammer")) {
                            addCheckbox = true;
                            break;
                        }
                    }

                    // add checkbox if tools icons were present
                    if (addCheckbox){
                        Widget wdg = window.add(new CheckBox("get irrlight"){
                            {a = ZeeManagerGobClick.autoPickIrrlight;}
                            public void changed(boolean val) {
                                ZeeManagerGobClick.autoPickIrrlight = val;
                                super.changed(val);
                            }
                        },360,45);
                        wdg.settip("pick Irrlight and try crafting again");
                    }
                    // disable autopick if no tools icons
                    else{
                        ZeeManagerGobClick.autoPickIrrlightExit();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void windowModCattleRoster(Window window) {

        //Cattle Roster, called for each animal type
        if(cattleRosterHeight  && cattleRosterHeightPercentage < 1.0){

            new ZeeThread() {
                public void run() {

                    // wait remote widgets
                    try{
                        sleep(1000);
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    int headerHeight = UI.scale(40);

                    //reize CattleRosters (HorseRoster, PigHorser, ...)
                    window.children().forEach(roster -> {

                        // RosterWindow > HorseRoster(CattleRoster)
                        if (roster.getClass().getSimpleName().endsWith("Roster")) {
                            try {

                                // roster resize
                                roster.resize(roster.sz.x, (int)(roster.sz.y * cattleRosterHeightPercentage));

                                // buttons reposition
                                final int[] btnHeight = new int[1];
                                roster.children(Button.class).forEach(button -> {
                                    btnHeight[0] = button.sz.y;
                                    button.c.y = (int) ((button.c.y * cattleRosterHeightPercentage) - (button.sz.y*0.6));
                                });

                                //entrycont resize
                                Field f = roster.getClass().getSuperclass().getDeclaredField("entrycont");
                                Widget entrycont = (Widget) f.get(roster);
                                entrycont.resize(entrycont.sz.x, (int)(entrycont.sz.y * cattleRosterHeightPercentage)  - (btnHeight[0]*2));

                                //scrollbar resize
                                f = roster.getClass().getSuperclass().getDeclaredField("sb");
                                Scrollbar sb = (Scrollbar) f.get(roster);
                                sb.max = (int) (sb.max * cattleRosterHeightPercentage) + btnHeight[0] ;
                                sb.resize((int) (sb.sz.y * cattleRosterHeightPercentage) + btnHeight[0]);

                                // flag dirty
                                f = roster.getClass().getSuperclass().getDeclaredField("dirty");
                                f.setAccessible(true);
                                f.setBoolean(roster,true);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    // animal type buttons
                    window.children(IButton.class).forEach(button -> {
                        button.c.y = (int) (button.c.y * cattleRosterHeightPercentage);
                    });

                    window.resize(window.sz.x, (int)(window.sz.y * cattleRosterHeightPercentage));
                    window.pack();
                }
            }.start();

        }
    }

    private static void windowModAutoHideButton(Window window, String windowTitle) {
        if (listWindowsAddHideButton.isBlank())
            return;
        boolean showButton = false;
        for (String cap : ZeeConfig.listWindowsAddHideButton.split(",")) {
            if (cap.contentEquals(windowTitle)) {
                showButton = true;
                break;
            }
        }
        if (!showButton)
            return;
        int pad = ZeeWindow.ZeeButton.BUTTON_SIZE;
        if (window.hasOrganizeButton)
            pad += 25;
        //create autohide button
        window.buttonAutoHide = ((Window.DefaultDeco)window.deco).add(
            new ZeeWindow.ZeeButton(ZeeWindow.ZeeButton.BUTTON_SIZE,ZeeWindow.ZeeButton.TEXT_AUTOHIDEWINDOW,"auto hide"),
            ((Window.DefaultDeco)window.deco).cbtn.c.x - pad,
            ((Window.DefaultDeco)window.deco).cbtn.c.y
        );
        //autohide window is active throughout user session
        if (listAutoHideWindowsActive.contains(window.cap)){
            window.isAutoHideOn = true;
            String buttonText = ZeeWindow.ZeeButton.TEXT_AUTOHIDEWINDOW;
            if (listAutoHideWindowsActiveFast.contains(window.cap)){
                window.isAutoHideFast = true;
                buttonText = ZeeWindow.ZeeButton.TEXT_AUTOHIDEWINDOW_FAST;
            }
            window.buttonAutoHide.change(buttonText, new Color(0,200,0));
            window.autoHideToggleWinPos();
        }
    }

    static void windowFitView(Window window) {
        if (window.c.x + window.sz.x > gameUI.sz.x)
            window.c.x = gameUI.sz.x - window.sz.x;
        // TODO fit vertical when Window sz y is more precise
    }

    static void windowGlueToBorder(Window window){
        if (window.c.x < 0)
            window.c.x = 0;
        else if( gameUI.sz.x - (window.c.x + window.sz.x)  <  window.sz.x )
            window.c.x = gameUI.sz.x - window.sz.x;
    }

    public static boolean isBuildWindow(Window window) {
        return getButtonNamed(window,"Build") != null;
    }


    static Gob windowFuelTargetGob;
    private static void windowAddFuelGUI(Window window, String windowTitle) {

        int y = window.sz.y - 60;
        int x = 45;
        int txtsz = 40;
        int btnsz = 60;
        TextEntry te;
        Button btn;

        windowFuelTargetGob = lastMapViewClickGob;

        if (windowTitle.contentEquals("Oven")) {
            window.add(te=new TextEntry(txtsz,"4"){
                public boolean mousewheel(Coord c, int amount) {
                    settext(ZeeConfig.getTextEntryNextScrollNumber(text(),amount));
                    return true;
                }
            }, 0, y);
            window.add(btn=new Button(UI.scale(btnsz),"branch"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate"))
                        windowAddFuel(te.text(),this.text.text,windowFuelTargetGob);
                }
            },x,y);
            window.add(btn=new Button(UI.scale(btnsz),"coal"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate"))
                        windowAddFuel(te.text(),this.text.text,windowFuelTargetGob);
                }
            },x+btn.sz.x,y);
        }
        else if (windowTitle.contentEquals("Kiln")) {
            window.add(te=new TextEntry(txtsz,"1"){
                public boolean mousewheel(Coord c, int amount) {
                    settext(ZeeConfig.getTextEntryNextScrollNumber(text(),amount));
                    return true;
                }
            }, 0, y);
            window.add(btn=new Button(UI.scale(btnsz),"branch"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate"))
                        windowAddFuel(te.text(),this.text.text,windowFuelTargetGob);
                }
            },x,y);
            window.add(btn=new Button(UI.scale(btnsz),"coal"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate"))
                        windowAddFuel(te.text(),this.text.text,windowFuelTargetGob);
                }
            },x+btn.sz.x,y);
        }
        else if ( windowTitle.contains("Smelter") ){
            window.add(te=new TextEntry(txtsz,"9"){
                public boolean mousewheel(Coord c, int amount) {
                    settext(ZeeConfig.getTextEntryNextScrollNumber(text(),amount));
                    return true;
                }
            }, 0, y);
            window.add(btn=new Button(UI.scale(btnsz),"coal"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate"))
                        windowAddFuel(te.text(),this.text.text,windowFuelTargetGob);
                }
            },x,y);
        }

        window.pack();
    }

    private static String getTextEntryNextScrollNumber(String text, int amount) {
        int num = Integer.parseInt(text);
        if (amount < 0)
            num++;
        else if (amount > 0)
            num--;
        if (num < 1)
            num = 1;
        return String.valueOf(num);
    }

    private static void windowAddFuel(String txtNum, String fuelName, Gob g) {
        try {
            int num = Integer.parseInt(txtNum);
            if (num < 1){
                msgError("invalid quantity");
                return;
            }
            if (g==null){
                msgError("Fuel target not found");
                return;
            }
            //get all fuel items
            List<WItem> items = getMainInventory().getWItemsByNameContains(fuelName);
            //ignore stack placeholder
            items.removeIf(wItem -> ZeeManagerItemClick.isStackByAmount(wItem.item));
            //add items
            //TODO get items from stack
            ZeeManagerGobClick.addItemsToGob(items,num,g);
        }catch (NumberFormatException e){
            ZeeConfig.msg(e.getMessage());
        }
    }


    public static void checkRemoteWidget(String type, Widget wdg) {

    }


    private static int bstandPriceX = 110; // price x (sprite img)
    private static int bstandQlX = 155; // quality x (label ql)
    private static void windowModBarterStand(Window window) {

        int v = 20;
        int winmidx = window.sz.x / 2;

        // change buttons
        window.children(Button.class).forEach(button -> {
            String buttonName = button.text.text;
            // skip close button
            if (buttonName.equalsIgnoreCase("x"))
                return;
            // resign button
            if(buttonName.contains("Resign")) {
                button.resize(button.sz.x/2, button.sz.y);
                button.c = button.c.add(-50, -(v * 5));
                button.change("Resign");
            }
            // buttons change, buy, connect
            else {
                int btnmidx = button.sz.x / 2;
                int btnaddx = 0;
                button.c = button.c.addy(-v);
                button.resize(btnmidx, button.sz.y);
                if (!buttonName.contains("Buy") && !buttonName.contains("Change"))
                    btnaddx -= btnmidx;
                if (button.c.x >= winmidx)
                    btnaddx -= 110;
                button.c = button.c.add(btnaddx,0);
                button.change(buttonName.substring(0,3));
            }
        });

        // change shopboxes and textentries
        AtomicInteger i = new AtomicInteger(0);
        window.children().forEach(el -> {
            //shopbox
            if (el.getClass().getSimpleName().equals("Shopbox")){
                // change size x and y
                el.sz = el.sz.add(-115, - v);
                // change y, except first one
                if ( i.get() > 0 )
                    el.c = el.c.addy( - (v * i.get()) );
                // half textentry
                el.children(TextEntry.class).forEach(te -> te.resize(UI.scale(35)));
                i.getAndIncrement();
            }
        });

        // checkbox auto-buy
        barterStandMidclickAutoBuy = false;
        Widget wdg = window.add(new CheckBox("midclick auto-buy"){
            public void changed(boolean val) {
                barterStandMidclickAutoBuy = val;
            }
        },0,400);

        // checkbox stacking off
        wdg = window.add(new CheckBox("auto-disable stacking"){
            { a = barterAutoDisableStacking; }
            public void changed(boolean val) {
                barterAutoDisableStacking = val;
            }
        },0,wdg.c.y+wdg.sz.y);
        barterAutoDisableStacking();

        //button return branches
        Widget btn = window.add(new Button(UI.scale(120),"return branches"){
            public void click() {
                //cehck inv branches
                List<WItem> branches = getMainInventory().getWItemsByNameContains("/branch");
                if (branches.size() == 0){
                    msgError("no branches to return");
                    return;
                }
                //check wooden chests
                Gob woodenChest = getClosestGobToPlayer(findGobsByNameEndsWith("/chest","/largechest"));
                double chestDistance = distanceToPlayer(woodenChest);
                if (woodenChest==null || chestDistance > 25){
                    msgError("no wooden chests close enough");
                    return;
                }
                //if chest already open, do transfer
                List openWindows = getWindowsNameEndsWith("Chest");
                if (openWindows.size() == 1) {
                    branches.get(0).item.wdgmsg("transfer",Coord.z,-1);
                    return;
                }
                //open chest and do transfer
                new ZeeThread(){
                    public void run() {
                        try {
                            ZeeManagerGobClick.gobClick(woodenChest, 3);
                            if (!waitWindowOpenedNameEndsWith("Chest")){
                                msgError("timeout waiting window opened");
                                return;
                            }
                            branches.get(0).item.wdgmsg("transfer",Coord.z,-1);
                            sleep(500);
                            getWindowsNameEndsWith("Chest").get(0).wdgmsg("close");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        },0,wdg.c.y+wdg.sz.y+3);
        btn.settip("return branches to closest wooden chest");


        window.resize(260,btn.c.y+btn.sz.y);
    }

    private static boolean barterAutoDisRunning = false;
    private static void barterAutoDisableStacking() {
        if (barterAutoDisRunning){
            println("stacking off switched stands?");
            return;
        }
        if (!barterAutoDisableStacking){
            println("auto disable stackng is off");
            return;
        }
        if (!autoStack){
            println("stackng is already off");
            return;
        }
        new ZeeThread(){
            public void run() {
                //println("auto disable stacking start");
                barterAutoDisRunning = true;
                try {
                    //disable stacking if necessary
                    if (autoStack)
                        toggleAutostack();
                    addPlayerText("stacking off");
                    //wait barter window close
                    do{
                        sleep(500);
                    }while(getWindow("Barter Stand")!=null || !isPlayerIdlePose());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //enable stacking
                if (!autoStack)
                    toggleAutostack();
                //println("auto disable stacking end");
                removePlayerText();
                barterAutoDisRunning = false;
            }
        }.start();
    }

    static boolean isPlayerIdlePose() {
        if (ZeeConfig.isPlayerMountingHorse()) {
            return gobHasAnyPose(getPlayerMountedHorse(), POSE_HORSE_IDLE) &&
                    !isPlayerActivePose();
        }
        else {
            return playerHasAnyPose(POSE_PLAYER_IDLE,POSE_PLAYER_KICKSLED_IDLE);
        }
    }

    static public String[] arrayPlayerActivePoses = new String[]{
            ZeeConfig.POSE_PLAYER_DRINK,
            ZeeConfig.POSE_PLAYER_CHOPTREE,
            ZeeConfig.POSE_PLAYER_CHOPBLOCK,
            ZeeConfig.POSE_PLAYER_DIGSHOVEL,
            ZeeConfig.POSE_PLAYER_PICK,
            ZeeConfig.POSE_PLAYER_SAWING,
            ZeeConfig.POSE_PLAYER_CHIPPINGSTONE,
            ZeeConfig.POSE_PLAYER_BUILD,
            ZeeConfig.POSE_PLAYER_BUTCH,
            ZeeConfig.POSE_PLAYER_HARVESTING,
            ZeeConfig.POSE_PLAYER_PICKGROUND,
            ZeeConfig.POSE_PLAYER_BUSHPICK
    };
    static boolean isPlayerActivePose() {
        return playerHasAnyPose(arrayPlayerActivePoses);
    }


    private static void windowApplySavedPosition(Window window, String windowTitle) {
        Coord c;
        if(rememberWindowsPos && !(window instanceof MapWnd) ){
            //use saved position window
            if (mapWindowPos!=null && (c = mapWindowPos.get(windowTitle)) != null) {
                window.c = c;
            }
        }
    }


    /*
        show organize button for duplicate windows
     */
    private static void windowModOrganizeButton(Window window, String windowTitle) {
        final String singleWindows = "Craft,Inventory,Character Sheet,Options,Kith & Kin,Equipment,Map";
        if(!singleWindows.contains(windowTitle)) { // avoid searching multiple Windows
            List<Window> wins = getWindows(windowTitle);
            wins.removeIf(w -> isMakewindow(w));
            if (wins.size() <= 1)
                return;
            Window.DefaultDeco deco = ((Window.DefaultDeco)window.deco);
            if (deco==null){
                println("no deco "+windowTitle);
                return;
            }
            int pad = ZeeWindow.ZeeButton.BUTTON_SIZE;
            deco.add(
                    new ZeeWindow.ZeeButton(pad,ZeeWindow.ZeeButton.TEXT_ORGANIZEWINDOWS,"organize duplicates"),
                    deco.cbtn.c.x - pad,
                    deco.cbtn.c.y
            );
            window.hasOrganizeButton = true;
        }
    }

    private static void windowModAnimalStats(Window window, String windowTitle) {

        TextEntry textEntryTop = window.getchild(TextEntry.class);
        Label[] labels = window.children(Label.class).stream().toArray(Label[] ::new);
        int breedY = labels[labels.length-1].c.y;

        // values help labels ([0] for Quality, [1] for Meat...)
        Label[] vals = windowTamedAnimalLabelValues(window,labels);
        for (int i = 0; i < vals.length; i++) {
            window.add( new Label("["+i+"]"), vals[i].c.x+26, vals[i].c.y );
        }

        // textEntry format
        TextEntry textEntryBottom = window.add(
            new TextEntry( UI.scale(200), windowTamedAnimalGetFormat(windowTitle)),
        0, breedY+20);

        // button name
        Widget btn = window.add(new ZeeWindow.ZeeButton(UI.scale(45),"name"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.equals("activate")){
                    new Thread(){
                        public void run() {
                            try {
                                String nameFormat = textEntryBottom.text();
                                String animalName = nameFormat;
                                for (int i = 0; i < vals.length; i++) {
                                    animalName = animalName.replace("[" + i + "]", vals[i].texts.replace("%", ""));
                                }
                                animalName = animalName.replace("[MF]", windowTamedAnimalGetGender(windowTitle));
                                textEntryTop.settext(animalName);
                                windowTamedAnimalUpdateFormat(windowTitle, nameFormat);
                                //set name and close window
                                textEntryTop.activate(animalName);
                                if (ZeeConfig.closeTamedAnimalWindowAfterNaming){
                                    sleep(555);
                                    ((Window.DefaultDeco)window.deco).cbtn.click();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
        }, textEntryBottom.sz.x+3, breedY+18);

        // autoclose checkbox
        window.add(new CheckBox("close"){
            {a = closeTamedAnimalWindowAfterNaming;}
            public void changed(boolean val) {
                a = closeTamedAnimalWindowAfterNaming = val;
                Utils.setprefb("closeTamedAnimalWindowAfterNaming",a);
            }
        },btn.c.x, btn.c.y-15);

        window.pack();
    }

    private static String windowTamedAnimalGetFormat(String animal) {
        String ret = "";

        if(animal.equals("Hog") || animal.equals("Sow"))
            ret = mapTamedAnimalNameFormat.get(MAP_ANIMAL_FORMAT_PIG);
        else if(animal.equals("Bull") || animal.equals("Cow") || animal.contains("Reindeer"))
            ret = mapTamedAnimalNameFormat.get(MAP_ANIMAL_FORMAT_CATTLE);
        else if(animal.equals("Stallion") || animal.equals("Mare"))
            ret = mapTamedAnimalNameFormat.get(MAP_ANIMAL_FORMAT_HORSE);
        else if(animal.equals("Nanny") || animal.equals("Billy"))
            ret = mapTamedAnimalNameFormat.get(MAP_ANIMAL_FORMAT_GOAT);
        else if(animal.equals("Ewe") || animal.equals("Ram"))
            ret = mapTamedAnimalNameFormat.get(MAP_ANIMAL_FORMAT_SHEEP);

        return ret;
    }

    public static String removeSuffix(final String s, final String suffix) {
        if (s != null && s.endsWith(suffix)) {
            return s.split(suffix)[0];
        }
        return s;
    }

    private static void windowTamedAnimalUpdateFormat(String animal, String nameFormat) {

        if(animal.equals("Hog") || animal.equals("Sow"))
            mapTamedAnimalNameFormat.put(MAP_ANIMAL_FORMAT_PIG, nameFormat);
        else if(animal.equals("Bull") || animal.equals("Cow"))
            mapTamedAnimalNameFormat.put(MAP_ANIMAL_FORMAT_CATTLE, nameFormat);
        else if(animal.equals("Stallion") || animal.equals("Mare"))
            mapTamedAnimalNameFormat.put(MAP_ANIMAL_FORMAT_HORSE, nameFormat);
        else if(animal.equals("Nanny") || animal.equals("Billy"))
            mapTamedAnimalNameFormat.put(MAP_ANIMAL_FORMAT_GOAT, nameFormat);
        else if(animal.equals("Ewe") || animal.equals("Ram"))
            mapTamedAnimalNameFormat.put(MAP_ANIMAL_FORMAT_SHEEP, nameFormat);

        Utils.setpref(ZeeConfig.MAP_ANIMAL_FORMAT, ZeeConfig.serialize(ZeeConfig.mapTamedAnimalNameFormat));
    }

    private static String windowTamedAnimalGetGender(String animal) {
        final String male = "Hog,Bull,Stallion,Billy,Ram,Buck";
        final String gender = male.contains(animal) ? "M" : "F";
        return gender;
    }

    private static Label[] windowTamedAnimalLabelValues(Window window, Label[] labels) {
        //HashMap<String,String> mapStatsNameValue = new HashMap<>();
        int labelsLength = labels.length;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].text.text.contains("Born to") || labels[i].text.text.contains("unbranded")){
                labelsLength--;
            }
        }
        Label[] vals = new Label[labelsLength/2];
        int j = 0;
        for (int i = 0; i < labels.length; i++) {
            // skip fist labels "Born to" and "unbranded"
            if (labels[i].text.text.contains("Born to") || labels[i].text.text.contains("unbranded"))
                continue;
            // detect digits
            if(labels[i].text.text.matches("\\d+%?")){ // if( i % 2 == 1 ) {
                vals[j++] = labels[i]; // label value
            }
        }
        return vals;
    }

    public static boolean isWindowAnimalStats(String windowTitle) {
        final String list = "Sow,Hog,Cow,Bull,Stallion,Mare,Nanny,Billy,Ewe,Ram,Reindeer Doe,Reindeer Buck";
        return list.contains(windowTitle);
    }

    public static int addZeecowOptions(OptWnd.Panel main, int y) {

        y += 17;

        main.add(new Button(200,"Zeecow options"){
            @Override
            public void click() {
                if(zeecowOptions == null)
                    zeecowOptions = new ZeeOptionsJFrame();
                else
                    zeecowOptions.toFront();
                main.getparent(OptWnd.class).hide();
            }
        }, 0, y);

        y += 10;

        return y;
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Set<String>> initMapCategoryGobs() {
        HashMap<String, Set<String>> ret;
        String s = Utils.getpref(MAP_CATEGORY_GOBS,"");
        if(s.isEmpty()) {
            ret = new HashMap<>();
            ret.put(CATEG_LOCRES, localizedResources);
            ret.put(CATEG_RAREFORAGE, rareForageables);
            ret.put(CATEG_AGROCREATURES, aggressiveGobs);
            ret.put(CATEG_PVPANDSIEGE, pvpGobs);
            Utils.setpref(MAP_CATEGORY_GOBS,serialize(ret));
        }else{
            ret = (HashMap<String, Set<String>>) deserialize(s);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> initMapCategoryAudio() {
        String s = Utils.getpref(MAP_CATEGORY_AUDIO,"");
        if (s.isEmpty())
            return new HashMap<String,String> ();
        else
            return (HashMap<String, String>) deserialize(s);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Color> initMapCategoryColor() {
        String s = Utils.getpref(MAP_CATEGORY_COLOR,"");
        if (s.isEmpty()) {
            HashMap<String, Color> ret = new HashMap<String, Color>();
            ret.put(CATEG_AGROCREATURES, COLOR_YELLOW);
            return ret;
        }else
            return (HashMap<String, Color>) deserialize(s);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> initMapGobSpeech() {
        String s = Utils.getpref(MAP_GOB_SPEECH,"");
        if (s.isEmpty())
            return new HashMap<String,String> ();
        else
            return (HashMap<String, String>) deserialize(s);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> initMapGobAudio() {
        String s = Utils.getpref(MAP_GOB_AUDIO,"");
        if (s.isEmpty())
            return new HashMap<String,String> ();
        else
            return (HashMap<String, String>) deserialize(s);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> initMapTamedAnimals() {
        //String s = "";//run once to reset on login
        String s = Utils.getpref(MAP_ANIMAL_FORMAT,"");
        if (s.isEmpty()) {
            HashMap<String, String> ret = new HashMap<>();
            ret.put(MAP_ANIMAL_FORMAT_HORSE, "([MF]) q[0] e[1] s[2]"); // ql, endurance, stamina
            ret.put(MAP_ANIMAL_FORMAT_PIG, "([MF]) q[0] t[3] m[1]"); // ql, truffle, meat
            ret.put(MAP_ANIMAL_FORMAT_CATTLE, "([MF]) q[0] m[2]"); // ql, milk
            ret.put(MAP_ANIMAL_FORMAT_GOAT, "([MF]) q[0] m[2] w[3]"); // ql, milk, wool
            ret.put(MAP_ANIMAL_FORMAT_SHEEP, "([MF]) q[0] m[2] w[3]");
            Utils.setpref(MAP_ANIMAL_FORMAT, serialize(ret));
            return ret;
        }
        else
            return (HashMap<String, String>) deserialize(s);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> initMapGobCategory() {
        String s = Utils.getpref(MAP_GOB_CATEGORY,"");
        if (s.isEmpty()) {
            HashMap ret = new HashMap<String, String>();
            //for each categ
            for (String categ: mapCategoryGobs.keySet()){
                //for each gob in categ
                for (String gob: mapCategoryGobs.get(categ)) {
                    //add to mapGobCateg
                    ret.put(gob,categ);
                }
            }
            return ret;
        }
        else
            return (HashMap<String, String>) deserialize(s);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Integer> initMapActionUses() {
        String s = Utils.getpref(MAP_ACTION_USES,"");
        if (s.isEmpty())
            return new HashMap<String,Integer> ();
        else
            return (HashMap<String, Integer>) deserialize(s);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Color> initMapGobColor() {
        String s = Utils.getpref(MAP_GOB_COLOR,"");
        if (s.isEmpty())
            return new HashMap<String,Color> ();
        else
            return (HashMap<String, Color>) deserialize(s);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Coord> initMapWindowPos() {
        String s = Utils.getpref(MAP_WND_POS,"");
        if (s.isEmpty())
            return new HashMap<String,Coord> ();
        else
            return (HashMap<String, Coord>) deserialize(s);
    }

    //count uses for each Action Search item
    public static void actionUsed(String actionName) {
        if(actionName==null || actionName.isEmpty())
            return;
        Integer uses = mapActionUses.get(actionName);
        if(uses==null){
            uses = 0;
        }
        uses++;
        mapActionUses.put(actionName, uses);
        if(mapActionUses.size() > 130) {
            //sort by value and limit to first 30
            mapActionUses = mapActionUses.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(30)
                .collect(Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));
        }
        Utils.setpref(MAP_ACTION_USES, serialize(mapActionUses));
    }

    static void windowChangedPos(Window window) {
        saveWindowPos(window);
    }

    static void saveWindowPos(Window window) {

        if(!rememberWindowsPos || window==null || isBuildWindow(window))
            return;

        String name = window.cap;

        //igonore cases
        if( name==null || name.isEmpty() )
            return;

        // set craft window unique name
        if(isMakewindow(window))
            name = WINDOW_NAME_CRAFT;

        //save window pos
        mapWindowPos.put(name, new Coord(window.c));
        Utils.setpref(MAP_WND_POS, serialize(mapWindowPos));
    }

    public static boolean isMakewindow(Window window) {
        return getButtonNamed(window,"Craft All") != null;
    }

    public static int drawText(String text, GOut g, Coord p) {
        Text txt = Text.render(text);
        TexI softTex = new TexI(txt.img);
        g.image(softTex, p);
        return softTex.sz().x;
    }


    private static boolean alreadyTrackingScents = false;
    static boolean initiatedToggles;
    public static void initToggles() {
        new ZeeThread(){
            public void run() {
                try {
                    println("init toggles > "+ZeeSess.charSwitchCurPlayingChar);

                    // add minimap resize buttons
                    toggleMinimapResizeButtons(gameUI.mapfile,isMiniMapCompacted());

                    // show char switch window
                    if (ZeeSess.charSwitchKeepWindow)
                        ZeeSess.charSwitchCreateWindow();

                    // discover autostack checkbox value (1/2)
                    toggleAutostack();//wait checkUiMsg() set checkbox value

                    // auto run speed
                    sleep(1000);
                    if(autoRunLogin)
                        setPlayerSpeed(PLAYER_SPEED_RUN);

                    // discover autostack checkbox value (2/2)
                    toggleAutostack();//reset to original value

                    // auto track scents
                    sleep(1000);
                    if(autoTrackScents && !alreadyTrackingScents) {
                        gameUI.menu.wdgmsg("act", "tracking");
                        //tracking for all characters
                        alreadyTrackingScents = true;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static int getPlayerSpeed() {
        return gameUI.ulpanel.getchild(Speedget.class).cur;
    }

    public static void setPlayerSpeed(int spd) {
        gameUI.ulpanel.getchild(Speedget.class).set(spd);
    }


    static boolean showMineSupport = false;
    public static void toggleMineSupport() {

        showMineSupport = !showMineSupport;

        // toggle mine support radius
        // TODO replace with wdgmsg
        if (classMSRad!=null) {
            try {
                Field field = classMSRad.getDeclaredField("show");
                Method method = classMSRad.getMethod("show", boolean.class);
                //field.setBoolean(classMSRad, !field.getBoolean(classMSRad));
                //method.invoke(classMSRad, !field.getBoolean(classMSRad));
                method.invoke(classMSRad, showMineSupport);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        List<Gob> ladders = findGobsByNameEndsWith("terobjs/ladder");
        if (!ladders.isEmpty()) {
            for (Gob ladder : ladders) {
                toggleMineLadderRadius(ladder);
            }
        }
    }

    static void toggleMineLadderRadius(Gob ladder) {
        //remove ladder radius
        if (!showMineSupport){
            Gob.Overlay radius = ladder.findol(ZeeGobRadius.class);
            if (radius!=null)
                radius.remove();
        }
        //add ladder radius
        else {
            ladder.addol(new Gob.Overlay(ladder, new ZeeGobRadius(ladder, null, ZeeGobRadius.RADIUS_MINE_LADDER_SUPPORT,new Color(139, 139, 185, 48))));
        }
    }

    static boolean showRadiusBeeskep = false;
    static void toggleRadiusBeeskep(){
        showRadiusBeeskep = ! showRadiusBeeskep;
        List<Gob> beeskeps = findGobsByNameEndsWith("/beehive");
        if (!beeskeps.isEmpty()){
            for (Gob skep : beeskeps) {
                toggleRadiusBeeskep(skep);
            }
        }
    }

    static void toggleRadiusBeeskep(Gob skep) {
        if (!showRadiusBeeskep){
            Gob.Overlay radius = skep.findol(ZeeGobRadius.class);
            if (radius!=null)
                radius.remove();
        }else{
            skep.addol(new Gob.Overlay(skep, new ZeeGobRadius(skep, null, ZeeGobRadius.RADIUS_BEESKEP,new Color(139, 139, 185, 48))));
        }
    }

    static boolean showRadiusFoodtrough = false;
    static void toggleRadiusFoodtrough(){
        showRadiusFoodtrough = ! showRadiusFoodtrough;
        List<Gob> foodtroughs = findGobsByNameEndsWith("/trough");
        if (!foodtroughs.isEmpty()){
            for (Gob t : foodtroughs) {
                toggleRadiusFoodtrough(t);
            }
        }
    }
    static void toggleRadiusFoodtrough(Gob trough) {
        if (!showRadiusFoodtrough){
            Gob.Overlay radius = trough.findol(ZeeGobRadius.class);
            if (radius!=null)
                radius.remove();
        }else{
            trough.addol(new Gob.Overlay(trough, new ZeeGobRadius(trough, null, ZeeGobRadius.RADIUS_FOOD_THROUGH,new Color(139, 139, 185, 48))));
        }
    }


    static boolean highlightCheeserack = false;
    static void toggleCheeserack(){
        highlightCheeserack = !highlightCheeserack;
        List<Gob> racks = ZeeConfig.findGobsByNameEndsWith("/cheeserack");
        if (!racks.isEmpty()){
            for (Gob r : racks) {
                toggleCheeserack(r);
            }
        }
    }
    static void toggleCheeserack(Gob cheeserack){
        if (highlightCheeserack){
            List<String> ols = ZeeManagerGobClick.getOverlayNames(cheeserack);
            int trays = 0;
            for (String ol : ols) {
                if (ol.contains("gfx/fx/eq")) {
                    trays++;
                }
            }
            if (trays==0)
                return;
            if (trays==3)
                ZeeConfig.addGobColor(cheeserack, Color.green.darker());
            else
                ZeeConfig.addGobColor(cheeserack, Color.orange);
        }else {
            ZeeConfig.removeGobColor(cheeserack);
        }
    }

    public static void checkClassMod(String name, Class<?> qlass){
        try {

            if(name.equals("haven.res.gfx.fx.bprad.BPRad")){
                //Change radius color
                setFinalStatic( qlass.getDeclaredField("smat"),
                    new BaseColor(new Color(139, 139, 185, 48)) );
                setFinalStatic( qlass.getDeclaredField("emat"),
                    Pipe.Op.compose(new Pipe.Op[]{new BaseColor(new Color(139, 139, 185, 48)), new States.LineWidth(1)})  );
            }
            else if (name.equals("haven.res.gfx.fx.msrad.MSRad")){
                classMSRad = qlass;
            }
            else if(name.equals("haven.res.ui.barterbox.Shopbox")) {

                // pricec = UI.scale(200, 5);
                setFinalStatic(
                        qlass.getDeclaredField("pricec"),
                        UI.scale(bstandPriceX, 5)
                );

                // qualc = UI.scale(260, 5).add(Inventory.invsq.sz());
                setFinalStatic(
                        qlass.getDeclaredField("qualc"),
                        UI.scale(bstandQlX, 5).add(Inventory.invsq.sz())
                );

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // https://stackoverflow.com/a/3301720
    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    public static void playMidi(String[] notes){
        if(playingAudio!=null && playingAudio.contains(notes.toString()))
            return;//avoid duplicate audio
        new ZeeSynth(notes).start();
    }

    public static void playMidi(String[] notes, int instr){
        if(playingAudio!=null && playingAudio.contains(notes.toString()))
            return;//avoid duplicate audio
        new ZeeSynth(notes,instr).start();
    }

    //"note, duration_ms, volume_from0to127",
    //"rest_ms",
    public static final String[] midiJawsTheme = new String[]{
            "200",//avoid stuttering
            "2F#,500,100", "100", "2G,250,120",
            "700",
            "2F#,500,100", "100", "2G,250,120",
            "400",
            "2F#,300,85", "100", "2G,300", "100",
            "2F#,200,90", "100", "2G,200", "100",
            "2F#,200,100", "100", "2G,200", "100",
            "2F#,200,110", "100", "2G,200", "100",
            "2F#,200,120", "100", "2G,200", "100",
            "2F#,200,120",
            "200"//avoid cuts
    };
    public static final String[] midiUfoThirdKind = new String[]{
            "200",
            "5D,300,100",
            "5E,300,120",
            "5C,600,110",
            "4C,600,100",
            "4G,1000,90",
            "200"
    };
    public static final String[] midiBeethoven5th = new String[]{
            "200",
            "3G,100,120","50",
            "3G,100,120","50",
            "3G,100,120","50",
            "3D#,1000,120",
            "200"
    };
    public static final String[] midiWoodPecker= new String[]{
            "200",
            "5C,80,80","50",
            "5F,80,90","50",
            "5A,80,100","50",
            "6C,200,120",
            "5A,200,100",
            "200"
    };


    private static final LinkedHashMap<Long,Long> mapGobidTimeplayed = new LinkedHashMap<Long,Long>(){
        protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
            return size() == 20;//limit map to size
        }
    };
    private static void playAudioGobId(String filePath, long gobId) {
        synchronized (mapGobidTimeplayed) {
            Long lastMs = mapGobidTimeplayed.get(gobId);
            //println("alert gob id = "+gobId+"  , lastms="+lastMs +" , mapsize="+mapGobidTimeplayed.size());
            if (lastMs == null) {
                playAudio(filePath);
                mapGobidTimeplayed.put(gobId, ZeeThread.now());
                return;
            }
            if (ZeeThread.now() - lastMs < 10000) { // same gob id waits 10s before playing again
                //println("too soon ms");
                return;
            }
            playAudio(filePath);
            mapGobidTimeplayed.put(gobId, ZeeThread.now());
        }
    }


    private static double lasterrsfx = 0;
    public static void playAudio(String filePath) {
        double now = Utils.rtime();
        if(now - lasterrsfx > 0.5) {
            lasterrsfx = now;
            new ZeeSynth(filePath).start();
        }
        //if(playingAudio!=null && playingAudio.contains(filePath))
            //return;//avoid duplicate audio
        //playingAudio = filePath;
        //new ZeeSynth(filePath).start();
    }

    public static String serialize(Serializable o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(o);
            oos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static Object deserialize(String s){
        byte[] data = Base64.getDecoder().decode(s);
        Object o = null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data))) {
            o = ois.readObject();
            ois.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return o;
    }

    public static String imgToBase64String(final RenderedImage img, final String formatName) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, formatName, Base64.getEncoder().wrap(os));
            return os.toString(StandardCharsets.ISO_8859_1.name());
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static BufferedImage base64StringToImg(final String base64String) {
        try {
            return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64String)));
        } catch (final IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public static void invCounterUpdate(GItem i) {
        try {
            String itemName = i.getres().name;
            Integer count = getMainInventory().countItemsByNameEquals(itemName);
            invMainoptionsWdg.updateLabelCount(itemName,count);
        }catch (Resource.Loading e){
        }
    }

    public static void searchNextInputMakeWnd(String inputName) {
        MenuSearch searchWindow = gameUI.toggleSearchWindow();
        searchWindow.sbox.settext(inputName);
    }

    public static boolean checkKeyPressed(KeyEvent ev) {

        //println(ev.getKeyCode()+"  "+ev.getKeyChar());

        //brightness
        if (ev.getKeyCode()==KeyEvent.VK_RIGHT) {
            return ZeeManagerGobClick.brightnessUp();
        }else if(ev.getKeyCode()==KeyEvent.VK_LEFT) {
            return ZeeManagerGobClick.brightnessDown();
        }else if(ev.getKeyCode()==KeyEvent.VK_HOME) {
            return ZeeManagerGobClick.brightnessDefault();
        }
        // fast zoom
        else if (ev.getKeyCode()==KeyEvent.VK_PAGE_DOWN) {
            return gameUI.map.camera.wheel(Coord.z,10);
        }else if (ev.getKeyCode()==KeyEvent.VK_PAGE_UP) {
            return gameUI.map.camera.wheel(Coord.z,-10);
        }
        //exit actions hovermenu (esc)
        else if(ZeeHoverMenu.checkExitEsc(ev)) {
            return true;
        }
        // pickup closest gob (q)
        else if (ev.getKeyCode()==81) {
            // Ctrl+q shows window pickup gob
            if(ev.isControlDown()){
                ZeeManagerGobClick.toggleWindowPickupGob();
                return true;
            }
            // simple q press pickup closest gob, if no combat
            else if(!isCombatActive()) {
                ZeeManagerGobClick.pickupClosestGob(ev);
                return true;
            }
            return false;
        }
        // key drink (')
        else if (ZeeConfig.drinkKey && ev.getKeyCode()==222){
            ZeeManagerItemClick.drinkFromBeltHandsInv();
            return true;
        }
        // volume up (arrow)
        else if (ZeeConfig.keyUpDownAudioControl && ev.getKeyCode()==KeyEvent.VK_UP){
            double vol = Audio.volume;
            if (vol < 0.9)
                Audio.setvolume(Double.parseDouble(String.format("%.1f", vol)) + 0.1d);
            else if (vol < 1)
                Audio.setvolume(1); // max 1
            msgLow("volume "+String.format("%.1f", Audio.volume));
            return true;
        }
        // volume down (arrow)
        else if (ZeeConfig.keyUpDownAudioControl && ev.getKeyCode()==KeyEvent.VK_DOWN){
            double vol = Audio.volume;
            if (vol > 0.1)
                Audio.setvolume(Double.parseDouble(String.format("%.1f", vol)) - 0.1d);
            else if (vol > 0)
                Audio.setvolume(0); // min 0
            msgLow("volume "+String.format("%.1f", Audio.volume));
            return true;
        }
        // alternate cams bad/ortho (Shift+c)
        else if(ZeeConfig.keyCamSwitchShiftC && ev.getKeyCode()==KeyEvent.VK_C && ev.isShiftDown()){
            String cam = gameUI.map.camera.getClass().getSimpleName();
            try {
                if(cam.endsWith("FreeCam")){
                    gameUI.map.findcmds().get("cam").run(null, new String[]{"cam", "ortho", "-f"});
                }else if(cam.endsWith("OrthoCam")){
                    gameUI.map.findcmds().get("cam").run(null, new String[]{"cam", "bad"});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        // toggle hitbox, hidden gobs
        else if (ev.getKeyCode()==KeyEvent.VK_H){
            // toggle models
            if(ev.isControlDown()) {
                // variable toggled outside function because QuickOptions runnable
                ZeeConfig.hideGobs = !ZeeConfig.hideGobs;
                ZeeManagerGobClick.toggleModelsAllGobs();
                return true;
            }
            // toggle hitboxes
            else if (ev.isShiftDown()){
                showHitbox = !showHitbox;
                ZeeManagerGobClick.toggleHitbox();
                return true;
            }
        }
        return false;
    }

    public static Window getWindowBuild() {
        for (Window window : getWindowsOpened()) {
            if (isBuildWindow(window))
                return window;
        }
        return null;
    }

    public static Window getWindow(String name) {
        Set<Window> windows = gameUI.children(Window.class);
        for(Window w : windows) {
            if(w.cap.equalsIgnoreCase(name)){
                return w;
            }
        }
        return null;
    }

    public static Window getWindowNameContains(String nameContains) {
        Set<Window> windows = gameUI.children(Window.class);
        for(Window w : windows) {
            if(w.cap.contains(nameContains)){
                return w;
            }
        }
        return null;
    }

    public static Button getButtonNamed(Window win, String name) {
        Set<Button> buttons = win.children(Button.class);
        for(Button b : buttons) {
            if(b.text.text.equalsIgnoreCase(name)){
                return b;
            }
        }
        return null;
    }

    public static boolean isWindowContainer(Window window) {
        final String[] containers = (
                //boxes
                "Woodbox,Cupboard,Chest,Crate,Basket,Casket,Box,Coffer,Steelbox,Metal Cabinet,"
                +"Urn,Pot,"
                //misc
                +"Knarr,Snekkja,Wagon,Table,Saddlebags,"
                +"Furnace,Smelter,Desk,Trunk,Shed,Packrack,Strongbox,Stockpile,"
                +"Tub,Compost Bin,Extraction Press,Rack,Herbalist Table,Frame,"
                +"Chicken Coop,Rabbit Hutch,Archery Target,Oven,Steel crucible,"
                +"Cauldron,Pane mold,Kiln,Old Trunk,Old Stump,Smoke shed,Finery Forge,"
                +"Tidepool,Fireplace,Quiver,Creel,Cache,Hidden Hollow"
        ).split(",");
        for (String contName: containers) {
            if ( !isMakewindow(window) && window.cap.contains(contName) ) {
                return true;
            }
        }
        return false;
    }

    public static List<Window> getContainersWindows() {
        List<Window> ret = new ArrayList<>();
        if(gameUI==null)
            return ret;
        Set<Window> windows = gameUI.children(Window.class);
        for(Window w : windows) {
            if( w.visible() && isWindowContainer(w) ){
                ret.add(w);
            }
        }
        return ret;
    }

    public static Set<Window> getWindowsOpened() {
        return gameUI.children(Window.class);
    }

    public static List<Window> getWindows(String name) {
        List<Window> ret = new ArrayList<>();
        if(gameUI==null)
            return ret;
        Set<Window> windows = gameUI.children(Window.class);
        for(Window w : windows) {
            if(w.cap.equalsIgnoreCase(name)){
                ret.add(w);
            }
        }
        return ret;
    }

    public static List<Window> getWindowsNameEndsWith(String nameEndsWith) {
        List<Window> ret = new ArrayList<>();
        if(gameUI==null)
            return ret;
        Set<Window> windows = gameUI.children(Window.class);
        for(Window w : windows) {
            if(w.cap.strip().endsWith(nameEndsWith)){
                ret.add(w);
            }
        }
        return ret;
    }

    /*
        - compile multi-line messages into single-line
        - show text ql above gob
     */
    public static void checkUiMsg(String text) {
        lastUIMsgMs = now = System.currentTimeMillis();
        lastUiMsg = text;

        // inspect quality msg may come in two sequential lines/calls
        if(now - lastUiQualityMsgMs > 555) { //new message
            lastUiQualityMsgMs = now;
            uiMsgTextQuality = "";
            uiMsgTextBuffer = "";
        }

        // add gob text ql
        if (text.contains("Quality")) {
            uiMsgTextQuality = text;
            String ql = uiMsgTextQuality.replaceAll("Quality: ","");
            if (ql.contains("grown"))
                ql = ql.replaceAll(",","q").replaceAll(" grown","");
            ZeeConfig.addGobText(ZeeConfig.lastMapViewClickGob, ql, 0,255,0,255,0);
        }
        // show two line ql msg in one single line
        else if(uiMsgTextQuality!=null && !uiMsgTextQuality.isEmpty() && !text.contains("Memories")){
            uiMsgTextBuffer += ", " + text;
            gameUI.ui.msg(uiMsgTextQuality + uiMsgTextBuffer);
        }

        // feasting msg
        if (isPlayerFeasting && text.startsWith("You gained ")){
            ZeeManagerCraft.feastingMsgStatGained(text);
        }
        //autostack checkbox on
        else if (text.contains("Stacking is now turned on.")){
            if(ZeeInvMainOptionsWdg.cbAutoStack.a != true)
                ZeeInvMainOptionsWdg.cbAutoStack.a = autoStack = true;
        }
        //autostack checkbox off
        else if (text.contains("Stacking is now turned off.")) {
            if(ZeeInvMainOptionsWdg.cbAutoStack.a != false)
                ZeeInvMainOptionsWdg.cbAutoStack.a = autoStack = false;
        }
    }

    public static void checkUiErr(String text){
        lastUIMsgMs = System.currentTimeMillis();

        if (ZeeManagerStockpile.busy && text.contains("That stockpile is already full."))
            ZeeManagerStockpile.exitManager("checkUiErr() > pile is full");
    }

    public static String getCursorName() {
        //return cursorName;//too slow for quick farming selection
        try {
            synchronized(gameUI.map.ui) {
                return gameUI.map.ui.getcurs(Coord.z).name;
            }
        } catch(Exception e) {
            println("getCursorName > "+e.getMessage());
        }
        return "";
    }

    /**
     * action names: dig, mine, carry, destroy, fish, inspect, repair,
     *      crime, swim, tracking, aggro, shoot
     */
    public static final String ACT_DIG = "dig", ACT_MINE = "mine", ACT_CARRY = "carry",
        ACT_DESTROY = "destroy", ACT_FISH = "fish", ACT_INSPECT = "inspect",
        ACT_REPAIR = "repair", ACT_CRIME = "crime", ACT_SWIM = "swim",
        ACT_TRACKING = "tracking", ACT_AGGRO = "aggro", ACT_SHOOT = "shoot";
    public static void cursorChange(String action) {
        gameUI.menu.wdgmsg("act", action);
    }


    //reset state
    public static void initCharacterVariables() {
        println("initCharacterVariables ");
        mainInv = null;
        ZeeManagerItemClick.invBelt = null;
        ZeeManagerItemClick.equipory = null;
        ZeeManagerStockpile.windowManager = null;
        ZeeManagerStockpile.selAreaPile = false;
        ZeeManagerCraft.windowFeasting = null;
        ZeeManagerMiner.tunnelHelperWindow = null;
        ZeeManagerGobClick.barterFindText = null;
        ZeeManagerGobClick.barterSearchOpen = false;
        ZeeManagerGobClick.remountClosestHorse = false;
        ZeeManagerItemClick.clickingAllItemsPetals = false;
        ZeeManagerGobClick.autoPickIrrlightExit();
        makeWindow = null;
        ZeeManagerMiner.tilesMonitorCleanup();
        ZeeHistWdg.clearHistory();
        ZeeManagerGobClick.plowQueueReset();
        ZeeManagerGobClick.chopTreeReset();
        ZeeManagerGobClick.chipStoneReset();
        ZeeFishing.exit();

        ZeeCupboardLabeler.reset();
        ZeeCupboardLabeler.isActive = false;

        if (ZeeManagerGobClick.winHideGobs!=null){
            try{
                ZeeManagerGobClick.winHideGobs.reqdestroy();
                ZeeManagerGobClick.winHideGobs = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(ZeeManagerFarmer.windowManager!=null){
            try {
                ZeeManagerFarmer.windowManager.reqdestroy();
                ZeeManagerFarmer.windowManager = null;
                ZeeManagerFarmer.resetInitialState();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void clickOpenBelt() {
        WItem belt = ZeeManagerItemClick.getEquippedItemNameEndsWith("belt");
        if (belt != null) {
            belt.mousedown(Coord.z, 3);
            ZeeManagerItemClick.invBelt = null;
        }
    }

    public static void saveTileSelection(Coord sc, Coord ec, int modflags, MCache.Overlay ol) {
        lastSavedOverlayStartCoord = sc;
        lastSavedOverlayEndCoord = ec;
        lastSavedOverlayModflags = modflags;
        lastSavedOverlay = ol;
        lastSavedOverlayMs = System.currentTimeMillis();
    }

    public static void resetTileSelection(){
        lastSavedOverlayStartCoord = null;
        lastSavedOverlayEndCoord = null;
        lastSavedOverlayModflags = -1;
        if(lastSavedOverlay !=null)
            lastSavedOverlay.destroy();
        lastSavedOverlayMs = 0;
        ZeeConfig.keepMapViewOverlay = false;
    }

    public static void printGobs(){
        List<String> gobs = ZeeConfig.gameUI.ui.sess.glob.oc.gobStream().map(gob -> gob.getres().name).collect(Collectors.toList());
        System.out.println(gobs.size()+" > "+gobs.toString());
    }

    static List<Gob> getAllGobs(){
        return ZeeConfig.gameUI.ui.sess.glob.oc.gobStream().filter(gob -> {
            if (gob != null && !gob.virtual && gob.getres() != null) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
    }

    // pattern must match whole gob name
    public static List<Gob> findGobsMatchingRegexpList(List<String> regexPatterns) {
        return ZeeConfig.gameUI.ui.sess.glob.oc.gobStream().filter(gob -> {
            if(gob!=null && gob.getres()!=null) {
                for (int i = 0; i < regexPatterns.size(); i++) {
                    if (gob.getres().name.matches(regexPatterns.get(i)))
                        return true;
                }
                return false;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
    }

    public static List<Gob> findGobsByNameEndsWith(String ... names) {
        return ZeeConfig.gameUI.ui.sess.glob.oc.gobStream().filter(gob -> {
            for (String n : names) {
                if(gob!=null && gob.getres()!=null && gob.getres().name.endsWith(n))
                    return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    public static List<Gob> findGobsByNameStartsWith(String ... names) {
        return ZeeConfig.gameUI.ui.sess.glob.oc.gobStream().filter(gob -> {
            for (String n : names) {
                if(gob!=null && gob.getres()!=null && gob.getres().name.startsWith(n))
                    return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    public static List<Gob> findGobsByNameContains(String ... names) {
        return ZeeConfig.gameUI.ui.sess.glob.oc.gobStream().filter(gob -> {
            for (String n : names) {
                if(gob!=null && gob.getres()!=null && gob.getres().name.contains(n))
                    return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    public static List<Gob> findGobsByNameEquals(String ... names) {
        return ZeeConfig.gameUI.ui.sess.glob.oc.gobStream().filter(gob -> {
            for (String n : names) {
                if(gob!=null && gob.getres()!=null && gob.getres().name.contentEquals(n))
                    return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    public static void checkMapClicked(int clickb, Coord pc, Coord2d mc, Object[] args, Gob clickGob) {
        lastMapViewClickButton = clickb;
        lastMapViewClickPcPrev = lastMapViewClickPc;
        lastMapViewClickMcPrev = lastMapViewClickMc;
        lastMapViewClickPc = pc;
        lastMapViewClickMc = mc;
        lastMapViewClickArgs = args;
        lastMapViewClickMs = ZeeThread.now();
        lastMapViewClickGob = clickGob;
        // clicked gob object
        if(clickGob!=null) {
            lastMapViewClickGobName = clickGob.getres().name;
            if(clickb == 2) {
                ZeeManagerGobClick.startMidClick(pc, mc, clickGob, lastMapViewClickGobName);
            } else if (clickb==3 && gameUI.ui.modflags()==0){// no mod keys
                //reset inspect tooltip
                showInspectTooltip = false;
                gameUI.map.ttip = null;
                ZeeManagerGobClick.checkRightClickGob(pc, mc, clickGob, lastMapViewClickGobName);
            }
        }
        // clicked ground
        else{
            lastMapViewClickGobName = "";
            if (clickb==1) {
                ZeeManagerStockpile.checkTileSourcePiling(mc);
            }
            else if (clickb==2) {
                if (isPlayerHoldingItem()) { //move while holding item
                    clickCoord(mc.floor(posres), 1, 0);
                }else
                    ZeeManagerGobClick.startMidClick(pc,mc,null,"");
            }
        }
    }


    public static void unmountPlayerFromHorse(Coord mcFloorPosres) {
        clickCoord(mcFloorPosres,1,2);//unmount at direction of mcFloorPosres
    }

    public static boolean isPlayerCarryingWheelbarrow() {
        return isPlayerLiftingGobNamecontains("/wheelbarrow") != null;
    }

    public static boolean isPlayerDrivingWheelbarrow() {
        return playerHasAnyPose(POSE_PLAYER_DRIVE_WHEELBARROW) && getCursorName().contentEquals(CURSOR_HAND);
    }

    public static boolean isPlayerDrivingPlow() {
        return playerHasAnyPose(POSE_PLAYER_DRIVE_WHEELBARROW) && getCursorName().contentEquals(CURSOR_DIG);
    }

    public static boolean isPlayerMountingHorse() {
        for (String playerPose : getPlayerPoses()) {
            if (playerPose.startsWith("gfx/borka/riding"))
                return true;
        }
        return false;
    }

    public static Gob getPlayerMountedHorse(){
        if (isPlayerMountingHorse())
            return ZeeConfig.getClosestGobByNameContains("gfx/kritter/horse/");
        else
            return null;
    }

    public static boolean isPlayerLiftingPose() {
        return playerHasAnyPose(POSE_PLAYER_LIFTING);
    }

    public static Gob isPlayerLiftingGobNamecontains(String gobNameContains) {
        if (!playerHasAnyPose(POSE_PLAYER_LIFTING)) // not lifting anything
            return null;
        return  isPlayerSharingGobCoord(gobNameContains);
    }

    public static boolean isPlayerLiftingGob(Gob gob) {
        if (!playerHasAnyPose(POSE_PLAYER_LIFTING)) // not lifting anything
            return false;
        return  isPlayerSharingGobCoord(gob);
    }

    public static Gob isPlayerSharingGobCoord(String gobNameContains){
        Gob g = getClosestGobByNameContains(gobNameContains);
        if (g==null) {
            return null;
        }
        if(isPlayerSharingGobCoord(g))
            return g;
        return null;
    }

    public static boolean isPlayerSharingGobCoord(Gob g){
        if (g==null)
            return false;
        double dist = distanceToPlayer(g);
        //println("dist2pl="+dist+"  g="+g.getc()+"  p="+getPlayerGob().getc());
        return (dist == 0);
    }

    public static Gob getClosestGobToPlayer(List<Gob> gobs) {
        return getClosestGob(getPlayerGob(),gobs);
    }

    public static Gob getClosestGob(Gob refGob, List<Gob> gobs) {
        if(gobs==null || gobs.size()==0 || refGob==null)
            return null;
        Gob closestGob = gobs.get(0);
        Float closestDist = distanceBetweenGobs(refGob, closestGob);
        if (closestDist==null)
            return null;
        Float dist;
        for (Gob g : gobs) {
            dist = distanceBetweenGobs(refGob,g);
            if (dist==null)
                return null;//TODO contine instead?
            if (dist < closestDist) {
                closestGob = g;
                closestDist = dist;
            }
        }
        return closestGob;
    }

    public static double distanceToPlayer(Gob gob) {
        return ZeeConfig.getPlayerGob().getc().dist(gob.getc());
    }

    public static Float distanceBetweenGobs(Gob gob1, Gob gob2) {
        try {
            return gob1.getc().dist(gob2.getc());
        }catch (Defer.NotDoneException e){
            return null;
        }
    }

    public static int getPlantStage(Gob g){
        ResDrawable rd = g.getattr(ResDrawable.class);
        String name = g.getres().name;;
        if(name.startsWith("gfx/terobjs/plants") && !name.endsWith("trellis") && rd != null) {
            int stage = rd.sdt.peekrbuf(0);
            return stage;
        }
        return -1;
    }

    public static Gob getPlayerGob() {
        return gameUI.map.player();
    }

    public static Coord getPlayerCoord(){
        return getGobCoord(getPlayerGob());
    }
    public static Coord getPlayerTile(){
        return getGobTile(getPlayerGob());
    }

    public static Coord getGobCoord(Gob gob){
        return gob.rc.floor(OCache.posres);
    }
    public static Coord getGobTile(Gob gob){
        return coordToTile(gob.rc);
    }

    // Returns 0-100
    public static double getMeterStamina() {
        return (100 * gameUI.getmeter("stam", 0).a);
    }
    public static double getMeterHp() {
        return (100 * gameUI.getmeter("hp", 0).a);
    }
    public static double getMeterEnergy() {
        return (100 * gameUI.getmeter("nrj", 0).a);
    }

    public static boolean isPlayerMoving() {
        Moving mov = getPlayerGob().getattr(Moving.class);
        return ( mov!=null && mov.getv()>0);
    }

    public static boolean isPlayerPoseDrinkOrMove(Gob mountedHorse){

        if (isPlayerDrinkingPose())
            return true;

        if (mountedHorse==null)
            // not mounted
            return ZeeConfig.playerHasAnyPose(
                    ZeeConfig.POSE_PLAYER_WALK,
                    ZeeConfig.POSE_PLAYER_RUN
            );
        else
            // mounted
            return ZeeConfig.gobHasAnyPose(
                    mountedHorse,
                    ZeeConfig.POSE_HORSE_WALKING,
                    ZeeConfig.POSE_HORSE_PACE,
                    ZeeConfig.POSE_HORSE_TROT,
                    ZeeConfig.POSE_HORSE_GALLOP
            );
    }

    public static boolean isPlayerDrinkingPose(){
        return getPlayerPoses().contains(POSE_PLAYER_DRINK);
    }

    public static boolean isPlayerHoldingItem() {
        return (gameUI.vhand != null);
    }

    /**
     * Returns value of hourglass, -1 = no hourglass, else the value between 0.0 and 1.0
     * @return value of hourglass
     */
    public static GameUI.Progress getUiProgressHourglassWidget() {
        return gameUI.prog;
    }

    public static void addPlayerText(String s) {
        addGobText(getPlayerGob(),s,0,255,0,255,10);
    }

    public static void addPlayerText(String s, int r, int g, int b, int a, int h) {
        addGobText(getPlayerGob(),s,r,g,b,a,h);
    }

    public static void removePlayerText() {
        removeGobText(getPlayerGob());
    }

    public static void addGobTextTemp(Gob g, String s){
        new ZeeThread() {
            public void run() {
                try {
                    addGobText(g,s);
                    sleep(2000); // wait before removing text
                    removeGobText(g);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void addGobTextTempMs(Gob g, String s, int waitMs){
        new ZeeThread() {
            public void run() {
                try {
                    addGobText(g,s);
                    sleep(waitMs); // wait before removing text
                    removeGobText(g);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void addGobText(Gob g, String s){
        addGobText(g,s,0,255,0,255,5);
    }

    public static void addGobText(Gob g, String s, Color c){
        addGobText(g,s,c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha(),5);
    }

    public static void addGobText(Gob g, String s, int height){
        addGobText(g,s,0,255,0,255,height);
    }

    static final Text.Foundry defGobTextFont = new Text.Foundry(Text.sans.deriveFont(Font.PLAIN, UI.scale(11))).aa(false);
    public static void addGobText(Gob gob, String text, int r, int g, int b, int a, int height) {
        addGobText( gob,
            new ZeeGobText(text, new Color(r, g, b, a), Color.black, height, defGobTextFont)
        );
    }

    public static void addGobText(Gob gob, ZeeGobText zeeGobText) {
        if (gob==null || zeeGobText==null)
            return;
        Gob.Overlay gt = null;
        try {
            gt = new Gob.Overlay(gob, zeeGobText);
            Gob.Overlay finalGt = gt;
            gameUI.ui.sess.glob.loader.defer(() -> {
                synchronized (gob) {

                    //cleanup previous text if present
                    Gob.Overlay ol = gob.findol(ZeeGobText.class);
                    if (ol != null) {
                        ol.remove(false);
                    }

                    //add new text overlay
                    gob.addol(finalGt);
                }
            }, null);
        }catch (Exception e){
            System.out.println("addGobText > "+e.getMessage());
        }
    }
    public static void removeGobText(ArrayList<Gob> gobs) {
        if(gobs==null || gobs.size()==0)
            return;
        try{
            gameUI.ui.sess.glob.loader.defer(() -> {
                for (Gob gob : gobs) {
                    synchronized(gob) {
                        Gob.Overlay ol = gob.findol(ZeeGobText.class);
                        if (ol != null) {
                            ol.remove(false);
                        }
                    }
                }
                gobs.clear();
            }, null);
        }catch (Exception e){
            System.out.println("removeGobText > "+e.getMessage());
        }
    }
    public static void removeGobText(Gob gob) {
        if(gob==null)
            return;
        try{
            gameUI.ui.sess.glob.loader.defer(() -> {synchronized(gob) {
                Gob.Overlay ol = gob.findol(ZeeGobText.class);
                if (ol != null) {
                    ol.remove(false);
                }
            }}, null);
        }catch (Exception e){
            System.out.println("removeGobText > "+e.getMessage());
        }
    }

    public static void addGobPointer(Gob gob, ZeeGobPointer gobPointer) {
        if (gob==null || gobPointer==null)
            return;
        Gob.Overlay gt = null;
        try {
            gt = new Gob.Overlay(gob, gobPointer);
            Gob.Overlay finalGt = gt;
            gameUI.ui.sess.glob.loader.defer(() -> {
                synchronized (gob) {

                    //cleanup previous pointer if present
                    Gob.Overlay ol = gob.findol(ZeeGobPointer.class);
                    if (ol != null) {
                        ol.remove(false);
                    }

                    //add new pointer overlay
                    gob.addol(finalGt);
                }
            }, null);
        }catch (Exception e){
            System.out.println("addGobPointer > "+e.getMessage());
        }
    }
    public static void removeGobPointer(Gob gob) {
        if(gob==null)
            return;
        try{
            gameUI.ui.sess.glob.loader.defer(() -> {synchronized(gob) {
                Gob.Overlay ol = gob.findol(ZeeGobPointer.class);
                if (ol != null) {
                    ol.remove(false);
                }
            }}, null);
        }catch (Exception e){
            System.out.println("removeGobPointer > "+e.getMessage());
        }
    }

    public static void addGobColor(Gob gob, Color c) {
        if(gob==null)
            return;
        addGobColor(gob,c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
    }

    public static void addGobColor(Gob gob, int r, int g, int b, int a) {
        if(gob==null)
            return;
        gameUI.ui.sess.glob.loader.defer(() -> {synchronized(gob) {
            if(gob.getattr(ZeeGobColor.class) != null) {
                gob.delattr(ZeeGobColor.class);
            }
            gob.setattr(new ZeeGobColor(gob, new Color(r, g, b, a)));
        }}, null);
    }

    public static void removeGobColor(Gob gob) {
        if(gob==null)
            return;
        gameUI.ui.sess.glob.loader.defer(() -> {synchronized(gob) {
            gob.delattr(ZeeGobColor.class);
        }}, null);
    }

    public static ZeeGobColor getGobColor(Gob gob) {
        if(gob==null)
            return null;
        synchronized(gob) {
            return gob.getattr(ZeeGobColor.class);
        }
    }

    public static void stopMovingEscKey() {
        // sends escape key to root ui
        gameUI.ui.root.wdgmsg("gk", 27);
    }


    public static void clickRemoveCursor(){
        ZeeConfig.clickCoord(Coord.z,3);
    }

    public static void clickCoord(Coord coord, int btn) {
        clickCoord(coord, btn, 0);
    }

    static void moveToGobTile(Gob gob) {
        moveToTile(getGobTile(gob));
    }

    static void moveToTile(Coord gobTile) {
        clickTile(gobTile,1);
    }

    public static void clickCoord(Coord coord, int btn, int mod) {
        gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), coord, btn, mod);
    }

    public static void clickTile(Coord tile, int btn) {
        clickTile(tile, btn, 0);
    }

    public static void clickTile(Coord tile, int btn, int mod) {
        clickCoord(tileToCoord(tile), btn, mod);
    }

    public static void itemActTile(Coord coord) {
        itemActTile(coord,0);
    }

    public static void itemActTile(Coord coord, int mod) {
        //haven.MapView@22e2c39e ; itemact ; [(700, 483), (-940973, -996124), 0]
        gameUI.map.wdgmsg("itemact", ZeeConfig.getCenterScreenCoord(), coord,  mod);
    }

    public static Coord getAreaCenterTile(Area a){
        Coord center = a.ul.add(a.sz().div(2));
        //println("ul"+a.ul+"  br"+a.br+"  sz"+a.sz()+"  center"+center);
        return center;
    }

    public static Coord coordToTile(Coord2d c) {
        return c.div(MCache.tilesz).floor();
    }
    public static Coord coordToTile(Coord c) {
        return c.div(MCache.tilesz2);
    }

    public static Coord tileToCoord(Coord tile){
        double mult;
        //TODO find better way
        if(tile.x > 0 || tile.y > 0)
            mult = 1023.5;
        else
            mult = 1023.45;
        return adjustPositiveTile(tile).mul(mult);
    }

    static Coord adjustPositiveTile(Coord tile){
        // adjust positive tile coords (TODO test more)
        if(tile.x > 0 || tile.y > 0) {
            //println("adjust tile > before " + tile);
            if (tile.x > 0)
                tile.x += 1;
            if (tile.y > 0)
                tile.y += 1;
            //println("              after " + tile);
        }
        return tile;
    }

    public static Coord getCenterScreenCoord(GameUI ui) {
        Coord sc, sz;
        sz = ui.map.sz;
        sc = new Coord((int) Math.round(Math.random() * 200 + sz.x / 2f - 100),
                (int) Math.round(Math.random() * 200 + sz.y / 2f - 100));
        return sc;
    }

    public static Coord getCenterScreenCoord() {
        return ZeeConfig.gameUI.map.sz.div(2);
    }

    public static void msg(String s) {
        gameUI.ui.msg(s);
    }

    public static void msgLow(String s) {
        gameUI.ui.msg(s,Color.white,ZeeSynth.msgsfxLow);
    }

    public static void msgError(String msg) {
        gameUI.error(msg);
    }

    public static String strArgs(Object... args){
        return Arrays.toString(args);
    }

    public static void println(int num) {
        System.out.println(""+num);
    }

    public static void println(String s) {
        System.out.println(s);
    }

    public static boolean isControlKey(int keyCode) {
        return keyCode==KeyEvent.VK_RIGHT || keyCode==KeyEvent.VK_LEFT || keyCode==KeyEvent.VK_BACK_SPACE || keyCode==KeyEvent.VK_DELETE || keyCode==KeyEvent.VK_HOME || keyCode==KeyEvent.VK_END || keyCode==KeyEvent.VK_SPACE;
    }

    public static boolean isGobRemoved(Gob gob) {
        return gob==null || gameUI.ui.sess.glob.oc.getgob(gob.id)==null;
    }

    public static boolean isCancelClick() {
        //cancel if clicked right/left button
        return lastMapViewClickButton != 2;
    }
    public static void prepareCancelClick() {
        ZeeConfig.lastMapViewClickButton = 2;
    }


    private static boolean iconListBusyCheckingAll = false;
    private static Button iconListButtonAll, iconListButtonNone;
    public static Widget getIconFilterWidget(){

        if (iconListFilterBox != null) {
            iconListButtonAll.disable(true);
            iconListButtonNone.disable(true);
            return iconListFilterBox;
        }

        iconListFilterBox =  new Widget(Coord.of(220,25));

        iconListFilterBox.add(
            new Dropbox<String>(110,14,20) {
                String space = "     ";
                private final List<String> filters = new ArrayList<String>() {{
                    add(space+"all");
                    add(space+"aggressive");
                    add(space+"birds");
                    add(space+"bugs");
                    add(space+"bushes");
                    add(space+"dungeon");
                    add(space+"flowers");
                    add(space+"herbs");
                    add(space+"kritters");
                    add(space+"locres and map");
                    add(space+"mushrooms");
                    add(space+"small animals");
                    add(space+"scents");
                    add(space+"spices");
                    add(space+"string");
                    add(space+"trees");
                    add(space+space+"bark");
                    add(space+space+"bough");
                    add(space+space+"fruit");
                    add(space+space+"leaves");
                    add(space+space+"nuts");
                }};
                protected String listitem(int idx) {
                    return(filters.get(idx));
                }
                protected int listitems() {
                    return(filters.size());
                }
                protected void drawitem(GOut g, String name, int idx) {
                    g.atext(name, Coord.of(0, g.sz().y / 2), 0.0, 0.5);
                }
                public void change(String filter) {
                    super.change(filter);
                    iconList.cur = null;
                    iconList.initOrdered();
                    filter = filter.strip();//remove formatting spaces
                    if (filter.equalsIgnoreCase("all")) {
                        iconListButtonAll.disable(true);
                        iconListButtonNone.disable(true);
                    }else{
                        iconListButtonAll.disable(false);
                        iconListButtonNone.disable(false);
                        iconList.ordered = getIconsFiltered(filter, iconList.ordered);
                    }
                }
                public void dispose() {
                    super.dispose();
                    this.sel = "";
                }
            }
        ,0,0);

        iconListFilterBox.add(new Label("check:"),120,7);

        iconListFilterBox.add(iconListButtonAll = new Button(30,"all"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    iconListToggleAll(true);
                }
            }
        },150,0);

        iconListFilterBox.add(iconListButtonNone = new Button(40,"none"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    iconListToggleAll(false);
                }
            }
        },180,0);

        iconListButtonAll.disable(true);
        iconListButtonNone.disable(true);

        //iconListFilterBox.pack();

        return iconListFilterBox;
    }

    private static void iconListToggleAll(boolean newVal){
        if (iconListBusyCheckingAll){
            println("icon list busy checking all");
            return;
        }
        iconListBusyCheckingAll = true;
        new ZeeThread(){
            public void run() {
                try{
                    List<CheckBox> cbs = new ArrayList<CheckBox>();
                    cbs.addAll(iconList.children(CheckBox.class));
                    cbs.removeIf(checkBox -> {
                        Widget.KeyboundTip tip = (Widget.KeyboundTip) checkBox.tooltip;
                        if (tip.base.contentEquals("Display"))
                            return false;
                        return true;
                    });
                    ZeeConfig.addPlayerText("checking "+cbs.size());
                    int changed = 0;
                    for (CheckBox cb : cbs) {
                        if (cb.state() != newVal) {
                            cb.set(newVal);
                            changed++;
                            sleep(50);
                        }
                    }
                    msgLow("set "+changed+" / "+cbs.size()+" checkboxes to "+newVal);
                }catch (Exception e){
                    e.printStackTrace();
                }
                iconListBusyCheckingAll = false;
                ZeeConfig.removePlayerText();
            }
        }.start();
    }


    public static List<GobIcon.SettingsWindow.ListIcon> getIconsFiltered(String filter, List<GobIcon.SettingsWindow.ListIcon> listOrdered) {

        ArrayList<GobIcon.SettingsWindow.ListIcon> filteredList = new ArrayList<>(listOrdered);

        //println("pre "+filteredList.size());

        if(filter.equals("birds"))
            filteredList.removeIf(entry -> !ZeeConfig.isBird(entry.conf.res.name));
        else if(filter.equals("bugs"))
            filteredList.removeIf(entry -> !ZeeConfig.isBug(entry.conf.res.name));
        else if(filter.equals("bushes"))
            filteredList.removeIf(entry -> !ZeeConfig.isBush(entry.conf.res.name));
        else if(filter.equals("flowers"))
            filteredList.removeIf(entry -> !ZeeConfig.isFlower(entry.conf.res.name));
        else if(filter.equals("herbs"))
            filteredList.removeIf(entry -> !ZeeConfig.isHerb(entry.conf.res.name));
        else if(filter.equals("mushrooms"))
            filteredList.removeIf(entry -> !ZeeConfig.isMushroom(entry.conf.res.name));
        else if(filter.equals("scents"))
            filteredList.removeIf(entry -> !ZeeConfig.isScent(entry.conf.res.name));
        else if(filter.equals("spices"))
            filteredList.removeIf(entry -> !ZeeConfig.isSpice(entry.conf.res.name));
        else if(filter.equals("string"))
            filteredList.removeIf(entry -> !ZeeConfig.isString(entry.conf.res.name));
        else if(filter.equals("kritters"))
            filteredList.removeIf(entry -> !ZeeConfig.isKritter(entry.conf.res.name));
        else if(filter.equals("trees"))
            filteredList.removeIf(entry -> !entry.conf.res.name.contains("/trees/"));
        else if(filter.equals("bark"))
            filteredList.removeIf(entry -> !ZeeConfig.isTreeToughBark(entry.conf.res.name));
        else if(filter.equals("bough"))
            filteredList.removeIf(entry -> !ZeeConfig.isTreeBough(entry.conf.res.name));
        else if(filter.equals("fruit"))
            filteredList.removeIf(entry -> !ZeeConfig.isTreeFruit(entry.conf.res.name));
        else if(filter.equals("leaves"))
            filteredList.removeIf(entry -> !ZeeConfig.isTreeLeaf(entry.conf.res.name));
        else if(filter.equals("nuts"))
            filteredList.removeIf(entry -> !ZeeConfig.isTreeNuts(entry.conf.res.name));
        else if(filter.equals("small animals"))
            filteredList.removeIf(entry -> !ZeeConfig.isSmallAnimal(entry.conf.res.name));
        else if(filter.equals("aggressive"))
            filteredList.removeIf(entry -> !ZeeConfig.isAggressiveIconName(entry.conf.res.name));
        else if(filter.equals("dungeon"))
            filteredList.removeIf(entry -> !ZeeConfig.isDungeonIconName(entry.conf.res.name));
        else if(filter.equals("locres and map"))
            filteredList.removeIf(entry -> !ZeeConfig.isLocResMapIconName(entry.conf.res.name));

        //println("pos "+filteredList.size());
        return filteredList;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    static Queue<Gob> gobsWaiting = new ConcurrentLinkedQueue<>();
    static ZeeThread gobConsumer = new ZeeThread(){
        public void run() {
            println("thread gob consumer");
            while (true){
                synchronized (gobConsumer) {
                    // queue empty = wait next gob arrival
                    if (ZeeConfig.gameUI==null || gobsWaiting.isEmpty()) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // remove gob from queue
                    Gob g = null;
                    try{
                        g = gobsWaiting.remove();
                    }catch (Exception e){
                        e.printStackTrace();
                        continue;
                    }
                    // process gob
                    synchronized (g) {
                        if (g.isGobWaitingSettings && gameUI!=null) {
                            // apply gob settings
                            countRemovals++;
                            Gob finalG = g;
                            gameUI.ui.sess.glob.loader.defer(() -> {
                                applyGobSettings(finalG);
                            },null);
                        } else {
                            // requeue gob up to a few times
                            if (g.requeued < ZeeConfig.gobMaxRequeues) { //TODO test other numbers
                                g.requeued++;
                                gobsWaiting.add(g);
                            }
                            // drop gob from queue eventually
                            else {
                                g.requeued = 0;
                                countDrops++;
                            }
                        }
                    }
                }
            }
        }
    };
    static {
        gobConsumer.start();
    }
    static void queueGobSettings(Gob ob) {
        if(ob != null && !ob.virtual && ob.getres()!=null) {
            synchronized (gobConsumer) {
                gobsWaiting.add(ob);
                gobConsumer.notify();
            }
        }
    }
    static long countRemovals=0, countDrops=0, maxReqs=0;//GLPanel.drawstats()
    static String maxReqstr="";
    static void applyGobSettings(Gob ob){

        try {

            if (ob.requeued > maxReqs) {
                maxReqs = ob.requeued;
                maxReqstr = ob.getres().name;
            }

            addGobTagsAdvanced(ob);

            String gobName = ob.getres().name;;

            // main player settings
            if(ob.tags.contains(Gob.Tag.PLAYER_MAIN)) {

                //main player loading thread
                new ZeeThread(){
                    public void run() {
                        try {

                            sleep(500);

                            // location based settings
                            ZeeManagerGobClick.initPlayerLocation();

                            // remount closest horse
                            if (ZeeManagerGobClick.remountClosestHorse) {
                                ZeeManagerGobClick.remountHorse();
                            }

                            if (ZeeConfig.showGobPointer ) {
                                // remove player pointer
                                if (ob.hasPointer) {
                                    removeGobPointer(ob);
                                }
                                else {
                                    // delayed remove player pointer
                                    sleep(500);
                                    if (ob.hasPointer)
                                        removeGobPointer(ob);
                                }

                                // gob radar (broken)
                                if (ZeeConfig.showGobRadar) {
                                    ob.addol(ZeeGobPointer.gobRadar = new ZeeGobRadar(ob, Coord3f.of(10, 10, 5), new Color(240, 0, 253, 90)));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
            // radius ladder
            else if(showMineSupport && gobName.endsWith("/terobjs/ladder")) {
                toggleMineLadderRadius(ob);
            }
            // radius beeskep
            else if(showRadiusBeeskep && gobName.endsWith("/terobjs/beehive")) {
                toggleRadiusBeeskep(ob);
            }
            // radius trough
            else if(showRadiusFoodtrough && gobName.endsWith("/terobjs/trough")) {
                toggleRadiusFoodtrough(ob);
            }
            // cheeserack color
            else if(highlightCheeserack && gobName.endsWith("/terobjs/cheeserack")){
                toggleCheeserack(ob);
            }
            // hide hearthfire names
            else if(!showKinNames && gobName.endsWith("/terobjs/pow")){
                synchronized (ob) {
                    if (gobHasAttr(ob, "Buddy")) {
                        ob.delattr(Buddy.class);
                    }
                }
            }


            // apply settings audio/aggro  (ignore bat if using batcape)
            if (!ob.getres().name.contentEquals("gfx/kritter/bat/bat") || !ZeeManagerItemClick.isItemEquipped("/batcape")) {

                // audio alerts
                ZeeConfig.applyGobSettingsAudio(ob);

                // aggro radius
                ZeeConfig.applyGobSettingsAggro(ob);

            }

            // highlight gob color
            ZeeConfig.applyGobSettingsHighlight(ob, ZeeConfig.getHighlightGobColor(ob));

            // smoking gob highlight
            if (ob.smokeHighlight){
                ZeeManagerGobClick.highlightGobSmoking(ob);
            }

            // auto boulder option (maybe remove)
            if (ZeeConfig.autoChipMinedBoulder && ZeeManagerMiner.isCursorMining() && ZeeManagerMiner.isBoulder(ob)) {
                ZeeManagerMiner.checkBoulderGobAdded(ob);
            }
            // barter stand item search labels
            else if (ZeeManagerGobClick.barterSearchOpen && ob.getres().name.endsWith("/barterstand")) {
                ZeeManagerGobClick.addTextBarterStand(ob);
            }
            // auto pick irrlight
            else if (ZeeManagerGobClick.autoPickIrrlight && ob.getres().name.endsWith("/irrbloss")) {
                ZeeManagerGobClick.autoPickIrrlight();
            }

            //gob health
            GobHealth healf = ob.getattr(GobHealth.class);
            if (healf!=null && healf.hp < 1){
                Color c = Color.lightGray;
                String s = String.valueOf(healf.hp).replaceFirst("0.", ".");
                ZeeConfig.addGobText(ob,s,c);
            }

            //hitbox
            ob.toggleHitbox();
            ob.toggleModel();

            // save gob name
            if (ob.getres().name!=null && !ob.getres().name.isBlank() && !listGobsSession.contains(ob.getres().name)) {
                listGobsSession.add(ob.getres().name);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        ob.settingsApplied = true;
    }

    static void addGobTagsByResName(Gob gob) {
        String gobName = gob.getres().name;
        if (isTree(gobName)) {
            gob.tags.add(Gob.Tag.TREE);
        }
        if (isBush(gobName)){
            gob.tags.add(Gob.Tag.BUSH);
        }
        if (isGobCrop(gobName)){
            gob.tags.add(Gob.Tag.CROP);
        }
        if (isBug(gobName)){
            gob.tags.add(Gob.Tag.BUG);
        }
        if (isBird(gobName)){
            gob.tags.add(Gob.Tag.BIRD);
        }
        if (isSmallAnimal(gobName)){
            gob.tags.add(Gob.Tag.SMALL_ANIMAL);
        }
        if (isAggressive(gobName)){
            gob.tags.add(Gob.Tag.AGGRESSIVE);
        }
        if(ZeeManagerGobClick.isGobWall(gobName)){
            gob.tags.add(Gob.Tag.WALL);
        }
        if (ZeeManagerGobClick.isGobHouse(gobName)){
            gob.tags.add(Gob.Tag.HOUSE);
        }
        if (ZeeManagerGobClick.isGobSmokeProducer(gobName)){
            gob.tags.add(Gob.Tag.SMOKE_PRODUCER);
        }
        if(ZeeManagerGobClick.isGobIdol(gobName)){
            gob.tags.add(Gob.Tag.IDOL);
        }
        if(ZeeManagerGobClick.isGobMineSupport(gobName) || gobName.endsWith("/ladder")){
            gob.tags.add(Gob.Tag.MINE_SUPPORT);
        }
    }

    static void addGobTagsAdvanced(Gob gob) {
        String gobName = gob.getres().name;
        // players
        if(isPlayer(gob) && gameUI!=null && gameUI.map.player()!=null) {
            gob.tags.add(Gob.Tag.PLAYER);
            if (gameUI.map.player().id == gob.id)
                gob.tags.add(Gob.Tag.PLAYER_MAIN);
            else
                gob.tags.add(Gob.Tag.PLAYER_OTHER);
        }
        //tamed animals
        if (ZeeManagerGobClick.isGobTamedAnimalOrAurochEtc(gobName)){
            gob.tags.add(Gob.Tag.TAMED_ANIMAL_OR_AUROCH_ETC);
        }
    }


    static boolean gobHasAttr(Gob gob, String gAttrClassName) {
        return ZeeManagerGobClick.getGAttrNames(gob).contains(gAttrClassName);
    }
    static boolean gobHasOverlay(Gob gob, String overlayResName) {
        return ZeeManagerGobClick.getOverlayNames(gob).contains(overlayResName);
    }

    static boolean playerHasAttr(String gAttrClassName) {
        return ZeeManagerGobClick.getGAttrNames(getPlayerGob()).contains(gAttrClassName);
    }
    static boolean playerHasOverlay(String overlayResName) {
        return ZeeManagerGobClick.getOverlayNames(getPlayerGob()).contains(overlayResName);
    }

    private static void applyGobSettingsAggro(Gob gob) {
        // aggro categ radius
        if( mapCategoryGobs.get(CATEG_AGROCREATURES).contains(gob.getres().name) && !ZeeManagerGobClick.isGobDeadOrKO(gob)) {
            if (ZeeConfig.aggroRadiusTiles > 0) {
                gob.addol(new Gob.Overlay(gob, new ZeeGobRadius(gob, null, ZeeConfig.aggroRadiusTiles * MCache.tilesz2.y)));
            }
        }
        //looserock radius 7
        else if(gob.getres().name.contentEquals("gfx/terobjs/looserock")){
            gob.addol(new Gob.Overlay(gob, new ZeeGobRadius(gob, null, 7 * MCache.tilesz2.y)));
        }
    }

    public static Gob getClosestGobByNameContains(Gob gobRef, String nameContains) {
        if (nameContains==null || nameContains.isBlank())
            return null;
        return getClosestGob(gobRef, findGobsByNameContains(nameContains));
    }
    public static Gob getClosestGobByNameContains(String nameContains) {
        if (nameContains==null || nameContains.isBlank())
            return null;
        return getClosestGobToPlayer(findGobsByNameContains(nameContains));
    }

    public static Gob getClosestGobByNameEnds(Gob gobRef, String nameEdns) {
        if (nameEdns==null || nameEdns.isBlank())
            return null;
        return getClosestGob(gobRef, findGobsByNameEndsWith(nameEdns));
    }
    public static Gob getClosestGobByNameEnds(String nameEdns) {
        if (nameEdns==null || nameEdns.isBlank())
            return null;
        return getClosestGobToPlayer(findGobsByNameEndsWith(nameEdns));
    }

    public static Gob getClosestGobByNameStarts(Gob gobRef, String nameStarts) {
        if (nameStarts==null || nameStarts.isBlank())
            return null;
        return getClosestGob(gobRef, findGobsByNameStartsWith(nameStarts));
    }
    public static Gob getClosestGobByNameStarts(String nameStarts) {
        if (nameStarts==null || nameStarts.isBlank())
            return null;
        return getClosestGobToPlayer(findGobsByNameStartsWith(nameStarts));
    }

    public static Inventory getMainInventory() {
        if(mainInv==null)
            mainInv = ZeeConfig.getWindow("Inventory").getchild(Inventory.class);
        return mainInv;
    }

    public static void cancelFlowerMenu() {
        try {
            FlowerMenu fm = ZeeConfig.gameUI.ui.root.getchild(FlowerMenu.class);
            if (fm != null) {
                fm.choose(null);
                fm.destroy();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getBarrelOverlayBasename(Gob barrel){
        List<String> ols =  ZeeManagerGobClick.getOverlayNames(barrel);
        if(ols.isEmpty())
            return "";
        return ols.get(0).replace("gfx/terobjs/barrel-","");
    }

    public static Coord getTileFurtherFromPlayer(Area area) {
        Coord p = getPlayerTile();
        Coord further = Coord.of(area.ul);
        //println("player "+p);
        //check player dist from all area tiles
        for (Coord newTile : area) {
            //println("    "+further+" "+ p.dist(further)+" < "+p.dist(newTile)+" "+ newTile);
            if (p.dist(further) < p.dist(newTile)) {
                further = newTile;
                //println("        further = "+further);
            }
        }
        return further;
    }

    public static Coord getTileFurtherFromPlayer(Area area, List<Coord> skipTiles) {
        if (area.area() == skipTiles.size()) {
            println("getTileFurtherFromPlayer > all tiles used");
            return null;
        }
        Coord p = getPlayerTile();
        Coord further = null;
        //println("player "+p);
        //check player dist from all area tiles
        for (Coord newTile : area) {
            if (skipTiles.contains(newTile))
                continue;
            //println("    "+further+" "+ p.dist(further)+" < "+p.dist(newTile)+" "+ newTile);
            if ( further==null || p.dist(further) < p.dist(newTile)) {
                further = newTile;
                //println("        further = "+further);
            }
        }
        return further;
    }

    public static Coord getTileFurtherFromPlayer(Coord c1, Coord c2) {
        Coord p = getPlayerTile();
        if (p.dist(c1) > p.dist(c2))
            return c1;
        return c2;
    }

    public static Coord getTileCloserToPlayer(Area area) {
        Coord p = getPlayerTile();
        Coord closest = area.ul;
        //check player dist from all area tiles
        for (Coord tile : area) {
            if (p.dist(closest) < p.dist(tile))
                closest = tile;
        }
        return closest;
    }

    public static Coord getTileCloserToPlayer(Coord c1, Coord c2) {
        Coord p = getPlayerTile();
        if (p.dist(c1) < p.dist(c2))
            return c1;
        return c2;
    }

    public static void midclickButtonWidget(Button button) {
        if (button.text.text.contentEquals("Buy")){
            Window w = button.getparent(Window.class);
            if (w==null || !w.cap.contentEquals("Barter Stand"))
                return;
            if (!ZeeConfig.barterStandMidclickAutoBuy){
                ZeeConfig.msg("Click bottom checkbox to allow auto-buy");
                return;
            }
            ZeeThread.clickButtonUntilMsgOrHoldingItem(button,"buying");
        }
    }

    public static double getItemQuality(WItem item) {
       return getItemQuality(item.item);
    }

    public static double getItemQuality(GItem item) {
        return Inventory.getQuality(item);
    }

    public static List<String> getPlayerPoses() {
        return getGobPoses(getPlayerGob());
    }

    public static boolean playerHasAnyPose(String ... poses){
        return gobHasAnyPose(getPlayerGob(),poses);
    }

    public static boolean gobHasAnyPose(Gob gob, String ... wantedPoses){
        List<String> gobPoses = getGobPoses(gob);
        for (int i = 0; i < wantedPoses.length; i++) {
            if (gobPoses.contains(wantedPoses[i]))
                return true;
        }
        return false;
    }

    public static boolean gobHasAnyPoseEndsWith(Gob gob, String ... wantedPosesEndsWith){
        List<String> gobPoses = getGobPoses(gob);
        for (int i = 0; i < wantedPosesEndsWith.length; i++) {
            for (String pose : gobPoses) {
                if (pose.endsWith(wantedPosesEndsWith[i]))
                    return true;
            }
        }
        return false;
    }

    public static boolean gobHasAnyPoseContains(Gob gob, String ... wantedPosesContains){
        List<String> gobPoses = getGobPoses(gob);
        for (int i = 0; i < wantedPosesContains.length; i++) {
            for (String pose : gobPoses) {
                if (pose.contains(wantedPosesContains[i]))
                    return true;
            }
        }
        return false;
    }

    public static List<String> getGobPoses(Gob gob) {
        List<String> ret = new ArrayList<>();
        if (gob==null)
            return ret;
        try {
            Drawable d = gob.getattr(Drawable.class);
            if (d instanceof Composite) {
                Composite comp = (Composite) d;
                for (ResData rd : comp.prevposes) {
                    try {
                        ret.add(rd.res.get().name);
                    } catch (Loading l) {
                    }
                }
            }
        }catch (Exception e){
            //println("getGobPoses > "+e.getMessage()+"  ,  gobReady="+gob.isGobWaitingSettings);
        }
        return ret;
    }

    public static Tiler getTilerAt(Coord2d coordMc) {
        return getTilerAt(coordMc.floor(MCache.tilesz));
    }

    public static Tiler getTilerAt(Coord tile) {
        int id = ZeeConfig.gameUI.ui.sess.glob.map.getTileee(tile);
        Tiler tl = ZeeConfig.gameUI.ui.sess.glob.map.tiler(id);
        return tl;
    }

    public static String getTileResName(Coord tileCoord) {
        int id = ZeeConfig.gameUI.ui.sess.glob.map.getTileee(tileCoord);
        Resource res = ZeeConfig.gameUI.ui.sess.glob.map.tilesetr(id);
        if (res==null)
            return "";
        return res.name;
    }

    public static String getTileResName(Coord2d mc) {
        return getTileResName(mc.floor(MCache.tilesz));
    }

    public static Color getComplementaryColor(Color bgColor) {
        return new Color(
            255-bgColor.getRed(),
            255-bgColor.getGreen(),
            255-bgColor.getBlue()
        );
    }

    public static Color intToColor(int rgb){
        return new Color(rgb,true);
    }

    public static int colorToInt(Color c){
        return c.getRGB();
    }

    public static void simpleWindowsUpdateAll() {
        gameUI.children(Window.class).forEach(ZeeConfig::simpleWindowsResize);
    }

    public static void simpleWindowsResize(Window w) {
        if (w != null)
            ((Window.DefaultDeco)w.deco).bgImgSimpleWindow = null;
    }

    public static boolean stopResAnimation(Resource res) {
        final String blocklist = "/chimingbluebell,/saptap,/brazierflame,/stockpile-trash,/beehive,/cigar,/dreca,/boostspeed,/visflag,/villageidol,/pow";
        if (res!=null && blocklist.contains(res.basename())) {
            //ZeeConfig.println("hide " + res.name);
            return true;
        }
        //ZeeConfig.println("show " + (res!=null?res.name:"null"));
        return false;
    }

    public static void newGridColor(Color newColor) {
        MapView.gridmat = new Material(
                new BaseColor(newColor),
                States.maskdepth,
                new MapMesh.OLOrder(null),
                Location.xlate(new Coord3f(0, 0, 0.5f))
        );
    }

    public static void checkNewCursorName(String curs) {

        ZeeConfig.cursorName = curs;

        isPlayerCursorMining = curs.contentEquals(CURSOR_MINE);

        // toggle grid lines
        if (autoToggleGridLines) {
            gameUI.map.showgrid(curs.contentEquals(CURSOR_HARVEST) || ZeeManagerMiner.isCursorMining() || curs.contentEquals(CURSOR_DIG));
        }

        // inspect cursor
        showInspectTooltip = curs.contentEquals(CURSOR_INSPECT);
        if (!showInspectTooltip) {
            //disable tooltips
            gameUI.map.ttip = null;
        }else{
            // label gob build preview
            Gob gobBuildPreview = ZeeManagerStockpile.lastPlob;
            if (gobBuildPreview!=null && gobBuildPreview.getres()!=null)
                addGobText(gobBuildPreview,gobBuildPreview.getres().name);
        }

        //feast window
        isPlayerFeasting = curs.contentEquals(CURSOR_EAT);
        if(!isPlayerFeasting)
            ZeeManagerCraft.windowFeasting = null;


        // fishing window
        if(curs.contentEquals(CURSOR_FISH) && ZeeManagerItemClick.isItemEquipped("/primrod")){
            ZeeFishing.buildWindow();
        }
    }

    public static boolean isPlayerDrivingingKicksled() {
        for (String playerPose : getPlayerPoses()) {
            if (playerPose.contains("/sparkan"))
                return true;
        }
        return false;
    }

    public static boolean isPlayerOnCoracle() {
        return playerHasAnyPose(ZeeConfig.POSE_PLAYER_CORACLE_IDLE,ZeeConfig.POSE_PLAYER_CORACLE_ACTIVE);
    }

    public static boolean isPlayerOnDugout(){
        return playerHasAnyPose(ZeeConfig.POSE_PLAYER_DUGOUT_IDLE,ZeeConfig.POSE_PLAYER_DUGOUT_ACTIVE);
    }

    public static boolean isPlayerOnRowboat(){
        return playerHasAnyPose(ZeeConfig.POSE_PLAYER_ROWBOAT_IDLE,ZeeConfig.POSE_PLAYER_ROWBOAT_ACTIVE);
    }

    public static void combatStarted() {
        lastMapViewClickButton = 1;//cancel click some tasks
        //stop mining
        if(ZeeManagerMiner.mining) {
            ZeeConfig.println(">combat relations, cancel mining");
            ZeeManagerMiner.stopMining();
        }
        //stop farming
        if (ZeeManagerFarmer.busy){
            ZeeConfig.println(">combat relations, cancel farming");
            ZeeManagerFarmer.resetInitialState();
        }
        //stop piler
        if(ZeeManagerStockpile.busy){
            ZeeManagerStockpile.exitManager();
        }
        //equip roundshield
        if (ZeeConfig.equipShieldOnCombat && !ZeeManagerItemClick.isItemEquipped("huntersbow","rangersbow"))
            ZeeManagerItemClick.equipBeltItem("/roundshield");
    }

    static boolean isCombatActive(){
        return ZeeConfig.gameUI.fv.current != null;
    }

    /*
        run with -t option:
            "java -jar hafen.jar -t -U http://game.havenandhearth.com/res/ game.havenandhearth.com "
     */
    static void runThinClient() {
        println("run thin client");
    }

    static Coord gameUIPrevSz;
    static void gameUIResized() {
        if (gameUIPrevSz.x==0 || gameUIPrevSz.y==0){
            return;
        }
        Coord newSz = gameUI.sz;
        Coord change = newSz.sub(gameUIPrevSz);
        //consider prev size mid.x as threshold
        Coord mid = gameUIPrevSz.div(2);
        Set<Window> windows = getWindowsOpened();
        windows.forEach( w -> {
            // windows with x > mid.x are repositioned
            if (w.c.x >= mid.x  &&  change.x!=0){
                //add positives or negatives changes
                w.c.x += change.x;
                // treat expanded map auto align feature
                if(w.cap.contentEquals("Map") && mapWndLastPos!=null)
                    mapWndLastPos.x += change.x;
                // save pos
                saveWindowPos(w);
            }
        });
    }

    public static boolean isPetalConfirmed(String name) {

        // confirm petal Eat, to preserve Food Efficacy
        if (confirmPetalEatReduceFoodEff && name.contentEquals("Eat")){
            if (getMeterEnergy() >= 80){
                if (!gameUI.ui.modctrl) {
                    ZeeConfig.msgError("Ctrl+click to confirm reducing Food Efficacy");
                    return false;
                }
            }
        }

        // confirm petal list
        if (confirmPetal) {
            String[] list = confirmPetalList.split(",");
            for (int i = 0; i < list.length; i++) {
                if (name.contentEquals(list[i])) {
                    if (!gameUI.ui.modctrl) {
                        ZeeConfig.msgError("Ctrl+click to confirm " + list[i]);
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public static boolean isMsgAudioMuted(String msg) {
        String[] arr = blockAudioMsgList.split(";");
        for (int i = 0; i < arr.length; i++) {
            if (msg.strip().contains(arr[i].strip()))
                return true;
        }
        return false;
    }

    static boolean skipNextLiftVehicle = false;
    static boolean liftVehicleBeforeTravelHearth(Object[] args) {
        if (!liftVehicleBeforeTravelHearth)
            return false;
        if (skipNextLiftVehicle){
            //c-c-combo breaker
            skipNextLiftVehicle = false;
            return false;
        }
        if (args==null && args.length<1)
            return false;
        if(!strArgs(args).contains("travel, hearth"))
            return false;

        // unmount kicksled before travel hearth
        if (isPlayerDrivingingKicksled()){
            try {
                new ZeeThread() {
                    public void run() {
                        ZeeManagerGobClick.disembarkVehicle(Coord2d.z);
                        //if disembark then liftup kicksled
                        if(waitPlayerPoseNotInListTimeout(1000,POSE_PLAYER_KICKSLED_IDLE, POSE_PLAYER_KICKSLED_ACTIVE)){
                            try {
                                sleep(100);//lagalagalaga
                                ZeeManagerGobClick.liftGob(getClosestGobByNameContains("vehicle/spark"));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            skipNextLiftVehicle = true; // travel hearth without checking

                        // travel hearth anyways
                        gameUI.act("travel","hearth");
                    }
                }.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        // disembark shalow water vehicle before travel hearth
        else if (isPlayerTileShallowWaterOrSwamp()) {
            Gob vehicle = null;
            if (isPlayerOnDugout())
                vehicle = getClosestGobByNameContains("vehicle/dugout");
            else if (isPlayerOnRowboat())
                vehicle = getClosestGobByNameContains("vehicle/rowboat");
            else if (isPlayerOnCoracle())
                vehicle = getClosestGobByNameContains("vehicle/coracle");
            else
                //player not in vehicle, returning false continues gameUI.wdgmsg()
                return false;

            try {
                Gob finalVehicle = vehicle;//thanks, IntelliJ
                new ZeeThread() {
                    public void run() {
                        ZeeManagerGobClick.disembarkVehicle(Coord2d.z);
                        boolean playerDisembarked = waitPlayerPoseNotInListTimeout(1500,
                                POSE_PLAYER_DUGOUT_ACTIVE, POSE_PLAYER_DUGOUT_IDLE,
                                POSE_PLAYER_ROWBOAT_ACTIVE, POSE_PLAYER_ROWBOAT_IDLE,
                                POSE_PLAYER_CORACLE_IDLE, POSE_PLAYER_CORACLE_ACTIVE
                        );
                        // if disembarked then liftup vehicle
                        if(playerDisembarked) {
                            try {
                                sleep(100);//lagalagalaga
                                ZeeManagerGobClick.liftGob(finalVehicle);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            skipNextLiftVehicle = true; // travel hearth without checking

                        //travel hearth anyways
                        gameUI.act("travel","hearth");
                    }
                }.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            return false;
        }

        // thread already started, returning true skips gameUI.wdgmsg()
        return true;
    }


    static boolean isPlayerTileWaterOrSwamp() {
        final List<String> names = Arrays.asList(
                TILE_WATER_FRESH_DEEP , TILE_WATER_FRESH_SHALLOW ,
                TILE_WATER_OCEAN_SHALLOW , TILE_WATER_OCEAN_DEEP , TILE_WATER_OCEAN_DEEPER ,
                TILE_SWAMP , TILE_SWAMP_WATER ,
                TILE_SWAMP_BOG , TILE_SWAMP_BOG_WATER,
                TILE_SWAMP_FEN , TILE_SWAMP_FEN_WATER
        );

        return names.contains(ZeeConfig.getTileResName(getPlayerTile()));
    }

    static boolean isPlayerTileShallowWaterOrSwamp() {
        final List<String> names = Arrays.asList(
            TILE_WATER_FRESH_SHALLOW ,
            TILE_WATER_OCEAN_SHALLOW ,
            TILE_SWAMP , TILE_SWAMP_WATER ,
            TILE_SWAMP_BOG , TILE_SWAMP_BOG_WATER,
            TILE_SWAMP_FEN , TILE_SWAMP_FEN_WATER
        );

        return names.contains(ZeeConfig.getTileResName(getPlayerTile()));
    }

    static boolean isPlayerTileDeepWater() {
        final List<String> names = Arrays.asList(
                TILE_WATER_FRESH_DEEP ,
                TILE_WATER_OCEAN_DEEP , TILE_WATER_OCEAN_DEEPER
        );

        return names.contains(ZeeConfig.getTileResName(getPlayerTile()));
    }


    public static void moveToAreaCenter(Area a) {
        clickTile(ZeeConfig.getAreaCenterTile(a),1);
    }

    public static boolean isCursorName(String name) {
        return getCursorName().contentEquals(name);
    }

    public static boolean isTileNamed(Coord2d mc, String... names) {
        if (names==null || names.length==0 || mc==null)
            return false;
        String tilename = getTileResName(mc);
        for (String name : names) {
            if (name.contentEquals(tilename))
                return true;
        }
        return false;
    }

    static boolean tileCoordsChangedBy(Coord c1, Coord c2, int minChange) {
        if ( Math.abs(Math.max(c1.x,c2.x)-Math.min(c1.x,c2.x)) > minChange
            || Math.abs(Math.max(c1.y,c2.y)-Math.min(c1.y,c2.y)) > minChange)
        {
            return true;
        }
        return false;
    }

    public static boolean isPlayerInCellar() {
        final List<Gob> cellarStairs = findGobsByNameEndsWith("arch/cellarstairs");
        if (cellarStairs==null || cellarStairs.isEmpty())
            return false;
        return true;
    }

    public static boolean isNumbersOnly(String str) {
        return str!=null && str.chars().allMatch( Character::isDigit );
    }

    public static void inventoryResized(Inventory inv) {

        // repos main inv checkboxes
        invMainoptionsWdg.reposition();

        Window win = (Window)inv.getparent(Window.class);

        // repos auto hiddden window
        if (autoHideWindows && win.isAutoHideOn)
            win.autoHideToggleWinPos();

        // repos autoHide button
        if (win.buttonAutoHide != null){
            //TODO uncomment
            int x = ((Window.DefaultDeco)win.deco).cbtn.c.x - ZeeWindow.ZeeButton.BUTTON_SIZE;
            int y = ((Window.DefaultDeco)win.deco).cbtn.c.y;
            win.buttonAutoHide.c = Coord.of(x,y);
        }

        // resize window bg image
        simpleWindowsResize(win);
    }

    public static void inventoryPreResize(Inventory inv) {
        Window win = (Window)inv.getparent(Window.class);

        //repos buttons to not influence next inventory window resize
        //TODO uncomment
        ((Window.DefaultDeco)win.deco).cbtn.c = Coord.of(0);
        if (win.buttonAutoHide != null){
            win.buttonAutoHide.c = Coord.of(0);
        }
    }

    // calculate next tile from origin to destination
    public static Coord getNextTileTowards(Coord orig, Coord dest) {
        int nextX, nextY;

        if (orig.x < dest.x)
            nextX = orig.x + 1;
        else if (orig.x > dest.x)
            nextX = orig.x - 1;
        else
            nextX = orig.x;

        if (orig.y < dest.y)
            nextY = orig.y + 1;
        else if (orig.y > dest.y)
            nextY = orig.y - 1;
        else
            nextY = orig.y;

        Coord ret = Coord.of(nextX,nextY);

        println("getNextTileTowards > orig"+orig+" dest"+dest+" = ret"+ret);

        return ret;
    }

    public static double doubleRound2(double val){
        val = val*100;
        val = Math.round(val);
        val = val /100;
        return val;
    }

    public static void toggleAutostack() {
        gameUI.menu.wdgmsg("act", "itemcomb", 0);
    }

    static Coord
        compactMapSizeScale1 = Utils.getprefc("compactMapSizeScale1",Coord.of(150,150)),
        compactMapSizeScale2 = Utils.getprefc("compactMapSizeScale2",Coord.of(200,200)),
        compactMapSizeScale3 = Utils.getprefc("compactMapSizeScale3",Coord.of(250,250)),
        minimapPrevSize;
    static void minimapCompactResizedMouseup() {
        if (!isMiniMapCompacted())
            return;
        reposMapResizeBtns();
        Coord sz = gameUI.mapfile.sz;
        if (MiniMap.scale==1 && gameUI.mapfile.view.zoomlevel==0){
            compactMapSizeScale1 = sz;
            Utils.setprefc("compactMapSizeScale1",sz);
        }
        else if (MiniMap.scale==2){
            compactMapSizeScale2 = sz;
            Utils.setprefc("compactMapSizeScale2",sz);
        }
        else if (MiniMap.scale==3){
            compactMapSizeScale3 = sz;
            Utils.setprefc("compactMapSizeScale3",sz);
        }
    }
    static int prevScale=-1;
    static void minimapCompactZoomChanged(int scale) {
        if (!isMiniMapCompacted())
            return;
        if (scale==1 && prevScale>1){
            minimapPrevSize = Coord.of(gameUI.mapfile.viewf.sz);
            gameUI.mapfile.resize(compactMapSizeScale1);
            prevScale = scale;
            minimapCompactReposition();
        }
        else if (scale==2 && compactMapSizeScale2!=null){
            minimapPrevSize = Coord.of(gameUI.mapfile.viewf.sz);
            gameUI.mapfile.resize(compactMapSizeScale2);
            prevScale = scale;
            minimapCompactReposition();
        }
        else if (scale==3 && compactMapSizeScale3!=null){
            minimapPrevSize = Coord.of(gameUI.mapfile.viewf.sz);
            gameUI.mapfile.resize(compactMapSizeScale3);
            prevScale = scale;
            minimapCompactReposition();
        }
    }
    static void minimapCompactReposition() {
        MapWnd map = gameUI.mapfile;

        // adjust x pos if out of screen, or if on the right side of screen
        if ( map.c.x + map.viewf.sz.x > gameUI.sz.x  ||  map.c.x > gameUI.sz.x/2)
            map.c.x = gameUI.sz.x - map.viewf.sz.x ;

        // adjust y pos only if map is bellow limit
        if (minimapPrevSize!=null && map.c.y > gameUI.sz.y/3)
            map.c.y -= map.viewf.sz.y - minimapPrevSize.y;

        // minimap resize buttons
        reposMapResizeBtns();
    }

    static boolean isMiniMapCompacted(){
        return !gameUI.mapfile.tool.visible();
    }

    public static void simulateClickJava(int buttonMask, Point clickLocation) {
        try {
            Robot robot = new Robot();
            robot.mouseMove(clickLocation.x, clickLocation.y);
            robot.mousePress(buttonMask);
            robot.mouseRelease(buttonMask);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void simulateClickJava(int buttonMask) {
        try {
            Robot robot = new Robot();
            robot.mousePress(buttonMask);
            robot.mouseRelease(buttonMask);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static boolean isCaveTile(String tileName) {
        // ignore tile "/mine" if player in a cellar (may break if used without player)
        if (ZeeConfig.findGobsByNameEndsWith("/cellarstairs").size() > 0)
            return false;
        return List.of("gfx/tiles/gloomdark","gfx/tiles/wildcavern","gfx/tiles/shadehollow", "gfx/tiles/warmdepth", "gfx/tiles/lushcave", "gfx/tiles/gleamgrotto", "gfx/tiles/deepcave", "gfx/tiles/mine").contains(tileName);
    }

    public static String getPlayerTileName() {
        return getTileResName(getPlayerTile());
    }

    public static boolean muteAudioMsg(String msg) {
        if (ZeeConfig.blockAudioMsg && ZeeConfig.isMsgAudioMuted(msg))
            return true;
        if ( ZeeManagerStockpile.selAreaPile &&
                (msg.contains("stockpile is already full") || msg.contains("site is occupied")) )
            return true;
        return false;
    }


    // MapView() constructor enable teh overlays with enol()
    public static void updateMapOverlayButtonState(GameUI.MenuCheckBox menuCheckBox, String base) {
        if(base.contentEquals("lbtn-claim") && showOverlayPclaim) {
            menuCheckBox.click();
        }else if(base.contentEquals("lbtn-vil") && showOverlayVclaim) {
            menuCheckBox.click();
        }else if(base.contentEquals("lbtn-rlm") && showOverlayProv){
            menuCheckBox.click();
        }
    }

    public static void updateMapOverlayPrefs(String tag, boolean a) {
        if(tag.contentEquals("cplot") ) {
            showOverlayPclaim = a;
            Utils.setprefb("showOverlayPclaim",showOverlayPclaim);
        }else if(tag.contentEquals("vlg")) {
            showOverlayVclaim = a;
            Utils.setprefb("showOverlayVclaim",showOverlayVclaim);
        }else if(tag.contentEquals("prov")){
            showOverlayProv = a;
            Utils.setprefb("showOverlayProv",showOverlayProv);
        }
    }

    static void checkCharWndAttrs(Object ... args){
        //println(strArgs(args));
        updMakewindowStats();
    }

    public static void updMakewindowStats() {
        if (ZeeConfig.makeWindow==null)
            return;
        // char stats for softcap calculation
        for (Indir<Resource> qm : ZeeConfig.makeWindow.qmod) {
            Glob.CAttr stat = ZeeConfig.gameUI.chrwdg.findattr(qm.get().basename());
            if (stat==null){
                println("updMakewindowStats > stat null");
                return;
            }
            ZeeConfig.makeWindow.qmodMapNameStat.put( qm.get().name, stat.comp );
        }
    }

    static void stackWindowAdded(GItem.ContentsWindow cw) {
        // reposition stack window if out of screen
        if(cw.c.x + cw.sz.x >= gameUI.sz.x){
            cw.c = cw.c.sub(cw.sz.x + GItem.HoverDeco.hovermarg.x, 0);
        }
        if(cw.c.y + cw.sz.y >= gameUI.sz.y){
            cw.c = cw.c.sub(0, cw.sz.y + GItem.HoverDeco.hovermarg.y);
        }
    }

    public static void checkQuestsUimsg() {
        // quests counter
        for (Tabs.TabButton tabButton : gameUI.chrwdg.children(Tabs.TabButton.class)) {
            // current quests button
            if (tabButton.text.text.contentEquals("Current")){
                tabButton.change("Current "+gameUI.chrwdg.quest.cqst.quests.size());
            }
            // completed quests button
            else if (tabButton.text.text.contentEquals("Completed")){
                tabButton.change("Completed "+gameUI.chrwdg.quest.dqst.quests.size());
            }
        }
    }

    static MenuGrid.PagButton getMenuButton(String wdgmsgArgName) {
        synchronized (gameUI.menu.paginae) {
            for (MenuGrid.Pagina pagina : gameUI.menu.paginae) {
                String buttonName = pagina.res().basename();
                if (buttonName.contentEquals(wdgmsgArgName)) {
                    //ZeeConfig.println("found button for " + recipeName);
                    return pagina.button();
                }
            }
        }
        return null;
    }

    public static String getPlayerLocationName() {
        switch (playerLocation){
            case LOCATION_CABIN: return "cabin";
            case LOCATION_CELLAR: return "cellar";
            case LOCATION_UNDERGROUND: return "undeground";
            case LOCATION_OUTSIDE: return "outside";
            case LOCATION_UNDEFINED: return "undefined";
        }
        return "wtf";
    }

    public static boolean isPlobActive() {
        return ZeeManagerStockpile.lastPlob != null;
    }

    public static String getRegexGroup(String liftedGobName, String regex, int groupIndexStartsAt1
    ) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(liftedGobName);
            matcher.find();
            return matcher.group(groupIndexStartsAt1);
        }catch (Exception e){
            println("getRegexGroup > "+e.getMessage());
        }
        return "";
    }

    public static Inventory getWindowsInventory(String windowName) {
        Window win = ZeeConfig.getWindow(windowName);
        if (win!=null){
            Inventory inv = win.getchild(Inventory.class);
            return inv;
        }
        return null;
    }

    public static boolean isPlayerPoseOnAnyShip() {
        final List<String> navPoses = List.of("borka/row","/coracle","/dugout","/snekkja","/knarr");
        for (String playerPose : getPlayerPoses()) {
            for (String navPose : navPoses) {
                if (playerPose.contains(navPose))
                    return true;
            }
        }
        return false;
    }

    static Gob findGobByNameAndCoord(String nameEndsWith, Coord2d coord) {
        try {
            List<Gob> gobs = findGobsByNameEndsWith(nameEndsWith);
            for (Gob gob : gobs) {
                if (gob.rc.compareTo(coord)==0){
                    return gob;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
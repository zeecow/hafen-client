package haven;

import haven.render.*;

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
    static final String MAKE_WINDOW_NAME = "Makewindow";

    static final String CURSOR_ARW = "gfx/hud/curs/arw";//cursor
    static final String CURSOR_ATK = "gfx/hud/curs/atk";
    static final String CURSOR_EAT = "gfx/hud/curs/eat";//feast
    static final String CURSOR_DIG = "gfx/hud/curs/dig";
    static final String CURSOR_HAND = "gfx/hud/curs/hand";//push,lift
    static final String CURSOR_HARVEST = "gfx/hud/curs/harvest";
    static final String CURSOR_MINE = "gfx/hud/curs/mine";//destroy
    static final String CURSOR_SHOOT = "gfx/hud/curs/shoot";
    static final String CURSOR_INSPECT = "gfx/hud/curs/study";

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
    static final String POSE_PLAYER_SAW = "gfx/borka/sawing";
    static final String POSE_PLAYER_CHIPPINGSTONE = "gfx/borka/chipping";//no pickaxe
    static final String POSE_PLAYER_CHOPBLOCK = "gfx/borka/choppan";
    static final String POSE_PLAYER_CHOPTREE = "gfx/borka/treechop";
    static final String POSE_PLAYER_DIGSHOVEL = "gfx/borka/shoveldig";
    static final String POSE_PLAYER_DRINK = "gfx/borka/drinkan";
    static final String POSE_PLAYER_LIFTING = "gfx/borka/banzai";
    static final String POSE_PLAYER_HARVESTING = "gfx/borka/harvesting";//reeds clearing
    static final String POSE_PLAYER_PICK = "gfx/borka/pickan";//pickaxe mining, chipping
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

    static final String TILE_BEACH = "gfx/tiles/beach";
    static final String TILE_SANDCLIFF = "gfx/tiles/sandcliff";
    static final String TILE_MOUNTAIN = "gfx/tiles/mountain";

    static final String DEF_LIST_MUTE_AUDIO = "Leashed horse.;Tracking is now turned;Stacking is now turned;must be empty to be unequipped";
    static final String DEF_LIST_CONFIRM_PETAL = "Empty,Swill,Clean out,Slaughter,Castrate,Unmoor,Take possession,Renounce Lawspeaker,Become Lawspeaker";
    static final String DEF_LIST_HOVER_HIDE_WINDOWS = "Inventory,Character Sheet,Basket,Creel,Cattle Roster,Quiver,Pickup Gobs,Tile Monitor,Switch Char";
    static final String DEF_LIST_BUTCH_AUTO = "Break,Scale,Wring neck,Kill,Skin,Flay,Pluck,Clean,Butcher,Collect bones";
    static final String DEF_LIST_AUTO_CLICK_MENU = "Pick,Harvest wax";
    static final String DEF_LIST_SHAPEICON = "stalagoomba 1,diamond 7 1 0,255 255 0;/amberwash 2,diamond 7 0 1,255 102 0;/cavepuddle 2,diamond 7 0 1,0 204 102;/ladder 2,triangleUp 5 0 1,0 204 102;/minehole 2,triangleDown 5 0 1,0 204 102;/burrow 2,triangleDown 6 0 1,204 0 255;/spark 2,square 4 0 1,102 102 255;/snekkja 2,square 4 0 1,255 255 102;/dugout 2,square 4 0 1,255 255 102;/wheelbarrow 2,square 4 0 1,0 255 255;/cart 2,square 4 0 1,0 153 255;/knarr 2,square 4 0 1,255 255 102;/rowboat 2,square 4 0 1,255 255 102;/horse/ 1,square 4 0 1,0 204 0;items/arrow 2,triangleUp 5 0 1,102 255 204;milestone-stone-e 2,diamond 4 0 1,255 255 255;milestone-wood-e 2,diamond 4 0 1,255 255 255;/fishingnet 2,diamond 4 0 1,153 153 153;wonders/wellspring 1,diamond 5 0 1,0 255 255;/map/starshard 2,diamond 7 0 1,255 255 0";
    static final Color DEF_SIMPLE_WINDOW_COLOR = new Color(55, 64, 32, 255);
    static final Color DEF_GRID_COLOR = new Color(204, 204, 255, 45);
    static final int MINIMAP_DRAG_BUTTON = 3;
    static final int DEF_GOB_MAX_REQUEUE = 100000;

    static final int PLAYER_SPEED_0 = 0;
    static final int PLAYER_SPEED_1 = 1;
    static final int PLAYER_SPEED_2 = 2;
    static final int PLAYER_SPEED_3 = 3;

    static MixColor MIXCOLOR_RED = new MixColor(255,0,0,200);
    static MixColor MIXCOLOR_ORANGE = new MixColor(255,128,0,200);
    static MixColor MIXCOLOR_YELLOW = new MixColor(255,255,0,200);
    static MixColor MIXCOLOR_MAGENTA= new MixColor(255,0,255,200);
    static MixColor MIXCOLOR_LIGHTBLUE = new MixColor(0, 255, 255, 200);

    static GameUI gameUI;
    private static String cursorName = CURSOR_ARW;
    static Window windowEquipment,windowInvMain, toggleEquipsLastWindowClicked;
    static Makewindow makeWindow;
    static ZeeInvMainOptionsWdg invMainoptionsWdg;
    static ZeeOptionsJFrame zeecowOptions;
    static Button btnMkWndSearchInput;
    static GobIcon.SettingsWindow.IconList iconList;
    static ChatUI.Channel multiChat;
    private static Dropbox<String> iconListFilterBox;
    private static Inventory mainInv;
    static Glob glob;
    static boolean keepMapViewOverlay;

    static String playingAudio = null;
    static String lastUiMsg, uiMsgTextQuality, uiMsgTextBuffer;
    static long now, lastUiQualityMsgMs=0, lastUIMsgMs, lastHafenWarningMs=0;
    static Object[] lastMapViewClickArgs;
    static Gob lastMapViewClickGob;
    static String lastMapViewClickGobName;
    static Coord lastMapViewClickPc;
    static Coord2d lastMapViewClickMc;
    static int lastMapViewClickButton;
    static long lastMapViewClickMs;
    static Coord lastSavedOverlayStartCoord, lastSavedOverlayEndCoord;
    static int lastSavedOverlayModflags;
    static long lastSavedOverlayMs;
    static MCache.Overlay lastSavedOverlay;
    static GItem lastInvItem;
    static String lastInvItemBaseName, lastInvItemName;
    static long lastInvItemMs;
    static Coord lastUiClickCoord;
    static Class<?> classMSRad;

    static int aggroRadiusTiles = Utils.getprefi("aggroRadiusTiles", 11);
    static boolean alertOnPlayers = Utils.getprefb("alertOnPlayers", true);
    static boolean allowMidclickAutoBuy = false;
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
    static boolean autoStack = false;//set at initToggles()
    static String butcherAutoList = Utils.getpref("butcherAutoList", DEF_LIST_BUTCH_AUTO);
    static boolean cattleRosterHeight = Utils.getprefb("cattleRosterHeight", true);
    static double cattleRosterHeightPercentage = Utils.getprefd("cattleRosterHeightPercentage", 1.0);
    static boolean confirmPetal = Utils.getprefb("confirmPetal", true);
    static String confirmPetalList = Utils.getpref("confirmPetalList", DEF_LIST_CONFIRM_PETAL);
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
    static boolean equipShieldOnCombat = Utils.getprefb("equipShieldOnCombat", true);
    static boolean farmerMode = false;
    static boolean freeGobPlacement = Utils.getprefb("freeGobPlacement", true);
    static boolean fishMoonXpAlert = Utils.getprefb("fishMoonXpAlert", true);
    static int gridColorInt = Utils.getprefi("gridColorInt",ZeeConfig.colorToInt(DEF_GRID_COLOR));
    public static boolean hideFxSmoke = Utils.getprefb("hideFxSmoke", true);
    public static boolean hideFxAnimations = Utils.getprefb("hideFxAnimations", true);
    static boolean hideCrops = Utils.getprefb("hideCrops", false);
    static boolean hideTileTransitions = Utils.getprefb("hideTileTransitions", true);
    static boolean highlightCropsReady = Utils.getprefb("highlightCropsReady", true);
    static boolean isThinClient = false;
    static boolean autoTrackScents = Utils.getprefb("autoTrackScents", true);
    public static boolean isRainLimited = Utils.getprefb("isRainLimited", false);
    public static Integer rainLimitPerc = Utils.getprefi("rainLimitPerc", 25);
    public static boolean showGrowingTreeScale = Utils.getprefb("showGrowingTreeScale", true);
    public static boolean treeAnimation = Utils.getprefb("treeAnimation", false);
    static boolean keyBeltShiftTab = Utils.getprefb("keyBeltShiftTab", true);
    static boolean keyCamSwitchShiftC = Utils.getprefb("keyCamSwitchShiftC", true);
    static boolean keyUpDownAudioControl = Utils.getprefb("keyUpDownAudioControl", true);
    static boolean autoHideWindows = Utils.getprefb("autoHideWindows", false);
    static String hoverHideWindowsList = Utils.getpref("hoverHideWindowsList", DEF_LIST_HOVER_HIDE_WINDOWS);
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
                return MIXCOLOR_LIGHTBLUE;
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
                c = ZeeConfig.MIXCOLOR_YELLOW.color();
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
        return gobName.startsWith("gfx/terobjs/plants/") && !gobName.endsWith("trellis");
    }

    //  gfx/invobjs/turnip , gfx/invobjs/seed-turnip
    public static boolean isItemCrop(String basename) {
        String crops = "beetroot,seed-turnip,turnip,seed-carrot,carrot,seed-flax,seed-hemp,seed-leek,leek,seed-poppy,"
            +"seed-pipeweed,seed-cucumber,seed-barley,seed-wheat,seed-millet,seed-lettuce,"
            +"seed-pumpkin";
        return crops.contains(basename);
    }

    public static boolean isBug(String nameContains){
        String[] list = {
            "/silkmoth","/grasshopper","/ladybug","/dragonfly","/waterstrider","/firefly","/sandflea",
            "/cavemoth","/stagbeetle","/cavecentipede","/moonmoth","/monarchbutterfly","/items/grub",
            "/springbumblebee"
        };
        for (int i = 0; i < list.length; i++) {
            if(nameContains.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isAggressive(String nameContains){
        String[] list = {
               "/adder","/sandflea","/boar/","/badger/","/bear/","/bat/","/boreworm/",
                "/ooze/","/cavelouse/","/caveangler/","orca","/goldeneagle/","/lynx/",
                "/mammoth/","/moose/","/troll/","/walrus/","/goat/","/wolf/","/wolverine/",
                "/plo"
        };
        for (int i = 0; i < list.length; i++) {
            if(nameContains.contains(list[i]))
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
        /*
        Kill > Pluck > Butch = "gfx/invobjs/meat"
            gfx/invobjs/rooster
            gfx/invobjs/rooster-dead
            gfx/invobjs/chicken-plucked
            gfx/invobjs/chicken-cleaned, gfx/invobjs/adder-clean
            gfx/invobjs/meat
         */
        // skip cases ("/squirrelhide", "squirrelhide-blood", "squirreltail")
        if (isAnimalHideTailEtc(nameContains))
            return false;
        String[] endlist = {
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

    public static boolean isSmallAnimal(String nameContains){
        /*
        private static final String[] CRITTERS = {
            "/rat/rat", "/swan", "/squirrel", "/silkmoth", "/frog/", "/rockdove", "/quail", "/toad", "/grasshopper",
            "/ladybug", "/forestsnail", "/dragonfly", "/forestlizard", "/waterstrider", "/firefly", "/sandflea",
            "/rabbit", "/crab/", "/cavemoth", "/hedgehog", "/stagbeetle", "jellyfish", "/mallard", "/chick","/hen",
            "/rooster", "/irrbloss",
            "/cavecentipede", "/bogturtle", "/moonmoth", "/monarchbutterfly", "/items/grub", "/springbumblebee"
        };
         */
        String[] endlist = {
                "rockdove","quail","/hen","/rooster","magpie",
                "mallard","seagull","ptarmigan","grouse",
                "/rat/rat","/squirrel","/hedgehog","/bogturtle",
                "/rabbit-buck","rabbit-doe","/crab","/jellyfish",
                "/frog","/toad","/forestlizard","snail",
                "/adder"
        };
        for (int i = 0; i < endlist.length; i++) {
            if(nameContains.contains(endlist[i]))
                return true;
        }
        return false;
    }

    private static boolean isIconsCategNoobies(String nameContains) {
        String[] list = {
                "/hen","/rooster","/rabbit","rabbit-doe","/rat/rat","squirrel",
                "/grasshopper","/ladybug","/dragonfly","/waterstrider",
                "/cavemoth","/items/grub","/springbumblebee",
                "stingingnettle","taproot","cattail",
                "clover","snapdragon","/windweed",
                "bloatedbolete","chantrelle","/lorchel","parasol","yellowfoot"
        };
        for (int i = 0; i < list.length; i++) {
            if(nameContains.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isString(String name){
        String[] list = {
            "stingingnettle","taproot","cattail"
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

    static boolean isKritterNotPickable(Gob gob) {
        return isKritterNotPickable(gob.getres().name);
    }

    static boolean isKritterNotPickable(String resname) {
        String[] list = {
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
            "gfx/kritter/ants/"
        };
        for (int i = 0; i < list.length; i++) {
            if(resname.contains(list[i]))
                return true;
        }
        return false;
    }

    static boolean isBird(String name){
        String[] list = {
            "rockdove","quail","/chick","/hen","/rooster","eagle","owl","magpie",
            "mallard","pelican","seagull","swan","ptarmigan","grouse"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isHerb(String name){
        return name.contains("/herbs/");
    }

    public static boolean isFlower(String name){
        String[] list = {
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
        println(name);
        String[] list = {
            "bolete","truffle","trumpet","cavelantern","chantrelle","/lorchel","fairy","blewit",
            "puffball","indigo","parasol","snowtop","yellowfoot"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isTreeLeaf(String name){
        String[] list = {
                "trees/maple","trees/conkertree","trees/mulberry","trees/fig"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isTreeToughBark(String name){
        String[] list = {
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
        String[] list = {
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
        String[] list = {
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
        String[] list = {
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

        if(isPlayer(gob)  &&  gameUI!=null && gameUI.map.player()!=null) {
            // other players
            if (gameUI.map.player().id != gobId) {
                if (autoHearthOnStranger && !playerHasAnyPose(POSE_PLAYER_TRAVELHOMEPOINT, POSE_PLAYER_TRAVELHOMESHRUG)) {
                    autoHearth();
                }
                if (alertOnPlayers && !ZeeManagerGobClick.isGobDeadOrKO(gob)) {
                    String audio = mapCategoryAudio.get(CATEG_PVPANDSIEGE);
                    if (audio != null && !audio.isEmpty())
                        playAudioGobId(audio, gobId);
                    else
                        gameUI.error("player spotted");
                }
            }
            // user player
            //else gob.addol(new ZeeGobBox(gob,Coord3f.of(12f,12f,0.5f),new Color(192, 0, 0, 128)));

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
        if (gobName.startsWith("gfx/terobjs/dng/")){//TODO specify names
            ZeeSynth.textToSpeakLinuxFestival("dungeon");
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


    // updates buttons for showing claims
    // MapView() constructor enable overlays using enol()
    public static void checkShowClaimsButtonState(GameUI.MenuCheckBox menuCheckBox, String base) {
        if(base.contains("lbtn-claim")) {
            menuCheckBox.click();
        }else if(base.contains("lbtn-vil")) {
            menuCheckBox.click();
        }else if(base.contains("lbtn-rlm")){
            menuCheckBox.click();
        }
    }

    // make expanded map window fit screen
    static Coord mapWndLastPos, mapWndLastSz;
    static int mapWndMinHeightBackup=350, mapWndMinHeight=350;
    public static void windowMapCompact(MapWnd mapWnd, boolean compact) {

        // TODO better way
        if(compact && mapWnd.c.equals(0,0)) {
            //println("special startup condition?");
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

        checkFishMoonXpAlert();

        // fix switching chars with different inv sizes?
        inventoryResized(invSlots);
    }

    // Fish Moon XP alert
    private static boolean fishMoonAlertDone = false;
    private static void checkFishMoonXpAlert() {
        if (fishMoonXpAlert && !fishMoonAlertDone) {
            Astronomy a = gameUI.ui.sess.glob.ast;
            int moonPhaseIndex = (int) Math.round(a.mp * (double) Cal.moon.f.length) % Cal.moon.f.length;
            if (a.moonPhases[moonPhaseIndex].toLowerCase().contains("full moon")) {
                fishMoonAlertDone = true;
                Cal.fishMoonShowText = true;
            }
        }
    }


    public static void windowAdded(Window window) {

        String windowTitle = window.cap.strip();

        if (windowTitle.contentEquals("Cattle Roster")) {
            windowModCattleRoster(window);
        }
        else if(List.of("Barrel","Cistern","Demijohn").contains(windowTitle)) {
            ZeeManagerGobClick.labelGobByContents(window);
        }
        else if(windowTitle.contentEquals("Equipment")) {
            windowEquipment = window;
        }
        else if(windowTitle.contentEquals("Inventory")) {
            windowInvMain = window;
        }
        else if(windowTitle.contentEquals("Barter Stand") && window.sz.x > 300){//avoid build window
            windowModBarterStand(window);
        }
        else if(isWindowAnimalStats(windowTitle)){
            windowModAnimalStats(window, windowTitle);
        }
        else if (windowTitle.contentEquals("Oven") || windowTitle.contentEquals("Kiln") || windowTitle.contains("Smelter")){
            if (!isBuildWindow(window))
                windowAddFuelGUI(window,windowTitle);
        }
        else if(windowTitle.contentEquals("Extraction Press")) {
            if (!isBuildWindow(window)) {
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
        }
        else if (ZeeManagerMiner.tunnelHelperStage == ZeeManagerMiner.TUNNELHELPER_STAGE5_BUILDCOL && windowTitle.contentEquals("Stone Column")){
            ZeeManagerMiner.tunnelHelperBuildColumn(window);
        }

        // Craft window
        if(isMakewindow(window)) {
            // bug collection
            if(windowTitle.contentEquals("Bug Collection")) {
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
        }

        if (gameUI!=null && !gameUI.sz.equals(0,0) && !isBuildWindow(window)){
            windowApplySavedPosition(window, windowTitle);
            windowFitView(window);
        }

        //order of call is important due to window.hasOrganizeButton
        windowModOrganizeButton(window, windowTitle);
        windowModAutoHideButton(window,windowTitle);
    }

    private static void windowModCattleRoster(Window window) {

        //Cattle Roster, called for each animal type
        if(cattleRosterHeight  && cattleRosterHeightPercentage < 1.0){

            new ZeeThread() {
                public void run() {

                    // wait remote widgets
                    try{
                        Field f = window.getClass().getDeclaredField("rmseq");
                        int rmseq = (int) f.get(window);
                        int lastRmSeq = rmseq;
                        long lastRmSeqMs = now();
                        //wait rmseq stop for 1sec
                        do{
                            sleep(PING_MS);
                            rmseq = (int) f.get(window);
                            if (rmseq != lastRmSeq) {
                                lastRmSeq = rmseq;
                                lastRmSeqMs = now();
                            }
                        }while(now() - lastRmSeqMs < PING_MS);
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

                    // animal type buttons reposition
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
        if (hoverHideWindowsList.isBlank())
            return;
        boolean showButton = false;
        for (String cap : ZeeConfig.hoverHideWindowsList.split(",")) {
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
        //TODO uncomment
        window.buttonAutoHide = ((Window.DefaultDeco)window.deco).add(
            new ZeeWindow.ZeeButton(ZeeWindow.ZeeButton.BUTTON_SIZE,ZeeWindow.ZeeButton.TEXT_AUTOHIDEWINDOW,"auto hide"),
            ((Window.DefaultDeco)window.deco).cbtn.c.x - pad,
            ((Window.DefaultDeco)window.deco).cbtn.c.y
        );
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
            items.removeIf(wItem -> ZeeManagerItemClick.isStackPagina(wItem.item));
            //add items
            //TODO get items from stack
            ZeeManagerGobClick.addItemsToGob(items,num,g);
        }catch (NumberFormatException e){
            ZeeConfig.msg(e.getMessage());
        }
    }


    public static void checkRemoteWidget(String type, Widget wdg) {
        //Cattle Roster, called for each animal type
//        if(cattleRosterHeight && type.contains("rosters/") && cattleRosterHeightPercentage < 1.0){
//
//            //resize "window"
//            wdg.resize(wdg.sz.x, (int)(wdg.sz.y * cattleRosterHeightPercentage));
//
//            int y = -1;
//            for (Widget w: wdg.children()) {
//                //reposition buttons
//                if(w.getClass().getSimpleName().contentEquals("Button")){
//                    if(y==-1) { //calculate once
//                        y = (int) (w.c.y * cattleRosterHeightPercentage) - (int)(w.sz.y*0.6);
//                    }
//                    w.c.y = y;
//                }
//                //resize scrollbar and entrycont
//                else if(w instanceof Scrollbar){
//                    Scrollbar sb = (Scrollbar) w;
//                    sb.resize(sb.sz.x, (int)(sb.sz.y * cattleRosterHeightPercentage));
//                    println(type+" > scrollbar > val="+sb.val+" max="+sb.max);
//                }
//            }
//        }
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

        // auto-buy checkbox
        ZeeConfig.allowMidclickAutoBuy = false;
        window.add(new CheckBox("allow midclick auto-buy"){
            public void changed(boolean val) {
                ZeeConfig.allowMidclickAutoBuy = val;
            }
        },0,400);


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
                Gob woodenChest = getClosestGob(findGobsByNameEndsWith("/chest","/largechest"));
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
        },0,413);
        btn.settip("return branches to closest wooden chest");


        window.resize(260,440);
    }


    private static void windowApplySavedPosition(Window window, String windowTitle) {
        Coord c;
        if(rememberWindowsPos && !(window instanceof MapWnd) ){
            if(isMakewindow(window)){
                windowTitle = MAKE_WINDOW_NAME;
            }
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
        String singleWindows = "Craft,Inventory,Character Sheet,Options,Kith & Kin,Equipment";
        if(!singleWindows.contains(windowTitle)) { // avoid searching multiple Windows
            List<Window> wins = getWindows(windowTitle);
            if (wins.size() > 1) {
                //add organize button
                int pad = ZeeWindow.ZeeButton.BUTTON_SIZE;
                //TODO uncomment
                Window.DefaultDeco deco = ((Window.DefaultDeco)window.deco);
                deco.add(
                    new ZeeWindow.ZeeButton(pad,ZeeWindow.ZeeButton.TEXT_ORGANIZEWINDOWS,"organize duplicates"),
                        deco.cbtn.c.x - pad,
                        deco.cbtn.c.y
                );
                window.hasOrganizeButton = true;
            }
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
        else if(animal.equals("Bull") || animal.equals("Cow"))
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
        String male = "Hog,Bull,Stallion,Billy,Ram";
        String gender = male.contains(animal) ? "M" : "F";
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
        String list = "Sow,Hog,Cow,Bull,Stallion,Mare,Nanny,Billy,Ewe,Ram";
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
            ret.put(CATEG_AGROCREATURES, MIXCOLOR_YELLOW.color());
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
            name = MAKE_WINDOW_NAME;

        //save window pos
        mapWindowPos.put(name, new Coord(window.c));
        Utils.setpref(MAP_WND_POS, serialize(mapWindowPos));
    }

    public static boolean isMakewindow(Window window) {
        for (Button b: window.children(Button.class)) {
            if(b.text.text.contentEquals("Craft All"))
                return true;
        }
        return false;
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
        new Thread(new Runnable() {
            public void run() {
                try {
                    println("init toggles > "+ZeeSess.charSwitchCurPlayingChar);

                    // add minimap resize buttons
                    toggleMinimapResizeButtons(gameUI.mapfile,isMiniMapCompacted());

                    // show char switch window
                    if (ZeeSess.charSwitchKeepWindow)
                        ZeeSess.charSwitchCreateWindow();

                    // auto run speed
                    Thread.sleep(1000);
                    if(autoRunLogin)
                        setPlayerSpeed(PLAYER_SPEED_2);

                    // auto track scents
                    Thread.sleep(1000);
                    if(autoTrackScents && !alreadyTrackingScents) {
                        gameUI.menu.wdgmsg("act", "tracking");
                        //tracking for all characters
                        alreadyTrackingScents = true;
                    }

                    // discover autostack checkbox value
                    //TODO waitUiMsg
                    toggleAutostack();//toggle autostack
                    Thread.sleep(1000);
                    if (lastUiMsg!=null){
                        //autostack was off
                        if (lastUiMsg.contains("Stacking is now turned on.")){
                            ZeeInvMainOptionsWdg.cbAutoStack.a = autoStack = false;
                        }
                        //autostack was on
                        else if (lastUiMsg.contains("Stacking is now turned off.")){
                            ZeeInvMainOptionsWdg.cbAutoStack.a = autoStack = true;
                        }
                        else {
                            println("couldn't determine stacking option value");
                        }
                    }
                    toggleAutostack();//reset to original value

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static int getPlayerSpeed() {
        return gameUI.ulpanel.getchild(Speedget.class).cur;
    }

    public static void setPlayerSpeed(int spd) {
        gameUI.ulpanel.getchild(Speedget.class).set(spd);
    }

    public static void toggleMineSupport() {
        try {
            Field field = classMSRad.getDeclaredField("show");
            Method method = classMSRad.getMethod("show",boolean.class);
            //field.setBoolean(classMSRad, !field.getBoolean(classMSRad));
            method.invoke(classMSRad, !field.getBoolean(classMSRad));
        }catch (Exception e){
            e.printStackTrace();
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
    public static String[] midiJawsTheme = new String[]{
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
    public static String[] midiUfoThirdKind = new String[]{
            "200",
            "5D,300,100",
            "5E,300,120",
            "5C,600,110",
            "4C,600,100",
            "4G,1000,90",
            "200"
    };
    public static String[] midiBeethoven5th = new String[]{
            "200",
            "3G,100,120","50",
            "3G,100,120","50",
            "3G,100,120","50",
            "3D#,1000,120",
            "200"
    };
    public static String[] midiWoodPecker= new String[]{
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
            Integer count = getMainInventory().countItemsByNameContains(itemName);
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

        // fast zoom
        if (ev.getKeyCode()==KeyEvent.VK_PAGE_DOWN) {
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
                Audio.setvolume(Double.parseDouble(String.format("%.1f", vol)) + 0.1);
            else if (vol < 1)
                Audio.setvolume(1); // max 1
            msgLow("volume "+Audio.volume);
            return true;
        }
        // volume down (arrow)
        else if (ZeeConfig.keyUpDownAudioControl && ev.getKeyCode()==KeyEvent.VK_DOWN){
            double vol = Audio.volume;
            if (vol > 0.1)
                Audio.setvolume(Double.parseDouble(String.format("%.1f", vol)) - 0.1);
            else if (vol > 0)
                Audio.setvolume(0); // min 0
            msgLow("volume "+Audio.volume);
            return true;
        }
        // Belt toggle (Shift+Tab)
        else if(ZeeConfig.keyBeltShiftTab && ev.getKeyCode()==KeyEvent.VK_TAB && ev.isShiftDown()){
            tooggleWindowsBeltOrBasketCreel();
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
        // show/hide crops (ctrl+h)
        else if (ev.getKeyCode()==KeyEvent.VK_H && ev.isControlDown()){
            toggleHideCrops();
            return true;
        }
        return false;
    }

    private static void toggleHideCrops() {
        hideCrops = !hideCrops;
        Utils.setprefb("hideCrops",hideCrops);
        ZeeQuickOptionsWindow.updateCheckboxNoBump("hideCrops",hideCrops);
        if (hideCrops)
            msgLow("hide crops");
        else
            msgLow("show crops");
    }

    private static boolean createdBasketWindow=false, createdCreelWindow=false;
    private static void tooggleWindowsBeltOrBasketCreel() {

        //toggle basket
        Window basketWindow = getWindow("Basket");
        if (basketWindow == null) {
            WItem item = ZeeManagerItemClick.getEquippedItemNameEndsWith("/pickingbasket");
            if (item != null) {
                item.mousedown(Coord.z, 3); //create basket window
                createdBasketWindow = true;
            }else {
                createdBasketWindow = false;
            }
        } else {
            if (basketWindow.visible()) {
                basketWindow.hide();
            } else {
                basketWindow.show();
            }
        }

        //toggle creel
        Window creelWindow = getWindow("Creel");
        if (creelWindow == null) {
            WItem item = ZeeManagerItemClick.getEquippedItemNameEndsWith("/creel");
            if (item != null) {
                item.mousedown(Coord.z, 3); //create creel window
                createdCreelWindow = true;
            }else{
                createdCreelWindow = false;
            }
        } else {
            if (creelWindow.visible()) {
                creelWindow.hide();
            } else {
                creelWindow.show();
            }
        }

        // toggle belt
        Window beltWindow = getWindow("Belt");
        if (beltWindow == null) {
            clickOpenBelt();
        } else {
            if (!beltWindow.visible()) {
                beltWindow.show();
            } else if(!createdBasketWindow && !createdCreelWindow){
                //only hide  belt if no basket/creel
                beltWindow.hide();
            }
        }
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
        String[] containers = (
                //boxes
                "Woodbox,Cupboard,Chest,Crate,Basket,Casket,Box,Coffer,Steelbox,Metal Cabinet,"
                +"Urn,Pot,"
                //misc
                +"Knarr,Snekkja,Wagon,Table,Saddlebags,"
                +"Furnace,Smelter,Desk,Trunk,Shed,Packrack,Strongbox,Stockpile,"
                +"Tub,Compost Bin,Extraction Press,Rack,Herbalist Table,Frame,"
                +"Chicken Coop,Rabbit Hutch,Archery Target,Oven,Steel crucible,"
                +"Cauldron,Pane mold,Kiln,Old Trunk,Old Stump,Smoke shed,Finery Forge,"
                +"Tidepool,Fireplace,Quiver,Creel"
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
            gameUI.msg(uiMsgTextQuality + uiMsgTextBuffer);
        }

        // feasting msg
        if (isPlayerFeasting && text.startsWith("You gained ")){
            ZeeManagerCraft.feastingMsgStatGained(text);
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

//    public static void checkCharSelection(String msg) {
//        if(msg.equalsIgnoreCase("play")){
//            ZeeConfig.resetCharSelected();
//        }
//    }

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
        ZeeManagerMiner.tilesMonitorCleanup();
        ZeeHistWdg.listHistButtons.clear();

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
        return isPlayerLiftingGob("/wheelbarrow") != null;
    }

    public static boolean isPlayerDrivingWheelbarrow() {
        return playerHasAnyPose(POSE_PLAYER_DRIVE_WHEELBARROW) && getCursorName().contentEquals(CURSOR_HAND);
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

    public static boolean isPlayerLiftingGob() {
        return playerHasAnyPose(POSE_PLAYER_LIFTING);
    }

    public static Gob isPlayerLiftingGob(String gobNameContains) {
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

    public static Gob getClosestGob(List<Gob> gobs) {
        if(gobs==null || gobs.size()==0)
            return null;
        Gob closestGob = gobs.get(0);
        double closestDist = distanceToPlayer(closestGob);
        double dist;
        for (Gob g : gobs) {
            dist = distanceToPlayer(g);
            if (dist < closestDist) {
                closestGob = g;
                closestDist = dist;
            }
        }
        return closestGob;
    }

    public static double distanceToPlayer(Gob gob) {
        return ZeeConfig.getPlayerGob().getc().dist(gob.getc());//TODO use rc if necessary
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
    public static double getStamina() {
        return (100 * gameUI.getmeter("stam", 0).a);
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

    public static boolean isPlayerDrinking(){
        return getHourglass() > -1;
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
    public static double getHourglass() {
        if (gameUI.prog==null)
            return -777;
        return gameUI.prog.prog;
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

    public static void addGobText(Gob g, String s, int height){
        addGobText(g,s,0,255,0,255,height);
    }

    static final Text.Foundry defGobTextFont = new Text.Foundry(Text.sans.deriveFont(Font.PLAIN, UI.scale(9))).aa(false);
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
            gob.setattr(new ZeeGobColor(gob, new MixColor(r, g, b, a)));
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

    public static void getPlayerTileType(){
        int id = gameUI.ui.sess.glob.map.getTileSafe(getPlayerGob().rc.floor(MCache.tilesz));
        Tiler tl = gameUI.ui.sess.glob.map.tiler(id);
        Resource res = gameUI.ui.sess.glob.map.tilesetr(id);
        println("id="+id+" , tl="+tl.getClass().getSimpleName()+" , res="+res.name);
    }
    public static List<String> bogtype = new ArrayList<>(Arrays.asList("gfx/tiles/bog", "gfx/tiles/bogwater", "gfx/tiles/fen", "gfx/tiles/fenwater", "gfx/tiles/swamp", "gfx/tiles/swampwater", "", ""));


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
        return c.div(11, 11).floor();
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
        gameUI.msg(s);
    }

    public static void msgLow(String s) {
        gameUI.msgLow(s);
    }

    public static void msgError(String s) {
        gameUI.error(s);
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

    public static boolean isTaskCanceledByGroundClick() {
        //cancel if clicked right/left button
        return lastMapViewClickButton != 2;
    }

    public static Widget getIconFilterWidget(){

        if (iconListFilterBox != null) {
            return iconListFilterBox;
        }

        iconListFilterBox =  new Dropbox<String>(110,14,20) {
            String space = "     ";
            private final List<String> filters = new ArrayList<String>() {{
                add(space+"all");
                add(space+"aggressive");
                add(space+"birds");
                add(space+"bugs");
                add(space+"bushes");
                add(space+"flowers");
                add(space+"herbs");
                add(space+"kritters");
                add(space+"mushrooms");
                add(space+"noob stuff");
                add(space+"small animals");
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
                if(!filter.equalsIgnoreCase("all")) {
                    iconList.ordered = getIconsFiltered(filter, iconList.ordered);
                }
            }
            public void dispose() {
                super.dispose();
                this.sel = "";
            }
        };

        return iconListFilterBox;
    }


    public static List<GobIcon.SettingsWindow.Icon> getIconsFiltered(String filter, List<GobIcon.SettingsWindow.Icon> listOrdered) {

        ArrayList<GobIcon.SettingsWindow.Icon> filteredList = new ArrayList<>(listOrdered);

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
        else if(filter.equals("noob stuff"))
            filteredList.removeIf(entry -> !ZeeConfig.isIconsCategNoobies(entry.conf.res.name));
        else if(filter.equals("small animals"))
            filteredList.removeIf(entry -> !ZeeConfig.isSmallAnimal(entry.conf.res.name));
        else if(filter.equals("aggressive"))
            filteredList.removeIf(entry -> !ZeeConfig.isAggressive(entry.conf.res.name));

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
                    if (gobsWaiting.isEmpty()) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // remove gob from queue
                    Gob g = gobsWaiting.remove();
                    synchronized (g) {
                        if (g.isGobWaitingSettings) {
                            // apply gob settings
                            countRemovals++;
                            consumeGobSettings(g);
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
        if(ob != null && ob.getres()!=null) {
            synchronized (gobConsumer) {
                gobsWaiting.add(ob);
                gobConsumer.notify();
            }
        }
    }
    static long countRemovals =0, countDrops =0;//GLPanel.drawstats()
    static void consumeGobSettings(Gob ob){

        try {

            // ignore bat if using batcape
            if (!ob.getres().name.contentEquals("gfx/kritter/bat/bat") || !ZeeManagerItemClick.isItemEquipped("/batcape")) {

                // audio alerts
                ZeeConfig.applyGobSettingsAudio(ob);

                // aggro radius
                ZeeConfig.applyGobSettingsAggro(ob);

            }

            // highlight gob color
            ZeeConfig.applyGobSettingsHighlight(ob, ZeeConfig.getHighlightGobColor(ob));

            // auto boulder option (maybe remove)
            if (ZeeConfig.autoChipMinedBoulder && ZeeManagerMiner.isCursorMining() && ZeeManagerMiner.isBoulder(ob))
                ZeeManagerMiner.checkBoulderGobAdded(ob);

            // remount closest horse
            if (ZeeManagerGobClick.remountClosestHorse && ZeeManagerGobClick.isGobHorse(ob.getres().name)) {
                ZeeManagerGobClick.remountHorse();
            }

            // barter stand item search labels
            if (ZeeManagerGobClick.barterSearchOpen && ob.getres().name.endsWith("/barterstand")) {
                ZeeManagerGobClick.addTextBarterStand(ob);
            }

            // save gob name
            if (ob.getres().name!=null && !ob.getres().name.isBlank() && !listGobsSession.contains(ob.getres().name)) {
                listGobsSession.add(ob.getres().name);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void applyGobSettingsAggro(Gob gob) {
        if( mapCategoryGobs.get(CATEG_AGROCREATURES).contains(gob.getres().name) && !ZeeManagerGobClick.isGobDeadOrKO(gob)) {
            //aggro radius
            if (ZeeConfig.aggroRadiusTiles > 0)
                gob.addol(new Gob.Overlay(gob, new ZeeGobRadius(gob, null, ZeeConfig.aggroRadiusTiles * MCache.tilesz2.y), ZeeManagerGobClick.OVERLAY_ID_AGGRO));
        }
    }

    public static Gob getClosestGobByNameContains(String nameContains) {
        if (nameContains==null || nameContains.isBlank())
            return null;
        return getClosestGob(findGobsByNameContains(nameContains));
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
            if (!ZeeConfig.allowMidclickAutoBuy){
                ZeeConfig.msg("Click bottom checkbox to allow auto-buy");
                return;
            }
            ZeeThread.clickButtonUntilMsgOrHoldingItem(button,"buying");
        }
    }

    public static double getItemQuality(WItem item) {
        return Inventory.getQuality(item.item);
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
            println("getGobPoses > "+e.getMessage()+"  ,  gobReady="+gob.isGobWaitingSettings);
        }
        return ret;
    }

    public static Tiler getTilerAt(Coord2d coordMc) {
        return getTilerAt(coordMc.floor(MCache.tilesz));
    }

    public static Tiler getTilerAt(Coord tile) {
        int id = ZeeConfig.gameUI.ui.sess.glob.map.getTileSafe(tile);
        Tiler tl = ZeeConfig.gameUI.ui.sess.glob.map.tiler(id);
        return tl;
    }

    public static String getTileResName(Coord tileCoord) {
        int id = ZeeConfig.gameUI.ui.sess.glob.map.getTileSafe(tileCoord);
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

    public static boolean hideAnimation(Resource res) {
        if (res!=null && "/chimingbluebell,/saptap,/brazierflame,/stockpile-trash,/beehive,/cigar,/dreca".contains(res.basename())) {
            return true;
        }
        //ZeeConfig.println("res " + (res!=null?res.name:"null"));
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

        // inspect tooltips
        showInspectTooltip = curs.contentEquals(CURSOR_INSPECT);
        if (!showInspectTooltip)
            gameUI.map.ttip = null;

        //feast window
        isPlayerFeasting = curs.contentEquals(CURSOR_EAT);
        if(!isPlayerFeasting)
            ZeeManagerCraft.windowFeasting = null;
    }

    static long lastIconNotifySaveMs = ZeeThread.now();
    public static boolean allowIconNotifySave() {
        return ZeeThread.now() - lastIconNotifySaveMs > 1000;
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
        if (ZeeConfig.equipShieldOnCombat && !ZeeManagerItemClick.isItemEquipped("huntersbow") && !ZeeManagerItemClick.isItemEquipped("rangersbow"))
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
        if (confirmPetal) {
            String[] list = confirmPetalList.split(",");
            for (int i = 0; i < list.length; i++) {
                if (name.contentEquals(list[i])) {
                    if (!gameUI.ui.modctrl) {
                        ZeeConfig.msgError("Ctrl+click to confirm "+list[i]);
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
        List<String> names = Arrays.asList(
                TILE_WATER_FRESH_DEEP , TILE_WATER_FRESH_SHALLOW ,
                TILE_WATER_OCEAN_SHALLOW , TILE_WATER_OCEAN_DEEP , TILE_WATER_OCEAN_DEEPER ,
                TILE_SWAMP , TILE_SWAMP_WATER ,
                TILE_SWAMP_BOG , TILE_SWAMP_BOG_WATER
        );

        return names.contains(ZeeConfig.getTileResName(getPlayerTile()));
    }

    static boolean isPlayerTileShallowWaterOrSwamp() {
        List<String> names = Arrays.asList(
            TILE_WATER_FRESH_SHALLOW ,
            TILE_WATER_OCEAN_SHALLOW ,
            TILE_SWAMP , TILE_SWAMP_WATER ,
            TILE_SWAMP_BOG , TILE_SWAMP_BOG_WATER
        );

        return names.contains(ZeeConfig.getTileResName(getPlayerTile()));
    }

    static boolean isPlayerTileDeepWater() {
        List<String> names = Arrays.asList(
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
        List<Gob> cellarStairs = findGobsByNameEndsWith("arch/cellarstairs");
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

    private static boolean backupBlockAudio;
    private static String backupBlockAudioList = null;
    public static void muteAudioMsg(String ... msgs) {
        //backup settings for restoreMutedAudioMsg()
        backupBlockAudio = blockAudioMsg;
        backupBlockAudioList = blockAudioMsgList;

        blockAudioMsg = true;
        if(!blockAudioMsgList.endsWith(";")){
            blockAudioMsgList += ";";
        }
        for (String m : msgs) {
            blockAudioMsgList += m + ";";
        }

        //remove last ";"
        blockAudioMsgList = blockAudioMsgList.replaceFirst(";$","");

        println("[TEMP] "+blockAudioMsgList);
    }
    public static void restoreMutedAudioMsg() {
        if (backupBlockAudioList==null){
            println("restoreMutedAudioMsg > backup list missing (call muteAudioMsg first)");
            return;
        }
        blockAudioMsg = backupBlockAudio;
        blockAudioMsgList = backupBlockAudioList;
        backupBlockAudioList = null;
        println("[DEF] "+blockAudioMsgList);
    }
}
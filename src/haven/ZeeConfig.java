package haven;

import haven.render.BaseColor;
import haven.render.MixColor;
import haven.render.Pipe;
import haven.render.States;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class ZeeConfig {
    public static final String CATEG_PVPANDSIEGE = "PVP and siege";
    public static final String CATEG_AGROCREATURES = "Agressive creatures";
    public static final String CATEG_RAREFORAGE = "Rare forageables";
    public static final String CATEG_LOCRES = "Localized resources";
    public static final String MAP_GOB_AUDIO = "mapGobSaved";
    public static final String MAP_GOB_CATEGORY = "mapGobCategory";
    public static final String MAP_CATEGORY_AUDIO = "mapCategoryAudio";
    public static final String MAP_CATEGORY_COLOR = "mapCategoryColor";
    public static final String MAP_CATEGORY_GOBS = "mapCategoryGobs";
    public static final String MAP_ACTION_USES = "mapActionUses";
    public static final String MAP_GOB_COLOR = "mapGobSettings";
    public static final String MAP_WND_POS = "mapWindowPos";
    public static final String MAKE_WINDOW_NAME = "Makewindow";
    public static MixColor MIXCOLOR_RED = new MixColor(255,0,0,200);
    public static MixColor MIXCOLOR_ORANGE = new MixColor(255,128,0,200);
    public static MixColor MIXCOLOR_YELLOW = new MixColor(255,255,0,200);
    public static MixColor MIXCOLOR_MAGENTA= new MixColor(255,0,255,200);
    public static MixColor MIXCOLOR_LIGHTBLUE = new MixColor(0, 255, 255, 200);

    public static GameUI gameUI;
    public static Window windowEquipment,windowInvMain;
    public static Makewindow makeWindow;
    public static ZeeInvMainOptionsWdg invMainoptionsWdg;
    public static ZeecowOptionsWindow zeecowOptions;
    public static Button btnMkWndSearchInput, btnMkWndBack, btnMkWndFwd, btnMkWndDel;

    public static String playingAudio = null;
    public static boolean clickPetal = false;
    public static String clickPetalName = "";
    public static String uiMsgTextQuality, uiMsgTextBuffer;
    public static long now, lastUiMessageMs = 0;
    public static Object[] lastMapViewClickArgs;

    public static boolean actionSearchGlobal = Utils.getprefb("actionSearchGlobal", true);
    public static boolean alertOnPlayers = Utils.getprefb("alertOnPlayers", true);
    public static boolean autoChipMinedBoulder = Utils.getprefb("autoChipMinedBoulder", true);
    public static boolean autoClickMenuOption = Utils.getprefb("autoClickMenuOption", true);
    public static String autoClickMenuOptionList = Utils.getpref("autoClickMenuOptionList", "Pick,Harvest wax");
    public static boolean autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);
    public static boolean autoOpenEquips = Utils.getprefb("beltToggleEquips", true);
    public static boolean autoOpenBelt = Utils.getprefb("autoOpenBelt", true);
    public static boolean autoRunLogin = Utils.getprefb("autoRunLogin", true);
    public static boolean butcherAuto = false;
    public static String butcherAutoList = Utils.getpref("butcherAutoList","Break,Scale,Wring neck,Kill,Skin,Flay,Pluck,Clean,Butcher,Collect bones");
    public static boolean cattleRosterHeight = Utils.getprefb("cattleRosterHeight", false);
    public static double cattleRosterHeightPercentage = Utils.getprefd("cattleRosterHeightPercentage", 1.0);
    public static List<String> craftHistoryList = initCraftHistory();
    public static int craftHistoryPos = -1;
    public static boolean isCraftHistoryNavigation = false;
    public static boolean ctrlClickMinimapContent = Utils.getprefb("dropCtrlClickMinimapContent", true);
    public static boolean debugWidgetMsgs = false;//disabled by default
    public static boolean debugCodeRes = Utils.getprefb("debugCodeRes", false);
    public static boolean dropHoldingItemAltKey = Utils.getprefb("dropHoldingItemAltKey", true);
    public static boolean dropMinedCurios = Utils.getprefb("dropMinedCurios", true);
    public static boolean dropMinedOre = Utils.getprefb("dropMinedOre", true);
    public static boolean dropMinedSilverGold = Utils.getprefb("dropMinedOrePrecious", true);
    public static boolean dropMinedStones = Utils.getprefb("dropMinedStones", true);
    public static boolean dropSeeds = false;//always starts off (TODO: set false when character loads)
    public static boolean dropSoil = false;
    public static boolean equiporyCompact = Utils.getprefb("equiporyCompact", false);
    public static boolean highlightAggressiveGobs = Utils.getprefb("highlighAggressiveGobs", true);
    public static boolean highlightCropsReady = Utils.getprefb("highlightCropsReady", true);
    public static boolean highlightGrowingTrees = Utils.getprefb("highlightGrowingTrees", true);
    public static boolean keyBeltShiftTab = Utils.getprefb("keyBeltShiftTab", true);
    public static boolean keyCamSwitchShiftC = Utils.getprefb("keyCamSwitchShiftC", true);
    public static boolean midclickEquipManager = Utils.getprefb("midclickEquipManager", true);
    public static boolean miniTrees = Utils.getprefb("miniTrees", false);
    public static Integer miniTreesSize = Utils.getprefi("miniTreesSize", 50);
    public static boolean notifyBuddyOnline = Utils.getprefb("notifyBuddyOnline", false);
    public static boolean showInventoryLogin = Utils.getprefb("showInventoryLogin", true);
    public static boolean showIconsZoomOut = Utils.getprefb("showIconsZoomOut", true);
    public static boolean showEquipsLogin = Utils.getprefb("showEquipsLogin", false);
    public static boolean sortActionsByUses = Utils.getprefb("sortActionsByUses", true);
    public static boolean rememberWindowsPos = Utils.getprefb("rememberWindowsPos", true);
    public static boolean zoomOrthoExtended = Utils.getprefb("zoomOrthoExtended", true);

    public final static Set<String> mineablesStone = new HashSet<String>(Arrays.asList(
            "gneiss","basalt","cinnabar","dolomite","feldspar","flint",
            "granite","hornblende","limestone","marble","porphyry","quartz",
            "sandstone","schist","blackcoal","zincspar","apatite","fluorospar",
            "gabbro","corund","kyanite","mica","microlite","orthoclase","soapstone",
            "sodalite","olivine","alabaster","breccia","diabase","arkose",
            "diorite","slate","arkose","eclogite","jasper","greenschist","pegmatite",
            "ilmenite","rhyolite","pumice"
    ));
    public final static Set<String> mineablesOre = new HashSet<String>(Arrays.asList(
            "cassiterite","chalcopyrite","malachite","ilmenite",
            "limonite","hematite","magnetite","leadglance","peacockore"
    ));
    public final static Set<String> mineablesOrePrecious = new HashSet<String>(Arrays.asList(
            "galena","argentite","hornsilver",
            "petzite","sylvanite","nagyagite"
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
            "gfx/terobjs/lilypadlotus"
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
        "gfx/terobjs/herbs/mandrake",
        "gfx/terobjs/herbs/seashell"
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
            "gfx/kritter/troll/troll",
            "gfx/kritter/walrus/walrus",
            "gfx/kritter/wildgoat/wildgoat",
            "gfx/kritter/wolf/wolf",
            "gfx/kritter/wolverine/wolverine"
    ));
    public final static Set<String> pvpGobs = new HashSet<String>(Arrays.asList(
            "gfx/terobjs/vehicle/bram",
            "gfx/terobjs/vehicle/catapult",
            "gfx/kritter/nidbane/nidbane",
            "gfx/terobjs/vehicle/wreckingball"
    ));


    public static HashMap<String,String> mapGobSession = new HashMap<String,String>();
    public static HashMap<String, Set<String>> mapCategoryGobs = initMapCategoryGobs();//init categs first
    public static HashMap<String,String> mapGobAudio = initMapGobAudio();
    public static HashMap<String,String> mapGobCategory = initMapGobCategory();
    public static HashMap<String,String> mapCategoryAudio = initMapCategoryAudio();
    public static HashMap<String,Integer> mapActionUses = initMapActionUses();
    public static HashMap<String, Color> mapGobColor = initMapGobColor();
    public static HashMap<String,Color> mapCategoryColor = initMapCategoryColor();
    public static HashMap<String,Coord> mapWindowPos = initMapWindowPos();
    public static GobIcon.SettingsWindow.IconList iconList;


    public static void checkRemoteWidget(String type, Widget wdg) {

        //Cattle Roster
        if(type.contains("rosters/") && ZeeConfig.cattleRosterHeightPercentage <1.0){

            //resize "window"
            wdg.resize(wdg.sz.x, (int)(wdg.sz.y * ZeeConfig.cattleRosterHeightPercentage));

            //reposition buttons
            int y = -1;
            for (Widget w: wdg.children()) {
                if(w.getClass().getSimpleName().contentEquals("Button")){
                    if(y==-1) { //calculate once
                        y = (int) (w.c.y * ZeeConfig.cattleRosterHeightPercentage) - (int)(w.sz.y*0.6);
                    }
                    w.c.y = y;
                }
            }
        }
    }


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

    public static MixColor getHighlightDrawable(Gob gob) {
        if(gob==null)
            return null;

        //get Type and name
        String gobName, categ;
        Color c;

        try{
            gobName = gob.getres().name;
        }catch (Resource.Loading loading){
            loading.printStackTrace();
            return null;
        }

        //System.out.printf("gobHighlightDrawable %s ", gobName);

        //if it's a tree
        if(highlightGrowingTrees && (isTree(gobName) || isBush(gobName))) {
            //System.out.printf(" TREE/BUSH \n");
            Message data = getDrawableData(gob);
            if(data != null && !data.eom()) {
                data.skip(1);
                int growth = data.eom() ? -1 : data.uint8();
                if(growth < 100 && growth >= 0) {
                    return MIXCOLOR_LIGHTBLUE;
                }
            }
        }

        //if it's a crop
        else if(highlightCropsReady && isCrop(gobName)) {
            //System.out.printf(" CROP \n");
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
                    return MIXCOLOR_LIGHTBLUE;
            }
        }
        //else System.out.printf(" NOPE \n");

        return null;
    }

    public static MixColor getHighlightColor(Gob gob) {
        if(gob==null || gob.getres()==null)
            return null;

        //get Type and name
        String gobName, categ;
        Color c;

        try{
            gobName = gob.getres().name;
        }catch (Resource.Loading loading){
            loading.printStackTrace();
            return null;
        }

        //System.out.printf("gobHighlight %s ", gobName);

        //if highlight aggressive gob
        if(ZeeConfig.highlightAggressiveGobs && mapCategoryGobs.get(CATEG_AGROCREATURES).contains(gobName)) {
            //System.out.printf(" AGRRO \n");
            c = mapCategoryColor.get(CATEG_AGROCREATURES);
            if(c==null) {
                //set default categ color if empty
                c = ZeeConfig.MIXCOLOR_MAGENTA.color();
                ZeeConfig.mapCategoryColor.put(ZeeConfig.CATEG_AGROCREATURES, c);
            }
            return new MixColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        }

        //if it's a custom gob setting, prioritize
        else if(ZeeConfig.mapGobColor.size() > 0   &&   (c = ZeeConfig.mapGobColor.get(gobName)) != null){
            //System.out.printf("  SAVEDGOB \n");
            return new MixColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        }

        //finally check if gob has category
        else if((categ = ZeeConfig.mapGobCategory.get(gobName)) != null){
            //System.out.printf(" CATEGOB ");
            c = mapCategoryColor.get(categ);
            if(c==null) {
                //System.out.printf(" null \n");
                //set default categ color if empty
                c = ZeeConfig.MIXCOLOR_YELLOW.color();
                ZeeConfig.mapCategoryColor.put(categ, c);
            }
            //else System.out.printf(" color=%s \n",c.toString());
            return new MixColor(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
        }
        //else System.out.printf(" NOPE ");

        //System.out.printf("\n");

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
        return gobName.contains("/trees/");
    }

    public static boolean isBush(String gobName) {
        return gobName.contains("/bushes/");
    }

    public static boolean isCrop(String gobName) {
        return gobName.startsWith("gfx/terobjs/plants/") && !gobName.endsWith("trellis");
    }

    public static boolean isBug(String name){
        /*
        private static final String[] CRITTERS = {
            "/rat/rat", "/swan", "/squirrel", "/silkmoth", "/frog", "/rockdove", "/quail", "/toad", "/grasshopper",
            "/ladybug", "/forestsnail", "/dragonfly", "/forestlizard", "/waterstrider", "/firefly", "/sandflea",
            "/rabbit", "/crab", "/cavemoth", "/hedgehog", "/stagbeetle", "jellyfish", "/mallard", "/chicken", "/irrbloss",
            "/cavecentipede", "/bogturtle", "/moonmoth", "/monarchbutterfly", "/items/grub", "/springbumblebee"
        };
         */
        String[] list = {
            "/silkmoth","/grasshopper","/ladybug","/dragonfly","/waterstrider","/firefly","/sandflea",
            "/cavemoth","/stagbeetle","/cavecentipede","/moonmoth","/monarchbutterfly","/items/grub",
            "/springbumblebee"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isString(String name){
        String[] list = {
            "nettle","taproot","cattail"
        };
        for (int i = 0; i < list.length; i++) {
            if(name.contains(list[i]))
                return true;
        }
        return false;
    }

    public static boolean isBird(String name){
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
        String[] list = {
            "bolete","truffle","trumpet","cavelantern","chanterelles","morel","fairy","blewit",
            "puffball","indigo","parasol","snowtop","yellowfeet"
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
            "trees/elm","trees/juniper","trees/beech","trees/mulberry"
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

    public static void highlight(Gob gob, MixColor mc) {
        if(gob==null || mc==null)
            return;
        try {
            gob.setattr(new ZeeGobHighlight(gob, mc));
        }catch (Resource.Loading e){
            e.printStackTrace();
        }
    }

    public static void gobAudio(Gob gob) {
        String gobName = gob.getres().name;
        long gobId = gob.id;
        if(gobName==null || gobName.isEmpty())
            return;
        String path = "";
        Integer gobType ;

        //if gob is new, add to session gobs
        if(mapGobSession.put(gobName,"") == null) {
            //System.out.println(gobName+"  "+mapGobSession.size());
        }

        if(isPlayer(gob)  &&  gobId != gameUI.map.player().id) {
            if(autoHearthOnStranger)
                gameUI.act("travel","hearth");
            if(alertOnPlayers){
                String audio = mapCategoryAudio.get(CATEG_PVPANDSIEGE);
                if(audio!=null && !audio.isEmpty())
                    playAudio(audio);
                else
                    gameUI.error("player spotted");
            }
        }else if( (path = mapGobAudio.get(gobName)) != null){
            //if single gob alert is saved, play alert
            ZeeConfig.playAudio(path);
        }else {
            //for each category in mapCategoryGobs...
            for (String categ: mapCategoryGobs.keySet()){
                if(categ==null || categ.isEmpty())
                    continue;
                //...check if gob is in category
                if(mapCategoryGobs.get(categ).contains(gobName)){
                    //play audio for category
                    path = mapCategoryAudio.get(categ);
                    ZeeConfig.playAudio(path);
                }
            }
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
    private static Coord mapWndLastPos;
    public static void windowMapCompact(MapWnd mapWnd, boolean compact) {
        if(compact && mapWnd.c.equals(0,0))//special startup condition?
            return;
        Coord screenSize = gameUI.map.sz;
        Coord pos = mapWnd.c;
        Coord size = mapWnd.sz;
        if(!compact){
            //make expanded window fit horizontally
            if(pos.x + size.x > screenSize.x){
                mapWndLastPos = new Coord(mapWnd.c);
                mapWnd.c = new Coord(screenSize.x - size.x, pos.y);
            }
        }else{
            //move compact window back to original pos
            if(mapWndLastPos!=null) {
                mapWnd.c = mapWndLastPos;
                mapWndLastPos = null;
            }
        }
    }


    //FIXME some cases the behavior is wrong
    public static void checkAutoOpenEquips(boolean done) {
        if(!ZeeConfig.autoOpenEquips)
            return;

        if(!windowEquipment.visible) {
            //from Equipory.drawslots()
            if((gameUI != null) && (gameUI.vhand != null)) {
                try {
                    Equipory.SlotInfo si = ItemInfo.find(Equipory.SlotInfo.class, gameUI.vhand.item.info());
                    if(si != null)
                        windowEquipment.show();
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
        Widget invSlots = windowInvMain.getchild(Inventory.class);
        invSlots.c = new Coord(0,30);

        windowInvMain.pack();
    }

    public static void windowAdded(Window window) {
        String windowTitle = window.cap.text;
        if(windowTitle.contains("Equipment")) {
            windowEquipment = window;
        }else if(windowTitle.contains("Inventory")) {
            windowInvMain = window;
        }

        //reposition window if saved
        Coord c;
        if(rememberWindowsPos && !(window instanceof MapWnd) ){
            if(isMakewindow(window)){
                windowTitle = MAKE_WINDOW_NAME;
            }
            if((c = mapWindowPos.get(windowTitle)) != null) {
                window.c = c;
            }
        }
    }

    public static int addZeecowOptions(OptWnd.Panel main, int y) {

        y += 17;

        main.add(new Button(200,"Zeecow options"){
            @Override
            public void click() {
                if(zeecowOptions == null)
                    zeecowOptions = new ZeecowOptionsWindow();
                else
                    zeecowOptions.toFront();
                main.getparent(OptWnd.class).hide();
            }
        }, 0, y);

        y += 17;

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
    public static HashMap<String, String> initMapGobAudio() {
        String s = Utils.getpref(MAP_GOB_AUDIO,"");
        if (s.isEmpty())
            return new HashMap<String,String> ();
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

    public static List<String> initCraftHistory() {
        String s = Utils.getpref("craftHistoryList", "");
        if (s.isEmpty()){
            craftHistoryPos = -1;
            return new ArrayList<String>();
        }else{
            ArrayList<String> arr = new ArrayList<String>();
            String arrs[] = s.split(",");
            for (int i = 0; i < arrs.length; i++) {
                arr.add(arrs[i]);
            }
            craftHistoryPos = arr.size();
            return arr;
        }
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

    public static void windowChangedPos(Window window) {
        if(!ZeeConfig.rememberWindowsPos || window==null)
            return;
        String name = ((Window)window).cap.text;
        if(name==null || name.isEmpty() || window instanceof MapWnd)
            return;
        if(isMakewindow(window))
            name = MAKE_WINDOW_NAME;
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


    public static boolean initTrackingDone = false;
    //public static boolean initSiegeDone = false;
    public static void initToggles() {
        new Thread(new Runnable() {
            public void run() {
                try {

                    if(autoRunLogin)
                        gameUI.ulpanel.getchild(Speedget.class).set(2);

                    if (autoOpenBelt)
                        openBelt();

                    Thread.sleep(1500);

                    if(!initTrackingDone) {
                        gameUI.menu.wdgmsg("act", "tracking");
                        initTrackingDone = true;
                    }
                    /*Thread.sleep(1500);
                    if(!initSiegeDone) {
                        gameUI.menu.wdgmsg("act", "siegeptr");//unnecessary?
                        initSiegeDone = true;
                    }*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void checkClassMod(String name, Class<?> qlass){
        try {

            if(name.equals("haven.res.ui.tt.q.quality.Quality")) {

                /*
                    Set Quality toggle on
                 */
                qlass.getDeclaredField("show").setBoolean(qlass, true);

            }else if(name.equals("haven.res.gfx.fx.bprad.BPRad")){

                /*
                    Change radius color
                 */
                setFinalStatic(
                    qlass.getDeclaredField("smat"),
                    new BaseColor(new Color(139, 139, 185, 48))
                );
                setFinalStatic(
                    qlass.getDeclaredField("emat"),
                    Pipe.Op.compose(new Pipe.Op[]{new BaseColor(new Color(139, 139, 185, 48)), new States.LineWidth(1)})
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


    private static double lasterrsfx = 0;
    public static void playAudio(String filePath) {
        double now = Utils.rtime();
        if(now - lasterrsfx > 0.1) {
            new ZeeSynth(filePath).start();
            lasterrsfx = now;
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

    public static HashMap<String,Integer> mapInvItemNameCount = new HashMap<String,Integer>();
    public static void addInvItem(GItem i) {
        try {
            String itemName = i.getres().name;
            Integer count = countInvItems(itemName);
            count++;
            mapInvItemNameCount.put(itemName, count);
            invMainoptionsWdg.updateLabelCount(itemName,count);
        }catch (Resource.Loading e){
        }
    }
    public static void removeInvItem(GItem i) {
        try{
            String itemName = i.getres().name;
            Integer count = countInvItems(itemName);
            count--;
            if(count < 0)
                count = 0;
            mapInvItemNameCount.put(itemName, count);
            invMainoptionsWdg.updateLabelCount(itemName,count);
        }catch (Resource.Loading e){
        }
    }
    private static Integer countInvItems(String itemName){
        Integer count = mapInvItemNameCount.get(itemName);
        if(count==null)
            count = 0;
        return count;
    }

    public static void searchNextInputMakeWnd(String inputName) {
        MenuSearch searchWindow = gameUI.toggleSearchWindow();
        searchWindow.sbox.settext(inputName);
    }

    public static String getItemInfoName(List<ItemInfo> info) {
        try {
            for (ItemInfo v : info) {
                if (v.getClass().getSimpleName().equals("Name")) {
                    return ((Text)v.getClass().getField("str").get(v)).text;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return(null);
    }

    public static boolean matchKeyShortcut(KeyEvent ev) {
        // Shift+Tab toggles Belt
        if(ZeeConfig.keyBeltShiftTab && ev.getKeyCode()==KeyEvent.VK_TAB && ev.isShiftDown()){
            Window belt = getWindow("Belt");
            if( belt != null){
                belt.getchild(Button.class).click();//click close button
            }else {
                openBelt();
            }
        }
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
        }
        return false;
    }

    public static Window getWindow(String name) {
        Set<Window> windows = gameUI.children(Window.class);
        for(Window w : windows) {
            if(w.cap.text.equalsIgnoreCase(name)){
                return w;
            }
        }
        return null;
    }

    //screen center coord
    public static Coord getCenterScreenCoord() {
        return ZeeConfig.gameUI.map.sz.div(2);
    }

    // compile multi-line messages into single-line
    public static void checkUiMsgText(String text) {
        now = System.currentTimeMillis();
        if(now - lastUiMessageMs > 555) { //new message
            lastUiMessageMs = now;
            uiMsgTextQuality = "";
            uiMsgTextBuffer = "";
        }
        if (text.contains("Quality")) {
            uiMsgTextQuality = text;
        }else if(uiMsgTextQuality!=null && !uiMsgTextQuality.isEmpty() && !text.contains("Memories")){
            uiMsgTextBuffer += ", " + text;
            gameUI.msg(uiMsgTextQuality + uiMsgTextBuffer);
        }
    }

    // set flags for clickWItem and ZeeClickGobManager.gobClick
    public static void scheduleClickPetal(String name) {
        ZeeConfig.clickPetal = true;
        ZeeConfig.clickPetalName = name;
    }
    public static void resetClickPetal() {
        ZeeConfig.clickPetal = false;
        ZeeConfig.clickPetalName = "";
    }

    public static String getCursorName() {
        try {
            return gameUI.map.ui.getcurs(Coord.z).name;
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * action names: dig, mine, carry, destroy, fish, inspect, repair,
     *      crime, swim, tracking, aggro, shoot
     */
    public static final String ACT_DIG = "dig", ACT_MINE = "mine", ACT_CARRY = "carry",
            ACT_DESTROY = "destroy", ACT_FISH = "fish", ACT_INSPECT = "inspect",
            ACT_REPAIR = "repair", ACT_CRIME = "crime", ACT_SWIM = "swim",
            ACT_TRACKING = "tracking", ACT_AGGRO = "aggro", ACT_SHOOT = "shoot";
    public static void cursorChange(String name) {
        gameUI.menu.wdgmsg("act", name);
    }

    public static void checkCharSelection(String msg) {
        if(msg.equalsIgnoreCase("play")){
            ZeeConfig.resetCharSelected();
        }
    }

    //reset state
    public static void resetCharSelected() {
        resetBeltState();
    }

    private static void resetBeltState() {
        ZeeClickItemManager.invBelt = null;
    }

    public static void openBelt() {
        windowEquipment.getchild(Equipory.class).children(WItem.class).forEach(witem -> {
            if (witem.item.res.get().name.endsWith("belt")) {
                witem.mousedown(Coord.z, 3);
                resetBeltState();
            }
        });
    }

    public static void craftHistoryAdd(String msg, Object[] args) {
        // haven.MenuGrid@c046fa2, act, [craft, snowball, 0]
        if( isCraftHistoryNavigation || !msg.contentEquals("act") || !args[0].toString().contentEquals("craft"))
            return;

        String name = (String) args[1];
        if(craftHistoryList.contains(name)) { // name already on history
            craftHistoryList.remove(name);
        }
        craftHistoryList.add(name);

        if(craftHistoryList.size() > 7) { // max history size
            craftHistoryList.remove(0);
        }

        craftHistoryPos = craftHistoryList.size() - 1;
    }

    public static void craftHistoryDelItem() {
        if (craftHistoryList.size() == 0)
            return;
        String name = craftHistoryList.remove(craftHistoryPos);
        if(craftHistoryPos >= craftHistoryList.size()-1) {
            craftHistoryPos = craftHistoryList.size() - 1;
        }
        if(craftHistoryPos > -1) {
            isCraftHistoryNavigation = true;
            gameUI.menu.wdgmsg("act", "craft", craftHistoryList.get(craftHistoryPos), "0");
        }else{
            makeWindow.wdgmsg("close");
        }
    }

    public static void craftHistoryGoBack() {
        if(craftHistoryPos <= 0)
            return;
        craftHistoryPos--;
        //craftHistoryUpdtBtns();
        isCraftHistoryNavigation = true;
        gameUI.menu.wdgmsg("act", "craft", craftHistoryList.get(craftHistoryPos), "0");
    }

    public static void craftHistoryGoFwd() {
        if(craftHistoryPos == craftHistoryList.size()-1)
            return;
        craftHistoryPos++;
        //craftHistoryUpdtBtns();
        isCraftHistoryNavigation = true;
        gameUI.menu.wdgmsg("act", "craft", craftHistoryList.get(craftHistoryPos), "0");
    }

    public static void craftHistoryUpdtBtns() {
        btnMkWndBack.change("<" + (craftHistoryPos==0 ? "" : craftHistoryPos) );
        btnMkWndFwd.change(">" + (craftHistoryPos==craftHistoryList.size()-1 ? "" : (craftHistoryList.size()-1-craftHistoryPos)) );
    }

    public static void craftHistorySave() {
        Utils.setpref("craftHistoryList", String.join(",", craftHistoryList));
        craftHistoryPos = craftHistoryList.size() - 1;
    }


    public static void println(String s) {
        System.out.println(s);
    }
}



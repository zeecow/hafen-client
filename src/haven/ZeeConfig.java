package haven;

import com.google.gson.Gson;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;



public class ZeeConfig {
    public static final String CTGNAME_PVP = "PVP and siege";
    public static final String CTGNAME_AGROCREATURES = "Agressive creatures";
    public static final String CTGNAME_RAREFORAGE = "Rare forageables";
    public static final String CTGNAME_LOCRES = "Localized resources";
    public static final String MAP_GOB_SAVED = "mapGobSaved";
    public static final String MAP_CATEGORY_AUDIO = "mapCategoryAudio";
    public static final String MAP_CATEGORY_GOBS = "mapCategoryGobs";
    public static GameUI gameUI;
    public static Window windowBelt;
    public static Window windowCattleRoster;
    public static Window windowEquipment;
    public static Window windowIconSettings;
    public static Window windowInventory;
    public static Window windowOptions;

    public static Gson gson = new Gson();

    public static ZeecowOptionsWindow zeecowOptions;

    public static boolean actionSearchGlobal = Utils.getprefb("actionSearchGlobal", true);
    public static boolean alertOnPlayers = Utils.getprefb("alertOnPlayers", true);
    public static boolean autoClickMenuOption = Utils.getprefb("autoClickMenuOption", true);
    public static String autoClickMenuOptionList = Utils.getpref("autoClickMenuOptionList", "Pick,Pluck,Flay,Slice,Harvest wax");
    public static boolean autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);
    public static boolean autoOpenEquips = Utils.getprefb("beltToggleEquips", true);
    public static boolean cattleRosterHeight = Utils.getprefb("cattleRosterHeight", false);
    public static double cattleRosterHeightPercentage = Utils.getprefd("cattleRosterHeightPercentage", 1.0);
    public static boolean dropMinedCurios = Utils.getprefb("dropMinedCurios", true);
    public static boolean dropMinedOre = Utils.getprefb("dropMinedOre", true);
    public static boolean dropMinedSilverGold = Utils.getprefb("dropMinedOrePrecious", true);
    public static boolean dropMinedStones = Utils.getprefb("dropMinedStones", true);
    public static boolean dropSeeds = false;//always starts off (TODO: set false when character loads)
    public static boolean dropSoil = false;
    public static boolean equiporyCompact = Utils.getprefb("equiporyCompact", false);
    public static boolean notifyBuddyOnline = Utils.getprefb("notifyBuddyOnline", false);;
    public static boolean showInventoryLogin = Utils.getprefb("showInventoryLogin", true);
    public static boolean showEquipsLogin = Utils.getprefb("showEquipsLogin", false);

    public static String playingAudio = null;


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
    public final static Set<String> noIconGobs = new HashSet<String>(Arrays.asList(
            "gfx/kritter/irrbloss/irrbloss" //irrlight
    ));


    public static HashMap<String,String> mapGobSession = new HashMap<String,String>();
    public static HashMap<String,String> mapGobAudio = initMapGobSaved();
    public static HashMap<String, Set<String>> mapCategoryGobs = initMapCategoryGobs();
    public static HashMap<String,String> mapCategoryAudio = initMapCategoryAudio();


    public static Collection<Field> getAllFields(Class<?> type) {
        TreeSet<Field> fields = new TreeSet<Field>(
                new Comparator<Field>() {
                    @Override
                    public int compare(Field o1, Field o2) {
                        int res = o1.getName().compareTo(o2.getName());
                        if (0 != res) {
                            return res;
                        }
                        res = o1.getDeclaringClass().getSimpleName().compareTo(o2.getDeclaringClass().getSimpleName());
                        if (0 != res) {
                            return res;
                        }
                        res = o1.getDeclaringClass().getName().compareTo(o2.getDeclaringClass().getName());
                        return res;
                    }
                });
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    public static void printAllFields(Object obj) {
        for (Field field : getAllFields(obj.getClass())) {
            field.setAccessible(true);
            if (field.getDeclaringClass().isPrimitive())
                continue;
            String name = field.getName();
            Object value = null;
            try {
                value = field.get(obj);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            System.out.printf("%s %s.%s = %s;\n", value==null?" ":"*", field.getDeclaringClass().getSimpleName(), name, value);
        }
    }


    public static void printObj(Object ob) {
        System.out.println(ReflectionToStringBuilder.toString(ob, new ZeeMyRecursiveToStringStyle(1)));
    }

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

    public static void checkGob(Gob gob) {
        if(gob==null || gob.getres()==null)
            return;

        String name = gob.getres().name;
        String path = "";

        //if gob is new, add to session gobs
        if(mapGobSession.put(name,"") == null) {
            //System.out.println(name+"  "+mapGobAlert.size());
        }

        if(name.contains("borka/body") && gob.id != gameUI.map.player().id) {
            if(autoHearthOnStranger)
                gameUI.act("travel","hearth");
            if(alertOnPlayers){
                String audio = mapCategoryAudio.get(CTGNAME_PVP);
                if(audio!=null && !audio.isEmpty())
                    playAudio(audio);
                else
                    gameUI.error("player spotted");
            }
        }else if( (path = mapGobAudio.get(name)) != null){
            //if single gob alert is saved, play alert
            ZeeConfig.playAudio(path);
        }else {
            //for each category in mapCategoryGobs...
            for (String categ: mapCategoryGobs.keySet()){
                if(categ==null || categ.isEmpty())
                    continue;
                //...check if gob is in category
                if(mapCategoryGobs.get(categ).contains(name)){
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
    public static void checkMapCompact(MapWnd mapWnd, boolean compact) {
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
            Equipory.SlotInfo si = ItemInfo.find(Equipory.SlotInfo.class, gameUI.vhand.item.info());
            if (si != null) {
                windowEquipment.show();
            }
        }else if(done){
            windowEquipment.hide();
        }
    }

    public static void initWindowInventory() {
        //add options interface
        windowInventory.add(new ZeeInventoryOptions("Inventory"));

        //change slots position
        Widget invSlots = windowInventory.getchild(Inventory.class);
        invSlots.c = new Coord(0,20);

        windowInventory.pack();
    }

    public static void getWindow(Window window, String cap) {
        cap = cap.trim();
        if(cap.contains("Belt")) {
            windowBelt = window;
        }else if(cap.equalsIgnoreCase("Cattle Roster")) {
            windowCattleRoster = window;
        }else if(cap.contains("Equipment")) {
            windowEquipment = window;
        }else if(cap.contains("Icon settings")) {
            windowIconSettings = window;
        }else if(cap.contains("Inventory")) {
            windowInventory = window;
        }else if(cap.contains("Options")) {
            windowOptions = window;
        }
    }

    public static int addZeecowOptions(OptWnd.Panel main, int y) {

        y += 7;

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

        y += 37;

        return y;
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Set<String>> initMapCategoryGobs() {
        HashMap<String, Set<String>> ret;
        String s = Utils.getpref("mapCategoryGobs","");
        if(s.isEmpty()) {
            ret = new HashMap<>();
            ret.put(CTGNAME_LOCRES, localizedResources);
            ret.put(CTGNAME_RAREFORAGE, rareForageables);
            ret.put(CTGNAME_AGROCREATURES, aggressiveGobs);
            ret.put(CTGNAME_PVP, pvpGobs);
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
    public static HashMap<String, String> initMapGobSaved() {
        String s = Utils.getpref(MAP_GOB_SAVED,"");
        if (s.isEmpty())
            return new HashMap<String,String> ();
        else
            return (HashMap<String, String>) deserialize(s);
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

/*
    public static String decodeURL(String s){
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    public static String encodeURL(String s){
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

 */

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

}



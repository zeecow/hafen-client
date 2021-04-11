package haven;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;



public class ZeeConfig {
    public static final String CTGNAME_PVP = "PVP and siege";
    public static final String CTGNAME_AGROCREATURES = "Agressive creatures";
    public static final String CTGNAME_RAREFORAGE = "Rare forageables";
    public static final String CTGNAME_LOCRES = "Localized resources";
    public static GameUI gameUI;
    public static Window windowBelt;
    public static Window windowCattleRoster;
    public static Window windowEquipment;
    public static Window windowIconSettings;
    public static Window windowInventory;
    public static Window windowOptions;

    public static ZeecowOptionsWindow zeecowOptions;

    public static boolean actionSearchGlobal = Utils.getprefb("actionSearchGlobal", true);
    public static boolean autoClickMenuOption = Utils.getprefb("autoClickMenuOption", true);
    public static String autoClickMenuOptionList = Utils.getpref("autoClickMenuOptionList", "Pick,Pluck,Flay,Slice,Harvest wax");
    public static boolean autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);
    public static boolean beltToggleEquips = Utils.getprefb("beltToggleEquips", true);
    public static boolean cattleRosterHeight = Utils.getprefb("cattleRosterHeight", false);
    public static double cattleRosterHeightPercentage = Utils.getprefd("cattleRosterHeightPercentage", 1.0);
    public static boolean dropMinedCurios = Utils.getprefb("dropMinedCurios", true);
    public static boolean dropMinedOre = Utils.getprefb("dropMinedOre", true);
    public static boolean dropMinedSilverGold = Utils.getprefb("dropMinedOrePrecious", true);
    public static boolean dropMinedStones = Utils.getprefb("dropMinedStones", true);
    public static boolean dropSeeds = false;//always starts off (TODO: set false when character loads)
    public static boolean dropSoil = false;
    public static boolean equiporyCompact = Utils.getprefb("equiporyCompact", false);
    public static boolean equipWindowOpenedByBelt = false;
    public static boolean notifyBuddyOnline = Utils.getprefb("notifyBuddyOnline", false);;
    public static boolean showInventoryLogin = Utils.getprefb("showInventoryLogin", true);
    public static boolean showEquipsLogin = Utils.getprefb("showInventoryLogin", true);

    public static String playingAudio = null;


    public final static Set<String> mineablesStone = new HashSet<String>(Arrays.asList(
            "gneiss",
            "basalt",
            "cinnabar",
            "dolomite",
            "feldspar",
            "flint",
            "granite",
            "hornblende",
            "limestone",
            "marble",
            "porphyry",
            "quartz",
            "sandstone",
            "schist",
            "blackcoal",
            "zincspar",
            "apatite",
            "fluorospar",
            "gabbro",
            "corund",
            "kyanite",
            "mica",
            "microlite",
            "orthoclase",
            "soapstone",
            "sodalite",
            "olivine",
            "alabaster",
            "breccia",
            "diabase",
            "arkose",
            "diorite",
            "kyanite",
            "slate"
    ));
    public final static Set<String> mineablesOre = new HashSet<String>(Arrays.asList(
            "cassiterite",
            "chalcopyrite",
            "malachite",
            "ilmenite",
            "limonite",
            "hematite",
            "magnetite"
    ));
    public final static Set<String> mineablesOrePrecious = new HashSet<String>(Arrays.asList(
            "galena",
            "argentite",
            "hornsilver",
            "petzite",
            "sylvanite",
            "nagyagite"
    ));
    public final static Set<String> mineablesCurios = new HashSet<String>(Arrays.asList(
            "catgold",
            "petrifiedshell",
            "strangecrystal"
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


    public static String mapGobSavedString;
    public static String mapCategoryGobsString;
    public static String mapCategoryAudioString;
    public static HashMap<String,String> mapGobSession = new HashMap<String,String>();
    public static HashMap<String,String> mapGobSaved = initMapGobSaved();
    public static HashMap<String,String> mapCategoryGobs = initMapCategoryGobs();
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


    public static void printObj(Widget wdg) {
        System.out.println(ReflectionToStringBuilder.toString(wdg, new ZeeMyRecursiveToStringStyle(1)));
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

        if(ZeeConfig.autoHearthOnStranger && name.contains("borka/body") && gob.id != gameUI.map.player().id) {
            gameUI.act("travel","hearth");
            try{
                playAudio(mapCategoryAudio.get(CTGNAME_PVP));
            }catch(Exception e) {
                playMidi(midiJawsTheme);
            }
        }else if( (path = mapGobSaved.get(name)) != null){
            //if single gob alert is saved, play alert
            ZeeConfig.playAudio(ZeeConfig.decodeURL(path));
        }else {
            //for each category in mapCategoryGobs...
            for (String categ: mapCategoryGobs.keySet()){
                if(categ==null || categ.isEmpty())
                    continue;
                //...check if gob is in category
                if(mapCategoryGobs.get(categ).contains(name)){
                    //play audio for category
                    path = mapCategoryAudio.get(categ);
                    ZeeConfig.playAudio(ZeeConfig.decodeURL(path));
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
    public static void checkBeltToggleWindow(Widget wdg) {
        if(!ZeeConfig.beltToggleEquips)
            return;
        if(wdg.parent!=null && wdg.parent.parent!=null && wdg.parent.parent instanceof Window){
            String windowName = "";
            try {
                windowName = ((Window) wdg.parent.parent).cap.text;
            }catch (Exception e){
            }
            if(!equipWindowOpenedByBelt && windowName.contains("Belt")){
                windowEquipment.show();
                if(windowEquipment.visible)
                    equipWindowOpenedByBelt = false;
                else
                    equipWindowOpenedByBelt = false;
            }
        }else if(equipWindowOpenedByBelt && wdg.parent!=null && wdg.parent instanceof GameUI){
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
            }
        }, 0, y);

        y += 37;

        return y;
    }

    public static HashMap<String, String> initMapCategoryGobs() {
        HashMap<String,String> mapCateGob = new HashMap<String,String>();
        mapCategoryGobsString = Utils.getpref("mapCategoryGobsString","");
        if(mapCategoryGobsString.isEmpty()) {
            System.out.println("initMapCategoryGobs() > setting default categories");
            mapCateGob.put(CTGNAME_LOCRES, localizedResources.toString());
            mapCateGob.put(CTGNAME_RAREFORAGE, rareForageables.toString());
            mapCateGob.put(CTGNAME_AGROCREATURES, aggressiveGobs.toString());
            mapCateGob.put(CTGNAME_PVP, pvpGobs.toString());
            mapCategoryGobsString = mapCateGob.toString();
        }else{
            System.out.println("initMapCategoryGobs() > fetching saved categories");
            //fill map object and return it
            String[] mapStr = mapCategoryGobsString.split("],?");
            String[] categArr;
            System.out.println("> len="+mapStr.length+"  "+mapCategoryGobsString);
            for (String s: mapStr) {
                if(s.trim().isEmpty()) {
                    System.out.println("skipping \""+s+"\"");
                    continue;
                }
                //"category=gob1,gob2"
                s = s.replaceAll("[{}\\[\\]]+","");
                categArr = s.split("=");
                if(categArr.length<2) {
                    System.out.println("skipping \"" + s + "\"");
                    continue;
                }
                mapCateGob.put(categArr[0].trim(),categArr[1].trim());
            }
        }
        return mapCateGob;
    }


    public static HashMap<String, String> initMapCategoryAudio() {
        mapCategoryAudioString = Utils.getpref("mapCategoryAudioString","");
        System.out.println("initMapCategoryAudio()");
        HashMap<String,String> ret = new HashMap<String,String>();
        String[] mapStr = mapCategoryAudioString.split(",");
        String[] categInfo;
        try {
            for (String s : mapStr) {
                if(s.isBlank())
                    continue;
                categInfo = s.split("=");
                ret.put(categInfo[0].trim(), categInfo.length>1 ? categInfo[1].trim() : "");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    public static HashMap<String, String> initMapGobSaved() {
        mapGobSavedString = Utils.getpref("mapGobSavedString","");
        System.out.println("initMapGobSaved()");
        HashMap<String,String> ret = new HashMap<String,String>();
        String[] mapStr = mapGobSavedString.split(",");
        String[] gobInfo;
        try {
            for (String s : mapStr) {
                if(s.isBlank())
                    continue;
                gobInfo = s.split("=");
                ret.put(gobInfo[0], gobInfo.length>1 ? gobInfo[1].trim() : "");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
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

    public static void playAudio(String filePath) {
        if(playingAudio!=null && playingAudio.contains(filePath))
            return;//avoid duplicate audio
        playingAudio = filePath;
        new ZeeSynth(filePath).start();
    }


    public static String decodeURL(String s){
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    public static String encodeURL(String s){
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
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

    private static Object deserialize(String s){
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

    // https://base64.guru/converter/encode/image
    // output format: plain text
    public static String imgB64Player = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAYFBMVEVVgKZwnsUeMVNUgKWid0pQeaEoPWazlXMtR3FEaJPy6N4jNlzv4tZSfKNnlbtNdZ3Jm3UnPGXOpYONtdg2U4AuR3Jod3/DlGg4VoOxdkWJYTLp18aq0PJ+rNX///////8LjhFyAAAAIHRSTlP/////////////////////////////////////////AFxcG+0AAAABYktHRB5yCiArAAAAQGhJU1QAAwADAAMAAgACAAMAAgACAAEAAQABAAEAAQABAAEAAQABAAIAAQABAAEAAQABAAgACQAKAAwADgARABIAKABc1/X1vAAAABt0RVh0U29mdHdhcmUAZ2lmMnBuZyAwLjYgKGJldGEpqt1pkgAAAH5JREFUeJxtz9sSgyAMRVG0Wntv7Y2EhOP//6UBFPvQ/UTWTIaJm3KwysuVOVjYAOHBzEVcnp/MqxRgsX6guwSRIF0FfxpEBvEVvj6teFoATslbpElcmmMkK770gBVStyaOGXbaJ2p0H9u/MOHaa+7cYvkW9yO9PyOwXYt6/wxZlRoprCU+cgAAAABJRU5ErkJggg==";

    public static  String imgB64Star = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAB90lEQVR42mNkwAPifVgtWZgZGeZu/HUclxpGfAacXMSzipmZgcEk+ksYyQa4mLEorO3lvg1ih5d9Vd1x/M8DkgxY38vV52TOWghiHzrzp9+34GsR0QZoKTLxHVvE85iRkZEPxP////8n28Qvspfv/PtElAGzajiLwj3YepHF1uz6VZzc9L0Pw4C2KM5wFX1Gc2lxJjkZcSZZAR5GWaC4ONBoJhSV/xn+AcmXH778f/zk1b/HT1/8e3T34v+TjJ76rGpz87g3cSv/V2dgYSAO/GFg+HaP8Wbq5K9+YC9oSzMLrCzmWSWrz+DKwE5A808GhqeXGHaH930Ju/z47wd4GIjwMrIsy+eZYO7BlI1P/+md/6ZGTfxS8OrT/z8YgWhvxCy7aRLPI3wGBBZ+ldt3+s9jrLFQEsseVpvOsRKfAW1zf4R3zv+5CqsBq7q4+t2tWAvAnN8MDH+fM74Ehj4Ds9R/cQZWiJo9J39PCC7+VojVgPtbeU8I8TGZM7xj/Hf+zL+5eQu+lv8DGjApnqvT2Iw5mUHoP9PHL/9Pynl+ssAwQFWOiePMXN6PX+4z3m5a8T195r6fR5ENT3Vgt66P4pzJq/hf1SLtM//1+/9+oBgQacVm4K3P5lW6/Gv38w//f2PzvwQ/I2tnJFfp7qu/ty05/OsCSAwA/W24+ow4DiYAAAAASUVORK5CYII=";

    public static  String getImgB64Cave = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAEnRFWHRUaXRsZQBDYXZlIFBhc3NhZ2WNnq5LAAAAK3RFWHRDcmVhdGlvbiBUaW1lAG9uIDI2IGF1ZyAyMDE1IDE0OjE2OjIwICswMTAwVwLkTwAAAAd0SU1FB+QKEBYsD+qNFfcAAAAJcEhZcwAAHsEAAB7BAcNpVFMAAAAEZ0FNQQAAsY8L/GEFAAAI1UlEQVR42sVXW2wcVxn+zszszOx9116vvXF8S3ASu62TJiH3klSkoARFRWoiUIvUCqUvoAohHnjoQ4VUEA+9BAmBkJBCCRIgaPuQijQtVBWBXJ2Lm/iSNL4kttfrXa+9uzM7s3Pnn8SkSe2kLiBxpE879p49//ffvv8M8H9ebIn7koR1hCbCHkILQScYhIb5zxKhSvgL4Q//SwK+gV8S1hOCmQS8A5uk+MYOnq8Lg89XYIs8E1TLw9lhy37/imuPF51Z2nuI8GuC8t8QyBBeSUXZ3n3rhMhDzR7XnmScCwGWyyDwQDwWwWxFu7054iKT8HBqjLk/OWrauZJbUHT32/TVe/czwD/A+G7f885G7rGffiMc3dQOPhXhmW4x2I4LifOwbu0jSGY6oZeyqNZs1BwORU1AYxTs2Z1hnoHFRvLuHs3w8nRW3+cl8BJhx8v7Q9FH2wUu3twDs5Il311YFO6e7XshhFLIDg+gWikjyBkIBQOwbMAkItMlD6syPNa1icFjfcaX6KwsoZ/g3W2Ee0DoVx3cKYVXpjkuEEmjYVkHOM9FU72MNR31SITpp0YZnCCSUQ+OIKGmGZAcFQnJRlAkEhUGUQ6yDSulOJ33NdwuYn4pBA4QWrd3CoJMB0VZARxVS8fqh8gwj45HtiEalOGVrkKWOciSB1FkUGoedIdHhPa0pCXUJ2PQTWBtm+Tb2UzYsFQCXYeekdOO5yFAMeelJtgTfyWv6PDWrZDkKDXcJDyqheaGJJqSMkzVQVTmEQ5JCASjSGfa0bY8jZhoojHBc/Pef5dgLYVAw9CU63rgDZ7gBdLgA0lwgTBi9a3gxDDsYAZNDQmEZAkNdQk0JCTUxUWkkhFI4TQMRnvUaXSkLNRHgBf2xmU/qp+uAWHRyuTgjRY8b9fDktDWuVYINnbR/xg4KQIWkKgLTCq6CWDF47A0FWF1Ci3LHDgcd4ukyUnIjo/BrM4hFGFojDNoBvNbvm5JXRCT2bcefziy5ut7d/Kp5atZgPKgVTWEYvVEnwdTr0MonwRvFSmELoVcgiAFIIbjcPgQspM5ZPMzEAUPJTNAxqlGBI5a1woOZl1f2I49KAWMHEl4nMiisTrGMw/VwjXMXP8Ahq5SAG3wlWsoiVtQi2+DnVwPW2qBI7eT5w1QNYZCsQTLMKGYjBLO+97DoMw/vSPExULcN7+zJ30n8oumoKJj2fBUFZMTUx6njLDDH5bQO92M321PgrkmzPptCMGBrmQRiFC+uDAsz0K1OotS7gokXodJBWt4EkpFalHXj74LzeLR0xKI0R8pQu5+KegOBMQf1IRUoOb7q5vsXPVRBJiGJ9fSt3oWW351GBdH+7AskENzWENAjkMsn4VZugaHIqRbHEqkiArZ4XgXZeoQ12MEj0TMZcf7NL6sOe/dj8CZDRs2NNTV1bOKzrOnv7wKxz9SoJSmcP7CSYxOjuPmdoZxmgiXEy7G+3XEoxnIngopRrKs5GBKq8GCIYSDDiXUxmzJhkOPfl/VhVzv79fses1wf75YCnpj8VjbwYMHWV9fH3oSWSRlA8XCCC71f4x3Zqu3Nj13M4OhNyu4cFRBeZWFG3/7AD/cvRkN4SCizVtQzmYh81UKeQ227dxOAXnvcJ5HE9RXTnPRIuQ4bs3rr73ONTU1YXh4GEPjJailHE784xLK88ZvlylD9nQV7cuDUEYdjFgKjlw4DzV/HkqxH1YtB9tSYTk2bNelqva9Jw4uYyIncLrpFhcrwouvvfpqsKVluVcqlZlWVTB4PY/nz5cpd/eGyaq6IPlH6rEQ4u9Tzinvm1I25FCSOsXBVLmISpWK1XVIPRkSEQHFsuMHgaLCPNPytAUEyPsvtHe0M8MwMDU1hZmZWfDkaXGOIRgMQtf1OwQ8moY8VY9GI7nbJflN8+QhDV95GWbL48hRzg3qfZu8NngHczSUbPqeJ4Ur6a7P/caCFESjUT0UCkFRVDYyMgJJktC2YhWeeuoADSIOgvBJsKpTLmo04R0y8vaHGp5ZH8NGGr0WJOhEiDEBKvE1aDjVxQKokQZYjt+ORNpm/lyLLiDgeZ5pmpZbLBZRKND0Ixf379+P3U88QVUeo0M/uTzpBWotul7MDdhIt22m/o8gQmWuqnQZ8HiYJECiwOh3AURCPKknm7dB5A3Pt9mzIAWqqgqnT51EjVIwNjaGrq4uOiCCd4/+iWrAvacGPv6j5gsicqdNhMitM7l2NGQqpHwm3JqCuqgLnXJvmC6miKwffuJDaWYYmLD8aWQviIDruufe+O0R5dixd5Ev5LFp06ZbwmFSSOvrU/dEwNZIUOgObBTJ+JkzWBcbREU1yWuePCpDlCz4gdYpBSXF9UXw1jCTJQdXbur+OH5lsTbcNz4+/ouBgQGju6vbbW1tJQ8sUjZG81/0U3Rno597f818ZJFCmmgKlBCMtyOVTgM0LQcmI3j7tIATV0VcvinAL5+waKMubGBOdUqSgN8v1ob+epEMbb0xNrqVnuWhoSESoRmY1j13CGRPGHeeO5uoU8oGWju30o1Ix4/fimC6qFLFC5AEF1mqjY4mG55g4uh5wzNs/ObusxaT4jemp/OrL1261JHJZCRKDXp7e0nDLT9NCzbTfQRdKxiaMyvwwqGLyJddPPf8QWzZto3Sx9G94CZmNNU7fsr0Lt5wfAXc9VkEbq1yudxNmhDqPXfWVatVwbbtRd8h/MtWT7eAt/7J0DdSw0sv/4jk10YuN43RsVFEOR2nLhf0iuH64jND+NlSCAyRtzO5XK5Qqxl0OXP9qvXl079WSXdv3NIZQF6TcX1Cwb79z0LTNPT3D+DKlcvI5wt+S89Nz6kTfmQJT37a0FLfDf21g/A9Qtc8keDGjetTX+0RxWs3BjHODL0x/BUxFApziqIwIuINDg5Okqr6BXeYMLjYoZ+HwN3Lf9H4fltb2/qVnSuaudnTV0/06ywcrkuk02mZZJubnp6u1Gq1I7TvxQcd9J8S+PfaRfgiIT7/6XeVn2e/Wt8k/PmzDvgXck0FMyzypcwAAAAASUVORK5CYII=";

}



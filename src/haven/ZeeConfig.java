package haven;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;



public class ZeeConfig {
    public static GameUI gameUI;
    public static MapView mapView;
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
        "gfx/terobjs/herbs/seashell",
            "gfx/terobjs/herbs/clover"
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
            "gfx/kritter/nidbane/nidbane",
            "gfx/kritter/troll/troll",
            "gfx/kritter/walrus/walrus",
            "gfx/kritter/wildgoat/wildgoat",
            "gfx/kritter/wolf/wolf",
            "gfx/kritter/wolverine/wolverine"
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

        if(ZeeConfig.autoHearthOnStranger && name.contains("borka/body") && gob.id != mapView.player().id) {
            gameUI.act("travel","hearth");
            try{
                playAudio(mapCategoryAudio.get("Danger"));
            }catch(Exception e) {
                playMidi(midiJawsTheme);
            }
        }else if( (path = mapGobSaved.get(name)) != null){
            //if gob alert is saved, play alert
            ZeeConfig.playAudio(path);
        }else if(rareForageables.contains(name)){
            try{
                playAudio(mapCategoryAudio.get("Rare Forageables"));
            }catch(Exception e) {
                playMidi(midiWoodPecker);
            }
        }else if(localizedResources.contains(name)){
            try{
                playAudio(mapCategoryAudio.get("Rare Forageables"));
            }catch(Exception e) {
                playMidi(midiUfoThirdKind);
            }
        }else if(aggressiveGobs.contains(name)){
            try{
                playAudio(mapCategoryAudio.get("Agressive creatures"));
            }catch(Exception e) {
                playMidi(midiJawsTheme);
            }
        }
    }


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
            mapCateGob.put("Localized resources", localizedResources.toString());
            mapCateGob.put("Rare forageables", rareForageables.toString());
            mapCateGob.put("Agressive creatures", aggressiveGobs.toString());
        }else{
            //fill map object and return it
            String[] mapStr = mapCategoryGobsString.split("],?");
            String[] categArr;
            for (String s: mapStr) {
                if(s.trim().isEmpty())
                    continue;
                //"category=gob1,gob2"
                s = s.replaceAll("[{}\\[\\]]+","");
                categArr = s.split("=");
                if(categArr.length<2)
                    continue;
                mapCateGob.put(categArr[0].trim(),categArr[1].trim());
            }
        }
        return mapCateGob;
    }


    public static HashMap<String, String> initMapCategoryAudio() {
        mapCategoryAudioString = Utils.getpref("mapCategoryAudioString","");
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
        HashMap<String,String> ret = new HashMap<String,String>();
        String[] mapStr = mapGobSavedString.split(",");
        String[] gobInfo;
        try {
            for (String s : mapStr) {
                if(s.isBlank())
                    continue;
                gobInfo = s.split("=");
                ret.put(gobInfo[0], gobInfo.length>1 ? gobInfo[1] : "");
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
}



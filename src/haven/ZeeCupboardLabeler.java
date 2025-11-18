package haven;

import haven.res.lib.vmat.AttrMats;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class ZeeCupboardLabeler {

    public static final int CABIN_LEVEL_UNKOWN = -1;
    public static final int CABIN_LEVEL_CELLAR = 0;
    public static final int CABIN_LEVEL_ENTRANCE = 1;
    public static Gob lastCupboardClicked;
    static Window win;
    static String winTitle = "Cupboard labeler";
    static HashMap<String,List<Interior>> mapHouseInteriors = new HashMap<>();
    static String lastHouseId = "";
    static Interior lastInterior = null;
    static int lastCabinLevel = CABIN_LEVEL_UNKOWN;

    static String generateHouseId(Gob house){
        List<String> mats = getHouseMatsBasenames(house);
        if (mats.isEmpty()){
            println("mats empty");
            return"";
        }
        String houseId = house.getres().basename() + "@" + ZeeConfig.getPlayerLocationName()+"+";
        for (int i = CABIN_LEVEL_CELLAR; i < mats.size(); i++) {
            if (i!= CABIN_LEVEL_CELLAR)
                houseId += ",";
            houseId += mats.get(i);
        }
        //println("houseId > "+houseId);
        return houseId;
    }

    private static void destroyWindow() {
        win = ZeeConfig.getWindow(winTitle);
        if (win != null){
            win.reqdestroy();
            win = null;
        }
    }

    static void showWindow() {

        destroyWindow();

        Widget wdg;

        //create window
        win = ZeeConfig.gameUI.add(
                new Window(Coord.of(120,70), winTitle){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("close")){
                            if (lastInterior!=null)
                                lastInterior.removeCupIcons();
                            reset();
                            this.reqdestroy();
                        }
                    }
                },
                ZeeConfig.gameUI.sz.div(2)
        );

        wdg = win.add(new Label(lastHouseId.substring(0,lastHouseId.indexOf(",")) + "..."), CABIN_LEVEL_CELLAR, CABIN_LEVEL_CELLAR);

        Interior inter = getCurrentInterior();
        wdg = win.add(new Label(inter.id+" , cups "+inter.cups.size()), ZeeWindow.posBelow(wdg, CABIN_LEVEL_CELLAR,3));

        wdg = win.add(new Label("mapHouseInteriors: "+mapHouseInteriors.size()), ZeeWindow.posBelow(wdg, CABIN_LEVEL_CELLAR,3));

        wdg = win.add(new Button(100,"delete house"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    deleteHouse();
                }
            }
        },ZeeWindow.posBelow(wdg, CABIN_LEVEL_CELLAR,3));

        win.pack();
    }

    private static void deleteHouse() {
        if (lastHouseId !=null && !lastHouseId.isBlank()) {
            if (lastInterior!=null && isActive){
                lastInterior.removeCupIcons();
            }
            mapHouseInteriors.remove(lastHouseId);
            lastHouseId = "";
            if (win!=null) {
                win.reqdestroy();
                win = null;
            }
        }
    }

    static void reset() {
        //println("reset cupboard lblr");
        isActive = false;
        lastHouseId = "";
        lastInterior = null;
        lastCupboardClicked = null;
        lastCabinLevel = CABIN_LEVEL_UNKOWN;
        if (win!=null)
            win.reqdestroy();
        win = null;
    }

    private static List<String> getHouseMatsBasenames(Gob house) {
        List<String> basenames = new ArrayList<>();
        try {
            AttrMats attr = (AttrMats) ZeeConfig.getGobAttr(house, AttrMats.class);
            if (attr==null){
                println("getHouseMatsBasenames > attr null");
                return basenames;
            }
            if (attr!=null){
                Map<Integer, Material> mats = attr.mats;
                for (Material m : mats.values()) {
                    //println(m.states.toString());
                    //match mats basenames
                    Pattern pattern = Pattern.compile("\\/([a-z\\-]+)\\(");
                    Matcher matcher = pattern.matcher(m.states.toString());
                    matcher.find();
                    String basename = matcher.group(1);
                    if (!basenames.contains(basename))
                        basenames.add(basename);
                }
            }
        } catch (IllegalStateException e){
            println("getHouseMatsBasenames > "+e.getMessage());
            return new ArrayList<>();
        }
        return basenames;
    }

    static void checkHouseClick() {

        //already inside
        if (ZeeConfig.playerLocation==ZeeConfig.LOCATION_CABIN || ZeeConfig.playerLocation==ZeeConfig.LOCATION_CELLAR){
            return;
        }

        Gob g = ZeeConfig.lastMapViewClickGob;
        String houseName = null;
        if (g!=null)
            houseName = g.getres().name;
        if(g!=null && ZeeManagerGobs.isGobHouse(houseName) && !houseName.endsWith("windmill") && !houseName.endsWith("stonetower")){
            //TODO cabin levels for windmill, stonetower
            lastHouseId = generateHouseId(g);
            lastCabinLevel = 1;
        }else{
            lastHouseId = "";
            lastCabinLevel = CABIN_LEVEL_UNKOWN;
        }
    }

    public static void checkPlayerLocation() {
        // player inside building
        if (ZeeConfig.playerLocation==ZeeConfig.LOCATION_CABIN || ZeeConfig.playerLocation==ZeeConfig.LOCATION_CELLAR) {
            Area area = ZeeConfig.gameUI.map.area();
            //println("area "+area.toString());
            //println("   int "+area.area()+" , rsz "+area.rsz());
            if (!lastHouseId.isBlank()) {
                initHouseInterior();
                showWindow();
            } else {
                reset();
            }
        }
        // player left building
        else{
            if (win!=null)
                win.reqdestroy();
            win = null;
        }
    }

    private static String generateInteriorId(){
        if (lastCabinLevel == CABIN_LEVEL_UNKOWN){
            return "";
        }
        String loc = ZeeConfig.getPlayerLocationName();
        if (ZeeConfig.playerLocation==ZeeConfig.LOCATION_CELLAR) {
            return loc;
        }
        return loc + lastCabinLevel;
    }

    private static void initHouseInterior() {
        if (ZeeConfig.playerLocation==ZeeConfig.LOCATION_CELLAR) {
            lastCabinLevel = CABIN_LEVEL_CELLAR;
        } else if (!ZeeConfig.findGobsByNameEndsWith("-door").isEmpty()) {
            lastCabinLevel = CABIN_LEVEL_ENTRANCE;
        } else{
            lastCabinLevel++;
        }
        Interior interior = getCurrentInterior();
        if (interior==null){
            println("interior not found");
            return;
        }
        lastInterior = interior;
        lastInterior.toggleIcons();
    }

    private static Interior getCurrentInterior() {
        String intId = generateInteriorId();
        if (intId.isBlank()){
            println("int id is blank");
            return null;
        }

        // generate interiors list
        List<Interior> interiors = mapHouseInteriors.get(lastHouseId);
        if (interiors==null){
            interiors = new ArrayList<>();
            mapHouseInteriors.put(lastHouseId,interiors);
        }

        // found interior
        for (Interior interior : interiors) {
            if (interior.id.contentEquals(intId)){
                return interior;
            }
        }

        // generate interior on demand
        lastInterior = new Interior();
        interiors.add(lastInterior);
        return lastInterior;
    }


    static void debug(String msg){
        println("ZeeInteriorTracker > "+msg);
    }
    static void println(String msg){
        ZeeConfig.println(msg);
    }

    static boolean isActive = false;
    public static void toggle() {
        if (ZeeCupboardLabeler.lastHouseId.isBlank()){
            ZeeConfig.msgError("unknown building");
            isActive = false;
            return;
        }
        isActive = !isActive;
        if (isActive) {
            showWindow();
        }else{
            destroyWindow();
        }
        if (lastInterior!=null)
            lastInterior.toggleIcons();
    }

    public static void checkCupboardContents(Window window) {
        if (lastCupboardClicked==null){
            println("lastCupboardClicked null");
            return;
        }
        if (lastCupboardClicked.rc==null){
            println("lastCupboardClicked.rc null");
            return;
        }
        if (window==null){
            println("cupboard window null");
            return;
        }
        Inventory inv = window.getchild(Inventory.class);
        if (inv==null){
            println("cupboard inventory null");
            return;
        }
        new ZeeThread(){
            public void run() {
                try {
                    sleep(333);
                    HashMap<String, Integer> mapItemQantity = inv.getMapItemNameCount();
                    if (mapItemQantity.isEmpty()) {
                        return;
                    }
                    //println(map.toString());
                    if (lastInterior==null){
                        lastInterior = getCurrentInterior();
                    }
                    lastInterior.updateCupboard(lastCupboardClicked,mapItemQantity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static class Interior {
        String id;
        List<Cupboard> cups;

        public Interior(){
            id = generateInteriorId();
            cups = new ArrayList<>();
        }

        public Cupboard getCupboard(Coord2d rc){
            for (Cupboard cup : cups) {
                if (cup.rc.compareToFixMaybe(rc) == 0)
                    return cup;
            }
            return null;
        }

        void removeCupIcons(){
            List<Gob> cupGobs = ZeeConfig.findGobsByNameEndsWith("/cupboard");
            for (Gob cg : cupGobs) {
                synchronized (cg) {
                    Gob.Overlay ol = cg.findol(ZeeGobPointer.class);
                    if (ol != null) {
                        ol.remove(false);
                    }
                }
            }
        }

        void addCupIcons(){
            try {
                List<Gob> cupGobs = ZeeConfig.findGobsByNameEndsWith("/cupboard");
                for (Gob gobCup : cupGobs) {
                    Cupboard cup = getCupboard(gobCup.rc);
                    if (cup!=null){
                        try {
                            Tex tex = Resource.remote().loadwait(cup.itemResName).flayer(Resource.Image.class).tex();
                            gobCup.addol(new ZeeGobPointer(gobCup, tex, true));
                            gobCup.hasPointer = true;
                        }catch (Resource.NoSuchLayerException e) {
                            println("cuplbl > "+e.getMessage());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        void toggleIcons() {
            if (!isActive){
                this.removeCupIcons();
            } else {
                this.addCupIcons();
            }
        }

        void updateCupboard(Gob gobCup, HashMap<String, Integer> mapItemNameCount) {

            if (mapItemNameCount.isEmpty()) {
                return;
            }

            // find most common item
            String mostCommonItem = mapItemNameCount.entrySet().iterator().next().getKey();
            for (Map.Entry<String, Integer> entry : mapItemNameCount.entrySet()) {
                int countItems = mapItemNameCount.get(mostCommonItem);
                if (countItems < entry.getValue()){
                    mostCommonItem = entry.getKey();
                }
            }

            // update cupboard and label gob
            Cupboard cupboard = this.getCupboard(gobCup.rc);
            if (cupboard==null){
                cupboard = new Cupboard(mostCommonItem,gobCup.rc);
                this.cups.add(cupboard);
            } else {
                cupboard.itemResName = mostCommonItem;
            }

            // label cupboard
            try {
                Tex tex = Resource.remote().loadwait(mostCommonItem).flayer(Resource.Image.class).tex();
                gobCup.addol(new ZeeGobPointer(gobCup, tex, true));
                gobCup.hasPointer = true;
                //update window
                showWindow();
            }catch (Resource.NoSuchLayerException e) {
                println("cuplbl > "+e.getMessage());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private class Cupboard {
            String itemResName;
            Coord2d rc;

            public Cupboard(String itemName, Coord2d rc) {
                this.itemResName = itemName;
                this.rc = Coord2d.of(rc.x,rc.y);
            }
        }
    }
}

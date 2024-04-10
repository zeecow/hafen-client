package haven;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return houseId;
    }

    private static void hideWindow() {
        win = ZeeConfig.getWindow(winTitle);
        if (win != null){
            win.reqdestroy();
            win = null;
        }
    }

    static void showWindow() {

        hideWindow();

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

        wdg = win.add(new Label(lastHouseId), CABIN_LEVEL_CELLAR, CABIN_LEVEL_CELLAR);

        wdg = win.add(new Label(generateInteriorId()), ZeeWindow.posBelow(wdg, CABIN_LEVEL_CELLAR,3));

        wdg = win.add(new Label("Interiors: "+mapHouseInteriors.size()), ZeeWindow.posBelow(wdg, CABIN_LEVEL_CELLAR,3));

        wdg = win.add(new Button(60,"delete"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    if (lastHouseId !=null && !lastHouseId.isBlank()) {
                        mapHouseInteriors.remove(lastHouseId);
                        win.wdgmsg("close");
                    }
                }
            }
        },ZeeWindow.posBelow(wdg, CABIN_LEVEL_CELLAR,3));

        win.pack();
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
            GAttrib attr = null;
            for (GAttrib a : house.attr.values()) {
                if (a.getClass().getSimpleName().contentEquals("Materials")){
                    attr = a;
                    break;
                }
            }
            if (attr!=null){
                Field f = attr.getClass().getDeclaredField("mats");
                @SuppressWarnings("unchecked")
                Map<Integer, Material> mats = (Map<Integer, Material>) f.get(attr);
                for (Material m : mats.values()) {
                    //println(m.states.toString());
                    Pattern pattern = Pattern.compile("\\/([a-z\\-]+)\\(");
                    Matcher matcher = pattern.matcher(m.states.toString());
                    matcher.find();
                    String basename = matcher.group(1);
                    if (!basenames.contains(basename))
                        basenames.add(basename);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
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
        if(g!=null && ZeeManagerGobClick.isGobHouse(houseName) && !houseName.endsWith("windmill") && !houseName.endsWith("stonetower")){
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
        lastInterior.toggleCupboardLabels();
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
            hideWindow();
        }
        if (lastInterior!=null)
            lastInterior.toggleCupboardLabels();
    }

    public static void checkCupboardContents(Window window) {
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
                    HashMap<String, Integer> map = inv.getMapItemNameCount();
                    if (map.isEmpty()) {
                        println("empty cupboard");
                        return;
                    }
                    //println(map.toString());
                    if (lastInterior==null){
                        lastInterior = getCurrentInterior();
                    }
                    lastInterior.updateCupboard(lastCupboardClicked,map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static class Interior {
        String id;
        Gob gobRef;
        List<Cupboard> cups;

        public Interior(){
            id = generateInteriorId();
            cups = new ArrayList<>();

            if (lastCabinLevel==CABIN_LEVEL_CELLAR)
                gobRef = ZeeConfig.getClosestGobByNameEnds("/cellarstairs");
            else if (lastCabinLevel==CABIN_LEVEL_ENTRANCE)
                gobRef = ZeeConfig.getClosestGobByNameEnds("-door");
            else
                gobRef = ZeeConfig.getClosestGobByNameEnds("downstairs");

            if (gobRef==null){
                println("new Interior has no gobRef");
            }
        }

        public Cupboard getCupboard(double distCupToRef){
            for (Cupboard cup : cups) {
                if (cup.distToGobRef == distCupToRef)
                    return cup;
            }
            return null;
        }

        void removeCupIcons(){
            List<Gob> cupGobs = ZeeConfig.findGobsByNameEndsWith("/cupboard");
            ZeeConfig.removeGobText(gobRef);
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
                ZeeConfig.addGobText(gobRef, "ref");
                List<Gob> cupGobs = ZeeConfig.findGobsByNameEndsWith("/cupboard");
                for (Gob gobCup : cupGobs) {
                    Float dist = ZeeConfig.distanceBetweenGobs(gobCup, this.gobRef);
                    if (dist == null) {
                        println("labelCupboard > cupboard dist null");
                        continue;
                    }
                    for (Cupboard cup : this.cups) {
                        if (dist == cup.distToGobRef) {
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
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        void toggleCupboardLabels() {
            if (!isActive){
                this.removeCupIcons();
            } else {
                this.addCupIcons();
            }
        }

        void updateCupboard(Gob gobCup, HashMap<String, Integer> mapItemNameCount) {

            if (mapItemNameCount.isEmpty()) {
                println("empty cupboard 2");
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
            Float dist = ZeeConfig.distanceBetweenGobs(gobCup, this.gobRef);
            if (dist==null){
                println("updateCupboard > dist not found");
                ZeeConfig.addGobText(gobCup,"noDist?");
            } else {
                Cupboard cupboard = this.getCupboard(dist);
                if (cupboard==null){
                    println("updateCupboard > new cupboard > "+mostCommonItem);
                    cupboard = new Cupboard(dist,mostCommonItem);
                    this.cups.add(cupboard);
                } else {
                    cupboard.itemResName = mostCommonItem;
                }
                try {
                    Tex tex = Resource.remote().loadwait(mostCommonItem).flayer(Resource.Image.class).tex();
                    gobCup.addol(new ZeeGobPointer(gobCup, tex, true));
                    gobCup.hasPointer = true;
                }catch (Resource.NoSuchLayerException e) {
                    println("cuplbl > "+e.getMessage());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        private class Cupboard {
            double distToGobRef;
            String itemResName;

            public Cupboard(double distRef, String itemName) {
                this.distToGobRef = distRef;
                this.itemResName = itemName;
            }
        }
    }
}

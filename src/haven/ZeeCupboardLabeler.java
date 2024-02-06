package haven;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZeeCupboardLabeler {

    static Window win;
    static String winTitle = "Cupboard labeler";
    static HashMap<String,List<Interior>> mapHouseInteriors = new HashMap<>();
    static String lastHouseId = "";
    static int cabinLevel = -1;

    static String generateHouseId(Gob house){
        List<String> mats = getHouseMatsBasenames(house);
        if (mats.isEmpty()){
            println("mats empty");
            return"";
        }
        String houseId = house.getres().basename() + "@" + ZeeConfig.getPlayerLocationName()+"+";
        for (int i = 0; i < mats.size(); i++) {
            if (i!=0)
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
                            reset();
                            this.reqdestroy();
                        }
                    }
                },
                ZeeConfig.gameUI.sz.div(2)
        );

        wdg = win.add(new Label(lastHouseId),0,0);

        wdg = win.add(new Label(getInteriorId()), ZeeWindow.posBelow(wdg,0,3));

        wdg = win.add(new Label("Interiors: "+mapHouseInteriors.size()), ZeeWindow.posBelow(wdg,0,3));

        wdg = win.add(new Button(60,"delete"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    if (lastHouseId !=null && !lastHouseId.isBlank()) {
                        mapHouseInteriors.remove(lastHouseId);
                        win.wdgmsg("close");
                    }
                }
            }
        },ZeeWindow.posBelow(wdg,0,3));

        win.pack();
    }

    static void reset() {
        //println("reset cupboard lblr");
        lastHouseId = "";
        cabinLevel = -1;
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
            cabinLevel = 1;
        }else{
            lastHouseId = "";
            cabinLevel = -1;
        }
    }

    public static void checkInterior() {
        if (!isActive)
            return;
        // player inside building
        if (ZeeConfig.playerLocation==ZeeConfig.LOCATION_CABIN || ZeeConfig.playerLocation==ZeeConfig.LOCATION_CELLAR) {
            if (!lastHouseId.isBlank()) {
                generateInteriorId();
                //println(lastHouseId);
                //println("    " + getInteriorId());
                showWindow();
            }else{
                reset();
            }
        }
        // player left building
        else{
            reset();
        }
    }

    private static String getInteriorId(){
        if (cabinLevel == -1){
            return "";
        }
        String loc = ZeeConfig.getPlayerLocationName();
        if (ZeeConfig.playerLocation==ZeeConfig.LOCATION_CELLAR) {
            return loc;
        }
        return loc + cabinLevel;
    }

    private static void generateInteriorId() {
        String loc = ZeeConfig.getPlayerLocationName();
        if (ZeeConfig.playerLocation==ZeeConfig.LOCATION_CELLAR) {
            cabinLevel = 0;
        }
        if (!ZeeConfig.findGobsByNameEndsWith("-door").isEmpty()) {
            cabinLevel = 1;
        }else{
            cabinLevel++;
        }
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
    }

    private static class Interior {

        // ex: "cellar" "cabin+door" "cabin+stairs" //TODO: Great Hall, Tower
        String id;
        List<Cupboard> cups;

        public Interior(){
            String loc = ZeeConfig.getPlayerLocationName();
            id = loc;
            cups = new ArrayList<>();
            List<Gob> gobs = ZeeConfig.findGobsByNameEndsWith("-door","cellarstairs","upstairs","downstairs");
            for (Gob g : gobs) {
                String basename = g.getres().basename();
                if (basename.endsWith("-door")){
                    id += "0";
                }
            }
            List<Gob> cupboards = ZeeConfig.findGobsByNameEndsWith("/cupboard");
            for (Gob g : cupboards) {
                cups.add(new Cupboard(ZeeConfig.distanceToPlayer(g)));
            }
        }

        private class Cupboard {
            double distToPlayer;
            String mostCommonItemBasename;

            public Cupboard(double distanceToPlayer) {
                distToPlayer = distanceToPlayer;
                mostCommonItemBasename = "?";
            }
        }
    }
}

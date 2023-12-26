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
    static HashMap<String,List<Interior>> mapHouseInteriors = new HashMap<>();
    static String clickedDoorHouseId = "";
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

//    static void showWindow() {
//        Widget wdg;
//        String title = "Cupboard labeler";
//
//        win = ZeeConfig.getWindow(title);
//        if (win != null){
//            win.reqdestroy();
//            win = null;
//        }
//
//        //create window
//        win = ZeeConfig.gameUI.add(
//                new Window(Coord.of(120,70),title){
//                    public void wdgmsg(String msg, Object... args) {
//                        if (msg.contentEquals("close")){
//                            exitCupbLblr();
//                            this.reqdestroy();
//                        }
//                    }
//                },
//                ZeeConfig.gameUI.sz.div(2)
//        );
//
//        wdg = win.add(new Label(clickedDoorHouseId),0,0);
//
//        wdg = win.add(new Label("Interiors: "+mapHouseInteriors.size()), ZeeWindow.posBelow(wdg,0,3));
//
//        wdg = win.add(new Button(60,"delete"){
//            public void wdgmsg(String msg, Object... args) {
//                if (msg.contentEquals("activate")){
//                    if (clickedDoorHouseId !=null) {
//                        mapHouseInteriors.remove(clickedDoorHouseId);
//                        win.wdgmsg("close");
//                    }
//                }
//            }
//        },ZeeWindow.posBelow(wdg,0,3));
//
//        win.pack();
//    }

    static void exitCupbLblr() {
        clickedDoorHouseId = "";
        cabinLevel = -1;
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

                    //Pattern pattern0 = Pattern.compile("(\\d+\\.?+\\d*)\\s+(\\S+) of ([\\S\\s]+)$");
                    //.compile("(.*?)(\\d+)(.*)");
                    Pattern pattern = Pattern.compile("\\/([a-z]+)\\(");
                    Matcher matcher = pattern.matcher(m.states.toString());
                    matcher.find();
                    String basename = matcher.group(1);
                    if (!basenames.contains(basename))
                        basenames.add(basename);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return basenames;
    }

    static void checkHouseClick() {

        //already inside
        if (ZeeConfig.playerLocation==ZeeConfig.LOCATION_CABIN || ZeeConfig.playerLocation==ZeeConfig.LOCATION_CELLAR){
            return;
        }

        Gob g = ZeeConfig.lastMapViewClickGob;
        if(g!=null && ZeeManagerGobClick.isGobHouse(g.getres().name)){
            clickedDoorHouseId = generateHouseId(g);
        }else{
            clickedDoorHouseId = "";
        }
        cabinLevel = -1;
    }

    public static void checkInterior() {
        if (!clickedDoorHouseId.isBlank()) {
            println(clickedDoorHouseId);
            println("    "+generateInteriorId());
        }
    }

    private static String generateInteriorId() {
        String loc = ZeeConfig.getPlayerLocationName();
        if (ZeeConfig.playerLocation==ZeeConfig.LOCATION_CELLAR) {
            cabinLevel = - 1;
            return loc;
        }
        if (!ZeeConfig.findGobsByNameEndsWith("-door").isEmpty()) {
            cabinLevel = 0;
        }else{
            cabinLevel++;
        }
        return loc + cabinLevel;
    }

    static void debug(String msg){
        println("ZeeInteriorTracker > "+msg);
    }
    static void println(String msg){
        ZeeConfig.println(msg);
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

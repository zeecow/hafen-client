package haven;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZeeCupboardLabeler {

    static final String STR_MENUPETAL = "Cupboard labeler";

    static HashMap<String,List<Interior>> mapHouseInteriors = new HashMap<>();

    static String currentHouseId = "";

    static Window win;

    public static void addHouse(Gob house) {
        List<String> mats = getHouseMatsBasenames(house);
        if (mats.isEmpty()){
            println("mats empty");
            return;
        }
        String houseId = house.getres().basename() + "@" + ZeeConfig.getPlayerLocationName()+"+";
        for (int i = 0; i < mats.size(); i++) {
            if (i!=0)
                houseId += ",";
            houseId += mats.get(i);
        }

        showWindow(houseId);

        if (!mapHouseInteriors.containsKey(houseId))
            mapHouseInteriors.put(houseId,new ArrayList<Interior>());

        currentHouseId = houseId;
    }

    private static void showWindow(String houseId) {
        Widget wdg;
        String title = "Cupboard labeler";

        win = ZeeConfig.getWindow(title);
        if (win != null){
            win.reqdestroy();
            win = null;
        }

        //create window
        win = ZeeConfig.gameUI.add(
                new Window(Coord.of(120,70),title){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("close")){
                            exitCupbLblr();
                            this.reqdestroy();
                        }
                    }
                },
                ZeeConfig.gameUI.sz.div(2)
        );

        wdg = win.add(new Label(houseId),0,0);

        wdg = win.add(new Button(60,"delete"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    if (currentHouseId!=null) {
                        mapHouseInteriors.remove(currentHouseId);
                        win.wdgmsg("close");
                    }
                }
            }
        },0,wdg.c.y+wdg.sz.y+3);

        win.pack();
    }

    static void exitCupbLblr() {
        currentHouseId = "";
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

        private class Cupboard {
            double distToPlayer;
            String mostCommonItemBasename;
        }
    }
}

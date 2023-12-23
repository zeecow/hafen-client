package haven;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZeeCupboardLabeler {

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
        println(houseId);
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
}

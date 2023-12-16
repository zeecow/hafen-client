package haven;

import java.util.HashMap;
import java.util.List;

public class ZeeInteriorTracker {

    class Interior{
        String id;
        int location;
        List<InteriorObj> objs;
        public void Interior(List<Gob> gobs){
        }
        private class InteriorObj {
            String resName;
            Coord tile;
        }
    }

    private static HashMap<String, Interior> mapIdInterior = new HashMap<>();

    public static void checkInterior(){

        // invalid location
        if ( ZeeConfig.playerLocation!=ZeeConfig.LOCATION_CABIN && ZeeConfig.playerLocation!=ZeeConfig.LOCATION_CELLAR ){
            debug("location invalid: "+ZeeConfig.getPlayerLocationName());
            return;
        }

        // gobs count
        HashMap<String,Integer> mapResnameCount = new HashMap<>();
        for (Gob g : ZeeConfig.getAllGobs()) {
            String name = g.getres().name;
            if (!mapResnameCount.containsKey(name))
                mapResnameCount.put(name, 1);
            else
                mapResnameCount.put( name, mapResnameCount.get(name) + 1 );
        }

        // print interior
        debug(ZeeConfig.getPlayerLocationName());
        println("    "+mapResnameCount.toString());

    }

    static void debug(String msg){
        println("ZeeInteriorTracker > "+msg);
    }
    static void println(String msg){
        ZeeConfig.println(msg);
    }
}

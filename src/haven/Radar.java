package haven;

import java.util.HashMap;
import java.util.Map;

public class Radar {
    
    private static final Map<String, String> gob2icon = new HashMap<>();
    
    static {
        //TODO: read from config
	gob2icon.put("gfx/kritter/squirrel/squirrel", "gfx/kritter/squirrel/icon");
	gob2icon.put("gfx/kritter/chicken/chicken[gfx/kritter/chicken/hen]", "gfx/invobjs/hen");
	gob2icon.put("gfx/terobjs/pow", "paginae/act/hearth");
    }
    
    public static boolean process(GobIcon icon) {
	try {
	    String gres = icon.gob.resid();
	    String ires = icon.res.get().name;
	    if(gres != null && ires != null) {
		if(!ires.equals(gob2icon.get(gres))) {
		    gob2icon.put(gres, ires);
		    Debug.log.printf("%s => %s%n", gres, ires);
		    //TODO: changed, save
		}
		return true;
	    }
	} catch (Loading ignored) {}
	return false;
    }
    
    public static GobIcon getIcon(Gob gob) {
	String resname = gob2icon.get(gob.resid());
	if(resname != null) {
	    return new GobIcon(gob, Resource.remote().load(resname));
	}
	return null;
    }
    
    public static void addCustomSettings(Map<String, GobIcon.Setting> settings) {
	//TODO: read from config
	addSetting(settings, "paginae/act/hearth", true);
    }
    
    private static void addSetting(Map<String, GobIcon.Setting> settings, String res, boolean def) {
	if(!settings.containsKey(res)) {
	    GobIcon.Setting cfg = new GobIcon.Setting(new Resource.Spec(null, res));
	    cfg.show = cfg.defshow = def;
	    settings.put(res, cfg);
	}
    }
}

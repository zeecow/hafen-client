package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.ender.Reflect;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GobRadius {
    private static final String GOB_RADIUS_JSON = "gob_radius.json";
    public static final Map<String, GobRadius> gobRadiusCfg;
    static final Color DEF_COL = new Color(255, 255, 255, 128);
    public static final Resource msrad = Resource.remote().loadwait("gfx/fx/msrad");
    
    public String color;
    public float radius;
    private static boolean init = false;
    
    static {
	gobRadiusCfg = parseJson(Config.loadFile(GOB_RADIUS_JSON));
    }
    
    static void init() {
	if(!init) {
	    init = true;
	    showDefaultRadii(CFG.SHOW_GOB_RADIUS.get());
	}
    }
    
    private static Map<String, GobRadius> parseJson(String json) {
	Map<String, GobRadius> result = new HashMap<>();
	if(json != null) {
	    try {
		Gson gson = getGson();
		Type collectionType = new TypeToken<HashMap<String, GobRadius>>() {
		}.getType();
		result = gson.fromJson(json, collectionType);
	    } catch (Exception e) {
		result = new HashMap<>();
	    }
	}
	return result;
    }

    public static void save() {
	Config.saveFile(GOB_RADIUS_JSON, getGson().toJson(gobRadiusCfg));
    }

    public static GobRadius get(String resname) {
	return gobRadiusCfg.get(resname);
    }
    
    public static void toggle() {
	boolean show = !CFG.SHOW_GOB_RADIUS.get();
	CFG.SHOW_GOB_RADIUS.set(show, true);
	showDefaultRadii(show);
    }
    
    public static void showDefaultRadii(boolean show) {
	try {
	    Reflect.invokeStatic(msrad.layer(Resource.CodeEntry.class).get("spr"), "show", new Class[]{boolean.class}, show);
	} catch (Exception ignored) {
	    ignored.printStackTrace();
	}
    }
    
    private static Gson getGson() {
	GsonBuilder builder = new GsonBuilder();
	builder.setPrettyPrinting();
	return builder.create();
    }

    public Color color() {
	Color c = Utils.hex2color(color, null);
	if(c == null) {
	    return DEF_COL;
	}
	return c;
    }

}

package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GobRadius {
    private static final String GOB_RADIUS_JSON = "gob_radius.json";
    public static final Map<String, GobRadius> gobRadiusCfg;
    static final Color DEF_COL = new Color(255, 255, 255, 128);

    public String color;
    public float radius;

    static {
	gobRadiusCfg = parseJson(Config.loadFile(GOB_RADIUS_JSON));
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

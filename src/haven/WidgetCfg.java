package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class WidgetCfg {
    private static final Gson gson;
    private static final String CONFIG_JSON = "windows.json";
    public static final Map<String, WidgetCfg> CFG;

    public Coord c, sz;

    static {
	gson = (new GsonBuilder()).setPrettyPrinting().create();
	Map<String, WidgetCfg> tmp = null;
	try {
	    Type type = new TypeToken<Map<String, WidgetCfg>>() {
	    }.getType();
	    tmp = gson.fromJson(Config.loadFile(CONFIG_JSON), type);
	} catch (Exception ignored) {
	}
	if(tmp == null) {
	    tmp = new HashMap<String, WidgetCfg>();
	}
	CFG = tmp;
    }

    public static synchronized WidgetCfg get(String name) {
	return name != null ? CFG.get(name) : null;
    }

    public static synchronized void set(String name, WidgetCfg cfg){
	if(name == null || cfg == null){
	    return;
	}
	CFG.put(name, cfg);
	store();
    }

    private static synchronized void store() {
	Config.saveFile(CONFIG_JSON, gson.toJson(CFG));
    }
}

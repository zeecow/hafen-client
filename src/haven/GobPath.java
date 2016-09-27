package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import haven.States.ColState;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GobPath extends Sprite {
    private static final String UNKNOWN = "<unknown>";
    private Moving move = null;
    private Gob gob;

    public GobPath(Gob gob) {
	super(gob, null);
	this.gob = gob;
    }

    @Override
    public Object staticp() {
	return Rendered.CONSTANS;
    }

    private String resname() {
	try {
	    Resource res = gob.getres();
	    if(res != null) {
		return res.name;
	    }
	} catch (Resource.Loading ignored) { }
	return UNKNOWN;
    }

    public void draw(GOut g) {
	Coord t = target();
	if(t == null) {return;}
	boolean good = false;
	Coord td = Coord.z;
	int tz = 0;
	try {
	    Coord3f gobc = gob.getc();
	    Coord ss = new Coord((int) (t.x - gobc.x), (int) (t.y - gobc.y));
	    td = ss.rotate(-gob.a);
	    tz = (int) (gob.glob.map.getcz(t) - gob.glob.map.getcz(gob.rc)) + 1;
	    good = true;
	} catch (Exception ignored) { }
	if(!good) { return; }

	g.apply();
	BGL gl = g.gl;
	gl.glLineWidth(2.0F);
	gl.glBegin(1);
	gl.glVertex3i(0, 0, 3);
	gl.glVertex3i(td.x, td.y, tz);
	gl.glEnd();
	GOut.checkerr(gl);
    }

    private Coord target() {
	Moving move = move();
	if(move != null) {
	    Class<? extends GAttrib> aClass = move.getClass();
	    if(aClass == LinMove.class) {
		return ((LinMove) move).t;
	    } else if(aClass == Homing.class) {
		return getGobCoords(((Homing) move).tgt());
	    } else if(aClass == Following.class) {
		return getGobCoords(((Following) move).tgt());
	    }
	}
	return null;
    }

    private Coord getGobCoords(Gob gob) {
	if(gob != null) {
	    Coord3f c = gob.getc();
	    return new Coord((int) c.x, (int) c.y);
	}
	return null;
    }

    public boolean setup(RenderList list) {
	Cfg cfg = Cfg.get(resname());
	if(!cfg.show) { return false;}
	Color color = cfg.color;
	KinInfo ki = gob.getattr(KinInfo.class);
	if(ki != null) {
	    color = BuddyWnd.gc[ki.group];
	}
	if(color == null) {
	    color = Cfg.DEFAULT.color;
	}
	list.prepo(new ColState(color));
	return true;
    }

    public synchronized Moving move() {
	return move;
    }

    public synchronized void move(Moving m) {
	move = m;
    }

    public synchronized void stop() {
	move = null;
    }

    public static class Cfg {
	public static final String GOB_PATH_JSON = "gob_path.json";
	private static final Cfg DEFAULT = new Cfg(Color.WHITE, false);
	public static final Map<String, Cfg> gobPathCfg;
	private static final Map<String, Cfg> cache = new HashMap<>();
	public Color color;
	public boolean show;
	public String name;

	static {
	    Map<String, Cfg> merge = new HashMap<>();
	    merge.putAll(parseJson(Config.loadJarFile(GOB_PATH_JSON)));
	    merge.putAll(parseJson(Config.loadFSFile(GOB_PATH_JSON)));
	    gobPathCfg = merge;
	    gobPathCfg.put("unknown", DEFAULT);
	}

	private static Map<String, Cfg> parseJson(String json) {
	    Map<String, Cfg> result = new HashMap<>();
	    if(json != null) {
		try {
		    Gson gson = Cfg.getGson();
		    Type collectionType = new TypeToken<HashMap<String, Cfg>>() {
		    }.getType();
		    result = gson.fromJson(json, collectionType);
		} catch (Exception e) {
		    result = new HashMap<>();
		}
	    }
	    return result;
	}

	public Cfg(Color color, boolean show) {
	    this.color = color;
	    this.show = show;
	}

	public static void save() {
	    Gson gson = GobPath.Cfg.getGson();
	    Config.saveFile(GOB_PATH_JSON, gson.toJson(gobPathCfg));
	}

	public static GobPath.Cfg get(String resname) {
	    if(cache.containsKey(resname)) {
		return cache.get(resname);
	    } else if(UNKNOWN.equals(resname)) {
		return DEFAULT;
	    }
	    Cfg cfg = DEFAULT;
	    if(gobPathCfg.containsKey(resname)) {
		cfg = gobPathCfg.get(resname);
	    } else {
		Set<String> keys = gobPathCfg.keySet();
		for (String pattern : keys) {
		    if(resname.contains(pattern)) {
			cfg = gobPathCfg.get(pattern);
			break;
		    }
		}
	    }
	    cache.put(resname, cfg);
	    return cfg;
	}

	public static Gson getGson() {
	    GsonBuilder builder = new GsonBuilder();
	    builder.setPrettyPrinting();
	    builder.registerTypeAdapter(GobPath.Cfg.class, new GobPath.Cfg.Adapter().nullSafe());
	    return builder.create();
	}

	public static class Adapter extends TypeAdapter<Cfg> {

	    @Override
	    public void write(JsonWriter writer, Cfg cfg) throws IOException {
		if(cfg == DEFAULT) {
		    writer.nullValue();
		    return;
		}
		writer.beginObject();
		writer.name("show").value(cfg.show);
		String color = Utils.color2hex(cfg.color);
		if(color != null) {
		    writer.name("color").value(color);
		}
		if(cfg.name != null) {
		    writer.name("name").value(cfg.name);
		}
		writer.endObject();
	    }

	    @Override
	    public Cfg read(JsonReader reader) throws IOException {
		Cfg cfg = new Cfg(null, true);
		reader.beginObject();
		while (reader.hasNext()) {
		    String name = reader.nextName();
		    switch (name) {
			case "show":
			    cfg.show = reader.nextBoolean();
			    break;
			case "color":
			    cfg.color = Utils.hex2color(reader.nextString(), null);
			    break;
			case "name":
			    cfg.name = reader.nextString();
			    break;
		    }
		}
		reader.endObject();
		return cfg;
	    }
	}
    }
}
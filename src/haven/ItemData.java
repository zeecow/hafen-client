package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemData {
    public static final ItemData EMPTY = new ItemData();
    private static Gson gson;
    private static Map<String, ItemData> item_data = new LinkedHashMap<String, ItemData>(9, 0.75f, true) {
	private static final long serialVersionUID = 1L;

	protected boolean removeEldestEntry(Map.Entry<String, ItemData> eldest) {
	    return size() > 75;
	}

    };
    private Curiosity.Data curiosity;


    public ItemData(GItem item) {
	this(item.info());
    }

    public ItemData(List<ItemInfo> info) {
	init(info);
    }

    private ItemData() {}

    public void init(List<ItemInfo> info) {
	for (ItemInfo ii : info) {
	    String className = ii.getClass().getCanonicalName();
	    QualityList q = new QualityList(ItemInfo.findall(QualityList.classname, info));

	    if(ii instanceof Curiosity) {
		curiosity = new Curiosity.Data((Curiosity) ii, q);
	    }
	}
    }

    public Tex longtip(Resource res) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt = "$b{$size[20]{" + ad.name + "}}\n\n";
	if(pg != null) {tt += pg.text + "\n\n";}

	BufferedImage img = MenuGrid.ttfnd.render(tt, 300).img;
	ITipData[] data = new ITipData[]{curiosity};
	for (ITipData tip : data) {
	    if(tip != null) {
		img = ItemInfo.catimgs(3, img, tip.create().tipimg());
	    }
	}

	return new TexI(img);
    }

    public static ItemData get(String name) {
	if(item_data.containsKey(name)) {
	    return item_data.get(name);
	}
	ItemData data = load(name);
	if(data == null) {data = EMPTY;}
	return data;
    }

    public static void actualize(GItem item, Glob.Pagina pagina) {
	if(item.resname() == null) { return; }

	ItemData data = new ItemData(item);
	String name = pagina.res().name;
	item_data.put(name, data);
	store(name, data);
    }

    private static ItemData load(String name) {
	ItemData data = null;
	String filename = getFilename(name);
	InputStream inputStream = null;
	File file = Config.getFile(filename);
	if(file.exists() && file.canRead()) {
	    try {
		inputStream = new FileInputStream(file);
	    } catch (FileNotFoundException ignored) {
	    }
	} else {
	    inputStream = ItemData.class.getResourceAsStream(filename);
	}
	if(inputStream != null) {
	    data = parseStream(inputStream);
	    item_data.put(name, data);
	}
	return data;
    }

    private static void store(String name, ItemData data) {
	File file = Config.getFile(getFilename(name));
	boolean exists = file.exists();
	if(!exists) {
	    try {
		//noinspection ResultOfMethodCallIgnored
		new File(file.getParent()).mkdirs();
		exists = file.createNewFile();
	    } catch (IOException ignored) {}
	}
	if(exists && file.canWrite()) {
	    PrintWriter out = null;
	    try {
		out = new PrintWriter(file);
		out.print(getGson().toJson(data));
	    } catch (FileNotFoundException ignored) {
	    } finally {
		if(out != null) {
		    out.close();
		}
	    }
	}
    }

    private static String getFilename(String name) {
	return "/item_data/" + name + ".json";
    }

    private static ItemData parseStream(InputStream inputStream) {
	ItemData data = null;
	try {
	    String json = Utils.stream2str(inputStream);
	    data = getGson().fromJson(json, ItemData.class);
	} catch (JsonSyntaxException ignore) {
	} finally {
	    try {inputStream.close();} catch (IOException ignored) {}
	}
	return data;
    }

    private static Gson getGson() {
	if(gson == null) {
	    GsonBuilder builder = new GsonBuilder();
	    builder.setPrettyPrinting();
	    gson = builder.create();
	}
	return gson;
    }

    public interface ITipData {
	ItemInfo.Tip create();
    }
}

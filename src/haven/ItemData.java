package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import haven.resutil.FoodInfo;
import me.ender.Reflect;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemData {
    private static final ItemData EMPTY = new ItemData();
    private static Gson gson;
    private static Map<String, ItemData> item_data = new LinkedHashMap<String, ItemData>(9, 0.75f, true) {
	private static final long serialVersionUID = 1L;

	protected boolean removeEldestEntry(Map.Entry<String, ItemData> eldest) {
	    return size() > 75;
	}

    };
    private Curiosity.Data curiosity;
    private FoodInfo.Data food;
    private Integer wear;
    private ArmorData armor;
    private GastronomyData gast;
    
    
    private ItemData(GItem item) {
	this(item.info());
    }

    private ItemData(List<ItemInfo> info) {
	init(info);
    }

    private ItemData() {}

    public void init(List<ItemInfo> info) {
	for (ItemInfo ii : info) {
	    String className = ii.getClass().getCanonicalName();
	    QualityList q = new QualityList(ItemInfo.findall(QualityList.classname, info));

	    if(ii instanceof Curiosity) {
		curiosity = new Curiosity.Data((Curiosity) ii, q);
	    } else if(ii instanceof FoodInfo){
		food = new FoodInfo.Data((FoodInfo) ii, q);
	    } else if("Gast".equals(className)){
	        gast = new GastronomyData(ii, q);
	    }
	    
	    Pair<Integer, Integer> w = ItemInfo.getWear(info);
	    if(w != null) {
		wear = (int) Math.round(w.b / q.single(QualityList.SingleType.Vitality).multiplier);
	    }
	    
	    Pair<Integer, Integer> a = ItemInfo.getArmor(info);
	    if(a != null) {
		armor = new ArmorData(a, q);
	    }
	    
	}
    }

    public Tex longtip(Resource res) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt = "$b{$size[20]{" + ad.name + "}}";
	if(pg != null) {tt += "\n\n" + pg.text;}

	BufferedImage img = MenuGrid.ttfnd.render(tt, 300).img;
	List<ItemInfo> infos = iteminfo();

	if(!infos.isEmpty()) {
	    img = ItemInfo.catimgs(20, img, ItemInfo.longtip(infos));
	}
	return new TexI(img);
    }
    
    public List<ItemInfo> iteminfo() {
	ITipData[] data = new ITipData[]{curiosity, food, WearData.make(wear), armor, gast};
	List<ItemInfo> infos = new ArrayList<>(data.length);
	for (ITipData tip : data) {
	    if(tip != null) {
		infos.add(tip.create());
	    }
	}
	return infos;
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
	ItemData data = parse(Config.loadFile(getFilename(name)));
	if(data != null) {
	    item_data.put(name, data);
	}
	return data;
    }

    private static void store(String name, ItemData data) {
        Config.saveFile(getFilename(name), getGson().toJson(data));
    }

    private static String getFilename(String name) {
	return "/item_data/" + name + ".json";
    }

    private static ItemData parse(String json) {
	ItemData data = null;
	try {
	    data = getGson().fromJson(json, ItemData.class);
	} catch (JsonSyntaxException ignore) {
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
	ItemInfo create();
    }
    
    private static class WearData implements ITipData {
	public final int max;
	
	private WearData(int wear) {
	    max = wear;
	}
	
	@Override
	public ItemInfo create() {
	    return ItemInfo.make("ui/tt/wear", null, 0, max);
	}
	
	public static WearData make(Integer wear) {
	    if(wear != null) {
		return new WearData(wear);
	    } else {
		return null;
	    }
	}
    }
    
    private static class ArmorData implements ITipData {
	private final Integer hard;
	private final Integer soft;
    
	public ArmorData(Pair<Integer, Integer> armor, QualityList q) {
	    hard = (int) Math.round(armor.a / q.single(QualityList.SingleType.Essence).multiplier);
	    soft = (int) Math.round(armor.b / q.single(QualityList.SingleType.Substance).multiplier);
	}
	
	@Override
	public ItemInfo create() {
	    return ItemInfo.make("ui/tt/armor", null, hard, soft);
	}
    }
    
    private static class GastronomyData implements ITipData {
	private final double glut;
	private final double fev;
    
	public GastronomyData(ItemInfo data, QualityList q) {
	    glut = Reflect.getFieldValueDouble(data, "glut") / q.single(QualityList.SingleType.Substance).multiplier;
	    fev = Reflect.getFieldValueDouble(data, "fev") / q.single(QualityList.SingleType.Essence).multiplier;
	}
    
	@Override
	public ItemInfo create() {
	    return ItemInfo.make("ui/tt/gast", null, glut, fev);
	}
    }
    
}

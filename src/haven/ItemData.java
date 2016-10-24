package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import haven.resutil.FoodInfo;
import me.ender.Reflect;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    private Map<String, Integer> attributes;
    private SlotsData slots;
    
    
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
	    } else if("ISlots".equals(className)){
	        slots = SlotsData.make(ii);
	    }
	    
	    Pair<Integer, Integer> a = ItemInfo.getArmor(info);
	    if(a != null) {
		armor = new ArmorData(a, q);
	    }
	    
	    Pair<Integer, Integer> w = ItemInfo.getWear(info);
	    if(w != null) {
		QualityList.Quality single = q.single(QualityList.SingleType.Vitality);
		wear = (int) Math.round(w.b / (a != null ? single.value / 10.0 : single.multiplier));
	    }
	    
	    List<ItemInfo> attrs = ItemInfo.findall("haven.res.ui.tt.attrmod.AttrMod", info);
	    if(!attrs.isEmpty()){
		Map<Resource, Integer> parsed = new HashMap<>(attrs.size());
		ItemInfo.parseAttrMods(parsed, attrs);
		QualityList.Quality single = q.single(QualityList.SingleType.Average);
		attributes = parsed.entrySet()
		    .stream()
		    .collect(Collectors.toMap(
		        e -> e.getKey().name,
			e -> {
			    double v = e.getValue() / single.multiplier;
			    if(v > 0) {
				return (int) Math.round(v);
			    } else {
				return (int) v;
			    }
			}
		    ));
	    }
	}
    }

    public Tex longtip(Resource res, Session sess) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt = "$b{$size[20]{" + ad.name + "}}";
	if(pg != null) {tt += "\n\n" + pg.text;}

	BufferedImage img = MenuGrid.ttfnd.render(tt, 300).img;
	List<ItemInfo> infos = iteminfo(sess);

	if(!infos.isEmpty()) {
	    img = ItemInfo.catimgs(20, img, ItemInfo.longtip(infos));
	}
	return new TexI(img);
    }
    
    public List<ItemInfo> iteminfo(Session sess) {
	ITipData[] data = new ITipData[]{
	    curiosity,
	    food,
	    WearData.make(wear),
	    armor,
	    gast,
	    AttrData.make(attributes),
	    slots
	    
	};
	List<ItemInfo> infos = new ArrayList<>(data.length);
	for (ITipData tip : data) {
	    if(tip != null) {
		infos.add(tip.create(sess));
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
	    builder.registerTypeAdapter(Resource.class, new ResourceAdapter().nullSafe());
	    gson = builder.create();
	}
	return gson;
    }

    public interface ITipData {
	ItemInfo create(Session sess);
    }
    
    private static class WearData implements ITipData {
	public final int max;
	
	private WearData(int wear) {
	    max = wear;
	}
	
	@Override
	public ItemInfo create(Session sess) {
	    return ItemInfo.make(sess, "ui/tt/wear", null, 0, max);
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
	public ItemInfo create(Session sess) {
	    return ItemInfo.make(sess, "ui/tt/armor", null, hard, soft);
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
	public ItemInfo create(Session sess) {
	    return ItemInfo.make(sess, "ui/tt/gast", null, glut, fev);
	}
    }
    
    
    private static class AttrData implements ITipData {
	private final Map<String, Integer> attrs;
    
	public AttrData(Map<String, Integer> attrs) {
	    this.attrs = attrs;
	}
    
	@Override
	public ItemInfo create(Session sess) {
	    Object[] params = new Object[2 * attrs.size() + 1];
	    params[0] = null;
	    int i = 1;
	    for (Map.Entry<String, Integer> a : attrs.entrySet()) {
		Resource res = Resource.remote().loadwait(a.getKey());
		params[i] = sess.getresid(res);
		params[i + 1] = a.getValue();
		i += 2;
	    }
	    return ItemInfo.make(sess, "ui/tt/attrmod", params);
	}
    
	public static ITipData make(Map<String, Integer> attrs) {
	    if(attrs != null) {
		return new AttrData(attrs);
	    }
	    return null;
	}
    }
    
    private static class SlotsData implements ITipData {
    
	private final int left;
	private final double pmin;
	private final double pmax;
	private final Resource[] attrs;
    
	public SlotsData(int left, double pmin, double pmax, Resource[] attrs) {
	    this.left = left;
	    this.pmin = pmin;
	    this.pmax = pmax;
	    this.attrs = attrs;
	}
    
	public static SlotsData make(ItemInfo info){
            if(info!=null){
		int left = Reflect.getFieldValueInt(info, "left");
		double pmin = Reflect.getFieldValueDouble(info, "pmin");
		double pmax = Reflect.getFieldValueDouble(info, "pmax");
		Resource[] attrres = (Resource[]) Reflect.getFieldValue(info, "attrs");
		return new SlotsData(left, pmin, pmax, attrres);
	    }
            return null;
	}
        
	@Override
	public ItemInfo create(Session sess) {
	    List<Object> params = new ArrayList<>();
	    params.add(null);
	    params.add(pmin);
	    params.add(pmax);
	    if(attrs != null) {
		params.addAll(Arrays.stream(attrs)
		    .map(sess::getresid)
		    .collect(Collectors.toList())
		);
	    }
	    params.add(null);
	    params.add(left);
	    return ItemInfo.make(sess, "ui/tt/slots", params.toArray());
	}
    }
    
    private static class ResourceAdapter extends TypeAdapter<Resource> {
	
	@Override
	public void write(JsonWriter writer, Resource resource) throws IOException {
	    writer.value(resource.name);
	}
	
	@Override
	public Resource read(JsonReader reader) throws IOException {
	    return Resource.remote().loadwait(reader.nextString());
	}
    }
}

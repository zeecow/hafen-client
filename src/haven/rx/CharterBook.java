package haven.rx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haven.*;
import rx.Subscription;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharterBook extends Window {
    private static final String PREFIX = "The name of this charterstone is";
    public static final String CONFIG_JSON = "charterbook.json";
    private static Subscription subscription;
    private static Pattern filter = Pattern.compile(String.format("%s \"(.*)\".", PREFIX));
    private static Map<String, List<String>> config;
    private static List<String> names;
    private static Gson gson;
    private static String username;
    private TextEntry text;

    public CharterBook(Coord sz, String cap, boolean lg, Coord tlo, Coord rbo) {
	super(sz, cap, lg, tlo, rbo);
	pack();
    }

    public static void setUserName(String username) {
	CharterBook.username = username;
    }

    public static void loadConfig(String player) {
	init();
	player = String.format("%s/%s", username, player);
	names = new ArrayList<>(config.getOrDefault(player, Collections.emptyList()));
	names.sort(String::compareTo);
	config.put(player, names);
    }

    private static void init() {
	if(subscription == null) {
	    subscription = Reactor.IMSG.filter(s -> s.startsWith(PREFIX)).subscribe(CharterBook::addCharter);
	}

	gson = (new GsonBuilder()).setPrettyPrinting().create();
	load();
    }

    private static void load() {
	if(config == null) {
	    try {
		config = gson.fromJson(Config.loadFile(CONFIG_JSON), new TypeToken<Map<String, List<String>>>() {
		}.getType());
	    } catch (Exception ignore) {}
	    if(config == null) {
		config = new HashMap<>();
	    }
	}
    }

    private static void save() {
	Config.saveFile(CONFIG_JSON, gson.toJson(config));
    }

    private static void addCharter(String message) {
	Matcher m = filter.matcher(message);
	if(m.find()) {
	    String name = m.group(1);
	    if(!names.contains(name)) {
		names.add(name);
		save();
	    }
	}
    }

    private void onCharterSelected(@SuppressWarnings("unused") int index, String charter) {
	text.settext(charter);
	text.buf.key('\0', KeyEvent.VK_END, 0); //move caret to the end
	setfocus(text);
    }

    @Override
    public <T extends Widget> T add(T child) {
	if(child instanceof TextEntry) {
	    text = (TextEntry) child;
	    add(new DropboxOfStrings(child.sz.x + 15, 5, child.sz.y), child.c)
		.setData(names)
		.setChangedCallback(this::onCharterSelected);
	    add(new Button(50, "GO", false, text::activate), child.c.add(child.sz.x + 20, 0));
	}
	return super.add(child);
    }
}

package me.ender;

import auto.Bot;
import haven.*;
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.ender.AnimalFarm.AnimalActions.*;

public class AnimalFarm {
    public static void processCattleInfo(Window wnd) {
	Set<Avaview> avatars = wnd.children(Avaview.class);
	AnimalType type = AnimalType.getType(wnd.caption());
	if(type != null && avatars.size() == 1) {
	    Avaview ava = avatars.iterator().next();
	    TextEntry edit = wnd.children(TextEntry.class).stream().findFirst().orElse(null);
	    Coord c = new Coord(0, 0);
	    if(edit != null) {
		c.x = edit.c.x;
		c.y = edit.c.y + edit.sz.y + 3;
	    }
	    
	    for (AnimalActions action : type.buttons) {
		c.x += wnd.add(new Button(50, action.name, action.make(wnd.ui.gui, ava.avagob)), c.x, c.y).sz.x + 3;
	    }
	    
	    List<Label> labels = new ArrayList<>(wnd.children(Label.class));
	    int i = 0;
	    while (i < labels.size()) {
		AnimalStatType statType = AnimalStatType.parse(labels.get(i).gettext());
		if(statType != null && i + 1 < labels.size()) {
		    AnimalStat stat = statType.make(labels.get(i + 1).gettext());
		    System.out.println(stat.toString());
		    i++;
		}
		i++;
	    }
	}
    }
    
    enum AnimalType {
	CATTLE(new String[]{"Bull", "Cow"}, Shoo, Slaughter),
	HORSE(new String[]{"Stallion", "Mare"}, Shoo, Slaughter, Ride),
	SHEEP(new String[]{"Ram", "Ewe"}, Shoo, Slaughter, Shear),
	PIG(new String[]{"Hog", "Sow"}, Shoo, Slaughter),
	GOAT(new String[]{"Billy", "Nanny"}, Shoo, Slaughter);
	
	private final Set<String> names;
	private final List<AnimalActions> buttons;
	
	AnimalType(String[] names, AnimalActions... buttons) {
	    this.names = Stream.of(names).collect(Collectors.toSet());
	    this.buttons = Stream.of(buttons).collect(Collectors.toList());
	}
	
	public static AnimalType getType(String name) {
	    for (AnimalType type : values()) {
		if(type.names.contains(name)) {return type;}
	    }
	    return null;
	}
    }
    
    enum AnimalActions {
	Shoo("Shoo", (gui, id) -> () -> Bot.selectFlower(gui, id, "Shoo")),
	Slaughter("Kill", (gui, id) -> () -> Bot.selectFlower(gui, id, "Slaughter")),
	Shear("Shear", (gui, id) -> () -> Bot.selectFlower(gui, id, "Shear wool")),
	Ride("Ride", (gui, id) -> () -> Bot.selectFlower(gui, id, "Giddyup!"));
	
	public final String name;
	private final Func2<GameUI, Long, Runnable> action;
	
	AnimalActions(String name, Func2<GameUI, Long, Runnable> action) {
	    this.name = name;
	    this.action = action;
	}
	
	public Runnable make(GameUI gui, long gob) {
	    return action.call(gui, gob);
	}
    }
    
    enum AnimalStatType {
	QUALITY("Quality:"),
	ENDURANCE("Endurance:", true),
	STAMINA("Stamina:"),
	METABOLISM("Metabolism:"),
	MEAT_QUANTITY("Meat quantity:"),
	MEAT_QUALITY("Meat quality:", true),
	MILK_QUANTITY("Milk quantity:"),
	MILK_QUALITY("Milk quality:", true),
	HIDE_QUALITY("Hide quality:", true),
	BREED_QUALITY("Breeding quality:");
	
	private final String name;
	private final boolean percent;
	
	AnimalStatType(String name, boolean percent) {
	    this.name = name;
	    this.percent = percent;
	}
	
	AnimalStatType(String name) {
	    this(name, false);
	}
    	
	AnimalStat make(String str) {
	    if(percent) {str = str.replaceAll("%", "");}
	    int value = 0;
	    try {value = Integer.parseInt(str);} catch (NumberFormatException ignored) {}
	    return new AnimalStat(this, value);
	}
	
	public static AnimalStatType parse(String name) {
	    for (AnimalStatType type : AnimalStatType.values()) {
		if(type.name.equals(name)) {
		    return type;
		}
	    }
	    return null;
	}
    }
    
    public static class AnimalStat {
	final AnimalStatType type;
	public final int value;
	
	AnimalStat(AnimalStatType type, int value) {
	    this.type = type;
	    this.value = value;
	}
	
	@Override
	public String toString() {
	    return String.format("%s\t%d%s", type.name, value, type.percent ? "%" : "");
	}
    }
}

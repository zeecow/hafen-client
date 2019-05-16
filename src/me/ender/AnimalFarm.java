package me.ender;

import auto.Bot;
import haven.*;
import rx.functions.Func2;

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
}

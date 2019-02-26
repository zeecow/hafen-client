package auto;

import haven.Defer;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.rx.Reactor;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class Bot {
    public static void pickup(GameUI gui) {
	UI ui = gui.ui;
	Defer.later(() -> {
	    ui.sess.glob.oc.stream()
		.filter(startsWith("gfx/terobjs/trees/"))
		.min(distance)
		.ifPresent(RClick.andThen(selectFlower("Take branch")));
	    return null;
	});
    }
    
    public static Comparator<Gob> distance = (o1, o2) -> {
	try {
	    Gob p = o1.glob.oc.getgob(o1.glob.sess.ui.gui.plid);
	    return Double.compare(p.rc.dist(o1.rc), p.rc.dist(o2.rc));
	} catch (Exception ignored) {}
	return Long.compare(o1.id, o2.id);
    };
    
    public static Consumer<Gob> RClick = Gob::rclick;
    
    public static Consumer<Gob> selectFlower(String option) {
	return gob -> Reactor.FLOWER.first().subscribe(flowerMenu -> flowerMenu.forceChoose(option));
    }
    
    private static Predicate<Gob> startsWith(String text) {
	return gob -> {
	    try {
		return gob.getres().name.startsWith(text);
	    } catch (Exception ignored) {}
	    return false;
	};
    }
}

package me.ender;

import auto.Bot;
import haven.Avaview;
import haven.Button;
import haven.Pair;
import haven.Window;
import haven.rx.Reactor;

import java.util.HashSet;
import java.util.Set;

public class WindowDetector {
    private static final Set<Window> toDetect = new HashSet<>();
    
    static {
	Reactor.WINDOW.subscribe(WindowDetector::onWindowEvent);
    }
    
    public static void detect(Window window) {
	synchronized (toDetect) {
	    toDetect.add(window);
	}
    }
    
    private static void onWindowEvent(Pair<Window, String> event) {
	synchronized (toDetect) {
	    if(toDetect.contains(event.a)) {
		switch (event.b) {
		    case Window.ON_DESTROY:
			toDetect.remove(event.a);
			break;
		    case Window.ON_PACK:
			recognize(event.a);
			break;
		}
	    }
	}
    }
    
    private static void recognize(Window window) {
	Set<Avaview> avas = window.children(Avaview.class);
	if(avas.size() == 1) {
	    Avaview ava = avas.iterator().next();
	    Button button = new Button(75, "Shoo", () -> Bot.selectFlower(window.ui.gui, ava.avagob, "Shoo"));
	    window.add(button, 0, 0);
	}
    }
}

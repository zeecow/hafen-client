package me.ender;

import haven.*;
import haven.rx.CharterBook;
import haven.rx.Reactor;

import java.util.HashSet;
import java.util.Set;

public class WindowDetector {
    private static final Object lock = new Object();
    private static final Set<Window> toDetect = new HashSet<>();
    private static final Set<Window> detected = new HashSet<>();
    
    static {
	Reactor.WINDOW.subscribe(WindowDetector::onWindowEvent);
    }
    
    public static void detect(Window window) {
	synchronized (toDetect) {
	    toDetect.add(window);
	}
    }
    
    private static void onWindowEvent(Pair<Window, String> event) {
	synchronized (lock) {
	    if(toDetect.contains(event.a)) {
		switch (event.b) {
		    case Window.ON_DESTROY:
			toDetect.remove(event.a);
			detected.remove(event.a);
			break;
		    //Detect window on 'pack' message - this is last message server sends after constructing a window
		    case Window.ON_PACK:
			if(!detected.contains(event.a)) {
			    detected.add(event.a);
			    recognize(event.a);
			}
			break;
		}
	    }
	}
    }
    
    private static void recognize(Window window) {
	AnimalFarm.processCattleInfo(window);
	if("Belt".equals(window.caption())) {
	    window.ui.gui.beltinv = window.getchild(Inventory.class);
	}
    }
    
    private static Widget.Factory convert(Widget parent, Widget.Factory f, Object[] cargs) {
	if(parent instanceof Window) {
	    Window window = (Window) parent;
	    //TODO: extract to separate class
	    if("Milestone".equals(window.caption()) && f instanceof Label.$_) {
		String text = (String) cargs[0];
		if(!text.equals("Make new trail:")) {
		    return new Label.Untranslated.$_();
		}
		System.out.println(text);
	    }
	}
	return f;
    }
    
    public static Widget create(Widget parent, Widget.Factory f, UI ui, Object[] cargs) {
	f = convert(parent, f, cargs);
	return f.create(ui, cargs);
    }
    
    public static Widget newWindow(Coord sz, String title, boolean lg) {
	if("Sublime Portico".equals(title) || "Charter Stone".equals(title)) {
	    return new CharterBook(sz, title, lg, Coord.z, Coord.z);
	}
	return(new Window(sz, title, lg, Coord.z, Coord.z));
    }
}

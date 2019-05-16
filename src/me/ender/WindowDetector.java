package me.ender;

import haven.Pair;
import haven.Window;
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
    }
}

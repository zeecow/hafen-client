package haven.rx;

import haven.FlowerMenu;
import haven.Pair;
import haven.Window;
import rx.subjects.PublishSubject;

public class Reactor {
    /** Publishes all 'error' messages from server*/
    public static final PublishSubject<String> EMSG = PublishSubject.create();

    /** Publishes all 'info' messages from server*/
    public static final PublishSubject<String> IMSG = PublishSubject.create();
    
    /** Publishes changes to player name */
    public static final PublishSubject<String> PLAYER = PublishSubject.create();
 
    public static final PublishSubject<FlowerMenu> FLOWER = PublishSubject.create();
    
    /** Publishes window events */
    public static final PublishSubject<Pair<Window, String>> WINDOW = PublishSubject.create();
}

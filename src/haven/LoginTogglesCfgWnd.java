package haven;

import haven.rx.BuffToggles;

public class LoginTogglesCfgWnd extends Window {
    private static Window instance;

    public static void toggle(Widget parent) {
	if(instance == null) {
	    instance = parent.add(new LoginTogglesCfgWnd());
	} else {
	    remove();
	}
    }

    public static void remove() {
	if(instance != null) {
	    instance.reqdestroy();
	    instance = null;
	}
    }

    public LoginTogglesCfgWnd() {
	super(Coord.z, "Toggle at login");
	justclose = true;

	int y = 0;
	for (BuffToggles.Toggle toggle : BuffToggles.toggles) {
	    add(new OptWnd.CFGBox(toggle.name, toggle.startup), 0, y);
	    y += 25;
	}

	pack();
	if(asz.x < 120) {
	    resize(new Coord(200, asz.y));
	}
    }

}

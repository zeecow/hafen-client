/* Preprocessed source code */
package haven.res.ui.croster;

import haven.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@haven.FromResource(name = "ui/croster", version = 75)
public class RosterWindow extends Window {
    public static final Map<Glob, RosterWindow> rosters = new HashMap<>();
    public static int rmseq = 0;
    public int btny = 0;
    public List<TypeButton> buttons = new ArrayList<>();

    RosterWindow() {
	super(Coord.z, "Cattle Roster", true);
    }

    public void show(CattleRoster rost) {
	for(CattleRoster ch : children(CattleRoster.class))
	    ch.show(ch == rost);
    }

    public void addroster(CattleRoster rost) {
	if(btny == 0)
	    btny = rost.sz.y + UI.scale(10);
	add(rost, Coord.z);
	TypeButton btn = this.add(rost.button());
	btn.action(() -> show(rost));
	buttons.add(btn);
	buttons.sort((a, b) -> (a.order - b.order));
	int x = 0;
	for(Widget wdg : buttons) {
	    wdg.move(new Coord(x, btny));
	    x += wdg.sz.x + UI.scale(10);
	}
	buttons.get(0).click();
	pack();
	rmseq++;
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if((sender == this) && msg.equals("close")) {
	    this.hide();
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }
}

/* >pagina: RosterButton$Fac */

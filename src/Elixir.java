/* Preprocessed source code */
import haven.*;
import haven.res.ui.tt.attrmod.AttrMod;
import haven.res.ui.tt.attrmod.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* >tt: Elixir */
@haven.FromResource(name = "ui/tt/alch/elixir", version = 6)
public class Elixir extends ItemInfo.Tip {
    public final int time;
    public final List<ItemInfo> effs;

    public Elixir(Owner owner, int time, List<ItemInfo> effs) {
	super(owner);
	this.time = time;
	this.effs = effs;
	ZeeResearch.hsElixirStr = getElixirStr();
	ZeeResearch.hsElixirStrMs = System.currentTimeMillis();
    }

    public static ItemInfo mkinfo(Owner owner, Object... args) {
	int time = (Integer)args[1];
	List<ItemInfo> effs = new ArrayList<>();
	for(Object raw : (Object[])args[2])
	    effs.add(ItemInfo.buildinfo(owner, new Object[] {(Object[])raw}).get(0));
	return(new Elixir(owner, time, effs));
    }

    static String[] units = {"s", "m", "h", "d"};
    static int[] div = {60, 60, 24};
    static String timefmt(int time) {
	int[] vals = new int[units.length];
	vals[0] = time;
	for(int i = 0; i < div.length; i++) {
	    vals[i + 1] = vals[i] / div[i];
	    vals[i] = vals[i] % div[i];
	}
	StringBuilder buf = new StringBuilder();
	for(int i = units.length - 1; i >= 0; i--) {
	    if(vals[i] > 0) {
		if(buf.length() > 0)
		    buf.append(' ');
		buf.append(vals[i]);
		buf.append(units[i]);
	    }
	}
	return(buf.toString());
    }

    private static final Text head = Text.render("Effects:");
    private static final Text none = RichText.render("$i{None}", -1);
    public void layout(Layout l) {
	l.cmp.add(head.img, new Coord(0, l.cmp.sz.y));
	if(effs.isEmpty()) {
	    l.cmp.add(none.img, new Coord(10, l.cmp.sz.y));
	} else {
	    for(ItemInfo eff : effs)
		l.cmp.add(ItemInfo.longtip(Collections.singletonList(eff)), new Coord(10, l.cmp.sz.y));
	}
	l.cmp.add(Text.render("Duration: " + timefmt(time)).img, new Coord(10, l.cmp.sz.y));
    }

	public String getElixirStr(){
		// returns ";attr,name,val;heal,name,val[,repl,name];wound,name,val;time,val"
		// ingrs prepended later ("ingr,i1,i2" )
		String ret = "";
		for(ItemInfo eff : effs){
			if (eff instanceof AttrMod) {
				AttrMod am = ((AttrMod) eff);
				for (Entry entry : am.tab) {
					//TODO test new attrmod format
					ret += ";attr,"+entry.attr.name()+","+entry.fmtvalue();
				}
			}
			else if (eff instanceof HealWound){
				HealWound heal = ((HealWound) eff);
				ret += ";heal,"+heal.res.get().basename()+","+heal.a;
				if (heal.repl!=null && heal.repl.get()!=null && heal.res.get().basename()!=null){
					ret += ",repl,"+heal.repl.get().basename();
				}
			}
			else if (eff instanceof AddWound){
				AddWound wound = ((AddWound) eff);
				ret += ";wound,"+wound.res.get().basename()+","+wound.a;
			}
		}
		ret += ";time,"+time;
		return ret;
	}
}

package haven;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static haven.Glob.*;

public class ActWindow extends GameUI.Hidewnd {
    private static final int WIDTH = 200;

    private final ActList filtered;
    private final TextEntry filter;
    private final List<Pagina> all = new LinkedList<>();
    private final String category;
    private int pagseq = 0;

    public ActWindow(String cap, String category) {
	super(Coord.z, cap);
	this.category = category;
	filter = add(new TextEntry(WIDTH, "") {
	    @Override
	    public void activate(String text) {
		act(filtered.sel.pagina);
		ActWindow.this.hide();
	    }

	    @Override
	    protected void changed() {
		super.changed();
		filter();
	    }

	    @Override
	    public boolean keydown(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_UP) {
		    filtered.change(Math.max(filtered.selindex - 1, 0));
		    filtered.showsel();
		    return true;
		} else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
		    filtered.change((Math.min(filtered.selindex + 1, filtered.listitems() - 1)));
		    filtered.showsel();
		    return true;
		} else {
		    return super.keydown(e);
		}
	    }
	});
	setfocus(filter);
	filtered = add(new ActList(WIDTH, 10), new Coord(0, filter.sz.y + 5));
	filtered.bgcolor = new Color(0, 0, 0, 128);
	pack();
    }

    private void act(Pagina pagina) {
	ui.gui.menu.use(pagina, false);
    }

    @Override
    public void show() {
	super.show();
	filter.settext("");
	filtered.change(0);
	filtered.showsel();
	parent.setfocus(this);
    }

    @Override
    public void lostfocus() {
	super.lostfocus();
	hide();
    }

    private void filter() {
	String filter = this.filter.text.toLowerCase();
	synchronized (all) {
	    filtered.clear();
	    for (Pagina p : all) {
		Resource res = p.res.get();
		String name = res.layer(Resource.action).name.toLowerCase();
		if(name.contains(filter)) {
		    filtered.add(p);
		}
	    }
	}
	filtered.sort(new ItemComparator());
	if(filtered.listitems() > 0) {
	    filtered.change(Math.min(filtered.selindex, filtered.listitems() - 1));
	    filtered.sb.val = 0;
	    filtered.showsel();
	}
    }

    @Override
    public void tick(double dt) {
	super.tick(dt);

	if(pagseq != ui.sess.glob.pagseq) {
	    synchronized (ui.sess.glob.paginae) {
		synchronized (all) {
		    all.clear();
		    for (Pagina p : ui.sess.glob.paginae) {
			if(Pagina.name(p).contains(category)) {
			    all.add(p);
			}
		    }

		    pagseq = ui.sess.glob.pagseq;
		    filter();
		}
	    }
	}
    }

    private static class ItemComparator implements Comparator<ActList.ActItem> {
	@Override
	public int compare(ActList.ActItem a, ActList.ActItem b) {
	    return a.name.text.compareTo(b.name.text);
	}
    }
}

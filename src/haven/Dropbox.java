/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.Color;

public abstract class Dropbox<T> extends ListWidget<T> {
    public static final Tex drop = Resource.loadtex("gfx/hud/drop");
    public final int listh;
    private final Coord dropc;
    private Droplist dl;

    public Dropbox(int w, int listh, int itemh) {
	super(new Coord(w, itemh), itemh);
	this.listh = listh;
	dropc = new Coord(sz.x - drop.sz().x, 0);
    }

    private class Droplist extends Listbox<T> {
	private UI.Grab grab = null;

	private Droplist() {
	    super(Dropbox.this.sz.x, Math.min(listh, Dropbox.this.listitems()), Dropbox.this.itemh);
	    sel = Dropbox.this.sel;
	    Dropbox.this.ui.root.add(this, Dropbox.this.rootpos().add(0, Dropbox.this.sz.y));
	    grab = ui.grabmouse(this);
	    display();
	}

	protected T listitem(int i) {return(Dropbox.this.listitem(i));}
	protected int listitems() {return(Dropbox.this.listitems());}
	protected void drawitem(GOut g, T item, int idx) {Dropbox.this.drawitem(g, item, idx);}

	public boolean mousedown(MouseDownEvent ev) {
	    if(!ev.c.isect(Coord.z, sz)) {
		reqdestroy();
		return(false);
	    }
	    return(super.mousedown(ev));
	}

	public void destroy() {
	    grab.remove();
	    super.destroy();
	    dl = null;
	}

	public void change(T item) {
	    Dropbox.this.change(item);
	    reqdestroy();
	}
    }

    public void draw(GOut g) {
	g.chcolor(Color.BLACK);
	g.frect(Coord.z, sz);
	g.chcolor();
	if(sel != null)
	    drawitem(g.reclip(Coord.z, new Coord(sz.x - drop.sz().x, itemh)), sel, 0);
	g.image(drop, dropc);
	super.draw(g);
    }

    public boolean mousedown(MouseDownEvent ev) {
	if(super.mousedown(ev))
	    return(true);
	if((dl == null) && (ev.b == 1)) {
	    dl = new Droplist();
	    return(true);
	}
	return(super.mousedown(ev));
    }
}

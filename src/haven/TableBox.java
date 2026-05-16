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

import java.util.*;
import java.util.function.*;

public abstract class TableBox<I> extends Widget {
    public final List<Column> cols;
    public final MainList main;

    public TableBox(Coord sz) {
	super(sz);
	List<ColSpec<? super I>> spec = spec();
	this.cols = new ArrayList<>(spec.size());
	for(int i = 0; i < spec.size(); i++)
	    cols.add(new Column(spec.get(i)));
	widths();
	int h = headh();
	if(h > 0) {
	    for(Column col : cols) {
		Widget head = col.spec.heading(Coord.of(col.w, h));
		if(head != null)
		    col.head = adda(head, Coord.of(col.x + (int)Math.round(col.w * col.spec.halign()), h / 2), col.spec.halign(), 0.5);
	    }
	}
	main = add(makelist(sz.sub(0, h)), Coord.of(0, h));
    }

    protected abstract List<? extends I> items();
    protected abstract List<ColSpec<? super I>> spec();
    protected abstract int itemh();
    protected int headh() {return(0);}
    protected int colmarg() {return(1);}
    protected int rowmarg() {return(1);}
    protected MainList makelist(Coord sz) {return(new MainList(sz));}
    protected Row makeitem(I item, int idx, Coord sz) {return(new Row(item, idx, sz));}

    public static abstract class ColSpec<I> {
	public int fixw() {return(0);}
	public double flexw() {return(0);}
	public double align() {return(0);}
	public double halign() {return(0.5);}
	public abstract Widget heading(Coord sz);
	public abstract Widget makecell(I item, int idx, Coord sz);

	public static <T> ColSpec<T> of(int fixw, double flexw, double align, double halign,
					Function<? super Coord, ? extends Widget> heading,
					SListWidget.ItemFactory<T, ? extends Widget> makecell) {
	    return(new ColSpec<T>() {
		public int fixw() {return(fixw);}
		public double flexw() {return(flexw);}
		public double align() {return(align);}
		public double halign() {return(halign);}
		public Widget heading(Coord sz) {return(heading.apply(sz));}
		public Widget makecell(T item, int idx, Coord sz) {return(makecell.makeitem(item, idx, sz));}
	    });
	}

	public static <T> ColSpec<T> of(int fixw, double flexw, double align, double halign, Tex heading,
					SListWidget.ItemFactory<T, ? extends Widget> makecell) {
	    return(of(fixw, flexw, align, halign, sz -> new Img(heading), makecell));
	}
    }

    public class Column {
	public final ColSpec<? super I> spec;
	public Widget head;
	public int x, w;

	protected Column(ColSpec<? super I> spec) {
	    this.spec = spec;
	}
    }

    public class Row extends Widget {
	protected Row(I item, int idx, Coord sz) {
	    super(sz);
	    for(Column col : cols) {
		Widget colw = col.spec.makecell(item, idx, Coord.of(col.w, sz.y));
		if(colw != null)
		    adda(colw, Coord.of(col.x + (int)Math.round(col.w * col.spec.align()), sz.y), col.spec.align(), 1.0);
	    }
	}
    }

    public class MainList extends SListBox<I, Row> {
	protected MainList(Coord sz) {
	    super(sz, TableBox.this.itemh(), TableBox.this.rowmarg());
	}

	public List<? extends I> items() {
	    return(TableBox.this.items());
	}

	public Row makeitem(I item, int idx, Coord sz) {
	    return(TableBox.this.makeitem(item, idx, sz));
	}
    }

    private void widths() {
	List<ColSpec<? super I>> spec = spec();
	int n = spec.size(), m = colmarg();;
	int fw = 0;
	double ws = 0;
	for(int i = 0; i < n; i++) {
	    fw += spec.get(i).fixw();
	    ws += spec.get(i).flexw();
	}
	fw += (n - 1) * m;
	int rw = sz.x - fw;
	if(ws == 0) ws = 1;
	double cw = 0;
	for(int i = 0, x = 0; i < n; i++) {
	    Column c = cols.get(i);
	    ColSpec s = spec.get(i);
	    c.x = x;
	    c.w = s.fixw() + (int)(Math.round((cw + s.flexw()) * rw / ws) - Math.round(cw * rw / ws));
	    cw += s.flexw();
	    x += c.w + m;
	}
    }

    protected void drawgrid(GOut g) {
	g.chcolor(255, 255, 0, 64);
	for(int i = 0; i < cols.size() - 1; i++) {
	    int x = cols.get(i).x + cols.get(i).w;
	    g.line(new Coord(x, 0), new Coord(x, sz.y), 1);
	}
	g.chcolor();
    }

    public void draw(GOut g) {
	drawgrid(g);
	super.draw(g);
    }
}

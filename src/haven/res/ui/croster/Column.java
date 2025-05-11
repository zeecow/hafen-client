/* Preprocessed source code */
package haven.res.ui.croster;

import haven.*;

import java.util.Comparator;

@haven.FromResource(name = "ui/croster", version = 75)
public class Column <E extends Entry> {
    public final Tex head;
    public final String tip;
    public final Comparator<? super E> order;
    public int w, x;
    public boolean r;

    public Column(String name, Comparator<? super E> order, int w) {
	this.head = CharWnd.attrf.render(name).tex();
	this.tip = null;
	this.order = order;
	this.w = UI.scale(w);
    }

    public Column(Indir<Resource> head, Comparator<? super E> order, int w) {
	Resource hres = Loading.waitfor(() -> head.get());
	Resource.Tooltip tip = hres.layer(Resource.tooltip);
	this.head = hres.layer(Resource.imgc).tex();
	this.tip = (tip == null) ? null : tip.t;
	this.order = order;
	this.w = UI.scale(w);
    }

    public Column(Indir<Resource> head, Comparator<? super E> order) {
	this(head, order, 50);
    }

    public Tex head() {
	return(head);
    }

    public Column<E> runon() {
	r = true;
	return(this);
    }

    public boolean hasx(int x) {
	return((x >= this.x) && (x < this.x + this.w));
    }
}

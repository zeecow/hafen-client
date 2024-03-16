/* Preprocessed source code */
package haven.res.ui.obj.buddy;

import haven.*;

import java.awt.*;

@haven.FromResource(name = "ui/obj/buddy", version = 2)
public class Buddy extends GAttrib implements InfoPart {
    public final int id;
    public final Info info;
    private int bseq = -1;
    private BuddyWnd bw = null;
    private BuddyWnd.Buddy b = null;
    private int rgrp;
    private String rnm;

    public Buddy(Gob gob, int id) {
	super(gob);
	this.id = id;
	info = Info.add(gob, this);
    }

    public static void parse(Gob gob, Message dat) {
	int fl = dat.uint8();
	if((fl & 1) != 0 && ZeeConfig.showKinNames)
	    gob.setattr(new Buddy(gob, dat.int32()));
	else
	    gob.delattr(Buddy.class);
    }

    public void dispose() {
	super.dispose();
	info.remove(this);
    }

    public BuddyWnd.Buddy buddy() {
	return(b);
    }

    public void draw(CompImage cmp, RenderContext ctx) {
	BuddyWnd.Buddy b = null;
	if(bw == null) {
	    if(ctx instanceof PView.WidgetContext) {
		GameUI gui = ((PView.WidgetContext)ctx).widget().getparent(GameUI.class);
		if(gui != null) {
		    if(gui.buddies == null)
			throw(new Loading());
		    bw = gui.buddies;
		}
	    }
	}
	if(bw != null)
	    b = bw.find(id);
	if(b != null) {
	    Color col = BuddyWnd.gc[rgrp = b.group];
	    cmp.add(Utils.outline2(Text.std.render(rnm = b.name, col).img, Utils.contrast(col)), Coord.z);
	}
	this.b = b;
    }

    public void ctick(double dt) {
	super.ctick(dt);
	if((bw != null) && (bw.serial != bseq)) {
	    bseq = bw.serial;
	    if((bw.find(id) != b) || (rnm != b.name) || (rgrp != b.group))
		info.dirty();
	}
    }

    public boolean auto() {return(true);}
    public int order() {return(-10);}
}

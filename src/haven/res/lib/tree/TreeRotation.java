/* Preprocessed source code */
/* $use: lib/globfx */
/* $use: lib/leaves */
/* $use: lib/svaj */

package haven.res.lib.tree;

import haven.GAttrib;
import haven.Gob;
import haven.render.Location;
import haven.render.Pipe;

@haven.FromResource(name = "lib/tree", version = 15)
public class TreeRotation extends GAttrib implements Gob.SetupMod {
    public final Location rot;

    public TreeRotation(Gob gob, Location rot) {
	super(gob);
	this.rot = rot;
    }

    public Pipe.Op placestate() {
	return(rot);
    }
}

/* Preprocessed source code */
package haven.res.lib.layspr;

import haven.Coord;
import haven.GOut;

@haven.FromResource(name = "lib/layspr", version = 16)
abstract class Layer {
    final int z;
    final Coord sz;

    Layer(int z, Coord sz) {
	this.z = z;
	this.sz = sz;
    }

    abstract void draw(GOut g);
}

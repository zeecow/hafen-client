/* Preprocessed source code */
package haven.res.lib.layspr;

import haven.Coord;
import haven.GOut;
import haven.Resource;

@haven.FromResource(name = "lib/layspr", version = 16)
class Image extends Layer {
    final Resource.Image img;

    Image(Resource.Image img) {
	super(img.z, img.ssz);
	this.img = img;
    }

    void draw(GOut g) {
	g.image(img, Coord.z);
    }
}

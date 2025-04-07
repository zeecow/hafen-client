/* Preprocessed source code */
package haven.res.ui.tt.attrmod;

import haven.Resource;

import java.awt.image.BufferedImage;

@Resource.PublishedCode(name = "attrmod")
@haven.FromResource(name = "ui/tt/attrmod", version = 12)
public abstract class resattr implements Attribute {
    public final Resource res;

    public resattr(Resource res) {
	this.res = res;
    }

    public BufferedImage icon() {
	Resource.Image img = res.layer(Resource.imgc);
	return((img == null) ? null : img.img);
    }

    public String name() {
	return(res.flayer(Resource.tooltip).t);
    }
}

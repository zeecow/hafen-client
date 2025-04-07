/* Preprocessed source code */
package haven.res.ui.tt.attrmod;

import haven.Resource;

@Resource.PublishedCode(name = "attrmod")
@haven.FromResource(name = "ui/tt/attrmod", version = 12)
public class Mod extends Entry {
    public final double mod;

    public Mod(Attribute attr, double mod) {
	super(attr);
	this.mod = mod;
    }

    public String fmtvalue() {
	return(attr.format(mod));
    }
}

/* >tt: AttrMod$Fac */

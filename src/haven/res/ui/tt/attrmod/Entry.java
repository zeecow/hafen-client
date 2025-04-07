/* Preprocessed source code */
package haven.res.ui.tt.attrmod;

import haven.Resource;

@Resource.PublishedCode(name = "attrmod")
@haven.FromResource(name = "ui/tt/attrmod", version = 12)
public abstract class Entry {
    public final Attribute attr;

    public Entry(Attribute attr) {
	this.attr = attr;
    }

    public abstract String fmtvalue();
}

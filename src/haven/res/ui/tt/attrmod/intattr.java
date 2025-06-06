/* Preprocessed source code */
package haven.res.ui.tt.attrmod;

import haven.Resource;
import haven.RichText;

@Resource.PublishedCode(name = "attrmod")
@haven.FromResource(name = "ui/tt/attrmod", version = 12)
public class intattr extends resattr {
    public intattr(Resource res) {
	super(res);
    }

    public String format(double val) {
	return(String.format("%s{%s%d}",
			     RichText.Parser.col2a((val < 0) ? debuff : buff),
			     (val < 0) ? "-" : "+", Math.abs((int)val)));
    }
}

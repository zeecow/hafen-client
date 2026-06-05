/* Preprocessed source code */
package haven.res.ui.tt.substprog;

import haven.*;
import java.awt.image.*;

/* >tt: Progress */
@haven.FromResource(name = "ui/tt/substprog", version = 1)
public class Progress extends ItemInfo.Tip {
    public final String desc;
    public final double done;

    public Progress(Owner owner, String desc, double done) {
	super(owner);
	this.desc = desc;
	this.done = done;
    }

    public static Progress mkinfo(Owner owner, Object... args) {
	double done = Utils.dv(args[1]);
	String desc = (args.length > 2) ? Utils.sv(args[2]) : "Progress";
	return(new Progress(owner, desc, done));
    }

    public BufferedImage tipimg() {
	return(RichText.render(String.format("%s: $col[192,128,255]{%d%%}", RichText.Parser.quote(desc), (int)Math.floor(done * 100)), 0).img);
    }
}

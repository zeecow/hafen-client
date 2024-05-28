/* Preprocessed source code */
package haven.res.ui.obj.buddy;

import haven.CompImage;
import haven.RenderContext;
import haven.Text;
import haven.UI;

import java.awt.*;
import java.awt.image.BufferedImage;

import static haven.PUtils.blurmask2;
import static haven.PUtils.rasterimg;

@haven.FromResource(name = "ui/obj/buddy", version = 4)
public interface InfoPart {
    public static final Text.Foundry fnd = new Text.Foundry(Text.sans.deriveFont(Font.BOLD, UI.scale(12f))).aa(true);

    public void draw(CompImage cmp, RenderContext ctx);
    public default int order() {return(0);}
    public default boolean auto() {return(false);}

    public static BufferedImage rendertext(String str, Color col) {
	return(rasterimg(blurmask2(fnd.render(str, col).img.getRaster(), UI.rscale(1.0), UI.rscale(1.0), Color.BLACK)));
    }
}

/* >objdelta: Buddy */

/* Preprocessed source code */
package haven.res.gfx.fx.floatimg;

import haven.*;

import java.awt.*;
import java.awt.image.BufferedImage;

import static haven.PUtils.blurmask2;
import static haven.PUtils.rasterimg;

@haven.FromResource(name = "gfx/fx/floatimg", version = 6)
public class FloatText extends FloatSprite {
    public static final Text.Foundry fnd = new Text.Foundry(Text.sans.deriveFont(Font.BOLD, UI.scale(12f))).aa(true);

    public static BufferedImage render(String str, Color col) {
	Color col2 = Utils.contrast(col);
	return(rasterimg(blurmask2(fnd.render(str, col).img.getRaster(), UI.rscale(1.0), UI.rscale(1.0), Color.BLACK)));
    }

    public FloatText(Owner owner, Resource res, String str, Color col) {
	super(owner, res, new TexI(render(str, col)), 2);
    ZeeFont.checkDmgHpMaybe(owner,res,str,col);
    }
}

/* >spr: Score */

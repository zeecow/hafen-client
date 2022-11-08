/* Preprocessed source code */
package haven.res.gfx.fx.floatimg;

import haven.Resource;
import haven.TexI;
import haven.Text;
import haven.Utils;

import java.awt.*;

@haven.FromResource(name = "gfx/fx/floatimg", version = 3)
public class FloatText extends FloatSprite {
    public static final Text.Foundry fnd = new Text.Foundry(Text.serif, 16);
    
    public FloatText(Owner owner, Resource res, String str, Color col) {
	super(owner, res, new TexI(Utils.outline2(fnd.render(str, col).img, Utils.contrast(col))), 2);
    }
}

/* >spr: Score */

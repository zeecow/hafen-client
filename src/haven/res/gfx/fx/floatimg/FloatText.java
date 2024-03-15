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
        checkDmgHpMaybe(owner,res,str,col);
    }

    static final Text.Foundry defDmgFont = new Text.Foundry(Text.sans.deriveFont(Font.PLAIN, UI.scale(14))).aa(true);
    private void checkDmgHpMaybe(Owner owner, Resource res, String str, Color col) {
        try {
            //ZeeConfig.println(owner.getClass().getName() + " , " + res + " , "+str+" , "+col);
            if (owner instanceof Gob) {
                if (res!=null && res.name!=null && res.name.contentEquals("gfx/fx/floatimg")) {
                    if (ZeeConfig.isNumbersOnly(str)) {
                        if (col!=null && col.equals(Color.red)) {
                            Gob gob = (Gob) owner;
                            gob.totalDmgHp += Integer.parseInt(str);
                            String txt = "-" + gob.totalDmgHp;
                            ZeeGobText zeeGobText = new ZeeGobText(txt,Color.red,Color.black,5, defDmgFont);
                            ZeeConfig.addGobText(gob, zeeGobText);
                        }
                    }
                    //else ZeeConfig.println("not number = " + str);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

/* >spr: Score */

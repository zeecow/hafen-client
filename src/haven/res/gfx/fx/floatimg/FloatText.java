/* Preprocessed source code */
package haven.res.gfx.fx.floatimg;

import haven.*;

import java.awt.*;

@haven.FromResource(name = "gfx/fx/floatimg", version = 3)
public class FloatText extends FloatSprite {
    public static final Text.Foundry fnd = new Text.Foundry(Text.serif, 16);
    
    public FloatText(Owner owner, Resource res, String str, Color col) {
	    super(owner, res, new TexI(Utils.outline2(fnd.render(str, col).img, Utils.contrast(col))), 2);
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

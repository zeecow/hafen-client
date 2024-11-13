package haven;

import java.awt.*;

public class ZeeFont {

    public static final Text.Foundry TXTFND_PROGRESS_WIDGET = new Text.Foundry(Text.sans.deriveFont(Font.BOLD, UI.scale(15f))).aa(false);
    public static final Text.Foundry TXTFND_TOTAL_DAMAGE = new Text.Foundry(Text.sans.deriveFont(Font.PLAIN, UI.scale(16))).aa(false);
    public static final Text.Foundry TXTFND_GOB_TEXT = new Text.Foundry(Text.sans.deriveFont(Font.PLAIN, UI.scale(11))).aa(false);

    public static void checkDmgHpMaybe(Sprite.Owner owner, Resource res, String str, Color col) {
        if (!(owner instanceof Gob))
            return;
        if (!ZeeConfig.isNumbersOnly(str))
            return;
        if (col==null || !col.equals(Color.red))
            return;
        try {
            Gob gob = (Gob) owner;
            gob.totalDmgHp += Integer.parseInt(str);
            String txt = "-" + gob.totalDmgHp;
            ZeeGobText zeeGobText = new ZeeGobText(txt,Color.yellow,Color.black,5, TXTFND_TOTAL_DAMAGE);
            ZeeConfig.addGobText(gob, zeeGobText);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

package haven;

import java.awt.*;

public class ZeeGobFind extends GAttrib implements Gob.SetupMod{

    public static final String TRIANGLE = "▼";

    public ZeeGobFind(Gob gob) {
        this( gob,
            Color.magenta,
            new ZeeGobText(TRIANGLE, Color.green, Color.black, 5, ZeeFont.TXTFND_GOB_TEXT)
        );
    }

    public ZeeGobFind(Gob gob, Color c, ZeeGobText gobText) {
        super(gob);
        ZeeConfig.addGobColor(gob,c);
        ZeeConfig.addGobText(gob,gobText);
    }

    @Override
    public void dispose() {
        super.dispose();
        ZeeConfig.removeGobColor(this.gob);
        ZeeConfig.removeGobText(this.gob);
    }
}

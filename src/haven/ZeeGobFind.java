package haven;

import java.awt.*;

public class ZeeGobFind extends GAttrib implements Gob.SetupMod{

    public ZeeGobFind(Gob gob) {
        super(gob);
        ZeeConfig.addGobColor(gob,Color.magenta);
        ZeeConfig.addGobText(gob,"▼");
    }

    @Override
    public void dispose() {
        super.dispose();
        ZeeConfig.removeGobColor(this.gob);
        ZeeConfig.removeGobText(this.gob);
    }
}

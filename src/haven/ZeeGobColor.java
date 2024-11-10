package haven;

import haven.render.MixColor;
import haven.render.Pipe;

import java.awt.*;

public class ZeeGobColor extends GAttrib implements Gob.SetupMod {
    static final Color COLOR_DARK_GREEN = new Color(30, 101, 0);
    public Color color;

    public ZeeGobColor(Gob g, Color color) {
        super(g);
        this.color = color;
    }

    public Pipe.Op placestate() {
        if (color==null) {
            ZeeConfig.println(this.gob.getres().name+"  "+ZeeConsole.isGobFindActive);
            if (ZeeConsole.gobFindRegex!=null)
                ZeeConfig.println("   gobfind list "+ZeeConsole.gobFindRegex.size());
            return Pipe.Op.nil;
        }
        return new MixColor(color);
    }

}

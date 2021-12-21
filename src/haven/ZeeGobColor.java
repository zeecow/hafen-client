package haven;

import haven.render.MixColor;
import haven.render.Pipe;

public class ZeeGobColor extends GAttrib implements Gob.SetupMod {
    private MixColor color;

    public ZeeGobColor(Gob g, MixColor color) {
        super(g);
        this.color = color;
    }

    public Pipe.Op placestate() {
        return color;
    }

}

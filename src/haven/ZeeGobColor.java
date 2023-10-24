package haven;

import haven.render.MixColor;
import haven.render.Pipe;

import java.awt.*;

public class ZeeGobColor extends GAttrib implements Gob.SetupMod {
    public Color color;

    public ZeeGobColor(Gob g, Color color) {
        super(g);
        this.color = color;
    }

    public Pipe.Op placestate() {
        return new MixColor(color);
    }

}

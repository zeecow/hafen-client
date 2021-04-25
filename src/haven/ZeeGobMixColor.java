package haven;

import haven.render.MixColor;
import haven.render.Pipe;

public class ZeeGobMixColor extends GAttrib implements Gob.SetupMod {
    public final MixColor fx;

    public ZeeGobMixColor(Gob g, MixColor c)
    {
        super(g);
        this.fx = c;
    }

    public Pipe.Op gobstate() {
        return(fx);
    }
}
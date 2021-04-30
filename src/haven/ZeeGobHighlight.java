package haven;

import haven.render.MixColor;
import haven.render.Pipe;

public class ZeeGobHighlight extends GAttrib implements Gob.SetupMod {
    public final MixColor fx;

    public ZeeGobHighlight(Gob g, MixColor c)
    {
        super(g);
        this.fx = c;
    }

    public Pipe.Op gobstate() {
        return(ZeeConfig.getHighlightColor(this.gob));
    }
}
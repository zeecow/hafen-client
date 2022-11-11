/* Preprocessed source code */
package haven.res.ui.tt.q.quality;

/* $use: ui/tt/q/qbuff */
import haven.*;
import haven.res.ui.tt.q.qbuff.QBuff;

import java.awt.*;
import java.awt.image.BufferedImage;

/* >tt: Quality */
@haven.FromResource(name = "ui/tt/q/quality", version = 25)
public class Quality extends QBuff implements GItem.OverlayInfo<Tex> {
    public static boolean show = Utils.getprefb("qtoggle", true);
    static final BufferedImage fakeicon = ZeeManagerIcons.imgDiamond(5,Color.cyan,false,false);

    public Quality(Owner owner, double q) {
        super(owner, fakeicon, "Quality", q);
	    //super(owner, Resource.classres(Quality.class).layer(Resource.imgc, 0).scaled(), "Quality", q);
    }

    public static ItemInfo mkinfo(Owner owner, Object... args) {
	return(new Quality(owner, ((Number)args[1]).doubleValue()));
    }

    public Tex overlay() {
        return GItem.NumberInfo.font.renderstroked(String.valueOf(Math.round(q)), Color.cyan, Color.black).tex();
	    //return(new TexI(GItem.NumberInfo.numrender((int)Math.round(q), new Color(192, 192, 255, 255))));
    }

    public void drawoverlay(GOut g, Tex ol) {
	if(show)
	    g.aimage(ol, new Coord(g.sz().x, 0), 1, 0);
    }
}

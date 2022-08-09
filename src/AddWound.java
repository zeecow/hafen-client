/* Preprocessed source code */
import haven.*;
import java.awt.image.BufferedImage;

/* >tt: AddWound */
@haven.FromResource(name = "ui/tt/alch/hurt", version = 1)
public class AddWound extends ItemInfo.Tip {
    public final Indir<Resource> res;
    public final int a;

    public AddWound(Owner owner, Indir<Resource> res, int a) {
	super(owner);
	this.res = res;
	this.a = a;
    }

    public static ItemInfo mkinfo(Owner owner, Raw raw, Object... args) {
	Indir<Resource> res = owner.context(Resource.Resolver.class).getres((Integer)args[1]);
	int a = ((Number)args[2]).intValue();
	return(new AddWound(owner, res, a));
    }

    public BufferedImage tipimg() {
	BufferedImage t1 = Text.render(String.format("Causes %d points of ", this.a)).img;
	BufferedImage t2 = Text.render(res.get().layer(Resource.tooltip).t).img;
	int h = t1.getHeight();
	BufferedImage icon = PUtils.convolvedown(res.get().layer(Resource.imgc).img, new Coord(h, h), CharWnd.iconfilter);
	return(catimgsh(0, t1, icon, t2));
    }
}

/* Preprocessed source code */
import haven.*;
import java.awt.image.BufferedImage;

/* >tt: HealWound */
@haven.FromResource(name = "ui/tt/alch/heal", version = 4)
public class HealWound extends ItemInfo.Tip {
    public final Indir<Resource> res, repl;
    public final int a;

    public HealWound(Owner owner, Indir<Resource> res, Indir<Resource> repl, int a) {
	super(owner);
	this.res = res;
	this.repl = repl;
	this.a = a;
    }

    public static ItemInfo mkinfo(Owner owner, Raw raw, Object... args) {
	Indir<Resource> res = owner.context(Resource.Resolver.class).getres((Integer)args[1]);
	int a = ((Number)args[2]).intValue();
	Indir<Resource> repl = null;
	if(args.length > 3)
	    repl = owner.context(Resource.Resolver.class).getres((Integer)args[3]);
	return(new HealWound(owner, res, repl, a));
    }

    public BufferedImage tipimg() {
	BufferedImage t1 = Text.render(String.format("Heal %d points of ", this.a)).img;
	BufferedImage t2 = Text.render(res.get().layer(Resource.tooltip).t).img;
	int h = t1.getHeight();
	BufferedImage icon = PUtils.convolvedown(res.get().layer(Resource.imgc).img, new Coord(h, h), CharWnd.iconfilter);
	BufferedImage ret = catimgsh(0, t1, icon, t2);
	if(repl != null) {
	    ret = catimgsh(0, ret,
			   Text.render(" into ").img,
			   PUtils.convolvedown(repl.get().layer(Resource.imgc).img, new Coord(h, h), CharWnd.iconfilter),
			   Text.render(repl.get().layer(Resource.tooltip).t).img);
	}
	return(ret);
    }
}

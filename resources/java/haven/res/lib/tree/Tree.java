//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package haven.res.lib.tree;

import haven.*;
import haven.MCache.Grid;

import java.util.Random;

public class Tree extends StaticSprite {
    private final Location scale;
    private Location rot;
    public final float fscale;
    private boolean initialized = false;
    
    public Tree(Owner owner, Resource res, float fscale, Message msg) {
	super(owner, res, msg);
	
	initRot();
	this.fscale = fscale;
	if(fscale == 1.0F) {
	    this.scale = null;
	} else {
	    this.scale = mkscale(fscale);
	}
    }
    
    private void initRot() {
	try {
	    if(owner instanceof Gob) {
		this.rot = rndrot(randoom((Gob) owner));
	    } else {
		this.rot = null;
	    }
	    initialized = true;
	} catch (Loading ignored) {}
    }
    
    private static Message invert(Message msg) {
	int var1 = 0;
	
	int var2;
	for (var2 = 0; !msg.eom(); var2 += 8) {
	    var1 |= msg.uint8() << var2;
	}
	
	var2 = -1 & ~var1;
	MessageBuf var3 = new MessageBuf();
	var3.addint32(var2);
	return new MessageBuf(var3.fin());
    }
    
    public Tree(Owner var1, Resource var2, Message var3) {
	this(var1, var2, var3.eom() ? 1.0F : (float) var3.uint8() / 100.0F, invert(var3));
    }
    
    public static Location mkscale(float var0, float var1, float var2) {
	return new Location(new Matrix4f(var0, 0.0F, 0.0F, 0.0F, 0.0F, var1, 0.0F, 0.0F, 0.0F, 0.0F, var2, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F));
    }
    
    public static Location mkscale(float var0) {
	return mkscale(var0, var0, var0);
    }
    
    public static Random randoom(Gob gob) {
	Coord c = gob.rc.floor(MCache.tilesz);
	Grid grid = gob.glob.map.getgridt(c);
	c = c.sub(grid.ul);
	Random random = new Random(grid.id);
	random.setSeed(random.nextLong() ^ (long) c.x);
	random.setSeed(random.nextLong() ^ (long) c.y);
	return random;
    }
    
    public static Location rndrot(Random var0) {
	double var1 = var0.nextDouble() * 3.141592653589793D * 2.0D;
	double var3 = var0.nextGaussian() * 3.141592653589793D / 64.0D;
	Coord3f var5 = new Coord3f((float) Math.sin(var1), (float) Math.cos(var1), 0.0F);
	return Location.rot(var5, (float) var3);
    }
    
    public boolean setup(RenderList var1) {
	if(!initialized) {initRot();}
	if(this.rot != null) {
	    var1.prepc(this.rot);
	}
	
	if(this.scale != null) {
	    var1.prepc(this.scale);
	    var1.prepc(States.normalize);
	}
	
	return super.setup(var1);
    }
}

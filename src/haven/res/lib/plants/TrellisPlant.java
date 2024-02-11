/* Preprocessed source code */
package haven.res.lib.plants;

import haven.*;
import haven.resutil.*;
import java.util.*;

@haven.FromResource(name = "lib/plants", version = 10)
public class TrellisPlant implements Sprite.Factory {
    public final int num;

    public TrellisPlant(int num) {
		if (ZeeConfig.simpleCrops)
			this.num = 1;
		else
			this.num = num;
    }

    public TrellisPlant() {
	this(2);
    }

    public TrellisPlant(Object[] args) {
	this(((Number)args[0]).intValue());
    }

    public Sprite create(Sprite.Owner owner, Resource res, Message sdt) {
	double a = ((owner instanceof Gob) ? (Gob)owner : owner.context(Gob.class)).a;
	float ac = (float)Math.cos(a), as = -(float)Math.sin(a);
	int st = sdt.uint8();
	ArrayList<FastMesh.MeshRes> var = new ArrayList<FastMesh.MeshRes>();
	for(FastMesh.MeshRes mr : res.layers(FastMesh.MeshRes.class)) {
	    if((mr.id / 10) == st)
		var.add(mr);
	}
	if(var.size() < 1)
	    throw(new Sprite.ResourceException("No variants for grow stage " + st, res));
	Random rnd = owner.mkrandoom();
	CSprite spr = new CSprite(owner, res);
	float d = 11f / num;
	float c = -5.5f + (d / 2);
	for(int i = 0; i < num; i++) {
	    FastMesh.MeshRes v = var.get(rnd.nextInt(var.size()));
	    spr.addpart(c * as, c * ac, v.mat.get(), v.m);
	    c += d;
	}
	return(spr);
    }
}

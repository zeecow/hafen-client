package haven;

import javax.media.opengl.GL2;
import java.awt.*;
import java.nio.FloatBuffer;
import java.util.Collection;

public class Hitbox extends Sprite {
    FloatBuffer buff;
    Pair<Coord, Coord> hitbox;
    private GLState SOLID = new States.ColState(new Color(180, 134, 255, 200));
    private GLState PASSABLE = new States.ColState(new Color(105, 207, 124, 200));
    private boolean passable = false;

    protected Hitbox(Gob gob) {
	super(gob, null);


	hitbox = getBounds(gob);
	if(hitbox != null) {
	    Coord a = hitbox.a;
	    Coord b = hitbox.b;

	    buff = Utils.mkfbuf(3 * 4);
	    buff.put(a.x).put(-a.y).put(1);
	    buff.put(a.x).put(-b.y).put(1);
	    buff.put(b.x).put(-b.y).put(1);
	    buff.put(b.x).put(-a.y).put(1);
	}
    }

    @Override
    public boolean setup(RenderList rl) {
	if(hitbox == null) {
	    return false;
	}
	passable = passable();
	rl.prepo(passable ? PASSABLE : SOLID);
	rl.prepo(States.xray);
	return true;
    }

    public void draw(GOut g) {
	buff.rewind();
	g.apply();
	BGL gl = g.gl;
	gl.glLineWidth(passable ? 1 : 2);
	gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
	gl.glVertexPointer(3, GL2.GL_FLOAT, 0, buff);
	gl.glDrawArrays(GL2.GL_LINE_LOOP, 0, 4);
	gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
    }

    private boolean passable() {
	try {
	    Gob gob = (Gob) owner;
	    Resource res = gob.getres();
	    String name = res.name;

	    ResDrawable rd = gob.getattr(ResDrawable.class);
	    if(rd != null) {
		MessageBuf sdt = rd.sdt.clone();
		int state = sdt.eom() ? 0xffff0000 : decnum(sdt);
		if(name.endsWith("gate") && name.startsWith("gfx/terobjs/arch")) {//gates
		    if(state == 1) { // gate is open
			return true;
		    }
		} else if(name.endsWith("/pow")) {//fire
		    if(state == 17 || state == 33) { // this fire is actually hearth fire
			return true;
		    }
		}
	    }
	} catch (Loading ignored) {}
	return false;
    }

    public static Pair<Coord, Coord> getBounds(Gob gob) {
	Resource res = gob.getres();
	if(res == null) {throw new Loading();}

	Collection<RenderLink.Res> links = res.layers(RenderLink.Res.class);
	for (RenderLink.Res link : links) {
	    if(link.mesh != null) {
		Resource.Neg neg = link.mesh.get().layer(Resource.Neg.class);
		if(neg != null) {
		    return new Pair<>(neg.ac, neg.bc);
		}
	    }
	}

	Resource.Neg neg = res.layer(Resource.Neg.class);
	if(neg == null) {
	    return null;
	}

	return new Pair<>(neg.ac, neg.bc);
    }

}

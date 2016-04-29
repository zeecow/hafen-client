package haven;

import java.awt.*;

public class GobInfo extends PView.Draw2D {
    public boolean ready = false;
    private Tex tex;
    private final Gob gob;
    private GLState.Buffer state;

    @Override
    public Object staticp() {
	return Rendered.CONSTANS;
    }

    protected GobInfo(Gob owner) {
	this.gob = owner;
    }

    @Override
    public void draw2d(GOut g) {
	if(tex != null) {
	    Coord sc = null;
	    if(state != null) {sc = getSC();}
	    if(sc != null && sc.isect(Coord.z, g.sz)) {
		g.aimage(tex, sc, 0.5, 0.5);
	    }
	}
    }

    private Coord getSC() {
	Matrix4f cam = new Matrix4f(),
	    wxf = new Matrix4f(),
	    mv = new Matrix4f();

	Camera camera = state.get(PView.cam);
	///Location.Chain loc = state.get(PView.loc);
	Coord3f cc = gob.getc();
	Coord3f pc = new Coord3f(cc.x, -cc.y, cc.z);
	Location loc = new Location(Transform.makexlate(new Matrix4f(), pc));
	Projection proj = state.get(PView.proj);
	PView.RenderState wnd = state.get(PView.wnd);
	if(camera == null || loc == null || proj == null || wnd == null) {
	    return null;
	}
	try {
	    mv.load(cam.load(camera.fin(Matrix4f.id))).mul1(wxf.load(loc.fin(Matrix4f.id)));
	    Coord3f s = proj.toscreen(mv.mul4(Coord3f.o), wnd.sz());
	    Coord3f sczu = proj.toscreen(mv.mul4(Coord3f.zu), wnd.sz()).sub(s);
	    return new Coord(s).add(new Coord(sczu));
	} catch (RuntimeException ignored) {}
	return null;
    }

    @Override
    public boolean setup(RenderList d) {
	state = d.state();
	if(!ready) {
	    try {
		tex = render();
		ready = true;
	    } catch (Loading ignored) { } catch (Exception e) {
		tex = null;
		ready = true;
	    }
	}
	return ready && tex != null;
    }

    private Tex render() {
	if(gob == null || gob.getres() == null) { return null;}
	Text.Line line = null;

	if(isSpriteKind("GrowingPlant", gob) || isSpriteKind("TrellisPlant", gob)) {
	    int maxStage = 0;
	    for (FastMesh.MeshRes layer : gob.getres().layers(FastMesh.MeshRes.class)) {
		if(layer.id / 10 > maxStage) {
		    maxStage = layer.id / 10;
		}
	    }
	    Message data = getDrawableData(gob);
	    if(data != null) {
		int stage = data.uint8();
		if(stage > maxStage) {stage = maxStage;}
		Color c = Utils.blendcol((double) stage / maxStage, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN);
		line = Text.std.renderstroked(String.format("%d/%d", stage, maxStage), c, Color.BLACK);
	    }
	} else if(isSpriteKind("Tree", gob)) {
	    Message data = getDrawableData(gob);
	    if(data != null && !data.eom()) {
		int growth = data.uint8();
		if(growth < 100) {
		    Color c = Utils.blendcol(growth / 100.0, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN);
		    line = Text.std.renderstroked(String.format("%d%%", growth), c, Color.BLACK);
		}
	    }
	}

	if(line != null) {
	    return line.tex();
	}

	return null;
    }

    private static Message getDrawableData(Gob gob) {
	Drawable dr = gob.getattr(Drawable.class);
	ResDrawable d = (dr instanceof ResDrawable) ? (ResDrawable) dr : null;
	if(d != null)
	    return d.sdt.clone();
	else
	    return null;
    }

    private static boolean isSpriteKind(String kind, Gob gob) {
	Resource.CodeEntry ce = gob.getres().layer(Resource.CodeEntry.class);
	if(ce != null) {
	    Class spc = ce.get("spr");
	    return spc != null && (spc.getSimpleName().equals(kind) || spc.getSuperclass().getSimpleName().equals(kind));
	} else {
	    return false;
	}
    }
}
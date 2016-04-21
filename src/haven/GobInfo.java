package haven;

import java.awt.*;

public class GobInfo extends Sprite {
    private boolean ready = false;
    private Rendered rendered;

    protected GobInfo(Gob owner) {
	super(owner, null);
    }

    @Override
    public boolean setup(RenderList d) {
	if(CFG.DISPLAY_GOB_INFO.get()) {
	    if(!ready) {
		try {
		    rendered = render();
		    ready = true;
		} catch (Loading ignored) { } catch (Exception e) {
		    rendered = null;
		    ready = true;
		}
	    }
	    if(ready && rendered != null) {
		d.add(rendered, null);
		return true;
	    }
	}
	return false;
    }

    private GobInfoTex render() {
	Gob gob = (Gob) owner;
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
	    return new GobInfoTex(gob, line.tex());
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

    private static class GobInfoTex extends PView.Draw2D {
	private Tex tex;
	private Gob gob;

	public GobInfoTex(Gob gob, Tex tex) {
	    this.gob = gob;
	    this.tex = tex;
	}

	@Override
	public void draw2d(GOut g) {
	    if(tex != null && gob.sc != null) {
		Coord sc = gob.sc.add(new Coord(gob.sczu.mul(1)));
		if(sc.isect(Coord.z, g.sz)) {
		    g.aimage(tex, sc, 0.5, 0.5);
		}
	    }
	}
    }
}
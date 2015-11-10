package haven;

import java.awt.*;

public class GobInfo extends GAttrib {
    private GobInfoTex infoTex;

    static class GobInfoTex extends PView.Draw2D {
	private Tex tex;
	private Gob gob;

	public GobInfoTex(Gob gob, Tex tex) {
	    this.gob = gob;
	    this.tex = tex;
	}

	@Override
	public void draw2d(GOut g) {
	    if(tex != null)
		g.aimage(tex, gob.sc, 0.5, 0.5);
	}
    }

    static GobInfoTex nullTex = new GobInfoTex(null, null);

    public GobInfo(Gob gob, Tex tex) {
	super(gob);
	if(tex != null)
	    infoTex = new GobInfoTex(gob, tex);
	else
	    infoTex = nullTex;
    }

    public GobInfoTex draw() {
	return infoTex;
    }

    public static GobInfo get(Gob gob) {
	try {
	    if(gob == null || gob.getres() == null) { return new GobInfo(gob, null);}
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
		return new GobInfo(gob, line.tex());
	    }
	} catch (Loading e) {
	    return null;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return new GobInfo(gob, null);
    }

    public static Message getDrawableData(Gob gob) {
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
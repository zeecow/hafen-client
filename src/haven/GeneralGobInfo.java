package haven;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GeneralGobInfo extends GobInfo {
    private GobHealth health;

    protected GeneralGobInfo(Gob owner) {
	super(owner);
    }

    @Override
    protected Tex render() {
	if(gob == null || gob.getres() == null) { return null;}

	BufferedImage growth = growth();
	BufferedImage health = health();

	if(growth == null && health == null) {
	    return null;
	}

	return new TexI(ItemInfo.catimgsh(3, health, growth));
    }

    @Override
    public boolean setup(RenderList d) {
	if(this.health != gob.getattr(GobHealth.class)) {
	    clean();
	    ready = false;
	}
	return super.setup(d);
    }

    @Override
    public void dispose() {
	health = null;
	super.dispose();
    }

    private BufferedImage health() {
	health = gob.getattr(GobHealth.class);
	if(health != null) {
	    return health.text();
	}

	return null;
    }

    private BufferedImage growth() {
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
		data.skip(1);
		int growth = data.eom() ? -1 : data.uint8();
		if(growth < 100 && growth >= 0) {
		    Color c = Utils.blendcol(growth / 100.0, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN);
		    line = Text.std.renderstroked(String.format("%d%%", growth), c, Color.BLACK);
		}
	    }
	}

	if(line != null) {
	    return line.img;
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
	Class spc = null;
	Drawable d = gob.getattr(Drawable.class);
	if(d instanceof ResDrawable) {
	    spc = ((ResDrawable) d).spr.getClass();
	} else {
	    Resource.CodeEntry ce = gob.getres().layer(Resource.CodeEntry.class);
	    if(ce != null) { spc = ce.get("spr"); }
	}
	return spc != null && (spc.getSimpleName().equals(kind) || spc.getSuperclass().getSimpleName().equals(kind));
    }

    @Override
    public String toString() {
	Resource res = gob.getres();
	return String.format("GobInfo<%s>", res != null ? res.name : "<loading>");
    }
}
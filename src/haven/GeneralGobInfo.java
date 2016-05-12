package haven;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GeneralGobInfo extends PView.Draw2D {
    public boolean ready = false;
    private Tex tex;
    private final Gob gob;
    private GobHealth health;
    private GLState.Buffer state;

    @Override
    public Object staticp() {
	return Rendered.CONSTANS;
    }

    protected GeneralGobInfo(Gob owner) {
	this.gob = owner;
    }

    @Override
    public void draw2d(GOut g) {
	if(tex != null) {
	    Coord sc = null;
	    if(state != null) {sc = Utils.world2screen(gob.getc(), state, 1);}
	    if(sc != null && sc.isect(Coord.z, g.sz)) {
		g.aimage(tex, sc, 0.5, 0.5);
	    }
	}
    }

    @Override
    public boolean setup(RenderList d) {
	if(this.health != gob.getattr(GobHealth.class)) {
	    clean();
	    ready = false;
	}
	state = d.state();
	if(!ready) {
	    try {
		tex = render();
		ready = true;
	    } catch (Loading ignored) {
	    } catch (Exception e) {
		tex = null;
		ready = true;
	    }
	}
	return ready && tex != null;
    }

    private Tex render() {
	if(gob == null || gob.getres() == null) { return null;}

	BufferedImage growth = growth();
	BufferedImage health = health();

	if(growth == null && health == null) {
	    return null;
	}

	return new TexI(ItemInfo.catimgsh(3, health, growth));
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
		int growth = data.uint8();
		if(growth < 100) {
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
	Resource.CodeEntry ce = gob.getres().layer(Resource.CodeEntry.class);
	if(ce != null) {
	    Class spc = ce.get("spr");
	    return spc != null && (spc.getSimpleName().equals(kind) || spc.getSuperclass().getSimpleName().equals(kind));
	} else {
	    return false;
	}
    }

    private void clean() {
	if(tex != null) {
	    tex.dispose();
	    tex = null;
	}
    }

    public void dispose() {
	clean();
	state = null;
	health = null;
    }

    @Override
    public String toString() {
	Resource res = gob.getres();
	return String.format("GobInfo<%s>", res != null ? res.name : "<loading>");
    }
}
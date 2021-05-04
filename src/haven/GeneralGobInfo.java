package haven;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class GeneralGobInfo extends GobInfo {
    private static int TREE_START = 10;
    private static int BUSH_START = 30;
    private static double TREE_MULT = 100.0 / (100.0 - TREE_START);
    private static double BUSH_MULT = 100.0 / (100.0 - BUSH_START);
    private GobHealth health;

    protected GeneralGobInfo(Gob owner) {
	super(owner);
    }
    
    @Override
    protected boolean enabled() {
	return CFG.DISPLAY_GOB_INFO.get();
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
 
	if(isSpriteKind(gob, "GrowingPlant", "TrellisPlant")) {
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
	} else if(isSpriteKind(gob, "Tree")) {
	    Message data = getDrawableData(gob);
	    if(data != null && !data.eom()) {
		data.skip(1);
		int growth = data.eom() ? -1 : data.uint8();
		if(growth < 100 && growth >= 0) {
		    if(gob.is(GobTag.TREE)) {
			growth = (int) (TREE_MULT * (growth - TREE_START));
		    } else if(gob.is(GobTag.BUSH)) {
			growth = (int) (BUSH_MULT * (growth - BUSH_START));
		    }
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
	Drawable dr = gob.drawable;
	ResDrawable d = (dr instanceof ResDrawable) ? (ResDrawable) dr : null;
	if(d != null)
	    return d.sdt.clone();
	else
	    return null;
    }
    
    private static boolean isSpriteKind(Gob gob, String... kind) {
	List<String> kinds = Arrays.asList(kind);
	boolean result = false;
	Class spc;
	Drawable d = gob.drawable;
	Resource.CodeEntry ce = gob.getres().layer(Resource.CodeEntry.class);
	if(ce != null) {
	    spc = ce.get("spr");
	    result = spc != null && (kinds.contains(spc.getSimpleName()) || kinds.contains(spc.getSuperclass().getSimpleName()));
	}
	if(!result) {
	    if(d instanceof ResDrawable) {
		Sprite spr = ((ResDrawable) d).spr;
		if(spr == null) {throw new Loading();}
		spc = spr.getClass();
		result = kinds.contains(spc.getSimpleName()) || kinds.contains(spc.getSuperclass().getSimpleName());
	    }
	}
	return result;
    }

    @Override
    public String toString() {
	Resource res = gob.getres();
	return String.format("GobInfo<%s>", res != null ? res.name : "<loading>");
    }
}
package haven;

import haven.render.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static haven.Sprite.*;

public class Hitbox extends GAttrib implements RenderTree.Node, Rendered {
    private static final VertexArray.Layout LAYOUT = new VertexArray.Layout(new VertexArray.Layout.Input(Homo3D.vertex, new VectorFormat(3, NumberFormat.FLOAT32), 0, 0, 12));
    private final Model model;
    private static final Map<Resource, Model> MODEL_CACHE = new HashMap<>();
    private static final float Z = 0.1f;
    private static final Color SOLID_COLOR = new Color(180, 134, 255, 255);
    private static final Color PASSABLE_COLOR = new Color(105, 207, 124, 255);
    private static final float PASSABLE_WIDTH = 1f;
    private static final float SOLID_WIDTH = 2f;
    private static final Pipe.Op TOP = Pipe.Op.compose(Rendered.last, States.Depthtest.none, States.maskdepth);
    private static final Pipe.Op SOLID = Pipe.Op.compose(new BaseColor(SOLID_COLOR), new States.LineWidth(SOLID_WIDTH));
    private static final Pipe.Op PASSABLE = Pipe.Op.compose(new BaseColor(PASSABLE_COLOR), new States.LineWidth(PASSABLE_WIDTH));
    private static final Pipe.Op SOLID_TOP = Pipe.Op.compose(SOLID, TOP);
    private static final Pipe.Op PASSABLE_TOP = Pipe.Op.compose(PASSABLE, TOP);
    private Pipe.Op state;
    
    private Hitbox(Gob gob) {
	super(gob);
	model = getModel(gob);
	updateState();
    }
    
    public static Hitbox forGob(Gob gob) {
	try {
	    return new Hitbox(gob);
	} catch (Loading ignored) { }
	return null;
    }
    
    @Override
    public void added(RenderTree.Slot slot) {
	super.added(slot);
	slot.ostate(state);
	updateState();
    }
    
    @Override
    public void draw(Pipe context, Render out) {
	if(model != null) {
	    out.draw(context, model);
	}
    }
    
    public void updateState() {
	if(model != null && slots != null) {
	    boolean top = CFG.DISPLAY_GOB_HITBOX_TOP.get();
	    Pipe.Op newState = passable() ? (top ? PASSABLE_TOP : PASSABLE) : (top ? SOLID_TOP : SOLID);
	    if(newState != state) {
		state = newState;
		for (RenderTree.Slot slot : slots) {
		    slot.ostate(state);
		}
	    }
	}
    }
    
    private boolean passable() {
	try {
	    Resource res = gob.getres();
	    String name = res != null ? res.name : "";
	    
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
		} else if(name.equals("gfx/terobjs/arch/cellardoor")) {
		    return true;
		}
	    }
	} catch (Loading ignored) {}
	return false;
    }
    
    private static Model getModel(Gob gob) {
	Model model;
	synchronized (MODEL_CACHE) {
	    Resource res = getResource(gob);
	
	    model = MODEL_CACHE.get(res);
	    if(model == null) {
		List<List<Coord3f>> polygons = new LinkedList<>();
	    
		Collection<Resource.Neg> negs = res.layers(Resource.Neg.class);
		if(negs != null) {
		    for (Resource.Neg neg : negs) {
			List<Coord3f> box = new LinkedList<>();
			box.add(new Coord3f(neg.ac.x, -neg.ac.y, Z));
			box.add(new Coord3f(neg.bc.x, -neg.ac.y, Z));
			box.add(new Coord3f(neg.bc.x, -neg.bc.y, Z));
			box.add(new Coord3f(neg.ac.x, -neg.bc.y, Z));
		    
			polygons.add(box);
		    }
		}
	    
		Collection<Resource.Obst> obstacles = res.layers(Resource.Obst.class);
		if(obstacles != null) {
		    for (Resource.Obst obstacle : obstacles) {
			if(!"build".equals(obstacle.id)) {
			    for (Coord2d[] polygon : obstacle.polygons) {
				polygons.add(Arrays.stream(polygon)
				    .map(coord2d -> new Coord3f(11 * (float) coord2d.x, 11 * (float) coord2d.y, Z))
				    .collect(Collectors.toList()));
			    }
			}
		    }
		}
	    
		if(!polygons.isEmpty()) {
		    List<Float> vertices = new LinkedList<>();
		
		    for (List<Coord3f> polygon : polygons) {
			addLoopedVertices(vertices, polygon);
		    }
		
		    float[] data = convert(vertices);
		    VertexArray.Buffer vbo = new VertexArray.Buffer(data.length * 4, DataBuffer.Usage.STATIC, DataBuffer.Filler.of(data));
		    VertexArray va = new VertexArray(LAYOUT, vbo);
		
		    model = new Model(Model.Mode.LINES, va, null);
		
		    MODEL_CACHE.put(res, model);
		}
	    }
	}
	return model;
    }
    
    private static float[] convert(List<Float> list) {
	float[] ret = new float[list.size()];
	int i = 0;
	for (Float value : list) {
	    ret[i++] = value;
	}
	return ret;
    }
    
    private static void addLoopedVertices(List<Float> target, List<Coord3f> vertices) {
	int n = vertices.size();
	for (int i = 0; i < n; i++) {
	    Coord3f a = vertices.get(i);
	    Coord3f b = vertices.get((i + 1) % n);
	    Collections.addAll(target, a.x, a.y, a.z);
	    Collections.addAll(target, b.x, b.y, b.z);
	}
    }
    
    private static Resource getResource(Gob gob) {
	Resource res = gob.getres();
	if(res == null) {throw new Loading();}
	Collection<RenderLink.Res> links = res.layers(RenderLink.Res.class);
	for (RenderLink.Res link : links) {
	    if(link.l instanceof RenderLink.MeshMat) {
		RenderLink.MeshMat mesh = (RenderLink.MeshMat) link.l;
		return mesh.mesh.get();
	    }
	}
	return res;
    }
}

package haven;

import haven.render.*;

import java.awt.*;
import java.util.List;
import java.util.*;

public class PathVisualizer implements RenderTree.Node {
    private static final VertexArray.Layout LAYOUT = new VertexArray.Layout(new VertexArray.Layout.Input(Homo3D.vertex, new VectorFormat(3, NumberFormat.FLOAT32), 0, 0, 12));
    
    
    public final Collection<RenderTree.Slot> slots = new ArrayList<>(1);
    private final Set<Moving> moves = new HashSet<>();
    private final Map<PathCategory, MovingPath> paths = new HashMap<>();
    
    public PathVisualizer() {
	for (PathCategory cat : PathCategory.values()) {
	    paths.put(cat, new MovingPath(cat.state));
	}
    }
    
    @Override
    public void added(RenderTree.Slot slot) {
	synchronized (slots) {slots.add(slot);}
	for (MovingPath path : paths.values()) {
	    slot.add(path);
	}
    }
    
    @Override
    public void removed(RenderTree.Slot slot) {
	synchronized (slots) {slots.remove(slot);}
    }
    
    private void update() {
	synchronized (moves) {
	    Map<PathCategory, Set<Moving>> categorized = new HashMap<>();
	    for (Moving m : moves) {
		PathCategory category = categorize(m);
		if(!categorized.containsKey(category)) {
		    categorized.put(category, new HashSet<>());
		}
		categorized.get(category).add(m);
	    }
	    
	    for (PathCategory cat : PathCategory.values()) {
		Set<Moving> moveset = categorized.get(cat);
		MovingPath path = paths.get(cat);
		if(moveset == null || moveset.isEmpty()) {
		    if(path != null) {
			path.update(null);
		    }
		} else {
		    path.update(moveset);
		}
	    }
	    
	}
    }
    
    private PathCategory categorize(Moving m) {
	if(m.gob.isMe()) {
	    return PathCategory.ME;
	} else {
	    return PathCategory.DEFAULT;
	}
    }
    
    private static final float Z = 1f;
    
    private static float[] convert(List<Pair<Coord3f, Coord3f>> lines) {
	float[] ret = new float[lines.size() * 6];
	int i = 0;
	for (Pair<Coord3f, Coord3f> line : lines) {
	    ret[i++] = line.a.x;
	    ret[i++] = -line.a.y;
	    ret[i++] = line.a.z + Z;
	    
	    ret[i++] = line.b.x;
	    ret[i++] = -line.b.y;
	    ret[i++] = line.b.z + Z;
	}
	return ret;
    }
    
    public void addPath(Moving moving) {
	synchronized (moves) {
	    moves.add(moving);
	    update();
	}
    }
    
    
    public void removePath(Moving moving) {
	synchronized (moves) {
	    moves.remove(moving);
	    update();
	}
    }
    
    public void tick(double dt) {
	update();
    }
    
    private static class MovingPath implements RenderTree.Node, Rendered {
	private final Pipe.Op state;
	public final Collection<RenderTree.Slot> slots = new ArrayList<>(1);
	private Model model;
	
	public MovingPath(Pipe.Op state) {
	    this.state = state;
	}
	
	@Override
	public void added(RenderTree.Slot slot) {
	    slot.ostate(state);
	    synchronized (slots) {slots.add(slot);}
	}
	
	@Override
	public void removed(RenderTree.Slot slot) {
	    synchronized (slots) {slots.remove(slot);}
	}
	
	@Override
	public void draw(Pipe context, Render out) {
	    if(model != null) {
		out.draw(context, model);
	    }
	}
	
	public void update(Set<Moving> moves) {
	    //TODO: make this method accept list of lines instead of list of Movings
	    if(moves == null || moves.isEmpty()) {
		model = null;
	    } else {
		List<Pair<Coord3f, Coord3f>> lines = new LinkedList<>();
		for (Moving m : moves) {
		    try {
			lines.add(new Pair<>(
			    m.getc(),
			    m.gett()
			));
		    } catch (Loading ignored) {}
		}
		
		float[] data = convert(lines);
		
		VertexArray.Buffer vbo = new VertexArray.Buffer(data.length * 4, DataBuffer.Usage.STATIC, DataBuffer.Filler.of(data));
		VertexArray va = new VertexArray(LAYOUT, vbo);
		
		model = new Model(Model.Mode.LINES, va, null);
	    }
	    synchronized (slots) {
		slots.forEach(RenderTree.Slot::update);
	    }
	}
	
    }
    
    private static final Pipe.Op TOP = Pipe.Op.compose(Rendered.last, States.Depthtest.none, States.maskdepth);
    private static final float LINE_WIDTH = 4f;
    private static final Pipe.Op BASE = Pipe.Op.compose(new States.LineWidth(LINE_WIDTH), TOP);
    
    private enum PathCategory {
	DEFAULT(new Color(255, 134, 215, 255)),
	ME(new Color(134, 249, 255, 255));
	
	
	private final Pipe.Op state;
	
	PathCategory(Color col) {
	    state = Pipe.Op.compose(BASE, new BaseColor(col));
	}
    }
}

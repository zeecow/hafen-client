package haven;

import haven.render.Homo3D;
import haven.render.Pipe;
import haven.render.RenderTree;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static haven.Sprite.*;

public class Hitbox extends GAttrib implements RenderTree.Node, PView.Render2D {
    private List<List<Coord3f>> polygons;
    private static final Color SOLID = new Color(180, 134, 255, 200);
    private static final Color PASSABLE = new Color(105, 207, 124, 200);
    private static final int PASSABLE_WIDTH = UI.scale(1);
    private static final int SOLID_WIDTH = UI.scale(2);
    private boolean ready = false;
    
    protected Hitbox(Gob gob) {
	super(gob);
    }
    
    @Override
    public void draw(GOut g, Pipe state) {
	if(CFG.DISPLAY_GOB_HITBOX.get()) {
	    if(!ready) {
		try {
		    polygons = getPolygons(gob);
		    ready = true;
		} catch (Loading ignored) {}
	    }
	    if(polygons != null) {
		boolean passable = passable();
		g.chcolor(passable ? PASSABLE : SOLID);
		Area area = Area.sized(g.sz());
		for (List<Coord3f> polygon : polygons) {
		    int points = polygon.size();
		    for (int i = 0; i < points; i++) {
			Coord3f a = Homo3D.obj2view(polygon.get(i % points), state, area);
			Coord3f b = Homo3D.obj2view(polygon.get((i + 1) % points), state, area);
			if(a.isect(Coord.z, g.sz()) || b.isect(Coord.z, g.sz())) {
			    g.line(a, b, passable ? PASSABLE_WIDTH : SOLID_WIDTH);
			}
		    }
		}
		
	    }
	}
    }
    
    private boolean passable() {
	try {
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
    
    private static final Map<Resource, List<List<Coord3f>>> cache = new HashMap<>();
    
    private static List<List<Coord3f>> getPolygons(Gob gob) {
	Resource res = getResource(gob);
	List<List<Coord3f>> polygons = cache.get(res);
	if(polygons == null) {
	    polygons = new LinkedList<>();
	    
	    Collection<Resource.Neg> negs = res.layers(Resource.Neg.class);
	    if(negs != null) {
		for (Resource.Neg neg : negs) {
		    List<Coord3f> box = new LinkedList<>();
		    box.add(new Coord3f(neg.ac.x, neg.ac.y, 0));
		    box.add(new Coord3f(neg.bc.x, neg.ac.y, 0));
		    box.add(new Coord3f(neg.bc.x, neg.bc.y, 0));
		    box.add(new Coord3f(neg.ac.x, neg.bc.y, 0));
		    
		    polygons.add(box);
		}
		
	    }
	    
	    Collection<Resource.Obst> obstacles = res.layers(Resource.Obst.class);
	    if(obstacles != null) {
		for (Resource.Obst obstacle : obstacles) {
		    if(!"build".equals(obstacle.id)) {
			for (Coord2d[] polygon : obstacle.polygons) {
			    polygons.add(Arrays.stream(polygon)
				.map(coord2d -> new Coord3f(11 * (float) coord2d.x, 11 * (float) coord2d.y, 0))
				.collect(Collectors.toList()));
			}
		    }
		}
		
	    }
	    
	    cache.put(res, polygons);
	}
	
	return polygons.isEmpty() ? null : polygons;
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
	    System.out.println(link);
	}
	return res;
    }
}

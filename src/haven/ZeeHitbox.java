package haven;

import haven.render.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ZeeHitbox extends ZeeSlottedNode implements Rendered {
    private static final VertexArray.Layout LAYOUT = new VertexArray.Layout(new VertexArray.Layout.Input(Homo3D.vertex, new VectorFormat(3, NumberFormat.FLOAT32), 0, 0, 12));
    private Model model;
    private final Gob gob;
    private static final Map<Resource, Model> MODEL_CACHE = new HashMap<>();
    private static final float Z = 0.1f;
    private static final Color SOLID_COLOR = new Color(139, 139, 185, 255);
    private static final Color PASSABLE_COLOR = new Color(105, 207, 124, 255);
    private static final float PASSABLE_WIDTH = 1.5f;
//    private static final float SOLID_WIDTH = 0f;
    private static final Pipe.Op TOP = Pipe.Op.compose(Rendered.last, States.Depthtest.none, States.maskdepth);
    private static final Pipe.Op SOLID = Pipe.Op.compose(new BaseColor(SOLID_COLOR));
    private static final Pipe.Op PASSABLE = Pipe.Op.compose(new BaseColor(PASSABLE_COLOR), new States.LineWidth(PASSABLE_WIDTH));
//    private static final Pipe.Op SOLID_TOP = Pipe.Op.compose(SOLID, TOP);
//    private static final Pipe.Op PASSABLE_TOP = Pipe.Op.compose(PASSABLE, TOP);
    private Pipe.Op state = SOLID;

    private ZeeHitbox(Gob gob) {
        model = getModel(gob);
        this.gob = gob;
        updateState();
    }

    public static ZeeHitbox forGob(Gob gob) {
        try {
            return new ZeeHitbox(gob);
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
            boolean top = false;//CFG.DISPLAY_GOB_HITBOX_TOP.get();
            //Pipe.Op newState = passable() ? (top ? PASSABLE_TOP : PASSABLE) : (top ? SOLID_TOP : SOLID);
            Pipe.Op newState = passable() ? PASSABLE : SOLID;
            try {
                Model m = getModel(gob);
                if(m != null && m != model) {
                    model = m;
                    slots.forEach(RenderTree.Slot::update);
                }
            }catch (Loading ignored) {}
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
            String name = gob.getres().name;//gob.resid();
            ResDrawable rd = (gob.drawable instanceof ResDrawable) ? (ResDrawable) gob.drawable : null;

            if(rd != null) {
                int state = gob.sdt();
                if(name.endsWith("gate") && name.startsWith("gfx/terobjs/arch")) {//gates
                    if(state == 1) { // gate is open
                        return true;
                    }
                } else if(name.endsWith("/dng/antdoor")) {
                    return state == 1 || state == 13;
                } else if(name.endsWith("/pow[hearth]")) {//hearth fire
                    return true;
                } else if(name.equals("gfx/terobjs/arch/cellardoor") || name.equals("gfx/terobjs/fishingnet")) {
                    return true;
                }
            }
        } catch (Loading ignored) {}
        return false;
    }

    private static Model getModel(Gob gob) {
        Model model;
        Resource res = getResource(gob);
        synchronized (MODEL_CACHE) {
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

                Collection<Resource.Obstacle> obstacles = res.layers(Resource.Obstacle.class);
                if(obstacles != null) {
                    for (Resource.Obstacle obstacle : obstacles) {
                        if("build".equals(obstacle.id)) {continue;}
                        for (Coord2d[] polygon : obstacle.p) {
                            polygons.add(Arrays.stream(polygon)
                                    .map(coord2d -> new Coord3f((float) coord2d.x, (float) -coord2d.y, Z))
                                    .collect(Collectors.toList()));
                        }
                    }
                }

                if(!polygons.isEmpty()) {
                    List<Float> vertices = new LinkedList<>();

                    //ZeeConfig.println(res.name);
                    //ZeeConfig.println("   polygons.get(0) size "+polygons.get(0).size());
                    addTriangles(vertices, polygons.get(0));

                    float[] data = convert(vertices);
                    VertexArray.Buffer vbo = new VertexArray.Buffer(data.length * 4, DataBuffer.Usage.STATIC, DataBuffer.Filler.of(data));
                    VertexArray va = new VertexArray(LAYOUT, vbo);

                    model = new Model(Model.Mode.TRIANGLE_STRIP, va, null);

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

    private static void addTriangles(List<Float> target, List<Coord3f> vertices) {
        int n = vertices.size();
        Coord3f a, b, c;
        //int triangles = 0;
        int i=0;
        // all triangles starting at "a"
        // triangles = abc,acd      (2 triangles, 4-sided-polygon)
        //             abc,acd,ade  (3 triangles, 5-side-polygon)
        a = vertices.get(0);
        do{
            b = vertices.get(i + 1);
            c = vertices.get(i + 2);
            Collections.addAll(target, a.x, a.y, a.z);
            Collections.addAll(target, b.x, b.y, b.z);
            Collections.addAll(target, c.x, c.y, c.z);
            //triangles++;
            i++;
        }while(i < n-2);
        //ZeeConfig.println("      triangles = "+triangles);
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
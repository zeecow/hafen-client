package haven;


import haven.render.Homo3D;
import haven.render.Pipe;
import haven.render.RenderTree;

import java.util.HashMap;


public class ZeeGobPointer extends Sprite implements RenderTree.Node, PView.Render2D {

    static final HashMap<String, Tex> mapGobPointer = new HashMap<>();
    Coord2d tc;
    long gobid = -1;
    Tex tex;
    Gob gob;

    public ZeeGobPointer(Gob gob, Tex tex) {
        super(null,null);//super(gob, icon.get());
        this.tex = tex;
        this.gob = gob;
        update(gob.rc,gob.id);
    }

    public void draw(GOut g, Pipe state){
        Coord sc = Homo3D.obj2view(new Coord3f(0, 0, 10), state, Area.sized(g.sz())).round2();
        try {
            if(tex == null) {
                tex = mapGobPointer.get(gob.getres().name);
            }
            g.image(tex, sc);
        } catch(Loading l) {
            ZeeConfig.println("ZeePointer.draw > "+l.getMessage());
        }
    }

    public void update(Coord2d tc, long gobid) {
        this.tc = tc;
        this.gobid = gobid;
    }

//    @Override
//    public void removed(RenderTree.Slot slot) {
//        mapGobPointer.remove(gob.getres().name);
//    }
}


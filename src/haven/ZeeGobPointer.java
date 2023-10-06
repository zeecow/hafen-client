package haven;


import haven.render.*;

import java.awt.*;
import java.util.HashMap;


public class ZeeGobPointer extends Sprite implements RenderTree.Node, PView.Render2D {

    static final HashMap<String, Tex> mapGobPointer = new HashMap<>();
    static final TexI bgTexTriangle = new TexI(ZeeManagerIcons.imgTriangleDown(10, Color.CYAN,false,false,true));
    static final TexI bgTexCircle = new TexI(ZeeManagerIcons.imgSquare(20, Color.CYAN,false,false,true));
    static ZeeGobRadar gobRadar;
    Coord2d tc;
    long gobid = -1;
    Tex iconTex;
    Gob gob;

    public ZeeGobPointer(Gob gob, Tex tex) {
        super(gob,null);//super(gob, icon.get());
        this.iconTex = tex;
        this.gob = gob;
        update(gob.rc,gob.id);
    }

    public void draw(GOut g, Pipe state){
        Coord sc;
        if (gobRadar!=null && gob.hasPointer && ZeeConfig.gameUI.mmap.playerSegment)
            sc = Homo3D.obj2view(new Coord3f(0, 0, 20), state, Area.sized(Coord.of(10))).round2();//ZeeConfig.gameUI.mmap.p2c(gob.rc);
        else
            sc = Homo3D.obj2view(new Coord3f(0, 0, 20), state, Area.sized(g.sz())).round2();
        try {
            if(iconTex == null) {
                iconTex = mapGobPointer.get(gob.getres().name);
            }
            g.aimage(bgTexTriangle, sc, -0.5, -1.5);
            g.image(bgTexCircle, sc);
            g.image(iconTex, sc, bgTexCircle.sz);
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


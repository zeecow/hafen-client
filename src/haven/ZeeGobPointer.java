package haven;


import haven.render.*;

import java.awt.*;
import java.util.HashMap;

@SuppressWarnings("deprecation")
public class ZeeGobPointer extends Sprite implements RenderTree.Node, PView.Render2D {

    static final HashMap<String, Tex> mapGobPointer = new HashMap<>();
    static final TexI BG_TEX_TRIANGLE = new TexI(ZeeManagerIcons.imgTriangleDown(10, Color.CYAN,false,false,true));
    static final TexI BG_TEX_SQUARE = new TexI(ZeeManagerIcons.imgSquare(20, Color.CYAN,false,false,true));
    static final TexI BG_TEX_TRIANGLE_AGGRO = new TexI(ZeeManagerIcons.imgTriangleDown(10, Color.RED,false,false,true));
    static final TexI BG_TEX_SQUARE_AGGRO = new TexI(ZeeManagerIcons.imgSquare(20, Color.RED,false,false,true));
    static final TexI BG_TEX_TRIANGLE_DED = new TexI(ZeeManagerIcons.imgTriangleDown(10, Color.LIGHT_GRAY,false,false,true));
    static final TexI BG_TEX_SQUARE_DED = new TexI(ZeeManagerIcons.imgSquare(20, Color.LIGHT_GRAY,false,false,true));
    static ZeeGobRadar gobRadar;
    private boolean rawIcon;
    Coord2d tc;
    long gobid = -1;
    Tex iconTex;
    Gob gob;
    TexI bgTexTriangle, bgTexSquare;

    public ZeeGobPointer(Gob gob, Tex tex) {
        super(gob,null);//super(gob, icon.get());
        this.iconTex = tex;
        this.gob = gob;
        this.rawIcon = false;
        update(gob.rc,gob.id);
    }

    public ZeeGobPointer(Gob gobCup, Tex tex, boolean rawIcon) {
        this(gobCup,tex);
        this.rawIcon = rawIcon;
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
            if (rawIcon) {
                g.image(iconTex, sc);
            } else {
                //TODO find better way
                if (gob.isPoseAggro) {
                    bgTexTriangle = BG_TEX_TRIANGLE_AGGRO;
                    bgTexSquare = BG_TEX_SQUARE_AGGRO;
                }else if (gob.isPoseDedKO){
                    bgTexTriangle = BG_TEX_TRIANGLE_DED;
                    bgTexSquare = BG_TEX_SQUARE_DED;
                }else{
                    bgTexTriangle = BG_TEX_TRIANGLE;
                    bgTexSquare = BG_TEX_SQUARE;
                }
                g.aimage(bgTexTriangle, sc, -0.5, -1.5);
                g.image(bgTexSquare, sc);
                g.image(iconTex, sc, bgTexSquare.sz);
            }
        } catch(Loading l) {
            ZeeConfig.println("ZeePointer.draw > "+l.getMessage());
        }
    }

    public void update(Coord2d tc, long gobid) {
        this.tc = tc;
        this.gobid = gobid;
    }


    // set flags for changing pointer bg color when gob pose aggro/ded
    public static void checkPoseAggroDed(Gob gob) {
        if (!gob.isPoseDedKO && ZeeManagerGobs.isGobDeadOrKO(gob)) {
            gob.isPoseDedKO = true;
        }else if (!gob.isPoseAggro && ZeeManagerGobs.isGobPoseAggro(gob)) {
            gob.isPoseAggro = true;
        }
    }

//    @Override
//    public void removed(RenderTree.Slot slot) {
//        mapGobPointer.remove(gob.getres().name);
//    }
}


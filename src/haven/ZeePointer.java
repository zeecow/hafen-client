package haven;


import haven.render.*;

import java.awt.*;
import java.awt.image.BufferedImage;


public class ZeePointer extends Sprite implements RenderTree.Node, PView.Render2D {
    public static final BaseColor col = new BaseColor(new Color(241, 227, 157, 255));
    public Indir<Resource> icon;
    public Coord2d tc;
    public Coord lc;
    public long gobid = -1;
    public boolean click;
    private Tex licon;
    public Gob gob;

    public ZeePointer(Gob gob, Indir<Resource> icon) {
        super(null,null);//super(gob, icon.get());
        this.icon = icon;
        this.gob = gob;
        update(gob.rc,gob.id);
    }

    private int signum(int var1) {
        if (var1 < 0) {
            return -1;
        } else {
            return var1 > 0 ? 1 : 0;
        }
    }

    private void drawarrow(GOut g, Coord tc) {
        Coord hsz = Coord.of(30);//sz.div(2);
        tc = tc.sub(hsz);
        if(tc.equals(Coord.z))
            tc = new Coord(1, 1);
        double d = Coord.z.dist(tc);
        Coord sc = tc.mul((d - 25.0) / d);
        float ak = ((float)hsz.y) / ((float)hsz.x);
        if((Math.abs(sc.x) > hsz.x) || (Math.abs(sc.y) > hsz.y)) {
            if(Math.abs(sc.x) * ak < Math.abs(sc.y)) {
                sc = new Coord((sc.x * hsz.y) / sc.y, hsz.y).mul(signum(sc.y));
            } else {
                sc = new Coord(hsz.x, (sc.y * hsz.x) / sc.x).mul(signum(sc.x));
            }
        }
        Coord ad = sc.sub(tc).norm(UI.scale(30.0));
        sc = sc.add(hsz);

        // gl.glEnable(GL2.GL_POLYGON_SMOOTH); XXXRENDER
        g.usestate(col);
        g.drawp(Model.Mode.TRIANGLES, new float[] {
                sc.x, sc.y,
                sc.x + ad.x - (ad.y / 3), sc.y + ad.y + (ad.x / 3),
                sc.x + ad.x + (ad.y / 3), sc.y + ad.y - (ad.x / 3),
        });

        this.lc = sc.add(ad);
    }

    static final BufferedImage circle = ZeeManagerIcons.imgCirle(150,col.color(),false,false,false);
    private final static int zOfs = 5;
    public void draw(GOut g, Pipe state){
        Coord sc = Homo3D.obj2view(new Coord3f(0, 0, 6 + zOfs), state, Area.sized(g.sz())).round2();
        try {
            if(licon == null)
                licon = icon.get().layer(Resource.imgc).tex();
            g.aimage(licon, sc, 0.5, 0.5);
        } catch(Loading l) {
            ZeeConfig.println("ZeePointer.draw > "+l.getMessage());
        }
        //drawarrow(g,sc);
    }

    public void update(Coord2d tc, long gobid) {
        this.tc = tc;
        this.gobid = gobid;
    }

//    public void draw(GOut g) {
//        this.lc = null;
//        if(tc == null)
//            return;
//        //Gob gob = (gobid < 0) ? null : ui.sess.glob.oc.getgob(gobid);
//        Coord3f sl;
//        if(gob != null) {
//            try {
//                sl = ZeeConfig.gameUI.map.screenxf(gob.getc());
//            } catch(Loading l) {
//                return;
//            }
//        } else {
//            sl = ZeeConfig.gameUI.map.screenxf(tc);
//        }
//        if(sl != null)
//            drawarrow(g, new Coord(sl));
//    }

//    public void uimsg(String name, Object... args) {
//        ZeeConfig.println(name+" , "+ZeeConfig.strArgs(args));
//        if(name == "upd") {
//            if(args[0] == null)
//                tc = null;
//            else
//                tc = ((Coord)args[0]).mul(OCache.posres);
//            if(args[1] == null)
//                gobid = -1;
//            else
//                gobid = Utils.uint32((Integer)args[1]);
//        } else if(name == "icon") {
//            int iconid = (Integer)args[0];
//            Indir<Resource> icon = (iconid < 0) ? null : ui.sess.getres(iconid);
//            this.icon = icon;
//            licon = null;
//        } else if(name == "cl") {
//            //click = ((Integer)args[0]) != 0;
//        } else {
//            super.uimsg(name, args);
//        }
//    }

//    public Object tooltip(Coord c, Widget prev) {
//        if((lc != null) && (lc.dist(c) < 20))
//            return(tooltip);
//        return(null);
//    }
}


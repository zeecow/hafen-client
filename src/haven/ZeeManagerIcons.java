package haven;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ZeeManagerIcons {
    static List<Gob> gobs = new ArrayList<>();
    static BufferedImage iconHorse, iconVehicle;

    public static BufferedImage getIconImage(Gob gob) {
        if (gob==null || gob.getres()==null)
            return null;
        if (gob.getres().name.contains("/horse/")) {
            if (iconHorse == null)
                iconHorse = imgRectangle(3,3,Color.GREEN);
            return iconHorse;
        }
        else if (gob.getres().name.contains("/vehicle/")) {
            if (iconVehicle == null)
                iconVehicle = imgRectangle(4,2,Color.BLUE);
            return iconVehicle;
        }
        return null;
    }

    private static BufferedImage imgRectangle(int w, int h, Color c) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w, h, type);
        Graphics2D g2d = ret.createGraphics();
        g2d.setColor(c);
        g2d.fillRect(0, 0, w, h);
        g2d.dispose();
        return ret;
    }

    public static void drawIcons(GOut g, MiniMap.Location sessloc, MiniMap.Location dloc, int dlvl, Coord sz) {
        if( (ZeeConfig.showIconsZoomOut && dlvl>2) || dlvl != 0)
            return;
        if((sessloc == null) || (dloc == null) || (dloc.seg != sessloc.seg))
            return;
        Coord sc;
        BufferedImage img;
        for (Gob gob : gobs) {
            img = getIconImage(gob);
            if (img==null || gob.rc==null)
                continue;
            sc = UI.scale(gob.rc.floor(MCache.tilesz).add(sessloc.tc).sub(dloc.tc).div(1 << dlvl)).add(sz.div(2));
            g.image(img, sc.sub(Coord.of(img.getWidth()/2)));
        }
    }

    public static void addQueue(Gob gob) {
        gobs.add(gob);
    }

    public static void clearQueue() {
        gobs.clear();
    }

    private static void println(String s) {
        ZeeConfig.println(s);
    }
}

package haven;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZeeManagerIcons {
    static List<Gob> gobs = new ArrayList<>();
    static BufferedImage iconHorse, iconVehicle;

    public static BufferedImage getIconImage(Gob gob) {
        if (gob==null || gob.getres()==null)
            return null;
        if (gob.getres().name.contains("/horse/")) {
            if (iconHorse == null)
                iconHorse = imgRect(3,3,Color.GREEN);
            return iconHorse;
        }
        else if (gob.getres().name.contains("/vehicle/")) {
            if (iconVehicle == null)
                iconVehicle = imgRect(4,2,Color.BLUE);
            return iconVehicle;
        }
        return null;
    }

    private static BufferedImage imgSquare(int side, Color c) {
        return imgRect(side,side,c);
    }

    private static BufferedImage imgCirle(int diameter, Color c) {
        return imgOval(diameter,diameter,c);
    }

    private static BufferedImage imgTriangle(int w, int h, Color c) {
        return imgPolygon(w, h, new int[]{w/2,0,w}, new int[]{0,h,h}, 3, c);
    }

    private static BufferedImage imgRect(int w, int h, Color c) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w, h, type);
        Graphics2D g2d = ret.createGraphics();
        g2d.setColor(c);
        g2d.fillRect(0, 0, w, h);
        g2d.dispose();
        return ret;
    }

    private static BufferedImage imgOval(int w, int h, Color c) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w, h, type);
        Graphics2D g2d = ret.createGraphics();
        g2d.setColor(c);
        g2d.fillOval(0, 0, w, h);
        g2d.dispose();
        return ret;
    }

    private static BufferedImage imgPolygon(int w, int h, int[] xPoints, int[] yPoints, int points, Color c) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w, h, type);
        Graphics2D g2d = ret.createGraphics();
        g2d.setColor(c);
        g2d.fillPolygon(xPoints,yPoints,points);
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

    static class ShapeIconsOptPanel extends JPanel{
        JComboBox nameCombo, shapeCombo;
        JTextField nameTF, colorTF, shapeTF;
        JPanel panelTop, panelCenter, panelBottom;
        public ShapeIconsOptPanel(JComboBox<String> comboRule){
            this.setLayout(new BorderLayout());
            this.add(panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.NORTH);
            this.add(panelCenter = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.CENTER);
            this.add(panelBottom = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.SOUTH);
            String[] arr = comboRule.getSelectedItem().toString().split(",");
            String[] arrGobName = arr[0].split(" ");
            String[] arrShape = arr[1].split(" ");
            String[] arrColor = arr[2].split(" ");

            panelTop.add(nameCombo = new JComboBox<>(new String[]{"startsWith", "contains", "endsWith"}), BorderLayout.NORTH);
            nameCombo.setSelectedIndex(Integer.parseInt(arrGobName[1]));
            panelTop.add(nameTF = new JTextField(arrGobName[0]));//gob query
            nameTF.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            panelCenter.add(shapeCombo = new JComboBox<>(new String[]{"circle", "triangle", "rect"}));
            shapeCombo.setSelectedItem(arrShape[0]);//shape name
            panelCenter.add(shapeTF = new JTextField(String.join(" ",Arrays.copyOfRange(arrShape,1,arrShape.length))));
            shapeTF.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            panelBottom.add(new JLabel("RGB: "));
            panelBottom.add(colorTF = new JTextField(String.join(" ", arrColor)));
            colorTF.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            colorTF.setBackground(new Color(Integer.parseInt(arrColor[0]), Integer.parseInt(arrColor[1]), Integer.parseInt(arrColor[2])));
            colorTF.setForeground(ZeeConfig.getComplementaryColor(colorTF.getBackground()));
        }
    }
}

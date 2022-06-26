package haven;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZeeManagerIcons {
    static List<Gob> gobs = new ArrayList<>();
    static HashMap<String,BufferedImage> mapRuleImg = new HashMap<String,BufferedImage>();

    public static BufferedImage getIconImage(Gob gob) {

        if (gob==null || gob.getres()==null || ZeeConfig.shapeIconsList.isBlank())
            return null;

        BufferedImage retImg = null;
        String gobName = gob.getres().name;
        String[] ruleArr, ruleName, ruleShape, ruleColor;
        String[] rules = ZeeConfig.shapeIconsList.split(";");

        //check all rules for gobName
        for (int i = 0; i < rules.length; i++) {

            // rule example: "/horse/ 1,circle 3,0 255 0"
            ruleArr = rules[i].split(",");

            // skip gob if name doesnt match rule
            ruleName = ruleArr[0].split(" ");
            if (ruleName[1].contentEquals("0") && !gobName.startsWith(ruleName[0]))
                continue;
            else if (ruleName[1].contentEquals("1") && !gobName.contains(ruleName[0]))
                continue;
            else if (ruleName[1].contentEquals("2") && !gobName.endsWith(ruleName[0]))
                continue;

            // returns icon already generated
            if (mapRuleImg.containsKey(rules[i]))
                return mapRuleImg.get(rules[i]);

            // generate color
            ruleColor = ruleArr[2].split(" ");
            Color c = new Color( Integer.parseInt(ruleColor[0]), Integer.parseInt(ruleColor[1]), Integer.parseInt(ruleColor[2]));

            // generate image
            ruleShape = ruleArr[1].split(" ");
            int num = Integer.parseInt(ruleShape[1]);
            if (ruleShape[0].contentEquals("circle"))
                retImg = imgCirle(num,c);
            else if (ruleShape[0].contentEquals("square"))
                retImg = imgSquare(num,c);
            else if (ruleShape[0].contentEquals("triangle"))
                retImg = imgTriangleUp(num,c);

            // store and return
            mapRuleImg.put(rules[i],retImg);
            println("mapRuleImg size "+mapRuleImg.size());
            return retImg;
        }
        return null;
    }

    private static BufferedImage imgSquare(int side, Color c) {
        return imgRect(side,side,c,1);
    }

    private static BufferedImage imgCirle(int diameter, Color c) {
        if (diameter < 5)
            diameter = 5;
        return imgOval(diameter,diameter,c,2);
    }

    private static BufferedImage imgTriangleUp(int s, Color c) {
        return imgPolygon(s, s,
            new int[]{ s/2, 0, s}, // x points
            new int[]{ 0, s, s}, // y points
            3, c, 3
        );
    }

    private static BufferedImage imgTriangleDown(int s, Color c) {
        return imgPolygon(s, s,
                new int[]{ 0, s, s/2}, // x points
                new int[]{ 0, 0, s}, // y points
                3, c, 3
        );
    }

    private static BufferedImage imgDiamond(int s, Color c) {
        return imgPolygon(s, s,
            new int[]{ s/2, 0, s/2, s }, // x points
            new int[]{ 0, s/2, s, s/2 }, // y points
            4, c, 2
        );
    }

    private static BufferedImage imgRect(int w, int h, Color c, int shadow) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w+shadow, h+shadow, type);
        Graphics2D g2d = ret.createGraphics();
        if (shadow > 0){
            g2d.setColor(Color.BLACK);
            g2d.fillRect(shadow, shadow, w, h);
        }
        g2d.setColor(c);
        g2d.fillRect(0, 0, w-shadow, h-shadow);
        g2d.dispose();
        return ret;
    }

    private static BufferedImage imgOval(int w, int h, Color c, int shadow) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w+shadow, h+shadow, type);
        Graphics2D g2d = ret.createGraphics();
        if (shadow > 0){
            g2d.setColor(Color.BLACK);
            g2d.fillOval(shadow, shadow, w, h);
            g2d.drawOval(shadow, shadow, w, h);
        }
        g2d.setColor(c);
        g2d.fillOval(0, 0, w-shadow, h-shadow);
        g2d.drawOval(0, 0, w-shadow, h-shadow);
        g2d.dispose();
        return ret;
    }

    private static BufferedImage imgPolygon(int w, int h, int[] xPoints, int[] yPoints, int points, Color c, int shadow) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w+shadow, h+shadow, type);
        Graphics2D g2d = ret.createGraphics();
        if (shadow > 0){
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < xPoints.length; i++) {
                xPoints[i] += shadow;
                yPoints[i] += shadow;
            }
            g2d.fillPolygon(xPoints,yPoints,points);
            g2d.drawPolygon(xPoints,yPoints,points);
            for (int i = 0; i < xPoints.length; i++) {
                xPoints[i] -= shadow;
                yPoints[i] -= shadow;
            }
        }
        g2d.setColor(c);
        g2d.fillPolygon(xPoints,yPoints,points);
        g2d.drawPolygon(xPoints,yPoints,points);
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

    static class ShapeIconsOptPanel extends JPanel{

        static JComboBox nameCombo, shapeCombo;
        static JTextField nameTF, colorTF;
        static JPanel panelTop, panelCenter, panelBottom;
        static JSpinner jspIconSize;
        static JButton btnGobColor;

        public ShapeIconsOptPanel(JComboBox<String> comboRule){
            this.setLayout(new BorderLayout());
            this.add(panelTop = new JPanel(new BorderLayout()), BorderLayout.NORTH);
            this.add(panelCenter = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.CENTER);
            this.add(panelBottom = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.SOUTH);

            JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pan.add(new JLabel("Gob name"), BorderLayout.NORTH);
            pan.add(nameCombo = new JComboBox<>(new String[]{"startsWith", "contains", "endsWith"}));
            panelTop.add(pan, BorderLayout.NORTH);
            panelTop.add(nameTF = new JTextField(), BorderLayout.SOUTH);//gob query
            nameTF.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            panelCenter.add(new JLabel("Icon:"));
            panelCenter.add(shapeCombo = new JComboBox<>(new String[]{"circle", "triangle", "square"}));
            SpinnerNumberModel model = new SpinnerNumberModel(3, 3, 10, 1);
            jspIconSize = new JSpinner(model);
            jspIconSize.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            panelCenter.add(new JLabel("size"));
            panelCenter.add(jspIconSize);

            panelBottom.add(btnGobColor = new JButton("Icon color"));
            btnGobColor.addActionListener(evt->{
                Color color = JColorChooser.showDialog(panelBottom, "Gob Highlight Color", null, false);
                if(color!=null){
                    btnGobColor.setBackground(color);
                    btnGobColor.setForeground(ZeeConfig.getComplementaryColor(color));
                }
            });

            if (comboRule.getSelectedIndex() > 0  &&  !comboRule.getSelectedItem().toString().isBlank())
                fillData(comboRule.getSelectedItem().toString());
        }

        private static void fillData(String selectedValue){
            String[] arr = selectedValue.split(",");
            String[] arrGobName = arr[0].split(" ");
            String[] arrShape = arr[1].split(" ");
            String[] arrColor = arr[2].split(" ");
            nameCombo.setSelectedIndex(Integer.parseInt(arrGobName[1]));
            nameTF.setText(arrGobName[0]);//gob query
            shapeCombo.setSelectedItem(arrShape[0]);//shape name
            int shapeSize = Integer.parseInt(arrShape[1]);
            jspIconSize.setValue(shapeSize);//shape size
            Color c = new Color(Integer.parseInt(arrColor[0]),Integer.parseInt(arrColor[1]),Integer.parseInt(arrColor[2]));
            btnGobColor.setBackground(c);
            btnGobColor.setForeground(ZeeConfig.getComplementaryColor(c));
        }

        public static String getRule(Component parent){
            if(nameTF.getText().isBlank()) {
                JOptionPane.showMessageDialog(parent,"gob name empty");
                return null;
            }
            String rule =  nameTF.getText() + " " + nameCombo.getSelectedIndex() + "," +
                shapeCombo.getSelectedItem().toString() + " " + jspIconSize.getValue() + "," +
                btnGobColor.getBackground().getRed() + " " +
                btnGobColor.getBackground().getGreen() + " " +
                btnGobColor.getBackground().getBlue();
            return rule;
        }
    }

    public static BufferedImage convertToBufferedImage(Image image)
    {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    static BufferedImage testcircle, testtriangleup, testtriangledown, testdiamond;
    public static void testIconsLoginScreen(GOut g) {
        if (testcircle ==null)
            testcircle = ZeeManagerIcons.imgCirle(8, Color.BLUE);
        g.image(testcircle,Coord.of(50));

        if (testtriangleup ==null)
            testtriangleup = ZeeManagerIcons.imgTriangleUp(8, Color.BLUE);
        g.image(testtriangleup,Coord.of(100));

        if (testtriangledown ==null)
            testtriangledown = ZeeManagerIcons.imgTriangleDown(8, Color.BLUE);
        g.image(testtriangledown,Coord.of(150));

        if (testdiamond==null)
            testdiamond = ZeeManagerIcons.imgDiamond(8, Color.BLUE);
        g.image(testdiamond,Coord.of(200));
    }

    private static void println(String s) {
        ZeeConfig.println(s);
    }
}

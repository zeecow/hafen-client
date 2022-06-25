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
                retImg = imgTriangle(num,c);

            // store and return
            mapRuleImg.put(rules[i],retImg);
            println("mapRuleImg size "+mapRuleImg.size());
            return retImg;
        }
        return null;
    }

    private static BufferedImage imgSquare(int side, Color c) {
        return imgRect(side,side,c);
    }

    private static BufferedImage imgCirle(int diameter, Color c) {
        return imgOval(diameter,diameter,c);
    }

    private static BufferedImage imgTriangle(int size, Color c) {
        return imgPolygon(size, size, new int[]{size/2,0,size}, new int[]{0,size,size}, 3, c);
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
}

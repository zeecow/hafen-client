package haven;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class ZeeManagerIcons {

    public static Coord2d lastMinimapClick;
    static List<Gob> gobs = new ArrayList<>();
    static HashMap<String,BufferedImage> mapRuleImg = new HashMap<String,BufferedImage>();

    public static BufferedImage getIconImage(Gob gob) {

        if (gob==null || gob.getres()==null || ZeeConfig.shapeIconsList.isBlank())
            return null;

        BufferedImage retImg = null;
        String gobName = gob.getres().name;
        String[] ruleArr, ruleName;
        String[] rules = ZeeConfig.shapeIconsList.split(";");

        //check all rules for gobName
        for (int i = 0; i < rules.length; i++) {

            // rule example: "/horse/ 1,square 6 0 1,0 255 0;"
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

            retImg = generateImage(ruleArr);

            // store and return
            mapRuleImg.put(rules[i],retImg);
            return retImg;
        }
        return null;
    }

    public static BufferedImage generateImage(String[] ruleArr) {

        BufferedImage retImg = null;
        String[] ruleShape, ruleColor;

        // generate color
        ruleColor = ruleArr[2].split(" ");
        Color c = new Color( Integer.parseInt(ruleColor[0]), Integer.parseInt(ruleColor[1]), Integer.parseInt(ruleColor[2]));

        // generate image ("/horse/ 1,square 6 0 1 0,0 255 0")
        ruleShape = ruleArr[1].split(" ");
        int size = Integer.parseInt(ruleShape[1]);
        boolean border = !ruleShape[2].contentEquals("0");
        boolean shadow = !ruleShape[3].contentEquals("0");
        boolean antiAliasing = !ruleShape[4].contentEquals("0");
        if (ruleShape[0].contentEquals("circle"))
            retImg = imgCirle(size,c,border,shadow,antiAliasing);
        else if (ruleShape[0].contentEquals("square"))
            retImg = imgSquare(size,c,border,shadow,antiAliasing);
        else if (ruleShape[0].contentEquals("triangleUp"))
            retImg = imgTriangleUp(size,c,border,shadow,antiAliasing);
        else if (ruleShape[0].contentEquals("triangleDown"))
            retImg = imgTriangleDown(size,c,border,shadow,antiAliasing);
        else if (ruleShape[0].contentEquals("diamond"))
            retImg = imgDiamond(size,c,border,shadow,antiAliasing);
        else if (ruleShape[0].contentEquals("boat"))
            retImg = imgBoat(size,c,border,shadow,antiAliasing);
        else if (ruleShape[0].contentEquals("star"))
            retImg = imgStar4(size,c,border,shadow,antiAliasing);

        return retImg;
    }

    public static BufferedImage imgSquare(int side, Color c, boolean border, boolean shadow,boolean antiAliasing) {
        return imgRect(side,side,c,border,shadow?1:0,antiAliasing);
    }

    public static BufferedImage imgCirle(int diameter, Color c, boolean border, boolean shadow, boolean antiAliasing) {
        if (diameter < 5)
            diameter = 5;
        return imgOval(diameter,diameter,c,border,shadow?1:0,antiAliasing);
    }

    public static BufferedImage imgTriangleUp(int s, Color c, boolean border, boolean shadow, boolean antiAliasing) {
        return imgPolygon(s, s,
            new int[]{ s/2, 0, s}, // x points
            new int[]{ 0, s, s}, // y points
            3, c, border, shadow?1:0, antiAliasing
        );
    }

    public static BufferedImage imgTriangleDown(int s, Color c, boolean border, boolean shadow, boolean antiAliasing) {
        return imgPolygon(s, s,
                new int[]{ 0, s, s/2}, // x points
                new int[]{ 0, 0, s}, // y points
                3, c, border, shadow?1:0, antiAliasing
        );
    }

    public static BufferedImage imgDiamond(int s, Color c, boolean border, boolean shadow, boolean antiAliasing) {
        if (s % 2 > 0)
            s++; // only even works
        return imgPolygon(s, s,
            new int[]{ s/2, 0, s/2, s }, // x points
            new int[]{ 0, s/2, s, s/2 }, // y points
            4, c, border, shadow?1:0, antiAliasing
        );
    }

    public static BufferedImage imgBoat(int s, Color c, boolean border, boolean shadow, boolean antiAliasing) {
        return imgPolygon(s*2, s,
                new int[]{ 0, s, s-(s/4), s/4 }, // x points
                new int[]{ s/2, s/2, s, s }, // y points
                4, c, border, shadow?1:0, antiAliasing
        );
    }

    public static BufferedImage imgRect(int w, int h, Color c, boolean border, int shadow, boolean antiAliasing) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w+shadow, h+shadow, type);
        Graphics2D g2d = ret.createGraphics();

        if(antiAliasing)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // draw shadow
        if (shadow > 0){
            g2d.setColor(Color.BLACK);
            g2d.fillRect(shadow, shadow, w-1, h-1);
        }

        // fill rectangle
        g2d.setColor(c);
        g2d.fillRect(0, 0, w-shadow, h-shadow);

        // draw border
        if (border) {
            g2d.setColor(ZeeConfig.getComplementaryColor(c));
            drawRectFix(g2d,0, 0, w-shadow, h-shadow);
        }

        g2d.dispose();
        return ret;
    }

    static BufferedImage imgOval(int w, int h, Color c, boolean border, int shadow, boolean antiAliasing) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w+shadow, h+shadow, type);
        Graphics2D g2d = ret.createGraphics();

        if(antiAliasing)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // drawfill shadow
        if (shadow > 0){
            g2d.setColor(Color.BLACK);
            g2d.fillOval(shadow, shadow, w-2, h-2);
            g2d.drawOval(shadow, shadow, w-2, h-2);
        }

        // fill circle
        g2d.setColor(c);
        g2d.fillOval(0, 0, w-shadow-1, h-shadow-1);

        //draw border
        if (border)
            g2d.setColor(ZeeConfig.getComplementaryColor(c));
        g2d.drawOval(0, 0, w - shadow - 1, h - shadow - 1);//always draw border to fix circl shape

        g2d.dispose();
        return ret;
    }

    static BufferedImage imgStar4(int size, Color c, boolean border, boolean shadow, boolean antiAliasing){
        int s = size;
        int s2 = s/2;
        int s4 = s2/4;
        return imgPolygon(
            size,
            size,
            new int[]{  0, s2-s4, s2, s2+s4,   s, s2+s4,  s2, s2-s4}, // x points
            new int[]{ s2, s2-s4,  0, s2-s4,  s2, s2+s4,   s, s2+s4}, // y points
            8,
            c,
            border,
            (shadow ? 1 : 0),
            antiAliasing
        );
    }

    // 0,0 = top left ; w,h = bottom right
    static BufferedImage imgPolygon(int w, int h, int[] xPoints, int[] yPoints, int points, Color c, boolean border, int shadow, boolean antiAliasing) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = new BufferedImage(w+shadow+1, h+shadow+1, type);
        Graphics2D g2d = ret.createGraphics();

        if(antiAliasing)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // draw shadow
        if (shadow > 0) {
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < xPoints.length; i++) {
                xPoints[i] += shadow ;
                yPoints[i] += shadow ;
            }
            g2d.fillPolygon(xPoints,yPoints,points);
            g2d.drawPolygon(xPoints,yPoints,points);
            for (int i = 0; i < xPoints.length; i++) {
                xPoints[i] -= shadow ;
                yPoints[i] -= shadow ;
            }
        }

        // fill polygon
        g2d.setColor(c);
        g2d.fillPolygon(xPoints,yPoints,points);

        // draw border always to correct outline
        if (border) {
            g2d.setColor(ZeeConfig.getComplementaryColor(c));
        }
        g2d.drawPolygon(xPoints, yPoints, points);

        g2d.dispose();
        return ret;
    }

    public static void drawRectFix(Graphics g, int x, int y, int w, int h) {
        g.drawRect(x, y, w - 1, h - 1);
    }

    public static void drawIcons(GOut g, MiniMap.Location sessloc, MiniMap.Location dloc, int dlvl, Coord sz) {
        if( !ZeeConfig.showIconsZoomOut  ||  dlvl > 2 )
            return;
        if((sessloc == null) || (dloc == null) || (dloc.seg != sessloc.seg))
            return;
        Coord sc;
        BufferedImage img;
        for (Gob gob : gobs) {
            img = getIconImage(gob);
            if (img==null || gob.rc==null)
                continue;
            sc = UI.scale(gob.rc.floor(MCache.tilesz).add(sessloc.tc).sub(dloc.tc).div(1 << dlvl)).mul(MiniMap.scale).add(sz.div(2));
            g.image(img, sc.sub(Coord.of(img.getWidth()/2)));
        }
    }

    public static void addQueue(Gob gob) {
        gobs.add(gob);
    }

    public static void clearQueue() {
        gobs.clear();
    }

    public static BufferedImage resizeBufferedImage(BufferedImage original, int w, int h) {
        BufferedImage resized = new BufferedImage(w, h, original.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, w, h, 0, 0, original.getWidth(),
                original.getHeight(), null);
        g.dispose();
        return resized;
    }

    static class ShapeIconsOptPanel extends JPanel{

        static JComboBox comboAllRules, nameCombo, shapeCombo;
        static JTextField nameTF;
        static JPanel panelTop, panelCenter, panelBottom;
        static JSpinner jspIconSize;
        static JButton btnGobColor,btnDrawOrderUp,btnDrawOrderDown;
        static JCheckBox cbBorder, cbShadow, cbAntiAliasing;
        static JLabel lblDrawOrder;

        public ShapeIconsOptPanel(JComboBox<String> comboAllRules){
            this.comboAllRules = comboAllRules;
            this.setLayout(new BorderLayout());
            this.add(panelTop = new JPanel(new BorderLayout()), BorderLayout.NORTH);
            this.add(panelCenter = new JPanel(new BorderLayout()), BorderLayout.CENTER);
            this.add(panelBottom = new JPanel(new FlowLayout(FlowLayout.LEFT)), BorderLayout.SOUTH);

            // draw order buttons
            if(comboAllRules.getSelectedIndex()!=0) {
                JPanel panUpDown = new JPanel(new FlowLayout(FlowLayout.LEFT));
                panelTop.add(panUpDown, BorderLayout.NORTH);
                panUpDown.add(lblDrawOrder = new JLabel("Draw order "));
                panUpDown.add(btnDrawOrderUp = new JButton("up"));
                btnDrawOrderUp.addActionListener(evt -> {
                    moveDrawOrderUp();
                });
                panUpDown.add(btnDrawOrderDown = new JButton("down"));
                btnDrawOrderDown.addActionListener(evt -> {
                    moveDrawOrderDown();
                });
            }

            // gob name and rule
            JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pan.add(new JLabel("Gob name"));
            pan.add(nameCombo = new JComboBox<>(new String[]{"startsWith", "contains", "endsWith"}));
            panelTop.add(pan, BorderLayout.CENTER);
            panelTop.add(nameTF = new JTextField(),BorderLayout.SOUTH);//gob query
            nameTF.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            // combo shape, size
            pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panelCenter.add(pan,BorderLayout.NORTH);
            pan.add(new JLabel("Icon:"));
            pan.add(shapeCombo = new JComboBox<>(new String[]{"boat","circle","diamond","square", "star","triangleUp", "triangleDown"}));
            SpinnerNumberModel model = new SpinnerNumberModel(3, 3, 10, 1);
            jspIconSize = new JSpinner(model);
            jspIconSize.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            pan.add(new JLabel("size"));
            pan.add(jspIconSize);

            // checkbox border, shadow
            pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panelCenter.add(pan,BorderLayout.CENTER);
            pan.add(cbBorder = new JCheckBox("border"));
            pan.add(cbShadow = new JCheckBox("shadow"));
            pan.add(cbAntiAliasing = new JCheckBox("anti-aliasing"));

            // button icon color
            panelBottom.add(btnGobColor = new JButton("Icon color"));
            btnGobColor.addActionListener(evt->{
                Color color = JColorChooser.showDialog(panelBottom, "Gob Highlight Color", null, false);
                if(color!=null){
                    btnGobColor.setBackground(color);
                    btnGobColor.setForeground(ZeeConfig.getComplementaryColor(color));
                }
            });

            if (comboAllRules.getSelectedIndex() > 0  &&  !comboAllRules.getSelectedItem().toString().isBlank())
                fillData(comboAllRules.getSelectedItem().toString(), comboAllRules.getSelectedIndex());
        }

        @SuppressWarnings("unchecked")
        private void moveDrawOrderDown() {
            int selIndex = comboAllRules.getSelectedIndex();
            int size = comboAllRules.getModel().getSize() - 1;
            if (selIndex <= 1) {
                //println("already bottom");
                return;
            }
            String temp;
            String value = comboAllRules.getSelectedItem().toString();
            String[] arr = ZeeConfig.shapeIconsList.split(";");
            boolean rebuild = false;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].contentEquals(value) && i>0){
                    temp = arr[i-1];
                    arr[i-1] = arr[i];
                    arr[i] = temp;
                    rebuild = true;
                    break;
                }
            }
            if (rebuild) {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(arr);
                model.insertElementAt(" ",0);
                model.setSelectedItem(value);
                comboAllRules.setModel(model);
                lblDrawOrder.setText("Draw order " + (comboAllRules.getSelectedIndex()) + "/" + (comboAllRules.getModel().getSize()-1));
                ZeeConfig.shapeIconsList = String.join(";",arr);
                // save pref
                Utils.setpref("shapeIconsList",ZeeConfig.shapeIconsList);
                //println("save list > "+ZeeConfig.shapeIconsList);
            }
        }

        @SuppressWarnings("unchecked")
        private void moveDrawOrderUp() {
            int selIndex = comboAllRules.getSelectedIndex();
            int size = comboAllRules.getModel().getSize() - 1;
            if (selIndex >= size) {
                //println("already on top");
                return;
            }
            String temp;
            String value = comboAllRules.getSelectedItem().toString();
            String[] arr = ZeeConfig.shapeIconsList.split(";");
            boolean rebuild = false;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].contentEquals(value) && i<arr.length-1){
                    temp = arr[i+1];
                    arr[i+1] = arr[i];
                    arr[i] = temp;
                    rebuild = true;
                    break;
                }
            }
            if (rebuild) {
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(arr);
                model.insertElementAt(" ",0);
                model.setSelectedItem(value);
                comboAllRules.setModel(model);
                lblDrawOrder.setText("Draw order " + (comboAllRules.getSelectedIndex()) + "/" + (comboAllRules.getModel().getSize()-1));
                ZeeConfig.shapeIconsList = String.join(";",arr);
                // save pref
                Utils.setpref("shapeIconsList",ZeeConfig.shapeIconsList);
                //println("save list > "+ZeeConfig.shapeIconsList);
            }
        }

        private static void fillData(String selectedValue, int selectedIndex){
            //  "/horse/ 1,square 6 0 1,0 255 0"
            String[] arr = selectedValue.split(",");
            String[] arrGobName = arr[0].split(" ");
            String[] arrShape = arr[1].split(" ");
            String[] arrColor = arr[2].split(" ");
            lblDrawOrder.setText("Draw order " + (selectedIndex) + "/" + (comboAllRules.getModel().getSize()-1));
            nameCombo.setSelectedIndex(Integer.parseInt(arrGobName[1]));
            nameTF.setText(arrGobName[0]);//gob query
            shapeCombo.setSelectedItem(arrShape[0]);//shape name
            int shapeSize = Integer.parseInt(arrShape[1]);
            cbBorder.setSelected(!arrShape[2].contentEquals("0"));
            cbShadow.setSelected(!arrShape[3].contentEquals("0"));
            cbAntiAliasing.setSelected(!arrShape[4].contentEquals("0"));
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
            String border = cbBorder.isSelected() ? "1" : "0";
            String shadow = cbShadow.isSelected() ? "1" : "0";
            String antiAliasing = cbAntiAliasing.isSelected() ? "1" : "0";
            String rule =  nameTF.getText() +" "+ nameCombo.getSelectedIndex() +","+
                shapeCombo.getSelectedItem().toString() +" "+ jspIconSize.getValue() +" "+ border +" "+ shadow +" "+ antiAliasing +","+
                btnGobColor.getBackground().getRed() +" "+
                btnGobColor.getBackground().getGreen() +" "+
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

    static BufferedImage[] testcircle, testtriangleup, testtriangledown, testdiamond, testboat;
    public static void testIconsLoginScreen(GOut g) {
        if (testcircle ==null) {
            testcircle = new BufferedImage[5];
            for (int i = 0; i < 5; i++) {
                testcircle[i] = ZeeManagerIcons.imgCirle(10-i, Color.BLUE, false, true,false);
            }
        }
        for (int i = 0; i < testcircle.length; i++) {
            g.image(testcircle[i],Coord.of(50+(i*20), 50));
        }

        if (testtriangleup ==null) {
            testtriangleup = new BufferedImage[5];
            for (int i = 0; i < 5; i++) {
                testtriangleup[i] = ZeeManagerIcons.imgTriangleUp(10-i, Color.BLUE, false, true,false);
            }
        }
        for (int i = 0; i < testtriangleup.length; i++) {
            g.image(testtriangleup[i],Coord.of(50+(i*20), 100));
        }


        if (testtriangledown ==null) {
            testtriangledown = new BufferedImage[5];
            for (int i = 0; i < 5; i++) {
                testtriangledown[i] = ZeeManagerIcons.imgTriangleDown(10-i, Color.BLUE, false, true,false);
            }
        }
        for (int i = 0; i < testtriangledown.length; i++) {
            g.image(testtriangledown[i],Coord.of(50+(i*20), 150));
        }


        if (testdiamond ==null) {
            testdiamond = new BufferedImage[5];
            for (int i = 0; i < 5; i++) {
                testdiamond[i] = ZeeManagerIcons.imgDiamond(10-i, Color.BLUE, false, true,false);
            }
        }
        for (int i = 0; i < testdiamond.length; i++) {
            g.image(testdiamond[i],Coord.of(50+(i*20), 200));
        }


        if (testboat ==null) {
            testboat = new BufferedImage[5];
            for (int i = 0; i < 5; i++) {
                testboat[i] = ZeeManagerIcons.imgBoat(10-i, Color.BLUE, false, true,false);
            }
        }
        for (int i = 0; i < testboat.length; i++) {
            g.image(testboat[i],Coord.of(50+(i*30), 250));
        }
    }


    static MiniMap.DisplayMarker latestMidclickMark;
    static BufferedImage latestFocusedMarkBgImg = ZeeManagerIcons.imgCirle(MiniMap.DisplayMarker.minimapMarkImg.getWidth()+1,Color.black,false,false,false);
    public static void checkMarkClicked(MiniMap.DisplayMarker mark, int button, boolean mapCompact) {

        if (button==3)
            return;
        if (mapCompact && button!=2)
            return;

        new ZeeThread(){
            public void run() {
                try {
                    MapWnd map = ZeeConfig.gameUI.mapfile;

                    //expand map
                    if (button == 2 && mapCompact) {
                        map.compact(false);
                        Utils.setprefb("compact-map", false);
                        sleep(PING_MS);
                    }

                    // select proper mark list
                    selectProperList(mark.m);
                    sleep(PING_MS);

                    //select mark from list
                    map.focus(mark.m);

                    //center mark on the map
                    map.view.center(new MiniMap.SpecLocator(mark.m.seg, mark.m.tc));

                    //??
                    if (ZeeManagerIcons.latestMidclickMark != null){
                        ZeeManagerIcons.latestMidclickMark.isListFocused = false;
                    }
                    mark.isListFocused = true;
                    latestMidclickMark = mark;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    static void selectProperList(MapFile.Marker m){
        MapWnd map = ZeeConfig.gameUI.mapfile;
        if (m.getClass().getSimpleName().contentEquals("SMarker")){
            map.tool.smbtn.click();
        }
        else if (m.getClass().getSimpleName().contentEquals("PMarker")){
            map.tool.pmbtn.click();
        }
    }


    public static BufferedImage getSolidColorTile(BufferedImage tileImg, boolean minimapTile) {
        Color color;
        if (minimapTile && ZeeConfig.desaturateMinimap)
            color = getAverageDesaturatedGrayFromImage(tileImg,0,0,tileImg.getWidth(),tileImg.getHeight(),0.7);
        else
            color = getAverageColorFromImage(tileImg,0,0,tileImg.getWidth(),tileImg.getHeight());
        BufferedImage ret = new BufferedImage(tileImg.getWidth(),tileImg.getHeight(),BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ret.createGraphics();
        g.setColor ( color );
        g.fillRect ( 0, 0, ret.getWidth(), ret.getHeight() );
        g.dispose();
        return ret;
    }
    static Color getAverageColorFromImage(BufferedImage image, int upperLeftX, int upperLeftY, int width, int height) {
        int x1 = upperLeftX + width;
        int y1 = upperLeftY + height;
        long sumr = 0, sumg = 0, sumb = 0;
        for (int x = upperLeftX; x < x1; x++) {
            for (int y = upperLeftY; y < y1; y++) {
                Color pixel = new Color(image.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }
        int num = width * height;
        return new Color( (int) (sumr / num), (int) (sumg / num), (int) (sumb / num));
    }
    static Color getAverageDesaturatedGrayFromImage(BufferedImage image, int upperLeftX, int upperLeftY, int width, int height, double desatFactor) {
        int x1 = upperLeftX + width;
        int y1 = upperLeftY + height;

        long sumr = 0, sumg = 0, sumb = 0;
        for (int x = upperLeftX; x < x1; x++) {
            for (int y = upperLeftY; y < y1; y++) {
                int rgb = image.getRGB(x, y);
                sumr += (rgb >> 16) & 0xFF;
                sumg += (rgb >> 8) & 0xFF;
                sumb += rgb & 0xFF;
            }
        }

        int num = width * height;
        if (num <= 0) return new Color(0, 0, 0);

        int avgR = (int) (sumr / num);
        int avgG = (int) (sumg / num);
        int avgB = (int) (sumb / num);

        // clamp factor
        desatFactor = Math.max(0.0, Math.min(1.0, desatFactor));

        // perceptual luminance (rec. 709)
        double lum = 0.2126 * avgR + 0.7152 * avgG + 0.0722 * avgB;

        int rr = (int) Math.round(avgR + desatFactor * (lum - avgR));
        int gg = (int) Math.round(avgG + desatFactor * (lum - avgG));
        int bb = (int) Math.round(avgB + desatFactor * (lum - avgB));

        rr = clamp(rr, 0, 255);
        gg = clamp(gg, 0, 255);
        bb = clamp(bb, 0, 255);

        return new Color(rr, gg, bb);
    }

    static int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }


    private static void println(String s) {
        ZeeConfig.println(s);
    }
}

package haven;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.*;

public class ZeeOptionsJFrame extends JFrame {
    public GridBagConstraints c;
    public JTabbedPane tabbedPane, tabbedPaneGobs;
    public JPanel panelTabAuto, panelTabMisc, panelTabInterface, panelTabAudio, panelTabGobs, panelTabControls, panelTabMinimap, panelDetailsBottom, panelTabCateg, panelShapeIcons, panelShapeIconsSaveCancel;
    public JCheckBox cbSimpleWindowBorder, cbSimpleWindows, cbShapeIcons, cbDebugWdgMsg, cbDebugCodeRes, cbCattleRosterHeight,cbMidiRadio;
    public JTextField tfAutoHideWindows, tfAutoHideWindowDelayMs, tfConfirmPetal, tfBlockAudioMsgs, tfAutoClickMenu, tfAggroRadiusTiles, tfButchermode, tfGobName, tfGobSpeech, tfAudioPath, tfCategName, tfAudioPathCateg, tfGobMaxRequeues;
    public JComboBox<String> cmbCattleRoster, cmbGobCategory, cmbMiniTreeSize, cmbRainLimitPerc, comboShapeIcons;
    public JList<String> listGobsTemp, listGobsSaved, listGobsCategories;
    public JButton btnRefresh, btnPrintState, btnResetGobs, btnAudioSave, btnAudioClear, btnAudioTest, btnRemGobFromCateg, btnGobColorAdd, btnCategoryColorAdd, btnGobColorRemove, btnCategoryColorRemove, btnResetCateg, btnAddCateg, btnRemoveCateg, btnResetWindowsPos, btnResetActionUses, btnSapeIconPreview, btnShapeIconSave, btnShapeIconDelete, btnSolidColorWindow, btnGridColor, btnHitboxColor, btnSelMidiFolder;
    public JTextArea txtAreaDebug;
    public static int TABGOB_SESSION = 0;
    public static int TABGOB_SAVED = 1;
    public static int TABGOB_CATEGS = 2;

    public ZeeOptionsJFrame(){
        super.setTitle("Zeecow Haven Options");
        setLocationRelativeTo(null);//center window
        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContents();
        pack();
        setVisible(true);
    }

    @Override
    public void dispose() {
        ZeeConfig.zeecowOptions = null;
        super.dispose();
    }

    private void setContents() {
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; // natural height, maximum width
        c.gridwidth = GridBagConstraints.REMAINDER; // last one in its row

        tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane);

        buildTabAuto();

        buildTabMisc();

        buildTabInterface();

        buildTabAudio();

        buildTabControls();

        buildTabMinimap();

        //Gobs tabbbed pane
        panelTabGobs = new JPanel(new BorderLayout());
        tabbedPane.addTab("Gobs", panelTabGobs);
        buildTabGobs();
    }


    private void buildTabControls() {

        JPanel pan;

        panelTabControls = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Controls", panelTabControls);

        panelTabControls.add(new ZeeOptionJCheckBox("free gob placement","freeGobPlacement"),c);

        panelTabControls.add(new ZeeOptionJCheckBox("scroll transfer items directly","scrollTransferItems"),c);

        panelTabControls.add(new ZeeOptionJCheckBox( "Alt+click drops holding item", "dropHoldingItemAltKey"),c);

        panelTabControls.add(new ZeeOptionJCheckBox( "Key up/down controls volume", "keyUpDownAudioControl"),c);

        panelTabControls.add(new ZeeOptionJCheckBox( "Shift+C switch cams", "keyCamSwitchShiftC"),c);

        panelTabControls.add(new ZeeOptionJCheckBox( "Drink key ' (single quote) ", "drinkKey"),c);

        // confirmThrowingAxeOrSpear
        panelTabControls.add(new ZeeOptionJCheckBox("Ctrl confirm throwing axe/spear","confirmThrowingAxeOrSpear"),c);

        // confirm petal "Eat" reducing Food Efficacy (energy>80%)
        panelTabControls.add(new ZeeOptionJCheckBox("Ctrl confirm petal Eat","confirmPetalEatReduceFoodEff"),c);

        //confirm petal list
        panelTabControls.add(new ZeeOptionJCheckBox( "Ctrl+click confirm petal:", "confirmPetal"),c);
        panelTabControls.add(tfConfirmPetal= new JTextField("",5), c);
        tfConfirmPetal.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfConfirmPetal.getPreferredSize().height));
        tfConfirmPetal.setText(ZeeConfig.confirmPetalList);
        tfConfirmPetal.addActionListener(actionEvent -> {
            String str = actionEvent.getActionCommand();
            String[] strArr = str.split(",");
            if(strArr!=null && strArr.length>0) {
                ZeeConfig.confirmPetalList = str;
                Utils.setpref("confirmPetalList",str.strip());
            }
        });
    }


    private void buildTabMinimap() {

        panelTabMinimap = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Minimap", panelTabMinimap);

        panelTabMinimap.add(new ZeeOptionJCheckBox( "Show gob pointer", "showGobPointer"),c);

        panelTabMinimap.add(new ZeeOptionJCheckBox( "Show gob radar", "showGobRadar"),c);

        panelTabMinimap.add(new ZeeOptionJCheckBox( "Solid colors map", "minimapSolidColor"),c);

        panelTabMinimap.add(new ZeeOptionJCheckBox( "Slower map", "slowMiniMap"),c);

        panelTabMinimap.add(new ZeeOptionJCheckBox( "Show icons while zoomed out", "showIconsZoomOut"),c);


        // checkbox shape icons
        panelTabMinimap.add(cbShapeIcons = new ZeeOptionJCheckBox( "Show basic shape icons", "shapeIcons"),c);
        cbShapeIcons.addActionListener(actionEvent -> {
            comboShapeIcons.setSelectedIndex(0);
            if (panelShapeIcons!=null){
                removePanelShape(actionEvent);
            }
        });

        // combobox shape icons
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(ZeeConfig.shapeIconsList.split(";"));
        model.insertElementAt(" ",0);//empty option
        comboShapeIcons = new JComboBox<String>(model);
        comboShapeIcons.setSelectedIndex(0);
        panelTabMinimap.add(comboShapeIcons, c);
        comboShapeIcons.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboShapeIcons.getPreferredSize().height));
        comboShapeIcons.setEnabled(ZeeConfig.shapeIcons);
        comboShapeIcons.addActionListener(e -> {
            if (panelShapeIcons!=null){
                removePanelShape(e);
            }
            panelTabMinimap.add(panelShapeIcons = new ZeeManagerIcons.ShapeIconsOptPanel(comboShapeIcons),c);
            panelTabMinimap.add(panelShapeIconsSaveCancel = new JPanel(new FlowLayout(FlowLayout.LEFT)),c);
            panelShapeIconsSaveCancel.add(btnSapeIconPreview = new JButton("Preview"));
            btnSapeIconPreview.addActionListener(this::previewShapeIcons);
            panelShapeIconsSaveCancel.add(btnShapeIconSave = new JButton("Save as new"));
            btnShapeIconSave.addActionListener(this::saveShapeIcons);
            panelShapeIconsSaveCancel.add(btnShapeIconDelete = new JButton("Delete"));
            btnShapeIconDelete.addActionListener(this::deleteShapeIcons);
            pack();
            repaint();
        });
    }


    private void previewShapeIcons(ActionEvent evt){
        String rule = ZeeManagerIcons.ShapeIconsOptPanel.getRule(this);
        if (rule==null)
            return;
        btnSapeIconPreview.setVerticalTextPosition(SwingConstants.CENTER);
        btnSapeIconPreview.setHorizontalTextPosition(SwingConstants.LEFT);
        btnSapeIconPreview.setIcon(new ImageIcon(ZeeManagerIcons.generateImage(rule.split(","))));
    }

    private void deleteShapeIcons(ActionEvent evt) {
        //save list
        String rule = ZeeManagerIcons.ShapeIconsOptPanel.getRule(this);
        if (rule==null)
            return;
        if(!ZeeConfig.shapeIconsList.contains(rule)) {
            JOptionPane.showMessageDialog(this,"rule doesn't exist");
            return;
        }
        List<String> linkedList = new LinkedList<>(Arrays.asList(ZeeConfig.shapeIconsList.split(";")));
        linkedList.remove(rule);
        String[] newArray = linkedList.toArray(new String[0]);
        String newList = String.join(";",newArray);
        ZeeConfig.println("new list > "+ newList);
        Utils.setpref("shapeIconsList",newList);
        ZeeConfig.shapeIconsList = newList;
        if(ZeeManagerIcons.mapRuleImg.containsKey(rule)){
            ZeeManagerIcons.mapRuleImg.remove(rule);
        }

        //update combo
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>(newList.split(";"));
        m.insertElementAt(" ",0);
        comboShapeIcons.setModel(m);
        removePanelShape(evt);
    }

    private void saveShapeIcons(ActionEvent evt) {
        //save list
        String rule = ZeeManagerIcons.ShapeIconsOptPanel.getRule(this);
        if (rule==null)
            return;
        if(ZeeConfig.shapeIconsList.contains(rule)) {
            JOptionPane.showMessageDialog(this,"rule already exist");
            return;
        }
        String newList = ZeeConfig.shapeIconsList;
        if (!newList.isBlank())
            newList += ";";
        newList += rule;
        ZeeConfig.println("new list > "+newList);
        Utils.setpref("shapeIconsList",newList);
        ZeeConfig.shapeIconsList = newList;

        //update combo
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>(newList.split(";"));
        m.insertElementAt(" ",0);
        comboShapeIcons.setModel(m);
        removePanelShape(evt);
    }

    private void removePanelShape(ActionEvent evt) {
        panelTabMinimap.remove(panelShapeIcons);
        panelTabMinimap.remove(panelShapeIconsSaveCancel);
        pack();
        repaint();
    }

    private void buildTabAuto(){

        panelTabAuto = new JPanel(new GridBagLayout());

        tabbedPane.addTab("Auto", panelTabAuto);

        panelTabAuto.add(new ZeeOptionJCheckBox( "Right-click aggro stoat", "clickIconStoatAggro"),c);

        panelTabAuto.add(new ZeeOptionJCheckBox( "Auto chip mined boulder", "autoChipMinedBoulder"),c);

        panelTabAuto.add(new ZeeOptionJCheckBox( "Drop mined stones", "dropMinedStones"),c);

        panelTabAuto.add(new ZeeOptionJCheckBox( "Drop mined ore", "dropMinedOre"),c);

        panelTabAuto.add(new ZeeOptionJCheckBox( "Drop mined silver/gold", "dropMinedOrePrecious"),c);

        panelTabAuto.add(new ZeeOptionJCheckBox( "Drop mined curios", "dropMinedCurios"),c);

        panelTabAuto.add(new ZeeOptionJCheckBox( "Auto-run on login", "autoRunLogin"),c);

        panelTabAuto.add(new ZeeOptionJCheckBox( "Auto-track scents", "autoTrackScents"),c);

        panelTabAuto.add(new ZeeOptionJCheckBox( "Sound alert on player sight", "alertOnPlayers"),c);

        panelTabAuto.add(new ZeeOptionJCheckBox("Travel hearth on player sight","autoHearthOnStranger"), c);

        panelTabAuto.add(new ZeeOptionJCheckBox("Equip shield on combat","equipShieldOnCombat"), c);

        panelTabAuto.add(new ZeeOptionJCheckBox("Lift vehicle before travel hearth","liftVehicleBeforeTravelHearth"), c);


        panelTabAuto.add(new ZeeOptionJCheckBox( "Research food tips", "researchFoodTips"),c);

        //auto click menu list
        panelTabAuto.add(new JLabel("Automenu list:"), c);
        panelTabAuto.add(tfAutoClickMenu = new JTextField("",5), c);
        tfAutoClickMenu.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfAutoClickMenu.getPreferredSize().height));
        tfAutoClickMenu.setText(ZeeConfig.autoClickMenuOptionList);
        tfAutoClickMenu.addActionListener(actionEvent -> {
            String str = actionEvent.getActionCommand();
            String[] strArr = str.split(",");
            if(strArr!=null && strArr.length>0) {
                ZeeConfig.autoClickMenuOptionList = str;
                Utils.setpref("autoClickMenuOptionList",str.strip());
            }
        });

        //butcher mode  list
        panelTabAuto.add(new JLabel("Butchermode list:"), c);
        panelTabAuto.add(tfButchermode= new JTextField("",5), c);
        tfButchermode.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfButchermode.getPreferredSize().height));
        tfButchermode.setText(ZeeConfig.butcherAutoList);
        tfButchermode.addActionListener(actionEvent -> {
            String str = actionEvent.getActionCommand();
            String[] strArr = str.split(",");
            if(strArr!=null && strArr.length>0) {
                ZeeConfig.butcherAutoList = str;
                Utils.setpref("butcherAutoList",str.strip());
            }
        });
    }


    private void buildTabMisc() {

        JPanel pan;

        panelTabMisc = new JPanel(new GridBagLayout());

        tabbedPane.addTab("Misc", panelTabMisc);

        //buttons table food, alchemy
        JPanel jpTables = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton b;
        panelTabMisc.add(jpTables,c);
        jpTables.add(b = new JButton("Table Food"));
        b.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                ZeeJTable.getTableFood();
            }
        });
        jpTables.add(b = new JButton("Table Swill"));
        b.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                ZeeJTable.getTableSwill();
            }
        });


        panelTabMisc.add(new ZeeOptionJCheckBox( "Solid color terrain (restart)", "terrainSolidColor"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Solid color paving (restart)", "pavingSolidColor"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Hide Gobs", "hideGobs"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Hide some animations", "hideFxAnimations"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Hide smoke effects", "hideFxSmoke"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Hide tile transitions", "hideTileTransitions"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Hide flavor objects(restart)", "noFlavObjs"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Simple herbs", "simpleHerbs"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Simple crops (restart)", "simpleCrops"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Highlight crops ready", "highlightCropsReady"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Show growing tree scale", "showGrowingTreeScale"),c);

        panelTabMisc.add(new ZeeOptionJCheckBox( "Show tree animation", "treeAnimation"),c);

        //mini trees
        pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTabMisc.add(pan,c);
        cmbMiniTreeSize = new JComboBox<String>(new String[]{"30%", "40%", "50%", "60%", "70%", "80%"});
        pan.add(new ZeeOptionJCheckBox( "Mini trees", "miniTrees"){
            protected void processMouseEvent(MouseEvent e) {
                super.processMouseEvent(e);
                cmbMiniTreeSize.setEnabled(ZeeConfig.miniTrees);
            }
        },c);
        pan.add(cmbMiniTreeSize, c);
        cmbMiniTreeSize.setSelectedItem(ZeeConfig.miniTreesSize+"%");
        cmbMiniTreeSize.setEnabled(ZeeConfig.miniTrees);
        cmbMiniTreeSize.addActionListener(e -> {
            String val = cmbMiniTreeSize.getSelectedItem().toString().split("%")[0];
            Integer num = ZeeConfig.miniTreesSize = Integer.parseInt(val);
            Utils.setprefi("miniTreesSize", num);
        });


        panelTabMisc.add(new ZeeOptionJCheckBox( "Hide Weather(restart)", "noWeather"),c);


        //rain rate limit
        pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTabMisc.add(pan,c);
        cmbRainLimitPerc = new JComboBox<String>(new String[]{"5%","12%","25%", "50%", "75%"});
        pan.add(new ZeeOptionJCheckBox( "Rain rate limit", "isRainLimited"){
            protected void processMouseEvent(MouseEvent e) {
                super.processMouseEvent(e);
                cmbRainLimitPerc.setEnabled(ZeeConfig.isRainLimited);
            }
        },c);
        //rain rate limit %
        pan.add(cmbRainLimitPerc, c);
        cmbRainLimitPerc.setSelectedItem(ZeeConfig.rainLimitPerc+"%");
        cmbRainLimitPerc.setEnabled(ZeeConfig.isRainLimited);
        cmbRainLimitPerc.addActionListener(e -> {
            String val = cmbRainLimitPerc.getSelectedItem().toString().split("%")[0];
            Integer num = ZeeConfig.rainLimitPerc = Integer.parseInt(val);
            Utils.setprefi("rainLimitPerc", num);
        });


        //agro radius tiles
        pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTabMisc.add(pan,c);
        pan.add(new JLabel("Aggro radius tiles"));
        pan.add(tfAggroRadiusTiles = new JTextField("",5));
        //tfAggroRadiusTiles.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfAggroRadiusTiles.getPreferredSize().height));
        tfAggroRadiusTiles.setText(""+ZeeConfig.aggroRadiusTiles);
        tfAggroRadiusTiles.addActionListener(actionEvent -> {
            String str = actionEvent.getActionCommand();
            str = str.strip();
            if(str!=null && str.chars().allMatch(Character::isDigit)) {
                ZeeConfig.aggroRadiusTiles = Integer.parseInt(str);
                Utils.setpref("aggroRadiusTiles",str);
            }
        });


        //Gob max requeues
        pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTabMisc.add(pan,c);
        pan.add(new JLabel("Gob max requeues (def "+ZeeConfig.DEF_GOB_MAX_REQUEUE+")"));
        pan.add(tfGobMaxRequeues = new JTextField("",5));
        //tfGobMaxRequeues.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfGobMaxRequeues.getPreferredSize().height));
        tfGobMaxRequeues.setText(""+ZeeConfig.gobMaxRequeues);
        tfGobMaxRequeues.addActionListener(actionEvent -> {
            String str = actionEvent.getActionCommand();
            str = str.strip();
            if(str!=null && str.chars().allMatch(Character::isDigit)) {
                ZeeConfig.gobMaxRequeues = Integer.parseInt(str);
                Utils.setpref("gobMaxRequeues",str);
            }
        });


        // debug wdgmsg
        panelTabMisc.add(cbDebugWdgMsg= new JCheckBox("Debug widget msgs"), c);
        cbDebugWdgMsg.setSelected(ZeeConfig.debugWidgetMsgs);
        cbDebugWdgMsg.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            ZeeConfig.debugWidgetMsgs = cb.isSelected();
        });

        //debug code
        panelTabMisc.add(cbDebugCodeRes= new JCheckBox("Debug code"), c);
        cbDebugCodeRes.setSelected(ZeeConfig.debugCodeRes);
        cbDebugCodeRes.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.debugCodeRes = cb.isSelected();
            Utils.setprefb("debugCodeRes",val);
        });

    }



    private void buildTabInterface() {

        JPanel pan;

        panelTabInterface = new JPanel(new GridBagLayout());
        tabbedPane.addTab("UI", panelTabInterface);

        panelTabInterface.add(new ZeeOptionJCheckBox( "Fish Moon Xp alert", "fishMoonXpAlert"),c);

        panelTabInterface.add(new ZeeOptionJCheckBox( "Show kin names (hearthfire)", "showKinNames"),c);

        panelTabInterface.add(cbSimpleWindows = new JCheckBox("Solid color windows"), c);
        cbSimpleWindows.setEnabled(ZeeConfig.gameUI!=null);
        cbSimpleWindows.setSelected(ZeeConfig.simpleWindows);
        cbSimpleWindows.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.simpleWindows = cb.isSelected();
            Utils.setprefb("simpleWindows",val);
            if(ZeeConfig.gameUI == null)
                val = false;
            btnSolidColorWindow.setEnabled(val);
            cbSimpleWindowBorder.setEnabled(val);
        });

        // simple window color
        pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTabInterface.add(pan,c);
        pan.add(btnSolidColorWindow = new JButton("color"));
        Color colBtn = ZeeConfig.intToColor(ZeeConfig.simpleWindowColorInt);
        btnSolidColorWindow.setEnabled(ZeeConfig.gameUI!=null);
        btnSolidColorWindow.setBackground(colBtn);
        btnSolidColorWindow.setForeground(ZeeConfig.getComplementaryColor(colBtn));
        btnSolidColorWindow.addActionListener(evt->{
            Color color = JColorChooser.showDialog(panelTabInterface, "Pick Color", ZeeConfig.intToColor(ZeeConfig.simpleWindowColorInt), false);
            if (color==null)
                color = ZeeConfig.DEF_SIMPLE_WINDOW_COLOR;
            btnSolidColorWindow.setBackground(color);
            btnSolidColorWindow.setForeground(ZeeConfig.getComplementaryColor(color));
            ZeeConfig.simpleWindowColorInt = ZeeConfig.colorToInt(color);
            Utils.setprefi("simpleWindowColorInt",ZeeConfig.simpleWindowColorInt);
            ZeeConfig.simpleWindowsUpdateAll();
        });
        pan.add(cbSimpleWindowBorder = new JCheckBox("auto-color border"));
        cbSimpleWindowBorder.setEnabled(ZeeConfig.gameUI!=null);
        cbSimpleWindowBorder.setSelected(ZeeConfig.simpleWindowBorder);
        cbSimpleWindowBorder.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.simpleWindowBorder  = cb.isSelected();
            Utils.setprefb("simpleWindowBorder",val);
            ZeeConfig.simpleWindowsUpdateAll();
        });

        panelTabInterface.add(new ZeeOptionJCheckBox( "Simple buttons (logoff)", "simpleButtons"),c);

        panelTabInterface.add(new ZeeOptionJCheckBox( "Compact equip window(restart)", "equiporyCompact"),c);

        panelTabInterface.add(new ZeeOptionJCheckBox( "Auto toggle equips window", "autoToggleEquips"),c);

        panelTabInterface.add(new ZeeOptionJCheckBox( "Remember windows pos", "rememberWindowsPos"),c);

        panelTabInterface.add(btnResetWindowsPos = new JButton("reset windows pos ("+ZeeConfig.mapWindowPos.size()+")"), c);
        btnResetWindowsPos.addActionListener(evt -> {
            resetWindowsPos();
        });

        panelTabInterface.add(new ZeeOptionJCheckBox( "Sort actions by uses", "sortActionsByUses"),c);

        panelTabInterface.add(btnResetActionUses = new JButton("reset actions uses ("+ZeeConfig.mapActionUses.size()+")"), c);
        btnResetActionUses.addActionListener(evt -> {
            resetActionUses();
        });

        //cattle roster height
        panelTabInterface.add(cbCattleRosterHeight = new JCheckBox("Cattle Roster height(logout)"), c);
        cbCattleRosterHeight.setSelected(ZeeConfig.cattleRosterHeight);
        cbCattleRosterHeight.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.cattleRosterHeight = cb.isSelected();
            Utils.setprefb("cattleRosterHeight",val);
            cmbCattleRoster.setEnabled(val);
        });
        String[] perc = {"20%","30%","40%","50%","60%","70%","80%","90%","100%"};
        panelTabInterface.add(cmbCattleRoster = new JComboBox<String>(perc), c);
        cmbCattleRoster.setMaximumSize(new Dimension(Integer.MAX_VALUE, cmbCattleRoster.getPreferredSize().height));
        cmbCattleRoster.setSelectedItem(((int)(ZeeConfig.cattleRosterHeightPercentage*100))+"%");
        cmbCattleRoster.setEnabled(ZeeConfig.cattleRosterHeight);
        cmbCattleRoster.addActionListener(e -> {
            String val = cmbCattleRoster.getSelectedItem().toString().split("%")[0];
            double d = ZeeConfig.cattleRosterHeightPercentage = Double.parseDouble(val) / 100;
            Utils.setprefd("cattleRosterHeightPercentage", d);
        });

        panelTabInterface.add(new ZeeOptionJCheckBox( "Auto-toggle grid lines", "autoToggleGridLines"),c);

        // grid lines color
        pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTabInterface.add(pan,c);
        pan.add(btnGridColor = new JButton("grid color"));
        colBtn = ZeeConfig.intToColor(ZeeConfig.gridColorInt);
        btnGridColor.setBackground(colBtn);
        btnGridColor.setForeground(ZeeConfig.getComplementaryColor(colBtn));
        btnGridColor.addActionListener(evt->{
            Color color = JColorChooser.showDialog(panelTabMisc, "Pick Color", ZeeConfig.intToColor(ZeeConfig.gridColorInt), true);
            if (color==null)
                color = ZeeConfig.DEF_GRID_COLOR;
            btnGridColor.setBackground(color);
            btnGridColor.setForeground(ZeeConfig.getComplementaryColor(color));
            ZeeConfig.gridColorInt = ZeeConfig.colorToInt(color);
            Utils.setprefi("gridColorInt",ZeeConfig.gridColorInt);
            ZeeConfig.newGridColor(color);
        });

        // hitbox color
        pan.add(btnHitboxColor= new JButton("hitbox color"));
        colBtn = ZeeHitbox.hitBoxColor;
        btnHitboxColor.setBackground(colBtn);
        btnHitboxColor.setForeground(ZeeConfig.getComplementaryColor(colBtn));
        btnHitboxColor.addActionListener(evt->{
            Color color = JColorChooser.showDialog(panelTabMisc, "Pick Color", ZeeHitbox.hitBoxColor, true);
            if (color==null)
                color = ZeeHitbox.DEF_HITBOX_COLOR;
            btnHitboxColor.setBackground(color);
            btnHitboxColor.setForeground(ZeeConfig.getComplementaryColor(color));
            ZeeHitbox.hitBoxColor = color;
            Utils.setprefi("hitBoxColor",ZeeConfig.colorToInt(ZeeHitbox.hitBoxColor));
            ZeeHitbox.updateHitboxColor();
        });

        //auto hide windows list
        panelTabInterface.add(new ZeeOptionJCheckBox( "Show auto-hide window button", "autoHideWindows"),c);
        panelTabInterface.add(tfAutoHideWindows= new JTextField("",5), c);
        tfAutoHideWindows.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfAutoHideWindows.getPreferredSize().height));
        tfAutoHideWindows.setText(ZeeConfig.listWindowsAddHideButton);
        tfAutoHideWindows.addActionListener(actionEvent -> {
            String str = actionEvent.getActionCommand();
            String[] strArr = str.split(",");
            if(strArr!=null && strArr.length>0) {
                ZeeConfig.listWindowsAddHideButton = str;
                Utils.setpref("listWindowsAddHideButton",str.strip());
            }
        });

        // auto hide delay ms
        panelTabInterface.add(new ZeeOptionJCheckBox( "Auto-hide win delay (ms)", "autoHideWindowDelay"),c);
        panelTabInterface.add(tfAutoHideWindowDelayMs = new JTextField("",5){
            @Override
            protected void processKeyEvent(KeyEvent e) {
                if (Character.isDigit(e.getKeyChar()) ||
                        e.getKeyCode()==KeyEvent.VK_ENTER ||
                        e.getKeyCode()==KeyEvent.VK_BACK_SPACE ||
                        e.getKeyCode()==KeyEvent.VK_DELETE )
                {
                    super.processKeyEvent(e);
                }
            }
        }, c);
        tfAutoHideWindowDelayMs.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfAutoHideWindowDelayMs.getPreferredSize().height));
        tfAutoHideWindowDelayMs.setText(""+ZeeConfig.autoHideWindowDelayMs);
        tfAutoHideWindowDelayMs.addActionListener(evt -> {
            ZeeConfig.autoHideWindowDelayMs = Integer.parseInt(evt.getActionCommand().strip());
            Utils.setprefi("autoHideWindowDelayMs", ZeeConfig.autoHideWindowDelayMs);
        });

    }


    private void buildTabAudio(){

        panelTabAudio = new JPanel(new GridBagLayout());

        tabbedPane.addTab("Audio", panelTabAudio);

        // notify buddies
        panelTabAudio.add(new ZeeOptionJCheckBox( "Notify when friends login", "notifyBuddyOnline"),c);

        // mute audio msg
        panelTabAudio.add(new ZeeOptionJCheckBox( "Mute audio messages (use ;)", "blockAudioMsg"),c);
        panelTabAudio.add(tfBlockAudioMsgs= new JTextField("",5), c);
        tfBlockAudioMsgs.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfBlockAudioMsgs.getPreferredSize().height));
        tfBlockAudioMsgs.setText(ZeeConfig.blockAudioMsgList);
        tfBlockAudioMsgs.addActionListener(actionEvent -> {
            String str = actionEvent.getActionCommand();
            String[] strArr = str.split(",");
            if(strArr!=null && strArr.length>0) {
                ZeeConfig.blockAudioMsgList = str;
                Utils.setpref("blockAudioMsgList",str.strip());
            }
        });

        // midi radio
        panelTabAudio.add(cbMidiRadio = new ZeeOptionJCheckBox( "Play midi radio", "playMidiRadio"),c);
        cbMidiRadio.addActionListener(actionEvent -> {
            btnSelMidiFolder.setEnabled(cbMidiRadio.isSelected());
            ZeeMidiRadio.toggleRadio();
        });
        panelTabAudio.add(btnSelMidiFolder = new JButton("select midi folder"),c);
        btnSelMidiFolder.setEnabled(cbMidiRadio.isSelected());
        btnSelMidiFolder.addActionListener(evt -> {
            JFileChooser f = new JFileChooser();
            f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            f.setAcceptAllFileFilterUsed(false);
            if (f.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                if (f.getCurrentDirectory() != null) {

                    // read path
                    String path = f.getSelectedFile().getAbsolutePath();

                    //check for midi files
                    File dir = new File(path);
                    File[] files = dir.listFiles((dir1, name) -> (name.toLowerCase().endsWith(".mid") || name.toLowerCase().endsWith(".midi")));
                    if (files==null || files.length==0){
                        JOptionPane.showMessageDialog(null,"no midi file in that folder");
                        return;
                    }

                    // set midi folder path
                    ZeeConfig.println("set midi directory: " + path);
                    ZeeMidiRadio.pathFolder = path;
                    Utils.setpref("midiRadioPath",ZeeMidiRadio.pathFolder);
                }

            }else{
                ZeeConfig.println("canceled midi path");
            }
        });

    }


    private void buildTabGobs() {

        panelTabGobs.removeAll();

        //panel bottom details
        if(panelDetailsBottom !=null) {
            panelDetailsBottom.repaint();
        }else {
            panelDetailsBottom = new JPanel(new GridBagLayout());
        }

        //subtabs pane
        tabbedPaneGobs = new JTabbedPane();
        tabbedPaneGobs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                try {
                    if(listGobsTemp!=null && listGobsTemp.isValid())
                        listGobsTemp.clearSelection();
                    if(listGobsSaved!=null && listGobsSaved.isValid())
                        listGobsSaved.clearSelection();
                    if(listGobsCategories!=null && listGobsCategories.isValid())
                        listGobsCategories.clearSelection();
                }catch (Exception e){
                    e.printStackTrace();
                }
                panelDetailsBottom.removeAll();
            }
        });
        panelTabGobs.add(tabbedPaneGobs, BorderLayout.NORTH);


        //subtab gobs session list
        if(ZeeConfig.listGobsSession.size() > 0) {
            listGobsTemp = new JList<String>( ZeeConfig.listGobsSession.toArray(new String[0]));
        }else {
            listGobsTemp = new JList<String>();
        }
        JPanel panelTabGobSess = new JPanel(new GridBagLayout());
        panelTabGobSess.add(new JScrollPane(listGobsTemp), c);
        tabbedPaneGobs.addTab("Session("+ZeeConfig.listGobsSession.size()+")", panelTabGobSess);


        //panel gobs main buttons
        //TODO move buttons up a container
        JPanel panelGobButtons = new JPanel(new FlowLayout());
        panelTabGobSess.add(panelGobButtons, c);
        panelGobButtons.add(btnRefresh = new JButton("refresh"));
        btnRefresh.addActionListener(evt -> {
            buildTabGobs();
        });
        panelGobButtons.add(btnPrintState = new JButton("print"));
        btnPrintState.addActionListener(evt -> {
            printSavedSettings();
        });
        panelGobButtons.add(btnResetGobs = new JButton("reset"));
        btnResetGobs.addActionListener(evt -> {
            resetGobs();
        });


        listGobsTemp.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if(evt.getValueIsAdjusting() || tabbedPaneGobs.getSelectedIndex()!=TABGOB_SESSION)
                    return;
                updatePanelDetails();
            }
        });


        //subtab gobs saved list
        listGobsSaved = fillUpListGobsSaved();
        tabbedPaneGobs.addTab("Saved("+listGobsSaved.getModel().getSize()+")",new JScrollPane(listGobsSaved));
        listGobsSaved.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if(evt.getValueIsAdjusting() || tabbedPaneGobs.getSelectedIndex()!=TABGOB_SAVED)
                    return;
                updatePanelDetails();
            }
        });

        //subtab gobs category
        listGobsCategories = new JList<String>(ZeeConfig.mapCategoryGobs.keySet().toArray(new String[0]));
        panelTabCateg = new JPanel(new GridBagLayout());
        tabbedPaneGobs.addTab("Categs("+ZeeConfig.mapCategoryGobs.size()+")", panelTabCateg);
        JPanel panelButtonCateg = new JPanel(new FlowLayout());
        panelTabCateg.add(panelButtonCateg, c);
        panelButtonCateg.add(btnResetCateg = new JButton("Reset"));
        btnResetCateg.addActionListener(evt->{ resetCategoriesToDefault(); });
        panelButtonCateg.add(btnAddCateg = new JButton("Add"));
        btnAddCateg.addActionListener(evt->{ addCategoryNew(); });
        panelButtonCateg.add(btnRemoveCateg = new JButton("Remove"));
        btnRemoveCateg.addActionListener(evt->{ removeCategory(); });
        panelTabCateg.add(new JScrollPane(listGobsCategories), c);
        listGobsCategories.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if(evt.getValueIsAdjusting() || tabbedPaneGobs.getSelectedIndex()!=TABGOB_CATEGS)
                    return;
                updatePanelDetails();
            }
        });

        panelTabGobs.add(panelDetailsBottom, BorderLayout.CENTER);
        pack();
    }

    private void resetActionUses() {
        if(JOptionPane.showConfirmDialog(this,"Confirm reset action uses?") != JOptionPane.OK_OPTION){
            return;
        }
        Utils.setpref(ZeeConfig.MAP_ACTION_USES,"");
        ZeeConfig.mapActionUses = ZeeConfig.initMapActionUses();
    }

    private void resetWindowsPos() {
        if(JOptionPane.showConfirmDialog(this,"Confirm reset windows pos?") != JOptionPane.OK_OPTION){
            return;
        }
        Utils.setpref(ZeeConfig.MAP_WND_POS,"");
        ZeeConfig.mapWindowPos = ZeeConfig.initMapWindowPos();
        btnResetWindowsPos.setText("reset windows pos ("+ZeeConfig.mapWindowPos.size()+")");
    }

    private JList<String> fillUpListGobsSaved() {
        Set<String> set = new HashSet<String>();
        JList<String> ret;
        if(ZeeConfig.mapGobAudio.size() > 0) {
            set.addAll(ZeeConfig.mapGobAudio.keySet());
        }
        if(ZeeConfig.mapGobSpeech.size() > 0) {
            set.addAll(ZeeConfig.mapGobSpeech.keySet());
        }
        if(ZeeConfig.mapGobCategory.size() > 0) {
            set.addAll(ZeeConfig.mapGobCategory.keySet());
        }
        if(ZeeConfig.mapGobColor.size() > 0) {
            set.addAll(ZeeConfig.mapGobColor.keySet());
        }
        List<String> sortedList = new ArrayList<>(set);
        Collections.sort(sortedList);
        ret = new JList<String>(sortedList.toArray(new String[0]));
        return ret;
    }

    private void printSavedSettings() {
        txtAreaDebug.setText("");
        txtAreaDebug.append("\n============");
        txtAreaDebug.append("\n\n[mapGobAudio]\n"+ZeeConfig.mapGobAudio);
        txtAreaDebug.append("\n\n[mapCategoryGobs]\n"+ZeeConfig.mapCategoryGobs);
        txtAreaDebug.append("\n\n[mapCategoryAudio]\n"+ZeeConfig.mapCategoryAudio);
        txtAreaDebug.selectAll();
    }

    private void resetGobs() {
        if(JOptionPane.showConfirmDialog(this,"Clear All Gobs settings?") != JOptionPane.OK_OPTION)
            return;


        //clear audios
        Utils.setpref(ZeeConfig.MAP_GOB_AUDIO,"");
        ZeeConfig.mapGobAudio = ZeeConfig.initMapGobAudio();

        //clear colors
        Utils.setpref(ZeeConfig.MAP_GOB_COLOR,"");
        ZeeConfig.mapGobColor = ZeeConfig.initMapGobColor();

        //reset map Category-Gobs (always reset categs before gobs)
        Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS,"");
        ZeeConfig.mapCategoryGobs = ZeeConfig.initMapCategoryGobs();
        //reflect in GobCategory
        Utils.setpref(ZeeConfig.MAP_GOB_CATEGORY,"");
        ZeeConfig.mapGobCategory = ZeeConfig.initMapGobCategory();

        buildTabGobs();
    }


    private void updatePanelDetails() {

        //build category details
        if(tabbedPaneGobs.getSelectedIndex()==TABGOB_CATEGS){
            tabCategsSelected();
            return;
        }

        //build gob details(saved and session)
        JList<String> list = null;
        if(tabbedPaneGobs.getSelectedIndex()==TABGOB_SESSION) {
            list = listGobsTemp;
        }else if(tabbedPaneGobs.getSelectedIndex()==TABGOB_SAVED) {
            list = listGobsSaved;
        }

        panelDetailsBottom.removeAll();


        /*
            Gob name
        */
        JPanel panelGobName = new JPanel(new GridBagLayout());
        panelDetailsBottom.add(panelGobName, c);
        panelGobName.setBorder(BorderFactory.createTitledBorder("Gob"));
        panelGobName.add(tfGobName = new JTextField(list.getSelectedValue()), c);
        tfGobName.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfGobName.getPreferredSize().height));


        /*
            Gob audio
         */
        //audio file name
        JPanel panelGobAudio = new JPanel(new GridBagLayout());
        panelDetailsBottom.add(panelGobAudio, c);
        panelGobAudio.setBorder(BorderFactory.createTitledBorder("Audio"));
        panelGobAudio.add(Box.createVerticalGlue());
        String audioPath = ZeeConfig.mapGobAudio.get(list.getSelectedValue());
        if(audioPath==null)
            audioPath = "";
        panelGobAudio.add(tfAudioPath = new JTextField(audioPath), c);
        tfAudioPath.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfAudioPath.getPreferredSize().height));
        //audio buttons
        JPanel panelAudioButtons;
        panelGobAudio.add(panelAudioButtons = new JPanel(new FlowLayout()), c);
        panelAudioButtons.add(btnAudioSave = new JButton("Select"));
        btnAudioSave.addActionListener(evt->{ audioSave(); });
        panelAudioButtons.add(btnAudioClear = new JButton("Clear"));
        btnAudioClear.addActionListener(evt->{ audioClear(); });
        panelAudioButtons.add(btnAudioTest = new JButton("Play"));
        btnAudioTest.addActionListener(evt->{ audioTest(); });



        /*
            Gob text to speech
         */
        //speech file name
        JPanel panelGobSpeech = new JPanel(new GridBagLayout());
        panelDetailsBottom.add(panelGobSpeech, c);
        panelGobSpeech.setBorder(BorderFactory.createTitledBorder("Text to Speech (festival)"));
        panelGobSpeech.add(Box.createVerticalGlue());
        String speech = ZeeConfig.mapGobSpeech.get(list.getSelectedValue());
        if(speech==null)
            speech = "";
        panelGobSpeech.add(tfGobSpeech = new JTextField(speech), c);
        tfGobSpeech.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfGobSpeech.getPreferredSize().height));
        //speech buttons
        JPanel panelSpeechButtons;
        panelGobSpeech.add(panelSpeechButtons = new JPanel(new FlowLayout()), c);
        JButton btn = (JButton) panelSpeechButtons.add(new JButton("Test"));
        btn.addActionListener(evt->{ speechTest(); });
        btn = (JButton) panelSpeechButtons.add(new JButton("Save"));
        btn.addActionListener(evt->{ speechSave(); });
        btn = (JButton) panelSpeechButtons.add(new JButton("Clear"));
        btn.addActionListener(evt->{ speechClear(); });



        /*
            Gob color highlight
        */
        //button
        JPanel panelHighlight = new JPanel(new GridBagLayout());
        panelDetailsBottom.add(panelHighlight, c);
        panelHighlight.setBorder(BorderFactory.createTitledBorder("Highlight Color"));
        c.gridwidth = GridBagConstraints.RELATIVE;
        panelHighlight.add(btnGobColorAdd = new JButton("Select") , c);
        btnGobColorAdd.addActionListener(evt->{
            Color color = JColorChooser.showDialog(panelHighlight, "Gob Highlight Color", Color.MAGENTA, true);
            if(color!=null){
                addGobColor(tfGobName.getText(), color);
            }
        });
        c.gridwidth = GridBagConstraints.REMAINDER;
        panelHighlight.add(btnGobColorRemove = new JButton("Clear"), c);
        btnGobColorRemove.addActionListener(evt->{
            removeGobColor();
        });
        //update color UI state
        Color currentColor = ZeeConfig.mapGobColor.get(tfGobName.getText());
        if(currentColor!=null){
            btnGobColorAdd.getParent().setBackground(currentColor);
        }



        /*
            Gob category
         */
        //combo category
        JPanel panelAddToCategory = new JPanel(new GridBagLayout());
        panelDetailsBottom.add(panelAddToCategory, c);
        panelAddToCategory.setBorder(BorderFactory.createTitledBorder("Category"));
        panelAddToCategory.add(cmbGobCategory = new JComboBox<String>(ZeeConfig.mapCategoryGobs.keySet().toArray(new String[0])), c);
        cmbGobCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, cmbGobCategory.getPreferredSize().height));
        //init combo categ state
        String categ = getCategoryByGobName(tfGobName.getText().strip());
        if(categ!=null && !categ.isEmpty()) {
            cmbGobCategory.setSelectedItem(categ);
        }else {
            cmbGobCategory.setSelectedIndex(-1);
        }
        // add combo categ event listener
        cmbGobCategory.addActionListener(evt->{
            addGobToCategory();
        });
        //add button remove from categ
        panelAddToCategory.add(btnRemGobFromCateg = new JButton("Clear"), c);
        btnRemGobFromCateg.addActionListener(evt->{ removeGobFromCategory(); });



        pack();
    }

    private void removeGobColor() {
        if(JOptionPane.showConfirmDialog(this,"Clear Gob Color?") == JOptionPane.OK_OPTION) {
            ZeeConfig.mapGobColor.remove(tfGobName.getText());
            btnGobColorAdd.getParent().setBackground(new JPanel().getBackground());
            Utils.setpref(ZeeConfig.MAP_GOB_COLOR, ZeeConfig.serialize(ZeeConfig.mapGobColor));
        }
    }

    private void removeCategoryColor() {
        if(JOptionPane.showConfirmDialog(this,"Clear Category Color?") == JOptionPane.OK_OPTION) {
            ZeeConfig.mapCategoryColor.remove(tfCategName.getText());
            btnCategoryColorAdd.getParent().setBackground(new JPanel().getBackground());
            Utils.setpref(ZeeConfig.MAP_CATEGORY_COLOR, ZeeConfig.serialize(ZeeConfig.mapCategoryColor));
        }
    }

    private void addCategoryNew() {
        String categName = JOptionPane.showInputDialog("Type new category name: ");
        if(categName!=null && !categName.strip().isEmpty()){
            if (ZeeConfig.mapCategoryGobs.keySet().contains(categName.strip())){
                JOptionPane.showMessageDialog(this,"Category already exists.");
                return;
            }
            ZeeConfig.mapCategoryGobs.put(categName, new HashSet<String>());
            Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS, ZeeConfig.serialize(ZeeConfig.mapCategoryGobs));
            buildTabGobs();
        }
    }

    private void removeCategory(){
        String categ = tfCategName.getText();

        if(JOptionPane.showConfirmDialog(this,"Remove category \""+categ+"\" ?") != JOptionPane.OK_OPTION)
            return;

        //update map gob-categ
        for(String gob: ZeeConfig.mapGobCategory.keySet())
            if(ZeeConfig.mapGobCategory.get(gob).contentEquals(categ))
                ZeeConfig.mapGobCategory.remove(gob);

        //update map categ-color
        ZeeConfig.mapCategoryColor.remove(categ);

        //update map categ-audio
        ZeeConfig.mapCategoryAudio.remove(categ);

        //update map categ-gobs
        if(ZeeConfig.isDefaultCateg(categ)){
            ZeeConfig.resetDefaultCateg(categ);
        }else {
            ZeeConfig.mapCategoryGobs.remove(categ);//custom categ
        }

        //save maps
        Utils.setpref(ZeeConfig.MAP_GOB_CATEGORY, ZeeConfig.serialize(ZeeConfig.mapGobCategory));
        Utils.setpref(ZeeConfig.MAP_CATEGORY_COLOR, ZeeConfig.serialize(ZeeConfig.mapCategoryColor));
        Utils.setpref(ZeeConfig.MAP_CATEGORY_AUDIO, ZeeConfig.serialize(ZeeConfig.mapCategoryAudio));
        Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS, ZeeConfig.serialize(ZeeConfig.mapCategoryGobs));

        //reset options window
        buildTabGobs();
    }


    private String getCategoryByGobName(String gobName) {
        return ZeeConfig.mapGobCategory.get(gobName);
    }


    private void tabCategsSelected() {
        panelDetailsBottom.removeAll();

        //category name
        JPanel panelCategName = new JPanel(new GridBagLayout());
        panelDetailsBottom.add(panelCategName, c);
        panelCategName.setBorder(BorderFactory.createTitledBorder("Category"));
        panelCategName.add(tfCategName = new JTextField(listGobsCategories.getSelectedValue()));
        tfCategName.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfCategName.getPreferredSize().height));
        panelCategName.setMaximumSize(new Dimension(Integer.MAX_VALUE, panelCategName.getPreferredSize().height));

        //category audio file
        JPanel panelCategAudio = new JPanel(new GridBagLayout());
        panelDetailsBottom.add(panelCategAudio, c);
        panelCategAudio.setBorder(BorderFactory.createTitledBorder("Audio"));
        String audioPath = ZeeConfig.mapCategoryAudio.get(listGobsCategories.getSelectedValue());
        if(audioPath==null)
            audioPath = "";
        panelCategAudio.add(tfAudioPathCateg = new JTextField(audioPath), c);
        tfAudioPathCateg.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfAudioPathCateg.getPreferredSize().height));

        //audio buttons
        JPanel panelCategAudioButtons = new JPanel(new FlowLayout());
        panelCategAudio.add(panelCategAudioButtons, c);
        panelCategAudioButtons.add(btnAudioSave = new JButton("Select"));
        btnAudioSave.addActionListener(evt->{ audioSave(); });
        panelCategAudioButtons.add(btnAudioClear = new JButton("Clear"));
        btnAudioClear.addActionListener(evt->{ audioClear(); });
        panelCategAudioButtons.add(btnAudioTest = new JButton("Test"));
        btnAudioTest.addActionListener(evt->{ audioTest(); });


        //Category Color highlight
        //button
        JPanel panelHighlight = new JPanel(new GridBagLayout());
        panelDetailsBottom.add(panelHighlight, c);
        panelHighlight.setBorder(BorderFactory.createTitledBorder("Highlight Color"));
        c.gridwidth = GridBagConstraints.RELATIVE;
        panelHighlight.add(btnCategoryColorAdd = new JButton("Select"), c);
        btnCategoryColorAdd.addActionListener(evt->{
            Color color = JColorChooser.showDialog(panelHighlight, "Category Highlight Color", Color.MAGENTA, true);
            if(color!=null){
                addCategoryColor(tfCategName.getText(), color);
            }
        });
        c.gridwidth = GridBagConstraints.REMAINDER;
        panelHighlight.add(btnCategoryColorRemove = new JButton("Clear"), c);
        btnCategoryColorRemove.addActionListener(evt->{
            removeCategoryColor();
        });
        //update color UI state
        Color currentColor = ZeeConfig.mapCategoryColor.get(tfCategName.getText());
        if(currentColor!=null){
            btnCategoryColorAdd.getParent().setBackground(currentColor);
        }

        pack();
    }

    private void resetCategoriesToDefault() {
        if(JOptionPane.showConfirmDialog(this,"Reset categories?") != JOptionPane.OK_OPTION)
            return;

        //reset map Category-Gobs (always reset categs before gobs)
        Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS,"");
        ZeeConfig.mapCategoryGobs = ZeeConfig.initMapCategoryGobs();
        //reflect in GobCategory
        Utils.setpref(ZeeConfig.MAP_GOB_CATEGORY,"");
        ZeeConfig.mapGobCategory = ZeeConfig.initMapGobCategory();

        //reset map Category-Audio
        Utils.setpref(ZeeConfig.MAP_CATEGORY_AUDIO,"");
        ZeeConfig.mapCategoryAudio = ZeeConfig.initMapCategoryAudio();

        //reset map Category-Color
        Utils.setpref(ZeeConfig.MAP_CATEGORY_COLOR,"");
        ZeeConfig.mapCategoryColor = ZeeConfig.initMapCategoryColor();

        buildTabGobs();
    }

    private void removeGobFromCategory(String gobName, String gobCategory) {
        if(gobName==null || gobCategory==null || gobName.isEmpty() || gobCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this,"removeGobFromCategory(g,c) > gob or category invalid");
            return;
        }

        //remove from map Category->Gobs
        Set<String> gobs = ZeeConfig.mapCategoryGobs.get(gobCategory);
        if(gobs!=null){
            gobs.remove(gobName);
            ZeeConfig.mapCategoryGobs.put(gobCategory,gobs);
            Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS, ZeeConfig.serialize(ZeeConfig.mapCategoryGobs));
        }

        //remove from map Gob->Category
        String categ = ZeeConfig.mapGobCategory.get(gobName);
        if(categ!=null && !categ.isEmpty()){
            ZeeConfig.mapGobCategory.remove(gobName);
            Utils.setpref(ZeeConfig.MAP_GOB_CATEGORY, ZeeConfig.serialize(ZeeConfig.mapGobCategory));
        }
    }

    private void removeGobFromCategory(){
        try {
            String gobName = tfGobName.getText().strip();
            String gobCategory = cmbGobCategory.getSelectedItem().toString().strip();
            removeGobFromCategory(gobName, gobCategory);
            cmbGobCategory.setSelectedIndex(-1);
        }catch(Exception e){
            JOptionPane.showMessageDialog(this,e.getMessage());
        }
    }

    private void addGobColor(String gobName, Color c) {
        if (gobName == null || gobName.isEmpty() || c == null) {
            JOptionPane.showMessageDialog(this, "Gob or color parameter missing");
            return;
        }
        Color color = new Color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
        ZeeConfig.mapGobColor.put(gobName, color);
        btnGobColorAdd.getParent().setBackground(color);
        Utils.setpref(ZeeConfig.MAP_GOB_COLOR, ZeeConfig.serialize(ZeeConfig.mapGobColor));
    }

    private void addCategoryColor(String categName, Color c) {
        if (categName == null || categName.isEmpty() || c == null) {
            JOptionPane.showMessageDialog(this, "Category or color parameter missing");
            return;
        }
        Color color = new Color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
        ZeeConfig.mapCategoryColor.put(categName, color);
        btnCategoryColorAdd.getParent().setBackground(color);
        Utils.setpref(ZeeConfig.MAP_CATEGORY_COLOR, ZeeConfig.serialize(ZeeConfig.mapCategoryColor));
    }

    private void addGobToCategory(String gobName, String gobCategory) {
        if(gobName==null || gobCategory==null || gobName.isEmpty() || gobCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Gob or Category invalid");
            return;
        }

        //add to map Category->Gobs
        Set<String> gobs = ZeeConfig.mapCategoryGobs.get(gobCategory);
        if (gobs == null) {
            gobs = new HashSet<String>();
        }
        gobs.add(gobName);//hashset adds only if not present already
        ZeeConfig.mapCategoryGobs.put(gobCategory,gobs);
        Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS, ZeeConfig.serialize(ZeeConfig.mapCategoryGobs));

        //add to map Gob->Category
        ZeeConfig.mapGobCategory.put(gobName,gobCategory);
        Utils.setpref(ZeeConfig.MAP_GOB_CATEGORY, ZeeConfig.serialize(ZeeConfig.mapGobCategory));
    }

    private void addGobToCategory() {
        try {
            if(cmbGobCategory==null || cmbGobCategory.getSelectedIndex() < 0)
                return;

            String gobName = tfGobName.getText().strip();
            String gobCategory =  cmbGobCategory.getSelectedItem().toString().strip();

            //remove gob if already has category
            String prevCategory = getCategoryByGobName(gobName);
            if(prevCategory!=null && !prevCategory.isEmpty()){
                removeGobFromCategory(gobName,prevCategory);
            }

            addGobToCategory(gobName,gobCategory);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void speechTest() {
        String speech = tfGobSpeech.getText().strip();

        if(speech.isBlank()){
            JOptionPane.showMessageDialog(this,"speech text is blank");
        }else{
            try{
                ZeeSynth.textToSpeakLinuxFestival(speech);
            }catch (Exception e){
                JOptionPane.showMessageDialog(this,e.getMessage());
            }
        }
    }

    private void speechSave() {
        String speech = tfGobSpeech.getText().strip();

        if(speech.isBlank()){
            JOptionPane.showMessageDialog(this,"speech text is blank");
            return;
        }

        //save speech for gob
        ZeeConfig.mapGobSpeech.put(
            tfGobName.getText().strip(),
            speech
        );
        Utils.setpref(ZeeConfig.MAP_GOB_SPEECH, ZeeConfig.serialize(ZeeConfig.mapGobSpeech));

        if(listGobsSaved!=null) {
            listGobsSaved.clearSelection();
            //list.updateUI();
        }
        panelDetailsBottom.removeAll();
        panelTabGobs.revalidate();
        panelTabGobs.repaint();
        buildTabGobs();
    }

    private void speechClear() {
        if(JOptionPane.showConfirmDialog(this,"Clear speech settings?") != JOptionPane.OK_OPTION)
            return;

        //remove gob speech
        String gobName = tfGobName.getText().strip();
        if (!ZeeConfig.mapGobSpeech.containsKey(gobName)){
            JOptionPane.showMessageDialog(this,"Gob has no speech set");
            return;
        }
        ZeeConfig.mapGobSpeech.remove(gobName);
        Utils.setpref(ZeeConfig.MAP_GOB_SPEECH, ZeeConfig.serialize(ZeeConfig.mapGobSpeech));

        panelDetailsBottom.removeAll();
        panelTabGobs.revalidate();
        panelTabGobs.repaint();
        buildTabGobs();
    }

    private void audioTest() {
        String path;

        if(tabbedPaneGobs.getSelectedIndex()==TABGOB_CATEGS)
            path = tfAudioPathCateg.getText().strip();
        else
            path = tfAudioPath.getText().strip();

        if(path.isBlank()){
            JOptionPane.showMessageDialog(this,"audio path is blank");
        }else{
            try{
                ZeeConfig.playAudio(path);
            }catch (Exception e){
                JOptionPane.showMessageDialog(this,e.getMessage());
            }
        }
    }

    private void audioClear() {
        if(JOptionPane.showConfirmDialog(this,"Clear audio settings?") == JOptionPane.OK_OPTION) {

            if(tabbedPaneGobs.getSelectedIndex()==TABGOB_CATEGS) {

                //remove category audio
                ZeeConfig.mapCategoryAudio.remove(listGobsCategories.getSelectedValue().strip());
                Utils.setpref(ZeeConfig.MAP_CATEGORY_AUDIO,  ZeeConfig.serialize(ZeeConfig.mapCategoryAudio));

            } else {

                //remove gob audio
                String gobName = tfGobName.getText().strip();
                if (!ZeeConfig.mapGobAudio.containsKey(gobName)){
                    JOptionPane.showMessageDialog(this,"Gob has no audio set.\n Try remove from category.");
                    return;
                }
                ZeeConfig.mapGobAudio.remove(gobName);
                Utils.setpref(ZeeConfig.MAP_GOB_AUDIO, ZeeConfig.serialize(ZeeConfig.mapGobAudio));
            }

            panelDetailsBottom.removeAll();
            panelTabGobs.validate();
            buildTabGobs();
        }
    }

    private void audioSave() {
        JList list;
        JFileChooser fileChooser;
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(null);
        fileChooser.setDialogTitle("Select audio file");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Audio files","mp3","wav","ogg","mid","midi"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            String str;

            //save audio for category
            if(tabbedPaneGobs.getSelectedIndex()==TABGOB_CATEGS){
                list = listGobsCategories;
                tfAudioPathCateg.setText(fileChooser.getSelectedFile().getAbsolutePath());
                ZeeConfig.mapCategoryAudio.put(
                        tfCategName.getText().strip(),
                        fileChooser.getSelectedFile().getAbsolutePath().strip()
                );
                Utils.setpref(ZeeConfig.MAP_CATEGORY_AUDIO, ZeeConfig.serialize(ZeeConfig.mapCategoryAudio));

            } else {

                //save audio for single gob
                list = listGobsSaved;
                tfAudioPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
                ZeeConfig.mapGobAudio.put(
                        tfGobName.getText().strip(),
                        fileChooser.getSelectedFile().getAbsolutePath().strip()
                );
                Utils.setpref(ZeeConfig.MAP_GOB_AUDIO, ZeeConfig.serialize(ZeeConfig.mapGobAudio));
            }

            if(list!=null) {
                list.clearSelection();
                //list.updateUI();
            }
            panelDetailsBottom.removeAll();
            panelTabGobs.validate();
            buildTabGobs();
        }
    }

}
package haven;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class ZeecowOptionsWindow extends JFrame {
    public GridBagConstraints c;
    public JTabbedPane tabbedPane, tabbedPaneGobs;
    public JPanel panelTabMisc, panelTabInterface, panelTabGobs, panelTabControls, panelTabMinimap, panelDetailsBottom, panelTabCateg, panelShapeIcons, panelShapeIconsSaveCancel;
    public JCheckBox cbDropAltKeyOnly, cbShowKinNames, cbSimpleWindowBorder, cbSimpleButtons, cbSimpleWindows, cbFreeGobPlacement, cbScrollTransferItems, cbCtrlClickMinimapContent, cbShapeIcons, cbSlowMiniMap, cbHideFxAnimations, cbHideFxSmoke, cbAutoChipMinedBoulder, cbDropMinedStone, cbDropMinedOre, cbDropMinedSilverGold, cbDropMinedCurios, cbActionSearchGlobal, cbCompactEquipsWindow, cbBeltTogglesEquipsReposition, cbBeltTogglesEquips, cbAutoRunLogin, cbAutohearth, cbHighlightCropsReady, cbTreeAnimation, cbShowGrowingTreePercentage, cbMiniTrees, cbKeyUpDownAudioControl, cbAlertOnPlayers,  cbShowInventoryLogin, cbShowBeltLogin, cbKeyBeltShiftTab, cbDrinkKey, cbDrinkAuto, cbKeyCamSwitchShiftC, cbShowIconsZoomOut, cbRememberWindowsPos, cbSortActionsByUse, cbDebugWidgetMsgs, cbDebugCodeRes, cbMidclickEquipManager, cbShowEquipsLogin, cbNotifyBuddyOnline, cbZoomOrthoExtended, cbCattleRosterHeight, cbAutoToggleGridLines;
    public JTextField tfAutoClickMenu, tfAggroRadiusTiles, tfButchermode, tfGobName, tfAudioPath, tfCategName, tfAudioPathCateg;
    public JComboBox<String> cmbCattleRoster, cmbGobCategory, cmbMiniTreeSize, comboShapeIcons, cmbDrinkAutoValue;
    public JList<String> listGobsTemp, listGobsSaved, listGobsCategories;
    public JButton btnRefresh, btnPrintState, btnResetGobs, btnAudioSave, btnAudioClear, btnAudioTest, btnRemGobFromCateg, btnGobColorAdd, btnCategoryColorAdd, btnGobColorRemove, btnCategoryColorRemove, btnResetCateg, btnAddCateg, btnRemoveCateg, btnResetWindowsPos, btnResetActionUses, btnSapeIconPreview, btnShapeIconSave, btnSapeIconDelete, btnSolidColorWindow, btnGridGolor;
    public JTextArea txtAreaDebug;
    public static int TABGOB_SESSION = 0;
    public static int TABGOB_SAVED = 1;
    public static int TABGOB_CATEGS = 2;

    public ZeecowOptionsWindow(){
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

        buildTabMisc();

        buildTabInterface();

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

        panelTabControls.add(cbFreeGobPlacement = new JCheckBox("free gob placement"), c);
        cbFreeGobPlacement.setSelected(ZeeConfig.freeGobPlacement);
        cbFreeGobPlacement.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.freeGobPlacement = cb.isSelected();
            Utils.setprefb("freeGobPlacement",val);
        });

        panelTabControls.add(cbScrollTransferItems = new JCheckBox("scroll transfer items directly"), c);
        cbScrollTransferItems.setSelected(ZeeConfig.scrollTransferItems);
        cbScrollTransferItems.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.scrollTransferItems = cb.isSelected();
            Utils.setprefb("scrollTransferItems",val);
        });

        panelTabControls.add(cbCtrlClickMinimapContent = new JCheckBox("Ctrl+click to pan/resize minimap"), c);
        cbCtrlClickMinimapContent.setSelected(ZeeConfig.ctrlClickMinimapContent);
        cbCtrlClickMinimapContent.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.ctrlClickMinimapContent = cb.isSelected();
            Utils.setprefb("ctrlClickMinimapContent",val);
        });

        panelTabControls.add(cbDropAltKeyOnly = new JCheckBox("Alt+click drops holding item"), c);
        cbDropAltKeyOnly.setSelected(ZeeConfig.dropHoldingItemAltKey);
        cbDropAltKeyOnly.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropHoldingItemAltKey = cb.isSelected();
            Utils.setprefb("dropHoldingItemAltKey",val);
        });

        panelTabControls.add(cbKeyUpDownAudioControl = new JCheckBox("Key up/down controls volume"), c);
        cbKeyUpDownAudioControl.setSelected(ZeeConfig.keyUpDownAudioControl);
        cbKeyUpDownAudioControl.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.keyUpDownAudioControl = cb.isSelected();
            Utils.setprefb("keyUpDownAudioControl",val);
        });

        panelTabControls.add(cbKeyBeltShiftTab = new JCheckBox("Shift+Tab toggles belt"), c);
        cbKeyBeltShiftTab.setSelected(ZeeConfig.keyBeltShiftTab);
        cbKeyBeltShiftTab.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.keyBeltShiftTab = cb.isSelected();
            Utils.setprefb("keyBeltShiftTab",val);
        });

        panelTabControls.add(cbKeyCamSwitchShiftC = new JCheckBox("Shift+C switch cams"), c);
        cbKeyCamSwitchShiftC.setSelected(ZeeConfig.keyCamSwitchShiftC);
        cbKeyCamSwitchShiftC.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.keyCamSwitchShiftC = cb.isSelected();
            Utils.setprefb("keyCamSwitchShiftC",val);
        });

        panelTabControls.add(cbDrinkKey = new JCheckBox("Drink key ' (single quote) "), c);
        cbDrinkKey.setSelected(ZeeConfig.drinkKey);
        cbDrinkKey.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.drinkKey = cb.isSelected();
            Utils.setprefb("drinkKey",val);
        });

        pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTabControls.add(pan,c);
        pan.add(cbDrinkAuto= new JCheckBox("Auto drink at"), c);
        cbDrinkAuto.setSelected(ZeeConfig.drinkAuto);
        cbDrinkAuto.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.drinkAuto = cb.isSelected();
            Utils.setprefb("drinkAuto",val);
            cmbDrinkAutoValue.setEnabled(val);
            if (ZeeInvMainOptionsWdg.cbDrink!=null)
                ZeeInvMainOptionsWdg.cbDrink.set(val);
        });
        //drink auto limit
        String[] perc = {"30%","40%","50%","60%","70%","80%"};
        pan.add(cmbDrinkAutoValue = new JComboBox<String>(perc), c);
        cmbDrinkAutoValue.setSelectedItem(ZeeConfig.drinkAutoValue+"%");
        cmbDrinkAutoValue.setEnabled(ZeeConfig.drinkAuto);
        cmbDrinkAutoValue.addActionListener(e -> {
            String val = cmbDrinkAutoValue.getSelectedItem().toString().split("%")[0];
            Integer num = ZeeConfig.drinkAutoValue = Integer.parseInt(val);
            Utils.setprefi("drinkAutoValue", num);
        });
    }

    private void buildTabMinimap() {

        panelTabMinimap = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Minimap", panelTabMinimap);

        panelTabMinimap.add(cbSlowMiniMap = new JCheckBox("Slower mini-map"), c);
        cbSlowMiniMap.setSelected(ZeeConfig.slowMiniMap);
        cbSlowMiniMap.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.slowMiniMap = cb.isSelected();
            Utils.setprefb("slowMiniMap",val);
        });

        panelTabMinimap.add(cbShowIconsZoomOut = new JCheckBox("Show icons while zoomed out"), c);
        cbShowIconsZoomOut.setSelected(ZeeConfig.showIconsZoomOut);
        cbShowIconsZoomOut.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showIconsZoomOut = cb.isSelected();
            Utils.setprefb("showIconsZoomOut",val);
        });

        // checkbox shape icons
        panelTabMinimap.add(cbShapeIcons = new JCheckBox("Show basic shape icons"), c);
        cbShapeIcons.setSelected(ZeeConfig.shapeIcons);
        cbShapeIcons.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.shapeIcons = cb.isSelected();
            Utils.setprefb("shapeIcons",val);
            comboShapeIcons.setEnabled(ZeeConfig.shapeIcons);
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
            panelShapeIconsSaveCancel.add(btnShapeIconSave = new JButton("Save"));
            btnShapeIconSave.addActionListener(this::saveShapeIcons);
            panelShapeIconsSaveCancel.add(btnSapeIconDelete = new JButton("Delete"));
            btnSapeIconDelete.addActionListener(this::deleteShapeIcons);
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
            ZeeConfig.println("mapRuleImg size "+ZeeManagerIcons.mapRuleImg.size());
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

    private void buildTabMisc() {

        JPanel pan;

        panelTabMisc = new JPanel(new GridBagLayout());
        tabbedPane.addTab("Misc", panelTabMisc);

        panelTabMisc.add(cbHideFxAnimations = new JCheckBox("hide animations"), c);
        cbHideFxAnimations.setSelected(ZeeConfig.hideFxAnimations);
        cbHideFxAnimations.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.hideFxAnimations = cb.isSelected();
            Utils.setprefb("hideFxAnimations",val);
        });

        panelTabMisc.add(cbHideFxSmoke = new JCheckBox("hide smoke effects"), c);
        cbHideFxSmoke.setSelected(ZeeConfig.hideFxSmoke);
        cbHideFxSmoke.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.hideFxSmoke = cb.isSelected();
            Utils.setprefb("hideFxSmoke",val);
        });

        panelTabMisc.add(cbAutoChipMinedBoulder = new JCheckBox("Auto chip mined boulder"), c);
        cbAutoChipMinedBoulder.setSelected(ZeeConfig.autoChipMinedBoulder);
        cbAutoChipMinedBoulder.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoChipMinedBoulder = cb.isSelected();
            Utils.setprefb("autoChipMinedBoulder",val);
        });

        panelTabMisc.add(cbDropMinedStone = new JCheckBox("Drop mined stones"), c);
        cbDropMinedStone.setSelected(ZeeConfig.dropMinedStones);
        cbDropMinedStone.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedStones = cb.isSelected();
            Utils.setprefb("dropMinedStones",val);
        });

        panelTabMisc.add(cbDropMinedOre = new JCheckBox("Drop mined ore"), c);
        cbDropMinedOre.setSelected(ZeeConfig.dropMinedOre);
        cbDropMinedOre.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedOre = cb.isSelected();
            Utils.setprefb("dropMinedOre",val);
        });

        panelTabMisc.add(cbDropMinedSilverGold = new JCheckBox("Drop mined silver/gold"), c);
        cbDropMinedSilverGold.setSelected(ZeeConfig.dropMinedSilverGold);
        cbDropMinedSilverGold.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedSilverGold = cb.isSelected();
            Utils.setprefb("dropMinedOrePrecious",val);
        });

        panelTabMisc.add(cbDropMinedCurios = new JCheckBox("Drop mined curios"), c);
        cbDropMinedCurios.setSelected(ZeeConfig.dropMinedCurios);
        cbDropMinedCurios.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedCurios = cb.isSelected();
            Utils.setprefb("dropMinedCurios",val);
        });

        panelTabMisc.add(cbAlertOnPlayers = new JCheckBox("Sound alert on players"), c);
        cbAlertOnPlayers.setSelected(ZeeConfig.alertOnPlayers);
        cbAlertOnPlayers.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.alertOnPlayers = cb.isSelected();
            Utils.setprefb("alertOnPlayers",val);
        });

        panelTabMisc.add(cbAutohearth = new JCheckBox("Auto-hearth on players"), c);
        cbAutohearth.setSelected(ZeeConfig.autoHearthOnStranger);
        cbAutohearth.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoHearthOnStranger = cb.isSelected();
            Utils.setprefb("autoHearthOnStranger",val);
        });

        panelTabMisc.add(cbAutoRunLogin = new JCheckBox("Auto-run on login"), c);
        cbAutoRunLogin.setSelected(ZeeConfig.autoRunLogin);
        cbAutoRunLogin.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoRunLogin = cb.isSelected();
            Utils.setprefb("autoRunLogin",val);
        });

        panelTabMisc.add(cbHighlightCropsReady = new JCheckBox("Highlight crops ready"), c);
        cbHighlightCropsReady.setSelected(ZeeConfig.highlightCropsReady);
        cbHighlightCropsReady.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.highlightCropsReady = cb.isSelected();
            Utils.setprefb("highlightCropsReady",val);
        });

        panelTabMisc.add(cbShowGrowingTreePercentage = new JCheckBox("Show growing tree %"), c);
        cbShowGrowingTreePercentage.setSelected(ZeeConfig.showGrowingTreePercentage);
        cbShowGrowingTreePercentage.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showGrowingTreePercentage = cb.isSelected();
            Utils.setprefb("showGrowingTreePercentage",val);
        });

        panelTabMisc.add(cbTreeAnimation = new JCheckBox("Show tree animation"), c);
        cbTreeAnimation.setSelected(ZeeConfig.treeAnimation);
        cbTreeAnimation.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.treeAnimation = cb.isSelected();
            Utils.setprefb("treeAnimation",val);
        });

        pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTabMisc.add(pan,c);
        pan.add(cbMiniTrees= new JCheckBox("Mini trees :3"), c);
        cbMiniTrees.setSelected(ZeeConfig.miniTrees);
        cbMiniTrees.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.miniTrees = cb.isSelected();
            Utils.setprefb("miniTrees",val);
            cmbMiniTreeSize.setEnabled(val);
        });

        //mini trees size
        String[] perc = {"30%","40%","50%","60%","70%","80%"};
        pan.add(cmbMiniTreeSize = new JComboBox<String>(perc), c);
        cmbMiniTreeSize.setSelectedItem(ZeeConfig.miniTreesSize+"%");
        cmbMiniTreeSize.setEnabled(ZeeConfig.miniTrees);
        cmbMiniTreeSize.addActionListener(e -> {
            String val = cmbMiniTreeSize.getSelectedItem().toString().split("%")[0];
            Integer num = ZeeConfig.miniTreesSize = Integer.parseInt(val);
            Utils.setprefi("miniTreesSize", num);
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

        //auto click menu list
        panelTabMisc.add(new JLabel("Automenu list:"), c);
        panelTabMisc.add(tfAutoClickMenu = new JTextField("",5), c);
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
        panelTabMisc.add(new JLabel("Butchermode list:"), c);
        panelTabMisc.add(tfButchermode= new JTextField("",5), c);
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

        panelTabMisc.add(cbDebugWidgetMsgs= new JCheckBox("Debug widget msgs"), c);
        cbDebugWidgetMsgs.setSelected(ZeeConfig.debugWidgetMsgs);
        cbDebugWidgetMsgs.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.debugWidgetMsgs = cb.isSelected();
            //no need to save pref, always start false
        });

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

        panelTabInterface.add(cbShowKinNames = new JCheckBox("Show kin names (hearthfire)"), c);
        cbShowKinNames.setSelected(ZeeConfig.showKinNames);
        cbShowKinNames.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showKinNames = cb.isSelected();
            Utils.setprefb("showKinNames",val);
        });

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

        panelTabInterface.add(cbSimpleButtons = new JCheckBox("Simple buttons (logoff)"), c);
        cbSimpleButtons.setSelected(ZeeConfig.simpleButtons);
        cbSimpleButtons.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.simpleButtons = cb.isSelected();
            Utils.setprefb("simpleButtons",val);
        });

        panelTabInterface.add(cbCompactEquipsWindow = new JCheckBox("Compact equip window(restart)"), c);
        cbCompactEquipsWindow.setSelected(ZeeConfig.equiporyCompact);
        cbCompactEquipsWindow.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.equiporyCompact = cb.isSelected();
            Utils.setprefb("equiporyCompact",val);
        });

        panelTabInterface.add(cbBeltTogglesEquips = new JCheckBox("Auto toggle equips window"), c);
        cbBeltTogglesEquips.setSelected(ZeeConfig.autoToggleEquips);
        cbBeltTogglesEquips.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoToggleEquips = cb.isSelected();
            Utils.setprefb("autoToggleEquips",val);
        });

        panelTabInterface.add(cbBeltTogglesEquipsReposition = new JCheckBox("Reposition toggled equips window"), c);
        cbBeltTogglesEquipsReposition.setSelected(ZeeConfig.autoToggleEquipsReposition);
        cbBeltTogglesEquipsReposition.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoToggleEquipsReposition = cb.isSelected();
            Utils.setprefb("autoToggleEquipsReposition",val);
        });

        panelTabInterface.add(cbShowEquipsLogin = new JCheckBox("Show equips at login"), c);
        cbShowEquipsLogin.setSelected(ZeeConfig.showEquipsLogin);
        cbShowEquipsLogin.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showEquipsLogin = cb.isSelected();
            Utils.setprefb("showEquipsLogin",val);
        });

        panelTabInterface.add(cbMidclickEquipManager = new JCheckBox("Mid-click belt item to autoequip"), c);
        cbMidclickEquipManager.setSelected(ZeeConfig.midclickEquipManager);
        cbMidclickEquipManager.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.midclickEquipManager = cb.isSelected();
            Utils.setprefb("midclickEquipManager",val);
        });

        panelTabInterface.add(cbShowInventoryLogin = new JCheckBox("Show inventory at login"), c);
        cbShowInventoryLogin.setSelected(ZeeConfig.showInventoryLogin);
        cbShowInventoryLogin.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showInventoryLogin = cb.isSelected();
            Utils.setprefb("showInventoryLogin",val);
        });

        panelTabInterface.add(cbShowBeltLogin = new JCheckBox("Show belt at login"), c);
        cbShowBeltLogin.setSelected(ZeeConfig.autoOpenBelt);
        cbShowBeltLogin.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoOpenBelt = cb.isSelected();
            Utils.setprefb("autoOpenBelt",val);
        });

        panelTabInterface.add(cbNotifyBuddyOnline = new JCheckBox("Notify when friends login"), c);
        cbNotifyBuddyOnline.setSelected(ZeeConfig.notifyBuddyOnline);
        cbNotifyBuddyOnline.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.notifyBuddyOnline = cb.isSelected();
            Utils.setprefb("notifyBuddyOnline",val);
        });

        panelTabInterface.add(cbZoomOrthoExtended = new JCheckBox("Zoom extended for Ortho cam"), c);
        cbZoomOrthoExtended.setSelected(ZeeConfig.zoomOrthoExtended);
        cbZoomOrthoExtended.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.zoomOrthoExtended = cb.isSelected();
            Utils.setprefb("zoomOrthoExtended",val);
        });

        panelTabInterface.add(cbActionSearchGlobal = new JCheckBox("Action search globally"), c);
        cbActionSearchGlobal.setSelected(ZeeConfig.actionSearchGlobal);
        cbActionSearchGlobal.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.actionSearchGlobal = cb.isSelected();
            Utils.setprefb("actionSearchGlobal",val);
        });

        panelTabInterface.add(cbRememberWindowsPos= new JCheckBox("Remember windows pos"), c);
        cbRememberWindowsPos.setSelected(ZeeConfig.rememberWindowsPos);
        cbRememberWindowsPos.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.rememberWindowsPos = cb.isSelected();
            Utils.setprefb("rememberWindowsPos",val);
        });

        panelTabInterface.add(btnResetWindowsPos = new JButton("reset windows pos ("+ZeeConfig.mapWindowPos.size()+")"), c);
        btnResetWindowsPos.addActionListener(evt -> {
            resetWindowsPos();
        });

        panelTabInterface.add(cbSortActionsByUse= new JCheckBox("Sort actions by uses"), c);
        cbSortActionsByUse.setSelected(ZeeConfig.sortActionsByUses);
        cbSortActionsByUse.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.sortActionsByUses = cb.isSelected();
            Utils.setprefb("sortActionsByUses",val);
        });

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
        String[] perc = {"30%","40%","50%","60%","70%","80%","90%","100%"};
        panelTabInterface.add(cmbCattleRoster = new JComboBox<String>(perc), c);
        cmbCattleRoster.setMaximumSize(new Dimension(Integer.MAX_VALUE, cmbCattleRoster.getPreferredSize().height));
        cmbCattleRoster.setSelectedItem(((int)(ZeeConfig.cattleRosterHeightPercentage*100))+"%");
        cmbCattleRoster.setEnabled(ZeeConfig.cattleRosterHeight);
        cmbCattleRoster.addActionListener(e -> {
            String val = cmbCattleRoster.getSelectedItem().toString().split("%")[0];
            double d = ZeeConfig.cattleRosterHeightPercentage = Double.parseDouble(val) / 100;
            Utils.setprefd("cattleRosterHeightPercentage", d);
        });

        panelTabInterface.add(cbAutoToggleGridLines = new JCheckBox("Auto-toggle grid lines"), c);
        cbAutoToggleGridLines.setSelected(ZeeConfig.autoToggleGridLines);
        cbAutoToggleGridLines.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoToggleGridLines = cb.isSelected();
            Utils.setprefb("autoToggleGridLines",val);
        });

        pan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTabInterface.add(pan,c);
        pan.add(btnGridGolor= new JButton("grid color"));
        colBtn = ZeeConfig.intToColor(ZeeConfig.gridColorInt);
        btnGridGolor.setBackground(colBtn);
        btnGridGolor.setForeground(ZeeConfig.getComplementaryColor(colBtn));
        btnGridGolor.addActionListener(evt->{
            Color color = JColorChooser.showDialog(panelTabMisc, "Pick Color", ZeeConfig.intToColor(ZeeConfig.gridColorInt), true);
            if (color==null)
                color = ZeeConfig.DEF_GRID_COLOR;
            btnGridGolor.setBackground(color);
            btnGridGolor.setForeground(ZeeConfig.getComplementaryColor(color));
            ZeeConfig.gridColorInt = ZeeConfig.colorToInt(color);
            Utils.setprefi("gridColorInt",ZeeConfig.gridColorInt);
            ZeeConfig.newGridColor(color);
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
        if(ZeeConfig.mapGobSession.size() > 0) {
            SortedSet<String> keys = new TreeSet<String>(ZeeConfig.mapGobSession.keySet());
            listGobsTemp = new JList<String>(keys.toArray(new String[0]));
        }else {
            listGobsTemp = new JList<String>();
        }
        JPanel panelTabGobSess = new JPanel(new GridBagLayout());
        panelTabGobSess.add(new JScrollPane(listGobsTemp), c);
        tabbedPaneGobs.addTab("Session("+ZeeConfig.mapGobSession.size()+")", panelTabGobSess);


        //panel gobs main buttons
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
    }

    private JList<String> fillUpListGobsSaved() {
        Set<String> set = new HashSet<String>();
        JList<String> ret;
        if(ZeeConfig.mapGobAudio.size() > 0) {
            set.addAll(ZeeConfig.mapGobAudio.keySet());
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

        //gob name
        JPanel panelGobName = new JPanel(new GridBagLayout());
        panelDetailsBottom.add(panelGobName, c);
        panelGobName.setBorder(BorderFactory.createTitledBorder("Gob"));
        panelGobName.add(tfGobName = new JTextField(list.getSelectedValue()), c);
        tfGobName.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfGobName.getPreferredSize().height));

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


        //Color highlight
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
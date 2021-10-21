package haven;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ZeecowOptionsWindow extends JFrame {
    public JTabbedPane tabbedPane, tabbedPaneGobs;
    public JPanel panelTabMisc, panelTabInterface, panelTabGobs, panelDetailsBottom, panelTabCateg;
    public JCheckBox cbDropAltKeyOnly, cbCtrlClickMinimapContent, cbDropMinedStone, cbDropMinedOre, cbDropMinedSilverGold, cbDropMinedCurios, cbActionSearchGlobal, cbCompactEquipsWindow, cbBeltTogglesEquips, cbAutohearth, cbHighlighAggressiveGobs, cbHighlightCropsReady, cbHighlightGrowingTrees, cbMiniTrees, cbAlertOnPlayers,  cbShowInventoryLogin, cbShowBeltLogin, cbKeyBeltShiftTab, cbShowIconsZoomOut, cbRememberWindowsPos, cbSortActionsByUse, cbDebugWidgetMsgs, cbShowEquipsLogin, cbNotifyBuddyOnline, cbZoomOrthoExtended, cbCattleRosterHeight;
    public JTextField tfAutoClickMenu, tfButchermode, tfGobName, tfAudioPath, tfCategName, tfAudioPathCateg;
    public JComboBox<String> cmbCattleRoster, cmbGobCategory, cmbMiniTreeSize;
    public JList<String> listGobsTemp, listGobsSaved, listGobsCategories;
    public JButton btnRefresh, btnPrintState, btnResetGobs, btnAudioSave, btnAudioClear, btnAudioTest, btnRemGobFromCateg, btnGobColorAdd, btnCategoryColorAdd, btnGobColorRemove, btnCategoryColorRemove, btnResetCateg, btnAddCateg, btnRemoveCateg, btnResetWindowsPos, btnResetActionUses;
    public JTextArea txtAreaDebug;
    public JSlider sliderAlpha, sliderGobQueueSleep;
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
        getContentPane().setPreferredSize(new Dimension(300,600));
        tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane);

        buildTabMisc();

        buildTabInterface();

        //Gobs tabbbed pane
        panelTabGobs = new JPanel();
        panelTabGobs.setLayout(new BoxLayout(panelTabGobs, BoxLayout.PAGE_AXIS));
        tabbedPane.add("Gobs", panelTabGobs);
        buildTabGobs();
    }


    private void buildTabMisc() {
        panelTabMisc = new JPanel();
        panelTabMisc.setLayout(new BoxLayout(panelTabMisc, BoxLayout.PAGE_AXIS));
        tabbedPane.addTab("Misc", panelTabMisc);

        panelTabMisc.add(Box.createRigidArea(new Dimension(0,5)));

        panelTabMisc.add(cbDropMinedStone = new JCheckBox("Drop mined stones"));
        cbDropMinedStone.setSelected(ZeeConfig.dropMinedStones);
        cbDropMinedStone.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedStones = cb.isSelected();
            Utils.setprefb("dropMinedStones",val);
        });

        panelTabMisc.add(cbDropMinedOre = new JCheckBox("Drop mined ore"));
        cbDropMinedOre.setSelected(ZeeConfig.dropMinedOre);
        cbDropMinedOre.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedOre = cb.isSelected();
            Utils.setprefb("dropMinedOre",val);
        });

        panelTabMisc.add(cbDropMinedSilverGold = new JCheckBox("Drop mined silver/gold"));
        cbDropMinedSilverGold.setSelected(ZeeConfig.dropMinedSilverGold);
        cbDropMinedSilverGold.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedSilverGold = cb.isSelected();
            Utils.setprefb("dropMinedOrePrecious",val);
        });

        panelTabMisc.add(cbDropMinedCurios = new JCheckBox("Drop mined curios"));
        cbDropMinedCurios.setSelected(ZeeConfig.dropMinedCurios);
        cbDropMinedCurios.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedCurios = cb.isSelected();
            Utils.setprefb("dropMinedCurios",val);
        });

        panelTabMisc.add(Box.createRigidArea(new Dimension(0,5)));

        panelTabMisc.add(cbHighlighAggressiveGobs = new JCheckBox("Highlight aggressive gobs"));
        cbHighlighAggressiveGobs.setSelected(ZeeConfig.highlightAggressiveGobs);
        cbHighlighAggressiveGobs.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.highlightAggressiveGobs = cb.isSelected();
            Utils.setprefb("highlighAggressiveGobs",val);
        });

        panelTabMisc.add(cbHighlightCropsReady = new JCheckBox("Highlight crops ready"));
        cbHighlightCropsReady.setSelected(ZeeConfig.highlightCropsReady);
        cbHighlightCropsReady.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.highlightCropsReady = cb.isSelected();
            Utils.setprefb("highlightCropsReady",val);
        });

        panelTabMisc.add(cbHighlightGrowingTrees = new JCheckBox("Highlight growing trees"));
        cbHighlightGrowingTrees.setSelected(ZeeConfig.highlightGrowingTrees);
        cbHighlightGrowingTrees.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.highlightGrowingTrees = cb.isSelected();
            Utils.setprefb("highlightGrowingTrees",val);
        });

        panelTabMisc.add(Box.createRigidArea(new Dimension(0,5)));

        panelTabMisc.add(cbMiniTrees= new JCheckBox("Mini trees :3"));
        cbMiniTrees.setSelected(ZeeConfig.miniTrees);
        cbMiniTrees.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.miniTrees = cb.isSelected();
            Utils.setprefb("miniTrees",val);
            cmbMiniTreeSize.setEnabled(val);
        });

        //mini trees size
        String[] perc = {"30%","40%","50%","60%","70%","80%"};
        panelTabMisc.add(cmbMiniTreeSize = new JComboBox<String>(perc));
        cmbMiniTreeSize.setMaximumSize(new Dimension(Integer.MAX_VALUE, cmbMiniTreeSize.getPreferredSize().height));
        cmbMiniTreeSize.setSelectedItem(ZeeConfig.miniTreesSize+"%");
        cmbMiniTreeSize.setEnabled(ZeeConfig.miniTrees);
        cmbMiniTreeSize.addActionListener(e -> {
            String val = cmbMiniTreeSize.getSelectedItem().toString().split("%")[0];
            Integer num = ZeeConfig.miniTreesSize = Integer.parseInt(val);
            Utils.setprefi("miniTreesSize", num);
        });

        panelTabMisc.add(Box.createRigidArea(new Dimension(0,5)));

        panelTabMisc.add(cbActionSearchGlobal = new JCheckBox("Action search global"));
        cbActionSearchGlobal.setSelected(ZeeConfig.actionSearchGlobal);
        cbActionSearchGlobal.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.actionSearchGlobal = cb.isSelected();
            Utils.setprefb("actionSearchGlobal",val);
        });

        panelTabMisc.add(cbAlertOnPlayers = new JCheckBox("Sound alert on players"));
        cbAlertOnPlayers.setSelected(ZeeConfig.alertOnPlayers);
        cbAlertOnPlayers.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.alertOnPlayers = cb.isSelected();
            Utils.setprefb("alertOnPlayers",val);
        });

        panelTabMisc.add(cbAutohearth = new JCheckBox("Auto-hearth on players"));
        cbAutohearth.setSelected(ZeeConfig.autoHearthOnStranger);
        cbAutohearth.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoHearthOnStranger = cb.isSelected();
            Utils.setprefb("autoHearthOnStranger",val);
        });

        panelTabMisc.add(cbNotifyBuddyOnline = new JCheckBox("Notify when friends login"));
        cbNotifyBuddyOnline.setSelected(ZeeConfig.notifyBuddyOnline);
        cbNotifyBuddyOnline.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.notifyBuddyOnline = cb.isSelected();
            Utils.setprefb("notifyBuddyOnline",val);
        });

        panelTabMisc.add(cbZoomOrthoExtended = new JCheckBox("Zoom extended for Ortho cam"));
        cbZoomOrthoExtended.setSelected(ZeeConfig.zoomOrthoExtended);
        cbZoomOrthoExtended.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.zoomOrthoExtended = cb.isSelected();
            Utils.setprefb("zoomOrthoExtended",val);
        });

        panelTabMisc.add(Box.createRigidArea(new Dimension(0,5)));

        //auto click menu list
        panelTabMisc.add(new JLabel("Automenu list:"));
        panelTabMisc.add(tfAutoClickMenu = new JTextField("",5));
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
        panelTabMisc.add(new JLabel("Butchermode list:"));
        panelTabMisc.add(tfButchermode= new JTextField("",5));
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

    private void buildTabInterface() {
        panelTabInterface = new JPanel();
        panelTabInterface.setLayout(new BoxLayout(panelTabInterface, BoxLayout.PAGE_AXIS));
        tabbedPane.addTab("Interface", panelTabInterface);

        panelTabInterface.add(cbDropAltKeyOnly = new JCheckBox("Alt+click drops holding item"));
        cbDropAltKeyOnly.setSelected(ZeeConfig.dropHoldingItemAltKey);
        cbDropAltKeyOnly.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropHoldingItemAltKey = cb.isSelected();
            Utils.setprefb("dropHoldingItemAltKey",val);
        });

        panelTabInterface.add(cbCtrlClickMinimapContent = new JCheckBox("Ctrl scroll/resize minimap"));
        cbCtrlClickMinimapContent.setSelected(ZeeConfig.ctrlClickMinimapContent);
        cbCtrlClickMinimapContent.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.ctrlClickMinimapContent = cb.isSelected();
            Utils.setprefb("dropCtrlClickMinimapContent",val);
        });

        panelTabInterface.add(cbCompactEquipsWindow = new JCheckBox("Compact equip window(restart)"));
        cbCompactEquipsWindow.setSelected(ZeeConfig.equiporyCompact);
        cbCompactEquipsWindow.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.equiporyCompact = cb.isSelected();
            Utils.setprefb("equiporyCompact",val);
        });

        panelTabInterface.add(cbBeltTogglesEquips = new JCheckBox("Auto toggle equips window"));
        cbBeltTogglesEquips.setSelected(ZeeConfig.autoOpenEquips);
        cbBeltTogglesEquips.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoOpenEquips = cb.isSelected();
            Utils.setprefb("beltToggleEquips",val);
        });

        panelTabInterface.add(cbShowEquipsLogin = new JCheckBox("Show equips at login"));
        cbShowEquipsLogin.setSelected(ZeeConfig.showEquipsLogin);
        cbShowEquipsLogin.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showEquipsLogin = cb.isSelected();
            Utils.setprefb("showEquipsLogin",val);
        });

        panelTabInterface.add(cbShowInventoryLogin = new JCheckBox("Show inventory at login"));
        cbShowInventoryLogin.setSelected(ZeeConfig.showInventoryLogin);
        cbShowInventoryLogin.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showInventoryLogin = cb.isSelected();
            Utils.setprefb("showInventoryLogin",val);
        });

        panelTabInterface.add(cbShowBeltLogin = new JCheckBox("Show belt at login"));
        cbShowBeltLogin.setSelected(ZeeConfig.autoOpenBelt);
        cbShowBeltLogin.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoOpenBelt = cb.isSelected();
            Utils.setprefb("autoOpenBelt",val);
        });

        panelTabInterface.add(cbKeyBeltShiftTab = new JCheckBox("Shift+Tab toggles belt"));
        cbKeyBeltShiftTab.setSelected(ZeeConfig.keyBeltShiftTab);
        cbKeyBeltShiftTab.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.keyBeltShiftTab = cb.isSelected();
            Utils.setprefb("keyBeltShiftTab",val);
        });

        panelTabInterface.add(cbShowIconsZoomOut = new JCheckBox("Show icons while zoomed out"));
        cbShowIconsZoomOut.setSelected(ZeeConfig.showIconsZoomOut);
        cbShowIconsZoomOut.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showIconsZoomOut = cb.isSelected();
            Utils.setprefb("showIconsZoomOut",val);
        });

        panelTabInterface.add(Box.createRigidArea(new Dimension(0,25)));

        panelTabInterface.add(cbRememberWindowsPos= new JCheckBox("Remember windows pos"));
        cbRememberWindowsPos.setSelected(ZeeConfig.rememberWindowsPos);
        cbRememberWindowsPos.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.rememberWindowsPos = cb.isSelected();
            Utils.setprefb("rememberWindowsPos",val);
        });

        panelTabInterface.add(btnResetWindowsPos = new JButton("reset windows pos ("+ZeeConfig.mapWindowPos.size()+")"));
        btnResetWindowsPos.addActionListener(evt -> {
            resetWindowsPos();
        });

        panelTabInterface.add(Box.createRigidArea(new Dimension(0,25)));

        panelTabInterface.add(cbSortActionsByUse= new JCheckBox("Sort actions by uses"));
        cbSortActionsByUse.setSelected(ZeeConfig.sortActionsByUses);
        cbSortActionsByUse.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.sortActionsByUses = cb.isSelected();
            Utils.setprefb("sortActionsByUses",val);
        });

        panelTabInterface.add(btnResetActionUses = new JButton("reset actions uses ("+ZeeConfig.mapActionUses.size()+")"));
        btnResetActionUses.addActionListener(evt -> {
            resetActionUses();
        });

        panelTabInterface.add(Box.createRigidArea(new Dimension(0,25)));

        //cattle roster height
        panelTabInterface.add(cbCattleRosterHeight = new JCheckBox("Cattle Roster height(logout)"));
        cbCattleRosterHeight.setSelected(ZeeConfig.cattleRosterHeight);
        cbCattleRosterHeight.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.cattleRosterHeight = cb.isSelected();
            Utils.setprefb("cattleRosterHeight",val);
            cmbCattleRoster.setEnabled(val);
        });
        String[] perc = {"30%","40%","50%","60%","70%","80%","90%","100%"};
        panelTabInterface.add(cmbCattleRoster = new JComboBox<String>(perc));
        cmbCattleRoster.setMaximumSize(new Dimension(Integer.MAX_VALUE, cmbCattleRoster.getPreferredSize().height));
        cmbCattleRoster.setSelectedItem(((int)(ZeeConfig.cattleRosterHeightPercentage*100))+"%");
        cmbCattleRoster.setEnabled(ZeeConfig.cattleRosterHeight);
        cmbCattleRoster.addActionListener(e -> {
            String val = cmbCattleRoster.getSelectedItem().toString().split("%")[0];
            double d = ZeeConfig.cattleRosterHeightPercentage = Double.parseDouble(val) / 100;
            Utils.setprefd("cattleRosterHeightPercentage", d);
        });


        panelTabInterface.add(Box.createRigidArea(new Dimension(0,25)));

        panelTabInterface.add(cbDebugWidgetMsgs= new JCheckBox("Debug widget msgs"));
        cbDebugWidgetMsgs.setSelected(ZeeConfig.debugWidgetMsgs);
        cbDebugWidgetMsgs.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.debugWidgetMsgs = cb.isSelected();
            //no need to save pref, always start false
        });
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


    private void buildTabGobs() {
        panelTabGobs.removeAll();

        //panel bottom details
        if(panelDetailsBottom !=null) {
            panelDetailsBottom.repaint();
        }else {
            panelDetailsBottom = new JPanel();
            panelDetailsBottom.setLayout(new BoxLayout(panelDetailsBottom, BoxLayout.PAGE_AXIS));
            panelDetailsBottom.setPreferredSize(new Dimension(300, 300));
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
        panelTabGobs.add(tabbedPaneGobs);


        //subtab gobs session list
        if(ZeeConfig.mapGobSession.size() > 0) {
            SortedSet<String> keys = new TreeSet<String>(ZeeConfig.mapGobSession.keySet());
            listGobsTemp = new JList<String>(keys.toArray(new String[0]));
        }else {
            listGobsTemp = new JList<String>();
        }
        JPanel panelTabGobSess = new JPanel();
        panelTabGobSess.setLayout(new BoxLayout(panelTabGobSess,BoxLayout.PAGE_AXIS));
        panelTabGobSess.add(new JScrollPane(listGobsTemp));
        tabbedPaneGobs.addTab("Session("+ZeeConfig.mapGobSession.size()+")", panelTabGobSess);


        //panel gobs main buttons
        JPanel panelGobButtons = new JPanel(new FlowLayout());
        panelTabGobSess.add(panelGobButtons);
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
        panelTabCateg = new JPanel();
        panelTabCateg.setLayout(new BoxLayout(panelTabCateg, BoxLayout.PAGE_AXIS));
        tabbedPaneGobs.addTab("Categs("+ZeeConfig.mapCategoryGobs.size()+")", panelTabCateg);
        JPanel panelButtonCateg = new JPanel(new FlowLayout());
        panelTabCateg.add(panelButtonCateg);
        panelButtonCateg.add(btnResetCateg = new JButton("Reset"), BorderLayout.NORTH);
        btnResetCateg.addActionListener(evt->{ resetCategoriesToDefault(); });
        panelButtonCateg.add(btnAddCateg = new JButton("Add"), BorderLayout.NORTH);
        btnAddCateg.addActionListener(evt->{ addCategoryNew(); });
        panelButtonCateg.add(btnRemoveCateg = new JButton("Remove"), BorderLayout.NORTH);
        btnRemoveCateg.addActionListener(evt->{ removeCategory(); });
        panelTabCateg.add(new JScrollPane(listGobsCategories), BorderLayout.CENTER);
        listGobsCategories.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if(evt.getValueIsAdjusting() || tabbedPaneGobs.getSelectedIndex()!=TABGOB_CATEGS)
                    return;
                updatePanelDetails();
            }
        });

        panelTabGobs.add(panelDetailsBottom);
        pack();
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
        JPanel panelGobName = new JPanel();
        panelDetailsBottom.add(panelGobName);
        panelGobName.setBorder(BorderFactory.createTitledBorder("Gob"));
        panelGobName.add(tfGobName = new JTextField(list.getSelectedValue()),BorderLayout.NORTH);
        tfGobName.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfGobName.getPreferredSize().height));

        //audio file name
        JPanel panelGobAudio = new JPanel();
        panelDetailsBottom.add(panelGobAudio);
        panelGobAudio.setBorder(BorderFactory.createTitledBorder("Audio"));
        panelGobAudio.add(Box.createVerticalGlue());
        String audioPath = ZeeConfig.mapGobAudio.get(list.getSelectedValue());
        if(audioPath==null)
            audioPath = "";
        panelGobAudio.add(tfAudioPath = new JTextField(audioPath),BorderLayout.CENTER);
        tfAudioPath.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfAudioPath.getPreferredSize().height));

        //audio buttons
        JPanel panelAudioButtons;
        panelGobAudio.add(panelAudioButtons = new JPanel(new FlowLayout()));
        panelAudioButtons.add(btnAudioSave = new JButton("Select"),BorderLayout.SOUTH);
        btnAudioSave.addActionListener(evt->{ audioSave(); });
        panelAudioButtons.add(btnAudioClear = new JButton("Clear"),BorderLayout.SOUTH);
        btnAudioClear.addActionListener(evt->{ audioClear(); });
        panelAudioButtons.add(btnAudioTest = new JButton("Play"),BorderLayout.SOUTH);
        btnAudioTest.addActionListener(evt->{ audioTest(); });


        //combo category
        JPanel panelAddToCategory = new JPanel();
        panelDetailsBottom.add(panelAddToCategory);
        panelAddToCategory.setBorder(BorderFactory.createTitledBorder("Category"));
        panelAddToCategory.add(cmbGobCategory = new JComboBox<String>(ZeeConfig.mapCategoryGobs.keySet().toArray(new String[0])),BorderLayout.CENTER);
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
        panelAddToCategory.add(Box.createVerticalGlue());
        panelAddToCategory.add(btnRemGobFromCateg = new JButton("Clear"),BorderLayout.SOUTH);
        btnRemGobFromCateg.addActionListener(evt->{ removeGobFromCategory(); });


        //Color highlight
        //button
        JPanel panelHighlight = new JPanel(new GridLayout(1,3));
        panelDetailsBottom.add(panelHighlight);
        panelHighlight.setBorder(BorderFactory.createTitledBorder("Highlight Color"));
        panelHighlight.add(btnGobColorAdd = new JButton("Select"));
        btnGobColorAdd.addActionListener(evt->{
            Color color = JColorChooser.showDialog(panelHighlight, "Gob Highlight Color", Color.MAGENTA);
            if(color!=null){
                addGobColor(tfGobName.getText(), color);
            }
        });
        panelHighlight.add(btnGobColorRemove = new JButton("Clear"));
        btnGobColorRemove.addActionListener(evt->{
            removeGobColor();
        });
        //slider transparency
        Color color = ZeeConfig.mapGobColor.get(tfGobName.getText());
        int alpha = color!=null ? color.getAlpha() : 200;
        sliderAlpha = new JSlider(JSlider.HORIZONTAL,0, 255, alpha);
        panelHighlight.add(sliderAlpha);
        sliderAlpha.addChangeListener(evt -> {
            if(!sliderAlpha.getValueIsAdjusting()) {
                updateGobColorAlpha();
            }
        });
        //update color UI state
        Color currentColor = ZeeConfig.mapGobColor.get(tfGobName.getText());
        if(currentColor!=null){
            btnGobColorAdd.getParent().setBackground(currentColor);
            sliderAlpha.setValue(currentColor.getAlpha());
        }

        pack();
    }

    private void updateGobColorAlpha() {
        Color c = ZeeConfig.mapGobColor.get(tfGobName.getText());
        if(c==null)
            return;
        int alpha = sliderAlpha.getValue();
        c = new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
        ZeeConfig.mapGobColor.put(tfGobName.getText(),c);
        Utils.setpref(ZeeConfig.MAP_GOB_COLOR, ZeeConfig.serialize(ZeeConfig.mapGobColor));
        sliderAlpha.getParent().repaint();
    }

    private void updateCategoryColorAlpha() {
        Color c = ZeeConfig.mapCategoryColor.get(tfCategName.getText());
        if(c==null)
            return;
        int alpha = sliderAlpha.getValue();
        c = new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
        ZeeConfig.mapCategoryColor.put(tfCategName.getText(), c);
        Utils.setpref(ZeeConfig.MAP_CATEGORY_COLOR, ZeeConfig.serialize(ZeeConfig.mapCategoryColor));
        sliderAlpha.getParent().repaint();
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
        JPanel panelCategName = new JPanel();
        panelDetailsBottom.add(panelCategName);
        panelCategName.setBorder(BorderFactory.createTitledBorder("Category"));
        panelCategName.add(tfCategName = new JTextField(listGobsCategories.getSelectedValue()));
        tfCategName.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfCategName.getPreferredSize().height));
        panelCategName.setMaximumSize(new Dimension(Integer.MAX_VALUE, panelCategName.getPreferredSize().height));

        //category audio file
        JPanel panelCategAudio = new JPanel();
        panelDetailsBottom.add(panelCategAudio);
        panelCategAudio.setBorder(BorderFactory.createTitledBorder("Audio"));
        String audioPath = ZeeConfig.mapCategoryAudio.get(listGobsCategories.getSelectedValue());
        if(audioPath==null)
            audioPath = "";
        panelCategAudio.add(tfAudioPathCateg = new JTextField(audioPath));
        tfAudioPathCateg.setMaximumSize(new Dimension(Integer.MAX_VALUE, tfAudioPathCateg.getPreferredSize().height));

        //audio buttons
        JPanel panelCategAudioButtons = new JPanel();
        panelCategAudio.add(panelCategAudioButtons);
        panelCategAudioButtons.add(btnAudioSave = new JButton("Select"));
        btnAudioSave.addActionListener(evt->{ audioSave(); });
        panelCategAudioButtons.add(btnAudioClear = new JButton("Clear"));
        btnAudioClear.addActionListener(evt->{ audioClear(); });
        panelCategAudioButtons.add(btnAudioTest = new JButton("Test"));
        btnAudioTest.addActionListener(evt->{ audioTest(); });
        //panelCategAudio.setMaximumSize(new Dimension(Integer.MAX_VALUE, panelCategAudio.getPreferredSize().height));


        //Category Color highlight
        //button
        JPanel panelHighlight = new JPanel(new GridLayout(1,3));
        panelDetailsBottom.add(panelHighlight);
        panelHighlight.setBorder(BorderFactory.createTitledBorder("Highlight Color"));
        panelHighlight.add(btnCategoryColorAdd = new JButton("Select"));
        btnCategoryColorAdd.addActionListener(evt->{
            Color color = JColorChooser.showDialog(panelHighlight, "Category Highlight Color", Color.MAGENTA);
            if(color!=null){
                addCategoryColor(tfCategName.getText(), color);
            }
        });
        panelHighlight.add(btnCategoryColorRemove = new JButton("Clear"));
        btnCategoryColorRemove.addActionListener(evt->{
            removeCategoryColor();
        });
        //slider transparency
        Color color = ZeeConfig.mapCategoryColor.get(tfCategName.getText());
        int alpha = color!=null ? color.getAlpha() : 200;
        if(alpha == 1)
            alpha = 200;
        sliderAlpha = new JSlider(JSlider.HORIZONTAL,0, 255, alpha);
        panelHighlight.add(sliderAlpha);
        sliderAlpha.addChangeListener(evt -> {
            if(!sliderAlpha.getValueIsAdjusting()) {
                updateCategoryColorAlpha();
            }
        });
        //update color UI state
        Color currentColor = ZeeConfig.mapCategoryColor.get(tfCategName.getText());
        if(currentColor!=null){
            btnCategoryColorAdd.getParent().setBackground(currentColor);
            sliderAlpha.setValue(currentColor.getAlpha());
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
        int alpha = sliderAlpha.getValue();
        Color color = new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
        ZeeConfig.mapGobColor.put(gobName, color);
        btnGobColorAdd.getParent().setBackground(color);
        Utils.setpref(ZeeConfig.MAP_GOB_COLOR, ZeeConfig.serialize(ZeeConfig.mapGobColor));
    }

    private void addCategoryColor(String categName, Color c) {
        if (categName == null || categName.isEmpty() || c == null) {
            JOptionPane.showMessageDialog(this, "Category or color parameter missing");
            return;
        }
        int alpha = sliderAlpha.getValue();
        Color color = new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
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
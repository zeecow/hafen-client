package haven;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.*;

public class ZeecowOptionsWindow extends JFrame {
    public JTabbedPane tabbedPane, tabbedPaneGobs;
    public JPanel panelMisc, panelGobs, panelGobDetails, panelDebug, panelTabCateg;
    public JCheckBox cbDropMinedStone, cbDropMinedOre, cbDropMinedSilverGold, cbDropMinedCurios, cbActionSearchGlobal, cbCompactEquipsWindow, cbBeltTogglesEquips, cbAutohearth, cbAlertOnPlayers,  cbShowInventoryLogin, cbShowEquipsLogin, cbNotifyBuddyOnline,cbAutoClickMenuOpts, cbCattleRosterHeight;
    public JTextField tfAutoClickMenu, tfGobName, tfAudioPath, tfCategName, tfAudioPathCateg;
    public JComboBox<String> cmbCattleRoster, cmbGobCategory;
    public JList<String> listGobsTemp, listGobsSaved, listGobsCategories;
    public JButton btnRefresh, btnPrintState, btnResetGobs, btnAudioSave, btnAudioClear, btnAudioTest, btnRemGobFromCateg, btnResetCateg, btnAddCateg;
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
        getContentPane().setPreferredSize(new Dimension(300,600));
        tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane);

        buildPanelMisc();

        //Gobs tabbbed pane
        panelGobs = new JPanel();
        panelGobs.setLayout(new BoxLayout(panelGobs, BoxLayout.PAGE_AXIS));
        tabbedPane.add("Gobs", panelGobs);
        buildPanelGobs();

        buildPanelDebug();
    }

    private void buildPanelDebug() {
        panelDebug = new JPanel();
        getContentPane().add(panelDebug,BorderLayout.SOUTH);
        txtAreaDebug = new JTextArea(3,30);
        panelDebug.add(new JScrollPane(txtAreaDebug));
    }

    private void buildPanelMisc() {
        panelMisc = new JPanel();
        panelMisc.setLayout(new BoxLayout(panelMisc, BoxLayout.PAGE_AXIS));
        tabbedPane.addTab("Misc", panelMisc);

        panelMisc.add(cbDropMinedStone = new JCheckBox("Drop mined stones"));
        cbDropMinedStone.setSelected(ZeeConfig.dropMinedStones);
        cbDropMinedStone.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedStones = cb.isSelected();
            Utils.setprefb("dropMinedStones",val);
        });

        panelMisc.add(cbDropMinedOre = new JCheckBox("Drop mined ore"));
        cbDropMinedOre.setSelected(ZeeConfig.dropMinedOre);
        cbDropMinedOre.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedOre = cb.isSelected();
            Utils.setprefb("dropMinedOre",val);
        });

        panelMisc.add(cbDropMinedSilverGold = new JCheckBox("Drop mined silver/gold"));
        cbDropMinedSilverGold.setSelected(ZeeConfig.dropMinedSilverGold);
        cbDropMinedSilverGold.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedSilverGold = cb.isSelected();
            Utils.setprefb("dropMinedOrePrecious",val);
        });

        panelMisc.add(cbDropMinedCurios = new JCheckBox("Drop mined curios"));
        cbDropMinedCurios.setSelected(ZeeConfig.dropMinedCurios);
        cbDropMinedCurios.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.dropMinedCurios = cb.isSelected();
            Utils.setprefb("dropMinedCurios",val);
        });

        panelMisc.add(new JLabel("--------------------"));

        panelMisc.add(cbActionSearchGlobal = new JCheckBox("Action search global"));
        cbActionSearchGlobal.setSelected(ZeeConfig.actionSearchGlobal);
        cbActionSearchGlobal.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.actionSearchGlobal = cb.isSelected();
            Utils.setprefb("actionSearchGlobal",val);
        });

        panelMisc.add(cbCompactEquipsWindow = new JCheckBox("Compact equip window(restart)"));
        cbCompactEquipsWindow.setSelected(ZeeConfig.equiporyCompact);
        cbCompactEquipsWindow.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.equiporyCompact = cb.isSelected();
            Utils.setprefb("equiporyCompact",val);
        });

        panelMisc.add(cbBeltTogglesEquips = new JCheckBox("Auto toggle equips window"));
        cbBeltTogglesEquips.setSelected(ZeeConfig.autoOpenEquips);
        cbBeltTogglesEquips.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoOpenEquips = cb.isSelected();
            Utils.setprefb("beltToggleEquips",val);
        });

        panelMisc.add(cbAlertOnPlayers = new JCheckBox("Sound alert on players"));
        cbAlertOnPlayers.setSelected(ZeeConfig.alertOnPlayers);
        cbAlertOnPlayers.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.alertOnPlayers = cb.isSelected();
            Utils.setprefb("alertOnPlayers",val);
        });

        panelMisc.add(cbAutohearth = new JCheckBox("Auto-hearth on players"));
        cbAutohearth.setSelected(ZeeConfig.autoHearthOnStranger);
        cbAutohearth.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoHearthOnStranger = cb.isSelected();
            Utils.setprefb("autoHearthOnStranger",val);
        });

        panelMisc.add(cbShowInventoryLogin = new JCheckBox("Show inventory at login"));
        cbShowInventoryLogin.setSelected(ZeeConfig.showInventoryLogin);
        cbShowInventoryLogin.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showInventoryLogin = cb.isSelected();
            Utils.setprefb("showInventoryLogin",val);
        });

        panelMisc.add(cbShowEquipsLogin = new JCheckBox("Show equips at login"));
        cbShowEquipsLogin.setSelected(ZeeConfig.showEquipsLogin);
        cbShowEquipsLogin.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.showEquipsLogin = cb.isSelected();
            Utils.setprefb("showEquipsLogin",val);
        });

        panelMisc.add(cbNotifyBuddyOnline = new JCheckBox("Notify when friends login"));
        cbNotifyBuddyOnline.setSelected(ZeeConfig.notifyBuddyOnline);
        cbNotifyBuddyOnline.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.notifyBuddyOnline = cb.isSelected();
            Utils.setprefb("notifyBuddyOnline",val);
        });

        panelMisc.add(new JLabel("--------------------"));

        //auto click menus
        panelMisc.add(cbAutoClickMenuOpts = new JCheckBox("Auto-click menu:"));
        cbAutoClickMenuOpts.setSelected(ZeeConfig.autoClickMenuOption);
        cbAutoClickMenuOpts.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoClickMenuOption = cb.isSelected();
            Utils.setprefb("autoClickMenuOption",val);
            tfAutoClickMenu.setEnabled(val);
        });
        panelMisc.add(tfAutoClickMenu = new JTextField("",5));
        tfAutoClickMenu.setText(ZeeConfig.autoClickMenuOptionList);
        tfAutoClickMenu.setEnabled(ZeeConfig.autoClickMenuOption);
        tfAutoClickMenu.addActionListener(actionEvent -> {
            String str = actionEvent.getActionCommand();
            String[] strArr = str.split(",");
            if(strArr!=null && strArr.length>0) {
                ZeeConfig.autoClickMenuOptionList = str;
                Utils.setpref("autoClickMenuOptionList",str.strip());
            }
        });


        panelMisc.add(new JLabel("--------------------"));

        //cattle roster height
        panelMisc.add(cbCattleRosterHeight = new JCheckBox("Cattle Roster height(logout)"));
        cbCattleRosterHeight.setSelected(ZeeConfig.cattleRosterHeight);
        cbCattleRosterHeight.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.cattleRosterHeight = cb.isSelected();
            Utils.setprefb("cattleRosterHeight",val);
            cmbCattleRoster.setEnabled(val);
        });
        String[] perc = {"30%","40%","50%","60%","70%","80%","90%","100%"};
        panelMisc.add(cmbCattleRoster = new JComboBox<String>(perc));
        cmbCattleRoster.setSelectedItem(((int)(ZeeConfig.cattleRosterHeightPercentage*100))+"%");
        cmbCattleRoster.setEnabled(ZeeConfig.cattleRosterHeight);
        cmbCattleRoster.addActionListener(e -> {
            String val = cmbCattleRoster.getSelectedItem().toString().split("%")[0];
            double d = ZeeConfig.cattleRosterHeightPercentage = Double.parseDouble(val) / 100;
            Utils.setprefd("cattleRosterHeightPercentage", d);
        });
    }


    private void buildPanelGobs() {
        panelGobs.removeAll();

        //panel bottom details
        if(panelGobDetails!=null) {
            panelGobDetails.repaint();
        }else {
            panelGobDetails = new JPanel();
            panelGobDetails.setLayout(new BoxLayout(panelGobDetails, BoxLayout.PAGE_AXIS));
            panelGobDetails.setPreferredSize(new Dimension(300, 300));
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
                panelGobDetails.removeAll();
            }
        });
        panelGobs.add(tabbedPaneGobs);


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
            buildPanelGobs();
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
        if(ZeeConfig.mapGobAudio.size() > 0) {
            listGobsSaved = new JList<String>(ZeeConfig.mapGobAudio.keySet().toArray(new String[0]));
        }else {
            listGobsSaved = new JList<String>();
        }
        tabbedPaneGobs.addTab("Saved("+ZeeConfig.mapGobAudio.size()+")",new JScrollPane(listGobsSaved));
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
        panelButtonCateg.add(btnResetCateg = new JButton("Reset categories"), BorderLayout.NORTH);
        btnResetCateg.addActionListener(evt->{ resetCategoriesToDefault(); });
        panelButtonCateg.add(btnAddCateg = new JButton("Add categories"), BorderLayout.NORTH);
        btnAddCateg.addActionListener(evt->{ addCategoryNew(); });
        panelTabCateg.add(new JScrollPane(listGobsCategories), BorderLayout.CENTER);
        listGobsCategories.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if(evt.getValueIsAdjusting() || tabbedPaneGobs.getSelectedIndex()!=TABGOB_CATEGS)
                    return;
                updatePanelDetails();
            }
        });

        panelGobs.add(panelGobDetails);
        pack();
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
        if(JOptionPane.showConfirmDialog(this,"Clear Gobs setitngs?") != JOptionPane.OK_OPTION)
            return;

        ZeeConfig.mapGobAudio.clear();
        Utils.setpref(ZeeConfig.MAP_GOB_SAVED,ZeeConfig.serialize(ZeeConfig.mapGobAudio));

        buildPanelGobs();
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

        panelGobDetails.removeAll();

        //gob name
        panelGobDetails.add(new JLabel("Gob name:"),BorderLayout.NORTH);
        panelGobDetails.add(tfGobName = new JTextField(list.getSelectedValue()),BorderLayout.NORTH);

        //audio file name
        panelGobDetails.add(new JLabel("Audio alert:"),BorderLayout.CENTER);
        String audioPath = ZeeConfig.mapGobAudio.get(list.getSelectedValue());
        if(audioPath==null)
            audioPath = "";
        panelGobDetails.add(tfAudioPath = new JTextField(audioPath),BorderLayout.CENTER);

        //audio buttons
        JPanel panelAudioButtons;
        panelGobDetails.add(panelAudioButtons = new JPanel(new FlowLayout()));
        panelAudioButtons.add(btnAudioSave = new JButton("Select Audio"),BorderLayout.SOUTH);
        btnAudioSave.addActionListener(evt->{ audioSave(); });
        panelAudioButtons.add(btnAudioClear = new JButton("Clear Audio"),BorderLayout.SOUTH);
        btnAudioClear.addActionListener(evt->{ audioClear(); });
        panelAudioButtons.add(btnAudioTest = new JButton("Test Audio"),BorderLayout.SOUTH);
        btnAudioTest.addActionListener(evt->{ audioTest(); });


        panelGobDetails.add(new JLabel("Category:"));
        panelGobDetails.add(cmbGobCategory = new JComboBox<String>(ZeeConfig.mapCategoryGobs.keySet().toArray(new String[0])),BorderLayout.CENTER);

        //update combo category
        String categ = getCategoryByGob(tfGobName.getText().strip());
        if(categ!=null && !categ.isEmpty()) {
            cmbGobCategory.setSelectedItem(categ);
        }else {
            cmbGobCategory.setSelectedIndex(-1);
        }

        // combo add gob to category
        cmbGobCategory.addActionListener(evt->{
            addGobToCategory();
        });

        JPanel panelButtonRemCateg = new JPanel(new FlowLayout());
        panelGobDetails.add(panelButtonRemCateg);
        panelButtonRemCateg.add(btnRemGobFromCateg = new JButton("Remove from category"),BorderLayout.SOUTH);
        btnRemGobFromCateg.addActionListener(evt->{ removeGobFromCategory(); });
        pack();
    }

    public void removeCategory(){
        if(JOptionPane.showConfirmDialog(this,"Delete category \""+listGobsCategories.getSelectedValue()+"\" ?") == JOptionPane.OK_OPTION){
            ZeeConfig.mapCategoryGobs.remove(listGobsCategories.getSelectedValue().strip());
            Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS, ZeeConfig.serialize(ZeeConfig.mapCategoryGobs));
        }
    }

    private void addCategoryNew() {
        String categName = JOptionPane.showInputDialog("Type new category name: ");
        if(categName!=null && !categName.strip().isEmpty()){
            if (ZeeConfig.mapCategoryGobs.keySet().contains(categName.strip())){
                JOptionPane.showMessageDialog(this,"Category already exists.");
                return;
            }
            ZeeConfig.mapCategoryGobs.put(categName, Collections.singleton(""));
            Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS, ZeeConfig.serialize(ZeeConfig.mapCategoryGobs));
        }
    }

    private String getCategoryByGob(String gobName) {
        HashMap<String, Set<String>> map = ZeeConfig.mapCategoryGobs;
        for (String categ: map.keySet()){
            if (map.get(categ).contains(gobName))
                return categ;
        }
        return null;
    }

    private void tabCategsSelected() {
        panelGobDetails.removeAll();

        //category name
        JLabel label;
        panelGobDetails.add(label=new JLabel("Category name:"),BorderLayout.NORTH);
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panelGobDetails.add(tfCategName = new JTextField(listGobsCategories.getSelectedValue()),BorderLayout.NORTH);

        //category audio file
        panelGobDetails.add(label=new JLabel("Audio alert:"),BorderLayout.CENTER);
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        String audioPath = ZeeConfig.mapCategoryAudio.get(listGobsCategories.getSelectedValue());
        if(audioPath==null)
            audioPath = "";
        panelGobDetails.add(tfAudioPathCateg = new JTextField(audioPath),BorderLayout.CENTER);

        //audio buttons
        JPanel panelAudioButtons;
        panelGobDetails.add(panelAudioButtons = new JPanel(new FlowLayout()));
        panelAudioButtons.add(btnAudioSave = new JButton("Select Audio"),BorderLayout.SOUTH);
        btnAudioSave.addActionListener(evt->{ audioSave(); });
        panelAudioButtons.add(btnAudioClear = new JButton("Clear Audio"),BorderLayout.SOUTH);
        btnAudioClear.addActionListener(evt->{ audioClear(); });
        panelAudioButtons.add(btnAudioTest = new JButton("Test Audio"),BorderLayout.SOUTH);
        btnAudioTest.addActionListener(evt->{ audioTest(); });
        pack();
    }

    private void resetCategoriesToDefault() {
        if(JOptionPane.showConfirmDialog(this,"Reset categories?") != JOptionPane.OK_OPTION)
            return;

        //reset map Category-Gob
        Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS,"");
        ZeeConfig.mapCategoryGobs = ZeeConfig.initMapCategoryGobs();

        //reset map Category-Audio
        Utils.setpref(ZeeConfig.MAP_CATEGORY_AUDIO,"");
        ZeeConfig.mapCategoryAudio = ZeeConfig.initMapCategoryAudio();

        buildPanelGobs();
    }

    private void removeGobFromCategory(String gobName, String gobCategory) {
        if(gobName==null || gobCategory==null || gobName.isEmpty() || gobCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this,"removeGobFromCategory(g,c) > gob or category invalid");
            return;
        }
        Set<String> gobs = ZeeConfig.mapCategoryGobs.get(gobCategory);
        if(gobs!=null){
            gobs.remove(gobName);
            ZeeConfig.mapCategoryGobs.put(gobCategory,gobs);
            Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS, ZeeConfig.serialize(ZeeConfig.mapCategoryGobs));
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

    private void addGobToCategory(String gobName, String gobCategory) {
        if(gobName==null || gobCategory==null || gobName.isEmpty() || gobCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Gob or Category invalid");
            return;
        }
        Set<String> gobs = ZeeConfig.mapCategoryGobs.get(gobCategory);
        if (gobs != null) {
            gobs.add(gobName);
            ZeeConfig.mapCategoryGobs.put(gobCategory,gobs);
            Utils.setpref(ZeeConfig.MAP_CATEGORY_GOBS, ZeeConfig.serialize(ZeeConfig.mapCategoryGobs));
        }
    }

    private void addGobToCategory() {
        try {
            if(cmbGobCategory==null || cmbGobCategory.getSelectedIndex() < 0)
                return;

            String gobName = tfGobName.getText().strip();
            String gobCategory =  cmbGobCategory.getSelectedItem().toString().strip();

            //if gob already has category, remove it
            String prevCategory = getCategoryByGob(gobName);
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
                Utils.setpref(ZeeConfig.MAP_GOB_SAVED, ZeeConfig.serialize(ZeeConfig.mapGobAudio));
            }

            panelGobDetails.removeAll();
            panelGobs.validate();
            buildPanelGobs();
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
                Utils.setpref(ZeeConfig.MAP_GOB_SAVED, ZeeConfig.serialize(ZeeConfig.mapGobAudio));
            }

            if(list!=null) {
                list.clearSelection();
                //list.updateUI();
            }
            panelGobDetails.removeAll();
            panelGobs.validate();
            buildPanelGobs();
        }
    }

}
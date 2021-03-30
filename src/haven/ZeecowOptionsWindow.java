package haven;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.HashMap;

public class ZeecowOptionsWindow extends JFrame {
    public HashMap<String, String> mapGobInfo;
    public JTabbedPane tabbedPane;
    public JPanel panelMisc, panelGobs, panelGobDetails;
    public JCheckBox cbDropMinedStone, cbDropMinedOre, cbDropMinedSilverGold, cbDropMinedCurios, cbActionSearchGlobal, cbCompactEquipsWindow, cbBeltTogglesEquips, cbAutohearth, cbAutoClickMenuOpts,cbCattleRosterHeight;
    public JButton btnRefresh;
    public JTextField tfAutoClickMenu;
    public JComboBox cmbCattleRoster;
    public JList<String> listGobs;

    public ZeecowOptionsWindow(HashMap<String, String> map){
        super.setTitle("Zeecow Haven Options");
        this.mapGobInfo = map;
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
        tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane);

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

        panelMisc.add(cbBeltTogglesEquips = new JCheckBox("Belt toggles equips window"));
        cbBeltTogglesEquips.setSelected(ZeeConfig.beltToggleEquips);
        cbBeltTogglesEquips.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.beltToggleEquips = cb.isSelected();
            Utils.setprefb("beltToggleEquips",val);
        });

        panelMisc.add(cbAutohearth = new JCheckBox("Auto-hearth on players"));
        cbAutohearth.setSelected(ZeeConfig.autoHearthOnStranger);
        cbAutohearth.addActionListener(actionEvent -> {
            JCheckBox cb = (JCheckBox) actionEvent.getSource();
            boolean val = ZeeConfig.autoHearthOnStranger = cb.isSelected();
            Utils.setprefb("autoHearthOnStranger",val);
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
            System.out.println(actionEvent.getActionCommand());
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


        //Gobs tab
        panelGobs = new JPanel();
        panelGobs.setLayout(new BoxLayout(panelGobs, BoxLayout.PAGE_AXIS));
        tabbedPane.add("Gobs", panelGobs);
        buildPanelGobs();
    }

    private void buildPanelGobs() {
        panelGobs.removeAll();
        panelGobs.add(btnRefresh = new JButton("refresh"));
        btnRefresh.addActionListener(evt -> {
            buildPanelGobs();
        });
        if(mapGobInfo.size() > 0)
            listGobs = new JList<String>(mapGobInfo.keySet().toArray(new String[0]));
        else
            listGobs = new JList<String>();
        panelGobs.add(new JScrollPane(listGobs));
        listGobs.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                gobSelected(evt.getFirstIndex());
            }
        });
        panelGobDetails = new JPanel();
        panelGobs.add(panelGobDetails);
        tabbedPane.setTitleAt(1,"Gobs("+mapGobInfo.size()+")");
    }

    private void gobSelected(int i) {
        panelGobDetails.removeAll();
        panelGobDetails.add(new JLabel("Gob: "+listGobs.getSelectedValue()));
        pack();
    }

}
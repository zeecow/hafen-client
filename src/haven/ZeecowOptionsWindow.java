package haven;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class ZeecowOptionsWindow extends JFrame {
    public JTabbedPane tabbedPane;
    public JPanel panelMisc, panelGobs, panelGobDetails;
    public JCheckBox cbDropMinedStone, cbDropMinedOre, cbDropMinedSilverGold, cbDropMinedCurios, cbActionSearchGlobal, cbCompactEquipsWindow, cbBeltTogglesEquips, cbAutohearth, cbAutoClickMenuOpts,cbCattleRosterHeight;
    public JButton btnRefresh;
    public JTextField tfAutoClickMenu;
    public JComboBox cmbCattleRoster;
    public JList<String> listGobsTemp, listGobsSaved;
    JButton btnAudioSave, btnAudioClear, btnAudioTest;
    JTextField tfGobName, tfAudioPath;

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
            String str = actionEvent.getActionCommand();
            String[] strArr = str.split(",");
            if(strArr!=null && strArr.length>0) {
                ZeeConfig.autoClickMenuOptionList = str;
                Utils.setpref("autoClickMenuOptionList",str.trim());
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

        //list gobs temp
        panelGobs.add(new JLabel("Session gobs: "+ZeeConfig.mapGobSession.size()));
        if(ZeeConfig.mapGobSession.size() > 0) {
            listGobsTemp = new JList<String>(ZeeConfig.mapGobSession.keySet().toArray(new String[0]));
        }else {
            listGobsTemp = new JList<String>();
        }
        panelGobs.add(new JScrollPane(listGobsTemp));
        listGobsTemp.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if(evt.getValueIsAdjusting())
                    return;
                gobSelected(listGobsTemp,evt.getFirstIndex());
            }
        });

        //list gobs saved
        panelGobs.add(new JLabel("Alert gobs: "+ZeeConfig.mapGobSaved.size()));
        if(ZeeConfig.mapGobSaved.size() > 0) {
            listGobsSaved = new JList<String>(ZeeConfig.mapGobSaved.keySet().toArray(new String[0]));
        }else {
            listGobsSaved = new JList<String>();
        }
        panelGobs.add(new JScrollPane(listGobsSaved));
        listGobsSaved.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if(evt.getValueIsAdjusting())
                    return;
                gobSelected(listGobsSaved,evt.getFirstIndex());
            }
        });

        //details
        panelGobDetails = new JPanel();
        panelGobDetails.setLayout(new BoxLayout(panelGobDetails,BoxLayout.PAGE_AXIS));
        panelGobs.add(panelGobDetails);
        tabbedPane.setTitleAt(1,"Gobs("+ZeeConfig.mapGobSession.size()+")");
    }

    private void gobSelected(JList<String> list, int i) {
        panelGobDetails.removeAll();
        panelGobDetails.add(new JLabel("Gob name:"),BorderLayout.NORTH);
        panelGobDetails.add(tfGobName = new JTextField(list.getSelectedValue()),BorderLayout.NORTH);
        panelGobDetails.add(new JLabel("Audio alert:"),BorderLayout.CENTER);
        panelGobDetails.add(tfAudioPath = new JTextField(ZeeConfig.mapGobSaved.get(list.getSelectedValue())),BorderLayout.CENTER);
        panelGobDetails.add(btnAudioSave = new JButton("Select Audio"),BorderLayout.CENTER);
        btnAudioSave.addActionListener(evt->{ audioSave(); });
        panelGobDetails.add(btnAudioClear = new JButton("Clear Audio"),BorderLayout.CENTER);
        btnAudioClear.addActionListener(evt->{ audioClear(); });
        panelGobDetails.add(btnAudioTest = new JButton("Test Audio"),BorderLayout.CENTER);
        btnAudioTest.addActionListener(evt->{ audioTest(); });
        pack();
    }

    private void audioTest() {
        String path = tfAudioPath.getText().trim();
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
            ZeeConfig.mapGobSaved.remove(listGobsSaved.getSelectedValue());
            Utils.setpref("mapGobSavedString",
                ZeeConfig.mapGobSaved.toString()
                    .replace("{","")
                    .replace("}","")
                    .trim()
            );
            buildPanelGobs();
        }
    }

    private void audioSave() {
        JFileChooser fileChooser;
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(null);
        fileChooser.setDialogTitle("select audio file");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Audio files","mp3","wav","ogg","mid","midi"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            tfAudioPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            ZeeConfig.mapGobSaved.put(
                tfGobName.getText().trim(),
                fileChooser.getSelectedFile().getAbsolutePath()
            );
            Utils.setpref("mapGobSavedString",
                ZeeConfig.mapGobSaved.toString()
                    .replace("{","")
                    .replace("}","")
                    .replace(", ",",")
                    .replace(" ,",",")
                    .trim()
            );
            buildPanelGobs();
        }
    }

}
package haven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZeeQuickOptionsWindow {

    private static ZeeWindow window;
    private static Scrollport scrollport;
    private static List<String[]> listConfigLabel = new ArrayList<>();
    private static List<CheckBox> listJOptsWidgets = new ArrayList<>();
    static CheckBox cbPetal;
    static String autoPetalName = "";


    static ZeeWindow getWindow() {
        if (window == null){
            window = new ZeeWindow(Coord.of(170,80),"Quick options");
            scrollport = window.add(new Scrollport(Coord.of(155,65)),0,21);
            ZeeConfig.gameUI.add(window,Coord.of(ZeeConfig.gameUI.sz.x/2,0));
        }
        else{
            window.show();
        }
        return window;
    }

    static Scrollport getScrollport(){
        getWindow();
        return scrollport;
    }


    public static void updateJCheckBoxWidget(String configName, String cbLabel) {

        // avoid crash
        if (ZeeConfig.gameUI==null)
            return;

        //add new config
        if (getConfigByLabel(cbLabel).isBlank()) { //avoid duplicate
            if (listConfigLabel.size() == 10) //max checkboxes
                listConfigLabel.remove(0);
            listConfigLabel.add(0,new String[]{configName, cbLabel});
        }

        //save pref to be loaded at initWindow()
        savePref();

        //remove all widgets
        for (int i = 0; i < listJOptsWidgets.size(); i++) {
            //ZeeConfig.println("remove "+i);
            listJOptsWidgets.get(i).remove();
        }
        listJOptsWidgets.clear();

        //add new widgets
        for (int i = 0; i < listConfigLabel.size(); i++) {
            CheckBox newcb = null;
            try{
                newcb = new CheckBox(listConfigLabel.get(i)[1]) {
                    {
                        //ZeeConfig.println(" > configName: "+getConfigByLabel(lbl.text));
                        a = ZeeOptionJCheckBox.getZeeConfigBoolean(getConfigByLabel(lbl.text));
                    }

                    public void set(boolean val) {
                        a = val;
                        //ZeeConfig.println(" set > configName: "+getConfigByLabel(lbl.text));
                        String configName = getConfigByLabel(lbl.text);
                        ZeeOptionJCheckBox.setZeeConfigBoolean(val, configName);
                        bumpCheckBox(configName);
                    }
                };
                newcb.settip("hold shift = no bump");
            }catch (Exception e){
                ZeeConfig.println("config \""+listConfigLabel.get(i)[1]+"\" not found? ");
            }

            // checkbox couldn't be created, skip to next
            if (newcb==null)
                continue;

            // checkbox successfully created
            listJOptsWidgets.add(newcb);

            //add cb to scrollport, bottom to top
            getScrollport().cont.add( newcb, 0, ((listConfigLabel.size()-1)-i) * 15);
        }

        window.pack();
        //repositionWidgets();
    }

    private static void bumpCheckBox(String configName) {

        // no bump if shift pressed
        if (ZeeConfig.gameUI.ui.modshift)
            return;

        // move checkbox up in the list, so it stays longer
        for (int j = 0; j < listConfigLabel.size(); j++) {
            if (listConfigLabel.get(j)[0].contentEquals(configName) && j<listConfigLabel.size()-1){
                //ZeeConfig.println("bump "+j+"  "+(listConfigLabel.get(j)[1]));
                Collections.swap(listConfigLabel,j,j+1);
                updateJCheckBoxWidget(listConfigLabel.get(j)[0],listConfigLabel.get(j)[1]);
                break;
            }
        }
    }


    private static void savePref() {
        Utils.setpref("quickOptConfigLabel",ZeeConfig.serialize((Serializable) listConfigLabel));
    }

    @SuppressWarnings("unchecked")
    private static void loadPref(){
        String s = Utils.getpref("quickOptConfigLabel","");
        if (!s.isBlank()) {
            listConfigLabel = (List<String[]>) ZeeConfig.deserialize(s);
        }
    }


    public static void initWindow() {
        reset();
        loadPref();
        if (listConfigLabel.size() > 0) {
            updateJCheckBoxWidget(listConfigLabel.get(0)[0], listConfigLabel.get(0)[1]);
        }
    }

    static String getConfigByLabel(String lbl) {
        for (int i = 0; i < listConfigLabel.size(); i++) {
            if (listConfigLabel.get(i)[1].contentEquals(lbl))
                return listConfigLabel.get(i)[0];
        }
        return "";
    }

    static String getLabelByConfig(String config) {
        for (int i = 0; i < listConfigLabel.size(); i++) {
            if (listConfigLabel.get(i)[0].contentEquals(config))
                return listConfigLabel.get(i)[1];
        }
        return "";
    }

    public static void updatePetalWidget(String name) {
        // avoid recursive loop?
        if (name.contentEquals(autoPetalName)) {
            return;
        }
        // clicked new petal name?
        autoPetalName = "";
        if (cbPetal !=null) {
            cbPetal.remove();
        }
        cbPetal = new CheckBox("auto-click \""+name+"\""){
            public void set(boolean val) {
                a = val;
                if (val)
                    autoPetalName = name;
                else
                    autoPetalName = "";
            }
        };
        getWindow().add(cbPetal,0,2);
        //repositionWidgets();
    }

    private static void repositionWidgets() {
        int y = 0;
        for (int i = listJOptsWidgets.size()-1; i >= 0 ; i--) {
            listJOptsWidgets.get(i).c = Coord.of(0,y);
            y += 15;
        }
    }

    public static void reset() {
        if (window!=null)
            window.remove();
        window = null;
        cbPetal = null;
        listJOptsWidgets.clear();
    }

    static void updateCheckboxNoBump(String configName, boolean val) {
        CheckBox checkBox;
        String labelText;
        for (int i = 0; i < listJOptsWidgets.size(); i++) {
            checkBox = listJOptsWidgets.get(i);
            labelText = checkBox.lbl.text;
            if (getConfigByLabel(labelText).contentEquals(configName)){
                checkBox.a = val;
                return;
            }
        }
    }
}

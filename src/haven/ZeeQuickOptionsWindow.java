package haven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ZeeQuickOptionsWindow {

    private static ZeeWindow window;
    private static List<String[]> listConfigLabel = new ArrayList<>();
    private static List<CheckBox> listJOptsWidgets = new ArrayList<>();
    static CheckBox cbPetal;
    static String autoPetalName = "";

    public static ZeeWindow getWindow() {
        if (window == null){
            window = new ZeeWindow(Coord.of(155,60),"Quick options");
            ZeeConfig.gameUI.add(window,Coord.of(ZeeConfig.gameUI.sz.x/2,0));
        }
        else{
            window.show();
        }
        return window;
    }

    public static void updateJCheckBoxWidget(String configName, String cbLabel) {

        // avoid crash
        if (ZeeConfig.gameUI==null)
            return;

        //add new config
        if (getConfigByLabel(cbLabel).isBlank()) { //avoid duplicate
            if (listConfigLabel.size() == 3) //max 3
                listConfigLabel.remove(0);
            listConfigLabel.add(new String[]{configName, cbLabel});
        }

        //TODO save pref to be loaded at initWindow()
        savePref();


        //remove all widgets
        for (int i = 0; i < listJOptsWidgets.size(); i++) {
            //ZeeConfig.println("remove "+i);
            listJOptsWidgets.get(i).remove();
        }
        listJOptsWidgets.clear();

        //add new widgets
        for (int i = 0; i < listConfigLabel.size(); i++) {
            listJOptsWidgets.add(new CheckBox(listConfigLabel.get(i)[1]) {
                {
                    try {
                        //ZeeConfig.println(" > configName: "+getConfigByLabel(lbl.text));
                        a = ZeeOptionJCheckBox.getZeeConfigBoolean(getConfigByLabel(lbl.text));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void set(boolean val) {
                    try {
                        //ZeeConfig.println(" set > configName: "+getConfigByLabel(lbl.text));
                        ZeeOptionJCheckBox.setZeeConfigBoolean(val, getConfigByLabel(lbl.text));
                        a = val;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            getWindow().add(listJOptsWidgets.get(listJOptsWidgets.size() - 1));
        }

        repositionWidgets();
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
        loadPref();
        //ZeeConfig.println("saved prefs = "+listConfigLabel.size());
        for (int i = 0; i < listConfigLabel.size(); i++) {
            updateJCheckBoxWidget(listConfigLabel.get(i)[0],listConfigLabel.get(i)[1]);
        }
        //TODO: add from saved listConfigLabel pref
    }

    private static String getConfigByLabel(String lbl) {
        for (int i = 0; i < listConfigLabel.size(); i++) {
            if (listConfigLabel.get(i)[1].contentEquals(lbl))
                return listConfigLabel.get(i)[0];
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
        getWindow().add(cbPetal);
        repositionWidgets();
    }

    private static void repositionWidgets() {
        int y = 0;
        if (cbPetal!=null) {
            cbPetal.c = Coord.of(0,y);
            y += 15;
        }
        for (int i = 0; i < listJOptsWidgets.size(); i++) {
            listJOptsWidgets.get(i).c = Coord.of(0,y);
            y += 15;
        }
    }

    public static void reset() {
        window = null;
        cbPetal = null;
        listJOptsWidgets.clear();
    }
}

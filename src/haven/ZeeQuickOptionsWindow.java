package haven;

public class ZeeQuickOptionsWindow {

    private static ZeeWindow window;
    private static CheckBox cbJOption;

    public static ZeeWindow getWindow() {
        if (window == null){
            window = new ZeeWindow(Coord.of(155,30),"Quick options");
            ZeeConfig.gameUI.add(window,Coord.of(ZeeConfig.gameUI.sz.x/2,0));
        }
        else{
            window.show();
        }
        return window;
    }

    public static void updateCheckBoxWidget(ZeeOptionJCheckBox zeeOptionJCheckBox) {
        if (ZeeConfig.gameUI==null)
            return;
        if (cbJOption != null)
            cbJOption.remove();
        cbJOption = new CheckBox(zeeOptionJCheckBox.label){
            {
                try {
                    a = zeeOptionJCheckBox.getZeeConfigBoolean();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            public void set(boolean val) {
                try {
                    zeeOptionJCheckBox.setZeeConfigBoolean(val);
                    a = val;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        getWindow().add(cbJOption);
        organizeItems();
    }

    public static void initWindow() {
        getWindow();
    }

    static CheckBox cbPetal;
    static String autoPetalName = "";
    public static void clickedFlowerMenuPetal(String name) {
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
        organizeItems();
    }

    private static void organizeItems() {
        int y = 0;
        if (cbJOption!=null){
            cbJOption.c = Coord.of(0,y);
            y+=20;
        }
        if (cbPetal!=null) {
            cbPetal.c = Coord.of(0,y);
            y+=20;
        }
    }

    public static void reset() {
        window = null;
        cbPetal = null;
        cbJOption = null;
    }
}

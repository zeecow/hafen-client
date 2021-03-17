package haven;

import haven.CheckBox;
import haven.Widget;

public class ZeeInventoryOptions extends Widget {

    public ZeeInventoryOptions(String windowCap) {

        if(windowCap.contains("Inventory"))
            invMain();
        pack();
    }

    private void invMain() {
        add(new CheckBox("Drop seeds when full") {
            {
                a = ZeeConfig.dropSeeds;
            }

            public void set(boolean val) {
                Utils.setprefb("dropSeeds", val);
                ZeeConfig.dropSeeds = val;
                a = val;
            }
        }, 0, 0);
    }

}

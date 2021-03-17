package haven;

import haven.CheckBox;
import haven.Widget;

public class ZeeInventoryOptions extends Widget {

    public ZeeInventoryOptions(String windowCap) {

        if(windowCap.contains("Inventory"))
            invMain();
        else if(windowCap.contains("Equipment"))
            invEquip();
        pack();
    }

    private void invEquip() {
        add(new CheckBox("compact") {
            {
                a = ZeeConfig.equiporyCompact;
            }

            public void set(boolean val) {
                Utils.setprefb("equiporyCompact", val);
                ZeeConfig.equiporyCompact = val;
                a = val;
            }
        }, 0, 0);
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

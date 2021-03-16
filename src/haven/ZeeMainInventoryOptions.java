package haven;

import haven.CheckBox;
import haven.Widget;

public class ZeeMainInventoryOptions extends Widget {

    public ZeeMainInventoryOptions() {

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

        pack();
    }

}

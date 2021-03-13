package haven;

import haven.CheckBox;
import haven.Widget;

public class ZeeMainInventoryOptions extends Widget {

    public ZeeMainInventoryOptions() {

        add(new Label("Drop:"));

        add(new CheckBox("seeds") {
            {
                a = ZeeConfig.dropSeeds;
            }

            public void set(boolean val) {
                Utils.setprefb("dropSeeds", val);
                ZeeConfig.dropSeeds = val;
                a = val;
            }
        }, 35, 0);

        pack();
    }

}

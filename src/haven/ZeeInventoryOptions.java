package haven;

public class ZeeInventoryOptions extends Widget {

    public ZeeInventoryOptions(String windowCap) {

        if(windowCap.trim().equalsIgnoreCase("Inventory"))
            invMain();
        pack();
    }

    private void invMain() {

        add(new Label("Drop:"), 0, 0);

        add(new CheckBox("seeds") {
            {
                a = ZeeConfig.dropSeeds;
            }

            public void set(boolean val) {
                Utils.setprefb("dropSeeds", val);
                ZeeConfig.dropSeeds = val;
                a = val;
            }
        }, 33, 0);

        add(new CheckBox("soil") {
            {
                a = ZeeConfig.dropSoil;
            }

            public void set(boolean val) {
                Utils.setprefb("dropSoil", val);
                ZeeConfig.dropSoil = val;
                a = val;
            }
        }, 86, 0);
    }
}

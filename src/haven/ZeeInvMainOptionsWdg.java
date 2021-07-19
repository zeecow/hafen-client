package haven;

public class ZeeInvMainOptionsWdg extends Widget {

    Label labelDrop, labelCount;
    CheckBox cbSeeds, cbSoil, cbButcher;
    Widget invSlots;

    public ZeeInvMainOptionsWdg(String windowCap) {

        if(windowCap.trim().equalsIgnoreCase("Inventory"))
            invMain();
        invSlots = ZeeConfig.windowInvMain.getchild(Inventory.class);
    }

    private void invMain() {

        int x = 0;

        add(labelDrop = new Label("Drop:"), x, 0);

        x += labelDrop.sz.x + 5;

        add(cbSeeds = new CheckBox("seeds") {
            {
                a = ZeeConfig.dropSeeds;
            }

            public void set(boolean val) {
                Utils.setprefb("dropSeeds", val);
                ZeeConfig.dropSeeds = val;
                a = val;
            }
        }, x, 0);

        x += cbSeeds.sz.x + 5;

        add(cbSoil = new CheckBox("soil") {
            {
                a = ZeeConfig.dropSoil;
            }

            public void set(boolean val) {
                Utils.setprefb("dropSoil", val);
                ZeeConfig.dropSoil = val;
                a = val;
            }
        }, x, 0);

        x = 0;

        add(cbButcher = new CheckBox("butchmode") {
            {
                a = ZeeConfig.butcherAuto;
            }

            public void set(boolean val) {
                Utils.setprefb("butcherAuto", val);
                ZeeConfig.butcherAuto = val;
                a = val;
            }
        }, x, 15);

        add(labelCount = new Label(""), 0, 0);
        pack();
    }

    public void updateLabelCount(String itemName, Integer count) {

        //update counter text
        labelCount.settext(itemName + "(" + count +")");

        repositionLabelCount();
    }

    public void repositionLabelCount() {
        //position counter at top right
        int x = invSlots.sz.x - labelCount.sz.x;
        int y = invSlots.sz.y + 20;
        labelCount.c = new Coord(x, y);
        pack();
    }
}

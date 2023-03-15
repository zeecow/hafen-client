package haven;

public class ZeeInvMainOptionsWdg extends Widget {

    Label labelCount;
    public static CheckBox cbSeeds, cbSoil, cbButcher, cbAutoStack, cbPiler, cbAutomenu, cbTunnel, cbDrink;
    Widget invSlots;

    public ZeeInvMainOptionsWdg(String windowCap) {

        if(windowCap.trim().equalsIgnoreCase("Inventory"))
            invMain();
        invSlots = ZeeConfig.windowInvMain.getchild(Inventory.class);
    }

    private void invMain() {

        int x = 0;

        add(cbAutomenu = new CheckBox("mn") {
            {
                a = ZeeConfig.autoClickMenuOption;
            }

            public void set(boolean val) {
                Utils.setprefb("autoClickMenuOption", val);
                ZeeConfig.autoClickMenuOption = val;
                a = val;
            }
        }, x, 0);
        cbAutomenu.settip("auto menu list");

        x += cbAutomenu.sz.x + 5;

        add(cbButcher = new CheckBox("bt") {
            {
                a = ZeeConfig.butcherMode;
            }

            public void set(boolean val) {
                ZeeConfig.butcherMode = val;
                a = val;
            }
        }, x, 0);
        cbButcher.settip("auto menu butch list");

        x += cbButcher.sz.x + 5;

        add(cbAutoStack = new CheckBox("stk") {
            {
                a = ZeeConfig.autoStack;
            }

            public void set(boolean val) {
                ZeeConfig.autoStack = val;
                a = val;
            }
        }, x, 0);
        cbAutoStack.settip("auto stack items");

        x = 0;

        add(cbSeeds = new CheckBox("sd") {
            {
                a = ZeeConfig.dropSeeds;
            }

            public void set(boolean val) {
                ZeeConfig.dropSeeds = val;
                a = val;
            }
        }, x, 15);
        cbSeeds.settip("drop seeds");

        x += cbSeeds.sz.x + 5;

        add(cbSoil = new CheckBox("sl") {
            {
                a = ZeeConfig.dropSoil;
            }

            public void set(boolean val) {
                ZeeConfig.dropSoil = val;
                a = val;
            }
        }, x, 15);
        cbSoil.settip("drop soil");

        x += cbSoil.sz.x + 5;

        add(cbTunnel = new CheckBox("tn") {
            {
                a = ZeeManagerMiner.tunnelCheckbox;
            }

            public void set(boolean val) {
                ZeeManagerMiner.tunnelCheckbox = val;
                a = val;
            }
        }, x, 15);
        cbTunnel.settip("tunnel helper");

        x += cbTunnel.sz.x + 5;

        add(cbPiler = new CheckBox("pl") {
            {
                a = ZeeConfig.pilerMode;
            }

            public void set(boolean val) {
                ZeeConfig.pilerMode = val;
                a = val;
                if (val)
                    ZeeConfig.msg("piler note: select petal by mouseclick");
            }
        }, x, 15);
        cbPiler.settip("pile helper (mouse-click menu required)");

        add(labelCount = new Label(""), 0, 0);
        pack();
    }

    public void updateLabelCount(String itemName, Integer count) {
        //update counter text
        labelCount.settext( itemName.replaceAll(".+/","")+ "(" + count +")");
        repositionLabelCount();
    }

    void addWindowTransferOptions() {

        Inventory inv = ZeeConfig.getMainInventory();
        if (inv == null) {
            ZeeConfig.println("addWindowTransferOptions > inv null");
            return;
        }

        Window window = ZeeConfig.getWindow("Inventory");
        if (window == null) {
            ZeeConfig.println("addWindowTransferOptions > window null");
            return;
        }

        RadioGroup rg = new RadioGroup(window){
            public void changed(int btn, String lbl) {
                ZeeConfig.windowShortMidclickTransferMode = lbl;
            }
        };
        int padx = 5;
        Widget wdg = rg.add("des", new Coord(0, inv.c.y + inv.sz.y + 2));
        wdg.settip("midclick transfer descending order");
        wdg = rg.add("asc", new Coord(wdg.c.x+wdg.sz.x+padx, wdg.c.y));
        wdg.settip("midclick transfer ascending order");
        wdg = rg.add("one", new Coord(wdg.c.x+wdg.sz.x+padx, wdg.c.y));
        wdg.settip("midclick transfer one");
        wdg = rg.add("ql", new Coord(wdg.c.x+wdg.sz.x+padx, wdg.c.y));
        wdg.settip("midclick transfer by quality");
        rg.check(ZeeConfig.windowShortMidclickTransferMode);//default des
        //window.resize(window.contentsz().addy(-2));
        pack();
    }

    private void repositionTransferOptions() {
        invSlots.getparent(Window.class).children(RadioGroup.RadioButton.class).forEach(radioButton -> {
            radioButton.c.y = invSlots.c.y + invSlots.sz.y + 2;
        });
        pack();
    }

    public void reposition() {
        repositionLabelCount();
        repositionTransferOptions();
        this.parent.pack();
    }

    public void repositionLabelCount() {
        //position counter at bottom right
        int x = invSlots.sz.x - labelCount.sz.x;
        int y = invSlots.sz.y + 20;
        labelCount.c = new Coord(x, y);
        pack();
    }
}

package haven;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ZeeFishing {

    final static String POSE_FISH_IDLE = "gfx/borka/fishidle";
    final static String POSE_FISH_REELING1 = "gfx/borka/napp1";

    public static boolean isFishingItem(String itemName) {
        String[] items = {"fline-","hook-","lure-","chitinhook"};
        for (int i = 0; i < items.length; i++) {
            if (itemName.contains(items[i])){
                return true;
            }
        }
        return false;
    }

    static void switchFishingEquips(WItem wItem, String itemName) {

        boolean isFishPose = ZeeConfig.playerHasAnyPose(POSE_FISH_IDLE,POSE_FISH_REELING1);

        ZeeConfig.addPlayerText("switch");

        try {

            Inventory invCreelOrMain = wItem.getparent(Inventory.class);

            // equip lure on primrod
            if (itemName.contains("lure-")){
                if(ZeeManagerItemClick.getLeftHandName().contains("/primrod") || ZeeManagerItemClick.getRightHandName().contains("/primrod")){
                    if(ZeeManagerItemClick.pickUpItem(wItem)){
                        ZeeManagerItemClick.equiporyItemAct("/primrod");//equip holding item
                        ZeeManagerItemClick.playFeedbackSound();
                        Thread.sleep(500);
                        invCreelOrMain.wdgmsg("drop", wItem.c.div(33));//return switched item
                        ZeeManagerItemClick.playFeedbackSound();
                    }
                } else {
                    ZeeConfig.gameUI.error("no fish rod equipped");
                    ZeeConfig.removePlayerText();
                    return;
                }
            }
            //equip hook or line
            else {
                String rodName = "";
                if(ZeeManagerItemClick.getLeftHandName().contains("/primrod") || ZeeManagerItemClick.getRightHandName().contains("/primrod")) {
                    rodName = "/primrod";
                } else if(ZeeManagerItemClick.getLeftHandName().contains("/bushpole") || ZeeManagerItemClick.getRightHandName().contains("/bushpole")){
                    rodName = "/bushpole";
                } else {
                    ZeeConfig.gameUI.error("no fish pole equipped");
                    ZeeConfig.removePlayerText();
                    return;
                }
                if(ZeeManagerItemClick.pickUpItem(wItem)){
                    ZeeManagerItemClick.equiporyItemAct(rodName);//equip holding item
                    ZeeManagerItemClick.playFeedbackSound();
                    Thread.sleep(500);
                    invCreelOrMain.wdgmsg("drop", wItem.c.div(33));//return switched item
                    ZeeManagerItemClick.playFeedbackSound();
                }
            }

            // click fishing spot again
            if (isFishPose) {
                Thread.sleep(500);
                ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.lastMapViewClickArgs);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ZeeConfig.removePlayerText();
    }


    public static void buildWindow() {

        String winName = "Fishing helper";

        Window win = ZeeConfig.getWindow(winName);

        if (win!=null) {
            win.reqdestroy();
            win = null;
        }

        win = ZeeConfig.gameUI.add(
                new Window(Coord.of(150,60), winName){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("close")){
                            this.reqdestroy();
                        }
                    }
                }
        ,ZeeConfig.gameUI.sz.div(2));

        Widget wdg = null;

        List<String> fishItems = getFishItemsAvailable();

        int y=0;
        int letterW = 10;

        //lure buttons
        wdg = win.add(new Label("Lures: "),0,y);
        for (String fitem : fishItems) {
            if (fitem.startsWith("lure-")) {
                String finalFitem = fitem;
                fitem = fitem.replaceAll("lure-","");
                wdg = win.add(new Button((int) (fitem.length()*letterW*.9), fitem) {
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("activate")) {
                            buttonChangeItem(finalFitem);
                        }
                    }
                }, ZeeWindow.posRight(wdg, 2, y));
            }
        }

        y += wdg.sz.y + 3;

        //line buttons
        wdg = win.add(new Label("Lines: "),0,y);
        for (String fitem : fishItems) {
            if (fitem.startsWith("fline-")) {
                String finalFitem = fitem;
                fitem = fitem.replaceAll("fline-","");
                wdg = win.add(new Button((int) (fitem.length()*letterW*.9), fitem) {
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("activate")) {
                            buttonChangeItem(finalFitem);
                        }
                    }
                }, ZeeWindow.posRight(wdg, 2, y));
            }
        }

        y += wdg.sz.y + 3;

        //hooks
        wdg = win.add(new Label("Hooks: "),0,y);
        for (String fitem : fishItems) {
            if (fitem.startsWith("hook-") || fitem.startsWith("chitinhook")) {
                String finalFitem = fitem;
                fitem = fitem.replaceAll("hook-","");
                wdg = win.add(new Button((int) (fitem.length()*letterW*.9), fitem) {
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("activate")) {
                            buttonChangeItem(finalFitem);
                        }
                    }
                }, ZeeWindow.posRight(wdg, 2, y));
            }
        }

        win.pack();
    }

    private static void buttonChangeItem(String fishItemName) {
        new ZeeThread(){
            public void run() {
                try {
                    WItem wItem = null;
                    List<WItem> list;
                    Inventory invCreel = ZeeConfig.getWindowsInventory("Creel");
                    if (invCreel!=null){
                        list = invCreel.getItemsByNameEnd(fishItemName);
                        if (!list.isEmpty())
                            wItem = list.get(0);
                    }
                    if (wItem==null){
                        list = ZeeConfig.getMainInventory().getItemsByNameEnd(fishItemName);
                        if (!list.isEmpty())
                            wItem = list.get(0);
                    }
                    if (wItem!=null){
                        switchFishingEquips(wItem,wItem.item.getres().name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    static List<String> getFishItemsAvailable() {

        List<WItem> ret = ZeeConfig.getMainInventory().getWItemsByNameContains("fline-", "hook-", "lure-", "chitinhook");

        Window winCreel = ZeeConfig.getWindow("Creel");
        if (winCreel!=null){
            Inventory inv = winCreel.getchild(Inventory.class);
            if (inv!=null) {
                ret.addAll(inv.getWItemsByNameContains("fline-", "hook-", "lure-", "chitinhook"));
            }
        }

        if (ret.isEmpty())
            return new ArrayList<>();

        return ret.stream().map(wItem -> wItem.item.getres().basename()).distinct().collect(Collectors.toList());
    }

    static void println(String msg){
        ZeeConfig.println(msg);
    }
}

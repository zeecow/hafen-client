package haven;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ZeeFishing {

    final static String POSE_FISH_IDLE = "gfx/borka/fishidle";
    final static String POSE_FISH_REELING1 = "gfx/borka/napp1";

    static boolean autoFish = false;
    static int autoFishAbove = 70;

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


    static TextEntry textEntryAutoFish;
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
                            buttonChangeItem(finalFitem,this);
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
                            buttonChangeItem(finalFitem,this);
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
                            buttonChangeItem(finalFitem,this);
                        }
                    }
                }, ZeeWindow.posRight(wdg, 2, y));
            }
        }

        y += wdg.sz.y + 3;

        // auto fish checkbox
        wdg = win.add(new CheckBox("Auto fish above %"){
            { a = autoFish; }
            public void changed(boolean val) {
                autoFish = val;
                if (textEntryAutoFish!=null)
                    textEntryAutoFish.setcanfocus(!autoFish);
            }
        }, 0 , wdg.c.y+wdg.sz.y+2);
        //autofish textentry
        wdg = win.add(textEntryAutoFish = new TextEntry(UI.scale(50),""+autoFishAbove){
            public boolean keydown(KeyEvent e) {
                if(!Character.isDigit(e.getKeyChar()) && !ZeeConfig.isControlKey(e.getKeyCode()))
                    return false;
                return super.keydown(e);
            }
            public void changed(ReadLine buf) {
                if (buf.empty()) {
                    super.changed(buf);
                    println("empty");
                    return;
                }
                try {
                    int num = Integer.parseInt(buf.line());
                    if (num < 0)
                        this.settext("0");
                    else if (num > 100)
                        this.settext("100");
                    else {
                        autoFishAbove = num;
                        println("autofish set to "+num);
                        buf.setline(""+num);
                        super.changed(buf);
                    }
                }catch (Exception e){
                    println("changed exception > "+e.getMessage());
                }
            }
        },ZeeWindow.posRight(wdg, 2, y));

        win.pack();
    }

    static Button btnLure, btnLine, btnHook;
    private static void buttonChangeItem(String fishItemName, Button btn) {

        // disable buttons to indicate used items
        if (fishItemName.startsWith("lure-")) {
            if (btnLure!=null)
                btnLure.disable(false);
            btnLure = btn;
            btnLure.disable(true);
        }
        else if (fishItemName.startsWith("fline-")) {
            if (btnLine!=null)
                btnLine.disable(false);
            btnLine = btn;
            btnLine.disable(true);
        }
        else if (fishItemName.startsWith("hook-") || fishItemName.startsWith("chitinhook")) {
            if (btnHook!=null)
                btnHook.disable(false);
            btnHook = btn;
            btnHook.disable(true);
        }

        // switch fish item
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

    public static void checkFishWindow(Window win) {
        if (autoFish) {
            int count = 0;
            Button btn = null;
            Label lbl;
            for (Widget child : win.children()) {
                if (child instanceof Button) {
                    count = 0;
                    btn = (Button) child;
                } else if (child instanceof Label) {
                    count++;
                    if (count == 6) {
                        lbl = (Label) child;
                        int num = Integer.parseInt(lbl.texts.replaceAll("[^0-9]", ""));
                        if (num >= autoFishAbove) {
                            println("clicking > "+lbl.texts);
                            if (btn!=null)
                                btn.click();
                            else
                                println("btn null");
                            return;
                        }
                    }
                }
            }
        }
    }
}

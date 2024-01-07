package haven;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ZeeFishing {

    final static String POSE_FISH_IDLE = "gfx/borka/fishidle";
    final static String POSE_FISH_REELING1 = "gfx/borka/napp1";

    static boolean autoFish = false, avoidInvoluntaryFishing = true;
    static int autoFishAbove = 70;
    static String fishNameAlert = "";

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

        //cancel recastThread()
        ZeeConfig.lastMapViewClickButton = 1;

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


    static TextEntry textEntryAutoFish, textEntryFishAlert;
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
                //highlight prev selected buttons
                Button newBtn = (Button) wdg;
                if (prevLure.contentEquals(newBtn.text.text)) {
                    btnLure = newBtn;
                    highlightButton(newBtn);
                }
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
                //highlight prev selected buttons
                Button newBtn = (Button) wdg;
                if (prevLine.contentEquals(newBtn.text.text)) {
                    btnLine = newBtn;
                    highlightButton(newBtn);
                }
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
                //highlight prev selected buttons
                Button newBtn = (Button) wdg;
                if (prevHook.contentEquals(newBtn.text.text)) {
                    btnHook = newBtn;
                    highlightButton(newBtn);
                }
            }
        }

        y += wdg.sz.y + 3;


        // alert fish name
        wdg = win.add(new Label("Alert on fish: "),0,y);
        wdg = win.add(textEntryFishAlert= new TextEntry(UI.scale(100),""+ fishNameAlert){
            public void activate(String text) {
                fishNameAlert = text.strip();
            }
        },ZeeWindow.posRight(wdg,2,y));;
        textEntryFishAlert.setcanfocus(false);
        textEntryFishAlert.setfocustab(false);


        // auto fish checkbox
        wdg = win.add(new CheckBox("Auto fish above %"){
            { a = autoFish; }
            public void changed(boolean val) {
                autoFish = val;
                if (textEntryAutoFish!=null)
                    textEntryAutoFish.setcanfocus(!autoFish);
            }
        }, ZeeWindow.posRight(wdg,7,y));
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
        textEntryAutoFish.setcanfocus(false);
        textEntryAutoFish.setfocustab(false);


        y += wdg.sz.y + 3;


        // alert fish name
        wdg = win.add(new CheckBox("Avoid involuntary fishing by recasting"){
            {a = avoidInvoluntaryFishing;}
            public void changed(boolean val) {
                //super.changed(val);
                avoidInvoluntaryFishing = val;
            }
        },0,y);

        win.pack();

    }

    private static void highlightButton(Button btn) {
        btn.change(btn.text.text, Color.blue);
    }

    static Button btnLure, btnLine, btnHook;
    static String prevLure="", prevLine="", prevHook="";
    private static void buttonChangeItem(String fishItemName, Button btn) {

        // disable buttons to indicate used items
        if (fishItemName.startsWith("lure-")) {
            if (btnLure!=null)
                restoreButton(btnLure);
            btnLure = btn;
            highlightButton(btnLure);
            prevLure = btnLure.text.text;
        }
        else if (fishItemName.startsWith("fline-")) {
            if (btnLine!=null)
                restoreButton(btnLine);
            btnLine = btn;
            highlightButton(btnLine);
            prevLine = btnLine.text.text;
        }
        else if (fishItemName.startsWith("hook-") || fishItemName.startsWith("chitinhook")) {
            if (btnHook!=null)
                restoreButton(btnHook);
            btnHook = btn;
            highlightButton(btnHook);
            prevHook = btnHook.text.text;
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

    private static void restoreButton(Button btn) {
        btn.change(btn.text.text);
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

        if (avoidInvoluntaryFishing){
            recastThread();
        }

        List<Widget> widgets = win.children();

        // fish name alert
        if (!fishNameAlert.isBlank()){
            for (Widget widget : widgets) {
                if (widget instanceof Label) {
                    Label lbl = (Label) widget;
                    if (lbl.texts.endsWith(":")) {
                        if (lbl.texts.toLowerCase().contains(fishNameAlert.toLowerCase())) {
                            lbl.setcolor(Color.green);
                            ZeeSynth.textToSpeakLinuxFestival("fish found");
                            break;
                        }
                    }
                }
            }
        }

        // autofish above %
        if(autoFish) {
            int lblCol = 0;
            Button btn = null;
            Label lbl;
            for (Widget widget : widgets) {
                if (widget instanceof Button) {
                    lblCol = 0;
                    btn = (Button) widget;
                } else if (widget instanceof Label) {
                    lbl = (Label) widget;
                    lblCol++;
                    //println(lblCol+" = "+lbl.texts);
                    if (lblCol == 6) {
                        int num = Integer.parseInt(lbl.texts.replaceAll("[^0-9]", ""));
                        if (num >= autoFishAbove) {
                            println("autofish > " + lbl.texts);
                            if (btn != null)
                                btn.click();
                            else
                                println("btn null");
                            break;
                        }
                    }
                }
            }
        }

    }

    static boolean recastingOn = false;
    private static void recastThread() {
        if (recastingOn){
            println("recasting thread already running");
            return;
        }
        recastingOn = true;
        new ZeeThread(){
            public void run() {
                try {
                    long timeout = 4;
                    prepareCancelClick();
                    do {
                        ZeeConfig.addPlayerText("recasting " + timeout);
                        sleep(1000);
                        timeout--;
                    }while(timeout > 0 && !isCancelClick());
                    if (!isCancelClick())
                        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.lastMapViewClickArgs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                recastingOn = false;
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    static void exit(){
        prevLure = ""; prevLine = ""; prevHook = "";
        btnLure = btnLine = btnHook = null;
    }
}

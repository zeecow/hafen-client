package haven;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ZeeFishing {

    final static String POSE_FISH_IDLE = "gfx/borka/fishidle";
    final static String POSE_FISH_REELING1 = "gfx/borka/napp1";

    static boolean
            autoFish = false,
            avoidInvoluntaryFishing = true,
            reorderFishList = false;
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
            public void changed(ReadLine buf) {
                super.changed(buf);
                if (buf.empty())
                    fishNameAlert = "";
                else
                    fishNameAlert = buf.line();
            }
        },ZeeWindow.posRight(wdg,2,y));
        textEntryFishAlert.setcanfocus(false);
        textEntryFishAlert.setfocustab(false);


        // auto fish checkbox
        wdg = win.add(new CheckBox("Auto fish above %"){
            { a = autoFish; }
            public void changed(boolean val) {
                autoFish = val;
                if (textEntryAutoFish!=null) {
                    textEntryAutoFish.show(autoFish);
                    textEntryAutoFish.setcanfocus(!autoFish);
                }
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
                    //println("empty");
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
                        //println("autofish set to "+num);
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
        textEntryAutoFish.show(autoFish);


        y += wdg.sz.y + 3;


        // alert fish name
        wdg = win.add(new CheckBox("Recast before idle fishing"){
            {a = avoidInvoluntaryFishing;}
            public void changed(boolean val) {
                //super.changed(val);
                avoidInvoluntaryFishing = val;
            }
        },0,y);


        // order fish by % chance
//        wdg = win.add(new CheckBox("Order fish by %"){
//            {a = reorderFishList;}
//            public void changed(boolean val) {
//                //super.changed(val);
//                reorderFishList = val;
//            }
//        },ZeeWindow.posRight(wdg,5,y));


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
                    Inventory invCreel = ZeeConfig.getUniqueWindowsInventory("Creel");
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

        // collect buttons and labels
        HashMap<Button,List<Label>> mapButtonLabels = new HashMap<>();
        Button key = null;
        List<Label> list = null;
        for (Widget widget : widgets) {
            if (widget instanceof Button) {
                key = (Button) widget;
                if (key!=null){
                    list = new ArrayList<>();
                    mapButtonLabels.put(key, list);
                }
            } else if (widget instanceof Label ) {
                if (key!=null && list!=null){
                    list.add((Label) widget);
                }
            }
        }
//        mapButtonLabels.forEach((keyBtn,listLabels) -> {
//            StringBuilder sb = new StringBuilder();
//            for (Label lbl : listLabels) {
//                sb.append(lbl.texts);
//                sb.append("  ");
//            }
//            println(sb.toString());
//        });
        if (mapButtonLabels.size()==0){
            println("fishing > couldnt build map button labels");
            return;
        }


        // fish name alert
        if (!fishNameAlert.isBlank()){
            for (Map.Entry<Button, List<Label>> buttonListEntry : mapButtonLabels.entrySet()) {
                List<Label> lbls = buttonListEntry.getValue();
                for (Label lbl : lbls) {
                    // search "fishname:"
                    if (lbl.texts.toLowerCase().contentEquals(fishNameAlert.toLowerCase()+":")){
                        //highlight row's labels
                        for (Label rowLbl : lbls) {
                            rowLbl.setcolor(Color.cyan);
                        }
                        // text2speak percentage
                        String percChance = lbls.get(lbls.size()-1).texts;
                        ZeeSynth.textToSpeakLinuxFestival(percChance + " percent");
                        break;
                    }
                }
            }
        }


        // autofish above %
        if(autoFish) {
            for (Map.Entry<Button, List<Label>> buttonListEntry : mapButtonLabels.entrySet()) {
                List<Label> lbls = buttonListEntry.getValue();
                String perc = lbls.get(lbls.size()-1).texts;
                int num = Integer.parseInt(perc.replaceAll("[^0-9]", ""));
                if (num >= autoFishAbove) {
                    println("autofish > " + perc);
                    Button btn = buttonListEntry.getKey();
                    if (btn != null) {
                        btn.click();
                        return;
                    }else {
                        println("btn null");
                    }
                    break;
                }
            }
        }


        // reoreder fish list by chance %
//        if (reorderFishList && mapButtonLabels.size() > 0){
//            println("TODO reorder fish list");
//            new ZeeThread(){
//                public void run() {
//                    try {
//                        sleep(1000);
//                        reorderFishListByPercChance(mapButtonLabels);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }.start();
//        }
    }

    private static void reorderFishListByPercChance(HashMap<Button, List<Label>> mapButtonLabels) {

        ArrayList<Map.Entry<Button, List<Label>>> arrWidgets = new ArrayList<>(mapButtonLabels.entrySet());

        println("size "+arrWidgets.size());

        for (int row1 = 0; row1 < arrWidgets.size()-1; row1++) {
            List<Label> list1 = arrWidgets.get(row1).getValue();
            Label percLabel1 = list1.get(list1.size()-1);
            int y1 = percLabel1.c.y;
            int perc1 = Integer.parseInt(percLabel1.texts.replaceAll("[^0-9]", ""));
            for (int row2 = 1; row2 < arrWidgets.size(); row2++) {
                List<Label> list2 = arrWidgets.get(row2).getValue();
                Label percLabel2 = list2.get(list2.size()-1);
                int y2 = percLabel2.c.y;
                int perc2 = Integer.parseInt(percLabel2.texts.replaceAll("[^0-9]", ""));
                // switch all widgets Coords.y
                println("    "+perc2+" > "+perc1);
                if (perc2 > perc1){
                    println("        switch!");
                    //change coords row1
                    arrWidgets.get(row1).getKey().c.y = y2;
                    for (Label label : arrWidgets.get(row1).getValue()) {
                        label.c.y = y2;
                    }
                    //change coords row2
                    arrWidgets.get(row2).getKey().c.y = y1;
                    for (Label label : arrWidgets.get(row2).getValue()) {
                        label.c.y = y1;
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

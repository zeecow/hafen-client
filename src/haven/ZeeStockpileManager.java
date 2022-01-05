package haven;

import java.util.List;

public class ZeeStockpileManager extends ZeeThread{

    static ZeeWindow windowManager;
    public static Gob gobPile, gobSource;
    public static boolean busy;
    static GameUI gameUI;
    static Inventory mainInv;
    static boolean audioExit;
    public static String lastPetalName;
    public static String lastInvItemName;

    public ZeeStockpileManager() {
        busy = true;
        audioExit = true;
        gameUI = ZeeConfig.gameUI;
        mainInv = gameUI.maininv;
    }

    @Override
    public void run() {
        println(">pile start");
        ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"auto piling",0,255,0,255,10);
        try{
            startPiling();
        }catch (Exception e){
            e.printStackTrace();
        }
        busy = false;
        ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
        println(">pile end");
    }

    // gfx/terobjs/stockpile-board
    // gfx/terobjs/stockpile-wblock
    private static void startPiling() throws InterruptedException {

        //find pile
        if(lastPetalName.equals("Pick leaf"))
            gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByName("stockpile-leaf"));
        else if (lastPetalName.equals("Chop into blocks"))
            gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByName("stockpile-wblock"));
        else if (lastPetalName.equals("Make boards"))
            gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByName("stockpile-board"));

        //mark gob pile and source
        ZeeConfig.addGobText(gobPile,"pile",0,255,0,255,10);
        ZeeConfig.addGobText(gobSource,"source",0,255,0,255,10);

        //start collection from source
        if( gobSource==null || !ZeeClickGobManager.clickGobPetal(gobSource, lastPetalName) ){
            println("no more source? gobSource0 = "+gobSource);
            pileAndExit();
            return;
        }

        while(busy) {

            if (lastPetalName.equals("Make boards"))
                waitInvFullOrHoldingItem(mainInv, 3000);//boards take longer to make
            else
                waitInvFullOrHoldingItem(mainInv);

            if (gameUI.vhand == null) {//if not holding item
                List<WItem> invItems = mainInv.getWItemsByName(lastInvItemName);
                if(invItems.size()==0) {
                    //no inventory items, try getting more from source
                    if( gobSource==null || !ZeeClickGobManager.clickGobPetal(gobSource, lastPetalName) ){
                        println("no more source? gobSource1 = "+gobSource);
                        pileAndExit();
                    }
                    continue;
                }
                WItem wItem = invItems.get(0);
                if (ZeeClickItemManager.pickUpItem(wItem)) { //pickup mulberry leaf
                    ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//right click stockpile
                    if (waitNotHoldingItem()) {
                        //piling successfull, try getting more from source
                        if( gobSource==null || !ZeeClickGobManager.clickGobPetal(gobSource, lastPetalName) ){
                            println("no more source? gobSource2 = "+gobSource);
                            pileAndExit();
                        }
                    } else {
                        println("pile full?");
                        gameUI.msg("stockpile full?");
                        pileAndExit();
                    }
                } else {
                    println("couldn't pickup source item?");
                    pileAndExit();
                }
            } else {
                println("holding item? try stockpiling...");
                ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//right click stockpile
                waitNotHoldingItem();
                if( gobSource==null || !ZeeClickGobManager.clickGobPetal(gobSource, lastPetalName) ){
                    println("no more source? gobSource3 = "+gobSource);
                    pileAndExit();
                    return;
                }
            }

        }
    }


    public static void checkPlacedPileUIWdgmsg(Widget sender, String msg) {
        if(ZeeConfig.pilerMode && msg.equals("place") && sender instanceof MapView) {
            showWindow();
        }
    }


    public static void checkClickedPetal(String petalName) {
        lastPetalName = petalName;
        if (petalName.equals("Pick leaf")) {
            if (ZeeConfig.lastMapViewClickGobName.equals("gfx/terobjs/trees/mulberry")
                || ZeeConfig.lastMapViewClickGobName.equals("gfx/terobjs/trees/laurel")) {
                ZeeConfig.pilerMode = true;
                gobSource = ZeeConfig.lastMapViewClickGob;
            }else{
                ZeeConfig.pilerMode = false;
            }
        }else if(petalName.equals("Chop into blocks")){
            ZeeConfig.pilerMode = true;
            gobSource = ZeeConfig.lastMapViewClickGob;
        }else if(petalName.equals("Make boards")){
            ZeeConfig.pilerMode = true;
            gobSource = ZeeConfig.lastMapViewClickGob;
        }else{
            ZeeConfig.pilerMode = false;
        }
    }

    private static void showWindow() {

        Widget wdg;

        if(windowManager ==null) {

            windowManager = new ZeeWindow(new Coord(300, 120), "Stockpile manager") {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("close")) {
                        audioExit = false;
                        exitManager();
                    }else
                        super.wdgmsg(msg, args);
                }
            };

            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(75),"auto pile"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        new ZeeStockpileManager().start();
                    }
                }
            }, 115,45);


            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));

        }else{
            windowManager.show();
        }
    }

    private static void pileAndExit() throws InterruptedException {
        pileItems();
        exitManager();
    }

    public static void exitManager() {
        busy = false;
        ZeeConfig.pilerMode = false;
        ZeeConfig.removeGobText(gobPile);
        ZeeConfig.removeGobText(gobSource);
        if (audioExit)
            gameUI.msg("Pile manager ended.");
        windowManager.hide();
    }


    public static void cancelFlowerMenu() throws InterruptedException {
        FlowerMenu fm = ZeeConfig.gameUI.ui.root.getchild(FlowerMenu.class);
        if(fm != null) {
            fm.choose(null);
            fm.destroy();
        }
    }


    private static void pileItems() throws InterruptedException {
        cancelFlowerMenu();
        if (gameUI.vhand == null) {//if not holding item
            List<WItem> invItems = mainInv.getWItemsByName(lastInvItemName);
            if(invItems.size()==0) {
                return;//inv has no more items
            }
            WItem wItem = invItems.get(0);
            if (ZeeClickItemManager.pickUpItem(wItem)) { //pickup source item
                ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//shift+right click stockpile
                if (!waitNotHoldingItem()) {
                    println("pileItems > pile full?");
                }
            } else {
                println("pileItems > couldn't pickup item?");
            }
        } else {
            ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//shift+right click stockpile
        }
    }

}

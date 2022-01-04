package haven;

import java.util.List;

public class ZeeStockpileManager extends ZeeThread{

    static ZeeWindow windowManager;
    public static Gob gobPile, gobSource;
    public static boolean busy;
    static GameUI gameUI;
    static Inventory mainInv;
    static boolean audioExit;

    public ZeeStockpileManager() {
        busy = true;
        audioExit = true;
        gameUI = ZeeConfig.gameUI;
        mainInv = gameUI.maininv;
    }

    @Override
    public void run() {
        println(">pile manager start");
        try{

            if(ZeeConfig.lastMapViewClickGobName.equals("gfx/terobjs/trees/mulberry")) {
                startMulberry();
            }else {
                println("no stockpile code for "+ZeeConfig.lastMapViewClickGobName);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        busy = false;
        println(">pile manager end");
    }

    private static void startMulberry() throws InterruptedException {

        //find pile
        gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByName("stockpile-leaf"));
        ZeeConfig.addGobText(gobPile,"pile",0,255,0,255,10);

        //pick leaf
        ZeeConfig.addGobText(gobSource,"tree",0,255,0,255,10);
        if( !ZeeClickGobManager.clickGobPetal(gobSource, "Pick leaf") ){
            println("no more leaves? 0");
            mulberryExit();
            return;
        }

        while(busy) {

            waitInvFull(mainInv);

            if (gameUI.vhand == null) {//if not holding item
                List<WItem> invLeaves = mainInv.getWItemsByName("leaf-mulberrytree");
                if(invLeaves.size()==0) {
                    //no inventory leaves, try getting more leaves
                    if( !ZeeClickGobManager.clickGobPetal(gobSource, "Pick leaf") ){
                        println("no more leaves? 1");
                        mulberryExit();
                    }
                    continue;
                }
                WItem wItem = invLeaves.get(0);
                if (ZeeClickItemManager.pickUpItem(wItem)) { //pickup mulberry leaf
                    ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//right click stockpile
                    if (waitNotHoldingItem()) {
                        //piling successfull, try getting more leaves
                        if( !ZeeClickGobManager.clickGobPetal(gobSource, "Pick leaf") ){
                            println("no more leaves? 2");
                            mulberryExit();
                        }
                    } else {
                        println("pile full?");
                        gameUI.msg("stockpile full?");
                        mulberryExit();
                    }
                } else {
                    println("couldn't pickup leaf item?");
                    mulberryExit();
                }
            } else {
                println("holding item? try stockpiling...");
                ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//right click stockpile
            }

        }
    }

    //  gfx/terobjs/stockpile-leaf
    //  gfx/invobjs/leaf-mulberrytree
    //  gfx/terobjs/trees/mulberry
    public static void checkPlacedPileUIWdgmsg(Widget sender, String msg) {
        if(ZeeConfig.pilerMode && msg.equals("place") && sender instanceof MapView) {
            showWindow();
        }
    }

    public static void checkClickedGob(Gob clickGob) {
        if(ZeeConfig.lastMapViewClickGobName.equals("gfx/terobjs/trees/mulberry")){
            gobSource = clickGob;
        }
    }

    private static void showWindow() {

        Widget wdg;

        if(windowManager ==null) {

            windowManager = new ZeeWindow(new Coord(300, 120), "Stockpile manager") {
                public void wdgmsg(String msg, Object... args) {
                    if (msg == "close") {
                        audioExit = false;
                        ZeeInvMainOptionsWdg.cbPiler.set(false);//uncheck piler option
                        exitManager();
                    }
                    super.wdgmsg(msg, args);
                }
            };

            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(75),"auto pile"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        new ZeeStockpileManager().start();
                    }
                }
            }, 5,5);


            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));

        }else{
            windowManager.show();
        }
    }

    private static void mulberryExit() throws InterruptedException {
        mulberryPileLeaves();
        exitManager();
    }

    public static void exitManager() {
        busy = false;
        ZeeConfig.removeGobText(gobPile);
        ZeeConfig.removeGobText(gobSource);
        if (audioExit)
            gameUI.msg("Pile manager ended.");
    }


    public static void cancelFlowerMenu() throws InterruptedException {
        FlowerMenu fm = ZeeConfig.gameUI.ui.root.getchild(FlowerMenu.class);
        if(fm != null) {
            fm.choose(null);
            fm.destroy();
        }
    }

    private static void mulberryPileLeaves() throws InterruptedException {
        cancelFlowerMenu();
        if (gameUI.vhand == null) {//if not holding item
            List<WItem> invLeaves = mainInv.getWItemsByName("leaf-mulberrytree");
            if(invLeaves.size()==0) {
                return;//inv has no leaf
            }
            WItem wItem = invLeaves.get(0);
            if (ZeeClickItemManager.pickUpItem(wItem)) { //pickup mulberry leaf
                ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//shift+right click stockpile
                if (!waitNotHoldingItem()) {
                    println("mulberryPileLeaves > pile full?");
                }
            } else {
                println("mulberryPileLeaves > couldn't pickup leaf item?");
            }
        } else {
            ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//shift+right click stockpile
        }
    }

}

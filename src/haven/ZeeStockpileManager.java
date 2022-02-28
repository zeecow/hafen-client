package haven;

import java.util.List;

public class ZeeStockpileManager extends ZeeThread{

    public static final String STOCKPILE_LEAF = "gfx/terobjs/stockpile-leaf";
    public static final String STOCKPILE_BLOCK = "gfx/terobjs/stockpile-wblock";
    public static final String STOCKPILE_BOARD = "gfx/terobjs/stockpile-board";
    public static final String STOCKPILE_STONE = "gfx/terobjs/stockpile-stone";
    public static final String BASENAME_MULB_LEAF = "leaf-mulberrytree";
    static ZeeWindow windowManager;
    public static boolean busy;
    static GameUI gameUI;
    static Inventory mainInv;
    static boolean audioExit;
    public static String lastPetalName;
    public static Gob gobPile, gobSource;
    public static MapView.Plob lastGobPlaced;
    public static String lastGroundItemName;
    static boolean isGroundItems;

    public ZeeStockpileManager(boolean groundItems) {
        busy = true;
        audioExit = true;
        gameUI = ZeeConfig.gameUI;
        mainInv = gameUI.maininv;
        isGroundItems = groundItems;
    }

    @Override
    public void run() {
        println(">pile start");
        ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"auto piling",0,255,0,255,10);
        try{
            if(isGroundItems)
                pileGroundItems();
            else
                pileSourceItems();
        }catch (Exception e){
            e.printStackTrace();
        }
        busy = false;
        ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
        println(">pile end");
    }


    public static void checkGroundItemClicked(String gobName) {
        if (gobName.equals("gfx/terobjs/items/tobacco-fresh")) {
            ZeeConfig.pilerMode = true;
            lastGroundItemName = gobName;
        }
    }


    // gfx/invobjs/tobacco-fresh
    // gfx/terobjs/items/tobacco-fresh
    // gfx/terobjs/stockpile-pipeleaves
    private static void pileGroundItems() throws InterruptedException {

        List<Gob> leaves;
        Gob closestLeaf;
        List<WItem> invLeaves;

        if (lastGroundItemName.equals("gfx/terobjs/items/tobacco-fresh")){

            gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-pipeleaves"));
            ZeeConfig.removeGobText(gobPile);
            ZeeConfig.addGobText(gobPile,"pile",0,255,0,255,10);

            invLeaves = mainInv.getWItemsByName("tobacco-fresh");
            if(invLeaves.size()>0) {
                ZeeClickItemManager.pickUpItem(invLeaves.get(0));
                ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);
                waitNotHoldingItem();
            }

            while (busy) {
                leaves = ZeeConfig.findGobsByNameContains("gfx/terobjs/items/tobacco-fresh");
                if(leaves.size()==0){
                    exitManager();
                    return;
                }
                closestLeaf = ZeeConfig.getClosestGob(leaves);
                if (closestLeaf != null) {
                    ZeeClickGobManager.gobClick(closestLeaf, 3, UI.MOD_SHIFT);
                    waitPlayerIdleFor(1);
                    if (!ZeeConfig.isPlayerHoldingItem()) {
                        invLeaves = mainInv.getWItemsByName("tobacco-fresh");
                        ZeeClickItemManager.pickUpItem(invLeaves.get(0));
                    }
                    ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);
                    waitPlayerIdleFor(1);
                    if(ZeeConfig.isPlayerHoldingItem()){
                        println("stockpile full");
                        exitManager();
                    }
                }else{
                    println("no close leaf");
                    exitManager();
                }
            }
        }
    }

    // gfx/terobjs/stockpile-board
    // gfx/terobjs/stockpile-wblock
    private static void pileSourceItems() throws InterruptedException {

        //find pile
        if(lastPetalName.equals("Pick leaf"))
            gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-leaf"));
        else if (lastPetalName.equals("Chop into blocks"))
            gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-wblock"));
        else if (lastPetalName.equals("Make boards"))
            gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-board"));
        else if (lastPetalName.equals("Chip stone"))
            gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-stone"));

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

            //if (lastPetalName.equals("Make boards") || lastPetalName.equals("Chip stone"))
            //    waitInvFullOrHoldingItem(mainInv, 3000);// boards/boulder take longer to make
            //else
            //    waitInvFullOrHoldingItem(mainInv);
            waitInvFullOrHoldingItem(mainInv, 3000);// boards/boulder takes longer to produce item

            if (gameUI.vhand == null) {//if not holding item
                List<WItem> invItems = mainInv.getWItemsByName(ZeeConfig.lastInvItemBaseName);
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

        if(msg.equals("place") && sender instanceof MapView) {

            if(lastGobPlaced!=null && lastGobPlaced.getres()!=null && lastGobPlaced.getres().name.contains("/stockpile-")) {

                //lastGobPlacedMs = now();
                //println((now() - lastGroundItemNameMs)+" < "+5000);

                if(lastGroundItemName!=null && lastGroundItemName.endsWith(ZeeConfig.lastInvItemBaseName)  &&  now() - ZeeConfig.lastInvItemMs < 3000) {
                    showWindow(true);
                } else {
                    String name = lastGobPlaced.getres().name;
                    boolean show = false;

                    if (now() - ZeeConfig.lastInvItemMs > 3000) //3s
                        show = false; // time limit to avoid late unwanted window popup
                    else if (name.equals(STOCKPILE_LEAF) && ZeeConfig.lastInvItemBaseName.equals(BASENAME_MULB_LEAF))
                        show = true;
                    else if (name.equals(STOCKPILE_BLOCK) && ZeeConfig.lastInvItemBaseName.startsWith("wblock-"))
                        show = true;
                    else if (name.equals(STOCKPILE_BOARD) && ZeeConfig.lastInvItemBaseName.contains("board-"))
                        show = true;
                    else if (name.equals(STOCKPILE_STONE) && ZeeMiningManager.isBoulder(gobSource))
                        show = true;

                    if(show)
                        showWindow(false);
                }
            }
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
        }else if(petalName.equals("Chip stone")){
            ZeeConfig.pilerMode = true;
            gobSource = ZeeConfig.lastMapViewClickGob;
        }else{
            ZeeConfig.pilerMode = false;
        }
    }

    private static void showWindow(boolean groundItems) {

        Widget wdg;

        if(windowManager ==null) {

            windowManager = new ZeeWindow(new Coord(150, 60), "Stockpile manager") {
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
                        new ZeeStockpileManager(groundItems).start();
                    }
                }
            }, 5,5);


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
            List<WItem> invItems = mainInv.getWItemsByName(ZeeConfig.lastInvItemBaseName);
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

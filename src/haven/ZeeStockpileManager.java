package haven;

import java.util.List;

import static haven.OCache.posres;

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
    }


    public static void checkGroundItemClicked(String gobName) {
        if (gobName.equals("gfx/terobjs/items/tobacco-fresh")) {
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
            ZeeConfig.addPlayerText("auto piling");
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
                        exitManager("pileGroundItems() > stockpile full");
                    }
                }else{
                    exitManager("no close leaf");
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
        ZeeConfig.addPlayerText("auto piling");
        ZeeConfig.addGobText(gobPile,"pile",0,255,0,255,10);
        ZeeConfig.addGobText(gobSource,"source",0,255,0,255,10);

        //start collection from source
        if( gobSource==null || !ZeeClickGobManager.clickGobPetal(gobSource, lastPetalName) ){
            println("no more source? gobSource0 = "+gobSource);
            pileAndExit();
            return;
        }

        while(busy) {

            if (lastPetalName.equals("Pick leaf"))
                waitInvIdleMs(2000);//TODO remove special case if possible
            else
                waitPlayerIdleOrHoldingItem(2);//blocks, boards, stones
            //waitInvFullOrHoldingItem(mainInv, 3000);// boards/boulder takes longer to produce item

            if (!ZeeConfig.isPlayerHoldingItem()) { //if not holding item
                /*
                    gfx/invobjs/wblock-maple
                    gfx/invobjs/board-maple
                 */
                List<WItem> invItems;
                if (lastPetalName.equals("Make boards"))
                    invItems = mainInv.getWItemsByName("gfx/invobjs/board-");//avoid spiral curio
                else if (lastPetalName.equals("Chop into blocks"))
                    invItems = mainInv.getWItemsByName("gfx/invobjs/wblock-");//avoid splinter curio
                else
                    invItems = mainInv.getWItemsByName(ZeeConfig.lastInvItemBaseName);//get last inv item without checking
                if(invItems.size()==0) {
                    //no inventory items, try getting more from source
                    if(!busy)
                        continue;
                    if( gobSource==null || !ZeeClickGobManager.clickGobPetal(gobSource, lastPetalName) ){
                        println("no more source? gobSource1 = "+gobSource);
                        pileAndExit();
                    }
                    continue;
                }
                if(!busy)
                    continue;
                WItem wItem = invItems.get(0);
                String itemName = wItem.item.getres().name;
                if (ZeeClickItemManager.pickUpItem(wItem)) { //pickup inv item
                    ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//right click stockpile
                    if (waitNotHoldingItem()) {//piling successfull
                        if (mainInv.getWItemsByName(itemName).size() > 0)
                            exitManager("pile full (inv still has items)");
                        if(!busy)
                            continue;
                        //try getting more from source
                        if( gobSource==null || !ZeeClickGobManager.clickGobPetal(gobSource, lastPetalName) ){
                            println("gob source consumed = "+gobSource);
                            pileAndExit();
                        }
                    } else {
                        exitManager("pile full??");
                    }
                } else {
                    //pileAndExit();
                    exitManager("couldn't pickup source item??");
                }
            }
            else {
                //holding item? try stockpiling...
                if(!busy)
                    continue;
                String itemName = gameUI.vhand.item.getres().name;
                ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//right click stockpile
                if(waitNotHoldingItem()) {
                    if (mainInv.getWItemsByName(itemName).size() > 0)
                        exitManager("pile full (inv still has items) 2");
                    if (!busy)
                        continue;
                    if (gobSource == null || !ZeeClickGobManager.clickGobPetal(gobSource, lastPetalName)) {
                        println("no more source? gobSource3 = " + gobSource);
                        pileAndExit();
                        return;
                    }
                } else {
                    exitManager("pile full?? 2");
                }
            }
        }
        //pileAndExit();
    }

    public static void checkWdgmsgPileExists() {
        new ZeeThread() {
            public void run() {
                try {
                    waitNotHoldingItem(5000);
                    Gob closestPile = ZeeConfig.getClosestGobName("/stockpile-");
                    checkShowWindow(closestPile.getres().name);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void checkWdgmsgPilePlacing() {

        if(lastGobPlaced!=null && lastGobPlaced.getres()!=null && lastGobPlaced.getres().name.contains("/stockpile-")) {
            //lastGobPlacedMs = now();
            //println((now() - lastGroundItemNameMs)+" < "+5000);
            if(lastGroundItemName!=null && lastGroundItemName.endsWith(ZeeConfig.lastInvItemBaseName)  &&  now() - ZeeConfig.lastInvItemMs < 3000) {
                showWindow(true);
            } else {
                String pileGobName = lastGobPlaced.getres().name;
                checkShowWindow(pileGobName);
            }
        }
    }

    private static void checkShowWindow(String pileGobName) {
        boolean show = false;
        if (lastPetalName==null || gobSource==null)
            show = false;
        else if (now() - ZeeConfig.lastInvItemMs > 3000) //3s
            show = false; // time limit to avoid late unwanted window popup
        else if (pileGobName.equals(STOCKPILE_LEAF) && ZeeConfig.lastInvItemBaseName.equals(BASENAME_MULB_LEAF))
            show = true;
        else if (pileGobName.equals(STOCKPILE_BLOCK) && ZeeConfig.lastInvItemBaseName.startsWith("wblock-"))
            show = true;
        else if (pileGobName.equals(STOCKPILE_BOARD) && ZeeConfig.lastInvItemBaseName.contains("board-"))
            show = true;
        else if (pileGobName.equals(STOCKPILE_STONE) && ZeeMiningManager.isBoulder(gobSource))
            show = true;

        if(show)
            showWindow(false);
    }

    public static void checkClickedPetal(String petalName) {
        lastPetalName = petalName;
        if (petalName.equals("Pick leaf")) {
            if (ZeeConfig.lastMapViewClickGobName.equals("gfx/terobjs/trees/mulberry")
                || ZeeConfig.lastMapViewClickGobName.equals("gfx/terobjs/trees/laurel")) {
                gobSource = ZeeConfig.lastMapViewClickGob;
            }
        }else if(petalName.equals("Chop into blocks")){
            gobSource = ZeeConfig.lastMapViewClickGob;
        }else if(petalName.equals("Make boards")){
            gobSource = ZeeConfig.lastMapViewClickGob;
        }else if(petalName.equals("Chip stone")){
            gobSource = ZeeConfig.lastMapViewClickGob;
        }
    }

    private static void showWindow(boolean groundItems) {
        Widget wdg;
        if(windowManager ==null) {
            windowManager = new ZeeWindow(new Coord(150, 60), "Stockpile manager") {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("close")) {
                        audioExit = false;
                        busy = false;
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

    public static void exitManager(String msg) {
        exitManager();
        println(msg);
    }
    public static void exitManager() {
        busy = false;
        ZeeConfig.clickGroundZero(1);
        if (ZeeConfig.pilerMode)
            ZeeInvMainOptionsWdg.cbPiler.set(false);
        ZeeConfig.removePlayerText();
        ZeeConfig.removeGobText(gobPile);
        ZeeConfig.removeGobText(gobSource);
        gobPile = gobSource = null;
        if (audioExit)
            gameUI.msg("Pile manager ended.");
        windowManager.hide();
    }


    private static void pileItems() throws InterruptedException {
        ZeeConfig.cancelFlowerMenu();
        waitNoFlowerMenu();
        if (!ZeeConfig.isPlayerHoldingItem()) {//if not holding item
            List<WItem> invItems = mainInv.getWItemsByName(ZeeConfig.lastInvItemBaseName);
            if(invItems.size()==0) {
                return;//inv has no more items
            }
            WItem wItem = invItems.get(0);
            if (ZeeClickItemManager.pickUpItem(wItem)) { //pickup source item
                ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//shift+right click stockpile
                if (!waitNotHoldingItem()) {
                    exitManager("pileItems > pile full?");
                }
            } else {
                exitManager("pileItems > couldn't pickup item?");
            }
        } else if (gobPile!=null){
            ZeeClickGobManager.gobItemAct(gobPile, UI.MOD_SHIFT);//shift+right click stockpile
        }
    }


    public static void unloadWheelbarrowAtStockpile(Gob gobStockpile) throws Exception{
        try {
            ZeeConfig.addPlayerText("wheeling");
            Coord stockpileCoord = ZeeConfig.lastMapViewClickMc.floor(posres);
            if (ZeeConfig.isPlayerMountingHorse())
                ZeeConfig.unmountPlayerFromHorse(stockpileCoord);
            sleep(500);
            Coord pc = ZeeConfig.getPlayerCoord();
            Coord subc = ZeeConfig.getCoordGob(gobStockpile).sub(pc);
            int xsignal, ysignal;
            xsignal = subc.x >= 0 ? 1 : -1;
            ysignal = subc.y >= 0 ? 1 : -1;
            //println("pc"+pc+"  subc"+subc+"  pc.add"+Coord.of(xsignal*200,ysignal*200));
            ZeeConfig.clickCoord(pc.add(xsignal * 500, ysignal * 500), 3);//drop wheelbarrow
            sleep(PING_MS);
            Gob wb = ZeeConfig.getClosestGobName("gfx/terobjs/vehicle/wheelbarrow");
            ZeeClickGobManager.gobClick(wb, 3);//activate wheelbarrow
            sleep(PING_MS);
            //ZeeConfig.clickCoord(stockpileCoord,3);//drop stockpile
            ZeeClickGobManager.gobClick(gobStockpile, 3);
            waitPlayerIdleFor(1);
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }


    public static void unloadWheelbarrowStockpileAtGround(Coord mcFloorPosres) {
        try {
            ZeeConfig.addPlayerText("wheeling");
            if (ZeeConfig.isPlayerMountingHorse()){
                ZeeConfig.unmountPlayerFromHorse(mcFloorPosres);
            }
            sleep(500);
            Coord pc = ZeeConfig.getPlayerCoord();
            Coord subc = mcFloorPosres.sub(pc);
            int xsignal, ysignal;
            xsignal = subc.x >= 0 ? 1 : -1;
            ysignal = subc.y >= 0 ? 1 : -1;
            //println("pc"+pc+"  subc"+subc+"  pc.add"+Coord.of(xsignal*200,ysignal*200));
            ZeeConfig.clickCoord(pc.add(xsignal*500,ysignal*500), 3);//drop wheelbarrow
            sleep(PING_MS);
            Gob wb = ZeeConfig.getClosestGobName("gfx/terobjs/vehicle/wheelbarrow");
            ZeeClickGobManager.gobClick(wb,3);//activate wheelbarrow
            sleep(PING_MS);
            ZeeConfig.clickCoord(mcFloorPosres,3);//drop stockpile
            waitPlayerIdleFor(1);
        } catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }
}

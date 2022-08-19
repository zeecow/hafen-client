package haven;

import java.util.List;

import static haven.OCache.posres;

public class ZeeManagerStockpile extends ZeeThread{

    static final String TASK_PILE_GROUND_ITEMS = "TASK_PILE_GROUND_ITEMS";
    static final String TASK_PILE_GOB_SOURCE = "TASK_PILE_GOB_SOURCE";
    static final String TASK_PILE_TILE_SOURCE = "TASK_PILE_TILE_SOURCE";
    public static final String STOCKPILE_LEAF = "gfx/terobjs/stockpile-leaf";
    public static final String STOCKPILE_BLOCK = "gfx/terobjs/stockpile-wblock";
    public static final String STOCKPILE_BOARD = "gfx/terobjs/stockpile-board";
    public static final String STOCKPILE_COAL = "gfx/terobjs/stockpile-coal";
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
    public static String lastTileStoneSourceTileName;
    public static Coord2d lastTileStoneSourceCoordMc;
    public static boolean diggingStone;
    private final String task;

    public ZeeManagerStockpile(String task) {
        this.task = task;
        busy = true;
        audioExit = true;
        gameUI = ZeeConfig.gameUI;
        mainInv = gameUI.maininv;
    }

    @Override
    public void run() {
        try{
            if(task.contentEquals(TASK_PILE_GROUND_ITEMS)) {
                startPilingGroundItems();
            }
            else if (task.contentEquals(TASK_PILE_GOB_SOURCE)){
                startPilingGobSource();
            }
            else if (task.contentEquals(TASK_PILE_TILE_SOURCE)){
                startPilingTileSource();
            }
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
    private void startPilingGroundItems() throws InterruptedException {

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
                ZeeManagerItemClick.pickUpItem(invLeaves.get(0));
                ZeeManagerGobClick.itemActGob(gobPile, UI.MOD_SHIFT);
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
                    ZeeManagerGobClick.gobClick(closestLeaf, 3, UI.MOD_SHIFT);
                    waitPlayerIdleFor(1);
                    if (!ZeeConfig.isPlayerHoldingItem()) {
                        invLeaves = mainInv.getWItemsByName("tobacco-fresh");
                        ZeeManagerItemClick.pickUpItem(invLeaves.get(0));
                    }
                    ZeeManagerGobClick.itemActGob(gobPile, UI.MOD_SHIFT);
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
    private void startPilingGobSource() throws InterruptedException {

        //find pile
        gobPile = findPile();

        //mark gob pile and source
        ZeeConfig.addPlayerText("piling");
        ZeeConfig.addGobText(gobPile,"pile");
        ZeeConfig.addGobText(gobSource,"source");

        //start collection from source
        if( gobSource==null || !ZeeManagerGobClick.clickGobPetal(gobSource, lastPetalName) ){
            println("no more source? gobSource0 = "+gobSource);
            pileAndExit();
            return;
        }

        while(busy) {

            // wait reaching source
            waitPlayerDistToGob(gobSource,15);

            // wait inv full
            waitInvFullOrHoldingItem(mainInv, 3000);

            // if not holding item
            if (!ZeeConfig.isPlayerHoldingItem()) {
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
                    if( gobSource==null || !ZeeManagerGobClick.clickGobPetal(gobSource, lastPetalName) ){
                        println("no more source? gobSource1 = "+gobSource);
                        pileAndExit();
                    }
                    continue;
                }
                if(!busy)
                    continue;
                WItem wItem = invItems.get(0);
                String itemName = wItem.item.getres().name;
                //pickup inv item
                if (ZeeManagerItemClick.pickUpItem(wItem)) {
                    //right click stockpile
                    ZeeManagerGobClick.itemActGob(gobPile, UI.MOD_SHIFT);
                    if (waitNotHoldingItem()) {//piling successfull
                        sleep(1000);//wait inv transfer to stockpile
                        if (mainInv.getWItemsByName(itemName).size() > 0)
                            exitManager("pile full (inv still has items)");
                        if(!busy)
                            continue;
                        //try getting more from source
                        if( gobSource==null || !ZeeManagerGobClick.clickGobPetal(gobSource, lastPetalName) ){
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
            //holding item? try stockpiling...
            else {

                if(!busy)
                    continue;
                String itemName = gameUI.vhand.item.getres().name;
                ZeeManagerGobClick.itemActGob(gobPile, UI.MOD_SHIFT);//right click stockpile
                if(waitNotHoldingItem()) {
                    sleep(1000);
                    //check if pile full
                    if (mainInv.getWItemsByName(itemName).size() > 0)
                        exitManager("pile full (inv still has items) 2");
                    if (!busy)
                        continue;
                    //check if tree still have leafs
                    if (gobSource == null || !ZeeManagerGobClick.clickGobPetal(gobSource, lastPetalName)) {
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

    private static Gob findPile() {
        if(lastPetalName.equals("Pick leaf"))
            return ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-leaf"));
        else if (lastPetalName.equals("Chop into blocks"))
            return ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-wblock"));
        else if (lastPetalName.equals("Make boards"))
            return ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-board"));
        else if (lastPetalName.equals("Chip stone"))
            return ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-stone"));
        else if (lastPetalName.equals("Collect coal"))
            return ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-coal"));
        return null;
    }


    private void startPilingTileSource() {
        println("startPilingTileSource > "+lastTileStoneSourceTileName);
        try {
            gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains("stockpile-stone"));
            ZeeConfig.addPlayerText("dig");
            ZeeConfig.addGobText(gobPile,"pile");

            pileItems();
            while (busy){

                //dig stone tile
                ZeeConfig.clickCoord(lastTileStoneSourceCoordMc.floor(posres),1);

                //wait inv full
                while(busy && ZeeConfig.getMainInventory().getNumberOfFreeSlots() > 0){
                    sleep(2000);
                }
                if (!busy)
                    break;

                pileItems();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        exitManager();
    }


    public static void checkWdgmsgPileExists() {
        new ZeeThread() {
            public void run() {
                try {
                    waitNotHoldingItem(5000);
                    Gob closestPile = findPile();
                    if (closestPile!=null) {
                        checkShowWindow(closestPile.getres().name);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void checkWdgmsgPilePlacing() {

        if (lastGobPlaced!=null && lastGobPlaced.getres()!=null && lastGobPlaced.getres().name.contains("/stockpile-")) {
            //lastGobPlacedMs = now();
            //println((now() - lastGroundItemNameMs)+" < "+5000);
            if (lastGroundItemName!=null && lastGroundItemName.endsWith(ZeeConfig.lastInvItemBaseName)  &&  now() - ZeeConfig.lastInvItemMs < 3000) {
                showWindow(TASK_PILE_GROUND_ITEMS);
            } else {
                String pileGobName = lastGobPlaced.getres().name;
                checkShowWindow(pileGobName);
            }
        }
    }

    private static void checkShowWindow(String pileGobName) {

        boolean show = false;
        String task = "";

        if ( diggingStone && pileGobName.equals(STOCKPILE_STONE) ) {
            show = true;
            task = TASK_PILE_TILE_SOURCE;
        }
        else if ( lastPetalName == null || gobSource == null ){
            show = false;
        }
        else if (now() - ZeeConfig.lastInvItemMs > 3000){ //3s
            show = false; // time limit to avoid late unwanted window popup
        }
        else if (pileGobName.equals(STOCKPILE_LEAF) && ZeeConfig.lastInvItemBaseName.contentEquals(BASENAME_MULB_LEAF)){
            show = true;
            task = TASK_PILE_GOB_SOURCE;
        }
        else if (pileGobName.equals(STOCKPILE_LEAF) && ZeeConfig.lastInvItemBaseName.contentEquals("leaf-laurel")){
            show = true;
            task = TASK_PILE_GOB_SOURCE;
        }
        else if (pileGobName.equals(STOCKPILE_BLOCK) && ZeeConfig.lastInvItemBaseName.startsWith("wblock-")){
            show = true;
            task = TASK_PILE_GOB_SOURCE;
        }
        else if (pileGobName.equals(STOCKPILE_BOARD) && ZeeConfig.lastInvItemBaseName.contains("board-")){
            show = true;
            task = TASK_PILE_GOB_SOURCE;
        }
        else if (pileGobName.equals(STOCKPILE_COAL) && ZeeConfig.lastInvItemBaseName.contentEquals("coal")){
            show = true;
            task = TASK_PILE_GOB_SOURCE;
        }
        else if (pileGobName.equals(STOCKPILE_STONE) && ZeeManagerMiner.isBoulder(gobSource)){
            show = true;
            task = TASK_PILE_GOB_SOURCE;
        }

        if(show)
            showWindow(task);
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
        }else if(petalName.equals("Collect coal")){
            gobSource = ZeeConfig.lastMapViewClickGob;
        }
    }

    private static void showWindow(String task) {
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
                        new ZeeManagerStockpile(task).start();
                    }
                }
            }, 5,5);
            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));
        }else{
            windowManager.show();
        }
    }

    private void pileAndExit() throws InterruptedException {
        pileItems();
        exitManager();
    }

    public static void exitManager(String msg) {
        exitManager();
        println(msg);
    }
    public static void exitManager() {
        busy = false;
        ZeeConfig.stopMovingEscKey();
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


    private void pileItems() throws InterruptedException {
        ZeeConfig.cancelFlowerMenu();
        waitNoFlowerMenu();
        // if not holding item, pickup from inventory
        if (!ZeeConfig.isPlayerHoldingItem()) {
            List<WItem> invItems = mainInv.getWItemsByName(ZeeConfig.lastInvItemBaseName);
            if(invItems.size()==0) {
                return;//inv has no more items
            }
            WItem wItem = invItems.get(0);
            if (ZeeManagerItemClick.pickUpItem(wItem)) { //pickup source item
                ZeeManagerGobClick.itemActGob(gobPile, UI.MOD_SHIFT);//shift+right click stockpile
                if (!waitNotHoldingItem()) {
                    exitManager("pileItems > pile full?");
                }
            } else {
                exitManager("pileItems > couldn't pickup item?");
            }
        }
        // holding item
        else if (gobPile!=null){
            ZeeManagerGobClick.itemActGob(gobPile, UI.MOD_SHIFT);//shift+right click stockpile
            if (!waitNotHoldingItem()) {
                exitManager("pileItems > pile full? 2");
            }
        }
    }


    public static void useWheelbarrowAtStockpile(Gob gobStockpile) {
        try {
            ZeeConfig.addPlayerText("wheeling");
            Coord stockpileCoord = ZeeConfig.lastMapViewClickMc.floor(posres);
            if (ZeeConfig.isPlayerMountingHorse()) {
                ZeeConfig.unmountPlayerFromHorse(stockpileCoord);
                sleep(555);
            }
            Coord pc = ZeeConfig.getPlayerCoord();
            Coord subc = ZeeConfig.getCoordGob(gobStockpile).sub(pc);
            int xsignal, ysignal;
            xsignal = subc.x >= 0 ? 1 : -1;
            ysignal = subc.y >= 0 ? 1 : -1;
            //try to drop wheelbarrow towards stockpile
            ZeeConfig.clickCoord(pc.add(xsignal * 500, ysignal * 500), 3);
            sleep(555);
            if (!ZeeConfig.isPlayerCarryingWheelbarrow()){
                Gob wb = ZeeConfig.getClosestGobName("gfx/terobjs/vehicle/wheelbarrow");
                //activate wheelbarrow
                ZeeManagerGobClick.gobClick(wb, 3);
                sleep(PING_MS);
                //use wheelbarrow on stockpile
                ZeeManagerGobClick.gobClick(gobStockpile, 3);
                waitPlayerIdleVelocity();
            }else
                ZeeConfig.msg("couldn't drop wheelbarrow");
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }


    public static void unloadWheelbarrowStockpileAtGround(Coord mcFloorPosres) {
        try {
            ZeeConfig.addPlayerText("wheeling");
            Coord pc = ZeeConfig.getPlayerCoord();
            Coord subc = mcFloorPosres.sub(pc);
            int xsignal, ysignal;
            xsignal = subc.x >= 0 ? 1 : -1;
            ysignal = subc.y >= 0 ? 1 : -1;
            //drop wheelbarrow
            ZeeConfig.clickCoord(pc.add(xsignal*500,ysignal*500), 3);
            sleep(500);
            if (!ZeeConfig.isPlayerCarryingWheelbarrow()) {
                Gob wb = ZeeConfig.getClosestGobName("gfx/terobjs/vehicle/wheelbarrow");
                //activate wheelbarrow
                ZeeManagerGobClick.gobClick(wb, 3);
                sleep(PING_MS);
                //use wheelbarrow at stockpile
                ZeeConfig.clickCoord(mcFloorPosres, 3);
                waitPlayerIdleVelocity();
            }else
                ZeeConfig.msg("couldn't drop wheelbarrow");
        } catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }
}

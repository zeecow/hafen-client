package haven;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static haven.OCache.posres;
import static java.util.Map.entry;

public class ZeeManagerStockpile extends ZeeThread{

    static final String TASK_PILE_GROUND_ITEMS = "TASK_PILE_GROUND_ITEMS";
    static final String TASK_PILE_GOB_SOURCE = "TASK_PILE_GOB_SOURCE";
    static final String TASK_PILE_TILE_SOURCE = "TASK_PILE_TILE_SOURCE";
    static final String STOCKPILE_LEAF = "gfx/terobjs/stockpile-leaf";
    static final String STOCKPILE_BLOCK = "gfx/terobjs/stockpile-wblock";
    static final String STOCKPILE_BOARD = "gfx/terobjs/stockpile-board";
    static final String STOCKPILE_COAL = "gfx/terobjs/stockpile-coal";
    static final String STOCKPILE_SAND = "gfx/terobjs/stockpile-sand";
    static final String STOCKPILE_STONE = "gfx/terobjs/stockpile-stone";
    static final String BASENAME_MULB_LEAF = "leaf-mulberrytree";
    static final String GFX_TILES_BEACH = "gfx/tiles/beach";
    static final String GFX_TILES_SANDCLIFF = "gfx/tiles/sandcliff";
    static final String GFX_TILES_MOUNTAIN = "gfx/tiles/mountain";

    static Map<String, String> mapItemPileRegex = Map.ofEntries(
            entry("gfx/(terobjs/items|invobjs)/flaxfibre", "/stockpile-flaxfibre"),
            entry("gfx/(terobjs/items|invobjs)/hempfibre", "/stockpile-hempfibre"),
            entry("gfx/(terobjs/items|invobjs)/turnip", "/stockpile-turnip"),
            entry("gfx/(terobjs/items|invobjs)/carrot", "/stockpile-carrot"),
            entry("gfx/(terobjs/items|invobjs)/beet", "/stockpile-beetroot"),
            entry("gfx/(terobjs/items|invobjs)/nugget-.+", "/stockpile-nugget-metal"),
            entry("gfx/(terobjs/items|invobjs)/bar-.+","/stockpile-metal"),
            entry("gfx/(terobjs/items|invobjs)/cloth", "/stockpile-cloth"),
            entry("gfx/(terobjs/items|invobjs)/rope","/stockpile-rope"),
            entry("gfx/(terobjs/items|invobjs)/(black)?coal","/stockpile-coal"),
            entry("gfx/(terobjs/items|invobjs)/wblock-.+","/stockpile-wblock"),
            entry("gfx/(terobjs/items|invobjs)/board-.+","/stockpile-board"),
            entry("gfx/(terobjs/items|invobjs)/leaf-.+","/stockpile-leaf"),
            entry("gfx/(terobjs/items|invobjs)/bough-.+","/stockpile-bough"),
            entry("gfx/(terobjs/items|invobjs)/sand","/stockpile-sand"),
            entry("gfx/(terobjs/items|invobjs)/clay-.+","/stockpile-clay"),
            entry("gfx/(terobjs/items|invobjs)/cattail","/stockpile-cattailpart"),
            entry("gfx/(terobjs/items|invobjs)/straw","/stockpile-straw"),
            entry("gfx/(terobjs/items|invobjs)/petrifiedshell","/stockpile-petrifiedshell"),
            entry("gfx/(terobjs/items|invobjs)/hopcones","/stockpile-hopcones"),
            entry("gfx/(terobjs/items|invobjs)/cucumber","/stockpile-cucumber"),
            entry("gfx/(terobjs/items|invobjs)/grapes","/stockpile-grapes"),
            entry("gfx/(terobjs/items|invobjs)/pumpkin","/stockpile-pumpkin"),
            entry("gfx/(terobjs/items|invobjs)/lettucehead","/stockpile-lettuce"),
            entry("gfx/(terobjs/items|invobjs)/plum","/stockpile-plum"),
            entry("gfx/(terobjs/items|invobjs)/apple","/stockpile-apple"),
            entry("gfx/(terobjs/items|invobjs)/pear","/stockpile-pear"),
            entry("gfx/(terobjs/items|invobjs)/quince","/stockpile-quince"),
            entry("gfx/(terobjs/items|invobjs)/lemon","/stockpile-lemon"),
            entry("gfx/(terobjs/items|invobjs)/tobacco-(cured|fresh)","/stockpile-pipeleaves"),
            entry("gfx/(terobjs/items|invobjs)/gems/gemstone","/stockpile-gemstone"),
            entry("gfx/(terobjs/items|invobjs)/bone","/stockpile-bone"),
            entry("gfx/(terobjs/items|invobjs)/feather","/stockpile-feather"),
            //ores
            entry("gfx/(terobjs/items|invobjs)/.+ite","/stockpile-ore"),
            // stones, requires extra code for regular ones
            entry("gfx/(terobjs/items|invobjs)/slag","/stockpile-stone"),
            entry("gfx/(terobjs/items|invobjs)/catgold","/stockpile-stone"),
            entry("gfx/(terobjs/items|invobjs)/quarryartz","/stockpile-stone"),
            // flowers, requires extra code for non poppy
            entry("gfx/(terobjs/items|invobjs)/flower-poppy","/stockpile-poppy"),
            //soil
            entry("gfx/(terobjs/items|invobjs)/(mulch|soil|earthworm)","/stockpile-soil"),
            //trash pile
            entry("gfx/(terobjs/items|invobjs)/(entrails|intestines|pumpkinflesh)","/stockpile-trash")
    );

    static ZeeWindow windowManager;
    public static boolean busy;
    static Inventory mainInv;
    public static String lastPetalName;
    public static Gob gobPile, gobSource;
    public static MapView.Plob lastGobPlaced;
    public static String lastGroundItemName;
    public static String lastTileStoneSourceTileName;
    public static Coord2d lastTileStoneSourceCoordMc;
    public static boolean diggingTileSource;
    private final String task;

    public ZeeManagerStockpile(String task) {
        this.task = task;
        busy = true;
        mainInv = ZeeConfig.gameUI.maininv;
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

        if(gobPile==null) {
            println("pile null");
            return;
        }

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

        // adjust timeout if chipping stone without pickaxe
        int timeout = 3000;
        if (lastPetalName.contentEquals("Chip stone")){
            if (ZeeManagerItemClick.isItemEquipped("/pickaxe"))
                timeout = 2000;
            else
                timeout = 5000;
        }else if (lastPetalName.contentEquals("Chop into blocks")){
            timeout = 1500;
        }else if (lastPetalName.contentEquals("Pick leaf")){
            timeout = 1500;
        }


        while(busy) {

            // wait reaching source
            waitPlayerDistToGob(gobSource,15);

            // wait inv full
            waitInvFullOrHoldingItem(mainInv, timeout);

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
                String itemName = ZeeConfig.gameUI.vhand.item.getres().name;
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
        if (diggingTileSource) {
            if (lastTileStoneSourceTileName.contentEquals(GFX_TILES_BEACH))
                return ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains(STOCKPILE_SAND));
            else
                return ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains(STOCKPILE_STONE));
        }
        if (lastPetalName==null)
            return null;
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
        try {

            if (lastTileStoneSourceTileName.contentEquals(GFX_TILES_BEACH))
                gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains(STOCKPILE_SAND));
            else // TODO check pickaxe?
                gobPile = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains(STOCKPILE_STONE));

            ZeeConfig.addGobText(gobPile,"pile");

            ZeeConfig.addPlayerText("dig");

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

        if ( diggingTileSource && (pileGobName.equals(STOCKPILE_STONE) || pileGobName.equals(STOCKPILE_SAND)) ) {
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
            if (ZeeConfig.isPlayerMountingHorse() || ZeeConfig.isPlayerDrivingingKicksled()) {
                //move to stockpile
                double dist = ZeeConfig.distanceToPlayer(gobStockpile);
                if (dist > 100){
                    ZeeConfig.moveToGobTile(gobStockpile);
                    waitPlayerDistToGob(gobStockpile,90);
                }
                //disembark/unmount
                ZeeManagerGobClick.disembarkVehicle(stockpileCoord);
                sleep(999);
            }
            Coord pc = ZeeConfig.getPlayerCoord();
            Coord subc = ZeeConfig.getGobCoord(gobStockpile).sub(pc);
            int xsignal, ysignal;
            xsignal = subc.x >= 0 ? 1 : -1;
            ysignal = subc.y >= 0 ? 1 : -1;
            //try to drop wheelbarrow towards stockpile
            ZeeConfig.clickCoord(pc.add(xsignal * 500, ysignal * 500), 3);
            sleep(555);
            if (!ZeeConfig.isPlayerCarryingWheelbarrow()){
                Gob wb = ZeeConfig.getClosestGobByNameContains("gfx/terobjs/vehicle/wheelbarrow");
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
                Gob wb = ZeeConfig.getClosestGobByNameContains("gfx/terobjs/vehicle/wheelbarrow");
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

    static boolean isGroundTileSource(String tileResName) {
        String[] list = new String[]{GFX_TILES_BEACH, GFX_TILES_SANDCLIFF , GFX_TILES_MOUNTAIN};
        //println(tileResName);
        for (int i = 0; i < list.length; i++) {
            if (list[i].contentEquals(tileResName))
                return true;
        }
        return false;
    }

    static void checkTileSourcePiling(Coord2d mc) {
        if ( ZeeConfig.pilerMode && ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_DIG) ) {
            String tileName = ZeeConfig.getTileResName(mc);
            if(ZeeManagerStockpile.isGroundTileSource(tileName)) {
                ZeeManagerStockpile.diggingTileSource = true;
                ZeeManagerStockpile.lastTileStoneSourceCoordMc = mc;
                ZeeManagerStockpile.lastTileStoneSourceTileName = tileName;
            } else
                ZeeManagerStockpile.diggingTileSource = false;
        } else
            ZeeManagerStockpile.diggingTileSource = false;
    }

    static boolean isGobPileable(Gob gob) {
        String resname = gob.getres().name;
        String[] keys = mapItemPileRegex.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++) {
            if(resname.matches(keys[i])) {
                return true;
            }
        }
        println("isGobPileable false");
        return false;
    }

    static boolean selAreaPile;
    static Gob selAreaPileGobItem;
    static ZeeWindow selAreaWindow;
    static void areaPilerWindow(Gob gobItem) {
        String title = "Area piler";
        Widget wdg;

        selAreaPileGobItem = gobItem;

        //remove prev windows
        if (selAreaWindow!=null)
            selAreaWindow.destroy();

        //create new window
        selAreaWindow = new ZeeWindow(Coord.of(330,170),title){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("close")){
                    exitAreaPiler();
                }
                super.wdgmsg(msg, args);
            }
        };

        //button select area
        wdg = selAreaWindow.add(new Button(UI.scale(160),"select pile items area"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    ZeeConfig.addPlayerText("Select pile items area");
                    selAreaPile = true;
                    //creates new MapView.Selector
                    ZeeConfig.gameUI.map.uimsg("sel", 1);
                    //show grid lines
                    ZeeConfig.gameUI.map.showgrid(true);
                }
            }
        });

        //label instructions
        wdg = selAreaWindow.add(new Label("Note: piles will be created outside area"),0,wdg.c.y+wdg.sz.y+7);

        selAreaWindow.pack();
        ZeeConfig.gameUI.add(selAreaWindow,300,300);
    }

    static void exitAreaPiler() {
        selAreaPile = false;
        ZeeConfig.removePlayerText();
        ZeeConfig.gameUI.map.uimsg("sel", 0);
        ZeeConfig.resetTileSelection();
        ZeeConfig.gameUI.map.showgrid(false);
        // TODO exit thread piler stop all activity and cleanup
    }

    static ZeeThread threadAreaPiler;
    static ZeeThread areaPilerStart() {
        if (selAreaPileGobItem==null){
            println("areaPilerStart > selAreaPileGobItem null");
            return null;
        }
        ZeeConfig.gameUI.map.showgrid(false);
        threadAreaPiler = new ZeeThread(){
            public void run() {
                try{
                    String itemGobName = selAreaPileGobItem.getres().name;
                    String itemInvName = "gfx/invobjs/" + selAreaPileGobItem.getres().basename();
                    Coord olStart = ZeeConfig.lastSavedOverlayStartCoord;
                    Coord olEnd = ZeeConfig.lastSavedOverlayEndCoord;
                    int minX,maxX,minY,maxY;
                    minX = Math.min(olStart.x,olEnd.x);
                    minY = Math.min(olStart.y,olEnd.y);
                    maxX = Math.max(olStart.x,olEnd.x);
                    maxY = Math.max(olStart.y,olEnd.y);
                    Gob latestPile = null;

                    // get border tiles for placing piles
                    List<Coord> borderTiles = getPilesTiles(olStart,olEnd);
                    if (borderTiles.size()==0){
                        println("no pile tiles found");
                        return;
                    }

                    ZeeConfig.addPlayerText("pilan");

                    // create pile(s) at border tiles
                    while(selAreaPile && borderTiles.size() > 0) {

                        //pickup items until idle
                        Gob closestItem = ZeeConfig.getClosestGobByNameContains(itemGobName);
                        if (closestItem==null){
                            break;
                        }
                        ZeeManagerGobClick.gobClick(closestItem,3,UI.MOD_SHIFT);
                        waitInvIdleMs(1000);
                        if (!selAreaPile){//user closed piler window?
                            break;
                        }

                        //move to center tile before creating pile
                        ZeeConfig.moveToAreaCenter(ZeeConfig.lastSavedOverlay.a);
                        waitPlayerIdleVelocity();

                        // hold item at vhand
                        if (!ZeeConfig.isPlayerHoldingItem()) {
                            List<WItem> items = ZeeConfig.getMainInventory().getWItemsByName(itemInvName);
                            if (items==null || items.size()==0){
                                println("no more inventory items to pile");
                                continue;
                            }
                            ZeeManagerItemClick.pickUpItem(items.get(0));
                        }

                        // use latest pile
                        if (latestPile!=null){
                            ZeeManagerGobClick.itemActGob(latestPile,UI.MOD_SHIFT);
                            waitPlayerIdleVelocity();//wait approach pile
                            sleep(500);//wait transf items
                            List<WItem> invItems = ZeeConfig.getMainInventory().getWItemsByName(itemInvName);
                            if (invItems!=null && invItems.size()>0){
                                println("latest pile is full");
                                if (latestPile!=null)
                                    ZeeConfig.removeGobText(latestPile);
                                latestPile = null;
                                //continue;
                            }
                        }
                        //new pile
                        else {

                            areaPilerCreateNewPile(borderTiles,itemGobName,minX,minY);

                            if (borderTiles.isEmpty()) {
                                println("out of tiles 2");
                                break;
                            }

                            //update next pile
                            if (latestPile!=null)
                                ZeeConfig.removeGobText(latestPile);
                            latestPile = ZeeConfig.getClosestGobByNameContains("/stockpile-");
                            ZeeConfig.addGobText(latestPile,"pile");
                        }
                    }

                    // pile remaining inv items
                    List<WItem> items = ZeeConfig.getMainInventory().getWItemsByName(itemInvName);
                    if (items.size() > 0){
                        println("pile remaining inv items");
                        ZeeConfig.moveToAreaCenter(ZeeConfig.lastSavedOverlay.a);
                        waitPlayerIdleVelocity();
                        ZeeManagerItemClick.pickUpItem(items.get(0));
                        areaPilerCreateNewPile(borderTiles,itemGobName,minX,minY);
                    }

                    ZeeConfig.removePlayerText();

                    if (latestPile!=null)
                        ZeeConfig.removeGobText(latestPile);

                }catch (Exception e){
                    e.printStackTrace();
                    ZeeConfig.removePlayerText();
                }

                //destroy area selection
                selAreaPile = false;
                ZeeConfig.gameUI.map.uimsg("sel",0);
                ZeeConfig.resetTileSelection();

                //close window
                if (selAreaWindow!=null)
                    selAreaWindow.destroy();
            }
        };
        threadAreaPiler.start();
        return threadAreaPiler;
    }

    private static void areaPilerCreateNewPile(List<Coord> borderTiles, String itemGobName, int minX, int minY) throws InterruptedException {
        // get non farming tile (gfx/tiles/field)
        Coord nonFarmingTile = Coord.of(minX - 1, minY - 1);
        String tileResName;
        do{
            nonFarmingTile = nonFarmingTile.sub(1,1);
            tileResName = ZeeConfig.getTileResName(nonFarmingTile);
        }while(tileResName.contentEquals("gfx/tiles/field"));
        if (tileResName.isBlank()){
            println("blank tile");
            nonFarmingTile = borderTiles.get(0);
        }

        //rclick nonfarming tile to create virtual pile
        ZeeConfig.gameUI.map.wdgmsg("itemact", nonFarmingTile, ZeeConfig.tileToCoord(nonFarmingTile), 0);
        sleep(500);

        // try placing pile until run out of border tiles
        areaPilerTryPlacingOnTiles(borderTiles,itemGobName);
    }

    private static void areaPilerTryPlacingOnTiles(List<Coord> borderTiles, String itemGobName) {
        do {
            //get new tile
            if (borderTiles.isEmpty()) {
                println("out of border tiles for placing piles");
                break;
            }
            Coord coordNewPile = borderTiles.remove(0);

            //reserve space for big piles
            if (itemGobName.contains("/straw")) {
                borderTiles.remove(0);
                if (itemGobName.contains("/board-"))
                    borderTiles.remove(0);
            }

            //try placing pile
            Coord playerTile = ZeeConfig.getPlayerTile();
            ZeeConfig.gameUI.map.wdgmsg("place", ZeeConfig.tileToCoord(coordNewPile), 16384, 1, UI.MOD_SHIFT);
            waitPlayerIdleFor(1);

            //player didnt move? tile occupied, terrain not flat...
            if (playerTile.compareTo(ZeeConfig.getPlayerTile()) == 0){
                println("tile can't be used for pile, trying next");
            }else{
                //tile free, pile placed?
                break;
            }

        }while (!borderTiles.isEmpty());
    }

    static List<Coord> getPilesTiles(Coord olStart, Coord olEnd) {
        List<Coord> listRowTiles = new ArrayList<>();
        int minX,maxX,minY,maxY;
        minX = Math.min(olStart.x,olEnd.x);
        minY = Math.min(olStart.y,olEnd.y);
        maxX = Math.max(olStart.x,olEnd.x);
        maxY = Math.max(olStart.y,olEnd.y);
        //top row tiles
        for (int i = minX-1; i <= maxX+1; i++) {
            listRowTiles.add(Coord.of(i,minY-1));
        }
        //bottom row tiles
        for (int i = minX-1; i <= maxX+1; i++) {
            listRowTiles.add(Coord.of(i,minY+1));
        }
        return listRowTiles;
    }

    static boolean isTileInsideArea(Coord gobCoord, Coord olStart, Coord olEnd) {
        int minX,maxX,minY,maxY;
        minX = Math.min(olStart.x,olEnd.x);
        minY = Math.min(olStart.y,olEnd.y);
        maxX = Math.max(olStart.x,olEnd.x);
        maxY = Math.max(olStart.y,olEnd.y);
        return (gobCoord.x >= minX && gobCoord.x <= maxX && gobCoord.y >= minY && gobCoord.y <= maxY);
    }
}

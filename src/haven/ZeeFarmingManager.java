package haven;

import java.util.HashMap;
import java.util.List;

public class ZeeFarmingManager extends ZeeThread{

    public static long IDLE_MS = 2222;
    public static final int MAX_BARREL_DIST = 300;
    public static final String CURSOR_HARVEST = "gfx/hud/curs/harvest";
    public static Gob lastBarrel;
    public static boolean busy;
    public static String gItemName, lastItemName;
    public static GItem gItem;
    public static WItem wItem;
    public static Inventory inv;
    public static boolean isHarvestDone, isPlantingDone, isScytheEquiped;
    public static HashMap<Gob,Integer> mapBarrelSeedql = new HashMap<Gob,Integer> ();

    public ZeeFarmingManager(GItem g, String name) {
        busy = true;
        gItem = g;
        gItemName = name;
        if(!name.equals(lastItemName)) {
            println(">new crop name, forget last barrel ("+lastBarrel+")");
            lastBarrel = null;
            mapBarrelSeedql.clear();
        }else{
            println(">same crop name, use last barrel ("+lastBarrel+")");
        }
        lastItemName = name;
        inv = (Inventory) gItem.parent;
        wItem = inv.getWItemByGItem(gItem);
    }

    public static void resetInitialState() {
        println(">reset initial state");
        busy = false;
        isHarvestDone = false;
        isPlantingDone = false;
        ZeeConfig.resetTileSelection();
        ZeeConfig.autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);;
    }

    private void updateWItem() {
        wItem = inv.getWItemsByName(gItemName).get(0);
        gItem = wItem.item;
    }

    public void run(){
        ZeeConfig.autoHearthOnStranger = false;

        println("busy="+busy+" , harvestIsDone="+ isHarvestDone +" , plantingIsDone="+ isPlantingDone+" , autoHearthOnStranger="+ZeeConfig.autoHearthOnStranger);

        try{

            isHarvestDone = false;
            while(busy && !isHarvestDone) {
                println("> harvesting");
                waitPlayerIdleFor(2000);
                if(inventoryHasSeeds()) {
                    //storeSeedsByQuality();
                    storeSeedsInBarrel(1);
                    if(!isInventoryFull()) {
                        harvestPlants();
                    }else {
                        println("harvest done, inv full");
                        isHarvestDone = true;
                    }
                }else{
                    isHarvestDone = true;
                }
            }

            isPlantingDone = false;
            isScytheEquiped = ZeeClickItemManager.isItemEquipped("scythe");
            while(busy && !isPlantingDone) {
                println("> planting");
                if (!inventoryHasSeeds()) {
                    if(getSeedsFromBarrel()) {
                        plantSeeds();
                    }else {
                        println("planting done, seed barrel empty");
                        isPlantingDone = true;
                    }
                }else{
                    isPlantingDone = true;
                }
                waitPlayerIdleFor(2000);
            }

            println("> final store barrel");
            storeSeedsInBarrel(1);
            println(">exit");

        }catch (Exception e){
            e.printStackTrace();
        }
        resetInitialState();
    }

    //TODO: consider multiple barrels from mapBarrelSeedql
    private boolean getSeedsFromBarrel() {
        try {
            if (lastBarrel == null) {
                println("getSeedsFromBarrel() > no lastBarrel defined");
                return false;
            }

            //remove harvest cursor
            if(ZeeConfig.getCursorName().equals(CURSOR_HARVEST)){
                println("remove cursor harvest");
                ZeeClickGobManager.gobClick(lastBarrel,3);
            }

            // shift+rclick last barrel
            println("> get seeds from barrel");
            ZeeClickGobManager.gobItemAct(lastBarrel, UI.MOD_SHIFT);

            //wait getting to the barrel
            waitPlayerIdleFor(2000);

            while (!ZeeClickGobManager.isBarrelEmpty(lastBarrel) && !isInventoryFull()) {
                ZeeClickGobManager.gobClick(lastBarrel, 3, UI.MOD_SHIFT);
                //TODO: waitInvCHanges
                Thread.sleep(PING_MS);
            }

            if (!isInventoryFull()) {
                println("inv not full, barrel is empty?");
                lastBarrel = null;
                if (inventoryHasSeeds()) {
                    println("got last seeds from barrel");
                    return true;
                } else {
                    println("got no seeds from barrel");
                    return false;
                }
            }else{
                println("inv full from barrel");
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void storeSeedsByQuality() {
        try {
            List<Gob> emptyBarrels = getEmptyCloseBarrels();
            if (emptyBarrels.size() == 0) {
                ZeeConfig.gameUI.msg("No empty barrels close, dropping seeds.");
                println("No empty barrels close, dropping seeds.");
                inv.dropItemsByName(lastItemName);
            } else {
                println("barrels = " + emptyBarrels.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean isInventoryFull() {
        return inv.getNumberOfFreeSlots() == 0;
    }

    private boolean plantSeeds() {
        updateWItem();
        if(activateCursorPlantGItem(gItem)) {
            /*
            if(isScytheEquiped){
                println("> expand planting tile selection");
                // expand tile selection by 1 tile, due to scythe's overharvesting
                println("before  sc=" + ZeeConfig.savedTileSelStartCoord + "  ec=" + ZeeConfig.savedTileSelEndCoord);
                ZeeConfig.expandTileSelectionBy(1);
                println("after  sc=" + ZeeConfig.savedTileSelStartCoord + "  ec=" + ZeeConfig.savedTileSelEndCoord);
            }
             */
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
            return true;
        }
        return false;
    }

    private boolean harvestPlants() {
        if(activateCursorHarvestGob()) {
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
            return true;
        }else {
            println("out of plants to harvest");
            return false;
        }
    }

    private long now() {
        return System.currentTimeMillis();
    }

    public static int getSeedsAmount(GItem gItem) {
        ItemInfo[] info = gItem.info().toArray(new ItemInfo[0]);
        for (ItemInfo i: info) {
            if(i instanceof GItem.Amount)
                return ((GItem.Amount) i).itemnum();
        }
        return 0;
    }

    public int getTotalSeedAmount(){
        int ret = 0;
        WItem[] arr = inv.getWItemsByName(gItemName).toArray(new WItem[0]);
        for (int i = 0; i < arr.length ; i++) {
            ret += getSeedsAmount(arr[i].item);
        }
        return ret;
    }

    private boolean activateCursorHarvestGob() {
        //println("find gobs named "+ZeeConfig.lastMapViewClickGobName);
        List<Gob> plants = ZeeConfig.findGobsByName(ZeeConfig.lastMapViewClickGobName);
        if (plants.size()==0) {
            return false;
        }
        plants.removeIf(plt -> ZeeConfig.getPlantStage(plt) == 0);
        Gob g = ZeeConfig.getClosestGob(plants);
        //println("closest "+g+" , stage "+ZeeConfig.getPlantStage(g));
        if(g!=null) {
            ZeeClickGobManager.gobClick(g, 3, UI.MOD_SHIFT);
            return waitCursor(CURSOR_HARVEST);
        }else {
            println("no gobs to shift+click");
            return false;
        }
    }

    private boolean activateCursorPlantGItem(GItem gi) {
        //haven.GItem@3a68ee9c ; iact ; [(23, 16), 1]
        ZeeClickItemManager.gItemAct(gi, UI.MOD_SHIFT);
        return waitCursor(CURSOR_HARVEST);
    }

    private boolean inventoryHasSeeds() {
        return inv.getWItemsByName(lastItemName).size() > 0;
    }

    private int getNumberOfSeedItems(){
        return inv.getWItemsByName(lastItemName).size();
    }

    private List<Gob> getEmptyCloseBarrels() {
        List<Gob> emptyBarrels = ZeeConfig.findGobsByName("barrel");
        emptyBarrels.removeIf(gob -> ZeeConfig.distanceToPlayer(gob) > MAX_BARREL_DIST); //remove distant barrels
        emptyBarrels.removeIf(gob -> !ZeeClickGobManager.isBarrelEmpty(gob)); //remove non-empty barrels
        return emptyBarrels;
    }

    private void storeSeedsInBarrel(double percSeedsToStore) {
        try {
            List<Gob> emptyBarrels = getEmptyCloseBarrels();
            println("barrels = " + emptyBarrels.size());
            if (emptyBarrels.size() == 0) {
                /*
                    no barrels, drop seeds
                 */
                ZeeConfig.gameUI.msg("No empty barrels close, dropping seeds.");
                println("No empty barrels close, dropping seeds.");
                inv.dropItemsByName(lastItemName);

            } else {
                /*
                    find barrel and store seeds
                 */
                println(">choose barrel");
                if (lastBarrel == null) { // find empty barrel?
                    lastBarrel = ZeeConfig.getClosestGob(emptyBarrels);
                }
                updateWItem();
                ZeeClickItemManager.pickUpItem(wItem);
                ZeeClickGobManager.gobItemAct(lastBarrel, UI.MOD_SHIFT);
                waitPlayerMove();
                waitPlayerStop();
                Thread.sleep(PING_MS);//wait storing seed
                if (ZeeConfig.isPlayerHoldingItem()) {
                    if(percSeedsToStore==1) {
                        // store all seeds (ctrl+shift)
                        println("store all seeds (ctrl+shift)");
                        ZeeClickGobManager.gobItemAct(lastBarrel, 3);
                    }else{
                        // store percentage of seed items
                        int numSeedItems = getNumberOfSeedItems();
                        int part = (int) (numSeedItems * percSeedsToStore);
                        println("store perc seeds "+part+"/"+numSeedItems);
                        for (int i=0; i<part ; i++) {
                            ZeeClickGobManager.gobItemAct(lastBarrel, UI.MOD_SHIFT);
                            Thread.sleep(PING_MS);//wait storing seed
                        }
                        if(ZeeConfig.gameUI.vhand!=null)//store seed in hand
                            ZeeClickGobManager.gobItemAct(lastBarrel, 0);
                    }
                    waitFreeHand();
                    if (ZeeConfig.isPlayerHoldingItem()) { //barrel full?
                        ZeeConfig.gameUI.msg("Barrel full or player blocked?");
                        println("barrel full");
                        lastBarrel = null;
                        println("lastBarrel = null");
                    } else {
                        println("seeds stored");
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

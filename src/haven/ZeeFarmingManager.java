package haven;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZeeFarmingManager extends ZeeThread{

    public static long IDLE_MS = 2222;
    public static final String CURSOR_HARVEST = "gfx/hud/curs/harvest";
    public static final int MAX_BARREL_DIST = 300;
    public static final double TILE_SIZE = MCache.tilesz.x;
    public static int tiles2Barrel = 0;
    public static Gob lastBarrel;
    public static boolean busy;
    public static String gItemName, lastItemName;
    public static GItem gItem;
    public static WItem wItem;
    public static Inventory inv;
    public static boolean isHarvestDone, isPlantingDone, isScytheEquiped;
    public static HashMap<Gob,Integer> mapBarrelSeedql = new HashMap<Gob,Integer> ();
    public static Window windowManager;
    public static boolean windowCheckboxHarvest, windowCheckboxPlant;
    public static String windowRadiogroupSeeds;
    public static TextEntry windowTxtentryTilesBarrel;


    public ZeeFarmingManager(GItem g, String name) {
        busy = true;
        gItem = g;
        gItemName = name;
        if(!name.equals(lastItemName)) {
            println(">new crop name, forget last barrel ("+lastBarrel+")");
            lastBarrel = null;
            mapBarrelSeedql.clear();
            ZeeConfig.removeGobText(lastBarrel);
        }else{
            println(">same crop name, use last barrel ("+lastBarrel+")");
        }
        lastItemName = name;
        inv = (Inventory) gItem.parent;
        wItem = inv.getWItemByGItem(gItem);
    }

    public ZeeFarmingManager(int tileX) {
        println("marking barrels");
        gItem = null;
        tiles2Barrel = tileX;
    }

    public static void resetInitialState() {
        println(">reset initial state");
        busy = false;
        isHarvestDone = false;
        isPlantingDone = false;
        ZeeConfig.resetTileSelection();
        ZeeConfig.autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);
        ZeeConfig.removeGobText(lastBarrel);
    }

    private void updateWItem() {
        wItem = inv.getWItemsByName(gItemName).get(0);
        gItem = wItem.item;
    }

    public void run(){
        if (gItem==null){
            getEmptyCloseBarrels().forEach(gob -> {
                ZeeConfig.addGobColor(gob,0,255,0,255);
                ZeeConfig.addGobText(gob,"↓",0,255,0,255,10);
            });
        }else{
            autoFarmOld();
        }
    }

    private void autoFarmOld() {
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

            if(waitHoldingItem())//store remaining holding item
                ZeeClickGobManager.gobItemAct(lastBarrel, 0);

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

    //TODO
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
        if (plants.size()==0) {
            return false;
        }
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
        emptyBarrels.removeIf(gob -> ZeeConfig.distanceToPlayer(gob) > (tiles2Barrel==0 ? MAX_BARREL_DIST : tiles2Barrel*TILE_SIZE )); //remove distant barrels
        emptyBarrels.removeIf(gob -> !ZeeClickGobManager.isBarrelEmpty(gob)); //remove non-empty barrels
        return emptyBarrels;
    }

    private void storeSeedsInBarrel(double percSeedsToStore) {
        try {
            List<Gob> emptyBarrels = getEmptyCloseBarrels();
            println("barrels = " + emptyBarrels.size());
            if (emptyBarrels.size()==0) {
                /*
                    no barrels, drop seeds
                 */
                ZeeConfig.gameUI.msg("No empty barrels, lastBarrel null, dropping seeds.");
                println("No empty barrels close, dropping seeds.");
                inv.dropItemsByName(lastItemName);

            } else {
                /*
                    find barrel and store seeds
                 */
                //emptyBarrels.forEach(b -> ZeeConfig.addGobColor(b,0,255,0,255));
                println(">choose barrel");
                if (lastBarrel == null) { // find empty barrel?
                    lastBarrel = ZeeConfig.getClosestGob(emptyBarrels);
                }else{
                    ZeeConfig.removeGobText(lastBarrel);
                }
                ZeeConfig.addGobText(lastBarrel, "SEEDS", 0,255,0,255,10);
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
                        Thread.sleep(PING_MS);
                        if(ZeeConfig.isPlayerHoldingItem())//store seed in hand
                            ZeeClickGobManager.gobItemAct(lastBarrel, 0);
                    }
                    if(waitNotHoldingItem()) {
                        println("seeds stored");
                        ZeeConfig.addGobColor(lastBarrel,0,255,0,255);
                    } else {
                        //still holding item = barrel full?
                        ZeeConfig.gameUI.msg("Barrel full or player blocked?");
                        println("barrel full (lastBarrel=null)");
                        ZeeConfig.removeGobText(lastBarrel);
                        lastBarrel = null;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void showWindow() {
        Widget wdg;
        if(windowManager ==null){
            windowManager = new ZeeWindow(new Coord(300,120), "Farming manager");

            //checkbox harvest
            wdg = windowManager.add(new CheckBox("harvest") {
                {
                    a = ZeeFarmingManager.windowCheckboxHarvest;
                }
                public void set(boolean val) {
                    ZeeFarmingManager.windowCheckboxHarvest = val;
                    a = val;
                }
            }, 0, 0);

            //checkbox plant
            wdg = windowManager.add(new CheckBox("plant") {
                {
                    a = ZeeFarmingManager.windowCheckboxPlant;
                }
                public void set(boolean val) {
                    ZeeFarmingManager.windowCheckboxPlant = val;
                    a = val;
                }
            }, 0, 15);

            // radio drop store
            RadioGroup grp = new RadioGroup(windowManager) {
                public void changed(int opt, String lbl) {
                    ZeeFarmingManager.windowRadiogroupSeeds = lbl;
                }
            };
            wdg = windowManager.add(new Label("Seeds: "), 0, 30);
            wdg = grp.add("store", new Coord(37, 30));
            wdg = grp.add("drop", new Coord(85, 30));

            // barrel tiles
            wdg = windowManager.add(new Label("Max tiles to barrel: "), 0, 50);
            ZeeFarmingManager.windowTxtentryTilesBarrel = new TextEntry(UI.scale(45),""+(int)(ZeeFarmingManager.MAX_BARREL_DIST/MCache.tilesz.x)){
                public boolean keydown(KeyEvent e) {
                    if(!Character.isDigit(e.getKeyChar()) && !ZeeConfig.isControlKey(e.getKeyCode()))
                        return false;
                    return super.keydown(e);
                }
            };
            wdg = windowManager.add(ZeeFarmingManager.windowTxtentryTilesBarrel, 95, 50-3);
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(45),"test"), 145,50-4);

            //start button
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(45),"start"),300-40, 120-20);
            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));
        }else {
            windowManager.show();
        }
    }

}

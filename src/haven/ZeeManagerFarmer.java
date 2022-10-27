package haven;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZeeManagerFarmer extends ZeeThread{

    public static final int MIN_ACCESSIBLE_DIST = 15;//TODO: isPlayerMountingHorse()
    public static final int MAX_BARREL_DIST = 300;
    public static Gob lastBarrel;
    public static boolean busy;
    public static String gItemSeedBasename, lastItemSeedBasename;
    public static GItem gItem;
    public static WItem wItem;
    public static Inventory inv;
    public static boolean isHarvestDone, isPlantingDone;
    public static Window windowManager;
    public static ZeeManagerFarmer manager;

    public static boolean farmerCbReplant = Utils.getprefb("farmerCbPlant",false);
    public static boolean farmerCbPile = Utils.getprefb("farmerCbPile",false);
    public static int farmerTxtTilesBarrel = Utils.getprefi("farmerTxtTilesBarrel",27);
    public static int farmerTxtQlSortingBarrels = Utils.getprefi("farmerTxtQlSortingBarrels",4);
    public static TextEntry textEntryTilesBarrel, textEntryQlSortBarrels;
    public static Gob farmerGobCrop;


    public ZeeManagerFarmer(GItem g, String nameSeed) {
        busy = true;
        gItem = g;
        gItemSeedBasename = nameSeed;// "seed-turnip"
        if(!nameSeed.equals(lastItemSeedBasename)) {
            //println(">new crop name, forget last barrel ("+lastBarrel+")");
            lastBarrel = null;
            ZeeConfig.removeGobText(lastBarrel);
        }
        lastItemSeedBasename = nameSeed;
        wItem = ZeeConfig.getMainInventory().getWItemByGItem(gItem);
    }

    public void run(){
        manager = this;
        startFarming();
    }

    public static void resetInitialState() {
        println("seedFarmer exit > resetInitialState");
        try {
            busy = false;
            isHarvestDone = true;
            isPlantingDone = true;
            droppedSeeds = 0;
            ZeeConfig.resetTileSelection();
            ZeeConfig.autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);
            if (manager!=null)
                manager.interrupt();

            //piler
            ZeeManagerStockpile.selAreaPile = false;

            ZeeConfig.removePlayerText();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
        Update seed-pile reference.
        Returns true if pile contains 5+ seeds, else returns false.
     */
    private static boolean updateSeedPileReference() {
        List<WItem> items = ZeeConfig.getMainInventory().getWItemsByName(gItemSeedBasename);
        if (items==null || items.size()==0)
            return false;
        int seeds;
        for (WItem item : items) {
            seeds = getSeedsAmount(item.item);
            if (seeds >= 5){
                // seed-pile big enough for planting
                wItem = item;
                gItem = item.item;
                //println("updateSeed > plantable > "+seeds);
                return true;
            }
        }
        wItem = items.get(0);
        gItem = wItem.item;
        //seeds = getSeedsAmount(gItem);
        //println("updateSeed > not plantable > "+seeds);
        return false;
    }


    public static void testBarrelsTiles(boolean allBarrels) {
        //highlight barrels in range
        List<Gob> barrels = ZeeConfig.findGobsByNameContains("barrel");

        barrels.forEach(gob -> {
            //clear all barrels
            ZeeConfig.removeGobText(gob);
            ZeeConfig.removeGobColor(gob);

            //tag barrels in tile range
            if(allBarrels  ||  ZeeConfig.distanceToPlayer(gob) <= farmerTxtTilesBarrel * TILE_SIZE) {
                if (!ZeeManagerGobClick.isBarrelEmpty(gob)) {
                    ZeeConfig.addGobText(gob, ZeeConfig.getBarrelOverlayBasename(gob));
                }
                ZeeConfig.addGobColor(gob, 0, 255, 0, 255);
            }
        });
    }

    public static void testBarrelsTilesClear() {
        //highlight barrels in range
        List<Gob> barrels = ZeeConfig.findGobsByNameContains("barrel");

        barrels.forEach(gob -> {
            //clear all barrels
            ZeeConfig.removeGobText(gob);
            ZeeConfig.removeGobColor(gob);
        });
    }

    private void startFarming() {
        ZeeConfig.autoHearthOnStranger = false;
        mapSeedqlBarrel = new HashMap<>();
        try{

            /*
                harvest stage
             */
            isHarvestDone = false;
            while(busy && !isHarvestDone) {
                //println("> harvesting loop");
                ZeeConfig.addPlayerText("harvesting");
                //waitPlayerIdleFor(2);//already farming
                waitPlayerIdleVelocityMs(999);
                if(inventoryHasSeeds()) {
                    storeSeedsBarrelByQlRecursive();
                    if(!isInventoryFull()) {
                        harvestPlants();
                    }else {
                        println("harvest done, inv full, out of barrels?");
                        isHarvestDone = true;
                    }
                }else{
                    isHarvestDone = true;
                }
            }

            /*
                planting stage
             */
            if(farmerCbReplant) {
                Gob highestBarrel = getHighestQlBarrel();
                if (highestBarrel == null) {
                    isPlantingDone = true;
                    println("planting abort: cant find highest ql barrel");
                }else {
                    isPlantingDone = false;
                    ZeeManagerItemClick.equipTwoSacks();
                }
                while (busy && !isPlantingDone) {
                    println("> planting loop");
                    if (getInvTotalSeedAmount() < 5) {
                        println("total seeds < 5, get from barrels");
                        centerFarmingArea();//center before getting barrel seeds
                        if(!getSeedsFromBarrel(highestBarrel)){//if (!getSeedsFromMultipleBarrels(gItemSeedBasename)) {
                            println("planting done, out of seeds");
                            isPlantingDone = true;
                            break;
                        }
                    }
                    plantSeeds();
                    waitPlayerIdleFor(3);
                    //waitPlayerIdleVelocityMs(2000);
                    //println("planting stopped?");
                    if(getInvTotalSeedAmount() >= 5) {
                        // player idle, 5+ seeds
                        if (getNumberOfSeedItems() == 1) {
                            // player idle, 5+ seeds, 1 seed-pile, area is fully planted
                            println("planting done, 1 plantable seedpile");
                            isPlantingDone = true;
                            break;
                        }else{
                            // player idle, 5+ seeds, 2+ seed-piles, area may still need planting
                            int plantablePiles = getNumPlantableSeedPiles();
                            if (plantablePiles == 0) {
                                println("no plantable piles, store and try again ");
                                storeSeedsBarrelByQlRecursive(); //store and try planting one more time
                            }else{
                                println("planting done, 1+ plantable seedpiles");
                                isPlantingDone = true;
                                break;
                            }
                        }
                    }
                    //else restart plant loop
                }
            }

            //final store barrel
            storeSeedsBarrelByQlRecursive();


           /*
                piling stage
            */
            if (farmerCbPile){
                ZeeManagerItemClick.equipTwoSacks();
                ZeeManagerStockpile.selAreaPile = true;
                ZeeManagerStockpile.selAreaPileGobItem = getPileItemGob();
                ZeeThread t = ZeeManagerStockpile.areaPilerStart();
                if (t!=null)
                    t.join();//wait pile thread finish
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        resetInitialState();
    }

    private Gob getHighestQlBarrel() {
        if (mapSeedqlBarrel.size()==0)
            return null;
        Map.Entry<Integer, Gob> highest = mapSeedqlBarrel.entrySet().iterator().next();
        for (Map.Entry<Integer, Gob> entry : mapSeedqlBarrel.entrySet()) {
            if (entry.getKey() > highest.getKey())
                highest = entry;
        }
        return highest.getValue();
    }

    static Gob getPileItemGob() {
        /*
            TODO: include vegetables beyond seeds
            seed-turnip,seed-carrot,seed-leek,seed-cucumber,
            seed-pumpkin,beetroot"
         */

        String pileItemName = "";

        if (lastItemSeedBasename.contains("seed-flax"))
            pileItemName = "/flaxfibre";
        else if (lastItemSeedBasename.contains("seed-hemp"))
            pileItemName = "/hempfibre";
        else if (lastItemSeedBasename.contains("seed-poppy"))
            pileItemName = "/flower-poppy";
        else if (lastItemSeedBasename.contains("seed-pipeweed"))
            pileItemName = "/tobacco-fresh";
        else if (lastItemSeedBasename.contains("seed-barley")
                || lastItemSeedBasename.contains("seed-wheat")
                || lastItemSeedBasename.contains("seed-millet"))
            pileItemName = "/straw";
        else if (lastItemSeedBasename.contains("seed-lettuce"))
            pileItemName = "/lettuce";//TODO check lettuce-head name


        return ZeeConfig.getClosestGobByNameContains(pileItemName);
    }

    private int getNumPlantableSeedPiles() {
        List<WItem> plantablePiles = ZeeConfig.getMainInventory().getWItemsByName(gItemSeedBasename);
        plantablePiles.removeIf(w -> (getSeedsAmount(w.item) < 5));
        return plantablePiles.size();
    }

    public static boolean isBarrelAccessible(Gob barrel) {
        ZeeManagerGobClick.gobClick(barrel, 3);
        return waitPlayerDistToGob(barrel,15);//waitPlayerIdleFor(2);
    }

    public static boolean getSeedsFromBarrel(Gob barrel) throws InterruptedException {
        ZeeConfig.addPlayerText("get barrel seeds");
        if(!isBarrelAccessible(barrel)){
            markBarrelInaccessible(barrel);
            ZeeConfig.removePlayerText();
            return false;
        }
        while(!isBarrelEmpty(barrel) && !isInventoryFull()){
            ZeeManagerGobClick.gobClick(barrel, 3, UI.MOD_SHIFT);
            //TODO: waitInvItemAddedOrChanged
            Thread.sleep(PING_MS*2);
        }
        if(waitHoldingItem(4000)) {//store remaining holding item
            ZeeManagerGobClick.itemActGob(barrel, 0);
            Thread.sleep(1000);
        }
        ZeeConfig.removePlayerText();
        //println("getSeedsFromBarrel() > holding item = "+ZeeConfig.isPlayerHoldingItem());
        return getInvTotalSeedAmount() >= 5;
    }


    public static List<Gob> getAllSeedBarrels(String seedBaseName) {
        //println(">get all seed barrels");
        List<Gob> barrels = getAccessibleBarrels();
        barrels.removeIf(b -> {
            if (ZeeManagerGobClick.isBarrelEmpty(b))
                return true;
            if (!isBarrelSameSeeds(b, seedBaseName))
                return true;
            return false;
        });
        return barrels;
    }

    public static boolean isInventoryFull() {
        return ZeeConfig.getMainInventory().getNumberOfFreeSlots() == 0;
    }

    private boolean plantSeeds() {
        ZeeConfig.addPlayerText("planting");
        if(updateSeedPileReference() && activateCursorPlantGItem(gItem)) {
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.lastSavedOverlayStartCoord, ZeeConfig.lastSavedOverlayEndCoord, ZeeConfig.lastSavedOverlayModflags);
            return true;
        }else{
            println("could not plant seed (small seedpiles?)");
        }
        return false;
    }

    private boolean harvestPlants() {
        ZeeConfig.addPlayerText("harvesting");
        if(activateCursorHarvestGob()) {
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.lastSavedOverlayStartCoord, ZeeConfig.lastSavedOverlayEndCoord, ZeeConfig.lastSavedOverlayModflags);
            return true;
        }else {
            //println("out of plants to harvest");
            return false;
        }
    }

    public static int getSeedsAmount(GItem gItem) {
        ItemInfo[] info = gItem.info().toArray(new ItemInfo[0]);
        for (ItemInfo i: info) {
            if(i instanceof GItem.Amount)
                return ((GItem.Amount) i).itemnum();
        }
        return 0;
    }

    public static int getInvTotalSeedAmount(){
        int ret = 0;
        WItem[] arr = ZeeConfig.getMainInventory().getWItemsByName(gItemSeedBasename).toArray(new WItem[0]);
        for (int i = 0; i < arr.length ; i++) {
            ret += getSeedsAmount(arr[i].item);
        }
        println("tot seed "+ret);
        return ret;
    }

    public static boolean activateCursorHarvestGob() {
        List<Gob> plants = ZeeConfig.findGobsByNameContains(ZeeConfig.lastMapViewClickGobName);
        if (plants.size()==0) {
            return false;
        }
        plants.removeIf(plt -> ZeeConfig.getPlantStage(plt) == 0  &&  !ZeeConfig.lastMapViewClickGobName.contains("/fallowplant"));
        if (plants.size()==0) {
            //println("no plants to click");
            return false;
        }
        Gob g = ZeeConfig.getClosestGob(plants);
        if(g!=null) {
            ZeeManagerGobClick.gobClick(g, 3, UI.MOD_SHIFT);
            return waitCursorName(ZeeConfig.CURSOR_HARVEST);
        }else {
            //println("no gobs to shift+click");
            return false;
        }
    }

    public static boolean activateCursorPlantGItem(GItem gi) {
        //haven.GItem@3a68ee9c ; iact ; [(23, 16), 1]
        //println("activateCursorPlantGItem > "+gi+", seeds = "+getSeedsAmount(gi));
        ZeeManagerItemClick.gItemActCoord(gi, UI.MOD_SHIFT);
        return waitCursorName(ZeeConfig.CURSOR_HARVEST);
    }

    private boolean inventoryHasSeeds() {
        return ZeeConfig.getMainInventory().getWItemsByName(gItemSeedBasename).size() > 0;
    }

    private int getNumberOfSeedItems(){
        return ZeeConfig.getMainInventory().getWItemsByName(gItemSeedBasename).size();
    }

    public static List<Gob> getAccessibleBarrels() {
        List<Gob> emptyBarrels = ZeeConfig.findGobsByNameContains("barrel");
        emptyBarrels.removeIf(barrel -> {
            if(ZeeConfig.distanceToPlayer(barrel) > farmerTxtTilesBarrel * TILE_SIZE)
                return true;//remove distant barrels
            if(isBarrelMarkedInaccessible(barrel)) {
                return true;//remove inaccessible barrels (red)
            }
            return false;
        });
        return emptyBarrels;
    }

    public static boolean isBarrelMarkedInaccessible(Gob barrel) {
        ZeeGobColor c = barrel.getattr(ZeeGobColor.class);
        if(c!=null && c.color.color().getRed()==1) {
            return true;
        }
        return false;
    }

    public static boolean isBarrelMarkedFull(Gob barrel) {
        ZeeGobColor c = barrel.getattr(ZeeGobColor.class);
        if(c!=null && c.color.color().getBlue()==1) {
            return true;
        }
        return false;
    }

    //TODO remove
    static Coord testCoordCenter;
    public static void testStoringBarrelQl(Gob seedPile) {
        new ZeeThread(){
            public void run() {
                try {
                    mapSeedqlBarrel = new HashMap<>();
                    testCoordCenter = ZeeConfig.getGobTile(seedPile);
                    //pickup seed piles until idle
                    ZeeManagerGobClick.gobClick(seedPile,3,UI.MOD_SHIFT);
                    waitInvIdleMs(777);
                    //seed vars
                    busy = true;
                    wItem = ZeeConfig.getMainInventory().getWItemsByName("/seed-").get(0);
                    gItem = wItem.item;
                    gItemSeedBasename = gItem.getres().basename();
                    //drop if holding item
                    if (ZeeConfig.isPlayerHoldingItem()){
                        ZeeConfig.gameUI.vhand.item.wdgmsg("drop", Coord.z);
                        waitNotHoldingItem();
                    }
                    //store by ql
                    storeSeedsBarrelByQlRecursive();
                }catch (Exception e){
                    e.printStackTrace();
                }
                ZeeConfig.removePlayerText();
                busy = false;
            }
        }.start();
    }

    private static void storeSeedsBarrelByQlRecursive() {
        try {
            //move to area center before choosing barrel
            centerFarmingArea();
            List<Gob> barrels = getAccessibleBarrels();
            if (barrels.size()==0) {
                /*
                    no barrels, drop seeds
                 */
                ZeeConfig.gameUI.msg("No empty barrels, lastBarrel null, dropping seeds.");
                ZeeConfig.getMainInventory().dropItemsByName(gItemSeedBasename);

            }
            else {
                /*
                    find barrel and store seeds
                 */
                barrels.removeIf(b -> {
                    ZeeGobColor c = b.getattr(ZeeGobColor.class);
                    if(c!=null && c.color.color().getBlue()==1) {
                        return true;//remove possible marked full(blue) barrels
                    }
                    if (!isBarrelEmpty(b) && !isBarrelSameSeeds(b, gItemSeedBasename)){
                        return true;//remove non-empty, non-matching seed barrels
                    }
                    return false;
                });
                //drop lowest ql seeds, keep highest
                int qlSeeds = dropLowestQlSeeds();
                if (qlSeeds < 0){
                    println("all seeds stored?");
                    //resetInitialState();
                    return;
                }
                //get barrel for current seed ql
                lastBarrel = getBarrelBySeedQl(qlSeeds,barrels);//ZeeConfig.getClosestGob(barrels);
                if(lastBarrel==null){
                    println("cant decide ql barrel");
                    resetInitialState();
                    return;
                }
                ZeeConfig.addPlayerText("storing ql "+qlSeeds);
                sleep(1000);//wait inventory update
                if (!updateSeedPileReference()) {
                    println("storeSeedsInBarrel > updateSeedPileReference > false");
                    return;
                }
                ZeeConfig.addGobText(lastBarrel, getSeedNameAndQl());
                if(!ZeeManagerItemClick.pickUpItem(wItem)){
                    println("couldn't pickup wItem ql "+Inventory.getQualityInt(wItem.item));
                    resetInitialState();
                    return;
                }
                sleep(PING_MS);//lag?
                ZeeManagerGobClick.itemActGob(lastBarrel, UI.MOD_SHIFT);//store first seeds
                sleep(PING_MS);//lag?
                waitPlayerDistToGob(lastBarrel,15);//wait reaching barrel
                sleep(1000);//waitHoldingItemChanged();//wait 1st seed transfer
                if (ZeeConfig.isPlayerHoldingItem()) {//2nd seed transfer?
                    // store all seeds (ctrl+shift)
                    ZeeManagerGobClick.itemActGob(lastBarrel, 3);//3==ctrl+shift
                    if(waitNotHoldingItem()) {
                        //all seeds stored
                        ZeeConfig.addGobText(lastBarrel, getSeedNameAndQl());
                        mapSeedqlBarrel.put(qlSeeds,lastBarrel);
                    } else {
                        //still holding item?
                        if(ZeeConfig.distanceToPlayer(lastBarrel) > MIN_ACCESSIBLE_DIST){
                            //can't reach barrel
                            markBarrelInaccessible(lastBarrel);
                        }else{
                            //barrel full
                            markBarrelFull(lastBarrel);
                        }
                        lastBarrel = null;
                    }
                }else{
                    // all seeds stored
                    ZeeConfig.addGobText(lastBarrel, getSeedNameAndQl());
                    mapSeedqlBarrel.put(qlSeeds,lastBarrel);
                }
            }

            //pickup dropped low q seeds and recall storeSeedsInBarrel
            if (droppedSeeds > 0 && busy){
                Gob seedPile = ZeeConfig.getClosestGobByNameContains("/seeds");
                if (seedPile!=null && ZeeConfig.distanceToPlayer(seedPile) < farmerTxtTilesBarrel * TILE_SIZE) {
                    //pickup seed piles until idle
                    ZeeManagerGobClick.gobClick(seedPile,3,UI.MOD_SHIFT);
                    waitInvIdleMs(777);
                    //drop if holding item
                    if (ZeeConfig.isPlayerHoldingItem()){
                        ZeeConfig.gameUI.vhand.item.wdgmsg("drop", Coord.z);
                        waitNotHoldingItem();
                    }
                    //recursive call
                    println(" > recursive storeSeedsInBarrel");
                    storeSeedsBarrelByQlRecursive();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            println("dropSeeds reset");
            droppedSeeds = 0;
        }
    }

    private static void centerFarmingArea() {
        ZeeConfig.addPlayerText("centering");
        ZeeConfig.clickTile(ZeeConfig.getAreaCenterTile(ZeeConfig.lastSavedOverlay.a),1);
        waitPlayerIdleVelocity();//waitPlayerIdleFor(1);
    }

    static Map<Integer,Gob> mapSeedqlBarrel;
    private static Gob getBarrelBySeedQl(int qlSeeds, List<Gob> barrels) {
        println("qlSeeds "+qlSeeds+" , mapQlBarrel "+mapSeedqlBarrel.size()+" = "+ mapSeedqlBarrel.keySet());
        Gob barrel = null;
        // first barrel, get closest empty one
        if (mapSeedqlBarrel.size()==0) {
            barrel = getClosestEmptyBarrel();
        }
        // ql barrel not found
        else if(mapSeedqlBarrel.get(qlSeeds)==null) {
            //if max barrels not reached, get closest empty one
            if (mapSeedqlBarrel.size() < farmerTxtQlSortingBarrels) {
                barrel = getClosestEmptyBarrel();
            }
            // if max barrels reached, add lower seeds to lower barrel
            else {
                Map.Entry<Integer, Gob> lowestQlEntry = mapSeedqlBarrel.entrySet().iterator().next();
                for (Map.Entry<Integer, Gob> entry : mapSeedqlBarrel.entrySet()) {
                    if (entry.getKey() < lowestQlEntry.getKey())
                        lowestQlEntry = entry;
                }
                println("max barrel reached, store on lower barrel = "+lowestQlEntry.getKey());
                barrel = lowestQlEntry.getValue();
            }
        }else{
            barrel = mapSeedqlBarrel.get(qlSeeds);
        }
        return barrel;
    }

    static Gob getClosestEmptyBarrel() {
        List<Gob> barrels = ZeeConfig.findGobsByNameEndsWith("/barrel");
        return ZeeConfig.getClosestGob(barrels.stream().filter(ZeeManagerFarmer::isBarrelEmpty).collect(Collectors.toList()));
    }

    static int droppedSeeds = 0;
    private static int dropLowestQlSeeds(){
        List<WItem> seedItems = ZeeConfig.getMainInventory().getWItemsByName(gItemSeedBasename);
        if (seedItems.size()==0)
            return -1;

        //find highest ql
        int highestQl = Inventory.getQualityInt(seedItems.get(0).item);
        int q;
        for (WItem seedItem : seedItems) {
            q = Inventory.getQualityInt(seedItem.item);
            if ( q > highestQl) {
                highestQl = q;
            }
        }

        //drop lower qls
        droppedSeeds = 0;
        for (WItem seedItem : seedItems) {
            if (Inventory.getQualityInt(seedItem.item) < highestQl) {
                seedItem.item.wdgmsg("drop", Coord.z);
                droppedSeeds++;
            }
        }

        //return highest ql found
        return highestQl;
    }

    private static int dropLessCommonSeeds() {
        List<WItem> seedItems = ZeeConfig.getMainInventory().getWItemsByName(gItemSeedBasename);
        if (seedItems.size()==0)
            return -1;
        //count quantity of items for each ql
        Map<Integer,Integer> mapQlQuantity = new HashMap<>();
        int ql;
        for (int i = 0; i < seedItems.size(); i++) {
            ql = Inventory.getQualityInt(seedItems.get(i).item);
            if (!mapQlQuantity.containsKey(ql)){
                mapQlQuantity.put(ql,1);//first item of "ql"
            }else{
                int quantity = mapQlQuantity.get(ql);
                quantity++;
                mapQlQuantity.put(ql,quantity);//increase "ql" count
            }
        }
        //find most common ql
        int qlKeeper = mapQlQuantity.keySet().iterator().next();
        for (Map.Entry<Integer, Integer> entry : mapQlQuantity.entrySet()) {
            if (qlKeeper != entry.getKey()){
                if(entry.getValue() > mapQlQuantity.get(qlKeeper)){
                    qlKeeper = entry.getKey();
                }
            }
        }
        //drop less common ones, keep most common only
        droppedSeeds = 0;
        for (WItem seedItem : seedItems) {
            if (Inventory.getQualityInt(seedItem.item) != qlKeeper) {
                seedItem.item.wdgmsg("drop", Coord.z);
                droppedSeeds++;
            }
        }
        return qlKeeper;
    }

    public static void markBarrelFull(Gob lastBarrel) {
        ZeeConfig.addGobColor(lastBarrel,0,0,255,255);
    }

    public static void markBarrelInaccessible(Gob lastBarrel) {
        ZeeConfig.addGobColor(lastBarrel,255,0,0,255);
    }

    public static boolean isBarrelEmpty(Gob b) {
        return ZeeManagerGobClick.isBarrelEmpty(b);
    }

    public static String getSeedNameAndQl() {
        return gItemSeedBasename.replace("seed-","")
                + " "
                + Inventory.getQualityInt(gItem);
    }

    // gfx/terobjs/barrel-flax
    // gfx/invobjs/seed-flax
    public static boolean isBarrelSameSeeds(Gob barrel, String seedName) {
        String seed = seedName.replace("seed-","");
        if(ZeeManagerGobClick.getOverlayNames(barrel).contains("gfx/terobjs/barrel-" + seed)) {
            //println("same seeds , gfx/terobjs/barrel-" + seed);
            return true;
        }else {
            //println("not same seeds , gfx/terobjs/barrel-" + seed);
            return false;
        }
    }

    public static void showWindow(Gob gobCrop) {
        Widget wdg;
        ZeeManagerFarmer.farmerGobCrop = gobCrop;
        ZeeConfig.farmerMode = true;

        if(windowManager ==null){

            windowManager = new ZeeWindow(new Coord(300,90), "Seed Farmer"){
                @Override
                public void wdgmsg(String msg, Object... args) {
                    if (msg=="close"){
                        ZeeConfig.farmerMode = false;
                        resetInitialState();
                    }
                    super.wdgmsg(msg, args);
                }
            };


            //checkbox replant
            wdg = windowManager.add(new CheckBox("replant highest ql") {
                { a = ZeeManagerFarmer.farmerCbReplant;  }
                public void set(boolean val) {
                    ZeeManagerFarmer.farmerCbReplant = val;
                    a = val;
                    Utils.setprefb("farmerCbPlant",val);
                }
            }, 0, 7);


            //checkbox create piles
            wdg = windowManager.add(new CheckBox("pile around selection") {
                { a = ZeeManagerFarmer.farmerCbPile;  }
                public void set(boolean val) {
                    ZeeManagerFarmer.farmerCbPile = val;
                    a = val;
                    Utils.setprefb("farmerCbPile",val);
                }
            }, wdg.c.x+wdg.sz.x+17, 7);


            // ql sort barrels textEntry
            wdg = windowManager.add(new Label("Ql sorting barrels: "), 0, 30);
            textEntryQlSortBarrels = new TextEntry(UI.scale(45),""){
                public boolean keydown(KeyEvent e) {
                    if(!Character.isDigit(e.getKeyChar()) && !ZeeConfig.isControlKey(e.getKeyCode()))
                        return false;
                    return super.keydown(e);
                }
                public void changed(ReadLine buf) {
                    if(!buf.line().isEmpty()) {
                        farmerTxtQlSortingBarrels = Integer.parseInt(buf.line());
                        Utils.setprefi("farmerTxtQlSortingBarrels", ZeeManagerFarmer.farmerTxtQlSortingBarrels);
                    }
                    super.changed(buf);
                }
            };
            wdg = windowManager.add(ZeeManagerFarmer.textEntryQlSortBarrels, 95, 30-5);
            textEntryQlSortBarrels.settext(""+ farmerTxtQlSortingBarrels);


            // barrel tiles textEntry
            wdg = windowManager.add(new Label("Max tiles to barrel: "), 0, 55);
            textEntryTilesBarrel = new TextEntry(UI.scale(45),""+(int)(MAX_BARREL_DIST/MCache.tilesz.x)){
                public boolean keydown(KeyEvent e) {
                    if(!Character.isDigit(e.getKeyChar()) && !ZeeConfig.isControlKey(e.getKeyCode()))
                        return false;
                    return super.keydown(e);
                }
                public void changed(ReadLine buf) {
                    if(!buf.line().isEmpty()) {
                        farmerTxtTilesBarrel = Integer.parseInt(buf.line());
                        Utils.setprefi("farmerTxtTilesBarrel", farmerTxtTilesBarrel);
                    }
                    super.changed(buf);
                }
            };
            wdg = windowManager.add(textEntryTilesBarrel, 95, 55-5);
            textEntryTilesBarrel.settext(""+ farmerTxtTilesBarrel);


            //barrel tiles test Button
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(45),"test"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("activate")){
                        try {
                            int tiles = Integer.parseInt(textEntryTilesBarrel.text().strip());
                            testBarrelsTiles(false);
                        } catch (NumberFormatException e) {
                            ZeeConfig.msg("numbers only");
                        }
                    }
                }
            }, 145,53-5);


            //barrel tiles clear Button
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(45),"clear"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("activate")){
                        testBarrelsTilesClear();
                    }
                }
            }, wdg.c.x+wdg.sz.x+5,53-5);


            //add bottom note
            wdg = windowManager.add(new Label("No path-finding: clear field obstacles, surround with barrels"), 0, 95-15);


            //add window
            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));

        }else {
            windowManager.show();
        }
    }

}

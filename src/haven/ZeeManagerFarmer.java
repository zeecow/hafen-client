package haven;

import java.util.ArrayList;
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
        startSeedFarming();
    }

    public static void exitSeedFarmer(String msg) {
        println("exitSeedFarmer > "+msg);
        exitSeedFarmer();
    }
    public static void exitSeedFarmer() {
        try {
            ZeeConfig.farmerMode = false;
            busy = false;
            isHarvestDone = true;
            isPlantingDone = true;
            droppedSeeds = 0;
            ZeeConfig.resetTileSelection();
            ZeeConfig.autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);

            //piler
            ZeeManagerStockpile.selAreaPile = false;

            if(windowManager!=null){
                windowManager.reqdestroy();
                windowManager = null;
            }

            if (manager!=null)
                manager.interrupt();

            //TODO test emptying seed barrels queue? (mapQlBarrel)

        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }

    /*
        Update seed-pile reference.
        Returns true if pile contains 5+ seeds, else returns false.
     */
    private static boolean updateSeedPileReference() {
        List<WItem> items = ZeeConfig.getMainInventory().getWItemsByNameContains(gItemSeedBasename);
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
                if (!ZeeManagerGobs.isBarrelEmpty(gob)) {
                    ZeeConfig.addGobText(gob, ZeeConfig.getBarrelOverlayBasename(gob));
                }
                ZeeConfig.addGobColor(gob, 0, 153, 0, 255);
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

    static void sliceAllHarvestedPumpkins(Gob pumpkin){
        new ZeeThread(){
            public void run() {
                final Gob[] nextPump = {pumpkin};
                try {
                    ZeeConfig.addPlayerText("pumpking");
                    Inventory mainInv = ZeeConfig.getMainInventory();
                    prepareCancelClick();
                    do {

                        //pick ground pumpkins
                        ZeeManagerGobs.gobClick(nextPump[0], 3, UI.MOD_SHIFT);
                        waitPlayerIdleLinMove();
                        if (!waitInvIdleMs(777)) {//waitInvIdle
                            println("error waiting picking pumpkins");
                            break;
                        }

                        //drop if holding pumpkin
                        if (ZeeConfig.isPlayerHoldingItem()){
                            ZeeConfig.gameUI.vhand.item.wdgmsg("drop", Coord.z);
                            if(!waitNotHoldingItem()){
                                println("couldnt drop hand pumpking?");
                                break;
                            }
                        }

                        List<WItem> invPumps = mainInv.getWItemsByNameEndsWith("/pumpkin");
                        if (invPumps.isEmpty()){
                            println("no pumpkins in inventory?");
                            break;
                        }

                        //slice inv pumps
                        int slicedPumps = ZeeManagerItems.clickAllItemsPetal(invPumps,"Slice");
                        if (slicedPumps==0)
                            println("no sliced pumpkins?");
                        ZeeConfig.addPlayerText("pumpking");//restore text

                        //wait inv update
                        sleep(500);

                        //drop seeds
                        mainInv.dropItemsByNameEndsWith("/seed-pumpkin");

                        //drop pumpkinflesh
                        mainInv.dropItemsByNameEndsWith("/pumpkinflesh");

                        //define next ground pumpkin
                        Gob next = ZeeConfig.getClosestGobByNameEnds("/items/pumpkin");
                        if (next==null){
                            println("pumpkins done");
                            break;
                        }
                        nextPump[0] = next;

                    } while(!isCancelClick());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    private void startSeedFarming() {
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
                    ZeeManagerItems.equipTwoSacks();
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
                ZeeManagerItems.equipTwoSacks();
                ZeeManagerStockpile.selAreaPile = true;
                ZeeManagerStockpile.selAreaPileGobItem = getPileGroundItemGob();
                ZeeThread t = ZeeManagerStockpile.areaPileAroundStart();
                if (t!=null)
                    t.join();//wait pile thread finish
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        exitSeedFarmer("done");
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

    static Gob getPileGroundItemGob() {
        /*
            TODO: include vegetables beyond seeds
            seed-turnip,seed-carrot,seed-leek,seed-cucumber,
            seed-pumpkin,beetroot"
         */

        String pileItemName = "";

        if (lastItemSeedBasename.contains("seed-flax"))
            pileItemName = "gfx/terobjs/items/flaxfibre";
        else if (lastItemSeedBasename.contains("seed-hemp"))
            pileItemName = "gfx/terobjs/items/hempfibre";
        else if (lastItemSeedBasename.contains("seed-poppy"))
            pileItemName = "gfx/terobjs/items/flower-poppy";
        else if (lastItemSeedBasename.contains("seed-pipeweed"))
            pileItemName = "gfx/terobjs/items/tobacco-fresh";
        else if (lastItemSeedBasename.contains("seed-barley")
                || lastItemSeedBasename.contains("seed-wheat")
                || lastItemSeedBasename.contains("seed-millet"))
            pileItemName = "gfx/terobjs/items/straw";
        else if (lastItemSeedBasename.contains("seed-turnip"))
            pileItemName = "gfx/terobjs/items/turnip";
        else if (lastItemSeedBasename.contains("seed-carrot"))
            pileItemName = "gfx/terobjs/items/carrot";
        else if (lastItemSeedBasename.contains("seed-lettuce"))
            pileItemName = "gfx/terobjs/items/lettucehead";//TODO check lettuce-head name


        return ZeeConfig.getClosestGobByNameContains(pileItemName);
    }

    private int getNumPlantableSeedPiles() {
        List<WItem> plantablePiles = ZeeConfig.getMainInventory().getWItemsByNameContains(gItemSeedBasename);
        plantablePiles.removeIf(w -> (getSeedsAmount(w.item) < 5));
        return plantablePiles.size();
    }

    public static boolean tryAccessingBarrel(Gob barrel) {
        ZeeManagerGobs.gobClick(barrel, 3);
        return waitPlayerDistToGob(barrel,15);//waitPlayerIdleFor(2);
    }

    public static boolean getSeedsFromBarrel(Gob barrel) throws InterruptedException {

        ZeeConfig.addPlayerText("get barrel seeds");

        //move to barrel, if not acessible then mark it so and return
        if(!tryAccessingBarrel(barrel)){
            markBarrelInaccessible(barrel);
            ZeeConfig.removePlayerText();
            return false;
        }

        //take all barrel seeds
        ZeeManagerGobs.barrelTakeAllSeeds(barrel);

        //store remaining holding item
        if(waitHoldingItem()) {
            ZeeManagerGobs.itemActGob(barrel, 0);
            Thread.sleep(1000);
        }

        ZeeConfig.removePlayerText();


        //return total inv seeds
        return getInvTotalSeedAmount() >= 5;
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
        WItem[] arr = ZeeConfig.getMainInventory().getWItemsByNameContains(gItemSeedBasename).toArray(new WItem[0]);
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
        Gob g = ZeeConfig.getClosestGobToPlayer(plants);
        if(g!=null) {
            return ZeeManagerGobs.activateHarvestGob(g);
        }else {
            //println("no gobs to shift+click");
            return false;
        }
    }

    public static boolean activateCursorPlantGItem(GItem gi) {
        //haven.GItem@3a68ee9c ; iact ; [(23, 16), 1]
        //println("activateCursorPlantGItem > "+gi+", seeds = "+getSeedsAmount(gi));
        ZeeManagerItems.gItemActCoord(gi, UI.MOD_SHIFT);
        return waitCursorName(ZeeConfig.CURSOR_HARVEST);
    }

    private boolean inventoryHasSeeds() {
        return ZeeConfig.getMainInventory().getWItemsByNameContains(gItemSeedBasename).size() > 0;
    }

    private int getNumberOfSeedItems(){
        return ZeeConfig.getMainInventory().getWItemsByNameContains(gItemSeedBasename).size();
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
        if(c!=null && c.color.getRed()==1) {
            return true;
        }
        return false;
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
                ZeeConfig.gameUI.ui.msg("No empty barrels, lastBarrel null, dropping seeds.");
                ZeeConfig.getMainInventory().dropItemsByNameEndsWith(gItemSeedBasename);

            }
            else {
                /*
                    find barrel and store seeds
                 */
                barrels.removeIf(b -> {
                    ZeeGobColor c = b.getattr(ZeeGobColor.class);
                    if(c!=null && c.color.getBlue()==1) {
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
                    exitSeedFarmer("cant decide ql barrel");
                    return;
                }
                ZeeConfig.addPlayerText("storing ql "+qlSeeds);
                sleep(1000);//wait inventory update
                if (!updateSeedPileReference()) {
                    println("storeSeedsInBarrel > updateSeedPileReference > false");
                    return;
                }
                ZeeConfig.addGobText(lastBarrel, getSeedNameAndQl());
                if(!ZeeManagerItems.pickUpItem(wItem)){
                    exitSeedFarmer("couldn't pickup wItem ql "+wItem.item.getInfoQualityInt());
                    return;
                }
                sleep(PING_MS);//lag?
                ZeeManagerGobs.itemActGob(lastBarrel, UI.MOD_SHIFT);//store first seeds
                sleep(PING_MS);//lag?
                waitPlayerDistToGob(lastBarrel,15);//wait reaching barrel
                sleep(1000);//waitHoldingItemChanged();//wait 1st seed transfer
                if (ZeeConfig.isPlayerHoldingItem()) {//2nd seed transfer?
                    // store all seeds (ctrl+shift)
                    ZeeManagerGobs.itemActGob(lastBarrel, 3);//3==ctrl+shift
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
                    ZeeManagerGobs.gobClick(seedPile,3,UI.MOD_SHIFT);
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
        return ZeeConfig.getClosestGobToPlayer(barrels.stream().filter(ZeeManagerFarmer::isBarrelEmpty).collect(Collectors.toList()));
    }

    static int droppedSeeds = 0;
    private static int dropLowestQlSeeds(){
        List<WItem> seedItems = ZeeConfig.getMainInventory().getWItemsByNameContains(gItemSeedBasename);
        if (seedItems.size()==0)
            return -1;

        //find highest ql
        int highestQl = seedItems.get(0).item.getInfoQualityInt();
        int q;
        for (WItem seedItem : seedItems) {
            q = seedItem.item.getInfoQualityInt();
            if ( q > highestQl) {
                highestQl = q;
            }
        }

        //drop lower qls
        droppedSeeds = 0;
        for (WItem seedItem : seedItems) {
            if (seedItem.item.getInfoQualityInt() < highestQl) {
                seedItem.item.wdgmsg("drop", Coord.z);
                droppedSeeds++;
            }
        }

        //return highest ql found
        return highestQl;
    }

    private static int dropLessCommonSeeds() {
        List<WItem> seedItems = ZeeConfig.getMainInventory().getWItemsByNameContains(gItemSeedBasename);
        if (seedItems.size()==0)
            return -1;
        //count quantity of items for each ql
        Map<Integer,Integer> mapQlQuantity = new HashMap<>();
        int ql;
        for (int i = 0; i < seedItems.size(); i++) {
            ql = seedItems.get(i).item.getInfoQualityInt();
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
            if (seedItem.item.getInfoQualityInt() != qlKeeper) {
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
        return ZeeManagerGobs.isBarrelEmpty(b);
    }

    public static String getSeedNameAndQl() {
        return gItemSeedBasename.replace("seed-","")
                + " "
                + gItem.getInfoQualityInt();
    }

    // gfx/terobjs/barrel-flax
    // gfx/invobjs/seed-flax
    public static boolean isBarrelSameSeeds(Gob barrel, String seedName) {
        String seed = seedName.replace("seed-","");
        if(ZeeManagerGobs.getOverlayNames(barrel).contains("gfx/terobjs/barrel-" + seed)) {
            //println("same seeds , gfx/terobjs/barrel-" + seed);
            return true;
        }else {
            //println("not same seeds , gfx/terobjs/barrel-" + seed);
            return false;
        }
    }

    public static void showSeedFarmerWindow(Gob gobCrop) {
        Widget wdg;
        ZeeManagerFarmer.farmerGobCrop = gobCrop;
        ZeeConfig.farmerMode = true;

        if(windowManager ==null){

            windowManager = new ZeeWindow(new Coord(300,90), "Seed Farmer"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg=="close"){
                        exitSeedFarmer();
                        windowManager = null;
                        this.reqdestroy();
                    }
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
            textEntryQlSortBarrels = new ZeeWindow.ZeeTextEntry(UI.scale(45),""){
                void onEnterPressed(String text) {
                    try {
                        farmerTxtQlSortingBarrels = Integer.parseInt(buf.line());
                        Utils.setprefi("farmerTxtQlSortingBarrels", ZeeManagerFarmer.farmerTxtQlSortingBarrels);
                    }catch (Exception ex){
                        ZeeConfig.msgError("not a number ?"+text);
                    }
                }
            };
            wdg = windowManager.add(ZeeManagerFarmer.textEntryQlSortBarrels, 95, 30-5);
            textEntryQlSortBarrels.settext(""+ farmerTxtQlSortingBarrels);


            // barrel tiles textEntry
            wdg = windowManager.add(new Label("Max tiles to barrel: "), 0, 55);
            textEntryTilesBarrel = new ZeeWindow.ZeeTextEntry(UI.scale(45),""+(int)(MAX_BARREL_DIST/MCache.tilesz.x)){
                void onEnterPressed(String text) {
                    try {
                        farmerTxtTilesBarrel = Integer.parseInt(buf.line());
                        Utils.setprefi("farmerTxtTilesBarrel", farmerTxtTilesBarrel);
                    }catch (Exception ex){
                        ZeeConfig.msgError("not a number ?"+text);
                    }
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



    private static Window farmAwayWin;
    private static boolean farmAwayDrop = Utils.getprefb("farmAwayDrop",false);
    private static boolean farmAwayCollect = Utils.getprefb("farmAwayCollect",true);
    private static boolean farmAwayEquipSacksPumpkin = Utils.getprefb("farmAwayEquipSacksPumpkin",true);
    private static boolean farmAwayEquipSacksNonPumpkin = Utils.getprefb("farmAwayEquipSacksNonPumpkin",false);
    private static boolean farmAwayOn = false;
    private static int farmAwayTilesInt = Utils.getprefi("farmAwayTilesInt",20);
    public static void farmAway(Gob crop) {

        //create window
        String winName = "Farm Away";
        farmAwayWin = ZeeConfig.getWindow(winName);
        if (farmAwayWin != null) {
            farmAwayWin.reqdestroy();
        }
        farmAwayWin = ZeeConfig.gameUI.add(
                new Window(Coord.of(70,140), winName){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("close")){
                            farmAwayExit("");
                            this.reqdestroy();
                        }
                    }
                }, ZeeConfig.gameUI.sz.div(2)
        );
        int x=0, y=3, padY=2;
        Widget widget = farmAwayWin.add(new Label("Tiles dist"), x, y);
        TextEntry textEntryMaxTiles = new ZeeWindow.ZeeTextEntry(UI.scale(45), "" + farmAwayTilesInt) {
            void onEnterPressed(String text) {
                try {
                    farmAwayTilesInt = Integer.parseInt(text);
                    Utils.setprefi("farmAwayTilesInt", farmAwayTilesInt);
                }catch (Exception ex){
                    ZeeConfig.msgError("not a number ? "+text);
                }
            }
        };
        x += widget.sz.x + 5;
        widget = farmAwayWin.add(textEntryMaxTiles, x, y);
        x = 0;
        y += widget.sz.y + padY;
        widget = farmAwayWin.add(new CheckBox("drop"){
            {a = farmAwayDrop;}
            public void changed(boolean val) {
                super.changed(val);
                farmAwayDrop = val;
                Utils.setprefb("farmAwayDrop",farmAwayDrop);
            }
        },x,y);
        y += widget.sz.y + padY;
        widget = farmAwayWin.add(new CheckBox("stack"){
            {a = ZeeConfig.autoStack;}
            public void changed(boolean val) {
                super.changed(val);
                if (val != ZeeConfig.autoStack)
                    ZeeInvMainOptionsWdg.cbAutoStack.click();
            }
        },x,y);
        y += widget.sz.y + padY;
        widget = farmAwayWin.add(new CheckBox("collect"){
            {a = farmAwayCollect;}
            public void changed(boolean val) {
                super.changed(val);
                farmAwayCollect = val;
                Utils.setprefb("farmAwayCollect",farmAwayCollect);
            }
        },x,y);
        y += widget.sz.y + padY;
        widget = farmAwayWin.add(new Label("equip sacks"),x,y);
        y += widget.sz.y + padY;
        widget = farmAwayWin.add(new CheckBox("pumpkin"){
            {a = farmAwayEquipSacksPumpkin;}
            public void changed(boolean val) {
                super.changed(val);
                farmAwayEquipSacksPumpkin = val;
                Utils.setprefb("farmAwayEquipSacksPumpkin", farmAwayEquipSacksPumpkin);
            }
        },x+7,y);
        y += widget.sz.y + padY;
        widget = farmAwayWin.add(new CheckBox("non-pumpkin"){
            {a = farmAwayEquipSacksNonPumpkin;}
            public void changed(boolean val) {
                super.changed(val);
                farmAwayEquipSacksNonPumpkin = val;
                Utils.setprefb("farmAwayEquipSacksNonPumpkin", farmAwayEquipSacksNonPumpkin);
            }
        },x+7,y);

        farmAwayWin.pack();

        //farmAwayEquipSacksNonPumpkin

        // start farming
        new ZeeThread(){
            public void run() {
                try {
                    println("farmaway thread start");
                    farmAwayOn = true;
                    Gob nextCrop = crop;
                    int minCropStage = ZeeConfig.getPlantStage(nextCrop);
                    String cropName = crop.getres().name;
                    Inventory inv = ZeeConfig.getMainInventory();

                    // farm
                    ZeeConfig.addPlayerText("farm");
                    do{
                        if (ZeeConfig.isPlayerDrinkingPose())
                            waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_DRINK);
                        ZeeManagerGobs.gobClick(nextCrop,3);
                        prepareCancelClick();
                        sleep(PING_MS);
                        waitPlayerIdleLinMove();
                        //drop
                        if(farmAwayDrop)
                            inv.dropItemsByNameEndsWith(ZeeConfig.lastInvGItemCreatedName);
                        nextCrop = getClosestCropAboveMinStage(cropName,minCropStage);
                        if (nextCrop==null)
                            break;
                        double dist = ZeeConfig.distanceToPlayer(nextCrop);
                        if (dist > farmAwayTilesInt * TILE_SIZE)
                            break;
                    }while(farmAwayOn && !isCancelClick() && nextCrop!=null);

                    //collect subproducts
                    if (farmAwayOn && !isCancelClick()){
                        List<String> subprods = new ArrayList<>();
                        if (cropName.endsWith("/poppy"))
                            subprods.addAll(List.of("/flower-poppy","/poppypod"));
                        else if (cropName.endsWith("/flax"))
                            subprods.add("/flaxfibre");
                        else if (cropName.endsWith("/hemp"))
                            subprods.add("/hempfibre");
                        else if (cropName.endsWith("/beet"))
                            subprods.add("/beetleaves");
                        else if (cropName.endsWith("/lettuce"))
                            subprods.add("/lettucehead");
                        else if (cropName.endsWith("/greenkale"))
                            subprods.add("/greenkale");
                        else if (cropName.endsWith("/pipeweed"))
                            subprods.add("/tobacco-fresh");
                        else if (cropName.endsWith("/pumpkin"))
                            subprods.add("/pumpkin");
                        else if (cropName.endsWith("/barley") || cropName.endsWith("/wheat") || cropName.endsWith("/millet"))
                            subprods.add("/straw");
                        if (!subprods.isEmpty())
                            ZeeConfig.addPlayerText("collect");
                        //equip sacks
                        if (farmAwayEquipSacksPumpkin && subprods.contains("/pumpkin"))
                            ZeeManagerItems.equipTwoSacks();
                        else if(farmAwayEquipSacksNonPumpkin && !subprods.isEmpty())
                            ZeeManagerItems.equipTwoSacks();
                        //collect subprods
                        prepareCancelClick();
                        for (String subprod : subprods) {
                            ZeeConfig.addPlayerText("collect "+subprod);
                            Gob closestSubprod = ZeeConfig.getClosestGobByNameEnds("gfx/terobjs/items" + subprod);
                            while(closestSubprod!=null && !isCancelClick()) {
                                // shift+click closest item
                                ZeeManagerGobs.gobClick(closestSubprod, 3, UI.MOD_SHIFT);
                                prepareCancelClick();
                                // wait first item acquired
                                waitInvItemOrCancelClick();
                                if (isCancelClick())
                                    break;
                                // wait other items
                                waitPlayerIdleLinMove();
                                // check for distant subroducts missed
                                closestSubprod = ZeeConfig.getClosestGobByNameEnds("gfx/terobjs/items" + subprod);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                farmAwayOn = false;
                ZeeConfig.removePlayerText();
                println("farmaway thread stop");
            }
        }.start();
    }
    private static void farmAwayExit(String s) {
        if (!s.isBlank())
            println(s);
        farmAwayOn = false;
        if (farmAwayWin!=null)
            farmAwayWin.reqdestroy();
        farmAwayWin = null;
    }


    public static Gob getClosestCropAboveMinStage(String cropName, int minCropStage) {

        List<Gob> gobs = ZeeConfig.findGobsByNameEndsWith(cropName);
        gobs.removeIf(g1 -> ZeeConfig.getPlantStage(g1) < minCropStage);
        if (gobs.size()==0)
            return null;

        Gob closestGob = gobs.remove(0);
        double closestDist = ZeeConfig.distanceToPlayer(closestGob);
        double dist;
        for (Gob g : gobs) {
            dist = ZeeConfig.distanceToPlayer(g);
            if (dist < closestDist) {
                closestGob = g;
                closestDist = dist;
            }
        }
        return closestGob;
    }

    static final Map<String,Integer> mapCropMinStageHarvest = Map.ofEntries(
            Map.entry("gfx/terobjs/plants/turnip",1),
            Map.entry("gfx/terobjs/plants/carrot",1),
            Map.entry("gfx/terobjs/plants/beet",3),
            Map.entry("gfx/terobjs/plants/poppy",4),
            Map.entry("gfx/terobjs/plants/lettuce",4),
            Map.entry("gfx/terobjs/plants/pumpkin",4),
            Map.entry("gfx/terobjs/plants/redonion",3),
            Map.entry("gfx/terobjs/plants/yellowonion",3),
            Map.entry("gfx/terobjs/plants/leek",2),
            Map.entry("gfx/terobjs/plants/hemp",3),
            Map.entry("gfx/terobjs/plants/flax",3),
            Map.entry("gfx/terobjs/plants/barley",3),
            Map.entry("gfx/terobjs/plants/wheat",3),
            Map.entry("gfx/terobjs/plants/millet",3),
            Map.entry("gfx/terobjs/plants/pipeweed",4),
            Map.entry("gfx/terobjs/plants/peas",4),
            Map.entry("gfx/terobjs/plants/cucumber",4),
            Map.entry("gfx/terobjs/plants/champignon",4),
            Map.entry("gfx/terobjs/plants/garlic",4),
            Map.entry("gfx/terobjs/plants/greenkale",4)
    );
    public static boolean isCropStageHarvestable(Gob crop) {
        boolean ret = false;
        Integer minHarvestage = mapCropMinStageHarvest.get(crop.getres().name);
        if (minHarvestage==null)
            return ret;
        int maxStage = 0;
        for (FastMesh.MeshRes layer : crop.getres().layers(FastMesh.MeshRes.class)) {
            if(layer.id / 10 > maxStage) {
                maxStage = layer.id / 10;
            }
        }
        Message data = ZeeConfig.getDrawableData(crop);
        if(data != null) {
            int stage = data.uint8();
            if(stage > maxStage)
                stage = maxStage;
            if(stage >= minHarvestage)
                ret = true;
            //println(crop.getres().name+" stage "+stage);
        }
        return ret;
    }

    public static boolean isCropMaxStage(Gob crop) {
        boolean ret = false;
        int maxStage = getCropMaxStage(crop);
        Message data = ZeeConfig.getDrawableData(crop);
        if(data != null) {
            int stage = data.uint8();
            if(stage > maxStage)
                stage = maxStage;
            if(stage==maxStage)
                ret = true;
        }
        return ret;
    }

    public static int getCropMaxStage(Gob crop){
        int maxStage = 0;
        for (FastMesh.MeshRes layer : crop.getres().layers(FastMesh.MeshRes.class)) {
            if(layer.id / 10 > maxStage) {
                maxStage = layer.id / 10;
            }
        }
        return maxStage;
    }
}

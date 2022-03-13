package haven;

import java.awt.event.KeyEvent;
import java.util.List;

public class ZeeSeedFarmingManager extends ZeeThread{

    public static final int MIN_ACCESSIBLE_DIST = 15;//TODO: isPlayerMountingHorse()
    public static final int MAX_BARREL_DIST = 300;
    public static final double TILE_SIZE = MCache.tilesz.x;
    public static Gob lastBarrel;
    public static boolean busy;
    public static String gItemSeedBasename, lastItemSeedBasename;
    public static GItem gItem;
    public static WItem wItem;
    public static Inventory inv;
    public static boolean isHarvestDone, isPlantingDone;
    public static Window windowManager;

    public static boolean farmerCbReplant = Utils.getprefb("farmerCbPlant",false);
    public static int farmerTxtTilesBarrel = Utils.getprefi("farmerTxtTilesBarrel",27);
    public static TextEntry textEntryTilesBarrel;
    public static Gob farmerGobCrop;


    public ZeeSeedFarmingManager(GItem g, String nameSeed) {
        busy = true;
        gItem = g;
        gItemSeedBasename = nameSeed;// "seed-turnip"
        if(!nameSeed.equals(lastItemSeedBasename)) {
            //println(">new crop name, forget last barrel ("+lastBarrel+")");
            lastBarrel = null;
            ZeeConfig.removeGobText(lastBarrel);
        }
        lastItemSeedBasename = nameSeed;
        inv = (Inventory) gItem.parent;
        wItem = inv.getWItemByGItem(gItem);
    }

    public static void resetInitialState() {
        //println(">reset initial state");
        busy = false;
        isHarvestDone = true;
        isPlantingDone = true;
        ZeeConfig.resetTileSelection();
        ZeeConfig.autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);
        ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
    }

    /*
        Update seed-pile reference.
        Returns true if pile contains 5+ seeds, else returns false.
     */
    private boolean updateSeedPileReference() {
        List<WItem> items = inv.getWItemsByName(gItemSeedBasename);
        for (WItem item : items) {
            if (getSeedsAmount(item.item) >= 5){
                // seed-pile big enough for planting
                wItem = item;
                gItem = item.item;
                return true;
            }
        }
        wItem = items.get(0);
        gItem = wItem.item;
        return false;
    }

    public void run(){
        startFarming();
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
                if (!ZeeClickGobManager.isBarrelEmpty(gob)) {
                    ZeeConfig.addGobText(gob, getBarrelOverlayBasename(gob));
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

        try{

            /*
                harvest stage
             */
            isHarvestDone = false;
            while(busy && !isHarvestDone) {
                //println("> harvesting loop");
                ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"harvesting",0,255,255,255,10);
                waitPlayerIdleFor(2);//already farming
                if(inventoryHasSeeds()) {
                    storeSeedsInBarrel();
                    if(!isInventoryFull()) {
                        harvestPlants();
                    }else {
                        //println("harvest done, inv full, out of barrels?");
                        isHarvestDone = true;
                    }
                }else{
                    isHarvestDone = true;
                }
            }

            /*
                planting stage
             */
            if(ZeeSeedFarmingManager.farmerCbReplant) {
                isPlantingDone = false;
                while (busy && !isPlantingDone) {
                    //println("> planting loop");
                    if (getTotalSeedAmount() < 5) {
                        //println("total seeds < 5, get from barrels");
                        if (!getSeedsFromMultipleBarrels(gItemSeedBasename)) {
                            isPlantingDone = true;
                            break;
                        }
                    }
                    plantSeeds();
                    waitPlayerIdleFor(3);
                    if(getTotalSeedAmount() >= 5) {
                        // player idle, 5+ seeds
                        if (getNumberOfSeedItems() == 1) {
                            // player idle, 5+ seeds, 1 seed-pile, area is fully planted
                            isPlantingDone = true;
                            println("planting done");
                        }else{
                            // player idle, 5+ seeds, 2+ seed-piles, area probably still need planting
                            println("store small seed-piles and try planting again ");
                            storeSeedsInBarrel(); //store and try planting one more time
                        }
                    }
                }

                //final store barrel
                storeSeedsInBarrel();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        resetInitialState();
    }

    private boolean getSeedsFromMultipleBarrels(String seedBaseName) {
        try {
            ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"getting seeds",0,255,255,255,10);

            while(!isInventoryFull()){

                // select all seed barrels
                List<Gob> barrels = getAllSeedBarrels(seedBaseName);
                if (barrels.isEmpty()){
                    //println("no barrels containing "+seedBaseName);
                    break;
                }

                // get closest barrel
                Gob closestBarrel = ZeeConfig.getClosestGob(barrels);

                // try access barrel and get seeds
                getSeedsFromBarrel(closestBarrel);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return getTotalSeedAmount() >= 5;
    }

    public static boolean isBarrelAccessible(Gob barrel) {
        ZeeClickGobManager.gobClick(barrel, 3);
        waitPlayerIdleFor(2);
        double dist = ZeeConfig.distanceToPlayer(barrel);
        return dist < MIN_ACCESSIBLE_DIST;
    }

    public static boolean getSeedsFromBarrel(Gob barrel) throws InterruptedException {
        if(!isBarrelAccessible(barrel)){
            markBarrelInaccessible(barrel);
            return false;
        }
        while(!isBarrelEmpty(barrel) && !isInventoryFull()){
            ZeeClickGobManager.gobClick(barrel, 3, UI.MOD_SHIFT);
            //TODO: waitInvCHanges
            Thread.sleep(PING_MS+100);
        }
        if(waitHoldingItem(4000))//store remaining holding item
            ZeeClickGobManager.gobItemAct(lastBarrel, 0);
        println("getSeedsFromBarrel() > holding item = "+ZeeConfig.isPlayerHoldingItem());
        return getTotalSeedAmount() >= 5;
    }


    public static List<Gob> getAllSeedBarrels(String seedBaseName) {
        //println(">get all seed barrels");
        List<Gob> barrels = getAccessibleBarrels();
        barrels.removeIf(b -> {
            if (ZeeClickGobManager.isBarrelEmpty(b))
                return true;
            if (!isBarrelSameSeeds(b, seedBaseName))
                return true;
            return false;
        });
        return barrels;
    }

    public static boolean isInventoryFull() {
        return inv.getNumberOfFreeSlots() == 0;
    }

    private boolean plantSeeds() {
        ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"planting",0,255,255,255,10);
        if(updateSeedPileReference() && activateCursorPlantGItem(gItem)) {
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
            return true;
        }else{
            println("couldn't activate planting cursor?");
        }
        return false;
    }

    private boolean harvestPlants() {
        ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"harvesting",0,255,255,255,10);
        if(activateCursorHarvestGob()) {
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
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

    public static int getTotalSeedAmount(){
        int ret = 0;
        WItem[] arr = inv.getWItemsByName(gItemSeedBasename).toArray(new WItem[0]);
        for (int i = 0; i < arr.length ; i++) {
            ret += getSeedsAmount(arr[i].item);
        }
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
            ZeeClickGobManager.gobClick(g, 3, UI.MOD_SHIFT);
            return waitCursor(ZeeConfig.CURSOR_HARVEST);
        }else {
            //println("no gobs to shift+click");
            return false;
        }
    }

    public static boolean activateCursorPlantGItem(GItem gi) {
        //haven.GItem@3a68ee9c ; iact ; [(23, 16), 1]
        println("activateCursorPlantGItem > "+gi+", seeds = "+getSeedsAmount(gi));
        ZeeClickItemManager.gItemAct(gi, UI.MOD_SHIFT);
        return waitCursor(ZeeConfig.CURSOR_HARVEST);
    }

    private boolean inventoryHasSeeds() {
        return inv.getWItemsByName(gItemSeedBasename).size() > 0;
    }

    private int getNumberOfSeedItems(){
        return inv.getWItemsByName(gItemSeedBasename).size();
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

    private void storeSeedsInBarrel() {
        try {
            List<Gob> barrels = getAccessibleBarrels();
            if (barrels.size()==0) {
                /*
                    no barrels, drop seeds
                 */
                ZeeConfig.gameUI.msg("No empty barrels, lastBarrel null, dropping seeds.");
                inv.dropItemsByName(gItemSeedBasename);

            } else {
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
                //if (lastBarrel != null)
                //    ZeeConfig.removeGobText(lastBarrel);
                lastBarrel = ZeeConfig.getClosestGob(barrels);
                if(lastBarrel==null){
                    resetInitialState();
                    return;
                }
                ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"storing",0,255,255,255,10);
                ZeeConfig.addGobText(lastBarrel, getSeedNameAndQl(), 0,255,0,255,10);
                updateSeedPileReference();
                ZeeClickItemManager.pickUpItem(wItem);
                ZeeClickGobManager.gobItemAct(lastBarrel, UI.MOD_SHIFT);//store first seeds
                waitPlayerMove();//wait reaching barrel
                waitPlayerStop();
                Thread.sleep(1000);//wait storing seed
                if (ZeeConfig.isPlayerHoldingItem()) {
                    // store all seeds (ctrl+shift)
                    ZeeClickGobManager.gobItemAct(lastBarrel, 3);//3==ctrl+shift
                    if(waitNotHoldingItem()) {
                        ZeeConfig.addGobText(lastBarrel, getSeedNameAndQl());
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
                    ZeeConfig.addGobText(lastBarrel, getSeedNameAndQl());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void markBarrelFull(Gob lastBarrel) {
        ZeeConfig.addGobColor(lastBarrel,0,0,255,255);
    }

    public static void markBarrelInaccessible(Gob lastBarrel) {
        ZeeConfig.addGobColor(lastBarrel,255,0,0,255);
    }

    public static boolean isBarrelEmpty(Gob b) {
        return ZeeClickGobManager.isBarrelEmpty(b);
    }

    public static String getSeedNameAndQl() {
        return gItemSeedBasename.replace("seed-","")
                + " q"
                + Inventory.getQuality(gItem).intValue();
    }

    public static String getBarrelOverlayBasename(Gob barrel){
        List<String> ols =  ZeeClickGobManager.getOverlayNames(barrel);
        if(ols.isEmpty())
            return "";
        return ols.get(0).replace("gfx/terobjs/barrel-","");
    }

    // gfx/terobjs/barrel-flax
    // gfx/invobjs/seed-flax
    public static boolean isBarrelSameSeeds(Gob barrel, String seedName) {
        String seed = seedName.replace("seed-","");
        if(ZeeClickGobManager.getOverlayNames(barrel).contains("gfx/terobjs/barrel-" + seed)) {
            //println("same seeds , gfx/terobjs/barrel-" + seed);
            return true;
        }else {
            //println("not same seeds , gfx/terobjs/barrel-" + seed);
            return false;
        }
    }

    public static void showWindow(Gob gobCrop) {
        Widget wdg;
        ZeeSeedFarmingManager.farmerGobCrop = gobCrop;
        ZeeConfig.farmerMode = true;

        if(windowManager ==null){

            windowManager = new ZeeWindow(new Coord(280,90), "Seed Farming manager"){
                @Override
                public void wdgmsg(String msg, Object... args) {
                    if (msg=="close"){
                        ZeeConfig.farmerMode = false;
                        resetInitialState();
                    }
                    super.wdgmsg(msg, args);
                }
            };


            //checkbox plant
            wdg = windowManager.add(new CheckBox("replant after harvest") {
                { a = ZeeSeedFarmingManager.farmerCbReplant;  }
                public void set(boolean val) {
                    ZeeSeedFarmingManager.farmerCbReplant = val;
                    a = val;
                    Utils.setprefb("farmerCbPlant",val);
                }
            }, 0, 7);


            // barrel tiles textEntry
            wdg = windowManager.add(new Label("Max tiles to barrel: "), 0, 30);
            ZeeSeedFarmingManager.textEntryTilesBarrel = new TextEntry(UI.scale(45),""+(int)(ZeeSeedFarmingManager.MAX_BARREL_DIST/MCache.tilesz.x)){
                public boolean keydown(KeyEvent e) {
                    if(!Character.isDigit(e.getKeyChar()) && !ZeeConfig.isControlKey(e.getKeyCode()))
                        return false;
                    return super.keydown(e);
                }
                public void changed(ReadLine buf) {
                    if(!buf.line().isEmpty()) {
                        ZeeSeedFarmingManager.farmerTxtTilesBarrel = Integer.parseInt(buf.line());
                        Utils.setprefi("farmerTxtTilesBarrel", ZeeSeedFarmingManager.farmerTxtTilesBarrel);
                    }
                    super.changed(buf);
                }
            };
            wdg = windowManager.add(ZeeSeedFarmingManager.textEntryTilesBarrel, 95, 30-3);
            ZeeSeedFarmingManager.textEntryTilesBarrel.settext(""+ ZeeSeedFarmingManager.farmerTxtTilesBarrel);


            //barrel tiles test Button
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(45),"test"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("activate")){
                        try {
                            int tiles = Integer.parseInt(textEntryTilesBarrel.text().strip());
                            ZeeSeedFarmingManager.testBarrelsTiles(false);
                        } catch (NumberFormatException e) {
                            ZeeConfig.msg("numbers only");
                        }
                    }
                }
            }, 145,30-4);


            //barrel tiles clear Button
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(45),"clear"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("activate")){
                        ZeeSeedFarmingManager.testBarrelsTilesClear();
                    }
                }
            }, wdg.c.x+wdg.sz.x+5,30-4);


            //add bottom note
            wdg = windowManager.add(new Label("Start from the field's center."), 0, 90-30);
            wdg = windowManager.add(new Label("Remove field obstacles, surround with barrels."), 0, 90-15);


            //add window
            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));

        }else {
            windowManager.show();
        }
    }

}

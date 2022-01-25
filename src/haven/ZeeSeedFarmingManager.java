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
    public static boolean isHarvestDone, isPlantingDone, isScytheEquiped;
    public static Window windowManager;
    public static int recursiveGetSeedsCount;

    public static boolean farmerCbReplant = Utils.getprefb("farmerCbPlant",false);
    public static int farmerTxtTilesBarrel = Utils.getprefi("farmerTxtTilesBarrel",27);
    public static TextEntry textEntryTilesBarrel;
    public static Gob farmerGobCrop;


    public ZeeSeedFarmingManager(GItem g, String nameSeed) {
        busy = true;
        gItem = g;
        gItemSeedBasename = nameSeed;// "seed-turnip"
        if(!nameSeed.equals(lastItemSeedBasename)) {
            println(">new crop name, forget last barrel ("+lastBarrel+")");
            lastBarrel = null;
            ZeeConfig.removeGobText(lastBarrel);
        }
        lastItemSeedBasename = nameSeed;
        inv = (Inventory) gItem.parent;
        wItem = inv.getWItemByGItem(gItem);
        recursiveGetSeedsCount = 0;
    }

    public ZeeSeedFarmingManager() {
        println("marking barrels");
        gItem = null;
    }

    public static void resetInitialState() {
        println(">reset initial state");
        busy = false;
        isHarvestDone = true;
        isPlantingDone = true;
        ZeeConfig.resetTileSelection();
        ZeeConfig.autoHearthOnStranger = Utils.getprefb("autoHearthOnStranger", true);
        if(lastBarrel!=null)
            ZeeConfig.removeGobText(lastBarrel);
        ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
    }

    private void updateWItem() {
        wItem = inv.getWItemsByName(gItemSeedBasename).get(0);
        gItem = wItem.item;
    }

    public void run(){
        if (gItem==null){
            testBarrelsTiles();
        }else{
            startFarming();
        }
    }

    private void testBarrelsTiles() {
        //highlight barrels in range
        getAccessibleBarrels().forEach(gob -> {
            ZeeConfig.addGobColor(gob,0,255,0,255);
            ZeeConfig.addGobText(gob,"↓",0,255,0,255,10);
        });
        /*
        busy = true;
        Moving m;
        try {
            while (busy) {
                Thread.sleep(PING_MS);
                m = ZeeConfig.getPlayerGob().getattr(Moving.class);
                if(m!=null)
                    println("v = "+m.getv()+" , c = "+m.getc());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
         */
    }

    private void startFarming() {
        ZeeConfig.autoHearthOnStranger = false;

        try{

            /*
                harvest stage
             */
            isHarvestDone = false;
            while(busy && !isHarvestDone) {
                println("> harvesting loop");
                ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"harvesting",0,255,255,255,10);
                waitPlayerIdleFor(2);//already farming
                if(inventoryHasSeeds()) {
                    storeSeedsInBarrel();
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
            if(ZeeSeedFarmingManager.farmerCbReplant) {
                isPlantingDone = false;
                isScytheEquiped = ZeeClickItemManager.isItemEquipped("scythe");
                while (busy && !isPlantingDone) {
                    println("> planting loop");
                    if (getTotalSeedAmount() < 5) {
                        println("total seeds < 5, get from barrels");
                        if (!getSeedsFromMultipleBarrels(gItemSeedBasename)) {
                            println("planting done, no seeds from barrel?");
                            isPlantingDone = true;
                            break;
                        }
                    }
                    plantSeeds();
                    waitPlayerIdleFor(2);
                    if(getTotalSeedAmount() >= 5) {
                        println("planting done");
                        isPlantingDone = true;
                    }else{
                        println("planted all inv seeds, get more?");
                    }
                }

                println("> final store barrel");
                storeSeedsInBarrel();
            }

            println(">exit");

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
                    println("no barrels containing "+seedBaseName);
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

        return getTotalSeedAmount() > 5;
    }

    public static boolean isBarrelAccessible(Gob barrel) {
        ZeeClickGobManager.gobClick(barrel, 3);
        waitPlayerIdleFor(2);
        double dist = ZeeConfig.distanceToPlayer(barrel);
        return dist < MIN_ACCESSIBLE_DIST;
    }

    public static boolean getSeedsFromBarrel(Gob barrel) throws InterruptedException {
        println(">get seeds from barrel");
        if(!isBarrelAccessible(barrel)){
            markBarrelInaccessible(barrel);
            return false;
        }
        while (!isBarrelEmpty(barrel) && !isInventoryFull()) {
            ZeeClickGobManager.gobClick(barrel, 3, UI.MOD_SHIFT);
            //TODO: waitInvCHanges
            Thread.sleep(PING_MS+100);
        }
        if(waitHoldingItem())//store remaining holding item
            ZeeClickGobManager.gobItemAct(lastBarrel, 0);
        return getTotalSeedAmount() > 5;
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
            println("planting...");
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
            return true;
        }else{
            println("couldn't activate cursor for planting");
        }
        return false;
    }

    private boolean harvestPlants() {
        ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"harvesting",0,255,255,255,10);
        if(activateCursorHarvestGob()) {
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
            return true;
        }else {
            println("out of plants to harvest");
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
        println("activateCursorHarvestGob()");
        //println("find gobs named "+ZeeConfig.lastMapViewClickGobName);
        List<Gob> plants = ZeeConfig.findGobsByName(ZeeConfig.lastMapViewClickGobName);
        if (plants.size()==0) {
            return false;
        }
        plants.removeIf(plt -> ZeeConfig.getPlantStage(plt) == 0  &&  !ZeeConfig.lastMapViewClickGobName.contains("/fallowplant"));
        if (plants.size()==0) {
            println("no plants to click");
            return false;
        }
        Gob g = ZeeConfig.getClosestGob(plants);
        //println("closest "+g+" , stage "+ZeeConfig.getPlantStage(g));
        if(g!=null) {
            ZeeClickGobManager.gobClick(g, 3, UI.MOD_SHIFT);
            return waitCursor(ZeeConfig.CURSOR_HARVEST);
        }else {
            println("no gobs to shift+click");
            return false;
        }
    }

    public static boolean activateCursorPlantGItem(GItem gi) {
        //haven.GItem@3a68ee9c ; iact ; [(23, 16), 1]
        //println("activateCursorPlantGItem()");
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
        List<Gob> emptyBarrels = ZeeConfig.findGobsByName("barrel");
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
            println("barrels = " + barrels.size());
            if (barrels.size()==0) {
                /*
                    no barrels, drop seeds
                 */
                ZeeConfig.gameUI.msg("No empty barrels, lastBarrel null, dropping seeds.");
                println("No empty barrels close, dropping seeds.");
                inv.dropItemsByName(gItemSeedBasename);

            } else {
                /*
                    find barrel and store seeds
                 */
                println(">choose barrel");
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
                    println("get closest barrel = null");
                    resetInitialState();
                    return;
                }
                ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"storing",0,255,255,255,10);
                ZeeConfig.addGobText(lastBarrel, getBarrelText(), 0,255,0,255,10);
                updateWItem();
                ZeeClickItemManager.pickUpItem(wItem);
                ZeeClickGobManager.gobItemAct(lastBarrel, UI.MOD_SHIFT);
                waitPlayerMove();
                waitPlayerStop();
                Thread.sleep(PING_MS);//wait storing seed
                if (ZeeConfig.isPlayerHoldingItem()) {
                    // store all seeds (ctrl+shift)
                    println("store all seeds (ctrl+shift)");
                    ZeeClickGobManager.gobItemAct(lastBarrel, 3);
                    if(waitNotHoldingItem()) {
                        println("seeds stored");
                        //ZeeConfig.addGobColor(lastBarrel,0,255,0,255);
                    } else {
                        //still holding item?
                        println("still holding item 1, mark barrel full");
                        markBarrelFull(lastBarrel);
                        lastBarrel = null;
                    }
                }else{
                    //still holding item?
                    if(ZeeConfig.distanceToPlayer(lastBarrel) > MIN_ACCESSIBLE_DIST){
                        //can't reach barrel
                        println("still holding item 2, mark barrel unaccessible");
                        markBarrelInaccessible(lastBarrel);
                    }else{
                        //barrel full
                        println("still holding item 2, mark barrel full");
                        markBarrelFull(lastBarrel);
                    }
                    lastBarrel = null;
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

    public static String getBarrelText() {
        return gItemSeedBasename.replace("seed-","")
                + " q"
                + Inventory.getQuality(gItem).intValue();
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

            windowManager = new ZeeWindow(new Coord(300,120), "Seed Farming manager"){
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


            // radiogroup seeds
            /*
            RadioGroup grp = new RadioGroup(windowManager) {
                public void changed(int opt, String lbl) {
                    ZeeFarmingManager.farmerRbSeeds = opt;
                    Utils.setprefi("farmerRbSeeds",opt);
                }
            };
            wdg = windowManager.add(new Label("Seeds: "), 0, 30);
            wdg = grp.add("store", new Coord(37, 30));
            wdg = grp.add("drop", new Coord(85, 30));
            wdg = grp.add("wait", new Coord(137, 30));
            grp.check(ZeeFarmingManager.farmerRbSeeds);
            */


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


            //barrel tiles Button
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(45),"test"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("activate")){
                        try {
                            int tiles = Integer.parseInt(textEntryTilesBarrel.text().strip());
                            new ZeeSeedFarmingManager().start();
                        } catch (NumberFormatException e) {
                            ZeeConfig.msg("numbers only");
                        }
                    }
                }
            }, 145,30-4);
            ZeeSeedFarmingManager.textEntryTilesBarrel.settext(""+ ZeeSeedFarmingManager.farmerTxtTilesBarrel);


            //add bottom note
            wdg = windowManager.add(new Label("(no path-find, remove field obstacles, surround with barrels)"), 0, 120-15);


            //add window
            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));

        }else {
            windowManager.show();
        }
    }

}

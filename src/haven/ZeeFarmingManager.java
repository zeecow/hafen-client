package haven;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;

public class ZeeFarmingManager extends ZeeThread{

    public static final int RB_SEEDS_STORE = 0;
    public static final int RB_SEEDS_DROP = 1;
    public static final int RB_SEEDS_WAIT = 2;
    public static long IDLE_MS = 2222;
    public static final int MAX_BARREL_DIST = 300;
    public static final double TILE_SIZE = MCache.tilesz.x;
    public static Gob lastBarrel;
    public static boolean busy;
    public static String gItemNameSeed, lastItemNameSeed;
    public static GItem gItem;
    public static WItem wItem;
    public static Inventory inv;
    public static boolean isHarvestDone, isPlantingDone, isScytheEquiped;
    public static HashMap<Gob,Integer> mapBarrelSeedql = new HashMap<Gob,Integer> ();
    public static Window windowManager;

    public static boolean farmerCbHarvest = Utils.getprefb("farmerCbHarvest",true);
    public static boolean farmerCbReplant = Utils.getprefb("farmerCbPlant",false);
    public static int farmerRbSeeds = Utils.getprefi("farmerRbSeeds",RB_SEEDS_WAIT);
    public static int farmerRgBarrelPriority = Utils.getprefi("farmerRgBarrelPriority",0);
    public static int farmerTxtTilesBarrel = Utils.getprefi("farmerTxtTilesBarrel",27);
    public static TextEntry textEntryTilesBarrel;
    public static Gob farmerGobCrop;


    public ZeeFarmingManager(GItem g, String nameSeed) {
        busy = true;
        gItem = g;
        gItemNameSeed = nameSeed;// "seed-turnip"
        if(!nameSeed.equals(lastItemNameSeed)) {
            println(">new crop name, forget last barrel ("+lastBarrel+")");
            lastBarrel = null;
            mapBarrelSeedql.clear();
            ZeeConfig.removeGobText(lastBarrel);
        }else{
            println(">same crop name, use last barrel ("+lastBarrel+")");
        }
        lastItemNameSeed = nameSeed;
        inv = (Inventory) gItem.parent;
        wItem = inv.getWItemByGItem(gItem);
    }

    public ZeeFarmingManager() {
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
        ZeeConfig.removeGobText(lastBarrel);
        ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
    }

    private void updateWItem() {
        wItem = inv.getWItemsByName(gItemNameSeed).get(0);
        gItem = wItem.item;
    }

    public void run(){
        if (gItem==null){
            getBarrels().forEach(gob -> {
                ZeeConfig.addGobColor(gob,0,255,0,255);
                ZeeConfig.addGobText(gob,"↓",0,255,0,255,10);
            });
        }else{
            startFarming();
        }
    }

    private void startFarming() {
        ZeeConfig.autoHearthOnStranger = false;

        try{

            isHarvestDone = false;
            while(busy && !isHarvestDone) {
                println("> harvesting");
                ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"harvesting",0,255,255,255,10);
                waitPlayerIdleFor(2000);
                if(inventoryHasSeeds()) {
                    if (ZeeFarmingManager.farmerRbSeeds == RB_SEEDS_STORE){
                        storeSeedsInBarrel();
                    }else if(ZeeFarmingManager.farmerRbSeeds == RB_SEEDS_DROP) {
                        inv.dropItemsByName(lastItemNameSeed);
                    }else if(ZeeFarmingManager.farmerRbSeeds == RB_SEEDS_WAIT) {
                        resetInitialState();
                        return;
                    }
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

            if(ZeeFarmingManager.farmerCbReplant) {
                isPlantingDone = false;
                isScytheEquiped = ZeeClickItemManager.isItemEquipped("scythe");
                while (busy && !isPlantingDone) {
                    println("> planting");
                    if (!inventoryHasSeeds()) {
                        if (getSeedsFromBarrel()) {
                            plantSeeds();
                        } else {
                            println("seed barrel empty");
                            isPlantingDone = true;
                        }
                    } else {
                        println("planting done, inv has remaining seeds");
                        isPlantingDone = true;
                    }
                    waitPlayerIdleFor(2000);
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

    //TODO: consider multiple barrels from mapBarrelSeedql
    private boolean getSeedsFromBarrel() {
        try {
            if (lastBarrel == null) {
                println("getSeedsFromBarrel() > no lastBarrel defined");
                return false;
            }

            ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"getting seeds...",0,255,255,255,10);

            //remove harvest cursor
            if(ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_HARVEST)){
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
                println("inv not full");
                if(ZeeClickGobManager.isBarrelEmpty(lastBarrel)){
                    println("barrel empty, find another");
                    List<Gob> barrels = findAnotherSeedBarrel();
                    if(barrels.size()==0) {
                        println("no more seed barrels, planting done");
                        isPlantingDone = true;
                        lastBarrel = null;
                        return false;
                    }else{
                        println("found another seed barrel... call recursive method");
                        lastBarrel = ZeeConfig.getClosestGob(barrels);
                        return getSeedsFromBarrel();//TODO: recursive check
                    }
                }else{
                    println("impossible state?");
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

    public static List<Gob> findAnotherSeedBarrel() {
        println(">find another seed barrel");
        List<Gob> barrels = getBarrels();
        barrels.removeIf(b -> {
            if (ZeeClickGobManager.isBarrelEmpty(b))
                return true;
            if (!isBarrelSameSeeds(b, lastItemNameSeed))
                return true;
            return false;
        });
        return barrels;
    }

    private boolean isInventoryFull() {
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
        WItem[] arr = inv.getWItemsByName(gItemNameSeed).toArray(new WItem[0]);
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
        println("activateCursorPlantGItem()");
        ZeeClickItemManager.gItemAct(gi, UI.MOD_SHIFT);
        return waitCursor(ZeeConfig.CURSOR_HARVEST);
    }

    private boolean inventoryHasSeeds() {
        return inv.getWItemsByName(lastItemNameSeed).size() > 0;
    }

    private int getNumberOfSeedItems(){
        return inv.getWItemsByName(lastItemNameSeed).size();
    }

    public static List<Gob> getBarrels() {
        List<Gob> emptyBarrels = ZeeConfig.findGobsByName("barrel");
        emptyBarrels.removeIf(barrel -> {
            if(ZeeConfig.distanceToPlayer(barrel) > farmerTxtTilesBarrel * TILE_SIZE)
                return true;//remove distant barrels
            ZeeGobColor c = barrel.getattr(ZeeGobColor.class);
            if(c!=null && c.color.color().getRed()==1) {
                return true;//remove inaccessible barrels (red)
            }
            return false;
        });
        return emptyBarrels;
    }

    private void storeSeedsInBarrel() {
        try {
            List<Gob> barrels = getBarrels();
            println("barrels = " + barrels.size());
            if (barrels.size()==0) {
                /*
                    no barrels, drop seeds
                 */
                ZeeConfig.gameUI.msg("No empty barrels, lastBarrel null, dropping seeds.");
                println("No empty barrels close, dropping seeds.");
                inv.dropItemsByName(lastItemNameSeed);

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
                        //still holding item
                        if(ZeeConfig.distanceToPlayer(lastBarrel) > TILE_SIZE*2){
                            //can't reach barrel
                            println("can't reach barrel? mark it red");
                            ZeeConfig.addGobColor(lastBarrel,255,0,0,255);
                            ZeeConfig.removeGobText(lastBarrel);
                            //ZeeConfig.addGobText(lastBarrel,"inaccessible",255,0,0,255,10);
                        }else{
                            //barrel full
                            println("barrel full? mark it blue");
                            ZeeConfig.addGobColor(lastBarrel,0,0,255,255);
                            ZeeConfig.removeGobText(lastBarrel);
                            //ZeeConfig.addGobText(lastBarrel,"full",0,0,255,255,10);
                        }
                        lastBarrel = null;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getBarrelText() {
        return getSeedName() +" q"+ Inventory.getQuality(gItem).intValue();
    }

    public static String getSeedName() {
        return lastItemNameSeed.replace("seed-","");
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
        ZeeFarmingManager.farmerGobCrop = gobCrop;
        ZeeConfig.farmerMode = true;

        if(windowManager ==null){

            windowManager = new ZeeWindow(new Coord(300,120), "Farming manager"){
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
                { a = ZeeFarmingManager.farmerCbReplant;  }
                public void set(boolean val) {
                    ZeeFarmingManager.farmerCbReplant = val;
                    a = val;
                    Utils.setprefb("farmerCbPlant",val);
                }
            }, 0, 7);


            // radiogroup seeds
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


            // barrel tiles
            wdg = windowManager.add(new Label("Max tiles to barrel: "), 0, 50);
            ZeeFarmingManager.textEntryTilesBarrel = new TextEntry(UI.scale(45),""+(int)(ZeeFarmingManager.MAX_BARREL_DIST/MCache.tilesz.x)){
                public boolean keydown(KeyEvent e) {
                    if(!Character.isDigit(e.getKeyChar()) && !ZeeConfig.isControlKey(e.getKeyCode()))
                        return false;
                    return super.keydown(e);
                }
                public void changed(ReadLine buf) {
                    if(!buf.line().isEmpty()) {
                        ZeeFarmingManager.farmerTxtTilesBarrel = Integer.parseInt(buf.line());
                        Utils.setprefi("farmerTxtTilesBarrel", ZeeFarmingManager.farmerTxtTilesBarrel);
                    }
                    super.changed(buf);
                }
            };
            wdg = windowManager.add(ZeeFarmingManager.textEntryTilesBarrel, 95, 50-3);
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(45),"test"), 145,50-4);
            ZeeFarmingManager.textEntryTilesBarrel.settext(""+ZeeFarmingManager.farmerTxtTilesBarrel);

            //add bottom note
            wdg = windowManager.add(new Label("(no path-find, remove field obstacles, surround with barrels)"), 0, 120-15);

            //add window
            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));

        }else {
            windowManager.show();
        }
    }

}

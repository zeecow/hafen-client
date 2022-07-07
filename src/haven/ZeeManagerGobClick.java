package haven;


import haven.resutil.WaterTile;

import java.util.ArrayList;
import java.util.List;

import static haven.OCache.posres;

public class ZeeManagerGobClick extends ZeeThread{

    public static final int OVERLAY_ID_AGGRO = 1341;

    Coord coordPc;
    Coord2d coordMc;
    Gob gob;
    String gobName;
    boolean isGroundClick;

    public static float camAngleStart, camAngleEnd, camAngleDiff;
    public static long clickStartMs, clickEndMs, clickDiffMs;
    public static boolean barrelLabelOn = false;
    public static boolean isRemovingAllTrees, isDestroyingAllTreelogs;
    private static ArrayList<Gob> treesForRemoval, treelogsForDestruction;
    private static Gob currentRemovingTree, currentDestroyingTreelog;

    public ZeeManagerGobClick(Coord pc, Coord2d mc, Gob gobClicked) {
        coordPc = pc;
        coordMc = mc;
        gob = gobClicked;
        isGroundClick = (gob==null);
        gobName = isGroundClick ? "" : gob.getres().name;
        ZeeConfig.getMainInventory();
    }

    public static void checkMidClickGob(Coord pc, Coord2d mc, Gob gob, String gobName) {

        clickDiffMs = clickEndMs - clickStartMs;

        //println(clickDiffMs+"ms > "+gobName + (gob==null ? "" : " dist="+ZeeConfig.distanceToPlayer(gob)));
        //if (gob!=null) println(gobName + " poses = "+ZeeConfig.getGobPoses(gob));

        if (isLongMidClick()) {
            /*
                long mid-clicks
             */
            new ZeeManagerGobClick(pc,mc,gob).start();
        }
        else {
            /*
                short mid-clicks
             */
            if(gob==null) {//ground clicks
                if (ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_INSPECT))
                    ZeeConfig.msg(ZeeConfig.getTileResName(mc));
            } else if (ZeeConfig.isPlayerHoldingItem()) {
                clickedGobHoldingItem(gob,gobName);
            } else if (isGobTrellisPlant(gobName)) {
                new ZeeThread() {
                    public void run() {
                        harvestOneTrellis(gob);
                    }
                }.start();
            } else if (isGobGroundItem(gobName)) {
                gobClick(gob,3, UI.MOD_SHIFT);//pick up all items (shift + rclick)
                if (ZeeConfig.pilerMode)
                    ZeeManagerStockpile.checkGroundItemClicked(gobName);
            } else if (isGobFireSource(gobName)) {
                new ZeeThread() {
                    public void run() {
                        if (pickupTorch())
                            itemActGob(gob,0);
                    }
                }.start();
            } else if (isGobHorse(gobName)) {
                new ZeeThread() {
                    public void run() {
                        mountHorse(gob);
                    }
                }.start();
            } else if (gobName.endsWith("/barrel")) {
                if (barrelLabelOn)
                    ZeeManagerFarmer.testBarrelsTilesClear();
                else
                    ZeeManagerFarmer.testBarrelsTiles(true);
                barrelLabelOn = !barrelLabelOn;
            } else if (gobName.endsWith("/dreca")) { // dream catcher
                new ZeeThread() {
                    public void run() {
                        twoDreamsPlease(gob);
                    }
                }.start();
            } else if (isGobMineSupport(gobName)) {
                ZeeConfig.toggleMineSupport();
            } else if(gobName.endsWith("/knarr") || gobName.endsWith("/snekkja")) {
                new ZeeThread() {
                    public void run() {
                        clickGobPetal(gob,"Cargo");
                    }
                }.start();
            }else if(ZeeConfig.isAggressive(gobName)){
                toggleOverlayAggro(gob);
            }else if (isInspectGob(gobName)) {
                inspectGob(gob);
            }
        }
    }

    public void run() {
        try {
            if (isLongMidClick()){
                if (isRemovingAllTrees && isGobTree(gobName)) {
                    scheduleRemoveTree(gob);
                } else if (isDestroyingAllTreelogs && isGobTreeLog(gobName)) {
                    scheduleDestroyTreelog(gob);
                } else if (!isGroundClick && !ZeeConfig.isPlayerHoldingItem() && showGobFlowerMenu()) {
                    //ZeeFlowerMenu
                } else if (isGobCrop(gobName)) {
                    if (!ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_HARVEST))
                        gobClick(gob, 3, UI.MOD_SHIFT);//activate cursor harvest if needed
                } else if (isGobStockpile(gobName) && ZeeConfig.isPlayerCarryingWheelbarrow()) {
                    ZeeManagerStockpile.useWheelbarrowAtStockpile(gob);
                } else if (isGobStockpile(gobName) || gobName.endsWith("/dframe")) {
                    gobClick(gob,3, UI.MOD_SHIFT);//pick up all items (shift + rclick)
                } else if (isGobTreeStump(gobName)) {
                    removeStump(gob);
                } else if (ZeeConfig.isPlayerHoldingItem() && gobName.endsWith("/barrel")) {
                    if (ZeeManagerFarmer.isBarrelEmpty(gob))
                        itemActGob(gob,UI.MOD_SHIFT);//shift+rclick
                    else
                        itemActGob(gob,3);//ctrl+shift+rclick
                } else if (isGroundClick){
                    if (isWaterTile(coordMc))
                        inspectWaterAt(coordMc);
                    else if (ZeeConfig.isPlayerMountingHorse())
                        dismountHorse(coordMc);
                    else if (ZeeConfig.isPlayerCarryingWheelbarrow())
                        ZeeManagerStockpile.unloadWheelbarrowStockpileAtGround(coordMc.floor(posres));
                } else if (ZeeConfig.isPlayerCarryingWheelbarrow()) {
                    if (isGobHorse(gobName))
                        mountHorseCarryingWheelbarrow(gob);
                    else
                        unloadWheelbarrowAtGob(gob);
                } else if (!gobName.endsWith("/wheelbarrow") && ZeeConfig.isPlayerDrivingWheelbarrow()) {
                    if (isGobHorse(gobName))
                        mountHorseDrivingWheelbarrow(gob);
                    else if (isGobGate(gobName))
                        openGateWheelbarrow(gob);
                    else if (gobName.endsWith("/cart"))
                        liftAndStoreWheelbarrow(gob);
                } else if(gobName.endsWith("/knarr") || gobName.endsWith("/snekkja")) {
                    clickGobPetal(gob,"Man the helm");
                } else if (isGobLiftable(gobName) || isGobBush(gobName)) {
                    liftGob(gob);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void inspectWaterAt(Coord2d coordMc) {

        // require wooden cup
        Inventory inv = ZeeConfig.getMainInventory();
        List<WItem> cups = inv.getWItemsByName("/woodencup");
        if (cups==null || cups.size()==0){
            ZeeConfig.msg("need woodencup to inspect water");
            return;
        }

        // pickup inv cup, click water, return cup
        WItem cup = cups.get(0);
        ZeeManagerItemClick.pickUpItem(cup);
        ZeeConfig.itemActTile(coordMc.floor(posres));
        waitPlayerIdleFor(1);

        // show msg
        String msg = ZeeManagerItemClick.getHoldingItemContentsNameQl();
        ZeeConfig.msg(msg);
        new ZeeThread(){
            public void run() {
                ZeeConfig.addPlayerText(msg);
                // wait click before removing player text
                waitMapClick();
                ZeeConfig.removePlayerText();
            }
        }.start();
        //haven.ChatUI$MultiChat@dd1ed65 ; msg ; ["hello world"]

        //empty cup
        Coord cupSlot = ZeeManagerItemClick.dropHoldingItemToInvAndRetCoord(inv);
        if (cupSlot!=null) {
            cup = inv.getItemBySlotCoord(cupSlot);
            ZeeManagerItemClick.clickItemPetal(cup, "Empty");
        }
    }

    public static boolean isWaterTile(Coord2d coordMc) {
        Tiler t = ZeeConfig.getTilerAt(coordMc);
        return t!=null && t instanceof WaterTile;
    }


    public static void checkRightClickGob(Coord pc, Coord2d mc, Gob gob, String gobName) {

        // click barrel transfer
        if (gobName.endsWith("/barrel") && ZeeConfig.getPlayerPoses().contains(ZeeConfig.POSE_PLAYER_LIFT)) {
            new ZeeThread() {
                public void run() {
                    try {
                        if(!waitPlayerDistToGob(gob,15))
                            return;
                        sleep(555);
                        String barrelName = ZeeConfig.getBarrelOverlayBasename(gob);
                        if (!barrelName.isEmpty())
                            ZeeConfig.addGobTextTemp(gob, barrelName);
                        Gob carryingBarrel = ZeeConfig.isPlayerLiftingGob("/barrel");
                        if (carryingBarrel!=null) {
                            barrelName = ZeeConfig.getBarrelOverlayBasename(carryingBarrel);
                            if (!barrelName.isEmpty())
                                ZeeConfig.addGobTextTemp(carryingBarrel, barrelName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        // auto gridline if driving wheelbarrel
        else if(gobName.endsWith("/wheelbarrow")){
            new ZeeThread() {
                public void run() {
                    try {
                        if(waitPlayerPose(ZeeConfig.POSE_PLAYER_DRIVE_WHEELBARROW)){
                            if (ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_HAND)){
                                ZeeConfig.gameUI.map.showgrid(true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        // while driving wheelbarrow: open gate, store wb on cart
        else if (gobNameEndsWith(gobName,"cart,gate") && ZeeConfig.isPlayerDrivingWheelbarrow()){
            new ZeeThread() {
                public void run() {
                    if (isGobGate(gobName))
                        openGateWheelbarrow(gob);
                    else if (gobName.endsWith("/cart"))
                        liftAndStoreWheelbarrow(gob);
                }
            }.start();
        }
        // gob requires unmounting horse (rope in inventory)
        else if (isGobRequireUmountHorse(gobName) && ZeeConfig.isPlayerMountingHorse()){
            if (ZeeConfig.getMainInventory().countItemsByName("/rope") > 0) {
                new ZeeThread() {
                    public void run() {
                        dismountHorse(mc);
                        if (isGobHouse(gobName))
                            gobClick(gob,3,0,16);//gob's door?
                        else
                            gobClick(gob,3);
                    }
                }.start();
            }
        }
        // use wheelbarrow on stockpile, dismount if necessary
        else if ( isGobStockpile(gobName) && ZeeConfig.isPlayerCarryingWheelbarrow()){
            new ZeeThread() {
                public void run() {
                    if (ZeeConfig.isPlayerMountingHorse())
                        dismountHorse(mc);
                    unloadWheelbarrowAtGob(gob);
                }
            }.start();
        }
        // mount horse while carrying/driving wheelbarrow
        else if ( isGobHorse(gobName) && (ZeeConfig.isPlayerCarryingWheelbarrow() || ZeeConfig.isPlayerDrivingWheelbarrow())){
            new ZeeThread() {
                public void run() {
                    if (ZeeConfig.isPlayerMountingHorse())
                        dismountHorse(mc);//horse to horse?
                    if (ZeeConfig.isPlayerDrivingWheelbarrow())
                        mountHorseDrivingWheelbarrow(gob);
                    else
                        mountHorseCarryingWheelbarrow(gob);
                }
            }.start();
        }

    }

    static boolean isGobRequireUmountHorse(String gobName) {
        return isGobHouseInnerDoor(gobName) || isGobHouse(gobName) || isGobChair(gobName)
                || gobNameEndsWith(gobName,
                    "/upstairs,/downstairs,/cavein,/caveout,/burrow,/igloo," +
                        "/wheelbarrow,/loom,/cauldron,/churn,/swheel,/ropewalk," +
                        "/meatgrinder,/potterswheel,/quern,/plow"
                );
    }

    static boolean isGobChair(String gobName) {
        String list = "/chair-rustic,/stonethrone,/royalthrone";
        return gobNameEndsWith(gobName,list);
    }

    public static boolean isGobHouseInnerDoor(String gobName){
        return gobName.endsWith("-door");
    }

    public static boolean isGobHouse(String gobName) {
        String list = "/logcabin,/timberhouse,/stonestead,/stonemansion,/stonetower,/greathall,/windmill";
        return gobNameEndsWith(gobName,list);
    }

    private void scheduleDestroyTreelog(Gob treelog) {
        if (treelogsForDestruction==null) {
            treelogsForDestruction = new ArrayList<Gob>();
        }

        if (treelogsForDestruction.contains(treelog)) {
            // remove treelog from queue
            removeScheduledTreelog(treelog);
        } else if (!currentDestroyingTreelog.equals(treelog)){
            // add treelog to queue
            treelogsForDestruction.add(treelog);
            ZeeConfig.addGobText(treelog,"des "+treelogsForDestruction.size());
        }
    }

    private static Gob removeScheduledTreelog(Gob treelog) {
        // remove treelog from queue
        treelogsForDestruction.remove(treelog);
        ZeeConfig.removeGobText(treelog);
        // update queue gob's texts
        for (int i = 0; i < treelogsForDestruction.size(); i++) {
            ZeeConfig.addGobText(treelogsForDestruction.get(i),"des "+(i+1));
        }
        return treelog;
    }


    private void scheduleRemoveTree(Gob tree) {
        if (treesForRemoval==null) {
            treesForRemoval = new ArrayList<Gob>();
        }

        if (treesForRemoval.contains(tree)) {
            // remove tree from queue
            removeScheduledTree(tree);
        } else if (!currentRemovingTree.equals(tree)){
            // add tree to queue
            treesForRemoval.add(tree);
            ZeeConfig.addGobText(tree,"rem "+treesForRemoval.size());
        }
    }

    private static Gob removeScheduledTree(Gob tree) {
        // remove tree from queue
        treesForRemoval.remove(tree);
        ZeeConfig.removeGobText(tree);
        // update queue gob's texts
        for (int i = 0; i < treesForRemoval.size(); i++) {
            ZeeConfig.addGobText(treesForRemoval.get(i),"rem "+(i+1));
        }
        return tree;
    }

    private static void toggleOverlayAggro(Gob gob) {
        Gob.Overlay ol = gob.findol(OVERLAY_ID_AGGRO);
        if (ol!=null) {
            //remove all aggro radius
            ZeeConfig.findGobsByNameStartsWith("gfx/kritter/").forEach(gob1 -> {
                if (ZeeConfig.isAggressive(gob1.getres().name)) {
                    Gob.Overlay ol1 = gob1.findol(OVERLAY_ID_AGGRO);
                    if (ol1!=null)
                        ol1.remove();
                }
            });
        }
        else if (ZeeConfig.aggroRadiusTiles > 0) {
            //add all aggro radius
            ZeeConfig.findGobsByNameStartsWith("gfx/kritter/").forEach(gob1 -> {
                if (ZeeConfig.isAggressive(gob1.getres().name)) {
                    gob1.addol(new Gob.Overlay(gob1, new ZeeGobRadius(gob1, null, ZeeConfig.aggroRadiusTiles * MCache.tilesz2.y), ZeeManagerGobClick.OVERLAY_ID_AGGRO));
                }
            });
        }
    }

    private static void unloadWheelbarrowAtGob(Gob gob) {
        ZeeManagerStockpile.useWheelbarrowAtStockpile(gob);
    }

    public static void dismountHorse(Coord2d coordMc) {
        Gob horse = ZeeConfig.getClosestGobName("gfx/kritter/horse/");
        ZeeConfig.clickCoord(coordMc.floor(posres),1,UI.MOD_CTRL);
        waitPlayerDismounted(horse);
        if (!ZeeConfig.isPlayerMountingHorse()) {
            ZeeConfig.setPlayerSpeed(ZeeConfig.PLAYER_SPEED_2);
        }
    }

    public static void mountHorse(Gob horse){
        int playerSpeed = ZeeConfig.getPlayerSpeed();
        clickGobPetal(horse,"Giddyup!");
        waitPlayerMounted(horse);
        if (ZeeConfig.isPlayerMountingHorse()) {
            if (playerSpeed <= ZeeConfig.PLAYER_SPEED_1)
                ZeeConfig.setPlayerSpeed(ZeeConfig.PLAYER_SPEED_1);//min auto horse speed
            else
                ZeeConfig.setPlayerSpeed(ZeeConfig.PLAYER_SPEED_2);//max auto horse speed
        }
    }

    private static void clickedGobHoldingItem(Gob gob, String gobName) {
        if (isGobStockpile(gobName))
            itemActGob(gob,UI.MOD_SHIFT);//try piling all items
        else
            gobClick(gob,3,0); // try ctrl+click simulation
    }

    private static void twoDreamsPlease(Gob gob) {
        if(clickGobPetal(gob,"Harvest")) {
            waitPlayerDistToGob(gob,15);
            waitNoFlowerMenu();
            if(clickGobPetal(gob,"Harvest"))
                waitNoFlowerMenu();
        }
    }

    public static boolean pickupTorch() {
        if (ZeeManagerItemClick.pickupBeltItem("/torch")) {
            return true;
        }else if(ZeeManagerItemClick.pickupHandItem("/torch")){
            return true;
        }else if (ZeeManagerItemClick.pickUpInvItem(ZeeConfig.getMainInventory(),"/torch")){
            return true;
        }
        return false;
    }


    public static void gobZeeMenuClicked(Gob gob, String petalName){

        String gobName = gob.getres().name;

        if (petalName.equals(ZeeFlowerMenu.STRPETAL_AUTOBUTCH_BIGDEADANIMAL)){
            autoButchBigDeadAnimal(gob);
        }
        else if (petalName.equals(ZeeFlowerMenu.STRPETAL_LIFTUPGOB)){
            liftGob(gob);
        }
        else if(gobName.endsWith("terobjs/oven")) {
            addFuelGobMenu(gob,petalName);
        }
        else if(gobName.endsWith("terobjs/smelter")){
            addFuelGobMenu(gob,petalName);
        }
        else if (isGobTrellisPlant(gobName)){
            if(petalName.equals(ZeeFlowerMenu.STRPETAL_REMOVEPLANT)) {
                destroyGob(gob);
            }else if (petalName.equals(ZeeFlowerMenu.STRPETAL_REMOVEALLPLANTS)){
                removeAllTrellisPlants(gob);
            }
        }
        else if(isGobTree(gobName)){
            if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_REMOVETREEANDSTUMP)
                || petalName.contentEquals(ZeeFlowerMenu.STRPETAL_REMOVEALLTREES))
            {
                removeTreeAndStump(gob, petalName);
            }
            else if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_INSPECT)) {//towercap case
                inspectGob(gob);
            }
        }
        else if (isGobCrop(gobName)) {
            if (petalName.equals(ZeeFlowerMenu.STRPETAL_SEEDFARMER)) {
                ZeeManagerFarmer.showWindow(gob);
            }
            else if (petalName.equals(ZeeFlowerMenu.STRPETAL_CURSORHARVEST)) {
                if (!ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_HARVEST))
                    gobClick(gob, 3, UI.MOD_SHIFT);
            }
        }
        else if (isBarrelTakeAll(gob)) {
            if (petalName.equals(ZeeFlowerMenu.STRPETAL_BARRELTAKEALL)) {
                barrelTakeAllSeeds(gob);
            }
        }
        else if ( petalName.equals(ZeeFlowerMenu.STRPETAL_DESTROYTREELOG3)
            || petalName.equals(ZeeFlowerMenu.STRPETAL_DESTROYTREELOG5)
            || petalName.contentEquals(ZeeFlowerMenu.STRPETAL_DESTROYALL))
        {
            destroyTreelogs(gob,petalName);
        }
        else{
            println("chooseGobFlowerMenu > unkown case");
        }
    }

    public static void autoButchBigDeadAnimal(Gob deadAnimal) {
        new ZeeThread() {
            public void run() {
                boolean butcherBackup = ZeeConfig.butcherMode;
                ZeeConfig.butcherAutoList = ZeeConfig.DEF_BUTCH_AUTO_LIST;
                try{
                    ZeeConfig.addPlayerText("autobutch");
                    ZeeConfig.lastMapViewClickButton = 2;//prepare for clickCancelTask()
                    while (!ZeeConfig.isTaskCanceledByGroundClick() && gobExistsBecauseFlowermenu(deadAnimal)) {

                        //prepare settings
                        ZeeConfig.lastInvItemMs = 0;
                        ZeeConfig.butcherMode = true;
                        ZeeConfig.autoClickMenuOption = false;

                        //click gob
                        gobClick(deadAnimal,3);

                        //wait not butching
                        waitNotPlayerPose(ZeeConfig.POSE_PLAYER_BUTCH);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                ZeeConfig.butcherMode = butcherBackup;
                ZeeConfig.autoClickMenuOption = Utils.getprefb("autoClickMenuOption", true);
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    private static void destroyTreelogs(Gob firstTreelog, String petalName) {
        if (!ZeeManagerItemClick.isItemEquipped("/bonesaw") || ZeeManagerItemClick.isItemEquipped("/saw-m")){
            ZeeConfig.msg("need bone saw equipped, no metal saw");
            return;
        }
        Gob treelog = firstTreelog;
        int logs = 2;
        try {
            waitNoFlowerMenu();
            String treelogName = treelog.getres().name;
            if (petalName.equals(ZeeFlowerMenu.STRPETAL_DESTROYTREELOG3)) {
                logs = 3;
            } else if (petalName.equals(ZeeFlowerMenu.STRPETAL_DESTROYTREELOG5)) {
                logs = 5;
            } else if (petalName.equals(ZeeFlowerMenu.STRPETAL_DESTROYALL)) {
                isDestroyingAllTreelogs = true;
                logs = 999;
            }
            ZeeConfig.dropBoards = true;
            ZeeConfig.lastMapViewClickButton = 2;//prepare for cancel click
            while ( logs > 0  &&  !ZeeConfig.isTaskCanceledByGroundClick() ) {
                ZeeConfig.addPlayerText("treelogs "+logs);
                if (!clickGobPetal(treelog,"Make boards")){
                    println("can't click treelog = "+treelog);
                    logs = -1;
                    currentDestroyingTreelog = null;
                    continue;
                }
                currentDestroyingTreelog = treelog;
                waitPlayerIdlePose();
                if (!ZeeConfig.isTaskCanceledByGroundClick()){
                    logs--;
                    if (isDestroyingAllTreelogs){
                        // destroy all, treelog queue is present
                        if (treelogsForDestruction != null) {
                            if (treelogsForDestruction.size() > 0) {
                                treelog = removeScheduledTreelog(treelogsForDestruction.remove(0));
                            } else {
                                //stop destroying when queue consumed
                                println("logs -1, treelogsForDestruction empty");
                                logs = -1;
                            }
                        }else{
                            // destroy all, no treelog queue
                            treelog = getClosestTreeLog();
                        }
                    } else {
                        // destroy 3 or 5 same type treelogs
                        treelog = ZeeConfig.getClosestGobName(treelogName);
                    }
                }else{
                    if (ZeeConfig.isTaskCanceledByGroundClick()) {
                        ZeeConfig.msg("destroy treelog canceled by click");
                        println("destroy treelog canceled by click");
                    }else
                        println("destreelog canceled by gobHasFlowermenu?");
                    logs = -1;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        isDestroyingAllTreelogs = false;
        ZeeConfig.dropBoards = false;
        currentDestroyingTreelog = null;
        if (treelogsForDestruction!=null)
            treelogsForDestruction.clear();
        treelogsForDestruction = null;
        ZeeConfig.removePlayerText();
    }

    public static Gob getClosestTree() {
        List<Gob> list = ZeeConfig.findGobsByNameContains("/trees/");
        list.removeIf(gob1 -> !isGobTree(gob1.getres().name));
        return ZeeConfig.getClosestGob(list);
    }

    public static Gob getClosestTreeLog() {
        List<Gob> list = ZeeConfig.findGobsByNameContains("/trees/");
        list.removeIf(gob1 -> !isGobTreeLog(gob1.getres().name));
        return ZeeConfig.getClosestGob(list);
    }

    private boolean showGobFlowerMenu(){

        boolean showMenu = true;
        ZeeFlowerMenu menu = null;
        ArrayList<String> opts;//petals array

        if (isGobButchable(gobName) && isGobKnocked(gob)) {
            menu = new ZeeFlowerMenu(gob, ZeeFlowerMenu.STRPETAL_AUTOBUTCH_BIGDEADANIMAL, ZeeFlowerMenu.STRPETAL_LIFTUPGOB);
        }
        else if(gobName.endsWith("terobjs/oven")){
            menu = new ZeeFlowerMenu(gob, ZeeFlowerMenu.STRPETAL_ADD4BRANCH);
        }
        else if(gobName.endsWith("terobjs/smelter")){
            menu = new ZeeFlowerMenu(gob,ZeeFlowerMenu.STRPETAL_ADD9COAL, ZeeFlowerMenu.STRPETAL_ADD12COAL);
        }
        else if (isGobTrellisPlant(gobName)){
            menu = new ZeeFlowerMenu(gob,ZeeFlowerMenu.STRPETAL_REMOVEPLANT, ZeeFlowerMenu.STRPETAL_REMOVEALLPLANTS);
        }
        else if (isGobTree(gobName)){
            opts = new ArrayList<String>();
            opts.add(ZeeFlowerMenu.STRPETAL_REMOVETREEANDSTUMP);
            opts.add(ZeeFlowerMenu.STRPETAL_REMOVEALLTREES);
            if (gobName.endsWith("/towercap"))
                opts.add(ZeeFlowerMenu.STRPETAL_INSPECT);
            menu = new ZeeFlowerMenu(gob, opts.toArray(String[]::new));
        }
        else if (isGobCrop(gobName)) {
            menu = new ZeeFlowerMenu(gob,ZeeFlowerMenu.STRPETAL_SEEDFARMER, ZeeFlowerMenu.STRPETAL_CURSORHARVEST);
        }
        else if (isBarrelTakeAll(gob)) {
            menu = new ZeeFlowerMenu(gob,ZeeFlowerMenu.STRPETAL_BARRELTAKEALL, ZeeFlowerMenu.STRPETAL_LIFTUPGOB);
        }
        else if (isDestroyTreelog()) {
            menu = new ZeeFlowerMenu( gob, ZeeFlowerMenu.STRPETAL_LIFTUPGOB,
                ZeeFlowerMenu.STRPETAL_DESTROYTREELOG3,
                ZeeFlowerMenu.STRPETAL_DESTROYTREELOG5,
                ZeeFlowerMenu.STRPETAL_DESTROYALL
            );
        }else{
            showMenu = false;
            //println("showGobFlowerMenu() > unkown case");
        }

        if (showMenu) {
            ZeeConfig.gameUI.ui.root.add(menu, coordPc);
        }

        return showMenu;
    }

    public static boolean isGobKnocked(Gob gob){
        String poses = ZeeConfig.getGobPoses(gob);
        //println("isGobKnocked > "+poses);
        return poses.contains("/knock") || poses.endsWith("-knock");
    }

    static boolean isGobDeadAnimal;
    private boolean isGobBigDeadAnimal_thread() {
        try{
            ZeeThread zt = new ZeeThread() {
                public void run() {
                    gobClick(gob, 3);
                    if (!waitFlowerMenu()) {//no menu detected
                        isGobDeadAnimal = false;
                        return;
                    }
                    FlowerMenu fm = getFlowerMenu();
                    for (int i = 0; i < fm.opts.length; i++) {
                        //if animal gob has butch menu, means is dead
                        if (ZeeConfig.DEF_BUTCH_AUTO_LIST.contains(fm.opts[i].name)){
                            isGobDeadAnimal = true;
                            break;
                        }
                    }
                    //close menu before returning
                    ZeeConfig.cancelFlowerMenu();
                    waitNoFlowerMenu();
                }
            };

            //disable automenu settings before thread clicks gob
            ZeeConfig.autoClickMenuOption = false;
            boolean butchBackup = ZeeConfig.butcherMode;
            ZeeConfig.butcherMode = false;

            //start thread and wait it finish
            isGobDeadAnimal = false;
            zt.start();
            zt.join();//wait thread

            //restore automenu settings
            ZeeConfig.autoClickMenuOption = Utils.getprefb("autoClickMenuOption", true);
            ZeeConfig.butcherMode = butchBackup;

            return isGobDeadAnimal;

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean isDestroyTreelog() {
        if(isGobTreeLog(gobName) && ZeeManagerItemClick.isItemEquipped("bonesaw"))
            return true;
        return false;
    }

    private static void mountHorseDrivingWheelbarrow(Gob gob){
        Gob horse = gob;
        try{
            //waitNoFlowerMenu();
            ZeeConfig.addPlayerText("mounting");
            Gob wb = ZeeConfig.getClosestGobName("gfx/terobjs/vehicle/wheelbarrow");
            if (wb == null) {
                ZeeConfig.msg("no wheelbarrow close 1");
            } else {
                Coord pc = ZeeConfig.getPlayerCoord();
                Coord subc = ZeeConfig.getCoordGob(horse).sub(pc);
                int xsignal, ysignal;
                xsignal = subc.x >= 0 ? -1 : 1;//switch 1s to change direction relative to horse
                ysignal = subc.y >= 0 ? -1 : 1;
                //try position wheelbarrow away from horse direction
                ZeeConfig.clickCoord(pc.add(xsignal * 500, ysignal * 500), 1);
                sleep(PING_MS);
                gobClick(wb,3);//stop driving wheelbarrow
                sleep(PING_MS);
                mountHorse(horse);
                waitPlayerMounted(horse);
                liftGob(wb);// lift wheelbarrow
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }

    private static void mountHorseCarryingWheelbarrow(Gob gob) {
        Gob horse = gob;
        try {
            //waitNoFlowerMenu();
            ZeeConfig.addPlayerText("mounting");
            Gob wb = ZeeConfig.getClosestGobName("gfx/terobjs/vehicle/wheelbarrow");
            if (wb == null) {
                ZeeConfig.msg("no wheelbarrow close 2");
            } else {
                Coord pc = ZeeConfig.getPlayerCoord();
                Coord subc = ZeeConfig.getCoordGob(horse).sub(pc);
                int xsignal, ysignal;
                xsignal = subc.x >= 0 ? -1 : 1;
                ysignal = subc.y >= 0 ? -1 : 1;
                //try to drop wheelbarrow away from horse direction
                ZeeConfig.clickCoord(pc.add(xsignal * 500, ysignal * 500), 3);
                sleep(500);
                //if drop wb success
                if (!ZeeConfig.isPlayerCarryingWheelbarrow()) {
                    ZeeConfig.clickRemoveCursor();//remove hand cursor
                    mountHorse(horse);
                    waitPlayerMounted(horse);
                    liftGob(wb);//lift wheelbarrow
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }

    private static void liftAndStoreWheelbarrow(Gob gob){
        Gob storage = gob;
        try {
            waitNoFlowerMenu();
            ZeeConfig.addPlayerText("storing");
            Gob wb = ZeeConfig.getClosestGobName("gfx/terobjs/vehicle/wheelbarrow");
            if (wb==null){
                ZeeConfig.msg("no wheelbarrow close 3");
            }else {
                double dist;
                ZeeConfig.clickRemoveCursor();//remove hand cursor
                liftGob(wb);
                dist = ZeeConfig.distanceToPlayer(wb);
                if (dist==0) {
                    gobClick(storage, 3);// click storage
                    waitPlayerIdleVelocity();
                }else{
                    ZeeConfig.msg("wheelbarrow unreachable?");//impossible case?
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }

    private static void openGateWheelbarrow(Gob gob) {
        // gfx/terobjs/vehicle/wheelbarrow
        Gob gate = gob;
        try {
            waitNoFlowerMenu();
            ZeeConfig.addPlayerText("wheeling");
            Gob wb = ZeeConfig.getClosestGobName("gfx/terobjs/vehicle/wheelbarrow");
            if (wb==null){
                ZeeConfig.msg("no wheelbarrow close 4");
            }else {
                double dist;
                liftGob(wb);
                sleep(PING_MS);
                dist = ZeeConfig.distanceToPlayer(wb);
                if (dist==0) {//lifted wb
                    gobClick(gate, 3);
                    waitPlayerIdleVelocity();
                }else{
                    //impossible case?
                    ZeeConfig.msg("wheelbarrow unreachable?");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }

    public static boolean isGobGate(String gobName) {
        if (gobName.startsWith("gfx/terobjs/arch/") && gobName.endsWith("gate"))
            return true;
        return false;
    }


    // barrel is empty if has no overlays ("gfx/terobjs/barrel-flax")
    public static boolean isBarrelEmpty(Gob barrel){
        return ZeeManagerGobClick.getOverlayNames(barrel).isEmpty();
    }

    private static void removeAllTrellisPlants(Gob firstPlant) {
        Gob closestPlant = null;
        try{
            String gobName = firstPlant.getres().basename();
            ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"rem "+gobName);
            waitNoFlowerMenu();
            waitPlayerIdleFor(1);
            closestPlant = firstPlant;
            double dist;
            do{
                if (ZeeConfig.isTaskCanceledByGroundClick()) {
                    // cancel if clicked right/left button
                    println("cancel click");
                    break;
                }
                ZeeConfig.addGobText(closestPlant,"plant");
                destroyGob(closestPlant);
                if(!waitGobRemovedOrCancelClick(closestPlant))
                    break;
                closestPlant = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameContains(gobName));
                dist = ZeeConfig.distanceToPlayer(closestPlant);
                //println("dist "+dist);
            }while(dist < 25);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
        ZeeConfig.removeGobText(closestPlant);
    }

    public static void removeTreeAndStump(Gob tree, String petalName){
        try{
            if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_REMOVEALLTREES)) {
                ZeeConfig.addPlayerText("removing all trees");
                isRemovingAllTrees = true;
            }else {
                ZeeConfig.addPlayerText("removing tree & stump");
            }
            waitNoFlowerMenu();
            ZeeManagerItemClick.equipAxeChopTree();
            ZeeConfig.lastMapViewClickButton = 2;//prepare for cancel click
            while (tree!=null && !ZeeConfig.isTaskCanceledByGroundClick()) {
                clickGobPetal(tree, "Chop");
                currentRemovingTree = tree;
                if (waitPlayerIdlePose() && !ZeeConfig.isTaskCanceledByGroundClick()) {//waitPlayerIdleFor(2)
                    sleep(1500);//wait new stump loading
                    Gob stump = ZeeConfig.getClosestGob(ZeeConfig.findGobsByNameEndsWith("stump"));
                    if (stump != null && ZeeConfig.distanceToPlayer(stump) < 25) {
                        ZeeConfig.addGobText(stump, "stump");
                        removeStump(stump);
                        //waitPlayerIdleFor(2);
                        waitPlayerIdlePose();
                    } else {
                        println("no stump close");
                    }
                    if (isRemovingAllTrees) {
                        if (treesForRemoval!=null){
                            if (treesForRemoval.size()>0)
                                tree = removeScheduledTree(treesForRemoval.remove(0));
                            else
                                tree = null; // stop removing trees if queue was consumed
                        }else{
                            // remove all trees until player blocked or something
                            tree = getClosestTree();
                        }
                    }else {
                        tree = null;
                    }
                    //println("next tree = "+tree);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRemovingAllTrees = false;
        currentRemovingTree = null;
        if (treesForRemoval!=null)
            treesForRemoval.clear();
        treesForRemoval = null;
        ZeeConfig.removePlayerText();
    }

    public static void removeStump(Gob gob) {
        ZeeManagerItemClick.equipBeltItem("shovel");
        waitItemEquipped("shovel");
        destroyGob(gob);
    }

    public static void addItemsToGob(List<WItem> invItens, int num, Gob gob){
        new ZeeThread(){
            public void run() {
                try{
                    ZeeConfig.addPlayerText("adding");
                    if(invItens.size() < num){
                        ZeeConfig.msgError("Need "+num+" item(s)");
                        return;
                    }
                    boolean exit = false;
                    int added = 0;
                    ZeeConfig.lastMapViewClickButton = 2;//prepare for cancel click
                    while(  !ZeeConfig.isTaskCanceledByGroundClick()
                            && !exit
                            && added < num
                            && invItens.size() > 0)
                    {
                        if(ZeeManagerItemClick.pickUpItem(invItens.get(0))){
                            itemActGob(gob,0);
                            if(waitNotHoldingItem()){
                                invItens.remove(0);
                                added++;
                            }else{
                                ZeeConfig.msgError("Couldn't right click "+gob.getres().basename());
                                exit = true;
                            }
                        }else {
                            ZeeConfig.msgError("Couldn't pickup inventory item");
                            exit = true;
                        }
                    }
                    ZeeConfig.removePlayerText();
                    ZeeConfig.addGobTextTempMs(gob,"Added "+added+" item(s)",3000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void addFuelGobMenu(Gob gob, String petalName) {
        String gobName = gob.getres().name;
        if(gobName.endsWith("oven") && petalName.equals(ZeeFlowerMenu.STRPETAL_ADD4BRANCH)){
            /*
                fuel oven with 4 branches
             */
           List<WItem> branches = ZeeConfig.getMainInventory().getWItemsByName("branch");
           if(branches.size() < 4){
               ZeeConfig.gameUI.msg("Need 4 branches to fuel oven");
               return;
           }
           boolean exit = false;
           int added = 0;
           while(!exit && added<4 && branches.size() > 0){
               if(ZeeManagerItemClick.pickUpItem(branches.get(0))){
                   itemActGob(gob,0);
                   if(waitNotHoldingItem()){
                       branches.remove(0);
                       added++;
                   }else{
                       ZeeConfig.gameUI.msg("Couldn't right click oven");
                       exit = true;
                   }
               }else {
                   ZeeConfig.gameUI.msg("Couldn't pickup branch");
                   exit = true;
               }
           }
           ZeeConfig.gameUI.msg("Added "+added+" branches");
        }
        else if(gobName.endsWith("smelter")){
            /*
                fuel smelter with 9 or 12 coal
             */
            int num = 12;
            if (petalName.equals(ZeeFlowerMenu.STRPETAL_ADD9COAL))
                num = 9;
            final int numCoal = num;
            List<WItem> coal = ZeeConfig.getMainInventory().getWItemsByName("coal");
            if(coal.size() < numCoal){
                ZeeConfig.gameUI.msg("Need "+numCoal+" coal to fuel smelter");
                return;
            }
            boolean exit = false;
            int added = 0;
            while(!exit && added<numCoal && coal.size() > 0){
                if(ZeeManagerItemClick.pickUpItem(coal.get(0))){
                    itemActGob(gob,0);
                    if(waitNotHoldingItem()){
                        coal.remove(0);
                        added++;
                    }else{
                        ZeeConfig.gameUI.msg("Couldn't right click smelter");
                        exit = true;
                    }
                }else {
                    ZeeConfig.gameUI.msg("Couldn't pickup coal");
                    exit = true;
                }
            }
            ZeeConfig.gameUI.msg("Added "+added+" coal");
        }
    }

    private boolean isFuelAction(String gobName) {
        if (gobName.endsWith("oven") || gobName.endsWith("smelter")){
            return true;
        }
        return false;
    }

    private static void harvestOneTrellis(Gob gob) {
        if(ZeeManagerItemClick.pickupBeltItem("scythe")){
            //hold scythe for user unequip it
        }else if(ZeeManagerItemClick.getLeftHandName().endsWith("scythe")){
            //hold scythe for user unequip it
            ZeeManagerItemClick.unequipLeftItem();
        }else{
            //no scythe around, just harvest
            clickGobPetal(gob,"Harvest");
        }
    }

    public static boolean isGobStockpile(String gobName) {
        return gobName.startsWith("gfx/terobjs/stockpile");
    }

    private static boolean isGobGroundItem(String gobName) {
        return gobName.startsWith("gfx/terobjs/items/");
    }

    public static boolean isLongMidClick() {
        return clickDiffMs >= LONG_CLICK_MS;
    }

    public static boolean isShortMidClick() {
        return clickDiffMs < LONG_CLICK_MS;
    }

    private static boolean isInspectGob(String gobName){
        if(isGobTree(gobName) || isGobBush(gobName) || isGobBoulder(gobName))
            return true;
        String list = "/meatgrinder,/potterswheel,/well,/dframe,/smokeshed,"
                +"/smelter,/crucible,/steelcrucible,/fineryforge,/kiln,/tarkiln,/oven,"
                +"/compostbin,/gardenpot,/beehive,/htable,/bed-sturdy,/boughbed,/alchemiststable,"
                +"/gemwheel,/spark,/cauldron,/churn,/chair-rustic,"
                +"/royalthrone,curdingtub,log,/still,/oldtrunk,/anvil,"
                +"/loom,/swheel,knarr,snekkja,dock,/ropewalk,"
                +"/ttub,/cheeserack,/dreca,/glasspaneframe";
        return gobNameEndsWith(gobName, list);
    }


    public static boolean isGobMineSupport(String gobName) {
        String list = "/minebeam,/column,/minesupport,/naturalminesupport,/towercap";
        return gobNameEndsWith(gobName, list);
    }


    private static boolean isGobLiftable(String gobName) {
        if(isGobBoulder(gobName))
            return true;
        String endList = "/meatgrinder,/potterswheel,/iconsign,/rowboat,/dugout,/wheelbarrow,"
                +"/compostbin,/gardenpot,/beehive,/htable,/bed-sturdy,/boughbed,/alchemiststable,"
                +"/gemwheel,/ancestralshrine,/spark,/cauldron,/churn,/wardrobe,"
                +"/table-rustic,/table-stone,/chair-rustic,/stonethrone,/royalthrone,"
                +"/trough,curdingtub,/plow,/barrel,/still,log,/oldtrunk,chest,/anvil,"
                +"/cupboard,/studydesk,/demijohn,/quern,/wreckingball-fold,/loom,/swheel,"
                +"/ttub,/cheeserack,/archerytarget,/dreca,/glasspaneframe,/runestone";
        return gobNameEndsWith(gobName,endList);
    }

    private static boolean isGobBoulder(String gobName) {
        return gobName.startsWith("gfx/terobjs/bumlings/") &&
               !gobName.startsWith("gfx/terobjs/bumlings/ras");
    }

    public static boolean isGobBush(String gobName) {
        return gobName.startsWith("gfx/terobjs/bushes");
    }

    public static boolean isGobTreeStump(String gobName) {
        return gobName.startsWith("gfx/terobjs/trees/") && gobName.endsWith("stump");
    }

    public static boolean isGobTree(String gobName) {
        return gobName.startsWith("gfx/terobjs/trees/") && !gobName.endsWith("log") && !gobName.endsWith("stump") && !gobName.endsWith("oldtrunk");
    }

    public static boolean isGobTreeLog(String gobName){
        return gobName.startsWith("gfx/terobjs/trees/") && gobName.endsWith("log");
    }

    public static boolean isBarrelTakeAll(Gob gob) {
        String gobName = gob.getres().name;
        if(!gobName.endsWith("barrel") || isBarrelEmpty(gob)){
            return false;
        }
        String list = "barley,carrot,cucumber,flax,grape,hemp,leek,lettuce,millet"
                +",pipeweed,poppy,pumpkin,wheat,turnip,wheat,barley,wheatflour,barleyflour,milletflour"
                +",ashes,gelatin,cavedust,caveslime,chitinpowder"
                +",colorred,coloryellow,colorblue,colorgreen,colorblack,colorwhite,colorgray"
                +",colororange,colorbeige,colorbrown,colorlime,colorturquoise,colorteal,colorpurple";
        return getOverlayNames(gob).stream().anyMatch(overlayName -> {
            return list.contains(overlayName.replace("gfx/terobjs/barrel-",""));
        });
    }

    public static void barrelTakeAllSeeds(Gob gob){
        try{
            // shift+rclick last barrel
            ZeeManagerGobClick.gobClick(gob, 3, UI.MOD_SHIFT);

            //wait getting to the barrel
            waitPlayerIdleFor(1);

            if (ZeeConfig.distanceToPlayer(gob) > ZeeManagerFarmer.MIN_ACCESSIBLE_DIST) {
                ZeeConfig.msg("barrel unreachable");
                return;
            }

            ZeeConfig.addPlayerText("taking contents...");

            while (!ZeeManagerGobClick.isBarrelEmpty(gob) && !isInventoryFull()) {
                ZeeManagerGobClick.gobClick(gob, 3, UI.MOD_SHIFT);
                Thread.sleep(PING_MS);
                if (ZeeConfig.isTaskCanceledByGroundClick())
                    break;
            }

            //if holding seed, store in barrel
            waitHoldingItem();
            ZeeManagerGobClick.itemActGob(gob, 0);

            if (isInventoryFull())
                ZeeConfig.msg("Inventory full");
            else if (!ZeeConfig.isTaskCanceledByGroundClick())
                ZeeConfig.msg("Took everything");

        }catch(Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }

    private void barrelTakeAllSeeds() {
        barrelTakeAllSeeds(gob);
    }

    public static boolean isInventoryFull() {
        return ZeeConfig.getMainInventory().getNumberOfFreeSlots() == 0;
    }

    public static void destroyGob(Gob gob) {
        ZeeConfig.gameUI.menu.wdgmsg("act","destroy","0");
        gobClick(gob,1);
    }
    private void destroyGob() {
        destroyGob(gob);
    }

    public static void liftGob(Gob gob) {
        if(isGobBush(gob.getres().name)) {
            ZeeManagerItemClick.equipBeltItem("shovel");
            waitItemEquipped("shovel");
        }
        ZeeConfig.gameUI.menu.wdgmsg("act", "carry","0");
        waitCursor(ZeeConfig.CURSOR_HAND);
        gobClick(gob,1);
        waitPlayerDistToGob(gob,0);
    }

    public static void inspectGob(Gob gob){
        ZeeConfig.gameUI.menu.wdgmsg("act","inspect","0");
        gobClick(gob, 1);
        ZeeConfig.clickRemoveCursor();
    }

    public static boolean isGobTrellisPlant(String gobName) {
        return gobNameEndsWith(gobName, "plants/wine,plants/hops,plants/pepper,plants/peas,plants/cucumber");
    }

    public static boolean isGobCrop(String gobName){
        return gobNameEndsWith(gobName,"plants/carrot,plants/beet,plants/yellowonion,plants/redonion,"
                +"plants/leek,plants/lettuce,plants/pipeweed,plants/hemp,plants/flax,"
                +"plants/turnip,plants/millet,plants/barley,plants/wheat,plants/poppy,"
                +"plants/pumpkin,plants/fallowplant"
        );
    }

    public static boolean isGobCookContainer(String gobName) {
        String containers ="cupboard,chest,crate,basket,box,coffer,cabinet";
        return gobNameEndsWith(gobName,containers);
    }

    private static boolean gobNameEndsWith(String gobName, String list) {
        String[] names = list.split(",");
        for (int i = 0; i < names.length; i++) {
            if (gobName.endsWith(names[i])){
                return true;
            }
        }
        return false;
    }

    private boolean gobNameStartsWith(String gobName, String list) {
        String[] names = list.split(",");
        for (int i = 0; i < names.length; i++) {
            if (gobName.startsWith(names[i])){
                return true;
            }
        }
        return false;
    }

    public static boolean clickGobPetal(Gob gob, String petalName) {
        if (gob==null){
            //println(">clickGobPetal gob null");
            return false;
        }
        //make sure cursor is arrow before clicking gob
        if (!ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_ARW)){
            ZeeConfig.clickRemoveCursor();
            if (!waitCursor(ZeeConfig.CURSOR_ARW))
                return false;
        }
        gobClick(gob,3);
        if(waitFlowerMenu()){
            choosePetal(getFlowerMenu(), petalName);
            return waitNoFlowerMenu();
        }else{
            //println("clickGobPetal > no flower menu?");
            return false;
        }
    }

    // if gob has flowermenu returns true
    public static boolean gobExistsBecauseFlowermenu(Gob gob) {

        boolean ret;

        //no gob, no menu
        if (ZeeConfig.isGobRemoved(gob)) {
            //println(">gobHasFlowermenu gob is inexistent");
            return false;
        }

        //select arrow cursor if necessary
        if (!ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_ARW)) {
            ZeeConfig.clickRemoveCursor();
            waitCursor(ZeeConfig.CURSOR_ARW);
        }

        //disable auto options before clicking gob
        boolean butchBackup = ZeeConfig.butcherMode;
        ZeeConfig.butcherMode = false;
        ZeeConfig.autoClickMenuOption = false;

        //click gob and wait menu
        gobClick(gob, 3);
        if (waitFlowerMenu()) {
            // menu opened means gob exist
            ZeeConfig.cancelFlowerMenu();
            waitNoFlowerMenu();
            //println("gobHasFlowermenu > true");
            ret = true;
        } else {
            //println("gobHasFlowermenu > cant click gob");
            ret = false;
        }

        //restore settings and return
        ZeeConfig.butcherMode = butchBackup;
        ZeeConfig.autoClickMenuOption = Utils.getprefb("autoClickMenuOption", true);
        return ret;
    }

    private boolean isGobButchable(String gobName){
        return gobNameEndsWith(
            gobName,
            "/stallion,/mare,/foal,/hog,/sow,/piglet,"
            +"/billy,/nanny,/kid,/sheep,/lamb,/cattle,/calf,"
            +"/wildhorse,/aurochs,/mouflon,/wildgoat,"
            +"/adder,/badger,/bear,/boar,/beaver,/deer,/reindeer,/reddeer,/fox,"
            +"/greyseal,/otter,/caveangler,/boreworm,/caverat,"
            +"/lynx,/mammoth,/moose,/troll,/walrus,/wolf,/wolverine"
        );
    }

    public static boolean isGobHorse(String gobName) {
        return gobNameEndsWith(gobName, "stallion,mare,horse");
    }

    private static boolean isGobFireSource(String gobName) {
        return gobNameEndsWith(gobName,"brazier,pow,snowlantern,/bonfire");
    }

    /**
     * Itemact with gob, to fill trough with item in hand for example
     * @param mod 1 = shift, 2 = ctrl, 4 = alt  (3 = ctrl+shift ?)
     */
    public static void itemActGob(Gob g, int mod) {
        ZeeConfig.gameUI.map.wdgmsg("itemact", Coord.z, g.rc.floor(OCache.posres), mod, 0, (int) g.id, g.rc.floor(OCache.posres), 0, -1);
    }

    public static void gobClick(Gob g, int btn, int mod, int x) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), g.rc.floor(OCache.posres), btn, mod, 0, (int)g.id, g.rc.floor(OCache.posres), 0, x);
    }

    public static void gobClick(Gob g, int btn) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), g.rc.floor(OCache.posres), btn, 0, 0, (int)g.id, g.rc.floor(OCache.posres), 0, -1);
    }

    public static void gobClick(Gob g, int btn, int mod) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), g.rc.floor(OCache.posres), btn, mod, 0, (int)g.id, g.rc.floor(OCache.posres), 0, -1);
    }

    public static double distanceCoordGob(Coord2d c, Gob gob) {
        return c.dist(gob.rc);
    }

    // return Gob or null
    public static Gob findGobById(long id) {
        return ZeeConfig.gameUI.ui.sess.glob.oc.getgob(id);
    }

    // "gfx/terobjs/barrel-flax"
    public static List<String> getOverlayNames(Gob gob) {
        List<String> ret = new ArrayList<>();
        for (Gob.Overlay ol : gob.ols) {
            if(ol.res != null)
                ret.add(ol.res.get().name);
        }
        return ret;
    }
}

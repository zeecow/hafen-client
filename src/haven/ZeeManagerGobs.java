package haven;


import haven.render.RenderTree;
import haven.resutil.WaterTile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static haven.OCache.posres;

@SuppressWarnings("deprecation")
public class ZeeManagerGobs extends ZeeThread{

    static final int OVERLAY_ID_AGGRO = 1341;

    static Coord coordPc;
    static Coord2d coordMc;
    static Gob gob;
    static String gobName;
    static boolean isGroundClick;

    static float camAngleStart, camAngleEnd, camAngleDiff;
    static long lastClickMouseDownMs, lastClickMouseUpMs, lastClickDiffMs;
    static int lastClickMouseButton;
    static boolean barrelLabelOn = false;
    static boolean remountClosestHorse;
    static Object[] lastMenuGridActArgs = null;

    public static void startMidClick(Coord pc, Coord2d mc, Gob gobClicked, String gName) {

        lastClickDiffMs = lastClickMouseUpMs - lastClickMouseDownMs;
        coordPc = pc;
        coordMc = mc;
        gob = gobClicked;
        isGroundClick = (gob==null);
        gobName = isGroundClick ? "" : gob.getres().name;

        //println(lastClickDiffMs+"ms > "+gobName + (gob==null ? "" : " dist="+ZeeConfig.distanceToPlayer(gob)));
        //if (gob!=null) println(gobName + " poses = "+ZeeConfig.getGobPoses(gob));

        // long mid-clicks
        if (isLongMidClick()) {
            if (ZeeConfig.isCombatActive())
                return;
            new ZeeThread(){
                public void run() {
                    runLongMidClick();
                }
            }.start();
        }
        // short mid-clicks
        else {
            shortMidClick();
        }
    }

    private static void shortMidClick() {

        /*
            ground clicks
         */
        if(gob==null) {

            // place all pile items
            if(ZeeManagerStockpile.lastPlob != null) {
                ZeeManagerGobs.gobPlace(ZeeManagerStockpile.lastPlob,UI.MOD_SHIFT);
            }
            // dig ballclay if cursor dig
            else if(ZeeConfig.isCursorName(ZeeConfig.CURSOR_DIG) && ZeeConfig.isTileNamed(coordMc, ZeeConfig.TILE_WATER_FRESH_SHALLOW,ZeeConfig.TILE_WATER_OCEAN_SHALLOW)){
                ZeeConfig.clickTile(ZeeConfig.coordToTile(coordMc),1,UI.MOD_SHIFT);
            }
            // queue plowing
            else if(ZeeConfig.isPlayerDrivingPlow()){
                plowQueueAddCoord(coordMc,coordPc);
            }
            // toggle crops by clicking farm tile, woodchip underground
            else if (ZeeConfig.getTileResName(coordMc).contentEquals("gfx/tiles/field") || (ZeeConfig.playerLocation==ZeeConfig.LOCATION_UNDERGROUND && ZeeConfig.getTileResName(coordMc).contains("/paving/woodchip"))){
                Utils.setprefb("hideGobCrops", (hideGobCrops = !hideGobCrops));
                toggleModelsInList(getGobsByTags(Gob.Tag.CROP));
            }
        }
        /*
            gob clicks
         */
        // hilite gob by name (:zeecow gobfind)
        else if(ZeeConfig.gameUI.ui.modshift && ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_ARW)){
            ZeeConsole.runCmdZeecow(new String[]{":zeecow","gobfind",gob.getres().basename()});
        }
        // queue remove tree and stump
        else if (ZeeManagerTrees.isRemovingTreesAndStumps && isGobTree(gobName)) {
            ZeeManagerTrees.scheduleRemoveTree(gob);
        }
        // queue treelog destruction
        else if (ZeeManagerTrees.isDestroyingTreelogs && isGobTreeLog(gobName)) {
            ZeeManagerTrees.scheduleDestroyTreelog(gob);
        }
        // queue butch animal
        else if(isAutoButchingBigAnimal && isGobDeadOrKO(gob)){
            queueButchAnimal(gob);
        }
        //queue chop tree/bush
        else if( ZeeManagerTrees.queueChopTreeBushIsPossible(gobName) ){
            queueChopTreeBush(gob);
        }
        //queue chip stone
        else if(isGobBoulder(gobName) && (queueChipStoneIsActive() || ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_CHIPPINGSTONE,ZeeConfig.POSE_PLAYER_PICK))){
            queueChipStone(gob);
        }
        // queue menugrid act (after specific queues)
        else if (ZeeManagerGobs.lastMenuGridActArgs!=null && ZeeConfig.isPlayerActivePose()){
            queueMenuGridAct(gob);
        }
        // feed clover to wild animal
        else if (checkCloverFeeding(gob)) {
            feedClover(gob);
        }
        // pick quicksilver from smelter
        else if (gobName.endsWith("/smelter") && ZeeConfig.isPlayerHoldingItem()){
            ZeeManagerItems.getQuicksilverFromSmelter(gob);
        }
        //barterstand
        else if (gobName.endsWith("barterstand")){
            barterstandSearchWindow();
        }
        // place lifted treelog next to clicked one
        else if ( isGobTreeLog(gobName) && ZeeConfig.isPlayerLiftingGobNamecontains("gfx/terobjs/trees/")!=null && !ZeeConfig.isPlayerLiftingGob(gob))
        {
            ZeeManagerTrees.placeTreelogNextTo(gob);
        }
        // start Gob Placer (if lifting same name gob, or boulder)
        else if(!ZeeConfig.isPlayerLiftingGob(gob) && (ZeeConfig.isPlayerLiftingGobNamecontains(gobName)!=null  || ZeeConfig.isPlayerLiftingGobNamecontains("gfx/terobjs/bumlings/")!=null))
        {
            Gob liftedGob = ZeeConfig.isPlayerLiftingGobNamecontains(gobName);
            Gob groundGob = gob;
            String groundGobName = gobName;
            // check if lifting any boulder
            if (liftedGob==null){
                liftedGob = ZeeConfig.isPlayerLiftingGobNamecontains("gfx/terobjs/bumlings/");
                if (liftedGob!=null && groundGobName.startsWith("gfx/terobjs/bumlings/")) {
                    String liftedGobName = liftedGob.getres().name;
                    String liftedBoulderSize = ZeeConfig.getRegexGroup(liftedGobName, "(\\d)$", 1);
                    String groundBoulderSize = ZeeConfig.getRegexGroup(groundGobName, "(\\d)$", 1);
                    if (!liftedBoulderSize.isBlank() && liftedBoulderSize.contentEquals(groundBoulderSize)) {
                        windowGobPlacer(gob, liftedGob);
                    } else {
                        println("gob placer > boulder size dont match > " + liftedBoulderSize + " != " + groundBoulderSize);
                    }
                } else {
                    println("gob placer canceled, was expecting 2 boulders");
                }
            }
            // non-boulder gobs
            else{
                //println("non boulder gob");
                windowGobPlacer(gob, liftedGob);
            }
        }
        // build obj and get more blocks/boards
        else if (gobName.contentEquals("gfx/terobjs/consobj")) {
            if (ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_CHOPBLOCK)){
                buildObjAndChopMoreBlocks(gob,ZeeManagerStockpile.lastTreelogChopped);
            }
            else if (ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_SAWING)){
                buildObjAndMakeMoreBoards(gob,ZeeManagerStockpile.lastTreelogSawed);
            }
        }
        // pile boards once and make more
        else if (gobName.endsWith("/stockpile-board") && ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_SAWING)) {
            ZeeManagerStockpile.pileInvBoardsAndMakeMore(gob);
        }
        // pile blocks once and chop more
        else if (gobName.endsWith("/stockpile-wblock") && ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_CHOPBLOCK)) {
            ZeeManagerStockpile.pileInvBlocksAndMakeMore(gob);
        }
        // pile sand once and dig more
        else if (gobName.endsWith("/stockpile-sand") && ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_DIG,ZeeConfig.POSE_PLAYER_DIGSHOVEL)) {
            ZeeManagerStockpile.pileInvSandAndDigMore(gob);
        }
        // pile inv stones and try chipping more stones
        else if (gobName.endsWith("/stockpile-stone") && ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_PICK, ZeeConfig.POSE_PLAYER_CHIPPINGSTONE)) {
            ZeeManagerStockpile.pileInvStonesAndChipMore(gob);
        }
        // pile inv coal and try collecting more
        else if (gobName.endsWith("/stockpile-coal") && ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_BUSHPICK)) {
            ZeeManagerStockpile.pileInvCoalAndCollectMore(gob);
        }
        // pile inv clay
        else if(gobName.endsWith("/stockpile-clay") && ZeeConfig.isCursorName(ZeeConfig.CURSOR_DIG)){
            ZeeManagerStockpile.pileInvClays(gob);
        }
        // if crating ropes, midclick fibre pile to get more strings and craft again
        else if((gobName.endsWith("/stockpile-flaxfibre") || gobName.endsWith("/stockpile-hempfibre")) && ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_ROPE_WALKING)){
            new ZeeThread() {
                public void run() {
                    ZeeManagerCraft.ropeFetchStringsAndCraft(gob);
                }
            }.start();
        }
        // click gob holding item (pile, etc)
        else if (ZeeConfig.isPlayerHoldingItem()) {
            clickedGobHoldingItem(gob,gobName);
        }
        // harvest one trellis withouth scythe
        else if (isGobTrellisPlant(gobName)) {
            new ZeeThread() {
                public void run() {
                    harvestOneTrellisWithoutScythe(gob);
                }
            }.start();
        }
        // activate harvest area if non-trellis crop
        else if (isGobCrop(gobName) && ZeeManagerFarmer.isCropStageHarvestable(gob)){
            if (!ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_HARVEST))
                gobClick(gob, 3, UI.MOD_SHIFT);
        }
        else if (isGobGroundItem(gobName)) {
            // place stockpile at gob coord
            if(ZeeConfig.isPlobActive()) {
                MapView.Plob plob = ZeeManagerStockpile.lastPlob;
                gobPlace(plob, ZeeConfig.tileToCoord(ZeeConfig.coordToTile(gob.rc)), UI.MOD_SHIFT);
            }
            // equip ground bucket if no belt(noob char?)
            else if (gobName.contains("/bucket") && ZeeManagerItems.getInvBelt()==null && !ZeeConfig.isPlayerHoldingItem()){
                new ZeeThread(){
                    public void run() {
                        try {
                            gobClick(gob,3);
                            waitHoldingItem();
                            if(ZeeManagerItems.isAnyHandEmpty()) {
                                ZeeManagerItems.equipEmptyHand();
                            }else {
                                ZeeManagerItems.equipLeftOccupiedHand();
                                sleep(PING_MS);
                                ZeeManagerItems.dropHoldingItemToInv(ZeeConfig.getMainInventory());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
            // pick up all ground items
            else {
                gobClick(gob, 3, UI.MOD_SHIFT);//shift + rclick
            }
        }
        // light up torch
        else if (isGobFireSource(gob)) {
            new ZeeThread() {
                public void run() {
                    if (pickupTorch())
                        itemActGob(gob,0);
                }
            }.start();
        }
        // mount horse
        else if (isGobHorse(gobName)) {
            new ZeeThread() {
                public void run() {
                    mountHorse(gob);
                }
            }.start();
        }
        // label all barrels
        else if (gobName.endsWith("/barrel")) {
            if (barrelLabelOn)
                ZeeManagerFarmer.testBarrelsTilesClear();
            else
                ZeeManagerFarmer.testBarrelsTiles(true);
            barrelLabelOn = !barrelLabelOn;
        }
        // pick  dreams from catchers closeby
        else if (gobName.endsWith("/dreca")) {
            pickAllDreamsCloseBy(gob);
        }
        //toggle mine support radius
        else if (isGobMineSupport(gobName) || gobName.endsWith("/ladder")) {
            ZeeConfig.toggleMineSupport();
        }
        //toggle beeskep radius
        else if (gobName.endsWith("/beehive")) {
            ZeeConfig.toggleRadiusBeeskep();
        }
        //toggle foodtrough radius
        else if (gobName.endsWith("/trough")) {
            ZeeConfig.toggleRadiusFoodtrough();
        }
        // toggle aggressive gob radius
        else if(ZeeConfig.isAggressive(gobName)){
            toggleOverlayAggro(gob);
        }
        // toggle cheeserack
        else if(gobName.endsWith("cheeserack")){
            ZeeConfig.toggleAllCheeserack();
        }
        // toggle gardenpot
        else if(gobName.endsWith("gardenpot")){
            ZeeConfig.toggleAllGardenpots();
        }
        // open cauldron
        else if(gobName.contains("/cauldron") && !ZeeConfig.isPlayerLiftingGob(gob)){
            cauldronOpen();
        }
        // open ship cargo
        else if(gobName.endsWith("/knarr") || gobName.endsWith("/snekkja")) {
            new ZeeThread() {
                public void run() {
                    clickGobPetal(gob,"Cargo");
                }
            }.start();
        }
        // toggle cart if not lifting (avoid picking cart slot when zoomedout)
        else if (gobName.endsWith("/cart")){
            if (!ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_LIFTING)) {
                gobClick(gob, 3);
            }
        }
        // cupboard labeler
        else if(gobName.endsWith("/cupboard")){
            ZeeCupboardLabeler.toggle();
        }
        // schedule auto remount if midclick gob passage
        else if ( isGobAmbientPassage(gob) && !ZeeConfig.isPlayerLiftingGob(gob)){
            // unmount horse
            if (ZeeConfig.isPlayerMountingHorse() && !ZeeConfig.nameInListEndsWith(gobName,"/ladder,/minehole") && ZeeConfig.getMainInventory().countItemsByNameContains("/rope") > 0)
            {
                ZeeManagerGobs.remountClosestHorse = true;
                ZeeConfig.addPlayerText("remount");
                dismountHorseAndClickGob(coordMc);
            }
        }
        // midclick cellar stairs on a horse (simulate click for convenience)
        else if(gobName.endsWith("/cellarstairs") && ZeeConfig.isPlayerMountingHorse()){
            gobClick(gob,3);
        }
        // add all fuel from inventory
        else if (ZeeConfig.isPlayerHoldingItem() && ZeeConfig.nameInListEndsWith(gobName,"brazier,bonfire,smelter,fireplace,oven,crucible")){
            String holdingItem = ZeeManagerItems.getHoldingItem().item.getres().name;
            if(ZeeConfig.nameInListContains(holdingItem,"/coal,/blackcoal,/branch,/wblock-")){
                itemActGob(gob,UI.MOD_CTRL_SHIFT);
            }
        }
        //quick toggle visibility for tree, wall, crop
        else if (isGobTree(gobName)) {
            Utils.setprefb("hideGobTrees", ( ZeeManagerTrees.hideGobTrees = !ZeeManagerTrees.hideGobTrees));
            toggleModelsInList(getGobsByTags(Gob.Tag.TREE));
        }
        else if (isGobWall(gobName)) {
            Utils.setprefb("hideGobWalls", (hideGobWalls = !hideGobWalls));
            toggleModelsInList(getGobsByTags(Gob.Tag.WALL));
        }
        else if (isGobCrop(gobName) || gobName.endsWith("/trellis")) {
            //also toggled by midclick farm soil
            Utils.setprefb("hideGobCrops", (hideGobCrops = !hideGobCrops));
            toggleModelsInList(getGobsByTags(Gob.Tag.CROP));
        }
        // inspect gob
        else {
            inspectGob(gob);
        }
    }

    private static boolean isScheduleRemount() {
        if ( isGobAmbientPassage(gob) &&
                !ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_LIFTING) &&
                ZeeConfig.isPlayerMountingHorse() &&
                !ZeeConfig.nameInListEndsWith(gobName,"/ladder,/minehole") &&
                ZeeConfig.getMainInventory().countItemsByNameContains("/rope") > 0)
        {
            return true;
        }
        return false;
    }

    static HashMap<String,Double> mapGobPlacerNameDist = mapGobPlacerInit();
    static Label gobPlacerLblDist;
    static Button gobPlacerBtnClear;
    static String gobPlacerWinTitle = "Gob placer";
    private static long goPlacerWindowTimeout = -1;
    private static void windowGobPlacer(Gob groundGob, Gob liftedGob) {
        Widget wdg;

        Window win = ZeeConfig.getWindow(gobPlacerWinTitle);
        if (win != null){
            win.reqdestroy();
            win = null;
        }

        String liftedGobName = liftedGob.getres().name;

        // rename boulders to boulder0 or boulder1, the only liftable sizes
        if(isGobBoulder(liftedGobName)){
            String boulderSize = ZeeConfig.getRegexGroup(liftedGobName,"(\\d)$",1);
            if (!List.of("0","1").contains(boulderSize)) {
                println("lifted boulder size unknown");
                return;
            }
            liftedGobName = "boulder"+boulderSize;
        }

        //create window
        win = ZeeConfig.gameUI.add(
                new Window(Coord.of(200,70),gobPlacerWinTitle){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("close")){
                            this.reqdestroy();
                        }
                    }
                },
                ZeeConfig.gameUI.sz.div(2)
        );

        // label gob name
        wdg = win.add(new Label(liftedGobName));

        // label saved gob dist
        wdg = gobPlacerLblDist = win.add(new Label(""),wdg.c.add(0,wdg.sz.y+2));

        // button clear saved dist
        String finalLiftedGobName = liftedGobName;
        wdg = gobPlacerBtnClear = win.add(new Button(120,"clear saved dist"){
            public void wdgmsg(String msg, Object... args) {
                //super.wdgmsg(msg, args);
                if (msg.contentEquals("activate")){
                    mapGobPlacerNameDist.remove(finalLiftedGobName);
                    Utils.setpref("mapGobPlacerNameDist", ZeeConfig.serialize(mapGobPlacerNameDist));
                    //println("removing "+liftedGobName+" , mapsize "+mapGobPlacerNameDist.size());
                    //close window
                    goPlacerWindowTimeout = -1;
                }
            }
        },wdg.c.add(0,wdg.sz.y+2));


        // find saved gob dist
        Double dist = mapGobPlacerNameDist.get(liftedGobName);

        // apply saved gob dist
        if (dist!=null){
            // window lbl dist
            gobPlacerLblDist.settext("saved dist: "+dist);
            placeLiftedGobNextTo(liftedGob,groundGob,dist);
            gobPlacerWindowTimeout();
        }
        // wait new gob dist and save it
        else{
            gobPlacerBtnClear.disable(true);
            gobPlacerLblDist.settext("waiting new gob placement");
            // wait user place liftedGob and save distance between ground and lifted gobs
            String finalLiftedGobName1 = liftedGobName;
            new ZeeThread(){
                public void run() {
                    try {

                        //wait player place gob
                        gobClick(liftedGob,3);//create plob
                        ZeeConfig.addPlayerText("waiting placing");
                        while(ZeeConfig.isPlayerLiftingPose()) {
                            sleep(500);
                        }

                        //save gob dist
                        double dist = groundGob.rc.dist(liftedGob.rc);
                        mapGobPlacerNameDist.put(finalLiftedGobName1,dist);
                        Utils.setpref("mapGobPlacerNameDist", ZeeConfig.serialize(mapGobPlacerNameDist));
                        //println("gobPlacer > "+dist+" , "+liftedGobName+" , mapsize "+mapGobPlacerNameDist.size());

                        // window lbl dist
                        gobPlacerLblDist.settext("saved dist: "+dist);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ZeeConfig.removePlayerText();
                    gobPlacerBtnClear.disable(false);
                    gobPlacerWindowTimeout();
                }
            }.start();
        }
    }

    private static void gobPlacerWindowTimeout(){

        // reset timeout if already running
        if (goPlacerWindowTimeout > -1){
            goPlacerWindowTimeout = 7000;
            //println("gobplacer reset window timeout");
            return;
        }

        //start timeout
        new ZeeThread(){
            public void run() {
                try {
                    String lbltxt = gobPlacerLblDist.texts;
                    goPlacerWindowTimeout = 7000;
                    while (goPlacerWindowTimeout > 0){
                        gobPlacerLblDist.settext(lbltxt + " ("+((int) goPlacerWindowTimeout /1000)+"s)");
                        sleep(1000);
                        goPlacerWindowTimeout -= 1000;
                    }
                    Window win = ZeeConfig.getWindow(gobPlacerWinTitle);
                    if ( win != null){
                        win.reqdestroy();
                        win = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                goPlacerWindowTimeout = -1;
            }
        }.start();
    }


    @SuppressWarnings("unchecked")
    public static HashMap<String, Double> mapGobPlacerInit() {
        String s = Utils.getpref("mapGobPlacerNameDist","");
        if (s.isEmpty())
            return new HashMap<String,Double> ();
        else
            return (HashMap<String, Double>) ZeeConfig.deserialize(s);
    }



    private static void buildObjAndChopMoreBlocks(Gob consObj, Gob treeLog) {
        new ZeeThread() {
            public void run() {

                if (consObj==null){
                    println("consObj null");
                    return;
                }

                //click build obj
                gobClick(consObj,3);

                //wait build window
                Window buildWindow = waitWindowBuildOpened();
                if(buildWindow==null){
                    println("coundn't wait build window");
                    return;
                }

                //click build butotn
                Button buildBtn = ZeeConfig.getButtonNamed(buildWindow,"Build");
                if (buildBtn==null) {
                    println("couldn't find build button");
                    return;
                }
                buildBtn.click();

                if (treeLog==null){
                    println("tree log null");
                    return;
                }

                //wait build and chop more
                ZeeManagerGobs.waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_BUILD,ZeeConfig.POSE_PLAYER_DRINK);
                clickGobPetal(treeLog,"Chop into blocks");
            }
        }.start();
    }

    private static void buildObjAndMakeMoreBoards(Gob consObj, Gob treeLog) {
        new ZeeThread() {
            public void run() {

                if (consObj==null){
                    println("consObj null");
                    return;
                }

                //click build obj
                gobClick(consObj,3);

                //wait build window
                Window buildWindow = waitWindowBuildOpened();
                if(buildWindow==null){
                    println("coundn't wait build window");
                    return;
                }

                //click build butotn
                Button buildBtn = ZeeConfig.getButtonNamed(buildWindow,"Build");
                if (buildBtn==null) {
                    println("couldn't find build button");
                    return;
                }
                buildBtn.click();

                if (treeLog==null){
                    println("tree log null");
                    return;
                }

                //wait build and saw more
                ZeeManagerGobs.waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_BUILD,ZeeConfig.POSE_PLAYER_DRINK);
                clickGobPetal(treeLog,"Make boards");
            }
        }.start();
    }


    private static List<Coord2d> plowQueueCoords = null;
    private static ZeeThread plowQueueThread = null;
    private static void plowQueueAddCoord(Coord2d coordMc, Coord coordPc) {

        // find plow
        Gob plow = ZeeConfig.getClosestGobByNameEnds("/plow");
        if (plow==null){
            println("no plow found");
            return;
        }

        // queue new plow coord
        if (plowQueueCoords ==null){
            plowQueueCoords = new ArrayList<>();
        }
        if (plowQueueCoords.contains(coordMc)){
            println("plow coord already queued");
            return;
        }
        plowQueueCoords.add(coordMc);
        ZeeManagerItems.playFeedbackSound();
        ZeeConfig.addPlayerText("plow q "+ plowQueueCoords.size());

        //starts thread
        if (plowQueueThread == null) {
            plowQueueThread = new ZeeThread(){
                public void run() {
                    try {
                        prepareCancelClick();
                        while(!isCancelClick()){
                            sleep(1000);
                            if (isCancelClick()){
                                println("plow queue cancel clicked");
                                break;
                            }
                            //wait plow stops moving
                            if (getGAttrNames(plow).contains("LinMove")){
                                //println("plow lin move");
                                continue;
                            }
                            //wait player stop walking and drinking
                            if (ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_WALK,ZeeConfig.POSE_PLAYER_DRINK)){
                                //println("player walk/drink");
                                continue;
                            }
                            // click next coord
                            if (!plowQueueCoords.isEmpty()){
                                Coord2d nextCoord = plowQueueCoords.remove(0);
                                ZeeConfig.clickCoord(nextCoord.floor(posres),1);
                                prepareCancelClick();
                                //drink
                                if (ZeeConfig.getMeterStamina() < 100) {
                                    ZeeManagerItems.drinkFromBeltHandsInv();
                                }
                                ZeeConfig.addPlayerText("plow q "+ plowQueueCoords.size());
                            }
                            else {
                                println("plow queue ended");
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        println("thread queue plow interrupted");
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    ZeeConfig.removePlayerText();
                    plowQueueReset();
                }
            };
        }
        if (!plowQueueThread.isAlive()) {
            plowQueueThread.start();
        }
    }

    static void plowQueueReset(){
        plowQueueCoords = null;
        plowQueueThread = null;
    }


    static void barterSearchUpdateGobs() {
        List<Gob> stands = ZeeConfig.findGobsByNameEndsWith("/barterstand");
        if (stands.size() == 0)
            return;
        for (Gob stand : stands) {
            synchronized (stand) {
                //remove barterstand text
                Gob.Overlay ol = stand.findol(ZeeGobText.class);
                if (ol != null) {
                    ol.remove(false);
                }
                //add barter text
                addTextBarterStand(stand);
            }
        }
    }
    static void addTextBarterStand(Gob ob) {

        if (!barterSearchOpen)
            return;

        List<String> foundItems = new ArrayList<>();
        List<String> barterItems = getBarterstandItems(ob);

        // checkboxes "ore", "stone"
        if (barterFindCheckOre || barterFindCheckStone) {
            for (String barterItem : barterItems) {
                // found generic "stone"
                if (barterFindCheckStone && !foundItems.contains("stone") && ZeeConfig.mineablesStone.contains(barterItem)) {
                    foundItems.add("stone");
                }
                // found generic "ore"
                if (barterFindCheckOre && !foundItems.contains("ore") && (ZeeConfig.mineablesOre.contains(barterItem) || ZeeConfig.mineablesOrePrecious.contains(barterItem))) {
                    foundItems.add("ore");
                }
            }
        }

        // keywords from text area
        if (barterFindText != null && !barterFindText.strip().isBlank()) {
            String[] arrKeywords = barterFindText.strip().split(" ");
            for (String keyword : arrKeywords) {
                for (String barterItem : barterItems) {
                    // found specific keyword
                    if (barterItem.contains(keyword) && !foundItems.contains(barterItem))
                    {
                        foundItems.add(barterItem);
                    }
                }
            }
        }

        //add found names to barterstand
        if (foundItems.size() > 0){
            ZeeConfig.addGobText(ob, foundItems.toString());
        }
    }
    static String barterFindText;
    static boolean barterSearchOpen = false;
    static boolean barterFindCheckStone = false;
    static boolean barterFindCheckOre = false;
    static void barterstandSearchWindow() {

        Widget wdg;
        String title = "Find Stand Item";

        Window win = ZeeConfig.getWindow(title);
        if (win != null){
            win.reqdestroy();
            win = null;
        }

        //create window
        win = ZeeConfig.gameUI.add(
            new Window(Coord.of(120,70),title){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("close")){
                        barterSearchOpen = false;
                        barterFindText = null;
                        for (Gob stand : ZeeConfig.findGobsByNameEndsWith("/barterstand")) {
                            ZeeConfig.removeGobText(stand);
                        }
                        this.reqdestroy();
                    }
                }
            },
            300,300
        );
        barterSearchOpen = true;

        //label
        wdg = win.add(new Label("keywords (space sep.)"));

        //text entry
        wdg = win.add(new TextEntry(UI.scale(130),""){
            public void activate(String text) {
                // update barterstand labels
                barterSearchUpdateGobs();
            }
            public boolean keyup(KeyUpEvent ev) {
                barterFindText = this.text();
                return super.keyup(ev);
            }
        },0,wdg.c.y+wdg.sz.y);

        //checkbox stones
        wdg = win.add(new CheckBox("stone"){
            public void changed(boolean val) {
                barterFindCheckStone = val;
                barterSearchUpdateGobs();
            }
        },0,wdg.c.y+wdg.sz.y+5);

        //checkbox ore
        wdg = win.add(new CheckBox("ore"){
            public void changed(boolean val) {
                barterFindCheckOre = val;
                barterSearchUpdateGobs();
            }
        },wdg.c.x+wdg.sz.x+5,wdg.c.y);

        win.pack();
    }
    static List<String> getBarterstandItems(Gob barterStand) {
        List<String> ret = new ArrayList<>();
        for (Gob.Overlay ol : barterStand.ols) {
            if(ol.spr.getClass().getName().contentEquals("haven.res.gfx.fx.eq.Equed")) {
                try {
                    Field f = ol.spr.getClass().getDeclaredField("espr");
                    f.setAccessible(true);
                    Sprite espr = (Sprite) f.get(ol.spr);
                    ret.add(espr.res.basename());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }


    private static void placeLiftedGobNextTo(Gob liftedGob, Gob groundGob, double dist) {
        new Thread() {
            public void run() {
                try {

                    ZeeConfig.addPlayerText("placing");

                    // right click lifted gob to create plob
                    gobClick(liftedGob,3);
                    sleep(500);

                    // adjust plob angle, postition and place it
                    if (ZeeManagerStockpile.lastPlob==null){
                        ZeeConfig.msgError("placeLiftedGobNextTo > couldn't find last plob");
                        ZeeConfig.removePlayerText();
                        return;
                    }
                    Coord2d playerrc = ZeeConfig.getPlayerGob().rc;
                    Coord2d newrc = Coord2d.of(groundGob.rc.x, groundGob.rc.y);
                    if (Math.abs(groundGob.rc.x - playerrc.x) > Math.abs(groundGob.rc.y - playerrc.y)){
                        if (groundGob.rc.x > playerrc.x)
                            newrc.x -= dist;
                        else
                            newrc.x += dist;
                    }else{
                        if (groundGob.rc.y > playerrc.y)
                            newrc.y -= dist;
                        else
                            newrc.y += dist;
                    }

                    // position plob
                    ZeeManagerStockpile.lastPlob.move(newrc, groundGob.a);

                    // place gob and wait
                    gobPlace(ZeeManagerStockpile.lastPlob,0);
                    waitNotPlayerPose(ZeeConfig.POSE_PLAYER_LIFTING);

                }catch (Exception e){
                    e.printStackTrace();
                }
                ZeeConfig.removePlayerText();
            }
        }.start();
    }


    private static boolean checkCloverFeeding(Gob animal) {

        if (ZeeManagerItems.getHoldingItem()==null) {
            //println("checkCloverFeeding > holding item null");
            return false;
        }

        GItem holditem = ZeeManagerItems.getHoldingItem().item;

        if (ZeeManagerItems.isStackByKeyPagina(holditem)) {
            //println("checkCloverFeeding > holding stack");
            return false;
        }

        if (holditem!=null && holditem.getres().name.endsWith("/clover")){
            List<String> endList = List.of("/cattle", "/sheep","/horse","/boar","/wildgoat","/reindeer");
            for (String s : endList) {
                if (animal.getres().name.endsWith(s))
                    return true;
            }
        }else{
            //println("checkCloverFeeding > item null or name wrong");
        }

        //println("checkCloverFeeding > ret false");
        return false;
    }

    private static void feedClover(Gob animal){
        if (animal==null) {
            //println("feedClover > animal gob null");
            return;
        }
        new ZeeThread(){
            public void run() {
                try {
                    ZeeConfig.addPlayerText("clovering");
                    double dist = ZeeConfig.distanceToPlayer(animal);

                    prepareCancelClick();

                    //click animal location until distance close enough
                    while (dist > 50 && !isCancelClick()){
                        ZeeConfig.clickCoord(ZeeConfig.getGobCoord(animal),1);
                        prepareCancelClick();
                        sleep(777);
                        dist = ZeeConfig.distanceToPlayer(animal);
                    }

                    //println("dist "+dist);

                    // try feeding clover
                    if (!isCancelClick()) {
                        //println("final click");
                        itemActGob(animal, 0);
                    }
                    //else println("final click canceled");

                } catch (Exception e) {
                    e.printStackTrace();
                }
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    public static void checkPlobUnplaced() {
        if (ZeeConfig.autoToggleGridLines)
            ZeeConfig.gameUI.map.showgrid(false);

        ZeeManagerStockpile.lastPlob = null;
    }

    static void runLongMidClick() {
        try {
            /*
                ground clicked
             */
            if (isGroundClick){
                // place pile and start autopiling
                if(ZeeConfig.isPlobActive()){
                    placePileAndAuto();
                }
                //dismount horse unless using shovel
                else if (ZeeConfig.isPlayerMountingHorse() && !(ZeeManagerItems.isItemEquipped("shovel") && !ZeeConfig.isPlayerLiftingPose())) {
                    dismountHorse(coordMc);
                }
                //clicked water
                else if (isTileInspectQl(coordMc)) {
                    showGobFlowerMenu();
                }
                //disembark water vehicles
                else if (ZeeConfig.isPlayerOnCoracle()) {
                    disembarkEquipCoracle(coordMc);
                }
                else if(ZeeConfig.gobHasAnyPoseContains(ZeeConfig.getPlayerGob(),"/coracle","/dugout","borka/row","/snekkja","/knarr")) {
                    disembarkBoatAtShore(coordMc);
                }
                //disembark kicksled
                else if(ZeeConfig.isPlayerDrivingingKicksled()){
                    disembarkVehicle(coordMc);
                }
                //unload wheelbarrow at tile
                else if (ZeeConfig.isPlayerCarryingWheelbarrow()) {
                    ZeeManagerStockpile.unloadWheelbarrowStockpileAtGround(coordMc.floor(posres));
                    if (ZeeConfig.autoToggleGridLines)
                        ZeeConfig.gameUI.map.showgrid(true);
                }
                // clear snow area
                else if (ZeeConfig.getTileResName(coordMc).contains("tiles/snow")){
                    //haven.MapView@11460448 ; click ; [(629, 490), (1014904, 1060429), 3, 1]
                    ZeeConfig.clickCoord(coordMc.floor(posres),3,UI.MOD_SHIFT);
                }
                else{
                    showGobFlowerMenu();
                }
            }
            /*
                gob clicked
            */
            // stump removal
            else if(isGobTreeStump(gobName)){
                ZeeManagerTrees.removeStumpMaybe(gob);
            }
            // pile boards from treelog
            else if (gobName.endsWith("/stockpile-board") && ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_SAWING)) {
                ZeeManagerStockpile.pileBoardsFromTreelog(gob);
            }
            // pile blocks from treelog
            else if (gobName.endsWith("/stockpile-wblock") && ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_CHOPBLOCK)) {
                ZeeManagerStockpile.pileBlocksFromTreelog(gob);
            }
            // pile sand from tile
            else if (gobName.endsWith("/stockpile-sand") && ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_DIG,ZeeConfig.POSE_PLAYER_DIGSHOVEL)) {
                ZeeManagerStockpile.pileSandFromSandTile(gob);
            }
            // put out cauldron
            else if(gobName.contains("/cauldron") && !ZeeConfig.isPlayerLiftingGob(gob)){
                cauldronPutOut();
            }
            // show ZeeFlowerMenu
            else if (!isGroundClick && !ZeeConfig.isPlayerHoldingItem() && showGobFlowerMenu()) {

            }
            // activate cursor harvest
            else if (isGobCrop(gobName)) {
                if (!ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_HARVEST))
                    gobClick(gob, 3, UI.MOD_SHIFT);
            }
            // stockpile + wheelbarrow
            else if (isGobStockpile(gobName)) {
                // driving wheelbarrow = start piles mover
                if (ZeeConfig.isPlayerDrivingWheelbarrow()) {
                    ZeeManagerStockpile.startPilesMover();
                }
                // carrying wheelbarrow = use wb on pile
                else if(ZeeConfig.isPlayerCarryingWheelbarrow()) {
                    ZeeManagerStockpile.useWheelbarrowAtStockpile(gob);
                    if (ZeeConfig.autoToggleGridLines)
                        ZeeConfig.gameUI.map.showgrid(true);
                }
                //pickup all pile items
                else {
                    gobClick(gob,3, UI.MOD_SHIFT);
                    ZeeManagerItems.playFeedbackSound();
                }
            }
            // pickup all items: dframe
            else if (gobName.endsWith("/dframe")) {
                gobClick(gob,3, UI.MOD_SHIFT);
            }
            // refill cauldron and craft all
            else if(ZeeConfig.isPlayerFollowingCauldron && gobName.endsWith("/barrel")){
                refillCauldronAndCraftAll(gob);
            }
            // item act barrel
            else if (ZeeConfig.isPlayerHoldingItem() && gobName.endsWith("/barrel")) {
                if (ZeeManagerFarmer.isBarrelEmpty(gob))
                    itemActGob(gob,UI.MOD_SHIFT);//shift+rclick
                else
                    itemActGob(gob,3);//ctrl+shift+rclick
            }
            // player lifting wheelbarrow
            else if (ZeeConfig.isPlayerCarryingWheelbarrow()) {
                // mount horse and liftup wb
                if (isGobHorse(gobName)) {
                    mountHorseCarryingWheelbarrow(gob);
                }
                // unload wb at gob
                else {
                    unloadWheelbarrowAtGob(gob);
                    if (ZeeConfig.autoToggleGridLines)
                        ZeeConfig.gameUI.map.showgrid(true);
                }
            }
            // player driving wheelbarrow
            else if (!gobName.endsWith("/wheelbarrow") && ZeeConfig.isPlayerDrivingWheelbarrow()) {
                // mount horse and liftup wb
                if (isGobHorse(gobName))
                    mountHorseDrivingWheelbarrow(gob);
                // lift up wb and open gate
                else if (isGobGate(gobName))
                    openGateWheelbarrow(gob);
                // lift up wb and store in cart
                else if (gobName.endsWith("/cart")) {
                    Gob wb = ZeeConfig.getClosestGobByNameContains("/wheelbarrow");
                    liftGobAndClickTarget(wb,gob);
                }
            }
            // drive ship
            else if(gobName.endsWith("/knarr") || gobName.endsWith("/snekkja")) {
                clickGobPetal(gob,"Man the helm");
            }
            // try picking from cart's first slot (arg 2), if empty will toggle cart
            else if (gobName.endsWith("/cart")){
                if (!ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_LIFTING)) {
                    gobClick(gob, 3, 0, 2);
                }
            }
            // lift up gob
            else if (isGobLiftable(gobName) || isGobBush(gobName)) {
                liftGob(gob);
            }
            // gob item piler
            else if (ZeeManagerStockpile.isGobPileable(gob)){
                ZeeManagerStockpile.areaPilerWindow(gob);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    static void refillCauldronAndCraftAll(Gob barrel) {
        new ZeeThread(){
            public void run() {
                try {
                    ZeeConfig.addPlayerText("refil");
                    Gob cauldron = ZeeConfig.getClosestGobByNameContains("/cauldron");
                    Coord barrelCoord = ZeeConfig.getGobCoord(barrel);
                    //backup aux containers before checkAttrDelFollowing()
                    List<Gob> backupConts = null;
                    List<Gob> tmp = ZeeConfig.listCauldronContainers;
                    if (tmp!=null && !tmp.isEmpty())
                        backupConts = new ArrayList<>(tmp);
                    //lift barrel
                    if(cauldron!=null && liftGob(barrel)){
                        //barrel click cauldron
                        gobClick(cauldron,3);
                        if (waitPlayerIdleLinMove()){
                            //return barrel
                            ZeeConfig.clickCoord(barrelCoord,3);
                            if (ZeeConfig.isPlayerMountingHorse())
                                return;
                            if (waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_LIFTING)){
                                //reopen cauldron
                                if (clickGobPetal(cauldron,"Open")){
                                    if (waitWindowOpened("Cauldron")) {
                                        //reopen aux containers
                                        if (backupConts!=null){
                                            for (Gob cont : backupConts) {
                                                gobClick(cont,3);
                                                sleep(PING_MS);
                                            }
                                            //restore containers removed by checkAttrDelFollowing()
                                            if (backupConts!=null && !backupConts.isEmpty()) {
                                                ZeeConfig.listCauldronContainers = new ArrayList<>(backupConts);
                                                ZeeConfig.addPlayerText("conts "+ZeeConfig.listCauldronContainers.size());
                                            }
                                        }
                                        //click "craft all" button
                                        if (ZeeConfig.makeWindow!=null) {
                                            ZeeConfig.getButtonNamed((Window) ZeeConfig.makeWindow.parent, "Craft All").click();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (ZeeConfig.listCauldronContainers==null || ZeeConfig.listCauldronContainers.isEmpty() ) {
                    ZeeConfig.removePlayerText();
                }
            }
        }.start();
    }

    private static void placePileAndAuto() {

        MapView.Plob plob = ZeeManagerStockpile.lastPlob;
        String pileName = plob.getres().name;

        if(pileName.endsWith("stockpile-wblock") && ZeeManagerStockpile.lastTreelogChopped!=null) {
            Coord2d pileCoord = new Coord2d(plob.rc.x, plob.rc.y);
            ZeeManagerGobs.gobPlace(plob,0);
            if(waitPlayerIdlePose()) {
                Gob newPile = ZeeConfig.findGobByNameAndCoord("stockpile-wblock", pileCoord);
                if (newPile==null){
                    println("place and autopile > new pile undecided");
                }else {
                    ZeeManagerStockpile.pileBlocksFromTreelog(newPile);
                }
            }else{
                println("place and autopile > failed waiting idle pose?");
            }
        }
        else if(pileName.endsWith("stockpile-board") && ZeeManagerStockpile.lastTreelogSawed!=null) {
            Coord2d pileCoord = new Coord2d(plob.rc.x, plob.rc.y);
            ZeeManagerGobs.gobPlace(plob,0);
            if(waitPlayerIdlePose()) {
                Gob newPile = ZeeConfig.findGobByNameAndCoord("stockpile-board", pileCoord);
                if (newPile==null){
                    println("place and autopile > new pile undecided");
                }else {
                    ZeeManagerStockpile.pileBoardsFromTreelog(newPile);
                }
            }else{
                println("place and autopile > failed waiting idle pose?");
            }
        }
        else if(pileName.endsWith("stockpile-stone") && ZeeManagerStockpile.lastBoulderChipped!=null) {
            Coord2d pileCoord = new Coord2d(plob.rc.x, plob.rc.y);
            ZeeManagerGobs.gobPlace(plob,0);
            if(waitPlayerIdlePose()) {
                Gob newPile = ZeeConfig.findGobByNameAndCoord("stockpile-stone", pileCoord);
                if (newPile==null){
                    println("place and autopile > new pile undecided");
                }else {
                    ZeeManagerStockpile.pileStonesFromBoulder(ZeeManagerStockpile.lastBoulderChipped,newPile);
                }
            }else{
                println("place and autopile > failed waiting idle pose?");
            }
        }
    }

    private static boolean isTileInspectQl(Coord2d coordMc) {
        if (isWaterTile(coordMc))
            return true;
        String resName = ZeeConfig.getTileResName(coordMc);
        //println(resName);
        if (resName.contentEquals(ZeeConfig.TILE_BEACH) || resName.contains("tiles/dirt"))
            return true;
        return false;
    }

    static void cauldronOpen() {
        new ZeeThread(){
            public void run() {
                try {
                    // dismount horse/kicksled
                    if (ZeeConfig.isPlayerMountingHorse() || ZeeConfig.isPlayerDrivingingKicksled()) {
                        Coord cauldronCoord = ZeeConfig.lastMapViewClickMc.floor(posres);
                        ZeeManagerGobs.disembarkVehicle(cauldronCoord);
                        sleep(777);
                    }
                    // open cauldron
                    clickGobPetal(gob,"Open");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    static void cauldronPutOut() {
        // no new thread needed, as long as caller already created one
        try {
            // dismount horse/kicksled
            if (ZeeConfig.isPlayerMountingHorse() || ZeeConfig.isPlayerDrivingingKicksled()) {
                Coord cauldronCoord = ZeeConfig.lastMapViewClickMc.floor(posres);
                ZeeManagerGobs.disembarkVehicle(cauldronCoord);
                sleep(777);
            }
            // "put out" cauldron
            clickGobPetal(gob,"Put out");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void disembarkBoatAtShore(Coord2d mc){
        try {
            ZeeConfig.addPlayerText("boatin");
            //move to shore
            ZeeConfig.clickTile(ZeeConfig.coordToTile(coordMc), 1);
            if (!waitPlayerIdleRc()){
                throw new Exception("failed waiting idle rc");
            }
            //disembark
            ZeeConfig.clickTile(ZeeConfig.coordToTile(mc), 1, UI.MOD_CTRL);
        }catch (Exception e){
            println("disembarkBoatAtShore > "+e.getMessage());
        }
        ZeeConfig.removePlayerText();
    }

    static void disembarkEquipCoracle(Coord2d coordMc){
        try {
            ZeeConfig.addPlayerText("coracling");
            //move to shore
            ZeeConfig.clickTile(ZeeConfig.coordToTile(coordMc),1);
            waitPlayerPose(ZeeConfig.POSE_PLAYER_CORACLE_IDLE);
            //disembark
            ZeeConfig.clickTile(ZeeConfig.coordToTile(coordMc),1,UI.MOD_CTRL);
            sleep(PING_MS*2);
            if (ZeeConfig.isPlayerOnCoracle()){
                println("couldn't dismount coracle");
                ZeeConfig.removePlayerText();
                return;
            }
            //find coracle
            Gob coracle = ZeeConfig.getClosestGobByNameContains("/coracle");
            if (coracle == null) {
                println("couldn't find gob coracle");
                ZeeConfig.removePlayerText();
                return;
            }
            //try pickup coracle, if cape slot empty
            clickGobPetal(coracle,"Pick up");
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }

    static void dropEmbarkCoracle(Coord2d waterMc) {
        try {
            ZeeConfig.addPlayerText("coracling");

            //wait player reach water
            Gob player = ZeeConfig.getPlayerGob();
            long timeout = 5000;
            ZeeConfig.clickTile(ZeeConfig.coordToTile(waterMc),1);
            waitNotPlayerPose(ZeeConfig.POSE_PLAYER_IDLE);
            while(!ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_IDLE) && !isWaterTile(player.rc)){
                if (timeout<=0){
                    println("couldn't reach water tile");
                    ZeeConfig.removePlayerText();
                    return;
                }
                timeout -= PING_MS;
                sleep(PING_MS);
            }

            if (ZeeConfig.isCancelClick()){
                ZeeConfig.removePlayerText();
                return;
            }

            //drop coracle at shalow water or terrain
            ZeeManagerItems.getEquipory().dropItemByNameContains("gfx/invobjs/small/coracle");
            ZeeConfig.stopMovingEscKey();
            waitNotPlayerPose(ZeeConfig.POSE_PLAYER_CORACLE_CAPE);


            //find coracle gob
            Gob coracle = ZeeConfig.getClosestGobByNameContains("/coracle");
            if (coracle == null) {
                println("couldn't find gob coracle");
                ZeeConfig.removePlayerText();
                return;
            }

            //if dropped tile is not water
            if (!isWaterTile(ZeeConfig.getGobTile(coracle))){
                //lift up coracle
                liftGob(coracle);
                sleep(PING_MS);
                if (ZeeConfig.isCancelClick()){
                    ZeeConfig.removePlayerText();
                    return;
                }
                // place coracle at water tile
                ZeeConfig.clickTile(ZeeConfig.coordToTile(waterMc),3);
                waitPlayerIdlePose();
                if (ZeeConfig.distanceToPlayer(coracle)==0){
                    // player blocked by deep water tile
                    Coord pc = ZeeConfig.getPlayerCoord();
                    Coord subc = ZeeConfig.coordToTile(waterMc).sub(pc);
                    int xsignal, ysignal;
                    xsignal = subc.x >= 0 ? -1 : 1;
                    ysignal = subc.y >= 0 ? -1 : 1;
                    //try to drop coracle torwards clicked water coord
                    ZeeConfig.clickCoord(pc.add(xsignal * 300, ysignal * 300), 3);
                    sleep(PING_MS*2);
                    if (ZeeConfig.isCancelClick()){
                        ZeeConfig.removePlayerText();
                        return;
                    }
                    if (ZeeConfig.distanceToPlayer(coracle)==0) {
                        println("failed dropping to deep water?");
                        ZeeConfig.removePlayerText();
                        return;
                    }
                }
            }

            //mount coracle
            clickGobPetal(coracle, "Into the blue yonder!");
            waitPlayerPose(ZeeConfig.POSE_PLAYER_CORACLE_IDLE);

        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }

    private static void inspectSandAt(Coord2d cmc) {
        new ZeeThread(){
            public void run() {
                Gob gobShip = null;
                try {

                    //disembark boats
                    gobShip = ZeeConfig.getPlayerFollowTarget();
                    if (gobShip!=null){
                        disembarkBoatAtShore(cmc);
                    }

                    // dig icon
                    ZeeConfig.cursorChange(ZeeConfig.ACT_DIG);
                    if (!waitCursorName(ZeeConfig.CURSOR_DIG))
                        throw new Exception("couldn't activate dig cursor");

                    // click tile source
                    ZeeConfig.clickCoord(cmc.floor(posres), 1);
                    sleep(PING_MS);

                    // wait inv sand
                    GItem gItem = waitInvItemOrCancelClick();
                    if (gItem==null)
                        throw  new Exception("wait inv item: cancel click?");
                    String itemName = gItem.getres().name;
                    if (!itemName.contains("/sand"))
                        throw  new Exception("inv item is not sand");

                    // stop digging, remove cursor
                    ZeeConfig.stopMovingEscKey();
                    ZeeConfig.clickRemoveCursor();

                    // notify ql
                    int ql = gItem.getInfoQualityInt();
                    ZeeConfig.msgLow("sand "+ql);
                    ZeeAudio.textToSpeakLinuxFestival("sand "+ql);

                    //drop item
                    gItem.wdgmsg("drop",Coord.z);

                } catch (Exception e) {
                    println("inspectSandAt > "+e.getMessage());
                }

                //reembark ship
                if (gobShip!=null){
                    clickGobPetal(gobShip,0);
                }

            }
        }.start();
    }

    private static void inspectClayAt(Coord2d cmc) {
        new ZeeThread(){
            public void run() {
                // disable autostack for clay inspection
                boolean prevAutostack = ZeeConfig.autoStack;
                if (ZeeConfig.autoStack) {
                    ZeeConfig.toggleAutostack();
                }

                try {

                    // disembark acreclay
                    if (!isWaterTile(cmc) && ZeeConfig.isPlayerPoseOnAnyShip()){
                        disembarkBoatAtShore(cmc);
                    }

                    // dig icon
                    ZeeConfig.cursorChange(ZeeConfig.ACT_DIG);
                    if (!waitCursorName(ZeeConfig.CURSOR_DIG))
                        throw new Exception("couldn't activate dig cursor");

                    // click tile source
                    ZeeConfig.clickCoord(cmc.floor(posres), 1);
                    sleep(PING_MS);

                    // wait inv clay
                    GItem gItem = waitInvItemOrCancelClick();
                    if (gItem==null)
                        throw  new Exception("wait inv item: cancel click?");
                    String basename = ZeeConfig.getResBasename(gItem.getres().name);
                    if (!basename.startsWith("clay-"))
                        throw  new Exception("inv item is not clay");

                    // stop digging, remove cursor
                    ZeeConfig.stopMovingEscKey();
                    ZeeConfig.clickRemoveCursor();

                    // notify ql
                    int ql = gItem.getInfoQualityInt();
                    ZeeConfig.msgLow(basename+" "+ql);
                    ZeeAudio.textToSpeakLinuxFestival("clay "+ql);

                    //drop clay
                    gItem.wdgmsg("drop",Coord.z);

                } catch (Exception e) {
                    println("inspectClayAt > "+e.getMessage());
                }

                //restore autostack
                if (prevAutostack && !ZeeConfig.autoStack)
                    ZeeConfig.toggleAutostack();
            }
        }.start();
    }

    public static boolean isWaterTile(Coord2d coordMc) {
        return isWaterTile(coordMc.floor(MCache.tilesz));
    }

    public static boolean isWaterTile(Coord tile) {
        Tiler t = ZeeConfig.getTilerAt(tile);
        return t!=null && t instanceof WaterTile;
    }


    static boolean clickedPlantGobForLabelingQl = false;
    static Gob plantGobForLabelingQl;
    static void labelHarvestedPlant(String clickedPetal){

        if ( !clickedPlantGobForLabelingQl || !clickedPetal.contentEquals("Harvest"))
            return;

        if(ZeeConfig.getMainInventory().getNumberOfFreeSlots() == 0){
            println("labelHarvestedPlant > inv full");
            return;
        }

        if( ZeeConfig.lastInvGItemCreatedMs > ZeeConfig.lastMapViewClickMs &&
            !ZeeConfig.lastInvGItemCreatedName.endsWith("plants/wine") &&
            !ZeeConfig.lastInvGItemCreatedName.endsWith("plants/hops") &&
            !ZeeConfig.lastInvGItemCreatedName.endsWith("plants/pepper"))
        {
            println("labelHarvestedPlant > invalid lastInvItem name ("+ZeeConfig.lastInvGItemCreatedName +")");
            return;
        }

        //save plant gob
        plantGobForLabelingQl = ZeeConfig.lastMapViewClickGob;

        new ZeeThread(){
            public void run() {
                try{
                    ZeeConfig.addPlayerText("inspect ql");
                    long t1 = ZeeThread.now();
                    //wait approaching plant
                    if(waitPlayerIdlePose()) {
                        //wait inventory harvested item
                        ZeeConfig.lastMapViewClickButton = 2; // prepare cancel click
                        while(ZeeConfig.lastInvGItemCreatedMs < t1 && !ZeeConfig.isCancelClick()){
                            sleep(PING_MS);
                        }
                        //timeout?
                        if (ZeeConfig.isCancelClick()){
                            println("label plant canceled by click");
                            clickedPlantGobForLabelingQl = false;
                            ZeeConfig.removePlayerText();
                            return;
                        }
                        // scythe not allowed
                        if (ZeeManagerItems.isItemEquipped("/scythe")){
                            println("labelHarvestedPlant > cancel labeling due to scythe equipped");
                            clickedPlantGobForLabelingQl = false;
                            ZeeConfig.removePlayerText();
                            return;
                        }
                        //label plant
                        Inventory inv = ZeeConfig.getMainInventory();
                        ZeeConfig.addGobText(
                            ZeeConfig.lastMapViewClickGob,
                            ""+ZeeConfig.lastInvGItemCreated.getInfoQualityInt()
                        );
                    }
                    else{
                        // idle pose failed, cancel click?
                        println("labelHarvestedPlant > failed waiting player idle pose");
                        clickedPlantGobForLabelingQl = false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                clickedPlantGobForLabelingQl = false;
                ZeeConfig.removePlayerText();
            }
        }.start();

    }

    static boolean activateHarvestGob(Gob cropGob){
        if (cropGob==null)
            return false;
        gobClick(cropGob, 3, UI.MOD_SHIFT);
        return waitCursorName(ZeeConfig.CURSOR_HARVEST);
    }

    static Gob gobAutoLabel;
    static void checkRightClickGob(Coord pc, Coord2d mc, Gob gobClicked, String name) {

        coordPc = pc;
        coordMc = mc;
        gob = gobClicked;
        gobName = name;

        ZeeFlowerMenu.guessMenuSource(gob);

        //gob to be labeled when window contents open
        if (autoLabelGobsBasename.contains(gob.getres().basename()))
            gobAutoLabel = gob;
        else
            gobAutoLabel = null;

        // bugCollectionAuto label bug containers and wood pile
        if (ZeeManagerCraft.bugColRecipeOpen && !ZeeManagerCraft.bugColBusy){
            ZeeManagerCraft.bugColGobClicked(gob);
        }

        // label harvested plant ql
        clickedPlantGobForLabelingQl = gobName.endsWith("plants/wine") || gobName.endsWith("plants/hops") || gobName.endsWith("plants/pepper");
        if(clickedPlantGobForLabelingQl) {
            if (ZeeManagerItems.isItemInHandSlot("/scythe")) {
                //println("cancel labeling plant > scythe already equipped");
                clickedPlantGobForLabelingQl = false;
            }
            else if(ZeeManagerItems.isTwoHandedItemEquippable("/scythe")){
                //println("cancel labeling plant > scythe equippable");
                clickedPlantGobForLabelingQl = false;
            }
        }
        // refil cauldron aux containers, barrels
        else if(ZeeConfig.isPlayerFollowingCauldron && (ZeeConfig.isGobContainer(gobName) || gobName.endsWith("/barrel"))){
            if (ZeeConfig.listCauldronContainers==null)
                ZeeConfig.listCauldronContainers = new ArrayList<>();
            List<Gob> list = ZeeConfig.listCauldronContainers;
            if (!list.contains(gob)) {
                list.add(gob);
                ZeeConfig.addPlayerText("conts "+list.size());
            }
        }
        //cupboard labeler
        else if(ZeeCupboardLabeler.isActive && gobName.endsWith("/cupboard")){
            ZeeCupboardLabeler.lastCupboardClicked = gob;
        }
        // entering a house
        else if (isGobHouse(gobName) && !ZeeConfig.isPlayerMountingHorse()) {
            gobClick(gob, 3, 0, 16);//gob's door?
        }
        // click barrel transfer, label by contents
        else if (gobName.endsWith("/barrel") && ZeeConfig.getPlayerPoses().contains(ZeeConfig.POSE_PLAYER_LIFTING)) {
            new ZeeThread() {
                public void run() {
                    try {
                        if(!waitPlayerDistToGob(gob,15))
                            return;
                        sleep(555);
                        String barrelName = ZeeConfig.getBarrelOverlayBasename(gob);
                        if (!barrelName.isEmpty())
                            ZeeConfig.addGobText(gob, barrelName);
                        Gob carryingBarrel = ZeeConfig.isPlayerLiftingGobNamecontains("/barrel");
                        if (carryingBarrel!=null) {
                            barrelName = ZeeConfig.getBarrelOverlayBasename(carryingBarrel);
                            if (!barrelName.isEmpty())
                                ZeeConfig.addGobText(carryingBarrel, barrelName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        // clicked wheelbarrel
        else if(gobName.endsWith("/wheelbarrow")  && !ZeeConfig.isPlayerLiftingGob(gob)){
            new ZeeThread() {
                public void run() {
                    try {
                        if (ZeeConfig.isPlayerMountingHorse()) {
                            //dismount horse
                            dismountHorse(mc);
                            //re-drive wheelbarrow
                            gobClick(gob,3);
                        }
                        if (ZeeConfig.isPlayerDrivingingKicksled()) {
                            //disembark kicksled
                            disembarkVehicle(mc);
                            //re-drive wheelbarrow
                            gobClick(gob,3);
                        }
                        //show gridline
                        if(ZeeConfig.autoToggleGridLines && waitPlayerPose(ZeeConfig.POSE_PLAYER_DRIVE_WHEELBARROW)){
                            ZeeConfig.gameUI.map.showgrid(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        // while driving wheelbarrow: lift and click
        else if (ZeeConfig.isPlayerDrivingWheelbarrow() &&
                ( ZeeConfig.nameInListEndsWith(gobName,"/cart,/rowboat,/snekkja,/knarr,/wagon,/spark,/gardenshed,/upstairs,/downstairs,/cellardoor,/cellarstairs,/minehole,/ladder,/cavein,/caveout,/burrow,/igloo,gate")
                  || isGobHouse(gobName) || isGobHouseInnerDoor(gobName)))
        {
            String finalGobName = gobName;
            new ZeeThread() {
                public void run() {
                    Gob wb = ZeeConfig.getClosestGobByNameContains("/wheelbarrow");
                    if (isGobHouse(finalGobName)) {
                        try {
                            liftGob(wb);
                            sleep(100);
                            gobClick(gob, 3, 0, 16);//gob's door?
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        liftGobAndClickTarget(wb, gob);
                    }
                }
            }.start();
        }
        // gob requires unmounting horse/kicksled (if not lifting gob itself)
        else if (isGobRequireDisembarkVehicle(gob) && !ZeeConfig.isPlayerLiftingGob(gob)){
            // unmount horse
            if (ZeeConfig.isPlayerMountingHorse() && !ZeeConfig.nameInListEndsWith(gobName,"/ladder,/minehole") && ZeeConfig.getMainInventory().countItemsByNameContains("/rope") > 0) {
                dismountHorseAndClickGob(mc);
            }
            // disembark kicksled
            else if(ZeeConfig.isPlayerDrivingingKicksled()){
                String finalGobName1 = gobName;
                new ZeeThread() {
                    public void run() {
                        try {
                            disembarkVehicle(mc);
                            if(waitPlayerPoseNotInListTimeout(1000,ZeeConfig.POSE_PLAYER_KICKSLED_IDLE, ZeeConfig.POSE_PLAYER_KICKSLED_ACTIVE)) {
                                sleep(100);//lagalagalaga
                                if (isGobHouse(finalGobName1))
                                    gobClick(gob, 3, 0, 16);//gob's door?
                                else
                                    gobClick(gob, 3);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
        // use wheelbarrow on stockpile, dismount if necessary
        else if ( isGobStockpile(gobName) && ZeeConfig.isPlayerCarryingWheelbarrow()){
            new ZeeThread() {
                public void run() {
                    unloadWheelbarrowAtGob(gob);
                    if (ZeeConfig.autoToggleGridLines)
                        ZeeConfig.gameUI.map.showgrid(true);
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
        // rclick tree tries collecting sap with lifted barrel
        else if (ZeeConfig.isPlayerLiftingGobNamecontains("gfx/terobjs/barrel")!=null && ZeeConfig.nameInListEndsWith(gobName,"/trees/birch,/trees/maple,/trees/terebinth")){
            collectTreeSapUsingBarrel(gob);
        }
    }

    static boolean isPickingKritterDismountHorse = false;
    static void waitKritDistThenDismountHorseAndPickup(Gob kritter,boolean shift) {

        if (isPickingKritterDismountHorse)
            return;

        if (!ZeeConfig.isPlayerMountingHorse())
            return;

        String kritName = kritter.getres().name;
        if( !kritName.endsWith("/grasshopper") && !kritName.endsWith("/rat"))
            return;

        new ZeeThread(){
            @Override
            public void run() {
                isPickingKritterDismountHorse = true;
                try {
                    ZeeConfig.addPlayerText("dismounting");
                    if(waitPlayerDistToGobOrCancelClick(kritter,50)){
                        if(dismountHorse(kritter.rc))
                            gobClick(kritter,3); //kritters have no pick menu?
                        else
                            ZeeConfig.msgError("dismount horse failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isPickingKritterDismountHorse = false;
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    private static void collectTreeSapUsingBarrel(Gob tree) {
        List<Gob.Overlay> saps = getGobOverlaysByName(tree, "gfx/terobjs/saptap");
        if (saps.isEmpty()){
            ZeeConfig.println("no saptap attached");
            return;
        }
        new ZeeThread(){
            public void run() {
                try {
                    ZeeConfig.addPlayerText("saptap");
                    Gob.Overlay ol = saps.get(0);
                    gobClickOverlay(tree,3,0,ol.id);
                    prepareCancelClick();
                    if (!waitPlayerIdlePose()){
                        println("couldn't wait saptap?");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    private static void dismountHorseAndClickGob(Coord2d mc) {
        new ZeeThread() {
            public void run() {
                if(dismountHorse(mc)) {
                    // entering a house
                    if (isGobHouse(gobName)) {
                        gobClick(gob, 3, 0, 16);//gob's door?
                    }
                    // entering a non-house (cave, cellar, stairs)
                    else {
                        gobClick(gob, 3);
                    }
                }
            }
        }.start();
    }

    static boolean isRightClickZooming = false;
    static int rClickZoomLastY;
    private static void startRightClickZooming(Gob g, Coord pc) {
        rClickZoomLastY = pc.y;
        // start watching if clicked center box of 50x50
        //if ( Math.abs(rcguisz.x - pc.x) < 50  &&  Math.abs(rcguisz.y - pc.y) < 50){
        if(g.id == ZeeConfig.gameUI.plid){//clicked player?
            isRightClickZooming = true;
        }else{
            isRightClickZooming = false;
        }
    }
    static void rightClickZoom(Coord pc){
        if(pc.y < rClickZoomLastY){
            ZeeConfig.gameUI.map.camera.wheel(pc,-2);
        }
        else if(pc.y > rClickZoomLastY){
            ZeeConfig.gameUI.map.camera.wheel(pc,2);
        }
        rClickZoomLastY = pc.y;
    }

    static boolean isGobAmbientPassage(Gob gob){
        String gobName = gob.getres().name;
        if( isGobHouseInnerDoor(gobName) ||
            isGobHouse(gobName) ||
            ZeeConfig.nameInListEndsWith(gobName,"/upstairs,/downstairs,/minehole,"+
                    "/ladder,/cavein,/caveout,/burrow,/igloo,/cellardoor") )
        {
            return true;
        }

        return false;
    }

    static boolean isGobRequireDisembarkVehicle(Gob gob) {

        String gobName = gob.getres().name;

        if ( gobName.endsWith("/cellardoor") && ZeeConfig.distanceToPlayer(gob) > TILE_SIZE*1 ){
            return true;
        }

        if (isGobAmbientPassage(gob) && !gobName.endsWith("/cellardoor")){
            return true;
        }

        if( isGobSittingFurniture(gobName)) {
            return true;
        }

        if(ZeeConfig.nameInListEndsWith(gobName,"/wheelbarrow,/loom,/churn,/swheel,/ropewalk,/meatgrinder,/potterswheel,/quern,/plow,/winepress,/hookah")){
            return true;
        }

        // avoid dismouting when transfering to cauldron
        if (gobName.contains("cauldron") && !ZeeConfig.isPlayerLiftingPose()) {
            return true;
        }

        return false;
    }

    static boolean isGobSittingFurniture(String gobName) {
        if ( gobName.contains("/furn/") &&
            ZeeConfig.nameInListContains(gobName,"throne,chair,sofa,stool,bench") )
            return true;
        if ( gobName.contains("rockinghorse") )
            return true;
        return false;
    }

    public static boolean isGobHouseInnerDoor(String gobName){
        return gobName.endsWith("-door");
    }

    public static boolean isGobHouse(String gobName) {
        final String list = "/logcabin,/timberhouse,/stonestead,/stonemansion,/stonetower,/greathall,/windmill,/greenhouse,/igloo,/primitivetent,/waterwheelhouse,/stonehut";
        return ZeeConfig.nameInListEndsWith(gobName,list);
    }

    public static boolean isGobWall(String gobName) {
        final String walls = "/palisadeseg,/palisadecp,/drystonewallseg,/drystonewallcp,/poleseg,/polecp,/brickwallseg,/brickwallcp";
        return ZeeConfig.nameInListEndsWith(gobName,walls);
    }


    static void toggleOverlayAggro(Gob gob) {
        ZeeConfig.findGobsByNameEquals(gob.getres().name).forEach(aggroGob -> {
            Gob.Overlay radius = aggroGob.findol(ZeeGobRadius.class);
            if (radius!=null){
                radius.remove();
            }else{
                aggroGob.addol(new Gob.Overlay(gob, new ZeeGobRadius(gob, null, ZeeConfig.aggroRadiusTiles * MCache.tilesz2.y,new Color(139, 139, 185, 48))));
            }
        });
    }

    private static void unloadWheelbarrowAtGob(Gob gob) {
        ZeeManagerStockpile.useWheelbarrowAtStockpile(gob);
    }

    public static void disembarkVehicle(Coord coordMc) {
        ZeeConfig.clickCoord(coordMc,1,UI.MOD_CTRL);
    }

    public static void disembarkVehicle(Coord2d coordMc) {
        disembarkVehicle(coordMc.floor(posres));
    }

    public static boolean dismountHorse(Coord2d coordMc) {
        Gob horse = ZeeConfig.getClosestGobByNameContains("gfx/kritter/horse/");
        ZeeConfig.clickCoord(coordMc.floor(posres),1,UI.MOD_CTRL);
        if(waitPlayerDismounted(horse)) {
            if (ZeeConfig.autoRunLogin && !ZeeConfig.isPlayerMountingHorse() && ZeeConfig.getPlayerSpeed() != ZeeConfig.PLAYER_SPEED_RUN) {
                ZeeConfig.setPlayerSpeed(ZeeConfig.PLAYER_SPEED_RUN);
            }
            return true;
        }else{
            return false;
        }
    }

    public static void mountHorse(Gob horse){
        int playerSpeed = ZeeConfig.getPlayerSpeed();
        clickGobPetal(horse,"Giddyup!");
        waitPlayerMounted(horse);
        if (ZeeConfig.autoRunLogin && ZeeConfig.isPlayerMountingHorse() && ZeeConfig.getPlayerSpeed() != ZeeConfig.PLAYER_SPEED_RUN) {
            ZeeConfig.setPlayerSpeed(ZeeConfig.PLAYER_SPEED_RUN);
        }
    }

    private static void clickedGobHoldingItem(Gob gob, String gobName) {
        if (isGobStockpile(gobName))
            itemActGob(gob,UI.MOD_SHIFT);//try piling all items
        else
            gobClick(gob,3,0); // try ctrl+click simulation
    }

    private static void pickAllDreamsCloseBy(Gob catcher1){
        new Thread(){
            public void run() {

                try{
                    ZeeConfig.addPlayerText("dreaming");

                    //prepare for clickCancelTask()
                    ZeeConfig.lastMapViewClickButton = 2;

                    //pick dreams from 1st catcher
                    pickDreamsFromCatcher(catcher1);
                    sleep(100);

                    //try picking from other catchers closeby
                    List<Gob> catchers = ZeeConfig.findGobsByNameEndsWith("/dreca");
                    catchers.removeIf(dreca -> ZeeConfig.distanceToPlayer(dreca) > 40);
                    Gob dc;
                    for (int i = 0; i < catchers.size(); i++) {
                        dc = catchers.get(i);
                        //cancel click
                        if (ZeeConfig.isCancelClick())
                            break;
                        //skip 1st catcher
                        if(dc.equals(catcher1))
                            continue;
                        pickDreamsFromCatcher(dc);
                        sleep(100);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    private static void pickDreamsFromCatcher(Gob dreamCatcher) {
        ZeeConfig.addGobText(dreamCatcher,"target");
        if(clickGobPetal(dreamCatcher,"Harvest")) {
            waitPlayerDistToGob(dreamCatcher,15);
            waitNoFlowerMenu();
            if(clickGobPetal(dreamCatcher,"Harvest"))
                waitNoFlowerMenu();
        }
        ZeeConfig.removeGobText(dreamCatcher);
    }

    public static boolean pickupTorch() {
        if (ZeeManagerItems.pickupBeltItem("/torch")) {
            return true;
        }else if(ZeeManagerItems.pickupHandItem("/torch")){
            return true;
        }else if (ZeeManagerItems.pickUpInvItem(ZeeConfig.getMainInventory(),"/torch")){
            return true;
        }
        return false;
    }


    public static void groundZeeMenuClicked(Coord2d coordMc, String petalName){

        if (petalName.contentEquals("dig"))
            ZeeConfig.gameUI.menu.wdgmsg("act","dig","0");
        else if (petalName.contentEquals("mine"))
            ZeeConfig.gameUI.menu.wdgmsg("act","mine","0");
        else if (petalName.contentEquals("plow"))
            ZeeConfig.gameUI.menu.wdgmsg("act","pave","field","0");
        else if (petalName.contentEquals("stomp"))
            ZeeConfig.gameUI.menu.wdgmsg("act","pave","stomp","0");
        else if (petalName.contentEquals("survey"))
            ZeeConfig.gameUI.menu.wdgmsg("act","survey","0");
        else if (petalName.contentEquals("fish"))
            ZeeConfig.gameUI.menu.wdgmsg("act","fish","0");
        else if (petalName.contentEquals("inspect water"))
            ZeeResearch.inspectWaterWindow(coordMc);
        else if (petalName.contentEquals("inspect clay"))
            inspectClayAt(coordMc);
        else if (petalName.contentEquals("inspect sand"))
            inspectSandAt(coordMc);
        else if (petalName.contentEquals("disembark"))
            disembarkBoatAtShore(coordMc);
        else if(petalName.contentEquals("embark coracle"))
            dropEmbarkCoracle(coordMc);
        else if(petalName.contentEquals( "build road"))
            ZeeConfig.gameUI.menu.wdgmsg("act","bp","woodendstone","0");
    }

    public static void gobZeeMenuClicked(Gob gob, String petalName){

        String gobName = gob.getres().name;

        if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_TOGGLE_CATTLEROSTER))
            ZeeConfig.getMenuButton("croster").use(new MenuGrid.Interaction(1, ZeeConfig.gameUI.ui.modflags()));
        else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_AUTO_SHEAR))
            autoShear(gob);
        else if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_MEMORIZEAREANIMALS))
            ZeeConfig.gameUI.menu.wdgmsg("act","croster","a");
        else if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_TILEMONITOR))
            ZeeManagerMiner.tileMonitorWindow();
        else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_SWITCHCHAR))
            ZeeSess.charSwitchCreateWindow();
        else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_PICKUP_GOBS))
            ZeeManagerGobs.showWindowPickupGob();
        else if(petalName.contentEquals(ZeeQuickOptionsWindow.WIN_TITLE))
            ZeeQuickOptionsWindow.initWindow();
        else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_TESTCOORDS))
            windowTestCoords();
        else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_AUDIOBLOCKER))
            ZeeAudio.aBlockWin();
        else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_ZEECOW_CMDS))
            ZeeConsole.showWindow();
        else if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_AUTOBUTCH_BIGDEADANIMAL)){
            autoButchBigDeadAnimal(gob);
        }
        else if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_LIFTUPGOB)){
            liftGob(gob);
        }
        else if (isGobTrellisPlant(gobName)){
            if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_REMOVEPLANT)) {
                destroyGob(gob);
            }
            else if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_REMOVEALLPLANTS)){
                removeAllTrellisPlants(gob);
            }
            else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_CURSORHARVEST)){
                if (!ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_HARVEST))
                    gobClick(gob, 3, UI.MOD_SHIFT);
            }
        }
        // toggle tree growth
        else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_TOGGLEGROWTHTEXTS)){
            // variable toggled outside function because QuickOptions runnable
            ZeeConfig.showGrowingTreeScale = !ZeeConfig.showGrowingTreeScale;
            toggleAllTreeGrowthTexts();
        }
        // remove tree & stump
        else if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_REMOVETREEANDSTUMP)) {
            ZeeManagerTrees.removeTreeAndStump(gob, petalName);
        }
        // ispect towercap tree
        else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_INSPECT) && isGobTree(gobName)){
            inspectGob(gob);
        }
        // harvested pumpkins
        else if(gobName.endsWith("/items/pumpkin")){
            if (petalName.contentEquals("Slice all pumpkins"))
                ZeeManagerFarmer.sliceAllHarvestedPumpkins(gob);
            else if(petalName.contentEquals("Show piler window"))
                ZeeManagerStockpile.areaPilerWindow(gob);
        }
        // crop
        else if (isGobCrop(gobName)) {
            if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_SEEDFARMER)) {
                ZeeManagerFarmer.showSeedFarmerWindow(gob);
            }
            else if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_CURSORHARVEST)) {
                if (!ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_HARVEST))
                    gobClick(gob, 3, UI.MOD_SHIFT);
            }
            else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_FARMAWAY)){
                ZeeManagerFarmer.farmAway(gob);
            }
        }
        else if (petalName.equals(ZeeFlowerMenu.STRPETAL_BARRELTAKEALL)) {
            barrelTakeAllSeeds(gob);
        }
        else if ( petalName.contentEquals(ZeeFlowerMenu.STRPETAL_DESTROY_TREELOGS)) {
            ZeeManagerTrees.destroyTreelogs(gob);
        }
        else if ( petalName.contentEquals(ZeeFlowerMenu.STRPETAL_TREELOGANIZE)) {
            ZeeConfig.treeloganize = true;
            liftGob(gob);
            if (!waitPlayerPose(ZeeConfig.POSE_PLAYER_LIFTING)){
                ZeeManagerTrees.treeloganizerExit("couldnt lift treelog");
            }
        }
        else if(petalName.contentEquals("wave")){
            ZeeConfig.gameUI.menu.wdgmsg("act","pose","wave",0);
        }
        else if(petalName.contentEquals("laugh")){
            ZeeConfig.gameUI.menu.wdgmsg("act","pose","lol",0);
        }
        else if(petalName.contentEquals("get XP from well")){
            getXpFromWell(gob);
        }
        // generic "Build [argName]"
        else if(petalName.startsWith("Build ")){
            String[] arr = petalName.split(" ");
            if (arr==null)
                println("context menu > build > arr null");
            else if (arr.length < 2)
                println("context menu > build >  arr length too short");
            else
                ZeeConfig.gameUI.menu.wdgmsg("act","bp",arr[1],0);
        }
        // generic "Craft [argName]"
        else if(petalName.startsWith("Craft ")){
            String[] arr = petalName.split(" ");
            if (arr==null)
                println("context menu > craft > arr null");
            else if (arr.length < 2)
                println("context menu > craft > arr length too short");
            else
                ZeeConfig.gameUI.menu.wdgmsg("act","craft",arr[1],0);
        }
        else{
            println("chooseGobFlowerMenu > unkown case");
        }
    }

    static void toggleAllTreeGrowthTexts() {
        try {
            if (ZeeConfig.showGrowingTreeScale) {
                ZeeConfig.msgLow("show trees growth ");
            } else {
                ZeeConfig.msgLow("hide trees growth ");
            }

            // find trees and bushes
            List<Gob> gobs = ZeeConfig.getAllGobs();
            gobs.removeIf(gob1 -> {
                String name = gob1.getres().name;
                if (!isGobTree(name) && !isGobBush(name))
                    return true;
                return false;
            });

            // toggle off
            if (!ZeeConfig.showGrowingTreeScale) {
                ZeeConfig.removeGobText((ArrayList<Gob>) gobs);
            }
            // toggle on
            else {
                for (Gob g : gobs) {
                    ZeeManagerTrees.labelTreeGrowth(g);
                }
            }

            Utils.setprefb("showGrowingTreeScale",ZeeConfig.showGrowingTreeScale);

            // quick options window
            ZeeQuickOptionsWindow.updateCheckboxNoBump("showGrowingTreeScale",ZeeConfig.showGrowingTreeScale);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void getXpFromWell(Gob well) {

        if (well == null ){
            ZeeConfig.msgError("need a well");
            return;
        }

        Gob barrel = ZeeConfig.getClosestGobByNameEnds(well,"/barrel");
        if (barrel == null ){
            ZeeConfig.msgError("need a barrel");
            return;
        }

        if (!isBarrelEmpty(barrel)){
            ZeeConfig.msgError("closest barrel to well is not empty");
            return;
        }

        Float dist = ZeeConfig.distanceBetweenGobs(well,barrel);
        if (dist==null){
            ZeeConfig.msgError("getXpFromWell > gob not ready");
            return;
        }
        if ( dist > TILE_SIZE*3){
            ZeeConfig.msgError("barrel too distant from well");
            return;
        }

        new ZeeThread(){
            public void run() {
                boolean backupConfirmPetal = ZeeConfig.confirmPetal;
                try {
                    ZeeConfig.addPlayerText("getting xp");
                    Coord barrelCoord = ZeeConfig.getGobCoord(barrel);
                    //lift barrel
                    if(liftGob(barrel)){
                        // click well
                        gobClick(well,3);
                        if(waitPlayerIdlePose() && waitNoHourglass()) {
                            //return barrel
                            ZeeConfig.clickCoord(barrelCoord, 3);
                            if (waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_LIFTING)){
                                //empty barrel
                                gobClick(barrel,3);
                                if(waitWindowOpened("Barrel")){
                                    Set<Button> buttons = ZeeConfig.getWindow("Barrel").children(Button.class);
                                    if (buttons!=null && !buttons.isEmpty()){
                                        for (Button button : buttons) {
                                            if (button.text.text.contentEquals("Empty")){
                                                ZeeConfig.confirmPetal = false;
                                                button.click();
                                            }
                                        }
                                    }else {
                                        println("getXpFromWell > couldnt find barrel's empty button");
                                    }
                                }else {
                                    println("getXpFromWell > couldnt open barrel");
                                }
                            }else{
                                println("getXpFromWell > couldnt drop barrel");
                            }
                        }else{
                            println("getXpFromWell > couldnt reach the well ?");
                        }
                    }else{
                        println("getXpFromWell > couldnt lift barrel");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ZeeConfig.confirmPetal = backupConfirmPetal;
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    static void autoShear(Gob first){
        new ZeeThread(){
            public void run() {
                println("autoshear thread start");
                try {
                    List<Gob> animals = ZeeConfig.findGobsByNameContains("/sheep/", "/goat/");
                    for (Gob a : animals) {
                        ZeeConfig.addGobColor(a, Color.CYAN);
                    }
                    int cont = animals.size();
                    ZeeConfig.addPlayerText("autoshear "+cont);
                    Gob latest = null;
                    while(!animals.isEmpty() && !isCancelClick()) {
                        Gob animal = ZeeConfig.getClosestGobToPlayer(animals);
                        animals.remove(animal);
                        // click shear wool
                        if(clickGobPetal(animal,"Shear wool")){

                            // wait shearing start
                            prepareCancelClick();
                            sleep(555);
                            do {
                                sleep(100);
                            } while(!isCancelClick() &&
                                    (!ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_SHEARING) || ZeeConfig.isPlayerMovingByAttrLinMove())
                            );

                            // wait finish shearing
                            if (waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_SHEARING)){
                                ZeeConfig.removeGobColor(animal);
                            }
                        }
                        // animal has no wool
                        else{
                            cancelFlowerMenu();//TODO cancel menu faster
                            waitNoFlowerMenu();
                            ZeeConfig.removeGobColor(animal);
                        }
                        if(--cont > 0)
                            ZeeConfig.addPlayerText("autoshear " + cont);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ZeeConfig.removePlayerText();
                ZeeConfig.removeGobColor((ArrayList<Gob>) ZeeConfig.findGobsByNameContains("/sheep/", "/goat/"));
                println("autoshear thread stop");
            }
        }.start();
    }

    static boolean isAutoButchingBigAnimal = false;
    public static void autoButchBigDeadAnimal(Gob deadAnimal) {
        new ZeeThread() {
            public void run() {
                boolean butcherBackup = ZeeConfig.butcherMode;
                ZeeConfig.butcherAutoList = ZeeConfig.DEF_LIST_BUTCH_AUTO;
                try{
                    isAutoButchingBigAnimal = true;
                    ZeeConfig.addPlayerText("autobutch");

                    //wait start butching
                    ZeeConfig.lastInvGItemCreatedMs = 0;
                    ZeeConfig.butcherMode = true;
                    ZeeConfig.autoClickMenuOption = false;
                    gobClick(deadAnimal,3);
                    prepareCancelClick();
                    while(!isCancelClick() && !ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_BUTCH)){
                        sleep(500);
                    }

                    if (!isCancelClick()) {
                        String animalName = deadAnimal.getres().name;
                        // loop butching stages
                        while (!isCancelClick() && gobExistsBecauseFlowermenu(deadAnimal)) {

                            //prepare settings
                            ZeeConfig.lastInvGItemCreatedMs = 0;
                            ZeeConfig.butcherMode = true;
                            ZeeConfig.autoClickMenuOption = false;

                            //click gob
                            gobClick(deadAnimal, 3);

                            //wait not butching
                            sleep(500);//pose lag?
                            waitNotPlayerPose(ZeeConfig.POSE_PLAYER_BUTCH);
                        }
                        //pickup leftovers
                        if (animalName.contains("boreworm")){
                            Gob beak = ZeeConfig.getClosestGobByNameContains("borewormbeak");
                            if (beak!=null)
                                pickupAllGobItemsServerSide(beak);
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                ZeeConfig.butcherMode = butcherBackup;
                ZeeConfig.autoClickMenuOption = Utils.getprefb("autoClickMenuOption", true);
                ZeeConfig.removePlayerText();
                isAutoButchingBigAnimal = false;

                // check for butch queue
                queueButchNext();
            }
        }.start();
    }

    private static boolean showGobFlowerMenu(){

        boolean showMenu = true;
        ZeeFlowerMenu menu = null;
        ArrayList<String> opts;//petals array

        if (isGroundClick) {
            if (isWaterTile(coordMc)) {
                boolean isShallowWater = ZeeConfig.isTileNamed(coordMc,ZeeConfig.TILE_WATER_FRESH_SHALLOW,ZeeConfig.TILE_WATER_OCEAN_SHALLOW);
                opts = new ArrayList<String>();
                if (isShallowWater)
                    opts.add("dig");
                if (ZeeManagerItems.isCoracleEquipped() && !ZeeConfig.isPlayerMountingHorse()) {
                    opts.add("embark coracle");
                }
                opts.add("fish");
                if (isShallowWater)
                    opts.add("inspect clay");
                opts.add("inspect water");
                menu = new ZeeFlowerMenu(coordMc, opts.toArray(String[]::new));
            }
            else if(ZeeConfig.getTileResName(coordMc).contains("tiles/beach")) {
                opts = new ArrayList<String>();
                if (ZeeConfig.isPlayerPoseOnAnyShip())
                    opts.add("disembark");
                opts.add("inspect sand");
                menu = new ZeeFlowerMenu(coordMc, opts.toArray(String[]::new));
            }
            else if(ZeeConfig.getTileResName(coordMc).contains("tiles/dirt")) {
                opts = new ArrayList<String>();
                if (ZeeConfig.isPlayerPoseOnAnyShip())
                    opts.add("disembark");
                opts.add("inspect clay");
                menu = new ZeeFlowerMenu(coordMc, opts.toArray(String[]::new));
            }
            else if(!ZeeConfig.isPlobActive()){
                menu = new ZeeFlowerMenu(coordMc, "dig", "mine", "plow","stomp");
            }
            else
                showMenu = false;
        }
        else if(gob.tags.contains(Gob.Tag.PLAYER_MAIN)) {
            opts = new ArrayList<String>();
            opts.add(ZeeFlowerMenu.STRPETAL_ZEECOW_CMDS);
            opts.add(ZeeFlowerMenu.STRPETAL_PICKUP_GOBS);
            opts.add(ZeeFlowerMenu.STRPETAL_SWITCHCHAR);
            opts.add(ZeeFlowerMenu.STRPETAL_AUDIOBLOCKER);
            if (ZeeConfig.isCaveTile(ZeeConfig.getPlayerTileName()))
                opts.add(ZeeFlowerMenu.STRPETAL_TILEMONITOR);
            if (ZeeConfig.getWindow(ZeeQuickOptionsWindow.WIN_TITLE) == null)
                opts.add(ZeeQuickOptionsWindow.WIN_TITLE);
            menu = new ZeeFlowerMenu(gob, opts.toArray(String[]::new));
        }
        else if (isGobTamedAnimalOrAurochEtc(gobName) && !isGobDeadOrKO(gob)) {
            opts = new ArrayList<String>();
            opts.add(ZeeFlowerMenu.STRPETAL_MEMORIZEAREANIMALS);
            opts.add(ZeeFlowerMenu.STRPETAL_TOGGLE_CATTLEROSTER);
            if(gobName.contains("/sheep/") || gobName.contains("/goat/"))
                opts.add(ZeeFlowerMenu.STRPETAL_AUTO_SHEAR);
            menu = new ZeeFlowerMenu(gob, opts.toArray(String[]::new));
        }
        else if (isGobButchable(gobName) && isGobDeadOrKO(gob)) {
            menu = new ZeeFlowerMenu(gob,
                    ZeeFlowerMenu.STRPETAL_AUTOBUTCH_BIGDEADANIMAL,
                    ZeeFlowerMenu.STRPETAL_LIFTUPGOB);
        }
        else if(gobName.endsWith("terobjs/oven")){
            menu = new ZeeFlowerMenu(gob, ZeeFlowerMenu.STRPETAL_ADD4BRANCH);
        }
        else if(gobName.endsWith("terobjs/smelter")){
            menu = new ZeeFlowerMenu(gob,
                    ZeeFlowerMenu.STRPETAL_ADD9COAL,
                    ZeeFlowerMenu.STRPETAL_ADD12COAL);
        }
        // trellis plant
        else if (isGobTrellisPlant(gobName)){
            menu = new ZeeFlowerMenu( gob,
                    ZeeFlowerMenu.STRPETAL_CURSORHARVEST,
                    ZeeFlowerMenu.STRPETAL_REMOVEPLANT,
                    ZeeFlowerMenu.STRPETAL_REMOVEALLPLANTS);
        }
        // tree
        else if (isGobTree(gobName)){
            opts = new ArrayList<String>();
            opts.add(ZeeFlowerMenu.STRPETAL_REMOVETREEANDSTUMP);
            opts.add(ZeeFlowerMenu.STRPETAL_TOGGLEGROWTHTEXTS);
            if (gobName.endsWith("/towercap"))
                opts.add(ZeeFlowerMenu.STRPETAL_INSPECT);
            menu = new ZeeFlowerMenu(gob, opts.toArray(String[]::new));
        }
        // boulder
        else if (isGobBoulder(gobName)){
            opts = new ArrayList<String>();
            opts.add(ZeeFlowerMenu.STRPETAL_INSPECT);
            opts.add(ZeeFlowerMenu.STRPETAL_LIFTUPGOB);
            menu = new ZeeFlowerMenu(gob, opts.toArray(String[]::new));
        }
        // harvested pumpkin
        else if(gobName.endsWith("/items/pumpkin")){
            menu = new ZeeFlowerMenu(gob,
                    "Show piler window",
                    "Slice all pumpkins");
        }
        // crop
        else if (isGobCrop(gobName)) {
            opts = new ArrayList<String>();
            if (ZeeManagerFarmer.isCropStageHarvestable(gob))
                opts.add(ZeeFlowerMenu.STRPETAL_FARMAWAY);
            opts.add(ZeeFlowerMenu.STRPETAL_SEEDFARMER);
            menu = new ZeeFlowerMenu(gob, opts.toArray(String[]::new));
        }
        else if (isBarrelTakeAll(gob)) {
            menu = new ZeeFlowerMenu( gob,
                    ZeeFlowerMenu.STRPETAL_LIFTUPGOB,
                    ZeeFlowerMenu.STRPETAL_BARRELTAKEALL
            );
        }
        // tree logs
        else if (isGobTreeLog(ZeeManagerGobs.gobName)) {
            opts = new ArrayList<String>();
            if (ZeeManagerItems.isItemInHandSlot("bonesaw")) {
                opts.add(ZeeFlowerMenu.STRPETAL_DESTROY_TREELOGS);
            }
            opts.add(ZeeFlowerMenu.STRPETAL_LIFTUPGOB);
            opts.add(ZeeFlowerMenu.STRPETAL_TREELOGANIZE);
            menu = new ZeeFlowerMenu(gob, opts.toArray(String[]::new));
        }
        else if (gobName.endsWith("/well")) {
            menu = new ZeeFlowerMenu( gob, "get XP from well");
        }
        else if (gobName.endsWith("/wildbeehive")) {
            menu = new ZeeFlowerMenu( gob, "Build bpyre");
        }
        else if (isGobFireTarget(gob)) {
            menu = new ZeeFlowerMenu( gob,
                "Craft firebrand",
                "Craft pyritespark"
            );
        }
        else if(gobName.endsWith("terobjs/crucible")){
            menu = new ZeeFlowerMenu(gob,
                "Craft bronzebar",
                    "Craft denuggify",
                    "Craft nuggify");
        }
        else if (gobName.endsWith("/churn")) {
            menu = new ZeeFlowerMenu( gob,
                ZeeFlowerMenu.STRPETAL_LIFTUPGOB,
                "Craft butter",
                "Craft tubermash"
            );
        }
        else if (gobName.endsWith("/ropewalk")) {
            menu = new ZeeFlowerMenu( gob,"Craft rope" );
        }
        else if (gobName.endsWith("/swheel")) {
            menu = new ZeeFlowerMenu( gob, "Craft yarn",
                    ZeeFlowerMenu.STRPETAL_LIFTUPGOB
            );
        }
        else if (gobName.endsWith("/loom")) {
            menu = new ZeeFlowerMenu( gob,
                    ZeeFlowerMenu.STRPETAL_LIFTUPGOB,
                    "Craft hempcloth",
                    "Craft linencloth",
                    "Craft silkcloth",
                    "Craft woolcloth"
            );
        }
        else if (gobName.endsWith("/quern")) {
            menu = new ZeeFlowerMenu( gob,
                    ZeeFlowerMenu.STRPETAL_LIFTUPGOB,
                    "Craft flour",
                    "Craft grist"
            );
        }
        else{
            showMenu = false;
            //println("showGobFlowerMenu() > unkown case");
        }

        if (showMenu) {
            if (menu==null){
                println("showGobFlowerMenu > menu is null");
                return false;
            }
            ZeeConfig.gameUI.ui.root.add(menu, coordPc);
        }

        return showMenu;
    }

    private static boolean isGobDeadAnimal;

    static final List<String> autoLabelGobsBasename = List.of("barrel","cistern","demijohn","trough","oven","cauldron","fineryforge","smelter","primsmelter","steelcrucible","smokeshed","rabbithutch","chickencoop","kiln","curdingtub");
    static final List<String> autoLabelWincapContainers = List.of("Barrel","Cistern","Demijohn","Food trough");
    static final List<String> autoLabelWincapVmeters = List.of("Cauldron","Oven","Ore Smelter","Smith's Smelter","Stack furnace","Finery Forge","Steelbox","Smoke shed","Rabbit Hutch","Chicken Coop","Kiln","Curding Tub");
    static final Map<String,Integer> mapWincapMaxfuel = Map.of(
        "Oven",30,
        "Ore Smelter",30,
        "Smith's Smelter",30,
        "Finery Forge",15,
        "Smoke shed",10,
        "Kiln",30,
        "Stack furnace",30,
        "Steelbox",18
    );
    @SuppressWarnings("unchecked")
    public static void labelGobByContents(Window window) {
        new Thread(){
            public void run() {
                try{

                    sleep(PING_MS);//wait window build

                    if (gobAutoLabel==null){
                        println("labelGobByContents > gob not eligible");
                        return;
                    }

                    if (window.children()==null) {
                        println("labelGobByContents > window.children null");
                        return;
                    }

                    // label vertical meters
                    if (autoLabelWincapVmeters.contains(window.cap))
                    {
                        Set<VMeter> vmeter = window.children(VMeter.class);
                        if (vmeter==null || vmeter.isEmpty()){
                            println("no vmeter for label fuel > "+window.cap);
                        }else{
                            String lblText =  "";

                            // fuel units: branches, coal, etc
                            if (mapWincapMaxfuel.keySet().contains(window.cap)){
                                Map<String,Integer> mapWincapMaxfuel = new HashMap<>();
                                int currentFuel=0, maxFuel = ZeeManagerGobs.mapWincapMaxfuel.get(window.cap);
                                for (VMeter vm : vmeter) {
                                    LayerMeter.Meter meter = vm.meters.get(0);
                                    currentFuel = (int) Math.round(meter.a * maxFuel);
                                    lblText += currentFuel + "/" + maxFuel;
                                }
                                Color color = ZeeGobColor.COLOR_DARK_GREEN;
                                if (currentFuel == 0)
                                    color = Color.red;
                                else if (currentFuel < maxFuel)
                                    color = Color.orange;
                                ZeeConfig.addGobText(gobAutoLabel, lblText,color);
                            }
                            // perc% water, swill, etc
                            else {
                                Color c = ZeeGobColor.COLOR_DARK_GREEN;
                                double lowestVmeter = -1;
                                for (VMeter vm : vmeter) {
                                    LayerMeter.Meter meter = vm.meters.get(0);
                                    double twoDecimals = new BigDecimal(meter.a).setScale(2, RoundingMode.HALF_UP).doubleValue();
                                    if(lowestVmeter == -1  ||  twoDecimals < lowestVmeter)
                                        lowestVmeter = twoDecimals;
                                    lblText += twoDecimals + "   ";
                                }
                                if (lowestVmeter < .3)
                                    c = Color.red;
                                else if (lowestVmeter < .8)
                                    c = Color.yellow;
                                lblText = lblText.strip().replaceAll("0\\.",".");
                                ZeeConfig.addGobText(gobAutoLabel, lblText, c);
                            }
                        }
                        return;
                    }

                    // label container substance name and quantity
                    window.children().forEach(w1 -> {
                        if (w1.getClass().getSimpleName().contentEquals("RelCont")){
                            w1.children().forEach(w2 -> {
                                if (w2.getClass().getSimpleName().contentEquals("TipLabel")){

                                    try {
                                        // get window info
                                        List<ItemInfo> info = (List<ItemInfo>) w2.getClass().getDeclaredField("info").get(w2);

                                        // get name
                                        String name = ZeeManagerItems.getItemInfoName(info);
                                        if(name==null || name.isBlank()) {
                                            // empty gob, remove text
                                            ZeeConfig.removeGobText(ZeeConfig.lastMapViewClickGob);
                                            return;
                                        }

                                        // (15.45) (l) of (Cave Slime)
                                        Pattern pattern = Pattern.compile("(\\d+\\.?+\\d*)\\s+(\\S+) of ([\\S\\s]+)$");//.compile("(.*?)(\\d+)(.*)");
                                        Matcher matcher = pattern.matcher(name);
                                        matcher.find();
                                        String quantity = String.format("%.0f",Math.rint(Double.parseDouble(matcher.group(1))));
                                        String metric = matcher.group(2);
                                        String substance = matcher.group(3).replaceAll("\\s+","");

                                        String gobText = quantity + metric + " " + substance;

                                        // get quality
                                        Double ql = ZeeManagerItems.getItemInfoQuality(info);
                                        if(ql > 0) {
                                            gobText += ql.intValue();
                                        }

                                        // label gob "name q"
                                        // TODO create variable lastClickedBarrelCIstern to avoid rare mislabeling
                                        ZeeConfig.addGobText(gobAutoLabel, gobText);

                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchFieldException e) {
                                        e.printStackTrace();
                                    }
                                    return;
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    static void clickedMinimapGobicon(Gob gob, int btn) {
        try {
            if (ZeeConfig.clickIconStoatAggro && btn==3 && gob.getres().name.contains("/stoat")) {
                if(!ZeeConfig.isPlayerMountingHorse())
                    return;
                ZeeConfig.cursorChange(ZeeConfig.ACT_AGGRO);
                gobClick(gob, 1);
                ZeeConfig.clickRemoveCursor();
                ZeeConfig.setPlayerSpeed(ZeeConfig.PLAYER_SPEED_SPRINT);
                ZeeAudio.textToSpeakLinuxFestival("get");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void remountHorse() {
        ZeeManagerGobs.remountClosestHorse = false;
        new ZeeThread(){
            public void run() {
                int countNotReady = 0;
                double backupAudio = Audio.volume;
                ZeeConfig.addPlayerText("mounting");
                try {

                    // wait horse spawn a few seconds
                    Gob closestHorse = null;
                    int timeoutMs = 5000;
                    while(closestHorse==null && timeoutMs > 0) {
                        sleep(PING_MS);
                        timeoutMs -= PING_MS;
                        closestHorse = ZeeConfig.getClosestGobToPlayer(ZeeConfig.findGobsByNameEndsWith("/mare", "/stallion"));
                    }

                    //mute volume (msg method doesnt work)
                    Audio.setvolume(0);

                    //mount horse
                    ZeeManagerGobs.clickGobPetal(closestHorse, "Giddyup!");
                    countNotReady = 0;//exit success?

                    // wait player mounting pose
                    waitPlayerPose(ZeeConfig.POSE_PLAYER_RIDING_IDLE);
                    sleep(500);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //restore volume (msg method doesnt work)
                Audio.setvolume(backupAudio);
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    static String gobVisNames;
    static boolean gobVisHitbox, gobVisHidden;
    static void windowGobHitboxAndVisibility() {
        Widget wdg;
        String title = "Gob visibility";

        Window win = ZeeConfig.getWindow(title);
        if (win != null){
            win.reqdestroy();
            win = null;
        }

        //create window
        win = ZeeConfig.gameUI.add(
                new Window(Coord.of(120,70),title){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("close")){
                            this.reqdestroy();
                        }
                    }
                },
                300,300
        );

        //label
        wdg = win.add(new Label("gob name contains (space sep.)"));

        //text entry
        wdg = win.add(new TextEntry(UI.scale(130),""){
            public void activate(String text) {
                // update barterstand labels
                barterSearchUpdateGobs();
            }
            public boolean keyup(KeyUpEvent ev) {
                gobVisNames = this.text();
                return super.keyup(ev);
            }
        },0,wdg.c.y+wdg.sz.y);

        //checkbox hitbox
        wdg = win.add(new CheckBox("hitbox"){
            public void changed(boolean val) {
                gobVisHitbox = val;
                //barterSearchUpdateGobs();
            }
        },0,wdg.c.y+wdg.sz.y+5);

        //checkbox ore
        wdg = win.add(new CheckBox("hidden"){
            public void changed(boolean val) {
                gobVisHidden = val;
                //barterSearchUpdateGobs();
            }
        },wdg.c.x+wdg.sz.x+5,wdg.c.y);

        win.pack();
    }

    static boolean autoPickIrrlight = false, alreadyPickingIrrlight = false;
    public static void autoPickIrrlight() {
        if (alreadyPickingIrrlight) {
            // avoid being called multiple times by gob consumer
            //println("already picking irrlight");
            return;
        }
        alreadyPickingIrrlight = true;
        new ZeeThread(){
            public void run() {
                try {
                    // guess working station
                    Gob workingStation = ZeeConfig.getClosestGobToPlayer(ZeeConfig.findGobsByNameEndsWith("/crucible","/anvil"));

                    // set max speed
                    ZeeConfig.setPlayerSpeed(ZeeConfig.PLAYER_SPEED_SPRINT);

                    // try picking irrlight
                    ZeeConfig.addPlayerText("irrlight!");
                    prepareCancelClick();
                    while (alreadyPickingIrrlight && !isCancelClick()) {
                        Gob irrlight = ZeeConfig.getClosestGobByNameContains("/irrbloss");
                        if (irrlight==null) {
                            break;
                        }
                        gobClick(irrlight,3);
                        waitPlayerIdlePose();
                    }

                    //try crafting again
                    if (workingStation!=null && !ZeeConfig.isCancelClick()){
                        gobClick(workingStation,3);
                        waitPlayerIdlePose();
                        sleep(PING_MS);
                        if(ZeeConfig.clickedCraftAll) {
                            ZeeConfig.getButtonNamed((Window) ZeeConfig.makeWindow.parent, "Craft All").click();
                        }
                        //drink after running
                        ZeeManagerItems.drinkFromBeltHandsInv();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                alreadyPickingIrrlight = false;
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    public static void autoPickIrrlightExit() {
        autoPickIrrlight = false;
        alreadyPickingIrrlight = false;
    }


    public static boolean isGobSmoking(Gob gob){
        return getOverlayNames(gob).contains("gfx/fx/ismoke");
    }

    public static boolean isGobSmokeProducer(String gobName) {
        return List.of(
            "gfx/terobjs/kiln",
            "gfx/terobjs/tarkiln",
            "gfx/terobjs/oven",
            "gfx/terobjs/smokeshed",
            "gfx/terobjs/steelcrucible",
            "gfx/terobjs/smelter",
            "gfx/terobjs/furnace",
            "gfx/terobjs/primsmelter",
            "gfx/terobjs/minepyre",
            "gfx/terobjs/bpyre",
            "gfx/terobjs/villageidol"
        ).contains(gobName);
    }

    public static boolean isGobIdol(String gobName) {
        return gobName.contentEquals("gfx/terobjs/pclaim")
                || gobName.contentEquals("gfx/terobjs/villageidol");
    }


    private static List<Gob> listQueuedChipStone = null;
    private static ZeeThread threadChipStone = null;
    static void queueChipStone(Gob boulder) {

        if (boulder==null){
            println("queueChipStone > boulder null");
            return;
        }
        if (listQueuedChipStone==null) {
            listQueuedChipStone = new ArrayList<>();
        }
        // add or remove boulder
        else if (listQueuedChipStone.contains(boulder)){
            listQueuedChipStone.remove(boulder);
            ZeeConfig.removeGobText(boulder);
            queueChipStoneUpdLabels();
            return;
        }
        listQueuedChipStone.add(boulder);
        queueChipStoneUpdLabels();

        if(threadChipStone==null) {
            threadChipStone = new ZeeThread() {
                public void run() {
                    try {
                        ZeeConfig.addPlayerText("queue " + listQueuedChipStone.size());
                        prepareCancelClick();
                        //only cancel click by button 1
                        while (!isCancelClick()) {
                            sleep(1000);
                            if (isCancelClick()) {
                                println("queueChipStone > cancel click");
                                break;
                            }
                            if (ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_DRINK, ZeeConfig.POSE_PLAYER_CHIPPINGSTONE,ZeeConfig.POSE_PLAYER_PICK)) {
                                continue;
                            }
                            if (ZeeConfig.isPlayerMovingByAttrLinMove()){
                                continue;
                            }
                            if (listQueuedChipStone.isEmpty()) {
                                println("queueChipStone > empty list");
                                break;
                            }
                            Gob nextBoulder = listQueuedChipStone.remove(0);
                            if (nextBoulder == null) {
                                println("queueChipStone > next boulder null");
                                break;
                            }

                            //update labels
                            ZeeConfig.removeGobText(nextBoulder);
                            queueChipStoneUpdLabels();

                            if (ZeeConfig.isPlayerHoldingItem()){
                                ZeeManagerItems.getHoldingItem().item.wdgmsg("drop", Coord.z);
                                waitNotHoldingItem();
                            }
                            clickGobPetal(nextBoulder,"Chip stone");
                            prepareCancelClick();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ZeeConfig.removePlayerText();
                    chipStoneReset();
                }
            };
            if (threadChipStone!=null)
                threadChipStone.start();
        }
    }
    static void chipStoneReset(){
        if (listQueuedChipStone!=null && !listQueuedChipStone.isEmpty()){
            ZeeConfig.removeGobText((ArrayList<Gob>) listQueuedChipStone);
            //listQueuedChipStone.clear();
        }
        listQueuedChipStone = null;
        threadChipStone = null;
    }
    static void queueChipStoneUpdLabels(){
        if(listQueuedChipStone!=null && !listQueuedChipStone.isEmpty()) {
            for (int i = 0; i < listQueuedChipStone.size(); i++) {
                ZeeConfig.addGobText(listQueuedChipStone.get(i), "" + (i+1));
            }
            ZeeConfig.addPlayerText("queue " + listQueuedChipStone.size());
        }
    }
    static boolean queueChipStoneIsActive(){
        return listQueuedChipStone!=null && !listQueuedChipStone.isEmpty();
    }


    static void queueChopTreeBush(Gob tree) {
        if (tree==null){
            println("queueChopTree > tree null");
            return;
        }
        if (ZeeManagerTrees.listQueuedTreeChop==null) {
            ZeeManagerTrees.listQueuedTreeChop = new ArrayList<>();
        }
        // add or remove tree
        else if (ZeeManagerTrees.listQueuedTreeChop.contains(tree)){
            ZeeManagerTrees.listQueuedTreeChop.remove(tree);
            ZeeConfig.removeGobText(tree);
            ZeeManagerTrees.queueChopTreeUpdLabels();
            return;
        }
        ZeeManagerTrees.listQueuedTreeChop.add(tree);
        ZeeManagerTrees.queueChopTreeUpdLabels();


        if(ZeeManagerTrees.threadChopTree==null) {
            ZeeManagerTrees.threadChopTree = new ZeeThread() {
                public void run() {
                    println("chop thread start");
                    try {
                        ZeeConfig.addPlayerText("queue " + ZeeManagerTrees.listQueuedTreeChop.size());
                        prepareCancelClick();
                        //only cancel click by button 1
                        while (!isCancelClick()) {
                            sleep(1000);
                            if (isCancelClick()) {
                                println("queueChopTree > cancel click");
                                break;
                            }
                            if (ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_DRINK, ZeeConfig.POSE_PLAYER_CHOPTREE)) {
                                continue;
                            }
                            if (ZeeConfig.isPlayerMovingByAttrLinMove()){
                                continue;
                            }
                            if (ZeeManagerTrees.listQueuedTreeChop.isEmpty()) {
                                println("queueChopTree > empty list");
                                break;
                            }
                            Gob nexTree = ZeeManagerTrees.listQueuedTreeChop.remove(0);
                            if (nexTree == null) {
                                println("queueChopTree > next tree null");
                                break;
                            }

                            //update labels
                            ZeeConfig.removeGobText(nexTree);
                            ZeeManagerTrees.queueChopTreeUpdLabels();

                            clickGobPetal(nexTree,"Chop");
                            prepareCancelClick();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ZeeConfig.removePlayerText();
                    println("chop thread end");
                    ZeeManagerTrees.queueChopTreeReset();
                }
            };
            if (ZeeManagerTrees.threadChopTree!=null)
                ZeeManagerTrees.threadChopTree.start();
        }
    }


    private static List<Gob> listQueuedButch = null;
    static void queueButchAnimal(Gob animal) {
        if (animal==null){
            println("listQueuedButch > animal null");
            return;
        }
        if (listQueuedButch==null) {
            listQueuedButch = new ArrayList<>();
        }
        // add or remove animal
        else if (listQueuedButch.contains(animal)){
            listQueuedButch.remove(animal);
            ZeeConfig.removeGobText(animal);
            queueButchUpdLabels();
            return;
        }
        listQueuedButch.add(animal);
        queueButchUpdLabels();
    }
    static void queueButchNext(){
        if (listQueuedButch!=null){
            if (isCancelClick()){
                println("queueButchNext > cancel click");
                queueButchReset();
                return;
            }
            if (!listQueuedButch.isEmpty()){
                Gob nextButch = listQueuedButch.remove(0);
                println("queueButchNext > "+listQueuedButch.size()+" to go");
                autoButchBigDeadAnimal(nextButch);
                return;
            }else{
                println("queueButchNext > done");
                queueButchReset();
            }
        }else{
            println("queueButchNext > list null");
        }
    }
    static void queueButchReset(){
        if (listQueuedButch!=null && !listQueuedButch.isEmpty()){
            ZeeConfig.removeGobText((ArrayList<Gob>) listQueuedButch);
        }
        listQueuedButch = null;
    }
    static void queueButchUpdLabels(){
        if(listQueuedButch!=null && !listQueuedButch.isEmpty()) {
            for (int i = 0; i < listQueuedButch.size(); i++) {
                ZeeConfig.addGobText(listQueuedButch.get(i), "" + (i+1));
            }
            ZeeConfig.addPlayerText("queue " + listQueuedButch.size());
        }
    }






    static List<Gob> listQueuedActTargets = null;
    static boolean queueActBusy=false , isQueueActAllGobs=false;
    static void queueMenuGridAct(Gob target) {

        if (target==null){
            println("queueMenuGridAct > target null");
            return;
        }

        // keep adding closest gob to queue
        isQueueActAllGobs = ZeeConfig.gameUI.ui.modshift;

        // init queue
        if (listQueuedActTargets ==null) {
            listQueuedActTargets = new ArrayList<>();
        }
        // remove from queue and return
        else if (listQueuedActTargets.contains(target)){
            listQueuedActTargets.remove(target);
            ZeeConfig.removeGobText(target);
            queueActUpdLabels();
            return;
        }
        // add clicked gob to queue
        listQueuedActTargets.add(target);

        queueActUpdLabels();

        // start thread once
        if (!queueActBusy){
            queueActBusy = true;
            new ZeeThread(){
                public void run() {
                    println("queueMenuGridAct > thread start");
                    try {
                        if (listQueuedActTargets !=null){
                            if (isCancelClick()){
                                println("queueMenuGridAct > cancel click");
                                queueMenuGridActReset();
                                return;
                            }
                            prepareCancelClick();
                            waitPlayerNonActivePose();//wait first work
                            while (!isCancelClick() && !listQueuedActTargets.isEmpty()){
                                // select gob to act
                                Gob curGob = listQueuedActTargets.remove(0);
                                ZeeConfig.removeGobText(curGob);
                                // define next if act on all gobs
                                if (isQueueActAllGobs){
                                    Gob nextGob = ZeeConfig.getClosestGob( curGob, ZeeConfig.findGobsByNameEquals(curGob.getres().name));
                                    if (nextGob!=null)
                                        listQueuedActTargets.add(nextGob);
                                }
                                queueActUpdLabels();
                                //change cursor
                                ZeeConfig.gameUI.menu.wdgmsg("act", lastMenuGridActArgs);
                                sleep(555);
                                //click next gob
                                gobClick(curGob,1);
                                //wait approach gob
                                if(!waitPlayerIdleLinMove() || isCancelClick()) {
                                    println("couldnt wait player idle linmove (or cancel click)");
                                    break;
                                }
                                //wait player work done
                                if(!waitPlayerNonActivePose() || isCancelClick()) {
                                    println("couldnt wait non active pose (or cancel click)");
                                    break;
                                }
                            }

                            println("queueMenuGridAct > done");
                            queueMenuGridActReset();

                        }else{
                            println("queueMenuGridAct > list null");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
    static void queueMenuGridActReset(){
        if (listQueuedActTargets !=null && !listQueuedActTargets.isEmpty()){
            ZeeConfig.removeGobText((ArrayList<Gob>) listQueuedActTargets);
        }
        ZeeConfig.removePlayerText();
        lastMenuGridActArgs = null;
        listQueuedActTargets = null;
        queueActBusy = false;
        isQueueActAllGobs = false;
    }
    static void queueActUpdLabels(){
        if (lastMenuGridActArgs==null || lastMenuGridActArgs.length==0) {
            println("queueActUpdLabels > no args, reseting maybe");
            queueMenuGridActReset();
            return;
        }
        if(listQueuedActTargets!=null && !listQueuedActTargets.isEmpty()) {
            // label player
            String lbl = isQueueActAllGobs ? "all" : String.valueOf(listQueuedActTargets.size()+1);
            ZeeConfig.addPlayerText(lastMenuGridActArgs[0]+ " " + lbl);
            // label queue gobs
            for (int i = 0; i < listQueuedActTargets.size(); i++) {
                ZeeConfig.addGobText(listQueuedActTargets.get(i), "queue" + (i+1));
            }
        }
    }


    public static void checkSmoke(Gob gob, String i) {
        if (ZeeConfig.hideFxSmoke){
            gob.smokeHighlight = true;
            //highlight already loaded gob (ugly)
            if (gob.settingsApplied){
                highlightGobSmoking(gob);
            }
        }else{
            gob.smokeHighlight = false;
        }
    }
    public static void highlightGobSmoking(Gob gob){
        if (gob.getres().name.endsWith("villageidol"))
            return;
        ZeeConfig.addGobColor(gob,ZeeGobColor.COLOR_DARK_GREEN);
    }

    public static String getBoulderNameNoSize(String name) {
        return name.replaceAll("\\d$", "");
    }

    public static void highlightCropsReady(Gob g) {
        String name = g.getres().name;
        if ((isGobCrop(name) || isGobTrellisPlant(name)) && ZeeManagerFarmer.isCropMaxStage(g))
            g.setattr(new ZeeGobColor(g, Color.CYAN));
    }

    static void hideModel(Gob gob) {
        Drawable d = gob.getattr(Drawable.class);
        if (d != null && d.slots != null) {
            ArrayList<RenderTree.Slot> tmpSlots = new ArrayList<>(d.slots);
            ZeeConfig.gameUI.ui.sess.glob.loader.defer(() -> {
                try{
                    RUtils.multirem(tmpSlots);
                } catch (Defer.NotDoneException | RenderTree.SlotRemoved ignored) {
                } catch (Exception e){
                    ZeeConfig.println("toggleModel hide > "+e.getClass().getName()+" , "+e.getMessage());
                }
            }, null);
        }
        //always show hitbox when hiding model
        gob.showHitBox();
    }

    static void showModel(Gob gob){
        Drawable d = gob.getattr(Drawable.class);
        ArrayList<RenderTree.Slot> tmpSlots = new ArrayList<>(gob.slots);
        ZeeConfig.gameUI.ui.sess.glob.loader.defer(() -> {
            try{
                RUtils.multiadd(tmpSlots,d);
            }
            catch (Defer.NotDoneException  | RenderTree.SlotRemoved ignored) {	}
            catch (Exception e){
                ZeeConfig.println("toggleModel show > "+e.getClass().getName()+" , "+e.getMessage());
            }
        }, null);
        // hide hitbox if setting permits
        if (!ZeeConfig.showHitbox)
            gob.hideHitBox();
    }

    private boolean isGobBigDeadAnimal_thread() {
        try{
            ZeeThread zt = new ZeeThread() {
                public void run() {
                    gobClick(gob, 3);
                    FlowerMenu fm = waitFlowerMenu();
                    if (fm==null) {//no menu detected
                        isGobDeadAnimal = false;
                        return;
                    }
                    for (int i = 0; i < fm.opts.length; i++) {
                        //if animal gob has butch menu, means is dead
                        if (ZeeConfig.DEF_LIST_BUTCH_AUTO.contains(fm.opts[i].name)){
                            isGobDeadAnimal = true;
                            break;
                        }
                    }
                    //close menu before returning
                    ZeeFlowerMenu.cancelFlowerMenu();
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

    private static void mountHorseDrivingWheelbarrow(Gob gob){
        Gob horse = gob;
        try{
            //waitNoFlowerMenu();
            ZeeConfig.addPlayerText("mounting");
            Gob wb = ZeeConfig.getClosestGobByNameContains("gfx/terobjs/vehicle/wheelbarrow");
            if (wb == null) {
                ZeeConfig.msg("no wheelbarrow close 1");
            } else {
                Coord pc = ZeeConfig.getPlayerCoord();
                Coord subc = ZeeConfig.getGobCoord(horse).sub(pc);
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
            Gob wb = ZeeConfig.getClosestGobByNameContains("gfx/terobjs/vehicle/wheelbarrow");
            if (wb == null) {
                ZeeConfig.msg("no wheelbarrow close 2");
            } else {
                Coord pc = ZeeConfig.getPlayerCoord();
                Coord subc = ZeeConfig.getGobCoord(horse).sub(pc);
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

    private static void liftGobAndClickTarget(Gob liftGob, Gob target){
        try {
            waitNoFlowerMenu();
            ZeeConfig.addPlayerText("lift and click");
            double dist;
            //remove hand cursor
            ZeeConfig.clickRemoveCursor();
            liftGob(liftGob);
            dist = ZeeConfig.distanceToPlayer(liftGob);
            if (dist==0) {
                // click target
                gobClick(target, 3);
                //waitPlayerIdleVelocity();
            }else{
                ZeeConfig.msg("couldnt lift gob?");//impossible case?
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
            Gob wb = ZeeConfig.getClosestGobByNameContains("gfx/terobjs/vehicle/wheelbarrow");
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
        return ZeeManagerGobs.getOverlayNames(barrel).isEmpty();
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
                if (ZeeConfig.isCancelClick()) {
                    // cancel if clicked right/left button
                    println("cancel click");
                    break;
                }
                ZeeConfig.addGobText(closestPlant,"plant");
                destroyGob(closestPlant);
                if(!waitGobRemovedOrCancelClick(closestPlant))
                    break;
                closestPlant = ZeeConfig.getClosestGobToPlayer(ZeeConfig.findGobsByNameContains(gobName));
                dist = ZeeConfig.distanceToPlayer(closestPlant);
                //println("dist "+dist);
            }while(dist < 25);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
        ZeeConfig.removeGobText(closestPlant);
    }

    public static void addItemsToGob(List<WItem> invItens, int num, Gob gob){
        new ZeeThread(){
            public void run() {
                try{
                    if(invItens.size() < num){
                        ZeeConfig.msgError("Need "+num+" item(s)");
                        return;
                    }
                    boolean exit = false;
                    int added = 0;
                    prepareCancelClick();
                    ZeeConfig.addPlayerText("adding");
                    while(  !ZeeConfig.isCancelClick()
                            && !exit
                            && added < num
                            && invItens.size() > 0)
                    {
                        if(ZeeManagerItems.pickUpItem(invItens.get(0))){
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
                    gobClick(gob,3);//label gob
                }catch (Exception e){
                    e.printStackTrace();
                }
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    private static void harvestOneTrellisWithoutScythe(Gob gob) {
        //hold scythe for user unequip it
        if(ZeeManagerItems.pickupBeltItem("scythe")){

        }
        //hold scythe for user unequip it
        else if(ZeeManagerItems.getLeftHandName().endsWith("scythe")){
            ZeeManagerItems.unequipLeftItem();
        }
        //no scythe, just harvest
        // TODO test harvest while holding scythe in vhand
        else{
            clickGobPetal(gob,"Harvest");
        }
    }

    static boolean isAutoPressan = false;
    static void autoPressWine(Window window) {

        if (isAutoPressan) {
            println("already pressan");
            return;
        }
        isAutoPressan = true;

        new Thread(){

            public void run() {

                try {
                    //  "/grapes"  "seed-grape"
                    ZeeConfig.addPlayerText("pressan");
                    Button btnPress = ZeeConfig.getButtonNamed(window,"Press");
                    Inventory invPress = window.getchild(Inventory.class);
                    Inventory invPlayer = ZeeConfig.getMainInventory();
                    List<WItem> playerGrapes = invPlayer.getWItemsByNameEndsWith("/grapes");
                    List<WItem> pressGrapes = invPress.getWItemsByNameEndsWith("/grapes");
                    List<WItem> pressSeeds = invPress.getWItemsByNameEndsWith("/seed-grape");

                    if (pressGrapes.size()==0){
                        if (playerGrapes.size() > 0) {
                            playerGrapes.get(0).item.wdgmsg("transfer", Coord.z, -1);
                        }else{
                            exitAutoWinepress("no grapes to start pressing");
                            return;
                        }
                    }

                    //while idle pressing pose
                    while(ZeeConfig.getPlayerPoses().contains(ZeeConfig.POSE_PLAYER_PRESSINGWINE_IDLE)){

                        //start pressing
                        println("pressing");
                        btnPress.click();
                        sleep(PING_MS);

                        //wait stop pressing
                        waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_PRESSINGWINE, ZeeConfig.POSE_PLAYER_DRINK);
                        sleep(PING_MS);

                        //exit if player left winepress
                        if (!ZeeConfig.getPlayerPoses().contains(ZeeConfig.POSE_PLAYER_PRESSINGWINE_IDLE)){
                            exitAutoWinepress("player left winepress");
                            return;
                        }

                        playerGrapes = invPlayer.getWItemsByNameEndsWith("/grapes");
                        pressGrapes = invPress.getWItemsByNameEndsWith("/grapes");
                        pressSeeds = invPress.getWItemsByNameEndsWith("/seed-grape");

                        if (pressGrapes.size() > 0){
                            exitAutoWinepress("press still has grapes, grapejuice full?");
                            return;
                        }

                        if(pressSeeds.size() > 0){
                            if (invPlayer.getNumberOfFreeSlots() == 0){
                                exitAutoWinepress("player inv full, cant switch winepress contents");
                                return;
                            }
                            println("press has only seeds, try refilling?");

                            //transfer seeds to player
                            pressSeeds.get(0).item.wdgmsg("transfer",Coord.z,-1);
                            sleep(PING_MS);

                            //transfer grapes to press
                            if (playerGrapes.size() == 0){
                                exitAutoWinepress("out of grapes to press");
                                return;
                            }
                            playerGrapes.get(0).item.wdgmsg("transfer",Coord.z,-1);

                            // restart pressing on next loop...
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                //exit
                exitAutoWinepress("ok");
            }

        }.start();

    }

    static void exitAutoWinepress(String msg) {
        println("exit autowine > "+msg);
        ZeeConfig.removePlayerText();
        isAutoPressan = false;
    }

    public static boolean isGobStockpile(String gobName) {
        return gobName.startsWith("gfx/terobjs/stockpile");
    }

    private static boolean isGobGroundItem(String gobName) {
        return gobName.startsWith("gfx/terobjs/items/");
    }

    public static boolean isLongMidClick() {
        return lastClickDiffMs >= LONG_CLICK_MS;
    }

    public static boolean isShortMidClick() {
        return lastClickDiffMs < LONG_CLICK_MS;
    }


    public static boolean isGobMineSupport(String gobName) {
        String list = "/minebeam,/column,/minesupport,/naturalminesupport,/towercap";
        return ZeeConfig.nameInListEndsWith(gobName, list);
    }


    private static boolean isGobLiftable(String gobName) {
        if(isGobBoulder(gobName) || isGobSittingFurniture(gobName) || gobName.contains("/table-"))
            return true;
        String endList = "/meatgrinder,/potterswheel,/iconsign,/rowboat,/dugout,/wheelbarrow,"
                +"/compostbin,/gardenpot,/beehive,/htable,/bed-sturdy,/boughbed,/alchemiststable,"
                +"/gemwheel,/ancestralshrine,/spark,/cauldron,/churn,/wardrobe,"
                +"/trough,curdingtub,/plow,/barrel,/still,log,/oldtrunk,chest,/anvil,"
                +"/cupboard,/studydesk,/demijohn,/quern,/wreckingball-fold,/loom,/swheel,"
                +"/ttub,/cheeserack,/archerytarget,/dreca,/glasspaneframe,/runestone,"
                +"woodbox,casket,basket,crate,chest,stonekist";
        return ZeeConfig.nameInListEndsWith(gobName,endList);
    }

    public static boolean isGobBoulder(String gobName) {
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
        return gobName.startsWith("gfx/terobjs/trees/")
                && !gobName.endsWith("log")
                && !gobName.endsWith("stump")
                && !gobName.endsWith("oldtrunk")
                && !gobName.startsWith("gfx/terobjs/trees/driftwood")
                && !gobName.contains("trees/yule"); // yulestar-spruce, yulelights-spruce
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
        ZeeManagerGobs.gobClick(gob, 3, UI.MOD_CTRL_SHIFT);
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

    public static boolean liftGob(Gob gob) {
        if(isGobBush(gob.getres().name)) {
            ZeeManagerItems.equipBeltOrInvItemThreadJoin("shovel");
            waitItemInHand("shovel");
        }
        ZeeConfig.gameUI.menu.wdgmsg("act", "carry","0");
        waitCursorName(ZeeConfig.CURSOR_HAND);
        gobClick(gob,1);
        if(ZeeConfig.treeloganize && !ZeeManagerTrees.treeloganizerWorking)
            ZeeManagerTrees.treeloganizerCheckLift();
        return waitPlayerPose(ZeeConfig.POSE_PLAYER_LIFTING);
    }

    static boolean isMidclickInspecting = false; // used by inspect tooltip feature
    public static void inspectGob(Gob gob){
        if (gob==null) {
            println("inspectGob > gob null");
            return;
        }
        isMidclickInspecting = true;
        new ZeeThread(){
            @Override
            public void run() {
                try {
                    ZeeConfig.gameUI.menu.wdgmsg("act","inspect","0");
                    sleep(50);
                    gobClick(gob, 1);
                    sleep(50);
                    ZeeConfig.clickRemoveCursor();
                    sleep(50);
                    ZeeConfig.gameUI.map.ttip = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isMidclickInspecting = false;
            }
        }.start();
    }

    public static boolean isGobTrellisPlant(String gobName) {
        return ZeeConfig.nameInListEndsWith(gobName, "plants/wine,plants/hops,plants/pepper,plants/peas,plants/cucumber");
    }

    public static boolean isGobCrop(String gobName){
        return ZeeConfig.nameInListEndsWith(gobName,
                "plants/carrot,plants/beet,plants/yellowonion,plants/redonion,plants/garlic,plants/greenkale,"
                +"plants/leek,plants/lettuce,plants/pipeweed,plants/hemp,plants/flax,"
                +"plants/turnip,plants/millet,plants/barley,plants/wheat,plants/poppy,"
                +"plants/pumpkin,plants/fallowplant,plants/champignon"
        );
    }

    static boolean isGobHarvestable(String gobName){
        return isGobCrop(gobName) || isGobTrellisPlant(gobName);
    }

    public static boolean isGobCraftingContainer(String gobName) {
        String containers ="cupboard,chest,crate,basket,box,coffer,cabinet";
        return ZeeConfig.nameInListEndsWith(gobName,containers);
    }


    private boolean isGobInListStartsWith(String gobName, String list) {
        String[] names = list.split(",");
        for (int i = 0; i < names.length; i++) {
            if (gobName.startsWith(names[i])){
                return true;
            }
        }
        return false;
    }

    static boolean clickGobPetal(Gob gob, int petalIndex) {
        if (gob==null){
            println(">clickGobPetal gob null");
            return false;
        }
        //make sure cursor is arrow before clicking gob
        if (!ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_ARW)){
            ZeeConfig.clickRemoveCursor();
            if (!waitCursorName(ZeeConfig.CURSOR_ARW)) {
                return false;
            }
        }
        //click gob
        gobClick(gob,3);
        //click petal
        FlowerMenu fm = waitFlowerMenu();
        if(fm!=null){
            choosePetal(fm, petalIndex);
            return waitNoFlowerMenu();
        }else{
            //println("clickGobPetal > no flower menu?");
            return false;
        }
    }

    static boolean clickGobPetal(Gob gob, String petalName) {
        if (gob==null){
            println(">clickGobPetal gob null");
            return false;
        }
        //make sure cursor is arrow before clicking gob
        if (!ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_ARW)){
            ZeeConfig.clickRemoveCursor();
            if (!waitCursorName(ZeeConfig.CURSOR_ARW)) {
                return false;
            }
        }
        //click gob
        gobClick(gob,3);
        //click petal
        FlowerMenu fm = waitFlowerMenu();
        if(fm!=null && fm.hasPetal(petalName)){
            choosePetal(fm, petalName);
            return waitNoFlowerMenu();
        }else{
            println("clickGobPetal > no flower menu?");
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
            waitCursorName(ZeeConfig.CURSOR_ARW);
        }

        //disable auto options before clicking gob
        boolean butchBackup = ZeeConfig.butcherMode;
        ZeeConfig.butcherMode = false;
        ZeeConfig.autoClickMenuOption = false;

        //click gob and wait menu
        gobClick(gob, 3);
        FlowerMenu fm = waitFlowerMenu();
        if (fm!=null) {
            // menu opened means gob exist
            ZeeFlowerMenu.cancelFlowerMenu();
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

    static boolean isGobTamedAnimalOrAurochEtc(String gobName){
        return ZeeConfig.nameInListEndsWith(
                gobName,
                "/stallion,/mare,/foal,/hog,/sow,/piglet,/billy,/nanny,/kid,/sheep,/lamb,/cattle,/calf,/teimdeercow,/teimdeerbull,/teimdeerkid"
        );
    }

    static boolean isGobButchable(String gobName){
        return ZeeConfig.nameInListEndsWith(
            gobName,
            "/stallion,/mare,/foal,/hog,/sow,/piglet,/teimdeercow,/teimdeerbull,/teimdeerkid,"
            +"/billy,/nanny,/kid,/sheep,/lamb,/cattle,/calf,"
            +"/horse,/aurochs,/mouflon,/wildgoat,"
            +"/adder,/badger,/bear,/boar,/beaver,/fox,"
            +"/reindeer,/reddeer,/roedeer,"
            +"/greyseal,/otter,/garefowl,"
            +"/lynx,/mammoth,/moose,/troll,/walrus,/wolf,/wolverine,"
            +"/caveangler,/boreworm,/caverat,/cavelouse"
        );
    }

    static boolean isGobHorse(String gobName) {
        return ZeeConfig.nameInListEndsWith(gobName, "stallion,mare,horse");
    }

    static boolean isGobFireSource(Gob gob) {
        String gobName = gob.getres().name;
        if ( ZeeConfig.nameInListEndsWith(gobName,"/brazier,/snowlantern,/pow,/bonfire") )
            if (getOverlayNames(gob).contains("gfx/fx/flight"))
                return true;
        return false;
    }

    static boolean isGobFireTarget(Gob gob) {
        String gobName = gob.getres().name;
        if ( ZeeConfig.nameInListEndsWith(gobName,"/brazier,/snowlantern,/pow,/bonfire,/bpyre") )
            if (!getOverlayNames(gob).contains("gfx/fx/flight"))
                return true;
        return false;
    }

    /**
     * Itemact with gob, to fill trough with item in hand for example
     * @param mod 1 = shift, 2 = ctrl, 4 = alt  (3 = ctrl+shift ?)
     */
    public static void itemActGob(Gob g, int mod) {
        ZeeConfig.gameUI.map.wdgmsg("itemact", Coord.z, g.rc.floor(OCache.posres), mod, 0, (int) g.id, g.rc.floor(OCache.posres), 0, -1);
    }

    public static void gobClickOverlay(Gob g, int btn, int mod, int overlayId) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), g.rc.floor(OCache.posres), btn, mod, 1, (int)g.id, g.rc.floor(OCache.posres), overlayId, 1);
    }

    public static void gobClick(Gob g, int btn, int mod, int x) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), g.rc.floor(OCache.posres), btn, mod, 0, (int)g.id, g.rc.floor(OCache.posres), 0, x);
    }

    public static void gobClick(Gob g, int btn) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), g.rc.floor(OCache.posres), btn, 0, 0, (int)g.id, g.rc.floor(OCache.posres), 0, -1);
    }

    static void gobClick(Gob g, int btn, int mod) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), g.rc.floor(OCache.posres), btn, mod, 0, (int)g.id, g.rc.floor(OCache.posres), 0, -1);
    }

    static void gobPlace(Gob g, int mod) {
        ZeeConfig.gameUI.map.wdgmsg("place", g.rc.floor(posres), (int) Math.round(g.a * 32768 / Math.PI), 1, mod);
    }

    static void gobPlace(Gob g, Coord c, int mod) {
        ZeeConfig.gameUI.map.wdgmsg("place", c, (int) Math.round(g.a * 32768 / Math.PI), 1, mod);
    }

    static double distanceCoordGob(Coord2d c, Gob gob) {
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
            if(ol.spr.res != null)
                ret.add(ol.spr.res.name);
        }
        return ret;
    }

    static List<Gob.Overlay> getGobOverlaysByName(Gob gob, String name){
        List<Gob.Overlay> ret = new ArrayList<>();
        for (Gob.Overlay ol : gob.ols) {
            if(ol.spr.res != null  &&  ol.spr.res.name.contentEquals(name))
                ret.add(ol);
        }
        return ret;
    }

    public static List<String> getGAttrNames(Gob gob) {
        List<String> ret = new ArrayList<>();
        Collection<GAttrib> attrs = gob.attr.values();
        for (GAttrib attr : attrs) {
            ret.add(attr.getClass().getSimpleName());
        }
        return ret;
    }

    public static GAttrib getGAttrByClassSimpleName(Gob gob, String attrSimpleName) {
        Collection<GAttrib> attrs = gob.attr.values();
        for (GAttrib attr : attrs) {
            if(attr.getClass().getSimpleName().contentEquals(attrSimpleName))
                return attr;
        }
        return null;
    }

    public static Gob getGobFromClickable(Clickable ci) {
        if(ci instanceof Gob.GobClick) {
            return ((Gob.GobClick) ci).gob;
        } else if(ci instanceof Composited.CompositeClick) {
            Gob.GobClick gi = ((Composited.CompositeClick) ci).gi;
            return gi != null ? gi.gob : null;
        }
        return null;
    }

    static void pickupAllGobItemsServerSide(Gob gobItem) {
        gobClick(gobItem,3,UI.MOD_SHIFT);
        ZeeManagerGobs.waitKritDistThenDismountHorseAndPickup(gobItem,true);
    }

    static boolean isPickingAllGobsClientSide = false;
    static void pickupAllGobsClientSide(String resname){
        if (isPickingAllGobsClientSide)
            return;
        new ZeeThread(){
            public void run() {
                int cont = 0;
                isPickingAllGobsClientSide = true;
                String basename = ZeeConfig.getResBasename(resname);
                if (basename.isBlank())
                    return;
                try {
                    do{
                        ZeeConfig.addPlayerText(basename+" "+cont);
                        Gob closest = ZeeConfig.getClosestGobByNameEnds(resname);
                        if (closest == null)
                            break;
                        // pick all serverside until player idle
                        ZeeManagerGobs.pickupAllGobItemsServerSide(closest);
                        prepareCancelClick();
                        waitPlayerIdlePose();
                        cont++;
                    }while(!isCancelClick() && !ZeeConfig.isPlayerHoldingItem());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                isPickingAllGobsClientSide = false;
                ZeeConfig.removePlayerText();
                ZeeConfig.msgLow(basename+" "+cont);
            }
        }.start();
    }

    // pattern must match whole gob name
    static boolean pickupGobShift, pickupGobCtrl;
    static boolean isPickingUpClosestGob = false;
    public static void pickupClosestGob(boolean shift, boolean ctrl) {
        if (isPickingUpClosestGob)
            return;
        isPickingUpClosestGob = true;
        pickupGobShift = shift;
        pickupGobCtrl = ctrl;

        try {

            // find eligible gobs
            List<Gob> gobs = findPickupGobs();
            if (gobs == null || gobs.size() == 0) {
                isPickingUpClosestGob = false;
                return;
            }

            // calculate closest gob
            double minDist = 99999, dist;
            Gob closestGob = null;
            String name;
            for (int i = 0; i < gobs.size(); i++) {
                Gob g = gobs.get(i);
                dist = ZeeConfig.distanceToPlayer(g);
                name = g.getres().name;
                if (closestGob == null) {
                    minDist = dist;
                    closestGob = g;
                } else if ((g.pickupPriority = ((ZeeConfig.isBug(name) || name.contains("/kritter/")) && dist < 88)) || dist < minDist) {
                    // prev closest gob had priority
                    if (closestGob.pickupPriority && !g.pickupPriority) {
                        continue;
                    }
                    minDist = dist;
                    closestGob = g;
                }
            }

            if (closestGob != null) {
                // pick all clientside
                if (pickupGobCtrl && pickupGobShift) {
                    pickupAllGobsClientSide(closestGob.getres().name);
                }
                // pick all serverside
                else if (pickupGobShift) {
                    pickupAllGobItemsServerSide(closestGob);
                }
                // pick one item
                else{
                    pickupGobItem(closestGob);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        isPickingUpClosestGob = false;
    }

    static void pickupGobItem(Gob gobItem) {
        if ( ! gobItemHasPickMenu(gobItem) ) {
            gobClick(gobItem, 3);
            ZeeManagerGobs.waitKritDistThenDismountHorseAndPickup(gobItem,false);
        } else {
            new ZeeThread() {
                public void run() {
                    clickGobPetal(gobItem, "Pick");
                }
            }.start();
        }
    }

    static boolean gobItemHasPickMenu(Gob gobItem) {
        if ( ZeeConfig.isBug(gobItem.getres().name)
                || gobItem.getres().name.contains("/kritter/")
                || gobItem.getres().name.contains("/terobjs/items/") )
        {
            return false;
        }
        return true;
    }

    private static List<Gob> findPickupGobs() {
        List<Gob> gobs = ZeeConfig.getAllGobs();
        gobs.removeIf(gob1 ->{
            String name = gob1.getres().name;
            //dont remove items, herbs, bugs
            if (name.contains("/items/") || name.contains("/herbs/") || ZeeConfig.isBug(name))
                return false;
            // kritters
            if ( name.contains("/kritter/")) {
                // remove non pickable kritter
                if ( ZeeConfig.isKritterNotPickable(gob1) )
                    return true;
                else
                    return false;
            }
            //remove leafpile
            if ( name.contentEquals("gfx/terobjs/herbs/leafpile") )
                return true;
            //remove all else
            return true;
        });
        return gobs;
    }

    static ZeeWindow winPickupGob;
    static void showWindowPickupGob() {

        // find eligible gobs
        List<Gob> gobs = findPickupGobs();

        Widget wdg = null;
        List<String> listNamesAdded = new ArrayList<>();

        // toggle window off
        if (winPickupGob!=null){
            winPickupGob.reqdestroy();
            winPickupGob = null;
            listNamesAdded.clear();
        }

        //create window
        winPickupGob = new ZeeWindow(Coord.of(230,300),ZeeFlowerMenu.STRPETAL_PICKUP_GOBS);

        //button refresh
        wdg = winPickupGob.add(new ZeeWindow.ZeeButton(60,"refresh"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    showWindowPickupGob();
                }
            }
        }, 0, 0);
        wdg = winPickupGob.add(new Label("(ctrl+q)"), wdg.c.x + wdg.sz.x + 5 , wdg.c.y + 5);

        //scroll port
        int y = wdg.c.y + wdg.sz.y + 15;
        Scrollport scrollport = winPickupGob.add(new Scrollport(new Coord(150, 200)), 0, y);


        // add window, exit if no gobs
        winPickupGob.pack();
        ZeeConfig.gameUI.add(winPickupGob);
        ZeeConfig.windowFitView(winPickupGob);
        ZeeConfig.windowGlueToBorder(winPickupGob);
        if (gobs==null || gobs.size()==0)
            return;


        // populate window with gob list
        y = 0;//inside port
        for (int i = 0; i < gobs.size(); i++) {

            String resname = gobs.get(i).getres().name;
            String basename = gobs.get(i).getres().basename();

            //avoid duplicates
            if (listNamesAdded.contains(resname))
                continue;

            //avoid big kritters
            if (ZeeConfig.isKritter(resname) && ZeeConfig.isKritterNotPickable(gobs.get(i)))
                continue;

            // avoid duplicate names
            listNamesAdded.add(resname);

            // add button "pick" single gob
            wdg = scrollport.cont.add(
                new ZeeWindow.ZeeButton(30, "1"){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("activate")){
                            //pickup closest matching
                            Gob closest = ZeeConfig.getClosestGobByNameEnds(resname);
                            if (closest!=null) {
                                pickupGobItem(closest);
                            }
                        }
                    }
                },
                0,y
            );
            wdg.settip("pick one (key: q)");


            // add button pick "all"
            wdg = scrollport.cont.add(
                    new ZeeWindow.ZeeButton(30, "all") {
                        public void wdgmsg(String msg, Object... args) {
                            if (msg.contentEquals("activate")) {
                                //pickup closest matching
                                Gob closest = ZeeConfig.getClosestGobByNameEnds(resname);
                                if (closest!=null) {
                                    pickupAllGobItemsServerSide(closest);
                                }
                            }
                        }
                    },
                    wdg.c.x + wdg.sz.x, y
            );
            // disable button all if not a terobj
            if (!resname.contains("/terobjs/")) {
                ((Button)wdg).disable(true);
            }
            wdg.settip("pick all (Shift+q)");


            // pickup gobs in sequence
            wdg = scrollport.cont.add(
                    new ZeeWindow.ZeeButton(30, "seq") {
                        public void wdgmsg(String msg, Object... args) {
                            if (msg.contentEquals("activate")) {
                                pickupAllGobsClientSide(resname);
                            }
                        }
                    },
                    wdg.c.x + wdg.sz.x, y
            );
            wdg.settip("pick sequentially (Ctrl+Shift+q)");


            // add gob icon or name
            GobIcon gobIcon = gobs.get(i).getattr(GobIcon.class);
            if (gobIcon != null) {
                BufferedImage img = Resource.remote().loadwait(gobIcon.res.get().name).flayer(Resource.Image.class).img;
                if (img.getWidth() > 24 || img.getHeight() > 24)
                    img = ZeeManagerIcons.resizeBufferedImage(img,24,24);
                wdg = scrollport.cont.add(new IButton(img,img){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("activate")) {
                            //pickup closest matching
                            Gob closest = ZeeConfig.getClosestGobByNameEnds(resname);
                            if (closest!=null) {
                                gobClick(closest, 3, UI.MOD_SHIFT);
                            }
                        }
                    }
                }, wdg.c.x + wdg.sz.x + 3, y);
            }else {
                wdg = scrollport.cont.add(new Label(basename), wdg.c.x + wdg.sz.x + 3, y + 5);
            }

            y += 27;
        }

        winPickupGob.pack();
        ZeeConfig.windowFitView(winPickupGob);
        ZeeConfig.windowGlueToBorder(winPickupGob);
    }

    private static void pickGobsFilterSort(List<Gob> gobs, String gobBaseName) {
        //filter gobs by selected name, sort by player dist
        gobs.stream()
        .filter(gob1 -> {
            if (!gob1.getres().basename().contentEquals(gobBaseName))
                return false;
            //avoid big kritters
            if (ZeeConfig.isKritter(gob1.getres().name) && ZeeConfig.isKritterNotPickable(gob1))
                return false;
            return true;
        })
        .collect(Collectors.toList())
        .sort((gob1, gob2) -> {
            double d1 = ZeeConfig.distanceToPlayer(gob1);
            double d2 = ZeeConfig.distanceToPlayer(gob2);
            if ( d1 > d2 )
                return -1; //less than
            if ( d1 < d2 )
                return 1; // greater than
            return 0; // equal
        });
    }


    static boolean brightnessDefault() {
        Glob glob = ZeeConfig.gameUI.ui.sess.glob;
        glob.blightamb = glob.lightamb;
        Utils.setprefi(getLightPrefName(), 1);
        brightnessMsg(true);
        return true;
    }

    static boolean brightnessDown() {
        Glob glob = ZeeConfig.gameUI.ui.sess.glob;
        glob.blightamb = colorStep(glob.blightamb,-15);
        Utils.setprefi(getLightPrefName(), ZeeConfig.colorToInt(glob.blightamb));
        brightnessMsg(false);
        return true;
    }


    static boolean brightnessUp() {
        Glob glob = ZeeConfig.gameUI.ui.sess.glob;
        glob.blightamb = colorStep(glob.blightamb,15);
        Utils.setprefi(getLightPrefName(), ZeeConfig.colorToInt(glob.blightamb));
        brightnessMsg(false);
        return true;
    }

    private static void brightnessMsg(boolean defLight) {
        String msg = "";
        if (defLight) {
            msg = "default light ";
        }
        else {
            if (ZeeConfig.playerLocation == ZeeConfig.LOCATION_OUTSIDE)
                msg = "outside light ";
            else if (ZeeConfig.playerLocation == ZeeConfig.LOCATION_CELLAR)
                msg = "cellar light ";
            else if (ZeeConfig.playerLocation == ZeeConfig.LOCATION_CABIN)
                msg = "cabin light ";
            else if (ZeeConfig.playerLocation == ZeeConfig.LOCATION_UNDERGROUND)
                msg = "underground light ";
        }
        Glob glob = ZeeConfig.gameUI.ui.sess.glob;
        ZeeConfig.msgLow(msg + glob.blightamb.getRed());
    }

    private static Color colorStep(Color c, int step) {
        int red, green, blue;
        if (step < 0) {
            //reduce colors by step
            red = Math.max((int) ((double) c.getRed() + step), 0);
            green = Math.max((int) ((double) c.getGreen() + step), 0);
            blue = Math.max((int) ((double) c.getBlue() + step), 0);
        }else{
            //increase colors by step
            red = Math.min((int) ((double) c.getRed() + step), 255);
            green = Math.min((int) ((double) c.getGreen() + step), 255);
            blue = Math.min((int) ((double) c.getBlue() + step), 255);
        }
        return new Color(red,green,blue,c.getAlpha());
    }

    static void initPlayerLocation() {
        Glob glob = ZeeConfig.gameUI.ui.sess.glob;

        // save player location
        if (glob.blightamb.getRed() == ZeeConfig.DEF_LIGHT_CELLAR)
            ZeeConfig.playerLocation = ZeeConfig.LOCATION_CELLAR;
        // cabins incompatible with blightamb detection? use gobs isntead
        else if(!ZeeConfig.findGobsByNameEndsWith("/downstairs","/upstairs","-door").isEmpty())
            ZeeConfig.playerLocation = ZeeConfig.LOCATION_CABIN;
        else if (glob.blightamb.getRed() == ZeeConfig.DEF_LIGHT_UNDERGROUND)
            ZeeConfig.playerLocation = ZeeConfig.LOCATION_UNDERGROUND;
        else
            ZeeConfig.playerLocation = ZeeConfig.LOCATION_OUTSIDE;
        //println("set player location to "+ZeeConfig.getPlayerLocationName());

        // restore saved brightness
        restoreSavedBrightness();

        // midi radio
        ZeeMidiRadio.toggleRadio();

        // cupboard labeler
        if (ZeeCupboardLabeler.isActive)
            ZeeCupboardLabeler.checkPlayerLocation();
    }

    private static void restoreSavedBrightness() {
        int intColor = Utils.getprefi(getLightPrefName(),1);
        if (intColor < 0) {
            ZeeConfig.gameUI.ui.sess.glob.blightamb = ZeeConfig.intToColor(intColor);
            //println("set blightamb to "+glob.blightamb+" , "+intColor);
        }
    }

    private static String getLightPrefName() {
        return "blightamb_" + ZeeConfig.playerLocation;
    }

    private static void windowTestCoords() {
        String name = "test coordss123";
        Window win = ZeeConfig.getWindow(name);
        if(win!=null){
            win.reqdestroy();
        }
        win = ZeeConfig.gameUI.add(new Window(Coord.of(225,100),name){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("close"))
                    this.reqdestroy();
            }
        });
        int x = win.csz().x;
        int y = win.csz().y;

        //up
        win.add(new Button(60,"up"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    windowTestCoordsMove(this);
                }
            }
        }, (int)(x*0.37),0);
        //down
        win.add(new Button(60,"down"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    windowTestCoordsMove(this);
                }
            }
        }, (int)(x*0.37),(int)(y*0.80));
        //left
        win.add(new Button(60,"left"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    windowTestCoordsMove(this);
                }
            }
        }, 0,(int)(y*0.40));
        //right
        win.add(new Button(60,"right"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    windowTestCoordsMove(this);
                }
            }
        }, (int)(x*0.75),(int)(y*0.40));
        //center
        win.add(new Button(60,"center"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    windowTestCoordsMove(this);
                }
            }
        }, (int)(x*0.37),(int)(y*0.40));
    }

    static void windowTestCoordsMove(Button button){
        new Thread(){
            public void run() {
                try {
                    Coord c1 = ZeeConfig.getPlayerTile();
                    Coord c2 = Coord.of(c1);
                    println(button.text.text+" ... ");
                    switch (button.text.text){
                        case "up":
                            c2 = c1.sub(0,1);
                            break;
                        case "down":
                            c2 = c1.add(0,1);
                            break;
                        case "left":
                            c2 = c1.sub(1,0);
                            break;
                        case "right":
                            c2 = c1.add(1,0);
                            break;
                    }
                    println("    moveTo "+c2);
                    ZeeConfig.moveToTile(c2);
                    waitPlayerIdlePose();
                    println("    final "+ZeeConfig.getPlayerTile());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public static boolean isMouseUp() {
        return lastClickMouseDownMs < lastClickMouseUpMs;
    }

    static boolean isGobDeadOrKO(Gob gob) {
        return ZeeConfig.gobHasAnyPoseContains(gob,"/dead","/knock","-knock","/waterdead");
    }

    static boolean isGobPoseAggro(Gob gob) {
        //TODO add more aggro pose names
        return ZeeConfig.gobHasAnyPoseContains(gob,"/fgt");
    }

    static void toggleHitbox() {
        if (ZeeConfig.showHitbox)
            ZeeConfig.msgLow("show hitbox");
        else
            ZeeConfig.msgLow("hide hitbox");

        List<Gob> gobs = ZeeConfig.getAllGobs();

        for (Gob gob : gobs) {
            gob.toggleHitbox();
        }
    }

    static void toggleModelsAllGobs() {

        // hide gob window
        showWinHideGobs();

        if (ZeeConfig.hideGobs)
            ZeeConfig.msgLow("hide gobs");
        else
            ZeeConfig.msgLow("show gobs");

        Utils.setprefb("hideGobs", ZeeConfig.hideGobs);

        ZeeQuickOptionsWindow.updateCheckboxNoBump("hideGobs",ZeeConfig.hideGobs);

        toggleModelsInList(ZeeConfig.getAllGobs());
    }

    private static void toggleModelsInList(List<Gob> listGobs) {
        for (Gob gob : listGobs) {
            gob.toggleModelMaybe();
        }
    }

    static Window winHideGobs = null;
    static boolean hideGobWalls = Utils.getprefb("hideGobWalls",true);
    static boolean hideGobCrops = Utils.getprefb("hideGobCrops",true);
    // todo uncomment when fix
//    static boolean hideGobHouses = Utils.getprefb("hideGobHouses",false);
//    static boolean hideGobIdols = Utils.getprefb("hideGobIdols",false);
//    static boolean hideGobTamedAnimals = Utils.getprefb("hideGobTamedAnimals",false);
//    static boolean hideGobSmokeProducers = Utils.getprefb("hideGobSmokers",false);
    static long winHideGobLastInteractionMs;
    private static Label winHideGobLabelClosing;
    private static void showWinHideGobs() {

        if (winHideGobs != null) {
            //println("win toggle models already open");
            winHideGobLastInteractionMs = now();
            return;
        }

        // window
        winHideGobs = ZeeConfig.gameUI.add(
            new Window(Coord.of(150,75),"Hide Gobs"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("close")){
                        winHideGobs = null;
                        this.reqdestroy();
                    }
                }
            },
            ZeeConfig.gameUI.sz.div(2)
        );

        Widget wdg;
        int xpad = 5, ypad = 2;

        // walls
        wdg = winHideGobs.add(new CheckBox("walls"){
            { a = hideGobWalls;}
            public void set(boolean a) {
                super.set(a);
                Utils.setprefb("hideGobWalls", (hideGobWalls = a));
                winHideGobLastInteractionMs = now();
                toggleModelsInList(getGobsByTags(Gob.Tag.WALL));
            }
        }, 0,0);
        // crops
        wdg = winHideGobs.add(new CheckBox("crops"){
            { a = hideGobCrops;}
            public void set(boolean a) {
                super.set(a);
                Utils.setprefb("hideGobCrops", (hideGobCrops = a));
                winHideGobLastInteractionMs = now();
                toggleModelsInList(getGobsByTags(Gob.Tag.CROP));
            }
        }, wdg.c.x+wdg.sz.x+xpad,0);
        // trees
        wdg = winHideGobs.add(new CheckBox("trees"){
            { a = ZeeManagerTrees.hideGobTrees;}
            public void set(boolean a) {
                super.set(a);
                ZeeManagerTrees.hideGobTrees = a;
                ZeeManagerGobs.winHideGobLastInteractionMs = now();
                toggleModelsInList(getGobsByTags(Gob.Tag.TREE));
            }
        }, wdg.c.x+wdg.sz.x+xpad,0);

        // todo uncomment when fix
        // houses
//        wdg = winHideGobs.add(new CheckBox("houses"){
//            { a = hideGobHouses;}
//            public void set(boolean a) {
//                super.set(a);
//                Utils.setprefb("hideGobHouses", (hideGobHouses = a));
//                winHideGobLastInteractionMs = now();
//                toggleModelsInList(getGobsByTags(Gob.Tag.HOUSE));
//            }
//        },0,wdg.c.y+wdg.sz.y+ypad);
//        // idols
//        wdg = winHideGobs.add(new CheckBox("idols"){
//            { a = hideGobIdols;}
//            public void set(boolean a) {
//                super.set(a);
//                Utils.setprefb("hideGobIdols", (hideGobIdols = a));
//                winHideGobLastInteractionMs = now();
//                toggleModelsInList(getGobsByTags(Gob.Tag.IDOL));
//            }
//        },wdg.c.x+wdg.sz.x+xpad,wdg.c.y);
//        // tamed animals
//        wdg = winHideGobs.add(new CheckBox("tamed"){
//            { a = hideGobTamedAnimals;}
//            public void set(boolean a) {
//                super.set(a);
//                Utils.setprefb("hideGobTamedAnimals", (hideGobTamedAnimals = a));
//                winHideGobLastInteractionMs = now();
//                toggleModelsInList(getGobsByTags(Gob.Tag.TAMED_ANIMAL_OR_AUROCH_ETC));
//            }
//        },wdg.c.x+wdg.sz.x+xpad,wdg.c.y);
//        // smokers
//        wdg = winHideGobs.add(new CheckBox("smokers"){
//            { a = hideGobSmokeProducers;}
//            public void set(boolean a) {
//                super.set(a);
//                Utils.setprefb("hideGobSmokers", (hideGobSmokeProducers = a));
//                winHideGobLastInteractionMs = now();
//                toggleModelsInList(getGobsByTags(Gob.Tag.SMOKE_PRODUCER));
//            }
//        },wdg.c.x+wdg.sz.x+xpad,wdg.c.y);

        // label countdown auto close
        winHideGobLabelClosing = winHideGobs.add(new Label(""),0,wdg.c.y+wdg.sz.y+ypad);

        winHideGobs.pack();

        // auto-close after timeout
        new ZeeThread(){
            public void run() {
                //println("winHideGobs auto-close start");
                try {
                    synchronized (winHideGobs) {
                        final int timeoutMs = 5000;
                        long idleMs = 0;
                        winHideGobLastInteractionMs = now();
                        winHideGobLabelClosing.settext("autoclose " + (timeoutMs - idleMs) / 1000 + "s");
                        do {
                            sleep(1000);
                            idleMs = now() - ZeeManagerGobs.winHideGobLastInteractionMs;
                            if (idleMs < timeoutMs) {
                                winHideGobLabelClosing.settext("autoclose " + (timeoutMs - idleMs) / 1000 + "s");
                            } else {
                                break;
                            }
                        } while (winHideGobs != null);
                        if (winHideGobs != null) {
                            winHideGobs.reqdestroy();
                            winHideGobs = null;
                        }
                        //println("    window destroyed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //println("winHideGobs auto-close end");
            }
        }.start();
    }

    private static List<Gob> getGobsByTags(Gob.Tag ... tags) {
        List<Gob> ret = ZeeConfig.getAllGobs();
        ret.removeIf(gob1 -> {
            for (Gob.Tag tag : tags) {
                if (gob1.tags.contains(tag))
                    return false;
            }
            return true;
        });
        return ret;
    }

    static boolean isHideGob(Gob gob){
        if (ZeeConfig.hideGobs){
            if (gob.tags.contains(Gob.Tag.TREE) && ZeeManagerTrees.hideGobTrees)
                return true;
            if (gob.tags.contains(Gob.Tag.WALL) && hideGobWalls)
                return true;
            if (gob.tags.contains(Gob.Tag.CROP) && hideGobCrops)
                return true;
        }
        return false;
    }

    public static void checkOverlayEquedAdded(Sprite.Owner owner, Resource res) {
        if (!(owner instanceof Gob) ) {
            return;
        }
        Gob gob1 = (Gob) owner;
        if (!gob1.removed && gob1.settingsApplied) {
            synchronized (gob1) {
                gob1.equedOverlays++;
                // cheeseracks
                if (gob1.getres().name.endsWith("terobjs/cheeserack")){
                    if (gob1.equedOverlays == 3)
                        ZeeConfig.addGobColor(gob1, ZeeGobColor.COLOR_DARK_GREEN);
                    else
                        ZeeConfig.addGobColor(gob1, Color.orange);
                }
            }
        }
    }

    public static void checkOverlayRemoved(Gob gob, Sprite spr) {
        if (!gob.removed && gob.settingsApplied) {
            if (spr!=null && spr.res!=null && spr.res.name.contentEquals("gfx/fx/eq")) {
                synchronized (gob) {
                    gob.equedOverlays--;
                    // gardenpot
                    if (gob.getres().name.endsWith("terobjs/gardenpot") && gob.equedOverlays==0) {
                        ZeeConfig.removeGobColor(gob);
                    }
                    // cheeserack
                    else if (gob.getres().name.endsWith("terobjs/cheeserack")) {
                        if (gob.equedOverlays == 2)//removed 3rd tray
                            ZeeConfig.addGobColor(gob, Color.orange);
                        else if (gob.equedOverlays == 0)//removed last tray
                            ZeeConfig.removeGobColor(gob);
                    }
                }
            }
        }
    }

    public static void checkAttrSetFollowing(Following f) {
        if (ZeeConfig.gameUI==null)
            return;
        if (ZeeConfig.isPlayerMain(f.gob)){
            Gob target = ZeeConfig.getGobFollowTarget(f.gob);
            if (target==null || target.getres()==null || target.getres().name==null){
                println("checkAttrSetFollowing > target not ready? "+target);
                return;
            }
            if (target.getres().name.contains("cauldron")) {
                ZeeConfig.isPlayerFollowingCauldron = true;
                //println("player following " + target.getres().name);
            }
        }
    }
    public static void checkAttrDelFollowing(Gob g) {
        if (ZeeConfig.gameUI==null)
            return;
        if (ZeeConfig.isPlayerFollowingCauldron && ZeeConfig.isPlayerMain(g)){
            ZeeConfig.isPlayerFollowingCauldron = false;
            if (ZeeConfig.listCauldronContainers!=null) {
                ZeeConfig.listCauldronContainers.clear();
            }
            ZeeConfig.removePlayerText();
            //println("del player following attr ");
        }
    }

    static boolean discHelpOn = false;
    static List<String> discHelpListGobs;
    static void discoveryHelperToggle() {
        discHelpOn = !discHelpOn;
        // disc help on
        if (discHelpOn) {
            if(discHelpListGobs==null)
                discHelpListGobs = new ArrayList<>();
            ZeeConfig.addPlayerText("disc "+discHelpListGobs.size());
            //trees and bushes
            for (Gob g : ZeeConfig.getAllGobs()) {
                String name = g.getres().name;
                if (discHelpListGobs.contains(name))
                    continue;
                if (!ZeeManagerGobs.isGobTree(name) && !ZeeManagerGobs.isGobBush(name))
                    continue;
                //show tree
                if (ZeeConfig.hideGobs && ZeeManagerTrees.hideGobTrees && ZeeManagerGobs.isGobTree(name))
                    showModel(g);
                //add color
                ZeeConfig.addGobColor(g,Color.cyan);
            }
            //caches
            ZeeConsole.runCmdZeecow(new String[]{":zeecow","gobfind","cache"});
        }
        // disc help off
        else{
            ZeeConfig.removePlayerText();
            //trees and bushes
            for (Gob g : ZeeConfig.getAllGobs()) {
                String name = g.getres().name;
                if (!ZeeManagerGobs.isGobTree(name) && !ZeeManagerGobs.isGobBush(name))
                    continue;
                //remove color
                ZeeConfig.removeGobColor(g);
                //hide tree
                if (ZeeConfig.hideGobs && ZeeManagerTrees.hideGobTrees && ZeeManagerGobs.isGobTree(name))
                    hideModel(g);
            }
            //caches
            ZeeConsole.runCmdZeecow(new String[]{":zeecow","gobfind"});
        }
    }
    static void discHelpCheckGob(Gob gob){
        String name = gob.getres().name;
        if (discHelpGobTreeBush(gob) && !discHelpListGobs.contains(name)){
            // show tree
            if (ZeeConfig.hideGobs && ZeeManagerTrees.hideGobTrees && ZeeManagerGobs.isGobTree(name))
                showModel(gob);
            // add color
            ZeeConfig.addGobColor(gob, Color.CYAN);
        }
    }
    static void discHelpCheckPetalClicked(String petalName){
        if (!ZeeManagerGobs.discHelpOn)
            return;
        if (ZeeFlowerMenu.lastRightClickedGobOrItem instanceof Gob){
            petalName = petalName.toLowerCase();
            if (!petalName.contains("branch") && !petalName.contains("bark") && !petalName.contains("chop")){
                discHelpGobDone((Gob) ZeeFlowerMenu.lastRightClickedGobOrItem);
            }
        }
    }
    private static void discHelpGobDone(Gob gob) {
        String name = gob.getres().name;
        if(!discHelpListGobs.contains(name))
            discHelpListGobs.add(name);
        ZeeConfig.addPlayerText("disc "+discHelpListGobs.size());
        ArrayList<Gob> gobs = (ArrayList<Gob>) ZeeConfig.findGobsByNameEquals(name);
        boolean isTree = ZeeManagerGobs.isGobTree(name);
        boolean isBirch = name.endsWith("birch");
        for (Gob treeOrBush : gobs) {
            // remove color
            ZeeConfig.removeGobColor(treeOrBush);
            // hide trees
            if (isTree && !isBirch && ZeeConfig.hideGobs && ZeeManagerTrees.hideGobTrees)
                hideModel(treeOrBush);
        }
    }
    private static boolean discHelpGobTreeBush(Gob gob) {
        String name = gob.getres().name;
        return ZeeManagerGobs.isGobTree(name) ||
                ZeeManagerGobs.isGobBush(name);
    }
    static void discHelpCharSwitch(){
        if(discHelpListGobs != null)
            discHelpListGobs.clear();
        ZeeFlowerMenu.lastRightClickedGobOrItem = null;
        discHelpOn = false;
    }


    //TODO test
    static void sortGobsByClosestToPlayer(List<Gob> gobs) {
        gobs.sort((gob1, gob2) -> {
            double d1 = ZeeConfig.distanceToPlayer(gob1);
            double d2 = ZeeConfig.distanceToPlayer(gob2);
            if ( d1 > d2 )
                return 1;
            if ( d1 < d2 )
                return -1;
            return 0;
        });
    }
    
}

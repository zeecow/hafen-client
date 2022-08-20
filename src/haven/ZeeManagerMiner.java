package haven;

import java.util.LinkedList;
import java.util.List;

public class ZeeManagerMiner extends ZeeThread{

    private static final double DIST_BOULDER = 25;
    static boolean useOreForColumns = false;
    public static long lastDropItemMs = 0;
    public static boolean mining;
    public static ZeeManagerMiner manager;
    static ZeeWindow tunnelHelperWindow;
    static String listOreColumn = "leadglance,cassiterite,chalcopyrite,cinnabar,malachite";

    public static void checkMiningSelection() {
        if (ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_MINE)){

            // skip if mining on horse
            if (ZeeConfig.isPlayerMountingHorse()){
                ZeeConfig.msgError("TROLL RISK: mining on horse");
                return;
            }

            // if tunnel mining, show Helper Window
            if (tunnelCheckbox && !tunneling && isTunnel(ZeeConfig.lastSavedOverlay.a.sz())){
                tunneling = true;
                tunnelHelperShowWindow();
                tunnelHelperButtonAct.disable(true);
            }

            // wait player idle, if boulder close, chip it and resume mining
            new ZeeThread(){
                public void run() {
                    try {
                        if (isCombatActive())
                            return;
                        if (tunneling) {
                            if (tunnelHelperStage == TUNNELHELPER_STAGE0_IDLE) {
                                tunnelHelperSetStage(TUNNELHELPER_STAGE1_MINETUNNEL);
                            }
                            else if (tunnelHelperStage == TUNNELHELPER_STAGE2_WAITNEWCOLTILE){
                                tunnelHelperSetStage(TUNNELHELPER_STAGE3_MINECOL);
                            }
                        }

                        // wait mining stop
                        waitPlayerPoseMs(ZeeConfig.POSE_PLAYER_IDLE, 1000);
                        if (isCombatActive())
                            return;

                        // save player coord after mining new col tile
                        if (tunneling && tunnelHelperStage == TUNNELHELPER_STAGE3_MINECOL){
                            println("saved end coord");
                            if (tunnelHelperEndCoord!=null)
                                tunnelHelperEndCoordPrev = Coord.of(tunnelHelperEndCoord);
                            tunnelHelperEndCoord = ZeeConfig.getPlayerCoord();
                        }

                        // check if boulder close
                        if (!ZeeConfig.autoChipMinedBoulder)
                            return;
                        Gob boulder = getBoulderCloseEnoughForChipping();

                        // no boulder, mining stopped
                        if (boulder == null) {
                            if (tunneling) {
                                // finished mining tunnel
                                if (tunnelHelperStage == TUNNELHELPER_STAGE1_MINETUNNEL) {
                                    tunnelHelperSetStage(TUNNELHELPER_STAGE2_WAITNEWCOLTILE);
                                    ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);// prepare mining cursor
                                    //ZeeConfig.addPlayerText("click new col tile");
                                }
                                // finished mining new col tile
                                else if (tunnelHelperStage == TUNNELHELPER_STAGE3_MINECOL) {
                                    tunnelHelperSetStage(TUNNELHELPER_STAGE4_PICKSTONE);
                                    //ZeeConfig.removePlayerText();
                                    tunnelHelperButtonAct.disable(false);
                                    ZeeConfig.clickRemoveCursor();
                                    waitCursor(ZeeConfig.CURSOR_ARW);
                                    if (!tunnelHelperPickStonesManualClick){
                                        tunnelHelperButtonAct.click();
                                        tunnelHelperButtonAct.disable(true);
                                    }
                                }
                            }
                            return;
                        }

                        if (isCombatActive())
                            return;

                        // chip boulder
                        taskChipBoulder(boulder);

                    }catch (Exception e){
                        e.printStackTrace();
                        ZeeConfig.removePlayerText();
                    }
                }
            }.start();
        }
    }

    static boolean tunnelCheckbox = false;
    static boolean tunneling = false;
    static Coord tunnelHelperEndCoord, tunnelHelperEndCoordPrev;
    static Label tunnelHelperLabelStatus;
    static Button tunnelHelperButtonAct;
    static int tunnelHelperStage = 0;
    static boolean tunnelHelperPickStonesManualClick = false;
    static final int TUNNELHELPER_STAGE_FAIL = -1;
    static final int TUNNELHELPER_STAGE0_IDLE = 0;
    static final int TUNNELHELPER_STAGE1_MINETUNNEL = 1;
    static final int TUNNELHELPER_STAGE2_WAITNEWCOLTILE = 2;
    static final int TUNNELHELPER_STAGE3_MINECOL = 3;
    static final int TUNNELHELPER_STAGE4_PICKSTONE = 4;
    static final int TUNNELHELPER_STAGE5_BUILDCOL = 5;
    private static void tunnelHelperShowWindow() {
        if (tunnelHelperWindow == null) {
            Widget wdg;

            tunnelHelperWindow = new ZeeWindow(new Coord(240, 100), "Tunnel Helper") {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("close")) {
                        tunnelHelperExit();
                    } else
                        super.wdgmsg(msg, args);
                }
            };

            wdg = tunnelHelperWindow.add( tunnelHelperLabelStatus = new Label("idle"));
            tunnelHelperSetStage(TUNNELHELPER_STAGE0_IDLE);

            wdg = tunnelHelperWindow.add( tunnelHelperButtonAct = new Button(UI.scale(85),"Pick stones"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("activate")) {
                        // action pick stones
                        if (this.text.text.contentEquals("Pick stones")) {
                            //disable button before picking stones
                            this.disable(true);
                            new ZeeThread(){
                                public void run() {
                                    try {
                                        mining = true;
                                        if (pickStones(30)){
                                            //move to player mining end coord
                                            if (tunnelHelperEndCoordPrev!=null)
                                                ZeeConfig.clickCoord(tunnelHelperEndCoordPrev,1);
                                            else
                                                ZeeConfig.clickCoord(tunnelHelperEndCoord,1);
                                            waitPlayerIdlePose();
                                            tunnelHelperSetStage(TUNNELHELPER_STAGE5_BUILDCOL);
                                            // select menu item for stone column
                                            ZeeConfig.gameUI.menu.wdgmsg("act","bp","column","0");
                                            //ZeeConfig.addPlayerText("wait place col");
                                        }else{
                                            int invStones = pickStonesInvCount();
                                            ZeeConfig.msgError("missing "+(30-invStones)+" stones");
                                            ZeeConfig.println("missing "+(30-invStones)+" stones");
                                            tunnelHelperExit();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mining = false;
                                }
                            }.start();
                        }
                    }
                }
            },0,wdg.c.y+wdg.sz.y+3);

            //checkbox auto click button pick stones
            tunnelHelperWindow.add(new CheckBox("manual click"){
                public void changed(boolean val) {
                    tunnelHelperPickStonesManualClick = val;
                    tunnelHelperButtonAct.disable(!val);
                }
            },wdg.c.x+wdg.sz.x+3 , wdg.c.y+3);

            //checkbox ore column
            wdg = tunnelHelperWindow.add(new CheckBox("ore col: lead, cass, chalco, cinna, mala"){
                public void changed(boolean val) {
                    useOreForColumns = val;
                }
            },0,wdg.c.y+wdg.sz.y+3);

            //add window
            ZeeConfig.gameUI.add(tunnelHelperWindow, new Coord(100,100));
        }
        else{
            tunnelHelperWindow.show();
        }
    }

    private static void tunnelHelperSetStage(int stage) {
        String name = "unknown";
        switch (stage){
            case TUNNELHELPER_STAGE_FAIL:
                name = "fail"; break;
            case TUNNELHELPER_STAGE0_IDLE:
                name = "idle"; break;
            case TUNNELHELPER_STAGE1_MINETUNNEL:
                name = "mine tunnel"; break;
            case TUNNELHELPER_STAGE2_WAITNEWCOLTILE:
                name = "wait new col tile"; break;
            case TUNNELHELPER_STAGE3_MINECOL:
                name = "mine tile"; break;
            case TUNNELHELPER_STAGE4_PICKSTONE:
                name = "pick stone"; break;
            case TUNNELHELPER_STAGE5_BUILDCOL:
                name = "build col"; break;
        }
        println("stage " +tunnelHelperStage+ " to "+stage+" ("+name+")");
        tunnelHelperStage = stage;
        tunnelHelperLabelStatus.settext("stage "+stage+" - "+name);
        if (stage > TUNNELHELPER_STAGE0_IDLE)
            ZeeConfig.addPlayerText("("+stage+"/5) "+name);
    }

    public static int tunnelHelperExit() {
        int ret = tunnelHelperStage;
        mining = false;
        tunneling = false;
        tunnelHelperEndCoord = tunnelHelperEndCoordPrev = null;
        tunnelHelperSetStage(TUNNELHELPER_STAGE0_IDLE);
        ZeeConfig.removePlayerText();
        if (tunnelHelperWindow !=null)
            tunnelHelperWindow.hide();
        if (ZeeInvMainOptionsWdg.cbTunnel!=null)
            ZeeInvMainOptionsWdg.cbTunnel.set(false);
        return ret;
    }

    public static boolean isTunnel(Coord sz) {
        int minSize = 9;
        if (sz.x==1 && sz.y>=minSize)
            return true;
        if (sz.y==1 && sz.x>=minSize)
            return true;
        return false;
    }

    public static void tunnelHelperBuildColumn(Window window) {
        Button buildBtn = ZeeConfig.getButtonNamed(window,"Build");
        if (buildBtn==null) {
            println("ZeeConfig.windowAdded() > no button for building column");
            return;
        }
        new ZeeThread(){
            public void run() {
                buildBtn.click();
                waitNotPlayerPose(ZeeConfig.POSE_PLAYER_BUILD);//waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_BUILD);
                ZeeConfig.removePlayerText();

                //back to saved coord
                ZeeConfig.clickCoord(tunnelHelperEndCoord,1);
                waitPlayerIdlePose();

                //drop remaining inv stones
                ZeeConfig.getMainInventory().children(WItem.class).forEach(wItem -> {
                    String name = wItem.item.getres().basename();
                    if (ZeeConfig.mineablesStone.contains(name) || (useOreForColumns && listOreColumn.contains(name)))
                        wItem.item.wdgmsg("drop", Coord.z);
                });

                // if window still open, means not enough mats
                if (ZeeConfig.getWindow("Stone Column") != null) {
                    println("miner semi helper > not enough mats for column");
                    tunnelHelperSetStage(ZeeManagerMiner.TUNNELHELPER_STAGE_FAIL);
                    tunnelHelperExit();
                    return;
                }

                //exit tunnelHelper window if checkbox not selected
                if (ZeeInvMainOptionsWdg.cbTunnel!=null && !ZeeInvMainOptionsWdg.cbTunnel.a) {
                    tunnelHelperExit();
                }
                //reset initial stage
                else{
                    tunneling = false;
                    tunnelHelperSetStage(TUNNELHELPER_STAGE0_IDLE);
                    tunnelHelperEndCoord = tunnelHelperEndCoordPrev = null;
                }
            }
        }.start();
    }

    @Override
    public void run() {
        manager = this;
    }


    public static void mineTiles(Coord c1, Coord c2) throws InterruptedException{
        mining = true;
        println("mine tiles  c1"+c1+"  c2"+c2);
        ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);
        waitCursor(ZeeConfig.CURSOR_MINE);
        ZeeConfig.gameUI.map.wdgmsg("sel", c1, c2, 0);
        //waitPlayerIdleFor(2);//minimum 2 sec
        waitPlayerIdlePose();
        ZeeConfig.clickRemoveCursor();
        waitCursor(ZeeConfig.CURSOR_ARW);
    }


    public static Gob getBoulderCloseEnoughForChipping() {
        Gob boulder = ZeeConfig.getClosestGobName("gfx/terobjs/bumlings/");
        if (boulder!=null && ZeeConfig.distanceToPlayer(boulder) < DIST_BOULDER){
            return boulder;
        }
        return null;
    }


    public static boolean pickStones(int wantedStones) throws Exception{
        List<Gob> terobjs;
        Gob closestStone;
        Inventory inv = ZeeConfig.getMainInventory();

        // set arrow cursor
        if (!ZeeConfig.CURSOR_ARW.contentEquals(ZeeConfig.getCursorName())) {
            ZeeConfig.clickRemoveCursor();
            waitCursor(ZeeConfig.CURSOR_ARW);
        }

        //check if inventory stones are enough
        int invStones = pickStonesInvCount();
        if (invStones >= wantedStones)
            return true;

        // if not enough stones, equip sack(s)
        if(inv.getNumberOfFreeSlots() < wantedStones){
            WItem sack = ZeeManagerItemClick.getSackFromBelt();//1st sack
            if (sack!=null) {
                Thread t = new ZeeManagerItemClick(sack);
                t.start();
                t.join();//wait equip sack
                sack = ZeeManagerItemClick.getSackFromBelt();//2nd sack
                if(inv.getNumberOfFreeSlots()<wantedStones && sack!=null){
                    t = new ZeeManagerItemClick(sack);
                    t.start();
                    t.join();//wait equip 2nd sack
                }
            }
            waitNotHoldingItem();
        }

        if (!tunneling)
            ZeeConfig.addPlayerText("Picking stone");

        //loop pickup stone types until total reached
        while (mining  &&  invStones<wantedStones && inv.getNumberOfFreeSlots()!=0) {
            terobjs = ZeeConfig.findGobsByNameContains("gfx/terobjs/items/");
            terobjs.removeIf(item -> {//filter column stone types
                String name = item.getres().basename();
                if (useOreForColumns && listOreColumn.contains(name))
                    return false;
                return !ZeeConfig.mineablesStone.contains(name);
            });
            if (terobjs.size() == 0)
                break; //no stones to pick, end loop
            closestStone = ZeeConfig.getClosestGob(terobjs);
            if (closestStone == null)
                continue; // picked all stone type, try other stones
            if (ZeeConfig.distanceToPlayer(closestStone) > 11*TILE_SIZE){
                println("stones too far away");
                return false;
            }
            //println("clicking closestStone "+closestStone.getres().basename());
            ZeeManagerGobClick.gobClick(closestStone,3,UI.MOD_SHIFT);//pick all
            if(!waitInvIdleMs(500)){
                return false;
            }
            //invStones += inv.countItemsByName(closestStone.getres().basename());
            invStones = pickStonesInvCount();
            //println("loop > invStones "+invStones);
        }

        // drop if holding stone
        if (ZeeConfig.isPlayerHoldingItem()){
            String name = ZeeConfig.gameUI.vhand.item.getres().basename();
            if ( ZeeConfig.mineablesStone.contains(name) || (useOreForColumns && listOreColumn.contains(name))){
                ZeeConfig.gameUI.vhand.item.wdgmsg("drop", Coord.z);
                waitNotHoldingItem();
            }
        }

        if (!tunneling)
            ZeeConfig.removePlayerText();

        //println("got "+invStones+" stones ,  enough="+(invStones >= wantedStones) +" , invfull="+ (inv.getNumberOfFreeSlots()==0));
        return invStones >= wantedStones || inv.getNumberOfFreeSlots()==0;
    }

    private static int pickStonesInvCount() {
        List<WItem> invItems = new LinkedList<>(ZeeConfig.getMainInventory().children(WItem.class));
        invItems.removeIf(item -> {
            String name = item.item.getres().basename();
            if (useOreForColumns && listOreColumn.contains(name))
                return false;
            return !ZeeConfig.mineablesStone.contains(name);
        });
        return invItems.size();
    }

    public static void taskChipBoulder(Gob gobBoulder) throws Exception {

        if (!ZeeConfig.autoChipMinedBoulder)
            return;

        //println(">task chip_boulder on");
        if (!tunneling)
            ZeeConfig.addPlayerText("boulder");

        //wait boulder loading/clickable
        sleep(1000);
        if (isCombatActive())
            return;

        //activate arrow cursor
        ZeeConfig.clickRemoveCursor();
        waitCursor(ZeeConfig.CURSOR_ARW);

        //select chip menu from boulder
        if (isCombatActive())
            return;
        ZeeManagerGobClick.clickGobPetal(gobBoulder,"Chip stone");
        sleep(100);

        //restore mining icon for autodrop
        ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);

        //wait chipping stop
        if(waitPlayerIdlePose()){
            if (isCombatActive())
                return;
            //println("chip boulder done");
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.lastSavedOverlayStartCoord, ZeeConfig.lastSavedOverlayEndCoord, ZeeConfig.lastSavedOverlayModflags);
        }else{
            println("canceled chipping boulder?");
        }

        //println(">task chip_boulder off");
        if (!tunneling)
            ZeeConfig.removePlayerText();
    }

    public static void notifyColumn(Gob gob, float hp){
        if (!isMineSupport(gob))
            return;
        ZeeConfig.addGobText(gob,(hp*100)+"%");
        //stopMining();
    }

    public static void stopMining() {
        new ZeeThread(){
            public void run() {
                try {
                    mining = false;
                    println("stop mining");
                    ZeeConfig.msgError("stop mining");
                    ZeeConfig.clickRemoveCursor();
                    waitCursor(ZeeConfig.CURSOR_ARW);
                    ZeeConfig.stopMovingEscKey();
                    ZeeThread.staminaMonitorStop();//case stam monitor thread is running
                    if (manager != null)
                        manager.interrupt();
                    if (tunneling)
                        tunnelHelperExit();
                    ZeeConfig.resetTileSelection();
                    ZeeConfig.removePlayerText();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static boolean chipBoulder(Gob boulder) {
        try {
            //ZeeClickGobManager.gobClick(boulder, 3);
            ZeeConfig.clickRemoveCursor();//remove mining cursor
            if(!waitCursor(ZeeConfig.CURSOR_ARW)){
                println(">chipBoulder couldn't change cursor to arrow");
                return false;
            }
            ZeeManagerGobClick.clickGobPetal(boulder, "Chip stone");//chip boulder
            waitNoFlowerMenu();
            ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);//restore mining icon for autodrop
            //return waitBoulderFinish(boulder);
            return waitPlayerIdlePose();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    // TODO delete if waitPlayerPoseIdle works
    private static boolean waitBoulderFinish(Gob boulder) {
        try {
            ZeeConfig.lastMapViewClickButton = 2;
            while (!ZeeConfig.isTaskCanceledByGroundClick() && ZeeManagerGobClick.findGobById(boulder.id) != null) {
                //println("gob still exist > "+ZeeClickGobManager.findGobById(boulder.id));
                Thread.sleep(PING_MS);//sleep 1s
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return (ZeeManagerGobClick.findGobById(boulder.id) == null);
    }


    public static boolean isCombatActive() {
        try {
            boolean isCombat = ZeeConfig.gameUI.fv.current != null;
            if (isCombat && tunneling)
                tunnelHelperExit();
            return isCombat;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isMining() {
        long now = System.currentTimeMillis();
        if(now - lastDropItemMs > 999) {
            //if last mined item is older than Xms, consider not mining
            return false;
        }else{
            return true;
        }
    }

    public static boolean isBoulder(Gob gob) {
        return (gob!=null &&
                gob.getres()!=null &&
                gob.getres().name.startsWith("gfx/terobjs/bumlings/") &&
                !gob.getres().name.startsWith("gfx/terobjs/bumlings/ras") // cave-in boulder
        );
    }

    public static boolean isMineSupport(Gob gob) {
        return gob!=null && gob.getres()!=null &&
            (gob.getres().name.equals("gfx/terobjs/minebeam") ||
            gob.getres().name.equals("gfx/terobjs/column") ||
            gob.getres().name.equals("gfx/terobjs/minesupport"));
    }

    public static void println(String s) {
        System.out.println(s);
    }
}

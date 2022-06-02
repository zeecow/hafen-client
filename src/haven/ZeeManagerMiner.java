package haven;

import java.util.LinkedList;
import java.util.List;

public class ZeeManagerMiner extends ZeeThread{

    private static final double DIST_BOULDER = 25;
    static boolean useOreForColumns = false;
    public static long lastDropItemMs = 0;
    public static boolean mining;
    public static ZeeManagerMiner manager;
    static ZeeWindow windowManager, tunnelHelperWindow;
    static MCache.Overlay ol;
    static String listOreColumn = "leadglance,cassiterite,chalcopyrite,cinnabar,malachite";

    public static void checkMiningSelection() {
        if (ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_MINE)){

            // skip if mining on horse
            if (ZeeConfig.isPlayerMountingHorse()){
                ZeeConfig.msgError("TROLL RISK: mining on horse");
                return;
            }

            // if tunnel mining, show Helper Window
            if (tunnelCheckbox && !tunneling && isTunnel(ZeeConfig.savedTileSelOverlay.a.sz())){
                tunneling = true;
                showWindowTunnelHelper();
                tunnelHelperButtonAct.disable(true);
            }

            // wait player idle, if boulder close, chip it and resume mining
            new ZeeThread(){
                public void run() {
                    try {
                        if (isCombatActive())
                            return;
                        if (tunneling) {
                            tunnelHelperLabelStatus.settext("mining");
                            if (tunnelHelperStage == TUNNELHELPER_STAGE0_IDLE) {
                                tunnelHelperStage = TUNNELHELPER_STAGE1_MINETUNNEL;
                            }
                        }
                        waitPlayerPoseMs(ZeeConfig.POSE_PLAYER_IDLE, 1000);//wait mining stop
                        if (isCombatActive())
                            return;
                        Gob boulder = getBoulderCloseEnoughForChipping();
                        // mining ended, since no boulder
                        if (boulder == null) {
                            if (tunneling) {
                                tunnelHelperLabelStatus.settext("mining done");
                                if (tunnelHelperStage == TUNNELHELPER_STAGE1_MINETUNNEL) {
                                    tunnelHelperStage = TUNNELHELPER_STAGE2_MINECOL;
                                    // prepare mining cursor
                                    ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);
                                    tunnelHelperLabelStatus.settext("waiting column tile");
                                    ZeeConfig.addPlayerText("click new col tile");
                                } else if (tunnelHelperStage == TUNNELHELPER_STAGE2_MINECOL) {
                                    tunnelHelperStage = TUNNELHELPER_STAGE3_PICKSTONE;
                                    ZeeConfig.removePlayerText();
                                    //save player final coord
                                    tunnelHelperLastPlayerCoord = ZeeConfig.getPlayerCoord();
                                    tunnelHelperButtonAct.change("Pick stones");
                                    tunnelHelperButtonAct.disable(false);
                                    ZeeConfig.clickRemoveCursor();
                                    waitCursor(ZeeConfig.CURSOR_ARW);
                                    if (tunnelHelperAutoPickStones){
                                        tunnelHelperButtonAct.click();
                                        tunnelHelperButtonAct.disable(true);
                                    }
                                }
                            }
                            return;
                        }
                        if (isCombatActive())
                            return;
                        if (tunneling) {
                            tunnelHelperLabelStatus.settext("boulder");
                        }
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
    static Coord tunnelHelperLastPlayerCoord;
    static Label tunnelHelperLabelStatus;
    static Button tunnelHelperButtonAct;
    static int tunnelHelperStage = 0;
    static boolean tunnelHelperAutoPickStones = false;
    static final int TUNNELHELPER_STAGE0_IDLE = 0;
    static final int TUNNELHELPER_STAGE1_MINETUNNEL = 1;
    static final int TUNNELHELPER_STAGE2_MINECOL = 2;
    static final int TUNNELHELPER_STAGE3_PICKSTONE = 3;
    static final int TUNNELHELPER_STAGE4_BUILDCOL = 4;
    static final int TUNNELHELPER_STAGE5_FAIL = 5;
    private static void showWindowTunnelHelper() {
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
            tunnelHelperStage = TUNNELHELPER_STAGE0_IDLE;

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
                                        tunnelHelperLabelStatus.settext("picking stones");
                                        mining = true;
                                        if (pickStones(30)){
                                            //move to player mining end coord
                                            ZeeConfig.clickCoord(tunnelHelperLastPlayerCoord,1);
                                            waitPlayerIdlePose();
                                            tunnelHelperStage = TUNNELHELPER_STAGE4_BUILDCOL;
                                            // select menu item for stone column
                                            ZeeConfig.gameUI.menu.wdgmsg("act","bp","column","0");
                                            ZeeConfig.addPlayerText("wait place col");
                                        }else{
                                            int invStones = pickStonesInvCount();
                                            ZeeConfig.msgError("missing "+invStones+" stones");
                                            ZeeConfig.println("missing "+invStones+" stones");
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
            tunnelHelperWindow.add(new CheckBox("auto click"){
                public void changed(boolean val) {
                    tunnelHelperAutoPickStones = val;
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

    public static int tunnelHelperExit() {
        int ret = tunnelHelperStage;
        mining = false;
        tunneling = false;
        tunnelHelperStage = TUNNELHELPER_STAGE0_IDLE;
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
                waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_BUILD);
                ZeeConfig.removePlayerText();
                //back to saved coord
                ZeeConfig.clickCoord(tunnelHelperLastPlayerCoord,1);
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
                    ZeeManagerMiner.tunnelHelperStage = ZeeManagerMiner.TUNNELHELPER_STAGE5_FAIL;
                    tunnelHelperExit();
                    return;
                }

                //exit tunnelHelper window if checkbox not selected
                if (ZeeInvMainOptionsWdg.cbTunnel!=null && !ZeeInvMainOptionsWdg.cbTunnel.a) {
                    tunnelHelperExit();
                }
                else{
                    //reset initial stage
                    tunneling = false;
                    tunnelHelperStage = TUNNELHELPER_STAGE0_IDLE;
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

        //loop pickup stone types until total reached
        ZeeConfig.addPlayerText("Picking stone");
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
        //println(">task chip_boulder on");
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
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
        }else{
            println("canceled chipping boulder?");
        }

        //println(">task chip_boulder off");
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

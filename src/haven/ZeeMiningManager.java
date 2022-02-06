package haven;

import java.util.List;

public class ZeeMiningManager extends ZeeThread{

    public static final String ACTION_CHIP_BOULDER = "chip boulder";
    private static final long MS_CURSOR_CHANGE = 200;
    private static final double DIST_BOULDER = 25;
    private final String task;
    public static long lastDropItemMs = 0;
    public static boolean mining;
    public static boolean isChipBoulder;
    public static Gob gobBoulder;
    public static ZeeMiningManager manager;

    public ZeeMiningManager(String task, Gob gob){
        this.task = task;
        this.gobBoulder = gob;
    }

    @Override
    public void run() {
        println("mining manager on");
        try {
            if (task.equalsIgnoreCase(ACTION_CHIP_BOULDER)) {
                ZeeClickGobManager.gobClick(gobBoulder, 3);//remove mining cursor
                Thread.sleep(MS_CURSOR_CHANGE);//wait cursor change
                ZeeClickGobManager.clickGobPetal(gobBoulder,"Chip stone");//chip boulder
                ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);//restore mining icon for autodrop
                if(waitBoulderFinish(gobBoulder)){
                    println("chip boulder done");
                    ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
                }else{
                    println("still chipping???");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeClickGobManager.resetClickPetal();
        println("mining manager off");
    }

    /*
    check if new boulder was created while mining
 */
    public static void checkNearBoulder(Gob gob) {
        if(ZeeConfig.autoChipMinedBoulder && isMining() && isBoulder(gob)){
            //println(ZeeClickGobManager.distanceToPlayer(gob)+" to "+gob.getres().name);
            if(ZeeConfig.distanceToPlayer(gob) < DIST_BOULDER){
                if(isCombatActive()) // cancel if combat active
                    return;
                new ZeeMiningManager(ACTION_CHIP_BOULDER, gob).start();
            }
        }
    }


    //TODO: if mining tile is too hard, 2000ms may not be enough
    public static boolean isInventoryIdle() {
        return (now() - ZeeConfig.lastInvItemMs > 2000);
    }


    public static void notifyColumn(Gob gob, float hp){
        if (!isMineSupport(gob))
            return;
        ZeeConfig.addGobText(gob,(hp*100)+"%");
        stopMining();
    }

    public static void stopMining() {
        println("stop mining");
        try {
            ZeeConfig.gameUI.msg("stop mining");
            ZeeConfig.clickGroundZero();//remove cursor?
            Thread.sleep(PING_MS+100);
            ZeeConfig.clickGroundZero(1);//right click ground
            cancelThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void cancelThread() {
        ZeeConfig.resetTileSelection();
        mining = false;
        isChipBoulder = false;
        manager.interrupt();
    }

    public static void chipBoulder(Gob boulder) {
        try {
            //ZeeClickGobManager.gobClick(boulder, 3);
            ZeeConfig.clickGroundZero();//remove mining cursor
            if(!waitCursor(ZeeConfig.CURSOR_ARW)){
                println(">chipBoulder couldn't change cursor to arrow");
                return;
            }
            println("> click gob Petal");
            ZeeClickGobManager.clickGobPetal(boulder, "Chip stone");//chip boulder
            ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);//restore mining icon for autodrop
            if (waitBoulderFinish(boulder)) {
                println("chip boulder done");
                ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
            } else {
                println("still chipping???");
            }
            //ZeeClickGobManager.resetClickPetal();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static boolean waitBoulderFinish(Gob boulder) {
        try {
            while (ZeeClickGobManager.findGobById(boulder.id) != null) {
                //println("gob still exist > "+ZeeClickGobManager.findGobById(boulder.id));
                Thread.sleep(PING_MS);//sleep 1s
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return (ZeeClickGobManager.findGobById(boulder.id) == null);
    }


    public static boolean isCombatActive() {
        try {
            return ZeeConfig.gameUI.fv.current != null;
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

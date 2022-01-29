package haven;

import java.util.List;

public class ZeeMiningManager extends ZeeThread{

    private static final double DIST_BOULDER = 25;
    public static long lastDropItemMs = 0;
    public static boolean mining;
    public static boolean isChipBoulder;
    public static Gob gobBoulder;
    public static ZeeMiningManager manager;

    public ZeeMiningManager(){
        manager = this;
        mining = true;
        isChipBoulder = false;
    }

    @Override
    public void run() {
        println("mining manager on");
        try {
            waitPlayerIdle();
            println("player idle");
            ZeeConfig.lastInvItemMs = now();// avoid isMining() break
            while(mining && !interrupted()){
                sleep(PING_MS);
                if(isCombatActive()) {
                    println("combat active, stop mining");
                    continue;
                }if(isChipBoulder){
                    println("isChipBoulder true");
                    chipBoulder(gobBoulder);
                    isChipBoulder = false;
                    ZeeConfig.lastInvItemMs = now();// avoid isMining() break
                    continue;
                }else if (isMining()) {
                    println("keep mining");
                }else{
                    println("break loop");
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        cancelThread();
        println("mining manager off");
    }

    public static void checkGobBoulder(Gob gob) {
        if (isBoulder(gob) && ZeeConfig.distanceToPlayer(gob) < DIST_BOULDER){
            isChipBoulder = true;
            gobBoulder = gob;
        }
    }

    //TODO: if mining tile is too hard, 2000ms may not be enough
    public static boolean isInventoryIdle() {
        return (now() - ZeeConfig.lastInvItemMs > 2000);
    }


    public static void notifyColumn(Gob gob, int hp){
        if (!isMineSupport(gob))
            return;
        ZeeConfig.addGobText(gob,(hp*25)+"%");
        stopMining();
    }

    public static void stopMining() {
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
        println("> isMinig() > " + mining +" && "+ "(" + ZeeConfig.isPlayerMoving() +" || "+ !isInventoryIdle() + ")");
        return mining && (ZeeConfig.isPlayerMoving() || !isInventoryIdle());
    }

    private static boolean isBoulder(Gob gob) {
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

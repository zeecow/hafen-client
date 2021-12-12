package haven;

public class ZeeMiningManager extends Thread{

    public static final String ACTION_CHIP_BOULDER = "chip boulder";
    private static final long MS_CURSOR_CHANGE = 200;
    public static long lastDropItemMs = 0;
    private final String task;
    private final Gob gob;

    public ZeeMiningManager(String task, Gob gob){
        this.task = task;
        this.gob = gob;
    }

    @Override
    public void run() {
        println("mining manager on");
        try {
            if (task.equalsIgnoreCase(ACTION_CHIP_BOULDER)) {
                ZeeClickGobManager.gobClick(gob, 3);//remove mining cursor
                Thread.sleep(MS_CURSOR_CHANGE);//wait cursor change
                ZeeConfig.scheduleClickPetal("Chip stone");
                ZeeClickGobManager.gobClick(gob, 3);//chip boulder
                ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);//restore mining icon for autodrop
                if(waitBoulderFinish()){
                    println("chip boulder done");
                    ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
                }else{
                    println("still chipping???");
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.resetClickPetal();
        println("mining manager off");
    }

    private boolean waitBoulderFinish() {
        try {
            while (ZeeClickGobManager.findGobById(gob.id) != null) {
                //println("gob still exist > "+ZeeClickGobManager.findGobById(gob.id));
                Thread.sleep(2000);//sleep 1s
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return (ZeeClickGobManager.findGobById(gob.id) == null);
    }

    /*
        check if new boulder was created while mining
     */
    public static void checkNearBoulder(Gob gob) {
        if(ZeeConfig.autoChipMinedBoulder && isMining() && isBoulder(gob)){
            //println(ZeeClickGobManager.distanceToPlayer(gob)+" to "+gob.getres().name);
            if(ZeeConfig.distanceToPlayer(gob) < 25){
                if(isCombatActive()) // cancel if combat active
                    return;
                new ZeeMiningManager(ACTION_CHIP_BOULDER, gob).start();
            }
        }
    }

    public static boolean isCombatActive() {
        try {
            return ZeeConfig.gameUI.fv.current != null;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isMining() {
        long now = System.currentTimeMillis();
        if(now - lastDropItemMs > 999) {
            //if last mined item is older than 2s, consider not mining
            return false;
        }else{
            return true;
        }
    }

    private static boolean isBoulder(Gob gob) {
        return (gob!=null &&
                gob.getres()!=null &&
                gob.getres().name.startsWith("gfx/terobjs/bumlings/") &&
                !gob.getres().name.startsWith("gfx/terobjs/bumlings/ras") // cave-in boulder
        );
    }

    public static void println(String s) {
        System.out.println(s);
    }
}

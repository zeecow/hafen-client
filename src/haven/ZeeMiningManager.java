package haven;

import java.awt.desktop.AppReopenedEvent;

public class ZeeMiningManager extends ZeeThread{

    public static final String TASK_CHIP_BOULDER = "chip boulder";
    public static final String TASK_MAKE_TUNNEL = "make tunnel";
    private static final long MS_CURSOR_CHANGE = 200;
    private static final double DIST_BOULDER = 25;
    private final String task;
    public static boolean busy;
    public static long lastDropItemMs = 0;
    public static boolean mining;
    public static boolean isChipBoulder;
    public static Gob gobBoulder;
    public static ZeeMiningManager manager;
    static ZeeWindow windowManager;
    static ZeeWindow.ZeeButton windowButtonTunnel;
    static Coord tunnelPlec, tunnelSubc, tunnelColc;

    public ZeeMiningManager(String task){
        this.task = task;
    }

    public ZeeMiningManager(String task, Gob gob){
        this.task = task;
        this.gobBoulder = gob;
    }

    @Override
    public void run() {
        println(">mining manager on");
        try {
            if (task.contentEquals(TASK_CHIP_BOULDER)) {
                taskChipBoulder();
            }else if (task.contentEquals(TASK_MAKE_TUNNEL)){
                taskMakeTunnel();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeClickGobManager.resetClickPetal();
        println(">mining manager off");
    }

    public static void checkTunnelMining(Coord sc, Coord ec) {
        //println("sc="+sc+"  ec="+ec);
        //println("sc-ec="+(sc.sub(ec))+"  ec-sc="+ec.sub(sc));
        //println("player coord="+ZeeConfig.getPlayerCoord());
        //println("player tile="+ZeeConfig.getPlayerTile());
        // sc-ec=(10, 0)  ec-sc=(-10, 0)
        if (!ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_MINE))
            return;
        Coord tiles = sc.sub(ec);
        if((Math.abs(tiles.x)==10 && tiles.y==0)
            || (Math.abs(tiles.y)==10 && tiles.x==0)) // if tunnel is 11x1 tiles
        {
            new ZeeThread() {
                public void run() {
                    waitStaminaIdleMs(2);
                    Coord plec = ZeeMiningManager.tunnelPlec = ZeeConfig.getPlayerCoord();
                    Coord subc = ZeeMiningManager.tunnelSubc = plec.sub(ec);
                    Coord colc = ZeeMiningManager.tunnelColc = new Coord();
                    if ((subc.x==0 && Math.abs(subc.y)==1) || (subc.y==0 && Math.abs(subc.x)==1)) {
                        //show window if player ended mining on prev to last tile
                        if (subc.x==0) {
                            colc.x = ec.x + 1;
                            colc.y = ec.y;
                        }else{
                            colc.x = ec.x;
                            colc.y = ec.y + 1;
                        }
                        showWindowTunnel();
                    }else
                        println("checkTunnelMining > plec="+plec+"  ec="+ec+"  subc="+subc);
                }
            }.start();
        }
    }

    private static void showWindowTunnel() {
        Widget wdg;
        if(windowManager ==null) {
            windowManager = new ZeeWindow(new Coord(150, 60), "Tunnelling") {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("close")) {
                        busy = false;
                        exitManager();
                    }else
                        super.wdgmsg(msg, args);
                }
            };
            windowButtonTunnel = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(85),"make tunnel"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        new ZeeMiningManager(TASK_MAKE_TUNNEL).start();
                    }
                }
            }, 5,5);
            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));
        }else{
            windowManager.show();
        }
    }

    public static void exitManager(String msg) {
        exitManager();
        println(msg);
    }
    public static void exitManager() {
        busy = false;
        ZeeConfig.clickGroundZero(1);
        windowManager.hide();
    }

    private void taskMakeTunnel() {
        try {

            /*
                mine stonecolumn 1x1 tile
             */
            //tunnelPlec
            //tunnelSubc
            Coord colc = new Coord();
            if (tunnelSubc.x==0);
            ZeeConfig.gameUI.map.wdgmsg("sel", colc, colc, ZeeConfig.savedTileSelModflags);
            waitPlayerIdleFor(2);


            /*
                build stonecolumn
             */
            //haven.MenuGrid@6ce8cd1d ; act ; [bp, column, 0, (-1000024, -1019956)]
            //haven.MapView@620bad61 ; place ; [(-1000960, -1017344), -32768, 1, 0]

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void taskMakeTunnel_old() {
        try {
            println("  >task make_tunnel on");
            ZeeConfig.addPlayerText("tunneling");
            // check if standing next to stone column, before start digging
            windowButtonTunnel.disable(true);
            Coord playersc = ZeeConfig.getPlayerTile();
            waitPlayerIdleFor(2);
            windowButtonTunnel.disable(false);
            Gob column = ZeeConfig.getClosestGobName("gfx/terobjs/column");
            if (column!=null) {
                int dist = (int)ZeeConfig.distanceToPlayer(column);
                ZeeConfig.addGobText(column, "dist " + dist);
                if (dist < 15) {
                    println("    make tunnel");
                    Coord sc = ZeeConfig.savedTileSelStartCoord;
                    Coord ec = ZeeConfig.savedTileSelEndCoord;
                    Coord playerec = ZeeConfig.getPlayerTile();
                    println("sc="+sc+"  ec="+ec);
                    println("sc-ec="+(sc.sub(ec))+"  ec-sc="+ec.sub(sc));
                    println("playersc="+playersc+"  playerec="+playerec);
                    println("playersc-playerec="+(playersc.sub(playerec))+"  playerec-playersc="+playerec.sub(playersc));

                    /*
                        calculate tunnel coords
                     */
                    Coord newsc, newec, coordBuildColumn;
                    int signal;
                    if(sc.x == ec.x){
                        // tunnel direction = X
                        println("tunnel on X="+sc.x);
                        newsc = new Coord(sc.x,0);
                        newec = new Coord(sc.x,0);
                        // extend coord that is closer to playerEndCoord
                        if( Math.abs(playerec.y - ec.y) < Math.abs(playerec.y - sc.y) ){
                            //playerEndCoord is closer to ec
                            println("playerEndCoord is closer to ec "+ec);
                            if (ec.y < 0)
                                signal = -1;
                            else
                                signal = 1;
                            newsc.y = ec.y + (1 * signal);
                            newec.y = ec.y + (11 * signal);
                        }else{
                            //playerEndCoord is closer to sc
                            println("playerEndCoord is closer to sc "+sc);
                            if (sc.y < 0)
                                signal = -1;
                            else
                                signal = 1;
                            newsc.y = sc.y + (1 * signal);
                            newec.y = sc.y + (11 * signal);
                        }
                        coordBuildColumn = new Coord(sc.x+1, newec.y);
                        println("coordBuildColumn xdir "+coordBuildColumn);
                    }
                    else{
                        // tunnel direction = Y
                        println("tunnel on Y="+sc.y);
                        newsc = new Coord(0, sc.y);
                        newec = new Coord(0, sc.y);
                        // extend coord that is closer to playerEndCoord
                        if( Math.abs(playerec.x - ec.x) < Math.abs(playerec.x - sc.x) ){
                            //playerEndCoord is closer to ec
                            println("playerEndCoord is closer to ec "+ec);
                            if (ec.x < 0)
                                signal = -1;
                            else
                                signal = 1;
                            newsc.x = ec.x + (1 * signal);
                            newec.x = ec.x + (11 * signal);
                        }else{
                            //playerEndCoord is closer to sc
                            println("playerEndCoord is closer to sc "+sc);
                            if (sc.x < 0)
                                signal = -1;
                            else
                                signal = 1;
                            newsc.x = sc.x + (1 * signal);
                            newec.x = sc.x + (11 * signal);
                        }
                        coordBuildColumn = new Coord(sc.y+1, newec.x);
                        println("coordBuildColumn ydir "+coordBuildColumn);
                    }

                    /*
                        mine tunnel
                     */
                    println("new tunnel  =  newsc"+newsc+"  newec"+newec);
                    ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);
                    waitCursor(ZeeConfig.CURSOR_MINE);
                    ZeeConfig.gameUI.map.wdgmsg("sel", newsc, newec, ZeeConfig.savedTileSelModflags);
                    waitPlayerIdleFor(2);

                    /*
                        mine stonecolumn 1x1 tile
                     */
                    ZeeConfig.gameUI.map.wdgmsg("sel", coordBuildColumn, coordBuildColumn, ZeeConfig.savedTileSelModflags);
                    waitPlayerIdleFor(2);


                    /*
                        build stonecolumn
                     */
                    //haven.MenuGrid@6ce8cd1d ; act ; [bp, column, 0, (-1000024, -1019956)]
                    //haven.MapView@620bad61 ; place ; [(-1000960, -1017344), -32768, 1, 0]
                }
                else{
                    println("    no column next tile (dist="+dist+")");
                }
            }
            else {
                println("    column null");
            }
            println("  >task make_tunnel off");
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
    }

    private void taskChipBoulder() throws Exception {
        println(">task chip_boulder on");
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
        println(">task chip_boulder off");
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
                new ZeeMiningManager(TASK_CHIP_BOULDER, gob).start();
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
        //stopMining();
    }

    public static void stopMining() {
        println("stop mining");
        try {
            ZeeConfig.gameUI.msg("stop mining");
            ZeeConfig.clickRemoveCursor();
            Thread.sleep(PING_MS+100);
            ZeeConfig.clickGroundZero(1);//click ground
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
            ZeeConfig.clickRemoveCursor();//remove mining cursor
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

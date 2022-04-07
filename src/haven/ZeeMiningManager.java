package haven;

public class ZeeMiningManager extends ZeeThread{

    public static final String TASK_CHIP_BOULDER = "TASK_CHIP_BOULDER";
    public static final String TASK_MINE_AREA = "TASK_MINE_AREA";
    public static final String DIR_NORTH = "DIR_NORTH";
    public static final String DIR_SOUTH = "DIR_SOUTH";
    public static final String DIR_WEST = "DIR_WEST";
    public static final String DIR_EAST = "DIR_EAST";
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
    static MCache.Overlay ol;
    static Coord areasize = new Coord(1,1);
    static String lastDir = "";
    static Coord upperLeft;

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
            if (task.contentEquals(TASK_CHIP_BOULDER))
                taskChipBoulder();
            else if (task.contentEquals(TASK_MINE_AREA))
                taskMineArea();
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeClickGobManager.resetClickPetal();
        println(">mining manager off");
    }

    private void taskMineArea() throws Exception{
        if (ol==null || ol.a.sz().x<1 || ol.a.sz().y<1) {
            println("area invalid");
            return;
        }
        ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);
        waitCursor(ZeeConfig.CURSOR_MINE);
        /*
            areasize (1, 3)   dir=DIR_NORTH
            ol.a.ul(-927, -1001)  ol.a.br(-926, -998)
            haven.MapView@2124b275 ; sel ; [(-927, -1001), (-927, -1003), 0] (DIG BUTTON)
            haven.MapView@2124b275 ; sel ; [(-927, -999), (-927, -1001), 0]
            ===============
            areasize (1, 3)   dir=DIR_SOUTH
            ol.a.ul(-927, -999)  ol.a.br(-926, -996)
            haven.MapView@24c17ef8 ; sel ; [(-927, -999), (-927, -1001), 0] (DIG BUTTON)
            haven.MapView@24c17ef8 ; sel ; [(-927, -999), (-927, -997), 0]
            ===============
            areasize (3, 1)   dir=DIR_WEST
            ol.a.ul(-929, -999)  ol.a.br(-926, -998)
            haven.MapView@1e8d5a05 ; sel ; [(-929, -999), (-931, -999), 0] (DIG BUTTON)
            haven.MapView@1e8d5a05 ; sel ; [(-927, -999), (-929, -999), 0]
            ===============
            areasize (3, 1)   dir=DIR_EAST
            ol.a.ul(-1027, -999)  ol.a.br(-1024, -998)
            haven.MapView@2001b699 ; sel ; [(-1027, -999), (-1029, -999), 0] (DIG BUTTON)
            haven.MapView@2001b699 ; sel ; [(-1027, -999), (-1025, -999), 0]
         */
        Coord c1=null,c2=null;
        Coord areasub1 = areasize.sub(1,1);
        if(lastDir.contentEquals(DIR_NORTH)) {
            c1 = upperLeft.add(areasub1);
            c2 = upperLeft;
        }else if(lastDir.contentEquals(DIR_SOUTH)) {
            c1 = upperLeft;
            c2 = upperLeft.add(areasub1);
        }else if(lastDir.contentEquals(DIR_WEST)) {
            c1 = upperLeft.add(areasub1);
            c2 = upperLeft;
        }else if(lastDir.contentEquals(DIR_EAST)) {
            c1 = upperLeft;
            c2 = upperLeft.add(areasub1);
        }
        println("ol.a.ul"+ol.a.ul+"  ol.a.br"+ol.a.br);
        if (c1!=null && c2!=null) {
            println("mine coords  "+c1+"  "+c2);
            ZeeConfig.gameUI.map.wdgmsg("sel", c1, c2, 0);
        }else {
            println("mine coords null");
        }
        //ZeeConfig.gameUI.map.wdgmsg("sel", upperLeft, upperLeft.sub(areasize.sub(1,1)), 0);
        waitPlayerIdleFor(2);

        ol.destroy();
        ol=null;
        /*
            test overlay
            sleep(3000);
            ol = ZeeConfig.glob.map.new Overlay(Area.sized(upperLeft, areasize), MapView.selol);
         */
    }


    private static void changeAreasize(String dir) {

        //println(dir + "  rc"+ZeeConfig.getPlayerGob().rc+"  coord"+ZeeConfig.getPlayerCoord()+"  tile"+ZeeConfig.getPlayerTile());

        if (!dir.contentEquals(lastDir))
            areasize = new Coord(1,1);
        if(dir.contentEquals(DIR_NORTH))
            areasize = areasize.add(0,1);
        else if(dir.contentEquals(DIR_SOUTH))
            areasize = areasize.add(0,1);
        else if(dir.contentEquals(DIR_WEST))
            areasize = areasize.add(1,0);
        else if(dir.contentEquals(DIR_EAST))
            areasize = areasize.add(1,0);

        lastDir = dir;

        Gob column = ZeeConfig.getClosestGobName("gfx/terobjs/column");
        if (column==null) {
            ZeeConfig.msg("player must be next to stonecolumn");
        }else{
            int dist = (int) ZeeConfig.distanceToPlayer(column);
            ZeeConfig.addGobText(column, "dist " + dist);
            if (dist > 12) {
                ZeeConfig.msg("player must be next to stonecolumn");
            }else{
                //mc.div(MCache.tilesz2);
                upperLeft = ZeeConfig.coordToTile(ZeeConfig.getPlayerGob().rc);

                if (dir.contentEquals(DIR_NORTH))
                    upperLeft.y -= Math.abs(areasize.y-1);
                else if (dir.contentEquals(DIR_WEST))
                    upperLeft.x -= Math.abs(areasize.x-1);

                println("upperLeft "+upperLeft);
                if(ol==null) {
                    ol = ZeeConfig.glob.map.new Overlay(Area.sized(upperLeft, areasize), MapView.selol);
                }else{
                    /*
                    Coord tc = mc.div(MCache.tilesz2);
                    Coord c1 = new Coord(Math.min(tc.x, sc.x), Math.min(tc.y, sc.y));
                    Coord c2 = new Coord(Math.max(tc.x, sc.x), Math.max(tc.y, sc.y));
                    ol.update(new Area(c1, c2.add(1, 1)));
                     */
                    areasize = new Coord(Math.abs(areasize.x),Math.abs(areasize.y));
                    ol.update(Area.sized(upperLeft, areasize));
                }
                println("areasize "+areasize+"   dir="+dir);
            }
        }
    }

    public static void showWindowTunnel() {
        Widget wdg;
        if(windowManager ==null) {
            windowManager = new ZeeWindow(new Coord(200, 90), "Mine manager") {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("close")) {
                        busy = false;
                        exitManager();
                    }else
                        super.wdgmsg(msg, args);
                }
            };
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(30),"N"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        changeAreasize(DIR_NORTH);
                    }
                }
            }, 35,5);
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(30),"W"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        changeAreasize(DIR_WEST);
                    }
                }
            }, 5,30);
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(30),"E"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        changeAreasize(DIR_EAST);
                    }
                }
            }, 65,30);
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(30),"S"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        changeAreasize(DIR_SOUTH);
                    }
                }
            }, 35,60);
            wdg = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(60),"dig"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        new ZeeMiningManager(TASK_MINE_AREA).start();
                    }
                }
            }, 120,5);
            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));
        }else{
            windowManager.show();
        }
        changeAreasize("");
    }

    public static void exitManager(String msg) {
        exitManager();
        println(msg);
    }
    public static void exitManager() {
        busy = false;
        ZeeConfig.clickGroundZero(1);
        windowManager.hide();
        if (ol!=null) {
            ol.destroy();
            ol = null;
        }
        areasize = new Coord(1,1);
        lastDir = "";
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

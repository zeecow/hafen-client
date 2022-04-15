package haven;

import java.util.LinkedList;
import java.util.List;

public class ZeeMiningManager extends ZeeThread{

    public static final String TASK_CHIP_BOULDER = "TASK_CHIP_BOULDER";
    public static final String TASK_MINE_AREA = "TASK_MINE_AREA";
    public static final String TASK_TEST = "TASK_TEST";
    public static final String DIR_NORTH = "DIR_NORTH";
    public static final String DIR_SOUTH = "DIR_SOUTH";
    public static final String DIR_WEST = "DIR_WEST";
    public static final String DIR_EAST = "DIR_EAST";
    private static final long MS_CURSOR_CHANGE = 200;
    private static final double DIST_BOULDER = 25;
    static boolean debug = false;
    private final String task;
    public static long lastDropItemMs = 0;
    public static boolean mining;
    public static boolean isChipBoulder;
    public static Gob gobBoulder;
    public static ZeeMiningManager manager;
    static ZeeWindow windowManager;
    static ZeeWindow.ZeeButton btnNorth, btnSouth, btnWest, btnEast, btnDig;
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
        manager = this;
        try {
            if (task.contentEquals(TASK_CHIP_BOULDER))
                taskChipBoulder();
            else if (task.contentEquals(TASK_MINE_AREA))
                taskMineArea();
            else if (task.contentEquals(TASK_TEST))
                taskTest();
        }catch (Exception e){
            e.printStackTrace();
            stopMining();
        }
        ZeeClickGobManager.resetClickPetal();
    }

    private static boolean showTestBtn = false;//change to show/hide button
    private void taskTest() throws Exception{

        mining=true;
        pickStones(30);
        mining=false;

    }

    public static void highlightTiles(Coord topleft, int areasize){
        highlightTiles(topleft, Coord.of(areasize));
    }

    public static void highlightTiles(Coord topleft, Coord areasize){
        if(ol==null) {
            ol = ZeeConfig.glob.map.new Overlay(Area.sized(topleft, areasize), MapView.selol);
        }else{
            /*
            Coord tc = mc.div(MCache.tilesz2);
            Coord c1 = new Coord(Math.min(tc.x, sc.x), Math.min(tc.y, sc.y));
            Coord c2 = new Coord(Math.max(tc.x, sc.x), Math.max(tc.y, sc.y));
            ol.update(new Area(c1, c2.add(1, 1)));
             */
            areasize = new Coord(Math.abs(areasize.x),Math.abs(areasize.y));
            ol.update(Area.sized(topleft, areasize));
        }
    }

    private boolean taskMineArea() throws Exception{

        if (ol==null || ol.a.sz().x<1 || ol.a.sz().y<1) {
            println("area invalid");
            return false;
        }

        ZeeConfig.autoChipMinedBoulder = false; //use specific boulder code

        Inventory inv = ZeeConfig.getMainInventory();

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
        if (c1==null || c2==null) {
            return exitManager("mine coords null");
        }
        debug("upperleft"+upperLeft+"  c1"+c1+"  c2"+c2+"  areasize"+areasize);
        //debug("ol.a.ul"+ol.a.ul+"  ol.a.br"+ol.a.br);


        /*
        mine until reach tile c2
         */
        mining = true;
        ZeeConfig.addPlayerText("dig");
        disableBtns(true);
        ZeeConfig.clickTile(c1,1);//start at c1
        mineTiles(c1,c2);
        if (!mining)
            return exitManager("mining canceled 2");
        Gob boulder = getBoulderCloseEnoughForChipping();
        //chip close boulder(s)
        while (boulder!=null) {
            ZeeConfig.addPlayerText("boulder");
            if (!mining)
                return exitManager("mining canceled 2.1");
            if (!chipBoulder(boulder)){
                return exitManager("couldn't chip boulder");
            }
            if (!mining)
                return exitManager("mining canceled 2.2");
            //resume mining
            ZeeConfig.addPlayerText("dig");
            if (!mining)
                return exitManager("mining canceled 2.3");
            mineTiles(c1,c2);
            if (!mining)
                return exitManager("mining canceled 2.4");
            boulder = getBoulderCloseEnoughForChipping();
        }


        /*
        mine column tile
         */
        ZeeConfig.addPlayerText("col");
        ZeeConfig.clickRemoveCursor();
        waitCursor(ZeeConfig.CURSOR_ARW);
        if (!mining)
            return exitManager("mining canceled 3");
        //ZeeConfig.clickCoord(ZeeConfig.tileToCoord(c2),1);
        ZeeConfig.clickTile(c2,1);//move to end tile
        if (!mining)
            return exitManager("mining canceled 3.1");
        waitPlayerIdleFor(1);
        if (!mining)
            return exitManager("mining canceled 3.2");
        Coord tileNewCol;
        if (miningVertical())
            tileNewCol = c2.add(1,0);
        else if (miningHorizontal())
            tileNewCol = c2.add(0,1);
        else
            return exitManager("no tile for new column");
        debug("new col tile "+tileNewCol);
        if (!mining)
            return exitManager("mining canceled 3.3");
        mineTiles(tileNewCol,tileNewCol);
        if (!mining)
            return exitManager("mining canceled 3.4");
        //possible newcoltile boulder
        Gob b = getBoulderCloseEnoughForChipping();
        if (b!=null){
            ZeeConfig.addPlayerText("boulder");
            if(!chipBoulder(b)){
                return exitManager("couldn't chip boulder at newcoltile");
            }
        }


        /*
        get stones
         */
        //realign player (caused by boulder on tileNewCol)
        ZeeConfig.addPlayerText("stones");
        ZeeConfig.clickTile(c2,1);
        waitPlayerIdleFor(1);
        if (!pickStones(30)) {
            return exitManager("not enough stones for new column");
        }


        /*
        check metal
         */
        if (inv.countItemsByName("/bar-bronze")==0 && inv.countItemsByName("/bar-castiron")==0 && inv.countItemsByName("/bar-wroughtiron")==0 ){
            return exitManager("no hard-metal for new column");
        }


        /*
        build column
         */
        ZeeConfig.addPlayerText("col");
        ZeeConfig.clickTile(c2,1);//move to end tile
        waitPlayerIdleFor(1);
        ZeeConfig.gameUI.menu.wdgmsg("act","bp","column","0");
        sleep(1000);
        if (!mining)
            return exitManager("mining canceled 4");
        ZeeConfig.gameUI.map.wdgmsg("place",ZeeConfig.tileToCoord(tileNewCol),0,1,0);
        sleep(1000);
        if (!mining)
            return exitManager("mining canceled 4.1");
        Window colWin = ZeeConfig.getWindow("Stone column");
        if (colWin==null)
            return exitManager("no window for new column");
        Button buildBtn = ZeeConfig.getButtonNamed(colWin,"Build");
        if (buildBtn==null)
            return exitManager("no build button for new column");
        buildBtn.click();
        if (!mining)
            return exitManager("mining canceled 4.2");
        waitInvFreeSlotsIdle();
        ZeeConfig.clickTile(c2,1);//realign to c2
        waitPlayerIdleFor(1);
        inv.children(WItem.class).forEach(wItem -> {//drop remaining inv stones
            if (ZeeConfig.mineablesStone.contains(wItem.item.getres().basename()))
                wItem.item.wdgmsg("drop", Coord.z);
        });
        colWin = ZeeConfig.getWindow("Stone column");
        if (colWin!=null)
            return exitManager("no mats for new column");


        /*
        update overlay for next tiles
         */
        upperLeft = ZeeConfig.coordToTile(ZeeConfig.getPlayerGob().rc);
        debug("upperLeft before"+upperLeft);
        if (lastDir.contentEquals(DIR_NORTH))
            upperLeft.y -= Math.abs(areasize.y)-1;
        else if (lastDir.contentEquals(DIR_WEST))
            upperLeft.x -= Math.abs(areasize.x)-1;
        debug("upperLeft after"+upperLeft);
        highlightTiles(upperLeft,areasize);


        /*
        finish
         */
        mining = false;
        ZeeConfig.autoChipMinedBoulder = Utils.getprefb("autoChipMinedBoulder", true);
        ZeeConfig.removePlayerText();
        disableBtns(false);

        return true;
    }

    public static Gob getBoulderCloseEnoughForChipping() {
        Gob boulder = ZeeConfig.getClosestGobName("gfx/terobjs/bumlings/");
        if (boulder!=null && ZeeConfig.distanceToPlayer(boulder) < DIST_BOULDER){
            return boulder;
        }
        return null;
    }


    public static void mineTiles(Coord c1, Coord c2) {
        mining = true;
        debug("mine coords  c1"+c1+"  c2"+c2);
        ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);
        waitCursor(ZeeConfig.CURSOR_MINE);
        ZeeConfig.gameUI.map.wdgmsg("sel", c1, c2, 0);
        waitPlayerIdleFor(2);//minimum 2 sec
    }


    private boolean miningVertical() {
        return lastDir.contentEquals(DIR_NORTH) || lastDir.contentEquals(DIR_SOUTH);
    }


    private boolean miningHorizontal() {
        return lastDir.contentEquals(DIR_WEST) || lastDir.contentEquals(DIR_EAST);
    }


    public static boolean pickStones(int wantedStones) throws Exception{
        List<Gob> terobjs;
        Gob closestStone;
        Inventory inv = ZeeConfig.getMainInventory();
        List<WItem> invItems = new LinkedList<>(inv.children(WItem.class));
        invItems.removeIf(wItem -> !ZeeConfig.mineablesStone.contains(wItem.item.getres().basename()));
        if (invItems.size() >= wantedStones)
            return true;
        if(inv.getNumberOfFreeSlots() < wantedStones){
            WItem sack = ZeeClickItemManager.getSackFromBelt();//1st sack
            if (sack!=null) {
                Thread t = new ZeeClickItemManager(sack);
                t.start();
                t.join();//wait equip sack
                sack = ZeeClickItemManager.getSackFromBelt();//2nd sack
                if(inv.getNumberOfFreeSlots()<wantedStones && sack!=null){
                    t = new ZeeClickItemManager(sack);
                    t.start();
                    t.join();//wait equip 2nd sack
                }
            }
            waitNotHoldingItem();
        }
        int invStones = invItems.size();
        while (mining  &&  invStones<wantedStones && inv.getNumberOfFreeSlots()!=0) {
            terobjs = ZeeConfig.findGobsByNameContains("gfx/terobjs/items/");
            terobjs.removeIf(item -> !ZeeConfig.mineablesStone.contains(item.getres().basename()));//remove non-stones
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
            ZeeClickGobManager.gobClick(closestStone,3,UI.MOD_SHIFT);//pick all
            if(!waitInvIdleMs(1000)){
                return exitManager("couldn't reach stone (timeout)");
            }
            invStones += inv.countItemsByName(closestStone.getres().basename());
            //println("loop > invStones "+invStones);
        }
        if (ZeeConfig.isPlayerHoldingItem()){//drop if holding stone
            if (ZeeConfig.mineablesStone.contains(ZeeConfig.gameUI.vhand.item.getres().basename())){
                ZeeConfig.gameUI.vhand.item.wdgmsg("drop", Coord.z);
                waitNotHoldingItem();
            }
        }
        debug("got "+invStones+" stones ,  enough="+(invStones >= wantedStones) +" , invfull="+ (inv.getNumberOfFreeSlots()==0));
        return invStones >= wantedStones || inv.getNumberOfFreeSlots()==0;
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
                upperLeft = ZeeConfig.coordToTile(ZeeConfig.getPlayerGob().rc);

                if (dir.contentEquals(DIR_NORTH))
                    upperLeft.y -= Math.abs(areasize.y-1);
                else if (dir.contentEquals(DIR_WEST))
                    upperLeft.x -= Math.abs(areasize.x-1);

                //println("upperLeft "+upperLeft);
                highlightTiles(upperLeft,areasize);
                //println("areasize "+areasize+"   dir="+dir);
            }
        }
    }

    public static void showWindowMining() {
        if(windowManager ==null) {
            windowManager = new ZeeWindow(new Coord(200, 90), "Mine manager") {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("close")) {
                        exitManager();
                    }else
                        super.wdgmsg(msg, args);
                }
            };
            btnNorth = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(30),"N"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        changeAreasize(DIR_NORTH);
                    }
                }
            }, 35,5);
            btnWest = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(30),"W"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        changeAreasize(DIR_WEST);
                    }
                }
            }, 5,30);
            btnEast = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(30),"E"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        changeAreasize(DIR_EAST);
                    }
                }
            }, 65,30);
            btnSouth = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(30),"S"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        changeAreasize(DIR_SOUTH);
                    }
                }
            }, 35,60);
            btnDig = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(60),"dig"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        new ZeeMiningManager(TASK_MINE_AREA).start();
                    }
                }
            }, 120,5);
            if (showTestBtn) {
                windowManager.add(new ZeeWindow.ZeeButton(UI.scale(80), "test") {
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.equals("activate")) {
                            new ZeeMiningManager(TASK_TEST).start();
                        }
                    }
                }, 120, 35);
            }
            windowManager.add(new CheckBox("debug"){
                public void changed(boolean val) {
                    debug = val;
                }
            },145,80);
            ZeeConfig.gameUI.add(windowManager, new Coord(100,100));
        }else{
            windowManager.show();
            disableBtns(false);
        }
        changeAreasize("");
    }

    public static void disableBtns(boolean dis){
        btnDig.disable(dis);
        btnSouth.disable(dis);
        btnEast.disable(dis);
        btnWest.disable(dis);
        btnNorth.disable(dis);
    }

    public static boolean exitManager(String msg) {
        println(msg);
        return exitManager();
    }
    public static boolean exitManager() {
        mining = false;
        ZeeConfig.autoChipMinedBoulder = Utils.getprefb("autoChipMinedBoulder", true);
        ZeeConfig.clickGroundZero(1);
        windowManager.hide();
        if (ol!=null) {
            ol.destroy();
            ol = null;
        }
        areasize = new Coord(1,1);
        lastDir = "";
        ZeeConfig.removePlayerText();
        return false;
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
                    isChipBoulder = false;
                    println("stop mining");
                    ZeeConfig.gameUI.msg("stop mining");
                    ZeeConfig.clickRemoveCursor();
                    waitCursor(ZeeConfig.CURSOR_ARW);
                    ZeeConfig.clickGroundZero(1);//click ground to stop mining?
                    ZeeThread.staminaMonitorStop();//case stam monitor thread is running
                    manager.interrupt();
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
            ZeeClickGobManager.clickGobPetal(boulder, "Chip stone");//chip boulder
            waitNoFlowerMenu();
            ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);//restore mining icon for autodrop
            return waitBoulderFinish(boulder);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
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

    public static void debug(String s){
        if (debug)
            println(s);
    }
}

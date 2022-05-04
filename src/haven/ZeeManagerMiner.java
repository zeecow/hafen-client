package haven;

import java.util.LinkedList;
import java.util.List;

public class ZeeManagerMiner extends ZeeThread{

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
    static boolean repeatTaskMineArea = false;
    static boolean useOreForColumns = false;
    private final String task;
    public static long lastDropItemMs = 0;
    public static boolean mining;
    public static boolean isChipBoulder;
    public static Gob gobBoulder;
    public static ZeeManagerMiner manager;
    static ZeeWindow windowManager;
    static ZeeWindow.ZeeButton btnNorth, btnSouth, btnWest, btnEast, btnDig, btnTest;
    static MCache.Overlay ol;
    static Coord areasize = new Coord(1,1);
    static String lastDir = "";
    static Coord upperLeft;

    public ZeeManagerMiner(String task){
        this.task = task;
    }

    public ZeeManagerMiner(String task, Gob gob){
        this.task = task;
        this.gobBoulder = gob;
    }

    @Override
    public void run() {
        manager = this;
        try {
            if (task.contentEquals(TASK_CHIP_BOULDER))
                taskChipBoulder();
            else if (task.contentEquals(TASK_MINE_AREA)) {
                do {
                    taskMineArea();
                } while(repeatTaskMineArea);
            }else if (task.contentEquals(TASK_TEST))
                taskTest();
        }catch (Exception e){
            e.printStackTrace();
            stopMining();
        }
    }

    private static boolean showTestBtn = false;//change to show/hide button
    private void taskTest() throws Exception{
        println("playertile"+ZeeConfig.getPlayerTile()+"  upperLeft"+upperLeft+"  areasize"+areasize);
        Coord c1=null,c2=null;
        Coord areasub1 = areasize.sub(1,1);
        boolean positive = upperLeft.x >= 0;
        debug("positive="+positive);
        /*
            DIR_WEST
            playertile(1020, 1022)  upperLeft(1017, 1022)  areasize(4, 1)
            ol.a.ul(1017, 1022)  ol.a.br(1021, 1023)
            mine tiles  c1(1018, 1023)  c2(1021, 1023)
            haven.MapView@5050cddf ; sel ; [(1018, 1023), (1021, 1023), 0] (TEST BTN)
            haven.MapView@5050cddf ; sel ; [(1020, 1022), (1017, 1022), 0]
            =======
            DIR_SOUTH
            playertile(1019, 1021)  upperLeft(1019, 1021)  areasize(1, 4)
            ol.a.ul(1019, 1021)  ol.a.br(1020, 1025)
            mine tiles  c1(1020, 1025)  c2(1020, 1022)
            haven.MapView@5050cddf ; sel ; [(1020, 1025), (1020, 1022), 0] (TEST BTN)
            haven.MapView@5050cddf ; sel ; [(1019, 1021), (1019, 1024), 0]
         */
        if(lastDir.contentEquals(DIR_NORTH) || lastDir.contentEquals(DIR_WEST)) {
            c1 = positive? ol.a.ul.add(areasub1) : upperLeft.add(areasub1);
            c2 = positive? ol.a.ul : upperLeft;
        }else if(lastDir.contentEquals(DIR_SOUTH) || lastDir.contentEquals(DIR_EAST)) {
            c1 = positive? ol.a.ul : upperLeft;
            c2 = positive? ol.a.ul.add(areasub1) : upperLeft.add(areasub1);
        }
        Coord startTile = ZeeConfig.getTileCloserToPlayer(c1,c2);
        Coord endTile = ZeeConfig.getTileFartherToPlayer(c1,c2);
        debug("upperleft"+upperLeft+"  c1"+c1+"  c2"+c2+"  areasize"+areasize);
        debug("ol.a.ul"+ol.a.ul+"  ol.a.br"+ol.a.br);
        mineTiles(c1,c2);
        ZeeConfig.clickTile(endTile,1);//TODO mineOverlayArea(ol) and overlayMineArea(c1,c2)


        /*
            update overlay for next tiles
         */
        upperLeft = endTile;
        debug("upperLeft before"+upperLeft);
        if (positive) {
            if (lastDir.contentEquals(DIR_NORTH) || lastDir.contentEquals(DIR_WEST))
                upperLeft = ol.a.ul.sub(areasub1);
            else if(lastDir.contentEquals(DIR_SOUTH) || lastDir.contentEquals(DIR_EAST))
                upperLeft = ol.a.ul.add(areasub1);
        } else {
            if (lastDir.contentEquals(DIR_NORTH))
                upperLeft.y -= Math.abs(areasize.y)-1;
            else if (lastDir.contentEquals(DIR_WEST))
                upperLeft.x -= Math.abs(areasize.x)-1;
        }
        debug("upperLeft after"+upperLeft);
        highlightTiles(upperLeft,areasize);
    }


    public static void mineTiles(Coord c1, Coord c2) throws InterruptedException{
        mining = true;
        debug("mine tiles  c1"+c1+"  c2"+c2);
        ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);
        waitCursor(ZeeConfig.CURSOR_MINE);
        ZeeConfig.gameUI.map.wdgmsg("sel", c1, c2, 0);
        //waitPlayerIdleFor(2);//minimum 2 sec
        waitPlayerPoseIdle();
        ZeeConfig.clickRemoveCursor();
        waitCursor(ZeeConfig.CURSOR_ARW);
    }


    public static void highlightTiles(Coord topleft, Coord areasize){
        debug("highlightTiles >  topleft"+topleft+"  areasize"+areasize);
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

    private void taskMineArea() throws Exception{

        if (ol==null || ol.a.sz().x<1 || ol.a.sz().y<1) {
            println("area invalid");
            return;
        }

        ZeeConfig.autoChipMinedBoulder = false; //use specific boulder code

        Inventory inv = ZeeConfig.getMainInventory();

        Coord c1=null, c2=null, startTile, endTile;
        Coord areasub1 = areasize.sub(1,1);
        boolean positive = upperLeft.x >= 0;
        if (positive)
            ZeeConfig.msg("Coords > 0");
        if(lastDir.contentEquals(DIR_NORTH) || lastDir.contentEquals(DIR_WEST)) {
            c1 = positive? upperLeft.add(1,1) : upperLeft.add(areasub1);
            c2 = positive? upperLeft.add(areasize) : upperLeft;
        }else if(lastDir.contentEquals(DIR_SOUTH) || lastDir.contentEquals(DIR_EAST)) {
            c1 = positive? upperLeft.add(areasize) : upperLeft;
            c2 = positive? upperLeft.add(1,1) : upperLeft.add(areasub1);
        }
        if (c1==null || c2==null) {
            exitManager("mine coords null");
            return;
        }
        startTile = ZeeConfig.getTileCloserToPlayer(c1,c2);
        endTile = ZeeConfig.getTileFartherToPlayer(c1,c2);
        debug("upperleft"+upperLeft+"  c1"+c1+"  c2"+c2+"  areasize"+areasize);
        debug("startTile"+startTile+"  endTile"+endTile);


        /*
        mine until reach tile c2
         */
        mining = true;
        ZeeConfig.addPlayerText("dig");
        disableBtns(true);
        debug("click startTile "+startTile);
        ZeeConfig.clickTile(startTile,1);//start at c1
        mineTiles(c1,c2);
        if (!mining) {
            exitManager("mining canceled 2");
            return;
        }
        Gob boulder = getBoulderCloseEnoughForChipping();
        //chip close boulder(s)
        while (boulder!=null) {
            ZeeConfig.addPlayerText("boulder");
            if (!mining) {
                exitManager("mining canceled 2.1");
                return;
            }
            if (!chipBoulder(boulder)){
                exitManager("couldn't chip boulder");
                return;
            }
            if (!mining) {
                exitManager("mining canceled 2.2");
                return;
            }
            //resume mining
            ZeeConfig.addPlayerText("dig");
            if (!mining) {
                exitManager("mining canceled 2.3");
                return;
            }
            mineTiles(c1,c2);
            if (!mining) {
                exitManager("mining canceled 2.4");
                return;
            }
            boulder = getBoulderCloseEnoughForChipping();
        }


        /*
        mine column tile
         */
        ZeeConfig.addPlayerText("col");
        ZeeConfig.clickRemoveCursor();
        waitCursor(ZeeConfig.CURSOR_ARW);
        if (!mining) {
            exitManager("mining canceled 3");
            return;
        }
        ZeeConfig.clickTile(endTile,1);
        if (!mining) {
            exitManager("mining canceled 3.1");
            return;
        }
        //waitPlayerIdleFor(1);
        waitPlayerPoseIdle();
        if (!mining) {
            exitManager("mining canceled 3.2");
            return;
        }
        Coord tileNewCol;
        if (miningVertical())
            tileNewCol = endTile.add(1,0);
        else if (miningHorizontal())
            tileNewCol = endTile.add(0,1);
        else {
            exitManager("no tile for new column");
            return;
        }
        debug("new col tile "+tileNewCol);
        if (!mining) {
            exitManager("mining canceled 3.3");
            return;
        }
        mineTiles(tileNewCol,tileNewCol);
        if (!mining) {
            exitManager("mining canceled 3.4");
            return;
        }
        //possible newcoltile boulder
        Gob b = getBoulderCloseEnoughForChipping();
        if (b!=null){
            ZeeConfig.addPlayerText("boulder");
            if(!chipBoulder(b)){
                exitManager("couldn't chip boulder at newcoltile");
                return;
            }
        }


        /*
        get stones
         */
        //realign player (caused by boulder on tileNewCol)
        ZeeConfig.addPlayerText("stones");
        ZeeConfig.clickTile(endTile,1);
        //waitPlayerIdleFor(1);
        waitPlayerPoseIdle();
        if (!pickStones(30)) {
            exitManager("not enough stones for new column");
            return;
        }


        /*
        check metal
         */
        if (inv.countItemsByName("/bar-bronze")==0 && inv.countItemsByName("/bar-castiron")==0 && inv.countItemsByName("/bar-wroughtiron")==0 ){
            exitManager("no hard-metal for new column");
            return;
        }


        /*
        build column
         */
        ZeeConfig.addPlayerText("col");
        ZeeConfig.clickTile(endTile,1);
        //waitPlayerIdleFor(1);
        waitPlayerPoseIdle();
        ZeeConfig.gameUI.menu.wdgmsg("act","bp","column","0");
        sleep(1000);
        if (!mining) {
            exitManager("mining canceled 4");
            return;
        }
        ZeeConfig.gameUI.map.wdgmsg("place",ZeeConfig.tileToCoord(tileNewCol),0,1,0);
        sleep(1000);
        if (!mining) {
            exitManager("mining canceled 4.1");
            return;
        }
        Window colWin = ZeeConfig.getWindow("Stone column");
        if (colWin==null) {
            exitManager("no window for new column");
            return;
        }
        Button buildBtn = ZeeConfig.getButtonNamed(colWin,"Build");
        if (buildBtn==null) {
            exitManager("no build button for new column");
            return;
        }
        buildBtn.click();
        if (!mining) {
            exitManager("mining canceled 4.2");
            return;
        }
        //waitInvFreeSlotsIdle();
        waitPlayerPoseIdle();
        ZeeConfig.clickTile(endTile,1);//realign to endTile
        //waitPlayerIdleFor(1);
        waitPlayerPoseIdle();
        inv.children(WItem.class).forEach(wItem -> {//drop remaining inv stones
            if (ZeeConfig.mineablesStone.contains(wItem.item.getres().basename()))
                wItem.item.wdgmsg("drop", Coord.z);
        });
        colWin = ZeeConfig.getWindow("Stone column");
        if (colWin!=null) {
            exitManager("no mats for new column");
            return;
        }


        /*
            update overlay for next tiles
         */
        upperLeft = endTile;
        debug("upperLeft before"+upperLeft);
        if (positive) {
            if (lastDir.contentEquals(DIR_NORTH) || lastDir.contentEquals(DIR_WEST))
                upperLeft = ol.a.ul.sub(areasub1);
            else if(lastDir.contentEquals(DIR_SOUTH) || lastDir.contentEquals(DIR_EAST))
                upperLeft = ol.a.ul.add(areasub1);
        } else {
            if (lastDir.contentEquals(DIR_NORTH))
                upperLeft.y -= Math.abs(areasize.y)-1;
            else if (lastDir.contentEquals(DIR_WEST))
                upperLeft.x -= Math.abs(areasize.x)-1;
        }
        debug("upperLeft after"+upperLeft);
        highlightTiles(upperLeft,areasize);


        /*
        finish
         */
        mining = false;
        ZeeConfig.autoChipMinedBoulder = Utils.getprefb("autoChipMinedBoulder", true);
        ZeeConfig.removePlayerText();
        disableBtns(false);

    }


    public static Gob getBoulderCloseEnoughForChipping() {
        Gob boulder = ZeeConfig.getClosestGobName("gfx/terobjs/bumlings/");
        if (boulder!=null && ZeeConfig.distanceToPlayer(boulder) < DIST_BOULDER){
            return boulder;
        }
        return null;
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

        //check if inventory stones are enough
        List<WItem> invItems = new LinkedList<>(inv.children(WItem.class));
        invItems.removeIf(item -> {
            String name = item.item.getres().basename();
            if (useOreForColumns && "leadglance,cassiterite,chalcopyrite,cinnabar,malachite".contains(name))
                return false;
            return !ZeeConfig.mineablesStone.contains(name);
        });
        if (invItems.size() >= wantedStones)
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
        int invStones = invItems.size();
        while (mining  &&  invStones<wantedStones && inv.getNumberOfFreeSlots()!=0) {
            terobjs = ZeeConfig.findGobsByNameContains("gfx/terobjs/items/");
            terobjs.removeIf(item -> {//filter column stone types
                String name = item.getres().basename();
                if (useOreForColumns && "leadglance,cassiterite,chalcopyrite,cinnabar,malachite".contains(name))
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

            //window
            windowManager = new ZeeWindow(new Coord(260, 110), "Mine manager") {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.equals("close")) {
                        exitManager();
                    }else
                        super.wdgmsg(msg, args);
                }
            };


            // buttons N, S, W, E
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


            //checkbox debug
            windowManager.add(new CheckBox("debug"){
                public void changed(boolean val) {
                    debug = val;
                }
            },5,85);


            //checkbox ore column
            windowManager.add(new CheckBox("ore column: lead, cass, chalco, cinna, mala"){
                public void changed(boolean val) {
                    useOreForColumns = val;
                }
            },5,100);


            //button dig
            btnDig = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(60),"dig"){
                public void wdgmsg(String msg, Object... args) {
                    if(msg.equals("activate")){
                        new ZeeManagerMiner(TASK_MINE_AREA).start();
                    }
                }
            }, 120,5);


            //checkbox repeat
            windowManager.add(new CheckBox("repeat"){
                public void changed(boolean val) {
                    repeatTaskMineArea = val;
                }
            },185,10);


            //button test
            if (showTestBtn) {
                btnTest = windowManager.add(new ZeeWindow.ZeeButton(UI.scale(80), "test") {
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.equals("activate")) {
                            new ZeeManagerMiner(TASK_TEST).start();
                        }
                    }
                }, 120, 35);
            }


            //add window
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
        if (btnTest!=null)
            btnTest.disable(dis);
    }

    public static boolean exitManager(String msg) {
        println(msg);
        return exitManager();
    }
    public static boolean exitManager() {
        mining = false;
        repeatTaskMineArea = false;
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
        ZeeConfig.addPlayerText("boulder");
        ZeeConfig.clickRemoveCursor();
        waitCursor(ZeeConfig.CURSOR_ARW);
        sleep(1000);//wait boulder clickable
        ZeeManagerGobClick.clickGobPetal(gobBoulder,"Chip stone");//chip boulder
        sleep(100);
        ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);//restore mining icon for autodrop
        if(waitPlayerPoseIdle()){ //waitBoulderFinish(gobBoulder)
            println("chip boulder done");
            ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.savedTileSelStartCoord, ZeeConfig.savedTileSelEndCoord, ZeeConfig.savedTileSelModflags);
        }else{
            println("canceled chipping boulder?");
        }
        println(">task chip_boulder off");
        ZeeConfig.removePlayerText();
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
                new ZeeManagerMiner(TASK_CHIP_BOULDER, gob).start();
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
            ZeeManagerGobClick.clickGobPetal(boulder, "Chip stone");//chip boulder
            waitNoFlowerMenu();
            ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);//restore mining icon for autodrop
            //return waitBoulderFinish(boulder);
            return waitPlayerPoseIdle();
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

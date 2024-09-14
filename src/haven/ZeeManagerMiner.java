package haven;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ZeeManagerMiner extends ZeeThread{

    private static final double MAX_DIST_BOULDER = 25;

    //static boolean isBuildAndDrinkBackup, restoreBuildAndDrink;
    public static long miningAreaSelectedMs = -1;
    static boolean useOreForColumns = false;
    public static long lastDropItemMs = 0;
    public static boolean mining;
    public static ZeeManagerMiner manager;
    static ZeeWindow tunnelHelperWindow;
    static String listOreColumn = "leadglance,cassiterite,chalcopyrite,cinnabar,malachite";

    public static void checkMiningSelection() {

        if (isChippingBoulder) {
            println("chipping boulder,  skip check selection");
            return;
        }

        // selection is not mining
        if (!isCursorMining()){
            miningAreaSelectedMs = -1;
            return;
        }

        // save mining start ms
        miningAreaSelectedMs = ZeeThread.now();

        // skip if mining on horse
        if (ZeeConfig.isPlayerMountingHorse()){
            ZeeConfig.msgError("TROLL RISK: mining on horse");
            return;
        }

        // show tile monitor if area bigger than 1 tile
        if (tilemonWindow==null && ZeeConfig.lastSavedOverlay.a.sz().compareTo(Coord.of(1)) != 0){
            tileMonitorWindow();
        }

        // if tunnel mining, show Helper Window
        if (tunnelCheckbox && !tunneling && isTunnel(ZeeConfig.lastSavedOverlay.a.sz())){
            tunneling = true;
            tunnelHelperShowWindow();
            tunnelHelperButtonAct.disable(true);
        }

        if(!tunneling)
            return;

        // wait player idle, if boulder close, chip it and resume mining
            new ZeeThread(){
                public void run() {
                    try {
                        if (isCombatActive())
                            return;

                        if (tunnelHelperStage == TUNNELHELPER_STAGE0_IDLE) {
                            tunnelHelperSetStage(TUNNELHELPER_STAGE1_MINETUNNEL);
                        }
                        else if (tunnelHelperStage == TUNNELHELPER_STAGE2_WAITNEWCOLTILE){
                            tunnelHelperSetStage(TUNNELHELPER_STAGE3_MINECOL);
                        }

                        // wait mining stop
                        waitPlayerPoseMs(ZeeConfig.POSE_PLAYER_IDLE, 1500);
                        if (!tunneling || isCombatActive())
                            return;

                        // save player coord after mining new col tile
                        if (tunnelHelperStage == TUNNELHELPER_STAGE3_MINECOL) {

                            println("save end coord ");
                            if (tunnelHelperEndCoord != null)
                                tunnelHelperEndCoordPrev = Coord.of(tunnelHelperEndCoord);
                            tunnelHelperEndCoord = ZeeConfig.getPlayerCoord();

                            println("wait mined tile boulder");
                            sleep(1000);
                            Gob boulder = ZeeConfig.getClosestGobToPlayer(getBoulders());
                            if (boulder!=null && ZeeConfig.distanceToPlayer(boulder) < TILE_SIZE*2){
                                chipBoulder(boulder);
                                ZeeConfig.clickCoord(tunnelHelperEndCoord,1);
                                waitPlayerIdlePose();
                            }

                            //pick stone
                            tunnelHelperSetStage(TUNNELHELPER_STAGE4_PICKSTONE);
                            tunnelHelperButtonAct.disable(false);
                            ZeeConfig.clickRemoveCursor();
                            waitCursorName(ZeeConfig.CURSOR_ARW);
                            if (!tunnelHelperPickStonesManualClick) {
                                tunnelHelperButtonAct.click();
                                tunnelHelperButtonAct.disable(true);
                            }
                        } else {
                            // finished mining tunnel
                            if (tunnelHelperStage == TUNNELHELPER_STAGE1_MINETUNNEL) {
                                tunnelHelperSetStage(TUNNELHELPER_STAGE2_WAITNEWCOLTILE);
                                ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);// prepare mining cursor
                                //ZeeConfig.addPlayerText("click new col tile");
                            }
                            // finished mining new col tile
                            else if (tunnelHelperStage == TUNNELHELPER_STAGE3_MINECOL) {
                                //pick stone
                                tunnelHelperSetStage(TUNNELHELPER_STAGE4_PICKSTONE);
                                //ZeeConfig.removePlayerText();
                                tunnelHelperButtonAct.disable(false);
                                ZeeConfig.clickRemoveCursor();
                                waitCursorName(ZeeConfig.CURSOR_ARW);
                                if (!tunnelHelperPickStonesManualClick) {
                                    tunnelHelperButtonAct.click();
                                    tunnelHelperButtonAct.disable(true);
                                }
                            }
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        ZeeConfig.removePlayerText();
                    }
                }
            }.start();
    }

    static boolean isCursorMining() {
        return ZeeConfig.getCursorName().contentEquals(ZeeConfig.CURSOR_MINE);
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

        // backup buildAndDrink
//        restoreBuildAndDrink = true;
//        isBuildAndDrinkBackup = ZeeConfig.isBuildAndDrink;
//        ZeeConfig.isBuildAndDrink = false;

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
        //println("stage " +tunnelHelperStage+ " to "+stage+" ("+name+")");
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
//        if (restoreBuildAndDrink) {
//            ZeeConfig.isBuildAndDrink = isBuildAndDrinkBackup;
//            restoreBuildAndDrink = false;
//        }
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
        if ( (sz.x==1 || sz.x==2) && sz.y>=minSize)
            return true;
        if ((sz.y==1 || sz.y==2) && sz.x>=minSize)
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
                waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_BUILD,ZeeConfig.POSE_PLAYER_DRINK);
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
        waitCursorName(ZeeConfig.CURSOR_MINE);
        ZeeConfig.gameUI.map.wdgmsg("sel", c1, c2, 0);
        //waitPlayerIdleFor(2);//minimum 2 sec
        waitPlayerIdlePose();
        ZeeConfig.clickRemoveCursor();
        waitCursorName(ZeeConfig.CURSOR_ARW);
    }


    static List<Gob> getBoulders(){
        List<Gob> boulders = ZeeConfig.findGobsByNameStartsWith("gfx/terobjs/bumlings/");
        boulders.removeIf(gob -> !ZeeManagerGobClick.isGobBoulder(gob.getres().name));//remove cave in boulders
        return boulders;
    }

    static Gob getBoulderCloseEnoughForChipping() {
        Gob boulder = ZeeConfig.getClosestGobToPlayer(getBoulders());
        if (boulder!=null && ZeeConfig.distanceToPlayer(boulder) < MAX_DIST_BOULDER){
            return boulder;
        }
        return null;
    }


    public static boolean pickStones(int numStonesWanted) throws Exception{
        List<Gob> terobjs;
        Gob closestStone;
        Inventory inv = ZeeConfig.getMainInventory();

        // set arrow cursor
        if (!ZeeConfig.CURSOR_ARW.contentEquals(ZeeConfig.getCursorName())) {
            ZeeConfig.clickRemoveCursor();
            waitCursorName(ZeeConfig.CURSOR_ARW);
        }

        //check if inventory stones are enough
        int invStones = pickStonesInvCount();
        if (invStones >= numStonesWanted) {
            println("invStones >= wantedStones");
            return true;
        }

        // if not enough stones, equip sack(s)
        int stackSlotsGuess = numStonesWanted/2;
        int freeSlotsRequired = ZeeConfig.autoStack ? stackSlotsGuess : numStonesWanted;
        if(inv.getNumberOfFreeSlots() < freeSlotsRequired){
            WItem sack = ZeeManagerItemClick.getSackFromBelt();//1st sack
            if (sack!=null) {
                Thread t = new ZeeManagerItemClick(sack);
                t.start();
                t.join();//wait equip sack
                sack = ZeeManagerItemClick.getSackFromBelt();//2nd sack
                if(inv.getNumberOfFreeSlots()<numStonesWanted && sack!=null){
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
        while (mining  &&  invStones<numStonesWanted && inv.getNumberOfFreeSlots()!=0) {
            terobjs = ZeeConfig.findGobsByNameContains("gfx/terobjs/items/");
            terobjs.removeIf(item -> {//filter column stone types
                String name = item.getres().basename();
                if (useOreForColumns && listOreColumn.contains(name))
                    return false;
                return !ZeeConfig.mineablesStone.contains(name);
            });
            if (terobjs.size() == 0)
                break; //no stones to pick, end loop
            closestStone = ZeeConfig.getClosestGobToPlayer(terobjs);
            if (closestStone == null)
                continue; // picked all stone type, try other stones
            if (ZeeConfig.distanceToPlayer(closestStone) > 11*TILE_SIZE){
                println("stones too far away");
                return false;
            }

            // start collecting current stone type
            println("picking "+closestStone.getres().basename());
            ZeeManagerGobClick.gobClick(closestStone,3,UI.MOD_SHIFT);//pick all

            // wait current stone type
            long invIdleMs = 500;
            boolean isInvIdle;
            do{
                sleep(555);
                invStones = pickStonesInvCount();
                if (invStones>=numStonesWanted || inv.getNumberOfFreeSlots()==0)
                    break;
                isInvIdle = (now() - ZeeConfig.lastInvGItemCreatedMs) > invIdleMs;
                if (isInvIdle && !ZeeConfig.isPlayerDrinkingOrLinMoving())
                    break;
            }while(mining);
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
        return invStones >= numStonesWanted || inv.getNumberOfFreeSlots()==0;
    }

    private static int pickStonesInvCount() {
        HashMap<String, Integer> mapItemCount = ZeeConfig.getMainInventory().getMapItemNameCount();
        int ret = 0;
        for (Map.Entry<String, Integer> entry : mapItemCount.entrySet()) {

            String basename = entry.getKey().replace("gfx/invobjs/","");

            if (useOreForColumns && listOreColumn.contains(basename))
                ret += entry.getValue();
            else if (ZeeConfig.mineablesStone.contains(basename))
                ret += entry.getValue();
        }
        //println("pickStonesInvCount > "+ret);
        return ret;
    }

    public static void stopMining() {
        new ZeeThread(){
            public void run() {
                try {
                    mining = false;
                    println("stop mining");
                    ZeeConfig.msgError("stop mining");
                    ZeeConfig.clickRemoveCursor();
                    waitCursorName(ZeeConfig.CURSOR_ARW);
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

    static boolean isChippingBoulder;
    static void chipBoulderAndResumeMining(Gob boulder) {
        isChippingBoulder = true;
        new ZeeThread(){
            public void run() {
                try {

                    chipBoulder(boulder);

                    //resume mining
                    ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.lastSavedOverlayStartCoord, ZeeConfig.lastSavedOverlayEndCoord, ZeeConfig.lastSavedOverlayModflags);

                }catch (Exception e){
                    e.printStackTrace();
                }

                isChippingBoulder = false;
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    static void chipBoulder(Gob boulder) {
        isChippingBoulder = true;
        try {
            ZeeConfig.addPlayerText("bouldan");

            //remove mining cursor
            ZeeConfig.clickRemoveCursor();
            if(!waitCursorName(ZeeConfig.CURSOR_ARW)){
                println(">chipBoulder couldn't change cursor to arrow");
                ZeeConfig.removePlayerText();
                return;
            }

            //chip boulder
            ZeeManagerGobClick.clickGobPetal(boulder, "Chip stone");
            waitNoFlowerMenu();

            //restore mining icon for autodrop
            ZeeConfig.cursorChange(ZeeConfig.ACT_MINE);

            //wait chipping
            waitPlayerIdlePose();

        }catch (Exception e){
            e.printStackTrace();
        }

        isChippingBoulder = false;
        ZeeConfig.removePlayerText();
    }

    // TODO delete if waitPlayerPoseIdle works
    private static boolean waitBoulderFinish(Gob boulder) {
        try {
            ZeeConfig.lastMapViewClickButton = 2;
            while (!ZeeConfig.isCancelClick() && ZeeManagerGobClick.findGobById(boulder.id) != null) {
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

    public static void checkBoulderGobAdded(Gob boulder) {

        // check mining cursor
        if (!isCursorMining())
            return;

        // new boulders only
        long boulderSpawnMs = ZeeThread.now();
        if (miningAreaSelectedMs < 0 || boulderSpawnMs < miningAreaSelectedMs)
            return;

        // discard distant boulders
        double dist = ZeeConfig.distanceToPlayer(boulder);
        if(dist > MAX_DIST_BOULDER)
            return;

        chipBoulderAndResumeMining(boulder);
    }

    static ZeeWindow tilemonWindow;
    static Scrollport tilemonScrollport;
    static boolean tilemonAutoRefresh = false;
    static ZeeThread tilemonAutoThread;
    static CheckBox tilemonAutoCheckbox;
    static Label tilemonLabelFindTile;
    static Coord tilemonCurPlayerTile, tilemonLastWindowRefreshPlayerTile, tilemonLastPreciousCoord;
    static String[] tilemonSearchNames = new String[]{};
    public static void tileMonitorWindow() {

        if (tilemonWindow == null){

            //window
            tilemonWindow = ZeeConfig.gameUI.add(new ZeeWindow(Coord.of(200,400),"Tile Monitor"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contains("close")){
                        tilesMonitorCleanup();
                        ZeeConfig.removePlayerText();
                    }
                    //super.wdgmsg(msg, args);
                }
            },300,300);

            //button refresh
            tilemonWindow.add(new Button(UI.scale(60),"refresh"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate")){
                        tilesWindowRefresh();
                    }
                }
            });

            //checkbox auto refresh
            tilemonAutoCheckbox = tilemonWindow.add(new CheckBox("auto"){
                public void set(boolean val) {
                    tilemonAutoRefresh = val;
                    a = val;
                    if (tilemonAutoRefresh) {
                        println("new thread auto refresh");
                        tilemonAutoThread = new ZeeThread() {
                            public void run() {
                                tilemonLastWindowRefreshPlayerTile = tilemonCurPlayerTile = ZeeConfig.getPlayerTile();
                                while(tilemonAutoRefresh){
                                    try {
                                        sleep(5000);
                                        if (ZeeConfig.getPlayerGob() == null)// player not loaded
                                            continue;
                                        if (tilemonAutoRefresh)
                                            tilesWindowRefresh();
                                    } catch (InterruptedException e) {
                                        //unchecked autorefresh
                                        tilemonAutoRefresh = false;
                                        if (tilemonAutoCheckbox != null)
                                            tilemonAutoCheckbox.set(false);
                                    } catch (Exception e){
                                        e.printStackTrace();
                                        tilemonAutoRefresh = false;
                                        if (tilemonAutoCheckbox != null)
                                            tilemonAutoCheckbox.set(false);
                                    }
                                }
                            }
                        };
                        tilemonAutoThread.start();
                    }
                    else if (tilemonAutoThread!=null && !tilemonAutoThread.isInterrupted()){
                        tilemonAutoThread.interrupt();
                    }
                }
            },63,6);



            //wishlist search box
            tilemonLabelFindTile = tilemonWindow.add(new Label("Find"),0,33);
            TextEntry te = tilemonWindow.add(new ZeeWindow.ZeeTextEntry(UI.scale(80),""){
                void onEnterPressed(String text) {
                    if (text.strip().isBlank()) {
                        tilemonSearchNames = null;
                    }else {
                        tilemonSearchNames = text.split(",");
                    }
                }
            },tilemonLabelFindTile.c.x+tilemonLabelFindTile.sz.x+5,30);
            te.settip("tile name(s), comma separated");

            // scrollport for tiles
            tilemonScrollport = tilemonWindow.add(new Scrollport(new Coord(120, 110)), 0, 55);

            // set auto refresh on
            tilemonAutoCheckbox.click();
        }
        else{
            tilemonWindow.show();
        }

        tilesWindowRefresh();//on window create
    }

    static long msLastTileMsg=0, msLastSilverMsg=0, msLastGoldMsg=0;
    private static void tilesWindowRefresh() {
        Glob g = ZeeConfig.gameUI.map.glob;
        Gob player = ZeeConfig.gameUI.map.player();
        if (player == null)
            return;
        Coord pltc = new Coord((int) player.getc().x / 11, (int) player.getc().y / 11);
        HashMap<String,Integer> mapTileresCount= new HashMap<>();
        for (int x = -44; x < 44; x++) {
            for (int y = -44; y < 44; y++) {
                int t = g.map.gettile(pltc.sub(x, y));
                Resource res = g.map.tilesetr(t);
                if (res == null)
                    continue;
                String name = res.name;
                if (!name.contains("/rocks/")) //ignore non mineable tiles
                    continue;
                Integer count = mapTileresCount.get(name);
                if (count==null)
                    count = 1;
                else
                    count++;
                //count tiles
                mapTileresCount.put(name,count);
            }
        }
        //remove old labels
        for (Label l : tilemonWindow.children(Label.class)) {
            if (!l.equals(tilemonLabelFindTile))
                l.destroy();
        }
        //sorted list
        SortedSet<String> tiles = new TreeSet<String>(mapTileresCount.keySet());
        //create new labels
        Label label;
        String basename;
        List<String> silverList = List.of("galena","argentite","hornsilver");
        List<Label> foundList = new ArrayList<>();
        List<Label> oreList = new ArrayList<>();
        List<Label> tilesList = new ArrayList<>();
        for (String tileName : tiles) {
            basename = tileName.replaceAll("gfx/tiles/rocks/","");
            label = new Label(basename+"   "+ mapTileresCount.get(tileName));
            // find list
            if (tilemonSearchNames!=null && tilemonSearchNames.length>0 && List.of(tilemonSearchNames).contains(basename)){
                label.setcolor(Color.green);
                label.settext(label.texts + "  (found)");
                // 5min limit, TODO better way
                if(ZeeThread.now() - msLastTileMsg > 1000*60*5) {
                    msLastTileMsg = ZeeThread.now();
                    ZeeConfig.msg("Found " + basename);
                    ZeeSynth.textToSpeakLinuxFestival("Tile found");
                }
                //add found tile to the top
                foundList.add(0,label);
            }
            // label ore
            else if (isRegularOre(basename)) {
                label.setcolor(Color.yellow);
                label.settext(label.texts+"  (ore)");
                //add ore tile to the top
                oreList.add(0,label);
            }
            // label silver/gold
            else if (isPreciousOre(basename)) {
                // label color
                label.setcolor(Color.red);
                // label text
                if (silverList.contains(basename)) {
                    label.settext(basename + "  (silver)");
                    // 5min limit, TODO find better way
                    if(ZeeThread.now() - msLastSilverMsg > 1000*60*5) {
                        msLastSilverMsg = ZeeThread.now();
                        ZeeConfig.msg("Silver ore found");
                        ZeeSynth.textToSpeakLinuxFestival("Silver ore found");
                    }
                } else {
                    label.settext(basename + "  (gold)");
                    // 5min limit, TODO better way
                    if(ZeeThread.now() - msLastGoldMsg > 1000*60*5) {
                        msLastGoldMsg = ZeeThread.now();
                        ZeeConfig.msg("Gold ore found");
                        ZeeSynth.textToSpeakLinuxFestival("Gold ore found");
                    }
                }
                //add precious ore to the top
                oreList.add(0,label);
            }
            else{
                //add regular tile to the end
                tilesList.add(label);
            }
        }


        // add ordered labels
        int y = 0;
        for (Label lbl : foundList) {
            tilemonScrollport.cont.add(lbl,0,y);
            y += 13;
        }
        oreList.sort(Comparator.comparing(label2 -> label2.texts));
        for (Label lbl : oreList) {
            tilemonScrollport.cont.add(lbl,0,y);
            y += 13;
        }
        for (Label lbl : tilesList) {
            tilemonScrollport.cont.add(lbl,0,y);
            y += 13;
        }


        /*
            append mining ql log
         */
        y = tilemonScrollport.c.y + tilemonScrollport.sz.y + 5;
        label = new Label("==== top 10 ql ====");
        label.setcolor(Color.green);
        tilemonWindow.add(label,0,y);
        y += 13;
        tilemonWindow.add((new CheckBox("spk"){
            {a = hiQlSpeak;}
            public void set(boolean val) {
                Utils.setprefb("hiQlSpeak", val);
                hiQlSpeak = val;
                a = val;
            }
        }).settip("speak highest ql"),0,y);
        tilemonWindow.add((new CheckBox("txt"){
            {a = hiQlText;}
            public void set(boolean val) {
                Utils.setprefb("hiQlText", val);
                hiQlText = val;
                a = val;
            }
        }).settip("text highest ql"),40,y);
        y += 3;
        List<Map.Entry<String,Integer>> miningLog = getSortedMiningLog();
        for (int i = 0; i < miningLog.size(); i++) {
            if (i < 10) {//limit list size
                y += 13;
                label = new Label(miningLog.get(i).getKey()+" q"+miningLog.get(i).getValue());
                tilemonWindow.add(label,0,y);
            }else
                break;
        }

        tilemonWindow.pack();
    }

    static void tilesMonitorCleanup() {
        //println("tilemonCleanup");
        try {
            tilemonAutoRefresh = false;
            tilemonLastWindowRefreshPlayerTile = tilemonCurPlayerTile = tilemonLastPreciousCoord = null;
            if (tilemonWindow != null) {
                tilemonWindow.destroy();
                tilemonWindow = null;
            }
            if (tilemonAutoThread != null)
                tilemonAutoThread.interrupt();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static boolean isRegularOre(String basename){
        return ZeeConfig.mineablesOre.contains(basename);
    }

    static boolean isStoneNotOre(String basename){
        return ZeeConfig.mineablesStone.contains(basename);
    }

    static boolean isPreciousOre(String basename){
        return ZeeConfig.mineablesOrePrecious.contains(basename);
    }

    static HashMap<String,Integer> mapMiningLogNameQl = new HashMap<>();
    static int highestQl=0;
    static boolean hiQlSpeak = Utils.getprefb("hiQlSpeak",true);
    static boolean hiQlText = Utils.getprefb("hiQlText",true);
    static void checkMiningLogHighestQl(GItem gItem, String basename) {
        if (!isStoneNotOre(basename) && !isPreciousOre(basename)
            && !isRegularOre(basename) && !ZeeConfig.mineablesCurios.contains(basename))
            return;
        try {
            Integer newQl = gItem.getInfoQualityInt();
            Integer oldQl = mapMiningLogNameQl.get(basename);
            if (oldQl == null || newQl > oldQl) {
                mapMiningLogNameQl.put(basename, newQl);
                //println("miningQl(" + mapMiningLogNameQl.size() + ") > " + getSortedMiningLog().toString());
                // highest ql
                if (highestQl < newQl){
                    highestQl = newQl;
                    // alert ql if tilemonitor is open
                    if (tilemonWindow!=null) {
                        if (hiQlSpeak)
                            ZeeSynth.textToSpeakLinuxFestival("" + highestQl);
                        if (hiQlText)
                            ZeeConfig.addPlayerText("q" + highestQl);
                    }
                }
            }
        }catch (Loading l){
            println(basename+"  spr not loaded yet?");
        }
    }
    static List<Map.Entry<String,Integer>> getSortedMiningLog() {
        return mapMiningLogNameQl.entrySet().stream()
                .sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()))
                .collect(Collectors.toList());
    }

    public static void println(String s) {
        System.out.println(s);
    }
}

package haven;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ZeeManagerMiner extends ZeeThread{

    private static final double MAX_DIST_BOULDER = 25;

    //public static boolean isCursorMining;
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
                            println("saved end coord, pick stone?");
                            if (tunnelHelperEndCoord != null)
                                tunnelHelperEndCoordPrev = Coord.of(tunnelHelperEndCoord);
                            tunnelHelperEndCoord = ZeeConfig.getPlayerCoord();
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
        waitCursorName(ZeeConfig.CURSOR_MINE);
        ZeeConfig.gameUI.map.wdgmsg("sel", c1, c2, 0);
        //waitPlayerIdleFor(2);//minimum 2 sec
        waitPlayerIdlePose();
        ZeeConfig.clickRemoveCursor();
        waitCursorName(ZeeConfig.CURSOR_ARW);
    }


    public static Gob getBoulderCloseEnoughForChipping() {
        Gob boulder = ZeeConfig.getClosestGobByNameContains("gfx/terobjs/bumlings/");
        if (boulder!=null && ZeeConfig.distanceToPlayer(boulder) < MAX_DIST_BOULDER){
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
            waitCursorName(ZeeConfig.CURSOR_ARW);
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
    public static void chipBoulderNewThread(Gob boulder) {
        isChippingBoulder = true;
        new ZeeThread(){
            public void run() {
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

                    //wait chipping and resume minig
                    if(waitPlayerIdlePose()){
                        //resume mining
                        ZeeConfig.gameUI.map.wdgmsg("sel", ZeeConfig.lastSavedOverlayStartCoord, ZeeConfig.lastSavedOverlayEndCoord, ZeeConfig.lastSavedOverlayModflags);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                isChippingBoulder = false;
                ZeeConfig.removePlayerText();
            }
        }.start();
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

        chipBoulderNewThread(boulder);
    }

    static ZeeWindow tilemonWindow;
    static boolean tilemonAutoRefresh = false;
    static ZeeThread tilemonAutoThread;
    static CheckBox tilemonAutoCheckbox;
    static Label tilemonLabelFindTile;
    static Coord tilemonCurPlayerTile, tilemonLastWindowRefreshPlayerTile, tilemonLastPreciousCoord;
    static String[] tilemonSearchNames = new String[]{};
    public static void tileMonitorWindow() {
        if (tilemonWindow == null){
            //window
            tilemonWindow = ZeeConfig.gameUI.add(new ZeeWindow(Coord.of(200,400),"Tiles"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contains("close")){
                        tilesMonitorCleanup();
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
                                        tilemonCurPlayerTile = ZeeConfig.getPlayerTile();
                                        //refresh tiles window if player coord changed by 88 tiles
                                        if(ZeeConfig.tileCoordsChangedBy(tilemonLastWindowRefreshPlayerTile,tilemonCurPlayerTile,88) )
                                        {
                                            println("calling auto refresh last="+ tilemonLastWindowRefreshPlayerTile +"  cur="+tilemonCurPlayerTile);
                                            tilesWindowRefresh();
                                            tilemonLastWindowRefreshPlayerTile = tilemonCurPlayerTile;
                                        }
                                    } catch (InterruptedException e) {
                                        //e.printStackTrace();
                                        tilemonAutoRefresh = false;
                                        if (tilemonAutoCheckbox !=null)
                                            tilemonAutoCheckbox.set(false);
                                    }
                                }
                            }
                        };
                        tilemonAutoThread.start();
                    }
                    else {
                        tilemonAutoThread.interrupt();
                    }
                }
            },63,6);
            //wishlist search box
            tilemonLabelFindTile = tilemonWindow.add(new Label("Find"),0,33);
            TextEntry te = tilemonWindow.add(new TextEntry(UI.scale(80),""){
                public void changed(ReadLine buf) {
                    if(!buf.line().isEmpty()) {
                        String names = buf.line();
                        if (names.isBlank()) {
                            tilemonSearchNames = new String[]{};
                        }else {
                            tilemonSearchNames = names.split(",");
                        }
                    }
                    super.changed(buf);
                }
            },tilemonLabelFindTile.c.x+tilemonLabelFindTile.sz.x+5,30);
            te.settip("tile name(s), comma separated");
        }
        else{
            tilemonWindow.show();
        }
        tilesWindowRefresh();//on window create
    }

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
        int y = 50;
        Label label;
        String basename;
        List<String> silverList = List.of("galena","argentite","hornsilver");
        for (String tileName : tiles) {
            basename = tileName.replaceAll("gfx/tiles/rocks/","");
            label = new Label(basename+"   "+ mapTileresCount.get(tileName));
            // find list
            if (tilemonSearchNames.length>0 && List.of(tilemonSearchNames).contains(basename)){
                label.setcolor(Color.green);
                label.settext(label.texts+"  (found)");
                ZeeConfig.msg("Found "+basename);
            }
            // label ore
            else if (isStoneOre(basename)) {
                label.setcolor(Color.yellow);
                label.settext(label.texts+"  (ore)");
            }
            // label silver/gold
            else if (isStoneOrePrecious(basename)) {
                // label color
                label.setcolor(Color.red);
                // label text
                if (silverList.contains(label.texts))
                    label.settext(label.texts+"  (silver)");
                else
                    label.settext(label.texts+"  (gold)");
                //alert precious ore if player tiles changed by  x
                ZeeConfig.msg("Precious ore detected!");
            }
            tilemonWindow.add(label,0,y);
            y += 17;
        }
        tilemonWindow.pack();
    }

    static void tilesMonitorCleanup() {
        println("tilemonCleanup");
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

    static boolean isStoneOre(String basename){
        return ZeeConfig.mineablesOre.contains(basename);
    }

    static boolean isStoneNotOre(String basename){
        return ZeeConfig.mineablesStone.contains(basename);
    }

    static boolean isStoneOrePrecious(String basename){
        return ZeeConfig.mineablesOrePrecious.contains(basename);
    }

    public static void println(String s) {
        System.out.println(s);
    }
}

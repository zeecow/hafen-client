package haven;

import java.util.ArrayList;
import java.util.List;

public class ZeeManagerTrees {

    static boolean isRemovingAllTrees;
    static boolean isDestroyingAllTreelogs;
    static boolean hideGobTrees = Utils.getprefb("hideGobTrees",true);
    private static ArrayList<Gob> treesForRemoval;
    private static ArrayList<Gob> treelogsForDestruction;
    private static Gob currentRemovingTree;
    private static Gob currentDestroyingTreelog;
    static List<Gob> listQueuedTreeChop = null;
    static ZeeThread threadChopTree = null;
    private static List<Gob> queuedStumps = null;
    private static ZeeThread threadShovelStumps = null;

    // place treelog next to other
    // TODO currently only works if player is perpendicular to treeLogGround
    // TODO may break depending on coords signal change
    static void placeTreelogNextTo(Gob treeLogGround) {
        new Thread() {
            public void run() {
                try {

                    ZeeConfig.addPlayerText("placing");

                    Gob liftedTreelog = ZeeConfig.isPlayerLiftingGobNamecontains("gfx/terobjs/trees/");
                    if (liftedTreelog==null){
                        ZeeConfig.msgError("placeTreelogNextTo > couldn't find lifted treelog");
                        ZeeConfig.removePlayerText();
                        return;
                    }
                    //println(liftedTreelog.rc +" "+liftedTreelog.a+"  ,  "+treeLogGround.rc+" "+treeLogGround.a);

                    // right click lifted treelog to create plob
                    ZeeManagerGobClick.gobClick(liftedTreelog,3);
                    sleep(500);

                    // adjust plob angle, postition and place it
                    if (ZeeManagerStockpile.lastPlob==null){
                        ZeeConfig.msgError("placeTreelogNextTo > couldn't find last plob");
                        ZeeConfig.removePlayerText();
                        return;
                    }
                    Coord2d playerrc = ZeeConfig.getPlayerGob().rc;
                    Coord2d newrc = Coord2d.of(treeLogGround.rc.x, treeLogGround.rc.y);
                    if (Math.abs(treeLogGround.rc.x - playerrc.x) > Math.abs(treeLogGround.rc.y - playerrc.y)){
                        if (treeLogGround.rc.x > playerrc.x)
                            newrc.x -= 4.125;
                        else
                            newrc.x += 4.125;
                    }else{
                        if (treeLogGround.rc.y > playerrc.y)
                            newrc.y -= 4.125;
                        else
                            newrc.y += 4.125;
                    }
                    // position plob
                    ZeeManagerStockpile.lastPlob.move(newrc, treeLogGround.a);

                    // place treelog and wait
                    ZeeManagerGobClick.gobPlace(ZeeManagerStockpile.lastPlob,0);
                    ZeeThread.waitNotPlayerPose(ZeeConfig.POSE_PLAYER_LIFTING);

                }catch (Exception e){
                    e.printStackTrace();
                }
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    static void scheduleDestroyTreelog(Gob treelog) {
        if (treelogsForDestruction==null) {
            treelogsForDestruction = new ArrayList<Gob>();
        }

        if (treelogsForDestruction.contains(treelog)) {
            // remove treelog from queue
            removeScheduledTreelog(treelog);
        } else if (!currentDestroyingTreelog.equals(treelog)){
            // add treelog to queue
            treelogsForDestruction.add(treelog);
            ZeeConfig.addGobText(treelog,"des "+treelogsForDestruction.size());
        }
    }

    private static Gob removeScheduledTreelog(Gob treelog) {
        // remove treelog from queue
        treelogsForDestruction.remove(treelog);
        ZeeConfig.removeGobText(treelog);
        // update queue gob's texts
        for (int i = 0; i < treelogsForDestruction.size(); i++) {
            ZeeConfig.addGobText(treelogsForDestruction.get(i),"des "+(i+1));
        }
        return treelog;
    }

    static void scheduleRemoveTree(Gob tree) {
        if (treesForRemoval==null) {
            treesForRemoval = new ArrayList<Gob>();
        }

        if (treesForRemoval.contains(tree)) {
            // remove tree from queue
            removeScheduledTree(tree);
        }
        else if (currentRemovingTree!=null && !currentRemovingTree.equals(tree)){
            // add tree to queue
            treesForRemoval.add(tree);
            ZeeConfig.addGobText(tree,"rem "+treesForRemoval.size());
        }
    }

    private static Gob removeScheduledTree(Gob tree) {
        // remove tree from queue
        ZeeConfig.removeGobText(tree);
        treesForRemoval.remove(tree);
        // update queue gob's texts
        for (int i = 0; i < treesForRemoval.size(); i++) {
            ZeeConfig.addGobText(treesForRemoval.get(i),"rem"+(i+1));
        }
        return tree;
    }

    static void destroyTreelogs(Gob firstTreelog, String petalName) {
        if (!ZeeManagerItemClick.isItemInHandSlot("/bonesaw") || ZeeManagerItemClick.isItemInHandSlot("/saw-m")){
            ZeeConfig.msg("need bone saw equipped, no metal saw");
            return;
        }
        Gob treelog = firstTreelog;
        int logs = 2;
        try {
            ZeeThread.waitNoFlowerMenu();
            String treelogName = treelog.getres().name;
            if (petalName.equals(ZeeFlowerMenu.STRPETAL_DESTROYTREELOG3)) {
                logs = 3;
            } else if (petalName.equals(ZeeFlowerMenu.STRPETAL_DESTROYTREELOG5)) {
                logs = 5;
            } else if (petalName.equals(ZeeFlowerMenu.STRPETAL_DESTROYALL)) {
                isDestroyingAllTreelogs = true;
                logs = 999;
            }
            ZeeConfig.destroyingTreelogs = true;
            ZeeThread.prepareCancelClick();
            while ( logs > 0  &&  !ZeeConfig.isCancelClick() ) {
                ZeeConfig.addPlayerText("treelogs "+logs);
                if (!ZeeManagerGobClick.clickGobPetal(treelog,"Make boards")){
                    ZeeThread.println("can't click treelog = "+treelog);
                    logs = -1;
                    currentDestroyingTreelog = null;
                    continue;
                }
                currentDestroyingTreelog = treelog;
                ZeeThread.waitPlayerIdlePose();
                if (!ZeeConfig.isCancelClick()){
                    logs--;
                    if (isDestroyingAllTreelogs){
                        // destroy all, treelog queue is present
                        if (treelogsForDestruction != null) {
                            if (treelogsForDestruction.size() > 0) {
                                treelog = removeScheduledTreelog(treelogsForDestruction.remove(0));
                            } else {
                                //stop destroying when queue consumed
                                ZeeThread.println("logs -1, treelogsForDestruction empty");
                                logs = -1;
                            }
                        }else{
                            // destroy all, no treelog queue
                            treelog = getClosestTreeLog();
                        }
                    } else {
                        // destroy 3 or 5 same type treelogs
                        treelog = ZeeConfig.getClosestGobByNameContains(treelogName);
                    }
                }else{
                    if (ZeeConfig.isCancelClick()) {
                        ZeeConfig.msg("destroy treelog canceled by click");
                        ZeeThread.println("destroy treelog canceled by click");
                    }else
                        ZeeThread.println("destreelog canceled by gobHasFlowermenu?");
                    logs = -1;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        isDestroyingAllTreelogs = false;
        ZeeConfig.destroyingTreelogs = false;
        currentDestroyingTreelog = null;
        if (treelogsForDestruction!=null)
            treelogsForDestruction.clear();
        treelogsForDestruction = null;
        ZeeConfig.removePlayerText();
    }

    public static Gob getClosestTree() {
        List<Gob> list = ZeeConfig.findGobsByNameContains("/trees/");
        list.removeIf(gob1 -> !ZeeManagerGobClick.isGobTree(gob1.getres().name));
        return ZeeConfig.getClosestGobToPlayer(list);
    }

    public static Gob getClosestTreeLog() {
        List<Gob> list = ZeeConfig.findGobsByNameContains("/trees/");
        list.removeIf(gob1 -> !ZeeManagerGobClick.isGobTreeLog(gob1.getres().name));
        return ZeeConfig.getClosestGobToPlayer(list);
    }

    static void chopTreeReset(){
        if (listQueuedTreeChop!=null && !listQueuedTreeChop.isEmpty()){
            ZeeConfig.removeGobText((ArrayList<Gob>) listQueuedTreeChop);
            //listQueuedTreeChop.clear();
        }
        listQueuedTreeChop = null;
        threadChopTree = null;
    }

    static void queueChopTreeUpdLabels(){
        if(listQueuedTreeChop!=null && !listQueuedTreeChop.isEmpty()) {
            for (int i = 0; i < listQueuedTreeChop.size(); i++) {
                ZeeConfig.addGobText(listQueuedTreeChop.get(i), "" + (i+1));
            }
            ZeeConfig.addPlayerText("queue " + listQueuedTreeChop.size());
        }
    }

    static void queueShovelStump(Gob stump) {
        if (stump==null){
            ZeeThread.println("queuedStumps > stump null");
            return;
        }
        if (queuedStumps==null) {
            queuedStumps = new ArrayList<>();
        }
        else if(queuedStumps.contains(stump)){
            queuedStumps.remove(stump);
            ZeeConfig.removeGobText(stump);
            queueStumpsUpdLabels();
            return;
        }
        queuedStumps.add(stump);
        queueStumpsUpdLabels();


        if(threadShovelStumps==null) {
            threadShovelStumps = new ZeeThread() {
                public void run() {
                    println("shovel stump thread start");
                    try {
                        ZeeConfig.addPlayerText("queue " + queuedStumps.size());
                        prepareCancelClick();
                        while (!isCancelClick()) {
                            sleep(1000);
                            if (isCancelClick()) {
                                println("queuedStumps > cancel click");
                                break;
                            }
                            if (ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_DRINK, ZeeConfig.POSE_PLAYER_DIGSHOVEL)) {
                                continue;
                            }
                            if (ZeeConfig.isPlayerMovingByAttrLinMove()){
                                continue;
                            }
                            if (queuedStumps.isEmpty()) {
                                println("queuedStumps > empty list");
                                break;
                            }
                            Gob nextStump = queuedStumps.remove(0);
                            if (nextStump == null) {
                                println("queuedStumps > next stump null");
                                break;
                            }

                            //update labels
                            ZeeConfig.removeGobText(nextStump);
                            queueStumpsUpdLabels();

                            removeStumpMaybe(nextStump);
                            prepareCancelClick();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ZeeConfig.removePlayerText();
                    println("shovel stump thread end");
                    queueStumpsReset();
                }
            };
            if (threadShovelStumps!=null)
                threadShovelStumps.start();
        }
    }

    static void queueStumpsReset(){
        if (queuedStumps!=null && !queuedStumps.isEmpty()){
            ZeeConfig.removeGobText((ArrayList<Gob>) queuedStumps);
        }
        queuedStumps = null;
        threadShovelStumps = null;
    }

    static void queueStumpsUpdLabels(){
        if(queuedStumps!=null && !queuedStumps.isEmpty()) {
            for (int i = 0; i < queuedStumps.size(); i++) {
                ZeeConfig.addGobText(queuedStumps.get(i), "" + (i+1));
            }
            ZeeConfig.addPlayerText("queue " + queuedStumps.size());
        }
    }

    static boolean isDestroyTreelog() {
        if(ZeeManagerGobClick.isGobTreeLog(ZeeManagerGobClick.gobName) && ZeeManagerItemClick.isItemInHandSlot("bonesaw"))
            return true;
        return false;
    }

    static void removeTreeAndStump(Gob tree, String petalName){
        try{
            if (petalName.contentEquals(ZeeFlowerMenu.STRPETAL_REMOVEALLTREES)) {
                ZeeConfig.addPlayerText("rem trees");
                isRemovingAllTrees = true;
            }else {
                ZeeConfig.addPlayerText("rem tree&stump");
            }
            ZeeThread.prepareCancelClick();
            if(!ZeeThread.waitNoFlowerMenu()){
                ZeeThread.println("remtree > failed waiting no flowemenu");
                exitRemoveAllTrees();
                return;
            }
            ZeeManagerItemClick.equipAxeChopTree();
            Coord2d treeCoord;
            while (tree!=null && !ZeeConfig.isCancelClick()) {
                Thread.sleep(500);//safe wait
                //start chopping
                if(!ZeeManagerGobClick.clickGobPetal(tree, "Chop")){
                    ZeeThread.println("remtree > couldnt click tree petal \"Chop\"");
                    break;
                }
                Thread.sleep(500);//safe wait
                if(!ZeeThread.waitPlayerPose(ZeeConfig.POSE_PLAYER_CHOPTREE)){
                    ZeeThread.println("remtree > failed waiting pose choptree");
                    break;
                }
                currentRemovingTree = tree;
                treeCoord = new Coord2d(tree.rc.x, tree.rc.y);
                //wait idle
                if (!ZeeConfig.isCancelClick() && ZeeThread.waitPlayerIdlePose()) {
                    //wait new stump loading
                    Thread.sleep(2000);
                    //check task canceled
                    if(ZeeConfig.isCancelClick()) {
                        ZeeThread.println("remtree > click canceled");
                        break;
                    }
                    Gob stump = ZeeConfig.getClosestGobToPlayer(ZeeConfig.findGobsByNameEndsWith("stump"));
                    if (stump != null) {
                        //stump location doesnt match tree and there's no other stump close
                        if (stump.rc.compareTo(treeCoord) != 0  &&  ZeeConfig.distanceToPlayer(stump) > 25){
                            ZeeThread.println("remtree > stump undecided");
                            break;
                        }
                        ZeeConfig.addGobText(stump, "stump");
                        removeStumpMaybe(stump);
                        ZeeThread.waitPlayerIdlePose();
                    } else {
                        ZeeThread.println("remtree > stump is null");
                    }
                    if (isRemovingAllTrees) {
                        if (treesForRemoval!=null){
                            if (treesForRemoval.size()>0)
                                tree = removeScheduledTree(treesForRemoval.remove(0));
                            else
                                tree = null; // stop removing trees if queue was consumed
                        }else{
                            // remove all trees until player blocked or something
                            tree = getClosestTree();
                        }
                    }else {
                        tree = null;
                    }
                    //println("next tree = "+tree);
                }
                else
                    ZeeThread.println("remtree > task canceled or !waitPlayerIdlePose");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        exitRemoveAllTrees();
    }

    private static void exitRemoveAllTrees() {
        isRemovingAllTrees = false;
        currentRemovingTree = null;
        if(treesForRemoval != null  &&  treesForRemoval.size() > 0) {
            ZeeConfig.removeGobText(treesForRemoval);
            //treesForRemoval.clear();
        }
        //treesForRemoval = null;
        ZeeConfig.removePlayerText();
    }

    public static boolean removeStumpMaybe(Gob stump) throws InterruptedException {
        boolean droppedBucket = false;

        //move closer to stump
        ZeeManagerGobClick.gobClick(stump,1);
        if(!ZeeThread.waitPlayerDistToGob(stump,25)){
            ZeeThread.println("couldn't get close to stump?");
            return false;
        }

        //drop bucket if present
        if (ZeeManagerItemClick.isItemEquipped("bucket")) {
            if (ZeeConfig.getMeterStamina() < 100) {
                ZeeManagerItemClick.drinkFromBeltHandsInv();
                Thread.sleep(ZeeThread.PING_MS*2);
                ZeeThread.waitNotPlayerPose(ZeeConfig.POSE_PLAYER_DRINK);
            }
            ZeeManagerItemClick.getEquipory().dropItemByNameContains("/bucket");
            droppedBucket = true;
        }

        //equip shovel
        ZeeManagerItemClick.equipBeltItem("shovel");
        if (!ZeeThread.waitItemInHand("shovel")){
            ZeeThread.println("couldnt equip shovel ?");
            return false;
        }
        ZeeThread.waitNotHoldingItem();//wait possible switched item go to belt?

        //remove stump
        ZeeManagerGobClick.destroyGob(stump);

        //reequip bucket if dropped
        if (droppedBucket){
            ZeeThread.waitPlayerPose(ZeeConfig.POSE_PLAYER_IDLE);
            Gob bucket = ZeeConfig.getClosestGobByNameContains("/bucket");
            if (bucket!=null){
                if (ZeeManagerItemClick.pickupHandItem("shovel")) {
                    if(ZeeManagerItemClick.dropHoldingItemToBeltOrInv()) {
                        Thread.sleep(ZeeThread.PING_MS);
                        ZeeConfig.clickRemoveCursor();
                        ZeeThread.waitCursorName(ZeeConfig.CURSOR_ARW);
                        Thread.sleep(ZeeThread.PING_MS);
                        ZeeManagerGobClick.gobClick(bucket, 3);
                        if (ZeeThread.waitHoldingItem())
                            ZeeManagerItemClick.equipEmptyHand();
                        else
                            ZeeThread.println("couldnt pickup da bucket");
                    }
                }else {
                    ZeeThread.println("couldnt return shovel to belt?");
                }
            }else{
                ZeeThread.println("bucket gob not found");
            }
        }

        // maybe stump was removed
        return true;
    }
}

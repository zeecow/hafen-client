package haven;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class ZeeThread  extends Thread{

    static final long SLEEP_MS = 50;
    static final long TIMEOUT_MS = 2000;
    static final long LONG_CLICK_MS = 333;
    static final long PING_MS = 250;
    public static final double TILE_SIZE = MCache.tilesz.x;
    static double stamChangeSec = 0;
    static Thread stamThread;

    public static void staminaMonitorStart() {
        stamThread = new Thread(){
            public void run() {
                double lastStam;
                try {
                    while (true) {
                        lastStam = ZeeConfig.getMeterStamina();
                        sleep(1000);
                        stamChangeSec = ZeeConfig.getMeterStamina() - lastStam ;
                        //println("stam/sec  "+stamChangeSec+"    lastStam "+lastStam);
                    }
                }catch (InterruptedException ie){
                    //println("staMonitor sleep interrupted");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        stamThread.start();
    }

    public static void staminaMonitorStop(){
        try {
            if (stamThread!=null)
                stamThread.interrupt();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean waitHoldingItemChanged() {
        long max = TIMEOUT_MS;
        if(ZeeConfig.gameUI.vhand==null) {
            //println("waitHoldingItemChanged > vhand null");
            return false;
        }
        //println("waitHoldingItemChanged > "+ZeeConfig.gameUI.vhand.item.wdgid());
        int itemId = ZeeConfig.gameUI.vhand.item.wdgid();
        try {
            while(max>0  &&  ZeeConfig.gameUI.vhand!=null  &&  itemId == ZeeConfig.gameUI.vhand.item.wdgid()) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
            sleep(PING_MS);//wait vhand update?
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean ret = (ZeeConfig.gameUI.vhand == null  ||  itemId != ZeeConfig.gameUI.vhand.item.wdgid());
        //println("waitHoldingItemChanged  ret="+ret + (ZeeConfig.gameUI.vhand==null ? "  vhand=null" : "  vhandId="+ZeeConfig.gameUI.vhand.item.wdgid()));
        return ret;
    }

    public static boolean waitNotHoldingItemOrCancelClick() {
        try {
            prepareCancelClick();
            while(!isCancelClick() && ZeeConfig.gameUI.vhand!=null) {
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (ZeeConfig.gameUI.vhand == null);
    }

    public static boolean waitNotHoldingItem() {
        long max = TIMEOUT_MS;
        try {
            while(max>0 && ZeeConfig.gameUI.vhand!=null) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (ZeeConfig.gameUI.vhand == null);
    }

    public static boolean waitNotHoldingItem(long timeOutMs) {
        long max = timeOutMs;
        try {
            while(max>0 && ZeeConfig.gameUI.vhand!=null) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (ZeeConfig.gameUI.vhand == null);
    }

    public static boolean waitHoldingItem() {
        long max = TIMEOUT_MS;
        try{
            //sleep(PING_MS*3); //item switch requires extra waiting
            while(max>0 && ZeeConfig.gameUI.vhand==null) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (ZeeConfig.gameUI.vhand != null);
    }

    public static boolean waitHoldingItem(long timeOutMs) {
        long max = timeOutMs;
        try{
            while(max>0 && ZeeConfig.gameUI.vhand==null) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (ZeeConfig.gameUI.vhand != null);
    }

    public static boolean waitItemInHand(String name) {
        //println("waitItemEquipped > "+name);
        int max = (int) TIMEOUT_MS;
        try {
            while(max>0 && !ZeeManagerItems.isItemInHandSlot(name)) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitItemEquipped > ret "+ZeeClickItemManager.isItemEquipped(name));
        return ZeeManagerItems.isItemInHandSlot(name);
    }

    public static boolean waitGobFollowedIdle(Gob follower){
        Gob followed = ZeeConfig.getGobFollowTarget(follower);
        return waitGobIdleVelocity(followed);//LinMove causes multiple calls
    }

    public static boolean waitGobIdleVelocity(Gob gob) {
        if (gob==null) {
            println("waitGobIdleVelocity gob null");
            return false;
        }
        //println("waitGobIdleVelocity "+gob.getres().name);
        boolean isPlayer = ZeeConfig.isPlayer(gob);
        Gob playerHorse = null;
        if (isPlayer)
            playerHorse = ZeeConfig.getPlayerMountedHorse();
        long countMs = 0;
        try {

            // wait start moving for a while
            while( (gob.getv()==0  &&  countMs<777) || (isPlayer && ZeeConfig.isPlayerPoseDrinkOrMove(playerHorse)) ) {
                sleep(SLEEP_MS);
                countMs += SLEEP_MS;
            }

            // wait idle
            while ( gob.getv()!=0  || (isPlayer && ZeeConfig.isPlayerPoseDrinkOrMove(playerHorse)) ){
                sleep(SLEEP_MS);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean ret = (gob.getv() == 0);
        //println("waitGobIdleVelocity  ret="+ret+"  gobVel="+gob.getv());
        return ret;
    }

    public static boolean waitPlayerIdleRc(){
        Gob player = ZeeConfig.getPlayerGob();
        Coord2d prev = null;
        boolean idle = false;
        try {
            do {
                prev = Coord2d.of(player.rc.x,player.rc.y);
                sleep(500);
            } while (player.rc.compareTo(prev) != 0);
            idle = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return idle;
    }

    public static boolean waitPlayerIdleVelocity() {
        return waitGobIdleVelocity(ZeeConfig.getPlayerGob());
    }

    public static boolean waitGobIdleVelocityMs(Gob gob, long idleMs_min777) {
        if (gob==null) {
            println("waitGobIdleVelocityMs gob null");
            return false;
        }
        //println("waitGobIdleVelocityMs");
        boolean isPlayer = ZeeConfig.isPlayer(gob);
        Gob playerHorse = null;
        if (isPlayer)
            playerHorse = ZeeConfig.getPlayerMountedHorse();
        long countMs = 0;
        try {

            //wait moving for a while
            while( gob.getv()==0  &&  countMs<idleMs_min777 )
            {
                sleep(SLEEP_MS);
                //doesn't count if player drinking,troting
                if( !isPlayer || !ZeeConfig.isPlayerPoseDrinkOrMove(playerHorse) )
                    countMs += SLEEP_MS;
            }

            //wait stop moving
            countMs = 0;
            while ( countMs < idleMs_min777 ) {
                sleep(SLEEP_MS);
                // player idle
                if ( gob.getv()==0  &&  !ZeeConfig.isPlayerPoseDrinkOrMove(playerHorse) )
                    countMs += SLEEP_MS;
                else
                    countMs = 0; // reset counter if player moves
                //println("count "+countMs);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitGobIdleVelocityMs  gobVel="+gob.getv()+"  countMs="+countMs);
        return gob.getv() == 0;
    }

    public static boolean waitPlayerIdleVelocityMs(long idleMs_min777) {
        return waitGobIdleVelocityMs(ZeeConfig.getPlayerGob(), idleMs_min777);
    }


    public static boolean waitGobIdleLinMove(Gob gob){
        try {
            do {
                sleep(555);
            }while (ZeeConfig.isGobMovingByAttrLinMove(gob));
        }catch (Exception e){
            e.printStackTrace();
        }
        return !ZeeConfig.isGobMovingByAttrLinMove(gob);
    }

    public static boolean waitPlayerIdleLinMove(){
        try {
            do {
                sleep(555);
            }while (ZeeConfig.isPlayerMovingOrFollowingByAttrLinMove());
        }catch (Exception e){
            e.printStackTrace();
        }
        return !ZeeConfig.isPlayerMovingOrFollowingByAttrLinMove();
    }

    public static boolean waitPlayerIdlePoseOrVehicleIdle(){
        // player horse idle
        if (ZeeConfig.isPlayerMountingHorse()) {
            //wait horse idle and player non active
            return waitGobPose(ZeeConfig.getPlayerMountedHorse(), ZeeConfig.POSE_HORSE_IDLE) &&
                    waitPlayerNonActivePose();
        }
        // player vehicle idle
        Gob gobFollow = ZeeConfig.getPlayerFollowTarget();
        if (gobFollow!=null){
            //println("gobFollow > "+gobFollow.getres().name);
            return waitGobFollowedIdle(ZeeConfig.getPlayerGob());//LinMove causes multiple calls
        }
        // player idle on foot
        else {
            //println("gobFollow null");
            return waitPlayerPose(ZeeConfig.POSE_PLAYER_IDLE);
        }
    }

    public static boolean waitPlayerNonActivePose(){
        return waitPlayerPoseNotInList(ZeeConfig.arrayPlayerActivePoses);
    }

    public static boolean waitPlayerPoseNotInList(String ... forbiddenPoses) {
        //println(">waitPlayerPoseNotInList");
        List<String> currentPoses;
        boolean exit = false;
        try{
            prepareCancelClick();
            do{
                sleep(PING_MS*2);
                exit = ZeeConfig.isCancelClick();
                if(exit) {
                    //println("    canceled by click");
                    break;
                }
                currentPoses = ZeeConfig.getPlayerPoses();
                //println("   "+playerPoses);
                exit = true;
                for (int i = 0; i < forbiddenPoses.length; i++) {
                    //println("      "+forbiddenPoses[i]);
                    if (currentPoses.contains(forbiddenPoses[i])){//if contains pose...
                        //println("         break");
                        exit = false;
                        break;//... break, loop and sleep again
                    }
                }
            }while(!exit);//ZeeConfig.isPlayerMoving() );
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitPlayerPoseNotInList  exit="+exit + "  cancel="+ZeeConfig.isTaskCanceledByGroundClick());
        return exit;
    }

    public static boolean waitPlayerPoseNotInListTimeout(long timeoutMs, String... poseList) {
        //println(">waitPlayerPoseNotInListTimeout");
        List<String> playerPoses;
        boolean exit = false;
        boolean poseInList = true;//assume pose is in list
        long elapsedTimeMs = 0;
        long sleepMs = PING_MS * 2;
        try{
            do{
                sleep(sleepMs);
                elapsedTimeMs += sleepMs;
                if (elapsedTimeMs >= timeoutMs) {
                    poseInList = true;//if timeout assume pose still in list
                    break;
                }
                else {
                    playerPoses = ZeeConfig.getPlayerPoses();
                    exit = true;
                    poseInList = false;
                    for (int i = 0; i < poseList.length; i++) {
                        //if contains pose, loop and sleep again
                        if (playerPoses.contains(poseList[i])) {
                            exit = false;
                            poseInList = true;
                            break;
                        }
                    }
                    if (!poseInList)
                        exit = true;
                }
            }while(!exit); // || ZeeConfig.isPlayerMoving()); // still necessary?
        }catch (Exception e){
            e.printStackTrace();
        }
        println("waitPlayerPoseNotInListTimeout ret "+!poseInList);
        return !poseInList;
    }

    public static boolean waitNotPlayerPose(String poseName) {
        List<String> poses = new ArrayList<>();
        try{
            prepareCancelClick();
            do{
                //println("waitNotPlayerPose > "+poses);
                sleep(PING_MS*2);
            } while (
                (poses = ZeeConfig.getPlayerPoses()).contains(poseName)
                && !ZeeConfig.isCancelClick()
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitNotPlayerPose ret "+(!poses.contains(poseName))+" , "+poseName);
        return !poses.contains(poseName);
    }

    static boolean waitAnyPlayerPoseFromList(String ... listAnyPose){
        return waitAnyGobPoseFromList(ZeeConfig.getPlayerGob(),listAnyPose);
    }

    static boolean waitAnyGobPoseFromList(Gob gob, String ... listAnyPose) {
        try{
            prepareCancelClick();
            do{
                sleep(PING_MS*2);
            } while (!ZeeConfig.gobHasAnyPose(gob,listAnyPose) && !ZeeConfig.isCancelClick());
        }catch (Exception e){
            e.printStackTrace();
        }
        return ZeeConfig.gobHasAnyPose(gob,listAnyPose);
    }

    static boolean waitGobPose(Gob gob, String poseName) {
        //println("waitGobPose > start");
        List<String> poses = new ArrayList<>();
        try{
            prepareCancelClick();
            do{
                //println("waitGobPose > "+poses);
                sleep(PING_MS*2);
            } while (
                !(poses = ZeeConfig.getGobPoses(gob)).contains(poseName)
                && !ZeeConfig.isCancelClick()
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("      return "+(poses.contains(poseName))+" "+poseName);
        return poses.contains(poseName);
    }

    static boolean waitGobPoseNoCancelClick(Gob gob, String poseName) {
        //println("waitGobPoseNoCancelClick > start");
        List<String> poses = new ArrayList<>();
        try{
            do{
                //println("waitGobPoseNoCancelClick > "+poses);
                sleep(PING_MS*2);
            } while (!(poses = ZeeConfig.getGobPoses(gob)).contains(poseName));
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("      return "+(poses.contains(poseName))+" "+poseName);
        return poses.contains(poseName);
    }

    static boolean waitGobOnlyPose(Gob gob, String singlePoseName) {
        //println("waitGobOnlyPose > start");
        List<String> poses = new ArrayList<>();
        try{
            prepareCancelClick();
            do{
                //println("waitGobOnlyPose > "+singlePoseName);
                sleep(PING_MS*2);
            } while (
                !(poses = ZeeConfig.getGobPoses(gob)).contains(singlePoseName)
                && poses.size() != 1
                && !ZeeConfig.isCancelClick()
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("      return "+(poses.contains(singlePoseName))+" "+singlePoseName);
        return poses.size()==1 && poses.contains(singlePoseName);
    }

    public static boolean waitPlayerOnlyPose(String singlePoseName) {
        return waitGobOnlyPose(ZeeConfig.getPlayerGob(),singlePoseName);
    }

    public static boolean waitPlayerPose(String poseName) {
        return waitGobPose(ZeeConfig.getPlayerGob(),poseName);
    }

    public static boolean waitPlayerPoseNoCancelClick(String poseName) {
        return waitGobPoseNoCancelClick(ZeeConfig.getPlayerGob(),poseName);
    }

    public static boolean waitPlayerPoseMs(String poseName, long idleMs) {
        //println("waitPlayerPoseMs ");
        List<String> poses = new ArrayList<>();
        long countMs = 0;
        try{
            while( countMs < idleMs ){
                countMs += SLEEP_MS;
                if (!(poses = ZeeConfig.getPlayerPoses()).contains(poseName))
                    countMs = 0; // reset if pose not present
                else
                    sleep(SLEEP_MS);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitPlayerPoseMs "+countMs+"ms "+(poses.contains(poseName))+" "+poseName);
        return poses.contains(poseName);
    }

    /*
        returns true if player idle for idleMS
     */
    public static boolean waitPlayerIdleFor(int idleSeconds) {
        //println("waitPlayerIdleFor "+idleSeconds+"s");
        staminaMonitorStart();
        long timer = idleSeconds * 1000;
        try {
            while( timer > 0 ) {
                if(ZeeConfig.isPlayerMoving() || stamChangeSec!=0){
                    timer = idleSeconds * 1000; //reset timer if player moving or stamina changing
                }else {
                    timer -= SLEEP_MS;
                }
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        staminaMonitorStop();
        //println("waitPlayerIdleFor ret "+(timer<=0));
        return timer <= 0;
    }


    public static boolean waitPlayerDistToGobOrCancelClick(Gob gob, int dist) {
        if (ZeeConfig.distanceToPlayer(gob) > dist) {
            prepareCancelClick();
            try {
                while (!isCancelClick() && ZeeConfig.distanceToPlayer(gob) > dist) {
                    Thread.sleep(SLEEP_MS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return !isCancelClick();
    }

    public static boolean waitPlayerDistToGob(Gob gob, int dist) {
        //println("waitPlayerDistToGob "+dist);
        if (ZeeConfig.distanceToPlayer(gob) > dist) {
            try {
                long idleMs = 0;
                while (idleMs < 1000 && ZeeConfig.distanceToPlayer(gob) > dist) {
                    Thread.sleep(SLEEP_MS);
                    if (!ZeeConfig.isPlayerMoving()) {
                        idleMs += SLEEP_MS;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //println("waitPlayerDistToGob ret "+(ZeeConfig.distanceToPlayer(gob) <= dist)+"  "+ZeeConfig.distanceToPlayer(gob));
        return ZeeConfig.distanceToPlayer(gob) <= dist;
    }


    public static boolean waitPlayerDismounted(Gob mount) {
        //println("waitPlayerDismounted");
        long timer = 1000;
        try {
            while( timer > 0 ) {
                if (!ZeeConfig.isPlayerSharingGobCoord(mount))
                    break;
                else
                    timer -= PING_MS;
                Thread.sleep(PING_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitPlayerDismounted ret "+!ZeeConfig.isPlayerSharingGobCoord(mount));
        return !ZeeConfig.isPlayerSharingGobCoord(mount);
    }


    public static boolean waitPlayerMounted(Gob mount) {
        //println("waitPlayerMounted");
        long timer = TIMEOUT_MS;
        try {
            while( timer > 0 ) {
                if (ZeeConfig.isPlayerSharingGobCoord(mount))
                    break;
                if(ZeeConfig.isPlayerMoving()){
                    timer = TIMEOUT_MS; //reset timer if player moving
                } else {
                    timer -= PING_MS;
                }
                Thread.sleep(PING_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitPlayerMounted ret "+ZeeConfig.isPlayerSharingGobCoord(mount));
        return ZeeConfig.isPlayerSharingGobCoord(mount);
    }


    public static boolean waitPlayerIdleOrHoldingItem() {
        //println("waitPlayerIdleOrHoldingItem");
        boolean pidle = false;
        try {
            ZeeConfig.prepareCancelClick();
            while( !ZeeConfig.isCancelClick()  &&  !ZeeConfig.isPlayerHoldingItem()) {
                Thread.sleep(PING_MS);
                // is mounted player idle
                if (ZeeConfig.isPlayerMountingHorse()){
                    //horse idle
                    if (ZeeConfig.gobHasAnyPose(ZeeConfig.getPlayerMountedHorse(), ZeeConfig.POSE_HORSE_IDLE)){
                        //player non active
                        if (!ZeeConfig.playerHasAnyPose(ZeeConfig.arrayPlayerActivePoses)){
                            pidle = true;
                            break;
                        }
                    }
                }
                // non mounted player idle
                else{
                    if (ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_IDLE)){
                        pidle = true;
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitPlayerIdleOrHoldingItem ret "+(pidle || ZeeConfig.isPlayerHoldingItem()));
        return  pidle || ZeeConfig.isPlayerHoldingItem();
    }


    public static boolean waitCursorName(String name) {
        //println("wait cursor "+name);
        int max = (int) TIMEOUT_MS*2;
        try {
            while(max>0 && !ZeeConfig.getCursorName().equals(name)) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("wait cursor "+name+" = "+ZeeConfig.getCursorName().equals(name));
        return ZeeConfig.getCursorName().contentEquals(name);
    }

    public static boolean waitItemRes(GItem g) {
        if (g==null)
            return false;
        int max = (int) TIMEOUT_MS;
        try {
            while(max>0 && g.getres()==null) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (g.getres() != null);
    }

    public static boolean waitNextInvItem(){
        long startMs = now();
        long timeout = 15000;
        while (startMs > ZeeConfig.lastInvGItemCreatedMs && timeout > 0){
            try {
                sleep(50);
                timeout -= 50;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return (startMs <= ZeeConfig.lastInvGItemCreatedMs);
    }

    public static GItem waitInvItemOrCancelClick(){
        long startMs = now();
        prepareCancelClick();
        try {
            while ( !isCancelClick()  &&  startMs > ZeeConfig.lastInvGItemCreatedMs) {
                sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isCancelClick())
            return null;
        return ZeeConfig.lastInvGItemCreated;
    }


    static int invFreeSlots, lastInvFreeSlots;
    public static boolean waitInvFull(Inventory inv) {
        //println("wait inv full");
        int timer = (int) TIMEOUT_MS;
        try {
            lastInvFreeSlots = invFreeSlots = inv.getNumberOfFreeSlots();
            while( timer > 0  &&  (invFreeSlots = inv.getNumberOfFreeSlots()) > 0 ) {
                if(lastInvFreeSlots != invFreeSlots) {
                    // reset timer if free slots changed
                    timer = (int) TIMEOUT_MS;
                    lastInvFreeSlots = invFreeSlots;
                }else {
                    timer -= SLEEP_MS;
                }
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return inv.getNumberOfFreeSlots() == 0;
    }
    public static boolean waitInvFull(Inventory inv, int freeSlots) {
        //println("wait inv full");
        int timer = (int) TIMEOUT_MS;
        try {
            lastInvFreeSlots = invFreeSlots = inv.getNumberOfFreeSlots();
            while( timer > 0  &&  (invFreeSlots = inv.getNumberOfFreeSlots()) > freeSlots ) {
                if(lastInvFreeSlots != invFreeSlots) {
                    // reset timer if free slots changed
                    timer = (int) TIMEOUT_MS;
                    lastInvFreeSlots = invFreeSlots;
                }else {
                    timer -= SLEEP_MS;
                }
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return inv.getNumberOfFreeSlots() <= freeSlots;
    }

    public static boolean waitInvFreeSlotsIdle() {
        return waitInvFreeSlotsIdleSec(2);//TODO use 3 if necessary
    }

    public static boolean waitInvFreeSlotsIdleSec(int idleSec) {
        return waitInvFreeSlotsIdleSec(ZeeConfig.getMainInventory(),idleSec);
    }

    public static boolean waitInvFreeSlotsIdleSec(Inventory inv, int idleSec) {
        long timerMs = idleSec*1000;
        //println("waitInvFreeSlotsIdleSec "+timerMs+"ms");
        try {
            lastInvFreeSlots = inv.getNumberOfFreeSlots();
            while( timerMs > 0 ){
                invFreeSlots = inv.getNumberOfFreeSlots();
                if(lastInvFreeSlots != invFreeSlots) {
                    // reset timer if free slots changed
                    timerMs = idleSec*1000;
                    lastInvFreeSlots = invFreeSlots;
                }else {
                    // freeslots didnt change
                    timerMs -= 500;
                }
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitInvFreeSlotsIdleMs ret "+(timerMs <= 0)+" "+timerMs+"ms");
        return timerMs <= 0;
    }

    public static boolean waitInvIdleMs(long idleMs) {
        //println("waitInvIdleMs "+idleMs);
        long timeElapsed = 0;
        try {
            //wait approaching item
            if(waitPlayerIdleFor(1)) {
                // wait inventory idle for idleMs
                while ((timeElapsed = now() - ZeeConfig.lastInvGItemCreatedMs) < idleMs) {
                    Thread.sleep(SLEEP_MS);
                }
            }
            else
                println("waitInvIdleMs > failed player idle");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitInvIdleMs ret "+(timeElapsed >= idleMs));
        return timeElapsed >= idleMs;
    }

    public static boolean waitInvFullOrHoldingItem(Inventory inv, int timeOutMs) {
        //println("wait inv full or holding item2");
        if(ZeeConfig.isPlayerHoldingItem())
            return true;
        int timer = timeOutMs;
        try {
            lastInvFreeSlots = invFreeSlots = inv.getNumberOfFreeSlots();
            while( timer > 0 && !ZeeConfig.isPlayerHoldingItem()  &&  (invFreeSlots = inv.getNumberOfFreeSlots()) > 0 ) {
                if(lastInvFreeSlots != invFreeSlots) {
                    // reset timer if free slots changed
                    timer = timeOutMs;
                    lastInvFreeSlots = invFreeSlots;
                }else{
                    timer -= SLEEP_MS;//TODO test PING_MS
                }
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("hasFreeslots="+(inv.getNumberOfFreeSlots()>0)+"  holdingItem="+ZeeConfig.isPlayerHoldingItem());
        if(ZeeConfig.isPlayerHoldingItem())
            return true;
        return inv.getNumberOfFreeSlots() == 0;
    }

    public static FlowerMenu waitFlowerMenu() {
        long max = TIMEOUT_MS;
        FlowerMenu fm = null;
        try {
            while(max>0 && (fm = ZeeConfig.gameUI.ui.root.getchild(FlowerMenu.class)) == null) {
                max -= SLEEP_MS;
                sleep(SLEEP_MS);
            }
            sleep(SLEEP_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("wait flowermenu = "+fm);
        return fm;
    }

    public static boolean waitNoFlowerMenu() {
        int max = (int) TIMEOUT_MS;
        FlowerMenu fm = null;
        try {
            while(max>0 && (fm = ZeeConfig.gameUI.ui.root.getchild(FlowerMenu.class)) != null) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("wait no flowermenu = "+fm);
        return (fm == null);
    }

    public static boolean waitMapClick(){
        boolean ret;
        long lastClick = now();
        while ( ZeeConfig.lastMapViewClickMs < lastClick ) {
            try {
                sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    //  parameters combination may vary depending on player task
    public static boolean waitStaminaIdleMs(long playerIdleMs, double stamChangeActive, long threadSleepMs) {
        long timeoutMs = playerIdleMs;
        double lastStam, stam, absChange;
        try {
            stam = lastStam = ZeeConfig.getMeterStamina();
            while(timeoutMs > 0) {
                absChange = Math.abs(lastStam - stam);
                //println(""+absChange);
                if( absChange >= stamChangeActive) // if stamina changed...
                    timeoutMs = playerIdleMs; // ...restore timeout
                else
                    timeoutMs -= threadSleepMs;
                Thread.sleep(threadSleepMs);
                lastStam = stam;
                stam = ZeeConfig.getMeterStamina();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitStaminaIdleMs > ret="+(timeoutMs <= 0)+" "+timeoutMs);
        return (timeoutMs <= 0);
    }

    public static boolean waitStaminaIdleMs(long idleMs) {
        long timeoutMs = idleMs;
        double lastStam, stam;
        try {
            stam = lastStam = ZeeConfig.getMeterStamina();
            while(timeoutMs > 0) {
                if(Math.abs(lastStam - stam) > 1) // if stamina changed...
                    timeoutMs = idleMs; // ...restore timeout
                else
                    timeoutMs -= PING_MS;
                Thread.sleep(PING_MS);
                lastStam = stam;
                stam = ZeeConfig.getMeterStamina();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitStaminaIdleMs > ret="+(timeoutMs <= 0));
        return (timeoutMs <= 0);
    }

    static boolean waitWindowClosed(String windowName){
        if (ZeeConfig.getWindow(windowName)==null) {
            //println("waitWindowClosed > already closed");
            return true;
        }
        long timeoutMs = 3000;
        try{
            while(timeoutMs>0 && ZeeConfig.getWindow(windowName)!=null){
                timeoutMs -= 50;
                Thread.sleep(50);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitWindowClosed > timeoutMs="+timeoutMs);
        return timeoutMs > 0;
    }

    static boolean waitWindowOpenedNameEndsWith(String windowNameEndsWith){
        if (!ZeeConfig.getWindowsNameEndsWith(windowNameEndsWith).isEmpty()) {
            //println("waitWindowOpened > already opened");
            return true;
        }
        long timeoutMs = 3000;
        try{
            while(timeoutMs>0 && ZeeConfig.getWindowsNameEndsWith(windowNameEndsWith).isEmpty()){
                timeoutMs -= 50;
                Thread.sleep(50);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitWindowOpened > timeoutMs="+timeoutMs);
        return timeoutMs > 0;
    }

    static boolean waitNoHourglass(){
        //println("waitNoHourglass");
        try {
            prepareCancelClick();
            while(!isCancelClick() && ZeeConfig.getUiProgressHourglassWidget() != null){
                sleep(50);
            }
            sleep(50);//ui lag?
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("    ret "+(ZeeConfig.getUiProgressHourglassWidget() == null));
        return ZeeConfig.getUiProgressHourglassWidget() == null;
    }

    static Window waitWindowBuildOpened(){
        if (ZeeConfig.getWindowBuild()!=null) {
            //println("waitWindowBuildOpened > already opened");
            return null;
        }
        Window ret = null;
        try{
            prepareCancelClick();
            while(!isCancelClick() && (ret=ZeeConfig.getWindowBuild())==null){
                Thread.sleep(PING_MS);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitWindowBuildOpened > "+ret);
        return ret;
    }

    static Window lastWindowOpened = null;
    static Window waitWindowOpened(){
        Window ret;
        lastWindowOpened = null;
        try{
            prepareCancelClick();
            while(!isCancelClick() && lastWindowOpened==null){
                Thread.sleep(100);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ret = lastWindowOpened;
        lastWindowOpened = null;
        return ret;
    }

    static boolean waitWindowOpened(String windowName){
        if (ZeeConfig.getWindow(windowName)!=null) {
            //println("waitWindowOpened > already opened");
            return true;
        }
        try{
            prepareCancelClick();
            while(!isCancelClick() && ZeeConfig.getWindow(windowName)==null){
                Thread.sleep(50);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitWindowOpened > "+(!isCancelClick()));
        return !isCancelClick();
    }

    static boolean waitWindowOpenedNameContains(String windowNameContains){
        if (ZeeConfig.getWindowNameContains(windowNameContains)!=null) {
            //println("waitWindowOpened > already opened");
            return true;
        }
        long timeoutMs = 3000;
        try{
            while(timeoutMs>0 && ZeeConfig.getWindowNameContains(windowNameContains)==null){
                timeoutMs -= 50;
                Thread.sleep(50);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitWindowOpened > timeoutMs="+timeoutMs);
        return timeoutMs > 0;
    }

    public static boolean waitGobRemovedOrCancelClick(Gob gob){
        try{
            //println("waitGobRemoved > enter loop");
            while(!ZeeConfig.isGobRemoved(gob) && !ZeeConfig.isCancelClick()){
                Thread.sleep(1000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitGobRemoved > ret="+ZeeConfig.isGobRemoved(gob));
        return ZeeConfig.isGobRemoved(gob);
    }

    public static boolean waitGobRemovedSeconds(Gob gob, int timeoutSeconds){
        long timeoutMs = timeoutSeconds * 1000L;
        try{
            while(timeoutMs>=0 && !ZeeConfig.isGobRemoved(gob)){
                timeoutMs -= PING_MS;
                Thread.sleep(PING_MS);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        println("waitGobRemovedSeconds > ret="+ZeeConfig.isGobRemoved(gob)+", timeoutMs="+timeoutMs);
        return ZeeConfig.isGobRemoved(gob);
    }

    public static FlowerMenu getFlowerMenu() {
        return ZeeFlowerMenu.getFlowerMenu();
    }
    public static void cancelFlowerMenu(){
        ZeeFlowerMenu.cancelFlowerMenu();
    }

    public static boolean choosePetal(FlowerMenu menu, int petalIndex) {
        try {
            synchronized (menu) {
                menu.choose(menu.opts[petalIndex]);
                menu.destroy();
                return true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean choosePetal(FlowerMenu menu, String petalName) {
        synchronized (menu.flag) {
            for (FlowerMenu.Petal p : menu.opts) {
                if (p.name.equalsIgnoreCase(petalName)) {
                    try {
                        menu.choose(p);
                        menu.destroy();
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            }
        }
        println("no petal named "+petalName);
        return false;
    }

    public static boolean choosePetalNameStartsWith(FlowerMenu menu, String petalNameStartsWith) {
        for(FlowerMenu.Petal p : menu.opts) {
            if(p.name.startsWith(petalNameStartsWith)) {
                try {
                    synchronized (menu) {
                        menu.choose(p);
                        menu.destroy();
                        return true;
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
        return false;
    }

    public static void clickButtonUntilMsgOrHoldingItem(Button button, String playerText) {
        new ZeeThread(){
            public void run() {
                long diffUiMsgMs, diffWarningMs;
                Window w = button.getparent(Window.class);
                int windowId = w.wdgid();
                try {
                    ZeeConfig.addPlayerText(playerText);
                    ZeeConfig.lastHafenWarningMs = ZeeConfig.lastUIMsgMs = 0;
                    diffWarningMs = diffUiMsgMs = now() - ZeeConfig.lastUIMsgMs;
                    while (!ZeeConfig.isPlayerHoldingItem() && diffUiMsgMs>500 && diffWarningMs>500 && windowId!=-1){
                        button.click();
                        sleep(PING_MS);
                        diffUiMsgMs = now() - ZeeConfig.lastUIMsgMs;
                        diffWarningMs = now() - ZeeConfig.lastHafenWarningMs;
                        windowId = w.wdgid();
                    }
                    //println("done, holding="+ZeeConfig.isPlayerHoldingItem()+", diffUiMsgMs="+diffUiMsgMs+", diffWarningMs="+diffWarningMs+"  winId="+windowId);
                }catch (Exception e){
                    e.printStackTrace();
                }
                ZeeConfig.removePlayerText();
            }
        }.start();
    }

    static long now() {
        return System.currentTimeMillis();
    }

    static boolean isCancelClick() {
        return ZeeConfig.isCancelClick();
    }
    static void doCancelClick() {
        ZeeConfig.simulateCancelClick();
    }
    static void prepareCancelClick() {
        ZeeConfig.prepareCancelClick();
    }

    public static void println(String s) {
        System.out.println(s);
    }
    public static void println(int i) {
        System.out.println(i);
    }
}

package haven;

import java.util.ArrayList;
import java.util.List;

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
                        lastStam = ZeeConfig.getStamina();
                        sleep(1000);
                        stamChangeSec = ZeeConfig.getStamina() - lastStam ;
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
            while(max>0 && !ZeeManagerItemClick.isItemInHandSlot(name)) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("waitItemEquipped > ret "+ZeeClickItemManager.isItemEquipped(name));
        return ZeeManagerItemClick.isItemInHandSlot(name);
    }

    public static boolean waitPlayerMove() {
        //println("wait player move");
        int max = (int) TIMEOUT_MS;
        try {
            while(max>0 && !ZeeConfig.isPlayerMoving()) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ZeeConfig.isPlayerMoving();
    }

    public static boolean waitPlayerStop() {
        //println("wait player stop");
        int max = (int) TIMEOUT_MS;
        try {
            while(max>0 && ZeeConfig.isPlayerMoving()) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return !ZeeConfig.isPlayerMoving();
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

    public static boolean waitPlayerIdlePoseMs(long idleMs){
        if (ZeeConfig.isPlayerMountingHorse())
            return waitPlayerIdleMountedMs(idleMs);
        else
            return waitPlayerPoseInListTimeout(idleMs, ZeeConfig.POSE_PLAYER_IDLE, ZeeConfig.POSE_PLAYER_KICKSLED_IDLE);
    }


    public static boolean waitPlayerIdlePose(){
        // player mounting horse
        if (ZeeConfig.isPlayerMountingHorse()) {
            return waitPlayerPose(ZeeConfig.POSE_PLAYER_RIDING_IDLE);
        }
        // player on kicksled
        else if (ZeeConfig.isPlayerDrivingingKicksled()) {
            return waitPlayerIdleOnKicksled();
        }
        // player on foot?
        else {
            return waitPlayerPose(ZeeConfig.POSE_PLAYER_IDLE);
        }
    }

    //TODO add more active poses
    public static boolean waitPlayerIdleOnKicksled() {
        return waitPlayerPoseNotInList(
                ZeeConfig.POSE_PLAYER_DRINK,
                ZeeConfig.POSE_PLAYER_CHOPTREE,
                ZeeConfig.POSE_PLAYER_DIGSHOVEL,
                ZeeConfig.POSE_PLAYER_PICK,
                ZeeConfig.POSE_PLAYER_SAW
        );
    }

    public static boolean waitPlayerIdleMountedMs(long idleMs) {
        return waitPlayerPoseNotInListTimeout(
                idleMs,
                ZeeConfig.POSE_PLAYER_DRINK,
                ZeeConfig.POSE_PLAYER_CHOPTREE,
                ZeeConfig.POSE_PLAYER_DIGSHOVEL,
                ZeeConfig.POSE_PLAYER_PICK,
                ZeeConfig.POSE_PLAYER_SAW
        );
    }


    public static boolean waitPlayerPoseInList(String ... poseList) {
        println(">waitPlayerPoseInList");
        List<String> playerPoses;
        boolean exit = false;
        try{
            do{
                sleep(PING_MS*2);
                playerPoses = ZeeConfig.getPlayerPoses();
                exit = false;
                for (int i = 0; i < poseList.length; i++) {
                    if (playerPoses.contains(poseList[i])){
                        println(poseList[i]+" > "+playerPoses);
                        exit = true;
                        break;
                    }
                }
            }while(!exit);
        }catch (Exception e){
            e.printStackTrace();
        }
        println("waitPlayerPoseInList ret "+exit);
        return exit;
    }

    // TODO test
    public static boolean waitPlayerPoseInListTimeout(long timeoutMs, String... poseList) {
        //println(">waitPlayerPoseInListTimeout");
        List<String> playerPoses;
        boolean exit = false;
        boolean poseInList = false;//assume pose is in list
        long elapsedTimeMs = 0;
        long sleepMs = PING_MS * 2;
        try{
            do{
                sleep(sleepMs);
                elapsedTimeMs += sleepMs;
                if (elapsedTimeMs >= timeoutMs) {
                    poseInList = false;//if timeout assume pose not in list
                    break;
                }
                else {
                    playerPoses = ZeeConfig.getPlayerPoses();
                    exit = false;
                    poseInList = false;
                    for (int i = 0; i < poseList.length; i++) {
                        //if contains pose then break and return true
                        if (playerPoses.contains(poseList[i])) {
                            exit = true;
                            poseInList = true;
                            break;
                        }
                    }
                    if (poseInList)
                        exit = true;
                }
            }while(!exit);
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitPlayerPoseInListTimeout ret "+poseInList);
        return poseInList;
    }


    public static boolean waitPlayerPoseNotInList(String ... forbiddenPoses) {
        println(">waitPlayerPoseNotInList");
        List<String> currentPoses;
        boolean exit = false;
        try{
            ZeeConfig.lastMapViewClickButton = 2;//prepare for cancel click
            do{
                sleep(PING_MS*2);
                exit = ZeeConfig.isTaskCanceledByGroundClick();
                if(exit) {
                    println("    canceled by click");
                    break;
                }
                currentPoses = ZeeConfig.getPlayerPoses();
                //println("   "+playerPoses);
                exit = true;
                for (int i = 0; i < forbiddenPoses.length; i++) {
                    println("      "+forbiddenPoses[i]);
                    if (currentPoses.contains(forbiddenPoses[i])){//if contains pose...
                        println("         break");
                        exit = false;
                        break;//... break, loop and sleep again
                    }
                }
            }while(!exit);//ZeeConfig.isPlayerMoving() );
        }catch (Exception e){
            e.printStackTrace();
        }
        println("waitPlayerPoseNotInList  exit="+exit + "  cancel="+ZeeConfig.isTaskCanceledByGroundClick());
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
        //println("waitPlayerPoseNotInListTimeout ret "+!poseInList);
        return !poseInList;
    }

    public static boolean waitNotPlayerPose(String poseName) {
        List<String> poses = new ArrayList<>();
        try{
            ZeeConfig.lastMapViewClickButton = 2;//prepare for cancel click
            do{
                //println("waitNotPlayerPose > "+poses);
                sleep(PING_MS*2);
            } while (
                (poses = ZeeConfig.getPlayerPoses()).contains(poseName)
                && !ZeeConfig.isTaskCanceledByGroundClick()
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("waitNotPlayerPose ret "+(!poses.contains(poseName))+" , "+poseName);
        return !poses.contains(poseName);
    }

    public static boolean waitGobPose(Gob gob, String poseName) {
        //println("waitGobPose > start");
        List<String> poses = new ArrayList<>();
        try{
            ZeeConfig.lastMapViewClickButton = 2;//prepare for cancel click
            do{
                //println("waitGobPose > "+poses);
                sleep(PING_MS*2);
            } while (
                    !(poses = ZeeConfig.getGobPoses(gob)).contains(poseName)
                            && !ZeeConfig.isTaskCanceledByGroundClick()
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        //println("      return "+(poses.contains(poseName))+" "+poseName);
        return poses.contains(poseName);
    }

    public static boolean waitPlayerPose(String poseName) {
        return waitGobPose(ZeeConfig.getPlayerGob(),poseName);
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


    public static boolean waitPlayerIdleOrHoldingItem(int idleSeconds) {
        //println("waitPlayerIdleFor "+idleSeconds+"s");
        staminaMonitorStart();
        long timer = idleSeconds * 1000;
        try {
            while( timer > 0  &&  !ZeeConfig.isPlayerHoldingItem()) {
                if(stamChangeSec!=0 || ZeeConfig.isPlayerMoving()){
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
        return timer <= 0  ||  ZeeConfig.isPlayerHoldingItem();
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
        return ZeeConfig.getCursorName().equals(name);
    }

    public static boolean waitRes(GItem g) {
        println("wait res gitem ");
        int max = (int) TIMEOUT_MS;
        try {
            while(max>0 && (g==null || g.resource()==null || g.resource().basename()==null)) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (g!=null && g.resource()!=null && g.resource().basename()!=null);
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
        long timeout = 5000;
        long startingMs;
        try {
            //wait first item, timeout 5s
            ZeeConfig.lastInvItemMs = 0;
            startingMs = now();
            while (timeout >= 0  &&  startingMs > ZeeConfig.lastInvItemMs){
                Thread.sleep(SLEEP_MS);
                timeout -= SLEEP_MS;
            }
            if (timeout <= 0){
                println("waitInvIdleMs timeout");
                return false;
            }
            // wait inventory idle for idleMs
            while( (timeElapsed = now()-ZeeConfig.lastInvItemMs) < idleMs ) {
                Thread.sleep(SLEEP_MS);
            }
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

    public static boolean waitFlowerMenu() {
        long max = TIMEOUT_MS;
        FlowerMenu fm = null;
        try {
            while(max>0 && (fm = ZeeConfig.gameUI.ui.root.getchild(FlowerMenu.class)) == null) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //println("wait flowermenu = "+fm);
        return (fm != null);
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

    //  parameters combination may vary depending on player task
    public static boolean waitStaminaIdleMs(long playerIdleMs, double stamChangeActive, long threadSleepMs) {
        long timeoutMs = playerIdleMs;
        double lastStam, stam, absChange;
        try {
            stam = lastStam = ZeeConfig.getStamina();
            while(timeoutMs > 0) {
                absChange = Math.abs(lastStam - stam);
                //println(""+absChange);
                if( absChange >= stamChangeActive) // if stamina changed...
                    timeoutMs = playerIdleMs; // ...restore timeout
                else
                    timeoutMs -= threadSleepMs;
                Thread.sleep(threadSleepMs);
                lastStam = stam;
                stam = ZeeConfig.getStamina();
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
            stam = lastStam = ZeeConfig.getStamina();
            while(timeoutMs > 0) {
                if(Math.abs(lastStam - stam) > 1) // if stamina changed...
                    timeoutMs = idleMs; // ...restore timeout
                else
                    timeoutMs -= PING_MS;
                Thread.sleep(PING_MS);
                lastStam = stam;
                stam = ZeeConfig.getStamina();
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

    static boolean waitWindowOpened(String windowName){
        if (ZeeConfig.getWindow(windowName)!=null) {
            //println("waitWindowOpened > already opened");
            return true;
        }
        long timeoutMs = 3000;
        try{
            while(timeoutMs>0 && ZeeConfig.getWindow(windowName)==null){
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
            while(!ZeeConfig.isGobRemoved(gob) && !ZeeConfig.isTaskCanceledByGroundClick()){
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
        return ZeeConfig.gameUI.ui.root.getchild(FlowerMenu.class);
    }

    public static boolean choosePetal(FlowerMenu menu, String petalName) {
        for(FlowerMenu.Petal p : menu.opts) {
            if(p.name.equals(petalName)) {
                try {
                    menu.choose(p);
                    menu.destroy();
                    return true;
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

    public static void println(String s) {
        System.out.println(s);
    }
    public static void println(int i) {
        System.out.println(i);
    }
}

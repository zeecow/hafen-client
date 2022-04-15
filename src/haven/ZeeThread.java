package haven;

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

    public static boolean waitItemEquipped(String name) {
        int max = (int) TIMEOUT_MS;
        try {
            while(max>0 && !ZeeClickItemManager.isItemEquipped(name)) {
                max -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ZeeClickItemManager.isItemEquipped(name);
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


    public static boolean waitCursor(String name) {
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
            //wait first item
            startingMs = ZeeConfig.lastInvItemMs = now();
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
                    timer -= SLEEP_MS;
                }
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        println("waitStaminaIdleMs > ret="+(timeoutMs <= 0));
        return (timeoutMs <= 0);
    }

    public static boolean waitGobRemoved(Gob gob){
        try{
            println("waitGobRemoved > enter loop");
            while(!ZeeConfig.isGobRemoved(gob)){
                Thread.sleep(1000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        println("waitGobRemoved > ret="+ZeeConfig.isGobRemoved(gob));
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

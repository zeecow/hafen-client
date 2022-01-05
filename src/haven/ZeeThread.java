package haven;

public class ZeeThread  extends Thread{

    static final long SLEEP_MS = 50;
    static final long TIMEOUT_MS = 2000;
    static final long LONG_CLICK_MS = 333;
    static final long PING_MS = 250;

    public static boolean waitNotHoldingItem() {
        int max = (int) TIMEOUT_MS;
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
        int max = (int) TIMEOUT_MS;
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
    public static boolean waitPlayerIdleFor(long idleMS) {
        //println("wait player idle");
        long timer = idleMS;
        try {
            while( timer > 0 ) {
                if(ZeeConfig.isPlayerMoving() || ZeeConfig.isPlayerDrinking()){
                    timer = idleMS; //reset timer if player moving or dringing
                }
                timer -= SLEEP_MS;
                Thread.sleep(SLEEP_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return timer <= 0;
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
        println("wait cursor "+name+" = "+ZeeConfig.getCursorName().equals(name));
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

    public static boolean waitPlayerIdle(){
        println("waitPlayerIdle()");
        int max = (int) TIMEOUT_MS * 3;
        int counter = max;
        try {
            while(counter > 0) {
                if(ZeeConfig.isPlayerMoving())
                    counter = max; //reset
                else
                    counter -= PING_MS;
                println("idle counter "+counter);
                Thread.sleep(PING_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return counter <= 0;
    }

    static int invFreeSlots, lastInvFreeSlots;
    public static boolean waitInvFull(Inventory inv) {
        println("wait inv full");
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

    public static boolean waitFlowerMenu() {
        int max = (int) TIMEOUT_MS;
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

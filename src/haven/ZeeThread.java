package haven;

public class ZeeThread  extends Thread{

    static final long SLEEP_MS = 50;
    static final long TIMEOUT_MS = 2000;
    static final long LONG_CLICK_MS = 333;

    public static boolean waitFreeHand() {
        int max = (int) TIMEOUT_MS;
        while(max>0 && ZeeConfig.gameUI.vhand!=null) {
            max -= SLEEP_MS;
            try { Thread.sleep(SLEEP_MS); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        if(max<=0)
            return false;
        return true;
    }

    public static boolean waitOccupiedHand() {
        int max = (int) TIMEOUT_MS;
        while(max>0 && ZeeConfig.gameUI.vhand==null) {
            max -= SLEEP_MS;
            try { Thread.sleep(SLEEP_MS); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        if(max<=0)
            return false;
        return true;
    }

    public static boolean waitItemEquipped(String name) {
        int max = (int) TIMEOUT_MS;
        while(max>0 && !ZeeClickItemManager.isItemEquipped(name)) {
            max -= SLEEP_MS;
            try { Thread.sleep(SLEEP_MS); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        if(max<=0)
            return false;
        return true;
    }
}

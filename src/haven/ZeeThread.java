package haven;

public class ZeeThread  extends Thread{

    static final long SLEEP_MS = 50;
    static final long TIMEOUT_MS = 2000;
    static final long LONG_CLICK_MS = 333;

    public static boolean waitFreeHand() {
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

    public static boolean waitOccupiedHand() {
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

    public static void println(String s) {
        System.out.println(s);
    }
}

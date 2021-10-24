package haven;

/*
mid-click gob for automating small tasks
 */
public class ZeeClickGobManager extends Thread{
    private final int btn;
    private final Gob gob;
    private final String gobName;

    public ZeeClickGobManager(int clickb, Gob clickGob) {
        this.btn = clickb;
        this.gob = clickGob;
        this.gobName = gob.getres().name;
    }

    @Override
    public void run() {
        if(isGobFireSource()){
            if(pickupTorch()){
                gobItemAct(0);
            }
        }
    }

    private boolean pickupTorch() {
        WItem witem = ZeeEquipManager.getInvBelt().getWItemsByName("torch").get(0);
        return ZeeEquipManager.pickUpItem(witem);
    }

    private boolean isGobFireSource() {
        String[] items = {"/brazier","/pow","/snowlantern"};
        for (int i = 0; i < items.length; i++) {
            if (gobName.contains(items[i])){
                return true;
            }
        }
        return false;
    }

    /**
     * Itemact with gob, to fill trough with item in hand for example
     * @param mod 1 = shift, 2 = ctrl, 4 = alt
     */
    public void gobItemAct(int mod) {
        ZeeConfig.gameUI.map.wdgmsg("itemact", Coord.z, gob.rc.floor(OCache.posres), mod, 0, (int) gob.id, gob.rc.floor(OCache.posres), 0, -1);
    }

    /**
     * Click the gob
     * @param btn 1 = left, 2 = middle, 3 = right
     * @param mod Key modifier mask 1 = shift 2 = ctrl 4 = alt
     * @param meshid can be a door, roasting spit etc.
     * @param olid gob overlay to click, for example roasting spit
     */
    public void gobClick(int btn, int mod, int meshid, int olid) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), gob.rc.floor(OCache.posres), btn, mod, 1, (int)gob.id, gob.rc.floor(OCache.posres), olid, meshid);
    }

}

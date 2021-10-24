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
        //System.out.println(gobName);
    }

    @Override
    public void run() {
        if(isGobFireSource()){
            if(ZeeEquipManager.pickupBeltItem("torch")){
                gobItemAct(0);
            }
        }else if(isGobHorse()){
            clickPetal("Giddyup!");
        }else if(isGobName("dreca")){//dream catcher
            clickPetal("Harvest");
        }else if(isGobTrellisPlant()){
            destroyGob();
        }else if(isGobTreeStump()){
            ZeeEquipManager.equipItem("shovel");
            destroyGob();
        }else if(isGobBush()){
            ZeeEquipManager.equipItem("shovel");
            liftGob();
        }else if(isLiftableGob()){//last check
            liftGob();
        }
    }

    private boolean isLiftableGob() {
        return isGobInList("/meatgrinder,/potterswheel,/iconsign,/rowboat,/dugout,/wheelbarrow,/compostbin,/gardenpot,/beehive,/htable,/bed-sturdy,/boughbed,/alchemiststable,/gemwheel,/ancestralshrine,/spark,/cauldron,/churn,/table-rustic,/chair-rustic,/royalthrone,/trough,curdingtub,/plow,/barrel,/still,log,/oldtrunk,chest,/anvil,/cupboard,/studydesk,/bumlings/,/demijohn,/quern,/wreckingball-fold,/loom,/swheel,/ttub,/cheeserack,/archerytarget");
    }

    private boolean isGobBush() {
        return gobName.startsWith("gfx/terobjs/bushes");
    }

    private boolean isGobTreeStump() {
        return gobName.startsWith("gfx/terobjs/trees/") && gobName.endsWith("stump");
    }

    private void destroyGob() {
        ZeeConfig.gameUI.menu.wdgmsg("act","destroy",0);
        gobClick(1);
    }

    private void liftGob() {
        ZeeConfig.gameUI.menu.wdgmsg("act","carry",0);
        gobClick(1);
    }

    private boolean isGobTrellisPlant() {
        return isGobInList("hops,pepper");
    }

    private boolean isGobInList(String list) {
        String[] names = list.split(",");
        for (int i = 0; i < names.length; i++) {
            if (gobName.endsWith(names[i])){
                return true;
            }
        }
        return false;
    }


    private boolean isGobName(String name) {
        return gobName.endsWith(name);
    }

    private void clickPetal(String petalName) {
        ZeeConfig.scheduleClickPetal(petalName);
        gobClick(3);
    }

    private boolean isGobHorse() {
        return isGobInList("stallion,mare,horse");
    }

    private boolean isGobFireSource() {
        return isGobInList("brazier,pow,snowlantern");
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
    public void gobClick(int btn) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), gob.rc.floor(OCache.posres), btn, 0, 1, (int)gob.id, gob.rc.floor(OCache.posres), -1, -1);
    }
}

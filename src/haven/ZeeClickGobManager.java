package haven;


/*
    Mid-click gob for automating small tasks.
    Short click: inspect, light torch, mount horse.
    Long click: lift, destroy.
 */
public class ZeeClickGobManager extends Thread{
    private final int btn;
    private final Gob gob;
    private final String gobName;

    public static float camAngleStart, camAngleEnd, camAngleDiff;
    public static long clickStartMs, clickEndMs, clickDiffMs;

    public ZeeClickGobManager(int clickb, Gob clickGob) {
        this.btn = clickb;
        this.gob = clickGob;
        this.gobName = gob.getres().name;
        clickDiffMs = clickEndMs - clickStartMs;
        //System.out.println(clickDiffMs+"ms > "+gobName);
    }

    @Override
    public void run() {
        if(!longClick()){
            /*
            short clicks
             */
            if(isGobGroundItem()) {
                gobClick(3, UI.MOD_SHIFT);//pick up all items (shift + rclick)
            } else if (isGobFireSource()) {
                if (ZeeClickItemManager.pickupBeltItem("torch")) {
                    gobItemAct(0);
                }
            } else if (isGobHorse()) {
                clickPetal("Giddyup!");
            } else if (isInspectGob()) {
                inspectGob();
            }

        } else {
            /*
            long clicks
             */
            if(isGobStockpile() || gobNameStartsWith("gfx/terobjs/dframe")) {
                gobClick(3, UI.MOD_SHIFT);//pick up all items (shift + rclick)
            } if (isDestroyGob()) {
                destroyGob();
            } else if (isLiftGob()) {
                liftGob();
            }
        }
    }

    private boolean isGobStockpile() {
        return gobName.startsWith("gfx/terobjs/stockpile");
    }

    private boolean isGobGroundItem() {
        return gobName.startsWith("gfx/terobjs/items/");
    }

    private boolean longClick() {
        return clickDiffMs > 250;
    }

    private boolean isInspectGob(){
        if(isGobTree() || isGobBush() || isGobBoulder())
            return true;
        String list = "/meatgrinder,/potterswheel,/well,/dframe,"
                +"/smelter,/crucible,/steelcrucible,/fineryforge,/kiln,/tarkiln,/oven,"
                +"/compostbin,/gardenpot,/beehive,/htable,/bed-sturdy,/boughbed,/alchemiststable,"
                +"/gemwheel,/spark,/cauldron,/churn,/chair-rustic,"
                +"/royalthrone,curdingtub,log,/still,/oldtrunk,/anvil,"
                +"/loom,/swheel,"
                +"/ttub,/cheeserack,/dreca,/glasspaneframe";
        return gobNameEndsWith(list);
    }

    private boolean isDestroyGob(){
        if(isGobTrellisPlant()){
            return true;
        }else if(isGobTreeStump()){
            ZeeClickItemManager.equipItem("shovel");
            return true;
        }
        return false;
    }

    private boolean isLiftGob() {
        if(isGobBush()) {
            ZeeClickItemManager.equipItem("shovel");
            return true;
        }
        if(isGobBoulder())
            return true;
        String endList = "/meatgrinder,/potterswheel,/iconsign,/rowboat,/dugout,/wheelbarrow,"
                +"/compostbin,/gardenpot,/beehive,/htable,/bed-sturdy,/boughbed,/alchemiststable,"
                +"/gemwheel,/ancestralshrine,/spark,/cauldron,/churn,/table-rustic,/chair-rustic,"
                +"/royalthrone,/trough,curdingtub,/plow,/barrel,/still,log,/oldtrunk,chest,/anvil,"
                +"/cupboard,/studydesk,/demijohn,/quern,/wreckingball-fold,/loom,/swheel,"
                +"/ttub,/cheeserack,/archerytarget,/dreca,/glasspaneframe";
        return gobNameEndsWith(endList);
    }

    private boolean isGobBoulder() {
        return gobName.startsWith("gfx/terobjs/bumlings/");
    }

    private boolean isGobBush() {
        return gobName.startsWith("gfx/terobjs/bushes");
    }

    private boolean isGobTreeStump() {
        return gobName.startsWith("gfx/terobjs/trees/") && gobName.endsWith("stump");
    }

    private boolean isGobTree() {
        return gobName.startsWith("gfx/terobjs/trees/") && !gobName.endsWith("log");
    }

    private boolean isGobTreeLog() {
        return gobName.startsWith("gfx/terobjs/trees/") && gobName.endsWith("log");
    }

    private void destroyGob() {
        ZeeConfig.gameUI.menu.wdgmsg("act","destroy","0");
        gobClick(1);
    }

    private void liftGob() {
        ZeeConfig.gameUI.menu.wdgmsg("act", "carry","0");
        gobClick(1);
    }

    private void inspectGob() {
        ZeeConfig.gameUI.menu.wdgmsg("act","inspect","0");
        gobClick(1);
    }

    private boolean isGobTrellisPlant() {
        return gobNameEndsWith("hops,pepper");
    }

    private boolean gobNameEndsWith(String list) {
        String[] names = list.split(",");
        for (int i = 0; i < names.length; i++) {
            if (gobName.endsWith(names[i])){
                return true;
            }
        }
        return false;
    }

    private boolean gobNameStartsWith(String list) {
        String[] names = list.split(",");
        for (int i = 0; i < names.length; i++) {
            if (gobName.startsWith(names[i])){
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
        return gobNameEndsWith("stallion,mare,horse");
    }

    private boolean isGobFireSource() {
        return gobNameEndsWith("brazier,pow,snowlantern");
    }

    /**
     * Itemact with gob, to fill trough with item in hand for example
     * @param mod 1 = shift, 2 = ctrl, 4 = alt
     */
    public void gobItemAct(int mod) {
        ZeeConfig.gameUI.map.wdgmsg("itemact", Coord.z, gob.rc.floor(OCache.posres), mod, 0, (int) gob.id, gob.rc.floor(OCache.posres), 0, -1);
    }

    public void gobClick(int btn) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), gob.rc.floor(OCache.posres), btn, 0, 1, (int)gob.id, gob.rc.floor(OCache.posres), -1, -1);
    }

    public void gobClick(int btn, int mod) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), gob.rc.floor(OCache.posres), btn, mod, 1, (int)gob.id, gob.rc.floor(OCache.posres), -1, -1);
    }
}

package haven;


import java.util.List;
import java.util.stream.Collectors;

/*
    Mid-click gob for automating small tasks.
    Short click: inspect, light torch, mount horse.
    Long click: lift, destroy.
 */
public class ZeeClickGobManager extends Thread{
    private static Coord2d clickCoord;
    private static Gob gob;
    private static String gobName;

    public static float camAngleStart, camAngleEnd, camAngleDiff;
    public static long clickStartMs, clickEndMs, clickDiffMs;

    public ZeeClickGobManager(Coord2d cc, Gob gobClicked) {
        clickCoord = cc;
        if(gobClicked!=null) {
            gob = gobClicked;
            gobName = gob.getres().name;
        }
        clickDiffMs = clickEndMs - clickStartMs;
        //System.out.println(clickDiffMs+"ms > "+gobName);
    }

    @Override
    public void run() {

        if(clickedTerrain()){
            //pickupClosestGob();
            //System.out.println("pickupClosestGob()");
            return;
        }

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
            } else if (isGobName("/barrel")) {
                gobClick(3,UI.MOD_SHIFT);//take from barrel
            } else if (isInspectGob()) {
                inspectGob();
            }

        } else {
            /*
            long clicks
             */
            if(isGobStockpile() || isGobName("/dframe")) {
                gobClick(3, UI.MOD_SHIFT);//pick up all items (shift + rclick)
            } if (isDestroyGob()) {
                destroyGob();
            } else if (isLiftGob()) {
                liftGob();
            }
        }
    }

    private void pickupClosestGob() {
        List<Gob> gobs = ZeeConfig.gameUI.ui.sess.glob.oc.gobStream().filter(gob1 -> {
            if (gob1 == null || gob1.getres()==null)
                return false;
            return gob1.getres().name.startsWith("gfx/terobjs/herbs/") ||
                    gob1.getres().name.startsWith("gfx/terobjs/items/");
        }).collect(Collectors.toList());
        if(gobs.size()==0) {
            System.out.println("no gobs herbs/item");
            return;
        }
        Gob closestGob = gobs.get(0);
        double closestDist = distanceCoordGob(clickCoord, closestGob);
        double dist;
        for (Gob g: gobs) {
            dist = distanceCoordGob(clickCoord, g);
            System.out.println(g.getres().name+" > "+dist);
            if(closestDist > dist) {
                closestDist = dist;
                closestGob = g;
            }
        }
        gobClick(closestGob,3);//pickup item (right click)
        System.out.println("closest = "+closestGob.getres().name+" > "+closestDist);
    }

    private boolean clickedTerrain() {
        return (gob==null);
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
            ZeeClickItemManager.waitFreeHand();
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
        ZeeClickItemManager.scheduleClickPetal(petalName);
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

    public void gobClick(Gob g, int btn) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), g.rc.floor(OCache.posres), btn, 0, 1, (int)g.id, g.rc.floor(OCache.posres), -1, -1);
    }

    public void gobClick(Gob g, int btn, int mod) {
        ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.getCenterScreenCoord(), g.rc.floor(OCache.posres), btn, mod, 1, (int)g.id, g.rc.floor(OCache.posres), -1, -1);
    }

    public static void printGobs(){
        List<String> gobs = ZeeConfig.gameUI.ui.sess.glob.oc.gobStream().map(gob -> gob.getres().name).collect(Collectors.toList());
        System.out.println(gobs.size()+" > "+gobs.toString());
    }

    public static double distanceToPlayer(Gob gob) {
        return ZeeConfig.gameUI.map.player().rc.dist(gob.rc);
    }

    public static double distanceCoordGob(Coord2d c, Gob gob) {
        return c.dist(gob.rc);
    }
}

package haven;

public class ZeeFlowerMenu extends FlowerMenu{

    public static final String STRPETAL_ADD12COAL = "Add 12 coal";
    public static final String STRPETAL_ADD9COAL = "Add 9 coal";
    public static final String STRPETAL_ADD4BRANCH = "Add 4 branch";
    public static final String STRPETAL_REMOVEPLANT = "Remove plant";
    public static final String STRPETAL_REMOVEALLPLANTS = "Remove all plants";
    public static final String STRPETAL_REMOVETREEANDSTUMP = "Remove tree & stump";
    public static final String STRPETAL_BARRELTAKEALL = "Take all";
    public static final String STRPETAL_CURSORHARVEST = "Harvest area";
    public static final String STRPETAL_SEEDFARMER = "Start Seed Farmer";
    public static final String STRPETAL_KILLALL = "Kill all";
    public static final String STRPETAL_EATALL = "Eat all";
    public static final String STRPETAL_LIFTUPGOB = "Lift up";
    public static final String STRPETAL_DESTROYTREELOG2 = "Destroy treelog x2";
    public static final String STRPETAL_DESTROYTREELOG3 = "Destroy treelog x3";
    public static final String STRPETAL_DESTROYTREELOG4 = "Destroy treelog x4";
    public static final String STRPETAL_SHOWWINDOWMINING = "Show window";
    public static final String STRPETAL_TRANSFER_ASC = "Transfer ASC";
    public static final String STRPETAL_TRANSFER_DESC = "Transfer DESC";
    public static final String STRPETAL_AUTO_BUTCH = "Auto-butch";
    public static final String STRPETAL_AUTO_BUTCH_ALL = "Auto-butch all";
    public static final String STRPETAL_AUTO_BUTCH_BIG = "Auto-butch";

    private final Gob gob;
    private final WItem wItem;

    /*
    haven.FlowerMenu@367eed1e ; cl ; [0, 0]
     */

    public ZeeFlowerMenu(Gob gob, String ... opts) {
        super(opts);
        this.gob = gob;
        this.wItem = null;
    }

    public ZeeFlowerMenu(WItem wItem, String ... opts) {
        super(opts);
        this.gob = null;
        this.wItem = wItem;
    }

    @Override
    public void choose(Petal opt) {
        if (opt!=null) {
            String petalName = opt.name;
            uimsg("cancel");
            new ZeeThread(){
                public void run() {
                    try{
                        if (gob != null) {
                            ZeeClickGobManager.gobZeeMenuClicked(gob, petalName);
                        } else if (wItem != null) {
                            ZeeClickItemManager.itemZeeMenuClicked(wItem, petalName);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        else
            uimsg("cancel");
    }
}

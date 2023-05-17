package haven;

public class ZeeFlowerMenu extends FlowerMenu{

    static final String STRPETAL_ADD12COAL = "Add 12 coal";
    static final String STRPETAL_ADD9COAL = "Add 9 coal";
    static final String STRPETAL_ADD4BRANCH = "Add 4 branch";
    static final String STRPETAL_REMOVEPLANT = "Remove plant";
    static final String STRPETAL_REMOVEALLPLANTS = "Remove all plants";
    static final String STRPETAL_REMOVETREEANDSTUMP = "Remove tree & stump";
    static final String STRPETAL_REMOVEALLTREES = "Remove all trees";
    static final String STRPETAL_BARRELTAKEALL = "Take all";
    static final String STRPETAL_CURSORHARVEST = "Harvest area";
    static final String STRPETAL_SEEDFARMER = "Start Seed Farmer";
    static final String STRPETAL_KILLALL = "Kill all";
    static final String STRPETAL_EATALL = "Eat all";
    static final String STRPETAL_LIFTUPGOB = "Lift up";
    static final String STRPETAL_DESTROYTREELOG3 = "Destroy treelog x3";
    static final String STRPETAL_DESTROYTREELOG5 = "Destroy treelog x5";
    static final String STRPETAL_DESTROYALL = "Destroy all";
    static final String STRPETAL_TRANSFER_ASC = "Transfer ASC";
    static final String STRPETAL_TRANSFER_DESC = "Transfer DESC";
    static final String STRPETAL_AUTO_BUTCH = "Auto-butch";
    static final String STRPETAL_AUTO_BUTCH_ALL = "Auto-butch all";
    static final String STRPETAL_AUTOBUTCH_BIGDEADANIMAL = "Auto-butch";
    static final String STRPETAL_INSPECT = "Inspect";
    static final String STRPETAL_SWITCHCHAR = "Switch char";
    static final String STRPETAL_TESTCOORDS = "Test Coords";
    static final String STRPETAL_TILEMONITOR = "Tile monitor";
    static final String STRPETAL_OPENCATTLEROSTER = "Open Roster";
    static final String STRPETAL_MEMORIZEAREANIMALS = "Memorize Area";

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
                            ZeeManagerGobClick.gobZeeMenuClicked(gob, petalName);
                        } else if (wItem != null) {
                            ZeeManagerItemClick.itemZeeMenuClicked(wItem, petalName);
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

    @Override
    public boolean mouseup(Coord c, int button) {
        if (button==2)
            return super.mousedown(c, button);
        return false;
    }
}

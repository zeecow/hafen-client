package haven;

public class ZeeFlowerMenu extends FlowerMenu{

    static final String STRPETAL_ADD12COAL = "Add 12 coal";
    static final String STRPETAL_ADD9COAL = "Add 9 coal";
    static final String STRPETAL_ADD4BRANCH = "Add 4 branch";
    static final String STRPETAL_REMOVEPLANT = "Remove plant";
    static final String STRPETAL_REMOVEALLPLANTS = "Remove all plants";
    static final String STRPETAL_REMOVETREEANDSTUMP = "Remove tree & stump";
    static final String STRPETAL_TOGGLEGROWTHTEXTS = "Toggle growth texts";
    static final String STRPETAL_BARRELTAKEALL = "Take all";
    static final String STRPETAL_CURSORHARVEST = "Harvest area";
    static final String STRPETAL_SEEDFARMER = "Seed Farmer";
    static final String STRPETAL_FARMAWAY = "Farm Away";
    static final String STRPETAL_KILLALL = "Kill all";
    static final String STRPETAL_EATALL = "Eat all";
    static final String STRPETAL_LIFTUPGOB = "Lift up";
    static final String STRPETAL_DESTROYTREELOGS = "Destroy treelogs";
    static final String STRPETAL_TRANSFER_ASC = "Transfer ASC";
    static final String STRPETAL_TRANSFER_DESC = "Transfer DESC";
    static final String STRPETAL_AUTO_BUTCH = "Auto-butch";
    static final String STRPETAL_AUTO_BUTCH_ALL = "Auto-butch all";
    static final String STRPETAL_AUTOBUTCH_BIGDEADANIMAL = "Auto-butch";
    static final String STRPETAL_INSPECT = "Inspect";
    static final String STRPETAL_CLEARGOBTEXTSPOINTERS = "Clear gobs texts/pointers";
    static final String STRPETAL_SWITCHCHAR = "Switch char";
    static final String STRPETAL_TESTCOORDS = "Test coords";
    static final String STRPETAL_TILEMONITOR = "Tile monitor";
    static final String STRPETAL_TIMERS = "Timers";
    static final String STRPETAL_AUTODISCOVERY = "Auto discovery";
    static final String STRPETAL_TOGGLE_CATTLEROSTER = "Toggle roster";
    static final String STRPETAL_MEMORIZEAREANIMALS = "Memorize area";

    private final Gob gob;
    private final WItem wItem;
    private final Coord2d coordMc;

    public ZeeFlowerMenu(Gob gob, String ... opts) {
        super(opts);
        this.gob = gob;
        this.wItem = null;
        this.coordMc = null;
    }

    public ZeeFlowerMenu(WItem wItem, String ... opts) {
        super(opts);
        this.gob = null;
        this.wItem = wItem;
        this.coordMc = null;
    }

    public ZeeFlowerMenu(Coord2d coordMc, String ... opts) {
        super(opts);
        this.gob = null;
        this.wItem = null;
        this.coordMc = coordMc;
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
                        }
                        else if (wItem != null) {
                            ZeeManagerItemClick.itemZeeMenuClicked(wItem, petalName);
                        }
                        else {
                            ZeeManagerGobClick.groundZeeMenuClicked(coordMc, petalName);
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

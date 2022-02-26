package haven;

public class ZeeFlowerMenu extends FlowerMenu{

    public static final String STRPETAL_ADD12COAL = "Add 12 coal";
    public static final String STRPETAL_ADD9COAL = "Add 9 coal";
    public static final String STRPETAL_ADD4BRANCH = "Add 4 branch";
    public static final String STRPETAL_REMOVETRELLIS = "Remove plant";
    public static final String STRPETAL_REMOVETREEANDSTUMP = "Remove tree & stump";
    public static final String STRPETAL_BARRELTAKEALL = "Take all";
    public static final String STRPETAL_CURSORHARVEST = "Harvest area";
    public static final String STRPETAL_SEEDFARMER = "Start Seed Farmer";

    private final Gob gob;

    /*
    haven.FlowerMenu@367eed1e ; cl ; [0, 0]
     */

    public ZeeFlowerMenu(Gob gob, String ... opts) {
        super(opts);
        this.gob = gob;
    }

    @Override
    public void choose(Petal opt) {
        if (opt!=null) {
            //ZeeConfig.println(">choose " + opt);
            ZeeClickGobManager.chooseGobFlowerMenu(gob,opt);
        }
        uimsg("cancel");
    }
}

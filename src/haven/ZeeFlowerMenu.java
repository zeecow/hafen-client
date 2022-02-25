package haven;

public class ZeeFlowerMenu extends FlowerMenu{
    public static final String STR_PETAL_ADD12COAL = "Add 12 coal";
    public static final String STR_PETAL_ADD9COAL = "Add 9 coal";
    public static final String STR_PETAL_ADD4BRANCH = "Add 4 branch";
    public static final String STR_PETAL_REMOVE_TRELLIS = "Remove plant";
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

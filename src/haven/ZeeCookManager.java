package haven;

import java.util.HashSet;
import java.util.Set;

public class ZeeCookManager extends ZeeThread{

    static boolean busy;
    static boolean pepperRecipeOpen;
    static CheckBox cbAutoCook;
    static Label labelStatus;
    static Gob gobCauldron, gobBarrel;
    static Set<Gob> gobsContainers = new HashSet<>();


    public ZeeCookManager(){
        busy = true;
    }


    public void run() {
        Coord barrelCoord;
        println("> start cook manager ");
        try {
            while (busy) {
                ZeeConfig.addPlayerText("cooking");
                waitStaminaIdleMs(2000);
                ZeeConfig.removePlayerText();
                if (gobBarrel != null) {
                    if (ZeeConfig.getBarrelOverlayBasename(gobBarrel).equals("water")) {

                        ZeeConfig.addPlayerText("fetch barrel");
                        barrelCoord = ZeeConfig.getTileCoord(gobBarrel);
                        ZeeClickGobManager.liftGob(gobBarrel);

                        ZeeConfig.addPlayerText("fill up caldron");
                        ZeeClickGobManager.gobClick(gobCauldron,3);
                        waitPlayerIdleFor(1);

                        ZeeConfig.addPlayerText("place barrel");
                        ZeeConfig.clickTile(barrelCoord,3);
                        waitPlayerIdleFor(1);

                        ZeeConfig.addPlayerText("open cauldron");
                        ZeeClickGobManager.clickGobPetal(gobCauldron,"Open");
                        //waitNoFlowerMenu();
                        waitPlayerIdleFor(1);

                        if (gobsContainers.size() > 0) {
                            ZeeConfig.addPlayerText("open containers");
                            gobsContainers.forEach(gob -> {
                                ZeeClickGobManager.gobClick(gob, 3);
                                try {Thread.sleep(PING_MS);} catch(InterruptedException e){e.printStackTrace();}
                            });
                        }else{
                            exitManager("no containers registered");
                        }

                        // craft all and loop again
                        ZeeConfig.makeWindow.wdgmsg("make",1); // 0==craft, 1==craft all

                    } else {
                        exitManager("barrel is empty");
                    }
                } else {
                    exitManager("barrel gob is null");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        busy = false;
        println("> end cook manager ");
    }


    public static void gobClicked(Gob gob, String gobName, int clickb) {

        if (busy)
            return; // already cooking, ignore gobs

        if (clickb==3 && gobName.contains("cauldron")){
            gobCauldron = gob;
        } else if (clickb==3 && ZeeClickGobManager.isGobCookContainer(gobName)){
            //double distCauldron = ZeeConfig.distanceToPlayer(gobCauldron);
            //println("dist cauldron " + distCauldron);
            //if (distCauldron < 7) // 6.9812401343731
            gobsContainers.add(gob);
        } else if (clickb!=3 && gobName.endsWith("barrel")){
            gobBarrel = gob;
        }

        addGobTexts();
        //labelStatus.settext(getLabelStatusText());
    }


    private static String getLabelStatusText() {
        return "cauldron"+(gobCauldron==null?"?":"")+", "
                +"barrel"+(gobBarrel==null?"?":"")+", "
                +"containers("+(gobsContainers.size())+")";
    }


    public static void pepperRecipeBuildUI() {
        pepperRecipeOpen = true;
        if (cbAutoCook==null){
            cbAutoCook = new CheckBox("try autocook"){
                public void changed(boolean val) {
                    super.changed(val);
                    if (val) {
                        //println("TODO start thread");
                        //new ZeeCookManager().start();
                    }else {
                        //exitManager("checkbox set false");
                    }
                }
            };
            //labelStatus = new Label(getLabelStatusText());
        }
        ZeeConfig.makeWindow.add(cbAutoCook,360,40);
        //ZeeConfig.makeWindow.add(labelStatus,290,60);
    }


    public static void clickedCraftAll() {
        if (!busy)
            new ZeeCookManager().start();
    }


    public static void exitManager(String msg) {
        println("exitManager() > "+msg);
        busy = false;
        pepperRecipeOpen = false;
        removeGobTexts();
    }


    private static void addGobTexts() {
        if (gobCauldron!=null)
            ZeeConfig.addGobText(gobCauldron,"cauldron");
        if (gobBarrel!=null)
            ZeeConfig.addGobText(gobBarrel,"barrel");
        if (gobsContainers.size()>0)
            gobsContainers.forEach(gob -> ZeeConfig.addGobText(gob,"container"));
    }


    private static void removeGobTexts() {
        if (gobCauldron!=null)
            ZeeConfig.removeGobText(gobCauldron);
        if (gobBarrel!=null)
            ZeeConfig.removeGobText(gobBarrel);
        if (gobsContainers.size()>0)
            gobsContainers.forEach(ZeeConfig::removeGobText);
    }

}

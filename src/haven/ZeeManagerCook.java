package haven;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZeeManagerCook extends ZeeThread{

    static boolean busy;
    static boolean pepperRecipeOpen;
    static Gob gobCauldron, gobBarrel;
    static Set<Gob> gobsContainers = new HashSet<>();
    static ZeeWindow.ZeeButton cookButton;


    public ZeeManagerCook(){
        busy = true;
        cookButton.disable(true);
    }

    public static void pepperRecipeOpened(Window window) {
        ZeeManagerCook.pepperRecipeOpen = true;
        cookButton = new ZeeWindow.ZeeButton(UI.scale(85),"auto-cook"){
            public void wdgmsg(String msg, Object... args) {
                super.wdgmsg(msg, args);
                if (msg.equals("activate")){
                    if (!busy)
                        new ZeeManagerCook().start();
                }
            }
        };
        window.add(cookButton,360,45);
        cookButton.disable(!isPepperCookReady());
        addGobTexts();
    }


    public void run() {
        Coord barrelCoord;
        println("> start cook manager ");
        try {
            while (busy) {

                ZeeConfig.addPlayerText("cooking");
                ZeeConfig.makeWindow.wdgmsg("make",1); // 0==craft, 1==craft all
                waitStaminaIdleMs(3000,1,500);
                ZeeConfig.removePlayerText();

                if(!busy)
                    continue;

                if (gobBarrel != null) {
                    if (ZeeConfig.getBarrelOverlayBasename(gobBarrel).equals("water")) {

                        if(!busy)
                            continue;
                        ZeeConfig.addPlayerText("fetch barrel");
                        barrelCoord = ZeeConfig.getCoordGob(gobBarrel);
                        ZeeManagerGobClick.liftGob(gobBarrel);

                        if(!busy)
                            continue;
                        ZeeConfig.addPlayerText("fill up caldron");
                        ZeeManagerGobClick.gobClick(gobCauldron,3);
                        waitPlayerIdleFor(1);

                        if(!busy)
                            continue;
                        ZeeConfig.addPlayerText("place barrel");
                        ZeeConfig.clickCoord(barrelCoord,3);
                        waitPlayerIdleFor(1);

                        if(!busy)
                            continue;
                        ZeeConfig.addPlayerText("open cauldron");
                        ZeeManagerGobClick.clickGobPetal(gobCauldron,"Open");
                        //waitNoFlowerMenu();
                        waitPlayerIdleFor(1);

                        if (gobsContainers.size() > 0) {
                            if(!busy)
                                continue;
                            ZeeConfig.addPlayerText("open containers");
                            gobsContainers.forEach(gob -> {
                                ZeeManagerGobClick.gobClick(gob, 3);
                                try {Thread.sleep(PING_MS);} catch(InterruptedException e){e.printStackTrace();}
                            });
                        }else{
                            exitManager("no containers registered");
                        }

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
        cookButton.disable(!isPepperCookReady());
    }


    public static void gobClicked(Gob gob, String gobName, int clickb) {

        if (busy)
            return; // already cooking, ignore gobs

        if (clickb==3 && gobName.contains("cauldron")){
            gobCauldron = gob;
        } else if (clickb==3 && ZeeManagerGobClick.isGobCookContainer(gobName)){
            //double distCauldron = ZeeConfig.distanceToPlayer(gobCauldron);
            //println("dist cauldron " + distCauldron);
            //if (distCauldron < 7) // 6.9812401343731
            gobsContainers.add(gob);
        } else if (clickb!=3 && gobName.endsWith("barrel")){
            gobBarrel = gob;
        }

        addGobTexts();

        cookButton.disable(!isPepperCookReady());
    }


    public static boolean isPepperCookReady() {
        return (!busy && gobCauldron!=null && gobBarrel!=null && gobsContainers.size()>0);
    }


    public static void exitManager(String msg) {
        println("CookManager exit > "+msg);
        busy = false;
        //pepperRecipeOpen = false;
        removeGobTexts();
        gobCauldron = gobBarrel = null;
        gobsContainers.clear();
        if (cookButton!=null)
            cookButton.disable(true);
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


    public static WItem hsJar;
    public static String hsElixirStr;
    public static long hsElixirStrMs;
    static List<WItem> hsItemsUsed;
    static boolean hsSaveResults = false;

    public static void hsRecipeOpened(Window window) {
        window.add(new CheckBox("save results"){
            public void changed(boolean val) {
                hsSaveResults = val;
            }
        },360,45).set(hsSaveResults);
    }
    public static void hsSaveResults(String rcpnm) {
        if(!hsSaveResults || !rcpnm.contentEquals("Herbal Swill"))
            return;
        hsElixirStr = null;
        hsElixirStrMs = System.currentTimeMillis();
        new ZeeThread(){
            public void run() {
                try{
                    // detected selected items
                    List<WItem> selectedItems = new ArrayList<WItem>();
                    List<Window> windows = ZeeConfig.getContainersWindows();
                    Window invWindow = ZeeConfig.getWindow("Inventory");
                    if (invWindow!=null)
                        windows.add(invWindow);
                    Inventory inv;
                    for (int i = 0; i < windows.size(); i++) {
                        inv = windows.get(i).getchild(Inventory.class);
                        if (inv!=null)
                            selectedItems.addAll(inv.getItemsSelectedForCrafting());
                    }
                    hsItemsUsed = selectedItems;
                    ZeeConfig.println("=======");
                    ZeeConfig.println("selected items: "+hsItemsUsed.size());

                    // collect ingredients names
                    String strIngredients = "ingr";
                    for (WItem wItem : hsItemsUsed) {
                        String name = wItem.item.getres().basename();
                        ZeeConfig.println(name);
                        if (!name.contentEquals("jar") && !name.startsWith("jar-") && !ZeeManagerItemClick.isItemDrinkingVessel(name)) {
                            strIngredients += "," + name;
                        }
                    }

                    // wait crafting finish, then check hsElixirStr
                    waitPlayerIdlePose();
                    long craftedMs = System.currentTimeMillis();
                    sleep(PING_MS); //wait tooltip creation?
                    if (hsElixirStrMs - craftedMs < 1000){
                        if (hsElixirStr==null) {
                            ZeeConfig.println("elixir null, missing ingredients? ");
                        }else {
                            hsElixirStr = strIngredients+hsElixirStr;
                            ZeeConfig.println("save elixir > " + hsElixirStr);
                        }
                    }else{
                        ZeeConfig.println("ignore old tooltips");
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

}

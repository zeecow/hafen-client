package haven;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ZeeManagerCraft extends ZeeThread{



    /*
        FEASTING LOG WINDOW
     */
    static Window windowFeasting;
    public static void feastingMsgStatGained(String msg) {

        if (windowFeasting==null){
            windowFeasting = new ZeeWindow(Coord.of(120,100),"Feasting Log");
            ZeeConfig.gameUI.add(windowFeasting,300,300);
            windowFeasting.add(new Label(" * * * * your noms * * * * "),0,0);
        }

        //stat name
        String newStatName = msg.replaceAll("You gained ","").replaceAll("\\s\\+\\d.*$","").strip();

        //stat gain
        int newStatNum = Integer.parseInt(msg.replaceAll("[^\\d]+","").strip());

        //println("name: "+newStatName+" , num: "+newStatNum);

        //update existing stat label
        int y = 0;
        boolean labelExists = false;
        for (Label label : windowFeasting.children(Label.class)) {
            y += 17;
            if (label.texts.contains("your noms"))
                continue;
            // update label color and text
            if (label.texts.startsWith(newStatName)){
                labelExists = true;
                label.setcolor(Color.green);
                int labelNum = Integer.parseInt(label.texts.replaceAll("[^\\d]+","").strip());
                label.settext(newStatName+" +"+(labelNum+newStatNum));
            }
            // restore label color
            else {
                label.setcolor(Color.white);
            }
        }

        //create new stat label
        if (!labelExists){
            Label l = new Label(newStatName+" +"+newStatNum, CharWnd.attrf);
            l.setcolor(Color.green);
            windowFeasting.add(l,0,y);
            y += 17;
        }

        windowFeasting.pack();
    }






    /*
        BUG COLLECTION
     */
    static boolean bugColRecipeOpen, bugColBusy;
    static Button bugColAutoBtn;
    static void bugColRecipeOpened(Window window) {
        bugColRecipeOpen = true;
        bugColAutoBtn = new ZeeWindow.ZeeButton(UI.scale(85),"another 1"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.equals("activate")){
                    if (bugColBusy)
                        return;
                    if (bugColWoodPile==null){
                        ZeeConfig.msgError("no wood pile found");
                        return;
                    }
                    if (bugColContainers==null || bugColContainers.size()==0){
                        ZeeConfig.msgError("no bug containers found");
                        return;
                    }

                    bugColStart();
                }
            }
        };
        bugColAutoBtn.disable(true);
        bugColAutoBtn.settip("craft using woodpile and bug containers");
        window.add(bugColAutoBtn,360,45);
        //autoBtn.disable(!bugCollectionReady());
    }
    public static void bugColCraftBtnClicked(String recipeName) {
        if (!bugColRecipeOpen || bugColBusy)
            return;

        // save bug names for auto craft
        java.util.List<WItem> bugs = ZeeConfig.getMainInventory().getItemsSelectedForCrafting();
        if (bugs.size() < 6){
            ZeeConfig.msgError("requires 6 inventory bugs");
            return;
        }
        bugColBugNames = bugs.stream().map(wItem1 -> wItem1.item.getres().name).collect(Collectors.toList());

        // enable auto btn after "Craft" was clicked; TODO check if craft was successful?
        bugColAutoBtn.disable(false);
    }
    static java.util.List<String> bugColBugNames;
    private static void bugColStart() {

        new Thread(){
            public void run() {
                try{
                    bugColBusy = true;
                    ZeeConfig.addPlayerText("Collecting");

                    //close any containers to use only inventory bugs
                    java.util.List<Window> openContainerWindows = ZeeConfig.getContainersWindows();
                    println("closing containers = "+openContainerWindows.size());
                    openContainerWindows.forEach(window -> {
                        window.reqdestroy();
                        waitWindowClosed(window.cap);
                    });
                    openContainerWindows.clear();
                    sleep(PING_MS);


                    //TODO TEST THIS
                    //get 6 bugs from containers
                    int bugsAcquired = 0;
                    for (Gob gobContainer : bugColContainers) {

                        //open container
                        ZeeManagerGobClick.gobClick(gobContainer,3);
                        waitWindowOpened(gobContainer.getres().basename());//TODO more containers
                        sleep(PING_MS);

                        //search container bugs
                        openContainerWindows = ZeeConfig.getContainersWindows();
                        if (openContainerWindows==null || openContainerWindows.size()==0){
                            bugColExit("open containers not found");
                            return;
                        }
                        Inventory invCont = openContainerWindows.get(0).getchild(Inventory.class);
                        for (int i=bugsAcquired; i<bugColBugNames.size(); i++) {
                            String bugName = bugColBugNames.get(i);
                            java.util.List<WItem> bugsFound = invCont.getWItemsByNameContains(bugName);
                            boolean waitSprite = false;
                            do {
                                sleep(PING_MS);
                                try {
                                    bugsFound.removeIf(wItem1 -> ZeeManagerItemClick.isStackPagina(wItem1.item));
                                } catch (Loading loading) {
                                    waitSprite = true;
                                    continue;
                                }
                                waitSprite = false;
                            }while(waitSprite);
                            if (bugsFound.size() > 0){
                                //transfer 1 bug to main inv and search next bugName
                                bugsFound.get(0).item.wdgmsg("transfer",Coord.z,UI.MOD_SHIFT);
                                sleep(PING_MS);
                                bugsAcquired++;
                            }
                        }

                        //6 bugs found, stop searching containers
                        if (bugsAcquired == 6){
                            break;
                        }

                        //close container(s)
                        openContainerWindows = ZeeConfig.getContainersWindows();
                        openContainerWindows.forEach(window -> {
                            window.reqdestroy();
                            waitWindowClosed(window.cap);
                        });
                        openContainerWindows.clear();
                        sleep(PING_MS);
                    }

                    // searched all containers && not enough bugs
                    if (bugsAcquired<6){
                        bugColExit("not enough bugs");
                        return;
                    }

                    //open wood pile
                    ZeeManagerGobClick.gobClick(bugColWoodPile,3);
                    waitWindowOpened("Stockpile");

                    // click Craft btn
                    ZeeConfig.getButtonNamed(ZeeConfig.getWindow("Bug Collection"),"Craft").click();

                }catch (Exception e){
                    e.printStackTrace();
                }

                bugColExit("");
            }
        }.start();
    }
    static void bugColExit(String msg){
        if (!msg.isEmpty()) {
            println("bugCol > "+msg);
            ZeeConfig.msgError(msg);
        }
        bugColBusy = false;
        ZeeConfig.removePlayerText();
    }
    static Gob bugColWoodPile;
    static List<Gob> bugColContainers;
    public static void bugColGobClicked(Gob gob) {
        // bug container
        if (ZeeManagerGobClick.isGobCraftingContainer(gob.getres().name)){
            ZeeConfig.addGobText(gob,"bugs");
            if (bugColContainers==null)
                bugColContainers = new ArrayList<>();
            if (!bugColContainers.contains(gob))
                bugColContainers.add(gob);
        }
        // wood pile
        else if (gob.getres().name.contentEquals(ZeeManagerStockpile.STOCKPILE_BLOCK)){
            ZeeConfig.addGobText(gob,"wood");
            bugColWoodPile = gob;
        }
    }
    public static void bugColWindowClosed() {
        bugColRecipeOpen = false;
        if (bugColContainers!=null) {
            bugColContainers.forEach(ZeeConfig::removeGobText);
            bugColContainers.clear();
        }
        ZeeConfig.removeGobText(bugColWoodPile);
        bugColWoodPile = null;
    }



    public static void println(String s) {
        System.out.println(s);
    }
}

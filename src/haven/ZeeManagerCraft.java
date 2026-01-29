package haven;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class ZeeManagerCraft extends ZeeThread{

    /*
        FEASTING LOG WINDOW
     */
    static Window windowFeasting;
    public static void feastingMsgStatGained(String msg) {

        int y = 20;
        if (windowFeasting==null){
            windowFeasting = new ZeeWindow(Coord.of(120,100),"Feasting Log");
            ZeeConfig.gameUI.add(windowFeasting,300,300);
            if (!mapDrinkCount.isEmpty())
                feastingDrinkBuffLabelUpd();
            windowFeasting.add(new Label(" * * * * gains * * * * "),0,y);
            mapDrinkCount.clear();
        }

        //stat name
        String newStatName = msg.replaceAll("You gained ","").replaceAll("\\s\\+\\d.*$","").strip();

        //stat gain
        int newStatNum = Integer.parseInt(msg.replaceAll("[^\\d]+","").strip());

        //println("name: "+newStatName+" , num: "+newStatNum);

        //update existing stat label
        boolean labelExists = false;
        for (Label label : windowFeasting.children(Label.class)) {
            y += 17;
            if (label.texts.contains("gains"))
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
        }

        windowFeasting.pack();
    }

    private static HashMap<String,Integer> mapDrinkCount = new HashMap<>();
    public static void feastingDrinkBuffChanged(String drinkName, int drinkCount){

        if (windowFeasting==null){
            println("feastingDrinkBuffUpdate > windowFeasting null");
            return;
        }

        mapDrinkCount.put(drinkName,drinkCount);

        feastingDrinkBuffLabelUpd();
    }

    private static void feastingDrinkBuffLabelUpd() {
        String str = "";
        for (Map.Entry<String, Integer> entry : mapDrinkCount.entrySet()) {
            str += entry.getKey() + entry.getValue() + " ";
        }
        str = str.strip();

        boolean labelExists  = false;
        for (Label label : windowFeasting.children(Label.class)) {
            if (label.c.y == 0){
                labelExists = true;
                label.settext(str);
            }
        }
        if (!labelExists){
            windowFeasting.add(new Label(str),0,0);
        }
    }


    /*
        CLOTH: linen, hemp
     */
    static boolean clothRecipeOpen=false, clothBusy=false;
    static String clothItemName = null, clothStockPileName = null;
    static int clothFibrePilesDist = 5;
    public static void clothRecipeOpened(Window window) {
        println("clothRecipeOpened");
        clothRecipeOpen = true;
        Button clothAutoBtn = new ZeeWindow.ZeeButton(UI.scale(85),"auto"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.equals("activate")){
                    if (clothBusy)
                        return;
                    if (!ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_LOOM_IDLE)){
                        ZeeConfig.msgError("must sit on a loom");
                        return;
                    }
                    //set cloth names
                    if (window.cap.contains("Hemp Cloth")){
                        clothItemName = "hempfibre";
                        clothStockPileName = "stockpile-hempfibre";
                    } else if(window.cap.contains("Linen Cloth")){
                        clothItemName = "flaxfibre";
                        clothStockPileName = "stockpile-flaxfibre";
                    } else{
                        clothExit("unkown type cloth");
                        return;
                    }
                    // label stockpiles
                    List<Gob> piles = ZeeConfig.findGobsByNameEndsWith(clothStockPileName);
                    piles.removeIf(gob -> ZeeConfig.distanceToPlayer(gob) > clothFibrePilesDist*TILE_SIZE);
                    if(piles.isEmpty()){
                        ZeeConfig.msgError("no fibre piles close");
                        return;
                    }
                    piles.forEach(gob -> ZeeConfig.addGobText(gob,"pile"));

                    clothStart(window);
                }
            }
        };
        clothAutoBtn.settip("use closest fiber piles until inventory full");
        window.add(clothAutoBtn,360,45);
    }
    private static void clothStart(Window window) {
        new ZeeThread(){
            public void run() {
                try {
                    // stockpile-hempfibre, flaxfibre
                    clothBusy = true;
                    ZeeConfig.addPlayerText("weaving");
                    // start craft loop
                    while(clothBusy)
                    {
                        Gob loom = ZeeConfig.getClosestGobByNameContains("gfx/terobjs/loom");
                        Inventory inv = ZeeConfig.getMainInventory();
                        int invFibreNum, invSlotsFree;

                        // click "Craft All" button
                        ZeeConfig.getButtonNamed(window,"Craft All").click();
                        sleep(PING_MS);
                        //wait loom idle
                        if(!waitPlayerPose(ZeeConfig.POSE_PLAYER_LOOM_IDLE)){
                            println("craftmanager > click canceled");
                            clothExit("");
                            return;
                        }
                        //update inv info
                        invFibreNum = inv.countItemsByNameContains(clothItemName);
                        invSlotsFree = inv.getNumberOfFreeSlots();
                        //check player tired
                        if (clothItemName.contains("flax") && invFibreNum >= 5
                            || clothItemName.contains("hemp") && invFibreNum >= 6)
                        {
                            clothExit("not enough energy to craft");
                            return;
                        }
                        //check inv space
                        if ( (clothItemName.contains("flax") && invSlotsFree < 5-invFibreNum)
                            || (clothItemName.contains("hemp") && invSlotsFree < 6-invFibreNum) )
                        {
                            clothExit("inventory full");
                            return;
                        }
                        //get more fibre from closest stockpile
                        if (invFibreNum < 6)
                        {
                            Gob closestPile = ZeeConfig.getClosestGobByNameContains(clothStockPileName);
                            // out of fibres stockpile
                            if (ZeeConfig.distanceToPlayer(closestPile) > clothFibrePilesDist*TILE_SIZE ){
                                clothExit("no stockpile close enough");
                                return;
                            }
                            //pickup fibres from stockpile
                            ZeeManagerGobs.gobClick(closestPile,3,UI.MOD_SHIFT);
                            if(!waitPlayerIdleFor(1)){
                                clothExit("couldn't reach stockpile");
                                return;
                            }
                            //return to loom
                            ZeeManagerGobs.gobClick(loom,3);
                            if (!waitPlayerPose(ZeeConfig.POSE_PLAYER_LOOM_IDLE)){
                                clothExit("couldnt return to loom");
                                return;
                            }
                        }
                        // restart crafting loop
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                clothExit("fin");
            }
        }.start();
    }
    public static void clothWindowClosed() {
        clothRecipeOpen = false;
        if (clothBusy)
            clothExit("window closed");
    }
    static void clothExit(String msg){
        if (!msg.isEmpty()) {
            println("cloth > "+msg);
            ZeeConfig.msgError(msg);
        }
        clothBusy = false;
        ZeeConfig.removePlayerText();
        ZeeConfig.removeGobText((ArrayList<Gob>) ZeeConfig.findGobsByNameEndsWith(clothStockPileName));
    }


    /*
        Rope making
     */
    static boolean ropeRecipeOpen = false;
    static boolean ropeBusy = false;
    static void ropeRecipeOpened(Window window) {
        println("ropeRecipeOpened");
        ropeRecipeOpen = true;
        Button ropeAutoBtn = new ZeeWindow.ZeeButton(UI.scale(85),"auto"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.equals("activate")){
                    if (ropeBusy)
                        return;
                    ropeStart(window);
                }
            }
        };
        ropeAutoBtn.settip("use closest fiber piles until inventory full");
        window.add(ropeAutoBtn,360,45);
    }
    public static void ropeWindowClosed() {
        ropeRecipeOpen = false;
        if (ropeBusy)
            ropeExit("window closed");
    }
    static void ropeStart(Window window){
        new ZeeThread(){
            public void run() {
                try {

                    int tilesMaxDist = 6;

                    // highlight piles in range
                    List<Gob> piles = ZeeConfig.findGobsByNameEndsWith("/stockpile-flaxfibre","/stockpile-hempfibre");
                    piles.removeIf(gob -> {
                        if (ZeeConfig.distanceToPlayer(gob) > tilesMaxDist*TILE_SIZE)
                            return true;
                        return false;
                    });
                    piles.forEach(gob -> {
                        if (ZeeConfig.distanceToPlayer(gob) <= tilesMaxDist*TILE_SIZE){
                            ZeeConfig.addGobColor(gob,Color.green);
                        }
                    });


                    // find closest pile
                    Gob closestPile = ZeeConfig.getClosestGobToPlayer(piles);
                    if (closestPile!=null){
                        ZeeConfig.addGobText(closestPile,"next");
                    }

                    ropeBusy = true;
                    ZeeConfig.lastMapViewClickButton = 2;//prepare cancel click

                    do{
                        ZeeConfig.addPlayerText("craftan");

                        // missing 10 strings
                        if(ZeeConfig.getMainInventory().getItemsSelectedForCrafting().size() < 10){
                            if (closestPile==null){
                                ZeeConfig.msgError("out of strings");
                                ropeExit("out of strings");
                                return;
                            }
                            //fetch strings from closest pile and craft again
                            if(!ropeFetchStringsAndCraft(closestPile)){
                                ZeeConfig.msgError("couldnt fetch more strings");
                                ropeExit("couldnt fetch more strings");
                                return;
                            }
                            //mark next pile
                            closestPile = ZeeConfig.getClosestGobToPlayer(piles);
                            if (closestPile!=null){
                                ZeeConfig.addGobText(closestPile,"next");
                            }
                        }

                        //click craft all
                        ZeeConfig.addPlayerText("craftan");
                        ZeeConfig.getButtonNamed(window,"Craft All").click();
                        ZeeConfig.lastMapViewClickButton = 2;//prepare cancel click
                        sleep(PING_MS);

                        //wait crafting end
                        waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_DRINK,ZeeConfig.POSE_PLAYER_ROPE_WALKING);

                    }while(ropeBusy && !ZeeConfig.isCancelClick());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                ropeExit("fin");
            }
        }.start();
    }
    static void ropeExit(String msg){
        ropeBusy = false;
        if (!msg.isEmpty()) {
            println("rope > "+msg);
            ZeeConfig.msgError(msg);
        }
        ZeeConfig.removePlayerText();
        ZeeConfig.findGobsByNameEndsWith("/stockpile-flaxfibre","/stockpile-hempfibre").forEach(gob -> {
            ZeeConfig.removeGobColor(gob);
            ZeeConfig.removeGobText(gob);
        });
    }
    static boolean ropeFetchStringsAndCraft(Gob stringPile) {
        try {
            ZeeConfig.addPlayerText("get strings");
            Gob ropewalk = ZeeConfig.getClosestGobByNameContains("gfx/terobjs/ropewalk");
            ZeeManagerGobs.gobClick(stringPile,3,UI.MOD_SHIFT);
            waitPlayerIdlePoseOrVehicleIdle();
            ZeeManagerGobs.gobClick(ropewalk,3);
            waitPlayerIdlePoseOrVehicleIdle();
            if (ZeeConfig.getMainInventory().getItemsSelectedForCrafting().size() < 10){
                ZeeConfig.removePlayerText();
                return false;
            }
            Window craftWindow = ZeeConfig.getWindow("Rope");
            ZeeConfig.getButtonNamed(craftWindow,"Craft All").click();
            ZeeConfig.removePlayerText();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        ZeeConfig.removePlayerText();
        return false;
    }


    static void craftWindowClosed() {
        ZeeConfig.makeWindow = null;
        if(clothRecipeOpen)
            clothWindowClosed();
        if(ropeRecipeOpen)
            ropeWindowClosed();
    }




    static boolean isCraftRecording =false, craftRecPlaying=false;
    static Window winCraftRec = null;
    static List<CraftRecStep> craftRecSteps;
    static void windowCraftRecorder() {
        String winTitle = "Craft recorder";
        winCraftRec = ZeeConfig.getWindow(winTitle);
        if (winCraftRec!=null)
            return;
        isCraftRecording = true;
        //window
        winCraftRec = ZeeConfig.gameUI.add(new Window(Coord.of(230, 170), winTitle) {
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("close")) {
                    craftRecExit();
                    this.reqdestroy();
                }
            }
        }, ZeeConfig.gameUI.sz.div(3));
        winCraftRec.add(new Label("select mats from containers, click start "),0,0);
        // btn start
        Widget wdg = winCraftRec.add(new ZeeWindow.ZeeButton("start"){
            @Override
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    craftRecStart();
                }
            }
        },0,15);
        // btn reset
        winCraftRec.add(new ZeeWindow.ZeeButton("clear"){
            @Override
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    if (craftRecPlaying) // let player cancel click instead
                        return;
                    if (craftRecSteps==null)
                        return;
                    for (CraftRecStep step : craftRecSteps) {
                        ZeeConfig.removeGobText(step.gob);
                    }
                    craftRecSteps.clear();
                    craftRecWinUpdate();
                }
            }
        },wdg.c.x+wdg.sz.x+3,15);
        // scrollport steps
        winCraftRec.add(new Scrollport(Coord.of(225, 125)), 0, 40);
    }
    static void craftRecGobClicked(Gob gob) {
        if (craftRecPlaying)
            return;
        String name = gob.getres().name;
        if (ZeeManagerGobs.isGobCraftingContainer(name) || ZeeManagerGobs.isGobStockpile(name)
                || name.contains("/cauldron") || name.endsWith("/crucible")
                || ZeeManagerGobs.isGobFireSource(gob) )
        {
            if (craftRecSteps == null)
                craftRecSteps = new ArrayList<>();
            if (craftRecGet(gob)==null)
                craftRecSteps.add(new CraftRecStep(gob));
        }
        craftRecWinUpdate();
    }
    static void craftRecItemAdd(GItem i) {
        if (craftRecPlaying)
            return;
        if(craftRecSteps.isEmpty())
            return;
        craftRecSteps.get(craftRecSteps.size()-1).itemsMainInv.add(i.getres().name);
        craftRecWinUpdate();
    }
    static void craftRecWinUpdate() {
        Scrollport scrollport = winCraftRec.getchild(Scrollport.class);
        if (scrollport==null){
            println("no scrollport?");
            return;
        }
        // clear scrollport items
        for (Widget w : scrollport.cont.children(Widget.class)) {
            w.remove();
        }
        int x=0, y=20;
        for (int i = 0; i < craftRecSteps.size(); i++) {
            CraftRecStep step = craftRecSteps.get(i);
            String basename = step.gob.getres().basename();
            scrollport.cont.add(new Label(basename + " " + i),x,y);
            ZeeConfig.addGobText(step.gob,basename +" "+ i);
            y += 15;
            for (String item : step.itemsMainInv) {
                scrollport.cont.add(new Label(item),x+15,y);
                y += 15;
            }
        }
    }
    static void craftRecStart() {
        if(craftRecPlaying)
            return;
        if(craftRecSteps==null || craftRecSteps.isEmpty()){
            ZeeConfig.msgError("no crafting steps recorded");
            return;
        }
        List<Button> btns = ZeeConfig.makeWindow.children(Button.class).stream().filter(button -> button.text.text.contentEquals("Craft")).toList();
        if (btns.isEmpty()){
            println("craftRecStart > craft button not found");
            return;
        }
        new ZeeThread(){
            public void run() {
                try {
                    craftRecPlaying = true;
                    ZeeConfig.addPlayerText("craftrec");
                    Button craftButton = btns.get(0);
                    do {
                        // craft item
                        craftButton.click();
                        sleep(PING_MS);
                        if(!waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_DRINK,
                                ZeeConfig.POSE_PLAYER_CRAFT,
                                ZeeConfig.POSE_PLAYER_CAULDRON_STIR,
                                ZeeConfig.POSE_PLAYER_PAN_FRYING))
                        {
                            println("couldnt wait crafting pose");
                            craftRecExit();
                            return;
                        }
                        // craft error
                        if (ZeeConfig.lastUiMsg.contains("You do not have all the ingredients")
                            || ZeeConfig.lastUiMsg.contains("need to be using"))
                        {
                            if ( now() - ZeeConfig.lastUIMsgMs < 1000 ){
                                break;
                            }
                        }
                        //ZeeConfig.lastUiMsg
                        if (isCancelClick())
                            break;
                        // prepare for next craft
                        for (int i = 0; i < craftRecSteps.size(); i++) {
                            CraftRecStep step = craftRecSteps.get(i);
                            String resName = step.gob.getres().name;
                            boolean isLastStep = (i == craftRecSteps.size() - 1);
                            boolean isWorkstation = resName.contains("terobjs/cauldron")
                                    || resName.endsWith("/brazier")
                                    || resName.endsWith("/bonfire");
                            if (isLastStep && isWorkstation){
                                // open cauldron,brazier,bonfire
                                if(!ZeeManagerGobs.clickGobPetal(step.gob,0)){
                                    println("couldnt click petal for gob "+resName);
                                    craftRecExit();
                                    return;
                                }
                                waitWindowOpened();
                            }
                            else {
                                // right click container
                                ZeeManagerGobs.gobClick(step.gob, 3);
                                prepareCancelClick();
                                // wait window open
                                Window winOpen = waitWindowOpened();
                                sleep(PING_MS);//wait sel upd?
                                if (isCancelClick())
                                    break;
                                if (winOpen == null) {
                                    println("couldnt wait window opened?");
                                    craftRecExit();
                                    return;
                                }
                                // select item(s) from window
                                for (String itemName : step.itemsMainInv) {
                                    List<WItem> itemsAvailable = winOpen.getchild(Inventory.class).getWItemsByNameEndsWith(itemName);
                                    if (itemsAvailable == null || itemsAvailable.isEmpty()) {
                                        ZeeConfig.msgLow("window had no item: " + itemName);
                                        craftRecExit();
                                        return;
                                    }
                                    // transfer item to inv, close window
                                    itemsAvailable.get(itemsAvailable.size() - 1).item.wdgmsg("transfer", Coord.z);
                                    sleep(PING_MS);
                                    if (isCancelClick())
                                        break;
                                }
                                if (isCancelClick())
                                    break;
                                // keep last window open...
                                boolean craftWithOpenWindow = step.itemsMainInv.isEmpty() && isLastStep;
                                if (craftWithOpenWindow) {
                                    //println("craft with open window from container or stockpile");
                                    continue;
                                }
                                // ... or close window before crafting
                                lastWindowOpened = null;
                                winOpen.reqdestroy();
                                sleep(PING_MS);
                                ZeeConfig.clickCoord(ZeeConfig.getPlayerCoord(), 1);//really close window
                                prepareCancelClick();
                                sleep(PING_MS);
                                if (isCancelClick())
                                    break;
                            }
                        }
                    }while(!isCancelClick());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                craftRecPlaying = false;
                ZeeConfig.removePlayerText();
            }
        }.start();
    }
    static void craftRecExit() {
        isCraftRecording = false;
        craftRecPlaying = false;
        if (winCraftRec!=null)
            winCraftRec.reqdestroy();
        winCraftRec = null;
        if (craftRecSteps!=null)
            for (CraftRecStep step : craftRecSteps) {
                ZeeConfig.removeGobText(step.gob);
            }
        craftRecSteps = null;
    }
    static CraftRecStep craftRecGet(Gob gob1){
        if (craftRecSteps==null || craftRecSteps.isEmpty())
            return null;
        for (CraftRecStep step : craftRecSteps) {
            if (step.gob.equals(gob1))
                return step;
        }
        return null;
    }
    private static class CraftRecStep {
        Gob gob;
        List<String> itemsMainInv;
        public CraftRecStep(Gob g) {
            this.gob = g;
            itemsMainInv = new ArrayList<>();
        }
    }


    public static void println(String s) {
        System.out.println(s);
    }

}

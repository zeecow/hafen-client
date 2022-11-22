package haven;

import java.awt.*;
import java.util.HashSet;
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
                        barrelCoord = ZeeConfig.getGobCoord(gobBarrel);
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
            y += 13;
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
            windowFeasting.add(new Label(newStatName+" +"+newStatNum),0,y);
            y += 13;
        }

        windowFeasting.pack();
    }

    public static void println(String s) {
        System.out.println(s);
    }
}

package haven;

import haven.res.ui.tt.q.quality.Quality;
import haven.resutil.FoodInfo;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static haven.OCache.posres;

public class ZeeResearch {

    static final String FILE_NAME_HERBALSWILL = "haven_research_herbalswill.txt";
    static final String FILE_NAME_FOOD = "haven_research_food.txt";
    public static String hsElixirStr;
    public static long hsElixirStrMs;
    static List<WItem> hsItemsUsed;
    private static List<String> listSkipFoodIds;

    public static void checkResearch(String recipe) {
        if(recipe.contentEquals("Herbal Swill")) {
            herbalSwillBuildEntry();
        }
    }

    public static void herbalSwillBuildEntry() {
        hsElixirStr = null;
        hsElixirStrMs = System.currentTimeMillis();
        new ZeeThread(){
            public void run() {
                try{
                    // detected selected items
                    List<WItem> selectedItems = new ArrayList<WItem>();
                    List<Window> windows = ZeeConfig.getContainersWindows(false);
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
                    //ZeeConfig.println("=======");
                    //ZeeConfig.println("selected items: "+hsItemsUsed.size());

                    // collect ingredients names
                    String strIngredients = "ingr";
                    for (WItem wItem : hsItemsUsed) {
                        String name = wItem.item.getres().basename();
                        //ZeeConfig.println(name);
                        if (!name.contentEquals("jar") && !name.startsWith("jar-") && !ZeeManagerItems.isItemDrinkingVessel(name)) {
                            strIngredients += "," + name;
                        }
                    }

                    // wait crafting finish, then check hsElixirStr
                    waitPlayerIdlePose();
                    long craftedMs = System.currentTimeMillis();
                    sleep(1000); //wait tooltip creation?
                    if (hsElixirStrMs - craftedMs < 1500){
                        if (hsElixirStr==null) {
                            ZeeConfig.println("elixir null (missing ingredients or jar is full)");
                        }else {
                            hsElixirStr = strIngredients+hsElixirStr;
                            //TODO test new attrmod format before removing comment
                            //herbalSwillSaveEntry(hsElixirStr);
                            println(" elixir not saved > "+hsElixirStr);
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

    private static void herbalSwillSaveEntry(String entry) {
        // entry format "ingr,i1,i2;attr,name,val;heal,name,val;wound,name,val;time,val"
        if (herbalSwillEntryExists(entry)){
            println("entry already exist > "+entry);
            return;
        }
        writeLineToFile(entry,FILE_NAME_HERBALSWILL);
        println("saved "+FILE_NAME_HERBALSWILL+" > "+entry);
    }

    private static boolean herbalSwillEntryExists(String entry) {
        // entry format "ingr,i1,i2;attr,name,val;heal,name,val;wound,name,val;time,val"
        List<String> lines = readAllLinesFromFile(FILE_NAME_HERBALSWILL);
        if (lines==null)
            return false;
        String[] entryArr = entry.split("&")[0].split(";")[0].split(",");
        String ing1 = entryArr[1];
        String ing2 = entryArr[2];
        String[] arrLine;
        boolean entryExists = false;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).isBlank())
                continue;
            arrLine = lines.get(i).split(";")[0].split(",");
            if (ing1.contentEquals(arrLine[1]) || ing1.contentEquals(arrLine[2])) {
                if (ing2.contentEquals(arrLine[1]) || ing2.contentEquals(arrLine[2])) {
                    entryExists = true;
                    break;
                }
            }
        }
        return entryExists;
    }


    public static void checkFoodTip(List<ItemInfo> info) {

        String line = "";
        String name="", ql="", ingreds="", events="";
        int ingredsFound = 0;

        for(ItemInfo ii : info) {
            // food name
            if(ii instanceof ItemInfo.Name) {
                name = ((ItemInfo.Name)ii).str.text + ";";
            }
            // food quality
            else if(ii instanceof Quality){
                ql = String.valueOf(ZeeConfig.doubleRound2(((Quality)ii).q)) + ";";
            }
            // food ingredient and smoke
            else if (ii.getClass().getSimpleName().contentEquals("Ingredient") || ii.getClass().getSimpleName().contentEquals("Smoke")){
                ingredsFound++;
                try {
                    String ingrName = (String) ii.getClass().getDeclaredField("name").get(ii);
                    double ingrVal = ZeeConfig.doubleRound2((Double) ii.getClass().getDeclaredField("val").get(ii));
                    ingreds += "igr," + ingrName + "," + ingrVal + ";";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // food events
            else if (ii instanceof FoodInfo){
                FoodInfo fi = (FoodInfo) ii;
                for (int i = 0; i < fi.evs.length; i++) {
                    events += "evt,"+getShortEventName(fi.evs[i].ev.nm) + "," + ZeeConfig.doubleRound2(fi.evs[i].a) + ";";
                }
            }
        }

        //save line
        if (!name.isBlank() && !ql.isBlank()) {
            line = name + ql + ingreds + events;
            foodSaveEntry(line);
        }
        //else println("research > checkFoodTip > name or ql is blank");

    }

    private static String getShortEventName(String nm) {
        String plus = nm.replaceAll("^[^\\+]+","");
        return nm.toUpperCase().substring(0,3) + plus;
    }

    private static void foodSaveEntry(String entry) {
        // entry format "name;ql;ingreds;events;"
        if (foodEntryExists(entry)){
            return;
        }
        writeLineToFile(entry,FILE_NAME_FOOD);
        println("saved "+FILE_NAME_FOOD+" > "+entry);
    }


    // full entry format "name;ql;ingreds;events;"
    // key forming fields: "name;ingreds;" (defines unique value)
    // ingreds format: "igr,[name],[perc];"
    private static boolean foodEntryExists(String entry) {

        String newFoodId = getFoodIdFromLine(entry);

        // skip reading disk if possible
        if (listSkipFoodIds == null) {
            listSkipFoodIds = new ArrayList<>();
        } else if (listSkipFoodIds.contains(newFoodId)) {
            return true;
        }

        // read lines from file
        List<String> lines = readAllLinesFromFile(FILE_NAME_FOOD);
        if (lines==null)
            return false;
        for (int i = 0; i < lines.size(); i++) {
            //skip empty lines
            if (lines.get(i).isBlank())
                continue;
            // found same foodId
            if (newFoodId.contentEquals(getFoodIdFromLine(lines.get(i)))){
                listSkipFoodIds.add(newFoodId);
                return true;
            }
        }

        // foodId not found
        return false;
    }

    private static String getFoodIdFromLine(String line) {

        // foodId format: "foodName[,igrName]"
        String foodId = "";

        // entry format "name;ql;ingreds;events;"
        String[] arr0 = line.split(";");

        for (int i = 0; i < arr0.length; i++) {
            // food name
            if (i==0){
                foodId += arr0[0];
            }
            // extract igrName from : "igr,[name],[perc];"
            else if (arr0[i].startsWith("igr,")){
                String igrName = arr0[i].split(",")[1];
                foodId += "," + igrName;
            }
        }

        // sort foodId alphabetically
        String[] arr = foodId.split(",");
        Arrays.sort(arr);

        return String.join(",",arr);
    }


    private static String newLine;
    private static synchronized void writeLineToFile(String line, String fileName)  {
        PrintWriter printWriter = null;
        String path = System.getProperty("user.home").concat(System.getProperty("file.separator")).concat(fileName);
        File file = new File(path);
        try {
            if (!file.exists()) {
                newLine = "";
                if (!file.createNewFile()) {
                    println("couldn't create file "+fileName);
                    return;
                }
            }else{
                newLine = System.getProperty("line.separator");
            }
            printWriter = new PrintWriter(new FileOutputStream(path, true));
            printWriter.write(newLine + line);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.flush();
                printWriter.close();
            }
        }
    }
    static synchronized List<String> readAllLinesFromFile(String fileName){
        List<String> allLines = null;
        try {
            Path path = Paths.get(System.getProperty("user.home"),fileName);
            //byte[] bytes = Files.readAllBytes(path);
            allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        }catch (NoSuchFileException e){
            println("research > readAllLinesFromFile > new file "+fileName+" ?");
        }catch (IOException e){
            e.printStackTrace();
        }
        return allLines;
    }



    private static Coord2d inspectWaterCoordBefore;
    static List<Integer> inspectWaterQls;
    static Window inspectWaterWin;
    static boolean inspectWaterSpeakQl = Utils.getprefb("inspectWaterSpeakQl",true);
    static boolean inspectWaterAuto = false;
    static boolean inspectWaterSkip = false;
    static Coord2d inspectWaterNextCoord;
    static boolean inspectWaterActive = false;
    static int inspectWaterHighestQl = -1;
    static void inspectWaterWindow(Coord2d coordMc) {

        // require wooden cup
        Inventory inv = ZeeConfig.getMainInventory();
        List<WItem> cups = inv.getWItemsByNameContains("/woodencup");
        if (cups == null || cups.size() == 0) {
            ZeeConfig.msgError("need woodencup to inspect water");
            return;
        }

        // build window
        String winTitle = "Inspect water";
        Widget wdg;
        inspectWaterWin = ZeeConfig.getWindow(winTitle);
        if (inspectWaterWin != null) {
            inspectWaterWin.reqdestroy();
        }
        inspectWaterWin = ZeeConfig.gameUI.add(new Window(Coord.of(100, 140), winTitle) {
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("close")) {
                    inspectWaterActive = false;
                    ZeeConfig.simulateCancelClick();
                    this.reqdestroy();
                }
            }
        }, ZeeConfig.gameUI.sz.div(3));
        wdg = inspectWaterWin.add(new CheckBox("speak"){
            {a= inspectWaterSpeakQl;}
            public void changed(boolean val) {
                super.changed(val);
                ZeeResearch.inspectWaterSpeakQl = val;
                Utils.setprefb("inspectWaterSpeakQl",val);
            }
        },0,0);
        wdg = inspectWaterWin.add(new Button(40, "next") {
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")) {
                    // next inspect coord in line
                    Gob player = ZeeConfig.getPlayerGob();
                    Coord2d coordNow = Coord2d.of(player.rc.x, player.rc.y);
                    inspectWaterNextCoord = coordNow.add(coordNow.sub(inspectWaterCoordBefore));
                    inspectWaterAt();
                }
            }
        },0,20);
        wdg = inspectWaterWin.add(new CheckBox("auto") {
            {a= inspectWaterAuto;}
            public void changed(boolean val) {
                inspectWaterAuto = val;
            }
        },wdg.c.x+wdg.sz.x+2,wdg.c.y);
        wdg.settip("auto inspect forward");
        wdg = inspectWaterWin.add(new CheckBox("skip") {
            {a= inspectWaterSkip;}
            public void changed(boolean val) {
                inspectWaterSkip = val;
            }
        },wdg.c.x,wdg.c.y+wdg.sz.y);
        wdg.settip("skip every other inspect");

        // start inspecting
        if (inspectWaterQls == null)
            inspectWaterQls = new ArrayList<>();
        else
            inspectWaterUpdLabels();
        inspectWaterNextCoord = coordMc;
        inspectWaterAt();
    }
    private static void inspectWaterUpdLabels() {
        for (Label label : inspectWaterWin.children(Label.class)) {
            label.remove();
        }
        Button btn = ZeeConfig.getButtonNamed(inspectWaterWin, "next");
        int y = btn.c.y + btn.sz.y + 10;
        Label lbl = inspectWaterWin.add(new Label("highest "+inspectWaterHighestQl),0,y);
        lbl.setcolor(Color.green);
        y += 17;
        int x=0, row=3;
        for (int i = 0; i < inspectWaterQls.size(); i++) {
            inspectWaterWin.add(new Label(""+inspectWaterQls.get(i)),x,y);
            if (row==0) {
                row = 3;
                x = 0;
                y += 13;
            }else{
                row--;
                x += 25;
            }
        }
    }
    static void inspectWaterAt() {

        if (ZeeConfig.getMainInventory().countItemsByNameEndsWith("/woodencup")==0){
            ZeeConfig.msgError("need woodencup to inspect water");
            return;
        }

        if (inspectWaterNextCoord==null){
            println("next coord null");
            return;
        }

        if (inspectWaterActive){
            println("inspect water already active");
            return;
        }
        inspectWaterActive = true;

        if (inspectWaterHighestQl == -1)
            inspectWaterHighestQl = 0;

        new ZeeThread(){
            public void run() {
                try {
                    ZeeConfig.addPlayerText("inspecting");
                    boolean skippedPrev = false;
                    do {
                        // approach target tile
                        prepareCancelClick();
                        Gob player = ZeeConfig.getPlayerGob();
                        inspectWaterCoordBefore = Coord2d.of(player.rc.x, player.rc.y);
                        ZeeConfig.clickCoord(inspectWaterNextCoord.floor(posres),1);
                        ZeeThread.waitPlayerIdleFor(1);
                        if (isCancelClick()) {
                            println("inspect water cancel click");
                            break;
                        }

                        // skip every other inspect
                        if(inspectWaterSkip && inspectWaterAuto){
                            if(!skippedPrev) {
                                Coord2d coordNow = Coord2d.of(player.rc.x, player.rc.y);
                                inspectWaterNextCoord = coordNow.add(coordNow.sub(inspectWaterCoordBefore));
                                skippedPrev = true;
                                continue;
                            }else{
                                skippedPrev = false;
                            }
                        }

                        // pickup inv cup
                        Inventory inv = ZeeConfig.getMainInventory();
                        List<WItem> cups = inv.getWItemsByNameContains("/woodencup");
                        if (cups==null || cups.size()==0){
                            ZeeConfig.msgError("need woodencup to inspect water");
                            inspectWaterActive = false;
                            return;
                        }
                        WItem cup = cups.get(0);
                        ZeeManagerItems.pickUpItem(cup);

                        // collect tile water, calc next coord
                        ZeeConfig.itemActTile(inspectWaterNextCoord.floor(posres));
                        prepareCancelClick();
                        ZeeThread.waitPlayerIdleFor(1);
                        if (isCancelClick()) {
                            println("inspect water cancel click");
                            break;
                        }
                        Coord2d coordNow = Coord2d.of(player.rc.x, player.rc.y);
                        inspectWaterNextCoord = coordNow.add(coordNow.sub(inspectWaterCoordBefore));

                        // read cup contents, register water quality
                        String msg = ZeeManagerItems.getHoldingItemContentsNameQl();
                        ZeeConfig.msgLow(msg);
                        Integer ql = Integer.valueOf(msg.replaceAll("\\D", ""));
                        inspectWaterAddQl(ql);

                        //return cup, empty cup
                        Coord cupSlot = ZeeManagerItems.dropHoldingItemToInvAndRetCoord(inv);
                        if (cupSlot != null) {
                            cup = inv.getItemBySlotCoord(cupSlot);
                            boolean confirmPetalBackup = ZeeConfig.confirmPetal;
                            ZeeConfig.confirmPetal = false;//temp disable confirm petal
                            boolean emptied = ZeeManagerItems.clickItemPetal(cup, "Empty");
                            ZeeConfig.confirmPetal = confirmPetalBackup;
                            if (!emptied){
                                println("couldnt empty cup?");
                                break;
                            }
                        }
                        else{
                            println("couldnt detect cup slot?");
                            break;
                        }

                    }while(inspectWaterAuto && inspectWaterActive && !isCancelClick());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                inspectWaterActive = false;
                ZeeConfig.removePlayerText();
            }
        }.start();
    }
    private static void inspectWaterAddQl(Integer ql) {
        try {
            if (inspectWaterSpeakQl && !inspectWaterAuto)
                ZeeAudio.textToSpeakLinuxFestival(""+ql);
            if (inspectWaterQls.contains(ql))
                return;
            if (ql <= inspectWaterHighestQl)
                return;
            inspectWaterHighestQl = ql;
            inspectWaterQls.add(ql);
            if (inspectWaterSpeakQl && inspectWaterAuto)
                ZeeAudio.textToSpeakLinuxFestival(""+ql);
            inspectWaterUpdLabels();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void inspectWaterClear() {
        if (inspectWaterQls != null)
            inspectWaterQls.clear();
        inspectWaterWin = null;
        inspectWaterActive = false;
        inspectWaterNextCoord = null;
        inspectWaterHighestQl = -1;
        inspectWaterAuto = false;
        inspectWaterSkip = false;
    }


    private static void println(String s) {
        System.out.println(s);
    }
}

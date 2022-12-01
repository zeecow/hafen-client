package haven;

import haven.res.ui.tt.q.quality.Quality;
import haven.resutil.FoodInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ZeeResearch {

    static final String FILE_NAME_HERBALSWILL = "haven_research_herbalswill.txt";
    static final String FILE_NAME_FOOD = "haven_research_food.txt";
    public static String hsElixirStr;
    public static long hsElixirStrMs;
    static List<WItem> hsItemsUsed;

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
                    //ZeeConfig.println("=======");
                    //ZeeConfig.println("selected items: "+hsItemsUsed.size());

                    // collect ingredients names
                    String strIngredients = "ingr";
                    for (WItem wItem : hsItemsUsed) {
                        String name = wItem.item.getres().basename();
                        //ZeeConfig.println(name);
                        if (!name.contentEquals("jar") && !name.startsWith("jar-") && !ZeeManagerItemClick.isItemDrinkingVessel(name)) {
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
                            herbalSwillSaveEntry(hsElixirStr);
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

        for(ItemInfo ii : info) {
            // food name
            if(ii instanceof ItemInfo.Name) {
                name = ((ItemInfo.Name)ii).str.text + ";";
            }
            // food quality
            else if(ii instanceof Quality){
                ql = String.valueOf(ZeeConfig.doubleRound2(((Quality)ii).q)) + ";";
            }
            // food ingredient
            else if (ii.getClass().getName().contentEquals("Ingredient")){
                try {
                    String ingrName = (String) ii.getClass().getDeclaredField("name").get(ii);
                    double ingrVal = (double) ii.getClass().getDeclaredField("val").get(ii);
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

        line = name + ql + ingreds + events;

        foodSaveEntry(line);
    }

    private static String getShortEventName(String nm) {
        String plus = nm.replaceAll("^[^\\+]+","");
        return nm.toUpperCase().substring(0,3) + plus;
    }

    private static void foodSaveEntry(String entry) {
        // entry format "name;ql;ingreds;events;"
        if (foodEntryExists(entry)){
            println("food entry already exist > "+entry);
            return;
        }
        writeLineToFile(entry,FILE_NAME_FOOD);
        println("saved "+FILE_NAME_FOOD+" > "+entry);
    }


    // full entry format "name;ql;ingreds;events;"
    // key forming fields: "name;ingreds;" (defines unique value)
    // ingreds format: "igr,[name],[perc];"
    private static boolean foodEntryExists(String entry) {

        List<String> lines = readAllLinesFromFile(FILE_NAME_FOOD);
        if (lines==null)
            return false;
        String[] entryArr = entry.split(";");
        String entryName = entryArr[0];
        String[] arrLine;
        String line;
        boolean entryExists = false;

        //check all lines from file
        for (int i = 0; i < lines.size(); i++) {

            //skip empty lines
            if (lines.get(i).isBlank())
                continue;

            //line to array
            line = lines.get(i);
            arrLine = line.split(";");

            //check same name
            if (!arrLine[0].contentEquals(entryName)){
                continue;
            }

            //check same ingredients (or no ingredients)
            boolean ingredsMatch = true;
            for (int j = 0; j < entryArr.length; j++) {
                //skip non ingredient entries
                if (!entryArr[j].contains("igr,"))
                    continue;
                //ingredient not present, match is false
                if (!line.contains(entryArr[j])){
                    ingredsMatch = false;
                    break;
                }
            }
            if (ingredsMatch){
                entryExists = true;
                break;
            }
        }
        return entryExists;
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

    private static void println(String s) {
        System.out.println(s);
    }
}

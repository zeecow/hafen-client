package haven;

import java.util.ArrayList;
import java.util.List;

public class ZeeResearch {

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
                    sleep(PING_MS); //wait tooltip creation?
                    if (hsElixirStrMs - craftedMs < 1000){
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
        // entries separated by "&"
        //println("list before > "+ZeeConfig.listAlchHerbalSwill);
        if (ZeeConfig.listAlchHerbalSwill.isBlank()){
            //println("list blank, saving > "+entry);
            ZeeConfig.listAlchHerbalSwill = entry;
            Utils.setpref("listAlchHerbalSwill", ZeeConfig.listAlchHerbalSwill);
            return;
        }
        // avoid duplicate entry
        if (herbalSwillEntryExists(entry)){
            //println("entry already exist > "+entry);
            return;
        }
        // save new entry
        ZeeConfig.listAlchHerbalSwill += "&" + entry;
        Utils.setpref("listAlchHerbalSwill", ZeeConfig.listAlchHerbalSwill);
        println("saved list > "+ZeeConfig.listAlchHerbalSwill);
    }

    private static boolean herbalSwillEntryExists(String entry) {
        // entry format "ingr,i1,i2;attr,name,val;heal,name,val;wound,name,val;time,val"
        // entries separated by "&"
        String[] ingrArr = entry.split("&")[0].split(";")[0].split(",");
        String ing1 = ingrArr[1];
        String ing2 = ingrArr[2];
        String[] storeArr = ZeeConfig.listAlchHerbalSwill.split("&");
        String[] storeIngrArr;
        boolean hasEntry = false;
        for (int i = 0; i < storeArr.length; i++) {
            storeIngrArr = storeArr[i].split(";")[0].split(",");
            // check for ingredients pair, order independent
            if (ing1.contentEquals(storeIngrArr[1]) || ing1.contentEquals(storeIngrArr[2])){
                if (ing2.contentEquals(storeIngrArr[1]) || ing2.contentEquals(storeIngrArr[2])){
                    hasEntry = true;
                    break;
                }
            }
        }
        return hasEntry;
    }

    private static void println(String s) {
        System.out.println(s);
    }
}

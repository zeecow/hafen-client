package haven;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class ZeeConsole {

    static Object lastCmdResults;
    static ArrayList<String> helpLines;
    static boolean isGobFindActive;
    static List<String> gobFindRegex;

    public static void runCmdZeecow(String[] args) {
        if (ZeeConfig.gameUI==null)
            return;
        initHelp();
        try{
            String cmd = "";

            for (int i = 1; i < args.length; i++) {
                cmd += " " + args[i].toLowerCase();
            }
            cmd.trim();
            println("zeecow > ("+cmd+" )");
            if (cmd.isBlank() || cmd.endsWith("-h") || cmd.endsWith("--help")){
                //show window help
                showWindow();
                return;
            }

            String[] arrPipes = cmd.replaceAll("^zeecow\\s+","").split(",");
            println("arrPipes = "+arrPipes.length);
            String finalCmd = cmd;
            new ZeeThread(){
                public void run() {
                    try {
                        lastCmdResults = null;
                        for (int i = 0; i < arrPipes.length; i++) {
                            long ms = ZeeThread.now();
                            lastCmdResults = runSmallestCmd(arrPipes[i].trim().split("\\s+"));
                            println(
                                "lastCmdResults = "
                                + (lastCmdResults==null?"null":lastCmdResults.getClass().getSimpleName())
                                + (lastCmdResults instanceof List && !((List<?>) lastCmdResults).isEmpty()? " ("+((List<?>) lastCmdResults).get(0)+") " : "")
                                + " , ms = "
                                +(ZeeThread.now() - ms)
                            );
                        }
                        if (lastCmdResults instanceof List){
                            List<?> list = (List<?>) lastCmdResults;
                            String msg = "list size "+list.size();
                            if (!list.isEmpty()){
                                msg += " ("+list.get(0).getClass().getSimpleName()+")";
                            }
                            ZeeConfig.msgLow(msg);
                        }

                        if (lastCmdResults != null) {
                            // save cmd hist
                            updateCmdHist(finalCmd);
                            // udpate hist btns
                            showWindow();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static List<String> listCmdHist = new ArrayList<>(Utils.getprefsl("listCmdHist",new String[]{}));
    private static void updateCmdHist(String cmd) {
        cmd = cmd.strip();
        println("saveCmdHist > \"" + cmd + "\"");
        final int MAX_SIZE = 10;
        try {
            int i = listCmdHist.indexOf(cmd);
            // new cmd
            if (i < 0)
                // list full, replace least used cmd
                if(listCmdHist.size() >= MAX_SIZE)
                    listCmdHist.set(MAX_SIZE-1,cmd);
                // append cmd
                else
                    listCmdHist.add(cmd);
            // bump existing cmd position
            else if (i > 0)
                Collections.swap(listCmdHist, i, i-1);

            // save pref
            Utils.setprefsl("listCmdHist",listCmdHist);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void initHelp() {
        if (helpLines==null){
            helpLines = new ArrayList<>();
            helpLines.add("======== examples ========");
            helpLines.add(":zeecow gobfind borka");
            helpLines.add("      highlight players");
            helpLines.add(":zeecow win cupboard , stack");
            helpLines.add("      select cupboard windows , stack similar items ");
            helpLines.add(":zeecow itres cheesetray , clickmenu slice up");
            helpLines.add("      select cheesetrays , clickmenu \"Slice up\"");
            helpLines.add("======== gob cmds ========");
            helpLines.add("   goball      select all gobs");
            helpLines.add("   gobne []    select gobs which name ends with []");
            helpLines.add("   gobns []    select gobs which name starts with []");
            helpLines.add("   gobnc []    select gobs which name contains []");
            helpLines.add("   gobfind []  select/highlight gobs matching [], regex capable");
            helpLines.add("   gobfind     clear gobs found");
            helpLines.add("   goblbl      label selected gobs with basenames");
            helpLines.add("   addt []     add text[] to all/sel gobs");
            helpLines.add("   addc []     add hexcolor[] to all/sel gobs");
            helpLines.add("   clg         clear gobs text/color/pointer");
            helpLines.add("   clg [tpc]   clear gobs [t]ext, [c]olor, [p]ointer");
            helpLines.add("======== items cmds ========");
            helpLines.add("   win []        select windows named []");
            helpLines.add("   itname []     select items which name contains []");
            helpLines.add("   itres []      select items with resname contains []");
            helpLines.add("   stack         stack selected windows/items ");
            helpLines.add("   clickmenu []  click menu named []");
            helpLines.add("======== misc cmds ========");
            helpLines.add("   count       msg counting last cmd results");
            helpLines.add("   disc        toggle discovery helper on/off");
            helpLines.add("   say []      text2speak parameter or last cmd results");
            helpLines.add("                 (requires LinuxFestival)");
            helpLines.add("   reslocal    print local res names reslocal.txt");
            helpLines.add("   resmote     print remote res names to resmote.txt");
        }
    }

    static void showWindow() {
        if (ZeeConfig.gameUI==null)
            return;
        initHelp();
        String winName = ":zeecow cmds";
        Window win = ZeeConfig.getWindow(winName);
        if (win!=null){
            win.reqdestroy();
        }
        Coord winsize = Coord.of(390,220);
        win = ZeeConfig.gameUI.add(
            new Window(winsize,winName){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("close")){
                        this.reqdestroy();
                    }
                }
            },
            ZeeConfig.gameUI.sz.div(3)
        );

        Widget wdg = null;

        // help text
        wdg = win.add(new Label(":zeecow cmd1 , cmd2 , ..."),0,0);
        int y=0;
        Scrollport scrollhelp = win.add(new Scrollport(new Coord(winsize.x-25, 120)), 15, y+15);
        for (String lines : helpLines) {
            scrollhelp.cont.add(new Label(lines),0,15*y++);
        }

        // cmd history
        wdg = scrollhelp.cont;
        wdg = win.add(new Label("CMD HIST ("+listCmdHist.size()+")  (leftclick to run, midclick to delete) "),0,wdg.c.y + wdg.sz.y+35);
        Scrollport scrollhist = win.add(new Scrollport(new Coord(winsize.x-25, 120)), 15, wdg.c.y + wdg.sz.y+10);
        y = 0;
        for (String cmd : listCmdHist) {
            wdg = scrollhist.cont.add(new ZeeWindow.ZeeButton(cmd){
                public boolean mousedown(MouseDownEvent ev) {
                    return super.mousedown(ev);
                }
                public boolean mouseup(MouseUpEvent ev) {
                    String runcmd = this.buttonText.strip();
                    if ( ev.b == 1 ) {
                        ZeeConsole.runCmdZeecow(new String[]{":zeecow",runcmd});
                    }else if( ev.b == 2 ) {
                        listCmdHist.remove(runcmd);
                        showWindow();
                    }
                    return super.mouseup(ev);
                }
            }, 15, y);
            y += wdg.sz.y;
        }
        win.pack();
    }

    @SuppressWarnings("unchecked")
    private static Object runSmallestCmd(String[] arr) {

        println("   smallestCmd "+Arrays.toString(arr));
        String cmd = arr[0];
        Object ret = null;

        /*
            gobs cmds
         */
        if (cmd.contentEquals("goball")){
            ret = ZeeConfig.getAllGobs();
        }
        else if (cmd.contentEquals("gobne")){
            if (arr.length < 2){
                ZeeConfig.msgError("gobne missing parameter");
                showWindow();
                return null;
            }
            ret = ZeeConfig.findGobsByNameEndsWith(arr[1]);
        }
        else if (cmd.contentEquals("gobns")){
            if (arr.length < 2){
                ZeeConfig.msgError("gobns missing parameter");
                showWindow();
                return null;
            }
            ret = ZeeConfig.findGobsByNameStartsWith(arr[1]);
        }
        else if (cmd.contentEquals("gobnc")){
            if (arr.length < 2){
                ZeeConfig.msgError("gobnc missing parameter");
                showWindow();
                return null;
            }
            ret = ZeeConfig.findGobsByNameContains(arr[1]);
        }
        else if (cmd.contentEquals("gobfind")){
            ret = gobFind(arr);
        }
        else if (cmd.contentEquals("goblbl")){
            ret = labelGobsBasename();
        }
        else if (cmd.contentEquals("clg")){
            boolean rt = false, rp = false, rc = false;
            boolean clearAll = false;
            if (arr.length < 2){
                clearAll = true;
            }
            if (clearAll || arr[1].contains("t"))
                rt = clearGobsTexts();
            if (clearAll || arr[1].contains("p"))
                rp = clearGobsPointers();
            if (clearAll || arr[1].contains("c"))
                rc = clearGobsColors();
            ret = rt && rp && rc;
        }
        else if (cmd.contentEquals("addt")){
            ret = addTextToGobs(arr[1]);
        }
        else if (cmd.contentEquals("addc")){
            ret = addColorToGobs(arr[1]);
        }

        /*
            windows items cmds
         */
        else if (cmd.contentEquals("win")){
            if (arr.length < 2){
                ZeeConfig.msgError("win parameter missing");
                showWindow();
                return null;
            }
            String windowName = arr[1];
            for (int i = 2; i < arr.length; i++) {
                windowName += " " + arr[i];
            }
            ret = ZeeConfig.getWindows(windowName);
        }
        else if (cmd.contentEquals("itname")){
            ret = selectWindowsItemsInfoName(arr);
        }
        else if (cmd.contentEquals("itres")){
            ret = selectWindowsItemsResName(arr);
        }
        else if (cmd.contentEquals("stack")){
            ZeeConfig.addPlayerText("stackin");
            ret = stack();
            ZeeConfig.removePlayerText();
        }
        else if (cmd.contentEquals("clickmenu")){
            if (arr.length < 2){
                ZeeConfig.msgError("clickmenu parameter missing");
                showWindow();
                return null;
            }
            ZeeConfig.addPlayerText("clickin");
            ret = clickItemPetal(arr);
            ZeeConfig.removePlayerText();
        }
        else if (cmd.contentEquals("reslocal")){
            PrintWriter printWriter = null;
            String fileName = "reslocal.txt";
            String path = System.getProperty("user.home").concat(System.getProperty("file.separator")).concat(fileName);
            File file = new File(path);
            try {
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        println("couldn't create file " + fileName);
                        ret = false;
                        throw new IOException("Couldn't create file " + fileName);
                    }
                }
                printWriter = new PrintWriter(new FileOutputStream(path, false));
                int lines = 0;
                for (Resource resource : Resource.local().cached()) {
                    printWriter.write(System.getProperty("line.separator") + resource.name);
                    lines++;
                }
                println("wrote " + lines + " lines to " + fileName);
                ret = true;
            } catch (IOException ioex) {
                ioex.printStackTrace();
            } finally {
                if (printWriter != null) {
                    printWriter.flush();
                    printWriter.close();
                }
            }

        }
        else if (cmd.contentEquals("resmote")){
            PrintWriter printWriter = null;
            String fileName = "resmote.txt";
            String path = System.getProperty("user.home").concat(System.getProperty("file.separator")).concat(fileName);
            File file = new File(path);
            try {
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        println("couldn't create file " + fileName);
                        ret = false;
                        throw new IOException("Couldn't create file " + fileName);
                    }
                }
                printWriter = new PrintWriter(new FileOutputStream(path, false));
                int lines = 0;
                for (Resource resource : Resource.remote().cached()) {
                    printWriter.write(System.getProperty("line.separator") + resource.name);
                    lines++;
                }
                println("wrote " + lines + " lines to " + fileName);
                ret = true;
            } catch (IOException ioex) {
                ioex.printStackTrace();
            } finally {
                if (printWriter != null) {
                    printWriter.flush();
                    printWriter.close();
                }
            }
        }

        /*
            misc cmds
         */
        else if (cmd.contentEquals("count")){
            if (lastCmdResults==null){
                ZeeConfig.msgError("nothing to print");
                return null;
            }
            String text = "";
            if (lastCmdResults instanceof List){
                text = "list size "+((List<?>) lastCmdResults).size();
                ZeeConfig.msgLow(text);
            }else{
                text = "no list to count";
                ZeeConfig.msgError(text);
            }
            ret = text;
        }
        else if (cmd.contentEquals("disc")){
            // toggle discovery help  on-off
            ZeeManagerGobs.discoveryHelperToggle();
        }
        else if (cmd.contentEquals("say")){
            // say without parameter
            if (arr.length < 2){
                // requires lastCmdResults, show help
                if (lastCmdResults==null){
                    ZeeConfig.msgError("\"say\" is missing parameter");
                    showWindow();
                }
                // say lastCmdResults
                else {
                    String text;
                    if (lastCmdResults instanceof List)
                        text = "list size "+((List<?>) lastCmdResults).size();
                    else
                        text = String.valueOf(lastCmdResults);
                    ZeeAudio.textToSpeakLinuxFestival(text);
                }
            }
            // say parameter(arr[1]) has priority over lastCmdResults
            else{
                StringBuilder text = new StringBuilder(arr[1]);
                for (int i = 2; i < arr.length; i++) {
                    text.append(" ").append(arr[i]);
                }
                ZeeAudio.textToSpeakLinuxFestival(text.toString());
            }
        }

        // unknown cmd
        else {
            ZeeConfig.msgError("unknown \""+cmd+"\"");
            println("cmd unknown \""+cmd+"\"");
            showWindow();
            return null;
        }

        return ret;
    }

    private static Object gobFind(String[] arr) {
        if (arr.length < 2){
            // missing regex param?
            if (!isGobFindActive) {
                ZeeConfig.msgError("gobfind missing parameter");
                showWindow();
                return null;
            }
            //clear gobs found
            gobFindClear();
            return null;
        }
        if (gobFindRegex==null)
            gobFindRegex = new ArrayList<>();
        // remove gob
        if (gobFindRegex.contains(arr[1])) {
            gobFindClear(arr[1]);
            return null;
        }
        //add gob
        else {
            isGobFindActive = true;
            if (!gobFindRegex.contains(arr[1]))
                gobFindRegex.add(arr[1]);
            List<Gob> gobs = ZeeConfig.findGobsByNameRegexMatch(arr[1]);
            for (Gob gob : gobs) {
                gobFindApply(gob);
            }
            return gobs;
        }
    }

    static void gobFindApply(Gob gob) {
        if (gob.id == ZeeConfig.getPlayerGob().id){
            println("gobFindApply > ignoring player gob");
            return;
        }
        gob.setattr(new ZeeGobFind(gob));
    }
    static void gobFindClear(){
        List<Gob> gobsfound = ZeeConfig.getAllGobs();
        gobsfound.removeIf(g1 -> ZeeManagerGobs.getGAttrByClassSimpleName(g1, "ZeeGobFind") == null);
        for (Gob g2 : gobsfound) {
            g2.delattr(ZeeGobFind.class);
        }
        isGobFindActive = false;
        gobFindRegex = null;
        ZeeConfig.msgLow("gobfind reset");
        println("gobfind reset");
    }
    static void gobFindClear(String basename){
        List<Gob> gobsfound = ZeeConfig.findGobsByNameEndsWith("/"+basename);
        for (Gob g2 : gobsfound) {
            g2.delattr(ZeeGobFind.class);
        }
        gobFindRegex.remove(basename);
        if (gobFindRegex.isEmpty()) {
            isGobFindActive = false;
        }
        ZeeConfig.msgLow("gobfind list "+gobFindRegex.size());
        println("gobfind list "+gobFindRegex.size());
    }

    @SuppressWarnings("unchecked")
    private static Boolean clickItemPetal(String[] args){
        if (!(lastCmdResults instanceof List) || (((List) lastCmdResults).isEmpty()) || !(((List<?>) lastCmdResults).get(0) instanceof WItem)){
            ZeeConfig.msgError("no items selected");
            println("no items selected");
            showWindow();
            return false;
        }
        StringBuilder petalName = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            petalName.append(args[i]).append(" ");
        }
        //ignore petal confirmation, otherwise player gets stuck in non confirmed petal
        boolean backupConfirm = ZeeConfig.confirmPetal;
        boolean backupConfirmEat = ZeeConfig.confirmPetalEat;
        ZeeConfig.confirmPetal = ZeeConfig.confirmPetalEat = false;
        try {
            int clicked = ZeeManagerItems.clickAllItemsPetal((List<WItem>) lastCmdResults, petalName.toString().strip());
            if (clicked==0){
                ZeeConfig.msgError("no petal named \""+petalName.toString().strip()+"\" ?");
                ZeeFlowerMenu.cancelFlowerMenu();
            }else{
                println("clicked "+clicked+" petals");
                ZeeConfig.msgLow("clicked "+clicked+" petals");
            }
            ZeeConfig.confirmPetal = backupConfirm;
            ZeeConfig.confirmPetalEat = backupConfirmEat;
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        ZeeConfig.confirmPetal = backupConfirm;
        ZeeConfig.confirmPetalEat = backupConfirmEat;
        return false;
    }


    private static Boolean stack() {

        if (!(lastCmdResults instanceof List) || (((List) lastCmdResults).isEmpty())){
            ZeeConfig.msgError("no items selected");
            println("no items selected");
            showWindow();
            return false;
        }

        Object first  = ((List<?>) lastCmdResults).get(0);
        if (first instanceof Window) {
            return stackWindowsContents();
        }
        else if (first instanceof WItem) {
            return stackItemsSelected();
        }
        else {
            println("stack > unknown selection");
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static Boolean stackWindowsContents() {
        try {
            List<Window> windows = (List<Window>) lastCmdResults;
            for (Window window : windows) {
                Inventory inv = ZeeConfig.getWindowsInventory(window);
                WItem[] invItems = inv.children(WItem.class).toArray(new WItem[0]);
                List<String> names = new ArrayList<>();
                List<String> meatNames = new ArrayList<>();

                /*
                 collect items names
                 */
                for (WItem item : invItems) {
                    String itemName = item.item.getres().name;
                    // special case for meat items
                    if (itemName.endsWith("/meat")){
                        String meatName = ZeeManagerItems.getItemInfoName(item.item.info());
                        if (!meatNames.contains(meatName))
                            meatNames.add(meatName);
                        continue;
                    }
                    // regular item names
                    if (names.contains(itemName))
                        continue;
                    names.add(itemName);
                }

                /*
                 stack regular items by res name
                 */
                for (String name : names) {
                    List<WItem> namedItems = inv.getWItemsByNameEndsWith(name);
                    for (int i = 0; i < namedItems.size()-1; i++) {
                        // skip stacks
                        if (namedItems.get(i).item.isStackByContent())
                            continue;
                        for (int j = i+1; j < namedItems.size(); j++) {
                            // skip stacks
                            if (namedItems.get(j).item.isStackByContent())
                                continue;
                            // pickup item i
                            if (!ZeeManagerItems.pickUpItem(namedItems.get(i))) {
                                println("couldnt pickup namedItem");
                                return false;
                            }
                            // stack all on item j
                            ZeeManagerItems.itemAct(namedItems.get(j), UI.MOD_CTRL_SHIFT);
                            Thread.sleep(250);
                            // item not stackable? return to container
                            if (ZeeConfig.isPlayerHoldingItem()){
                                if(!ZeeManagerItems.dropHoldingItemToInv(inv)) {
                                    println("couldnt return non-stackable to inv?");
                                    return false;
                                }
                            }
                            // next item name
                            i = j = namedItems.size() + 1;
                        }
                    }
                }

                /*
                 stack meat items by ItemInfo.Name
                 */
                for (String meatName : meatNames) {
                    List<WItem> meatItems = inv.getWItemsByInfoNameContains(meatName);
                    for (int i = 0; i < meatItems.size(); i++) {
                        // skip stacks
                        if (meatItems.get(i).item.isStackByContent())
                            continue;
                        for (int j = i+1; j < meatItems.size(); j++) {
                            // skip stacks
                            if (meatItems.get(j).item.isStackByContent())
                                continue;
                            // pickup item i
                            if (!ZeeManagerItems.pickUpItem(meatItems.get(i))) {
                                println("couldnt pickup meatItem");
                                return false;
                            }
                            // stack all on item j
                            ZeeManagerItems.itemAct(meatItems.get(j), UI.MOD_CTRL_SHIFT);
                            Thread.sleep(400);
                            // next item name
                            i = j = meatItems.size() + 1;
                        }
                    }
                }

                // optimize stacks
                if (!names.isEmpty())
                    if(!optimizeStacks(inv, names))
                        return false;
                if (!meatNames.isEmpty())
                    if(!optimizeStacks(inv, meatNames))
                        return false;

            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean optimizeStacks(Inventory inv, List<String> names){
        println("optimizeStacks() > "+names.size()+" names");
        try{
            boolean isResName = names.get(0).startsWith("gfx/invobjs/");
            /*
             join stacks when no remainder
             */
            for (String name : names) {
                int maxStack = 2;
                List<WItem> wItems;
                if(isResName)
                    wItems = inv.getWItemsByNameEndsWith(name);
                else
                    wItems = inv.getWItemsByInfoNameContains(name);

                // guess max stack size  //TODO: if equals 2, try joining once and recalc
                for (WItem w1 : wItems) {
                    if (!w1.item.isStackByContent())
                        continue;
                    int amount = ZeeManagerItems.getItemInfoAmount(w1.item.info());
                    if (maxStack < amount)
                        maxStack = amount;
                }
                println("    "+name+" , maxStack "+maxStack);
                println("        "+wItems.size()+" items from "+(isResName?" res":" ItemInfo"));

                // add single items to stacks with 1 space
                List<WItem> singleItems = wItems.stream().filter(wItem -> !wItem.item.isStackByContent()).toList();
                int finalMaxStack = maxStack;
                List<WItem> oneSpaceStacks = wItems.stream().filter(wItem -> wItem.item.isStackByContent() && (ZeeManagerItems.getItemInfoAmount(wItem.item.info())==finalMaxStack-1)).toList();
                for (int i = 0; i < singleItems.size(); i++) {
                    if (oneSpaceStacks.isEmpty() || oneSpaceStacks.get(i)==null) {
                        println("        not enough oneSpaceStacks , i = "+i);
                        break;
                    }
                    // pickup single item
                    if (!ZeeManagerItems.pickUpItem(singleItems.get(i))) {
                        println("        couldnt pickup single item");
                        break;
                    }
                    // click one space stack
                    ZeeManagerItems.itemAct(oneSpaceStacks.get(i));
                    Thread.sleep(400);//wait item upd
                    if (ZeeConfig.isPlayerHoldingItem()) {
                        println("        shouldnt be holding item?");
                        return false;
                    }
                    println("        joined singleItem "+i+" to stack "+i);
                }

                // join incomplete stacks, smaller than maxSize
                List<WItem> incompleteStacks = wItems.stream().filter(wItem ->
                        wItem.item.isStackByContent() && finalMaxStack > 3
                                && ZeeManagerItems.getItemInfoAmount(wItem.item.info()) <= (finalMaxStack-2) ).toList();
                if (incompleteStacks.size() < 2){
                    println("        not enough incompleteStacks "+incompleteStacks.size());
                }
                else {
                    println("        incompleteStacks = " + incompleteStacks.size());
                    for (int i = 0; i < incompleteStacks.size() - 1; i += 2) {
                        // pickup stack
                        if (!ZeeManagerItems.pickUpItem(incompleteStacks.get(i))) {
                            println("        couldnt pickup incompleteStack");
                            break;
                        }
                        // click other stack
                        ZeeManagerItems.itemAct(incompleteStacks.get(i + 1));
                        Thread.sleep(400);//wait item upd
                        if (ZeeConfig.isPlayerHoldingItem()) {
                            println("        shouldnt be holding incompleteStack?");
                            return false;
                        }
                        println("        joined incompleteStack " + i + " to " + (i + 1));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static Boolean stackItemsSelected() {
        try {
            List<WItem> selectedItems = (List<WItem>) lastCmdResults;
            List<String> names = new ArrayList<>();
            List<String> meatNames = new ArrayList<>();
            List<Window> windows = new ArrayList<>();

            // collect items names, windows
            for (WItem item : selectedItems) {
                // save items windows
                Window win = item.getparent(Window.class);
                if (!windows.contains(win))
                    windows.add(win);
                String itemName = item.item.getres().name;
                // save meat names
                if (itemName.endsWith("/meat")){
                    String meatName = ZeeManagerItems.getItemInfoName(item.item.info());
                    if (!meatNames.contains(meatName))
                        meatNames.add(meatName);
                    continue;
                }
                // save regular item names
                if (names.contains(itemName))
                    continue;
                names.add(itemName);
            }

            for (Window window : windows) {
                Inventory inv = ZeeConfig.getWindowsInventory(window);
                // stack regular items by name
                for (String name : names) {
                    List<WItem> namedItems = inv.getWItemsByNameEndsWith(name);
                    for (int i = 0; i < namedItems.size()-1; i++) {
                        // skip stacks
                        if (namedItems.get(i).item.isStackByContent())
                            continue;
                        for (int j = i+1; j < namedItems.size(); j++) {
                            // skip stacks
                            if (namedItems.get(j).item.isStackByContent())
                                continue;
                            // pickup item i
                            if (!ZeeManagerItems.pickUpItem(namedItems.get(i))) {
                                println("couldnt pickup namedItem");
                                return false;
                            }
                            // stack all on item j
                            ZeeManagerItems.itemAct(namedItems.get(j), UI.MOD_CTRL_SHIFT);
                            Thread.sleep(250);
                            // next item name
                            i = j = namedItems.size() + 1;
                        }
                    }
                }
                //stack meat items
                for (String meatName : meatNames) {
                    List<WItem> meatItems = inv.getWItemsByInfoNameEquals(meatName);
                    for (int i = 0; i < meatItems.size(); i++) {
                        // skip stacks
                        if (meatItems.get(i).item.isStackByContent())
                            continue;
                        for (int j = i+1; j < meatItems.size(); j++) {
                            // skip stacks
                            if (meatItems.get(j).item.isStackByContent())
                                continue;
                            // pickup item i
                            if (!ZeeManagerItems.pickUpItem(meatItems.get(i))) {
                                println("couldnt pickup meatItem");
                                return false;
                            }
                            // stack all on item j
                            ZeeManagerItems.itemAct(meatItems.get(j), UI.MOD_CTRL_SHIFT);
                            Thread.sleep(250);
                            // next item name
                            i = j = meatItems.size() + 1;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static List<Window> getWinsSelected() {
        List<Window> wins;
        // default to maininv and containers
        if (!(lastCmdResults instanceof List)){
            wins = new ArrayList<>();
            for (Window w : ZeeConfig.getWindowsOpened()) {
                if (ZeeConfig.isWindowContainer(w) || w.cap.equalsIgnoreCase("inventory"))
                    wins.add(w);
            }
        }
        // previous selected windows
        else {
            wins = (List<Window>) lastCmdResults;
        }
        return wins;
    }

    @SuppressWarnings("unchecked")
    private static List<WItem> selectWindowsItemsResName(String[] arr) {
        try {
            if (arr.length < 2){
                ZeeConfig.msgError("item parameter missing");
                showWindow();
                return null;
            }
            List<Window> wins = getWinsSelected();
            List<WItem> ret = new ArrayList<>();
            for (Window win : wins) {
                ret.addAll(ZeeConfig.getWindowsInventory(win).getWItemsByNameContains(arr[1]));
            }
            return ret;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<WItem> selectWindowsItemsInfoName(String[] arr) {
        try {
            if (arr.length < 2){
                ZeeConfig.msgError("item parameter missing");
                showWindow();
                return null;
            }
            List<Window> wins = getWinsSelected();
            List<WItem> ret = new ArrayList<>();
            for (Window win : wins) {
                ret.addAll(ZeeConfig.getWindowsInventory(win).getWItemsByInfoNameContains(arr[1]));
            }
            return ret;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private static Boolean addColorToGobs(String hexColor) {
        try {
            Iterable<Gob> gobs = getTargetGobs();
            if (gobs == null)
                return false;
            synchronized (gobs) {
                gobs.forEach(gob -> {
                    ZeeConfig.addGobColor(gob, Color.decode(hexColor));
                });
                return true;
            }
        }
        catch (NumberFormatException nfe){
            println("hexColor invalid = "+hexColor);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean addTextToGobs(String text) {
        try {
            Iterable<Gob> gobs = getTargetGobs();
            if (gobs == null)
                return false;
            synchronized (gobs) {
                gobs.forEach(gob -> {
                    ZeeConfig.addGobText(gob, text);
                });
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private static Boolean labelGobsBasename() {
        try {
            Iterable<Gob> gobs = getTargetGobs();
            if (gobs == null)
                return false;
            synchronized (gobs) {
                gobs.forEach(gob -> {
                    ZeeConfig.addGobText(gob, gob.getres().basename());
                });
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    static Boolean clearGobsTexts() {
        try {
            Iterable<Gob> gobs = getTargetGobs();
            if (gobs==null)
                gobs = ZeeConfig.getAllGobs();
            synchronized (gobs) {
                gobs.forEach(gob -> {
                    synchronized (gob) {
                        Gob.Overlay ol = gob.findol(ZeeGobText.class);
                        if (ol != null) {
                            ol.remove(false);
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static Boolean clearGobsPointers() {
        try {
            Iterable<Gob> gobs = getTargetGobs();
            if (gobs==null)
                gobs = ZeeConfig.getAllGobs();
            synchronized (gobs) {
                gobs.forEach(gob -> {
                    synchronized (gob) {
                        Gob.Overlay ol = gob.findol(ZeeGobPointer.class);
                        if (ol != null) {
                            ol.remove(false);
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static Boolean clearGobsColors() {
        try {
            Iterable<Gob> gobs = getTargetGobs();
            if (gobs==null)
                gobs = ZeeConfig.getAllGobs();
            synchronized (gobs) {
                gobs.forEach(ZeeConfig::removeGobColor);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static Boolean clearGobsTextsAndPointers() {
        try {
            Iterable<Gob> gobs = getTargetGobs();
            if (gobs==null)
                gobs = ZeeConfig.getAllGobs();
            synchronized (gobs) {
                gobs.forEach(gob -> {
                    synchronized (gob) {
                        Gob.Overlay ol = gob.findol(ZeeGobText.class);
                        if (ol != null) {
                            ol.remove(false);
                        }
                        ol = gob.findol(ZeeGobPointer.class);
                        if (ol != null) {
                            ol.remove(false);
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static Iterable<Gob> getTargetGobs() {
        if (lastCmdResults instanceof List){
            return ((List<Gob>) lastCmdResults);
        }
        else if (lastCmdResults!=null){
            return ZeeConfig.gameUI.ui.sess.glob.oc;
        }
        return null;
    }

    public static void println(String s) {
        System.out.println(s);
    }
}

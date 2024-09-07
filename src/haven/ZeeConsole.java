package haven;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZeeConsole {

    static Object lastCmdResults;
    static ArrayList<String> helpLines;

    public static void runCmdZeecow(String[] args) {
        initHelp();
        try{
            String cmd = "";

            for (int i = 1; i < args.length; i++) {
                cmd += " " + args[i].toLowerCase();
            }
            cmd.trim();
            println("zeecow > ("+cmd+")");
            if (cmd.isBlank() || cmd.endsWith("-h") || cmd.endsWith("--help")){
                //print help in terminal
                for (String line : helpLines) {
                    println(line);
                }
                //show window help
                showHelpWindow();
                return;
            }

            cmd = cmd.replaceAll("^zeecow\\s+","");

            String[] arrPipes = cmd.split("\\|");
            println("arrPipes = "+arrPipes.length);
            lastCmdResults = null;
            for (int i = 0; i < arrPipes.length; i++) {
                lastCmdResults = runSmallestCmd(arrPipes[i].trim().split("\\s+"));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void initHelp() {
        if (helpLines==null){
            helpLines = new ArrayList<>();
            helpLines.add(":zeecow  cmd1 | cmd2 | ... ");
            helpLines.add("======== gob ========");
            helpLines.add("   goball      select all gobs");
            helpLines.add("   gobne []    select gobs which name ends with []");
            helpLines.add("   gobns []    select gobs which name starts with []");
            helpLines.add("   gobnc []    select gobs which name contains []");
            helpLines.add("   gobfind []  select/highlight gobs matching [], regex capable");
            helpLines.add("   goblbl      label selected gobs with basenames");
            helpLines.add("   addt []     add text[] to all/sel gobs");
            helpLines.add("   addc []     add hexcolor[] to all/sel gobs");
            helpLines.add("   clg         clear gobs text/color/pointer");
            helpLines.add("   clg [tpc]   clear gobs [t]ext, [c]olor, [p]ointer");
            helpLines.add("======== win items ========");
            helpLines.add("   win []     select windows named []");
            helpLines.add("   item []    select items named []");
            helpLines.add("======== misc ========");
            helpLines.add("   count       msg counting last cmd results");
            helpLines.add("   say []      text2speak parameter or last cmd results (requires LinuxFestival)");
        }
    }

    private static void showHelpWindow() {
        String winName = ":zeecow cmds";
        Window win = ZeeConfig.getWindow(winName);
        if (win!=null){
            win.reqdestroy();
        }
        win = ZeeConfig.gameUI.add(
            new Window(Coord.of(200,200),winName){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("close")){
                        this.reqdestroy();
                    }
                }
            },
            ZeeConfig.gameUI.sz.div(3)
        );
        int y=0;
        for (String lines : helpLines) {
            win.add(new Label(lines),0,15*y++);
        }
        win.pack();
    }

    private static Object runSmallestCmd(String[] arr) {

        println("   smallestCmd "+arr.length);
        println("       "+ Arrays.toString(arr));
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
                showHelpWindow();
                return null;
            }
            ret = ZeeConfig.findGobsByNameEndsWith(arr[1]);
        }
        else if (cmd.contentEquals("gobns")){
            if (arr.length < 2){
                ZeeConfig.msgError("gobns missing parameter");
                showHelpWindow();
                return null;
            }
            ret = ZeeConfig.findGobsByNameStartsWith(arr[1]);
        }
        else if (cmd.contentEquals("gobnc")){
            if (arr.length < 2){
                ZeeConfig.msgError("gobnc missing parameter");
                showHelpWindow();
                return null;
            }
            ret = ZeeConfig.findGobsByNameContains(arr[1]);
        }
        else if (cmd.contentEquals("gobfind")){
            if (arr.length < 2){
                ZeeConfig.msgError("gobfind missing parameter");
                showHelpWindow();
                return null;
            }
            ret = ZeeConfig.findGobsByNameRegexMatch(arr[1]);
            List<Gob> gobsfound = (List<Gob>) ret;
            for (Gob gob : gobsfound) {
                synchronized (gob){
                    ZeeConfig.addGobText(gob,"▼");
                    ZeeConfig.addGobColor(gob,Color.magenta);
                }
            }
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
                showHelpWindow();
                return null;
            }
            ret = ZeeConfig.getWindows(arr[1]);
        }
        else if (cmd.contentEquals("item")){
            if (arr.length < 2){
                ZeeConfig.msgError("item parameter missing");
                showHelpWindow();
                return null;
            }
            ret = selectWindowsItems(arr[1]);
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
        else if (cmd.contentEquals("say")){
            // say without parameter
            if (arr.length < 2){
                // requires lastCmdResults, show help
                if (lastCmdResults==null){
                    ZeeConfig.msgError("\"say\" is missing parameter");
                    showHelpWindow();
                }
                // say lastCmdResults
                else {
                    String text;
                    if (lastCmdResults instanceof List)
                        text = "list size "+((List<?>) lastCmdResults).size();
                    else
                        text = String.valueOf(lastCmdResults);
                    ZeeSynth.textToSpeakLinuxFestival(text);
                }
            }
            // say parameter(arr[1]) has priority over lastCmdResults
            else{
                ZeeSynth.textToSpeakLinuxFestival(arr[1]);
            }
        }

        // unknown cmd
        else {
            ZeeConfig.msgError("unknown \""+cmd+"\"");
            println("cmd unknown \""+cmd+"\"");
            showHelpWindow();
        }

        return ret;
    }

    private static List<WItem> selectWindowsItems(String itemNameContains) {
        try {
            List<Window> wins = (List<Window>) lastCmdResults;
            if (wins==null || wins.isEmpty())
                return null;
            List<WItem> ret = new ArrayList<>();
            for (Window win : wins) {
                ret.addAll(ZeeConfig.getWindowsInventory(win).getWItemsByNameContains(itemNameContains));
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

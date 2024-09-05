package haven;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class ZeeConsole {

    static Object lastCmdResults;

    public static void runCmdZeecow(String[] args) {
        try{
            String cmd = "";

            for (int i = 1; i < args.length; i++) {
                cmd += " " + args[i].toLowerCase();
            }
            cmd.trim();
            println("zeecow > ("+cmd+")");
            if (cmd.isBlank() || cmd.endsWith("-h") || cmd.endsWith("--help")){
                println("zeecow  cmd  [ | cmd] ");
                println("   goball      select all gobs");
                println("   gobne []    select gobs which name ends with []");
                println("   gobns []    select gobs which name starts with []");
                println("   gobnc []    select gobs which name contains []");
                println("   goblbl      label selected gobs");
                println("   addt []     add text[] to all/sel gobs");
                println("   addc []     add hexcolor[] to all/sel gobs");
                println("   clg [tpc]   clear gobs [t]exts, [c]olors, [p]ointers");
                println("example");
                println("   \"gobnc plants | clg c\"");
                println("       select crops gobs (gobnc plants)");
                println("       with selection do (|)");
                println("       clear gob color (clg c)");
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

    private static Object runSmallestCmd(String[] arr) {
        println("   smallestCmd "+arr.length);
        println("       "+ Arrays.toString(arr));
        String cmd = arr[0];
        Object ret = null;
        if (cmd.contentEquals("goball")){
            ret = ZeeConfig.getAllGobs();
        }
        else if (cmd.contentEquals("gobne")){
            if (arr.length < 2){
                println("gobne missing parameter");
                return null;
            }
            ret = ZeeConfig.findGobsByNameEndsWith(arr[1]);
        }
        else if (cmd.contentEquals("gobns")){
            if (arr.length < 2){
                println("gobns missing parameter");
                return null;
            }
            ret = ZeeConfig.findGobsByNameStartsWith(arr[1]);
        }
        else if (cmd.contentEquals("gobnc")){
            if (arr.length < 2){
                println("gobnc missing parameter");
                return null;
            }
            ret = ZeeConfig.findGobsByNameContains(arr[1]);
        }
        else if (cmd.contentEquals("goblbl")){
            ret = labelGobsBasename();
        }
        else if (cmd.contains("clg")){
            boolean rt = false, rp = false, rc = false;
            if (arr.length < 2){
                println("clg missing parameter");
                return null;
            }
            if (arr[1].contains("t"))
                rt = clearGobsTexts();
            if (arr[1].contains("p"))
                rp = clearGobsPointers();
            if (arr[1].contains("c"))
                rc = clearGobsColors();
            ret = rt && rp && rc;
        }
        else if (cmd.contains("addt")){
            ret = addTextToGobs(arr[1]);
        }
        else if (cmd.contains("addc")){
            ret = addColorToGobs(arr[1]);
        }
        //println("      returning "+ret);
        return ret;
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

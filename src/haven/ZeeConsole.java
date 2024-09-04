package haven;

import java.util.Arrays;
import java.util.List;

public class ZeeConsole {
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
                println("   clt     clear gob texts");
                println("   clp     clear gob pointers");
                println("   cltp    clear gob text and pointers ");
                println("   clc     clear gob colors");
                println("   gobne []   select gobs which name ends with []");
                println("   gobns []   select gobs which name starts with []");
                println("   gobnc []   select gobs which name contains []");
                println("   goblbl     label selected gobs");
                return;
            }

            cmd = cmd.replaceAll("^zeecow\\s+","");

            /*
               gobnc terobjs/plants | clc

                    select crops gobs (terobjs/plants)
                    then (|)
                    clear their colors (clc)
             */
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

    static Object lastCmdResults;
    private static Object runSmallestCmd(String[] arr) {
        println("   smallestCmd "+arr.length);
        println("       "+ Arrays.toString(arr));
        String cmd = arr[0];
        Object ret = null;
        if (cmd.contentEquals("gobne")){
            ret = ZeeConfig.findGobsByNameEndsWith(arr[1]);
        }
        else if (cmd.contentEquals("gobns")){
            ret = ZeeConfig.findGobsByNameStartsWith(arr[1]);
        }
        else if (cmd.contentEquals("gobnc")){
            ret = ZeeConfig.findGobsByNameStartsWith(arr[1]);
        }
        else if (cmd.contentEquals("goblbl")){
            ret = labelGobsBasename();
        }
        else if (cmd.contentEquals("cltp")){
            ret = clearGobsTextsAndPointers();
        }
        else if (cmd.contains("clt")){
            ret = clearGobsTexts();
        }
        else if (cmd.contains("clp")){
            ret = clearGobsPointers();
        }
        else if (cmd.contains("clc")){
            ret = clearGobsColors();
        }
        //println("      returning "+ret);
        return ret;
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
                return false;
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
                return false;
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
                return false;

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
                return false;
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

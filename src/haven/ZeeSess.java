package haven;

import java.util.ArrayList;
import java.util.List;

public class ZeeSess {

    static final String WINTITLE = "Switch char";

    static boolean charSwitchKeepWindow = Utils.getprefb("charSwitchKeepWindow",false);

    static List<String> charSwitchNamesList;
    static String charSwitchNextName = "";
    static String charSwitchLastUser = "";
    public static String charSwitchCurPlayingChar;
    static long charSwitchLastMs = 0;

    static void charSwitchAddName(String name, String username) {

        // reset char list if start of session
        if (!username.contentEquals(charSwitchLastUser)){
            charSwitchNamesList = new ArrayList<>();
        }

        // add char name to list
        if(!charSwitchNamesList.contains(name)) {
            //println("charSwitchAddName > "+username+" > "+name);
            charSwitchNamesList.add(name);
        }

        charSwitchLastUser = username;
    }

    static void charSwitchCreateWindow(){

        Window win = ZeeConfig.getWindow(WINTITLE);
        if (win!=null){
            win.reqdestroy();
            win = null;
        }
        win = new ZeeWindow(Coord.of(200,300), WINTITLE);

        int y = 5;

        win.add(new CheckBox("keep window"){
            {a = charSwitchKeepWindow;}
            public void set(boolean val) {
                a = val;
                charSwitchKeepWindow = a;
                Utils.setprefb("charSwitchKeepWindow",charSwitchKeepWindow);
            }
        },0,y);

        y += 17;

        win.add(new Button(120,"Switch Screen"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    ZeeConfig.gameUI.act("lo", "cs");
                    ZeeConfig.getWindow(WINTITLE).reqdestroy();
                }
            }
        },0,y);

        y += 35;

        win.add(new Label("Play char:"),0,y);

        y += 15;

        // chars list
        Scrollport scroll = win.add(new Scrollport(new Coord(140, 155)), 0, y);
        y = 0;// inside scrollport
        for (String charName : charSwitchNamesList) {
            Button btn = scroll.cont.add(new Button(120,charName){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate")){
                        charSwitchNextName = charName;
                        ZeeConfig.gameUI.act("lo", "cs");
                        charSwitchLastMs = ZeeThread.now();
                    }
                }
            },0,y);
            // disable current char btn
            if (charName.contentEquals(charSwitchCurPlayingChar))
                btn.disable(true);
            y += 25;
        }

        win.pack();
        ZeeConfig.gameUI.add(win, ZeeConfig.gameUI.sz.div(2).sub(win.sz.div(2)));
        ZeeConfig.gameUI.opts.wdgmsg("close");
    }

    public static void charSwitchAutoLogin(Charlist charlist) {

        // cancel auto login by timeout (in case player cancel logout animation and exit game)
        long logoutMs = ZeeThread.now() - charSwitchLastMs;
        if (logoutMs > 6000){
            charSwitchCancelAutologin("auto login timed out by "+logoutMs+" ms");
        }

        // auto login
        else if (!charSwitchNextName.isEmpty() && charlist.ui != null){
            charlist.wdgmsg("play", charSwitchNextName);
            charSwitchNextName = "";
        }

    }

    static void charSwitchCancelAutologin(String msg) {
        if(!charSwitchNextName.isEmpty()) {
            println("cancel autologin > " + msg);
            charSwitchNextName = "";
            charSwitchCurPlayingChar = "";
            charSwitchKeepWindow = false;
            Utils.setprefb("charSwitchKeepWindow", false);
        }
    }

    static void println(String s) {
        System.out.println(s);
    }

    public static void charSwitchStartPlaying(String charName) {
        charSwitchCurPlayingChar = charName;
        charSwitchNamesList.remove(charName);
        charSwitchNamesList.add(0,charName);
    }
}

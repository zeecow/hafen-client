package haven;

import java.util.ArrayList;
import java.util.List;

public class ZeeSess {

    static final String WINTITLE = "Switch Char";

    static boolean charSwitchKeepWindow = Utils.getprefb("charSwitchKeepWindow",false);

    static List<String> charSwitchListNameServer;
    static String charSwitchNextNameAndServer = "";
    static String charSwitchLastUser = "";
    public static String charSwitchCurPlayingChar;
    public static String charSwitchCurPlayingServer;
    static long charSwitchLastMs = 0;

    static void charSwitchAddNameServer(String charname, String servername, String username) {

        // reset char list if start of session
        if (!username.contentEquals(charSwitchLastUser)){
            charSwitchListNameServer = new ArrayList<>();
        }

        // add "charname@servername" to list
        String str = charname+"@"+servername;
        if(!charSwitchListNameServer.contains(str)) {
            charSwitchListNameServer.add(str);
        }

        charSwitchLastUser = username;
    }

    static void charSwitchCreateWindow(){

        Window win = ZeeConfig.getWindow(WINTITLE);
        if (win!=null){
            win.reqdestroy();
            win = null;
        }
        win = new ZeeWindow(Coord.of(160,200), WINTITLE);

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
        Scrollport scroll = win.add(new Scrollport(new Coord(140, 80)), 0, y);
        y = 0;// inside scrollport
        for (String charAndServer : charSwitchListNameServer) {
            String[] arr = charAndServer.split("@"); //name@server
            String charName = arr[0];
            String serverName = arr[1];
            Button btn = scroll.cont.add(new Button(120,charName){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate")){
                        ZeeSess.charSwitchClicked(charName,serverName);
                        charSwitchNextNameAndServer = charAndServer;
                        charSwitchLastMs = ZeeThread.now();
                        ZeeConfig.gameUI.act("lo", "cs");
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
        else if (!charSwitchNextNameAndServer.isEmpty() && charlist.ui != null){
            charlist.wdgmsg("play", charSwitchNextNameAndServer.split("@")[0]);//char@server
            charSwitchNextNameAndServer = "";
        }

    }

    static void charSwitchCancelAutologin(String msg) {
        if(!charSwitchNextNameAndServer.isEmpty()) {
            println("cancel autologin > " + msg);
            charSwitchNextNameAndServer = "";
            charSwitchCurPlayingChar = "";
            charSwitchCurPlayingServer = "";
            charSwitchKeepWindow = false;
            Utils.setprefb("charSwitchKeepWindow", false);
        }
    }

    // called before logout animation, case cancelled
    public static void charSwitchClicked(String name, String server) {
        charSwitchCurPlayingChar = name;
        charSwitchCurPlayingServer = server;
        String str = name+"@"+server;
        charSwitchListNameServer.remove(str);
        charSwitchListNameServer.add(0,str);
    }

    public static void newCharList() {
        println("new char list");

        /*
            fix for:
            Exception in thread "Thread-1" java.lang.NullPointerException
    	        at haven.ZeeConfig$34.run(ZeeConfig.java:3797)
         */
        try{
            ZeeConfig.gobsWaiting.clear();
        }catch (Exception e){
            e.printStackTrace();
        }

        ZeeMidiRadio.stopPlayingMidi("charlist");
    }

    static void println(String s) {
        System.out.println(s);
    }
}

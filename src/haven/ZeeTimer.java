package haven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ZeeTimer {

    private static final String PREF_TIMERS = "prefTimers";
    static final float SERVER_RATIO = 3.29f;

    private static long server;
    private static long local;

    private static List<Timer> listTimers;// = initPrefs();

    // start  =  server + SERVER_RATIO * (now - local)
    // finish date  =  duration + local - (server - start) / SERVER_RATIO
    // remaining  =  duration - now + local - (server - start) / SERVER_RATIO


    public static void checkServerMsg(long newTime) {
        long elapsedServer = (long) ((newTime - server) / SERVER_RATIO);
        long elapsedLocal = ZeeThread.now() - local;
        server = newTime;
        local = ZeeThread.now();
        //println("elapsed  server = "+elapsedServer+" , local = "+elapsedLocal);
        //checkTimers();
    }

    private static void checkTimers() {
        if (listTimers.isEmpty())
            return;
        for (Timer timer : listTimers) {

        }
    }

    private static void savePrefs(){
        Utils.setpref(PREF_TIMERS, ZeeConfig.serialize((Serializable) listTimers));
    }

    @SuppressWarnings("unchecked")
    private static List<Timer> initPrefs() {
        String s = Utils.getpref(PREF_TIMERS,"");
        if (s.isEmpty())
            return new ArrayList<Timer>();
        else
            return (ArrayList<Timer>) ZeeConfig.deserialize(s);
    }

    public static void showWindow() {

        String wintitle = "Timers";
        Window win = ZeeConfig.getWindow(wintitle);
        if (win!=null){
            win.reqdestroy();
        }
        win = ZeeConfig.gameUI.add(
            new Window(Coord.of(150,60), wintitle){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("close")){
                        this.reqdestroy();
                    }
                }
            },
            ZeeConfig.gameUI.sz.div(2)
        );

        Widget wdg = null;
        final TextEntry name, hours, minutes, seconds;
        final Button btnadd;
        int y=0, padX=3;
        int textw = UI.scale(45);
        wdg = name = win.add(new TextEntry(UI.scale(120), "timer"));
        wdg = win.add(new Label("hours"), ZeeWindow.posRight(wdg,padX*2,y));
        wdg = hours = win.add(new TextEntry(textw, "00"){
            public void gotfocus() { focusTextEntry(this); }
            public void lostfocus() { focusTextEntry(this); }
        }, ZeeWindow.posRight(wdg,padX,y));
        wdg = win.add(new Label("min"), ZeeWindow.posRight(wdg,padX,y));
        wdg = minutes = win.add(new TextEntry(textw, "00"){
            public void gotfocus() { focusTextEntry(this); }
            public void lostfocus() { focusTextEntry(this); }
        }, ZeeWindow.posRight(wdg,padX,y));
        wdg = win.add(new Label("sec"), ZeeWindow.posRight(wdg,padX,y));
        wdg = seconds = win.add(new TextEntry(textw, "00"){
            public void gotfocus() { focusTextEntry(this); }
            public void lostfocus() { focusTextEntry(this); }
        }, ZeeWindow.posRight(wdg,padX,y));
        wdg = btnadd = win.add(new Button(UI.scale(50), "Add"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){

                }
            }
        }, ZeeWindow.posRight(wdg,padX,y));

        win.pack();
    }

    private static void focusTextEntry(TextEntry textEntry) {
        if(textEntry.hasfocus) {
            textEntry.settext("");
        } else {
            try {
                textEntry.settext(String.format("%02d", Integer.parseInt(textEntry.text())));
            } catch (NumberFormatException ignored) {
                textEntry.settext("00");
            }
        }
    }

    private static void println(String s) {
        System.out.println(s);
    }

    private class Timer {
        String name;
        long start;
        long duration;
    }
}

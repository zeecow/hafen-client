package haven;

import java.util.ArrayList;
import java.util.List;

public class ZeeManagerRepeat {

    static Window windowManager;
    static Button buttonRepeat;
    static List<Object[]> msgQueue;

    static void newUIWdgmsg(Widget sender, String msg, Object[] args) {

        // show window
        if(windowManager ==null) {
            createWindow();
        }else{
            windowManager.show();
        }

        // add new msg to queue
        if (msgQueue == null)
            msgQueue = new ArrayList<>();
        msgQueue.add(new Object[]{sender, msg, args});
        if (msgQueue.size() > 7)
            msgQueue.remove(0);

        // cleanup window list
        windowManager.children().forEach(widget -> {
            if (widget instanceof Label || widget instanceof ZeeWindow.ZeeButton || widget instanceof CheckBox)
                widget.reqdestroy();
        });

        // build window list
        Widget wdg;
        int x=0, y=0;
        for (int i = 0; i < msgQueue.size(); i++) {
            Widget sender1 = (Widget) msgQueue.get(i)[0];
            String msg1 = (String) msgQueue.get(i)[1];
            Object[] args1 = (Object[]) msgQueue.get(i)[2];
            //sender
            wdg = windowManager.add(new Label(sender1.getClass().getName()),x,y);
            x += wdg.sz.x + 5;
            //msg
            wdg = windowManager.add(new Label(msg1),x,y);
            x += wdg.sz.x + 5;
            //args
            wdg = windowManager.add(new Label(ZeeConfig.strArgs(args1)),x,y);
            x = 0;
            y += 15;
        }
        buttonRepeat.c.y = y + 5;

    }

    static boolean isActive() {
        return windowManager!=null && windowManager.visible();
    }

    private static void createWindow() {
        windowManager = new ZeeWindow(new Coord(300, 150), "Repeater") {
            public void wdgmsg(String msg, Object... args) {
                if (msg.equals("close")) {
                    //busy = false;
                    exitManager();
                }else
                    super.wdgmsg(msg, args);
            }
        };

        buttonRepeat = windowManager.add(new Button(UI.scale(75),"repeat"){
            public void wdgmsg(String msg, Object... args) {
                if(msg.equals("activate")){
                    repeat();
                }
            }
        }, 0,0);

        ZeeConfig.gameUI.add(windowManager, new Coord(300,300));
    }

    static void exitManager() {
        if (msgQueue!=null)
            msgQueue.clear();
        if (windowManager!=null)
            windowManager.reqdestroy();
        windowManager = null;
    }

    static void repeat(){
        println("repeatManager > ");
    }

    static void toggleWindow() {
        if (windowManager!=null){
            exitManager();
        }
        else {
            createWindow();
        }
    }

    static void println(String msg){
        System.out.println(msg);
    }
    static void printf(String format, Object...args){
        System.out.printf(format,args);
    }

}

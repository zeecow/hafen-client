package haven;

import java.util.List;

public class ZeeTaskBuilder {

    static List<String> btnames = List.of("if","while");
    static Widget taskPanel;
    static Window winTaskBuilder;
    static void showWindow(){

        String winName = "Task Builder";
        winTaskBuilder = ZeeConfig.getWindow(winName);
        if (winTaskBuilder != null) {
            winTaskBuilder.reqdestroy();
        }
        winTaskBuilder = ZeeConfig.gameUI.add(
                new Window(Coord.of(240,140), winName){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("close")){
                            //exit("msg")
                            this.reqdestroy();
                        }
                    }
                }, ZeeConfig.gameUI.sz.div(2)
        );

        int x=0, y=0;
        Widget wdg = null;
        for (String btname : btnames) {
            wdg = winTaskBuilder.add(new ZeeWindow.ZeeButton(btname){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate")){
                        appendToTaskPanel(new Label(this.text.text));
                    }
                }
            },x,y);
            x += wdg.sz.x;
            if ( wdg.c.x + wdg.sz.x >= (winTaskBuilder.sz.x * .85))
                y += wdg.sz.y;
        }

        taskPanel = new Widget();
        winTaskBuilder.add(taskPanel, 0,y + wdg.sz.y);
    }

    static void appendToTaskPanel(Widget newWdg){
        if (taskPanel==null){
            println("taskPanel null");
            return;
        }
        Widget bottomWdg = null;
        for (Widget child : taskPanel.children()) {
            if (bottomWdg==null)
                bottomWdg = child;
            else if (child.c.y > bottomWdg.c.y)
                bottomWdg = child;
        }
        if (bottomWdg==null)
            taskPanel.add(newWdg,0,0);
        else
            taskPanel.add(newWdg,0,bottomWdg.c.y+bottomWdg.sz.y);
        taskPanel.pack();
        winTaskBuilder.pack();
    }

    public static void println(String s) {
        System.out.println(s);
    }

}

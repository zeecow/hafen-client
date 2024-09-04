package haven;

import java.util.ArrayList;
import java.util.List;

public class ZeeTaskBuilder {

    static String winName = "Task Builder";
    static Dropbox<String> dropbox;
    static int x=0, y=0, tokensY;

    static void showWindow(){

        // create window
        Window temp = ZeeConfig.getWindow(winName);
        if ( temp != null) {
            temp.reqdestroy();
        }
        ZeeConfig.gameUI.add(
                new Window(Coord.of(240,140), winName){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("close")){
                            //exit("msg")
                            this.reqdestroy();
                        }
                    }
                }, ZeeConfig.gameUI.sz.div(2)
        );

        winAppend(new Label("* rclick remove btns"));
        x = 0;
        y += 15;

        // dropbox
        winAppend( dropbox = new Dropbox<String>(110, 14, 20) {
                String space = "     ";
                private final List<String> filters = new ArrayList<String>() {{
                    add(space + "gob");
                    add(space + "item");
                    add(space + "tile");
                    add(space + "window");
                    add(space + "nameStarts");
                    add(space + "nameContains");
                    add(space + "nameEnds");
                    add(space + "lift");
                    add(space + "click");
                }};
                protected String listitem(int idx) {
                    return (filters.get(idx));
                }
                protected int listitems() {
                    return (filters.size());
                }
                protected void drawitem(GOut g, String name, int idx) {
                    g.atext(name, Coord.of(0, g.sz().y / 2), 0.0, 0.5);
                }
                public void change(String filter) {
                    super.change(filter);
                    addTokenButton(filter.strip());
                }
                public void dispose() {
                    super.dispose();
                    this.sel = "";
                }
            }
        );

        // ctrl buttons
        winAppend(new ZeeWindow.ZeeButton("test"){
            public void wdgmsg(String msg, Object... args) {
                if (msg.contentEquals("activate")){
                    ZeeConfig.msg("test");
                }
            }
        });

        x = 0;
        y += 25;
        tokensY = y;
    }


    static List<ZeeButtonRemovable> listAppended;
    private static Widget winAppend(Widget w) {
        Window win = ZeeConfig.getWindow(winName);
        if (win == null) {
            return null;
        }
        Widget ret = win.add(w,x,y);
        x += ret.sz.x + 3;
        if ( ret.c.x + ret.sz.x >= (win.sz.x * .80)) {
            y += ret.sz.y + 3;
            x = 0;
        }
        //config wdg
        if (w instanceof ZeeButtonRemovable) {
            if (listAppended == null)
                listAppended = new ArrayList<>();
            if (!listAppended.contains(w))
                listAppended.add((ZeeButtonRemovable) ret);
        }
        return ret;
    }

    private static void addTokenButton(String s) {
        winAppend(
            new ZeeButtonRemovable(s){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate")){
                        println(this.buttonText);
                    }
                }
            }
        );
    }

    private static void updateRemovables() {
        for (ZeeButtonRemovable w0 : listAppended) {
            w0.remove();
        }
        x=0;
        y=tokensY;
        for (ZeeButtonRemovable w1 : listAppended) {
            winAppend(w1);
        }
    }

    private static class ZeeButtonRemovable extends ZeeWindow.ZeeButton {
        public ZeeButtonRemovable(String btnName) {
            super(btnName);
        }
        public boolean mouseup(Coord c, int button) {
            if (button!=3)
                return super.mouseup(c,button);
            this.remove();// rclick remove widget
            listAppended.remove(this);
            ZeeTaskBuilder.updateRemovables();
            return false;
        }
    }

    public static void runCmdZ(String[] args) {
        try{
            String s = "";
            for (int i = 1; i < args.length; i++) {
                s += args[i].toLowerCase();
            }
            println("z > "+s);
            if (s.isBlank() || s.endsWith("-h") || s.endsWith("--help")){
                println("z  [cmd]");
                println("   cl      clear gob text and pointers ");
                println("   clt     clear gob texts");
                println("   clp     clear gob pointers");
                println("   clc     clear gob colors");
                return;
            }
            s = s.replaceAll("^z\\s+","").strip();
            if (s.contentEquals("cl")){
                ZeeManagerGobClick.clearGobsTextsAndPointers();
            }
            else if (s.contains("clt")){
                ZeeManagerGobClick.clearGobsTexts();
            }
            else if (s.contains("clp")){
                ZeeManagerGobClick.clearGobsPointers();
            }
            else if (s.contains("clc")){
                ZeeManagerGobClick.clearGobsColors();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void println(String s) {
        System.out.println(s);
    }

}

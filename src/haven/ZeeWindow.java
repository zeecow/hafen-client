package haven;

import java.awt.*;

public class ZeeWindow extends Window {

    public ZeeWindow(Coord size, String title) {
        super(size, title);
    }

    public static Coord posBelow(Widget wdg, int padX, int padY) {
        return Coord.of(wdg.c.x + padX, wdg.c.y + wdg.sz.y + padY);
    }

    @Override
    public void wdgmsg(String msg, Object... args) {
        //ZeeConfig.println(this.getClass().getSimpleName()+" > "+msg);
        if(msg.equals("close")){
            reqdestroy();
        }
    }

    static class ZeeButton extends Button{

        public static final String TEXT_ORGANIZEWINDOWS = "↔";
        //"◀" "⊲" "◁" "ᐊ"
        public static final String TEXT_AUTOHIDEWINDOW = "ᐊ";
        public static final String TEXT_AUTOHIDEWINDOW_FAST = "◀";
        public static final String TEXT_CLOSE = "x";
        public static final int BUTTON_SIZE = 20;
        String buttonText;

        public ZeeButton(int width, String title) {
            super(width,title);
            this.buttonText = title;
        }

        public ZeeButton(int width, String title, String tooltip) {
            super(width,title);
            this.buttonText = title;
            this.settip(tooltip);
        }

        @Override
        public void wdgmsg(String msg, Object... args) {
            String windowName = this.getparent(Window.class).cap;
            //ZeeConfig.println(windowName+" > "+ buttonText +" > "+msg);
            if(msg.equals("activate")){
                //organize duplicate windows
                if(buttonText.equals(TEXT_ORGANIZEWINDOWS)){
                    organizeDuplicateWindows(windowName);
                }
                //auto hide window
                else if (buttonText.contentEquals(TEXT_AUTOHIDEWINDOW) || buttonText.contentEquals(TEXT_AUTOHIDEWINDOW_FAST)){
                    Window win = this.getparent(Window.class);
                    if (!win.isAutoHideOn){
                        // delay mode
                        win.isAutoHideOn = true;
                        if(!ZeeConfig.listAutoHideWindowsActive.contains(win.cap))
                            ZeeConfig.listAutoHideWindowsActive.add(win.cap);

                        win.isAutoHideFast = false;
                        ZeeConfig.listAutoHideWindowsActiveFast.remove(win.cap);

                        this.change(TEXT_AUTOHIDEWINDOW, new Color(0,200,0));
                    }
                    else {
                        // fast mode
                        if (!win.isAutoHideFast){
                            win.isAutoHideFast = true;
                            this.change(TEXT_AUTOHIDEWINDOW_FAST, new Color(0,200,0));
                            if(!ZeeConfig.listAutoHideWindowsActiveFast.contains(win.cap))
                                ZeeConfig.listAutoHideWindowsActiveFast.add(win.cap);
                        }
                        // off
                        else{
                            win.isAutoHideOn = false;
                            win.isAutoHideFast = false;
                            this.change(TEXT_AUTOHIDEWINDOW);
                            ZeeConfig.listAutoHideWindowsActive.remove(win.cap);
                            ZeeConfig.listAutoHideWindowsActiveFast.remove(win.cap);
                            ZeeConfig.windowFitView(win);
                            ZeeConfig.saveWindowPos(win);
                        }
                    }
                    //save prefs
                    Utils.setprefsl("listAutoHideWindowsActive",ZeeConfig.listAutoHideWindowsActive);
                    Utils.setprefsl("listAutoHideWindowsActiveFast",ZeeConfig.listAutoHideWindowsActiveFast);
                }
                else
                    super.wdgmsg(msg,args);
            }
        }

        private void organizeDuplicateWindows(String windowName) {

            Window[] wins = ZeeConfig.getWindows(windowName).toArray(new Window[0]);
            Coord ui = ZeeConfig.gameUI.sz;
            Coord wsz = wins[0].sz;//wsz
            Window w;
            int x,y,row,col;

            //distribute windows right to left, top to bottom
            row = col = 1;
            for (int i=0; i<wins.length; i++){
                w = wins[i];
                if(i == 0) {
                    x = ui.x - wsz.x;
                    y = 0;
                    col++;
                } else {
                    if(ui.x - (wsz.x * (col-1)) > wsz.x){ //if horizontal space available, pos left
                        x = ui.x - (wsz.x * col);
                        y = wins[i-1].c.y;//same y
                        col++;
                    } else { //no horizontal space, next line
                        x = ui.x - wsz.x;
                        y = wins[i-1].c.y + wsz.y;
                        col = 2; // 1st col already used
                        row++;
                    }
                }
                w.move(new Coord(x, y));
            }
        }
    }
}

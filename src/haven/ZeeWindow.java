package haven;

import java.awt.*;

public class ZeeWindow extends Window {

    public ZeeWindow(Coord size, String title) {
        super(size, title);
    }

    @Override
    public void wdgmsg(String msg, Object... args) {
        //ZeeConfig.println(this.getClass().getSimpleName()+" > "+msg);
        if(msg.equals("close")){
            hide();
        }
    }

    static class ZeeButton extends Button{

        public static final String TEXT_ORGANIZEWINDOWS = "↔";
        public static final String TEXT_AUTOHIDEWINDOW = "ᐊ";
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
            String windowName = this.getparent(Window.class).cap.text;
            //ZeeConfig.println(windowName+" > "+ buttonText +" > "+msg);
            if(msg.equals("activate")){
                //organize duplicate windows
                if(buttonText.equals(TEXT_ORGANIZEWINDOWS)){
                    organizeDuplicateWindows(windowName);
                }
                //auto hide window
                else if (buttonText.contentEquals(TEXT_AUTOHIDEWINDOW)){
                    Window win = this.getparent(Window.class);
                    win.isAutoHideOn = !win.isAutoHideOn;
                    // highlihgt button
                    if (win.isAutoHideOn)
                        this.change(TEXT_AUTOHIDEWINDOW, new Color(0,200,0));
                    else
                        this.change(TEXT_AUTOHIDEWINDOW);
                }
                else
                    super.wdgmsg(msg,args);
            }
        }

        private void organizeDuplicateWindows(String windowName) {

            Window[] wins = ZeeConfig.getWindows(windowName).toArray(new Window[0]);
            Coord ui = ZeeConfig.gameUI.sz;
            Coord wsz = wins[0].wsz;
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

package haven;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ZeeWindow extends Window {

    public ZeeWindow(Coord size, String title) {
        super(size, title);
    }

    public static Coord posBelow(Widget wdg, int padX, int padY) {
        if (wdg==null)
            return Coord.z;
        return Coord.of(wdg.c.x + padX, wdg.c.y + wdg.sz.y + padY);
    }

    public static Coord posRight(Widget wdg, int padX, int y) {
        if (wdg==null)
            return Coord.z;
        return Coord.of(wdg.c.x + wdg.sz.x + padX, y);
    }

    static List<String> uiClassesMouseUp = new ArrayList<String>();
    static String lastClassMouseDown = "";
    public static void checkCloseWinDFStyle(Widget widget, MouseButtonEvent ev) {

        // works on right click
        if (ev.b!=3)
            return;

        // skip login screen
        if ( ZeeConfig.gameUI==null || ZeeConfig.gameUI.ui==null){
            return;
        }

        // skip transfers
        if ( ZeeConfig.gameUI.ui.modmeta || ZeeConfig.gameUI.ui.modshift){
            return;
        }

        String className = widget.getClass().getName();

        // save mousedown class and return
        if (ev instanceof MouseDownEvent){
            lastClassMouseDown = className;
            return;
        }

        // reset classes mouseup on RootWidget
        if (className.startsWith("haven.RootWidget")){
            uiClassesMouseUp.clear();
            return;
        }

        // skip WItem clicks
        if (lastClassMouseDown.contentEquals("haven.WItem") || uiClassesMouseUp.contains("haven.WItem")){
            return;
        }

        // skip holding item
        if (ZeeConfig.isPlayerHoldingItem()){
            return;
        }

        // add class mouseup
        uiClassesMouseUp.add(className);

        if (widget instanceof Window.DefaultDeco) {
            ZeeConfig.println("uiClassesMouseUp > "+uiClassesMouseUp.toString());
            if (uiClassesMouseUp.contains("haven.MapWnd$View")) {
                return;
            }
            Window win = (Window) widget.parent;
            if (uiClassesMouseUp.contains("haven.MapWnd")) {
                ZeeConfig.gameUI.mapfile.compact(true);
                Utils.setprefb("compact-map", true);
            }else{
                win.reqclose();
            }
            uiClassesMouseUp.clear();
        }
    }

    @Override
    public void wdgmsg(String msg, Object... args) {
        if(msg.equals("close")){
            reqdestroy();
        }
    }

    static abstract class ZeeTextEntry extends TextEntry{
        public ZeeTextEntry(int w, String deftext) {
            super(w, deftext, false);
            this.settip("scroll values, press enter");
        }
        public static boolean isControlKey(int keyCode) {
            return keyCode==KeyEvent.VK_RIGHT || keyCode==KeyEvent.VK_LEFT || keyCode==KeyEvent.VK_BACK_SPACE || keyCode==KeyEvent.VK_DELETE || keyCode==KeyEvent.VK_HOME || keyCode==KeyEvent.VK_END || keyCode==KeyEvent.VK_SPACE;
        }
        abstract void onEnterPressed(String text);
        public boolean mouseup(MouseUpEvent ev) {
            hasfocus = true;
            return super.mouseup(ev);
        }
        public boolean keydown(KeyDownEvent ev) {
            if(!hasfocus)
                return false;
            return super.keydown(ev);
        }
        public boolean keyup(KeyUpEvent ev) {
            if (!hasfocus)
                return false;
            if (ev.awt.getKeyCode()==KeyEvent.VK_ENTER) {
                if (!buf.line().isEmpty()) {
                    ZeeManagerItems.playFeedbackSound();
                    this.onEnterPressed(buf.line());
                }
                this.hasfocus = false;
                return super.keyup(ev);
            }
            if (!isControlKey(ev.awt.getKeyCode()))
                return false;
            return super.keyup(ev);
        }
        public boolean mousewheel(MouseWheelEvent ev) {
            if (text().chars().allMatch(Character::isDigit) ) {
                settext(getTextEntryNextScrollNumber(text(), ev.a, 0, Integer.MAX_VALUE));
                ZeeManagerItems.playFeedbackSound();
                if (!hasfocus) {
                    //ZeeConfig.println("set focus");
                    setfocus(this);
                }
            }
            return true;
        }
        static String getTextEntryNextScrollNumber(String text, int amount, int min, int max) {
            int num = Integer.parseInt(text);
            if (amount < 0)
                num++;
            else if (amount > 0)
                num--;
            if (num < min)
                num = min;
            else if(num > max)
                num = max;
            return String.valueOf(num);
        }
    }

    static class ZeeButton extends Button{
        public static final String TEXT_ORGANIZEWINDOWS = "↔";
        //"◀" "⊲" "◁" "ᐊ"
        public static final String TEXT_AUTOHIDEWINDOW = "ᐊ";
        public static final String TEXT_AUTOHIDEWINDOW_FAST = "◀";
        public static final String TEXT_CLOSE = "x";
        public static final String TEXT_TOGGLE_EQUIPORY_SIZE = "*";
        public static final int BUTTON_SIZE = 20;
        String buttonText;
        public ZeeButton(String title) {
            super(((15*title.length())-20*(title.length()/3)),title);
            this.buttonText = title;
        }
        public ZeeButton(int width, String title) {
            super(UI.scale(width),title);
            this.buttonText = title;
        }
        public ZeeButton(int width, String title, String tooltip) {
            super(UI.scale(width),title);
            this.buttonText = title;
            this.settip(tooltip);
        }
        @Override
        public void wdgmsg(String msg, Object... args) {
            String windowName = this.getparent(Window.class).cap;
            //ZeeConfig.println(windowName+" > "+ buttonText +" > "+msg);
            if(msg.equals("activate")){
                if (buttonText.equals(TEXT_TOGGLE_EQUIPORY_SIZE)) {
                    ZeeManagerItems.getEquipory().toggleSize();
                }
                //organize duplicate windows
                else if(buttonText.equals(TEXT_ORGANIZEWINDOWS)){
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

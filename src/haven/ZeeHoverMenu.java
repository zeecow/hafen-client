package haven;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class ZeeHoverMenu {

    static final long CHANGE_MENU_MS = 350;

    static MenuWidget rootMenu, latestMenu;
    static boolean isMouseOver = false;
    static long mouseOutMs = -1;
    private static boolean ignoreNextGridMenu;
    private static int latestParentLevel;
    private static MenuLineWidget latestMenuLine;


    static void menuStart() {
        if (rootMenu == null){
            rootMenu = latestMenu = ZeeConfig.gameUI.add(new MenuWidget(null,null));
            positionLatestMenu(rootMenu);
            rootMenu.setfocusctl(true);//for esc exit
        }
    }

    private static void setBottomRightCoord(Coord br, MenuWidget menu) {
        int x = br.x - (menu.sz.x);
        int y = br.y - menu.sz.y;
        menu.c = Coord.of(x,y);
    }

    static void menuGridChanged(Collection<MenuGrid.PagButton> curbtns) {
        if (ignoreNextGridMenu){
            ignoreNextGridMenu = false;
            return;
        }
        if (rootMenu!=null && curbtns.size() > 0){
            MenuGrid.Pagina curPag = ZeeConfig.gameUI.menu.cur;
            // check for sibling menus
            MenuWidget parentMenu;
            if (latestMenu.parentMenu != null  &&  latestParentLevel <= latestMenu.parentMenu.level) {
                // menu replaces sibling tree
                do {
                    MenuWidget tempMenu = latestMenu.parentMenu;
                    latestMenu.destroy();
                    parentMenu = tempMenu;
                    latestMenu = parentMenu;
                } while(latestParentLevel < parentMenu.level);
            } else {
                parentMenu = latestMenu;
            }
            //add menu
            latestMenu = ZeeConfig.gameUI.add(new MenuWidget(curPag,parentMenu));
            positionLatestMenu(latestMenu);
        }
    }

    private static void positionLatestMenu(MenuWidget mnw) {
        GameUI.Hidepanel brpanel = ZeeConfig.gameUI.brpanel;
        Coord brc;
        if (mnw.parentMenu==null) {
            brc = Coord.of(brpanel.c.x, ZeeConfig.gameUI.sz.y);
        }else{
            int parentLineY = mnw.parentMenu.lineSelected.i * mnw.parentMenu.lineSelected.btnHeight;
            int y = mnw.parentMenu.c.y + parentLineY + mnw.sz.y;
            if (y > ZeeConfig.gameUI.sz.y)
                y = ZeeConfig.gameUI.sz.y;
            brc = Coord.of(mnw.parentMenu.c.x, y);
        }
        //println("add lvl"+mnw.level+" to "+brc);
        setBottomRightCoord(brc,mnw);
    }

    static volatile boolean exiting = false;
    public static void exitMenu(String msg){
        if (exiting)
            return;
        exiting = true;
        if (!msg.isBlank())
            println("exit "+(msg!=null && !msg.isEmpty() ? " > "+msg : ""));
        mouseOutMs = -1;
        isMouseOver = false;
        latestMenuLine = null;
        MenuWidget tempMenu = null;
        // destroy menus, from latest to root
        int cont = 0;
        while (latestMenu != null) {
            tempMenu = latestMenu.parentMenu;
            latestMenu.destroy();
            latestMenu = tempMenu;
            cont++;
        }
        //println("destroyed "+cont+" menus");
        resetMenuGrid();
        rootMenu = latestMenu = null;
        exiting = false;
    }

    public static void println(String s) {
        System.out.println(s);
    }


    static boolean isButtonNotSubmenu(MenuGrid.PagButton button){
        Resource.AButton ai = button.act();
        return (ai.ad.length != 0);
    }

    static class MenuWidget extends Widget{

        private final MenuGrid.Pagina pagina;
        MenuLineWidget lineSelected;
        List<MenuGrid.PagButton> btns;
        BufferedImage bg;
        static MenuGrid menuGrid;
        int level;
        MenuWidget parentMenu;
        BufferedImage bgHoverLine = ZeeManagerIcons.imgRect( 160, 32, Color.blue, false, 0,false);

        public MenuWidget(MenuGrid.Pagina pagina, MenuWidget parent){

            menuGrid = ZeeConfig.gameUI.menu;
            //latestMenu = this;
            this.pagina = pagina;
            this.parentMenu = parent;

            // set menu level
            if (parent==null){
                this.level = 0;
            } else {
                this.level = parent.level + 1;
            }
            //println("MenuWidget() lvl "+this.level);

            //construct menu buttons
            this.btns = new ArrayList<>();
            if(!menuGrid.cons(pagina,btns)){
                println("menuGrid.cons == false");
            }
            if (btns.size()==0) {
                println("btns emptyyy");
                return;
            }

            //sort buttons
            Collections.sort(btns, Comparator.comparing(MenuGrid.PagButton::sortkey));

            //build Menu lines
            MenuLineWidget line;
            int lineWidth = 0;
            for (int i = 0; i < btns.size(); i++) {
                line = this.add(new MenuLineWidget(btns.get(i), i, menuGrid, this.level, this));
                if (line.sz.x > lineWidth)
                    lineWidth = line.sz.x;
            }
            this.pack();

            //resize lines
            for (MenuLineWidget menuLine : children(MenuLineWidget.class)) {
                menuLine.resize(lineWidth,menuLine.sz.y);
            }
            //bg line hover
            bgHoverLine = ZeeManagerIcons.imgRect( lineWidth, btns.get(0).img().getHeight(), Color.blue, false, 0,false);

            //bg menu img
            this.bg = ZeeManagerIcons.imgRect( this.sz.x, this.sz.y, ZeeConfig.intToColor(ZeeConfig.simpleWindowColorInt), ZeeConfig.simpleWindowBorder, 0,false);
        }

        public void draw(GOut g) {
            g.image(this.bg,Coord.z);
            super.draw(g);
        }
    }

    static class MenuLineWidget extends Widget{

        final MenuGrid menuGrid;
        final MenuWidget menuWidget;
        IButton btn;
        MenuGrid.Pagina pagina;
        final int i, btnWidth, btnHeight;
        Label label;
        Coord lineTopRight;
        int menuLevel;
        long msHoverLine = -1;

        public MenuLineWidget(MenuGrid.PagButton pagButton, int i, MenuGrid menuGrid, int level, MenuWidget menuWidget) {
            this.menuWidget = menuWidget;
            this.pagina = pagButton.pag;
            this.i =  i;
            this.menuGrid = menuGrid;
            this.menuLevel = level;

            // arrBtns.get(i).getres().flayer(Resource.Image.class)
            BufferedImage img = pagButton.getres().flayer(Resource.Image.class).img;
            img = ZeeManagerIcons.resizeBufferedImage(img,MenuGrid.bgsz.x,MenuGrid.bgsz.y);
            this.btn = new IButton(img, img);
            this.btnWidth = img.getWidth();
            this.btnHeight = img.getHeight();
            int x=0;
            int y = i * this.btnHeight;
            this.add(new IButton(img, img), Coord.of(x,y));
            x += img.getWidth();
            this.add(label=new Label(pagButton.name()), Coord.of(x,y));
            this.pack();
            this.lineTopRight = Coord.of(0,y);
        }

        public void draw(GOut g) {
            if (this.equals(menuWidget.lineSelected))
                g.image(menuWidget.bgHoverLine, this.lineTopRight);
            super.draw(g);
        }

        public void mousemove(MouseMoveEvent ev) {
            int y = this.i * this.btnHeight;
            Coord evc = ev.c;
            if(evc.y > y   &&  evc.y < (y + this.btnHeight)
                && evc.x > this.c.x && evc.x < this.c.x+this.sz.x)
            {
                //println(this.pagina.button().name() + "" + c.toString());
                menuWidget.lineSelected = this;
                //open submenu buttons only
                if (isButtonNotSubmenu(this.pagina.button())) {
                    return;
                }
                //menu already opened
                if (latestMenu!=null && latestMenuLine!=null && latestMenuLine.equals(this)) {
                    return;
                }
                // first mousemove
                if (msHoverLine==-1) {
                    msHoverLine = ZeeThread.now();
                }
                // open new submenu
                else if (ZeeThread.now() - msHoverLine > ZeeHoverMenu.CHANGE_MENU_MS){
                    //isHoverLine = false;
                    msHoverLine = -1;
                    ZeeHoverMenu.latestParentLevel = this.menuLevel;
                    ZeeHoverMenu.latestMenuLine = MenuLineWidget.this;
                    menuGrid.use(pagina.button(), new MenuGrid.Interaction(), false);
                }
                return;
            }
            return;
        }

        public boolean mouseup(MouseUpEvent ev) {
            if(this.equals(menuWidget.lineSelected) && isButtonNotSubmenu(pagina.button())) {
                // ignore next gridMenu due to reset
                ZeeHoverMenu.ignoreNextGridMenu = true;
                // click non submenu and reset gridMenu
                menuGrid.use(pagina.button(), new MenuGrid.Interaction(), true);
                //close open menus
                //exitMenu("mouseup line "+pagina.button().name());
                return true;
            }
            return false;
        }

        public boolean mousedown(MouseDownEvent ev) {
            return true;//grabs click?
        }
    }

    static boolean checkExitEsc(KeyEvent evt) {
        if (evt.getKeyCode()==KeyEvent.VK_ESCAPE){
            if (rootMenu!=null && !exiting) {
                exitMenu("exit hovermenu ESC");
                return true;
            }
        }
        return false;
    }

    static void exitIfMenuExists() {
        if (rootMenu!=null && !exiting) {
            exitMenu("");
        }
    }

    static void resetMenuGrid(){
        ZeeHoverMenu.ignoreNextGridMenu = true;
        ZeeConfig.gameUI.menu.change(null);
    }
}

package haven;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class ZeeHoverMenu {

    static final int START_MENU_MS = 950;
    static final long EXIT_MENU_MS = 1000;
    static final long CHANGE_MENU_MS = 700;

    static MenuWidget rootMenu, latestMenu;
    static boolean isMouseOver = false;
    static long mouseOutMs = -1;
    private static boolean ignoreNextGridMenu;
    private static int latestParentLevel;

    public static void mouseMoved(GameUI.MenuButton menuButton, Coord c) {
        if (menuButton.checkhit(c)) {
            if (!isMouseOver) {
                //println("root ismouseover true");
                isMouseOver = true;
                menuStart();
            }
        } else {
            if (isMouseOver && !MenuWidget.hit) {
                //println("root ismouseover false");
                isMouseOver = false;
            }
        }
    }

    private static void menuStart() {
        if (!isMouseOver || rootMenu !=null)
            return;
        new ZeeThread(){
            public void run() {
                int countMs = 0;
                try {
                    while (isMouseOver){
                        if (countMs > ZeeHoverMenu.START_MENU_MS)
                            break;
                        sleep(50);
                        countMs += 50;
                    }
                    if (isMouseOver)
                        menuStart2();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private static void menuStart2() {
        if (rootMenu == null){
            rootMenu = latestMenu = ZeeConfig.gameUI.add(new MenuWidget(null,null));
            positionLatestMenu(rootMenu);
        }
    }

    private static void setBottomRightCoord(Coord br, MenuWidget menu) {
        int x = br.x - (menu.sz.x);
        int y = br.y - menu.sz.y;
        menu.c = Coord.of(x,y);
    }

    static List<MenuGrid.PagButton> curbtns;
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
            int y = mnw.parentMenu.c.y + mnw.sz.y;
            if (y > ZeeConfig.gameUI.sz.y)
                y = ZeeConfig.gameUI.sz.y;
            brc = Coord.of(mnw.parentMenu.c.x, y);
        }
        println("add lvl"+mnw.level+" to "+brc);
        setBottomRightCoord(brc,mnw);
    }

    static volatile boolean exiting = false;
    public static void exitMenu(){
        if (exiting)
            return;
        exiting = true;
        println("exit");
        mouseOutMs = -1;
        isMouseOver = false;
        MenuWidget tempMenu = null;
        // destroy menus, from latest to root
        int cont = 0;
        while (latestMenu != null) {
            tempMenu = latestMenu.parentMenu;
            latestMenu.destroy();
            latestMenu = tempMenu;
            cont++;
        }
        println("destroyed "+cont+" menus");
        rootMenu = null;
        exiting = false;
    }

    public static void println(String s) {
        System.out.println(s);
    }


    static boolean isButtonNotSubmenu(MenuGrid.PagButton button){
        Resource.AButton ai = button.pag.act();
        return (ai.ad.length != 0);
    }

    static class MenuWidget extends Widget{

        private final MenuGrid.Pagina pagina;
        List<MenuGrid.PagButton> btns;
        BufferedImage bg;
        static boolean hit;
        static MenuGrid menuGrid;
        int level;
        MenuWidget parentMenu;

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
            println("MenuWidget() lvl "+this.level);

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
                line = this.add(new MenuLineWidget(btns.get(i), i, menuGrid, this.level));
                if (line.sz.x > lineWidth)
                    lineWidth = line.sz.x;
            }
            this.pack();

            //resize lines
            for (MenuLineWidget menuLine : children(MenuLineWidget.class)) {
                menuLine.resize(lineWidth,menuLine.sz.y);
            }
            //bg line hover
            MenuLineWidget.bgHoverLine = ZeeManagerIcons.imgRect( lineWidth, btns.get(0).img().getHeight(), Color.blue, false, 0);

            //bg menu img
            this.bg = ZeeManagerIcons.imgRect( this.sz.x, this.sz.y, ZeeConfig.intToColor(ZeeConfig.simpleWindowColorInt), ZeeConfig.simpleWindowBorder, 0);
        }

        public void draw(GOut g) {
            g.image(this.bg,Coord.z);
            super.draw(g);
        }

        public void mousemove(Coord c) {
            hit = c.isect(Coord.z, this.sz);
            if(!hit){
                //println("out");
                if (mouseOutMs == -1){
                    mouseOutMs = ZeeThread.now();
                }
                if (rootMenu!=null && mouseOutMs!=-1 && ZeeThread.now()-mouseOutMs > ZeeHoverMenu.EXIT_MENU_MS) {//1s outside to exit menu
                    exitMenu();
                    return;
                }
            }
            else {
                //println("in");
                mouseOutMs = -1;
            }
            super.mousemove(c);
        }
    }

    static class MenuLineWidget extends Widget{

        final MenuGrid menuGrid;
        IButton btn;
        MenuGrid.Pagina pagina;
        final int i, btnWidth, btnHeight;
        boolean isHoverLine;
        static BufferedImage bgHoverLine;
        Label label;
        Coord lineTopRight;
        int menuLevel;
        long msHoverLine = -1;

        public MenuLineWidget(MenuGrid.PagButton pagButton, int i, MenuGrid menuGrid, int level) {
            this.pagina = pagButton.pag;
            this.i =  i;
            this.menuGrid = menuGrid;
            this.menuLevel = level;

            this.btn = new IButton(pagButton.img(),pagButton.img());
            this.btnWidth = pagButton.img().getWidth();
            this.btnHeight = pagButton.img().getHeight();
            int x=0;
            int y = i * this.btnHeight;
            this.add(new IButton(pagButton.img(), pagButton.img()), Coord.of(x,y));
            x += pagButton.img().getWidth();
            this.add(label=new Label(pagButton.name()), Coord.of(x,y));
            this.pack();
            this.lineTopRight = Coord.of(0,y);
        }

        public void draw(GOut g) {
            if (isHoverLine)
                g.image(bgHoverLine, this.lineTopRight);
            super.draw(g);
        }

        public void mousemove(Coord c) {
            int y = this.i * this.btnHeight;
            if(c.y > y   &&  c.y < (y + this.btnHeight)
                && c.x > this.c.x && c.x < this.c.x+this.sz.x)
            {
                isHoverLine = true;
                //open submenu buttons only
                if (isButtonNotSubmenu(this.pagina.button()))
                    return;
                if (msHoverLine==-1) {
                    msHoverLine = ZeeThread.now();
                }
                // open new submenu
                else if (ZeeThread.now() - msHoverLine > ZeeHoverMenu.CHANGE_MENU_MS){
                    isHoverLine = false;
                    msHoverLine = -1;
                    ZeeHoverMenu.latestParentLevel = this.menuLevel;
                    menuGrid.use(pagina.button(), new MenuGrid.Interaction(), false);
                }
            }
            else {
                isHoverLine = false;
                msHoverLine = -1;
            }
            //super.mousemove(c);
        }

        public boolean mouseup(Coord c, int button) {
            if(isHoverLine && isButtonNotSubmenu(pagina.button())) {
                // ignore next gridMenu due to reset
                ZeeHoverMenu.ignoreNextGridMenu = true;
                // click non submenu and reset gridMenu
                menuGrid.use(pagina.button(), new MenuGrid.Interaction(), true);
                //close open menus
                exitMenu();
                return true;
            }
            return false;
        }

        public boolean mousedown(Coord c, int button) {
            return true;//grabs click?
        }
    }
}

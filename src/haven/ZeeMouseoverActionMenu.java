package haven;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ZeeMouseoverActionMenu {

    static Widget rootMenu;
    static boolean isMouseOver = false;

    public static void mouseMoved(GameUI.MenuButton menuButton, Coord c) {
        if (menuButton.checkhit(c)) {
            if (!isMouseOver) {
                //println("root ismouseover true");
                isMouseOver = true;
                menuStart();
            }
        } else {
            if (isMouseOver && !MenuWdgGroup.hit) {
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
                        if (countMs > 950)
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
        if (rootMenu ==null){
            rootMenu = ZeeConfig.gameUI.add(new MenuWdgGroup(null));
        }
    }

    private static void setBottomRightCoord(Coord br,Widget menu) {
        int x = br.x - menu.sz.x + 33;
        int y = br.y - menu.sz.y;
        menu.c = Coord.of(x,y);
    }

    static List<MenuGrid.PagButton> curbtns;
    static void menuGridButtons(int curoff) {
        //println("curoff = "+curoff);
    }

    public static void exitMenu(){
        if (rootMenu !=null) {
            rootMenu.destroy();
            rootMenu = null;
            isMouseOver = false;
        }
    }

    public static void println(String s) {
        System.out.println(s);
    }



    static class MenuWdgGroup extends Widget{

        List<MenuGrid.PagButton> btns;
        BufferedImage bg;
        static boolean hit;
        static MenuGrid menuGrid;

        public MenuWdgGroup(MenuGrid.Pagina pagina){
            menuGrid = ZeeConfig.gameUI.menu;
            //root menu
            if (pagina==null){
                pagina = menuGrid.cur;
            }
            this.btns = new ArrayList<>();
            if(!menuGrid.cons(pagina,btns)){
                println("menuGrid.cons == false");
            }
            if (btns.size()==0) {
                println("btns emptyyy");
                return;
            }
            //fill buttons
            for (int i = 0; i < btns.size(); i++) {
                this.add(new MenuWdgLine(btns.get(i), i, menuGrid));
            }
            this.pack();
            this.bg = ZeeManagerIcons.imgRect( this.sz.x, this.sz.y, ZeeConfig.intToColor(ZeeConfig.simpleWindowColorInt), ZeeConfig.simpleWindowBorder, 0);
            GameUI.Hidepanel brpanel = ZeeConfig.gameUI.brpanel;
            setBottomRightCoord(Coord.of(brpanel.c.x, ZeeConfig.gameUI.sz.y),this);
        }

        public void draw(GOut g) {
            g.image(this.bg,Coord.z);
            super.draw(g);
        }

        public void mousemove(Coord c) {
            hit = c.isect(Coord.z, this.sz);
            if(!hit){
                exitMenu();
                println("exit");
                return;
            }
            super.mousemove(c);
        }
    }

    static class MenuWdgLine extends Widget{

        final MenuGrid menuGrid;
        IButton btn;
        MenuGrid.Pagina pagina;
        final int i, btnWidth, btnHeight;
        boolean isHoverLine;
        static BufferedImage bgHoverLine = ZeeManagerIcons.imgRect( 200, 32, Color.blue, false, 0);
        Label label;
        Coord lineTopRight;

        public MenuWdgLine(MenuGrid.PagButton pagButton, int i, MenuGrid menuGrid) {
            this.btn = new IButton(pagButton.img(),pagButton.img());
            this.pagina = pagButton.pag;
            this.i =  i;
            this.btnWidth = pagButton.img().getWidth();
            this.btnHeight = pagButton.img().getHeight();
            int x=0;
            int y = i * this.btnHeight;
            this.add(new IButton(pagButton.img(), pagButton.img()), Coord.of(x,y));
            x += pagButton.img().getWidth();
            this.add(label=new Label(pagButton.name()), Coord.of(x,y));
            this.pack();
            this.lineTopRight = Coord.of(0,y);
            this.menuGrid = menuGrid;
        }

        public void draw(GOut g) {
            if (isHoverLine)
                g.image(bgHoverLine, this.lineTopRight);
            super.draw(g);
        }

        public void mousemove(Coord c) {
            int y = this.i * this.btnHeight;
            if( c.y > y   &&  c.y < (y + this.btnHeight) ) {
                isHoverLine = true;
                //println(this.btn.name() + " " + c + " " + isHoverLine);
            }else{
                isHoverLine = false;
            }
            //super.mousemove(c);
        }

        public boolean mouseup(Coord c, int button) {
            if(isHoverLine) {
                println("clicked "+pagina.button().name());
                menuGrid.use(pagina.button(), new MenuGrid.Interaction(), false);
                return true;
            }
            return false;
        }

        public boolean mousedown(Coord c, int button) {
            return true;//grabs click?
        }
    }
}

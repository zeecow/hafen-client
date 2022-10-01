package haven;

import java.util.List;

public class ZeeMouseoverActionMenu {

    static Widget menu;

    static boolean isMouseOver = false;

    public static void mouseMoved(boolean checkhit) {
        if (checkhit) {
            if (!isMouseOver) {
                isMouseOver = true;
                menuStart();
            }
        } else {
            if (isMouseOver) {
                isMouseOver = false;
                ZeeThread.println("mouse left");
                if (menu!=null) {
                    menu.destroy();
                    menu = null;
                }
            }
        }
    }

    private static void menuStart() {
        if (!isMouseOver)
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
        GameUI gameUI = ZeeConfig.gameUI;
        GameUI.Hidepanel brpanel = ZeeConfig.gameUI.brpanel;
        Coord brc = Coord.of(brpanel.c.x, gameUI.sz.y);
        if (menu==null){
            menu = ZeeConfig.gameUI.add(new Widget());
        }
        int x=0, y=0;
        MenuGrid.PagButton btn;
        Label label;
        for (int i = 0; i < curbtns.size(); i++) {
            btn = curbtns.get(i);
            menu.add(new IButton(btn.img(), btn.img()), Coord.of(x,y));
            x += btn.img().getWidth();
            y += btn.img().getHeight()/2;
            menu.add(label=new Label(btn.name()),Coord.of(x,y));
            y += btn.img().getHeight()/2;
            x = 0;
        }
        menu.pack();
        setBottomRightCoord(brc);
    }

    private static void setBottomRightCoord(Coord br) {
        int x = br.x - menu.sz.x;
        int y = br.y - menu.sz.y;
        menu.c = Coord.of(x,y);
    }

    static List<MenuGrid.PagButton> curbtns;
    static void menuGridButtons(List<MenuGrid.PagButton> btns) {
        curbtns = btns;
    }

    public static void println(String s) {
        System.out.println(s);
    }

}

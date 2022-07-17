package haven;

import java.util.LinkedList;
import java.util.List;

public class ZeeHistWdg extends Widget{

    final static List<MenuGrid.PagButton> listHistButtons = new LinkedList<MenuGrid.PagButton>();
    final static int numItems = 10;
    final static int xpad = 2;

    ZeeHistWdg(){
        super( Coord.of( (MenuGrid.bgsz.x+ xpad) * numItems, MenuGrid.bgsz.y ) );
    }

    static void checkMenuHistory(MenuGrid.PagButton pagButton, Object[] args) {
        if (listHistButtons.contains(pagButton) ||
            (args!=null &&
            !String.valueOf(args[0]).contentEquals("bp") &&
            !String.valueOf(args[0]).contentEquals("craft")))
        {
            return;
        }
        if(listHistButtons.size() == numItems)
            listHistButtons.remove(0);
        listHistButtons.add(pagButton);
    }

    public void draw(GOut g) {
        Coord c = Coord.z;
        for (int i = 0; i < numItems; i++) {
            g.image(MenuGrid.bg,c);
            if ( i < listHistButtons.size()) {
                g.image(listHistButtons.get(i).img(), c);
            }
            c = c.add(MenuGrid.bgsz.x+ xpad,0);
        }
        super.draw(g);
    }

    @Override
    public boolean mousedown(Coord c, int button) {
        return true;
    }

    public boolean mouseup(Coord c, int button) {
        MenuGrid.PagButton btn = bhit(c);
        if((button == 1 && btn!=null)) {
            // from PagButton.use()
            Object[] args = Utils.extend(new Object[0], btn.res.flayer(Resource.action).ad);
            args = Utils.extend(args, Integer.valueOf(btn.pag.scm.ui.modflags()));
            btn.pag.scm.wdgmsg("act", args);
            return true;
        }
        return false;
    }

    private MenuGrid.PagButton bhit(Coord c) {
        int buttonIndex = c.div(MenuGrid.bgsz.x+xpad).x;
        MenuGrid.PagButton ret = null;
        if (listHistButtons.size() > buttonIndex) {
            ret = listHistButtons.get(buttonIndex);
        }
        //ZeeConfig.println(c + " " + buttonIndex + " " + ret);
        return ret;
    }
}

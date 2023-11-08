package haven;

import java.util.ArrayList;
import java.util.Collections;

public class ZeeHistWdg extends Widget{

    public static boolean ignoreNextMenuMsg;
    static ArrayList<String> arrNames = new ArrayList<>();
    static ArrayList<MenuGrid.PagButton> arrBtns = new ArrayList<MenuGrid.PagButton>();
    final static int maxHist = 10;
    final static int xpad = 2;

    ZeeHistWdg(){
        super( Coord.of( (MenuGrid.bgsz.x+ xpad) * maxHist, MenuGrid.bgsz.y ) );
    }

    static void println(String msg){
        ZeeConfig.println(msg);
    }

    static void checkMenuHistory(MenuGrid.PagButton pagButton, Object[] args) {
        //button already in history
        if (arrNames.contains(pagButton.name())){
            //bump used button if possible
            int ibtn = arrNames.indexOf(pagButton.name());
            if (ibtn < arrNames.size()-1){
                Collections.swap(arrNames,ibtn,ibtn+1);
                Collections.swap(arrBtns,ibtn,ibtn+1);
            }
            return;
        }
        //remove oldest button
        if(arrNames.size() == maxHist) {
            arrNames.remove(0);
            arrBtns.remove(0);
        }
        // add new button to hist
        arrNames.add(0,pagButton.name());
        arrBtns.add(0,pagButton);
    }

    public static void clearHistory() {
        arrBtns.clear();
        arrNames.clear();
    }

    public void draw(GOut g) {
        Coord c = Coord.z;
        for (int i = 0; i < maxHist; i++) {
            g.image(MenuGrid.bg,c);
            if ( i < arrBtns.size()) {
                g.image(arrBtns.get(i).img(), c);
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
        if (button != 1)
            return false;
        int ibtn = buttonIndex(c);
        if (ibtn < 0) {
            return false;
        }
        MenuGrid.PagButton btn = arrBtns.get(ibtn);
        if(btn != null) {
            // from PagButton.use()
            Object[] args = Utils.extend(new Object[0], btn.res.flayer(Resource.action).ad);
            args = Utils.extend(args, Integer.valueOf(btn.pag.scm.ui.modflags()));
            ignoreNextMenuMsg = true;
            btn.pag.scm.wdgmsg("act", args);
            //bump used button if possible
            if (ibtn < arrNames.size()-1){
                Collections.swap(arrNames,ibtn,ibtn+1);
                Collections.swap(arrBtns,ibtn,ibtn+1);
            }
            return true;
        }
        return false;
    }

    private int buttonIndex(Coord c) {
        int ibtn = c.div(MenuGrid.bgsz.x+xpad).x;
        if (arrBtns.size() > ibtn) {
            return ibtn;
        }
        return -1;
    }

    @Override
    public Object tooltip(Coord c, Widget prev) {
        int ibtn = buttonIndex(c);
        if (ibtn < 0)
            return null;
        MenuGrid.PagButton btn = arrBtns.get(ibtn);
        return btn.name();
    }
}

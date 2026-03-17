package haven;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ZeeManagerMinimap {

    // make expanded map window fit screen
    static Coord mapWndLastPos;
    static int mapWndMinHeightBackup=350, mapWndMinHeight=350;
    public static void minimapCompactedOrExpanded(MapWnd mapWnd, boolean compact) {

        if(ZeeConfig.gameUI==null || ZeeConfig.gameUI.mapfile==null) {
            //println("windowMapCompact > gameUI "+gameUI+" , mapfile "+gameUI.mapfile);
            return;
        }

        Coord screenSize = ZeeConfig.gameUI.map.sz;
        Window.DefaultDeco deco = (Window.DefaultDeco) mapWnd.deco;

        // window expanded
        if(!compact){
            if (deco.ca.sz().y < mapWndMinHeight){
                mapWndMinHeightBackup = mapWnd.view.sz.y;
                mapWnd.resize(deco.ca.sz().x, mapWndMinHeight);
            }
            // used when gameUI resize
            mapWndLastPos = new Coord(mapWnd.c);
            // horizontal fit
            if(mapWnd.c.x + deco.ca.sz().x > screenSize.x){
                mapWnd.c = new Coord(screenSize.x - deco.ca.sz().x, mapWnd.c.y);
            }
            // vertical fit
            if(mapWnd.c.y + deco.ca.sz().y > screenSize.y){
                mapWnd.c = new Coord(mapWnd.c.x, screenSize.y - deco.ca.sz().y);
            }else if(mapWnd.c.y < 0){
                mapWnd.c = new Coord(mapWnd.c.x, 0);
            }
        }

        // window compacted
        else{
            if(MiniMap.minimapScale ==1){
                ZeeConfig.gameUI.mapfile.resize(compactMapSizeScale1);
            }else if(MiniMap.minimapScale==2){
                ZeeConfig.gameUI.mapfile.resize(compactMapSizeScale2);
            }else if(MiniMap.minimapScale==3){
                ZeeConfig.gameUI.mapfile.resize(compactMapSizeScale3);
            }else if(MiniMap.minimapScale==4){
                ZeeConfig.gameUI.mapfile.resize(compactMapSizeScale4);
            }
            minimapCompactReposition();

            //recenter player when map compacts
            mapWnd.recenter();
        }


        // show/hide minimap resize btns
        toggleMinimapOptionsButton(mapWnd,compact);

    }

    static Widget wdgMapResizeBtns;
    static void reposMapResizeBtns(){
        if (wdgMapResizeBtns!=null)
            wdgMapResizeBtns.c = Coord.of(0,ZeeConfig.gameUI.mapfile.sz.y-23);
    }
    static void toggleMinimapOptionsButton(MapWnd mapWnd, boolean compact) {

        // hide map resize buttons
        if (!compact && wdgMapResizeBtns!=null){
            wdgMapResizeBtns.reqdestroy();
            wdgMapResizeBtns = null;
        }

        // show map resize buttons
        else if (compact){

            wdgMapResizeBtns = mapWnd.add(new Widget(Coord.z),0,0);
            reposMapResizeBtns();

            // config button
            Button button = wdgMapResizeBtns.add(new Button(20,new String(Character.toChars(0x1F6E0))){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate")){
                        windowMinimapOpts();
                    }
                }
            },Coord.z);
            button.settip("map opts");

            wdgMapResizeBtns.pack();
        }
    }

    static void windowMinimapOpts() {

        String wincap = "map opts";
        Window win = ZeeConfig.getWindow(wincap);
        if (win!=null){
            win.reqdestroy();
            win = null;
        }
        win = ZeeConfig.gameUI.add(new ZeeWindow(Coord.of(105,220),wincap));


        // position config window
        Coord clickPos = Coord.of(ZeeConfig.gameUI.ui.mc);
        if (clickPos.x > ZeeConfig.gameUI.sz.div(2).x) // map on right side
            win.c.x = clickPos.x - win.sz.x -15;
        else // map on left side
            win.c.x = clickPos.x + 25;
        if (clickPos.y < ZeeConfig.gameUI.sz.div(2).y) // map on top half
            win.c.y = clickPos.y;
        else // map bottom half
            win.c.y = clickPos.y - win.sz.div(2).y;


        // add resize buttons
        int y = 1;
        String infoSymbol = new String(Character.toChars(0x1F6C8));
        win.add(new Label("Map size "+infoSymbol),0,y).settip("left-click increase, right-click decrease");
        y += 40;
        win.add(new Button(25,"◀"){
            public boolean mousedown(MouseDownEvent ev) {
                minimapResize("left",ev.b);
                return false;
            }
            public boolean mouseup(MouseUpEvent ev) {
                return false;
            }
        },0,y-12);
        win.add(new Button(25,"▲"){
            public boolean mousedown(MouseDownEvent ev) {
                minimapResize("up",ev.b);
                return false;
            }
            public boolean mouseup(MouseUpEvent ev) {
                return false;
            }
        },25,y-25);
        win.add(new Button(25,"▼"){
            public boolean mousedown(MouseDownEvent ev) {
                minimapResize("down",ev.b);
                return false;
            }
            public boolean mouseup(MouseUpEvent ev) {
                return false;
            }
        },25,y);
        win.add(new Button(25,"▶"){
            public boolean mousedown(MouseDownEvent ev) {
                minimapResize("right",ev.b);
                return false;
            }
            public boolean mouseup(MouseUpEvent ev) {
                return false;
            }
        },50,y-12);


        // add checkbox hide player marks
        y += 35;
        win.add(new CheckBox("hide placed marks"){
            {a = false;}//default show markers
            public void set(boolean a) {
                super.set(a);
                int cont = 0;
                for (MapFile.Marker marker : ZeeConfig.gameUI.mapfile.file.markers) {
                    if (marker instanceof MapFile.PMarker) {
                        marker.mapOptsHide = a;
                        cont++;
                    }
                }
                ZeeConfig.msgLow((a?"hide":"show")+" placed marks");
            }
        },0,y);


        // add marks list
        y += 25;
        win.add(new Label("Natural marks "+infoSymbol),0,y).settip("left-click hide , right-click show only");
        y += 20;
        mapOptsMarksRow = mapOptsMarksCol = 0; // reset 3 col button grid
        Scrollport sp = win.add(new Scrollport(Coord.of(110,100)),0,y);
        for (int i = 0; i < mapOptsMarksResnames.size(); i++) {
            String resname = mapOptsMarksResnames.get(i);
            for (MapFile.Marker marker : ZeeConfig.gameUI.mapfile.file.markers) {
                if (marker instanceof MapFile.SMarker){
                    MapFile.SMarker smark = (MapFile.SMarker) marker;
                    if (smark.res.name.contentEquals(resname)){
                        minimapOptCreateMarkButton(sp.cont, smark);
                        //println("creating mark button "+ markOptsMarksButtonsCreated+"  "+resname);
                        break;
                    }
                }
            }
        }

    }
    static List<String> mapOptsMarksResnames = new ArrayList<>();
    static void minimapOptsAddMark(MapFile.SMarker marker){

        String resname = Loading.waitfor(marker.res).name;

        if (mapOptsMarksResnames.contains(resname)){
            return;
        }
        mapOptsMarksResnames.add(resname);
        println("mapOptsMarks "+ mapOptsMarksResnames.size()+"  "+resname);

        Window win = ZeeConfig.getWindow("map opts");
        if (win==null) {
            return;
        }

        Scrollport sp = win.getchild(Scrollport.class);
        if (sp==null) {
            return;
        }

        // create mark button
        minimapOptCreateMarkButton(sp.cont,marker);
    }
    static boolean mapOptsMarksShowOnlyIsOn = false;
    static int mapOptsMarksRow = 0;
    static int mapOptsMarksCol = 0;
    private static void minimapOptCreateMarkButton(Widget cont, MapFile.SMarker sMarker){

        BufferedImage img = sMarker.res.get().flayer(Resource.imgc).scaled();
        String resname = sMarker.res.name;
        String basename = sMarker.res.get().basename();

        // set button coord in 3 columns grid
        Coord pos = Coord.of(mapOptsMarksCol * img.getWidth(), mapOptsMarksRow * img.getHeight());
        // calc next coord
        if (mapOptsMarksCol == 2) {
            mapOptsMarksCol = 0;
            mapOptsMarksRow++;
        } else {
            mapOptsMarksCol++;
        }

        cont.add(new IButton(img,img){
            public boolean mouseup(MouseUpEvent ev) {
                // left click hide mark
                if (ev.b == 1) {
                    for (MapFile.Marker m1 : ZeeConfig.gameUI.mapfile.file.markers) {
                        if (m1 instanceof MapFile.SMarker) {
                            if (((MapFile.SMarker) m1).res.name.contentEquals(resname)) {
                                m1.mapOptsHide = !m1.mapOptsHide;
                            }
                        }
                    }
                    ZeeConfig.msgLow("toggle hide " + basename);
                }
                // right click show only mark
                else if(ev.b == 3){
                    mapOptsMarksShowOnlyIsOn = !mapOptsMarksShowOnlyIsOn;
                    for (MapFile.Marker m1 : ZeeConfig.gameUI.mapfile.file.markers) {
                        if (m1 instanceof MapFile.SMarker) {
                            if (mapOptsMarksShowOnlyIsOn) {
                                //show clicked mark
                                if (((MapFile.SMarker) m1).res.name.contentEquals(resname)) {
                                    m1.mapOptsHide = !mapOptsMarksShowOnlyIsOn;
                                }
                                //hide other marks
                                else {
                                    m1.mapOptsHide = mapOptsMarksShowOnlyIsOn;
                                }
                            }else{
                                //show all marks
                                m1.mapOptsHide = false;
                            }

                        }
                    }
                    ZeeConfig.msgLow("toggle show only " + basename);
                }
                return true;
            }
            public boolean mousedown(MouseDownEvent ev) {
                return true;
            }
        },pos.x,pos.y).settip(basename);
    }

    private static void minimapResize(String dir, int btn){
        int change = 25;
        if (btn==3)
            change *= -1;
        MapWnd map = ZeeConfig.gameUI.mapfile;
        if (dir.contentEquals("left")){
            minimapPrevSize = Coord.of(map.viewf.sz);
            map.resize(minimapPrevSize.add(change,0));
            if (map.sz.x != minimapPrevSize.x) { // check minimum size
                // left changes win pos
                map.c.x -= change;
                ZeeConfig.saveWindowPos(map);
            }
        } else if (dir.contentEquals("up")) {
            minimapPrevSize = Coord.of(map.viewf.sz);
            map.resize(minimapPrevSize.add(0,change));
            if (map.sz.y != minimapPrevSize.y) { // check minimum size
                // up changes win pos
                map.c.y -= change;
                ZeeConfig.saveWindowPos(map);
            }
        } else if (dir.contentEquals("down")) {
            minimapPrevSize = Coord.of(map.viewf.sz);
            map.resize(minimapPrevSize.add(0,change));
        } else if (dir.contentEquals("right")) {
            minimapPrevSize = Coord.of(map.viewf.sz);
            map.resize(minimapPrevSize.add(change,0));
        }
        minimapCompactResizedMouseup();
    }


    static Coord
            compactMapSizeScale1 = Utils.getprefc("compactMapSizeScale1",Coord.of(150,150)),
            compactMapSizeScale2 = Utils.getprefc("compactMapSizeScale2",Coord.of(200,200)),
            compactMapSizeScale3 = Utils.getprefc("compactMapSizeScale3",Coord.of(250,250)),
            compactMapSizeScale4 = Utils.getprefc("compactMapSizeScale4",Coord.of(300,300)),
            minimapPrevSize;
    static void minimapCompactResizedMouseup() {
        if (!isMinimapCompacted())
            return;
        reposMapResizeBtns();
        Coord sz = ZeeConfig.gameUI.mapfile.sz;
        if (MiniMap.minimapScale==1 && ZeeConfig.gameUI.mapfile.view.zoomlevel==0){
            compactMapSizeScale1 = sz;
            Utils.setprefc("compactMapSizeScale1",sz);
        }
        else if (MiniMap.minimapScale==2){
            compactMapSizeScale2 = sz;
            Utils.setprefc("compactMapSizeScale2",sz);
        }
        else if (MiniMap.minimapScale==3){
            compactMapSizeScale3 = sz;
            Utils.setprefc("compactMapSizeScale3",sz);
        }
        else if (MiniMap.minimapScale==4){
            compactMapSizeScale4 = sz;
            Utils.setprefc("compactMapSizeScale4",sz);
        }
    }
    static int prevScale=-1;
    static void minimapCompactZoomChanged(int scale) {
        if (!ZeeConfig.isMinimapCompacted())
            return;
        if (scale==1 && prevScale>1){
            minimapPrevSize = Coord.of(ZeeConfig.gameUI.mapfile.viewf.sz);
            ZeeConfig.gameUI.mapfile.resize(compactMapSizeScale1);
            prevScale = scale;
            minimapCompactReposition();
        }
        else if (scale==2 && compactMapSizeScale2!=null){
            minimapPrevSize = Coord.of(ZeeConfig.gameUI.mapfile.viewf.sz);
            ZeeConfig.gameUI.mapfile.resize(compactMapSizeScale2);
            prevScale = scale;
            minimapCompactReposition();
        }
        else if (scale==3 && compactMapSizeScale3!=null){
            minimapPrevSize = Coord.of(ZeeConfig.gameUI.mapfile.viewf.sz);
            ZeeConfig.gameUI.mapfile.resize(compactMapSizeScale3);
            prevScale = scale;
            minimapCompactReposition();
        }
        else if (scale==4 && compactMapSizeScale4!=null){
            minimapPrevSize = Coord.of(ZeeConfig.gameUI.mapfile.viewf.sz);
            ZeeConfig.gameUI.mapfile.resize(compactMapSizeScale4);
            prevScale = scale;
            minimapCompactReposition();
        }
    }
    static void minimapCompactReposition() {
        MapWnd map = ZeeConfig.gameUI.mapfile;

        // apply saved win first
        ZeeConfig.windowApplySavedPosition(ZeeConfig.gameUI.mapfile,ZeeWindow.getMinimapWindowTitle());

        // adjust x pos if out of screen, or if on the right side of screen
        if ( map.c.x + map.viewf.sz.x > ZeeConfig.gameUI.sz.x  ||  map.c.x > ZeeConfig.gameUI.sz.x/2)
            map.c.x = ZeeConfig.gameUI.sz.x - map.viewf.sz.x ;

        // adjust y pos only if map is bellow limit
        //if (minimapPrevSize!=null)// && map.c.y > gameUI.sz.y/3)
        //map.c.y -= map.viewf.sz.y - minimapPrevSize.y;

        // minimap resize buttons
        reposMapResizeBtns();
    }

    static boolean isMinimapCompacted(){
        return !ZeeConfig.gameUI.mapfile.tool.visible();
    }

    static void println(String msg){
        ZeeConfig.println(msg);
    }
}

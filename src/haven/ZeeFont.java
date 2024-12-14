package haven;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ZeeFont {

    public static final Text.Foundry TXTFND_PROGRESS_WIDGET = new Text.Foundry(Text.sans.deriveFont(Font.BOLD, UI.scale(15f))).aa(false);
    public static final Text.Foundry TXTFND_TOTAL_DAMAGE = new Text.Foundry(Text.sans.deriveFont(Font.PLAIN, UI.scale(16))).aa(false);
    public static final Text.Foundry TXTFND_GOB_TEXT = new Text.Foundry(Text.sans.deriveFont(Font.PLAIN, UI.scale(11))).aa(false);

    public static void checkDmgHpMaybe(Sprite.Owner owner, Resource res, String str, Color col) {
        if (!(owner instanceof Gob))
            return;
        if (!ZeeConfig.isNumbersOnly(str))
            return;
        if (col==null || !col.equals(Color.red))
            return;
        try {
            Gob gob = (Gob) owner;
            gob.totalDmgHp += Integer.parseInt(str);
            String txt = gob.totalDmgHp + getFleeHpInfo(gob);
            ZeeGobText zeeGobText = new ZeeGobText(txt,Color.yellow,Color.black,5, TXTFND_TOTAL_DAMAGE);
            ZeeConfig.addGobText(gob, zeeGobText);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String getFleeHpInfo(Gob gob) {
        String ret = "";
        final Map<String,java.util.List<Integer>> mapGobFleeMax = Map.ofEntries(
            Map.entry("ants",java.util.List.of(50,50)),
            Map.entry("adder",java.util.List.of(30,70)),
            Map.entry("aurochs",java.util.List.of(250,350)),
            Map.entry("badger",java.util.List.of(150,250)),
            Map.entry("bat",java.util.List.of(60,90)),
            Map.entry("bear",java.util.List.of(500,850)),
            Map.entry("beaver",java.util.List.of(75,100)),
            Map.entry("boar",java.util.List.of(275,450)),
            Map.entry("boreworm",java.util.List.of(800,1200)),
            Map.entry("caveangler",java.util.List.of(850,1200)),
            Map.entry("cavelouse",java.util.List.of(300,1000)),
            Map.entry("caverat",java.util.List.of(75,120)),
            Map.entry("eagleowl",java.util.List.of(100,180)),
            Map.entry("fox",java.util.List.of(75,110)),
            Map.entry("garefowl",java.util.List.of(40,85)),
            Map.entry("goldeneagle",java.util.List.of(80,250)),
            Map.entry("greyseal",java.util.List.of(240,320)),
            Map.entry("lynx",java.util.List.of(150,400)),
            Map.entry("mammoth",java.util.List.of(2800,4000)),
            Map.entry("mouflon",java.util.List.of(120,200)),
            Map.entry("moose",java.util.List.of(800,800)),
            Map.entry("otter",java.util.List.of(60,100)),
            Map.entry("greenooze",java.util.List.of(40,100)), //TODO check name
            Map.entry("pelican",java.util.List.of(80,130)),
            Map.entry("reindeer",java.util.List.of(110,200)),
            Map.entry("reddeer",java.util.List.of(150,200)),
            Map.entry("roedeer",java.util.List.of(80,150)),
            Map.entry("swan",java.util.List.of(80,150)),
            Map.entry("walrus",java.util.List.of(600,900)),
            Map.entry("wildgoat",java.util.List.of(200,300)),
            Map.entry("wildhorse",java.util.List.of(200,320)),
            Map.entry("wolf",java.util.List.of(400,500)),
            Map.entry("wolverine",java.util.List.of(200,300)),
            Map.entry("woodgrouse",java.util.List.of(100,120)) //TODO check male name
        );
        try {
            String basename = gob.getres().basename();
            if (mapGobFleeMax.containsKey(basename)) {
                List<Integer> list = mapGobFleeMax.get(basename);
                if (gob.totalDmgHp < list.get(0))
                    ret += "/" + list.get(0); // flee hp
                else
                    ret += "/" + list.get(1); // max hp
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

}

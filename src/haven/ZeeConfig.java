package haven;

import haven.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static haven.Utils.getprop;

public class ZeeConfig {
    public static boolean dropSeeds = Utils.getprefb("dropSeeds", false);
    public static boolean dropMinedStones = Utils.getprefb("dropMinedStones", true);
    public static boolean dropMinedOre = Utils.getprefb("dropMinedOre", true);
    public static boolean dropMinedOrePrecious = Utils.getprefb("dropMinedOrePrecious", true);
    public static boolean dropMinedCurios = Utils.getprefb("dropMinedCurios", true);

    public final static Set<String> mineablesStone = new HashSet<String>(Arrays.asList(
            "gneiss",
            "basalt",
            "cinnabar",
            "dolomite",
            "feldspar",
            "flint",
            "granite",
            "hornblende",
            "limestone",
            "marble",
            "porphyry",
            "quartz",
            "sandstone",
            "schist",
            "blackcoal",
            "zincspar",
            "apatite",
            "fluorospar",
            "gabbro",
            "corund",
            "kyanite",
            "mica",
            "microlite",
            "orthoclase",
            "soapstone",
            "sodalite",
            "olivine",
            "alabaster",
            "breccia",
            "diabase",
            "arkose",
            "diorite",
            "kyanite",
            "slate"
    ));
    public final static Set<String> mineablesOre = new HashSet<String>(Arrays.asList(
            "cassiterite",
            "chalcopyrite",
            "malachite",
            "ilmenite",
            "limonite",
            "hematite",
            "magnetite"
    ));
    public final static Set<String> mineablesOrePrecious = new HashSet<String>(Arrays.asList(
            "galena",
            "argentite",
            "hornsilver",
            "petzite",
            "sylvanite",
            "nagyagite"
    ));
    public final static Set<String> mineablesCurios = new HashSet<String>(Arrays.asList(
            "catgold",
            "petrifiedshell",
            "strangecrystal"
    ));
}

package haven;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.lang.reflect.Field;
import java.util.*;


public class ZeeConfig {

    public static boolean actionSearchGlobal = Utils.getprefb("actionSearchGlobal", true);
    public static boolean autoClickMenuOption = Utils.getprefb("autoClickMenuOption", true);
    public static String autoClickMenuOptionList = Utils.getpref("autoClickMenuOptionList", "Pick,Pluck,Flay,Slice,Harvest wax");
    public static boolean beltToggleEquips = Utils.getprefb("beltToggleEquips", true);
    public static double cattleRosterHeightPercentage = Utils.getprefd("cattleRosterHeight", 1.0);
    public static boolean dropMinedCurios = Utils.getprefb("dropMinedCurios", true);
    public static boolean dropMinedOre = Utils.getprefb("dropMinedOre", true);
    public static boolean dropMinedOrePrecious = Utils.getprefb("dropMinedOrePrecious", true);
    public static boolean dropMinedStones = Utils.getprefb("dropMinedStones", true);
    public static boolean dropSeeds = false;//always starts off (TODO: set false when character loads)
    public static boolean dropSoil = false;
    public static boolean equiporyCompact = Utils.getprefb("equiporyCompact", false);
    public static String lastWindowDrop = "";
    public static String lastWindowTake = "";

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

    public static Collection<Field> getAllFields(Class<?> type) {
        TreeSet<Field> fields = new TreeSet<Field>(
                new Comparator<Field>() {
                    @Override
                    public int compare(Field o1, Field o2) {
                        int res = o1.getName().compareTo(o2.getName());
                        if (0 != res) {
                            return res;
                        }
                        res = o1.getDeclaringClass().getSimpleName().compareTo(o2.getDeclaringClass().getSimpleName());
                        if (0 != res) {
                            return res;
                        }
                        res = o1.getDeclaringClass().getName().compareTo(o2.getDeclaringClass().getName());
                        return res;
                    }
                });
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    public static void printAllFields(Object obj) {
        for (Field field : getAllFields(obj.getClass())) {
            field.setAccessible(true);
            if (field.getDeclaringClass().isPrimitive())
                continue;
            String name = field.getName();
            Object value = null;
            try {
                value = field.get(obj);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            System.out.printf("%s %s.%s = %s;\n", value==null?" ":"*", field.getDeclaringClass().getSimpleName(), name, value);
        }
    }


    public static void printObj(Widget wdg) {
        //System.out.println( new ReflectionToStringBuilder(wdg, RecursiveToStringStyle.MULTI_LINE_STYLE).toString());
        System.out.println(ReflectionToStringBuilder.toString(wdg, new ZeeMyRecursiveToStringStyle(1)));
    }

    public static void cattleRoster(String type, Widget wdg) {
        if(type.contains("rosters/") && ZeeConfig.cattleRosterHeightPercentage <1.0){

            //resize "window"
            wdg.resize(wdg.sz.x, (int)(wdg.sz.y * ZeeConfig.cattleRosterHeightPercentage));

            //reposition buttons
            int y = -1;
            for (Widget w: wdg.children()) {
                if(w.getClass().getSimpleName().contentEquals("Button")){
                    if(y==-1) { //calculate once
                        y = (int) (w.c.y * ZeeConfig.cattleRosterHeightPercentage) - (int)(w.sz.y*0.6);
                    }
                    w.c.y = y;
                }
            }
        }
    }
}



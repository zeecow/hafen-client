package haven;

import java.util.List;
import java.util.stream.Collectors;

/*
    Mid-click auto-equips items from belt/hands.
    Drinks from vessels: waterskin, bucket.
 */
public class ZeeClickItemManager extends ZeeThread{

    private final WItem wItem;
    String itemName;
    String leftHandItemName, rightHandItemName, itemSourceWindow;
    boolean cancelManager = false;
    public static Equipory equipory;
    static Inventory invBelt = null;
    public static long clickStartMs, clickEndMs, clickDiffMs;
    

    public ZeeClickItemManager(WItem wItem) {
        clickDiffMs = clickEndMs - clickStartMs;
        this.wItem = wItem;
        equipory = ZeeConfig.windowEquipment.getchild(Equipory.class);
        leftHandItemName = (equipory.leftHand==null ? "" : equipory.leftHand.item.getres().name);
        rightHandItemName = (equipory.rightHand==null ? "" : equipory.rightHand.item.getres().name);
        try{
            itemName = wItem.item.getres().name;//clicked item, started manager
            itemSourceWindow = wItem.getparent(Window.class).cap.text;//save source window name before pickup
        }catch (NullPointerException e){
            //error caused by midClicking again before task ending
            cancelManager = true;
        }
        //println(itemName +"  "+ wItem.c.div(33)+"  "+ZeeConfig.getCursorName());
    }

    @Override
    public void run() {

        if(cancelManager)
            return;

        try{

            //kill all, eat all, etc...
            if(wItem.ui.modctrl && isLongClick()){
                actOnAllInventoryItems();
                return;
            } else if (isLongClick() && isFishingItem()) {
                equipFishingItem();
                return;
            }

            //sort-transfer
            if(!itemSourceWindow.equalsIgnoreCase("Belt") && !itemSourceWindow.equalsIgnoreCase("Equipment")){
                if(transferWindowOpen()) { //avoid belt transfer?
                    if(isLongClick())
                        wItem.wdgmsg("transfer-sort", wItem.item, true);//ascending order
                    else
                        wItem.wdgmsg("transfer-sort", wItem.item, false);//descending order
                    return;
                }else {
                    //no transfer window open
                    if(itemSourceWindow.equalsIgnoreCase("Inventory") && isItemPlantable()){
                        //activate farming area cursor
                        itemAct(wItem,UI.MOD_SHIFT);
                    }
                }
            }

            //check for windows belt/equips ?
            if(ZeeConfig.getWindow("Belt")==null){
                ZeeConfig.gameUI.msg("no belt window");
                return;
            }
            if(ZeeConfig.getWindow("Equipment")==null){
                ZeeConfig.gameUI.msg("no equips window");
                return;
            }

            if(isItemDrinkingVessel()) {
                drinkFrom();
            }
            else if (isItemSack()) { // travellersack or bindle

                if(isSourceBeltWindow()) {//send to equipory
                    if(isLeftHandEmpty() || isRightHandEmpty()) {
                        pickUpItem();
                        equipEmptyHand();
                    }else if (!isItemSack(leftHandItemName)) {//avoid switching sack for sack
                        pickUpItem();
                        equipLeftOccupiedHand();
                        trySendItemToBelt();
                    }else if(!isItemSack(rightHandItemName)) {
                        pickUpItem();
                        equipRightOccupiedHand();
                        trySendItemToBelt();
                    }else { //both hands are sacks?
                        ZeeConfig.gameUI.msg("both hand sacks");
                    }

                    if(ZeeConfig.isPlayerHoldingItem()) {//equip was a switch or failed
                        ZeeConfig.gameUI.msg("couldn't switch sack");
                        trySendItemToBelt();
                    }
                }else if(isSourceEquipsWindow()){//send to belt
                    pickUpItem();
                    if(ZeeConfig.isPlayerHoldingItem()){ //unequip sack was successfull
                        if(!trySendItemToBelt())
                            println("belt full?");
                    }
                }

            }
            else if(isTwoHandedItem()) {//2 handed item

                if(isSourceBeltWindow()) {
                    if(!isLeftHandEmpty() && isTwoHandedItem(leftHandItemName)) {
                        //switch 2handed item for another 2handed item
                        pickUpItem();
                        equipLeftOccupiedHand();
                        trySendItemToBelt();
                    }else if(isLeftHandEmpty() || isRightHandEmpty()) {
                        //switch for 2handed item for 1handed equipped, or none equipped
                        pickUpItem();
                        if(!isLeftHandEmpty())
                            equipLeftOccupiedHand();
                        else if(!isRightHandEmpty())
                            equipRightOccupiedHand();
                        else
                            equipLeftEmptyHand();
                        trySendItemToBelt();
                    }else if(!isLeftHandEmpty() && !isRightHandEmpty()){
                        //switch 2handed item for 2 separate items
                        if (ZeeClickItemManager.getInvBelt().getNumberOfFreeSlots() > 0) {
                            unequipLeftItem();//unequip 1st item
                            if(trySendItemToBelt()){
                                pickUpItem();
                                equipRightOccupiedHand();//switch for 2nd item
                                trySendItemToBelt();
                            }
                        }
                    }
                }
                else if(isSourceEquipsWindow()) {
                    if (ZeeClickItemManager.getInvBelt().getNumberOfFreeSlots() > 0) {
                        //send to belt if possible
                        pickUpItem();
                        trySendItemToBelt();
                    }
                }

            }
            else{// 1handed item

                if(isSourceBeltWindow()) { // send to equipory
                    if(isLeftHandEmpty() || isRightHandEmpty()) {//1 item equipped
                        pickUpItem();
                        equipEmptyHand();
                    }else { // 2 hands occupied
                        if(isTwoHandedItem(getLeftHandName())) {
                            //switch 1handed for 2handed
                            pickUpItem();
                            equipLeftOccupiedHand();
                            trySendItemToBelt();
                        }if(isShield()) {
                            //avoid replacing 1handed swords
                            pickUpItem();
                            if (!isOneHandedSword(leftHandItemName)){
                                equipLeftOccupiedHand();
                                trySendItemToBelt();
                            }else if (!isOneHandedSword(rightHandItemName)){
                                equipRightOccupiedHand();
                                trySendItemToBelt();
                            }else
                                println("2 swords equipped? let user decide...");
                        }else if(isOneHandedSword()) {
                            //avoid replacing shields
                            pickUpItem();
                            if (!isShield(leftHandItemName)){
                                equipLeftOccupiedHand();
                                trySendItemToBelt();
                            }else if (!isShield(rightHandItemName)){
                                equipRightOccupiedHand();
                                trySendItemToBelt();
                            }else//2 shields?
                                println("2 shields equipped? let user decide...");
                        }else if(!isItemSack(leftHandItemName)) {
                            //switch 1handed item for left hand
                            pickUpItem();
                            equipLeftOccupiedHand();
                            trySendItemToBelt();
                        }else if(!isItemSack(rightHandItemName)) {
                            //switch 1handed item for right hand
                            pickUpItem();
                            equipRightOccupiedHand();
                            trySendItemToBelt();
                        }else{
                            // switch 1handed item for one of both sacks equipped
                            pickUpItem();
                            equipLeftOccupiedHand();
                            if (!isItemSack(getHoldingItemName())){
                                //couldn't switch, try other sack
                                equipRightOccupiedHand();
                            }
                            trySendItemToBelt();
                        }
                    }

                }else if(isSourceEquipsWindow()){//send to belt
                    pickUpItem();
                    if(!trySendItemToBelt()) {
                        ZeeConfig.gameUI.msg("Belt is full");
                    }
                }

            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void equiporyItemAct(String itemNameContains){
        getEquipory().children(WItem.class).forEach(witem -> {
            if (witem.item.res.get().name.contains(itemNameContains)) {
                witem.item.wdgmsg("itemact",0);
            }
        });
    }

    private void equipFishingItem() {
        //   gfx/invobjs/small/primrod-h
        //   gfx/invobjs/small/bushpole-l
        /*
            haven.MenuGrid@35e8b5d9 ; act ; [fish, 0]
            haven.GameUI@3692df36 ; focus ; [8]
            haven.MapView@6c6f1998 ; click ; [(711, 519), (1067680, 1019086), 1, 0, 0, 156122677, (1068567, 1021959), 0, -1]
         */
        try {
            if (itemName.contains("lure-")){
                // equip lure on primrod
                if(getLeftHandName().contains("/primrod") || getRightHandName().contains("/primrod")){
                    if(pickUpItem()){
                        equiporyItemAct("/primrod");
                        Thread.sleep(PING_MS / 2);
                        wItem.getparent(Inventory.class).wdgmsg("drop", wItem.c.div(33));
                    }
                } else {
                    ZeeConfig.gameUI.msg("no fish rod equipped");
                }
            } else {
                //equip hook or line
                String rodName = "";
                if(getLeftHandName().contains("/primrod") || getRightHandName().contains("/primrod")) {
                    rodName = "/primrod";
                } else if(getLeftHandName().contains("/bushpole") || getRightHandName().contains("/bushpole")){
                    rodName = "/bushpole";
                } else {
                    ZeeConfig.gameUI.msg("no fish pole equipped");
                    return;
                }
                if(pickUpItem()){
                    equiporyItemAct(rodName);
                    Thread.sleep(PING_MS / 2);
                    wItem.getparent(Inventory.class).wdgmsg("drop", wItem.c.div(33));
                }
            }

            // click fishing spot again
            Thread.sleep(PING_MS / 2);
            ZeeConfig.gameUI.map.wdgmsg("click", ZeeConfig.lastMapViewClickArgs);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isFishingItem() {
        String[] items = {"fline-","hook-","lure-","chitinhook"};
        for (int i = 0; i < items.length; i++) {
            if (itemName.contains(items[i])){
                return true;
            }
        }
        return false;
    }

    public String getHoldingItemName() {
        if(ZeeConfig.gameUI.vhand==null)
            return "";
        try {
            return ZeeConfig.gameUI.vhand.item.getres().name;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private boolean actOnAllInventoryItems() {

        // kill all inventory cocoons
        if(itemName.endsWith("silkcocoon") || itemName.endsWith("chrysalis")){
            Inventory inv = wItem.getparent(Inventory.class);
            List<WItem> items = inv.children(WItem.class).stream()
                .filter(wItem1 -> wItem1.item.getres().name.endsWith("silkcocoon") || wItem1.item.getres().name.endsWith("chrysalis"))
                .collect(Collectors.toList());
            clickAllItemsPetal(items,"Kill");
            ZeeConfig.gameUI.msg(items.size()+" cocoons clicked");
            return true;
        }

        //eat all table similar items
        else if(ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_EAT)){
            Inventory inv = wItem.getparent(Inventory.class);
            List<WItem> items = inv.children(WItem.class).stream()
                .filter(wItem1 -> wItem1.item.getres().name.equals(itemName))
                .collect(Collectors.toList());
            takeAllInvItems(inv, items);
            ZeeConfig.gameUI.msg(items.size()+" noms");
            return true;
        }

        return false;
    }

    public static void takeAllInvItems(Inventory inv, List<WItem> items) {
        try {
            for (WItem w : items) {
                w.item.wdgmsg("take", w.getInvSlotCoord());
                Thread.sleep(PING_MS);
            }
        }catch (Exception e){
            e.printStackTrace();
            ZeeConfig.gameUI.msg("takeAllInvItems: "+e.getMessage());
        }
    }

    public static boolean clickAllItemsPetal(List<WItem> items, String petalName) {
        ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"clicking "+items.size()+" items",0,255,255,255,10);
        for (WItem w: items) {
            //ZeeClickGobManager.scheduleClickPetalOnce(petalName);
            try {
                itemAct(w);
                if(waitFlowerMenu()){
                    choosePetal(getFlowerMenu(),petalName);
                }else{
                    println("clickAllItemsPetal > no flower menu for "+petalName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ZeeClickGobManager.resetClickPetal();
                ZeeConfig.gameUI.msg("clickAllItemsPetal: "+e.getMessage());
                ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
                return false;
            }
        }
        ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
        return true;
    }

    public static boolean clickAllItemsPetalOld(List<WItem> items, String petalName) {
        for (WItem w: items) {
            ZeeClickGobManager.scheduleClickPetalOnce(petalName);
            try {
                itemAct(w);
                int max = (int) TIMEOUT_MS;
                while(max>0 && ZeeClickGobManager.clickPetal){//wait FlowerMenu end and set clickPetal to false
                    max -= SLEEP_MS;
                    Thread.sleep(SLEEP_MS);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ZeeClickGobManager.resetClickPetal();
                ZeeConfig.gameUI.msg("clickAllItemsPetal: "+e.getMessage());
                return false;
            }
        }
        return true;
    }

    public static void itemAct(WItem item){
        itemAct(item, item.ui.modflags());
    }

    public static void itemAct(WItem item, int modflags){
        gItemAct(item.item, modflags);
    }

    public static void gItemAct(GItem item, int modflags){
        item.wdgmsg("iact", item.c.div(2), modflags);
    }

    private boolean isLongClick() {
        return clickDiffMs > LONG_CLICK_MS;
    }

    private boolean transferWindowOpen() {
        String windowsNames = getWindowsNames();
        String[] containers = (
            "Knarr,Snekkja,Wagon,Cupboard,Chest,Table,Crate,Saddlebags,Basket,Box,"
            +"Furnace,Smelter,Desk,Trunk,Shed,Coffer,Packrack,Strongbox,Stockpile,"
            +"Tub,Compost Bin,Extraction Press,Rack,Herbalist Table,Frame,"
            +"Chicken Coop,Rabbit Hutch,Archery Target,Creel,Oven,Steel crucible,"
            +"Cauldron,Pane mold,Kiln,Old Trunk,Smoke shed"
        ).split(",");
        for (String contName: containers) {
            if (windowsNames.contains(contName))
                return true;
        }
        return false;
    }

    private String getWindowsNames() {
        return ZeeConfig.gameUI.children(Window.class).stream().map(window -> window.cap.text).collect(Collectors.joining(","));
    }

    private void drinkFrom() {
        ZeeClickGobManager.scheduleClickPetalOnce("Drink");
        itemAct(wItem);
    }

    private boolean isItemDrinkingVessel() {
        return isItemDrinkingVessel(itemName);
    }
    private boolean isItemDrinkingVessel(String name) {
        String[] items = {"waterskin","waterflask","bucket"};
        for (int i = 0; i < items.length; i++) {
            if (name.contains(items[i])){
                return true;
            }
        }
        return false;
    }

    private boolean isOneHandedSword() {
        return isOneHandedSword(itemName);
    }
    private boolean isOneHandedSword(String name) {
        String[] items = {"fyrdsword","hirdsword","bronzesword"};
        for (int i = 0; i < items.length; i++) {
            if (name.contains(items[i])){
                return true;
            }
        }
        return false;
    }

    private boolean isOneHandedWeapon() {
        return isOneHandedWeapon(itemName);
    }
    private boolean isOneHandedWeapon(String name) {
        String[] items = {"fyrdsword","hirdsword","bronzesword","axe-m","woodsmansaxe","stoneaxe","butcherscleaver","sling"};
        for (int i = 0; i < items.length; i++) {
            if (name.contains(items[i])){
                return true;
            }
        }
        return false;
    }

    private boolean isShield() {
        return isShield(itemName);
    }
    private boolean isShield(String name) {
        return name.contains("roundshield");
    }

    public static boolean trySendItemToBelt() {
        if(!ZeeConfig.isPlayerHoldingItem())
            return false;
        try{
            List<Coord> freeSlots = ZeeClickItemManager.getInvBelt().getFreeSlots();
            if (freeSlots.size()==0)
                return false;//belt full
            Coord c = freeSlots.get(0);
            ZeeClickItemManager.getInvBelt().wdgmsg("drop", c);
            return waitNotHoldingItem();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean isSourceBeltWindow() {
        return (itemSourceWindow.equalsIgnoreCase("Belt"));
    }

    private boolean isSourceEquipsWindow() {
        return (itemSourceWindow.equalsIgnoreCase("Equipment"));
    }

    /*
        equip occupied hand and wait
     */
    private void equipLeftOccupiedHand() {
        equipory.wdgmsg("drop", 6);
        waitHoldingItem();
    }
    private void equipRightOccupiedHand() {
        equipory.wdgmsg("drop", 7);
        waitHoldingItem();
    }

    /*
        equip empty hand and wait
     */
    private void equipLeftEmptyHand() {
        equipory.wdgmsg("drop", 6);
        waitNotHoldingItem();
    }
    private void equipRightEmptyHand() {
        equipory.wdgmsg("drop", 7);
        waitNotHoldingItem();
    }

    private boolean equipEmptyHand() {
        if(isLeftHandEmpty())
            equipLeftEmptyHand();
        else if(isRightHandEmpty())
            equipRightEmptyHand();
        return waitNotHoldingItem();
    }

    private boolean isLeftHandEmpty() {
        return (equipory.leftHand==null);
    }

    private boolean isRightHandEmpty() {
        return (equipory.rightHand==null);
    }


    private boolean pickUpItem() {
        return pickUpItem(wItem);
    }
    public static boolean pickUpItem(WItem wItem) {
        wItem.item.wdgmsg("take", new Coord(wItem.sz.x / 2, wItem.sz.y / 2));
        return waitHoldingItem();
    }

    public static boolean unequipLeftItem() {
        if(equipory.leftHand==null)
            return true;
        equipory.leftHand.item.wdgmsg("take", new Coord(equipory.leftHand.sz.x/2, equipory.leftHand.sz.y/2));
        return waitHoldingItem();
    }

    public static boolean unequipRightItem() {
        if(equipory.rightHand==null)
            return true;
        equipory.rightHand.item.wdgmsg("take", new Coord(equipory.rightHand.sz.x/2, equipory.rightHand.sz.y/2));
        return waitHoldingItem();
    }

    private boolean isItemSack() {
        return isItemSack(itemName);
    }

    private boolean isItemSack(String name) {
        return name.endsWith("travellerssack") || name.endsWith("bindle");
    }

    private boolean isItemPlantable() {
        //println("is item Plantable "+itemName);
        String endList = "seed-barley,seed-carrot,carrot,seed-cucumber,seed-flax,"
            +"seed-grape,seed-hemp,seed-leek,leek,seed-lettuce,seed-millet,"
            +"seed-pipeweed,seed-poppy,seed-pumpkin,seed-wheat,seed-turnip,turnip,"
            +"seed-wheat,seed-barley,beetroot,yellowonion,redonion";
        String name = itemName.replace("gfx/invobjs/","");
        return endList.contains(name);
    }

    private boolean isTwoHandedItem() {
        return isTwoHandedItem(itemName);
    }
    public static boolean isTwoHandedItem(String name) {
        String[] items = {"scythe","pickaxe","shovel","b12axe",
                "boarspear","cutblade","sledgehammer",
                "huntersbow","rangersbow","dowsingrod"};
        for (int i = 0; i < items.length; i++) {
            if (name.contains(items[i])){
                return true;
            }
        }
        return false;
    }

    public static Inventory getInvBelt() {
        if (invBelt==null) {
            Window w = ZeeConfig.getWindow("Belt");
            if(w!=null)
                invBelt = w.getchild(Inventory.class);
        }
        return  invBelt;
    }

    public static boolean pickupBeltItem(String name) {
        try {
            WItem witem = getInvBelt().getWItemsByName(name).get(0);
            return pickUpItem(witem);
        }catch (Exception e){
            return false;
        }
    }

    public static boolean pickupHandItem(String name) {
        try {
            if(getLeftHandName().contains(name))
                return pickUpItem(getLeftHand());
            else if(getRightHandName().contains(name))
                return pickUpItem(getRightHand());
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static WItem getBeltWItem(String name) {
        try {
            WItem witem = getInvBelt().getWItemsByName(name).get(0);
            return witem;
        }catch (Exception e){
            return null;
        }
    }

    public static boolean isItemEquipped(String name){
        try {
            Equipory eq = ZeeConfig.windowEquipment.getchild(Equipory.class);
            return eq.leftHand.item.getres().name.contains(name)
                    || eq.rightHand.item.getres().name.contains(name);
        }catch (Exception e){
            return false;
        }
    }

    public static Equipory getEquipory(){
        if (equipory==null)
            equipory = ZeeConfig.windowEquipment.getchild(Equipory.class);
        return equipory;
    }

    public static WItem getLeftHand() {
        return getEquipory().leftHand;
    }
    public static WItem getRightHand() {
        return getEquipory().rightHand;
    }

    public static String getLeftHandName() {
        if(getEquipory().leftHand==null)
            return "";
        else
            return getEquipory().leftHand.item.getres().name;
    }
    public static String getRightHandName() {
        if(getEquipory().rightHand==null)
            return "";
        else
            return getEquipory().rightHand.item.getres().name;
    }

    public static void equipItem(String name) {
        if(ZeeClickItemManager.isItemEquipped(name))
            return;
        WItem item = ZeeClickItemManager.getBeltWItem(name);
        new ZeeClickItemManager(item).start();//use equipManager logic
    }
}

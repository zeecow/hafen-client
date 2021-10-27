package haven;

import java.util.List;
import java.util.stream.Collectors;

/*
    Mid-click auto-equips items from belt/hands.
    Drinks from vessels: waterskin, bucket.
 */
public class ZeeClickItemManager extends Thread{

    static long SLEEP_MS = 77;
    static long PING_MS = 200;
    static long TIMEOUT_MS = 2000;
    private final WItem wItem;
    String itemName;
    String leftHandItemName, rightHandItemName, itemSourceWindow;
    boolean cancelManager = false;
    Equipory equipory;
    static Inventory invBelt = null;
    public static long clickStartMs, clickEndMs, clickDiffMs;
    public static boolean clickPetal = false;
    public static String clickPetalName = "";



    public ZeeClickItemManager(WItem wItem) {
        clickDiffMs = clickEndMs - clickStartMs;
        this.wItem = wItem;
        itemName = wItem.item.getres().name;//clicked item, started manager
        equipory = ZeeConfig.windowEquipment.getchild(Equipory.class);
        leftHandItemName = (equipory.leftHand==null ? "" : equipory.leftHand.item.getres().name);
        rightHandItemName = (equipory.rightHand==null ? "" : equipory.rightHand.item.getres().name);
        try{
            itemSourceWindow = wItem.getparent(Window.class).cap.text;//save source window name before pickup
        }catch (NullPointerException e){
            //error caused by midClicking again before task ending
            cancelManager = true;
        }
        //System.out.println(itemName +" , "+ leftHandItemName +" , "+ rightHandItemName);
        //System.out.println(clickDiffMs+" > "+itemName);
    }

    @Override
    public void run() {

        if(cancelManager)
            return;

        try{

            //kill all, eat all, etc...
            if(actOnAllInventoryItems()){
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
                }
            }

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

                pickUpItem();
                if(isSourceBeltWindow()) {//send to equipory
                    if(isLeftHandEmpty() || isRightHandEmpty())
                        equipEmptyHand();
                    else if (!isItemSack(leftHandItemName))//avoid switching sack for sack
                        equipLeftHand();
                    else if(!isItemSack(rightHandItemName))
                        equipRightHand();
                    else //both hands are sacks?
                        equipAnyHand(); //drop at -1, server decide

                    if(isHoldingItem())//equip was a switch or failed
                        trySendItemToBelt();
                }else if(isSourceEquipsWindow()){//send to belt
                    if(isHoldingItem()){ //unequip sack was successfull
                        if(!trySendItemToBelt())
                            equipAnyHand();//belt full?
                    }
                }

            }
            else if(isTwoHandedItem()) {//2 handed item

                if(isSourceBeltWindow()) {
                    if(!isLeftHandEmpty() && isTwoHandedItem(leftHandItemName)) {
                        //switch 2handed item for another 2handed item
                        pickUpItem();
                        equipAnyHand();
                        trySendItemToBelt();
                    }else if(isLeftHandEmpty() || isRightHandEmpty()) {
                        //switch 2handed item for regular item
                        pickUpItem();
                        equipAnyHand();
                        trySendItemToBelt();
                    }else if(!isLeftHandEmpty() && !isRightHandEmpty()){
                        //switch 2handed item for 2 separate items
                        if (ZeeClickItemManager.getInvBelt().getNumberOfFreeSlots() > 0) {
                            unequipLeftItem();//unequip 1st item
                            if(trySendItemToBelt()){
                                //switch for remaining item
                                pickUpItem();
                                equipAnyHand();
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
                    }else { // 2 items equipped
                        if(isShield()) {
                            //avoid replacing 1handed swords
                            pickUpItem();
                            if (!isOneHandedSword(leftHandItemName)){
                                equipLeftHand();
                            }else if (!isOneHandedSword(rightHandItemName)){
                                equipRightHand();
                            }else
                                equipAnyHand();
                            trySendItemToBelt();
                        }else if(isOneHandedSword()) {
                            //avoid replacing shields
                            pickUpItem();
                            if (!isShield(leftHandItemName)){
                                equipLeftHand();
                            }else if (!isShield(rightHandItemName)){
                                equipRightHand();
                            }else//2 shields?
                                equipAnyHand();
                            trySendItemToBelt();
                        }else if(!isItemSack(leftHandItemName)) {
                            //switch item for left hand
                            pickUpItem();
                            equipLeftHand();
                            trySendItemToBelt();
                        }else if(!isItemSack(rightHandItemName)) {
                            //switch item for right hand
                            pickUpItem();
                            equipRightHand();
                            trySendItemToBelt();
                        }else{ // both hands are sacks
                            unequipLeftItem();
                            if (isHoldingItem()) {//unequip left sack successful
                                if(trySendItemToBelt()) {
                                    pickUpItem();
                                    equipEmptyHand();
                                }else
                                    ZeeConfig.gameUI.msg("Belt is full");
                            }else{//left sack cannot unequip
                                unequipRightItem();
                                if (isHoldingItem()){//unequip right sack successful
                                    if(trySendItemToBelt()) {
                                        pickUpItem();
                                        equipEmptyHand();
                                    }else
                                        ZeeConfig.gameUI.msg("Belt is full");
                                }
                            }
                        }
                    }

                }else if(isSourceEquipsWindow()){//send to belt
                    pickUpItem();
                    if(!trySendItemToBelt()) {
                        equipAnyHand();//belt full?
                        ZeeConfig.gameUI.msg("Belt is full");
                    }
                }

            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean actOnAllInventoryItems() {

        if(!wItem.ui.modctrl || !isLongClick())// require Ctrl + LongClick
            return false;

        // kill all inventory cocoons
        if(itemName.endsWith("silkcocoon") || itemName.endsWith("chrysalis")){
            Inventory inv = wItem.getparent(Inventory.class);
            List<WItem> items = inv.children(WItem.class).stream()
                    .filter(wItem1 -> wItem1.item.getres().name.endsWith("silkcocoon") || wItem1.item.getres().name.endsWith("chrysalis"))
                    .collect(Collectors.toList());
            for (WItem w: items) {
                scheduleClickPetal("Kill");
                try {
                    clickWItem(w);
                    int max = (int) TIMEOUT_MS;
                    while(max>0 && clickPetal){//wait FlowerMenu end and set clickPetal to false
                        max -= SLEEP_MS;
                        Thread.sleep(SLEEP_MS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    resetClickPetal();
                    ZeeConfig.gameUI.msg("Kill cocoons: "+e.getMessage());
                    return false;
                }
            }
            ZeeConfig.gameUI.msg(items.size()+" cocoons clicked");
            return true;
        }

        return false;
    }

    public static void clickWItem(WItem item){
        item.item.wdgmsg("iact", item.item.c.div(2), item.ui.modflags());
    }

    // set flags for clickWItem and ZeeClickGobManager.gobClick
    public static void scheduleClickPetal(String name) {
        clickPetal = true;
        clickPetalName = name;
    }
    public static void resetClickPetal() {
        clickPetal = false;
        clickPetalName = "";
    }

    private boolean isLongClick() {
        return clickDiffMs > 250;
    }

    private boolean transferWindowOpen() {
        String windowsNames = getWindowsNames();
        String[] containers = (
            "Knarr,Snekkja,Wagon,Cupboard,Chest,Table,Crate,Saddlebags,Basket,Box,"
            +"Furnace,Smelter,Desk,Trunk,Shed,Coffer,Packrack,Strongbox,Stockpile,"
            +"Tub,Compost Bin,Extraction Press,Cheese Rack,Herbalist Table,Frame,"
            +"Chicken Coop,Rabbit Hutch,Archery Target,Creel,Oven,Steel crucible"
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
        scheduleClickPetal("Drink");
        clickWItem(wItem);
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

    private void equipAnyHand() throws InterruptedException {
        equipory.wdgmsg("drop",-1);//server decide
        Thread.sleep(SLEEP_MS);
    }

    public static boolean trySendItemToBelt() {
        try{
            List<Coord> freeSlots = ZeeClickItemManager.getInvBelt().getFreeSlots();
            if (freeSlots.size()==0)
                return false;//belt full
            Coord c = freeSlots.get(0);
            ZeeClickItemManager.getInvBelt().wdgmsg("drop", c);
            return waitFreeHand();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

        public static boolean waitFreeHand() {
        int max = (int) TIMEOUT_MS;
        while(max>0 && ZeeConfig.gameUI.vhand!=null) {
            max -= SLEEP_MS;
            try { Thread.sleep(SLEEP_MS); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        if(max<=0)
            return false;
        return true;
    }

    public static boolean waitOccupiedHand() {
        int max = (int) TIMEOUT_MS;
        while(max>0 && ZeeConfig.gameUI.vhand==null) {
            max -= SLEEP_MS;
            try { Thread.sleep(SLEEP_MS); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        if(max<=0)
            return false;
        return true;
    }

    public static boolean isHoldingItem() {
        return (ZeeConfig.gameUI.vhand != null);
    }

    private boolean isSourceBeltWindow() {
        return (itemSourceWindow.equalsIgnoreCase("Belt"));
    }

    private boolean isSourceEquipsWindow() {
        return (itemSourceWindow.equalsIgnoreCase("Equipment"));
    }

    private void equipLeftHand() {
        //waitOccupiedHand();
        equipory.wdgmsg("drop", 6);
    }

    private void equipRightHand() {
        //waitOccupiedHand();
        equipory.wdgmsg("drop", 7);
    }

    private boolean equipEmptyHand() {
        if(isLeftHandEmpty())
            equipLeftHand();
        else if(isRightHandEmpty())
            equipRightHand();
        return waitFreeHand();
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
        return waitOccupiedHand();
    }

    private boolean unequipLeftItem() {
        equipory.leftHand.item.wdgmsg("take", new Coord(equipory.leftHand.sz.x/2, equipory.leftHand.sz.y/2));
        return waitOccupiedHand();
    }

    private boolean unequipRightItem() {
        equipory.rightHand.item.wdgmsg("take", new Coord(equipory.rightHand.sz.x/2, equipory.rightHand.sz.y/2));
        return waitOccupiedHand();
    }

    private boolean isItemSack() {
        return isItemSack(itemName);
    }

    private boolean isItemSack(String name) {
        return name.endsWith("travellerssack") || name.endsWith("bindle");
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

    public static void equipItem(String name) {
        if(ZeeClickItemManager.isItemEquipped(name))
            return;
        WItem item = ZeeClickItemManager.getBeltWItem(name);
        new ZeeClickItemManager(item).start();//use equipManager logic
    }
}

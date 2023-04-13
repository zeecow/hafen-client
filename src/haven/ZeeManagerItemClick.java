package haven;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
    Mid-click auto-equips items from belt/hands.
    Drinks from vessels: waterskin, bucket.
 */
public class ZeeManagerItemClick extends ZeeThread{

    private final WItem wItem;
    private Coord coord;
    String itemName;
    String itemBasename;
    String leftHandItemName, rightHandItemName;
    boolean cancelManagerBecauseException = false;
    public static Equipory equipory;
    static Inventory invBelt = null;
    public static long clickStartMs, clickEndMs, clickDiffMs;

    public ZeeManagerItemClick(WItem wItem, Coord c) {
        clickDiffMs = clickEndMs - clickStartMs;
        this.wItem = wItem;
        this.coord = c;
        init(wItem);
    }

    public ZeeManagerItemClick(WItem wItem) {
        clickDiffMs = clickEndMs - clickStartMs;
        this.wItem = wItem;
        init(wItem);
    }

    private void init(WItem wItem) {
        try{
            equipory = ZeeConfig.windowEquipment.getchild(Equipory.class);
            leftHandItemName = (getEquipory().leftHand==null ? "" : getEquipory().leftHand.item.getres().name);
            rightHandItemName = (getEquipory().rightHand==null ? "" : getEquipory().rightHand.item.getres().name);
            itemName = wItem.item.getres().name;//clicked item, started manager
            itemBasename = wItem.item.getres().basename();
        }catch (Exception e){
            cancelManagerBecauseException = true;
            println("click manager init exception "+e.getCause());
        }
        //println(itemName +"  "+ getWItemCoord(wItem));//+"  "+ZeeConfig.getCursorName());
    }

    @Override
    public void run() {

        if(cancelManagerBecauseException)
            return;

        try{

            // stack items
            if (ZeeConfig.isPlayerHoldingItem()){
                if (itemName.contentEquals(getHoldingItem().item.getres().name)){
                    // create multiple stacks
                    if (isLongClick())
                        gItemAct(wItem.item,3);
                    // create one stack
                    else
                        gItemAct(wItem.item,1);
                    return;
                }
            }

            // item context menu
            if(showItemFlowerMenu()){
                return;
            }

            // fishing
            if (isLongClick() && isFishingItem()) {
                equipFishingItem();
                return;
            }


            // undo stack if no transfer available
            if( isStackItemPlaceholder(wItem.item)  &&  !isStackTransferable(wItem.item)){
                //undo one stack
                if (!isLongClick()){
                    undoStack(wItem.item);
                    return;
                }
                //undo multiple stacks
                else{
                    undoMultipleStacks(wItem.item);
                    return;
                }
            }
            // if not hand item, do items special transfers
            else if( isTransferWindowOpened() ){
                if(!isItemWindowName("Belt")) {
                    // long click sort transfer asc
                    if (isLongClick())
                        wItem.wdgmsg("transfer-sort", wItem.item, true);
                    //short clicks...
                    // ...transfer same quality items
                    else if (ZeeConfig.windowShortMidclickTransferMode.contentEquals("ql"))
                        wItem.item.wdgmsg("transfer-ql", wItem.item, false);
                    // ...transfer single item
                    else if (ZeeConfig.windowShortMidclickTransferMode.contentEquals("one"))
                        wItem.item.wdgmsg("transfer", Coord.z);
                    // ...sort transfer asc
                    else if (ZeeConfig.windowShortMidclickTransferMode.contentEquals("asc"))
                        wItem.wdgmsg("transfer-sort", wItem.item, true);
                    // ...sort transfer des
                    else
                        wItem.wdgmsg("transfer-sort", wItem.item, false);
                    return;
                }
            }
            //activate farming area cursor
            else if(isItemWindowName("Inventory") && isItemPlantable())
            {
                itemActCoord(wItem,UI.MOD_SHIFT);
                return;
            }
            else if (isItemHandEquipable() && !isItemWindowName("Inventory") && !isItemWindowName("Belt")) {
                println("itemManager > only Belt and Inventory allowed (for now?)");
                return;
            }
            else if(isItemDrinkingVessel()) {
                drinkFrom();
                return;
            }
            else if (!isItemHandEquipable()) {
                // cancel manager and do nothing
                return;
            }


            //check for windows belt/equips ?
            if(ZeeConfig.getWindow("Belt")==null){
                ZeeConfig.gameUI.error("no belt window");
                return;
            }
            if(ZeeConfig.getWindow("Equipment")==null){
                ZeeConfig.gameUI.error("no equips window");
                return;
            }


            if (isItemSack()) { // travellersack or bindle

                if(!isItemWindowEquips()) {//send to equipory

                    if(isLongClick()) {
                        equipTwoSacks();
                    } else if(isLeftHandEmpty() || isRightHandEmpty()) {
                        pickUpItem();
                        equipEmptyHand();
                    }else if (!isItemSack(leftHandItemName)) {//avoid switching sack for sack
                        pickUpItem();
                        equipLeftOccupiedHand();
                        dropHoldingItemToBeltOrInv();
                    }else if(!isItemSack(rightHandItemName)) {
                        pickUpItem();
                        equipRightOccupiedHand();
                        dropHoldingItemToBeltOrInv();
                    }else { //both hands are sacks?
                        ZeeConfig.gameUI.error("both hand sacks");
                    }

                    if(ZeeConfig.isPlayerHoldingItem()) {//equip was a switch or failed
                        ZeeConfig.gameUI.error("couldn't switch sack");
                        dropHoldingItemToBeltOrInv();
                    }
                }
                else if(isItemWindowEquips()){//send to belt
                    pickUpItem();
                    if(ZeeConfig.isPlayerHoldingItem()){ //unequip sack was successfull
                        if(!dropHoldingItemToBeltOrInv())
                            println("drop inv full?");
                    }
                }

            }
            else if(isTwoHandedItem()) {//2 handed item
                if(!isItemWindowEquips()) {
                    if(!isLeftHandEmpty() && isTwoHandedItem(leftHandItemName)) {
                        //switch 2handed item for another 2handed item
                        pickUpItem();
                        equipLeftOccupiedHand();
                        dropHoldingItemToBeltOrInv();
                    }else if(isLeftHandEmpty() || isRightHandEmpty()) {
                        //switch for 2handed item for 1handed equipped, or none equipped
                        pickUpItem();
                        if(!isLeftHandEmpty())
                            equipLeftOccupiedHand();
                        else if(!isRightHandEmpty())
                            equipRightOccupiedHand();
                        else
                            equipLeftEmptyHand();
                        dropHoldingItemToBeltOrInv();
                    }else if(!isLeftHandEmpty() && !isRightHandEmpty()){
                        //switch 2handed item for 2 separate items
                        if (ZeeManagerItemClick.getInvBelt().getNumberOfFreeSlots() > 0) {
                            unequipLeftItem();//unequip 1st item
                            if(dropHoldingItemToBeltOrInv()){
                                pickUpItem();
                                equipRightOccupiedHand();//switch for 2nd item
                                dropHoldingItemToBeltOrInv();
                            }
                        }
                    }
                }
                else if(isItemWindowEquips()) {
                    if (ZeeManagerItemClick.getInvBelt().getNumberOfFreeSlots() > 0) {
                        //send to belt if possible
                        pickUpItem();
                        dropHoldingItemToBeltOrInv();
                    }
                }

            }
            else if (isItemAxeChopTree()) { // try switch axe for axe (1handed)

                if (isItemAxeChopTree(leftHandItemName) && isItemAxeChopTree(rightHandItemName)) {
                    // unequip 1 of 2 axes, before next steps
                    if (unequipLeftItem()) {
                        if(!dropHoldingItemToBeltOrInv()) {
                            equipEmptyHand(); // fall back
                        }
                    }
                }
                pickUpItem();
                if (isItemSack(leftHandItemName) && !isItemSack(rightHandItemName)) {
                    equipRightOccupiedHand();
                }else if (!isItemSack(leftHandItemName) && isItemSack(rightHandItemName)) {
                    equipLeftOccupiedHand();
                }else if (isItemAxeChopTree(getLeftHandName())) {
                    equipLeftOccupiedHand(); //switch left hand axe
                }else if (isItemAxeChopTree(getRightHandName())) {
                    equipRightOccupiedHand(); // switch right hand axe
                }else if (isLeftHandEmpty() || isRightHandEmpty()) {
                    equipEmptyHand();
                }else if (getLeftHandName().contains("/bucket")){
                    equipRightOccupiedHand();
                }else if (getRightHandName().contains("/bucket")){
                    equipLeftOccupiedHand();
                }else {
                    equipLeftOccupiedHand();//all hands occupied, equip left
                }
                dropHoldingItemToBeltOrInv();
            }
            else{// 1handed item

                if(!isItemWindowEquips()) { // send to equipory
                    if(isLeftHandEmpty() || isRightHandEmpty()) {//1 item equipped
                        pickUpItem();
                        equipEmptyHand();
                    }
                    else { // 2 hands occupied
                        if(isTwoHandedItem(getLeftHandName())) {
                            //switch 1handed for 2handed
                            pickUpItem();
                            equipLeftOccupiedHand();
                            dropHoldingItemToBeltOrInv();
                        }else if (leftHandItemName.contains("bucket")){
                            pickUpItem();
                            equipRightOccupiedHand();
                            dropHoldingItemToBeltOrInv();
                        }else if (rightHandItemName.contains("bucket")){
                            pickUpItem();
                            equipLeftOccupiedHand();
                            dropHoldingItemToBeltOrInv();
                        }else if(isShield()) {
                            //avoid replacing 1handed swords
                            pickUpItem();
                            if (!isOneHandedSword(leftHandItemName)){
                                equipLeftOccupiedHand();
                                dropHoldingItemToBeltOrInv();
                            }else if (!isOneHandedSword(rightHandItemName)){
                                equipRightOccupiedHand();
                                dropHoldingItemToBeltOrInv();
                            }else
                                println("2 swords equipped? let user decide...");
                        }else if(isOneHandedSword()) {
                            //avoid replacing shields
                            pickUpItem();
                            if (!isShield(leftHandItemName)){
                                equipLeftOccupiedHand();
                                dropHoldingItemToBeltOrInv();
                            }else if (!isShield(rightHandItemName)){
                                equipRightOccupiedHand();
                                dropHoldingItemToBeltOrInv();
                            }else//2 shields?
                                println("2 shields equipped? let user decide...");
                        }
                        else if(!isItemSack(leftHandItemName)) {
                            //switch 1handed item for left hand
                            pickUpItem();
                            equipLeftOccupiedHand();
                            dropHoldingItemToBeltOrInv();
                        }else if(!isItemSack(rightHandItemName)) {
                            //switch 1handed item for right hand
                            pickUpItem();
                            equipRightOccupiedHand();
                            dropHoldingItemToBeltOrInv();
                        }else{
                            // switch 1handed item for one of both sacks equipped
                            pickUpItem();
                            equipLeftOccupiedHand();
                            if (!isItemSack(getHoldingItemName())){
                                //couldn't switch, try other sack
                                equipRightOccupiedHand();
                            }
                            dropHoldingItemToBeltOrInv();
                        }
                    }

                }
                else if(isItemWindowEquips()){//send to belt
                    pickUpItem();
                    if(!dropHoldingItemToBeltOrInv()) {
                        ZeeConfig.gameUI.error("Belt is full");
                    }
                }

            }

        }catch (Exception e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }
    }


    private static void equipTwoSacks(WItem sack){
        if (sack!=null) {
            pickUpItem(sack);
            if (isLeftHandEmpty()) {
                equipLeftEmptyHand();
            } else {
                equipLeftOccupiedHand();
                if (!dropHoldingItemToBeltOrInv()) {
                    println("equipTwoSacks() > couldnt switch left item");
                    return;
                }
            }
            sack = getSackFromBelt();
            if (sack != null) {
                pickUpItem(sack);
                if (isRightHandEmpty()) {
                    equipRightEmptyHand();
                } else {
                    equipRightOccupiedHand();
                    if (!dropHoldingItemToBeltOrInv()) {
                        println("equipTwoSacks() > couldnt switch right item");
                    }
                }
            }
        }
    }

    public static void equipTwoSacks() {
        WItem sack = getSackFromBelt();
        if (sack!=null)
            equipTwoSacks(sack);
    }

    public static void equiporyItemAct(String itemNameContains){
        getEquipory().children(WItem.class).forEach(witem -> {
            if (witem.item.res.get().name.contains(itemNameContains)) {
                witem.item.wdgmsg("itemact",0);
            }
        });
    }

    public static void equipAxeChopTree() {
        /*
            gfx/invobjs/woodsmansaxe
            gfx/invobjs/axe-m
            gfx/invobjs/butcherscleaver
            gfx/invobjs/tinkersthrowingaxe
            gfx/invobjs/stoneaxe
         */
        if (isItemInHandSlot("woodsmansaxe"))
            return;
        WItem axe = getBeltWItem("woodsmansaxe");
        if (axe!=null){
            equipBeltItem("woodsmansaxe");
            waitItemInHand("woodsmansaxe");
        }else{
            if (isItemInHandSlot("axe-m"))
                return;
            axe = getBeltWItem("axe-m");
            if (axe!=null){
                equipBeltItem("axe-m");
                waitItemInHand("axe-m");
            }
        }
    }

    public static WItem getSackFromBelt() {
        WItem ret = getBeltWItem("travellerssack");
        if (ret==null)
            ret = getBeltWItem("bindle");
        return ret;
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
                    ZeeConfig.gameUI.error("no fish rod equipped");
                }
            } else {
                //equip hook or line
                String rodName = "";
                if(getLeftHandName().contains("/primrod") || getRightHandName().contains("/primrod")) {
                    rodName = "/primrod";
                } else if(getLeftHandName().contains("/bushpole") || getRightHandName().contains("/bushpole")){
                    rodName = "/bushpole";
                } else {
                    ZeeConfig.gameUI.error("no fish pole equipped");
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


    public static void itemZeeMenuClicked(WItem wItem, String petalName) {

        String itemName = wItem.item.getres().name;

        if(petalName.equals(ZeeFlowerMenu.STRPETAL_TRANSFER_ASC))
        {
            wItem.wdgmsg("transfer-sort", wItem.item, true);// true = ascending order
        }
        else if(petalName.equals(ZeeFlowerMenu.STRPETAL_TRANSFER_DESC))
        {
            wItem.wdgmsg("transfer-sort", wItem.item, false);// false = descending order
        }
        else if(petalName.equals(ZeeFlowerMenu.STRPETAL_AUTO_BUTCH))
        {
            autoButch(wItem,false);
        }
        else if(petalName.equals(ZeeFlowerMenu.STRPETAL_AUTO_BUTCH_ALL))
        {
            autoButch(wItem,true);
        }
        else if(petalName.equals(ZeeFlowerMenu.STRPETAL_KILLALL))
        {
            // kill all inventory cocoons
            if(itemName.endsWith("silkcocoon") || itemName.endsWith("chrysalis")){
                if (waitNoFlowerMenu()) {//wait petal "Kill All" is gone
                    Inventory inv = wItem.getparent(Inventory.class);
                    List<WItem> items = inv.children(WItem.class).stream()
                            .filter(wItem1 -> wItem1.item.getres().name.endsWith("silkcocoon") || wItem1.item.getres().name.endsWith("chrysalis"))
                            .collect(Collectors.toList());
                    ZeeConfig.gameUI.msg(clickAllItemsPetal(items, "Kill") + " cocoons clicked");
                }
            }
        }
        else if(petalName.equals(ZeeFlowerMenu.STRPETAL_EATALL))
        {
            //eat all table similar items
            if(ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_EAT)){
                Inventory inv = wItem.getparent(Inventory.class);
                List<WItem> items = inv.children(WItem.class).stream()
                        .filter(wItem1 -> wItem1.item.getres().name.equals(itemName))
                        .collect(Collectors.toList());
                takeAllInvItems(inv, items);
                ZeeConfig.gameUI.msg(items.size() + " noms");
            }
        }
        else
        {
            println("chooseItemFlowerMenu > unknown case");
        }
    }


    private static void autoButch(WItem wItem, boolean butchAll) {
        new ZeeThread() {
            public void run() {
                boolean bmBackup = ZeeConfig.butcherMode;
                try {
                    ZeeConfig.addPlayerText("autobutch");

                    //adjust autobutch settings
                    ZeeConfig.butcherMode = true;
                    ZeeConfig.butcherAutoList = ZeeConfig.DEF_LIST_BUTCH_AUTO;

                    //start
                    WItem item = wItem;
                    Inventory inv = getItemInventory(item);
                    Coord itemSlotCoord = getWItemCoord(item);
                    String itemName = getWItemName(item);
                    String firstItemName = itemName;
                    long changeMs;
                    ZeeConfig.lastMapViewClickButton = 2;//prepare for cancel click
                    while (!ZeeConfig.isTaskCanceledByGroundClick() && (!(itemName.endsWith("-clean") || itemName.endsWith("-cleaned"))) ){

                        //butch item and wait inventory changes
                        changeMs = now();
                        itemActCoord(item);
                        while (changeMs > ZeeConfig.lastInvItemMs) {
                            sleep(PING_MS);
                        }

                        // get next stage item, ends with "-dead", "-plucked", "-clean" or "-cleaned"
                        item = inv.getItemBySlotCoord(itemSlotCoord);//TODO empty slot may change 1-slot-item position
                        itemName = getWItemName(item);
                        //println("next item > "+itemName);

                        //if butch is over("-clean"), prepare next "butch all" item
                        if (butchAll && (itemName.endsWith("-clean") || itemName.endsWith("-cleaned"))){

                            //butch "-clean" item and wait inventory changes
                            //println("last butch 2> "+itemName);
                            changeMs = now();
                            itemActCoord(item);
                            while (changeMs > ZeeConfig.lastInvItemMs) {
                                sleep(PING_MS);
                            }

                            //get next dead animal for butching
                            List<WItem> items;
                            if (ZeeConfig.isFish(firstItemName))
                                items = inv.getWItemsByNameContains("/fish-"); //consider all fish the same
                            else
                                items = inv.getWItemsByNameContains(firstItemName);

                            if (items.size() == 0){
                                //no more items to butch
                                //println("no more items");
                                break;
                            }else{
                                //update next dead animal vars
                                item = items.get(0);
                                itemName = getWItemName(item);
                                itemSlotCoord = getWItemCoord(item);
                                //println("next item > "+itemName);
                            }
                        }
                    }

                    //single butch animal last action
                    if (!ZeeConfig.isTaskCanceledByGroundClick() && !butchAll && (itemName.endsWith("-clean") || itemName.endsWith("-cleaned"))) {
                        //println("last butch > " + itemName);
                        changeMs = now();
                        itemActCoord(item);
                        while (changeMs > ZeeConfig.lastInvItemMs) {
                            sleep(PING_MS);
                        }
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
                //restore settings
                ZeeConfig.butcherMode = bmBackup;
                ZeeConfig.butcherAutoList = Utils.getpref("butcherAutoList",ZeeConfig.DEF_LIST_AUTO_CLICK_MENU);
                ZeeConfig.removePlayerText();
            }
        }.start();
    }


    public static boolean sameNameAndQuality(WItem w1, WItem w2) {
        boolean ret = false;
        if (w1.item.getres().name.contentEquals(w2.item.getres().name)){
            if (getItemQuality(w1) == getItemQuality(w2))
                ret = true;
        }
        return ret;
    }


    public static double getItemQuality(WItem w) {
        return ZeeConfig.getItemQuality(w);
    }

    public static Inventory getItemInventory(WItem wItem) {
        if (wItem==null)
            return null;
        return wItem.getparent(Inventory.class);
    }

    public static Coord getWItemCoord(WItem wItem){
        return wItem.c.div(33);
    }

    public static String getWItemName(WItem wItem) {
        String name = "";
        if (wItem!=null && wItem.item!=null && wItem.item.getres()!=null)
            name = wItem.item.getres().name;
        return name;
    }


    public boolean showItemFlowerMenu(){

        if (!isLongClick())
            return false;

        boolean showMenu = true;
        ZeeFlowerMenu menu = null;

        ArrayList<String> opts = new ArrayList<String>();//petals array
        Inventory inv = getItemInventory(wItem);

        if (ZeeConfig.isFish(itemName)) {
            if (inv.countItemsByNameContains("/fish-") > 1){
                opts.add(ZeeFlowerMenu.STRPETAL_AUTO_BUTCH_ALL);
                if (isTransferWindowOpened()) {
                    opts.add(ZeeFlowerMenu.STRPETAL_TRANSFER_ASC);
                    opts.add(ZeeFlowerMenu.STRPETAL_TRANSFER_DESC);
                }
            }
            if (opts.size()==0)
                showMenu = false;
            else
                menu = new ZeeFlowerMenu(wItem, opts.toArray(String[]::new));
        }
        else if (ZeeConfig.isButchableSmallAnimal(itemName)) {
            opts.add(ZeeFlowerMenu.STRPETAL_AUTO_BUTCH);
            if (inv.countItemsByNameContains(itemName) > 1){
                opts.add(ZeeFlowerMenu.STRPETAL_AUTO_BUTCH_ALL);
            }
            if (isTransferWindowOpened()) {
                opts.add(ZeeFlowerMenu.STRPETAL_TRANSFER_ASC);
                opts.add(ZeeFlowerMenu.STRPETAL_TRANSFER_DESC);
            }
            menu = new ZeeFlowerMenu(wItem, opts.toArray(String[]::new));
        }
        else if(itemName.endsWith("silkcocoon") || itemName.endsWith("chrysalis")){
            opts.add(ZeeFlowerMenu.STRPETAL_KILLALL);
            if (isTransferWindowOpened()) {
                opts.add(ZeeFlowerMenu.STRPETAL_TRANSFER_ASC);
                opts.add(ZeeFlowerMenu.STRPETAL_TRANSFER_DESC);
            }
            menu = new ZeeFlowerMenu(wItem, opts.toArray(String[]::new));
        }
        else if (isItemWindowTable() && ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_EAT)){
            menu = new ZeeFlowerMenu(wItem, ZeeFlowerMenu.STRPETAL_EATALL);
        }
        else{
            showMenu = false;
        }

        if (showMenu && menu!=null) {
            ZeeConfig.gameUI.ui.root.add(menu, ZeeConfig.lastUiClickCoord);
        }

        return showMenu;
    }

    public static void takeAllInvItems(Inventory inv, List<WItem> items) {
        try {
            for (WItem w : items) {
                Thread.sleep(PING_MS);
                w.item.wdgmsg("take", w.getInvSlotCoord());
            }
        }catch (Exception e){
            e.printStackTrace();
            ZeeConfig.gameUI.msg("takeAllInvItems: "+e.getMessage());
        }
    }

    public static int clickAllItemsPetal(List<WItem> items, String petalName) {
        ZeeConfig.addGobText(ZeeConfig.getPlayerGob(),"clicking "+items.size()+" items",0,255,255,255,10);
        int itemsClicked = 0;
        ZeeConfig.lastMapViewClickButton = 2; // setup for clickCancelTask()
        int countNoMenu = 0;
        for (WItem w: items) {
            try {
                if (ZeeConfig.isTaskCanceledByGroundClick()) {
                    //ZeeClickGobManager.resetClickPetal();
                    ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
                    return itemsClicked;
                }
                itemActCoord(w);
                if(waitFlowerMenu()){
                    choosePetal(getFlowerMenu(), petalName);
                    itemsClicked++;
                }else{
                    countNoMenu++;
                    println("clickAllItemsPetal > no flowermenu "+countNoMenu+"/"+items.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
                //ZeeClickGobManager.resetClickPetal();
                ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
                return itemsClicked;
            }
        }
        ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
        return itemsClicked;
    }

    public static void itemAct(WItem item){
        itemAct(item, item.ui.modflags());
    }
    public static void itemAct(WItem item, int modflags){
        gItemAct(item.item, modflags);
    }
    public static void gItemAct(GItem item, int modflags){
        item.wdgmsg("itemact", modflags);
    }

    public static void itemActCoord(WItem item){
        itemActCoord(item, item.ui.modflags());
    }
    public static void itemActCoord(WItem item, int modflags){
        gItemActCoord(item.item, modflags);
    }
    public static void gItemActCoord(GItem item, int modflags){
        item.wdgmsg("iact", item.c.div(2), modflags);
    }

    private boolean isLongClick() {
        return clickDiffMs > LONG_CLICK_MS;
    }

    static boolean isTransferWindowOpened() {
        List<Window> list = ZeeConfig.getContainersWindows();
        println("isTransferWindowOpened > "+list.size()+" windows > "+list.stream().map(window -> window.cap).collect(Collectors.joining(" , ")));
        return list.size() > 0;
    }

    static boolean isTransferWindowOpened(List<String> excludeWindowsNamed) {
        List<Window> openWindows = ZeeConfig.getContainersWindows();
        int openWinCount = openWindows.size();
        for (Window window : openWindows) {
            for (String exclude : excludeWindowsNamed) {
                if (window.cap.contentEquals(exclude))
                    openWinCount--;//exclude window from counting
            }
        }
        return openWinCount > 0;
    }

    static boolean isStackTransferable(GItem item){
        // check if trasnfer window is open, except quiver, creel, ...
        return isTransferWindowOpened(List.of("Quiver","Creel","Basket"));
    }

    private void drinkFrom() {
        //ZeeClickGobManager.scheduleClickPetalOnce("Drink");
        //itemAct(wItem);
        clickItemPetal(wItem,"Drink");
    }

    public static boolean clickItemPetal(WItem wItem, String petalName) {
        if (wItem==null){
            println(">clickItemPetal wItem null");
            return false;
        }
        itemActCoord(wItem);
        if(waitFlowerMenu()){
            //println("clickItemPetal > flower menu");
            choosePetal(getFlowerMenu(), petalName);
            return waitNoFlowerMenu();
        }else{
            //println("clickItemPetal > no flower menu");
            return false;
        }
    }


    private boolean isItemDrinkingVessel() {
        return isItemDrinkingVessel(itemName);
    }
    public static boolean isItemDrinkingVessel(String name) {
        String[] items = {"waterskin","bucket-","kuksa","woodencup","glassjug","waterflask","tankard","metalmug","winebottle","wineglass"};
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
    public static boolean isShield(String name) {
        return name.endsWith("/roundshield");
    }

    public static boolean dropHoldingItemToBeltOrInv() {
        Inventory inv;
        if(ZeeConfig.getWindow("Belt")==null){
            return false;
            //inv = ZeeConfig.getMainInventory(); // TODO fix fitting item to inventory
        }else{
            inv = ZeeManagerItemClick.getInvBelt();
        }
        return dropHoldingItemToInv(inv);
    }

    public static boolean dropHoldingItemToInv(Inventory inv) {
        if(!ZeeConfig.isPlayerHoldingItem() || inv==null)
            return false;
        try{
            String windowTitle = inv.getparent(Window.class).cap;
            if (windowTitle.contentEquals("Belt")){
                List<Coord> freeSlots = inv.getFreeSlots();
                if (freeSlots.size()==0)
                    return false;//inv full
                Coord c = freeSlots.get(0);
                inv.wdgmsg("drop", c);
                return waitNotHoldingItem();
            }
            else {
                List<Coord> freeSlots = inv.getFreeSlots();
                if (freeSlots.size()==0)
                    return false;//inv full
                Coord c = freeSlots.get(0);
                inv.wdgmsg("drop", c);
                return waitNotHoldingItem();
            }
//            else if(windowTitle.contentEquals("Inventory")){
                //no belt, drop to inv area
//                WItem holdingItem = getHoldingItem();
//                Coord itemSize = holdingItem.sz.div(Inventory.sqsz);
//                Coord topLeftSlot = inv.getFreeSlotAreaSized(itemSize.x,itemSize.y);
//                if (topLeftSlot==null) {
//                    //println("dropHoldingItemToInv > topLeftSlot null");
//                    return false;
//                }
//                inv.wdgmsg("drop",topLeftSlot);
//                sleep(PING_MS*4);
//                return !ZeeConfig.isPlayerHoldingItem();//waitHoldingItemChanged();//waitNotHoldingItem();
//            }
//            else {
//                ZeeConfig.println("dropHoldingItemToInv > "+windowTitle);
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static Coord dropHoldingItemToInvAndRetCoord(Inventory inv) {
        if(!ZeeConfig.isPlayerHoldingItem() || inv==null)
            return null;
        try{
            List<Coord> freeSlots = inv.getFreeSlots();
            if (freeSlots.size()==0)
                return null;//inv full
            Coord c = freeSlots.get(0);
            inv.wdgmsg("drop", c);
            waitNotHoldingItem();
            return c;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private boolean isItemWindowName(String windowName){
        return (wItem.getparent(Window.class).cap.equalsIgnoreCase(windowName));
    }
    private boolean isItemWindowBelt() {
        return isItemWindowName("Belt");
    }
    private boolean isItemWindowEquips() {
        return isItemWindowName("Equipment");
    }
    private boolean isItemWindowTable() {
        return isItemWindowName("Table");
    }

    /*
        equip occupied hand and wait
     */
    public static void equipLeftOccupiedHand() {
        getEquipory().wdgmsg("drop", 6);
        waitHoldingItemChanged();//waitHoldingItem();
    }
    public static void equipRightOccupiedHand() {
        getEquipory().wdgmsg("drop", 7);
        waitHoldingItemChanged();//waitHoldingItem();
    }

    /*
        equip empty hand and wait
     */
    public static void equipLeftEmptyHand() {
        getEquipory().wdgmsg("drop", 6);
        waitNotHoldingItem();
    }
    public static void equipRightEmptyHand() {
        getEquipory().wdgmsg("drop", 7);
        waitNotHoldingItem();
    }

    public static boolean equipEmptyHand() {
        if(isLeftHandEmpty())
            equipLeftEmptyHand();
        else if(isRightHandEmpty())
            equipRightEmptyHand();
        return waitNotHoldingItem();
    }

    public static boolean isLeftHandEmpty() {
        return (getEquipory().leftHand==null);
    }

    public static boolean isRightHandEmpty() {
        return (getEquipory().rightHand==null);
    }


    private boolean pickUpItem() {
        return pickUpItem(wItem);
    }
    public static boolean pickUpItem(WItem wItem) {
        wItem.item.wdgmsg("take", new Coord(wItem.sz.x / 2, wItem.sz.y / 2));
        return waitHoldingItem();
    }

    public static boolean unequipLeftItem() {
        if(getEquipory().leftHand==null)
            return true;
        getEquipory().leftHand.item.wdgmsg("take", new Coord(getEquipory().leftHand.sz.x/2, getEquipory().leftHand.sz.y/2));
        return waitHoldingItem();
    }

    public static boolean unequipRightItem() {
        if(getEquipory().rightHand==null)
            return true;
        getEquipory().rightHand.item.wdgmsg("take", new Coord(getEquipory().rightHand.sz.x/2, getEquipory().rightHand.sz.y/2));
        return waitHoldingItem();
    }

    private boolean isItemSack() {
        return isItemSack(itemName);
    }

    public static boolean isItemSack(String name) {
        return name.endsWith("travellerssack") || name.endsWith("bindle");
    }

    private boolean isItemAxeChopTree() {
        return isItemAxeChopTree(itemName);
    }
    public static boolean isItemAxeChopTree(String name) {
        return name.endsWith("woodsmansaxe") || name.endsWith("axe-m") || name.endsWith("butcherscleaver") || name.endsWith("tinkersthrowingaxe") || name.endsWith("stoneaxe");
    }

    public static boolean isItemButchingTool(WItem wItem){
        return isItemButchingTool(wItem.item.getres().name);
    }
    public static boolean isItemButchingTool(String itemName){
        String endlist = "woodsmansaxe,axe-m,butcherscleaver,tinkersthrowingaxe,stoneaxe,fyrdsword,hirdsword,bronzesword,b12axe,cutblade";
        String[] arr = endlist.split(",");
        for (int i = 0; i < arr.length; i++) {
            if (itemName.endsWith(arr[i]))
                return true;
        }
        return false;
    }
    private boolean isItemButchingTool(){
        return isItemButchingTool(itemName);
    }

    private boolean isItemPlantable() {
        return isItemPlantable(itemName);
    }

    public static boolean isItemPlantable(String name){
        String list = "seed-barley,seed-carrot,carrot,seed-cucumber,seed-flax,"
                +"seed-grape,seed-hemp,seed-leek,leek,seed-lettuce,seed-millet,"
                +"seed-pipeweed,seed-poppy,seed-pumpkin,seed-wheat,seed-turnip,turnip,"
                +"seed-wheat,seed-barley,beetroot,yellowonion,redonion,peapod";
        name = name.replace("gfx/invobjs/","");
        return list.contains(name);
    }

    public boolean isItemHandEquipable() {
        String[] items = {
            // weapons tools
            "b12axe","boarspear","cutblade","fyrdsword","hirdsword","bronzesword","sling",
            "sledgehammer","huntersbow","rangersbow","roundshield",
            "axe-m","woodsmansaxe","stoneaxe","butcherscleaver","tinkersthrowingaxe",
            // tools equips
            "bonesaw","saw-m","scythe","pickaxe","shovel","smithshammer","shears",
            "travellerssack","bindle","bushpole","primrod","glassrod","dowsingrod",
            "fryingpan","lantern","torch","mortarandpestle","bucket","volvawand",
            "diversweight",
            // instruments
            "flute","harmonica","bagpipe","drum"
        };
        for (int i = 0; i < items.length; i++) {
            if (itemName.contains(items[i])){
                return true;
            }
        }
        return false;
    }

    private boolean isTwoHandedItem() {
        return isTwoHandedItem(itemName);
    }
    public static boolean isTwoHandedItem(WItem w) {
        return isTwoHandedItem(w.item.getres().name);
    }
    public static boolean isTwoHandedItem(String name) {
        String basename = name.replaceAll("[^/]+/","");
        String[] items = {"scythe","pickaxe","shovel","b12axe",
                "boarspear","cutblade","sledgehammer", "mortarandpestle",
                "huntersbow","rangersbow","dowsingrod", "glassrod", "diversweight"};
        for (int i = 0; i < items.length; i++) {
            if (basename.contains(items[i])){
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
            WItem witem = getInvBelt().getWItemsByNameContains(name).get(0);
            return pickUpItem(witem);
        }catch (Exception e){
            return false;
        }
    }

    public static boolean pickupHandItem(String nameContains) {
        try {
            if(getLeftHandName().contains(nameContains))
                return pickUpItem(getLeftHand());
            else if(getRightHandName().contains(nameContains))
                return pickUpItem(getRightHand());
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    // pick up first item found in inventory, using names list
    public static boolean pickUpInvItem(Inventory inv, String ... names) {
        try {
            if (names==null || names.length==0) {
                println("[ERROR] pickUpInvItem > names empty or null");
                return false;
            }
            WItem witem = null;
            List<WItem> list;
            for (int i = 0; i < names.length; i++) {
                list = inv.getWItemsByNameContains(names[i]);
                if (list.isEmpty())
                    continue;
                //found item with name[i]
                witem = list.get(i);
                break;
            }
            // found no item with listed names
            if (witem==null)
                return false;
            // pickup found item
            return pickUpItem(witem);
        }catch (Exception e){
            return false;
        }
    }

    public static WItem getBeltWItem(String nameContains) {
        try {
            WItem witem = getInvBelt().getWItemsByNameContains(nameContains).get(0);
            return witem;
        }catch (Exception e){
            return null;
        }
    }

    public static boolean isItemEquipped(String nameContains){
        try {
            GItem items[] = getEquipory().wmap.keySet().toArray(new GItem[]{});
            for (int i = 0; i < items.length; i++) {
                if (items[i].getres().name.contains(nameContains))
                    return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static WItem getEquippedItemNameContains(String nameContains) {
        WItem[] items = getEquipory().children(WItem.class).toArray(new WItem[]{});
        for (int i = 0; i < items.length; i++) {
            if (items[i].item.getres().name.contains(nameContains)){
                return items[i];
            }
        }
        return null;
    }

    public static WItem getEquippedItemNameEndsWith(String nameEndsWith) {
        WItem[] items = getEquipory().children(WItem.class).toArray(new WItem[]{});
        for (int i = 0; i < items.length; i++) {
            if (items[i].item.getres().name.endsWith(nameEndsWith)){
                return items[i];
            }
        }
        return null;
    }

    public static boolean isItemInHandSlot(String name){
        try {
            /*
            Equipory eq = ZeeConfig.windowEquipment.getchild(Equipory.class);
            return eq.leftHand.item.getres().name.contains(name)
                    || eq.rightHand.item.getres().name.contains(name);
             */
            return getLeftHandName().contains(name) || getRightHandName().contains(name);
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

    public static void equipBeltItem(String name) {
        if(ZeeManagerItemClick.isItemInHandSlot(name)) {
            return;
        }
        WItem item = ZeeManagerItemClick.getBeltWItem(name);
        if (item!=null)
            new ZeeManagerItemClick(item).start();//use equipManager logic
        else
            println("itemManager.equipBeltItem() > item '"+name+"' not found");
    }

    public static WItem getHoldingItem(){
        return ZeeConfig.gameUI.vhand;
    }

    // "3.00 l of Water"
    public static String getItemContentsName(WItem w) {
        ItemInfo.Contents contents = getItemInfoContents(w.item.info());
        if (contents!=null && contents.sub!=null) {
            return getItemInfoName(contents.sub);
        }
        return "";
    }

    public static String getHoldingItemContentsNameQl() {
        WItem item = getHoldingItem();
        String msg = "";
        if (item==null){
            println("getHoldingItemContentsNameQl > not holding item?");
            return "";
        }
        ItemInfo.Contents contents = getItemInfoContents(item.item.info());
        if (contents!=null && contents.sub!=null) {
            String name = getItemInfoName(contents.sub);
            int ql = getItemInfoQuality(contents.sub).intValue();
            msg += name.replaceAll(".+ of ","");// 0.45 l of Water
            msg += " q" + ql;
        }else
            println("contents null? try picking  up item first");
        //println("msg = "+msg);
        return msg;
    }

    public static String getItemInfoName(List<ItemInfo> info) {
        try {
            for (ItemInfo v : info) {
                if (v instanceof ItemInfo.Name) {
                    return ((ItemInfo.Name)v).str.text;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return(null);
    }

    public static int getItemInfoAmount(List<ItemInfo> info) {
        try {
            for (ItemInfo v : info) {
                if (v instanceof GItem.Amount) {
                    return ((GItem.Amount)v).itemnum();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public static ItemInfo getItemInfoByClass(List<ItemInfo> info, Class tClass) {
        try {
            for (ItemInfo v : info) {
                if (v.getClass().getName().contentEquals(tClass.getName())) {
                    return v;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return(null);
    }

    public static Double getItemInfoQuality(List<ItemInfo> info) {
        try{
            for(ItemInfo v : info) {
                if(v.getClass().getSimpleName().equals("Quality")) {
                    return((Double) v.getClass().getField("q").get(v));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return (double)-1;
    }

    public static ItemInfo.Contents getItemInfoContents(List<ItemInfo> info) {
        try{
            for(ItemInfo v : info) {
                if(v instanceof ItemInfo.Contents) {
                    return((ItemInfo.Contents) v);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return(null);
    }

    static boolean drinkThreadWorking = false;
    public static void drinkFromBeltHandsInv() {
        if (drinkThreadWorking || ZeeConfig.isPlayerDrinkingPose()) {
            println("already drinking");
            return;
        }
        new ZeeThread(){
            public void run() {
                boolean drank = false;
                try {
                    Inventory inv;
                    drinkThreadWorking = true;

                    // drink from belt
                    if (!drank && (inv = getInvBelt()) != null) {
                        WItem beltItems[] = inv.children(WItem.class).toArray(WItem[]::new);
                        for (int i = 0; i < beltItems.length; i++) {
                            String name = beltItems[i].item.getres().basename();
                            String contents;
                            if (isItemDrinkingVessel(name)) {
                                // "3.00 l of Water"
                                contents = getItemContentsName(beltItems[i]);
                                if (contents.contains("Water")) {
                                    //println("drink belt " + contents);
                                    if(clickItemPetal(beltItems[i], "Drink")) {
                                        //ZeeManagerItemClick.waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_DRINK);
                                        drank = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // drink from inv
                    if ( !drank && (inv = ZeeConfig.getMainInventory()) != null){
                        WItem invItems[] = inv.children(WItem.class).toArray(WItem[]::new);
                        for (int i = 0; i < invItems.length; i++) {
                            String name = invItems[i].item.getres().basename();
                            String contents;
                            if (isItemDrinkingVessel(name)) {
                                // "3.00 l of Water"
                                contents = getItemContentsName(invItems[i]);
                                if (contents.contains("Water")) {
                                    //println("drink inv " + contents);
                                    if (clickItemPetal(invItems[i], "Drink")) {
                                        //ZeeManagerItemClick.waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_DRINK);
                                        drank = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // drink form hands (bucket?)
                    if (!drank) {
                        WItem w = null;
                        if (getLeftHandName().contains("bucket-water") || getLeftHandName().contains("bucket-tea"))
                            w = getLeftHand();
                        else if (getRightHandName().contains("bucket-water") || getRightHandName().contains("bucket-tea"))
                            w = getRightHand();
                        if (w != null) {
                            //println("drink bucket " + getItemContentsName(w));
                            if (clickItemPetal(w, "Drink")) {
                                //ZeeManagerItemClick.waitPlayerPoseNotInList(ZeeConfig.POSE_PLAYER_DRINK);
                                drank = true;
                            }
                        }else
                            println("bucket hand null");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                drinkThreadWorking = false;
            }
        }.start();
    }

    public static boolean isCoracleEquipped() {
        return ZeeManagerItemClick.isItemEquipped("gfx/invobjs/small/coracle");
    }

    public static boolean isStackItemPlaceholder(GItem i) {
        try {
            if (i.getres().basename().startsWith("seed-"))
                return false;
            if (getItemInfoAmount(i.info()) > 0)
                return true;
        }catch (Exception e){
            e.getMessage();
        }
        return false;
    }

    public static void undoStack(GItem i) {
        try {
            gItemActCoord(i,3);
        }catch (Exception e){
            //e.printStackTrace();
        }
    }

    public static void undoMultipleStacks(GItem item) {
        new Thread(){
            public void run() {
                try {
                    Inventory inv = item.getparent(Inventory.class);
                    List<WItem> invItems = inv.getWItemsByNameEndsWith(item.getres().name);
                    for (WItem wItem : invItems) {
                        if (!isStackItemPlaceholder(wItem.item))
                            continue;
                        undoStack(wItem.item);
                        sleep(PING_MS);
                        if (inv.getNumberOfFreeSlots() < 3)
                            break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

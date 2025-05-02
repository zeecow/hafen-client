package haven;

import haven.res.ui.stackinv.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
    Mid-click auto-equips items from belt/hands.
    Drinks from vessels: waterskin, bucket.
 */
public class ZeeManagerItems extends ZeeThread{

    private final WItem wItem;
    private Coord coord;
    String itemName;
    String itemBasename;
    String leftHandItemName, rightHandItemName;
    boolean cancelManagerBecauseException = false;
    public static Equipory equipory;
    static Inventory invBelt = null;
    public static long clickStartMs, clickEndMs, clickDiffMs;

    static WItem lastItemClicked;
    static long lastItemClickedMs;
    static int lastItemClickedButton;

    static boolean droppedBucket;

    public ZeeManagerItems(WItem wItem, Coord c) {
        clickDiffMs = clickEndMs - clickStartMs;
        this.wItem = wItem;
        this.coord = c;
        init(wItem);
    }

    public ZeeManagerItems(WItem wItem) {
        clickDiffMs = clickEndMs - clickStartMs;
        this.wItem = wItem;
        init(wItem);
    }

    //called on midclick smelter holding a bucket
    static void getQuicksilverFromSmelter(Gob smelter) {

        WItem bucket = ZeeManagerItems.getHoldingItem();

        if (bucket==null){
            println("holding item null");
            return;
        }

        if (bucket.item.getres().name.contains("/bucket")){

            // check bucket contents
            String contents = ZeeManagerItems.getItemContentsName(bucket);
            if ( contents.contains("10.00 l of Quicksilver") ||
                    (!contents.isBlank() && !contents.contains("Quicksilver")) )
            {
                println("cant pick quicksilver with this bucket");
                return;
            }

            // open smelter
            new ZeeThread(){
                public void run() {
                    try {

                        ZeeConfig.addPlayerText("get quicksilver");

                        //close any open smelters
                        List<Window> openSmeltWins = ZeeConfig.getWindowsNameEndsWith("Smelter");
                        if (openSmeltWins.size() > 0){
                            for (Window openWin : openSmeltWins) {
                                String cap = openWin.cap;
                                openWin.wdgmsg("close");
                                waitWindowClosed(cap);
                            }
                        }

                        // ctrl + rclick smelter
                        ZeeManagerGobs.gobClick(smelter,3,0);

                        if (!waitPlayerDistToGob(smelter,35)){
                            println("failed approaching smelter");
                            ZeeConfig.removePlayerText();
                            return;
                        }

                        if(!waitWindowOpenedNameContains("Smelter")){
                            println("failed waiting smelter window");
                            ZeeConfig.removePlayerText();
                            return;
                        }

                        Window win = ZeeConfig.getWindowNameContains("Smelter");
                        if (win == null){
                            println("couldnt get smelter window");
                            ZeeConfig.removePlayerText();
                            return;
                        }

                        Inventory inv = win.getchild(Inventory.class);
                        List<WItem> list = inv.getWItemsByNameEndsWith("/mercury");
                        if (list.size() > 0) {
                            for (WItem quicksilver : list) {
                                ZeeManagerItems.itemAct(quicksilver,0);
                                sleep(PING_MS);
                            }
                        }
                        else{
                            println("no quicksilver found");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ZeeConfig.removePlayerText();
                }
            }.start();
        }
    }

    public static void showWindowClickAllItemPetals(String petalName) {

        WItem itemClickAll = ZeeManagerItems.lastItemClicked;
        if (itemClickAll==null){
            println("checkClickAllItems > item null");
            return;
        }

        String wtName = itemClickAll.item.getres().name;

        // flowermenu belongs to witem and not gob
        if (isFlowerMenuFromWItem()) {

            Inventory inv = itemClickAll.getparent(Inventory.class);
            if (inv==null){
                println("checkClickAllItems > inv null");
                return;
            }


            //show window click all items
            String winTitle = "Click all items";
            Window win = ZeeConfig.getWindow(winTitle);
            if (win != null) {
                win.reqdestroy();
            }
            win = ZeeConfig.gameUI.add(
                new Window(Coord.of(120,70),winTitle){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("close")){
                            this.reqdestroy();
                        }
                    }
                },ZeeConfig.gameUI.sz.div(2)
            );
            Widget wdg;
            wdg = win.add(new Label("item: "+wtName),0,0);
            wdg = win.add(new Label("petal: "+petalName),0,15);
            Window finalWin = win;
            wdg = win.add(new Button(100,"click all"){
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate")){
                        if (!ui.modctrl){
                            ZeeConfig.msgError("ctrl+click to confirm");
                            return;
                        }
                        // click all items petals
                        List<WItem> invItems = inv.getWItemsByNameEndsWith(wtName);
                        if (invItems.size() > 0){
                            new ZeeThread(){
                                public void run() {
                                    try {
                                        finalWin.reqdestroy();
                                        clickAllItemsPetal(invItems,petalName);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    }
                }
            },0,30);

            win.pack();
        }
    }

    static boolean isFlowerMenuFromWItem() {
        return (ZeeManagerItems.lastItemClickedMs > ZeeConfig.lastMapViewClickMs);
    }

    static void checkGItem(GItem gItem) {

        if (gItem.contents!=null)
            return;

        String basename = gItem.getres().basename();
        Inventory inv = gItem.getparent(Inventory.class);
        if(inv!=null && inv.isMainInv()) {
            ZeeConfig.lastInvGItemCreated = gItem;
            ZeeConfig.lastInvGItemCreatedName = gItem.getres().name;
            ZeeConfig.lastInvGItemCreatedBaseName = basename;
            ZeeConfig.lastInvGItemCreatedMs = ZeeThread.now();
        }

        //drop mined items
        if (ZeeConfig.isPlayerCursorMining) {
            ZeeManagerMiner.checkMiningLogHighestQl(gItem,basename);
            if (ZeeConfig.dropMinedStones && ZeeManagerMiner.isStoneNotOre(basename) ||
                    ZeeConfig.dropMinedOre && ZeeManagerMiner.isRegularOre(basename) ||
                    ZeeConfig.dropMinedOrePrecious && ZeeManagerMiner.isPreciousOre(basename) ||
                    ZeeConfig.dropMinedCurios && ZeeConfig.mineablesCurios.contains(basename) )
            {
                ZeeManagerMiner.lastDropItemMs = System.currentTimeMillis();
                gItem.wdgmsg("drop", Coord.z);
            }
        }
        else if( ZeeConfig.farmerMode ) {
            //drop all non-seed crops at once
            if(ZeeManagerFarmer.busy && !ZeeManagerStockpile.selAreaPile) {
                if (!basename.startsWith("seed-") && ZeeConfig.isItemCrop(basename)) {
                    gItem.wdgmsg("drop", gItem.c, -1);//TODO c should be witem coord?
                }
            }
            else if(basename.startsWith("seed-") && gItem.parent instanceof Inventory) {
                //farmermode not busy
                if (ZeeConfig.lastSavedOverlayEndCoord == null) {
                    //cancel farmermode
                    ZeeConfig.println("seedfarmer > no tile selection, reset initial state");
                    //ZeeConfig.farmerMode = false; //TODO test
                    ZeeManagerFarmer.exitSeedFarmer();
                }
                else {
                    //start farmermode
                    new ZeeManagerFarmer(gItem, basename).start();
                }
            }
        }
        // drop boards if destroying logs
        else if (ZeeManagerTrees.isDestroyingTreelogs && basename.startsWith("board-")){
            gItem.wdgmsg("drop", Coord.z);
        }
        //drop seeds
        else if( ZeeConfig.dropCrops && ZeeConfig.isItemCrop(basename) && gItem.parent instanceof Inventory){
            if (inv!=null && inv.isMainInv() && inv.getNumberOfFreeSlots() < inv.getNumberOfSlots()/2) {
                inv.dropItemsByNameEndsWith(basename);
            }
        }

        // update counter
        ZeeConfig.invCounterUpdate(gItem);
    }

    static boolean isAnyHandEmpty() {
        return ZeeManagerItems.isLeftHandEmpty() || ZeeManagerItems.isRightHandEmpty();
    }

    private void init(WItem wItem) {
        try{
            equipory = ZeeConfig.windowEquipment.getchild(Equipory.class);
            updateHandItemNames();
            itemName = wItem.item.getres().name;//clicked item, started manager
            itemBasename = wItem.item.getres().basename();
        }catch (Exception e){
            cancelManagerBecauseException = true;
            println("click manager init exception "+e.getCause());
        }
        //println(itemName +"  "+ getWItemCoord(wItem));//+"  "+ZeeConfig.getCursorName());
        //wItem.item.info().forEach(itemInfo -> println("    "+itemInfo.getClass().getSimpleName()));
    }

    private void updateHandItemNames() {
        leftHandItemName = (getEquipory().leftHand==null ? "" : getEquipory().leftHand.item.getres().name);
        rightHandItemName = (getEquipory().rightHand==null ? "" : getEquipory().rightHand.item.getres().name);
    }


    @Override
    public void run() {

        if(cancelManagerBecauseException)
            return;

        try{

            if (ZeeConfig.isPlayerHoldingItem()){

                // stack items
                if (itemName.contentEquals(getHoldingItem().item.getres().name)){
                    // create multiple stacks
                    if (isLongClick())
                        gItemAct(wItem.item,UI.MOD_CTRL_SHIFT);
                    // create one stack
                    else
                        gItemAct(wItem.item,UI.MOD_SHIFT);
                    playFeedbackSound();
                    return;
                }
            }

            // show item context menu
            if(showMenuLongClick()){
                return;
            }
            // cancel if holding item and no item context menu
            else if (ZeeConfig.isPlayerHoldingItem()){
                return;
            }

            // fishing
            if (isLongClick() && ZeeFishing.isFishingItem(itemName)) {
                ZeeFishing.switchFishingEquips(wItem,itemName);
                return;
            }


            // undo stack if no transfer available
            if( isStackByAmount(wItem.item)  &&  !isStackTransferable(wItem.item)){
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
                    playFeedbackSound();
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
//            if(ZeeConfig.getWindow("Belt")==null){
//                ZeeConfig.gameUI.error("no belt window");
//                return;
//            }
            if(ZeeConfig.getWindow("Equipment")==null){
                ZeeConfig.gameUI.error("no equips window");
                return;
            }

            invBelt = getInvBelt();

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
                    //switch 2handed item for bucket, if no belt (noob char)
                    if (invBelt==null && isItemEquipped("/bucket")) {
                        if (ZeeConfig.isPlayerTileDeepWater()){//deep water cancel drop
                            ZeeConfig.msgError("not dropping bucket in deep water");
                            return;
                        }
                        if (pickupHandItem("/bucket")) {
                            getHoldingItem().item.wdgmsg("drop", Coord.z);
                            droppedBucket = true;
                        }
                    }else{
                        droppedBucket = false;
                    }
                    //switch 2handed item for another 2handed item
                    if(!isLeftHandEmpty() && isTwoHandedItem(leftHandItemName)) {
                        pickUpItem();
                        equipLeftOccupiedHand();
                        dropHoldingItemToBeltOrInv();
                    }
                    //switch 2handed item for 1handed equipped, or none equipped
                    else if(isLeftHandEmpty() || isRightHandEmpty()) {
                        pickUpItem();
                        if(!isLeftHandEmpty())
                            equipLeftOccupiedHand();
                        else if(!isRightHandEmpty())
                            equipRightOccupiedHand();
                        else
                            equipLeftEmptyHand();
                        dropHoldingItemToBeltOrInv();
                    }
                    //switch 2handed item for 2 separate items
                    else if(!isLeftHandEmpty() && !isRightHandEmpty()){
                        // switch using belt
                        if (invBelt!=null && invBelt.getNumberOfFreeSlots() > 0) {
                            unequipLeftItem();//unequip 1st item
                            if(dropHoldingItemToBeltOrInv()){
                                pickUpItem();
                                equipRightOccupiedHand();//switch for 2nd item
                                dropHoldingItemToBeltOrInv();
                            }
                        }
                        // switch using main inv
                        else if(invBelt==null && isItemWindowName("Inventory")){
                            unequipLeftItem();//unequip 1st item
                            if(dropHoldingItemToBeltOrInv()){
                                if (ZeeConfig.isPlayerHoldingItem()){
                                    println("still holding item > drop to inv switched items?");
                                } else {
                                    pickUpItem();
                                    equipRightOccupiedHand();//switch for 2nd item
                                    dropHoldingItemToBeltOrInv();
                                }
                            }
                        }
                    }
                }
                else if(isItemWindowEquips()) {
                    if (invBelt!=null && invBelt.getNumberOfFreeSlots() > 0) {
                        //send to belt if possible
                        pickUpItem();
                        dropHoldingItemToBeltOrInv();
                    }
                }

            }
            // try switch axe for axe (1handed)
            else if (isItemAxeChopTree()) {
                // if 2 axes equipped, unnequip one before next steps
                if (isItemAxeChopTree(leftHandItemName) && isItemAxeChopTree(rightHandItemName)) {
                    if (unequipLeftItem()) {
                        if(!dropHoldingItemToBeltOrInv()) {
                            equipEmptyHand(); // fall back
                        }
                    }
                    updateHandItemNames();
                }
                pickUpItem();
                // if hand item is only sack, try replacing other hand
                if (isItemSack(leftHandItemName) && !isItemSack(rightHandItemName)) {
                    equipRightOccupiedHand();
                } else if (!isItemSack(leftHandItemName) && isItemSack(rightHandItemName)) {
                    equipLeftOccupiedHand();
                }
                // if hand item is shield, try replacing other hand
                else if (isShield(leftHandItemName)) {
                    equipRightOccupiedHand();
                }else if (isShield(rightHandItemName)) {
                    equipLeftOccupiedHand();
                }
                // replace axe for axe
                else if (isItemAxeChopTree(leftHandItemName)) {
                    equipLeftOccupiedHand();
                }
                else if (isItemAxeChopTree(rightHandItemName)) {
                    equipRightOccupiedHand();
                }
                // replace empty hand
                else if (isLeftHandEmpty() || isRightHandEmpty()) {
                    equipEmptyHand();
                }
                // equip non-bucket hand
                else if (leftHandItemName.contains("/bucket")){
                    equipRightOccupiedHand();
                }else if (rightHandItemName.contains("/bucket")){
                    equipLeftOccupiedHand();
                }
                else {
                    equipLeftOccupiedHand();//all hands occupied, try equip left
                }
                dropHoldingItemToBeltOrInv();
            }
            // non-axe 1-handed items
            else{

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

    private static void fillCheesetrays(WItem tray) {
        new ZeeThread(){
            public void run() {
                try {
                    ZeeConfig.addPlayerText("curding");

                    String curdName = getHoldingItem().item.getres().name;
                    Inventory mainInv = ZeeConfig.getMainInventory();
                    Inventory trayInv = tray.getparent(Inventory.class);
                    int invCurds = mainInv.countItemsByNameEquals(curdName);
                    List<WItem> freeTrays = trayInv.getItemsByNameEnd("/cheesetray");

                    while(invCurds > 0 && freeTrays.size() > 0){

                        // add holding curd to tray until filled or no more curds
                        freeTrays.get(0).item.wdgmsg("itemact",UI.MOD_CTRL_SHIFT);
                        playFeedbackSound();

                        // wait transfers
                        sleep(555);

                        // if not holding curd try pickup more
                        if (!ZeeConfig.isPlayerHoldingItem()){
                            // if cant pickup inv curd, then job is done
                            if(!pickUpInvItem(mainInv,curdName)){
                                ZeeConfig.msg("all curds used");
                                break;
                            }
                        }
                        // if still holding curd next loop will check for empty trays

                        invCurds = mainInv.countItemsByNameEquals(curdName);
                        freeTrays = trayInv.getItemsByNameEnd("/cheesetray");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                ZeeConfig.removePlayerText();
            }
        }.start();
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
        //TODO add other names
        final String[] axeName = new String[]{"woodsmansaxe","axe-m","butcherscleaver","tinkersthrowingaxe","stoneaxe"};
        for (int i = 0; i < axeName.length; i++) {
            if (isItemInHandSlot(axeName[i]))
                return;
            WItem axe = getBeltOrInvWItem(axeName[i]);
            if (axe!=null){
                equipBeltOrInvItemThreadJoin(axeName[i]);
                waitItemInHand(axeName[i]);
                return;
            }
        }
    }

    public static WItem getSackFromBelt() {
        WItem ret = getBeltOrInvWItem("travellerssack");
        if (ret==null)
            ret = getBeltOrInvWItem("bindle");
        return ret;
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
            autoButch(wItem);
        }
        else if(petalName.equals(ZeeFlowerMenu.STRPETAL_AUTO_BUTCH_ALL))
        {
            autoButchAll(wItem);
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
                    ZeeConfig.gameUI.ui.msg(clickAllItemsPetal(items, "Kill") + " cocoons clicked");
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
                ZeeConfig.gameUI.ui.msg(items.size() + " noms");
            }
        }
        else if(petalName.contentEquals("fill all trays")){
            fillCheesetrays(wItem);
        }
        else if(petalName.contentEquals(ZeeFlowerMenu.STRPETAL_BINDWATER)){
            new ZeeThread(){
                public void run() {
                    try {
                        Inventory inv = getItemInventory(wItem);
                        List<WItem> items = inv.getWItemsByNameEndsWith(itemName);
                        // bind items to shotcut bar 9/9
                        //      from last slot(117) to first slot(108)
                        int i;
                        for (i=117; i>=108 && !items.isEmpty(); i--){
                            ZeeConfig.addPlayerText("set belt "+items.size());
                            WItem it = items.remove(0);
                            if(pickUpItem(it)) {
                                ZeeConfig.gameUI.wdgmsg("setbelt", i, 0);
                                sleep(333);
                                if(!dropHoldingItemToInv(inv)){
                                    println("couldnt return item to inv");
                                    ZeeConfig.removePlayerText();
                                    return;
                                }
                            }
                        }
                        // bind water bucket if slot available
                        if (i > 108){
                            WItem bucket = getEquippedItemNameEndsWith("/bucket-water");
                            println("bucket = "+bucket);
                            if (bucket != null){
                                ZeeConfig.addPlayerText("set bucket");
                                if(pickUpItem(bucket)) {
                                    ZeeConfig.gameUI.wdgmsg("setbelt", i, 0);
                                    sleep(333);
                                    if(!equipEmptyHand()){
                                        println("couldnt return bucket to hand");
                                        ZeeConfig.removePlayerText();
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ZeeConfig.removePlayerText();
                }
            }.start();
        }
        else
        {
            println("chooseItemFlowerMenu > unknown case");
        }
    }

    private static void autoButchExit(String msg){
        println("auto butch > "+msg);
        autoButchExit();
    }

    private static void autoButchExit(){
        autoButching = false;
        ZeeConfig.removePlayerText();
    }

    private static boolean autoButching = false;
    private static void autoButch(WItem wItem) {

        // fix method being called twice for some unknown reason
        if (autoButching){
            //println("autoButch > already busy");
            return;
        }
        autoButching = true;

        new ZeeThread() {
            public void run() {
                try {
                    ZeeConfig.addPlayerText("autobutch");

                    //start
                    WItem item = wItem;
                    Inventory inv = getItemInventory(item);
                    Coord itemSlotCoord = getWItemCoord(item);
                    String itemName = getWItemName(item);
                    final long sleepMs = 500;

                    prepareCancelClick();

                    while (!isCancelClick() && item!=null && (!(itemName.endsWith("-clean") || itemName.endsWith("-cleaned"))) ){
                        //click first menu petal
                        if (clickItemPetal(item,0)) {
                            sleep(sleepMs);
                            // get next stage item, ends with "-dead", "-plucked", "-clean" or "-cleaned"
                            item = inv.getItemBySlotCoord(itemSlotCoord);//TODO empty slot may change 1-slot-item position
                            itemName = getWItemName(item);
                        }else{
                            throw new Exception("couldn't auto-click item "+itemName);
                        }
                    }

                    // last item click
                    if (!isCancelClick() && item!=null && (itemName.endsWith("-clean") || itemName.endsWith("-cleaned"))) {
                        println("last butch > " + itemName);
                        clickItemPetal(item,0);
                    }

                }catch (Exception e){
                    println("autoButch exception > "+e.getMessage());
                }
                autoButchExit("done");
            }
        }.start();
    }

    private static void autoButchAll(WItem wItem) {

        // fix method being called twice for some unknown reason
        if (autoButching){
            //println("autoButch > already busy");
            return;
        }
        autoButching = true;

        new ZeeThread() {
            public void run() {
                try {
                    ZeeConfig.addPlayerText("autobutch");

                    WItem item = wItem;
                    Inventory inv = getItemInventory(item);
                    Coord itemSlotCoord = getWItemCoord(item);
                    String itemName = getWItemName(item);
                    String firstItemName = itemName;
                    final long sleepMs = 500;

                    prepareCancelClick();

                    while (!isCancelClick() && item!=null && (!(itemName.endsWith("-clean") || itemName.endsWith("-cleaned"))) ){

                        //click first menu petal
                        if (clickItemPetal(item,0)) {
                            sleep(sleepMs);
                            // get next stage item, ends with "-dead", "-plucked", "-clean" or "-cleaned"
                            item = inv.getItemBySlotCoord(itemSlotCoord);//TODO empty slot may change 1-slot-item position
                            itemName = getWItemName(item);
                        }else{
                            throw new Exception("failed autoclick 1 "+itemName);
                        }

                        //if butch is over("-clean"), prepare next "butch all" item
                        if (item!=null && (itemName.endsWith("-clean") || itemName.endsWith("-cleaned"))){

                            //butch "-clean" item and wait inventory changes
                            //println("last butch 2> "+itemName);
                            if(clickItemPetal(item,0)){
                                sleep(sleepMs);

                                //get next dead/live animal for butching
                                List<WItem> items;
                                if (firstItemName.contains("/rabbit-"))
                                    items = inv.getWItemsByNameContains("gfx/invobjs/rabbit-");
                                else if (firstItemName.endsWith("/hen") || firstItemName.endsWith("/rooster"))
                                    items = inv.getItemsByNameEnd("/hen","/rooster");
                                else
                                    items = inv.getWItemsByNameEndsWith(firstItemName);

                                //filter animal hides
                                items.removeIf(wItem1->ZeeConfig.isAnimalHideTailEtc(wItem1.item.getres().name));

                                if (items.size() == 0){
                                    //no more items to butch
                                    autoButchExit("no more items");
                                    return;
                                }else{
                                    //update next dead/live animal vars
                                    item = items.get(0);
                                    itemName = getWItemName(item);
                                    itemSlotCoord = getWItemCoord(item);
                                    println("next item > "+itemName);
                                }
                            }else{
                                throw new Exception("failed autoclick 2 "+itemName);
                            }
                        }
                    }

                    // last item click
                    if (!isCancelClick() && item!=null && (itemName.endsWith("-clean") || itemName.endsWith("-cleaned"))) {
                        println("last butch > " + itemName);
                        clickItemPetal(item,0);
                    }


                }catch (Exception e){
                    println("autoButchAll exception > "+e.getMessage());
                }
                autoButchExit("done");
            }
        }.start();
    }


    public static boolean sameNameAndQuality(WItem w1, WItem w2) {
        boolean ret = false;
        if (w1.item.getres().name.contentEquals(w2.item.getres().name)){
            if (w1.item.getInfoQuality() == w2.item.getInfoQuality())
                ret = true;
        }
        return ret;
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


    public boolean showMenuLongClick(){

        if (!isLongClick())
            return false;

        if (isStackByAmount(wItem.item))
            return false;

        boolean showMenu = true;
        ZeeFlowerMenu menu = null;

        ArrayList<String> opts = new ArrayList<String>();//petals array
        Inventory inv = getItemInventory(wItem);

        //bind water to shortcuts
        if (isItemDrinkingVessel(itemName)){
            menu = new ZeeFlowerMenu(wItem, ZeeFlowerMenu.STRPETAL_BINDWATER);
        }
        // todo fish fix
        else if (ZeeConfig.isFish(itemName)) {
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

            int items;
            if (itemName.endsWith("/hen") || itemName.endsWith("/rooster"))
                items = inv.getItemsByNameEnd("/hen","/rooster").size();
            else if ((itemName.endsWith("/rabbit-buck") || itemName.endsWith("/rabbit-doe")))
                items = inv.getItemsByNameEnd("/rabbit-buck","/rabbit-doe").size();
            else
                items = inv.countItemsByNameContains(itemName);

            if (items > 1){
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
        else if (ZeeConfig.getCursorName().equals(ZeeConfig.CURSOR_EAT)){
            menu = new ZeeFlowerMenu(wItem, ZeeFlowerMenu.STRPETAL_EATALL);
        }
        // fill cheese trays
        else if(itemName.endsWith("/cheesetray") && getHoldingItemName().contains("/curd-")){
            opts.add("fill all trays");
            opts.add(ZeeFlowerMenu.STRPETAL_TRANSFER_ASC);
            opts.add(ZeeFlowerMenu.STRPETAL_TRANSFER_DESC);
            menu = new ZeeFlowerMenu(wItem, opts.toArray(String[]::new));
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
            ZeeConfig.gameUI.ui.msg("takeAllInvItems: "+e.getMessage());
        }
    }

    static boolean clickingAllItemsPetals = false;
    public static int clickAllItemsPetal(List<WItem> items, String petalName) {
        if (clickingAllItemsPetals){
            println("alredy clicking all items petal = "+petalName);
            return 0;
        }
        clickingAllItemsPetals = true;
        ZeeConfig.addPlayerText("clicking "+items.size()+" items");
        int itemsClicked = 0;
        int countNoMenu = 0;
        prepareCancelClick();
        for (WItem w: items) {
            try {
                if (ZeeConfig.isCancelClick()) {
                    //ZeeClickGobManager.resetClickPetal();
                    ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
                    clickingAllItemsPetals = false;
                    return itemsClicked;
                }
                itemActCoord(w);
                FlowerMenu fm = waitFlowerMenu();
                if(fm!=null){
                    if(!choosePetal(fm, petalName))
                        break; // no petal found
                    itemsClicked++;
                    sleep(100);//delay
                }else{
                    countNoMenu++;
                    println("clickAllItemsPetal > no flowermenu "+countNoMenu+"/"+items.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
                //ZeeClickGobManager.resetClickPetal();
                ZeeConfig.removeGobText(ZeeConfig.getPlayerGob());
                clickingAllItemsPetals = false;
                return itemsClicked;
            }
        }
        ZeeConfig.removePlayerText();
        clickingAllItemsPetals = false;
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
        List<Window> list = ZeeConfig.getContainersWindows(true);
        //println("isTransferWindowOpened > "+list.size()+" windows > "+list.stream().map(window -> window.cap).collect(Collectors.joining(" , ")));
        return list.size() > 0;
    }

    static boolean isTransferWindowOpened(List<String> excludeWindowsNamed) {
        List<Window> openWindows = ZeeConfig.getContainersWindows(true);
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
        return isTransferWindowOpened(List.of("Quiver","Creel","Wicker Picker"));
    }

    private void drinkFrom() {
        //ZeeClickGobManager.scheduleClickPetalOnce("Drink");
        //itemAct(wItem);
        clickItemPetal(wItem,"Drink");
    }

    public static boolean clickItemPetal(WItem wItem, String petalName) {
        if (wItem==null){
            println(">clickItemPetal wItem null, petal "+petalName);
            return false;
        }
        itemActCoord(wItem);
        FlowerMenu fm = waitFlowerMenu();
        if(fm!=null){
            //println("clickItemPetal > flower menu");
            choosePetal(fm, petalName);
            return waitNoFlowerMenu();
        }else{
            //println("clickItemPetal > no flower menu");
            return false;
        }
    }

    public static boolean clickItemPetal(WItem wItem, int petal) {
        if (wItem==null){
            println(">clickItemPetal wItem null , petal "+petal);
            return false;
        }
        itemActCoord(wItem);
        FlowerMenu fm = waitFlowerMenu();
        if(fm!=null){
            //println("clickItemPetal > flower menu");
            choosePetal(fm, petal);
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
        String[] items = {"waterskin","bucket-","kuksa","woodencup","glassjug","waterflask","tankard","metalmug","winebottle","wineglass","leafcup-full"};
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
            //return false;
            inv = ZeeConfig.getMainInventory(); // TODO fix fitting item to inventory
        }else{
            inv = ZeeManagerItems.getInvBelt();
        }
        return dropHoldingItemToInv(inv);
    }

    public static boolean dropHoldingItemToInv(Inventory inv) {
        if(!ZeeConfig.isPlayerHoldingItem() || inv==null)
            return false;
        try{
            String windowTitle = inv.getparent(Window.class).cap;
            // drop to belt inv
            if (windowTitle.contentEquals("Belt")){
                List<Coord> freeSlots = inv.getFreeSlots();
                if (freeSlots.size()==0)
                    return false;//inv full
                Coord c = freeSlots.get(0);
                inv.wdgmsg("drop", c);
                return waitNotHoldingItem();
            }
            // drop to non belt inv
            WItem holdingItem = getHoldingItem();
            Coord itemSize = holdingItem.sz.div(Inventory.sqsz);
            Coord topLeftSlot = inv.getFreeSlotAreaSized(itemSize.x,itemSize.y);
            if (topLeftSlot==null) {
                println("dropHoldingItemToInv > topLeftSlot null");
                return false;
            }
            inv.wdgmsg("drop",topLeftSlot);
            sleep(PING_MS*4);
            return !ZeeConfig.isPlayerHoldingItem();//waitHoldingItemChanged();//waitNotHoldingItem();
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
        return name.endsWith("woodsmansaxe") || name.endsWith("axe-m") || name.endsWith("butcherscleaver") || name.endsWith("tinkersthrowingaxe") || name.endsWith("stoneaxe") || name.endsWith("shears");
    }

    public static boolean isItemButchingTool(WItem wItem){
        return isItemButchingTool(wItem.item.getres().name);
    }
    public static boolean isItemButchingTool(String itemName){
        String endlist = "woodsmansaxe,axe-m,butcherscleaver,tinkersthrowingaxe,stoneaxe,fyrdsword,hirdsword,bronzesword,b12axe,cutblade,shears";
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
                +"seed-grape,seed-greenkale,seed-hemp,seed-leek,leek,seed-lettuce,seed-millet,"
                +"seed-pipeweed,seed-poppy,seed-pumpkin,seed-wheat,seed-turnip,turnip,"
                +"seed-wheat,seed-barley,beetroot,yellowonion,redonion,garlic,"
                +"peapod,peppercorn,hopcones";
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
    public static Inventory getMainInv(){
        return ZeeConfig.getMainInventory();
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
            WItem witem=null;
            List<WItem> list;
            for (int i = 0; i < names.length; i++) {
                list = inv.getWItemsByNameContains(names[i]);
                if (list.isEmpty()) {
                    witem = null;
                    continue;
                }
                //found item with name[i]
                witem = list.get(0);
                // if is a stack, select from it
                if (witem.item.contents!=null){
                    ItemStack itemStack = (ItemStack) witem.item.contents;
                    witem = itemStack.wmap.values().iterator().next();
                }
                break;
            }
            // found no item with listed names
            if (witem==null) {
                println("found no item with listed names");
                return false;
            }
            // pickup found item
            return pickUpItem(witem);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static WItem getBeltOrInvWItem(String nameContains) {
        try {
            WItem witem;
            // belt
            if (getInvBelt()!=null) {
                witem = getInvBelt().getWItemsByNameContains(nameContains).get(0);
                return witem;
            }
            // no belt
            witem = getMainInv().getWItemsByNameContains(nameContains).get(0);
            return witem;
        }catch (Exception e){
            println("getBeltOrInvWItem > "+e.getMessage());
            return null;
        }
    }

    public static boolean isItemEquipped(String ... nameContains){
        try {
            GItem items[] = getEquipory().wmap.keySet().toArray(new GItem[]{});
            for (int i = 0; i < items.length; i++) {
                for (int j = 0; j < nameContains.length; j++) {
                    if (items[i].getres().name.contains(nameContains[j]))
                        return true;
                }
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

    public static List<WItem> getEquippedItemsNameEndsWith(String ... nameEndsWith) {
        WItem[] items = getEquipory().children(WItem.class).toArray(new WItem[]{});
        List<WItem> ret = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            for (String name : nameEndsWith) {
                if (items[i].item.getres().name.endsWith(name)){
                    ret.add(items[i]);
                    break;
                }
            }
        }
        return ret;
    }

    public static boolean isItemInHandSlot(String nameContains){
        try {
            /*
            Equipory eq = ZeeConfig.windowEquipment.getchild(Equipory.class);
            return eq.leftHand.item.getres().name.contains(name)
                    || eq.rightHand.item.getres().name.contains(name);
             */
            return getLeftHandName().contains(nameContains) || getRightHandName().contains(nameContains);
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

    public static void equipBeltOrInvItemThreadJoin(String name) {
        if(ZeeManagerItems.isItemInHandSlot(name)) {
            return;
        }
        WItem item = ZeeManagerItems.getBeltOrInvWItem(name);
        if (item!=null) {
            try {
                //use equipManager logic
                Thread t = new ZeeManagerItems(item);
                t.start();
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else
            println("itemManager.equipBeltOrInvItem() > item '"+name+"' not found");
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
        }else {
            println("contents null? try picking  up item first");
            msg = "error";
        }
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
            println("getItemInfoName > "+e.getMessage());
        }
        return("");
    }

    public static int getItemInfoAmount(List<ItemInfo> info) {
        try {
            for (ItemInfo v : info) {
                if (v instanceof GItem.Amount) {
                    return ((GItem.Amount)v).itemnum();
                }
            }
        }catch (Exception e){
            println("getItemInfoAmount > "+e.getMessage());
        }
        return 0;
    }

    public static ItemInfo getItemInfoByClassSimpleName(List<ItemInfo> info, String classSimpleName) {
        try {
            for (ItemInfo v : info) {
                if (v.getClass().getSimpleName().contentEquals(classSimpleName)) {
                    return v;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return(null);
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

    public static List<String> getItemInfoClasses(List<ItemInfo> info) {
        List<String> ret = new ArrayList<>();
        try {
            for (ItemInfo v : info) {
                ret.add(v.getClass().getName());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return(ret);
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

        boolean shift = ZeeConfig.gameUI.ui.modshift;

        new ZeeThread(){
            public void run() {
                drinkThreadWorking = true;
                boolean drank = false;
                Inventory inv;
                try {
                    double stam1 = ZeeConfig.getMeterStamina();

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
                            if (isItemDrinkingVessel(name)) {
                                String contents = getItemContentsName(invItems[i]);// "3.00 l of Water"
                                if (contents.contains("Water")) {
                                    if (clickItemPetal(invItems[i], "Drink")) {
                                        drank = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // drink from equips
                    if (!drank) {
                        List<WItem> items = getEquippedItemsNameEndsWith("bucket-water", "bucket-tea", "waterskin", "glassjug-full");
                        for (WItem item : items) {
                            String contents = getItemContentsName(item);// "3.00 l of Water"
                            if (contents.contains("of Water")) {
                                if (clickItemPetal(item, "Drink")) {
                                    drank = true;
                                    break;
                                }
                            }
                        }
                    }

                    // if shift down drink 1 gulp and click target coord again
                    // TODO consider minimap, gob clicks
                    if (drank && shift && ZeeConfig.playerHasAttr("LinMove")){
                        Coord2d destCoord = Coord2d.of(ZeeConfig.lastMapViewClickMc.x,ZeeConfig.lastMapViewClickMc.y);
                        double stam2 = ZeeConfig.getMeterStamina();
                        double stamGains = 0;
                        double stamGulp = 10;
                        if (stam2 + stamGulp < 100) {
                            //println("stam1 = " + stam1 + "  ,  stam2 " + stam2 );
                            prepareCancelClick();
                            do {
                                sleep(50);
                                stam2 = ZeeConfig.getMeterStamina();
                                stamGains = stam2 - stam1;
                            } while (!isCancelClick() && stamGains < stamGulp);
                            println("gulp > stam  "+stam2+" ,  gains " + stamGains);
                            // click destination coord again if didn't change
                            if (destCoord.compareToFixMaybe(ZeeConfig.lastMapViewClickMc)==0){
                                ZeeConfig.clickCoord(destCoord.floor(OCache.posres),1);
                            }
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                drinkThreadWorking = false;
            }
        }.start();
    }

    public static boolean isCoracleEquipped() {
        return ZeeManagerItems.isItemEquipped("gfx/invobjs/small/coracle");
    }

    public static boolean isStackByContent(GItem item) {
        return item.contents!=null;
    }

    public static boolean isStackByAmount(GItem i) throws Loading {
        try {
            if (i.getres().basename().startsWith("seed-"))
                return false;
            if (getItemInfoAmount(i.info()) > 0)
                return true;
        }catch(Loading l){
        } catch(Exception e){
            println("isStackByAmount > "+e.getMessage());
        }
        return false;
    }

    public static boolean isStackByKeyPagina(GItem i) throws Loading {
        try {
            if (i.getres().basename().startsWith("seed-"))
                return false;
            List<ItemInfo> info = i.info();
            if (getItemInfoByClassSimpleName(info,"KeyPagina")!=null
                    && getItemInfoAmount(info) > 0)
                return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void undoStack(GItem i) {
        try {
            gItemActCoord(i,3);
            playFeedbackSound();
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
                        if (!isStackByAmount(wItem.item))
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

    static boolean isTwoHandedItemEquippable(String beltItemNameContains) {

        Inventory invBelt = getInvBelt();
        if (invBelt.countItemsByNameContains(beltItemNameContains) == 0)
            return false;

        if (cannotUnequipSack())
            return false;

        int freeBeltSlots = invBelt.getNumberOfFreeSlots();
        int handItems = getHandItemsCount();
        if (freeBeltSlots==0 && handItems==2)
            return false;

        // items is equippable, maybe
        return true;
    }


    // TODO check mixed bindle/sack
    static boolean cannotUnequipSack() {

        Inventory mainInv = ZeeConfig.getMainInventory();

        // bindles
        if (getRightHandName().endsWith("/bindle")){
            // bindle in rightHand, check if last row occupied
            for (WItem item : mainInv.children(WItem.class)) {
                //println("    "+item.item.getres().basename()+" >  c"+item.c.div(33) +"  sz"+ item.sz.div(33) +"  invsz"+ mainInv.isz);
                if (item.c.div(33).x + item.sz.div(33).x == mainInv.isz.x){
                    println("last row occupied, can't unnequip bindle");
                    return true;
                }
            }
        } else if (getLeftHandName().endsWith("/bindle")){
            // bindle in leftHand, check if last col occupied
            for (WItem item : mainInv.children(WItem.class)) {
                //println("    "+item.item.getres().basename()+" >  c"+item.c.div(33) +"  sz"+ item.sz.div(33) +"  invsz"+ mainInv.isz);
                if (item.c.div(33).y + item.sz.div(33).y == mainInv.isz.y){
                    println("last col occupied, can't unnequip bindle");
                    return true;
                }
            }
        }

        //traveller sacks
        else if(getLeftHandName().endsWith("travellerssack") || getRightHandName().endsWith("travellerssack")){
            // check if last row or last col is occupied
            for (WItem item : mainInv.children(WItem.class)) {
                //println("    "+item.item.getres().basename()+" >  c"+item.c.div(33) +"  sz"+ item.sz.div(33) +"  invsz"+ mainInv.isz);
                if (item.c.div(33).y + item.sz.div(33).y == mainInv.isz.y || item.c.div(33).x + item.sz.div(33).x == mainInv.isz.x){
                    //println("last col/row occupied, can't unnequip traveller sack");
                    return true;
                }
            }
        }

        return false;
    }

    static int getHandItemsCount(){
        int handItems = 0;
        if (!getLeftHandName().isBlank())
            handItems++;
        if (!getRightHandName().isBlank())
            handItems++;
        // two handed item
        if (handItems==2 && getLeftHandName().contentEquals(getRightHandName()))
            handItems--;
        return handItems;
    }

    static int getFreeHandsCount(){
        int free = 0;
        if (getLeftHandName().isBlank())
            free++;
        if (getRightHandName().isBlank())
            free++;
        return free;
    }


    public static boolean craftIngrRequiresConfirmation() {

        final List<String> nameListEndsWith = List.of(
                "silkcloth","goldencloth","erminecloth","ratcloth","/felt",
                "-silver","-gold","-steel","-rosegold",
                "clay-cave","clay-soap","clay-pit","clay-bone",
                "stargem",
                "beetweird","-crying",
                "yarn-goat","yarn-sheep" // used as string
        );

        // main inv
        List<WItem> items = getMainInv().getItemsSelectedForCrafting();
        for (WItem item : items) {
            for (String s : nameListEndsWith) {
                if (item.item.getres().name.endsWith(s))
                    return true;
            }
        }

        // open containers invs
        List<Window> openWins = ZeeConfig.getContainersWindows(false);
        for (Window win : openWins) {
            Inventory inv = win.getchild(Inventory.class);
            if (inv==null)
                continue;
            List<WItem> itemsConts = inv.getItemsSelectedForCrafting();
            for (WItem item : itemsConts) {
                for (String s : nameListEndsWith) {
                    if (item.item.getres().name.endsWith(s))
                        return true;
                }
            }
        }
        return false;
    }


    static List<String> cheeseProgressList = new ArrayList<>(Utils.getprefsl("cheeseProgressList",new String[]{}));
    public static void checkCheeseTray(Window window) {
        new ZeeThread(){
            public void run() {
                try {
                    sleep(500);
                    Inventory inv = window.getchild(Inventory.class);
                    if (inv==null) {
                        println("checkCheeseTray > inv null");
                        return;
                    }
                    List<WItem> cheesetrayList = inv.getWItemsByNameContains("cheesetray");
                    if (cheesetrayList.isEmpty())
                        return;
                    WItem firstCheesetray = cheesetrayList.get(0);
                    Double meter = (firstCheesetray.item.meter > 0) ? Double.valueOf(firstCheesetray.item.meter / 100.0) : firstCheesetray.itemmeter.get();
                    // cheese is progressing
                    if (meter!=null) {
                        String newCheesePerc = ((int) (meter * 100)) + "%";
                        String newCheeseName = getItemContentsName(firstCheesetray);
                        String newCheeseLocation = "";
                        if (ZeeConfig.playerLocation == ZeeConfig.LOCATION_CELLAR)
                            newCheeseLocation = "cellar";
                        else if (ZeeConfig.playerLocation == ZeeConfig.LOCATION_CABIN )
                            newCheeseLocation = "cabin";
                        else if (ZeeConfig.playerLocation == ZeeConfig.LOCATION_UNDERGROUND)
                            newCheeseLocation = "mines";
                        else if(ZeeConfig.playerLocation == ZeeConfig.LOCATION_OUTSIDE)
                            newCheeseLocation = "outside";
                        else {
                            println("checkCheeseTray > couldnt determine player location");
                            return;
                        }

                        // add/update saved cheese
                        String newCheeseDateMs = String.valueOf(new Date().getTime());
                        boolean isNewCheese = true;
                        for (int i = 0; i < cheeseProgressList.size(); i++) {

                            // format "cheesename,progress,location,cheeseDateMs"
                            String[] arr = cheeseProgressList.get(i).split(",");

                            // cheese already in list (name+location) //TODO improve identification
                            if (newCheeseName.contentEquals(arr[0]) && newCheeseLocation.contentEquals(arr[2])) {
                                isNewCheese = false;
                                // update existing cheese percentage
                                arr[1] = newCheesePerc;
                                // reset last seen date
                                arr[3] = newCheeseDateMs;
                                cheeseProgressList.set(i, String.join(",", arr));
                            }
                        }

                        // add new cheese
                        if (isNewCheese) {
                            cheeseProgressList.add(newCheeseName+","+newCheesePerc+","+newCheeseLocation+","+newCheeseDateMs);
                        }

                        // save pref
                        Utils.setprefsl("cheeseProgressList", cheeseProgressList);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    static void cheeseTrayMakeWindow(Window window){
        if (!ZeeManagerItems.cheeseProgressList.isEmpty()){
            List<String> allCheese = ZeeManagerItems.cheeseProgressList;
            int buttonsX = 0 , buttonsY = 0;
            Label lbl;
            for (int i = 0; i < allCheese.size(); i++) {
                // format "cheesename,progress,location,cheeseDateMs"
                String[] arr = allCheese.get(i).split(",");
                long timeElapsed = new Date().getTime() - Long.parseLong(arr[3]);
                int labelY = (i*13)-10;
                // add cheese label
                lbl = window.add(new Label("["+i+"] "+arr[0]+" , "+arr[1]+" , "+arr[2]+" , "+getDurationXUnitsAgo(timeElapsed)),85,labelY);
                if ( i == 0 ){
                    buttonsX = lbl.c.x + lbl.sz.x;
                    buttonsY = labelY;
                }
                // add remove button
                window.add(new ZeeWindow.ZeeButton(15,""+i,"remove "+arr[0]){
                    public void wdgmsg(String msg, Object... args) {
                        if (msg.contentEquals("activate")){
                            if (!ui.modctrl){
                                ZeeConfig.msgError("Ctrl+click to confirm remove");
                                return;
                            }
                            //remove cheese
                            String cheeseNameToRemove = ((KeyboundTip)this.tooltip).base.replace("remove ","");
                            if(ZeeManagerItems.cheeseProgressList.removeIf(s -> s.startsWith(cheeseNameToRemove))) {
                                Utils.setprefsl("cheeseProgressList", ZeeManagerItems.cheeseProgressList);
                                ZeeConfig.gameUI.menu.wdgmsg("act","craft","cheesetray",0);
                            }else{
                                println("couldnt remove cheese "+cheeseNameToRemove);
                            }
                        }
                    }
                }, buttonsX+7+(i*18), buttonsY-5);
            }
        }
    }
    static String getDurationXUnitsAgo(long durationMs) {
        final List<Long> times = Arrays.asList(
                //TimeUnit.DAYS.toMillis(365),   // 1year ms
                //TimeUnit.DAYS.toMillis(30),    // 1month ms
                //TimeUnit.DAYS.toMillis(1),     // 1day ms
                TimeUnit.HOURS.toMillis(1),    // 1hour ms
                TimeUnit.MINUTES.toMillis(1),  // 1min ms
                TimeUnit.SECONDS.toMillis(1) );// 1sec ms
        //final List<String> timesString = Arrays.asList("yr","mt","day","hr","min","sec");
        final List<String> timesString = Arrays.asList("hr","min","sec");

        StringBuffer res = new StringBuffer();
        for(int i=0;i< times.size(); i++) {
            Long current = times.get(i);
            long temp = durationMs/current;
            if(temp>0) {
                res.append(temp).append(" ").append( timesString.get(i) ).append(temp != 1 ? "s" : "").append(" ago");
                break;
            }
        }
        if("".equals(res.toString()))
            return "0 secs ago";
        else
            return res.toString();
    }

    public static final Resource resSoundWood2 = Resource.local().loadwait("sfx/hud/mmap/wood2");
    public static void playFeedbackSound() {
        ZeeConfig.gameUI.ui.sfx(resSoundWood2);
    }
}

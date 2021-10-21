package haven;

import java.util.Arrays;
import java.util.List;

public class ZeeEquipManager extends Thread{

    static long SLEEP_MS = 77;
    static long PING_MS = 333;
    static long TIMEOUT_MS = 2000;
    private final WItem wItem;
    String itemName;
    String leftHandItemName, rightHandItemName, itemSourceWindow;
    Equipory equipory;
    Inventory invBelt = null;




    public ZeeEquipManager(WItem wItem) {
        this.wItem = wItem;
        itemName = wItem.item.getres().name;
        equipory = ZeeConfig.windowEquipment.getchild(Equipory.class);
        leftHandItemName = (equipory.leftHand==null ? "" : equipory.leftHand.item.getres().name);
        rightHandItemName = (equipory.rightHand==null ? "" : equipory.rightHand.item.getres().name);
        itemSourceWindow = wItem.getparent(Window.class).cap.text;//save source window name before pickup
        //System.out.println(itemName +" , "+ leftHandItemName +" , "+ rightHandItemName);
    }


    @Override
    public void run() {
        try{

            if(ZeeConfig.getWindow("Belt")==null){
                ZeeConfig.gameUI.msg("no belt window");
                return;
            }
            if(ZeeConfig.getWindow("Equipment")==null){
                ZeeConfig.gameUI.msg("no equips window");
                return;
            }

            if (isItemSack()) {

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

            }else if(isTwoHandedItem()) {

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
                        if (getInvBelt().getNumberOfFreeSlots() > 0) {
                            unequipLeftItem();//unequip 1st item
                            if(trySendItemToBelt()){
                                //switch for remaining item
                                pickUpItem();
                                equipAnyHand();
                                trySendItemToBelt();
                            }
                        }
                    }
                }else if(isSourceEquipsWindow()) {
                    if (getInvBelt().getNumberOfFreeSlots() > 0) {
                        //send to belt if possible
                        pickUpItem();
                        trySendItemToBelt();
                    }
                }

            }else{// 1handed item

                if(isSourceBeltWindow()) { // send to equipory
                    if(isLeftHandEmpty() || isRightHandEmpty()) {
                        pickUpItem();
                        equipEmptyHand();
                    }else { // both hands occupied
                        if(!isItemSack(leftHandItemName)) {
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
                    if(!trySendItemToBelt())
                        equipAnyHand();//belt full?
                }

            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isTwoHandedItem() {
        if(isTwoHandedItem(itemName))
            return true;
        return false;
    }

    private boolean isTwoHandedItem(String name) {
        return ZeeConfig.isTwoHandedItem(name);
    }

    private void equipAnyHand() throws InterruptedException {
        equipory.wdgmsg("drop",-1);//server decide
        Thread.sleep(PING_MS);
    }

    private boolean trySendItemToBelt() {
        try{
            List<Coord> freeSlots = getInvBelt().getFreeSlots();
            if (freeSlots.size()==0)
                return false;//belt full
            Coord c = freeSlots.get(0);
            getInvBelt().wdgmsg("drop", c);
            waitFreeHand();
            return true;
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return false;
    }

    private void waitFreeHand() {
        int max = (int) TIMEOUT_MS;
        while(max>0 && ZeeConfig.gameUI.vhand!=null) {
            max -= SLEEP_MS;
            try { Thread.sleep(SLEEP_MS); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private void waitOccupiedHand() {
        int max = (int) TIMEOUT_MS;
        while(max>0 && ZeeConfig.gameUI.vhand==null) {
            max -= SLEEP_MS;
            try { Thread.sleep(SLEEP_MS); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    private Inventory getInvBelt() {
        if (invBelt==null) {
            Window w = ZeeConfig.getWindow("Belt");
            if(w!=null)
                invBelt = w.getchild(Inventory.class);
        }
        return  invBelt;
    }

    private boolean isHoldingItem() {
        return (ZeeConfig.gameUI.vhand != null);
    }

    private boolean isSourceBeltWindow() {
        return (itemSourceWindow.equalsIgnoreCase("Belt"));
    }

    private boolean isSourceEquipsWindow() {
        return (itemSourceWindow.equalsIgnoreCase("Equipment"));
    }

    private void equipLeftHand() {
        waitOccupiedHand();
        equipory.wdgmsg("drop", 6);
    }

    private void equipRightHand() {
        waitOccupiedHand();
        equipory.wdgmsg("drop", 7);
    }

    private void equipEmptyHand() throws InterruptedException {
        if(isLeftHandEmpty())
            equipLeftHand();
        else if(isRightHandEmpty())
            equipRightHand();
        Thread.sleep(PING_MS);
    }

    private boolean isLeftHandEmpty() {
        return (equipory.leftHand==null);
    }

    private boolean isRightHandEmpty() {
        return (equipory.rightHand==null);
    }

    private void pickUpItem() {
        wItem.item.wdgmsg("take", new Coord(wItem.sz.x / 2, wItem.sz.y / 2));
        waitOccupiedHand();
    }

    private void unequipLeftItem() {
        equipory.leftHand.item.wdgmsg("take", new Coord(equipory.leftHand.sz.x/2, equipory.leftHand.sz.y/2));
        waitOccupiedHand();
    }

    private void unequipRightItem() {
        equipory.rightHand.item.wdgmsg("take", new Coord(equipory.rightHand.sz.x/2, equipory.rightHand.sz.y/2));
        waitOccupiedHand();
    }

    private boolean isItemSack() {
        return isItemSack(itemName);
    }

    private boolean isItemSack(String name) {
        return name.endsWith("travellerssack") || name.endsWith("bindle");
    }
}

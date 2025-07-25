# Zeecow hafen-client

Client focusing on small helper tasks, many using mouse middle button, and other stuff:

Controls
--------
- left click ground cancel tasks whenever possible
- right click windows to close (DF style, minimap ignored for now)
- gob flowermenu also works on mouse up
- minimap drags with button 3
- keys up/down controls volume
- keys left/right controls ambient light (home key resets light)
- quick zoom with PageUp/PageDown
- camera drag also works by holding left-click (move player + drag camera)   
- shift+c alternates cams ortho/free
- shift+h toggle hitboxes
- ctrl+h toggle all trees/palisegs/crops (shows opt win)
  - midclick quick toggle tree/wall/non-ready-crop(farmtile,trellis) 
- mousewheel scroll transfer items directly (no shift required)
- Ctrl+click to confirm actions (Empty, Swill, Slaughter, etc)
- Ctrl confirm throwing axe/spear
- Ctrl+Rclick confirm remove shortcut
- Alt+click to drop holding item
- shift+tab toggles between belt, creel and basket (probably outdated)
- rclick Stoat's icon to aggro on max speed (option available)
- pickup key "q" 
  - ctrl+q show pickup window
  - shift+q pickup all server side (sim shift+rclick)
  - ctrl+shift+q pickup all client side (sequentially) 
- drink key '
  - shift drink will drink 1 gulp and click ground again (missing gob/minimap clicks)
- click building/passage gob to enter it
  - dismount horse if rope in inventory
  - schedule remount if mclick instead of rclick
- rclick tree tries collecting sap with lifted barrel
- rclick workstations to auto dismount (req inventory rope)
- Icon Settings: shift+space toggles notify checkbox
- Search Actions single click 
  - left-click-hold search button shows Actions window, and select item on mouse up 
  - right-click-hold search button to start a hover menu
- mid-click tree/bush/boulder/log/stump/animal queue for next chop/chip/destroyLog/butch (not all cases)
- midclick obj to queue actions ("swrk", "destroy", "repair")
  - shift midclick obj to act on all objs
- shift midclick object toggle highlight object (:zeecow cmd)
- shift + area mine starts tunnel helper if area matches
- cancel auto clicking menu petal by holding special key (ctrl,shift,alt,super) 
- type ":zeecow" for small interactive commands 




Midclick interactions (incomplete/outdated)
-------------------------------------------

Midclick Ground
- cancel tasks whenever possible
- while holding item will move to location (simulates ctrl+click)
- while inspecting icon active, will msg tile name
- place stockpile and all items
- dig multiple ballclay if cursor dig
- while driving plow will queue coords 

Long Midclick Ground (LMC)
- while mounting horse will try to dismount
- while carrying wheelbarrow will try to(dismount and) unload stockpile at ground
- while coracle equipped, LMC water will try to drop and mount
- while coracle mounted, LMC ground will try to dismount and equip
- disembark dugout, rowboat, coracle, kicksled
- activate snow clear area
- show flowermenu for
  - inspect water tile using inventory woodencup
  - build road
  - actions fish, dig, mine
- place pile and start autopiling for blocks, boards

Midclick Ground Object
- inspect object quality and show text
- harvest 2 dreams from dreamcatch objects closeby
- Giddyup! if obj is horse
- lightup torch on firesource obj
- open "Cargo" on Knarr/Snekkja obj
- toggle barrels labels
- toggle mine support radius
- toggle aggressive animal obj radius
- while holding item will try to ctrl+click obj(containers,stockpiles)
- open cauldron
- place lifted treelog next to clicked treelog(perpendicular only)
- while crafting rope, midclicking fibre piles will get fibres and "Craft All" again
- midclick barterstand to open search window
- midclick smelter holding bucket to collect all quicksilver
- midclick stockpile while making block/board/sand will pile inv items and try acquire more
- midclick animal holding clover to follow and feed it when close enough
- midclick crop activate area harvest 
- midclick construction obj while chopping/sawing will build and get more mats
- midclick cart try picking first slot
- gob placer: midclick gob while lifting gob of same name will try to place next to it and save distance per gob name (temp window to clear distance)
- queue tree/bush/boulder/log/stump/animal for repeating actions like chop/chip/butch/destroyLog
- midclick obj to queue actions ("swrk", "destroy", "repair")
  - shift midclick obj to act on all objs

Long Midclick Ground Object
- show context menu for various objs (details below)
- lift up obj
- take all from stockpile obj
- click "Man the helm" on Knarr/Snekkja obj
- while holding item will try to store all on barrel obj
- add/remove tree obj to "Remove all trees" ordered queue
- add/remove treelog obj to "Destroy all" ordered queue
- while carrying wheelbarrow will try to(dismount and) unload at stockpile/container
- while driving wheelbarrow will lift it up and open gate obj
- while driving wheelbarrow will lift it up and place on cart obj
- while carrying/driving wheelbarrow will mount horse obj and lift wheelbarrow again
- show window for area piler
- pile all clay if clicked pile with cursor dig
- longmidclick stockpile while making block/board/sand will try pile items until pile full or object consumed
- put out cauldron
- longMidClick barrel while stirring to refill cauldron and click "craft all" again 
  - tries reopening aux containers before crafting again

Midclick Window Item
- sort-transfer item ascending(?)
- pick-up all
- quick-equip from belt
- seed-planting cursor
- while holding cheese-curd will fill up cheese-tray item
- undo stack item (ctrl+shift+Rclick) if transfer not available
- create single stack when hold and midclick same item type

Long Midclick Window Item
- sort-transfer item descending(?)
- show context menu for various items (details below)
- equip fishing hook/lure and recast
- equip two sack items from belt
- undo multiple stack items if transfer not available
- create multiple stacks when hold and midclick same item type

LongMidClick Flowermenu for Ground Objects
- auto butch dead animal objs
- start seed farmer for crop objs: harvest, store and replant seeds (no pathfinding)
- harvest area for crop objs: activate harvest icon (shift+click)
- add 4 branches to oven obj
- add 9/12 coal to smelter obj
- remove tree and stump
- remove trellis plant(s)
- destroy multiple treelogs (if bonesaw is equipped)
- player context menu
  - switch char
  - mine tiles window
  - test coords
  - emote wave, laugh
  - clear gob texts
- tamed animal context menu
  - open cattle roster
  - memorize area
- wildbeehive shows build pyre
- lightable objects shows craft firebrand, pyrite spark
- crucible smelt nuggets, bar
- get xp from well

LongMidClick Flowermenu for Window Items
- auto butch dead animal items
- kill all cocoon items
- feast all food items(requires table)
- transfer sort items (asc/desc)
- fill all cheesetrays if holding curd
- bind water items to last shortcut bar


Random Features
---------------
- opt Animated UI messages, roll msgs at screen center
- option midi radio folder for playing when cellar/cabin    
- solid colors option: minimap, terrain
- search actions works globally, instead of current menu
- sort actions by most used craft items (size limited for now)
- craft window's search ingredients button
- window buttons auto-organize duplicates, auto-hoverhide
- simple window and buttons
- compact equip window, auto toggle equip window
- shorter cattle roster window
- reposition map window when compacted/expanded
- basic inventory item counter
- tamed animal name generator, using animal's stats
- auto-pile helper for mulberry leaves, woodblocks, boards, coal, ground stones
- seed-farmer manager: no pathfinding, buggy (ctrlLongMidClick crop)
- cook manager: auto cook pepper and refill cauldron until barrel is empty(no pathfinding,old,buggy feature)
- icon list categs
- shaped icons options
- label trasfering barrels
- Window fuel buttons for Oven, Kiln, Smelter
- free gob placement option
- auto toggle gridlines
- Quick Options window show 3 latest used options, and latest flowermenu
- default icon sound to "Bell 2" if empty
- inspect cursor tooltip containing gob/tile/minimap info
- Barter Stand "allow mid-click auto-buy" checkbox
- Barter Stand button return branches to closest wooden chest
- Audio msg mute list
- lift vehicle before travel hearth
- midclick mark expands minimap and select mark
- new mark zooms in map
- area piler: create piles around selected area (long-midclick item)
- main inventory checkboxes for midclick transfer modes: asc, desc, one, ql
- gob monitor: highlight, play audio, text to speech (cmd festival)
- Feasting log window, count gains
- Option "Fish Moon XP" show text over calendar
- Scroll text entry fuel for kiln, oven, smelter
- hover menu on search actions button (hold right-click)
- auto-label gob Barrel/Cistern/Demijohn when it's window is opened
- auto craft helper: cloth linen/hemp, rope, bug collection
  - use closest fibre pile until inv full
- auto press wine from inventtory
  - switch seeds and press until it stops
- (unfinished) Lag Camera (:cam lag)
- autodrop mined items, seeds, soil
- auto click menu options
- auto drink
- sort transfer with Alt+Rclick or Alt+Lclick (no gemstone yet)
- Auto-hearth
- replaces arrow cursor with system's default
- show claims by default
- optional friend online notification
- craft window numbers, history, search ingredients
- highlight damaged gobs more
- highlight finished crops, growing trees, gobs and categories
- zoom extended for ortho cam
- mini trees
- hide smoke and some animations
- keybelt shortcuts navigation
- reposition rightmost windows horiz. when game resize
- CheeseTray recipe show progress of recently opened CheeseRack 
- try auto pick irrlight and craft again (craft window checkbox)
- move adjacent treelogs (mainInv checkbox)
- checkbox build and drink, if stamina low enough
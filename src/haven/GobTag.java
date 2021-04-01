package haven;

import java.util.HashSet;
import java.util.Set;

public enum GobTag {
    TREE, BUSH, LOG, STUMP, HERB,
    CREATURE,
    
    CRITTER,
    RAT, FROG, TOAD, LIZARD, GRASSHOPPER,
    BAT,
    
    DOMESTIC, YOUNG, ADULT,
    CATTLE, COW, BULL, CALF,
    GOAT, NANNY, BILLY, KID,
    HORSE, MARE, STALLION, FOAL,
    PIG, SOW, HOG, PIGLET,
    SHEEP, EWE, RAM, LAMB,
    
    MENU, PICKUP;
    
    public static Set<GobTag> tags(Gob gob) {
        Set<GobTag> tags = new HashSet<>();
        
        Resource res = gob.getres();
        if(res != null) {
            String name = res.name;
            
            if(name.startsWith("gfx/terobjs/trees")) {
                if(name.endsWith("log") || name.endsWith("oldtrunk")) {
                    tags.add(LOG);
                } else if(name.contains("stump")) {
                    tags.add(STUMP);
                } else {
                    tags.add(TREE);
                }
            } else if(name.startsWith("gfx/terobjs/bushes")) {
                tags.add(BUSH);
            } else if(name.startsWith("gfx/terobjs/herbs/")) {
                tags.add(HERB);
            } else if(name.startsWith("gfx/kritter/")) {
                tags.add(CREATURE);
                if(name.contains("/cattle/")) {
                    tags.add(CATTLE);
                    //TODO: add distinction between cow and bull
                    if(name.endsWith("/calf")) {
                        tags.add(CALF);
                    }
                } else if(name.contains("/goat/")) {
                    tags.add(GOAT);
                    if(name.endsWith("/billy")) {
                        tags.add(BILLY);
                    } else if(name.endsWith("/nanny")) {
                        tags.add(NANNY);
                    } else if(name.endsWith("/kid")) {
                        tags.add(KID);
                    }
                } else if(name.contains("/horse/")) {
                    tags.add(HORSE);
                    if(name.endsWith("/foal")) {
                        tags.add(FOAL);
                    } else if(name.endsWith("/mare")) {
                        tags.add(MARE);
                    } else if(name.endsWith("/stallion")) {
                        tags.add(STALLION);
                    }
                } else if(name.contains("/pig/")) {
                    tags.add(PIG);
                    if(name.endsWith("/hog")) {
                        tags.add(HOG);
                    } else if(name.endsWith("/piglet")) {
                        tags.add(PIGLET);
                    } else if(name.endsWith("/sow")) {
                        tags.add(SOW);
                    }
                } else if(name.contains("/sheep/")) {
                    tags.add(SHEEP);
                    //TODO: add distinction between ewe and ram
                    if(name.endsWith("/lamb")) {
                        tags.add(LAMB);
                    }
                } else if(name.endsWith("/rat")) {
                    tags.add(RAT);
                } else if(name.endsWith("/frog")) {
                    tags.add(FROG);
                } else if(name.endsWith("/toad")) {
                    tags.add(TOAD);
                } else if(name.endsWith("/grasshopper")) {
                    tags.add(GRASSHOPPER);
                } else if(name.endsWith("/forestlizard")) {
                    tags.add(LIZARD);
                } else if(name.endsWith("/bat")) {
                    tags.add(BAT);
                }
            }
            
            if(anyOf(tags, CRITTERS)) { tags.add(CREATURE); }
            if(anyOf(tags, DOMESTICATED)) { tags.add(DOMESTIC); }
            
            if(anyOf(tags, GobTag.HERB, GobTag.CRITTER)) {
                tags.add(PICKUP);
            }
            
            if(!anyOf(tags, GobTag.STUMP, GobTag.CREATURE) || anyOf(tags, GobTag.DOMESTIC)) {
                tags.add(MENU);
            }
        }
        
        return tags;
    }
    
    
    private static boolean anyOf(Set<GobTag> target, GobTag... tags) {
        for (GobTag tag : tags) {
            if(target.contains(tag)) {return true;}
        }
        return false;
    }
    
    private static final GobTag[] CRITTERS = {RAT, FROG, TOAD, LIZARD, GRASSHOPPER};
    private static final GobTag[] DOMESTICATED = {CATTLE, GOAT, HORSE, PIG, SHEEP};
}

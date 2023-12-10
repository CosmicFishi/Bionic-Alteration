package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.LazyLib;

import java.util.*;

/**
 * Handle how many limbs can a person have
 * A person always have their ANATOMY VARIANT(s), which is defined in a csv along with their generic's limbs
 */
public class ba_anatomymanager {
    static Logger log = Global.getLogger(ba_anatomymanager.class);
    static HashMap<String, List<String>> anatomyList = new HashMap<>();
    static List<String> anatomyVariantList = new ArrayList<>();
    protected static HashMap<String, ba_limb> limbMap = new HashMap<>();
    public static void onApplicationLoad() {
        loadAnatomyVariantList();
        loadLimbs();
    }
    //todo: change this to get from csv
    public static void loadAnatomyVariantList() {
        anatomyList.put("GENERIC_HUMAN", new ArrayList<String>(Arrays.asList("brain", "heart", "eye_left", "eye_right", "hand_left", "hand_right")));
    }
    //todo: change to get csv
    public static void loadLimbs() {
        limbMap.put("heart",new ba_limb("heart","Heart"));
        limbMap.put("hand_left",new ba_limb("hand_left","Left Hand"));
        limbMap.put("hand_right",new ba_limb("hand_right","Right Hand"));
        limbMap.put("eye_left",new ba_limb("eye_left","Left Eye"));
        limbMap.put("eye_right",new ba_limb("eye_right","Right Eye"));
        limbMap.put("brain",new ba_limb("brain","Brain"));
    }
    public static ba_limb getLimb(String id) {
        ba_limb section = limbMap.get(id);
//        log.info("getting limb: " + id);
        if(section == null) {
            log.error("Can not find bionic section of id: "+ id);
        }
        return section;
    }
    public static List<String> getListAnatomyKeys() {
        return new ArrayList<>(anatomyList.keySet());
    }
    /**
     * @param keys Person tags
     * @return the Generic Variants
     */
    public static List<String> getAnatomyVariantTag(Set<String> keys) {
        List<String> anatomyVariantList = getListAnatomyKeys();
        List<String> personVariant = new ArrayList<>();
        if(keys != null && !keys.isEmpty()) {
            for (String k: keys) {
                for(String variant: anatomyVariantList) {
                    if(variant.equals(k)) {
                        personVariant.add(k);
                    }
                }
            }
        }
        return personVariant;
    }
    //todo: change this so it will select from faction's random list
    public static String getRandomVariant() {
        WeightedRandomPicker<String> randomPicker = new WeightedRandomPicker<>();
        randomPicker.addAll(getListAnatomyKeys());
        return randomPicker.pick();
    }
    public static class ba_limb {
        public String bionicSectionId;
        public String name;
        public HashMap<String, Object> customData = new HashMap<>();
        public ba_limb(String bionicSectionId, String bionicName) {
            this.bionicSectionId = bionicSectionId;
            this.name = bionicName;
        }
    }
}

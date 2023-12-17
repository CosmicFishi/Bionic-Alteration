package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicSettings;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.io.IOException;
import java.util.*;

/**
 * Handle how many limbs can a person have
 * A person always have their ANATOMY VARIANT(s), which is defined in a csv along with their generic's limbs
 * Probably will have a .json file to store all the variant type
 * @author PigeonPun
 */
//todo: change variant manager so it can control which faction have what type of variant, what kind of bionic do they have.
public class ba_variantmanager {
    static Logger log = Global.getLogger(ba_variantmanager.class);
    static HashMap<String, List<String>> variantList = new HashMap<>();
//    protected static HashMap<String, ba_limb> limbMap = new HashMap<>();
    public static void onApplicationLoad() {
        loadAnatomyVariantList();
//        loadLimbs();
    }
    public static void loadAnatomyVariantList() {
//        variantList.put("GENERIC_HUMAN", new ArrayList<String>(Arrays.asList("brain", "heart", "eye_left", "eye_right", "hand_left", "hand_right")));
//        variantList.put("GENERIC_HALF_BORN", new ArrayList<String>(Arrays.asList("brain", "heart_2", "heart", "eye_left", "eye_right", "hand_left", "hand_right")));

        List<String> limbFiles = MagicSettings.getList(ba_variablemanager.BIONIC_ALTERATION, "variant_files");
        for (String path : limbFiles) {
            log.error("merging anatomy variant files");
            JSONArray variantData = new JSONArray();
            try {
                variantData = Global.getSettings().getMergedSpreadsheetDataForMod("variantId", path, ba_variablemanager.BIONIC_ALTERATION);
            } catch (IOException | JSONException | RuntimeException ex) {
                log.error("unable to read " + path, ex);
            }
            for (int i = 0; i < variantData.length(); i++) {
                try{
                    JSONObject row = variantData.getJSONObject(i);
                    String variantId = row.getString("variantId");
                    if(!Objects.equals(variantId, "")) {
                        List<String> limbIdList = ba_utils.trimAndSplitString(row.getString("limbIdList"));
                        variantList.put(variantId, new ArrayList<>(limbIdList));
                    }
                } catch (JSONException ex) {
                    log.error("Invalid line, skipping");
                }
            }
        }
//        for (Map.Entry<String, List<String>> entry: variantList.entrySet()) {
//            System.out.println(entry.getKey() + " " + Arrays.toString(entry.getValue().toArray()));
//        }
    }
    public static List<String> getListAnatomyKeys() {
        return new ArrayList<>(variantList.keySet());
    }
    /**
     * @param keys Person tags
     * @return the Generic Variant tags
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
}

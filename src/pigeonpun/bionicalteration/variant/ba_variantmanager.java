package pigeonpun.bionicalteration.variant;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicSettings;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.faction.ba_factiondata;
import pigeonpun.bionicalteration.faction.ba_factionmanager;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.io.IOException;
import java.util.*;

/**
 * Handle how many limbs can a person have
 * A person always have their ANATOMY VARIANT, which is defined in a csv along with their generic's limbs
 * @author PigeonPun
 */
public class ba_variantmanager {
    static Logger log = Global.getLogger(ba_variantmanager.class);
    public static HashMap<String, ba_variant> variantList = new HashMap<>();
//    protected static HashMap<String, ba_limb> limbMap = new HashMap<>();
    public static void onApplicationLoad() {
        loadAnatomyVariantList();
//        loadLimbs();
    }
    public static void loadAnatomyVariantList() {
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
                        variantList.put(variantId, new ba_variant(
                                row.getString("variantId"),
                                row.getString("variantName"),
                                limbIdList,
                                row.getBoolean("allowPlayerChangeTo")
                            ));
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
    public static ba_variant getVariant(String variantId) {
        ba_variant variant = variantList.get(variantId);
        if(variant == null) {
            log.error("Can not find variant of id: "+ variantId);
        }
        return variant;
    }
    public static List<String> getListLimbFromVariant(String variantId) {
        List<String> limbList = new ArrayList<>();
        if (!variantList.isEmpty() &&  variantList.get(variantId) != null) {
            limbList = variantList.get(variantId).limbIdList;
        } else {
            log.error("variant list is empty or can't find limb list from variant Id of: " + variantId);
        }
        return limbList;
    }
    public static List<String> getListAnatomyKeys() {
        return new ArrayList<>(variantList.keySet());
    }
    /**
     * @param person Person
     * @return the Generic Variant tag of a person. A person must only have 1 variant
     */
    public static String getPersonVariantTag(PersonAPI person) {
        List<String> anatomyVariantList = getListAnatomyKeys();
        String personVariant = null;
        Set<String> tags = person.getTags();
        if(tags != null && !tags.isEmpty()) {
            for (String k: tags) {
                for(String variant: anatomyVariantList) {
                    if(variant.equals(k)) {
                        personVariant = k;
                    }
                }
            }
        }
        return personVariant;
    }

    /**
     * Get a random variant based on faction. Defined in faction_data.json
     * @param factionId
     * @return
     */
    public static String getRandomVariant(String factionId) {
        return ba_factionmanager.getRandomFactionVariant(factionId);
    }

    /**
     * Get a random variant
     * @return
     */
    public static String getRandomVariant() {
        WeightedRandomPicker<String> randomPicker = new WeightedRandomPicker<>();
        randomPicker.addAll(getListAnatomyKeys());
        return randomPicker.pick();
    }

    /**
     * Get random variant from faction id
     * @param factionId
     * @return
     */
    public static String getRandomVariantFromFaction(String factionId) {
        WeightedRandomPicker<String> randomPicker = new WeightedRandomPicker<>();
        ba_factiondata factionData = ba_factionmanager.getFactionData(factionId);
        if(factionData != null) {
            for(ba_factiondata.ba_factionVariantDetails details:  factionData.variantDetails) {
                randomPicker.add(details.variant.variantId, details.variantSpawnWeight);
            }
        }
        return randomPicker.pick();
    }
}

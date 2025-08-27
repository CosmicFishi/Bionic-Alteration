package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.CollectionUtils;
import org.magiclib.util.MagicSettings;
import pigeonpun.bionicalteration.utils.ba_utils;
import pigeonpun.bionicalteration.variant.ba_variantmanager;

import java.io.IOException;
import java.util.*;

public class ba_limbmanager {
    static Logger log = Global.getLogger(ba_limbmanager.class);
    public static HashMap<String, ba_limbmanager.ba_limb> limbMap = new HashMap<>();
    public static HashMap<String, List<ba_limb>> limbGroupMap = new HashMap<>();
    public static final String DYNAMIC_LIMB_ID_CUSTOM_DIVIDER = "`";
    public static void onApplicationLoad() {
        loadLimbs();
    }
    public static void loadLimbs() {
        //load from csv
        //limbMap.clear();
        List<String> limbFiles = MagicSettings.getList(ba_variablemanager.BIONIC_ALTERATION, "limb_files");
        for (String path : limbFiles) {
            log.error("merging limb files");
            JSONArray limbData = new JSONArray();
            try {
                limbData = Global.getSettings().getMergedSpreadsheetDataForMod("limbId", path, ba_variablemanager.BIONIC_ALTERATION);
            } catch (IOException | JSONException | RuntimeException ex) {
                log.error("unable to read " + path, ex);
            }
            for (int i = 0; i < limbData.length(); i++) {
                try{
                    JSONObject row = limbData.getJSONObject(i);
                    //limb
                    String limbId = row.getString("limbId");
                    String limbGroup = row.getString("groupId");
                    if(!Objects.equals(limbId, "") && !Objects.equals(limbGroup, "")) {
                        List<String> limbGroupList = ba_utils.trimAndSplitString(limbGroup);
                        if(row.getString("tags") != null && row.getString("tags") != "") {
                            limbMap.put(
                                    limbId,
                                    new ba_limb(
                                            limbId,
                                            row.getString("name"),
                                            row.getString("description"),
                                            limbGroupList,
                                            ba_utils.trimAndSplitString(row.getString("tags")),
                                            row.getInt("order")
                                    )
                            );
                        } else {
                            limbMap.put(
                                    limbId,
                                    new ba_limb(
                                            limbId,
                                            row.getString("name"),
                                            row.getString("description"),
                                            limbGroupList,
                                            row.getInt("order")
                                    )
                            );
                        }
                        for (String limbGroupId: limbGroupList) {
                            if(limbGroupMap.get(limbGroupId) != null) {
                                limbGroupMap.get(limbGroupId).add(limbMap.get(limbId));
                            } else {
                                limbGroupMap.put(limbGroupId, new ArrayList<>(Arrays.asList(limbMap.get(limbId))));
                            }
                        }
                    }
                } catch (JSONException ex) {
                    log.error("Invalid line, skipping");
                }
            }
        }
        for (Map.Entry<String, List<ba_limb>> entry: limbGroupMap.entrySet()) {
            log.info("loaded: " + entry.getKey() + "-----");
            for(ba_limb limb: entry.getValue()) {
                log.info("-----child: " + limb.name);
            }
        }
    }
    public static List<String> getListLimbGroupKeys() {
        return new ArrayList<>(limbGroupMap.keySet());
    }
    /**
     * Return a string list of limbs belongs to a group
     * Return empty if cant find the group
     * @param groupId group id
     * @return list<String>
     */
    public static List<String> getListLimbKeys(String groupId) {
        List<String> listLimbs = new ArrayList<>();
        List<ba_limb> list = limbGroupMap.get(groupId);
        if(list == null) {
            log.error("Can not find list of id: "+ groupId);
        } else {
            for(ba_limb limb: list){
                listLimbs.add(limb.limbId);
            }
        }
        return listLimbs;
    }
    public static ba_limb getLimb(String id) {
        ba_limb section = limbMap.get(id);
//        log.info("getting limb: " + id);
        if(section == null) {
            log.error("Can not find bionic section of id: "+ id);
        }
        return section;
    }
    public static List<ba_limb> getLimbListFromGroupOnPerson(String groupId, PersonAPI person) {
        List<ba_limb> personLimbList = new ArrayList<>();
        String personVariant = ba_variantmanager.getPersonVariantTag(person);
        if(personVariant != null) {
            for (ba_limbmanager.ba_limb limb: ba_limbmanager.getListLimbFromGroup(groupId)) {
                for(String limbId: ba_variantmanager.getListLimbFromVariant(personVariant)) {
                    if(limb.limbId.equals(limbId)) {
                        personLimbList.add(limb);
                    }
                }
            }
        }

        return personLimbList;
    }
    public static List<ba_limb> getListLimbFromGroup(String groupId) {
        List<ba_limb> list = limbGroupMap.get(groupId);
        if(list == null) {
            log.error("Can not find limb list of group id: "+ groupId);
        }
        return list;
    }
    public static boolean isLimbInGroup(String groupId, String limbId) {
        List<ba_limb> listLimb = getListLimbFromGroup(groupId);
        limbId = getBaseLimbId(limbId);
        if(listLimb != null) {
            for(ba_limb limb: listLimb) {
                if(limb.limbId.equals(limbId)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static List<ba_officermanager.ba_bionicAugmentedData> sortBionicDataByLimbOrder(List<ba_officermanager.ba_bionicAugmentedData> data) {
        List<ba_officermanager.ba_bionicAugmentedData> result = new ArrayList<>(data);
        result.sort((o1, o2) -> o1.limb.order > o2.limb.order ? 1 : 0);
        return result;
    }

    /**
     * AI for now. todo: add normal human
     * @param baseLimb
     * @param data NULL will create limb with startPrefix 0
     * @return     */
    public static ba_limb createDynamicLimb(@NotNull ba_limbmanager.ba_limb baseLimb, @Nullable List<ba_officermanager.ba_bionicAugmentedData> data) {
        boolean isDuplicated = true;
        int startPrefix = 1;
        String dynamicId = baseLimb.limbId + DYNAMIC_LIMB_ID_CUSTOM_DIVIDER + startPrefix;
        while(isDuplicated && data != null) {
            boolean found = false;
            for (ba_officermanager.ba_bionicAugmentedData baBioformAugmentedData : data) {
                if(baBioformAugmentedData.limb.limbId.equals(dynamicId)) {
                    startPrefix += 1;
                    dynamicId = baseLimb.limbId + DYNAMIC_LIMB_ID_CUSTOM_DIVIDER + startPrefix;
                    found = true;
                    break;
                }
            }
            if(!found) {
                isDuplicated = false;
            }
        }
        if(startPrefix == 1) {
            return new ba_limb(dynamicId, baseLimb, String.valueOf(startPrefix), false);
        }
        return new ba_limb(dynamicId, baseLimb, String.valueOf(startPrefix));
    }
    @Nullable
    public static ba_limbmanager.ba_limb getLimbFromPerson(PersonAPI person, String limbId) {
        List<ba_officermanager.ba_bionicAugmentedData> augmentedData = new ArrayList<>();
        if(person.isAICore()) {
            ba_officermanager.ba_aimemorydata data = ba_officermanager.getAIMemData(person, Global.getSector().getCampaignUI().getCurrentInteractionDialog());
            if(data != null) augmentedData = data.anatomy;
        } else {
            ba_officermanager.ba_personmemorydata data = ba_officermanager.getPersonMemoryData(person);
            if(data != null) augmentedData = data.anatomy;
        }
        for (ba_officermanager.ba_bionicAugmentedData d: augmentedData) {
            if(d.limb.limbId.equals(limbId)) {
                return d.limb;
            }
        }
        return null;
    }
    public static ba_limbmanager.ba_limb getBaseLimb(ba_limbmanager.ba_limb dynamicLimb) {
        return getBaseLimb(dynamicLimb.limbId);
    }
    @Nullable
    public static ba_limbmanager.ba_limb getBaseLimb(String dynamicLimbId) {
        String[] limbIds = dynamicLimbId.split(DYNAMIC_LIMB_ID_CUSTOM_DIVIDER.toString());
        if(ba_limbmanager.getLimb(limbIds[0]) != null) {
            return ba_limbmanager.getLimb(limbIds[0]);
        }
        return null;
    }
    public static String getBaseLimbId(String dynamicLimbId) {
        String[] limbIds = dynamicLimbId.split(DYNAMIC_LIMB_ID_CUSTOM_DIVIDER.toString());
        return limbIds[0];
    }

    /**
     * Center of the entire bioform, can NOT be removed/alter/add using UI. Will be count toward total limb count
     * @param limb
     * @return
     */
    public static boolean isLimbCentralLimb(ba_limb limb) {
        return limb.tags.contains(ba_variablemanager.BA_BIOFORM_CENTRAL_TAG);
    }
    public static boolean isLimbBaseLimb(ba_limb limb) {
        return limb.tags.contains("base_dynamic_limb");
    }
    public static boolean isLimbDynamicLimb(ba_limb limb) {
        return !isLimbBaseLimb(limb);
    }
    public static class ba_limb {
        public String limbId;
        public String name;
        public String description;
        public List<String> limbGroupList;
        public List<String> tags = new ArrayList<>();
        public int order = 0;
        public HashMap<String, Object> customData = new HashMap<>();
        public ba_limb(String limbId, String name, String description, List<String> limbGroupId, int order) {
            this.limbId = limbId;
            this.name = name;
            this.description = description;
            this.limbGroupList = limbGroupId;
            this.order = order;
        }
        public ba_limb(String limbId, String name, String description, List<String> limbGroupId, List<String> tags, int order) {
            this.limbId = limbId;
            this.name = name;
            this.description = description;
            this.limbGroupList = limbGroupId;
            this.tags = tags;
            this.order = order;
        }

        /**
         * Use for creating dynamic limb
         * @param limbId
         * @param baseLimb
         */
        public ba_limb(String limbId, @NotNull ba_limb baseLimb, String namePrefix) {
            this.limbId = limbId;
            this.name = baseLimb.name + " " + namePrefix;
            this.description = baseLimb.description;
            this.limbGroupList = baseLimb.limbGroupList;
            this.tags = baseLimb.tags;
            this.order = baseLimb.order;
            this.tags.add(ba_variablemanager.BA_DYNAMICALLY_CREATE_LIMB);
        }
        public ba_limb(String limbId, @NotNull ba_limb baseLimb, String namePrefix, boolean isAddPrefixToName) {
            this.limbId = limbId;
            this.name = baseLimb.name + (isAddPrefixToName? " " + namePrefix: "");
            this.description = baseLimb.description;
            this.limbGroupList = baseLimb.limbGroupList;
            this.tags = baseLimb.tags;
            this.order = baseLimb.order;
            this.tags.add(ba_variablemanager.BA_DYNAMICALLY_CREATE_LIMB);
        }

        /**
         * Comparing using the base limb ID, DO NOT USE THIS IF YOU WANT TO COMPARE THE ACTUAL LIMB ID
         * @param otherLimbId
         * @return
         */
        public boolean sameAs(String otherLimbId) {
            try {
                return Objects.requireNonNull(getBaseLimbId(this.limbId)).equals(Objects.requireNonNull(getBaseLimbId(otherLimbId)));
            } catch (Exception ex) {
                log.error("Limb ID comparison mismatch between " + this.limbId + " + " + otherLimbId + " - " + ex);
            }
            return false;
        }
    }
}

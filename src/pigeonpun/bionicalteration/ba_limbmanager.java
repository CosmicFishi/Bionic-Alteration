package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicSettings;
import pigeonpun.bionicalteration.utils.ba_utils;
import pigeonpun.bionicalteration.variant.ba_variantmanager;

import java.io.IOException;
import java.util.*;

public class ba_limbmanager {
    static Logger log = Global.getLogger(ba_limbmanager.class);
    protected static HashMap<String, ba_limbmanager.ba_limb> limbMap = new HashMap<>();
    protected static HashMap<String, List<ba_limb>> limbGroupMap = new HashMap<>();
    public static final String DYNAMIC_LIMB_ID_CUSTOM_DIVIDER = "|";
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
                                            ba_utils.trimAndSplitString(row.getString("tags"))
                                    )
                            );
                        } else {
                            limbMap.put(
                                    limbId,
                                    new ba_limb(
                                            limbId,
                                            row.getString("name"),
                                            row.getString("description"),
                                            limbGroupList
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
        if(listLimb != null) {
            for(ba_limb limb: listLimb) {
                if(limb.limbId.equals(limbId)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static ba_limb createDynamicLimb(ba_limbmanager.ba_limb baseLimb, String prefix) {
        String dynamicId = baseLimb.limbId + DYNAMIC_LIMB_ID_CUSTOM_DIVIDER + prefix;
        return new ba_limb(dynamicId, baseLimb);
    }
    protected static ba_limbmanager.ba_limb getBaseLimb(ba_limbmanager.ba_limb dynamicLimb) {
        String[] limbIds = dynamicLimb.limbId.split(DYNAMIC_LIMB_ID_CUSTOM_DIVIDER.toString());
        if(ba_limbmanager.getLimb(limbIds[0]) != null) {
            return ba_limbmanager.getLimb(limbIds[0]);
        }
        return null;
    }
    public static class ba_limb {
        public String limbId;
        public String name;
        public String description;
        public List<String> limbGroupList;
        public List<String> tags = new ArrayList<>();
        public HashMap<String, Object> customData = new HashMap<>();
        public ba_limb(String limbId, String name, String description, List<String> limbGroupId) {
            this.limbId = limbId;
            this.name = name;
            this.description = description;
            this.limbGroupList = limbGroupId;
        }
        public ba_limb(String limbId, String name, String description, List<String> limbGroupId, List<String> tags) {
            this.limbId = limbId;
            this.name = name;
            this.description = description;
            this.limbGroupList = limbGroupId;
            this.tags = tags;
        }

        /**
         * Use for creating dynamic limb
         * @param limbId
         * @param baseLimb
         */
        public ba_limb(String limbId, ba_limb baseLimb) {
            this.limbId = limbId;
            this.name = baseLimb.name;
            this.description = baseLimb.description;
            this.limbGroupList = baseLimb.limbGroupList;
            this.tags = baseLimb.tags;
            this.tags.add(ba_variablemanager.BA_DYNAMICALLY_CREATE_LIMB);
        }
    }
}

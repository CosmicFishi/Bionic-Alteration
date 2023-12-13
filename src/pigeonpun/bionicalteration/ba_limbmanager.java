package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicSettings;

import java.io.IOException;
import java.util.*;

public class ba_limbmanager {
    static Logger log = Global.getLogger(ba_limbmanager.class);
    protected static HashMap<String, ba_limbmanager.ba_limb> limbMap = new HashMap<>();
    protected static HashMap<String, List<ba_limb>> limbGroupMap = new HashMap<>();
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
                    if(!Objects.equals(limbId, "")) {
                        limbMap.put(
                                limbId,
                                new ba_limb(
                                        limbId,
                                        row.getString("name"),
                                        row.getString("description")
                                )
                        );
                        //limb group
                        //todo: change the limb group to support array instead of single
                        String limbGroup = row.getString("groupId");
                        if(!Objects.equals(limbGroup, ""))
                        if(limbGroupMap.get(limbGroup) != null) {
                            limbGroupMap.get(limbGroup).add(limbMap.get(limbId));
                        } else {
                            limbGroupMap.put(limbGroup, new ArrayList<>(Arrays.asList(limbMap.get(limbId))));
                        }
                    }
                } catch (JSONException ex) {
                    log.error("Invalid line, skipping");
                }
            }
        }
//        for (Map.Entry<String, ba_limb> entry: limbMap.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue().limbId);
//        }
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
    public static class ba_limb {
        public String limbId;
        public String name;
        public String description;
        public HashMap<String, Object> customData = new HashMap<>();
        public ba_limb(String limbId, String name, String description) {
            this.limbId = limbId;
            this.name = name;
            this.description = description;
        }
    }
}

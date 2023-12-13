package pigeonpun.bionicalteration.bionic;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicSettings;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_variablemanager;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Handle bionic list/loading
 * Install tag: <BIONIC_TAG>:<LIMB_TAB>. Ex: bionic_id:hand_right
 * @author PigeonPun
 */
public class ba_bionicmanager {
    static Logger log = Global.getLogger(ba_bionicmanager.class);
    protected static HashMap<String, ba_bionic> bionicMap = new HashMap<>();
    public ba_bionicmanager() {
        loadBionic();
    }
    public static void onApplicationLoad() {
        loadBionic();
    }
    //todo: change to get csv
    public static void loadBionic() {
//        bionicMap.put("1",new ba_bionic("1","hand_group","Your mom hand", "idk", Color.RED.darker()));
//        bionicMap.put("2",new ba_bionic("2","hand_group","Hand in Hand?", "idk", Color.YELLOW.darker()));
//        bionicMap.put("3",new ba_bionic("3","eye_group","Smoll eye", "idk", Color.magenta));
//        bionicMap.put("4",new ba_bionic("4","eye_group","Big eye", "idk", Color.green));
//        bionicMap.put("5",new ba_bionic("5","brain_group","Big brainnnnn", "idk", Color.CYAN.darker()));
//        bionicMap.put("6",new ba_bionic("6","heart_group","My heart <3", "idk", Color.blue));
//        bionicMap.put("7",new ba_bionic("7","hand_group","Big MF hand", "idk", Color.pink));
//        bionicMap.put("8",new ba_bionic("8","heart_group","Broken heart </3", "idk", Color.green));
//        bionicMap.put("9",new ba_bionic("9","eye_group","Pirate eye, ARRRRR", "idk", Color.cyan));
//        bionicMap.put("10",new ba_bionic("10","brain_group","ANother fking brain to understand WTF did i code", "idk", Color.YELLOW));
//        bionicMap.put("11",new ba_bionic("11","brain_group","Another another brain", "idk", Color.blue.darker()));

        //load from csv
        List<String> limbFiles = MagicSettings.getList(ba_variablemanager.BIONIC_ALTERATION, "bionic_files");
        for (String path : limbFiles) {
            log.error("merging bionic files");
            JSONArray bionicData = new JSONArray();
            try {
                bionicData = Global.getSettings().getMergedSpreadsheetDataForMod("bionicId", path, ba_variablemanager.BIONIC_ALTERATION);
            } catch (IOException | JSONException | RuntimeException ex) {
                log.error("unable to read " + path, ex);
            }
            for (int i = 0; i < bionicData.length(); i++) {
                try{
                    JSONObject row = bionicData.getJSONObject(i);
                    String bionicId = row.getString("bionicId");
                    ba_bionicEffect effect = null;
                    try {
                        if(!Objects.equals(row.getString("scriptPath"), "") && row.getString("scriptPath") != null) {
                            Class<?> clazz = Global.getSettings().getScriptClassLoader().loadClass(row.getString("scriptPath"));
                            effect = (ba_bionicEffect) clazz.newInstance();
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    bionicMap.put(bionicId, new ba_bionic(
                            bionicId,
                            row.getString("limbGroupId"),
                            row.getString("name"),
                            row.getString("description"),
                            MagicSettings.toColor3(row.getString("colorDisplay")),
                            (float) row.getDouble("consciousnessCost"),
                            row.getInt("tier"),
                            row.getBoolean("isOfficerBionic"),
                            effect
                    ));

                } catch (JSONException ex) {
                    log.error("Invalid line, skipping");
                }
            }
        }
        for (Map.Entry<String, ba_bionic> entry: bionicMap.entrySet()) {
            log.info(entry.getKey() + " " + entry.getValue().bionicLimbGroupId + "-----" + entry.getValue().bionicId);
        }
    }
    public static ba_bionic getBionic(String id) {
        ba_bionic bionic = bionicMap.get(id);
        if(bionic == null) {
            log.error("Can not find bionic of id: "+ id);
        }
        return bionic;
    }
    public static List<String> getListStringBionicInstalled(PersonAPI person) {
        List<String> bionics = new ArrayList<>();
        if (!person.getTags().isEmpty()) {
            for (String tag: person.getTags()) {
                if(bionicMap.get(tag) != null) {
                    bionics.add(tag);
                }
            }
        }
        return bionics;
    }
    public static HashMap<ba_limbmanager.ba_limb, List<ba_bionic>> getListBionicInstalled(PersonAPI person) {
        HashMap<ba_limbmanager.ba_limb, List<ba_bionic>> bionicsInstalledList = new HashMap<>();
        if (!person.getTags().isEmpty()) {
            for (String tag: person.getTags()) {
                if(tag.contains(":")) {
                    String[] tokens = tag.split(":");
                    ba_bionic bionicInstalled = bionicMap.get(tokens[0]);
                    if(bionicInstalled == null) log.error("Can't find bionic of tag: " + tokens[0]);
                    ba_limbmanager.ba_limb sectionInstalled = ba_limbmanager.getLimb(tokens[1]);
                    if(sectionInstalled == null) log.error("Can't find section of tag: " + tokens[1]);
                    if(bionicsInstalledList.get(sectionInstalled) != null) {
                        bionicsInstalledList.get(sectionInstalled).add(bionicInstalled);
                    } else {
                        bionicsInstalledList.put(sectionInstalled, new ArrayList<ba_bionic>(Arrays.asList(bionicInstalled)));
                    }
                }
            }
        }
        return bionicsInstalledList;
    }
    public static List<String> getRandomBionic() {
        List<String> randomBionic = new ArrayList<>();
        WeightedRandomPicker<String> random = new WeightedRandomPicker<>();
        random.addAll(getListBionicKeys());
        int i = 0;
        int maxNumberOfRandomBionic = 6;
        while(i < maxNumberOfRandomBionic) {
            String picked = random.pick();
            random.remove(picked);
            ba_bionic bionic = getBionic(picked);
            WeightedRandomPicker<String> randomSectionPicker = new WeightedRandomPicker<>();
            randomSectionPicker.addAll(ba_limbmanager.getListLimbKeys(bionic.bionicLimbGroupId));
            randomBionic.add(picked+":"+ randomSectionPicker.pick());
            i++;
        }
        return randomBionic;
    }
    public static List<String> getListBionicKeys() {
        return new ArrayList<>(bionicMap.keySet());
    }
    public static class ba_bionic {
        public String bionicId;
//        public List<String> bionicSectionId;
        public String bionicLimbGroupId;
        public String name;
        public String description;
        public Color displayColor;
        public float consciousnessCost;
        public int tier;
        public ba_bionicEffect effectScript;
        public boolean isOfficerBionic;
        //todo: support sprite
        public HashMap<String, Object> customData = new HashMap<>();
        public ba_bionic(String bionicId, String bionicLimbGroupId, String name, String description, Color displayColor, float consciousnessCost, int tier, boolean isOfficerBionic, ba_bionicEffect effectScript) {
            this.bionicId = bionicId;
            this.bionicLimbGroupId = bionicLimbGroupId;
            this.name = name;
            this.description = description;
            this.displayColor = displayColor;
            this.consciousnessCost = consciousnessCost;
            this.tier = tier;
            this.isOfficerBionic = isOfficerBionic;
            this.effectScript = effectScript;
        }
    }
}

package pigeonpun.bionicalteration.bionic;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.CollectionUtils;
import org.magiclib.util.MagicSettings;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.utils.ba_utils;

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
    public static HashMap<String, ba_bionicitemplugin> bionicItemMap = new HashMap<>();
    public ba_bionicmanager() {
        loadBionic();
    }
    public static void onApplicationLoad() {
        loadBionic();
    }
    public static void loadBionic() {

        //load from csv
        List<String> limbFiles = MagicSettings.getList(ba_variablemanager.BIONIC_ALTERATION, "bionic_files");
        for (String path : limbFiles) {
            log.error("merging bionic files");
            JSONArray bionicData = new JSONArray();
            try {
                bionicData = Global.getSettings().getMergedSpreadsheetDataForMod("id", path, ba_variablemanager.BIONIC_ALTERATION);
            } catch (IOException | JSONException | RuntimeException ex) {
                log.error("unable to read " + path, ex);
            }
            for (int i = 0; i < bionicData.length(); i++) {
                try{
                    JSONObject row = bionicData.getJSONObject(i);
                    try{
                        row.getString("limbGroupId");
                    }catch (JSONException ex) {
                        continue;
                    }
                    String bionicId = row.getString("id");
                    ba_bioniceffect effect = null;
                    try {
                        if(!Objects.equals(row.getString("effectScript"), "")) {
                            Class<?> clazz = Global.getSettings().getScriptClassLoader().loadClass(row.getString("effectScript"));
                            effect = (ba_bioniceffect) clazz.newInstance(); //check if this is created ?
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    ba_bionicitemplugin bionic = new ba_bionicitemplugin(
                            bionicId,
                            Global.getSettings().getSpecialItemSpec(bionicId),
                            row.getString("limbGroupId"),
                            !row.getString("namePrefix").equals("") ? row.getString("namePrefix") : "",
                            MagicSettings.toColor3(row.getString("colorDisplay")),
                            row.getInt("brmCost"),
                            (float) row.getDouble("consciousnessCost"),
                            row.getBoolean("isCaptainBionic"),
                            row.getBoolean("isAICoreBionic"),
                            effect,
                            row.getBoolean("isAdvanceInCombat"),
                            row.getBoolean("isAdvanceInCampaign"),
                            !row.getString("conflictedBionicIdList").equals("")? ba_utils.trimAndSplitString(row.getString("conflictedBionicIdList")): null,
                            row.getBoolean("isAllowedRemoveAfterInstall")
                    );
                    bionicItemMap.put(bionicId, bionic);
                    if(effect != null) effect.setBionicItem(bionic);

                } catch (JSONException ex) {
                    log.error(ex);
                    log.error("Invalid line, skipping");
                }
            }
        }
//        for (Map.Entry<String, ba_bionicitemplugin> entry: bionicItemMap.entrySet()) {
//            log.info(entry.getKey() + ": " + entry.getValue().bionicLimbGroupId + "-----" + entry.getValue().getSpec().getDesc());
//        }
    }

    /**
     * Return null with error if can't find the id
     * @param id
     * @return
     */
    public static ba_bionicitemplugin getBionic(String id) {
        ba_bionicitemplugin bionic = bionicItemMap.get(id);
        if(bionic == null) {
            log.error("Can not find bionic of id: "+ id);
        }
        return bionic;
    }
    public static boolean checkIfHaveBionicInstalled(PersonAPI person) {
        return getListStringBionicInstalled(person).size() > 0;
    }

    /**
     * Check the entire person limbs. Want to check specific limb? use ba_officermanager.checkIfCanInstallBionic()
     * @param bionic the bionic
     * @param person the person
     * @return
     */
    public static boolean checkIfHaveBionicInstalled(ba_bionicitemplugin bionic, PersonAPI person) {
        if (!person.getTags().isEmpty()) {
            for (String tag: person.getTags()) {
                if(tag.contains(":")) {
                    String[] tokens = tag.split(":");
                    if(tokens[0].equals(bionic.bionicId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * check the entire person limbs to see the bionic conflicts with any bionics installed on person
     * @param bionic checking bionic
     * @param person
     * @return true if conflicted
     */
    public static boolean checkIfBionicConflicted(ba_bionicitemplugin bionic, PersonAPI person) {
        if (!person.getTags().isEmpty()) {
            //check for bionic item
            List<String> listStringBionicInstalled = getListStringBionicInstalled(person);
            if(!bionic.conflictedBionicIdList.isEmpty()) {
                for(String id: bionic.conflictedBionicIdList) {
                    for(String conflictId: listStringBionicInstalled) {
                        if(id.equals(conflictId)) return true;
                    }
                }
            }
            //check for person's bionics
            Set<ba_bionicitemplugin> personConflictedList = getListBionicConflicts(person);
            if(!personConflictedList.isEmpty()) {
                for (ba_bionicitemplugin b: personConflictedList) {
                    if(b.bionicId.equals(bionic.bionicId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Return list bionic that conflicted from the parameter bionic.
     * Note: Bionic isn't available from bionicItemMap won't show up in the return list in case the mod that have that bionic isn't enabled
     * @param bionic search conflicted from this bionic
     * @return
     */
    public static List<ba_bionicitemplugin> getListBionicConflicts(ba_bionicitemplugin bionic) {
        List<ba_bionicitemplugin> conflictList = new ArrayList<>();
        if(!bionic.conflictedBionicIdList.isEmpty()) {
            for (String id: bionic.conflictedBionicIdList) {
                ba_bionicitemplugin b = bionicItemMap.get(id);
                if(b != null) {
                    conflictList.add(b);
                }
            }
        }

        return conflictList;
    }

    /**
     * Return list bionic that conflicted from a person.
     * Note: this will return the entire list of conflicted bionic from the person's installed bionics
     * @param person
     * @return
     */
    public static Set<ba_bionicitemplugin> getListBionicConflicts(PersonAPI person) {
        List<ba_bionicitemplugin> conflictList = new ArrayList<>();
        List<ba_bionicitemplugin> personBionicList = getListBionicInstalled(person);
        //get conflicted list from the person -> add it into a big array
        for(ba_bionicitemplugin bionic: personBionicList) {
            conflictList.addAll(getListBionicConflicts(bionic));
        }

        return new HashSet<>(conflictList);
    }
    public static List<String> getListStringBionicInstalled(PersonAPI person) {
        List<String> bionics = new ArrayList<>();
        if (!person.getTags().isEmpty()) {
            for (String tag: person.getTags()) {
                if(tag.contains(":")) {
                    String[] tokens = tag.split(":");
                    ba_bionicitemplugin bionicInstalled = bionicItemMap.get(tokens[0]);
                    if(bionicInstalled == null) {
                        log.error("Can't find bionic of tag: " + tokens[0] + ". Skipping");
                    } else {
                        bionics.add(tag);
                    }
                }
            }
        }
        return bionics;
    }

    /**
     * Return empty list if found none bionics on the limb
     * @param limb the limb
     * @param person the person
     * @return List of bionic installed on that specific limb
     */
    public static List<ba_bionicitemplugin> getListBionicInstalledOnLimb(ba_limbmanager.ba_limb limb, PersonAPI person) {
        List<ba_bionicitemplugin> bionicsInstalledList = new ArrayList<>();
        if(limb != null && !person.getTags().isEmpty()) {
            for (String tag: person.getTags()) {
                if(tag.contains(":")) {
                    String[] tokens = tag.split(":");
                    if(tokens[1].equals(limb.limbId)) {
                        ba_bionicitemplugin bionicInstalled = bionicItemMap.get(tokens[0]);
                        if(bionicInstalled == null) {
                            log.error("Can't find bionic of tag: " + tokens[0]);
                        } else {
                            bionicsInstalledList.add(bionicInstalled);
                        }
                    }
                }
            }
        }
        return bionicsInstalledList;
    }

    /**
     * Get a list of all bionic installed <br>
     * If you want to see the full list of which limb have which bionic. Use {@code getListLimbAndBionicInstalled}.
     * @param person person
     * @return List of bionic installed.<br> NOTE: There will be duplicate of existing bionic in the list.
     */
    public static List<ba_bionicitemplugin> getListBionicInstalled(PersonAPI person) {
        List<ba_bionicitemplugin> bionicsInstalledList = new ArrayList<>();
        if (!person.getTags().isEmpty()) {
            for (String tag: person.getTags()) {
                if(tag.contains(":")) {
                    String[] tokens = tag.split(":");
                    ba_bionicitemplugin bionicInstalled = bionicItemMap.get(tokens[0]);
                    if(bionicInstalled == null) {
                        log.error("Can't find bionic of tag: " + tokens[0] + ". Skipping");
                    } else {
                        bionicsInstalledList.add(bionicInstalled);
                    }
                }
            }
        }
        return bionicsInstalledList;
    }

    /**
     * Get list bionic and which limb they are installed on. Unsorted.
     * @param person Person
     * @return The bionics and limb they are installed on.
     */
    public static HashMap<ba_limbmanager.ba_limb, List<ba_bionicitemplugin>> getListLimbAndBionicInstalled(PersonAPI person) {
        HashMap<ba_limbmanager.ba_limb, List<ba_bionicitemplugin>> bionicsInstalledList = new HashMap<>();
        if (!person.getTags().isEmpty()) {
            for (String tag: person.getTags()) {
                if(tag.contains(":")) {
                    String[] tokens = tag.split(":");
                    ba_bionicitemplugin bionicInstalled = bionicItemMap.get(tokens[0]);
                    if(bionicInstalled == null) log.error("Can't find bionic of tag: " + tokens[0]);
                    ba_limbmanager.ba_limb sectionInstalled = ba_limbmanager.getLimb(tokens[1]);
                    if(sectionInstalled == null) log.error("Can't find limb of tag: " + tokens[1]);
                    if(bionicsInstalledList.get(sectionInstalled) != null) {
                        bionicsInstalledList.get(sectionInstalled).add(bionicInstalled);
                    } else {
                        bionicsInstalledList.put(sectionInstalled, new ArrayList<ba_bionicitemplugin>(Arrays.asList(bionicInstalled)));
                    }
                }
            }
        }
        return bionicsInstalledList;
    }
    public static List<String> getRandomBionic(int count) {
        List<String> randomBionic = new ArrayList<>();
        WeightedRandomPicker<String> random = new WeightedRandomPicker<>();
        random.addAll(getListBionicKeys());
        int i = 0;
        while(i < count && !random.isEmpty()) {
            String picked = random.pick();
            random.remove(picked);
//            ba_bionicitemplugin bionic = getBionic(picked);
//            WeightedRandomPicker<String> randomSectionPicker = new WeightedRandomPicker<>();
            randomBionic.add(picked);
            i++;
        }
        return randomBionic;
    }
    public static List<String> getListBionicKeys() {
        return new ArrayList<>(bionicItemMap.keySet());
    }
    //todo: Render the stars
    public static TooltipMakerAPI renderExtraOnItem() {
        return null;
    }
}

package pigeonpun.bionicalteration.bionic;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.CollectionUtils;
import org.magiclib.util.MagicSettings;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.overclock.ba_overclock;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;
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
    //NOTE: DO NOT MODIFY ANY OF THE BIONICS ON THIS LIST
    //This is just a list of bionic item always available for query
    public static HashMap<String, ba_bionicitemplugin> bionicItemMap = new HashMap<>();
    //todo: create a seperate map from the normal bionicIemMap for easier identification. Currently some bionic item will have overclock list, some wont
    public static HashMap<String, List<String>> overclockableBionicItemMap = new HashMap<>();
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
                    ba_bionicitemplugin bionic = null;
                    try {
                        if(!Objects.equals(row.getString("effectScript"), "")) {
                            Class<?> clazz = Global.getSettings().getScriptClassLoader().loadClass(row.getString("effectScript"));
                            bionic = (ba_bionicitemplugin) clazz.newInstance(); //check if this is created ?
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    if(bionic != null) {
                        bionic.setId(bionicId);
                        bionic.spec = Global.getSettings().getSpecialItemSpec(bionicId);
                        bionic.bionicLimbGroupId = row.getString("limbGroupId");
                        bionic.namePrefix = !row.getString("namePrefix").equals("") ? row.getString("namePrefix") : "";
                        bionic.displayColor = MagicSettings.toColor3(row.getString("colorDisplay"));
                        bionic.brmCost = row.getInt("brmCost");
                        bionic.consciousnessCost = (float) row.getDouble("consciousnessCost");
                        bionic.dropChance = (float) row.getDouble("dropChance");
                        bionic.isApplyCaptainEffect = row.getBoolean("isApplyCaptainEffect");
                        bionic.isApplyAdminEffect = row.getBoolean("isApplyAdminEffect");
                        bionic.isAICoreBionic = row.getBoolean("isAICoreBionic");
                        if(!row.getString("conflictedBionicIdList").equals("")) {
                            bionic.conflictedBionicIdList = ba_utils.trimAndSplitString(row.getString("conflictedBionicIdList"));
                        }
                        bionic.isAllowedRemoveAfterInstall = row.getBoolean("isAllowedRemoveAfterInstall");
                        bionic.isEffectAppliedAfterRemove = row.getBoolean("isEffectAppliedAfterRemove");
                        bionic.afterInit();
                        bionicItemMap.put(bionicId, bionic);
//                        if(effect != null) effect.setBionicItem(bionic);
                    }
                } catch (JSONException ex) {
                    log.error(ex);
                    log.error("Invalid line, skipping");
                }
            }
        }
//        for (Map.Entry<String, ba_bionicitemplugin> entry: bionicItemMap.entrySet()) {
//            log.info(entry.getKey() + ": " + entry.getValue().bionicLimbGroupId + "-----" + entry.getValue().getSpec().getDesc());
//        }
        List<String> overclockingFiles = MagicSettings.getList(ba_variablemanager.BIONIC_ALTERATION, "overclocking_bionic_files");
        for (String path : overclockingFiles) {
            log.warn("merging bionic overclocking files");
            JSONArray overclockingData = new JSONArray();
            try {
                overclockingData = Global.getSettings().getMergedSpreadsheetDataForMod("bionicId", path, ba_variablemanager.BIONIC_ALTERATION);
            } catch (IOException | JSONException | RuntimeException ex) {
                log.error("unable to read " + path, ex);
            }
            for (int i = 0; i < overclockingData.length(); i++) {
                try{
                    JSONObject row = overclockingData.getJSONObject(i);
                    try{
                        row.getString("overclockIds");
                    }catch (JSONException ex) {
                        continue;
                    }
                    String bionicId = row.getString("bionicId");
                    ba_bionicitemplugin bionic = getBionic(bionicId);
                    if(bionic != null) {
                        List<String> overclockList = ba_utils.trimAndSplitString(row.getString("overclockIds"));
                        for(String id: overclockList) {
                            if(ba_overclockmanager.getOverclock(id) != null) {
                                bionic.overclockList.add(id);
                            } else {
                                log.error("Can't find overclock of id: " + id + " for bionic " + bionicId);
                            }
                        }
                        if(!bionic.overclockList.isEmpty()) {
                            Collections.sort(bionic.overclockList, new Comparator<String>() {
                                @Override
                                public int compare(String o1, String o2) {
                                    ba_overclock overclock1 = ba_overclockmanager.getOverclock(o1);
                                    ba_overclock overclock2 = ba_overclockmanager.getOverclock(o2);
                                    return overclock1.order > overclock2.order ? 1 : 0;
                                }
                            });
                        }
                    }

                } catch (JSONException ex) {
                    log.error(ex);
                    log.error("Invalid line, skipping");
                }
            }
        }

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
//
//    /**
//     * Check the entire person limbs. Want to check specific limb? use ba_officermanager.checkIfCanInstallBionic()
//     * @param bionic the bionic
//     * @param person the person
//     * @return
//     */
//    public static boolean checkIfHaveBionicInstalled(ba_bionicitemplugin bionic, PersonAPI person) {
//        if (!person.getTags().isEmpty()) {
//            for (String tag: person.getTags()) {
//                if(tag != null && tag.contains(":")) {
//                    String[] tokens = tag.split(":");
//                    if(tokens[0].equals(bionic.bionicId)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

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
//        if (person.getTags() != null && !person.getTags().isEmpty() ) {
//            for (String tag: person.getTags()) {
//                if(tag != null && tag.contains(":")) {
//                    String[] tokens = tag.split(":");
//                    ba_bionicitemplugin bionicInstalled = bionicItemMap.get(tokens[0]);
//                    if(bionicInstalled == null) {
//                        log.error("Can't find bionic of tag: " + tokens[0] + ". Skipping");
//                    } else {
//                        bionics.add(tag);
//                    }
//                }
//            }
//        }
        ba_officermanager.ba_personmemorydata data = ba_officermanager.getPersonMemoryData(person);
        if(data != null) {
            for(ba_officermanager.ba_bionicAugmentedData bionicData: data.anatomy) {
                if(bionicData.bionicInstalled != null) {
                    bionics.add(bionicData.bionicInstalled.bionicId);
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
//        if(limb != null && !person.getTags().isEmpty()) {
//            for (String tag: person.getTags()) {
//                if(tag != null && tag.contains(":")) {
//                    String[] tokens = tag.split(":");
//                    if(tokens[1].equals(limb.limbId)) {
//                        ba_bionicitemplugin bionicInstalled = bionicItemMap.get(tokens[0]);
//                        if(bionicInstalled == null) {
//                            log.error("Can't find bionic of tag: " + tokens[0]);
//                        } else {
//                            bionicsInstalledList.add(bionicInstalled);
//                        }
//                    }
//                }
//            }
//        }
        ba_officermanager.ba_personmemorydata data = ba_officermanager.getPersonMemoryData(person);
        if(data != null) {
            for(ba_officermanager.ba_bionicAugmentedData bionicData: data.anatomy) {
                if(bionicData.limb.limbId.equals(limb.limbId) && bionicData.bionicInstalled != null) {
                    bionicsInstalledList.add(bionicData.bionicInstalled);
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
//        if (!person.getTags().isEmpty()) {
//            for (String tag: person.getTags()) {
//                if(tag != null && tag.contains(":")) {
//                    String[] tokens = tag.split(":");
//                    ba_bionicitemplugin bionicInstalled = bionicItemMap.get(tokens[0]);
//                    if(bionicInstalled == null) {
//                        log.error("Can't find bionic of tag: " + tokens[0] + ". Skipping");
//                    } else {
//                        bionicsInstalledList.add(bionicInstalled);
//                    }
//                }
//            }
//        }
        ba_officermanager.ba_personmemorydata data = ba_officermanager.getPersonMemoryData(person);
        if(data != null) {
            for(ba_officermanager.ba_bionicAugmentedData bionicData: data.anatomy) {
                if(bionicData.bionicInstalled != null) {
                    bionicsInstalledList.add(bionicData.bionicInstalled);
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
//    public static HashMap<ba_limbmanager.ba_limb, bionicData> getListLimbAndBionicInstalled(PersonAPI person) {
//        HashMap<ba_limbmanager.ba_limb, bionicData> bionicsInstalledList = new HashMap<>();
//        if (!person.getTags().isEmpty()) {
//            for (String tag: person.getTags()) {
//                if(tag != null && tag.contains(":")) {
//                    String[] tokens = tag.split(":");
//                    ba_bionicitemplugin bionicInstalled = bionicItemMap.get(tokens[0]);
//                    if(bionicInstalled == null) log.error("Can't find bionic of tag: " + tokens[0]);
//                    ba_limbmanager.ba_limb sectionInstalled = ba_limbmanager.getLimb(tokens[1]);
//                    if(sectionInstalled == null) log.error("Can't find limb of tag: " + tokens[1]);
//                    ba_overclock appliedOverclock = null;
//                    if(tokens.length >= 3 && tokens[2] != null) {
//                        appliedOverclock = ba_overclockmanager.getOverclock(tokens[2]);
//                        if (appliedOverclock == null) {
//                            log.error("Can't find overclock of tag: " + tokens[2]);
//                        }
//                    }
//                    if(bionicsInstalledList.get(sectionInstalled) != null) {
//                        bionicsInstalledList.get(sectionInstalled).bionic = bionicInstalled;
//                        bionicsInstalledList.get(sectionInstalled).overclock = appliedOverclock;
//                    } else {
//                        bionicsInstalledList.put(sectionInstalled, new bionicData(appliedOverclock, bionicInstalled));
//                    }
//                }
//            }
//        }
//        return bionicsInstalledList;
//    }
//    public static List<String> getRandomBionic(int count) {
//        List<String> randomBionic = new ArrayList<>();
//        WeightedRandomPicker<String> random = new WeightedRandomPicker<>();
//        random.addAll(getListBionicKeys());
//        int i = 0;
//        while(i < count && !random.isEmpty()) {
//            String picked = random.pick();
//            random.remove(picked);
//            randomBionic.add(picked);
//            i++;
//        }
//        return randomBionic;
//    }

    /**
     * Get bionics based on special item TAG
     * @param tag
     * @return
     */
    public static List<String> getListBionicsIdFromTag(String tag) {
        List<String> bionics = new ArrayList<>();
        for(ba_bionicitemplugin item: bionicItemMap.values()) {
            for(String t: item.getSpec().getTags()) {
                if(t.equals(tag)) {
                    bionics.add(item.bionicId);
                }
            }
        }
        return bionics;
    }
    //todo: should i keep this ?
    public static boolean checkIfPersonHasBionicDataSavedAsTag(PersonAPI person) {
        log.info("Converting bionic data from tag to memory ===== ");
        boolean found = false;
        if (!person.getTags().isEmpty()) {
            log.info(person.getName().getFullName() + ", tags: " + person.getTags().toString());
            for (String tag: person.getTags()) {
                if(tag != null && tag.contains(":")) {
                    String[] tokens = tag.split(":");
                    ba_bionicitemplugin bionicInstalled = bionicItemMap.get(tokens[0]);
                    if(bionicInstalled != null) found = true;
                }
            }
        }
        return found;
    }
    public static List<String> getListBionicKeys() {
        return new ArrayList<>(bionicItemMap.keySet());
    }

    public static void displayBionicItemDescription(TooltipMakerAPI tooltip, ba_bionicitemplugin bionic) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;

        tooltip.setParaInsigniaLarge();
        LabelAPI nameLabel = tooltip.addPara(bionic.getName(), Misc.getHighlightColor(),0);
        tooltip.addSpacer(5);
        tooltip.setParaFontDefault();

        String design = bionic.getDesignType();
        Misc.addDesignTypePara(tooltip, design, opad);
        //---------effect
        ba_bionicmanager.getBionic(bionic.getId()).displayEffectDescription(tooltip, null, null, true);
        //---------Install type
        StringBuilder effectType = new StringBuilder();
        if(bionic.isApplyAdminEffect) {
            effectType.append("Administrator");
        }
        if(bionic.isApplyCaptainEffect) {
            effectType.setLength(0);
            effectType.append("Captain");
        }
        if(bionic.isApplyAdminEffect && bionic.isApplyCaptainEffect) {
            effectType.append(" and Administrator");
        }
        LabelAPI installTypeLabel = tooltip.addPara("%s %s", pad, Misc.getBasePlayerColor(), "Install type:", effectType.toString());
        installTypeLabel.setHighlight("Apply effect type:", effectType.toString());
        installTypeLabel.setHighlightColors(g.brighter().brighter(), Misc.getPositiveHighlightColor());
        //---------BRM + conscious
        LabelAPI brmConsciousLabel = tooltip.addPara("%s %s     %s %s", pad, Misc.getBasePlayerColor(), "BRM:", "" + Math.round(bionic.brmCost), "Conscious:", "" + Math.round(bionic.consciousnessCost * 100) + "%");
        brmConsciousLabel.setHighlight("BRM:", "" + Math.round(bionic.brmCost), "Conscious:", "" + Math.round(bionic.consciousnessCost * 100) + "%");
        brmConsciousLabel.setHighlightColors(g.brighter().brighter(), Color.red, g.brighter().brighter(), Color.red);
        //---------limb list
        StringBuilder limbNameList = new StringBuilder();
        for (ba_limbmanager.ba_limb limb: ba_limbmanager.getListLimbFromGroup(bionic.bionicLimbGroupId)) {
            limbNameList.append(limb.name).append(", ");
        }
        if(limbNameList.length() > 0) limbNameList.setLength(limbNameList.length()-2);
        LabelAPI limbListLabel = tooltip.addPara("%s %s", pad, t,"Install on:", limbNameList.toString());
        limbListLabel.setHighlight("Install on:", limbNameList.toString());
        limbListLabel.setHighlightColors(g.brighter().brighter(), Misc.getBrightPlayerColor());
        //---------Conflicts
        StringBuilder conflictsList = new StringBuilder();
        for (ba_bionicitemplugin b: ba_bionicmanager.getListBionicConflicts(bionic)) {
            conflictsList.append(b.getName()).append(", ");
        }
        if(conflictsList.length() > 0) {
            conflictsList.setLength(conflictsList.length() - 2);
        } else {
            conflictsList.append("None");
        }
        LabelAPI conflictListLabel = tooltip.addPara("%s %s", pad, t,"Conflicts:", conflictsList.toString());
        conflictListLabel.setHighlight("Conflicts:", conflictsList.toString());
        conflictListLabel.setHighlightColors(g.brighter().brighter(), conflictsList.toString().equals("None")? g: Misc.getNegativeHighlightColor());

        if(ba_overclockmanager.isBionicOverclockable(bionic)) {
            //----------overclock
            ba_overclock overclock = ba_overclockmanager.getOverclockFromItem(bionic);
            String overclockApplied = overclock != null ? overclock.name : "none active";
            LabelAPI overclockLabel = tooltip.addPara("%s %s", pad, t, "Overclock:", overclockApplied);
            overclockLabel.setHighlight("Overclock:", overclockApplied);
            overclockLabel.setHighlightColors(special, overclock != null ? h: g);
        }
        //----------desc
        String desc = bionic.getSpec().getDesc();
        LabelAPI descLabel = tooltip.addPara("%s %s", pad, t, "Description:", desc);
        descLabel.setHighlight("Description:", desc);
        descLabel.setHighlightColors(g.brighter().brighter(), t);

        if(bionic.isAllowedRemoveAfterInstall) {
            if(bionic.isEffectAppliedAfterRemove) {
                LabelAPI removableLabel = tooltip.addPara("%s", pad, t, "Has effect on uninstalling this bionic.");
                removableLabel.setHighlightColors(Misc.getHighlightColor());
            }
        } else {
            LabelAPI removableLabel = tooltip.addPara("%s", pad, t, "Can not be uninstall AFTER installing");
            removableLabel.setHighlightColors(bad);
        }
    }
}

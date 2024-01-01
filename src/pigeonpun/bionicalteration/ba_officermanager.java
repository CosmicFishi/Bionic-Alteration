package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;

import java.awt.*;
import java.util.*;
import java.util.List;

import static pigeonpun.bionicalteration.ba_variantmanager.getAnatomyVariantTag;

/**
 * Handle how many bionic available on an officer
 * @author PigeonPun
 */
public class ba_officermanager {
    public static List<PersonAPI> listPersons = new ArrayList<>();
    static Logger log = Global.getLogger(ba_officermanager.class);
    public static void onSaveLoad() {
        //install random bionic on start
        refresh();
        for (PersonAPI person: listPersons) {
            installRandomBionic(person);
        }
    }

    /**
     * Refresh the entire list person pls all the other stats set up needed
     */
    public static void refresh() {
        refreshListPerson();
        setUpVariant();
        setUpDynamicStats();
        setUpSkill();
    }
    //create new admin
    //runcode import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent; import com.fs.starfarer.api.impl.campaign.ids.Factions; PersonAPI person = OfficerManagerEvent.createAdmin(Global.getSector().getFaction(Factions.MERCENARY), 1, new Random()); Global.getSector().getCharacterData().addAdmin(person);
    public static List<PersonAPI> refreshListPerson() {
        listPersons.clear();
        List<PersonAPI> listP = new ArrayList<>();
        listP.add(Global.getSector().getPlayerPerson());
        List<OfficerDataAPI> listPlayerMember = Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy();
        for (OfficerDataAPI officer: listPlayerMember) {
            listP.add(officer.getPerson());
        }
        for (AdminData admin: Global.getSector().getCharacterData().getAdmins()) {
            listP.add(admin.getPerson());
        }
        listPersons.addAll(listP);
        return listPersons;
    }
    public static void setUpVariant() {
        //todo: set up variant per faction
        for(PersonAPI person: listPersons) {
            if(getAnatomyVariantTag(person) == null) {
                String randomVariant = ba_variantmanager.getRandomVariant();
                person.addTag(randomVariant);
            }
        }
    }
    public static void setUpDynamicStats() {
        //todo: have side effects base on consciousness
        for(PersonAPI person: listPersons) {
            //consciousness
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).modifyFlat(ba_variablemanager.BA_CONSCIOUSNESS_SOURCE_KEY, setUpConsciousness(person));
            //BRM limit
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).modifyFlat(ba_variablemanager.BA_BRM_LIMIT_SOURCE_KEY, setUpBRMLimit(person));
            //BRM current
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).modifyFlat(ba_variablemanager.BA_BRM_CURRENT_SOURCE_KEY, setUpBRMCurrent(person));
        }
    }
    public static void setUpSkill() {
        for(PersonAPI person: ba_officermanager.listPersons) {
            for (MutableCharacterStatsAPI.SkillLevelAPI skill: person.getStats().getSkillsCopy()) {
                if (!skill.getSkill().getId().equals(ba_variablemanager.BA_BIONIC_SKILL_ID) && ba_bionicmanager.checkIfHaveBionicInstalled(person)) {
                    person.getStats().setSkillLevel(ba_variablemanager.BA_BIONIC_SKILL_ID, 1);
                }
            }
        }
    }
    protected static int setUpBRMLimit(PersonAPI person) {
        int brmLimit = (int) (person.getStats().getLevel() * ba_variablemanager.BA_BRM_LIMIT_BONUS_PER_LEVEL);
        return brmLimit;
    }
    protected static int setUpBRMCurrent(PersonAPI person) {
        int brmCurrent = 0;
        List<ba_bionicitemplugin> listBionics = ba_bionicmanager.getListBionicInstalled(person);
        for (ba_bionicitemplugin bionic: listBionics) {
            brmCurrent += bionic.brmCost;
        }
        if(brmCurrent < 0) brmCurrent = 0;
        return brmCurrent;
    }
    protected static float setUpConsciousness(PersonAPI person) {
        float currentConsciousness = ba_variablemanager.BA_CONSCIOUSNESS_DEFAULT;
        List<ba_bionicitemplugin> listBionics = ba_bionicmanager.getListBionicInstalled(person);
        for (ba_bionicitemplugin bionic: listBionics) {
            currentConsciousness -= bionic.consciousnessCost;
        }
//        log.info(currentConsciousness);
        if(currentConsciousness > ba_variablemanager.BA_CONSCIOUSNESS_DEFAULT) currentConsciousness = ba_variablemanager.BA_CONSCIOUSNESS_DEFAULT;
        return currentConsciousness;
    }

    /**
     * Return consciousness color
     * @param consciousnessLevel 0-1
     * @return
     */
    public static Color getConsciousnessColorByLevel(float consciousnessLevel) {
//        log.info(ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_STABLE_THRESHOLD));
        Color returnColor = Misc.getPositiveHighlightColor();
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_STABLE_THRESHOLD)) {
            returnColor = ba_variablemanager.BA_CONSCIOUSNESS_COLOR.get(ba_variablemanager.BA_CONSCIOUSNESS_STABLE_THRESHOLD);
        }
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_UNSTEADY_THRESHOLD)) {
            returnColor = ba_variablemanager.BA_CONSCIOUSNESS_COLOR.get(ba_variablemanager.BA_CONSCIOUSNESS_UNSTEADY_THRESHOLD);
        }
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_WEAKENED_THRESHOLD)) {
            returnColor = ba_variablemanager.BA_CONSCIOUSNESS_COLOR.get(ba_variablemanager.BA_CONSCIOUSNESS_WEAKENED_THRESHOLD);
        }
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_FRAGILE_THRESHOLD)) {
            returnColor = ba_variablemanager.BA_CONSCIOUSNESS_COLOR.get(ba_variablemanager.BA_CONSCIOUSNESS_FRAGILE_THRESHOLD);
        }
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_CRITICAL_THRESHOLD)) {
            returnColor = ba_variablemanager.BA_CONSCIOUSNESS_COLOR.get(ba_variablemanager.BA_CONSCIOUSNESS_CRITICAL_THRESHOLD);
        }
        return returnColor;
    }

    /**
     * Use for getting person anatomy including limb and bionic installed on the limb. Sort by person variant's limb order.
     * @param person
     * @return
     */
    public static List<ba_bionicAugmentedData> getBionicAnatomyList(PersonAPI person) {
        //create random bionic
//        installRandomBionic(person);

        //return list with full limb details
        List<ba_bionicAugmentedData> anatomyList = new ArrayList<>();
        HashMap<ba_limbmanager.ba_limb, List<ba_bionicitemplugin>> bionicsInstalledList = ba_bionicmanager.getListLimbAndBionicInstalled(person);
        String personGenericVariant = getAnatomyVariantTag(person);
        List<String> variantAnatomy = ba_variantmanager.variantList.get(personGenericVariant);
        for (String limbString: variantAnatomy) {
            ba_limbmanager.ba_limb limb = ba_limbmanager.getLimb(limbString);
            if(bionicsInstalledList.get(limb) != null) {
                List<ba_bionicitemplugin> bionicsInstalled = bionicsInstalledList.get(limb);
                anatomyList.add(new ba_bionicAugmentedData(limb, bionicsInstalled));
            } else {
                anatomyList.add(new ba_bionicAugmentedData(limb, new ArrayList<ba_bionicitemplugin>()));
            }
        }
        return anatomyList;
    }
    /**
     * @param bionic the bionic going to be installed
     * @param limb the limb to install bionic
     * @param person the person that installing the bionic
     * @return
     */
    public static boolean checkIfCanInstallBionic(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person) {
        if(limb.limbGroupList.contains(bionic.bionicLimbGroupId)) {
            //conflicts
            if(ba_bionicmanager.checkIfBionicConflicted(bionic, person)) return false;

            float currentBrm = person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);
            float limitBrm = person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
            float conscious = person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);;
            for(ba_bionicAugmentedData data: getBionicAnatomyList(person)) {
                if(data.limb.limbId.equals(limb.limbId) &&
                        !data.bionicInstalled.contains(bionic) &&
                        currentBrm + bionic.brmCost <= limitBrm &&
                        conscious - bionic.consciousnessCost > 0
                ) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * @param bionic the bionic going to be removed
     * @param limb the limb to remove bionic
     * @param person the person that removing the bionic
     * @return
     */
    public static boolean checkIfCanRemoveBionic(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person) {
        if(limb.limbGroupList.contains(bionic.bionicLimbGroupId)) {
            for(ba_bionicAugmentedData data: getBionicAnatomyList(person)) {
                if(data.limb.limbId.equals(limb.limbId) && data.bionicInstalled.contains(bionic)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean checkIfCanEditLimb(ba_limbmanager.ba_limb limb, PersonAPI person) {
        for(ba_bionicAugmentedData data: getBionicAnatomyList(person)) {
            if(data.limb.limbId.equals(limb.limbId)) {
                return true;
            }
        }
        return false;
    }
    public static boolean installBionic(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person) {
        if(checkIfCanInstallBionic(bionic, limb, person)) {
            person.addTag(bionic.bionicId+":"+limb.limbId);
            //todo: add class to handle interaction with player inventory
            SpecialItemData specialItem = new SpecialItemData(bionic.bionicId, null);
            Global.getSector().getPlayerFleet().getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, specialItem, 1);
            setUpDynamicStats();
            if(bionic.effectScript != null) {
                bionic.effectScript.onInstall(person, limb, bionic);
            }
            return true;
        } else {
            log.error("Can't install "+ bionic.bionicId + " on " + limb.limbId);
        }
        return false;
    }
    public static boolean removeBionic(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person) {
        if(checkIfCanRemoveBionic(bionic, limb, person)) {
            person.removeTag(bionic.bionicId+":"+limb.limbId);
            SpecialItemData specialItem = new SpecialItemData(bionic.bionicId, null);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(specialItem, 1);
            setUpDynamicStats();
            if(bionic.effectScript != null) {
                bionic.effectScript.onRemove(person, limb, bionic);
            }
            return true;
        } else {
            log.error("Can't remove "+ bionic.bionicId + " on " + limb.limbId);
        }
        return false;
    }
    protected static void installRandomBionic(PersonAPI person) {
        //todo: setting.json that control random installment
        int installedBionicCount = 0;
        int totalBionicInstall = 2;
        int currentTry = 0;
        int maxTotalTries = 50;
        if(!person.hasTag(ba_variablemanager.BA_RANDOM_BIONIC_GENERATED_TAG)) {
            while(currentTry < maxTotalTries && installedBionicCount < totalBionicInstall){
                List<String> randomBionics = ba_bionicmanager.getRandomBionic(2);
                for (String random: randomBionics) {
                    ba_bionicitemplugin bionic = ba_bionicmanager.getBionic(random);
                    WeightedRandomPicker<ba_limbmanager.ba_limb> randomLimbPicker = new WeightedRandomPicker<>();
                    randomLimbPicker.addAll(ba_limbmanager.getLimbListFromGroupOnPerson(bionic.bionicLimbGroupId, person));
                    ba_limbmanager.ba_limb selectedLimb = randomLimbPicker.pick();
                    boolean success = installBionic(bionic, selectedLimb, person);
                    if(success) {
                        installedBionicCount++;
                    }
                    currentTry++;
                }
            }
            person.addTag(ba_variablemanager.BA_RANDOM_BIONIC_GENERATED_TAG);
        }
    }
    public static class ba_bionicAugmentedData {
        public ba_limbmanager.ba_limb limb;
        public List<ba_bionicitemplugin> bionicInstalled;
        ba_bionicAugmentedData(ba_limbmanager.ba_limb limb, List<ba_bionicitemplugin> bionic) {
            this.limb = limb;
            this.bionicInstalled = bionic;
        }
    }
}

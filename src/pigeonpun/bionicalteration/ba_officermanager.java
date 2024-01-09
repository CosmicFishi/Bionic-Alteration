package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.variant.ba_variantmanager;

import java.util.*;
import java.util.List;

import static pigeonpun.bionicalteration.variant.ba_variantmanager.getAnatomyVariantTag;

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
    //todo: check if refresh on save/game load run this
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
        /*
         * The idea is to have the lower the consciousness is the higher the DP cost, the CR cost is, the maintenance is
         * Have to a chance to change personality in combat to a different one like aggressive/reckless/fearless
         */
        for(PersonAPI person: listPersons) {
            //consciousness
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).modifyFlat(ba_variablemanager.BA_CONSCIOUSNESS_SOURCE_KEY, setUpConsciousness(person));
            //BRM limit
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).modifyFlat(ba_variablemanager.BA_BRM_LIMIT_SOURCE_KEY, setUpBRMLimit(person));
            //BRM current
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).modifyFlat(ba_variablemanager.BA_BRM_CURRENT_SOURCE_KEY, setUpBRMCurrent(person));

            if(ba_bionicmanager.checkIfHaveBionicInstalled(person)) {
                List<ba_bionicAugmentedData> list = getBionicAnatomyList(person);
                for(ba_bionicAugmentedData data: list) {
                    for(ba_bionicitemplugin bionic: data.bionicInstalled) {
                        updatePersonStatsOnInteract(bionic, data.limb, person, true);
                    }
                }
            }
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
        //todo: add extra tags for person to calculate BRM/Consciousness mult ?
        int brmLimit = (int) (person.getStats().getLevel() * ba_variablemanager.BA_BRM_LIMIT_BONUS_PER_LEVEL);
        return brmLimit;
    }
    protected static int setUpBRMCurrent(PersonAPI person) {
        int brmCurrent = 0;
        return brmCurrent;
    }
    protected static float setUpConsciousness(PersonAPI person) {
        float currentConsciousness = ba_variablemanager.BA_CONSCIOUSNESS_DEFAULT;
        return currentConsciousness;
    }

    /**
     * Update necessary stats on a person
     * @param bionic
     * @param limb
     * @param person
     * @param isInstall
     */
    public static void updatePersonStatsOnInteract(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person, boolean isInstall) {
        updateConsciousness(bionic, limb, person, isInstall);
        updateCurrentBRM(bionic, limb, person, isInstall);
    }

    /**
     * Update consciousness stats on a person. use .modifyFlat()
*    * Note: Key should be {@code  bionicId:limbId} if you are planning to modify the stats. Important for the {@code bionicId} to be in the front so UI can display correctly
     * @param bionic
     * @param limb
     * @param person
     * @param isInstall
     */
    public static void updateConsciousness(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person, boolean isInstall) {
        String key = bionic.bionicId + ":" + limb.limbId;
        if(isInstall) {
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).modifyFlat(key, -bionic.consciousnessCost);
        } else {
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).unmodifyFlat(key);
        }
    }

    /**
     * Update current BRM stats on a person. use .modifyFlat()
     * Note: Key should be {@code  bionicId:limbId} if you are planning to modify the stats. Important for the {@code bionicId} to be in the front so UI can display correctly
     * @param bionic
     * @param limb
     * @param person
     * @param isInstall
     */
    public static void updateCurrentBRM(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person, boolean isInstall) {
        String key = bionic.bionicId + ":" + limb.limbId;
        if(isInstall) {
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).modifyFlat(key, bionic.brmCost);
        } else {
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).unmodifyFlat(key);
        }
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
            for(ba_bionicAugmentedData data: getBionicAnatomyList(person)) {
                if(data.limb.limbId.equals(limb.limbId) &&
                        !data.bionicInstalled.contains(bionic) &&
                        checkIfCurrentBRMLowerThanLimitOnInstall(bionic, person) &&
                        checkIfConsciousnessReduceAboveZeroOnInstall(bionic, person) &&
                        checkIfBionicInstallableBaseOnPersonType(bionic, person)
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check the person type (Player/Admin/Officer) <br>
     * True for player (meaning the player can install both admins bionic and officer bionic <br>
     * If is admin/officer, check if the bionic can be installed on them or not base on {@code isCaptainBionic}
     * @param bionic
     * @param person
     * @return
     */
    public static boolean checkIfBionicInstallableBaseOnPersonType(ba_bionicitemplugin bionic, PersonAPI person) {
        if(person.isPlayer()) return true;
        return isOfficer(person) == bionic.isCaptainBionic;
    }
    public static boolean checkIfCurrentBRMLowerThanLimitOnInstall(ba_bionicitemplugin bionic, PersonAPI person) {
        float currentBrm = person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);
        float limitBrm = person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
        return currentBrm + bionic.brmCost <= limitBrm;
    }
    public static boolean checkIfConsciousnessReduceAboveZeroOnInstall(ba_bionicitemplugin bionic, PersonAPI person) {
        float conscious = person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
        return conscious - bionic.consciousnessCost > 0;
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
            updatePersonStatsOnInteract(bionic, limb, person, true);
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
            updatePersonStatsOnInteract(bionic, limb, person, false);
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
    public static String getProfessionText(PersonAPI person) {
        String profString = "";
        if(person.isPlayer()) {
            return "Captain/Admin";
        }
        if(person.getFleet() != null) {
            if(isOfficer(person)) profString = "Captain (Idle)";
            if(Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(person) != null) {
                profString = "Captain";
            }
        } else {
            if(!isOfficer(person)) profString = "Admin (Idle)";
            if (person.getMarket() != null) {
                MarketAPI market = person.getMarket();
                if(market.getAdmin() == person) {
                    profString = "Admin";
                }
            }
        }
        return profString;
    }
    public static boolean isOfficer(PersonAPI person) {
        for (AdminData admin: Global.getSector().getCharacterData().getAdmins()) {
            if(admin.getPerson().getId().equals(person.getId())) return false;
        }
        if(person.getFleet() != null) {
            if(Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy() != null) {
                for(OfficerDataAPI member: Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
                    if(member.getPerson().getId().equals(person.getId())) return true;
                }
                return true;
            }
        }
        return false;
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

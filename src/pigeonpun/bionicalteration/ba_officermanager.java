package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.faction.ba_factiondata;
import pigeonpun.bionicalteration.faction.ba_factionmanager;
import pigeonpun.bionicalteration.inventory.ba_inventoryhandler;
import pigeonpun.bionicalteration.overclock.ba_overclock;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;
import pigeonpun.bionicalteration.utils.ba_utils;
import pigeonpun.bionicalteration.variant.ba_variantmanager;

import java.util.*;
import java.util.List;

import static pigeonpun.bionicalteration.variant.ba_variantmanager.getPersonVariantTag;

/**
 * Handle bionic related stuffs
 * @author PigeonPun
 */
public class ba_officermanager {
    public static List<PersonAPI> listPersons = new ArrayList<>();
    static Logger log = Global.getLogger(ba_officermanager.class);
    public static void onSaveLoad() {
        //disable random bionic generation on new game
        if(!bionicalterationplugin.isAllowBionicsToSpawnInPlayerFleetOnNewSave && !Global.getSector().getMemoryWithoutUpdate().contains(ba_variablemanager.BA_BIONIC_ON_NEW_GAME_KEY)) {
            for(PersonAPI person: getListOfficerFromFleet(null, true)) {
                person.addTag(ba_variablemanager.BA_RANDOM_BIONIC_GENERATED_TAG);
            }
            Global.getSector().getMemoryWithoutUpdate().set(ba_variablemanager.BA_BIONIC_ON_NEW_GAME_KEY, true);
        }
        refresh(null);
    }

    /**
     * Refresh the entire list person plus all the other stats set up needed <br>
     * @param listOfficer if Null, player fleet's officers will be loaded into listPersons
     * Call when first init
     */
    public static void refresh(List<PersonAPI> listOfficer) {
        refreshListPerson(listOfficer);
        setUpVariant(listPersons);
        setUpDynamicStats(listPersons);
        setUpBionic(listPersons);
        setUpSkill(listPersons);
    }
    /**
     * Set up all the needed stats/bionic/skill for encountering officer from other fleet to display bionics
     * @param listOfficers
     */
    public static void setUpListOfficers(List<PersonAPI> listOfficers) {
        setUpVariant(listOfficers);
        setUpDynamicStats(listOfficers);
        setUpBionic(listOfficers);
        setUpSkill(listOfficers);
    }
    //create new admin
    //runcode import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent; import com.fs.starfarer.api.impl.campaign.ids.Factions; PersonAPI person = OfficerManagerEvent.createAdmin(Global.getSector().getFaction(Factions.MERCENARY), 1, new Random()); Global.getSector().getCharacterData().addAdmin(person);
    public static List<PersonAPI> refreshListPerson(List<PersonAPI> listOfficers) {
        listPersons.clear();
        List<PersonAPI> listP = new ArrayList<>();
        if(listOfficers == null) {
            listP.addAll(getListOfficerFromFleet(null, true));
        } else {
            listP.addAll(listOfficers);
        }
        listPersons.addAll(listP);
        return listPersons;
    }
    public static void setUpVariant(List<PersonAPI> listOfficers) {
        for(PersonAPI person: listOfficers) {
            if(getPersonVariantTag(person) == null && person.getFaction() != null) {
                String randomVariant = ba_variantmanager.getRandomVariantFromFaction(person.getFaction().getId());
                person.addTag(randomVariant);
            }
        }
    }
    public static void setUpDynamicStats(List<PersonAPI> listOfficers) {
        for(PersonAPI person: listOfficers) {
            //consciousness
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).modifyFlat(ba_variablemanager.BA_CONSCIOUSNESS_SOURCE_KEY, setUpConsciousness(person));
            //BRM limit
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).modifyFlat(ba_variablemanager.BA_BRM_LIMIT_SOURCE_KEY, setUpBRMLimit(person));
            //BRM current
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).modifyFlat(ba_variablemanager.BA_BRM_CURRENT_SOURCE_KEY, setUpBRMCurrent(person));

            if(ba_bionicmanager.checkIfHaveBionicInstalled(person)) {
                List<ba_bionicAugmentedData> list = getBionicAnatomyList(person);
                for(ba_bionicAugmentedData data: list) {
                    updatePersonStatsOnInteract(data.bionicInstalled, data.limb, person, true);
                }
            }
        }
    }
    public static void setUpSkill(List<PersonAPI> listOfficers) {
        for(PersonAPI person: listOfficers) {
            for (MutableCharacterStatsAPI.SkillLevelAPI skill: person.getStats().getSkillsCopy()) {
                if (!skill.getSkill().getId().equals(ba_variablemanager.BA_BIONIC_SKILL_ID) && ba_bionicmanager.checkIfHaveBionicInstalled(person)) {
                    person.getStats().setSkillLevel(ba_variablemanager.BA_BIONIC_SKILL_ID, 1);
                }
            }
        }
    }

    /**
     * Bionics choosing will be base on faction_data.json
     * Note: If bionicUseOverride array length is 0 even when defined in the faction_data.json will be ignored and use the bionicUse from the faction instead.
     */
    public static void setUpBionic(List<PersonAPI> listOfficer) {
        for(PersonAPI person : listOfficer) {
            int currentTry = 0;
            int maxTotalTries = 50;
            if(person.getFaction() == null) {
                continue;
            }
            ba_factiondata factionData = ba_factionmanager.getFactionData(person.getFaction().getId());
            ba_factiondata.ba_factionVariantDetails personVariant = null;
            for(ba_factiondata.ba_factionVariantDetails detail: factionData.variantDetails) {
                if(detail.variant.variantId.equals(ba_variantmanager.getPersonVariantTag(person))) {
                    personVariant = detail;
                }
            }
            if(!person.hasTag(ba_variablemanager.BA_RANDOM_BIONIC_GENERATED_TAG) && personVariant != null) {
                WeightedRandomPicker<String> randomBionics = new WeightedRandomPicker<>();
                if(personVariant.bionicUseIdsOverride != null && personVariant.bionicUseIdsOverride.size() != 0) {
                    for(ba_factiondata.ba_bionicUseIdDetails idDetails: personVariant.bionicUseIdsOverride) {
                        if(ba_bionicmanager.getBionic(idDetails.bionic.bionicId) != null) {
                            randomBionics.add(idDetails.bionic.bionicId, idDetails.spawnWeight);
                        }
                    }
                }
                if(personVariant.bionicUseTagsOverride != null && personVariant.bionicUseTagsOverride.size() != 0) {
                    for(ba_factiondata.ba_bionicUseTagDetails tagDetails: personVariant.bionicUseTagsOverride) {
                        for(String bionicId: ba_bionicmanager.getListBionicsIdFromTag(tagDetails.tag)) {
                            randomBionics.add(bionicId, tagDetails.spawnWeight);
                        }
                    }
                }
                //if cant find any overriding bionics
                if(randomBionics.getTotal() == 0) {
                    for(ba_factiondata.ba_bionicUseIdDetails idDetails: factionData.bionicUseIdsList) {
                        if(ba_bionicmanager.getBionic(idDetails.bionic.bionicId) != null) {
                            randomBionics.add(idDetails.bionic.bionicId, idDetails.spawnWeight);
                        }
                    }
                    for(ba_factiondata.ba_bionicUseTagDetails tagDetails: factionData.bionicUseTagsList) {
                        for(String bionicId: ba_bionicmanager.getListBionicsIdFromTag(tagDetails.tag)) {
                            randomBionics.add(bionicId, tagDetails.spawnWeight);
                        }
                    }
                }
                //cryopod officer
                if(checkIfCryopodOfficer(person)) {
                    randomBionics.addAll(getListPreferBionicsForCryopodOfficer());
                }
                while(currentTry < maxTotalTries && setUpBionicConditions(person, factionData)) {
                    String bionicId = randomBionics.pick(ba_utils.getRandom());
                    ba_bionicitemplugin bionic = ba_bionicmanager.getBionic(bionicId);
                    WeightedRandomPicker<ba_limbmanager.ba_limb> randomLimbPicker = new WeightedRandomPicker<>();
                    randomLimbPicker.addAll(ba_limbmanager.getLimbListFromGroupOnPerson(bionic.bionicLimbGroupId, person));
                    ba_limbmanager.ba_limb selectedLimb = randomLimbPicker.pick();
                    boolean success = installBionic(bionic, selectedLimb, person, false);
                    if(success) {
                        randomBionics.remove(bionicId);
                    }
                    currentTry++;
                }
                person.addTag(ba_variablemanager.BA_RANDOM_BIONIC_GENERATED_TAG);
            }
        }
    }
    protected static boolean checkIfCryopodOfficer(PersonAPI person) {
        if(person.isAICore()) return false;
        if(person.getMemoryWithoutUpdate().get("$ome_adminTier") != null) {
            return true;
        }
        if(person.getMemoryWithoutUpdate().get(MemFlags.EXCEPTIONAL_SLEEPER_POD_OFFICER) != null) {
            return true;
        }
        return false;
    }
    protected static WeightedRandomPicker<String> getListPreferBionicsForCryopodOfficer() {
        WeightedRandomPicker<String> listBionic = new WeightedRandomPicker<>();
        for(String bionic: ba_bionicmanager.getListBionicsIdFromTag("ba_bionic_t2")) {
            listBionic.add(bionic, 10f);
        }
        return listBionic;
    }
    protected static boolean setUpBionicConditions(PersonAPI person, ba_factiondata factionData) {
        if(factionData.maxBionicUseCount > 0 && ba_bionicmanager.getListStringBionicInstalled(person).size() > factionData.maxBionicUseCount) {
            return false;
        }
        if(!checkIfCurrentBRMLowerThanLimit(person, factionData.targetBRMLevel)) {
            return false;
        }
        if(!checkIfCurrentConsciousLowerThanLimit(person, factionData.targetConsciousLevel)) {
            return false;
        }
        return true;
    }
    protected static int setUpBRMLimit(PersonAPI person) {
        //todo: add extra tags for person to calculate BRM/Consciousness mult ?
        int brmLimit = (int) (person.getStats().getLevel() * ba_variablemanager.BA_BRM_LIMIT_BONUS_PER_LEVEL);
        if(isCaptainOrAdmin(person, false).equals(ba_profession.ADMIN)) {
            brmLimit = (int) (person.getStats().getLevel() * ba_variablemanager.BA_BRM_LIMIT_BONUS_PER_LEVEL_ADMIN);
        }
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
     * @param fleets fleets that will get the list officer from
     * @param isPlayer If true, only get from player fleet
     */
    public static List<PersonAPI> getListOfficerFromFleet(List<CampaignFleetAPI> fleets, boolean isPlayer) {
        List<PersonAPI> listP = new ArrayList<>();
        if(!isPlayer) {
            for (CampaignFleetAPI fleet : fleets) {
                if(!fleet.isPlayerFleet()) {
                    for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
                        if (member.isFighterWing()) continue;
                        if (!member.getCaptain().isDefault() && !member.getCaptain().isAICore()) {
                            listP.add(member.getCaptain());
                        }
                    }
                }
            }
        } else {
            listP.add(Global.getSector().getPlayerPerson());
            List<OfficerDataAPI> listPlayerMember = new ArrayList<>();
            if(Global.getSector().getPlayerFleet() != null) {
                listPlayerMember = Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy();
            }
            for (OfficerDataAPI officer: listPlayerMember) {
                if(!officer.getPerson().isAICore() && !officer.getPerson().isDefault()) {
                    listP.add(officer.getPerson());
                }
            }
            for (AdminData admin: Global.getSector().getCharacterData().getAdmins()) {
                if(!admin.getPerson().isDefault() && !admin.getPerson().isAICore()) {
                    listP.add(admin.getPerson());
                }
            }
        }

        return listP;
    }
    /**
     * Update necessary stats on a person
     * @param bionic
     * @param limb
     * @param person
     * @param isInstall
     */
    public static void updatePersonStatsOnInteract(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person, boolean isInstall) {
        if(bionic != null) {
            updateConsciousness(bionic, limb, person, isInstall);
            updateCurrentBRM(bionic, limb, person, isInstall);
        }
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
     * Bionic and appliedOverclock can be null
     * @param person
     * @return
     */
    public static List<ba_bionicAugmentedData> getBionicAnatomyList(PersonAPI person) {
        //return list with full limb details
        List<ba_bionicAugmentedData> anatomyList = new ArrayList<>();
        HashMap<ba_limbmanager.ba_limb, ba_bionicmanager.bionicData> bionicsInstalledList = ba_bionicmanager.getListLimbAndBionicInstalled(person);
        String personGenericVariant = getPersonVariantTag(person);
        if(personGenericVariant != null && ba_variantmanager.variantList.get(personGenericVariant) != null) {
            List<String> variantAnatomy = ba_variantmanager.variantList.get(personGenericVariant).limbIdList;
            for (String limbString: variantAnatomy) {
                ba_limbmanager.ba_limb limb = ba_limbmanager.getLimb(limbString);
                if(bionicsInstalledList.get(limb) != null) {
                    ba_bionicmanager.bionicData bionicsInstalled = bionicsInstalledList.get(limb);
                    anatomyList.add(new ba_bionicAugmentedData(limb, bionicsInstalled.bionic, bionicsInstalled.overclock));
                } else {
                    anatomyList.add(new ba_bionicAugmentedData(limb, null, null));
                }
            }
        } else {
            log.info("Error, can't find anatomy of variant: " + personGenericVariant + " for officer from " + (person.getFaction() != null ? person.getFaction().getDisplayName(): "(No faction)") + " with tags " + person.getTags());
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
        if(!checkIfBionicLimbGroupContainSelected(bionic, limb)) return false;
        if(ba_bionicmanager.checkIfBionicConflicted(bionic, person)) return false;
        if(!checkIfCurrentBRMLowerThanLimitOnInstall(bionic, person)) return false;
        if(!checkIfConsciousnessReduceAboveZeroOnInstall(bionic, person)) return false;
        if(!checkIfBionicInstallableBaseOnPersonType(bionic, person)) return false;
        if(checkIfBionicIsAlreadyInstalled(bionic, limb, person)) return false;
        return true;
    }
    public static boolean checkIfBionicLimbGroupContainSelected(ba_bionicitemplugin selectedBionic, ba_limbmanager.ba_limb limb) {
        return limb.limbGroupList.contains(selectedBionic.bionicLimbGroupId);
    }
    public static boolean checkIfBionicIsAlreadyInstalled(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person) {
        if(limb.limbGroupList.contains(bionic.bionicLimbGroupId)) {
            for(ba_bionicAugmentedData data: getBionicAnatomyList(person)) {
                //ONLY ONE BIONIC PER LIMB, sorry forks :d
                if(data.limb.limbId.equals(limb.limbId) && data.bionicInstalled != null) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Check the person type (Player/Admin/Officer) <br>
     * True for player (meaning the player can install both admins bionic and officer bionic <br>
     * If is admin/officer, check if the bionics isApplyCaptainEffect and isApplyAdminEffect
     * @param bionic
     * @param person
     * @return
     */
    public static boolean checkIfBionicInstallableBaseOnPersonType(ba_bionicitemplugin bionic, PersonAPI person) {
        if(person.isPlayer() || (bionic.isApplyCaptainEffect && bionic.isApplyAdminEffect)) return true;
        return (isCaptainOrAdmin(person, false).equals(ba_profession.CAPTAIN) && bionic.isApplyCaptainEffect) || (isCaptainOrAdmin(person, false).equals(ba_profession.ADMIN) && bionic.isApplyAdminEffect);
    }
    public static boolean checkIfCurrentBRMLowerThanLimitOnInstall(ba_bionicitemplugin bionic, PersonAPI person) {
        float currentBrm = person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);
        float limitBrm = person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
        if(bionicalterationplugin.isBRMCapDisable) return true; //disabling BRM limit
        return currentBrm + bionic.brmCost <= limitBrm;
    }
    public static boolean checkIfConsciousnessReduceAboveZeroOnInstall(ba_bionicitemplugin bionic, PersonAPI person) {
        float conscious = person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
        return (conscious - bionic.consciousnessCost) >= 0;
    }

    /**
     * @param person
     * @param limit percentage. Will be calculated with the BRM limit to get the final comparing float
     * @return
     */
    public static boolean checkIfCurrentBRMLowerThanLimit(PersonAPI person, float limit) {
        float currentBrm = person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);
        float limitBrm = person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
        if(bionicalterationplugin.isBRMCapDisable) return true; //disabling BRM limit
        return currentBrm < limit * limitBrm;
    }
    public static boolean checkIfCurrentConsciousLowerThanLimit(PersonAPI person, float limit) {
        float current = person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
        return current > limit;
    }
    /**
     * @param bionic the bionic going to be removed
     * @param limb the limb to remove bionic
     * @param person the person that removing the bionic
     * @return
     */
    public static boolean checkIfCanRemoveBionic(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person) {
        if(!bionic.isAllowedRemoveAfterInstall) return false;
        if(limb.limbGroupList.contains(bionic.bionicLimbGroupId)) {
            for(ba_bionicAugmentedData data: getBionicAnatomyList(person)) {
                if(data.limb.limbId.equals(limb.limbId) && data.bionicInstalled.getId().equals(bionic.getId())) {
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
    /**
     * Start by looking at the bionics at the player inventory, if visible => remove the bionic
     * Then after removing => generate a new tag and put it on the person
     * If the bionic overclocked, => override previous bionic tag, generate a new one => put it in person
     * @param bionic
     * @param limb
     * @param person
     * @param removeBionicOnInstall
     * @return
     */
    public static boolean installBionic(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person, boolean removeBionicOnInstall) {
        if(checkIfCanInstallBionic(bionic, limb, person)) {
            boolean removeSuccessful = true;
            ba_overclock overclock = ba_overclockmanager.getOverclockFromItem(bionic);
            if(removeBionicOnInstall) {
//                SpecialItemData specialItem = new SpecialItemData(bionic.bionicId, null);
//                if(overclock != null) {
//                    specialItem = new SpecialItemData(bionic.bionicId, overclock.id);
//                }
//                removeSuccessful = Global.getSector().getPlayerFleet().getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, specialItem, 1);
                removeSuccessful = ba_inventoryhandler.removeFromContainer(bionic);
            }
            if(removeSuccessful) {
                String bionicTag = convertToTag(bionic, limb, null);
                if(overclock != null) {
                    bionicTag = convertToTag(bionic, limb, overclock.id);
                }
                person.addTag(bionicTag);
                updatePersonStatsOnInteract(bionic, limb, person, true);
                if(bionic.isApplyAdminEffect && !getPersonGovernMarkets(person).isEmpty()) {
                    for(MarketAPI market: getPersonGovernMarkets(person)) {
                        if(!market.hasCondition(ba_variablemanager.BA_MARKET_CONDITION_ID)) {
                            market.addCondition(ba_variablemanager.BA_MARKET_CONDITION_ID);
                        }
                    }
                }
                if(bionic != null) {
                    bionic.onInstall(person, limb, bionic);
                }
            }
            if(!removeSuccessful) {
                log.error("Can't find bionic item in player inventory => abort installing");
            }
            return removeSuccessful;
        } else {
            if(bionicalterationplugin.isDevmode) {
//                log.error("Can't install "+ bionic.bionicId + " on " + limb.limbId);
            }
        }
        return false;
    }

    /**
     * Remove the bionic tag, if can find tag => add the bionic to player inventory
     * @param bionic
     * @param limb
     * @param person
     * @return
     */
    public static boolean removeBionic(ba_bionicitemplugin bionic, ba_limbmanager.ba_limb limb, PersonAPI person) {
        if(checkIfCanRemoveBionic(bionic, limb, person)) {
            if(bionic.isApplyAdminEffect && !getPersonGovernMarkets(person).isEmpty()) {
                for(MarketAPI market: getPersonGovernMarkets(person)) {
                    if(market.hasCondition(ba_variablemanager.BA_MARKET_CONDITION_ID)) {
                        market.removeCondition(ba_variablemanager.BA_MARKET_CONDITION_ID);
                    }
                }
            }
            //remove later because the bionic tag is needed
            String removingTag = convertToTag(bionic, limb, null);
            boolean canFindTag = false;
            ba_overclock overclock = ba_overclockmanager.getOverclockFromPerson(person, limb);
            if(overclock != null) {
                removingTag = convertToTag(bionic, limb, overclock.id);
            }
            for(String tag: person.getTags()) {
                if(tag.equals(removingTag)) {
                    ba_inventoryhandler.addToContainer(bionic, person, limb);
                    person.removeTag(removingTag);

//                    SpecialItemData specialItem = new SpecialItemData(bionic.bionicId, null);
//                    if(overclock != null) {
//                        //require special item data to do the overclock things
//                        specialItem =  new SpecialItemData(bionic.bionicId, overclock.id);
//                    }
//                    Global.getSector().getPlayerFleet().getCargo().addSpecial(specialItem, 1);
                    updatePersonStatsOnInteract(bionic, limb, person, false);
                    if(bionic != null && bionic.isEffectAppliedAfterRemove) {
                        bionic.onRemove(person, limb, bionic);
                    }
                    canFindTag = true;
                    break;
                }
            }
            if(canFindTag) {
                return true;
            }
            return false;
        } else {
            log.error("Can't remove "+ bionic.bionicId + " on " + limb.limbId);
        }
        return false;
    }

    /**
     * Find existing bionic tag from the person => remove the existing tag => put in new tag which contains the overclock
     * @param bionic
     * @param overclockId
     * @param limb can be null
     * @param person can be null
     * @return
     */
    public static boolean overclockBionic(ba_bionicitemplugin bionic, String overclockId, ba_limbmanager.ba_limb limb, PersonAPI person) {
        //Exchange the overclock if the bionic has the overclock. Get the overclock from person -> convert it
        //For item, get the special data -> remove the bionic from the player inventory -> add the new bionic with the new special data
        if(ba_overclockmanager.isBionicOverclockable(bionic)) {
            boolean removeSuccessful = false;
            ba_overclock selectedOverclock = ba_overclockmanager.getOverclock(overclockId);
            if(selectedOverclock != null && ba_inventoryhandler.getEvoshardsFromPlayerInventory() > selectedOverclock.upgradeCost) {
                SpecialItemData specialItem = new SpecialItemData(ba_variablemanager.BA_OVERCLOCK_ITEM, null);
                removeSuccessful = Global.getSector().getPlayerFleet().getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, specialItem, selectedOverclock.upgradeCost);
            }
            if(removeSuccessful) {
                if(limb != null && person != null) {
                    //overclock bionic while the bionic is on the person
                    String removingTag = convertToTag(bionic, limb, null);
                    ba_overclock addedOverclock = ba_overclockmanager.getOverclockFromPerson(person, limb);
                    if(addedOverclock != null) {
                        removingTag = convertToTag(bionic, limb, addedOverclock.id);
                    }
                    if(person.getTags().contains(removingTag)) {
                        person.getTags().remove(removingTag);
                        String newTag = convertToTag(bionic, limb, overclockId);
                        person.addTag(newTag);
                    }
                    updatePersonStatsOnInteract(bionic, limb, person, true);
                } else {
                    ba_overclockmanager.overclockBionicItem(bionic, overclockId);
                }
            }
            if(!removeSuccessful) {
                log.error("Can't find bionic item in player inventory or overclock ID => abort installing");
            }
            return removeSuccessful;
        }

        return false;
    }

    public static String convertToTag(@NotNull ba_bionicitemplugin bionic, @NotNull ba_limbmanager.ba_limb limb, @Nullable String overclockId) {
        if(bionic != null && limb != null) {
            if(overclockId != null) {
                return bionic.bionicId+":"+limb.limbId + ":" + overclockId;
            }
            return bionic.bionicId+":"+limb.limbId;
        }
        return "";
    }
    /**
     * To get all person tag related to bionic augmentation and put it in person memory <br>
     * @param person
     */
    public static void convertPersonBionicTagToPersonMemory(PersonAPI person) {
        //todo: Convert all tag to memory in 0.0.4
//        if(person.getMemoryWithoutUpdate().get(ba_variablemanager.BA_PERSON_MEMORY_BIONIC_KEY) == null) {
//            person.getMemoryWithoutUpdate().set(ba_variablemanager.BA_PERSON_MEMORY_BIONIC_KEY, new ba_personmemorydata());
//        }
    }
    public static FleetMemberAPI getFleetMemberFromFleet(PersonAPI person, List<CampaignFleetAPI> fleets, boolean isPlayer) {
        List<PersonAPI> listP = new ArrayList<>();
        if(!isPlayer) {
            for (CampaignFleetAPI fleet : fleets) {
                if(!fleet.isPlayerFleet()) {
                    for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
                        if (member.isFighterWing()) continue;
                        if (!member.getCaptain().isDefault() && !member.getCaptain().isAICore()) {
                            if(person.getId().equals(member.getCaptain().getId())) {
                                return member;
                            }
                        }
                    }
                }
            }
        } else {
            listP.add(Global.getSector().getPlayerPerson());
            if(Global.getSector().getPlayerFleet() != null) {
                for(FleetMemberAPI member: Global.getSector().getPlayerFleet().getMembersWithFightersCopy()) {
                    if (!member.getCaptain().isDefault() && !member.getCaptain().isAICore()) {
                        if(person.getId().equals(member.getCaptain().getId())) {
                            return member;
                        }
                    }
                }
            }
        }
        return null;
    }
    public static List<MarketAPI> getPersonGovernMarkets(PersonAPI person) {
        List<MarketAPI> governMarkets = new ArrayList<>();
        if(person.getFaction() != null) {
            for(MarketAPI market: Misc.getFactionMarkets(person.getFaction())) {
                if(market.getAdmin().getId().equals(person.getId())) {
                    governMarkets.add(market);
                }
            }
        }
        return governMarkets;
    }
    public static List<PersonAPI> getListPersonsHaveBionic(CampaignFleetAPI fleet) {
        List<PersonAPI> list = new ArrayList<>();
        for(FleetMemberAPI member: fleet.getMembersWithFightersCopy()) {
            if(member.isFighterWing()) continue;
            if(!member.getCaptain().isDefault()) {
                if(!ba_bionicmanager.getListStringBionicInstalled(member.getCaptain()).isEmpty()) {
                    list.add(member.getCaptain());
                }
            }
        }
        return list;
    }
    public static List<ba_bionicitemplugin> getListPotentialBionicDrop(CampaignFleetAPI fleet) {
        List<ba_bionicitemplugin> listBionic = new ArrayList<>();
        List<PersonAPI> listPerson = getListPersonsHaveBionic(fleet);
        if(!listPerson.isEmpty()) {
            for(PersonAPI person: listPerson) {
                listBionic.addAll(ba_bionicmanager.getListBionicInstalled(person));
            }
        }
        return listBionic;
    }

    public static String getProfessionText(PersonAPI person, boolean isDisplayingOtherFleets) {
        if(isDisplayingOtherFleets) {
            //admin for hire
            if(isCaptainOrAdmin(person, true).equals(ba_profession.ADMIN)) {
                return "Admin (For hire)";
            }
            return "Captain";
        }
        String profString = "";
        if(person.isPlayer()) {
            return "Captain/Admin";
        }
        if(person.getFleet() != null) {
            if(isCaptainOrAdmin(person, false).equals(ba_profession.CAPTAIN)) profString = "Captain (Idle)";
            if(Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(person) != null) {
                profString = "Captain";
            }
        } else {
            if(isCaptainOrAdmin(person, false).equals(ba_profession.ADMIN)) profString = "Admin (Idle)";
            if (person.getMarket() != null) {
                MarketAPI market = person.getMarket();
                if(market.getAdmin() == person) {
                    profString = "Admin";
                }
            }
        }
        return profString;
    }

    /**
     * Return ADMIN or CAPTAIN
     * @param person
     * @param isDisplayingOtherFleets
     * @return
     */
    public static ba_profession isCaptainOrAdmin(PersonAPI person, boolean isDisplayingOtherFleets) {
        for (AdminData admin: Global.getSector().getCharacterData().getAdmins()) {
            if(admin.getPerson().getId().equals(person.getId())) return ba_profession.ADMIN;
        }
        if(person.getMarket() != null && person.getMarket().getAdmin().getId().equals(person.getId())) {
            return ba_profession.ADMIN;
        }
        if(person.getFleet() != null) {
            if(Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy() != null) {
                for(OfficerDataAPI member: Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
                    if(member.getPerson().getId().equals(person.getId())) return ba_profession.CAPTAIN;
                }
                return ba_profession.CAPTAIN;
            }
        }
        if(person.getMemoryWithoutUpdate().get("$ome_isAdmin") != null) {
            return ba_profession.ADMIN;
        }
        //if is displaying other fleet => its always captain
        if(isDisplayingOtherFleets) return ba_profession.CAPTAIN;
        return ba_profession.CAPTAIN;
    }
    public enum ba_profession {
        CAPTAIN, ADMIN
    }
    public static class ba_bionicAugmentedData {
        public ba_limbmanager.ba_limb limb;
        public ba_bionicitemplugin bionicInstalled;
        public ba_overclock appliedOverclock = null;
        public ba_bionicAugmentedData(ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic, ba_overclock appliedOverclock) {
            this.limb = limb;
            this.bionicInstalled = bionic;
            this.appliedOverclock = appliedOverclock;
        }
    }
    //todo: this in 0.0.4
    public class ba_personmemorydata {
        public HashMap<String, ba_bionicData> bionicInstalled = new HashMap<>();
        public class ba_bionicData {
            public String bionicId;
            public String overclockId;
            public ba_bionicData(String bionicId, String overclockId) {
                this.bionicId = bionicId;
                this.overclockId = overclockId;
            }
        }
    }
}

package pigeonpun.bionicalteration.overclock;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicSettings;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bioniceffect;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.inventory.ba_inventoryhandler;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Handle overclocking list
 * @author PigeonPun
 */
public class ba_overclockmanager {
    public static Logger log = Global.getLogger(ba_overclockmanager.class);
    public static HashMap<String, ba_overclock> overclockMap = new HashMap<>();
    public static final float evoshardToBRMRate = 1f;
    public static void onApplicationLoad() {
        loadOverclock();
    }
    public static void loadOverclock() {
        List<String> overclockingFiles = MagicSettings.getList(ba_variablemanager.BIONIC_ALTERATION, "overclock_files");
        for (String path : overclockingFiles) {
            log.error("merging overclock files");
            JSONArray overclockData = new JSONArray();
            try {
                overclockData = Global.getSettings().getMergedSpreadsheetDataForMod("overclockId", path, ba_variablemanager.BIONIC_ALTERATION);
            } catch (IOException | JSONException | RuntimeException ex) {
                log.error("unable to read " + path, ex);
            }
            for (int i = 0; i < overclockData.length(); i++) {
                try{
                    JSONObject row = overclockData.getJSONObject(i);
                    ba_overclock overclock = null;
                    try{
                        row.getString("overclockId");
                    }catch (JSONException ex) {
                        continue;
                    }
                    try {
                        if(!Objects.equals(row.getString("effectScript"), "")) {
                            Class<?> clazz = Global.getSettings().getScriptClassLoader().loadClass(row.getString("effectScript"));
                            overclock = (ba_overclock) clazz.newInstance();
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    if(overclock != null) {
                        overclock.setOverclock(
                                row.getString("overclockId"),
                                row.getString("overclockName"),
                                row.getBoolean("isApplyCaptainEffect"),
                                row.getBoolean("isApplyAdminEffect"),
                                row.getInt("upgradeCost"),
                                (float) row.getDouble("prebuiltChance"),
                                (float) row.getDouble("order")
                        );
                        overclockMap.put(overclock.id, overclock);
                    }

                } catch (JSONException ex) {
                    log.error(ex);
                    log.error("Invalid line, skipping");
                }
            }
        }
        log.info(overclockMap);
    }

    /**
     * @param id overclock id
     * @return overclock itself, return null if cant find the overclock
     */
    public static ba_overclock getOverclock(String id) {
        ba_overclock overclock = overclockMap.get(id);
        if(overclock == null) {
            log.error("Can not find overclock of id: "+ id);
        }
        return overclock;
    }

    /**
     * Find the bionic overclock list to see if empty or not. <br>
     * Overclock list empty can be due to: <br>
     * 1. Overclock id doesnt match any overclock id in overclock_data.csv <br>
     * 2. Bionic id doesn't match any bionic id in overclocking_bionic_data.csv <br>
     * @param bionic
     * @return true if overclockable
     */
    public static boolean isBionicOverclockable(ba_bionicitemplugin bionic) {
        ba_bionicitemplugin defaultBionic = ba_bionicmanager.getBionic(bionic.getId());
        return !defaultBionic.overclockList.isEmpty();
    }
    public static boolean overclockBionicItem(ba_bionicitemplugin bionic, String overclocKId) {
//        if(overclocKId != null && !overclocKId.equals("")) {
//            if(isBionicOverclockable(bionic) && bionic.overclockList.contains(overclocKId) && getOverclock(overclocKId) != null) {
//                bionic.appliedOverclock = getOverclock(overclocKId);
//            }
//        }
        if(bionic != null && overclocKId != null) {
//            SpecialItemData specialItemRemoving = null;
//            if(bionic.getAppliedOverclockOnItem() != null) {
//                specialItemRemoving = new SpecialItemData(bionic.bionicId, bionic.getAppliedOverclockOnItem().id);
//            } else {
//                specialItemRemoving = new SpecialItemData(bionic.bionicId, null);
//            }
//            boolean removed = Global.getSector().getPlayerFleet().getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, specialItemRemoving, 1);;
            boolean removed = ba_inventoryhandler.removeFromContainer(bionic);
            if (removed) {
                SpecialItemData specialItemAdd = new SpecialItemData(bionic.bionicId, overclocKId);
                Global.getSector().getPlayerFleet().getCargo().addSpecial(specialItemAdd, 1);
                ba_inventoryhandler.compressAllBionics();
                return true;
            }
        }
        return false;
    }
    public static ba_overclock getOverclockFromPerson(PersonAPI person, ba_limbmanager.ba_limb limb) {
        ba_overclock overclock = null;
//        if(person.getMemoryWithoutUpdate().get(ba_variablemanager.BA_PERSON_MEMORY_BIONIC_KEY) != null && person.getMemoryWithoutUpdate().get(ba_variablemanager.BA_PERSON_MEMORY_BIONIC_KEY) instanceof ba_officermanager.ba_personmemorydata) {
//            ba_officermanager.ba_personmemorydata data = (ba_officermanager.ba_personmemorydata) person.getMemoryWithoutUpdate().get(ba_variablemanager.BA_PERSON_MEMORY_BIONIC_KEY);
//            if(data.bionicInstalled.get(limb.limbId) != null) {
//                ba_officermanager.ba_personmemorydata.ba_bionicData bionicData = data.bionicInstalled.get(limb.limbId);
//                if(bionicData.overclockId != null) {
//                    overclock = getOverclock(bionicData.overclockId);
//                }
//            }
//        }
        List<ba_officermanager.ba_bionicAugmentedData> listData = ba_officermanager.getBionicAnatomyList(person);
        for(ba_officermanager.ba_bionicAugmentedData data: listData) {
            if(data.limb.limbId.equals(limb.limbId)) {
                overclock = data.appliedOverclock;
                break;
            }
        }
        
        return overclock;
    }
    public static ba_overclock getOverclockFromItem(ba_bionicitemplugin bionic) {
        ba_overclock overclock = null;
//        if(person.getMemoryWithoutUpdate().get(ba_variablemanager.BA_PERSON_MEMORY_BIONIC_KEY) != null && person.getMemoryWithoutUpdate().get(ba_variablemanager.BA_PERSON_MEMORY_BIONIC_KEY) instanceof ba_officermanager.ba_personmemorydata) {
//            ba_officermanager.ba_personmemorydata data = (ba_officermanager.ba_personmemorydata) person.getMemoryWithoutUpdate().get(ba_variablemanager.BA_PERSON_MEMORY_BIONIC_KEY);
//            if(data.bionicInstalled.get(limb.limbId) != null) {
//                ba_officermanager.ba_personmemorydata.ba_bionicData bionicData = data.bionicInstalled.get(limb.limbId);
//                if(bionicData.overclockId != null) {
//                    overclock = getOverclock(bionicData.overclockId);
//                }
//            }
//        }
        overclock = bionic.getAppliedOverclockOnItem();

        return overclock;
    }
    public static float computeEvoshardForBionic(ba_bionicitemplugin bionic) {
        return bionic.brmCost * evoshardToBRMRate;
    }
    public static float computeEvoshardForAICore(String coreType) {
        int base = (int) 20f;
        if (Commodities.OMEGA_CORE.equals(coreType)) {
            return 15f * base;
        }
        if (Commodities.ALPHA_CORE.equals(coreType)) {
            return 5f * base;
        }
        if (Commodities.BETA_CORE.equals(coreType)) {
            return 2f * base;
        }
        if (Commodities.GAMMA_CORE.equals(coreType)) {
            return 1f * base;
        }
        return 1f;
    }
}

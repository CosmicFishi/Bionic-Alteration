package pigeonpun.bionicalteration.overclock;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.magiclib.util.MagicSettings;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bioniceffect;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
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
                                row.getBoolean("isApplyAdminEffect"),
                                row.getBoolean("isApplyCaptainEffect"),
                                row.getBoolean("isAdvanceInCombat"),
                                row.getBoolean("isAdvanceInCampaign"),
                                row.getInt("upgradeCost"),
                                (float) row.getDouble("prebuiltChance")
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
        return !bionic.overclockList.isEmpty();
    }
}

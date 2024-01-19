package pigeonpun.bionicalteration.faction;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.utils.ba_utils;
import pigeonpun.bionicalteration.variant.ba_variantmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ba_factionmanager {
    static HashMap<String, ba_factiondata> factionVariantMap = new HashMap<>();
    static Logger log = Global.getLogger(ba_factiondata.class);
    protected static final String FACTION_CSV_PATH = "data/world/factions/factions.csv";
    public static void onApplicationLoad() {
        loadFactionData();
    }
    public static void loadFactionData() {
        try {
            List<String> factionList = new ArrayList<>();
            JSONArray factionCsvList = Global.getSettings().getMergedSpreadsheetData("faction", FACTION_CSV_PATH);
            for (int i = 0; i < factionCsvList.length(); i++) {
                try {
                    JSONObject faction = Global.getSettings().getMergedJSON(factionCsvList.getJSONObject(i).getString("faction"));
                    factionList.add(faction.getString("id"));
                } catch (JSONException ex) {
                    log.error("Invalid line, skipping", ex);
                }
            }
            JSONObject factionController = Global.getSettings().getMergedJSONForMod(ba_variablemanager.FACTION_DATA_PATH, ba_variablemanager.BIONIC_ALTERATION);
            factionList.add("ba_default"); //default selector for factions that don't have their faction variant/bionic data defined.
            for(String factionId: factionList) {
                try {
                    JSONObject factionJsonData = factionController.getJSONObject(factionId);
                    JSONArray variantUse = factionJsonData.getJSONArray("variantUse");
                    ba_factiondata factionData = null;
                    List<ba_factiondata.ba_factionVariantDetails> listVariantDetails = new ArrayList<>();
                    for (int i = 0; i < variantUse.length(); i++) {
                        JSONObject variantData = variantUse.getJSONObject(i);
                        ba_factiondata.ba_factionVariantDetails detail = null;
                        if(ba_variantmanager.getVariant(variantData.getString("variantId")) != null) {
                            try {
                                detail = new ba_factiondata.ba_factionVariantDetails(
                                        ba_variantmanager.getVariant(variantData.getString("variantId")),
                                        (float) variantData.getDouble("spawnChance"),
                                        ba_utils.getListStringFromJsonArray(factionJsonData.getJSONObject("bionicUseOverride").getJSONArray("tags")),
                                        ba_utils.getListStringFromJsonArray(factionJsonData.getJSONObject("bionicUseOverride").getJSONArray("ids"))
                                );
                            } catch (Exception e) {
                                log.info("Can not find bionicUseOverride for " + factionId + " variant " + variantData.getString("variantId") + ". Creating empty list for bionic override");
                                detail = new ba_factiondata.ba_factionVariantDetails(
                                        ba_variantmanager.getVariant(variantData.getString("variantId")),
                                        (float) variantData.getDouble("spawnChance"),
                                        null,
                                        null
                                );
                            }
                            listVariantDetails.add(detail);
                        }
                    }
                    factionData = new ba_factiondata(
                            factionId,
                            listVariantDetails,
                            ba_utils.getListStringFromJsonArray(factionJsonData.getJSONObject("bionicUse").getJSONArray("tags")),
                            ba_utils.getListStringFromJsonArray(factionJsonData.getJSONObject("bionicUse").getJSONArray("ids")),
                            (float) factionJsonData.getDouble("targetBRMLevel"),
                            (float) factionJsonData.getDouble("targetConsciousLevel")
                    );
                    factionVariantMap.put(factionId, factionData);
                } catch (Exception e) {
                    log.error("Unable to find faction bionic data for " + factionId + ". Skipping");
                }
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException("Failed loading ", e);
        }
    }
    /**
     * Get a random variant based on faction. Defined in faction_data.json
     * @param factionId
     * @return
     */
    //todo: change this so it will select from faction's random list
    public static String getRandomFactionVariant(String factionId) {
        WeightedRandomPicker<String> randomPicker = new WeightedRandomPicker<>();

        return randomPicker.pick();
    }
}

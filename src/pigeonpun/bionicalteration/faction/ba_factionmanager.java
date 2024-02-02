package pigeonpun.bionicalteration.faction;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.utils.ba_utils;
import pigeonpun.bionicalteration.variant.ba_variant;
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
                                JSONArray bionicTagsUse = variantData.getJSONObject("bionicUseOverride").getJSONArray("tags");
                                List<ba_factiondata.ba_bionicUseTagDetails> listBionicTagDetails = new ArrayList<>();
                                for (int j = 0; j < bionicTagsUse.length(); j++) {
                                    JSONObject tagData = bionicTagsUse.getJSONObject(i);
                                    ba_factiondata.ba_bionicUseTagDetails tagDetail = new ba_factiondata.ba_bionicUseTagDetails(
                                            tagData.getString("tag"),
                                            (float) tagData.getDouble("spawnWeight")
                                    );
                                    listBionicTagDetails.add(tagDetail);
                                }
                                JSONArray bionicIdsUse = variantData.getJSONObject("bionicUseOverride").getJSONArray("ids");
                                List<ba_factiondata.ba_bionicUseIdDetails> listBionicIdDetails = new ArrayList<>();
                                for (int j = 0; j < bionicIdsUse.length(); j++) {
                                    JSONObject idData = bionicIdsUse.getJSONObject(i);
                                    if(ba_bionicmanager.getBionic(idData.getString("id")) != null) {
                                        ba_factiondata.ba_bionicUseIdDetails idDetail = new ba_factiondata.ba_bionicUseIdDetails(
                                                ba_bionicmanager.getBionic(idData.getString("id")),
                                                (float) idData.getDouble("spawnWeight")
                                        );
                                        listBionicIdDetails.add(idDetail);
                                    } else {
                                        log.info("Can not find bionic ID for " + factionId + " in bionicUse with bionic ID: " + idData.getString("id") + " Skipping.");
                                    }
                                }
                                detail = new ba_factiondata.ba_factionVariantDetails(
                                        ba_variantmanager.getVariant(variantData.getString("variantId")),
                                        (float) variantData.getDouble("spawnWeight"),
                                        listBionicTagDetails,
                                        listBionicIdDetails
                                );
                            } catch (Exception e) {
                                log.info("Can not find bionicUseOverride for " + factionId + " variant " + variantData.getString("variantId") + ". Creating empty list for bionic override");
                                detail = new ba_factiondata.ba_factionVariantDetails(
                                        ba_variantmanager.getVariant(variantData.getString("variantId")),
                                        (float) variantData.getDouble("spawnWeight"),
                                        null,
                                        null
                                );
                            }
                            listVariantDetails.add(detail);
                        }
                    }
                    JSONArray bionicTagsUse = factionJsonData.getJSONObject("bionicUse").getJSONArray("tags");
                    List<ba_factiondata.ba_bionicUseTagDetails> listBionicTagDetails = new ArrayList<>();
                    for (int i = 0; i < bionicTagsUse.length(); i++) {
                        JSONObject tagData = bionicTagsUse.getJSONObject(i);
                        ba_factiondata.ba_bionicUseTagDetails detail = new ba_factiondata.ba_bionicUseTagDetails(
                                tagData.getString("tag"),
                                (float) tagData.getDouble("spawnWeight")
                        );
                        listBionicTagDetails.add(detail);
                    }
                    JSONArray bionicIdsUse = factionJsonData.getJSONObject("bionicUse").getJSONArray("ids");
                    List<ba_factiondata.ba_bionicUseIdDetails> listBionicIdDetails = new ArrayList<>();
                    for (int i = 0; i < bionicIdsUse.length(); i++) {
                        JSONObject idData = bionicIdsUse.getJSONObject(i);
                        ba_factiondata.ba_bionicUseIdDetails detail = null;
                        if(ba_bionicmanager.getBionic(idData.getString("id")) != null) {
                            detail = new ba_factiondata.ba_bionicUseIdDetails(
                                    ba_bionicmanager.getBionic(idData.getString("id")),
                                    (float) idData.getDouble("spawnWeight")
                            );
                            listBionicIdDetails.add(detail);
                        } else {
                            log.info("Can not find bionic ID for " + factionId + " in bionicUse with bionic ID: " + idData.getString("id") + " Skipping.");
                        }
                    }
                    try {
                        factionData = new ba_factiondata(
                                factionId,
                                listVariantDetails,
                                listBionicTagDetails,
                                listBionicIdDetails,
                                (float) factionJsonData.getDouble("targetBRMLevel"),
                                (float) factionJsonData.getDouble("targetConsciousLevel"),
                                (float) factionJsonData.getDouble("maxBionicUseCount")
                        );
                    } catch (Exception e) {
                        factionData = new ba_factiondata(
                                factionId,
                                listVariantDetails,
                                listBionicTagDetails,
                                listBionicIdDetails,
                                (float) factionJsonData.getDouble("targetBRMLevel"),
                                (float) factionJsonData.getDouble("targetConsciousLevel"),
                                -1
                        );
                    }
                    factionVariantMap.put(factionId, factionData);
                } catch (Exception e) {
                    log.info(e);
                    log.error("Unable to find faction bionic data for " + factionId + ". Skipping");
                }
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException("Failed loading ", e);
        }
    }
    public static ba_factiondata getFactionData(String factionId) {
        ba_factiondata data = factionVariantMap.get(factionId);
        if(data == null) {
            //fallback on faction that haven't been defined in faction_data.json
            data = factionVariantMap.get("ba_default");
            log.error("Can not find faction data of faction id: "+ factionId + ". Replacing with default faction profile.");
        }
        return data;
    }
    /**
     * Get a random variant based on faction. Defined in faction_data.json
     * @param factionId
     * @return
     */
    public static String getRandomFactionVariant(String factionId) {
        WeightedRandomPicker<String> randomPicker = new WeightedRandomPicker<>();
        ba_factiondata data = getFactionData(factionId);
        if(data != null) {
            for(ba_factiondata.ba_factionVariantDetails detail: data.variantDetails) {
                randomPicker.add(detail.variant.variantId);
            }
        }
        return randomPicker.pick();
    }
}

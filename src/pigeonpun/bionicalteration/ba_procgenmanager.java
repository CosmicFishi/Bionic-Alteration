package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicCampaign;

import java.util.*;

public class ba_procgenmanager {
    static Logger log = Global.getLogger(ba_procgenmanager.class);
    static final int bionicStationSpawnCount = 4;
    public static void generate() {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(ba_variablemanager.BA_BIONIC_RESEARCH_STATION_SPAWNED_KEY)) {
            List<String> themesLookingFor = new ArrayList<>();
            themesLookingFor.add(Tags.THEME_INTERESTING);
            themesLookingFor.add(Tags.THEME_DERELICT);

            List<String> themesAvoid = new ArrayList<>();
            themesAvoid.add("theme_already_colonized");
            themesAvoid.add("theme_hidden");

            List<String> entitiesDesired = new ArrayList<>();
            entitiesDesired.add(Tags.GATE);

//        List<String> entitiesLookingFor = new ArrayList<>();
//        entitiesLookingFor.add(Tags.STATION);
//        entitiesLookingFor.add(Tags.DEBRIS_FIELD);
//        entitiesLookingFor.add(Tags.WRECK);
//        entitiesLookingFor.add(Tags.SALVAGEABLE);

            int spawnCount = 0;
            while(spawnCount < bionicStationSpawnCount) {
                SectorEntityToken targetLocation = MagicCampaign.findSuitableTarget(
                        null,
                        null,
                        "CLOSE",
                        themesLookingFor,
                        themesAvoid,
                        entitiesDesired,
                        false,
                        true,
                        false);
                if (targetLocation == null) {
                    log.info("No suitable system found to spawn bionic research station");
                } else {
                    log.info("Found " + targetLocation.getStarSystem().getName() + " system, spawning bionic research station.");

                    List<PlanetAPI> potentialPlanet = new ArrayList<>();
                    HashMap<String, String> allEntites = new HashMap<>();
                    for (SectorEntityToken entity: targetLocation.getStarSystem().getAllEntities()) {
                        if(entity instanceof PlanetAPI || entity.getOrbitFocus() instanceof PlanetAPI) {
//                            if(entity.getOrbitFocus() instanceof PlanetAPI && !entity.getTags().contains(Tags.STATION)) {
//                                continue;
//                            }
                            //todo: find a way to spawn bionic research station on planet without any other entity focused on
                            if(entity instanceof PlanetAPI) {
                                allEntites.put(entity.getId() + " - " + entity.getName(), "");
                            } else {
                                if(allEntites.containsKey(entity.getOrbitFocus().getId() + " - " + entity.getOrbitFocus().getName())) {
                                    allEntites.put(entity.getOrbitFocus().getId() + " - " + entity.getOrbitFocus().getName(), allEntites.get(entity.getOrbitFocus().getId() + " - " + entity.getOrbitFocus().getName()).toString() + "-" +entity.getName());
                                } else {
                                    allEntites.put(entity.getOrbitFocus().getId() + " - " + entity.getOrbitFocus().getName(), "");
                                }
                            }
                        }
                        if (entity instanceof PlanetAPI) {
                            potentialPlanet.add((PlanetAPI) entity);
                        }
//                        else {
//                            if (potentialPlanet.contains(entity.getOrbitFocus())) {
//                                potentialPlanet.remove(entity.getOrbitFocus());
//                            }
//                        }
                    }

                    for (Map.Entry<String, String> set: allEntites.entrySet()) {
                        log.info(set.getKey() + " : " + set.getValue());
                    }

                    spawnCount += 1;

//                    SectorEntityToken station = targetLocation.getStarSystem().addCustomEntity("ba_bionic_research_station_" + spawnCount, "Bionic Research Station", ba_variablemanager.BA_OVERCLOCK_STATION, Factions.DERELICT);
//                    station.setCircularOrbit(targetLocation, targetLocation.getCircularOrbitAngle(), 60f, targetLocation.getCircularOrbitPeriod());
                }
            }
            Global.getSector().getMemoryWithoutUpdate().set(ba_variablemanager.BA_BIONIC_RESEARCH_STATION_SPAWNED_KEY, true);
        }
    }
}

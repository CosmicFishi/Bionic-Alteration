package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.magiclib.util.MagicCampaign;
import pigeonpun.bionicalteration.utils.ba_utils;

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
            final int maxTriesBeforeQuit = 1000;
            int tryCountBeforeQuit = 0;
            while(spawnCount < bionicStationSpawnCount && tryCountBeforeQuit < maxTriesBeforeQuit) {
                tryCountBeforeQuit += 1;
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
                    boolean continueToNextOne = false;
                    WeightedRandomPicker<PlanetAPI> randomPlanetPicker = new WeightedRandomPicker<>(ba_utils.getRandom());
                    for (SectorEntityToken entity: targetLocation.getStarSystem().getAllEntities()) {
                        if(entity.getTags() != null &&  entity.getTags().contains("ba_overclock_station")) {
                            continueToNextOne = true;
                            log.info("Duplicated searching, skip for " + targetLocation.getStarSystem().getName());
                            break;
                        }
                    }
                    if(continueToNextOne) continue;

                    //if no duplicate bionic research station in that system
                    for (SectorEntityToken entity: targetLocation.getStarSystem().getAllEntities()) {
                        if(entity instanceof PlanetAPI && entity.getTags() != null && !entity.getTags().contains("star")) {
                            randomPlanetPicker.add((PlanetAPI) entity);
                        }
                    }
                    PlanetAPI selectedPlanet = randomPlanetPicker.pick();
                    if(selectedPlanet == null) continue;
                    List<SectorEntityToken> listOrbittingEntities = new ArrayList<>();
                    List<String> ignoredType = new ArrayList<>();
                    ignoredType.add("wreck");
                    for (SectorEntityToken entity: targetLocation.getStarSystem().getAllEntities()) {
                        if(entity instanceof PlanetAPI || entity.getTags() == null) continue;
                        if(entity.getTags() != null && !entity.getTags().contains(Tags.SALVAGEABLE)) continue;
                        if(entity.getCustomEntityType() != null &&  ignoredType.contains(entity.getCustomEntityType())) continue;
                        if(entity.getOrbitFocus() == null) continue;
                        if(Objects.equals(entity.getOrbitFocus().getId(), selectedPlanet.getId())) {
                            listOrbittingEntities.add(entity);
                        }
                    }
                    boolean regnerate = true;
                    float orbitAngle = 0;
                    float maxRetry = 10;
                    float currentRetry = 0;
                    while(regnerate) {
                        orbitAngle = MathUtils.getRandomNumberInRange(0, 360);
                        boolean overlaps = false;
                        for(SectorEntityToken entity: listOrbittingEntities) {
                            if(entity.getCircularOrbitAngle() - 10 < orbitAngle && orbitAngle < entity.getCircularOrbitAngle() + 10) {
                                overlaps = true;
                                currentRetry += 1;
                            }
                        }
                        if(!overlaps || currentRetry >= maxRetry) {
                            regnerate = false;
                        }
                    }
                    if(currentRetry < maxRetry) {
                        //waiting for Alex to reply back
                        SectorEntityToken station = targetLocation.getStarSystem().addCustomEntity(
                                "ba_bionic_research_station_" + spawnCount,
                                "Bionic Research Station", ba_variablemanager.BA_OVERCLOCK_STATION,
                                Factions.DERELICT
                        );
                        station.setCircularOrbit(selectedPlanet, orbitAngle,  selectedPlanet.getRadius() + 180f, selectedPlanet.getCircularOrbitPeriod());
                        station.setDiscoverable(true);
                        station.setSensorProfile(1f);
                        long salvageSeed = ba_utils.getRandom().nextLong();
                        station.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, salvageSeed);
//                        station.getMemoryWithoutUpdate().set("$hasDefenders", true, 0f);
                        log.info("Found " + selectedPlanet.getStarSystem().getName() + " system, spawning bionic research station at " + selectedPlanet.getName());
                        spawnCount += 1;
                    }
                }
            }
            Global.getSector().getMemoryWithoutUpdate().set(ba_variablemanager.BA_BIONIC_RESEARCH_STATION_SPAWNED_KEY, true);
        }
    }
}

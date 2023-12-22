package pigeonpun.bionicalteration.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.util.DynamicStats;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;

import java.awt.*;
import java.util.List;

public class ba_bionic_augmented {
    static Logger log = Global.getLogger(ba_bionic_augmented.class);
    PersonAPI person;
    public ba_bionic_augmented(PersonAPI person) {
        this.person = person;
    }
    public static boolean isOfficer(MutableShipStatsAPI stats) {
        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            return !ship.getCaptain().isDefault();
        } else {
            FleetMemberAPI member = stats.getFleetMember();
            if (member == null) return false;
            return !member.getCaptain().isDefault();
        }
    }
    public static PersonAPI findPerson(MutableCharacterStatsAPI stats) {
        for (PersonAPI person: ba_officermanager.listPersons) {
            if(person.getStats().equals(stats)) {
                return person;
            }
        }
        return null;
    }
    public static class Officer extends BaseSkillEffectDescription implements ShipSkillEffect, AfterShipCreationSkillEffect {
        @Override
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);
            float opad = 10f;
            Color c = Misc.getBasePlayerColor();

            PersonAPI person = findPerson(stats);
            if(person != null) {
                List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(person);
                for(ba_bionicitemplugin bionic: listBionic) {
                    if(bionic.isCaptainBionic) {
                        String description = "No effect yet";
                        if(bionic.effectScript != null) {
                            description = bionic.effectScript.getShortEffectDescription();
                        }
                        LabelAPI descriptionLabel = info.addPara("%s: %s", opad, Misc.getBrightPlayerColor() ,bionic.getName() ,description);
                        descriptionLabel.setHighlight(bionic.getName(), description);
                        descriptionLabel.setHighlightColors(bionic.displayColor, bionic.effectScript != null ? c : Misc.getGrayColor());
                    }
                }
            }
        }

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            PersonAPI captain = ship.getCaptain();
            List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(captain);
            for(ba_bionicitemplugin bionic: listBionic) {
                if(bionic.isAdvanceInCombat) {
                    log.info("Registering Bionic Listener");
                    ship.addListener(new bionicInCombat(ship, listBionic));
                    break;
                }
            }
        }

        @Override
        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
            if(ship.getListenerManager() != null && ship.getListenerManager().hasListener(bionicInCombat.class)) {
                ship.removeListenerOfClass(bionicInCombat.class);
            }
        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            if(stats.getFleetMember() != null) {
                PersonAPI captain = stats.getFleetMember().getCaptain();
                List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(captain);
                for(ba_bionicitemplugin bionic: listBionic) {
                    if(bionic.isCaptainBionic && bionic.effectScript != null) {
                        bionic.effectScript.applyOfficerEffect(stats, hullSize, id);
                    }
                }
            }
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            if(stats.getFleetMember() != null) {
                PersonAPI captain = stats.getFleetMember().getCaptain();
                List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(captain);
                for(ba_bionicitemplugin bionic: listBionic) {
                    if(bionic.isCaptainBionic && bionic.effectScript != null) {
                        bionic.effectScript.unapplyOfficerEffect(stats, hullSize, id);
                    }
                }
            }
        }

        @Override
        public String getEffectDescription(float level) {
            return null;
        }

        @Override
        public String getEffectPerLevelDescription() {
            return null;
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return null;
        }
    }
    public static class Admin extends BaseSkillEffectDescription implements CharacterStatsSkillEffect {

        @Override
        public void apply(MutableCharacterStatsAPI stats, String id, float level) {

        }

        @Override
        public void unapply(MutableCharacterStatsAPI stats, String id) {

        }

        @Override
        public String getEffectDescription(float level) {
            return null;
        }

        @Override
        public String getEffectPerLevelDescription() {
            return null;
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return null;
        }
    }
    public static class bionicInCombat implements AdvanceableListener {
        protected List<ba_bionicitemplugin> bionics;
        protected ShipAPI ship;
        public bionicInCombat(ShipAPI ship, List<ba_bionicitemplugin> bionics) {
            this.bionics = bionics;
            this.ship = ship;
        }
        @Override
        public void advance(float amount) {
            for(ba_bionicitemplugin bionic: this.bionics) {
                if (bionic.isAdvanceInCombat && bionic.effectScript != null) {
                    bionic.effectScript.advanceInCombat(this.ship, amount);
                }
            }
        }
    }
}

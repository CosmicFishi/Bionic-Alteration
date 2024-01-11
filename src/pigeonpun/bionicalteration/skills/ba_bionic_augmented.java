package pigeonpun.bionicalteration.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
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
import pigeonpun.bionicalteration.conscious.ba_conscious;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
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
            Color c = Misc.getHighlightColor();

            PersonAPI person = findPerson(stats);
            if(person != null) {
                //bionics
                info.setParaOrbitronLarge();
                List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(person);
                Color[] listBionicColor = new Color[listBionic.size()+1];
                StringBuilder description = new StringBuilder();
                listBionicColor[0] = Misc.getBrightPlayerColor();
                int colorIndex = 1;
                if(!listBionic.isEmpty()) {
                    description.append("Bionics").append(", ");
                    for(ba_bionicitemplugin bionic: listBionic) {
                        description.append(bionic.getName()).append(", ");
                        listBionicColor[colorIndex] = bionic.displayColor;
                        colorIndex++;
                    }
                    description.setLength(description.length()-2);
                } else {
                    description.append("No bionic...yet");
                    listBionicColor[1] = Misc.getGrayColor();
                }
                String[] stringArray = description.toString().split(", ");
                StringBuilder formatString = new StringBuilder("%s: ");
                if(!listBionic.isEmpty()) {
                    int i = 0;
                    while(i < listBionic.size()) {
                        formatString.append("%s, ");
                        i++;
                    }
                    formatString.setLength(formatString.length() - 2);
                } else {
                    formatString.append("%s");
                }
                LabelAPI descriptionLabel = info.addPara(formatString.toString(), opad, listBionicColor , stringArray);
//                descriptionLabel.setHighlight("Bionics", description.toString());
//                descriptionLabel.setHighlightColors(Misc.getTextColor(), listBionic.isEmpty()? Misc.getGrayColor() : c);
                //conscious
                info.setParaFontDefault();
                ba_consciousmanager.getConsciousnessLevel(person).displayTooltipDescription(info, person, true, true);
            }
        }

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            PersonAPI captain = ship.getCaptain();
            List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(captain);
            for(ba_bionicitemplugin bionic: listBionic) {
                if(bionic.isAdvanceInCombat) {
                    log.info("Registering Bionic Listener");
                    ship.addListener(new bionicInCombat(ship, listBionic, captain));
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
                ba_consciousmanager.getConsciousnessLevel(captain).applyEffectOfficer(stats, id);
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
                ba_consciousmanager.getConsciousnessLevel(captain).unapplyEffectOfficer(stats, id);
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
    public static class Admin implements CharacterStatsSkillEffect {

        @Override
        public void apply(MutableCharacterStatsAPI stats, String id, float level) {
            PersonAPI person = findPerson(stats);
            if(person != null) {
                List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(person);
                for(ba_bionicitemplugin bionic: listBionic) {
                    if(!bionic.isCaptainBionic && bionic.effectScript != null) {
                        bionic.effectScript.applyAdminEffect(stats, id);
                    }
                }
                ba_consciousmanager.getConsciousnessLevel(person).applyEffectAdmin(stats, id);
            }
        }

        @Override
        public void unapply(MutableCharacterStatsAPI stats, String id) {
            PersonAPI person = findPerson(stats);
            if(person != null) {
                List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(person);
                for(ba_bionicitemplugin bionic: listBionic) {
                    if(!bionic.isCaptainBionic && bionic.effectScript != null) {
                        bionic.effectScript.unapplyAdminEffect(stats, id);
                    }
                }
                ba_consciousmanager.getConsciousnessLevel(person).unapplyEffectAdmin(stats, id);
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
    public static class AdminMarket implements MarketSkillEffect {
        @Override
        public void apply(MarketAPI market, String id, float level) {
            if(market.getAdmin() != null) {
                PersonAPI person = market.getAdmin();
                List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(person);
                for(ba_bionicitemplugin bionic: listBionic) {
                    if(!bionic.isCaptainBionic && bionic.effectScript != null) {
                        bionic.effectScript.applyEffectAdminMarket(market, id, level, bionic);
                    }
                }
                ba_consciousmanager.getConsciousnessLevel(person).applyEffectAdminMarket(market, id, level);
            }
        }

        @Override
        public void unapply(MarketAPI market, String id) {
            if(market.getAdmin() != null) {
                PersonAPI person = market.getAdmin();
                List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(person);
                for(ba_bionicitemplugin bionic: listBionic) {
                    if(!bionic.isCaptainBionic && bionic.effectScript != null) {
                        bionic.effectScript.unapplyEffectAdminMarket(market, id);
                    }
                }
                ba_consciousmanager.getConsciousnessLevel(person).unapplyEffectAdminMarket(market, id);
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
    public static class bionicInCombat implements AdvanceableListener {
        protected List<ba_bionicitemplugin> bionics;
        protected ShipAPI ship;
        protected PersonAPI person;
        public bionicInCombat(ShipAPI ship, List<ba_bionicitemplugin> bionics, PersonAPI person) {
            this.bionics = bionics;
            this.ship = ship;
            this.person = person;
        }
        @Override
        public void advance(float amount) {
            ba_conscious conscious = ba_consciousmanager.getConsciousnessLevel(person);
            conscious.advanceInCombat(this.ship, amount);
            for(ba_bionicitemplugin bionic: this.bionics) {
                if (bionic.isAdvanceInCombat && bionic.effectScript != null) {
                    bionic.effectScript.advanceInCombat(this.ship, amount);
                }
            }
        }
    }
}

package pigeonpun.bionicalteration.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableMarketStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.fleet.MutableMarketStats;
import com.fs.starfarer.util.DynamicStats;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.conscious.ba_conscious;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;

import java.awt.*;
import java.util.*;
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
            displayBionicDescriptions(person, info, opad);
        }

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            if(ship.getListeners(bionicInCombat.class).isEmpty()) {
                PersonAPI captain = ship.getCaptain();
                List<ba_officermanager.ba_bionicAugmentedData> listAnatomy = ba_officermanager.getBionicAnatomyList(captain);
//                log.info("Registering Bionic Listener");
                ship.addListener(new bionicInCombat(ship, listAnatomy, captain));
//                log.info("aaaa");
            }
        }

        @Override
        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
//            if(ship.getListenerManager() != null && ship.getListenerManager().hasListener(bionicInCombat.class)) {
//                ship.removeListenerOfClass(bionicInCombat.class);
//            }
        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            if(stats.getFleetMember() != null) {
                PersonAPI captain = stats.getFleetMember().getCaptain();
                List<ba_officermanager.ba_bionicAugmentedData> listAnatomy = ba_officermanager.getBionicAnatomyList(captain);
                for(ba_officermanager.ba_bionicAugmentedData anatomy: listAnatomy) {
                    if(anatomy.bionicInstalled != null) {
                        if(anatomy.bionicInstalled != null && anatomy.bionicInstalled.isApplyCaptainEffect) {
                            String applyId = id + anatomy.bionicInstalled.bionicId + anatomy.limb;
                            anatomy.bionicInstalled.applyOfficerEffect(stats, hullSize, applyId);
                        }
                        if(anatomy.appliedOverclock != null) {
                            if(anatomy.appliedOverclock.isApplyCaptainEffect) {
                                String applyId = id + "_" + anatomy.bionicInstalled.bionicId + "_" + anatomy.appliedOverclock.id + "_" + anatomy.limb;
                                anatomy.appliedOverclock.applyOfficerEffect(stats, hullSize, applyId);
                            }
                        }
                    }
                }
                ba_consciousmanager.resetBeforeApplyEffectOfficer(stats, id);
                if(!bionicalterationplugin.isConsciousnessDisable) {
                    ba_consciousmanager.getConsciousnessLevel(captain).applyEffectOfficer(stats, id);
                }
                if(stats.getFleetMember() != null && stats.getFleetMember().getVariant() != null) {
                    stats.getFleetMember().getVariant().addPermaMod(ba_variablemanager.BA_BIONIC_INFO_HULLMOD);
                }
            }
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            if(stats.getFleetMember() != null) {
                PersonAPI captain = stats.getFleetMember().getCaptain();
                List<ba_officermanager.ba_bionicAugmentedData> listAnatomy = ba_officermanager.getBionicAnatomyList(captain);
                for(ba_officermanager.ba_bionicAugmentedData anatomy: listAnatomy) {
                    if(anatomy.bionicInstalled != null) {
                        if(anatomy.bionicInstalled != null && anatomy.bionicInstalled.isApplyCaptainEffect) {
                            String applyId = id + anatomy.bionicInstalled.bionicId + anatomy.limb;
                            anatomy.bionicInstalled.unapplyOfficerEffect(stats, hullSize, applyId);
                        }
                        if(anatomy.appliedOverclock != null) {
                            if(anatomy.appliedOverclock.isApplyCaptainEffect) {
                                String applyId = id + "_" + anatomy.bionicInstalled.bionicId + "_" + anatomy.appliedOverclock.id + "_" + anatomy.limb;
                                anatomy.appliedOverclock.unapplyOfficerEffect(stats, hullSize, applyId);
                            }
                        }
                    }
                }
                ba_consciousmanager.resetBeforeApplyEffectOfficer(stats, id);
                if(stats.getFleetMember() != null && stats.getFleetMember().getVariant() != null) {
                    stats.getFleetMember().getVariant().removePermaMod(ba_variablemanager.BA_BIONIC_INFO_HULLMOD);
                }
            }
        }

        @Override
        public String getEffectDescription(float level) {
            return "";
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
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);
            float opad = 10f;
            Color c = Misc.getHighlightColor();

            PersonAPI person = findPerson(stats);
            displayBionicDescriptions(person, info, opad);
        }
        @Override
        public void apply(MutableCharacterStatsAPI stats, String id, float level) {
//            PersonAPI person = findPerson(stats);
//            if(person != null) {
//                List<ba_officermanager.ba_bionicAugmentedData> listAnatomy = ba_officermanager.getBionicAnatomyList(person);
//                for(ba_officermanager.ba_bionicAugmentedData anatomy: listAnatomy) {
//                    if(anatomy.bionicInstalled != null) {
//                        if(anatomy.bionicInstalled != null && anatomy.bionicInstalled.isApplyAdminEffect) {
//                            String applyId = id + anatomy.bionicInstalled.bionicId + anatomy.limb;
//                            anatomy.bionicInstalled.applyAdminEffect(stats, applyId);
//                        }
//                        if(anatomy.appliedOverclock != null) {
//                            if(anatomy.appliedOverclock.isApplyAdminEffect) {
//                                String applyId = id + "_" + anatomy.bionicInstalled.bionicId + "_" + anatomy.appliedOverclock.id + "_" + anatomy.limb;
//                                anatomy.appliedOverclock.applyAdminEffect(stats, applyId);
//                            }
//                        }
//                    }
//                }
//                ba_consciousmanager.resetBeforeApplyEffectAdmin(stats, id);
//                if(!bionicalterationplugin.isConsciousnessDisable) {
//                    ba_consciousmanager.getConsciousnessLevel(person).applyEffectAdmin(stats, id);
//                }
//
//            }
        }

        @Override
        public void unapply(MutableCharacterStatsAPI stats, String id) {
//            PersonAPI person = findPerson(stats);
//            if(person != null) {
//                List<ba_officermanager.ba_bionicAugmentedData> listAnatomy = ba_officermanager.getBionicAnatomyList(person);
//                for(ba_officermanager.ba_bionicAugmentedData anatomy: listAnatomy) {
//                    if(anatomy.bionicInstalled != null) {
//                        if(anatomy.bionicInstalled != null && anatomy.bionicInstalled.isApplyAdminEffect) {
//                            String applyId = id + anatomy.bionicInstalled.bionicId + anatomy.limb;
//                            anatomy.bionicInstalled.unapplyAdminEffect(stats, applyId);
//                        }
//                        if(anatomy.appliedOverclock != null) {
//                            if(anatomy.appliedOverclock.isApplyAdminEffect) {
//                                String applyId = id + "_" + anatomy.bionicInstalled.bionicId + "_" + anatomy.appliedOverclock.id + "_" + anatomy.limb;
//                                anatomy.appliedOverclock.unapplyAdminEffect(stats, applyId);
//                            }
//                        }
//                    }
//                }
//                ba_consciousmanager.resetBeforeApplyEffectAdmin(stats, id);
//            }
        }

        @Override
        public String getEffectDescription(float level) {
            return "";
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
    public static class AdminMarket extends BaseSkillEffectDescription implements MarketSkillEffect {
        @Override
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);
            float opad = 10f;
            Color c = Misc.getHighlightColor();

            PersonAPI person = findPerson(stats);
            displayBionicDescriptions(person, info, opad);
        }
        @Override
        public void apply(MarketAPI market, String id, float level) {
            if(market != null && !market.hasCondition(Conditions.DECIVILIZED) && market.getAdmin() != null &&  !market.getAdmin().isDefault()) {
                PersonAPI person = market.getAdmin();
                if(ba_bionicmanager.checkIfHaveBionicInstalled(person) && !market.hasCondition(ba_variablemanager.BA_MARKET_CONDITION_ID)) {
                    market.addCondition(ba_variablemanager.BA_MARKET_CONDITION_ID);
                }
            }
        }

        @Override
        public void unapply(MarketAPI market, String id) {
//            if(market.getAdmin() != null && market.getConditions() != null) {
//                PersonAPI person = market.getAdmin();
//                if(market.hasCondition(ba_variablemanager.BA_MARKET_CONDITION_ID)) {
//                    market.removeCondition(ba_variablemanager.BA_MARKET_CONDITION_ID);
//                }
//            }
        }

        @Override
        public String getEffectDescription(float level) {
            return "";
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
    public static void displayBionicDescriptions(PersonAPI person, TooltipMakerAPI info, float opad) {
        if(person != null) {
            List<ba_bionicitemplugin> listBionic = ba_bionicmanager.getListBionicInstalled(person);
            if(Keyboard.isKeyDown(Keyboard.KEY_F1)) {
                if(!listBionic.isEmpty()) {
                    for(ba_bionicitemplugin bionic: listBionic) {
                        UIComponentAPI border = info.createRect(Misc.getGrayColor().darker().darker(), 1);
                        border.getPosition().setSize(info.getWidthSoFar(), 1);
                        info.addCustom(border, 10f);
                        ba_bionicmanager.getBionic(bionic.getId()).displayEffectDescription(info, person, bionic, false);
                    }
                } else {
                    info.addPara("No bionic...yet", 10f);
                }
            } else {
                //bionics
                info.setParaOrbitronLarge();
                Color[] listBionicColor = new Color[listBionic.size()+1];
                StringBuilder description = new StringBuilder();
                listBionicColor[0] = Misc.getBrightPlayerColor();
                int colorIndex = 1;
                description.append("Bionics").append(", ");
                if(!listBionic.isEmpty()) {
                    for(ba_bionicitemplugin bionic: listBionic) {
                        description.append(bionic.getName()).append(", ");
                        listBionicColor[colorIndex] = bionic.displayColor;
                        colorIndex++;
                    }
                    description.setLength(description.length()-2);
                } else {
                    description.append("No bionic...yet");
                    listBionicColor = new Color[2];
                    listBionicColor[0] = Misc.getBrightPlayerColor();
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
                //conscious
                info.setParaFontDefault();
                ba_consciousmanager.getConsciousnessLevel(person).displayTooltipDescription(info, person, true, true);
                LabelAPI f1Label = info.addPara("To display the full bionic list, press F1 while hovering this skill.", Misc.getGrayColor().darker(), opad);
            }
        }
    }
    //this is for save compatibility
    public static class bionicInCombat implements AdvanceableListener {
        protected List<ba_officermanager.ba_bionicAugmentedData> dataList;
        protected ShipAPI ship;
        protected PersonAPI person;
        public bionicInCombat(ShipAPI ship, List<ba_officermanager.ba_bionicAugmentedData> data, PersonAPI person) {
            this.dataList = data;
            this.ship = ship;
            this.person = person;
        }
        @Override
        public void advance(float amount) {
            if(Global.getCombatEngine().isPaused()) return;
            if(bionicalterationplugin.isConsciousnessDisable) {
                ba_conscious conscious = ba_consciousmanager.getConsciousnessLevel(person);
                conscious.advanceInCombat(this.ship, amount);
            }
            for(ba_officermanager.ba_bionicAugmentedData anatomy: this.dataList) {
                if (anatomy.bionicInstalled != null) {
                    if(anatomy.bionicInstalled.isAdvanceInCombat && anatomy.bionicInstalled != null) {
                        anatomy.bionicInstalled.advanceInCombat(this.ship, amount);
                    }
                    if(anatomy.appliedOverclock != null) {
                        if(anatomy.appliedOverclock.isAdvanceInCombat()) {
                            anatomy.appliedOverclock.advanceInCombat(ship, amount);
                        }
                    }
                }
            }
        }
    }
}

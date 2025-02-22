package pigeonpun.bionicalteration.conscious.impl;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.conscious.ba_base_conscious;
import pigeonpun.bionicalteration.conscious.ba_conscious;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.ui.bionic.ba_uiplugin;
import pigeonpun.bionicalteration.utils.ba_stringhelper;

import java.awt.*;

public class ba_conscious_critical extends ba_base_conscious {
    //officer
    public final static float SHIP_MAINTENANCE = 1f;
//    public final static float MANEUVERABILITY_BONUS = 0.22f;
    public final static float SHIP_OVERLOAD = 0.75f;
    public final static float VENTING = 0.5f;
    public final static float EMP = 0.5f;
//    public final static float SHIP_CR = 0.15f;
//    public final static String SHIP_PERSONALITY = Personalities.RECKLESS;
    //admin
    public final static float MARKET_UPKEEP = 0.2f;
    public final static float ADMIN_FUND = 7000f;
//    public final static float MARKET_STABILITY = 2f;
//    public final static float MARKET_ACCESS = 0.25f;
//    public final static float MARKET_DEFEND = 0.28f;
    @Override
    public Color getColor() {
        return ba_variablemanager.BA_CONSCIOUSNESS_COLOR.get(ba_variablemanager.BA_CONSCIOUSNESS_CRITICAL_THRESHOLD);
    }

    @Override
    public float getThreshold() {
        return ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_CRITICAL_THRESHOLD);
    }

    @Override
    public float getDisplayOrder() {
        return 4;
    }

    @Override
    public String getDisplayName() {
        return "Critical";
    }

    @Override
    public void displayTooltipDescription(TooltipMakerAPI tooltip, PersonAPI person, boolean isActive, boolean isSimpleMode) {
        float textAlpha = 1f;
        final float pad = 10f;
        boolean showBoth = false;
        boolean showOfficer = false;
        if(person.isPlayer()) {
            showBoth = true;
        } else {
            if(ba_officermanager.isCaptainOrAdmin(person, ba_uiplugin.isDisplayingOtherFleets).equals(ba_officermanager.ba_profession.CAPTAIN)) {
                showOfficer = true;
            }
        }
        if(!isSimpleMode) {
            //on hover
            tooltip.addSectionHeading("Description", Alignment.MID, 0);
            tooltip.addPara("%s: %s", pad, Misc.getTextColor(), "Critical", ba_stringhelper.getString("conscious", "ba_critical_person")).setHighlightColors(getColor(), Misc.getTextColor());
            tooltip.addSectionHeading("Effects", Alignment.MID, pad);
        } else {
            if(!isActive) textAlpha = 0.6f;
            tooltip.setParaFontOrbitron();
            tooltip.addPara("%s %s", pad, getColor(), getDisplayName() + ":","<" + Math.round(getThreshold() * 100) + "%").setOpacity(textAlpha);
        }
        tooltip.setParaFontDefault();
        if(showBoth || showOfficer) {
            tooltip.addPara("- Ship maintenance increased by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(SHIP_MAINTENANCE * 100) + "%").setOpacity(textAlpha);
            tooltip.addPara("- Ship overload duration increased by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(SHIP_OVERLOAD * 100) + "%").setOpacity(textAlpha);
            tooltip.addPara("- Flux venting rate decrease by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(VENTING * 100) + "%").setOpacity(textAlpha);
            tooltip.addPara("- EMP damage taken increase by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(EMP * 100) + "%").setOpacity(textAlpha);
//            tooltip.addPara("- Ship peak performance duration reduced by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(SHIP_CR * 100) + "%").setOpacity(textAlpha);
//            tooltip.addPara("- Captain personality changed to %s", pad/2, Misc.getNegativeHighlightColor(), "" + SHIP_PERSONALITY).setOpacity(textAlpha);
        }
        if(showBoth || !showOfficer) {
            tooltip.addPara("- Market upkeep increased by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(MARKET_UPKEEP * 100) + "%").setOpacity(textAlpha);
            tooltip.addPara("- Admin mentality stabilization fund: %s", pad/2, Misc.getNegativeHighlightColor(), "" + Misc.getDGSCredits(ADMIN_FUND)).setOpacity(textAlpha);
//            tooltip.addPara("- Market stability reduced by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(MARKET_STABILITY)).setOpacity(textAlpha);
//            tooltip.addPara("- Market accessibility reduced by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(MARKET_ACCESS * 100) + "%").setOpacity(textAlpha);
        }
    }

    @Override
    public void applyEffectOfficer(MutableShipStatsAPI stats, String id) {
//        stats.getAcceleration().modifyMult(id + "conscious", 1 - MANEUVERABILITY_BONUS);
//        stats.getDeceleration().modifyMult(id + "conscious", 1 - MANEUVERABILITY_BONUS);
//        stats.getTurnAcceleration().modifyMult(id + "conscious", 1 - MANEUVERABILITY_BONUS);
//        stats.getMaxTurnRate().modifyMult(id + "conscious", 1 - MANEUVERABILITY_BONUS);
        stats.getSuppliesPerMonth().modifyPercent(id + "conscious", SHIP_MAINTENANCE * 100);
        stats.getOverloadTimeMod().modifyPercent(id + "conscious", SHIP_OVERLOAD * 100);
        stats.getVentRateMult().modifyMult(id + "conscious", 1-VENTING);
        stats.getEmpDamageTakenMult().modifyPercent(id + "conscious", EMP);
//        stats.getPeakCRDuration().modifyMult(id + "conscious", 1 - SHIP_CR);
    }

    @Override
    public void unapplyEffectOfficer(MutableShipStatsAPI stats, String id) {
//        stats.getAcceleration().unmodifyMult(id + "conscious");
//        stats.getDeceleration().unmodifyMult(id + "conscious");
//        stats.getTurnAcceleration().unmodifyMult(id + "conscious");
//        stats.getMaxTurnRate().unmodifyMult(id + "conscious");
        stats.getSuppliesPerMonth().unmodifyPercent(id + "conscious");
        stats.getOverloadTimeMod().unmodifyPercent(id + "conscious");
        stats.getVentRateMult().unmodifyMult(id + "conscious");
        stats.getEmpDamageTakenMult().unmodifyPercent(id + "conscious");
//        stats.getPeakCRDuration().unmodifyMult(id + "conscious");
    }

    @Override
    public void applyEffectAdmin(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyEffectAdmin(MutableCharacterStatsAPI stats, String id) {

    }


    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level) {
//        market.getAccessibilityMod().modifyFlat(id + "conscious", -MARKET_ACCESS, ba_consciousmanager.getConsciousnessLevel(market.getAdmin()).getDisplayName() + " (Admins " + ba_consciousmanager.getDisplayConditionLabel(market.getAdmin()) + ")");
//        market.getStability().modifyFlat(id + "conscious", -MARKET_STABILITY, ba_consciousmanager.getConsciousnessLevel(market.getAdmin()).getDisplayName() + " (Admins " + ba_consciousmanager.getDisplayConditionLabel(market.getAdmin()) + ")");
        market.getUpkeepMult().modifyMult(id + "conscious", 1 + MARKET_UPKEEP,  ba_consciousmanager.getConsciousnessLevel(market.getAdmin()).getDisplayName() + " (Admins " + ba_consciousmanager.getDisplayConditionLabel(market.getAdmin()) + ")");
//        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id + "conscious", (1 - MARKET_DEFEND), ba_consciousmanager.getConsciousnessLevel(market.getAdmin()).getDisplayName() + " (Admins " + ba_consciousmanager.getDisplayConditionLabel(market.getAdmin()) + ")");
    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {
//        market.getAccessibilityMod().unmodifyFlat(id + "conscious");
//        market.getStability().unmodifyFlat(id + "conscious");
        market.getUpkeepMult().unmodifyMult(id + "conscious");
//        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id + "conscious");
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        //add incompatibility with Automated Command that change personality
//        if(ship.getCaptain() != null) {
//            ship.getCaptain().setPersonality(SHIP_PERSONALITY);
//        }
    }

    @Override
    public float getConsciousTreatmentFee() {
        return ADMIN_FUND;
    }
}

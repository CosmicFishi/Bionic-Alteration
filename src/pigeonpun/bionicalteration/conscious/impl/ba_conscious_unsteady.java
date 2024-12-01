package pigeonpun.bionicalteration.conscious.impl;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
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

public class ba_conscious_unsteady extends ba_base_conscious {
    //officer
    public final static float SHIP_MAINTENANCE = 0.1f;
//    public final static float MANEUVERABILITY_BONUS = 0.1f;
//    public final static float SHIP_OVERLOAD = 0.1f;
    //admin
//    public final static float MARKET_UPKEEP = 0.1f;
    public final static float ADMIN_FUND = 1000f;
//    public final static float MARKET_STABILITY = 1f;
//    public final static float MARKET_ACCESS = 0.1f;
    @Override
    public Color getColor() {
        return ba_variablemanager.BA_CONSCIOUSNESS_COLOR.get(ba_variablemanager.BA_CONSCIOUSNESS_UNSTEADY_THRESHOLD);
    }

    @Override
    public float getThreshold() {
        return ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_UNSTEADY_THRESHOLD);
    }

    @Override
    public float getDisplayOrder() {
        return 1;
    }

    @Override
    public String getDisplayName() {
        return "Unsteady";
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
            tooltip.addPara("%s: %s", pad, Misc.getTextColor(), "Unsteady", ba_stringhelper.getString("conscious", "ba_unsteady_person")).setHighlightColors(getColor(), Misc.getTextColor());
            tooltip.addSectionHeading("Effects", Alignment.MID, pad);
        } else {
            if(!isActive) textAlpha = 0.6f;
            tooltip.setParaFontOrbitron();
            tooltip.addPara("%s %s", pad, getColor(), getDisplayName() + ":","<" + Math.round(getThreshold() * 100) + "%").setOpacity(textAlpha);
        }
        tooltip.setParaFontDefault();
        if(showBoth || showOfficer) {
            tooltip.addPara("- Ship maintenance increased by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(SHIP_MAINTENANCE * 100) + "%").setOpacity(textAlpha);
//            tooltip.addPara("- Ship maneuverability reduced by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(MANEUVERABILITY_BONUS * 100) + "%").setOpacity(textAlpha);
//            tooltip.addPara("- Ship overload duration increased by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(SHIP_OVERLOAD * 100) + "%").setOpacity(textAlpha);
        }
        if(showBoth || !showOfficer) {
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
//        stats.getOverloadTimeMod().modifyPercent(id + "conscious", SHIP_OVERLOAD * 100);
    }

    @Override
    public void unapplyEffectOfficer(MutableShipStatsAPI stats, String id) {
//        stats.getAcceleration().unmodifyMult(id + "conscious");
//        stats.getDeceleration().unmodifyMult(id + "conscious");
//        stats.getTurnAcceleration().unmodifyMult(id + "conscious");
//        stats.getMaxTurnRate().unmodifyMult(id + "conscious");
        stats.getSuppliesPerMonth().unmodifyPercent(id + "conscious");
//        stats.getOverloadTimeMod().unmodifyPercent(id + "conscious");
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
//        market.getUpkeepMult().modifyMult(id + "conscious", 1 + MARKET_UPKEEP, ba_consciousmanager.getConsciousnessLevel(market.getAdmin()).getDisplayName() + " (Admins " + ba_consciousmanager.getDisplayConditionLabel(market.getAdmin()) + ")");
    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {
//        market.getAccessibilityMod().unmodifyFlat(id + "conscious");
//        market.getStability().unmodifyFlat(id + "conscious");
//        market.getUpkeepMult().unmodifyMult(id + "conscious");
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }

    @Override
    public float getConsciousTreatmentFee() {
        return ADMIN_FUND;
    }
}

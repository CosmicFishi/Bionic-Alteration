package pigeonpun.bionicalteration.conscious.impl;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.conscious.ba_conscious;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.ui.ba_uiplugin;
import pigeonpun.bionicalteration.utils.ba_stringhelper;

import java.awt.*;

public class ba_conscious_unsteady implements ba_conscious {
    //officer
    public final static float SHIP_MAINTENANCE = 0.1f;
    public final static float MANEUVERABILITY_BONUS = 0.1f;
    public final static float SHIP_OVERLOAD = 0.05f;
    //admin
    public final static float MARKET_UPKEEP = 0.1f;
    public final static float MARKET_STABILITY = 1f;
    public final static float MARKET_ACCESS = 0.1f;
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
            if(ba_officermanager.isOfficer(person, ba_uiplugin.isDisplayingOtherFleets)) {
                showOfficer = true;
            }
        }
        if(!isSimpleMode) {
            //on hover
            tooltip.addSectionHeading("Description", Alignment.MID, 0);
            tooltip.addPara("%s: %s", pad, Misc.getTextColor(), "Unstable", ba_stringhelper.getString("conscious", "ba_unsteady_person")).setHighlightColors(getColor(), Misc.getTextColor());
            tooltip.addSectionHeading("Effects", Alignment.MID, pad);
        } else {
            if(!isActive) textAlpha = 0.6f;
            tooltip.setParaFontOrbitron();
            tooltip.addPara("%s %s", pad, getColor(), getDisplayName() + ":","<" + Math.round(getThreshold() * 100) + "%").setOpacity(textAlpha);
        }
        tooltip.setParaFontDefault();
        if(showBoth || showOfficer) {
            tooltip.addPara("- Ship maintenance increased by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(SHIP_MAINTENANCE * 100) + "%").setOpacity(textAlpha);
            tooltip.addPara("- Ship maneuverability reduced by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(MANEUVERABILITY_BONUS * 100) + "%").setOpacity(textAlpha);
            tooltip.addPara("- Ship overload duration increased by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(SHIP_OVERLOAD * 100) + "%").setOpacity(textAlpha);
        }
        if(showBoth || !showOfficer) {
            tooltip.addPara("- Market upkeep increased by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(MARKET_UPKEEP * 100) + "%").setOpacity(textAlpha);
            tooltip.addPara("- Market stability reduced by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(MARKET_STABILITY)).setOpacity(textAlpha);
            tooltip.addPara("- Market accessibility reduced by %s", pad/2, Misc.getNegativeHighlightColor(), "" + Math.round(MARKET_ACCESS * 100) + "%").setOpacity(textAlpha);
        }
    }

    @Override
    public void applyEffectOfficer(MutableShipStatsAPI stats, String id) {
        stats.getAcceleration().modifyMult(id, 1 - MANEUVERABILITY_BONUS);
        stats.getDeceleration().modifyMult(id, 1 - MANEUVERABILITY_BONUS);
        stats.getTurnAcceleration().modifyMult(id, 1 - MANEUVERABILITY_BONUS);
        stats.getMaxTurnRate().modifyMult(id, 1 - MANEUVERABILITY_BONUS);
        stats.getSuppliesPerMonth().modifyPercent(id, SHIP_MAINTENANCE * 100);
        stats.getOverloadTimeMod().modifyPercent(id, SHIP_OVERLOAD * 100);
    }

    @Override
    public void unapplyEffectOfficer(MutableShipStatsAPI stats, String id) {
        stats.getAcceleration().unmodifyMult(id);
        stats.getDeceleration().unmodifyMult(id);
        stats.getTurnAcceleration().unmodifyMult(id);
        stats.getMaxTurnRate().unmodifyMult(id);
        stats.getSuppliesPerMonth().unmodifyPercent(id);
        stats.getOverloadTimeMod().unmodifyPercent(id);
    }

    @Override
    public void applyEffectAdmin(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyEffectAdmin(MutableCharacterStatsAPI stats, String id) {

    }


    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level) {
        market.getAccessibilityMod().modifyFlat(id, -MARKET_ACCESS, ba_consciousmanager.getConsciousnessLevel(market.getAdmin()).getDisplayName() + " (Admins " + ba_consciousmanager.getDisplayConditionLabel(market.getAdmin()) + ")");
        market.getStability().modifyFlat(id, -MARKET_STABILITY, ba_consciousmanager.getConsciousnessLevel(market.getAdmin()).getDisplayName() + " (Admins " + ba_consciousmanager.getDisplayConditionLabel(market.getAdmin()) + ")");
        market.getUpkeepMult().modifyMult(id, 1 + MARKET_UPKEEP, ba_consciousmanager.getConsciousnessLevel(market.getAdmin()).getDisplayName() + " (Admins " + ba_consciousmanager.getDisplayConditionLabel(market.getAdmin()) + ")");
    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {
        market.getAccessibilityMod().unmodifyFlat(id);
        market.getStability().unmodifyFlat(id);
        market.getUpkeepMult().unmodifyMult(id);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }

    @Override
    public void advanceInCampaign() {

    }
}

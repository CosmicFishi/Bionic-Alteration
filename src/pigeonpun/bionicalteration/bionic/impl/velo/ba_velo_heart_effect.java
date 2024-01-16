package pigeonpun.bionicalteration.bionic.impl.velo;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.bionic.ba_bioniceffect;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_velo_heart_effect implements ba_bioniceffect {
    public static float SHIP_PHASE_ACTIVATION = 0.75f;
    public static float SHIP_SHIELD_FOLDING_TIME = 1.25f;
    public static float SHIP_PHASE_COST = 1.25f;
    public static float SHIP_SHIELD_UPKEEP = 1.25f;
    Logger log = Global.getLogger(ba_velo_heart_effect.class);

    @Override
    public void setBionicItem(ba_bionicitemplugin bionic) {

    }

    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's phase cloak activation time by " + Math.round(100 - SHIP_PHASE_ACTIVATION * 100) + "% and shield folding rate by " + Math.round(SHIP_SHIELD_FOLDING_TIME * 100 - 100);
        String negativeText = ", but increase ship's phase cloak upkeep cost by " + Math.round(SHIP_PHASE_COST * 100 - 100) + "% and shield upkeep cost by " + Math.round(SHIP_SHIELD_UPKEEP * 100 - 100) + "%" ;
        String name = isItem? "Effect:": bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s", pad, t, name, text, negativeText);
        descriptions.setHighlight(name, text, negativeText);
        descriptions.setHighlightColors(isItem? g.brighter().brighter() : bionic.displayColor, t, bad);
    }

    @Override
    public String getShortOnRemoveEffectDescription() {
        return null;
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getPhaseCloakCooldownBonus().modifyMult(id, SHIP_PHASE_ACTIVATION);
        stats.getShieldUnfoldRateMult().modifyMult(id, SHIP_SHIELD_FOLDING_TIME);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, SHIP_PHASE_COST);
        stats.getShieldUpkeepMult().modifyMult(id, SHIP_SHIELD_UPKEEP);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getPhaseCloakCooldownBonus().unmodifyMult(id);
        stats.getShieldUnfoldRateMult().unmodifyMult(id);
        stats.getPhaseCloakUpkeepCostBonus().unmodifyMult(id);
        stats.getShieldUpkeepMult().unmodifyMult(id);
    }

    @Override
    public void applyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level, ba_bionicitemplugin bionic) {

    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {

    }

    @Override
    public boolean isAdvanceInCombat() {
        return false;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
//        log.info("working");
    }

    @Override
    public boolean isAdvanceInCampaign() {
        return false;
    }

    @Override
    public void advanceInCampaign() {

    }

    @Override
    public void onRemove(PersonAPI person, ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic) {

    }
    @Override
    public void onInstall(PersonAPI person, ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic) {

    }
    @Override
    public void renderExtraOnItem(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemPlugin.SpecialItemRendererAPI renderer) {

    }
}

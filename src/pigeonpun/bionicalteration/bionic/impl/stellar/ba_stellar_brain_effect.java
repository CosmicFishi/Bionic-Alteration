package pigeonpun.bionicalteration.bionic.impl.stellar;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.bionic.ba_bioniceffect;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;

import java.awt.*;

public class ba_stellar_brain_effect implements ba_bioniceffect {
    public static float OFFICER_FIGHTER_RATE_INCREASE_MULT = 1.15f;
    public static float OFFICER_MAINT_MULT = 1.2f;
    public static float ADMIN_DEFENSE_MULT = 1.1f;
    public static float ADMIN_UPKEEP_MULT = 1.15f;
    Logger log = Global.getLogger(ba_stellar_brain_effect.class);

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

        String text = "For captain, increase piloting ship's fighter replacement rate by " + Math.round(OFFICER_FIGHTER_RATE_INCREASE_MULT * 100 - 100) + "%";
        String negativeText = "but increase ship's maintenance by " + Math.round(OFFICER_MAINT_MULT * 100 - 100) + "%";
        String textAdmin = "For admin, increase market defense by " + Math.round(ADMIN_DEFENSE_MULT * 100 - 100) + "%";
        String negativeTextAdmin = "but also increase upkeep cost by " + Math.round(ADMIN_UPKEEP_MULT * 100 - 100) + "%";
        String name = isItem? "Effect:": bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s. %s %s", pad, t, name, text, negativeText, textAdmin, negativeTextAdmin);
        descriptions.setHighlight(name, text, negativeText, textAdmin, negativeTextAdmin);
        descriptions.setHighlightColors(isItem? g.brighter().brighter() : bionic.displayColor, t, bad, t, bad);
    }

    @Override
    public String getShortOnRemoveEffectDescription() {
        return null;
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyMult(id, OFFICER_FIGHTER_RATE_INCREASE_MULT);
        stats.getSuppliesPerMonth().modifyMult(id, OFFICER_MAINT_MULT);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).unmodifyMult(id);
        stats.getSuppliesPerMonth().unmodifyMult(id);
    }

    @Override
    public void applyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level, ba_bionicitemplugin bionic) {
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, ADMIN_DEFENSE_MULT, bionic.getName() + " (Admins bionic)");
        market.getUpkeepMult().modifyMult(id, ADMIN_UPKEEP_MULT,  bionic.getName() + " (Admins bionic)");
    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {
        market.getAccessibilityMod().unmodifyFlat(id);
        market.getUpkeepMult().unmodifyMult(id);
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

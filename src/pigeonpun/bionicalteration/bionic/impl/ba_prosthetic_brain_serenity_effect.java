package pigeonpun.bionicalteration.bionic.impl;

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
import java.util.List;

public class ba_prosthetic_brain_serenity_effect implements ba_bioniceffect {
    public static float TURN_RATE_MULT = 1.2f;
    public static float MAX_SPEED_MULT = 1.1f;
    Logger log = Global.getLogger(ba_prosthetic_brain_serenity_effect.class);
//    @Override
//    public String getShortEffectDescription() {
//        return
//    }

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

        String text = "Increase piloting ship max speed by " + Math.round(MAX_SPEED_MULT * 100 - 100) + "% and turn rate by " + Math.round(TURN_RATE_MULT * 100 - 100) + "%";
        String name = isItem? "Effect:": bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s", pad, t, name, text);
        descriptions.setHighlight(name, text);
        descriptions.setHighlightColors(isItem? g.brighter().brighter() : bionic.displayColor, t);
    }

    @Override
    public String getShortOnRemoveEffectDescription() {
        return null;
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getMaxSpeed().modifyMult(id, MAX_SPEED_MULT);
        stats.getMaxTurnRate().modifyMult(id, TURN_RATE_MULT);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
    }

    @Override
    public void applyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level) {

    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
//        log.info("working");
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

    //todo: do custom rendering with sprite in graphics/icons/danger.png
    @Override
    public void renderExtraOnItem(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemPlugin.SpecialItemRendererAPI renderer) {

    }
}

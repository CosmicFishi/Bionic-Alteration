package pigeonpun.bionicalteration.bionic.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.bionic.ba_bioniceffect;

public class ba_prosthetic_brain_serenity_effect implements ba_bioniceffect {
    public static float TURN_RATE_MULT = 1.2f;
    public static float MAX_SPEED_MULT = 1.1f;
    Logger log = Global.getLogger(ba_prosthetic_brain_serenity_effect.class);
    @Override
    public String getShortEffectDescription() {
        return "Increase piloting ship max speed by " + Math.round(MAX_SPEED_MULT * 100 - 100) + "% and turn rate by " + Math.round(TURN_RATE_MULT * 100 - 100) + "%";
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
    public void advanceInCombat(ShipAPI ship, float amount) {
//        log.info("working");
    }

    @Override
    public void advanceInCampaign() {

    }

    @Override
    public void onRemove() {

    }

    @Override
    public void onInstall() {

    }
    //todo: do custom rendering with sprite in graphics/icons/danger.png
    @Override
    public void renderExtraOnItem() {

    }
}
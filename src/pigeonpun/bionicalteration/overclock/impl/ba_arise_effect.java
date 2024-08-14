package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.impl.domain.ba_brain_neura_matrix_effect;
import pigeonpun.bionicalteration.bionic.impl.domain.ba_heart_zen_core_effect;
import pigeonpun.bionicalteration.overclock.ba_overclock;

import java.awt.*;
import java.util.Random;

public class ba_arise_effect extends ba_overclock {
    static Logger log = Global.getLogger(ba_arise_effect.class);
    public static final float CHANCE_TRIGGERING_INVINCIBLE = 0.05f;
    public static final float DURATION_INVINCIBLE = 5f; //in seconds
    public static final float CD_AFTER_TRIGGER = 30f; //in seconds
    public static final String dataKey = "arise_damage_listener_data";
    String modifyId = "ba_overclock_arise_invincible";
    public ba_arise_effect() {}
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean inBionicTable) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;
        float speedBonus = ba_brain_neura_matrix_effect.MAX_SPEED_MULT;
        if(!inBionicTable) {
            LabelAPI descriptions = tooltip.addPara("" +
                            "Whenever the ship take %s, there is a %s chance of the ship's hull/armor damage taken reduced to %s (Invincibility) for %s seconds. Can only trigger one every %s seconds",
                    pad, t, "hull/armor damage", "" + Math.round(CHANCE_TRIGGERING_INVINCIBLE * 100) + "%", "0%" ,"" + Math.round(DURATION_INVINCIBLE), "" + Math.round(CD_AFTER_TRIGGER));
            descriptions.setHighlightColors(h,special,h,h,h);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Whenever the ship take %s, there is a %s chance of the ship's hull/armor damage taken reduced to %s (Invincibility) for %s seconds. Can only trigger one every %s seconds",
                    pad, t, this.name, "[O]", "hull/armor damage", "" + Math.round(CHANCE_TRIGGERING_INVINCIBLE * 100) + "%", "0%", "" + Math.round(DURATION_INVINCIBLE), "" + Math.round(CD_AFTER_TRIGGER));
            overclockLabel.setHighlightColors(h,special,h,special,h,h,h);
        }
    }
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

    }

    @Override
    public boolean isAdvanceInCombat() {
        return true;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(Global.getCombatEngine().isPaused()) return;
        if(!ship.getListenerManager().hasListenerOfClass(ariseDamageListener.class)) {
            ship.addListener(new ariseDamageListener(ship));
        }
        invincibleData data = new invincibleData();
        if(ship.getCustomData().get(dataKey) instanceof invincibleData) {
            data = (invincibleData) ship.getCustomData().get(dataKey);
        }
        if(data.state == ariseInvincibleState.ACTIVE) {
            ship.getMutableStats().getHullDamageTakenMult().modifyMult(modifyId, 0f);
            ship.getMutableStats().getArmorDamageTakenMult().modifyMult(modifyId, 0f);
            data.activeDuration += amount;
            ship.setJitterUnder(this, ba_variablemanager.BA_OVERCLOCK_COLOR, 1f, 5, 0f, 15f);
            if(data.activeDuration > DURATION_INVINCIBLE) {
                log.error("After activation");
                data.state = ariseInvincibleState.CD;
                data.activeDuration = 0;
            }
        }
        if(data.state == ariseInvincibleState.CD) {
            ship.getMutableStats().getHullDamageTakenMult().unmodifyMult(modifyId);
            ship.getMutableStats().getArmorDamageTakenMult().unmodifyMult(modifyId);
            data.cdDuration += amount;
            ship.setJitterUnder(this, Color.red, 0.6f, 5, 0f, 12f);
            if(data.cdDuration > CD_AFTER_TRIGGER) {
                log.error("After cooldown");
                data.state = ariseInvincibleState.NONE_ACTIVE;
                data.cdDuration = 0;
            }
        }
        ship.setCustomData(dataKey, data);

        if(ship == Global.getCombatEngine().getPlayerShip()) {
            if(data.state == ariseInvincibleState.ACTIVE) {
                Global.getCombatEngine().maintainStatusForPlayerShip(dataKey, "", "Zen Core Invincibility", "Active: " + Math.round(DURATION_INVINCIBLE - data.activeDuration) + "s", false);
            }
            if(data.state == ariseInvincibleState.CD) {
                Global.getCombatEngine().maintainStatusForPlayerShip(dataKey, "", "Zen Core Invincibility", "CD: " + Math.round(CD_AFTER_TRIGGER - data.cdDuration) + "s", true);
            }
            if(data.state == ariseInvincibleState.NONE_ACTIVE) {
                Global.getCombatEngine().maintainStatusForPlayerShip(dataKey, "", "Zen Core Invincibility", "Activatable", false);
            }
        }
    }
    private class ariseDamageListener implements DamageTakenModifier {
        ShipAPI ship;
        public ariseDamageListener(ShipAPI ship) {
            this.ship = ship;

        }
        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if(!shieldHit) {
                invincibleData data = null;
                if(ship.getCustomData().get(dataKey) instanceof invincibleData) {
                    data = (invincibleData) ship.getCustomData().get(dataKey);
                }
                if(data != null) {
                    boolean willTrigger = Math.random() > CHANCE_TRIGGERING_INVINCIBLE;
                    if(willTrigger && data.state == ariseInvincibleState.NONE_ACTIVE) {
                        log.error("Activate");
                        data.state = ariseInvincibleState.ACTIVE;
                    }
                }
                ship.getCustomData().put(dataKey, data);
            }
            return null;
        }
    }
    public enum ariseInvincibleState {
        ACTIVE,
        NONE_ACTIVE,
        CD
    }
    public class invincibleData {
        public float activeDuration;
        public float cdDuration;
        public ariseInvincibleState state = ariseInvincibleState.NONE_ACTIVE;
        public invincibleData() {
            activeDuration = 0;
            cdDuration = 0;
        }
    }
}

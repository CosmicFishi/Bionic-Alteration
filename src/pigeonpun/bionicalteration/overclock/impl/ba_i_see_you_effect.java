package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.impl.domain.ba_brain_neura_matrix_effect;
import pigeonpun.bionicalteration.overclock.ba_overclock;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.awt.*;
import java.util.Random;

public class ba_i_see_you_effect extends ba_overclock {
    public static final float SHIELD_UP_KEEP_MULT = 2.0f;
    public static final float CHANCE_DECREASE_HARD_FLUX_PERC = 0.4f;
    public static final float PROJ_DAMAGE_PERC = 0.5f;
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
                            "Projectile hitting shield has a %s chance of decreasing %s, based on a portion projectile hit point. Shield up keep increase by %s",
                    pad, t, "" + Math.round(CHANCE_DECREASE_HARD_FLUX_PERC * 100) + "%", "hard flux", "" + Math.round(SHIELD_UP_KEEP_MULT * 100 - 100) + "%");
            descriptions.setHighlightColors(h,h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Projectile hitting shield has a %s chance of decreasing %s, based on a portion projectile hit point. Shield up keep increase by %s",
                    pad, t, this.name, "[O]", "" + Math.round(CHANCE_DECREASE_HARD_FLUX_PERC * 100) + "%", "hard flux", "" + Math.round(SHIELD_UP_KEEP_MULT * 100 - 100) + "%");
            overclockLabel.setHighlightColors(h,special,h,h,bad);
        }
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getShieldUpkeepMult().modifyMult(id, SHIELD_UP_KEEP_MULT);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getShieldUpkeepMult().unmodifyMult(id);
    }

    @Override
    public boolean isAdvanceInCombat() {
        return true;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(Global.getCombatEngine().isPaused()) return;
        if(!ship.isAlive() && !Global.getCombatEngine().isEntityInPlay(ship)) return;
        if(!ship.getListenerManager().hasListenerOfClass(iSeeYouDamageListener.class)) {
            ship.addListener(new iSeeYouDamageListener(ship));
        }
    }
    private class iSeeYouDamageListener implements DamageTakenModifier {
        ShipAPI ship;
        public iSeeYouDamageListener(ShipAPI ship) {
            this.ship = ship;

        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if(param instanceof DamagingProjectileAPI && shieldHit) {
                Random rand = ba_utils.getRandom();
                boolean willTrigger = rand.nextDouble() < CHANCE_DECREASE_HARD_FLUX_PERC;
                if(willTrigger) {
                    float preHard = ship.getFluxTracker().getHardFlux();
                    float hitpoint = damage.getDamage() * PROJ_DAMAGE_PERC;
                    float newHard = Math.round(preHard - hitpoint);
                    if(newHard > 0 && preHard > newHard) {
                        ship.getFluxTracker().setHardFlux(newHard);
                    }
                    ship.setJitterShields(true);
                    ship.setJitterUnder(this, new Color(0, 234, 255, 255), 0.8f, 5, 10f, 15f);
                }
            }
            return null;
        }
    }
}

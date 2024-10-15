package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.overclock.ba_overclock;

import java.awt.*;

public class ba_all_cycle extends ba_overclock {
    public static final float HULL_REGEN_UPKEEP_RATIO = 0.2f;
    public static final float HULL_REDUCE = 0.5f;

    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean inBionicTable) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;
        if(!inBionicTable) {
            LabelAPI descriptions = tooltip.addPara("" +
                            "Ship regenerate hull by %s of shield upkeep or phase upkeep per second. Ship hull reduce by %s.",
                    pad, t, "" + Math.round(HULL_REGEN_UPKEEP_RATIO * 100) + "%", "" + Math.round(HULL_REDUCE * 100) + "%");
            descriptions.setHighlightColors(h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Ship regenerate hull by %s of shield upkeep or phase upkeep per second. Ship hull reduce by %s.",
                    pad, t, this.name, "[O]", "" + Math.round(HULL_REGEN_UPKEEP_RATIO * 100) + "%", "" + Math.round(HULL_REDUCE * 100) + "%");
            overclockLabel.setHighlightColors(h,special,h,bad);
        }
    }

    @Override
    public boolean hasCustomHullmodInfo() {
        return true;
    }

    @Override
    public void customHullmodInfo(TooltipMakerAPI tooltip, ShipAPI ship, ba_bionicitemplugin bionic) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;
        LabelAPI descriptions = tooltip.addPara(" - " +
                        "Current ship hull regeneration: %s",
                pad, t, "" + Math.round(getHullRegen(ship)) + "/s");
        descriptions.setHighlightColors(h,bad);
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getHullBonus().modifyMult(id, HULL_REDUCE);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getHullBonus().unmodifyMult(id);
    }
    protected float getHullRegen(ShipAPI ship) {
        float recoverAmount = 0;

        if(ship.getShield() != null) {
            recoverAmount = ship.getShield().getUpkeep() * HULL_REGEN_UPKEEP_RATIO;
        } else {
            if(ship.getPhaseCloak() != null) {
                recoverAmount = ship.getPhaseCloak().getFluxPerSecond() * HULL_REGEN_UPKEEP_RATIO;
            }
        }
        return recoverAmount;
    }

    @Override
    public boolean isAdvanceInCombat() {
        return true;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(Global.getCombatEngine().isPaused()) return;
        if(!ship.isAlive() || !Global.getCombatEngine().isEntityInPlay(ship)) return;
        float recoverAmount = getHullRegen(ship);
        if(recoverAmount > 0 && ship.getHitpoints() < ship.getMaxHitpoints()) {
            ship.setHitpoints(ship.getHitpoints() + recoverAmount * amount);
        }
    }
}

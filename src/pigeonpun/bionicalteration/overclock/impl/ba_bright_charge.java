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

public class ba_bright_charge extends ba_overclock {
    public static final float MANEUVERABILITY = 0.6f;
    public static final float SPEED_BONUS = 50f;
    private String modifyId = "";
    @Override
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
                            "When ship system is active, increase ship max speed by %s. Ship maneuverability reduce by %s",
                    pad, t, "" + Math.round((1-MANEUVERABILITY) * 100) + "%", "" + Math.round(SPEED_BONUS) + "%");
            descriptions.setHighlightColors(h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "When ship system is active, increase ship max speed by %s. Ship maneuverability reduce by %s",
                    pad, t, this.name, "[O]", "" + Math.round((1-MANEUVERABILITY) * 100) + "%", "" + Math.round(SPEED_BONUS) + "%");
            overclockLabel.setHighlightColors(h,special,h,bad);
        }
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        modifyId = id;
        stats.getAcceleration().modifyMult(id, MANEUVERABILITY);
        stats.getDeceleration().modifyMult(id, MANEUVERABILITY);
        stats.getTurnAcceleration().modifyMult(id, MANEUVERABILITY);
        stats.getMaxTurnRate().modifyMult(id, MANEUVERABILITY);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getAcceleration().unmodifyMult(id);
        stats.getDeceleration().unmodifyMult(id);
        stats.getTurnAcceleration().unmodifyMult(id);
        stats.getMaxTurnRate().unmodifyMult(id);
    }

    @Override
    public boolean isAdvanceInCombat() {
        return true;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(Global.getCombatEngine().isPaused()) return;
        if(!ship.isAlive() || !Global.getCombatEngine().isEntityInPlay(ship)) return;
        if(ship.getSystem() != null && ship.getSystem().isActive() && modifyId != "") {
            ship.getMutableStats().getMaxSpeed().modifyPercent(modifyId, SPEED_BONUS);
        } else {
            ship.getMutableStats().getMaxSpeed().unmodifyPercent(modifyId);
        }
    }
}

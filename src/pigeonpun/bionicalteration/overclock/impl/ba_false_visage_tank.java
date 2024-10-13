package pigeonpun.bionicalteration.overclock.impl;

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

public class ba_false_visage_tank extends ba_overclock {
    public static final float MANEUVERABILITY = 0.3f, SHIELD_EFF = 0.6f, ARMOR = 1.4f;
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
                            "Increase ship's shield efficiency by %s and armor by %s but reduce ship maneuverability by %s",
                    pad, t, "" + Math.round((1-SHIELD_EFF) * 100) + "%", "" + Math.round((ARMOR - 1) * 100) + "%","" + Math.round((1-MANEUVERABILITY) * 100) + "%");
            descriptions.setHighlightColors(h,h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Increase ship's shield efficiency by %s and armor by %s but reduce ship maneuverability by %s",
                    pad, t, this.name, "[O]", "" + Math.round((1-SHIELD_EFF) * 100) + "%", "" + Math.round((ARMOR - 1) * 100) + "%","" + Math.round((1-MANEUVERABILITY) * 100) + "%");
            overclockLabel.setHighlightColors(h,special,h,h,bad);
        }
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getAcceleration().modifyMult(id, MANEUVERABILITY);
        stats.getDeceleration().modifyMult(id, MANEUVERABILITY);
        stats.getTurnAcceleration().modifyMult(id, MANEUVERABILITY);
        stats.getMaxTurnRate().modifyMult(id, MANEUVERABILITY);
        stats.getShieldAbsorptionMult().modifyMult(id, SHIELD_EFF);
        stats.getArmorBonus().modifyPercent(id, (ARMOR-1)*100);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getAcceleration().unmodifyMult(id);
        stats.getDeceleration().unmodifyMult(id);
        stats.getTurnAcceleration().unmodifyMult(id);
        stats.getMaxTurnRate().unmodifyMult(id);
        stats.getShieldAbsorptionMult().unmodifyMult(id);
        stats.getArmorBonus().unmodifyPercent(id);
    }
}

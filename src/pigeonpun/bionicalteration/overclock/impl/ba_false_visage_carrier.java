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

public class ba_false_visage_carrier extends ba_overclock {
    public static final float MAX_SPEED = 40f, REPLACEMENT = 1.4f, ROF_MULT = 0.6f, SHIELD_EFF = 1.4f;
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
                            "Increase ship's max speed by %s and fighter replacement rate by %s but reduce ship RoF by %s and increase shield efficiency by %s",
                    pad, t, "" + Math.round(MAX_SPEED) + "%", "" + Math.round((REPLACEMENT-1) * 100) + "%", "" + Math.round((1-ROF_MULT) * 100) + "%", "" + Math.round((SHIELD_EFF - 1) * 100) + "%");
            descriptions.setHighlightColors(h,h,bad,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Increase ship's max speed by %s and fighter replacement rate by %s but reduce ship Ballistic/Energy weapons RoF by %s and increase shield efficiency by %s",
                    pad, t, this.name, "[O]", "" + Math.round(MAX_SPEED) + "%", "" + Math.round((REPLACEMENT-1) * 100) + "%", "" + Math.round((1-ROF_MULT) * 100) + "%", "" + Math.round((SHIELD_EFF - 1) * 100) + "%");
            overclockLabel.setHighlightColors(h,special,h,h,bad,bad);
        }
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getMaxSpeed().modifyPercent(id, MAX_SPEED);
        stats.getFighterRefitTimeMult().modifyMult(id, REPLACEMENT);
        stats.getBallisticRoFMult().modifyMult(id, ROF_MULT);
        stats.getEnergyRoFMult().modifyMult(id, ROF_MULT);
        stats.getShieldAbsorptionMult().modifyMult(id, SHIELD_EFF);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getMaxSpeed().unmodifyPercent(id);
        stats.getFighterRefitTimeMult().unmodifyMult(id);
        stats.getBallisticRoFMult().unmodifyMult(id);
        stats.getEnergyRoFMult().unmodifyMult(id);
        stats.getShieldAbsorptionMult().unmodifyMult(id);
    }
}

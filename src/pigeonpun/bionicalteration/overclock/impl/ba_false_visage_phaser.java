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

public class ba_false_visage_phaser extends ba_overclock {
    public static final float SPEED = 1.4f, FLUX_CAP = 1.4f, HULL = 0.4f;
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
                            "Increase ship's max speed by %s and ship flux capacity by %s but reduce ship hull by %s",
                    pad, t, "" + Math.round((SPEED-1) * 100) + "%", "" + Math.round((FLUX_CAP-1) * 100) + "%", "" + Math.round((1-HULL) * 100) + "%");
            descriptions.setHighlightColors(h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Increase ship's max speed by %s and ship flux capacity by %s but reduce ship hull by %s",
                    pad, t, this.name, "[O]", "" + Math.round((SPEED-1) * 100) + "%", "" + Math.round((FLUX_CAP-1) * 100) + "%", "" + Math.round((1-HULL) * 100) + "%");
            overclockLabel.setHighlightColors(h,special,h,bad);
        }
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getMaxSpeed().modifyPercent(id, (SPEED-1) * 100);
        stats.getFluxCapacity().modifyPercent(id, (FLUX_CAP-1) * 100);
        stats.getHullBonus().modifyMult(id, HULL);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getMaxSpeed().unmodifyPercent(id);
        stats.getFluxCapacity().unmodifyPercent(id);
        stats.getHullBonus().unmodifyMult(id);
    }
}

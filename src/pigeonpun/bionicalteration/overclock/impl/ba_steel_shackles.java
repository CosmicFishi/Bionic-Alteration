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

public class ba_steel_shackles extends ba_overclock {
    public static final float ARMOR_BONUS = 70f;
    public static final float MANEUVERABILITY_REDUCE = 60f;

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
                            "Raise the ship's armor by %s but reduce the ship maneuverability by %s",
                    pad, t, "" + Math.round(ARMOR_BONUS) + "%", "" + Math.round(MANEUVERABILITY_REDUCE) + "%");
            descriptions.setHighlightColors(h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Raise the ship's armor by %s but reduce the ship maneuverability by %s",
                    pad, t, this.name, "[O]", "" + Math.round(ARMOR_BONUS) + "%", "" + Math.round(MANEUVERABILITY_REDUCE) + "%");
            overclockLabel.setHighlightColors(h,special,h,bad);
        }
    }
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getArmorBonus().modifyPercent(id, ARMOR_BONUS);
        float SHIP_MANEUVERABILITY = (100 - MANEUVERABILITY_REDUCE) / 100;
        stats.getAcceleration().modifyMult(id, SHIP_MANEUVERABILITY * 2f);
        stats.getDeceleration().modifyMult(id, SHIP_MANEUVERABILITY);
        stats.getTurnAcceleration().modifyMult(id, SHIP_MANEUVERABILITY * 2f);
        stats.getMaxTurnRate().modifyMult(id, SHIP_MANEUVERABILITY);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getArmorBonus().unmodifyPercent(id);
        stats.getAcceleration().unmodifyMult(id);
        stats.getDeceleration().unmodifyMult(id);
        stats.getTurnAcceleration().unmodifyMult(id);
        stats.getMaxTurnRate().unmodifyMult(id);
    }
}

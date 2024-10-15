package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.impl.domain.ba_brain_neura_matrix_effect;
import pigeonpun.bionicalteration.overclock.ba_overclock;

import java.awt.*;

public class ba_cyclobs_effect extends ba_overclock {
    public static final float PD_RANGE_BONUS = 300f;
    public static final float PD_FLUX_BONUS = 1.2f;
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
                            "Extends the range of point-defense weapons by %s but increase beam weapons flux cost by %s",
                    pad, t, "" + Math.round(PD_RANGE_BONUS) + "u", "" + Math.round(PD_FLUX_BONUS * 100 - 100) + "%");
            descriptions.setHighlightColors(h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Extends the range of point-defense weapons by %s but increase beam weapons flux cost by %s",
                    pad, t, this.name, "[O]", "" + Math.round(PD_RANGE_BONUS) + "u", "" + Math.round(PD_FLUX_BONUS * 100 - 100) + "%");
            overclockLabel.setHighlightColors(h,special,h,bad);
        }
    }
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getBeamPDWeaponRangeBonus().modifyFlat(id, PD_RANGE_BONUS);
        stats.getBeamWeaponFluxCostMult().modifyMult(id, PD_FLUX_BONUS);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getBeamPDWeaponRangeBonus().unmodifyFlat(id);
        stats.getBeamWeaponFluxCostMult().unmodifyMult(id);
    }
}

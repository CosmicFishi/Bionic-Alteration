package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.impl.domain.ba_brain_neura_matrix_effect;
import pigeonpun.bionicalteration.overclock.ba_overclock;

import java.awt.*;

public class ba_based_instinct_effect extends ba_overclock {
    static Logger log = Global.getLogger(ba_based_instinct_effect.class);
    public static final float ROF_BONUS_MULT = 1.4f;
    public ba_based_instinct_effect() {}

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
                            "\"%s\". Ship's speed reduced by %s, increase ballistic and energy weapons ROF by %s",
                    pad, t, "Well now that you mention, annihilation is my specialty", "" + Math.round(speedBonus * 100 - 100) + "%", "" + Math.round(ROF_BONUS_MULT * 100 - 100)+"%");
            descriptions.setHighlightColors(h,bad,h);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "\"%s\". Ship's speed reduced by %s, increase ballistic and energy weapons ROF by %s",
                    pad, t, this.name, "[O]", "Well now that you mention, annihilation is my specialty", "" + Math.round(speedBonus * 100 - 100) + "%", "" + Math.round(ROF_BONUS_MULT * 100 - 100)+"%");
            overclockLabel.setHighlightColors(h,special,h,bad,h);
        }
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        float speedBonus = ba_brain_neura_matrix_effect.MAX_SPEED_MULT;
        stats.getMaxSpeed().modifyPercent(id, 100 - (speedBonus * 100));
        stats.getEnergyRoFMult().modifyPercent(id, (ROF_BONUS_MULT * 100) - 100);
        stats.getBallisticRoFMult().modifyPercent(id, (ROF_BONUS_MULT * 100) - 100);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getMaxSpeed().unmodifyPercent(id);
        stats.getEnergyRoFMult().unmodifyPercent(id);
        stats.getBallisticRoFMult().unmodifyPercent(id);
    }

    @Override
    public boolean isAdvanceInCombat() {
        return false;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }
}

package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
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

public class ba_concentration_effect extends ba_overclock {
    static Logger log = Global.getLogger(ba_concentration_effect.class);
    public static final float SYSTEM_CD_PERCENTAGE = 30f;
    public static final float ROF_REDUCE_MULT = 0.7f;
    public ba_concentration_effect() {}
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
                            "If the ship %s, reduce ship system cooldown by %s but ballistic/energy weapons RoF reduced by %s",
                    pad, t, "has shield", "" + Math.round(SYSTEM_CD_PERCENTAGE) + "%", "" + Math.round(100 - ROF_REDUCE_MULT * 100) + "%");
            descriptions.setHighlightColors(h,h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "If the ship %s, reduce ship system cooldown by %s but ballistic/energy weapons RoF reduced by %s",
                    pad, t, this.name, "[O]", "has shield", "" + Math.round(SYSTEM_CD_PERCENTAGE) + "%", "" + Math.round(100 - ROF_REDUCE_MULT * 100) + "%");
            overclockLabel.setHighlightColors(h,special,h,h,bad);
        }
    }
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(stats.getEntity() != null) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            if(ship.getShield() != null) {
                stats.getSystemCooldownBonus().modifyPercent(id, SYSTEM_CD_PERCENTAGE);
                stats.getEnergyRoFMult().modifyMult(id, ROF_REDUCE_MULT);
                stats.getBallisticRoFMult().modifyMult(id, ROF_REDUCE_MULT);
            }
        }
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getSystemCooldownBonus().unmodifyPercent(id);
        stats.getEnergyRoFMult().unmodifyMult(id);
        stats.getBallisticRoFMult().unmodifyMult(id);
    }

    @Override
    public boolean isAdvanceInCombat() {
        return false;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }
}

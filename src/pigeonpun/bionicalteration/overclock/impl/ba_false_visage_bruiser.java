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

public class ba_false_visage_bruiser extends ba_overclock {
    public static final float ROF_MULT = 1.3f;
    public static final float PROJ_SPEED_MULT = 1.4f;
    public static final float MAX_SPEED_MULT = 0.6f;
    public static final float ARMOR_MULT = 0.6f;
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
                            "Increase Ballistic/Energy/Missile weapon RoF by %s, increase projectile speed by %s but reduce max speed by %s and armor by %s",
                    pad, t, "" + Math.round((ROF_MULT -1) * 100) + "%", "" + Math.round((PROJ_SPEED_MULT -1) * 100) + "%", "" + Math.round((1-MAX_SPEED_MULT) * 100) + "%", "" + Math.round((1-ARMOR_MULT) * 100) + "%");
            descriptions.setHighlightColors(h,h,bad,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Increase Ballistic/Energy/Missile weapon RoF by %s, increase projectile speed by %s but reduce max speed by %s and armor by %s",
                    pad, t, this.name, "[O]", "" + Math.round((ROF_MULT -1) * 100) + "%", "" + Math.round((PROJ_SPEED_MULT -1) * 100) + "%", "" + Math.round((1-MAX_SPEED_MULT) * 100) + "%", "" + Math.round((1-ARMOR_MULT) * 100) + "%");
            overclockLabel.setHighlightColors(h,special,h,h,bad,bad);
        }
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getBallisticRoFMult().modifyMult(id, ROF_MULT);
        stats.getEnergyRoFMult().modifyMult(id, ROF_MULT);
        stats.getMissileRoFMult().modifyMult(id, ROF_MULT);
        stats.getProjectileSpeedMult().modifyMult(id, PROJ_SPEED_MULT);
        stats.getMaxSpeed().modifyMult(id, MAX_SPEED_MULT);
        stats.getArmorBonus().modifyMult(id, ARMOR_MULT);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getBallisticRoFMult().unmodifyMult(id);
        stats.getEnergyRoFMult().unmodifyMult(id);
        stats.getMissileRoFMult().unmodifyMult(id);
        stats.getProjectileSpeedMult().unmodifyMult(id);
        stats.getMaxSpeed().unmodifyMult(id);
        stats.getArmorBonus().unmodifyMult(id);
    }
}

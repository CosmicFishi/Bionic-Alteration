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

public class ba_let_dance extends ba_overclock {
    public static final float SPEED_INCREMENT = 10f;
    public static final float TIME_DILATION_PER_INCREMENT = 0.01f; //percentage
    public static final float MAINT = 60f;
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
                            "Each %s increment for max speed result in %s increment in ship's time dilation. Ship maintenance increase by %s",
                    pad, t, "" + Math.round(SPEED_INCREMENT) + "u", "" + Math.round(TIME_DILATION_PER_INCREMENT * 100) + "%", "" + Math.round(MAINT) + "%");
            descriptions.setHighlightColors(h,h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Each %s increment for max speed result in %s increment in ship's time dilation. Ship maintenance increase by %s",
                    pad, t, this.name, "[O]", "" + Math.round(SPEED_INCREMENT) + "u", "" + Math.round(TIME_DILATION_PER_INCREMENT * 100) + "%", "" + Math.round(MAINT) + "%");
            overclockLabel.setHighlightColors(h,special,h,h,bad);
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
                        "Current time dilation bonus: %s",
                pad, t, "" + Math.round(getTimeDilation(ship)) + "%");
        descriptions.setHighlightColors(h,bad);
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(stats.getEntity() != null) {
            stats.getTimeMult().modifyMult(id, 1 + (getTimeDilation((ShipAPI) stats.getEntity())/100));
        }
        stats.getSuppliesPerMonth().modifyPercent(id, MAINT);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getSuppliesPerMonth().unmodifyPercent(id);
        stats.getTimeMult().unmodifyMult(id);
    }
    protected float getTimeDilation(ShipAPI ship) {
        if(ship != null) {
            return (float) Math.floor(ship.getMutableStats().getMaxSpeed().getModifiedValue()/SPEED_INCREMENT);
        }
        return 0;
    }
}

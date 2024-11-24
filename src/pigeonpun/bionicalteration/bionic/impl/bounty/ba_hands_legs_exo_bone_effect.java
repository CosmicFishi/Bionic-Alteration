package pigeonpun.bionicalteration.bionic.impl.bounty;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_hands_legs_exo_bone_effect extends ba_bionicitemplugin {
    public static final float HULL_BONUS_FRIGATE = 20f, HULL_BONUS_DESTROYER = 30f;
    public static final float WPN_REPAIR_RATE_FRIGATE = 20f, WPN_REPAIR_RATE_DESTROYER = 30f;
    @Override
    public void afterInit() {
        super.setApplicable(true, true, false, false);
    }
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's hull by";
        String textNum = Math.round(HULL_BONUS_FRIGATE) + "%/" + Math.round(HULL_BONUS_DESTROYER) + "%";
        String text2 = "but decrease weapon repair rate by";
        String text2Num = Math.round(WPN_REPAIR_RATE_FRIGATE) + "%/" + Math.round(WPN_REPAIR_RATE_DESTROYER) + "%";
        String text3 = "Only has effects on";
        String text3Num = "Frigate/Destroyer";
        String name = isItem ? "Effect:" : bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s %s %s. %s %s", pad, t, name, text, textNum, text2, text2Num, text3, text3Num);
        descriptions.setHighlightColors(isItem ? g.brighter().brighter() : bionic.displayColor, t, h, t, bad, t, h);
    }

    @Override
    public boolean hasCustomHullmodInfo() {
        return true;
    }

    @Override
    public void customHullmodInfo(TooltipMakerAPI tooltip, ShipAPI ship, ba_bionicitemplugin bionic) {
        customBountyBionicHullmodState(tooltip, ship);
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(stats.getEntity() != null) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            if(isHullSizeCorrect(ship)) {
                if(ship.getHullSize().equals(ShipAPI.HullSize.FRIGATE)) {
                    stats.getCombatWeaponRepairTimeMult().modifyPercent(id, WPN_REPAIR_RATE_FRIGATE);
//                    stats.getHullBonus().modifyPercent(id, HULL_BONUS_FRIGATE);
                } else if (ship.getHullSize().equals(ShipAPI.HullSize.DESTROYER)) {
                    stats.getCombatWeaponRepairTimeMult().modifyPercent(id, WPN_REPAIR_RATE_DESTROYER);
//                    stats.getHullBonus().modifyPercent(id, HULL_BONUS_DESTROYER);
                }
            }
        }
    }

    @Override
    public void applyOfficerEffectBeforeShipCreation(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(hullSize.equals(ShipAPI.HullSize.FRIGATE)) {
            stats.getHullBonus().modifyPercent(id, HULL_BONUS_FRIGATE);
        } else if (hullSize.equals(ShipAPI.HullSize.DESTROYER)) {
            stats.getHullBonus().modifyPercent(id, HULL_BONUS_DESTROYER);
        }
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getCombatWeaponRepairTimeMult().unmodifyPercent(id);
        stats.getHullBonus().unmodifyPercent(id);
    }
}

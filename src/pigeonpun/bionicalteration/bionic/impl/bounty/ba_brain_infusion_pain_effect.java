package pigeonpun.bionicalteration.bionic.impl.bounty;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_brain_infusion_pain_effect extends ba_bionicitemplugin{
    public static final float HULL_BONUS_CRUISER = 20f, HULL_BONUS_CAPITAL = 40f;
    public static final float FLUX_PERC_CRUISER = 10f, FLUX_PERC_CAPITAL = 20f;
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's hull by";
        String textNum = Math.round(HULL_BONUS_CRUISER) + "%/" + Math.round(HULL_BONUS_CAPITAL) + "%";
        String text2 = "but increase ballistic/energy weapon's flux by";
        String text2Num = Math.round(FLUX_PERC_CRUISER) + "%/" + Math.round(FLUX_PERC_CAPITAL) + "%";
        String text3 = "Only has effects on";
        String text3Num = "Cruiser/Capital";
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
        customBountyBionicHullmodState(tooltip, ship, false, false, true, true);
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(stats.getEntity() != null) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            if(isHullSizeCorrect(ship, false, false, true, true)) {
                if(ship.getHullSize().equals(ShipAPI.HullSize.CRUISER)) {
//                    stats.getHullBonus().modifyPercent(id, HULL_BONUS_CRUISER);
                    stats.getBallisticWeaponFluxCostMod().modifyPercent(id, FLUX_PERC_CRUISER);
                    stats.getEnergyWeaponFluxCostMod().modifyPercent(id, FLUX_PERC_CRUISER);
                } else if (ship.getHullSize().equals(ShipAPI.HullSize.CAPITAL_SHIP)) {
//                    stats.getHullBonus().modifyPercent(id, HULL_BONUS_CAPITAL);
                    stats.getBallisticWeaponFluxCostMod().modifyPercent(id, FLUX_PERC_CAPITAL);
                    stats.getEnergyWeaponFluxCostMod().modifyPercent(id, FLUX_PERC_CAPITAL);
                }
            }
        }
    }

    @Override
    public void applyOfficerEffectBeforeShipCreation(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(hullSize.equals(ShipAPI.HullSize.CRUISER)) {
            stats.getHullBonus().modifyPercent(id, HULL_BONUS_CRUISER);
        } else if (hullSize.equals(ShipAPI.HullSize.CAPITAL_SHIP)) {
            stats.getHullBonus().modifyPercent(id, HULL_BONUS_CAPITAL);
        }
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getHullBonus().unmodifyPercent(id);
        stats.getBallisticWeaponFluxCostMod().unmodifyPercent(id);
        stats.getEnergyWeaponFluxCostMod().unmodifyPercent(id);
    }
}

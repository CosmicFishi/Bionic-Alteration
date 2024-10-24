package pigeonpun.bionicalteration.bionic.impl.bounty;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_heart_infusion_pulse_effect extends ba_bionicitemplugin{
    public static final float EMP_BONUS_DESTROYER = 30f,EMP_BONUS_CRUISER = 40f, EMP_BONUS_CAPITAL = 50f;
    public static final float SHIELD_ARC_DESTROYER = 10f, SHIELD_ARC_CRUISER = 20f, SHIELD_ARC_CAPITAL = 30f;
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Decrease piloting ship's EMP damage taken by";
        String textNum = Math.round(EMP_BONUS_DESTROYER) + "%/" + Math.round(EMP_BONUS_CRUISER) + "%/" + Math.round(EMP_BONUS_CAPITAL) + "%";
        String text2 = "but decrease ship's shield arc by";
        String text2Num = Math.round(SHIELD_ARC_DESTROYER) + "/" + Math.round(SHIELD_ARC_CRUISER) + "/" + Math.round(SHIELD_ARC_CAPITAL) + "";
        String text3 = "Only has effects on";
        String text3Num = "Destroyer/Cruiser/Capital";
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
        customBountyBionicHullmodState(tooltip, ship, false, true, true ,true);
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(stats.getEntity() != null) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            if(isHullSizeCorrect(ship, false, true, true ,true)) {
                if(ship.getHullSize().equals(ShipAPI.HullSize.DESTROYER)) {
                    stats.getEmpDamageTakenMult().modifyMult(id, 1-(EMP_BONUS_DESTROYER/100));
//                    stats.getShieldArcBonus().modifyFlat(id, -SHIELD_ARC_DESTROYER);
                } else if (ship.getHullSize().equals(ShipAPI.HullSize.CRUISER)) {
                    stats.getEmpDamageTakenMult().modifyMult(id, 1-(EMP_BONUS_CRUISER/100));
//                    stats.getShieldArcBonus().modifyFlat(id, -SHIELD_ARC_CRUISER);
                } else if (ship.getHullSize().equals(ShipAPI.HullSize.CAPITAL_SHIP)) {
                    stats.getEmpDamageTakenMult().modifyMult(id, 1-(EMP_BONUS_CAPITAL/100));
//                    stats.getShieldArcBonus().modifyFlat(id, -SHIELD_ARC_CAPITAL);
                }
            }
        }
    }

    @Override
    public void applyOfficerEffectBeforeShipCreation(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(hullSize.equals(ShipAPI.HullSize.DESTROYER)) {
            stats.getShieldArcBonus().modifyFlat(id, -SHIELD_ARC_DESTROYER);
        } else if (hullSize.equals(ShipAPI.HullSize.CRUISER)) {
            stats.getShieldArcBonus().modifyFlat(id, -SHIELD_ARC_CRUISER);
        } else if (hullSize.equals(ShipAPI.HullSize.CAPITAL_SHIP)) {
            stats.getShieldArcBonus().modifyFlat(id, -SHIELD_ARC_CAPITAL);
        }
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getEmpDamageTakenMult().unmodifyMult(id);
        stats.getShieldArcBonus().unmodifyFlat(id);
    }
}

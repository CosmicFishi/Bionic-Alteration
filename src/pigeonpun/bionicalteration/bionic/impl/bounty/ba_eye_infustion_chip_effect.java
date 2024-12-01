package pigeonpun.bionicalteration.bionic.impl.bounty;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_eye_infustion_chip_effect extends ba_bionicitemplugin{
    public static final float RANGE_BONUS_FRIGATE = 30f, RANGE_BONUS_DESTROYER = 20f;
    public static final float SHIELD_EFF_FRIGATE = 15f, SHIELD_EFF_DESTROYER = 10f;
    @Override
    public void afterInit() {
        super.setApplicable(true, true, false ,false);
    }
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's max ballistic/energy weapon range by";
        String textNum = Math.round(RANGE_BONUS_FRIGATE) + "%/" + Math.round(RANGE_BONUS_DESTROYER) + "%";
        String text2 = "but decrease shield efficiency by";
        String text2Num = Math.round(SHIELD_EFF_FRIGATE) + "%/" + Math.round(SHIELD_EFF_DESTROYER) + "%";
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
                    stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_FRIGATE);
                    stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_FRIGATE);
                    stats.getShieldAbsorptionMult().modifyMult(id, 1+(SHIELD_EFF_FRIGATE/100));
                } else if (ship.getHullSize().equals(ShipAPI.HullSize.DESTROYER)) {
                    stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_DESTROYER);
                    stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS_DESTROYER);
                    stats.getShieldAbsorptionMult().modifyMult(id, 1+(SHIELD_EFF_DESTROYER/100));
                }
            }
        }
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getBallisticWeaponRangeBonus().unmodifyPercent(id);
        stats.getEnergyWeaponRangeBonus().unmodifyPercent(id);
        stats.getShieldAbsorptionMult().unmodifyPercent(id);
    }
}

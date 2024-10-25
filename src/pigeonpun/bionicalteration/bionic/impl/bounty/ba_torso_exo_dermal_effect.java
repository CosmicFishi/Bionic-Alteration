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

public class ba_torso_exo_dermal_effect extends ba_bionicitemplugin {
    public static final float ARMOR_DMG_BONUS_FRIGATE = 30f, ARMOR_DMG_BONUS_DESTROYER = 25f, ARMOR_DMG_BONUS_CRUISER = 20f;
    public static final float FLUX_CAP_FRIGATE = 20f, FLUX_CAP_DESTROYER = 15f, FLUX_CAP_CRUISER = 10f;

    @Override
    public void afterInit() {
        super.setApplicable(true, true, true ,false);
    }

    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Decrease piloting ship's armor damage taken by";
        String textNum = Math.round(ARMOR_DMG_BONUS_FRIGATE) + "%/" + Math.round(ARMOR_DMG_BONUS_DESTROYER) + "%/" + Math.round(ARMOR_DMG_BONUS_CRUISER) + "%";
        String text2 = "but decrease ship's flux capacity by";
        String text2Num = Math.round(FLUX_CAP_FRIGATE) + "%/" + Math.round(FLUX_CAP_DESTROYER) + "%/" + Math.round(FLUX_CAP_CRUISER) + "%";
        String text3 = "Only has effects on";
        String text3Num = super.getApplicableHullSizeText();
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
                if(ship.getHullSize().equals(ShipAPI.HullSize.DESTROYER)) {
                    stats.getArmorDamageTakenMult().modifyMult(id, 1-(ARMOR_DMG_BONUS_DESTROYER/100));
                    stats.getFluxCapacity().modifyMult(id, 1-(FLUX_CAP_DESTROYER/100));
                } else if (ship.getHullSize().equals(ShipAPI.HullSize.CRUISER)) {
                    stats.getArmorDamageTakenMult().modifyMult(id, 1-(ARMOR_DMG_BONUS_CRUISER/100));
                    stats.getFluxCapacity().modifyMult(id, 1-(FLUX_CAP_CRUISER/100));
                } else if (ship.getHullSize().equals(ShipAPI.HullSize.FRIGATE)) {
                    stats.getArmorDamageTakenMult().modifyMult(id, 1-(ARMOR_DMG_BONUS_FRIGATE/100));
                    stats.getFluxCapacity().modifyMult(id, 1-(FLUX_CAP_FRIGATE/100));
                }
            }
        }
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getArmorDamageTakenMult().unmodifyMult(id);
        stats.getFluxCapacity().unmodifyMult(id);
    }
}

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

public class ba_mouth_exo_vocal_effect extends ba_bionicitemplugin {
    public static final float RELOAD_BONUS_CRUISER = 40f, RELOAD_BONUS_CAPITAL = 20f;
    public static final float HULL_DMG_TAKEN_CRUISER = 25f, HULL_DMG_TAKEN_CAPITAL = 10f;
    @Override
    public void afterInit() {
        super.setApplicable(false, false, true, true);
    }
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's ballistic/energy weapons reload rate by";
        String textNum = Math.round(RELOAD_BONUS_CRUISER) + "%/" + Math.round(RELOAD_BONUS_CAPITAL) + "%";
        String text2 = "but increase ship hull damage taken by";
        String text2Num = Math.round(HULL_DMG_TAKEN_CRUISER) + "%/" + Math.round(HULL_DMG_TAKEN_CAPITAL) + "%";
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
        customBountyBionicHullmodState(tooltip, ship);
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(stats.getEntity() != null) {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            if(isHullSizeCorrect(ship)) {
                if(ship.getHullSize().equals(ShipAPI.HullSize.CRUISER)) {
                    stats.getHullDamageTakenMult().modifyPercent(id, HULL_DMG_TAKEN_CRUISER);
                    stats.getBallisticAmmoRegenMult().modifyPercent(id, RELOAD_BONUS_CRUISER);
                    stats.getEnergyAmmoRegenMult().modifyPercent(id, RELOAD_BONUS_CRUISER);
                } else if (ship.getHullSize().equals(ShipAPI.HullSize.CAPITAL_SHIP)) {
                    stats.getHullDamageTakenMult().modifyPercent(id, HULL_DMG_TAKEN_CAPITAL);
                    stats.getBallisticAmmoRegenMult().modifyPercent(id, RELOAD_BONUS_CAPITAL);
                    stats.getEnergyAmmoRegenMult().modifyPercent(id, RELOAD_BONUS_CAPITAL);
                }
            }
        }
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getHullDamageTakenMult().unmodifyPercent(id);
        stats.getBallisticAmmoRegenMult().unmodifyPercent(id);
        stats.getEnergyAmmoRegenMult().unmodifyPercent(id);
    }
}

package pigeonpun.bionicalteration.hullmod;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;

import java.awt.*;
import java.util.List;

public class ba_bionicinfo extends BaseHullMod {
    //todo: Add apply effect before/after ship creation to the bionic
    public static final float tooltipWitdth = 500f;
    @Override
    public float getTooltipWidth() {
        return tooltipWitdth;
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize,
                                               MutableShipStatsAPI stats, String id) {
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;
        float col1W = 50;
        float lastW = 362;

        if(ship != null && ship.getCaptain() != null && !isForModSpec) {
            if(ba_officermanager.isCaptainOrAdmin(ship.getCaptain(), false).equals(ba_officermanager.ba_profession.CAPTAIN) || ship.getCaptain().isPlayer()) {
                List<ba_officermanager.ba_bionicAugmentedData> bionicData = ba_officermanager.getBionicAnatomyList(ship.getCaptain());
                boolean isEmpty = true;
                for(ba_officermanager.ba_bionicAugmentedData data :bionicData) {
                    if(data.bionicInstalled != null) {
                        isEmpty = false;
                        UIComponentAPI border = tooltip.createRect(g.darker().darker(), 1);
                        border.getPosition().setSize(tooltipWitdth, 1);
                        tooltip.addCustom(border, pad);
                        data.bionicInstalled.displayEffectDescription(tooltip, ship.getCaptain(), data.bionicInstalled, false);
                        if(data.appliedOverclock != null) {
                            data.appliedOverclock.displayEffectDescription(tooltip, ship.getCaptain(), data.bionicInstalled, true);
                            if(data.appliedOverclock.hasCustomHullmodInfo()) {
                                data.appliedOverclock.customHullmodInfo(tooltip, ship, data.bionicInstalled);
                            }
                        }
                    }
                }
                if(isEmpty) {
                    LabelAPI empty = tooltip.addPara("No augmentation information found.", pad);
                }

            } else {
                //save spot for the AI fleet
            }
        } else {
            LabelAPI info = tooltip.addPara("You shouldn't be able to get this item at the first place :(", pad);
        }
    }
}

package pigeonpun.bionicalteration.hullmod;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;

import java.awt.*;
import java.util.List;

public class ba_bionicinfo extends BaseHullMod {
    public static final float tooltipWitdth = 600f;
    @Override
    public float getTooltipWidth() {
        return tooltipWitdth;
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize,
                                               MutableShipStatsAPI stats, String id) {
        if(stats.getFleetMember() != null && !stats.getFleetMember().getCaptain().isDefault()) {
            PersonAPI captain = stats.getFleetMember().getCaptain();
            List<ba_officermanager.ba_bionicAugmentedData> listAnatomy = ba_officermanager.getBionicAnatomyList(captain);
            for(ba_officermanager.ba_bionicAugmentedData anatomy: listAnatomy) {
                if(anatomy.bionicInstalled != null) {
                    if(anatomy.bionicInstalled != null && anatomy.bionicInstalled.isApplyCaptainEffect) {
                        String applyId = id + anatomy.bionicInstalled.bionicId + anatomy.limb;
                        anatomy.bionicInstalled.applyOfficerEffectBeforeShipCreation(stats, hullSize, applyId);
                    }
                    if(anatomy.appliedOverclock != null) {
                        if(anatomy.appliedOverclock.isApplyCaptainEffect) {
                            String applyId = id + "_" + anatomy.bionicInstalled.bionicId + "_" + anatomy.appliedOverclock.id + "_" + anatomy.limb;
                            anatomy.appliedOverclock.applyOfficerEffectBeforeShipCreation(stats, hullSize, applyId);
                        }
                    }
                }
            }
        }
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

        if(ship != null && ship.getCaptain() != null && !ship.getCaptain().isDefault() && !isForModSpec) {
            boolean isEmpty = true;
            if(ba_officermanager.isCaptainOrAdmin(ship.getCaptain(), false).equals(ba_officermanager.ba_profession.CAPTAIN) || ship.getCaptain().isPlayer()) {
                ba_consciousmanager.getConsciousnessLevel(ship.getCaptain()).displayTooltipDescription(tooltip, ship.getCaptain(), true, true);
                List<ba_officermanager.ba_bionicAugmentedData> bionicData = ba_officermanager.getBionicAnatomyList(ship.getCaptain());

                for(ba_officermanager.ba_bionicAugmentedData data :bionicData) {
                    if(data.bionicInstalled != null) {
                        isEmpty = false;
                        UIComponentAPI border = tooltip.createRect(g.darker().darker(), 1);
                        border.getPosition().setSize(tooltipWitdth, 1);
                        tooltip.addCustom(border, pad);
                        data.bionicInstalled.displayEffectDescription(tooltip, ship.getCaptain(), data.bionicInstalled, false);
                        if(data.bionicInstalled.hasCustomHullmodInfo()) {
                            data.bionicInstalled.customHullmodInfo(tooltip, ship, data.bionicInstalled);
                        }
                        if(data.appliedOverclock != null) {
                            data.appliedOverclock.displayEffectDescription(tooltip, ship.getCaptain(), data.bionicInstalled, true);
                            if(data.appliedOverclock.hasCustomHullmodInfo()) {
                                data.appliedOverclock.customHullmodInfo(tooltip, ship, data.bionicInstalled);
                            }
                        }
                    }
                }
            }
            if(isEmpty) {
                LabelAPI empty = tooltip.addPara("No augmentation information found.", pad);
            }
        } else {
            LabelAPI info = tooltip.addPara("No information found.", pad);
        }
    }
}

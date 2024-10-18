package pigeonpun.bionicalteration.bionic.impl.domain;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_eye_holovision_effect extends ba_bionicitemplugin {
    public static float TIME_DAL_BONUS = 0.1f;
    public static float CR_REDUCE_ON_REMOVE = 0.25f;
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's time dilation by";
        String textNum = Math.round(TIME_DAL_BONUS * 100) + "%";
        String name = isItem ? "Effect:" : bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s", pad, t, name, text, textNum);
        descriptions.setHighlightColors(isItem ? g.brighter().brighter() : bionic.displayColor, t, h);
    }
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getTimeMult().modifyPercent(id, (TIME_DAL_BONUS * 100));
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getTimeMult().unmodifyPercent(id);
    }

    @Override
    public void onRemove(PersonAPI person, ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic) {
        if((person.getFleet() != null && person.getFleet().getFleetData().getMemberWithCaptain(person) != null) || person.isPlayer()) {
            float cost = CR_REDUCE_ON_REMOVE;
            FleetMemberAPI member = null;
            if(person.isPlayer()) {
                 member = Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(person);
            } else {
                member = person.getFleet().getFleetData().getMemberWithCaptain(person);
            }
            if(member != null) {
                float endCR = member.getRepairTracker().getCR() - cost;;
                float cr = member.getRepairTracker().getBaseCR();
                if (cr > endCR) {
                    member.getRepairTracker().applyCREvent(-(cr - endCR), "Removed " + bionic.getName() + " bionic");
                }
            }
        }
    }

    @Override
    public void getLongOnRemoveEffectDescription(TooltipMakerAPI tooltip) {
        LabelAPI label = tooltip.addPara("On remove, reduce ship combat readiness by %s", 10f ,Misc.getTextColor(), "" + Math.round(CR_REDUCE_ON_REMOVE * 100) + "%");
        label.setHighlightColors(Misc.getNegativeHighlightColor());
    }
}

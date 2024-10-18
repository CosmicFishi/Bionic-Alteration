package pigeonpun.bionicalteration.bionic.impl.unknown;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_brain_false_visage_effect extends ba_bionicitemplugin {
    public static final float DP_DECREASE_MULT = 0.25f;
    public static final float CR_REDUCE_ON_REMOVE = 1f;
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Deployment point cost of ship reduced by";
        String textNum = Math.round(DP_DECREASE_MULT * 100) + "%";
        String name = isItem ? "Effect:" : bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s", pad, t, name, text, textNum);
        descriptions.setHighlightColors(isItem ? g.brighter().brighter() : bionic.displayColor, t, h);
    }
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyMult(id, 1-DP_DECREASE_MULT);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodifyMult(id);
    }

    @Override
    public void getLongOnRemoveEffectDescription(TooltipMakerAPI tooltip) {
        LabelAPI label = tooltip.addPara("On remove, reduce ship combat readiness by %s", 10f ,Misc.getTextColor(), "" + Math.round(CR_REDUCE_ON_REMOVE * 100) + "%");
        label.setHighlightColors(Misc.getNegativeHighlightColor());
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
}

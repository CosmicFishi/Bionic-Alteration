package pigeonpun.bionicalteration.bionic.impl.unknown;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_brain_false_visage_effect extends ba_bionicitemplugin {
    public static final float DP_DECREASE_MULT = 0.25f;
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
}

package pigeonpun.bionicalteration.bionic.impl.domain;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_leg_hand_anti_g_effect extends ba_bionicitemplugin {
    public static final float MANEUVERABILITY_BONUS = 100f;
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's maneuverability by";
        String textNum = Math.round(MANEUVERABILITY_BONUS) + "%";
        String name = isItem ? "Effect:" : bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s", pad, t, name, text, textNum);
        descriptions.setHighlightColors(isItem ? g.brighter().brighter() : bionic.displayColor, t, h);
    }
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS);
        stats.getDeceleration().modifyPercent(id, MANEUVERABILITY_BONUS);
        stats.getTurnAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS);
        stats.getMaxTurnRate().modifyPercent(id, MANEUVERABILITY_BONUS);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getAcceleration().unmodifyPercent(id);
        stats.getDeceleration().unmodifyPercent(id);
        stats.getTurnAcceleration().unmodifyPercent(id);
        stats.getMaxTurnRate().unmodifyPercent(id);
    }
}

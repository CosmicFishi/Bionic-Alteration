package pigeonpun.bionicalteration.bionic.impl.domain;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_torso_nexa_harmonizer_effect extends ba_bionicitemplugin {
    public static float PEAK_TIME_FRIGATE = 100f,
            PEAK_TIME_DESTROYER = 65f,
            PEAK_TIME_CRUISER = 40f,
            PEAK_TIME_CAPITAL = 20f;
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's peak time by";
        String textNum = Math.round(PEAK_TIME_FRIGATE) + "/" + Math.round(PEAK_TIME_DESTROYER) + "/" + Math.round(PEAK_TIME_CRUISER) + "/" + Math.round(PEAK_TIME_CAPITAL) + "%";
        String name = isItem ? "Effect:" : bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s", pad, t, name, text, textNum);
        descriptions.setHighlightColors(isItem ? g.brighter().brighter() : bionic.displayColor, t, h);
    }
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        float peakTime = 0;
        if(hullSize != null) {
            switch (hullSize) {
                case FRIGATE:
                    peakTime = PEAK_TIME_FRIGATE;
                    break;
                case DESTROYER:
                    peakTime = PEAK_TIME_DESTROYER;
                    break;
                case CRUISER:
                    peakTime = PEAK_TIME_CRUISER;
                    break;
                case CAPITAL_SHIP:
                    peakTime = PEAK_TIME_CAPITAL;
                    break;
                default:
                    peakTime = 0;
            }
        }
        stats.getPeakCRDuration().modifyPercent(id, peakTime);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getPeakCRDuration().unmodifyPercent(id);
    }
}

package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.overclock.ba_overclock;
import pigeonpun.bionicalteration.overclock.ba_overclockeffect;

import java.awt.*;

public class ba_woobly_psyche_effect extends ba_overclock {
    static Logger log = Global.getLogger(ba_woobly_psyche_effect.class);
    public static final float MAX_SPEED_THRESHOLD = 200;
    public static final float BONUS_SPEED = 50;
    public static final float MANEUVERABILITY_REDUCE = 80f;
    public ba_woobly_psyche_effect() {}
    //todo: Add a hullmod which display certain effect if they are active ot not base on the condition
    //in this case, its the max speed over certain threshold
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(stats.getEntity() != null) {
            if(stats.getMaxSpeed().modified > MAX_SPEED_THRESHOLD) {
                stats.getMaxSpeed().modifyFlat(id, BONUS_SPEED);
                stats.getAcceleration().modifyMult(id, MANEUVERABILITY_REDUCE * 0.01f);
                stats.getDeceleration().modifyMult(id, MANEUVERABILITY_REDUCE * 0.01f);
                stats.getTurnAcceleration().modifyMult(id, MANEUVERABILITY_REDUCE * 0.01f);
                stats.getMaxTurnRate().modifyMult(id, MANEUVERABILITY_REDUCE * 0.01f);
            }
        }
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getMaxSpeed().unmodifyFlat(id);
        stats.getAcceleration().unmodifyMult(id);
        stats.getDeceleration().unmodifyMult(id);
        stats.getTurnAcceleration().unmodifyMult(id);
        stats.getMaxTurnRate().unmodifyMult(id);
    }

    @Override
    public boolean isAdvanceInCombat() {
        return false;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }

    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean inBionicTable) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;

        if(!inBionicTable) {
            LabelAPI descriptions = tooltip.addPara("" +
                            "When ship's max speed exceed %s, %s flat bonus will be added to the ship speed but this will overwhelm the mind of the pilot, decrease the ship's maneuverability by %s.",
                    pad, t, "" + Math.round(MAX_SPEED_THRESHOLD) + "u", "" + Math.round(BONUS_SPEED) + "u", "" + Math.round(MANEUVERABILITY_REDUCE)+"%");
            descriptions.setHighlightColors(h,h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                    "When ship's max speed exceed %s, %s flat bonus will be added to the ship speed but this will overwhelm the mind of the pilot, decrease the ship's maneuverability by %s.",
                    pad, t, this.name, "[O]", "" + Math.round(MAX_SPEED_THRESHOLD) + "u", "" + Math.round(BONUS_SPEED) + "u", "" + Math.round(MANEUVERABILITY_REDUCE)+"%");
            overclockLabel.setHighlightColors(h,special,h,h,bad);
        }
    }
}

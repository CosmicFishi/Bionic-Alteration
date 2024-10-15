package pigeonpun.bionicalteration.bionic.impl.guardian;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.bionic.ba_bioniceffect;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_guardian_hand_effect extends ba_bionicitemplugin {
    public static float SHIP_SHIELD_RAISE = 1.18f;
    public static float SHIP_SHIELD_UPKEEP = 1.05f;
    public static float SHIP_MANEUVERABILITY = 0.95f;
    static Logger log = Global.getLogger(ba_guardian_hand_effect.class);



    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's shield unfolding speed by";
        String textNum = Math.round(SHIP_SHIELD_RAISE * 100 - 100) + "%";
        String negativeText = "but increase ship's shield upkeep by";
        String negativeTextNum = Math.round(SHIP_SHIELD_UPKEEP * 100 - 100) + "%";
        String negativeText2 = "and ship's maneuverability by";
        String negativeText2Num = Math.round(100 - SHIP_MANEUVERABILITY * 100) + "%";
        String name = isItem ? "Effect:" : bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s %s %s %s %s", pad, t, name, text, textNum, negativeText, negativeTextNum, negativeText2, negativeText2Num);
        descriptions.setHighlightColors(isItem ? g.brighter().brighter() : bionic.displayColor, t, h, t, bad, t, bad);
    }

    @Override
    public String getShortOnRemoveEffectDescription() {
        return null;
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getShieldUpkeepMult().modifyMult(id, SHIP_SHIELD_UPKEEP);
        stats.getShieldUnfoldRateMult().modifyMult(id, SHIP_SHIELD_RAISE);
        stats.getAcceleration().modifyMult(id, SHIP_MANEUVERABILITY);
        stats.getDeceleration().modifyMult(id, SHIP_MANEUVERABILITY);
        stats.getTurnAcceleration().modifyMult(id, SHIP_MANEUVERABILITY);
        stats.getMaxTurnRate().modifyMult(id, SHIP_MANEUVERABILITY);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getShieldUpkeepMult().unmodifyMult(id);
        stats.getShieldUnfoldRateMult().unmodifyMult(id);
        stats.getAcceleration().unmodifyMult(id);
        stats.getDeceleration().unmodifyMult(id);
        stats.getTurnAcceleration().unmodifyMult(id);
        stats.getMaxTurnRate().unmodifyMult(id);
    }

    @Override
    public void applyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level, ba_bionicitemplugin bionic) {

    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {

    }

    @Override
    public boolean isAdvanceInCombat() {
        return false;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }


    @Override
    public void onRemove(PersonAPI person, ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic) {

    }

    @Override
    public void onInstall(PersonAPI person, ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic) {

    }

    @Override
    public void renderExtraOnItem(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemPlugin.SpecialItemRendererAPI renderer) {

    }
}

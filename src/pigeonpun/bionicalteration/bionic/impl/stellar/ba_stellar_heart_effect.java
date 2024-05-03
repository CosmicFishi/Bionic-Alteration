package pigeonpun.bionicalteration.bionic.impl.stellar;

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

public class ba_stellar_heart_effect implements ba_bioniceffect {
    public static float SHIP_HULL = 1.1f;
    public static float SHIP_MANEUVERABILITY = 1.15f;
    public static float SHIP_ARMOR = 0.8f;
    static Logger log = Global.getLogger(ba_stellar_heart_effect.class);

    @Override
    public void setBionicItem(ba_bionicitemplugin bionic) {

    }

    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "For captain, increase piloting ship's hull by";
        String textNum = Math.round(SHIP_HULL * 100 - 100) + "%";
        String text2 = "and ship's maneuverability by";
        String text2Num = Math.round(SHIP_MANEUVERABILITY * 100 - 100) + "%";
        String negativeText = "but decrease ship's armor by";
        String negativeTextNum = Math.round(100 - SHIP_ARMOR * 100) + "%";
        String name = isItem ? "Effect:" : bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s %s %s %s %s", pad, t, name, text, textNum, text2, text2Num, negativeText, negativeTextNum);
        descriptions.setHighlightColors(isItem ? g.brighter().brighter() : bionic.displayColor, t, h, t, h, t, bad);
    }

    @Override
    public String getShortOnRemoveEffectDescription() {
        return null;
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getAcceleration().modifyMult(id + "ba_stellar_heart_effect", SHIP_MANEUVERABILITY * 2f);
        stats.getDeceleration().modifyMult(id + "ba_stellar_heart_effect", SHIP_MANEUVERABILITY);
        stats.getTurnAcceleration().modifyMult(id + "ba_stellar_heart_effect", SHIP_MANEUVERABILITY * 2f);
        stats.getMaxTurnRate().modifyMult(id + "ba_stellar_heart_effect", SHIP_MANEUVERABILITY);
        stats.getArmorBonus().modifyMult(id + "ba_stellar_heart_effect", SHIP_ARMOR);
        stats.getHullBonus().modifyMult(id + "ba_stellar_heart_effect", SHIP_HULL);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getAcceleration().unmodifyMult(id + "ba_stellar_heart_effect");
        stats.getDeceleration().unmodifyMult(id + "ba_stellar_heart_effect");
        stats.getTurnAcceleration().unmodifyMult(id + "ba_stellar_heart_effect");
        stats.getMaxTurnRate().unmodifyMult(id + "ba_stellar_heart_effect");
        stats.getArmorBonus().unmodifyMult(id + "ba_stellar_heart_effect");
        stats.getHullBonus().unmodifyMult(id + "ba_stellar_heart_effect");
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

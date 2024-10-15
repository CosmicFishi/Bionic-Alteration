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

public class ba_guardian_torso_effect extends ba_bionicitemplugin {
    public static float SHIP_SHIELD_EFF = 0.9f;
    public static float SHIP_HULL = 1.1f;
    public static float SHIP_MAX_SPEED = 0.85f;
    static Logger log = Global.getLogger(ba_guardian_torso_effect.class);



    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase piloting ship's shield efficiency by";
        String textNum = Math.round(100 - SHIP_SHIELD_EFF * 100) + "%";
        String text2 = "and increase ship hull by";
        String text2Num = Math.round(SHIP_HULL * 100 - 100) + "%";
        String negativeText = "but decreasing ship's max speed by";
        String negativeTextNum = Math.round(100 - SHIP_MAX_SPEED * 100) + "%";
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
        stats.getShieldAbsorptionMult().modifyMult(id, SHIP_SHIELD_EFF);
        stats.getHullBonus().modifyMult(id, SHIP_HULL);
        stats.getMaxSpeed().modifyMult(id, SHIP_MAX_SPEED);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getShieldAbsorptionMult().unmodifyMult(id);
        stats.getHullBonus().unmodifyMult(id);
        stats.getMaxSpeed().unmodifyMult(id);
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

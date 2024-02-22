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

public class ba_guardian_leg_effect implements ba_bioniceffect {
    public static float SHIP_CR_DECREASE_MULT = 0.86f;
    public static float SHIP_ENGINE_DAMAGE_TAKEN_MULT = 0.95f;
    public static float SHIP_HULL_DAMAGE_TAKEN_MULT = 1.15f;
    Logger log = Global.getLogger(ba_guardian_leg_effect.class);

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

        String text = "Reduce piloting ship's combat readiness degrade speed by";
        String textNum = Math.round(100 - SHIP_CR_DECREASE_MULT * 100) + "%";
        String text2 = "and reduce ship's engine damage taken by";
        String text2Num = Math.round(100 - SHIP_ENGINE_DAMAGE_TAKEN_MULT * 100) + "%";
        String negativeText = "but increase ship's hull damage taken by";
        String negativeTextNum = Math.round(SHIP_HULL_DAMAGE_TAKEN_MULT * 100 - 100) + "%";
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
        stats.getCRLossPerSecondPercent().modifyMult(id, SHIP_CR_DECREASE_MULT);
        stats.getHullDamageTakenMult().modifyMult(id, SHIP_HULL_DAMAGE_TAKEN_MULT);
        stats.getEngineDamageTakenMult().modifyMult(id, SHIP_ENGINE_DAMAGE_TAKEN_MULT);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getCRLossPerSecondPercent().unmodifyMult(id);
        stats.getHullDamageTakenMult().unmodifyMult(id);
        stats.getEngineDamageTakenMult().unmodifyMult(id);
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

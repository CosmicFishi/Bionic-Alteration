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

public class ba_stellar_torso_effect implements ba_bioniceffect {
    public static float SHIP_PEAK_CR = 1.15f;
    public static float SHIP_ENGINE_DAMAGE = 0.9f;
    public static float SHIP_MIN_CREW_REQUIRE = 1.25f;
    Logger log = Global.getLogger(ba_stellar_torso_effect.class);

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

        String text = "Increase piloting ship's max peak CR by";
        String textNum = Math.round(SHIP_PEAK_CR * 100 - 100) + "%";
        String text2 = "and reduce damage taken by the ship engine by";
        String text2Num = Math.round(100 - SHIP_ENGINE_DAMAGE * 100) + "%";
        String negativeText = "but increase ship's min crew requirement by";
        String negativeTextNum = Math.round(SHIP_MIN_CREW_REQUIRE * 100 - 100) + "%";
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
        stats.getPeakCRDuration().modifyMult(id, SHIP_PEAK_CR);
        stats.getEngineDamageTakenMult().modifyMult(id, SHIP_ENGINE_DAMAGE);
        stats.getMinCrewMod().modifyMult(id, SHIP_MIN_CREW_REQUIRE);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getPeakCRDuration().unmodifyMult(id);
        stats.getEngineDamageTakenMult().unmodifyMult(id);
        stats.getMinCrewMod().unmodifyMult(id);
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

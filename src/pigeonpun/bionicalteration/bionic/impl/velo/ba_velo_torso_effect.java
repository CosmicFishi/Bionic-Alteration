package pigeonpun.bionicalteration.bionic.impl.velo;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.bionic.ba_bioniceffect;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_velo_torso_effect implements ba_bioniceffect {
    public static float OFFICER_SHIP_PEAK_CR = 1.25f;
    public static float OFFICER_SHIP_OVERLOAD_MULT = 1.2f;
    public static float ADMIN_DEFEND_MULT = 1.1f;
    public static float ADMIN_UPKEEP_MULT = 1.1f;
    static Logger log = Global.getLogger(ba_velo_torso_effect.class);

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

        String text = "For captain, increase piloting ship's peak CR by";
        String textNum = Math.round(OFFICER_SHIP_PEAK_CR * 100 - 100) + "%";
        String negativeText = "but increase ship's overload time by";
        String negativeTextNum = Math.round(OFFICER_SHIP_OVERLOAD_MULT * 100 - 100) + "%";
        String textAdmin = "For admin, increase market defend by";
        String textAdminNum = Math.round(ADMIN_DEFEND_MULT * 100 - 100) + "%";
        String negativeTextAdmin = "but also increase upkeep cost by";
        String negativeTextAdminNum = Math.round(ADMIN_UPKEEP_MULT * 100 - 100) + "%";
        String name = isItem ? "Effect:" : bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s %s %s. %s %s %s %s", pad, t, name, text, textNum, negativeText, negativeTextNum, textAdmin, textAdminNum, negativeTextAdmin, negativeTextAdminNum);
        descriptions.setHighlightColors(isItem ? g.brighter().brighter() : bionic.displayColor, t, h, t, bad, t, h, t, bad);
    }

    @Override
    public String getShortOnRemoveEffectDescription() {
        return null;
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getPeakCRDuration().modifyMult(id + "ba_velo_torso_effect", OFFICER_SHIP_PEAK_CR);
        stats.getOverloadTimeMod().modifyMult(id + "ba_velo_torso_effect", OFFICER_SHIP_OVERLOAD_MULT);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getPeakCRDuration().unmodifyMult(id + "ba_velo_torso_effect");
        stats.getOverloadTimeMod().unmodifyMult(id + "ba_velo_torso_effect");
    }

    @Override
    public void applyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level, ba_bionicitemplugin bionic) {
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id + "ba_velo_torso_effect", ADMIN_DEFEND_MULT, bionic.getName() + "(Admins bionic)");
        market.getUpkeepMult().modifyMult(id + "ba_velo_torso_effect", ADMIN_UPKEEP_MULT, bionic.getName() + "(Admins bionic)");
    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id + "ba_velo_torso_effect");
        market.getUpkeepMult().unmodifyMult(id + "ba_velo_torso_effect");
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

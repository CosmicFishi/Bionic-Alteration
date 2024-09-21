package pigeonpun.bionicalteration.bionic.impl.harmony;

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
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;

import java.awt.*;

public class ba_harmony_brain_effect extends ba_bionicitemplugin {
    public static float MARKET_DEFEND_MULT = 1.05f;
    public static float MARKET_FLEET_SIZE_FLAT = 0.1f;
    static Logger log = Global.getLogger(ba_harmony_brain_effect.class);



    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String text = "Increase market fleet size by";
        String textNum = Math.round(MARKET_FLEET_SIZE_FLAT * 100) + "%";
        String text2 = "and increase defend by";
        String text2Num = Math.round(MARKET_DEFEND_MULT * 100 - 100) + "%";
        String name = isItem ? "Effect:" : bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s %s %s", pad, t, name, text, textNum, text2, text2Num);
        descriptions.setHighlightColors(isItem ? g.brighter().brighter() : bionic.displayColor, t, h, t, h);
    }

    @Override
    public String getShortOnRemoveEffectDescription() {
        return null;
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

    }

    @Override
    public void applyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level, ba_bionicitemplugin bionic) {
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, (MARKET_DEFEND_MULT), bionic.getName() + " (Admins bionic)");
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, MARKET_FLEET_SIZE_FLAT, bionic.getName() + " (Admins bionic)");
    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(id);
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

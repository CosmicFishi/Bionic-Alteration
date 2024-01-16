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

public class ba_velo_mouth_effect implements ba_bioniceffect {
    public static float OFFICER_WEAPON_DAMAGE = 1.2f;
    public static float OFFICER_SHIP_DP = 4f;
    Logger log = Global.getLogger(ba_velo_mouth_effect.class);

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

        String text = "Increase piloting ship's energy, ballistic and missile damage by " + Math.round(OFFICER_WEAPON_DAMAGE * 100 - 100) + "%";
        String negativeText = "but increase ship's DP by " + Math.round(OFFICER_SHIP_DP) + "";
        String name = isItem? "Effect:": bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s", pad, t, name, text, negativeText);
        descriptions.setHighlight(name, text, negativeText);
        descriptions.setHighlightColors(isItem? g.brighter().brighter() : bionic.displayColor, t, bad);
    }

    @Override
    public String getShortOnRemoveEffectDescription() {
        return null;
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getEnergyWeaponDamageMult().modifyMult(id, OFFICER_WEAPON_DAMAGE);
        stats.getBallisticWeaponDamageMult().modifyMult(id, OFFICER_WEAPON_DAMAGE);
        stats.getMissileWeaponDamageMult().modifyMult(id, OFFICER_WEAPON_DAMAGE);
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, OFFICER_SHIP_DP);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getEnergyWeaponDamageMult().unmodifyMult(id);
        stats.getBallisticWeaponDamageMult().unmodifyMult(id);
        stats.getMissileWeaponDamageMult().unmodifyMult(id);
        stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).unmodifyFlat(id);
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
    public boolean isAdvanceInCampaign() {
        return false;
    }

    @Override
    public void advanceInCampaign() {

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

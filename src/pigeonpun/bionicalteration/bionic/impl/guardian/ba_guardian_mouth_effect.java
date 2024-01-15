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

public class ba_guardian_mouth_effect implements ba_bioniceffect {
    public static float OFFICER_TURRET_SPIN = 0.9f;
    public static float OFFICER_TURRET_DAMAGE = 0.85f;
    public static float ADMIN_STABILITY_FLAT = 1f;
    public static float ADMIN_ACCESS_FLAT = 10f;
    Logger log = Global.getLogger(ba_guardian_mouth_effect.class);

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

        String text = "For captain, reduce piloting ship turret damage taken by " + Math.round(100 - OFFICER_TURRET_DAMAGE * 100) + "%";
        String negativeText = "but reduce ship's turret turn rate by" + Math.round(100 - OFFICER_TURRET_SPIN * 100) + "%";
        String textAdmin = "For admin, increase market stability by " + Math.round(ADMIN_STABILITY_FLAT) + "";
        String negativeTextAdmin = "but also reduce accessibility by" + Math.round(ADMIN_ACCESS_FLAT) + "%";
        String name = isItem? "Effect:": bionic.getName() + ":";
        LabelAPI descriptions = tooltip.addPara("%s %s %s. %s %s", pad, t, name, text, negativeText, textAdmin, negativeTextAdmin);
        descriptions.setHighlight(name, text, negativeText, textAdmin, negativeTextAdmin);
        descriptions.setHighlightColors(isItem? g.brighter().brighter() : bionic.displayColor, t, bad, t, bad);
    }

    @Override
    public String getShortOnRemoveEffectDescription() {
        return null;
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getWeaponTurnRateBonus().modifyMult(id, OFFICER_TURRET_SPIN);
        stats.getBeamWeaponTurnRateBonus().modifyMult(id, OFFICER_TURRET_SPIN);
        stats.getWeaponDamageTakenMult().modifyMult(id, OFFICER_TURRET_DAMAGE);
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getWeaponTurnRateBonus().unmodifyMult(id);
        stats.getBeamWeaponTurnRateBonus().unmodifyMult(id);
        stats.getWeaponDamageTakenMult().unmodifyMult(id);
    }

    @Override
    public void applyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyAdminEffect(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level, ba_bionicitemplugin bionic) {
        market.getStability().modifyFlat(id, ADMIN_STABILITY_FLAT, bionic.getName() + "(Admins bionic)");
        market.getAccessibilityMod().modifyFlat(id, -ADMIN_ACCESS_FLAT, bionic.getName() + "(Admins bionic)");
    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {
        market.getStability().unmodifyFlat(id);
        market.getAccessibilityMod().unmodifyFlat(id);
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

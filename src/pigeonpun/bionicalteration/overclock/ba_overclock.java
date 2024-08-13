package pigeonpun.bionicalteration.overclock;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.awt.*;

public class ba_overclock implements ba_overclockeffect{
    public String id;
    public String name;
    public boolean isApplyCaptainEffect;
    public boolean isApplyAdminEffect;
    public boolean isAdvanceInCombat;
    public boolean isAdvanceInCampaign;
    public int upgradeCost;
    //todo: add feature for this V
    public float prebuiltChance; //the chance of which the overclock is already applied when the bionic spawned in
    public float order;
    public ba_overclock() {};
    public void setOverclock(String id, String name, boolean isApplyCaptainEffect, boolean isApplyAdminEffect, boolean isAdvanceInCombat,
                        boolean isAdvanceInCampaign, int upgradeCost, float prebuiltChance, float order) {
        this.id = id;
        this.name = name;
        this.isApplyAdminEffect = isApplyAdminEffect;
        this.isApplyCaptainEffect = isApplyCaptainEffect;
        this.isAdvanceInCombat = isAdvanceInCombat;
        this.isAdvanceInCampaign = isAdvanceInCampaign;
        this.upgradeCost = upgradeCost;
        this.prebuiltChance = prebuiltChance;
        this.order = order;
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
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean inBionicTable) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;

        LabelAPI descriptions = tooltip.addPara("Nothing here yet.....", pad, t);
    }
}

package pigeonpun.bionicalteration.overclock;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

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
    public ba_overclock() {};
    public void setOverclock(String id, String name, boolean isApplyCaptainEffect, boolean isApplyAdminEffect, boolean isAdvanceInCombat,
                        boolean isAdvanceInCampaign, int upgradeCost, float prebuiltChance) {
        this.id = id;
        this.name = name;
        this.isApplyAdminEffect = isApplyAdminEffect;
        this.isApplyCaptainEffect = isApplyCaptainEffect;
        this.isAdvanceInCombat = isAdvanceInCombat;
        this.isAdvanceInCampaign = isAdvanceInCampaign;
        this.upgradeCost = upgradeCost;
        this.prebuiltChance = prebuiltChance;
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
}

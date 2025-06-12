package pigeonpun.bionicalteration.hullmod;

import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class ba_corruptedSyntheticBody extends BaseHullMod {
    public static final float tooltipWitdth = 600f;
    @Override
    public float getTooltipWidth() {
        return tooltipWitdth;
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

    }
    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return super.getUnapplicableReason(ship);
    }

    @Override
    public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
        return super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
    }

    @Override
    public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CampaignUIAPI.CoreUITradeMode mode) {
        return super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
    }

    @Override
    public CargoStackAPI getRequiredItem() {
        return super.getRequiredItem();
    }

    @Override
    public void addRequiredItemSection(TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI currentVariant, MarketAPI dockedAt, float width, boolean isForModSpec) {
        super.addRequiredItemSection(tooltip, member, currentVariant, dockedAt, width, isForModSpec);
    }
}

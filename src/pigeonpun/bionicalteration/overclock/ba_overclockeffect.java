package pigeonpun.bionicalteration.overclock;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

public interface ba_overclockeffect {
    /**
     * NOTE: This is AFTER ship creation. Use applyOfficerEffectBeforeShipCreation for before ship creation
     * @param stats
     * @param hullSize
     * @param id
     */
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id);

    /**
     * As the name implies, unapply effects for officer <br>
     * @param stats
     * @param hullSize
     * @param id
     */
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id);
    public void applyOfficerEffectBeforeShipCreation(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id);


    /**
     * As the name implies, apply effects for admin
     * @param stats
     * @param id
     */
    public void applyAdminEffect(MutableCharacterStatsAPI stats, String id);
    /**
     * As the name implies, unapply effects for admin
     * @param stats
     * @param id
     */
    public void unapplyAdminEffect(MutableCharacterStatsAPI stats, String id);
    /**
     * As the name implies, apply effects for market
     * @param market
     * @param id
     * @param level
     */
    public void applyEffectAdminMarket(MarketAPI market, String id, float level, ba_bionicitemplugin bionic);
    /**
     * As the name implies, unapply effects for market
     * @param market
     * @param id
     */
    public void unapplyEffectAdminMarket(MarketAPI market, String id);
    public boolean isAdvanceInCombat();
    /**
     * isAdvanceInCombat() needed to be set to true
     * Note: This also run in refit :D. Magic I know
     */
    public void advanceInCombat(ShipAPI ship, float amount);
}

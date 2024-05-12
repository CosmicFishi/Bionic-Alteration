package pigeonpun.bionicalteration.bionic;

import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import pigeonpun.bionicalteration.ba_limbmanager;

import java.util.List;

/**
 * @author PigeonPun
 */
public interface ba_bioniceffect{
    /**
     * Return bionic item if needed
     * @param bionic
     */
    public void setBionicItem(ba_bionicitemplugin bionic);
    /**
     * Use to display the effect of bionic on the bionic item and on bionics effects description in workshop.
     * @return text
     */
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean isItem);
    /**
     * Use to display the effect of bionic on remove, will be display in bionic description inside bionic workshop inventory.
     * @return text
     */
    public String getShortOnRemoveEffectDescription();

    /**
     * As the name implies, apply effects for officer
     * @param stats
     * @param hullSize
     * @param id
     */
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id);

    /**
     * As the name implies, unapply effects for officer
     * @param stats
     * @param hullSize
     * @param id
     */
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id);

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

    /**
     * IMPORTANT: Make sure that isEffectAppliedAfterRemove is TRUE for this method to be called.
     * @param person
     * @param limb
     * @param bionic
     */
    public void onRemove(PersonAPI person, ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic);
    public void onInstall(PersonAPI person, ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic);

    /**
     * Render extra indicators on top right of the item (Visual cue for the player)
     * Can leave empty if not needed
     */
    public void renderExtraOnItem(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemPlugin.SpecialItemRendererAPI renderer);
}

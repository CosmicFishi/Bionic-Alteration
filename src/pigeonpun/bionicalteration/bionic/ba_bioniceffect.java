package pigeonpun.bionicalteration.bionic;

import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import pigeonpun.bionicalteration.ba_limbmanager;

import java.util.List;

/**
 * @author PigeonPun
 */
public interface ba_bioniceffect{
    /**
     * Use to display the effect of bionic on the person skill panel and on bionics description.
     * @return text
     */
    public String getShortEffectDescription();
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
     * isAdvanceInCombat in bionic_data.csv needed to be set to true
     * Note: This also run in refit :D. Magic I know
     */
    public void advanceInCombat(ShipAPI ship, float amount);

    /**
     * isAdvanceInCampaign in bionic_data.csv needed to be set to true
     */
    //todo: look into this
    public void advanceInCampaign();
    //todo: look into onRemove and onInstall
    public void onRemove(PersonAPI person, ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic);
    public void onInstall(PersonAPI person, ba_limbmanager.ba_limb limb, ba_bionicitemplugin bionic);

    /**
     * Render extra indicators on top right of the item (Visual cue for the player)
     * Can leave empty if not needed
     */
    public void renderExtraOnItem(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemPlugin.SpecialItemRendererAPI renderer);
}

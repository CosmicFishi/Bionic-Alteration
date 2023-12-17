package pigeonpun.bionicalteration.bionic;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

/**
 * @author PigeonPun
 */
public interface ba_bioniceffect {
    /**
     * Use to display the effect of bionic on the person skill panel and on bionics description.
     * @return text
     */
    public String getShortEffectDescription();

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
    //todo: focus on this
    public void advanceInCombat(ShipAPI ship, float amount);

    /**
     * isAdvanceInCampaign in bionic_data.csv needed to be set to true
     */
    //todo: look into this
    public void advanceInCampaign();
    public void onRemove();
    public void onInstall();
}

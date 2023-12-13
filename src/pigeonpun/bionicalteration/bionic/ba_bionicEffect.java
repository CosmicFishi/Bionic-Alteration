package pigeonpun.bionicalteration.bionic;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

/**
 * @author PigeonPun
 */
public interface ba_bionicEffect {
    /**
     * Use to display the effect of bionic on the person skill panel.
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
}

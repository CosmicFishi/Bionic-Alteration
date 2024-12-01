package pigeonpun.bionicalteration.conscious;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.awt.*;

public interface ba_conscious {
    public Color getColor();
    public float getThreshold();
    public float getDisplayOrder();
    public String getDisplayName();
    public void displayTooltipDescription(TooltipMakerAPI tooltip, PersonAPI person, boolean isActive, boolean isSimpleMode);
    public void applyEffectOfficer(MutableShipStatsAPI stats, String id);
    public void unapplyEffectOfficer(MutableShipStatsAPI stats, String id);
    public void applyEffectAdmin(MutableCharacterStatsAPI stats, String id);
    public void unapplyEffectAdmin(MutableCharacterStatsAPI stats, String id);
    public void applyEffectAdminMarket(MarketAPI market, String id, float level);
    public void unapplyEffectAdminMarket(MarketAPI market, String id);
    public void advanceInCombat(ShipAPI ship, float amount);
    public float getConsciousTreatmentFee();
}

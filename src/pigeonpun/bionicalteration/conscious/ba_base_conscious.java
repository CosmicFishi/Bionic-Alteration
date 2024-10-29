package pigeonpun.bionicalteration.conscious;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import pigeonpun.bionicalteration.conscious.ba_conscious;

import java.awt.*;

public class ba_base_conscious implements ba_conscious {
    @Override
    public Color getColor() {
        return Color.red;
    }

    @Override
    public float getThreshold() {
        return 0;
    }

    @Override
    public float getDisplayOrder() {
        return 0;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public void displayTooltipDescription(TooltipMakerAPI tooltip, PersonAPI person, boolean isActive, boolean isSimpleMode) {

    }

    @Override
    public void applyEffectOfficer(MutableShipStatsAPI stats, String id) {

    }

    @Override
    public void unapplyEffectOfficer(MutableShipStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectAdmin(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void unapplyEffectAdmin(MutableCharacterStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectAdminMarket(MarketAPI market, String id, float level) {

    }

    @Override
    public void unapplyEffectAdminMarket(MarketAPI market, String id) {

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }
    @Override
    public float getConsciousTreatmentFee() {
        return 0f;
    }
}

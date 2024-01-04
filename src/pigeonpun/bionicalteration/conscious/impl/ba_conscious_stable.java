package pigeonpun.bionicalteration.conscious.impl;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.conscious.ba_conscious;

import java.awt.*;

public class ba_conscious_stable implements ba_conscious {
    @Override
    public Color getColor() {
        return ba_variablemanager.BA_CONSCIOUSNESS_COLOR.get(ba_variablemanager.BA_CONSCIOUSNESS_STABLE_THRESHOLD);
    }

    @Override
    public float getThreshold() {
        return ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_STABLE_THRESHOLD);
    }

    @Override
    public String getDisplayName() {
        return "Stable";
    }

    @Override
    public void displayTooltipDescription(TooltipMakerAPI tooltip, PersonAPI person, boolean isActive, boolean isSimpleMode) {
        if(!isSimpleMode) {
            //todo: do things appear in not simple (on condition hover)
        }
        //todo: do things on simple
    }

    @Override
    public void applyEffectOfficer(MutableShipStatsAPI shipStats, String id) {

    }

    @Override
    public void unapplyEffectOfficer(String id) {

    }

    @Override
    public void applyEffectAdmin(MutableCharacterStatsAPI stats, MarketAPI market, String id) {

    }

    @Override
    public void unapplyEffectAdmin(String id) {

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }

    @Override
    public void advanceInCampaign() {

    }
}

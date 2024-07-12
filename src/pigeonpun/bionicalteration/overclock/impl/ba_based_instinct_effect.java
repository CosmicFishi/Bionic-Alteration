package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.overclock.ba_overclock;

public class ba_based_instinct_effect extends ba_overclock {
    static Logger log = Global.getLogger(ba_based_instinct_effect.class);
    public ba_based_instinct_effect() {}

    //todo: do effects
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

    }

    @Override
    public boolean isAdvanceInCombat() {
        return false;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }
}

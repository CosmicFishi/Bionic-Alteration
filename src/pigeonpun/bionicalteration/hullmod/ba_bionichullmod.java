package pigeonpun.bionicalteration.hullmod;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class ba_bionichullmod extends BaseHullMod {
    //todo: Add a hullmod that display the effects of each bionic
    //todo: Add apply effect before/after ship creation to the bionic
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize,
                                               MutableShipStatsAPI stats, String id) {
    }
}

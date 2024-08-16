package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import pigeonpun.bionicalteration.overclock.ba_overclock;

public class ba_juice_fusing_effect extends ba_overclock {
    static Logger log = Global.getLogger(ba_juice_fusing_effect.class);
    public ba_juice_fusing_effect() {}

    //todo: do effects
    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

    }

    @Override
    public boolean isAdvanceInCombat() {
        return true;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }

    private class juiceFusingDamageDeaListener implements DamageDealtModifier {

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if(damage.isMissile()) {

            }
            return null;
        }
    }
}

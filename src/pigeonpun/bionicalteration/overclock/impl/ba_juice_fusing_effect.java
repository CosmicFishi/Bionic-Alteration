package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.impl.domain.ba_brain_neura_matrix_effect;
import pigeonpun.bionicalteration.overclock.ba_overclock;

import java.awt.*;
import java.util.Random;

public class ba_juice_fusing_effect extends ba_overclock {
    static Logger log = Global.getLogger(ba_juice_fusing_effect.class);
    public static final int RELOAD_SALVO = 1;
    public static final float RELOAD_CHANCE = 0.3f; //in percentage
    public static final float MAX_RELOAD_CD = 10f; //in seconds
    static final String dataKey = "ba_juice_reload_data";
    public ba_juice_fusing_effect() {}

    //todo: do effects
    @Override
    public void displayEffectDescription(TooltipMakerAPI tooltip, PersonAPI person, ba_bionicitemplugin bionic, boolean inBionicTable) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;
        float speedBonus = ba_brain_neura_matrix_effect.MAX_SPEED_MULT;
        if(!inBionicTable) {
            LabelAPI descriptions = tooltip.addPara("" +
                            "Whenever the ship system is %s, every %s seconds, there is a %s chance to refill all %s weapons on ship with %s of the weapon burst salvo.",
                    pad, t, "active", "" + Math.round(MAX_RELOAD_CD) ,"" + Math.round(RELOAD_CHANCE * 100) + "%", "missile", "" + RELOAD_SALVO);
            descriptions.setHighlightColors(h,special,h,h,h);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "Whenever the ship system is %s, every %s seconds, there is a %s chance to refill all %s weapons on ship with %s of the weapon burst salvo.",
                    pad, t, this.name, "[O]", "active", "" + Math.round(MAX_RELOAD_CD) ,"" + Math.round(RELOAD_CHANCE * 100) + "%", "missile", "" + RELOAD_SALVO);
            overclockLabel.setHighlightColors(h,special,h,special,h,h,h);
        }
    }
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
        if(Global.getCombatEngine().isPaused()) return;
        juiceData data = new juiceData();
        if(ship.getCustomData().get(dataKey) instanceof juiceData) {
            data = (juiceData) ship.getCustomData().get(dataKey);
        }
        if(ship.getSystem() != null) {
            if(ship.getSystem().isActive() && data.state == juiceState.INACTIVE) {
                data.state = juiceState.RANDOM_CHECKING;
            }
        }
        if(data.state == juiceState.RANDOM_CHECKING) {
            Random rand = new Random();
            boolean willReload = rand.nextDouble() < RELOAD_CHANCE;
            if(willReload) {
                for(WeaponAPI weapon: ship.getAllWeapons()) {
                    if(weapon.getType().equals(WeaponAPI.WeaponType.MISSILE) & weapon.getAmmo() < weapon.getMaxAmmo()) {
                        weapon.ensureClonedSpec();
                        weapon.setAmmo(weapon.getAmmo() + weapon.getSpec().getBurstSize() * RELOAD_SALVO);
                        if (weapon.getAmmo() > weapon.getMaxAmmo()) {
                            weapon.setAmmo(weapon.getMaxAmmo());
                        }
                    }
                }
                Global.getSoundPlayer().playSound("juice_fusing", 1,1, ship.getLocation(), ship.getVelocity());
            }
            data.state = juiceState.POST_CHECKING;
        }
        if(data.state == juiceState.POST_CHECKING) {
            data.currentCD += amount;
            if(ship == Global.getCombatEngine().getPlayerShip()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(dataKey, "", "Juice Fusing", "CD: " + Math.round(MAX_RELOAD_CD - data.currentCD) + "s", true);
            }
            if(data.currentCD > MAX_RELOAD_CD) {
                data.state = juiceState.INACTIVE;
                data.currentCD = 0;
            }
        }

        ship.setCustomData(dataKey, data);
    }
    public enum juiceState {
        POST_CHECKING, INACTIVE, RANDOM_CHECKING
    }
    private class juiceData {
        public juiceState state = juiceState.INACTIVE;
        public float currentCD;
        public juiceData() {
            currentCD = 0;
        }
    }
}

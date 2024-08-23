package pigeonpun.bionicalteration.overclock.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.impl.domain.ba_brain_neura_matrix_effect;
import pigeonpun.bionicalteration.overclock.ba_overclock;

import java.awt.*;

public class ba_urgent_filling_effect extends ba_overclock {
    static Logger log = Global.getLogger(ba_urgent_filling_effect.class);
    public static final float AMMO_REGEN_BONUS = 1.4f;
    public static final float ARMOR_REDUCE_MULT = 0.8f;
    public ba_urgent_filling_effect() {}

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
                            "When all turret mounts on the ship are all %s or all %s, increase weapon's ammo regeneration by %s but reduce the ship's armor by %s.",
                    pad, t, "ballistic", "energy", "" + Math.round((AMMO_REGEN_BONUS * 100) - 100) + "%", "" + Math.round((1-ARMOR_REDUCE_MULT) * 100) + "%");
            descriptions.setHighlightColors(Misc.getBallisticMountColor(),Misc.getEnergyMountColor(),h,bad);
        } else {
            LabelAPI overclockLabel = tooltip.addPara("%s %s: " +
                            "When all turret mounts on the ship are all %s or all %s, increase weapon's ammo regeneration by %s but reduce the ship's armor by %s.",
                    pad, t, this.name, "[O]", "ballistic", "energy", "" + Math.round((AMMO_REGEN_BONUS * 100) - 100) + "%", "" + Math.round((1-ARMOR_REDUCE_MULT) * 100) + "%");
            overclockLabel.setHighlightColors(h,special,Misc.getBallisticMountColor(),Misc.getEnergyMountColor(),h,bad);
        }
    }

    @Override
    public void applyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        if(stats.getEntity() != null) {
            boolean isAllBallistic = true;
            ShipAPI ship = (ShipAPI) stats.getEntity();
            for(WeaponSlotAPI slot: ship.getHullSpec().getAllWeaponSlotsCopy()) {
                if(!slot.isHardpoint()) {
                    if(!slot.getWeaponType().equals(WeaponAPI.WeaponType.BALLISTIC)) {
                        isAllBallistic = false;
                        break;
                    }
                }
            }
            boolean isALlEnergy = true;
            for(WeaponSlotAPI slot: ship.getHullSpec().getAllWeaponSlotsCopy()) {
                if(!slot.isHardpoint()) {
                    if(!slot.getWeaponType().equals(WeaponAPI.WeaponType.ENERGY)) {
                        isALlEnergy = false;
                        break;
                    }
                }
            }
            if(isAllBallistic || isALlEnergy) {
                stats.getBallisticAmmoRegenMult().modifyMult(id, AMMO_REGEN_BONUS);
                stats.getEnergyAmmoRegenMult().modifyMult(id, AMMO_REGEN_BONUS);
                stats.getMissileAmmoRegenMult().modifyMult(id, AMMO_REGEN_BONUS);
                stats.getArmorBonus().modifyMult(id, ARMOR_REDUCE_MULT);
            }
        }
    }

    @Override
    public void unapplyOfficerEffect(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        stats.getBallisticAmmoRegenMult().unmodifyMult(id);
        stats.getEnergyAmmoRegenMult().unmodifyMult(id);
        stats.getMissileAmmoRegenMult().unmodifyMult(id);
        stats.getArmorBonus().unmodifyMult(id);
    }

    @Override
    public boolean isAdvanceInCombat() {
        return false;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

    }
}

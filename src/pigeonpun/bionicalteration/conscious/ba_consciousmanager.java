package pigeonpun.bionicalteration.conscious;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.conscious.impl.*;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;
import pigeonpun.bionicalteration.utils.ba_stringhelper;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ba_consciousmanager {
    //todo: tile consciousness to the colony major event ?
    static Logger log = Global.getLogger(ba_consciousmanager.class);
    public static HashMap<ba_variablemanager.ba_consciousnessLevel, ba_conscious> consciousMap = new HashMap<>();
    public static void onApplicationLoad() {
        consciousMap.put(ba_variablemanager.ba_consciousnessLevel.STABLE, new ba_conscious_stable());
        consciousMap.put(ba_variablemanager.ba_consciousnessLevel.UNSTEADY, new ba_conscious_unsteady());
        consciousMap.put(ba_variablemanager.ba_consciousnessLevel.WEAKEN, new ba_conscious_weaken());
        consciousMap.put(ba_variablemanager.ba_consciousnessLevel.FRAGILE, new ba_conscious_fragile());
        consciousMap.put(ba_variablemanager.ba_consciousnessLevel.CRITICAL, new ba_conscious_critical());
    }
    public static ba_conscious getConsciousnessLevel(PersonAPI person) {
        //In case of player assigning to a AI ship and switched back to AI core as captain.
        if(person.isAICore()) {
            return consciousMap.get(ba_variablemanager.ba_consciousnessLevel.STABLE);
        }
        float conscious = person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
        return getConsciousnessLevel(conscious);
    }
    /**
     * return conscious level
     * @param consciousnessLevel 0-1
     * @return
     */
    public static ba_conscious getConsciousnessLevel(float consciousnessLevel) {
        ba_conscious defaultLevel = consciousMap.get(ba_variablemanager.ba_consciousnessLevel.STABLE);
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_STABLE_THRESHOLD)) {
            defaultLevel = consciousMap.get(ba_variablemanager.ba_consciousnessLevel.STABLE);
        }
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_UNSTEADY_THRESHOLD)) {
            defaultLevel = consciousMap.get(ba_variablemanager.ba_consciousnessLevel.UNSTEADY);
        }
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_WEAKENED_THRESHOLD)) {
            defaultLevel = consciousMap.get(ba_variablemanager.ba_consciousnessLevel.WEAKEN);
        }
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_FRAGILE_THRESHOLD)) {
            defaultLevel = consciousMap.get(ba_variablemanager.ba_consciousnessLevel.FRAGILE);
        }
        if (consciousnessLevel < ba_variablemanager.BA_CONSCIOUSNESS_THRESHOLD.get(ba_variablemanager.BA_CONSCIOUSNESS_CRITICAL_THRESHOLD)) {
            defaultLevel = consciousMap.get(ba_variablemanager.ba_consciousnessLevel.CRITICAL);
        }

        return defaultLevel;
    }
    public static void resetBeforeApplyEffectOfficer(MutableShipStatsAPI stats, String id) {
        for(ba_conscious conscious: consciousMap.values() ){
            conscious.unapplyEffectOfficer(stats, id);
        }
    }
    public static void resetBeforeApplyEffectAdmin(MutableCharacterStatsAPI stats, String id) {
        for(ba_conscious conscious: consciousMap.values() ){
            conscious.unapplyEffectAdmin(stats, id);
        }
    }
    public static void resetBeforeApplyEffectAdminMarket(MarketAPI market, String id) {
        for(ba_conscious conscious: consciousMap.values() ){
            conscious.unapplyEffectAdminMarket(market, id);
        }
    }
    /**
     * Return consciousness color
     * @param consciousnessLevel 0-1
     * @return
     */
    public static Color getConsciousnessColorByLevel(float consciousnessLevel) {
        if(getConsciousnessLevel(consciousnessLevel).getColor() != null) {
            return getConsciousnessLevel(consciousnessLevel).getColor();
        }
        return Misc.getTextColor();
    }
    public static String getDisplayConditionLabel(PersonAPI person) {
        if(person.isAICore()) {
            return "Stability";
        }
        return "Humanity";
    }
    public static void displayConsciousEffects(TooltipMakerAPI tooltip, PersonAPI person, boolean isSimpleMode) {
        final float pad = 10f;
        float consciousnessLevel = person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
        ba_conscious conscious = getConsciousnessLevel(consciousnessLevel);
        List<ba_conscious> orderedList = new ArrayList<>();
        for(ba_variablemanager.ba_consciousnessLevel level: consciousMap.keySet()) {
            orderedList.add(consciousMap.get(level));
        }
        Collections.sort(orderedList, new Comparator<ba_conscious>() {
            @Override
            public int compare(ba_conscious o1, ba_conscious o2) {
                return o1.getDisplayOrder() > o2.getDisplayOrder()? 1: (o1.getDisplayOrder() < o2.getDisplayOrder())? -1: 0;
            }
        });
        if(!bionicalterationplugin.isConsciousnessDisable) {
            if(!isSimpleMode) {
                //in hover view
                conscious.displayTooltipDescription(tooltip, person, true, isSimpleMode);
            } else {
                //in effect list view
                for(ba_conscious level: orderedList) {
                    if(level.equals(conscious)) {
                        level.displayTooltipDescription(tooltip, person, true ,isSimpleMode);
                    } else {
                        level.displayTooltipDescription(tooltip, person, false ,isSimpleMode);
                    }
                    tooltip.addSpacer(pad);
                }
            }
        } else {
            if(!isSimpleMode) {
                //in hover view
                LabelAPI disabled = tooltip.addPara("%s %s", pad, Misc.getTextColor(), conscious.getDisplayName(), "[Effects Disabled]");
                tooltip.addPara("%s", pad, Misc.getTextColor(), ba_stringhelper.getString("conscious", "ba_fragile_person")).setHighlightColors(Misc.getTextColor());
                disabled.setHighlightColors(conscious.getColor(), Misc.getGrayColor());
            } else {
                //in effect list view
                for(ba_conscious level: orderedList) {
                    LabelAPI disabled = tooltip.addPara("%s %s", pad, Misc.getTextColor(), level.getDisplayName(), "[Effects Disabled]");
                    disabled.setHighlightColors(level.getColor(), Misc.getGrayColor());
                    if(!level.equals(conscious)) {
                        disabled.setOpacity(0.6f);
                    }
                    tooltip.addSpacer(pad);
                }
            }
        }
    }
}

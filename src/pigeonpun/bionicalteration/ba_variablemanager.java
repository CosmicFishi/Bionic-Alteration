package pigeonpun.bionicalteration;

import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ba_variablemanager {
    public static final String BIONIC_ALTERATION = "pigeonpun_bionicalteration";
    /**
     * Use for changing bionic's consciousness cost
     */
    public static final String BA_CONSCIOUSNESS_STATS_KEY = "ba_consciousness";
    public static final String BA_CONSCIOUSNESS_SOURCE_KEY = "ba_consciousness_source";
    public static final float BA_BRM_LIMIT_BONUS_PER_LEVEL = 5;
    public static final String BA_BRM_LIMIT_STATS_KEY = "ba_brmLimit";
    public static final String BA_BRM_LIMIT_SOURCE_KEY = "ba_brmLimit_source";
    public static final String BA_BRM_CURRENT_STATS_KEY = "ba_brmCurrent";
    public static final String BA_BRM_CURRENT_SOURCE_KEY = "ba_brmCurrent_source";
    public static final float BA_CONSCIOUSNESS_DEFAULT = 1f;
    public static final String BA_CONSCIOUSNESS_STABLE_THRESHOLD = "ba_conscious_stable";
    public static final String BA_CONSCIOUSNESS_UNSTEADY_THRESHOLD = "ba_conscious_unsteady";
    public static final String BA_CONSCIOUSNESS_WEAKENED_THRESHOLD = "ba_conscious_weaken";
    public static final String BA_CONSCIOUSNESS_FRAGILE_THRESHOLD = "ba_conscious_fragile";
    public static final String BA_CONSCIOUSNESS_CRITICAL_THRESHOLD = "ba_conscious_critical";
    public static final HashMap<String, Float> BA_CONSCIOUSNESS_THRESHOLD = new HashMap<>();
    static {
        BA_CONSCIOUSNESS_THRESHOLD.put(BA_CONSCIOUSNESS_STABLE_THRESHOLD, 0.90f);
        BA_CONSCIOUSNESS_THRESHOLD.put(BA_CONSCIOUSNESS_UNSTEADY_THRESHOLD, 0.75f);
        BA_CONSCIOUSNESS_THRESHOLD.put(BA_CONSCIOUSNESS_WEAKENED_THRESHOLD, 0.60f);
        BA_CONSCIOUSNESS_THRESHOLD.put(BA_CONSCIOUSNESS_FRAGILE_THRESHOLD, 0.45f);
        BA_CONSCIOUSNESS_THRESHOLD.put(BA_CONSCIOUSNESS_CRITICAL_THRESHOLD, 0.30f);
    }
    public static final HashMap<String, Color> BA_CONSCIOUSNESS_COLOR = new HashMap<>();
    static {
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_STABLE_THRESHOLD, Misc.interpolateColor(Misc.getPositiveHighlightColor(), Misc.getNegativeHighlightColor(), 0.8f));
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_UNSTEADY_THRESHOLD, Misc.interpolateColor(Misc.getPositiveHighlightColor(), Misc.getNegativeHighlightColor(), 0.6f));
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_WEAKENED_THRESHOLD, Misc.interpolateColor(Misc.getPositiveHighlightColor(), Misc.getNegativeHighlightColor(), 0.4f));
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_FRAGILE_THRESHOLD, Misc.interpolateColor(Misc.getPositiveHighlightColor(), Misc.getNegativeHighlightColor(), 0.2f));
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_CRITICAL_THRESHOLD, Misc.getNegativeHighlightColor());
    }
}

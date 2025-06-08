package pigeonpun.bionicalteration;

import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ba_variablemanager {
    public static final String BIONIC_ALTERATION = "pigeonpun_bionicalteration";
    public static final String FACTION_DATA_PATH = "data/bionic/faction_data.json";
    public static final int BIONIC_INSTALL_PER_LIMB = 1;
    /**
     * Use for changing bionic's consciousness cost
     */
    public static final int BA_BRM_LIMIT_BONUS_PER_LEVEL = 6;
    public static final float BA_BRM_LIMIT_BONUS_PER_LEVEL_ADMIN = BA_BRM_LIMIT_BONUS_PER_LEVEL * 3;
    /**
     * person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f)
     */
    public static final String BA_BRM_LIMIT_STATS_KEY = "ba_brmLimit";
    public static final String BA_BRM_LIMIT_SOURCE_KEY = "ba_brmLimit_source";
    /**
     * person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f)
     */
    public static final String BA_BRM_CURRENT_STATS_KEY = "ba_brmCurrent";
    public static final String BA_BRM_CURRENT_SOURCE_KEY = "ba_brmCurrent_source";
    /**
     * person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f)
     */
    public enum ba_consciousnessLevel {
        STABLE,
        UNSTEADY,
        WEAKEN,
        FRAGILE,
        CRITICAL
    }
    public static final String BA_CONSCIOUSNESS_STATS_KEY = "ba_consciousness";
    public static final String BA_CONSCIOUSNESS_SOURCE_KEY = "ba_consciousness_source";
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
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_STABLE_THRESHOLD, Misc.interpolateColor(Misc.getPositiveHighlightColor(), Misc.getNegativeHighlightColor(), 0.2f));
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_UNSTEADY_THRESHOLD, Misc.interpolateColor(Misc.getPositiveHighlightColor(), Misc.getNegativeHighlightColor(), 0.4f));
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_WEAKENED_THRESHOLD, Misc.interpolateColor(Misc.getPositiveHighlightColor(), Misc.getNegativeHighlightColor(), 0.6f));
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_FRAGILE_THRESHOLD, Misc.interpolateColor(Misc.getPositiveHighlightColor(), Misc.getNegativeHighlightColor(), 0.8f));
        BA_CONSCIOUSNESS_COLOR.put(BA_CONSCIOUSNESS_CRITICAL_THRESHOLD, Misc.getNegativeHighlightColor());
    }
    public static final String BA_BIONIC_SKILL_ID = "ba_bionic_augmented";
    public static final String BA_RANDOM_BIONIC_GENERATED_TAG = "ba_random_bionic_generated";
    public static final String BA_SEED_KEY = "ba_sector_seed";
    public static final String BA_BIONIC_ON_NEW_GAME_KEY = "$ba_bionic_on_new_game";
    public static final String BIONIC_NO_DROP_TAG = "no_drop_bionic";
    public static final String PERSISTENT_RANDOM_KEY = "$ba_bionic_random_generation";
    public static final String BA_ABILITY_KEY = "ba_bionicalteration";
    public static final String BA_MARKET_CONDITION_ID = "bionic_condition";
    public static final String BA_MARKET_BIONIC_MEMORY_KEY = "$ba_market_bionic_effect_applied";
    public static final String BA_MARKET_ADMIN_SET_UP = "$ba_market_admin_set_up";
    public static final Color BA_OVERCLOCK_COLOR = new Color(255, 149, 0);
    public static final String BA_OVERCLOCK_STATION = "ba_station_overclock";
    public static final String BA_BIONIC_RESEARCH_STATION_SPAWNED_KEY = "$ba_bionic_research_station_spawned";
    public static final String BA_OVERCLOCK_ITEM = "ba_evoshard";
    public static final String BA_PERSON_MEMORY_BIONIC_KEY = "$ba_bionic_key";
    public static final String BA_FLEET_MEMORY_BIONIC_KEY = "$ba_fleet_bionic_key";
    public static final String BA_BIONIC_INFO_HULLMOD = "ba_bionicinfo";
    public static final String BA_SHELL_CORRUPTED_HULLMOD = "ba_corruptedshell";
    public static final String BA_SHELL_PRISTINE_HULLMOD = "ba_pristineshell";
    public static final String BA_BIONIC_CONTAINER_PERSISTENT_KEY = "ba_bionic_container";
    public static final String BA_OVERCLOCK_STATION_TAG_NAME = "Overclock station";
    public static final String BA_ACHIEVEMENT_ZERO_CONSCIOUSNESS_ITEM_KEY = "$ba_zero_consciousness_reward";
    /**
     * Default value, can be change in settings.json
     */
    public static final int BA_ACADEMIC_MAX_BRM_TIER = 7;
    public static final int BA_ACADEMIC_UPGRADE_BASE_CREDIT = 20000;
    public static final HashMap<Integer, String> BA_CVE = new HashMap<>();
    static {
        BA_CVE.put(0, "CVE-2024-47611");
        BA_CVE.put(1, "CVE-2021-44228");
        BA_CVE.put(2, "CVE-2014-0160");
        BA_CVE.put(3, "CVE-2022-0609");
        BA_CVE.put(4, "CVE-2025-20124");
    }
    public static final String BA_BLIND_ENTRY_ITEM_ID = "ba_blind_entry";
    public static final int BA_BLIND_ENTRY_BRM_INCREMENT = (int) (BA_BRM_LIMIT_BONUS_PER_LEVEL * 2);
    public static final String BA_DYNAMICALLY_CREATE_LIMB = "ba_dynamically_created_limb";
}

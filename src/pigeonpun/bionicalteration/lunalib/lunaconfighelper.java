package pigeonpun.bionicalteration.lunalib;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;

public class lunaconfighelper implements LunaSettingsListener {
    public static Logger log = Global.getLogger(lunaconfighelper.class);

    public static void initLunaConfig() {
        String mid = ba_variablemanager.BIONIC_ALTERATION;
        //List<String> tags = DEFAULT_TAGS;

        addHeader("Generate", null);
        addSetting("isAllowBionicsToSpawnInPlayerFleetOnNewSave", "boolean", null, bionicalterationplugin.isAllowBionicsToSpawnInPlayerFleetOnNewSave);
        addSetting("isBRMCapDisable", "boolean", null, bionicalterationplugin.isBRMCapDisable);
        addSetting("isConsciousnessDisable", "boolean", null, bionicalterationplugin.isConsciousnessDisable);
        addHeader("BRM", null);
        addSetting("maxAcademyBRMTier", "int", null, bionicalterationplugin.maxAcademyBRMTier, 1, 100);
        addSetting("brmUpgradeBaseCredit", "int", null, bionicalterationplugin.academyBRMUpgradeBase, 0, 10000000);
        addSetting("brmUpgradePerTier", "int", null, bionicalterationplugin.brmUpgradePerTier, 1, 100);

        addHeader("debug", null);
        addSetting("isDevmode", "boolean", null, bionicalterationplugin.isDevmode);

        LunaSettings.SettingsCreator.refresh(mid);

        tryLoadLunaConfig();

        createListener();
    }

    public static void tryLoadLunaConfig() {
        try {
            loadConfigFromLuna();
        } catch (NullPointerException npe) {
            // config not created yet I guess, do nothing
        }
    }

    public static void loadConfigFromLuna() {
        bionicalterationplugin.isAllowBionicsToSpawnInPlayerFleetOnNewSave = (boolean) loadSetting("isAllowBionicsToSpawnInPlayerFleetOnNewSave", "boolean");
        bionicalterationplugin.isBRMCapDisable = (boolean) loadSetting("isBRMCapDisable", "boolean");
        bionicalterationplugin.isConsciousnessDisable = (boolean) loadSetting("isConsciousnessDisable", "boolean");
        bionicalterationplugin.isDevmode = (boolean) loadSetting("isDevmode", "boolean");
        bionicalterationplugin.maxAcademyBRMTier = (int) loadSetting("maxAcademyBRMTier", "int");
        bionicalterationplugin.academyBRMUpgradeBase = (int) loadSetting("brmUpgradeBaseCredit", "int");
        bionicalterationplugin.brmUpgradePerTier = (int) loadSetting("brmUpgradePerTier", "int");
    }

    public static Object loadSetting(String var, String type) {
        String mid = ba_variablemanager.BIONIC_ALTERATION;
        switch (type) {
            case "bool":
            case "boolean":
                return LunaSettings.getBoolean(mid, var);
            case "int":
            case "integer":
            case "key":
                return LunaSettings.getInt(mid, var);
            case "float":
                return (float)(double)LunaSettings.getDouble(mid, var);
            case "double":
                return LunaSettings.getDouble(mid, var);
            default:
                log.error(String.format("Setting %s has invalid type %s", var, type));
        }
        return null;
    }

    public static void addSetting(String var, String type, Object defaultVal) {
        addSetting(var, type, null, defaultVal, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static void addSetting(String var, String type, @Nullable String tab, Object defaultVal) {
        addSetting(var, type, tab, defaultVal, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static void addSetting(String var, String type, Object defaultVal, double min, double max) {
        addSetting(var, type, null, defaultVal, min, max);
    }

    public static void addSetting(String var, String type, @Nullable String tab, Object defaultVal, double min, double max) {
        String tooltip = getString("tooltip_" + var);
        if (tooltip.startsWith("Missing string:")) {
            tooltip = "";
        }
        String mid = ba_variablemanager.BIONIC_ALTERATION;
        String name = getString("name_" + var);

        if (tab == null) tab = "";

        switch (type) {
            case "boolean":
                LunaSettings.SettingsCreator.addBoolean(mid, var, name, tooltip, (boolean)defaultVal, tab);
                break;
            case "int":
            case "integer":
                if (defaultVal instanceof Float) {
                    defaultVal = Math.round((float)defaultVal);
                }
                LunaSettings.SettingsCreator.addInt(mid, var, name, tooltip,
                        (int)defaultVal, (int)Math.round(min), (int)Math.round(max), tab);
                break;
            case "float":
                // fix float -> double conversion causing an unround number
                String floatStr = ((Float)defaultVal).toString();
                LunaSettings.SettingsCreator.addDouble(mid, var, name, tooltip,
                        Double.parseDouble(floatStr), min, max, tab);
                break;
            case "double":
                LunaSettings.SettingsCreator.addDouble(mid, var, name, tooltip,
                        (double)defaultVal, min, max, tab);
                break;
            case "key":
                LunaSettings.SettingsCreator.addKeybind(mid, var, name, tooltip, (int)defaultVal, tab);
            default:
                log.error(String.format("Setting %s has invalid type %s", var, type));
        }
    }

    public static void addHeader(String id, String tab) {
        addHeader(id, getString("header_" + id), tab);
    }

    public static void addHeader(String id, String title, String tab) {
        if (tab == null) tab = "";
        LunaSettings.SettingsCreator.addHeader(ba_variablemanager.BIONIC_ALTERATION, id, title, tab);
    }

    public static lunaconfighelper createListener() {
        lunaconfighelper helper = new lunaconfighelper();
        LunaSettings.INSTANCE.addListener(helper);
        return helper;
    }

    @Override
    public void settingsChanged(String modId) {
        if (ba_variablemanager.BIONIC_ALTERATION.equals(modId)) {
            loadConfigFromLuna();
        }
    }

    public static String getString(String id) {
        return Global.getSettings().getString("ps_lunasettings", id);
    }
}

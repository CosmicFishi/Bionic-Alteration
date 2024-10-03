package pigeonpun.bionicalteration.plugin;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import lunalib.lunaRefit.LunaRefitManager;
import org.lazywizard.lazylib.MathUtils;
import pigeonpun.bionicalteration.*;
import pigeonpun.bionicalteration.ability.ba_bionicability;
import pigeonpun.bionicalteration.faction.ba_factionmanager;
import pigeonpun.bionicalteration.listeners.ba_campaignlistener;
import pigeonpun.bionicalteration.listeners.ba_salvagelistener;
import pigeonpun.bionicalteration.lunalib.ba_bionic_refit_btn;
import pigeonpun.bionicalteration.lunalib.lunaconfighelper;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;
import pigeonpun.bionicalteration.variant.ba_variantmanager;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;

public class bionicalterationplugin extends BaseModPlugin {
    ba_campaignlistener campaignListener = null;
    ba_salvagelistener salvageListener = null;
    public static boolean isLunalibEnabled = false;
    public static boolean isAllowBionicsToSpawnInPlayerFleetOnNewSave = false;
    public static boolean isDevmode = false;
    public static boolean isBRMCapDisable = false;
    public static boolean isConsciousnessDisable = false;
    @Override
    public void onApplicationLoad() throws Exception {
//        ba_manager.getInstance();
        ba_overclockmanager.onApplicationLoad();
        ba_bionicmanager.onApplicationLoad();
        ba_variantmanager.onApplicationLoad();
        ba_limbmanager.onApplicationLoad();
        ba_consciousmanager.onApplicationLoad();
        ba_factionmanager.onApplicationLoad();

        isLunalibEnabled = Global.getSettings().getModManager().isModEnabled("lunalib");
        isAllowBionicsToSpawnInPlayerFleetOnNewSave = Global.getSettings().getBoolean("isAllowBionicsToSpawnInPlayerFleetOnNewSave");
        isDevmode = Global.getSettings().getBoolean("isDevmode");
        isBRMCapDisable = Global.getSettings().getBoolean("isBRMCapDisable");
        isConsciousnessDisable = Global.getSettings().getBoolean("isConsciousnessDisable");

        if(isLunalibEnabled) {
            lunaconfighelper.initLunaConfig();
            LunaRefitManager.addRefitButton(new ba_bionic_refit_btn());
        }
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        ba_procgenmanager.generate();
        ba_officermanager.onSaveLoad();
        ba_marketmanager.onSaveLoad();
        addListeners();
        Global.getSector().getCharacterData().addAbility(ba_variablemanager.BA_ABILITY_KEY);
    }

    @Override
    public void beforeGameSave() {
        removeListeners();
    }

    @Override
    public void afterGameSave() {
        addListeners();
    }

//    @Override
//    public void onNewGameAfterProcGen() {
//        ba_procgenmanager.generate();
//    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        ba_marketmanager.onNewGameAfterEconomyLoad();
    }
    protected void addListeners() {
        campaignListener = new ba_campaignlistener(false);
        salvageListener = new ba_salvagelistener();

        Global.getSector().getListenerManager().addListener(salvageListener, true);
        Global.getSector().addTransientListener(campaignListener);
        Global.getSector().getListenerManager().addListener(campaignListener, true);
        Global.getSector().addTransientScript(campaignListener);
//        Global.getSector().addListener(campaignListener);
    }
    protected void removeListeners() {
        Global.getSector().getListenerManager().removeListener(salvageListener);
        Global.getSector().getListenerManager().removeListener(campaignListener);
        Global.getSector().removeTransientScript(campaignListener);
        Global.getSector().removeListener(campaignListener);
    }
    public static String getSectorSeed() {
        if(!Global.getSector().getPersistentData().containsKey(ba_variablemanager.BA_SEED_KEY)) {
            Global.getSector().getPersistentData().put(ba_variablemanager.BA_SEED_KEY, String.valueOf(MathUtils.getRandomNumberInRange(1000, 10000)).hashCode());
        }
        return String.valueOf(Global.getSector().getPersistentData().get(ba_variablemanager.BA_SEED_KEY));
    }
//
//    @Override
//    public void beforeGameSave() {
//        ba_manager.getInstance().beforeGameSave();
//    }
//
//    @Override
//    public void afterGameSave() {
//        ba_manager.getInstance().afterGameSave();
//    }
}

package pigeonpun.bionicalteration.plugin;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.faction.ba_factionmanager;
import pigeonpun.bionicalteration.listeners.ba_campaignlistener;
import pigeonpun.bionicalteration.listeners.ba_salvagelistener;
import pigeonpun.bionicalteration.variant.ba_variantmanager;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;

public class bionicalterationplugin extends BaseModPlugin {
    ba_campaignlistener campaignListener = null;
    ba_salvagelistener salvageListener = null;
    @Override
    public void onApplicationLoad() throws Exception {
//        ba_manager.getInstance();
        ba_bionicmanager.onApplicationLoad();
        ba_variantmanager.onApplicationLoad();
        ba_limbmanager.onApplicationLoad();
        ba_consciousmanager.onApplicationLoad();
        ba_factionmanager.onApplicationLoad();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        ba_officermanager.onSaveLoad();
        addListeners();
    }

    @Override
    public void beforeGameSave() {
        removeListeners();
    }

    @Override
    public void afterGameSave() {
        addListeners();
    }

    protected void addListeners() {
        campaignListener = new ba_campaignlistener(false);
        salvageListener = new ba_salvagelistener();

        Global.getSector().getListenerManager().addListener(salvageListener, true);
        Global.getSector().addTransientListener(campaignListener);
        Global.getSector().addListener(campaignListener);
    }
    protected void removeListeners() {
        Global.getSector().getListenerManager().removeListener(salvageListener);
        Global.getSector().removeTransientScript(campaignListener);
        Global.getSector().removeListener(campaignListener);
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

package pigeonpun.bionicalteration.plugin;

import com.fs.starfarer.api.BaseModPlugin;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variantmanager;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;

public class bionicalterationplugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
//        ba_manager.getInstance();
        ba_bionicmanager.onApplicationLoad();
        ba_variantmanager.onApplicationLoad();
        ba_limbmanager.onApplicationLoad();
        ba_consciousmanager.onApplicationLoad();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        ba_officermanager.onSaveLoad();
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

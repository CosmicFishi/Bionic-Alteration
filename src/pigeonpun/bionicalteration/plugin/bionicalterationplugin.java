package pigeonpun.bionicalteration.plugin;

import com.fs.starfarer.api.BaseModPlugin;
import pigeonpun.bionicalteration.ba_anatomymanager;
import pigeonpun.bionicalteration.ba_bionicmanager;

public class bionicalterationplugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
//        ba_manager.getInstance();
        ba_bionicmanager.onApplicationLoad();
        ba_anatomymanager.onApplicationLoad();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
    }

    @Override
    public void onGameLoad(boolean newGame) {
//        ba_manager.getInstance();
//        ba_manager.getInstance().onGameLoad();
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

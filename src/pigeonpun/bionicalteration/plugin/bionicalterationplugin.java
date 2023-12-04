package pigeonpun.bionicalteration.plugin;

import com.fs.starfarer.api.BaseModPlugin;

public class bionicalterationplugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
//        ba_manager.getInstance();

        // Test that the .jar is loaded and working, using the most obnoxious way possible.
//        throw new RuntimeException("Template mod loaded! Remove this crash in TemplateModPlugin.");
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

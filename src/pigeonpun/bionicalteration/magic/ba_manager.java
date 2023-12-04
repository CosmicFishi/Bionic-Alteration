package pigeonpun.bionicalteration.magic;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import org.apache.log4j.Logger;

public class ba_manager {
    Logger log = Global.getLogger(ba_manager.class);
    private static ba_manager instance;
    public static ba_manager getInstance() {
        if(instance == null) {
            instance = new ba_manager();
        }
        return instance;
    }
    public void initIntel() {
        if(Global.getSector() == null) return;
        ba_bionicintel intel = new ba_bionicintel();
        Global.getSector().getIntelManager().addIntel(intel, true);
        intel.setImportant(true);
        intel.setNew(false);
    }
    public void onGameLoad() {
        initIntel();
    }
    public void beforeGameSave() {
        log.info("before game save");
        removeIntel();
    }
    public void afterGameSave() {
        log.info("after game save");
        initIntel();
    }
    public ba_bionicintel getIntel() {
        try {
            return (ba_bionicintel) Global.getSector().getIntelManager().getFirstIntel(ba_bionicintel.class);
        } catch (Exception e) {
            log.warn("Can not get Bionic Intel");
            return null;
        }
    }
    public void removeIntel() {
        if (Global.getSector() == null) return;

        IntelManagerAPI intelManager = Global.getSector().getIntelManager();

        while (intelManager.hasIntelOfClass(ba_bionicintel.class)) {
            intelManager.removeIntel(intelManager.getFirstIntel(ba_bionicintel.class));
        }
    }
}

package pigeonpun.bionicalteration;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;

public class ba_test implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        //todo: generate a salvage pod to test the cryopod thingy
        //todo: create a method to generate salvage pod that will always give officer/admin (May be merge it into magiclib ?) SleeperPodsSpecialCreator class
    }
}

package pigeonpun.bionicalteration.achievement;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.magiclib.achievements.MagicAchievement;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.inventory.ba_inventoryhandler;

import java.util.List;

public class ba_zeroconsciousness extends MagicAchievement {

    @Override
    public void advanceAfterInterval(float amount) {
        if(!isComplete()) {
            List<PersonAPI> listP = ba_officermanager.getListOfficerFromFleet(null, true);
            for(PersonAPI person: listP) {
                if(person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f) <= 0f) {
                    completeAchievement();
                    break;
                }
            }
        }
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void createTooltip(@NotNull TooltipMakerAPI tooltipMakerAPI, boolean isExpanded, float width) {
        tooltipMakerAPI.addTitle(getName());
        LabelAPI info = tooltipMakerAPI.addPara("Reach %s consciousness on any officer / admin, a %s will be rewarded.", 10f, Misc.getNegativeHighlightColor(), "0","special bionic");
        info.setHighlightColors(Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("%s", 10, Misc.getGrayColor(), "Save independent, the bionic will be available on new saves after completion");
    }

    @Override
    public void onSaveGameLoaded(boolean isComplete) {
        if(isComplete && !Global.getSector().getMemoryWithoutUpdate().contains(ba_variablemanager.BA_ACHIEVEMENT_ZERO_CONSCIOUSNESS_ITEM_KEY)) {
            addSpecialItem();
            Global.getSector().getMemoryWithoutUpdate().set(ba_variablemanager.BA_ACHIEVEMENT_ZERO_CONSCIOUSNESS_ITEM_KEY, true);
        }
    }

    @Override
    public void onCompleted(@Nullable PersonAPI completedByPlayer) {
        addSpecialItem();
    }

    protected void addSpecialItem() {
        ba_inventoryhandler.addToContainer(ba_bionicmanager.getBionic("ba_false_visage"), null);
    }
}

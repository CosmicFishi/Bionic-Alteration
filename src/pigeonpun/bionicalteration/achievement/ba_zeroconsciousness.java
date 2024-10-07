package pigeonpun.bionicalteration.achievement;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
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
        //todo: check for consciousness on player fleet for a person that reached 0 consciousness
        List<PersonAPI> listP = ba_officermanager.getListOfficerFromFleet(null, true);
        for(PersonAPI person: listP) {
            if(person.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f) <= 0f) {
                completeAchievement();
                break;
            }
        }
    }

    @Override
    public void onSaveGameLoaded(boolean isComplete) {
        if(isComplete && !Global.getSector().getMemoryWithoutUpdate().contains(ba_variablemanager.BA_ACHIEVEMENT_ZERO_CONSCIOUSNESS_ITEM_KEY)) {
            addSpecialItem();
            Global.getSector().getMemoryWithoutUpdate().set(ba_variablemanager.BA_ACHIEVEMENT_ZERO_CONSCIOUSNESS_ITEM_KEY, true);
        }
    }

    @Override
    public void completeAchievement() {
        addSpecialItem();
    }
    protected void addSpecialItem() {
        ba_inventoryhandler.addToContainer(ba_bionicmanager.getBionic(""), null);
    }
}

package pigeonpun.bionicalteration.ui.overclock;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

import java.util.Map;

//blank interaction to select the option to show the panel
public class ba_interactiondialog implements InteractionDialogPlugin {
    public InteractionDialogAPI dialog;

    static enum OptionID{
        INIT,
        LEAVE
    }
    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        dialog.hideTextPanel();
        dialog.hideVisualPanel();
        dialog.setPromptText("");

        optionSelected(null, OptionID.INIT);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if(optionData == OptionID.INIT) {
            dialog.showCustomVisualDialog(ba_uiplugin.MAIN_CONTAINER_WIDTH,
                    ba_uiplugin.MAIN_CONTAINER_HEIGHT,
                    new ba_delegate(ba_uiplugin.createDefault(), dialog)
                    );
        }
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {

    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {

    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }
}

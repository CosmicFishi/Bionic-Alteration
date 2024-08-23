package pigeonpun.bionicalteration.ui.overclock;

import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import java.util.List;

//following cptdash aka SpeedRacer guide
//need his to "trigger" the interaction to show the container panel
public class ba_delegate implements CustomVisualDialogDelegate {
    protected DialogCallbacks callbacks;
    protected ba_uiplugin containerPanelPlugin;
    protected InteractionDialogAPI dialog;
    public ba_delegate(ba_uiplugin containerPanel, InteractionDialogAPI dialog) {
        this.dialog = dialog;
        this.containerPanelPlugin = containerPanel;
    }

    @Override
    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.callbacks = callbacks;
        this.containerPanelPlugin.init(panel, callbacks, dialog);
    }

//    public CustomUIPanelPlugin getCustomPanelPlugin() {
//        return null;
//    }
    @Override
    public ba_uiplugin getCustomPanelPlugin() { return containerPanelPlugin;}

    @Override
    public float getNoiseAlpha() {
        return 0;
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void reportDismissed(int option) {

    }
}

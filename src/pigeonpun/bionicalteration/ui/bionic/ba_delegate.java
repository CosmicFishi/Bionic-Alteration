package pigeonpun.bionicalteration.ui.bionic;

import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import java.util.List;
import java.util.Objects;

//following cptdash aka SpeedRacer guide
//need his to "trigger" the interaction to show the container panel
public class ba_delegate implements CustomVisualDialogDelegate {
    protected DialogCallbacks callbacks;
    protected ba_uiplugin containerPanelPlugin;
    protected InteractionDialogAPI dialog;
    protected List<PersonAPI> listPerson = null;
    protected String workshopMode = ba_uiplugin.INSTALL_WORKSHOP;
    public ba_delegate(ba_uiplugin containerPanel, InteractionDialogAPI dialog, List<PersonAPI> listPerson) {
        this.dialog = dialog;
        this.containerPanelPlugin = containerPanel;
        this.listPerson = listPerson;
    }

    /**
     * @param containerPanel
     * @param dialog
     * @param listPerson
     */
    public ba_delegate(ba_uiplugin containerPanel, InteractionDialogAPI dialog, List<PersonAPI> listPerson, String workshopMode) {
        this.dialog = dialog;
        this.containerPanelPlugin = containerPanel;
        this.listPerson = listPerson;
        this.workshopMode = workshopMode;
    }

    @Override
    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.callbacks = callbacks;
        if(listPerson == null || listPerson.isEmpty()) {
            if(Objects.equals(this.workshopMode, ba_uiplugin.INSTALL_WORKSHOP)) {
                this.containerPanelPlugin.init(panel, callbacks, dialog);
            } else {
//                this.containerPanelPlugin.init(panel, callbacks, dialog, ba_uiplugin.BIOFORM, null);
            }
        } else {
            this.containerPanelPlugin.init(panel, callbacks, dialog, ba_uiplugin.WORKSHOP, listPerson);
        }
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

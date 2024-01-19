package pigeonpun.bionicalteration.listeners;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;

import java.util.List;

public class ba_campaignlistener extends BaseCampaignEventListener implements EveryFrameScript {
    public ba_campaignlistener(boolean permaRegister) {
        super(permaRegister);
    }

    @Override
    public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
        SectorEntityToken target = dialog.getInteractionTarget();
        if(target == null) return;
        InteractionDialogPlugin plugin = dialog.getPlugin();
        if(plugin instanceof FleetInteractionDialogPluginImpl) {
            FleetEncounterContext context = (FleetEncounterContext) plugin.getContext();
            List<CampaignFleetAPI> fleets = context.getBattle().getBothSides();
            for (CampaignFleetAPI fleet: fleets) {
                if(!fleet.isPlayerFleet()) {
                    //generate bionic here
                    //todo: generate bionic base on faction on fleet interact
                }
            }
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {

    }
}

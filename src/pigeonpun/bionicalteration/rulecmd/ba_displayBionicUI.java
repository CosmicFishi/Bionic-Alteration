package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ui.bionic.ba_delegate;
import pigeonpun.bionicalteration.ui.bionic.ba_uiplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ba_displayBionicUI extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        SectorEntityToken target = dialog.getInteractionTarget();
        if (target == null) return false;
        InteractionDialogPlugin plugin = dialog.getPlugin();
        //Fleet interaction
        if (plugin instanceof FleetInteractionDialogPluginImpl) {
            FleetEncounterContext context = (FleetEncounterContext) plugin.getContext();
            List<CampaignFleetAPI> fleets = context.getBattle().getBothSides();
            List<PersonAPI> listPerson = new ArrayList<>(ba_officermanager.getListOfficerFromFleet(fleets, false));
            dialog.showCustomVisualDialog(ba_uiplugin.MAIN_CONTAINER_WIDTH,
                    ba_uiplugin.MAIN_CONTAINER_HEIGHT,
                    new ba_delegate(ba_uiplugin.createDefault(), dialog, listPerson)
            );
            return true;
        }
        if (plugin instanceof RuleBasedInteractionDialogPluginImpl) {
            //hiring UI
            List<PersonAPI> listPerson = new ArrayList<>();
            if(!listPerson.isEmpty()) {
                for(PersonAPI person: target.getMarket().getPeopleCopy()) {
                    if(person.getId().equals(params.get(0).getString(memoryMap))) {
                        listPerson.add(person);
                        break;
                    }
                }
                dialog.showCustomVisualDialog(ba_uiplugin.MAIN_CONTAINER_WIDTH,
                        ba_uiplugin.MAIN_CONTAINER_HEIGHT,
                        new ba_delegate(ba_uiplugin.createDefault(), dialog, listPerson)
                );
            }
            if(target.getTags().contains("ba_overclock_station")) {
                dialog.showCustomVisualDialog(ba_uiplugin.MAIN_CONTAINER_WIDTH,
                        ba_uiplugin.MAIN_CONTAINER_HEIGHT,
                        new ba_delegate(ba_uiplugin.createDefault(), dialog, null)
                );
            }
            return true;
        }
        return false;
    }
}

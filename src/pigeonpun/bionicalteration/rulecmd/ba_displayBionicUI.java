package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
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
    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    //    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
//        if (dialog == null) return false;
//        SectorEntityToken target = dialog.getInteractionTarget();
//        if (target == null) return false;
//        InteractionDialogPlugin plugin = dialog.getPlugin();
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        if(command.equals("hiring")) {
            hiring(params);
        } else if (command.equals("otherFleet")) {
            otherFleet();
        } else if (command.equals("station")) {
            station();
        }
        return true;
    }
    public void hiring(List<Misc.Token> params) {
        if (this.dialog.getPlugin() instanceof RuleBasedInteractionDialogPluginImpl) {
            //hiring UI
            List<PersonAPI> listPerson = new ArrayList<>();
            for(PersonAPI person: this.entity.getMarket().getPeopleCopy()) {
                if(person.getId().equals(params.get(1).getString(memoryMap))) {
                    listPerson.add(person);
                    break;
                }
            }
            if(!listPerson.isEmpty()) {
                dialog.showCustomVisualDialog(ba_uiplugin.MAIN_CONTAINER_WIDTH,
                        ba_uiplugin.MAIN_CONTAINER_HEIGHT,
                        new ba_delegate(ba_uiplugin.createDefault(), dialog, listPerson)
                );
            }
        }
    }
    public void otherFleet() {
//Fleet interaction
        if (this.dialog.getPlugin() instanceof FleetInteractionDialogPluginImpl) {
            FleetEncounterContext context = (FleetEncounterContext) this.dialog.getPlugin().getContext();
            List<CampaignFleetAPI> fleets = context.getBattle().getBothSides();
            List<PersonAPI> listPerson = new ArrayList<>(ba_officermanager.getListOfficerFromFleet(fleets, false));
            dialog.showCustomVisualDialog(ba_uiplugin.MAIN_CONTAINER_WIDTH,
                    ba_uiplugin.MAIN_CONTAINER_HEIGHT,
                    new ba_delegate(ba_uiplugin.createDefault(), dialog, listPerson)
            );
        }
    }
    public void station() {
        if (this.dialog.getPlugin() instanceof RuleBasedInteractionDialogPluginImpl) {
            if(entity.getTags().contains("ba_overclock_station")) {
                dialog.showCustomVisualDialog(ba_uiplugin.MAIN_CONTAINER_WIDTH,
                        ba_uiplugin.MAIN_CONTAINER_HEIGHT,
                        new ba_delegate(ba_uiplugin.createDefault(), dialog, null)
                );
            }
        }
    }
}

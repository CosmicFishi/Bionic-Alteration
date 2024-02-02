package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_officermanager;

import java.util.List;
import java.util.Map;

public class ba_hasBionics extends BaseCommandPlugin {
    static Logger log = Global.getLogger(ba_hasBionics.class);
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if(dialog == null) return false;
        SectorEntityToken entity = dialog.getInteractionTarget();
        if(entity != null) {
            if (dialog.getPlugin() instanceof FleetInteractionDialogPluginImpl) {
                FleetInteractionDialogPluginImpl plugin = (FleetInteractionDialogPluginImpl) dialog.getPlugin();
                FleetEncounterContext context = (FleetEncounterContext) plugin.getContext();
                CampaignFleetAPI fleet = context.getBattle().getNonPlayerCombined();
                return !ba_officermanager.getListPersonsHaveBionic(fleet).isEmpty();
            }
        }

        return false;
    }
}

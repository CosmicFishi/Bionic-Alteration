package pigeonpun.bionicalteration.listeners;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.campaign.CampaignPlanet;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.faction.ba_factiondata;
import pigeonpun.bionicalteration.faction.ba_factionmanager;

import java.util.ArrayList;
import java.util.List;

public class ba_campaignlistener extends BaseCampaignEventListener implements EveryFrameScript {
    static Logger log = Global.getLogger(ba_campaignlistener.class);
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
            List<PersonAPI> listPerson = new ArrayList<>(ba_officermanager.getListOfficerFromFleet(fleets, false));
            ba_officermanager.setUpListOfficers(listPerson);
            CampaignFleetAPI otherFleet = context.getBattle().getCombinedTwo();
            otherFleet.getMemoryWithoutUpdate().set("$ba_bionic_dropList", ba_officermanager.getListPotentialBionicDrop(otherFleet));
            log.info("Set up for officers completed");
        }
//        if(target.getMarket() != null) {
//            if(target.getMarket().getAdmin() != null) {
//                List<PersonAPI> listPerson = new ArrayList<>();
//                listPerson.add(target.getMarket().getAdmin());
//                ba_officermanager.setUpListOfficers(listPerson);
//            }
//        }
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

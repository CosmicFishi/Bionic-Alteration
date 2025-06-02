package pigeonpun.bionicalteration.events;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.ProcurementMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ba_ripper_bionic extends HubMissionWithBarEvent {
    public static enum Stage {
        SELLING,
        COMPLETED
    }
    protected int cost;
    protected FactionAPI faction;
    protected MarketAPI market;
    public static class CheckDate implements ConditionChecker {
        public boolean conditionsMet() {
            return true;
        }
    }
    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        if(barEvent) {
            setGiverRank(Ranks.CITIZEN);
            setGiverPost(pickOne(Ranks.POST_SMUGGLER, Ranks.POST_GANGSTER,
                    Ranks.POST_FENCE, Ranks.POST_CRIMINAL));
            setGiverImportance(pickImportance());
            setGiverTags(Tags.CONTACT_UNDERWORLD);
            setGiverFaction(Factions.PIRATES);
            findOrCreateGiver(createdAt, false, false);
        }
        PersonAPI person = getPerson();
        if (person == null) return false;
        MarketAPI market = person.getMarket();
        if (market == null) return false;

        if (!setPersonMissionRef(person, "$ba_ripperBionic_ref")) {
            return false;
        }

        if (barEvent) {
            setGiverIsPotentialContactOnSuccess(1);
        }
        setStartingStage(Stage.SELLING);
        setStageOnMemoryFlag(Stage.COMPLETED, person, "$ba_ripperBionic_completed");
        setName("RipperDoc contact acquisition");
        return true;
    }

    @Override
    public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String action = params.get(0).getString(memoryMap);

        if(action.equals("ba_addRipperDoc_Contact")) {
            addPotentialContacts(dialog);
            endSuccess(dialog, memoryMap);
            return true;
        }
        return super.callEvent(ruleId, dialog, params, memoryMap);
    }
}

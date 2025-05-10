package pigeonpun.bionicalteration.events;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
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
    protected List<ba_bionicitemplugin> listBionics = new ArrayList<>();
    protected boolean armsDealer = false;
    protected int maxTotalBionic;
    protected int minTotalBionic;
    protected float costMult;
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
        return true;
    }

    @Override
    public void setCurrentStage(Object next, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.setCurrentStage(next, dialog, memoryMap);
    }

    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if(action.equals("ba_ripperBionic_selectBionics")) {
            dialog.showCargoPickerDialog("Offers", "Confirm", "Cancel", false, 400f, null, new CargoPickerListener() {
                @Override
                public void pickedCargo(CargoAPI cargo) {

                }

                @Override
                public void cancelledCargoSelection() {

                }

                @Override
                public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
                    panel.addPara("testing, AHHHHHHHHHHHHHHH", 10f);
                }
            });
            return true;
        }
        return super.callAction(action, ruleId, dialog, params, memoryMap);
    }
}

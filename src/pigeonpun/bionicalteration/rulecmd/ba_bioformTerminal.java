package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class ba_bioformTerminal extends BaseCommandPlugin {
    Logger log = Global.getLogger(ba_bioformTerminal.class);
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
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        if(command.equals("upgrade")) {
            upgrade();
        } else if (command.equals("checkIfCanUpgrade")) {
            checkIfCanUpgrade();
//        } else if (command.equals("station")) {
//            station();
        }
        return true;
    }
    public void checkIfCanUpgrade() {
        int requiredCrew = memory.getInt("$ba_upgrade_crewReq");
        int requiredMetal = memory.getInt("$ba_upgrade_metalsReq");
        int requiredMachine = memory.getInt("$ba_upgrade_machineReq");
        int requiredAlphaCore = memory.getInt("$ba_upgrade_ai_alphaReq");
        boolean disableUpgrade = true;
        if(checkCargoAvailable(requiredCrew, "crew") && checkCargoAvailable(requiredMetal, "metals") && checkCargoAvailable(requiredMachine, "heavy_machinery") && checkCargoAvailable(requiredAlphaCore, "alpha_core")) {
            disableUpgrade = false;
        }
        if(disableUpgrade) {
            dialog.getOptionPanel().setEnabled("ba_bioformUpgradeOpt", false);
        }
    }
    public boolean checkCargoAvailable(int amount, String commoditiesId) {
        int total = 0;
        for(CargoStackAPI stack: Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {
            if(stack.isCommodityStack() && stack.getCommodityId().equals(commoditiesId)) {
                total += (int) stack.getSize();
            }
        }
        return total >= amount;
    }
    public void upgrade() {

    }
}

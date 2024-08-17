package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ba_evoshardExchange extends BaseCommandPlugin {
    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        //todo: do the evoshard exchange similar to how project did for the special weapons
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        if(command.equals("exchange")) {
            exchangeEvoshard();
        }

        return true;
    }
    protected void exchangeEvoshard() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for(CargoStackAPI cargo: playerCargo.getStacksCopy()) {
            if(cargo.isSpecialStack() && cargo.getSpecialItemSpecIfSpecial() != null && ba_bionicmanager.bionicItemMap.containsKey(cargo.getSpecialDataIfSpecial().getId())) {
                SpecialItemData data = cargo.getSpecialDataIfSpecial();
                copy.addSpecial(data, cargo.getSize());
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select bionics to exchange to Evoshard", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
            public void pickedCargo(CargoAPI cargo) {
                if (cargo.isEmpty()) {
                    cancelledCargoSelection();
                    return;
                }
                cargo.sort();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    if(stack.isSpecialStack()) {
                        playerCargo.removeItems(stack.getType(), stack.getSpecialDataIfSpecial(), stack.getSize());
                        AddRemoveCommodity.addItemLossText(stack.getSpecialDataIfSpecial(), (int) stack.getSize(), text);
                    }
                }
                float evoshardsGain = computeEvoshardValue(cargo);
                if(evoshardsGain > 0) {
                    addEvoshardsGainText((int) evoshardsGain, text);
                }
            }
            public void cancelledCargoSelection() {
            }

            @Override
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
                final float pad = 10f;
                float opad = 10f;
                Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                final Color t = Misc.getTextColor();
                final Color g = Misc.getGrayColor();
                final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;
                panel.addPara("Exchanging bionics below for Evoshards, will result in:", opad);
                panel.beginGrid(width, 2, t);
                if(!cargo.getStacksCopy().isEmpty()) {
                    int i = 0;
                    for(CargoStackAPI stack: cargo.getStacksCopy()) {
                        SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
                        if (spec != null && ba_bionicmanager.getBionic(spec.getId()) != null) {
                            ba_bionicitemplugin bionic = ba_bionicmanager.getBionic(spec.getId());
                            panel.setGridLabelColor(bionic.displayColor);
                            panel.addToGrid(0, i, bionic.getName(), "" + Math.round(ba_overclockmanager.computeEvoshardForBionic(bionic)) + " Evoshards");
                            i++;
                        }
                    }
                } else {
                    panel.addPara("Empty, select bionic to exchange for Evoshards", pad);
                }
                panel.addGrid(pad);
            }

        });
    }
    public float computeEvoshardValue(CargoAPI cargo) {
        float evoshard = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
            if (spec != null && ba_bionicmanager.getBionic(spec.getId()) != null) {
                ba_bionicitemplugin bionic = ba_bionicmanager.getBionic(spec.getId());
                evoshard += bionic.brmCost * stack.getSize();
            }
        }
        evoshard *= ba_overclockmanager.evoshardToBRMRate;
        return evoshard;
    }
    public static void addEvoshardsGainText(int evoshards, TextPanelAPI text) {
        text.setFontSmallInsignia();
        String str = evoshards + " Evoshards";
        text.addParagraph("Gained " + str + "", Misc.getPositiveHighlightColor());
        text.highlightInLastPara(Misc.getHighlightColor(), str);
        text.setFontInsignia();

//        if (Global.getCurrentState() == GameState.CAMPAIGN) {
//            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
//            MemoryAPI memory = Global.getSector().getCharacterData().getMemoryWithoutUpdate();
//            memory.set("$credits", (int)fleet.getCargo().getCredits().get(), 0);
//            memory.set("$creditsStr", Misc.getWithDGS((int)fleet.getCargo().getCredits().get()), 0);
//            memory.set("$creditsStrC", Misc.getWithDGS((int)fleet.getCargo().getCredits().get()) + Strings.C, 0);
//        }
    }
}

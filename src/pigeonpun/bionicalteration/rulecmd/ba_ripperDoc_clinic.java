package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.inventory.ba_inventoryhandler;
import pigeonpun.bionicalteration.overclock.ba_overclock;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ba_ripperDoc_clinic extends BaseCommandPlugin {
    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected CargoAPI clinicShopInv;
    protected String clinicShopMemKey = "$ba_clinicShopInv_key";
    protected String toPurchaseCargoMemKey = "$ba_toPurchaseCargo_key";
    protected String markupBionicPriceMemKey = "$ba_markupBionicPrice";
    protected float markUp = 3f;
    protected float IN_STOCK_DAY = 30f;
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        clinicShopInv = Global.getFactory().createCargo(true);

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        switch (arg) {
            case "initMemKey":
                initMemKey();
                break;
            case "displayClinic":
                displayClinicInv();
                break;
            case "purchaseInfo":
                CargoAPI toPurchaseCargo = null;
                if (memory.get(toPurchaseCargoMemKey) != null && memory.get(toPurchaseCargoMemKey) instanceof CargoAPI) {
                    toPurchaseCargo = (CargoAPI) memory.get(toPurchaseCargoMemKey);
                }
                disabledPurchaseIfNeeded(toPurchaseCargo);
                displayPurchasingInfo(toPurchaseCargo);
                break;
            case "purchase":
                purchase();
                break;
        }
        return true;
    }
    public void initMemKey() {
        memory.set(markupBionicPriceMemKey, "" + Math.round(markUp * 100) + "%");
    }
    public void disabledPurchaseIfNeeded(CargoAPI cargo) {
        if(cargo == null) return;
        float availableCredits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        float neededCredits = computeTotalCreditsNeeded(cargo);
        if(availableCredits < neededCredits) {
            dialog.getOptionPanel().setEnabled("ba_ripperBionic_purchase_opt", false);
            dialog.getOptionPanel().addOptionTooltipAppender("ba_ripperBionic_purchase_opt", new OptionPanelAPI.OptionTooltipCreator() {
                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
                    tooltip.addPara("Not enough credits to make this transaction", 5f);
                }
            });
        }
    }
    public void displayPurchasingInfo(CargoAPI cargo) {
        if(cargo == null) return;

        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        if(!cargo.isEmpty()) {
            int i = 1;
            TooltipMakerAPI panel = text.beginTooltip();
            panel.setParaFontOrbitron();
            panel.addTitle("Purchasing information");
            panel.addPara("------------------------------------", 5f, g);
            for(CargoStackAPI stack: cargo.getStacksCopy()) {
                if(stack.isSpecialStack()) {
                    SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
                    if (spec != null && ba_bionicmanager.getBionic(spec.getId()) != null) {
                        ba_bionicitemplugin bionic = ba_bionicmanager.getBionic(spec.getId());
                        panel.addPara("- %s", 0f, t, bionic.displayColor, bionic.getName() + (stack.getSize() > 1? " ("+Math.round(stack.getSize())+")": ""));
                        i++;
                    }
                }
            }
            panel.addPara("------------------------------------", 0f, g);
            panel.addPara("Marked up: %s", 5f, bad, Math.round(markUp * 100)+ "%");
            panel.addPara("Total: %s", 5f, h, Misc.getDGSCredits(computeTotalCreditsNeeded(cargo)));
            panel.addPara("Available credits: %s", 5f, Misc.getPositiveHighlightColor(), Misc.getDGSCredits(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
            panel.setParaFontDefault();
            text.addTooltip();
        }
    }
    public void purchase() {
        CargoAPI toPurchaseCargo = null;
        if (memory.get(toPurchaseCargoMemKey) != null && memory.get(toPurchaseCargoMemKey) instanceof CargoAPI) {
            toPurchaseCargo = (CargoAPI) memory.get(toPurchaseCargoMemKey);
        }
        if(toPurchaseCargo == null) return;
        float availableCredits = Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        float neededCredits = computeTotalCreditsNeeded(toPurchaseCargo);
        if(availableCredits < neededCredits) return;

        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(neededCredits);
        CargoAPI clinicInv = null;
        if(memory.get(clinicShopMemKey) != null && memory.get(clinicShopMemKey) instanceof CargoAPI) {
            clinicInv = (CargoAPI) memory.get(clinicShopMemKey);
        }
        if(clinicInv != null) {
            for(CargoStackAPI stack :toPurchaseCargo.getStacksCopy()) {
                clinicInv.removeItems(CargoAPI.CargoItemType.SPECIAL,stack.getSpecialDataIfSpecial(),stack.getSize());
                ba_inventoryhandler.addToContainer(stack);
            }
            setShopInvMemKey(clinicInv, false);
            toPurchaseCargo.clear();
            memory.unset(toPurchaseCargoMemKey);
            text.addPara("\"Transaction successful, pleasure doing business with you.\"");
        }
        //todo: figure out a way to reset the clinic inv once a month
    }
    public void displayClinicInv() {
        getShopInv();
        final float width = 300f;
        dialog.showCargoPickerDialog("Clinic Shop", "Confirm", "Cancel", true, width, clinicShopInv, new CargoPickerListener() {
            @Override
            public void pickedCargo(CargoAPI cargo) {
                if (cargo.isEmpty()) {
                    cancelledCargoSelection();
                    return;
                }
                //display flavor text
                memory.set(toPurchaseCargoMemKey, cargo);
                FireBest.fire(null, dialog, memoryMap, "ba_ripperBionicConfirmPurchase");
            }

            @Override
            public void cancelledCargoSelection() {
                //display flavor text

            }

            @Override
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
                //display selected bionic name + price
                final float pad = 10f;
                float opad = 10f;
                Color h = Misc.getHighlightColor();
                Color bad = Misc.getNegativeHighlightColor();
                final Color t = Misc.getTextColor();
                final Color g = Misc.getGrayColor();

                panel.setParaOrbitronLarge();
                LabelAPI info = panel.addPara("%s", opad, h, "Select purchasing bionics");
                panel.setParaFontDefault();

                panel.beginGrid(width, 2, t);
                panel.addToGrid(0, 0, "Names", "Price");
                if(!cargo.isEmpty()) {
                    int i = 1;
                    int maxI = 18;
                    for(CargoStackAPI stack: cargo.getStacksCopy()) {
                        if(stack.isSpecialStack()) {
                            SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
                            if (spec != null && ba_bionicmanager.getBionic(spec.getId()) != null && i < maxI) {
                                ba_bionicitemplugin bionic = ba_bionicmanager.getBionic(spec.getId());
                                panel.setGridLabelColor(bionic.displayColor);
                                panel.addToGrid(0, i, bionic.getName() + (stack.getSize() > 1? " ("+Math.round(stack.getSize())+")": ""), "" + Misc.getDGSCredits(bionic.getSpec().getBasePrice()));
                                i++;
                            }
                        }

                        if(i == maxI) {
                            panel.setGridValueColor(Misc.getHighlightColor());
                            panel.setGridLabelColor(Misc.getHighlightColor());
                            panel.addToGrid(0, maxI, "...", "...");
                        }
                        panel.addToGrid(0, maxI + 1, "", "");
                        panel.setGridValueColor(Misc.getHighlightColor());
                        panel.setGridLabelColor(Misc.getHighlightColor());
                        panel.setParaOrbitronLarge();
                        panel.setGridLabelColor(Misc.getTextColor());
                        panel.addToGrid(0, maxI + 2, "Marked up", Math.round(markUp * 100)+ "%", bad);
                        panel.addToGrid(0, maxI + 3, "Total", Misc.getDGSCredits(computeTotalCreditsNeeded(cargo)), h);
                        panel.addToGrid(0, maxI + 4, "Available credits", Misc.getDGSCredits(Global.getSector().getPlayerFleet().getCargo().getCredits().get()), Misc.getPositiveHighlightColor());
                        panel.setParaFontDefault();
                    }
                } else {
                    panel.setGridValueColor(Misc.getGrayColor());
                    panel.setGridLabelColor(Misc.getGrayColor());
                    panel.addToGrid(0, 1, "Empty", "-");
//                    panel.addPara("%s", pad, Misc.getGrayColor().darker().darker(), "Empty");
                }
                panel.addGrid(pad);
            }
        });
    }
    public float computeTotalCreditsNeeded(CargoAPI cargo) {
        float credits = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if(stack.isSpecialStack()) {
                SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
                if (spec != null && ba_bionicmanager.getBionic(spec.getId()) != null) {
                    ba_bionicitemplugin bionic = ba_bionicmanager.getBionic(spec.getId());
                    credits += bionic.getSpec().getBasePrice() * stack.getSize();
                }
            }
        }
        credits *= markUp;
        return credits;
    }
    public void getShopInv() {
        if(clinicShopInv != null) clinicShopInv.clear();
        if(memory.get(clinicShopMemKey) != null && memory.get(clinicShopMemKey) instanceof CargoAPI) {
            CargoAPI clinicInv = (CargoAPI) memory.get(clinicShopMemKey);
            clinicShopInv.addAll(clinicInv);
            clinicShopInv.sort();
        } else {
            CargoAPI clinicInv = generateClinicShopInv();
            clinicShopInv.addAll(clinicInv);
            setShopInvMemKey(clinicInv, true);
            clinicShopInv.sort();
        }
    }
    public void setShopInvMemKey(CargoAPI cargo, boolean refreshTime) {
        float timeLeft = IN_STOCK_DAY;
        if(!refreshTime && memory.contains(clinicShopMemKey)) {
            timeLeft = memory.getExpire(clinicShopMemKey);
        }
        memory.set(clinicShopMemKey, cargo, timeLeft);
    }
    public CargoAPI generateClinicShopInv() {
        CargoAPI cargo = Global.getFactory().createCargo(true);
        Random rand = ba_utils.getRandom();
        HashMap<String, Integer> allowedBionicTag = new HashMap<>();
        allowedBionicTag.put("ba_bionic_t1", 100);
        allowedBionicTag.put("ba_bionic_t2", 50);
        allowedBionicTag.put("ba_bionic_bounty", 25);
        allowedBionicTag.put("ba_bionic_t3", 1);
        WeightedRandomPicker<String> allowedBionics = new WeightedRandomPicker<>(rand);
        for (String tag: allowedBionicTag.keySet()) {
            List<String> bionicIds = ba_bionicmanager.getListBionicsIdFromTag(tag);
            int dropWeight = allowedBionicTag.get(tag);
            for (String id: bionicIds) {
                allowedBionics.add(id, dropWeight);
            }
        }

        int maxPicked = 10;
        int minPicked = 5;
        int maxQuantity = 2;
        int minQuantity = 1;
        int actualPicked = MathUtils.getRandomNumberInRange(minPicked, maxPicked);
        int added = 0;
        while (added < actualPicked) {
            String bionicId = allowedBionics.pick();
            ba_bionicitemplugin bionic = ba_bionicmanager.getBionic(bionicId);
            if(bionic != null) {
                SpecialItemData specialItem = new SpecialItemData(bionicId, null);

                if(ba_overclockmanager.isBionicOverclockable(bionic)) {
                    WeightedRandomPicker<String> randomPickerOverclock = new WeightedRandomPicker<>(rand);
                    for(String overclockId: bionic.overclockList) {
                        randomPickerOverclock.add(overclockId, 50);
                    }
                    randomPickerOverclock.add("NONE", 100);
                    String pickedOverclock = randomPickerOverclock.pick();
                    if(!Objects.equals(pickedOverclock, "NONE")) {
                        //require special item data to do the overclock things
                        specialItem =  new SpecialItemData(bionic.bionicId, pickedOverclock);
                    }
                }

                cargo.addSpecial(specialItem, MathUtils.getRandomNumberInRange(minQuantity, maxQuantity));
                added++;
            }
        }

        return cargo;
    }
}

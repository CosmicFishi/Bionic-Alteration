package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.overclock.ba_overclock;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.util.*;

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
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        switch (arg) {
            case "displayClinic":
                displayClinicInv();
                break;
        }
        return false;
    }
    public void displayClinicInv() {
        getShopInv();
        dialog.showCargoPickerDialog("Clinic Shop", "Confirm Purchase", "Cancel", false, 200f, 700f, 400f, clinicShopInv, new CargoPickerListener() {
            @Override
            public void pickedCargo(CargoAPI cargo) {
                //remove player cash
                //display flavor text
            }

            @Override
            public void cancelledCargoSelection() {
                //display flavor text
            }

            @Override
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
                //display selected bionic name + price
            }
        });
    }
    public void getShopInv() {
        clinicShopInv.clear();
        if(memory.get(clinicShopMemKey) != null && memory.get(clinicShopMemKey) instanceof CargoAPI) {
            CargoAPI clinicInv = (CargoAPI) memory.get(clinicShopMemKey);
            clinicShopInv.addAll(clinicInv);
        } else {
            CargoAPI clinicInv = generateClinicShopInv();
            clinicShopInv.addAll(clinicInv);
            memory.set(clinicShopMemKey, clinicInv);
        }
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
        int maxQuantity = 3;
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

                clinicShopInv.addSpecial(specialItem, MathUtils.getRandomNumberInRange(minQuantity, maxQuantity));
                added++;
            }
        }

        return cargo;
    }
}

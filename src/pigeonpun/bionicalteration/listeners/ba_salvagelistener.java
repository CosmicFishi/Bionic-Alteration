package pigeonpun.bionicalteration.listeners;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageEntityGeneratorOld;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//Credits to President Matt Damon since I copied some part of their codes.
public class ba_salvagelistener implements ShowLootListener {
    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        if(dialog.getInteractionTarget() == null) return;
        if(dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
            CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
            if(fleet.getMemoryWithoutUpdate().contains("$ba_bionic_dropList")) {
                List<ba_bionicitemplugin> bionicDrop = (List<ba_bionicitemplugin>) fleet.getMemoryWithoutUpdate().get("$ba_bionic_dropList");
                Random rand = Misc.getRandom(bionicalterationplugin.getSectorSeed().hashCode(), 100);
                for (ba_bionicitemplugin bionic: bionicDrop) {
                    if(bionic.dropChance > 0 && rand.nextFloat() <= bionic.dropChance) {
                        loot.addSpecial(new SpecialItemData(bionic.bionicId, null), 1);
                    }
                }
            }
        }
        //todo: test this further
        List<SalvageEntityGenDataSpec.DropData> dropData = getDropDataFromEntity(dialog.getInteractionTarget());

        MemoryAPI memory = dialog.getInteractionTarget().getMemoryWithoutUpdate();
        long randomSeed = memory.getLong(MemFlags.SALVAGE_SEED);
        Random random = Misc.getRandom(randomSeed, 100);

        List<SalvageEntityGenDataSpec.DropData> dropValue = generateDropValueList(dropData);
        List<SalvageEntityGenDataSpec.DropData> dropRandom = generateDropRandomList(dropData);

        CargoAPI salvage = SalvageEntity.generateSalvage(random,
                1f, 1f, 1f, 1f, dropValue, dropRandom);
        loot.addAll(salvage);
    }

    /**
     * To create drop value for the custom drop group: ba_bionic_civil, ba_bionic_military...
     * @param dropData
     * @return
     */
    private List<SalvageEntityGenDataSpec.DropData> generateDropValueList(List<SalvageEntityGenDataSpec.DropData> dropData) {
        List<SalvageEntityGenDataSpec.DropData> dropValueList = new ArrayList<>();
        for(SalvageEntityGenDataSpec.DropData d: dropData) {
            if(d.group == null) continue;
            if(d.value == -1) continue;
            int value = -1;
            if(d.group.contains("rare_tech")) {
                value = (int) (d.value * 0.2f);
            }
            if(d.group.contains("goods")) {
                value = (int) (d.value * 0.05f);
            }
            if(d.group.contains("supply")) {
                value = (int) (d.value * 0.08f);
            }
            if(d.group.contains("machinery")) {
                value = (int) (d.value * 0.14f);
            }
            if(d.group.contains("freighter_cargo")) {
                value = (int) (d.value * 0.1f);
            }
            if(value != -1) {
                SalvageEntityGenDataSpec.DropData civilDropValue = new SalvageEntityGenDataSpec.DropData();
                civilDropValue.group = "ba_bionic_civil";
                civilDropValue.valueMult = d.valueMult;
                civilDropValue.value = value;
                dropValueList.add(civilDropValue);

                SalvageEntityGenDataSpec.DropData militaryDropValue = new SalvageEntityGenDataSpec.DropData();
                militaryDropValue.group = "ba_bionic_civil";
                militaryDropValue.valueMult = d.valueMult;
                militaryDropValue.value = value;
                dropValueList.add(militaryDropValue);
            }
        }

        return dropValueList;
    }
    /**
     * To create drop random for the custom drop group: ba_bionic_civil, ba_bionic_military...
     * @param dropData
     * @return
     */
    private List<SalvageEntityGenDataSpec.DropData> generateDropRandomList(List<SalvageEntityGenDataSpec.DropData> dropData) {
        List<SalvageEntityGenDataSpec.DropData> dropRandomList = new ArrayList<>();
        for(SalvageEntityGenDataSpec.DropData d: dropData) {
            if(d.group == null) continue;
            if(d.chances == -1) continue;
            int chances = -1;
            if(d.group.contains("rare_tech")) {
                chances = (int) (d.chances * 1f);
            }
            if(d.group.contains("goods")) {
                chances = (int) (d.chances * 0.2f);
            }
            if(d.group.contains("supply")) {
                chances = (int) (d.chances * 0.6f);
            }
            if(d.group.contains("machinery")) {
                chances = (int) (d.chances * 0.8f);
            }
            if(d.group.contains("freighter_cargo")) {
                chances = (int) (d.chances * 0.4f);
            }
            if(chances != -1) {
                SalvageEntityGenDataSpec.DropData civilDropValue = new SalvageEntityGenDataSpec.DropData();
                civilDropValue.group = "ba_bionic_civil";
                civilDropValue.maxChances = d.maxChances;
                civilDropValue.chances = chances;
                dropRandomList.add(civilDropValue);

                SalvageEntityGenDataSpec.DropData militaryDropValue = new SalvageEntityGenDataSpec.DropData();
                militaryDropValue.group = "ba_bionic_civil";
                militaryDropValue.maxChances = d.maxChances;
                militaryDropValue.chances = chances;
                dropRandomList.add(militaryDropValue);
            }
        }

        return dropRandomList;
    }

    private static List<SalvageEntityGenDataSpec.DropData> getDropDataFromEntity(SectorEntityToken entity) {
        List<SalvageEntityGenDataSpec.DropData> dropData = new ArrayList<>();

        //first get drops assigned directly to entity
        if (entity.getDropRandom() != null) {
            dropData.addAll(entity.getDropRandom());
        }

        if (entity.getDropValue() != null) {
            dropData.addAll(entity.getDropValue());
        }

        //then try to get spec from entity and the spec's drops
        String specId = entity.getCustomEntityType();
        if (specId == null || entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPEC_ID_OVERRIDE)) {
            specId = entity.getMemoryWithoutUpdate().getString(MemFlags.SALVAGE_SPEC_ID_OVERRIDE);
        }

        if (specId != null
                && SalvageEntityGeneratorOld.hasSalvageSpec(specId)) {
            SalvageEntityGenDataSpec spec = SalvageEntityGeneratorOld.getSalvageSpec(specId);

            //get drop randoms from that spec
            if (spec != null && spec.getDropRandom() != null) {
                dropData.addAll(spec.getDropRandom());
            }

            if (spec != null && spec.getDropValue() != null) {
                dropData.addAll(spec.getDropValue());
            }
        }

        return dropData;
    }
}

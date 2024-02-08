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
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//Credits to President Matt Damon since I copied some part of their codes.
public class ba_salvagelistener implements ShowLootListener {
    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        //todo: salvage spawning
        if(dialog.getInteractionTarget() == null) return;
        if(dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
            CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
            List<ba_officermanager.ba_bionicDropPotentialData> bionicDrop = ba_officermanager.getListPotentialBionicDrop(fleet);
            Random rand = Misc.getRandom(bionicalterationplugin.getSectorSeed().hashCode(), 100);
            //todo: get the officer bionics and somehow setup the bionic salvage

            //todo: test this
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
            if(d.group.equals("rare_tech")) {
                value = (int) (d.value * 0.2f);
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
            if(d.group.equals("rare_tech")) {
                chances = (int) (d.chances * 1f);
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

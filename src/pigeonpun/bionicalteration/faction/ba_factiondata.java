package pigeonpun.bionicalteration.faction;

import pigeonpun.bionicalteration.variant.ba_variant;

import java.util.ArrayList;
import java.util.List;

public class ba_factiondata {
    public String factionId;
    public List<ba_factionVariantDetails> variantDetails = new ArrayList<>();
    public List<String> bionicUseTagsList = new ArrayList<>();
    public List<String> bionicUseIdsList = new ArrayList<>();
    public float targetBRMLevel;
    public float targetConsciousLevel;
    public ba_factiondata(String factionId, List<ba_factionVariantDetails> variantDetails, List<String> bionicUseTagsList, List<String> bionicUseIdsList,float targetBRMLevel, float targetConsciousLevel) {
        this.factionId = factionId;
        this.targetBRMLevel = targetBRMLevel;
        this.targetConsciousLevel = targetConsciousLevel;
        if(bionicUseTagsList != null) this.bionicUseTagsList = bionicUseTagsList;
        if(bionicUseIdsList != null) this.bionicUseIdsList = bionicUseIdsList;
        if(variantDetails != null) this.variantDetails = variantDetails;
    }
    public static class ba_factionVariantDetails {
        public ba_variant variant;
        public float variantSpawnChance;
        public List<String> bionicUseTagsOverride = new ArrayList<>();
        public List<String> bionicUseIdsOverride =new ArrayList<>();
        /**
         * @param variant
         * @param variantSpawnChance
         * @param bionicUseTagsOverride can be null. List bionic tags that only this variant use. Will override and replace the faction bionicUse list for this variant.
         * @param bionicUseIdsOverride can be null. List bionic ID that only this variant use. Will override and replace the faction bionicUse list for this variant.
         */
        public ba_factionVariantDetails(ba_variant variant, float variantSpawnChance, List<String> bionicUseTagsOverride, List<String> bionicUseIdsOverride) {
            this.variant = variant;
            this.variantSpawnChance = variantSpawnChance;
            if(bionicUseTagsOverride != null) this.bionicUseTagsOverride = bionicUseTagsOverride;
            if(bionicUseIdsOverride != null) this.bionicUseIdsOverride = bionicUseIdsOverride;
        }
    }
    public static class ba_bionicUseDetails {

    }
}

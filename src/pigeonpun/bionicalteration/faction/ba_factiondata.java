package pigeonpun.bionicalteration.faction;

import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.variant.ba_variant;

import java.util.ArrayList;
import java.util.List;

public class ba_factiondata {
    public String factionId;
    public List<ba_factionVariantDetails> variantDetails = new ArrayList<>();
    public List<ba_bionicUseTagDetails> bionicUseTagsList = new ArrayList<>();
    public List<ba_bionicUseIdDetails> bionicUseIdsList = new ArrayList<>();
    public float targetBRMLevel;
    public float targetConsciousLevel;
    public float maxBionicUseCount = -1;

    /**
     * @param factionId
     * @param variantDetails
     * @param bionicUseTagsList
     * @param bionicUseIdsList
     * @param targetBRMLevel
     * @param targetConsciousLevel
     * @param maxBionicUseCount -1 for unused, >0 for defining max bionic use for the faction
     */
    public ba_factiondata(String factionId, List<ba_factionVariantDetails> variantDetails, List<ba_bionicUseTagDetails> bionicUseTagsList, List<ba_bionicUseIdDetails> bionicUseIdsList,float targetBRMLevel, float targetConsciousLevel, float maxBionicUseCount) {
        this.factionId = factionId;
        this.targetBRMLevel = targetBRMLevel;
        this.targetConsciousLevel = targetConsciousLevel;
        if(bionicUseTagsList != null) this.bionicUseTagsList = bionicUseTagsList;
        if(bionicUseIdsList != null) this.bionicUseIdsList = bionicUseIdsList;
        if(variantDetails != null) this.variantDetails = variantDetails;
        if(maxBionicUseCount > 0) this.maxBionicUseCount = maxBionicUseCount;
    }
    public static class ba_factionVariantDetails {
        public ba_variant variant;
        public float variantSpawnWeight;
        public List<ba_bionicUseTagDetails> bionicUseTagsOverride = new ArrayList<>();
        public List<ba_bionicUseIdDetails> bionicUseIdsOverride =new ArrayList<>();
        /**
         * @param variant
         * @param variantSpawnWeight
         * @param bionicUseTagsOverride can be null. List bionic tags that only this variant use. Will override and replace the faction bionicUse list for this variant.
         * @param bionicUseIdsOverride can be null. List bionic ID that only this variant use. Will override and replace the faction bionicUse list for this variant.
         */
        public ba_factionVariantDetails(ba_variant variant, float variantSpawnWeight, List<ba_bionicUseTagDetails> bionicUseTagsOverride, List<ba_bionicUseIdDetails> bionicUseIdsOverride) {
            this.variant = variant;
            this.variantSpawnWeight = variantSpawnWeight;
            if(bionicUseTagsOverride != null) this.bionicUseTagsOverride = bionicUseTagsOverride;
            if(bionicUseIdsOverride != null) this.bionicUseIdsOverride = bionicUseIdsOverride;
        }
    }
    public static class ba_bionicUseTagDetails {
        public String tag;
        public float spawnWeight;
        public ba_bionicUseTagDetails(String tag, float spawnWeight) {
            this.tag = tag;
            this.spawnWeight = spawnWeight;
        }
    }
    public static class ba_bionicUseIdDetails {
        public ba_bionicitemplugin bionic;
        public float spawnWeight;
        public ba_bionicUseIdDetails(ba_bionicitemplugin bionic, float spawnWeight) {
            this.bionic = bionic;
            this.spawnWeight = spawnWeight;
        }
    }
}

package pigeonpun.bionicalteration.variant;

import java.util.ArrayList;
import java.util.List;

public class ba_factionvariant {
    public String variantId;
    public float variantSpawnChance;
    public List<String> knownBionicTagList = new ArrayList<>();
    public List<String> knownBionicIdList =new ArrayList<>();
    public ba_factionvariant(String variantId, float variantSpawnChance, List<String> knownBionicTagList, List<String> knownBionicIdList) {
        this.variantId = variantId;
        this.variantSpawnChance = variantSpawnChance;
        if(knownBionicTagList != null) this.knownBionicTagList = knownBionicTagList;
        if(knownBionicIdList != null) this.knownBionicIdList = knownBionicIdList;
    }
}

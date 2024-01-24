package pigeonpun.bionicalteration.variant;

import java.util.ArrayList;
import java.util.List;

public class ba_variant {
    public String variantId;
    public String name;
    public List<String> limbIdList = new ArrayList<>();
    /**
     * Current have no usage but will expand in the future, allowing the player to select what variant they are.
     */
    public boolean isAllowPlayerChangeTo;
    public ba_variant(String variantId, String name, List<String> limbIdList, boolean isAllowPlayerChangeTo) {
        this.variantId = variantId;
        this.name = name;
        if(limbIdList != null) this.limbIdList = limbIdList;
        this.isAllowPlayerChangeTo = isAllowPlayerChangeTo;
    }
}

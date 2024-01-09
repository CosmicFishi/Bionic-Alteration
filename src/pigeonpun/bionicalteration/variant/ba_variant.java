package pigeonpun.bionicalteration.variant;

import java.util.ArrayList;
import java.util.List;

public class ba_variant {
    public String name;
    public List<String> limbIdList = new ArrayList<>();
    public ba_variant(String name, List<String> limbIdList) {
        this.name = name;
        if(limbIdList != null) this.limbIdList = limbIdList;
    }
}

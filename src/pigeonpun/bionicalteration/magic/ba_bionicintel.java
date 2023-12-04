package pigeonpun.bionicalteration.magic;

import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ba_bionicintel extends BaseIntelPlugin {
//    @Override
    public void createLargeDescriptionImpl(@NotNull CustomPanelAPI panel, float width, float height) {
        float opad = 10;
        float pad = 3;
        int headerHeight = 40;
        //create the tooltip container for the header panel
        TooltipMakerAPI headerTooltip = panel.createUIElement(width, height, false);

        //create the inside sub panel
        CustomPanelAPI headerSubPanel = panel.createCustomPanel(width, headerHeight, null);
        TooltipMakerAPI headerSubTooltip = headerSubPanel.createUIElement(width, headerHeight,false);
        headerSubTooltip.addPara("erm idk", pad);

        //add the sub tooltip into sub panel
        headerSubPanel.addUIElement(headerTooltip);
        //add the sub panel into the big panel
        headerTooltip.addCustom(headerSubPanel, 0);

    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        float opad = 10;
        float pad = 3;
        int headerHeight = 40;
        //create the tooltip container for the header panel
        TooltipMakerAPI headerTooltip = panel.createUIElement(width, height, false);

        //create the inside sub panel
        CustomPanelAPI headerSubPanel = panel.createCustomPanel(width, headerHeight, null);
        TooltipMakerAPI headerSubTooltip = headerSubPanel.createUIElement(width, headerHeight,false);
        headerSubTooltip.addPara("erm idk", pad);

        //add the sub tooltip into sub panel
        headerSubPanel.addUIElement(headerTooltip);
        //add the sub panel into the big panel
        headerTooltip.addCustom(headerSubPanel, 0);

    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("Personal");
        return tags;
    }

    @Override
    public boolean hasLargeDescription() {
        return true;
    }

    @Override
    public boolean hasSmallDescription() {
        return false;
    }

    @Override
    public boolean isEnding() {
        return false;
    }

    @Override
    public boolean isEnded() {
        return false;
    }
}

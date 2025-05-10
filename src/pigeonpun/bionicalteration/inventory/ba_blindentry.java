package pigeonpun.bionicalteration.inventory;

import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ba_blindentry extends BaseSpecialItemPlugin {

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        if(stack != null && stack.getSpecialItemSpecIfSpecial() != null) {
            String data = String.valueOf(stack.getSpecialDataIfSpecial());
            tooltip.addPara(data, Misc.getDarkPlayerColor(), 5f);
        }
        super.createTooltip(tooltip, expanded, transferHandler, stackSource);
    }
}

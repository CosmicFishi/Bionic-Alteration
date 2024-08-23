package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ui.overclock.ba_delegate;
import pigeonpun.bionicalteration.ui.overclock.ba_uiplugin;

import java.util.List;
import java.util.Map;

public class ba_displayOverclockUI extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        SectorEntityToken target = dialog.getInteractionTarget();
        if (target == null) return false;
        InteractionDialogPlugin plugin = dialog.getPlugin();
        dialog.showCustomVisualDialog(ba_uiplugin.MAIN_CONTAINER_WIDTH, ba_uiplugin.MAIN_CONTAINER_HEIGHT, new ba_delegate(ba_uiplugin.createDefault(), dialog));
        return true;
    }
}

package pigeonpun.bionicalteration.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;

public class ba_addAllBionics implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        //todo: test this
        for(ba_bionicitemplugin bionic: ba_bionicmanager.bionicItemMap.values()) {
            SpecialItemData specialItem = new SpecialItemData(bionic.bionicId, null);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(specialItem, 1);
        }
        return CommandResult.SUCCESS;
    }
}

package pigeonpun.bionicalteration.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import java.util.List;

public class ba_findBionicStations implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        boolean found = false;
        Console.showMessage("Finding Bionic Research Stations...");
        for (StarSystemAPI systemAPI: Global.getSector().getStarSystems()) {
            List<SectorEntityToken> listEntities = systemAPI.getAllEntities();
            for( SectorEntityToken entity: listEntities) {
                if(entity.getTags() != null &&  entity.getTags().contains("ba_overclock_station") && entity.getStarSystem() != null) {
                    Console.showMessage("- At " + entity.getStarSystem().getName());
                    found = true;
                }
            }
        }
        if(!found) {
            Console.showMessage("Can't find bionic station at (very weird, please contact the author (PigeonPun) :(");
        }
        return null;
    }
}

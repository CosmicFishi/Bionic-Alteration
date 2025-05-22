package pigeonpun.bionicalteration.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.lazywizard.lazylib.MathUtils;
import org.magiclib.campaign.MagicCaptainBuilder;
import org.magiclib.util.MagicCampaign;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.util.Random;

/**
 * Copy a bit of code from AddShip from ConsoleCommand. Ty LazyWizard
 * ba_spawnTestShip <pristine || corrupted>
 */
public class ba_spawnTestShip implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!context.isInCampaign())
        {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
        String shellHullmod = "";

        String[] tmp = args.split(" ");

        if (tmp.length == 1)
        {
            if(tmp[0].equals("corrupted")) {
                shellHullmod = "ba_corruptedshell";
            }
            if(tmp[0].equals("pristine")) {
                shellHullmod = "ba_pristineshell";
            }
        }
        Random r = new Random();
        WeightedRandomPicker<String> randomPicker = new WeightedRandomPicker<>(r);
        randomPicker.addAll(Global.getSector().getFaction(Factions.REMNANTS).getKnownShips());
        String pickedShipId = randomPicker.pick();
        String variant = null;
        FleetMemberAPI ship;
        // Test for variants
        for (String id : Global.getSettings().getAllVariantIds())
        {
            if (pickedShipId.equalsIgnoreCase(id))
            {
                variant = id;
                break;
            }
        }

        // Test for empty hulls
        if (variant == null)
        {
            final String withHull = pickedShipId + "_Hull";
            for (String id : Global.getSettings().getAllVariantIds())
            {
                if (withHull.equalsIgnoreCase(id))
                {
                    variant = id;
                    break;
                }
            }
        }

        // Before we give up, maybe the .variant file doesn't match the ID?
        if (variant == null)
        {
            try
            {
                variant = Global.getSettings().loadJSON("data/variants/"
                        + tmp[0] + ".variant").getString("variantId");
                Console.showMessage("Warning: variant ID doesn't match"
                        + " .variant filename!", Level.WARN);
            }
            catch (Exception ex)
            {
                Console.showMessage("No ship found with id '" + tmp[0]
                        + "'! Use 'list ships' for a complete list of valid ids.");
                return CommandResult.ERROR;
            }
        }
        try
        {
            ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
        }
        catch (Exception ex)
        {
            Console.showException("Failed to create variant '" + variant + "'!", ex);
            return CommandResult.ERROR;
        }
        ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant);
        FleetEncounterContext.prepareShipForRecovery(ship,
                true, true, true,1f, 1f, MathUtils.getRandom());
        Global.getSector().getPlayerFleet().getFleetData().addFleetMember(ship);
        if((shellHullmod.equals("ba_corruptedshell") || shellHullmod.equals("ba_pristineshell"))) {
            ship.getVariant().addPermaMod(shellHullmod);
        }
        ship.setCaptain(MagicCampaign.createCaptainBuilder(Factions.REMNANTS).setIsAI(true).setAICoreType(Commodities.ALPHA_CORE).create());
        Console.showMessage("Added 1 ship "
                + ship.getSpecId() + " to player fleet.");
        return CommandResult.SUCCESS;
    }
}

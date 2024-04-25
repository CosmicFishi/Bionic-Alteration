package pigeonpun.bionicalteration.commands;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SleeperPodsSpecial;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

import java.util.Random;

public class ba_addCryoPerson implements BaseCommand{
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
        String[] tmp = args.split(" ");
        if (tmp.length == 0 || tmp[0].isEmpty())
            return CommandResult.BAD_SYNTAX;

        if (!tmp[0].equals("admin") && !tmp[0].equals("officer")) {
            Console.showMessage("Use \"admin\" or  \"officer\"");
            return CommandResult.ERROR;
        }

        Random random = new Random();

        DerelictShipEntityPlugin.DerelictShipData params = DerelictShipEntityPlugin.createRandom(Factions.INDEPENDENT, null, null, DerelictShipEntityPlugin.getDefaultSModProb());
        CustomCampaignEntityAPI derelict = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
                Global.getSector().getPlayerFleet().getStarSystem(), Entities.WRECK, Factions.NEUTRAL, params);
        derelict.addTag(Tags.EXPIRES);
        derelict.setLocation(Global.getSector().getCurrentLocation().getLocation().x, Global.getSector().getCurrentLocation().getLocation().y);
        float radius = 400f + 400f * (float) Math.random();
        float maxRadius = Global.getSector().getPlayerFleet().getStarSystem().getStar().getRadius() + 400f;
        if (radius > maxRadius) radius = maxRadius;

        float orbitDays = radius / (5f + Misc.random.nextFloat() * 20f);
        float angle = (float) Math.random() * 360f;
        derelict.setCircularOrbit(Global.getSector().getPlayerFleet().getStarSystem().getStar(), angle, radius, orbitDays);

        WeightedRandomPicker officerFactions = new WeightedRandomPicker<>();
        officerFactions.add(Factions.INDEPENDENT);
        SalvageSpecialAssigner.SpecialCreator pick = null;
        if(tmp[0].equals("admin")) {
            pick = new SalvageSpecialAssigner.SleeperPodsSpecialCreator(random, SleeperPodsSpecial.SleeperSpecialType.ADMIN, 2, 2, officerFactions);
        }
        if(tmp[0].equals("officer")) {
            pick = new SalvageSpecialAssigner.SleeperPodsSpecialCreator(random, SleeperPodsSpecial.SleeperSpecialType.OFFICER, SalvageSpecialAssigner.STANDARD_PODS_OFFICER_LEVEL, SalvageSpecialAssigner.EXCEPTIONAL_PODS_OFFICER_LEVEL, officerFactions);
        }
        SalvageSpecialAssigner.SpecialCreationContext context2 = new SalvageSpecialAssigner.SpecialCreationContext();

        if(pick != null) {
            Object specialData = pick.createSpecial(derelict, context2);
            if (specialData != null) {
                Misc.setSalvageSpecial(derelict, specialData);
                Console.showMessage("Cryopod spawned around the center of the star");
            }
        }
        return CommandResult.SUCCESS;
    }
}

package pigeonpun.bionicalteration.commands;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import pigeonpun.bionicalteration.ba_variablemanager;

import java.util.HashSet;

public class clearDuplicateCondition implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
        String[] tmp = args.split(" ");
        if (tmp.length == 0 || tmp[0].isEmpty())
            return CommandResult.BAD_SYNTAX;

        MarketAPI selectedMarket = null;
        for(MarketAPI market: Misc.getPlayerMarkets(false)) {
            if(market.getName().equalsIgnoreCase(tmp[0])) {
                selectedMarket = market;
                break;
            }
        }
        if(selectedMarket == null) {
            Console.showMessage("Can't find market of name: " + tmp[0]);
            return CommandResult.ERROR;
        }

        for(String key: new HashSet<>(selectedMarket.getIncomeMult().getMultMods().keySet())) {
            if(key.contains(ba_variablemanager.BA_BIONIC_SKILL_ID)) {
                selectedMarket.getIncomeMult().unmodify(key);
            }
        }
        for(String key: new HashSet<>(selectedMarket.getUpkeepMult().getMultMods().keySet())) {
            if(key.contains(ba_variablemanager.BA_BIONIC_SKILL_ID)) {
                selectedMarket.getUpkeepMult().unmodify(key);
            }
        }
        for(String key: new HashSet<>(selectedMarket.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).getMultBonuses().keySet())) {
            if(key.contains(ba_variablemanager.BA_BIONIC_SKILL_ID)) {
                selectedMarket.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(key);
            }
        }
        for(String key: new HashSet<>(selectedMarket.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).getMultBonuses().keySet())) {
            if(key.contains(ba_variablemanager.BA_BIONIC_SKILL_ID)) {
                selectedMarket.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(key);
            }
        }
        for(String key: new HashSet<>(selectedMarket.getAccessibilityMod().getFlatBonuses().keySet())) {
            if(key.contains(ba_variablemanager.BA_BIONIC_SKILL_ID)) {
                selectedMarket.getAccessibilityMod().unmodifyFlat(key);
            }
        }
        for(String key: new HashSet<>(selectedMarket.getStability().getFlatMods().keySet())) {
            if(key.contains(ba_variablemanager.BA_BIONIC_SKILL_ID)) {
                selectedMarket.getStability().unmodifyFlat(key);
            }
        }
        return CommandResult.SUCCESS;
    }
}

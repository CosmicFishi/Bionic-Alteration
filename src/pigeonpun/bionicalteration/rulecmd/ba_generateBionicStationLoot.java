package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageEntityGeneratorOld;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity.computeRequiredToSalvage;
import static com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity.getPlayerShipsSalvageModUncapped;

public class ba_generateBionicStationLoot extends BaseCommandPlugin {

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected SalvageEntityGenDataSpec spec;
    protected CargoAPI cargo;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    private DebrisFieldTerrainPlugin debris;
    private Map<String, MemoryAPI> memoryMap;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();

        String specId = entity.getCustomEntityType();
        if (specId == null || entity.getMemoryWithoutUpdate().contains(MemFlags.SALVAGE_SPEC_ID_OVERRIDE)) {
            specId = entity.getMemoryWithoutUpdate().getString(MemFlags.SALVAGE_SPEC_ID_OVERRIDE);
        }
        spec = SalvageEntityGeneratorOld.getSalvageSpec(specId);

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        cargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        if (command.equals("performSalvage")) {
            performSalvage();
        }
        return false;
    }
    public void performSalvage() {
        long seed = memory.getLong(MemFlags.SALVAGE_SEED);
        Random random = Misc.getRandom(seed, 100);

        Misc.stopPlayerFleet();

        MutableStat valueRecovery = getValueRecoveryStat(true);
        float valueMultFleet = valueRecovery.getModifiedValue();
        float rareItemSkillMult = playerFleet.getStats().getDynamic().getValue(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE);

        List<SalvageEntityGenDataSpec.DropData> dropValue = new ArrayList<SalvageEntityGenDataSpec.DropData>(spec.getDropValue());
        List<SalvageEntityGenDataSpec.DropData> dropRandom = new ArrayList<SalvageEntityGenDataSpec.DropData>(spec.getDropRandom());
        dropValue.addAll(entity.getDropValue());
        dropRandom.addAll(entity.getDropRandom());


        float overallMult = 1f;
        if (debris != null) {
            // to avoid same special triggering over and over while scavenging through
            // the same debris field repeatedly
            BaseCommandPlugin.getEntityMemory(memoryMap).unset(MemFlags.SALVAGE_SPECIAL_DATA);
        }

        float fuelMult = playerFleet.getStats().getDynamic().getValue(Stats.FUEL_SALVAGE_VALUE_MULT_FLEET);
        CargoAPI salvage = SalvageEntity.generateSalvage(random, valueMultFleet, rareItemSkillMult, overallMult, fuelMult, dropValue, dropRandom);

        //ExtraSalvage extra = BaseSalvageSpecial.getExtraSalvage(memoryMap);
        CargoAPI extra = BaseSalvageSpecial.getCombinedExtraSalvage(memoryMap);
        salvage.addAll(extra);
        BaseSalvageSpecial.clearExtraSalvage(memoryMap);
        if (!extra.isEmpty()) {
            ListenerUtil.reportExtraSalvageShown(entity);
        }

        //if (loot)
        if (!salvage.isEmpty()) {
            dialog.getVisualPanel().showLoot("Salvaged", salvage, false, true, true, new CoreInteractionListener() {
                public void coreUIDismissed() {
                    long xp = 0;
                    if (entity.hasSalvageXP()) {
                        xp = (long) (float) entity.getSalvageXP();
                    } else if (spec != null && spec.getXpSalvage() > 0) {
                        xp = (long) spec.getXpSalvage();
                    }
//                    if (!memory.contains("$doNotDismissDialogAfterSalvage")) {
//                        dialog.dismiss();
//                        dialog.hideTextPanel();
//                        dialog.hideVisualPanel();
//
//                        if (xp > 0) {
//                            Global.getSector().getPlayerPerson().getStats().addXP(xp);
//                        }
//                    } else {
//
//                    }
                    if (xp > 0) {
                        Global.getSector().getPlayerPerson().getStats().addXP(xp, dialog.getTextPanel());
                    }
                }
            });
//            options.clearOptions();
//            dialog.setPromptText("");
        } else {
            text.addParagraph("Operations conclude with nothing of value found.");
//            options.clearOptions();
//            String leave = "Leave";
//            if (memory.contains("$salvageLeaveText")) {
//                leave = memory.getString("$salvageLeaveText");
//            }
//            options.addOption(leave, "defaultLeave");
//            options.setShortcut("defaultLeave", Keyboard.KEY_ESCAPE, false, false, false, true);
        }
    }
    protected MutableStat getValueRecoveryStat(boolean withSkillMultForRares) {
        Map<String, Integer> requiredRes = computeRequiredToSalvage(entity);
        MutableStat valueRecovery = new MutableStat(1f);
        int i = 0;

        float machineryContrib = 0.75f;
        valueRecovery.modifyPercent("base", -100f);
        if (machineryContrib < 1f) {
            valueRecovery.modifyPercent("base_positive", (int) Math.round(100f - 100f * machineryContrib), "Base effectiveness");
        }
        //valueRecovery.modifyPercent("base", -75f);

        float per = 0.5f;
        per = 1f;
        for (String commodityId : requiredRes.keySet()) {
            float required = requiredRes.get(commodityId);
            float available = (int) cargo.getCommodityQuantity(commodityId);
            if (required <= 0) continue;
            CommoditySpecAPI spec = Global.getSector().getEconomy().getCommoditySpec(commodityId);

            float val = Math.min(available / required, 1f) * per;
            int percent = (int) Math.round(val * 100f);
            //valueRecovery.modifyPercent("" + i++, percent, Misc.ucFirst(spec.getLowerCaseName()) + " requirements met");
            if (Commodities.HEAVY_MACHINERY.equals(commodityId)) {
                val = Math.min(available / required, machineryContrib) * per;
                percent = (int) Math.round(val * 100f);
                valueRecovery.modifyPercentAlways("" + i++, percent, Misc.ucFirst(spec.getLowerCaseName()) + " available");
            } else {
                valueRecovery.modifyMultAlways("" + i++, val, Misc.ucFirst(spec.getLowerCaseName()) + " available");
            }
//			float val = Math.max(1f - available / required, 0f) * per;
//			int percent = -1 * (int) Math.round(val * 100f);
//			valueRecovery.modifyPercent("" + i++, percent, "Insufficient " + spec.getLowerCaseName());
        }

        boolean modified = false;
        if (withSkillMultForRares) {
            for (MutableStat.StatMod mod : playerFleet.getStats().getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_INCLUDES_RARE).getFlatMods().values()) {
                modified = true;
                valueRecovery.modifyPercentAlways("" + i++, (int) Math.round(mod.value * 100f), mod.desc);
            }
        }

        {
            for (MutableStat.StatMod mod : playerFleet.getStats().getDynamic().getStat(Stats.SALVAGE_VALUE_MULT_FLEET_NOT_RARE).getFlatMods().values()) {
                modified = true;
                valueRecovery.modifyPercentAlways("" + i++, (int) Math.round(mod.value * 100f), mod.desc);
            }
        }
        if (!modified) {
            valueRecovery.modifyPercentAlways("" + i++, (int) Math.round(0f), "Salvaging skill");
        }

        float fleetSalvageShips = getPlayerShipsSalvageModUncapped();
        valueRecovery.modifyPercentAlways("" + i++, (int) Math.round(fleetSalvageShips * 100f), "Fleetwide salvaging capability");

        return valueRecovery;
    }
}

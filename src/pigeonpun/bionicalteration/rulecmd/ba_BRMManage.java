package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.utilities.StringHelper;
import org.lwjgl.input.Keyboard;
import pigeonpun.bionicalteration.ba_marketmanager;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.inventory.ba_inventoryhandler;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ba_BRMManage extends PaginatedOptions {
    public static final String BRM_OFFICER_OPT_PREFIX = "ba_officer_selection_";
    public List<PersonAPI> personList;
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        updatePersonList();
        switch (arg) {
            case "displayOfficersList":
                setupDelegateDialog(dialog);
                displayOfficersList();
                showOptions();
                break;
            case "select":
                int index = Integer.parseInt(memoryMap.get(MemKeys.LOCAL).getString("$option").substring(BRM_OFFICER_OPT_PREFIX.length()));
                memoryMap.get(MemKeys.LOCAL).set("$ba_officerIndex", index);
                PersonAPI person = personList.get(index);
                displayInfo(person, dialog.getTextPanel());
                updateMemoryAndRenameOption(person);
                displayUpgradeCost(person, dialog.getTextPanel());
                disableUpgradeIfNeeded(person);
                break;
            case "upgrade":
                int indexUpgrade = Integer.parseInt(memoryMap.get(MemKeys.LOCAL).getString("$ba_officerIndex"));
                PersonAPI currentPerson = personList.get(indexUpgrade);
                upgradeBRMTier(currentPerson);
                updateMemoryAndRenameOption(currentPerson);
                displayUpgradeCost(currentPerson, dialog.getTextPanel());
                disableUpgradeIfNeeded(currentPerson);
                ba_officermanager.updateLimitBRM(currentPerson);
                break;
            case "upgrade_blindEntry":
                int indexUpgradeBlindEntry = Integer.parseInt(memoryMap.get(MemKeys.LOCAL).getString("$ba_officerIndex"));
                PersonAPI currentPersonBlindEntry = personList.get(indexUpgradeBlindEntry);
                upgradeBRMTier_viaBlindEntry(currentPersonBlindEntry);
                displayUpgradeCost(currentPersonBlindEntry, dialog.getTextPanel());
                disableUpgradeIfNeeded(currentPersonBlindEntry);
                break;
        }
        return false;
    }
    public void updatePersonList() {
        personList = ba_officermanager.getListOfficerFromFleet(null, true);
    }

    /**
     * To be called only when paginated dialog options are required.
     * Otherwise we get nested dialogs that take multiple clicks of the exit option to actually exit.
     * @param dialog
     */
    protected void setupDelegateDialog(InteractionDialogAPI dialog)
    {
        originalPlugin = dialog.getPlugin();

        dialog.setPlugin(this);
        init(dialog);
    }
    @Override
    public void showOptions() {
        super.showOptions();
        dialog.getOptionPanel().setShortcut("ba_BRMSwapMenuReturn", Keyboard.KEY_ESCAPE, false, false, false, false);
    }
    public void upgradeBRMTier_viaBlindEntry(PersonAPI person) {
        if(ba_inventoryhandler.removeBlindEntryFromPersonCargo(1)) {
            int brmBeforeBlindEntryUpgrade = (int) person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
            String modifyId = ba_variablemanager.BA_BLIND_ENTRY_ITEM_ID + ":" + Global.getSector().getClock().getTimestamp();
            person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).modifyFlat(modifyId, ba_variablemanager.BA_BLIND_ENTRY_BRM_INCREMENT);
            int brmAfterBlindEntryUpgrade = (int) person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
            dialog.getTextPanel().setFontSmallInsignia();
            dialog.getTextPanel().addPara("Removed %s from inventory", Misc.getNegativeHighlightColor(), "1 Blind Entry");
            dialog.getTextPanel().addPara("BRM capacity increased by %s", Misc.getHighlightColor(), "" + ba_variablemanager.BA_BLIND_ENTRY_BRM_INCREMENT);
            dialog.getTextPanel().addPara("BRM capacity changed from %s -> %s", Misc.getPositiveHighlightColor(), "" + brmBeforeBlindEntryUpgrade, "" + brmAfterBlindEntryUpgrade);
            dialog.getTextPanel().setFontInsignia();
        }
    }
    public void upgradeBRMTier(PersonAPI person) {
        ba_officermanager.ba_personmemorydata memoryData = ba_officermanager.getPersonMemoryData(person);
        if(memoryData != null) {
            int nextTier = memoryMap.get(MemKeys.LOCAL).get("$ba_nextBRMTier") != null? (int) memoryMap.get(MemKeys.LOCAL).get("$ba_nextBRMTier") : 2;
            float upgradeCost = nextTier * bionicalterationplugin.academyBRMUpgradeBase;
            memoryData.BRMTier = nextTier;
            Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(upgradeCost);
            ba_officermanager.savePersonMemoryData(memoryData, person);
            dialog.getTextPanel().addPara("Upgraded BRM Tier to Tier " + nextTier + " using credits");
        }
    }
    public void updateMemoryAndRenameOption(PersonAPI person) {
        ba_officermanager.ba_personmemorydata data = ba_officermanager.getPersonMemoryData(person);
        int nextTier = 2;
        if(data != null) {
            nextTier = data.BRMTier+1;
            if(nextTier > bionicalterationplugin.maxAcademyBRMTier) {
                nextTier = bionicalterationplugin.maxAcademyBRMTier;
            }
        }
        memoryMap.get(MemKeys.LOCAL).set("$ba_nextBRMTier", nextTier);
        dialog.getOptionPanel().setOptionText("Upgrade to BRM Tier " + nextTier, "ba_BRMTier_showOpt_upgradeSelect_Opt");
    }
    public void disableUpgradeIfNeeded(PersonAPI person) {
        ba_officermanager.ba_personmemorydata memoryData = ba_officermanager.getPersonMemoryData(person);
        if(memoryData != null) {
            int nextTier = memoryMap.get(MemKeys.LOCAL).get("$ba_nextBRMTier") != null? (int) memoryMap.get(MemKeys.LOCAL).get("$ba_nextBRMTier") : 2;
            final float upgradeCost = nextTier * bionicalterationplugin.academyBRMUpgradeBase;
            if(memoryData.BRMTier >= bionicalterationplugin.maxAcademyBRMTier || Global.getSector().getPlayerFleet().getCargo().getCredits().get() <= upgradeCost) {
                dialog.getOptionPanel().setEnabled("ba_BRMTier_showOpt_upgradeSelect_Opt", false);
                dialog.getOptionPanel().addOptionTooltipAppender("ba_BRMTier_showOpt_upgradeSelect_Opt", new OptionPanelAPI.OptionTooltipCreator() {
                    @Override
                    public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
                        if(Global.getSector().getPlayerFleet().getCargo().getCredits().get() <= upgradeCost) {
                            tooltip.addPara("Not enough credit to continue upgrading", 5f);
                        } else {
                            tooltip.addPara("Max BRM Tier reached, can't upgrade further", 5f);
                        }
                    }
                });
            }
        }
        boolean hasBlindEntry = false;
        for( CargoStackAPI cargoStackAPI: Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {
            if (cargoStackAPI.isSpecialStack() && cargoStackAPI.getSpecialItemSpecIfSpecial().getId().equals(ba_variablemanager.BA_BLIND_ENTRY_ITEM_ID)) {
                hasBlindEntry = true;
                break;
            }
        }
        if(!hasBlindEntry) {
            dialog.getOptionPanel().setEnabled("ba_BRMTier_showOpt_upgradeSelected_blindEntry_Opt", false);
            dialog.getOptionPanel().addOptionTooltipAppender("ba_BRMTier_showOpt_upgradeSelected_blindEntry_Opt", new OptionPanelAPI.OptionTooltipCreator() {
                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean hadOtherText) {
                    tooltip.addPara("No available item", 5f);
                }
            });
        }
    }
    public void displayUpgradeCost(PersonAPI person, TextPanelAPI text) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

        ba_officermanager.ba_personmemorydata memoryData = ba_officermanager.getPersonMemoryData(person);
        int nextTier = memoryMap.get(MemKeys.LOCAL).get("$ba_nextBRMTier") != null? (int) memoryMap.get(MemKeys.LOCAL).get("$ba_nextBRMTier") : 2;
        float upgradeCost = nextTier * bionicalterationplugin.academyBRMUpgradeBase;
        text.setFontSmallInsignia();
        text.setFontOrbitronUnnecessarilyLarge();
        text.addPara("BRM Upgrade Information Tier " + nextTier);
        text.setFontSmallInsignia();
        LabelAPI currentLabel = text.addPara("Current credit: %s", t,  "" +Misc.getDGSCredits(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        currentLabel.setHighlightColors(h);
        LabelAPI costLabel = text.addPara("Upgrade cost: %s", t, "" + Misc.getDGSCredits(upgradeCost));
        costLabel.setHighlightColors(bad);
        text.setFontInsignia();
    }
    public void displayOfficersList() {
        //add officers into the list
        dialog.getOptionPanel().clearOptions();

        int index = 0;
        for (PersonAPI person : personList)
        {
            ba_officermanager.ba_personmemorydata data = ba_officermanager.getPersonMemoryData(person);
            String tierText = "Tier ";
            if(data != null) {
                tierText += data.BRMTier;
            } else {
                tierText += "1";
            }
            addOption( tierText + " - " + person.getNameString() + (person.isPlayer()? " (You)": ""), BRM_OFFICER_OPT_PREFIX + index);
            index++;
        }

        addOptionAllPages("Returns", "ba_BRMSwapMenuReturn");
    }
    public void displayInfo(PersonAPI person, TextPanelAPI text) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

        ba_officermanager.ba_personmemorydata memoryData = ba_officermanager.getPersonMemoryData(person);
        text.setFontSmallInsignia();

        //display ship or planet for selected officer/admin
        if(ba_officermanager.isCaptainOrAdmin(person, false).equals(ba_officermanager.ba_profession.CAPTAIN)) {
            //display ship for the officer in player's fleet
            List<FleetMemberAPI> temp = new ArrayList<>();
            FleetMemberAPI member = ba_officermanager.getFleetMemberFromFleet(person, Collections.singletonList(Global.getSector().getPlayerFleet()), true);
            if(member != null) {
                temp.add(member);
                text.beginTooltip().addShipList(1, 1, 150, Global.getSettings().getBasePlayerColor(), temp, 0);
                text.addTooltip();
            } else {
                //display the person pfp if idle
                String spriteName = person.getPortraitSprite();
                text.addImage(spriteName);
            }
        } else {
            AdminData selectedAdmin = null;
            for (AdminData admin: Global.getSector().getCharacterData().getAdmins()) {
                if(!admin.getPerson().isDefault() && !admin.getPerson().isAICore()) {
                    if(admin.getPerson().getId().equals(person.getId())) {
                        selectedAdmin = admin;
                        break;
                    }
                }
            }
            if(selectedAdmin != null && selectedAdmin.getMarket() != null) {
                //display planet
                if(selectedAdmin.getMarket().getPlanetEntity() != null) {
                    text.beginTooltip().showPlanetInfo(selectedAdmin.getMarket().getPlanetEntity(), 150,150,true,0);
                    text.addTooltip();
                } else {
                    //display whatever the market connected to

                    //display the person pfp if idle
                    String spriteName = selectedAdmin.getMarket().getPrimaryEntity().getCustomEntitySpec().getSpriteName();;
                    text.addImage(spriteName);
                }
            } else {
                //display the person pfp if idle
                String spriteName = person.getPortraitSprite();
                text.addImage(spriteName);
            }
        }

        boolean isAdmin = ba_officermanager.isCaptainOrAdmin(person, false).equals(ba_officermanager.ba_profession.ADMIN);

        String profString = ba_officermanager.getProfessionText(person, false);
        LabelAPI prof = text.addPara("Profession: " + profString);
        prof.setHighlight("Profession: ", profString);
        prof.setHighlightColors(g,h);

        if(memoryData != null) {
            int brmTier = memoryData.BRMTier;
            LabelAPI BRMTier = text.addPara("BRM Tier: " + brmTier);
            BRMTier.setHighlight("BRM Tier: ", "" +brmTier);
            BRMTier.setHighlightColors(t, h);
        }

        int currentBRM = (int) person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);
        int limitBRM = (int) person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
        LabelAPI BRM = text.addPara("BRM Capacity: " + limitBRM);
        BRM.setHighlight("BRM Capacity: ", "" +limitBRM);
        BRM.setHighlightColors(t,h);
        text.addSkillPanel(person, isAdmin);

        text.setFontInsignia();


    }
}

package pigeonpun.bionicalteration.ui.overclock;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;
import pigeonpun.bionicalteration.ui.ba_component;
import pigeonpun.bionicalteration.ui.ba_debounceplugin;
import pigeonpun.bionicalteration.ui.ba_uicommon;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static pigeonpun.bionicalteration.ui.bionic.ba_uiplugin.currentScrollPositionBionicTable;

/**
 * @author PigeonPun
 */
public class ba_uiplugin implements CustomUIPanelPlugin {
    Logger log = Global.getLogger(ba_debounceplugin.class);
    protected CustomVisualDialogDelegate.DialogCallbacks callbacks;
    protected InteractionDialogAPI dialog;
    protected CustomPanelAPI containerPanel; //Created panel from ba_deligate.java
    protected TooltipMakerAPI mainTooltip;
    public static final float MAIN_CONTAINER_PADDING = 150f;
    public static final String OVERCLOCK = "OVERCLOCK_WORKSHOP";
    public static final float MAIN_CONTAINER_WIDTH = Global.getSettings().getScreenWidth() - MAIN_CONTAINER_PADDING;
    public static final float MAIN_CONTAINER_HEIGHT = Global.getSettings().getScreenHeight() - MAIN_CONTAINER_PADDING;
    List<ButtonAPI> buttons = new ArrayList<>();
    HashMap<ButtonAPI, String> buttonMap = new HashMap<>();
    HashMap<String, ba_component> componentMap = new HashMap<>();
    int dW, dH, pW, pH;
    protected HashMap<String, ba_component> tabMap = new HashMap<>();
    public static ba_debounceplugin debounceplugin = new ba_debounceplugin();
    private PersonAPI currentPerson;
    public ba_bionicitemplugin currentHoveredBionic; //hovering in the inventory
    public ba_limbmanager.ba_limb currentSelectedLimb; //selected for installation/removal
    public ba_bionicitemplugin currentSelectedBionic; //selected for installation
    private float currentScrollPositionInventory = 0;
    private float currentScrollPositionBionicTable = 0;
    private ba_uicommon commonUI;

    /**
     * @param panel panel
     * @param callbacks callbacks
     * @param dialog dialog
     */
    public void init(CustomPanelAPI panel, CustomVisualDialogDelegate.DialogCallbacks callbacks, InteractionDialogAPI dialog) {
        this.callbacks = callbacks;
        this.containerPanel = panel;
        this.dialog = dialog;

        dW = Display.getWidth();
        dH = Display.getHeight();
        pW = (int) this.containerPanel.getPosition().getWidth();
        pH = (int) this.containerPanel.getPosition().getHeight();
//        commonUI = new ba_uicommon(
//                componentMap, buttons, buttonMap, currentHoveredBionic, currentSelectedLimb, currentSelectedBionic,
//                currentPerson, currentScrollPositionInventory
//        );
        debounceplugin.addToList("WORKSHOP_INVENTORY_TOOLTIP");

        initialUICreation();
        //change the current tab id and "focus" on it
//        focusContent(moveToTabId);
//        currentScrollPositionOverview = 0;
//        debounceplugin.addToList("OVERVIEW_PERSON_LIST_TOOLTIP");
    }
    public static ba_uiplugin createDefault() {
        return new ba_uiplugin();
    }
    private void initialUICreation() {
        mainTooltip = this.containerPanel.createUIElement(this.containerPanel.getPosition().getWidth(), this.containerPanel.getPosition().getHeight(), false);
        mainTooltip.setForceProcessInput(true);
        containerPanel.addUIElement(mainTooltip).inTL(0,0);
        refresh();
    }
    protected void refresh() {
//        log.info("refreshing");
        ba_component overviewComponent = tabMap.get(OVERCLOCK);
        if (overviewComponent != null) {
            containerPanel.removeComponent(overviewComponent.mainPanel);
        }
        buttons.clear();
        buttonMap.clear();
        componentMap.clear();
        //create smaller container for focus/unforcus
        displayOverclock();
    }
    public void saveScrollPosition() {
        //for the bionic table
        ba_component component = componentMap.get("WORKSHOP_PERSON_INFO_BIONICS_PANEL");
        if(component != null && component.tooltipMap.get("PERSON_INFO_BIONICS_TOOLTIP") != null) {
            if(component.tooltipMap.get("PERSON_INFO_BIONICS_TOOLTIP").getExternalScroller() != null) {
                currentScrollPositionBionicTable = component.tooltipMap.get("PERSON_INFO_BIONICS_TOOLTIP").getExternalScroller().getYOffset();
            }
        }
        //for the inventory
        ba_component component2 = componentMap.get("WORKSHOP_INVENTORY_PANEL");
        if(component2 != null && component2.tooltipMap.get("WORKSHOP_INVENTORY_TOOLTIP") != null) {
            if(component2.tooltipMap.get("WORKSHOP_INVENTORY_TOOLTIP").getExternalScroller() != null) {
                currentScrollPositionInventory = component2.tooltipMap.get("WORKSHOP_INVENTORY_TOOLTIP").getExternalScroller().getYOffset();
            }
        }
    }
    public void displayOverclock() {
        float pad = 5f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

        float inventoryW = 0.75f * pW;
        float listOverclockW = (1 - (inventoryW/pW)) * pW;

        String mainOverclockPanelKey = "MAIN_OVERCLOCk_CONTAINER";
        String inventoryTooltipKey = "INVENTORY_TOOLTIP";
        String overclockListTooltipKey = "OVERCLOCK_LIST_TOOLTIP";
        ba_component overclockContainer = new ba_component(componentMap, containerPanel, pW, pH, 0, 0, true, mainOverclockPanelKey);
        tabMap.put(OVERCLOCK, overclockContainer);
        TooltipMakerAPI inventoryTooltipContainer = overclockContainer.createTooltip(inventoryTooltipKey, inventoryW, pH, false, 0, 0);
        TooltipMakerAPI overclockListTooltipContainer = overclockContainer.createTooltip(overclockListTooltipKey, listOverclockW, pH, false, 0, 0);
        overclockListTooltipContainer.getPosition().inTL(inventoryW, 0);
        //overviewPerson
        displayInventory(overclockContainer, inventoryTooltipKey, inventoryW, pH);
        displayOverclockList(overclockContainer, overclockListTooltipKey, listOverclockW, pH);
    }
    public void displayInventory(ba_component creatorComponent, String creatorComponentTooltip, float invW, float invH) {
        displayInventoryWorkshop(creatorComponent, creatorComponentTooltip, "WORKSHOP", invW, invH, 0, 0);
    }
    public void displayOverclockList(ba_component creatorComponent, String creatorComponentTooltip, float listW, float listH) {

    }
    public void displayInventoryWorkshop(
            ba_component creatorComponent,
            String creatorComponentTooltip,
            String keyPrefix,
            float inventoryW, float inventoryH,
            float inventoryX, float inventoryY
    ) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        String prefix = keyPrefix + "_";

        //big container
        final float containerW = inventoryW - pad - pad / 2;
        int containerH = (int) (inventoryH - pad - pad);
        int containerX = (int) pad;
        int containerY = (int) pad;
        String inventoryTooltipKey = prefix + "INVENTORY_TOOLTIP";
        String inventoryPanelKey = prefix + "INVENTORY_PANEL";
        ba_component inventoryContainer = new ba_component(componentMap, creatorComponent.mainPanel, containerW, containerH, containerX, containerY, true, inventoryPanelKey);
        TooltipMakerAPI inventoryTooltipContainer = inventoryContainer.createTooltip(inventoryTooltipKey, containerW, containerH, true, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryPanelKey, inventoryContainer, containerX, containerY);

        CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
        java.util.List<CargoStackAPI> availableBionics = new ArrayList<>();
        for(CargoStackAPI cargo: playerCargo.getStacksCopy()) {
            if(cargo.isSpecialStack() && cargo.getSpecialItemSpecIfSpecial() != null && ba_bionicmanager.bionicItemMap.containsKey(cargo.getSpecialDataIfSpecial().getId())) {
                availableBionics.add(cargo);
            }
        }
        List<ba_component> subComponentItemList = new ArrayList<>();
        if(availableBionics.size() != 0) {
            int index = 0;
            int row = 0;
            int itemW = 100;
            int itemH = 100;
            int itemsPerRow = (int) Math.floor(containerW / itemW);
            int defaultRows = 1;
            int neededRows = defaultRows;
            if((float) availableBionics.size() / itemsPerRow > defaultRows) {
                neededRows = (int) Math.ceil((float) availableBionics.size() / itemsPerRow);
            }
            while(row < neededRows) {
                int rowX = 0;
                int rowY = (int) (row * itemH);
                final int rowW = (int) containerW;
                int rowH = (int) (itemH);
                String rowTooltipKey = "INVENTORY_ROW_TOOLTIP";
                String rowPanelKey = "INVENTORY_ROW_PANEL_"+ row;
                ba_component rowContainer = new ba_component(componentMap, inventoryContainer.mainPanel, rowW, rowH, rowX, rowY, false, rowPanelKey);
                TooltipMakerAPI rowTooltipContainer = rowContainer.createTooltip(rowTooltipKey, rowW, rowH, false, 0,0);
                inventoryContainer.attachSubPanel(inventoryTooltipKey, rowPanelKey, rowContainer, rowX, rowY);
                subComponentItemList.add(rowContainer);
                int rowItemCount = 0;
                while(rowItemCount < itemsPerRow) {
                    if(index < availableBionics.size()) {
                        CargoStackAPI cargo = availableBionics.get(index);
                        final ba_bionicitemplugin bionic = ba_bionicmanager.bionicItemMap.get(cargo.getSpecialItemSpecIfSpecial().getId());
                        float quantity = cargo.getSize();

                        int itemX = rowItemCount * itemW;
                        int itemY = 0;
                        //--------image
                        int imageW = 80;
                        int imageH = 80;
                        int imageX = itemX + itemW / 2 - imageW / 2;
                        int imageY = itemY + itemH / 2 - imageH / 2;
                        String spriteName = bionic.getSpec().getIconName();
                        TooltipMakerAPI personImageTooltip = rowContainer.createTooltip("ITEM_IMAGE", imageW, imageH, false, 0, 0);
                        personImageTooltip.addImage(spriteName, imageW * 0.9f, imageH * 0.9f, 0);
                        //name
                        String name = ba_utils.getShortenBionicName(bionic.getName());
                        LabelAPI nameLabel = rowTooltipContainer.addPara(name, 0);
                        nameLabel.getPosition().inTL(itemX + 5, itemY + itemH - nameLabel.getPosition().getHeight() - 5);
                        //---------quantity
                        LabelAPI quantityLabel = rowTooltipContainer.addPara(String.valueOf((int) quantity), Misc.getBrightPlayerColor(), 0);
                        quantityLabel.getPosition().inTL(itemX + itemW - quantityLabel.computeTextWidth(String.valueOf((int) quantity)) - pad / 2, itemY + pad / 2);
                        //---------hover
                        ButtonAPI areaChecker = rowTooltipContainer.addAreaCheckbox("", null,Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), itemW, itemH, 0);
                        addButtonToList(areaChecker, "hover_bionic_item:"+bionic.getId());
                        areaChecker.getPosition().setLocation(0,0).inTL(itemX, itemY);
                        if(currentSelectedBionic != null) {
                            if(currentSelectedBionic.equals(bionic)) {
                                areaChecker.highlight();
                            }
                        }
                        if(currentSelectedLimb != null) {
                            if(ba_bionicmanager.checkIfBionicConflicted(bionic, currentPerson)) {
                                areaChecker.setEnabled(false);
                            }
                        }
                        //hover thingy
                        personImageTooltip.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
                            @Override
                            public boolean isTooltipExpandable(Object tooltipParam) {
                                return false;
                            }

                            @Override
                            public float getTooltipWidth(Object tooltipParam) {
                                return rowW * 0.8f;
                            }

                            @Override
                            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                                if(currentHoveredBionic == null) {
                                    tooltip.addPara("Somehow the hover isn't registering the bionic ????? Im clueless LMAO. Try hovering again", Misc.getHighlightColor(),0);
                                    return;
                                }
                                ba_bionicmanager.displayBionicItemDescription(tooltip, currentHoveredBionic);
                            }
                        }, areaChecker, TooltipMakerAPI.TooltipLocation.ABOVE);

                        personImageTooltip.getPosition().inTL(imageX, imageY);
                    }
                    rowItemCount++;
                    index++;
                }
                row++;
            }
        } else {
            //Empty text
            int emptyX = (int) containerW / 2;
            int emptyY = (int) containerH / 2;
            String emptyText = "Currently no available bionic to install.";
            LabelAPI emptyLabel = inventoryTooltipContainer.addPara(emptyText,Misc.getDarkPlayerColor(), 1);
            emptyLabel.getPosition().inTL(emptyX - (emptyLabel.computeTextWidth(emptyText) / 2), emptyY);
        }
        inventoryContainer.subComponentListMap.put("ITEM_LIST", subComponentItemList);
        //add this later so scroll works
        inventoryContainer.mainPanel.addUIElement(inventoryTooltipContainer).setLocation(0,0).inTL(0, 0);
        if(inventoryTooltipContainer.getExternalScroller() != null) {
            inventoryTooltipContainer.getExternalScroller().setYOffset(currentScrollPositionInventory);
        }
        //border
        UIComponentAPI border = inventoryTooltipContainer.createRect(Misc.getDarkPlayerColor(), 1);
        border.getPosition().setSize(containerW, containerH);
        inventoryContainer.mainPanel.addComponent(border).setLocation(0,0).inTL(0, 0);
    }
    protected void displayBionicTable(
            ba_component creatorComponent,
            String creatorComponentTooltip,
            String keyPrefix,
            final boolean isWorkshopMode,
            boolean isScroll , float tableW, float tableH, float tableX, float tableY) {
        final float pad = 10f;
        float opad = 10f;
        final Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;
        String prefix = keyPrefix + "_";

        String infoPersonBionicTooltipKey = "PERSON_INFO_BIONICS_TOOLTIP";
        String infoPersonBionicPanelKey = prefix + "PERSON_INFO_BIONICS_PANEL";
        ba_component infoPersonBionicContainer = new ba_component(componentMap, creatorComponent.mainPanel, tableW, tableH, tableX, tableY, !isScroll, infoPersonBionicPanelKey);
        TooltipMakerAPI infoPersonBionicTooltipContainer = infoPersonBionicContainer.createTooltip(infoPersonBionicTooltipKey, tableW, tableH, isScroll, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonBionicPanelKey, infoPersonBionicContainer, tableX, tableY);

        //table header
        String tableHeaderTooltipContainerKey = "BIONIC_TABLE_HEADER_TOOLTIP";
        String tableHeaderPanelContainerKey = prefix + "BIONIC_TABLE_HEADER_PANEL";
        int tableHeaderH = 40;
        int tableHeaderW = (int) (tableW - pad);
        //--------bionic container
        ba_component tableHeaderDisplayContainer = new ba_component(componentMap, infoPersonBionicContainer.mainPanel, tableHeaderW, tableHeaderH,0,0,false, tableHeaderPanelContainerKey);
        TooltipMakerAPI tableHeaderDisplayContainerTooltip = tableHeaderDisplayContainer.createTooltip(tableHeaderTooltipContainerKey, tableHeaderW, tableHeaderH, false, 0,0);
        tableHeaderDisplayContainerTooltip.setForceProcessInput(true);
        //attach to have the main tooltip scroll effect this component's panel
        infoPersonBionicContainer.attachSubPanel(infoPersonBionicTooltipKey, infoPersonBionicPanelKey, tableHeaderDisplayContainer);
        int limbX = (int) pad;
        int limbW = 150;
        int bionicRowX = limbW;
        int bionicRowW = (int) (tableW - limbW - pad);
        int bionicNameX = bionicRowX;
        int bionicNameW = (int) (bionicRowW * 0.6f);
        int bionicBRMX = bionicNameW;
        int bionicBRMW = (int) (bionicRowW * 0.2f);
        int bionicConsciousX = bionicBRMX + bionicBRMW;
        int bionicConsciousW = (int) (bionicRowW * 0.2f);
        //>Limb
        LabelAPI limbHeader = tableHeaderDisplayContainerTooltip.addPara("LIMB", 0, Misc.getBrightPlayerColor(), "LIMB");
        limbHeader.getPosition().setSize(limbW, tableHeaderH);
        limbHeader.getPosition().inTL(pad + 5, pad);
        //>Bionic
        LabelAPI bionicNameHeader = tableHeaderDisplayContainerTooltip.addPara("BIONIC", 0, Misc.getBrightPlayerColor(), "BIONIC");
        bionicNameHeader.getPosition().setSize(bionicNameW, tableHeaderH);
        bionicNameHeader.getPosition().inTL(bionicNameX + 5, pad);
        //>BRM
        LabelAPI bionicBRMHeader = tableHeaderDisplayContainerTooltip.addPara("BRM", 0, Misc.getBrightPlayerColor(), "BRM");
        bionicBRMHeader.getPosition().setSize(bionicBRMW, tableHeaderH);
        bionicBRMHeader.getPosition().inTL(bionicBRMX + bionicNameX,0);
        bionicBRMHeader.setAlignment(Alignment.MID);
        //>Conscious
        LabelAPI bionicConsciousHeader = tableHeaderDisplayContainerTooltip.addPara(ba_consciousmanager.getDisplayConditionLabel(currentPerson).toUpperCase(), 0, Misc.getBrightPlayerColor(), ba_consciousmanager.getDisplayConditionLabel(currentPerson).toUpperCase());
        bionicConsciousHeader.getPosition().setSize(bionicConsciousW, tableHeaderH);
        bionicConsciousHeader.getPosition().inTL(bionicConsciousX + bionicNameX,0);
        bionicConsciousHeader.setAlignment(Alignment.MID);

        //rows
        int i = 0;
        List<ba_component> subComponentBionicList = new ArrayList<>();
        List<ba_officermanager.ba_bionicAugmentedData> currentAnatomyList = ba_officermanager.getBionicAnatomyList(this.currentPerson);
//        log.info(currentAnatomyList.size() + " - " + this.currentPerson.getTags());
//        for (ba_officermanager.ba_bionicAugmentedData data: currentAnatomyList) {
//            log.info(data.limb.name + " - " + data.bionicInstalled.size());
//            if(data.bionicInstalled.size() != 0) {
//                for (ba_bionicmanager.ba_bionic bionic: data.bionicInstalled) {
//                    log.info("---------" + bionic.name);
//                }
//            }
//        }
        for(final ba_officermanager.ba_bionicAugmentedData bionic: currentAnatomyList) {
            String bionicTooltipContainerKey = "BIONIC_TOOLTIP_CONTAINER";
            String bionicPanelContainerKey = prefix + "BIONIC_PANEL_CONTAINER_"+i;
            int singleBionicInstalledNameH = 40;
            int bionicH = bionic.bionicInstalled.size()!= 0 ? singleBionicInstalledNameH * bionic.bionicInstalled.size() : singleBionicInstalledNameH;
            //add a extra line for the overclock
            if(bionic.bionicInstalled.size()!= 0 && ba_overclockmanager.isBionicOverclockable(bionic.bionicInstalled.get(0))) {
                bionicH += singleBionicInstalledNameH;
            }
            final int bionicW = (int) (tableW - pad);
            //--------bionic container
            ba_component bionicDisplayContainer = new ba_component(componentMap, infoPersonBionicContainer.mainPanel, bionicW, bionicH,0,0,false, bionicPanelContainerKey);
            TooltipMakerAPI personDisplayContainerTooltip = bionicDisplayContainer.createTooltip(bionicTooltipContainerKey, bionicW, bionicH, false, 0,0);
            personDisplayContainerTooltip.setForceProcessInput(true);
            //attach to have the main tooltip scroll effect this component's panel
            infoPersonBionicContainer.attachSubPanel(infoPersonBionicTooltipKey, infoPersonBionicPanelKey, bionicDisplayContainer);
            subComponentBionicList.add(bionicDisplayContainer);
            //hover
            ButtonAPI areaChecker = personDisplayContainerTooltip.addAreaCheckbox("", null,Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), bionicW, bionicH, 0);
            addButtonToList(areaChecker, "hover_bionic_table_limb:"+bionic.limb.limbId);
            areaChecker.getPosition().setLocation(0,0).inTL(0, 0);
            if(this.currentSelectedLimb != null) {
                if(this.currentSelectedLimb.limbId.equals(bionic.limb.limbId)) {
                    areaChecker.highlight();
                }
            }
            //hover pop up
            personDisplayContainerTooltip.addTooltipToPrevious(new TooltipMakerAPI.TooltipCreator() {
                @Override
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return true;
                }

                @Override
                public float getTooltipWidth(Object tooltipParam) {
                    return bionicW * 0.8f;
                }

                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addSectionHeading("Limb", Alignment.MID, 0);
                    tooltip.addPara(bionic.limb.description, pad);
                    tooltip.addSpacer(pad);
                    tooltip.addSectionHeading("Bionics", Alignment.MID, 0);
                    if(bionic.bionicInstalled.size() != 0) {
                        for(ba_bionicitemplugin b: bionic.bionicInstalled) {
                            b.effectScript.displayEffectDescription(tooltip, currentPerson, b, false);
                            if(isWorkshopMode) {
                                //---------Overclock
                                if(ba_overclockmanager.isBionicOverclockable(b)) {
                                    LabelAPI overclockLabel = tooltip.addPara("%s %s", pad, t,"Overclock:", b.isOverClockApplied()? b.appliedOverclock.name: "None active");
                                    overclockLabel.setHighlight("Overclock:", b.isOverClockApplied()? b.appliedOverclock.name: "None active");
                                    overclockLabel.setHighlightColors(special, b.isOverClockApplied()? h: g);
                                }
                                //---------Conflicts
                                StringBuilder conflictsList = new StringBuilder();
                                for (ba_bionicitemplugin bionic: ba_bionicmanager.getListBionicConflicts(b)) {
                                    conflictsList.append(bionic.getName()).append(", ");
                                }
                                if(conflictsList.length() > 0) {
                                    conflictsList.setLength(conflictsList.length() - 2);
                                } else {
                                    conflictsList.append("None");
                                }
                                LabelAPI conflictListLabel = tooltip.addPara("%s %s", pad, t,"Conflicts:", conflictsList.toString());
                                conflictListLabel.setHighlight("Conflicts:", conflictsList.toString());
                                conflictListLabel.setHighlightColors(g.brighter().brighter(), conflictsList.toString().equals("None")? g : Misc.getNegativeHighlightColor());
                            }
                            if(expanded) {
//                                if(!isWorkshopMode) {
//                                    b.effectScript.displayEffectDescription(tooltip, currentPerson, b);
////                                    LabelAPI expandedTooltip = tooltip.addPara("%s %s", pad, Misc.getBasePlayerColor(), "Effects:", effect);
////                                    expandedTooltip.setHighlight("Effects:", effect);
////                                    expandedTooltip.setHighlightColors(Misc.getGrayColor().brighter(), b.effectScript != null ? Misc.getHighlightColor() :Misc.getGrayColor());
//                                } else {
//
//                                }
                                LabelAPI expandedTooltip = tooltip.addPara("%s %s", pad, Misc.getBasePlayerColor(), "Description:", b.getSpec().getDesc());
                                expandedTooltip.setHighlight("Description:", b.getSpec().getDesc());
                                expandedTooltip.setHighlightColors(Misc.getGrayColor().brighter(), t);
                            }
                            tooltip.addSpacer(pad);
                        }
                    } else {
                        tooltip.addPara("No bionic installed", pad, Misc.getGrayColor(), "No bionic installed");
                        tooltip.addSpacer(pad);
                    }
                }
            }, isWorkshopMode? TooltipMakerAPI.TooltipLocation.BELOW : TooltipMakerAPI.TooltipLocation.ABOVE);
            //---------Limb Name
            int nameH = bionicH;
            int nameW = limbW;
            int nameX = limbX;
            TooltipMakerAPI bionicLimbNameTooltip = bionicDisplayContainer.createTooltip("BIONIC_LIMB_NAME", nameW, nameH, false, 0, 0);
            bionicLimbNameTooltip.getPosition().inTL(nameX, 0);
            LabelAPI limbName = bionicLimbNameTooltip.addPara(bionic.limb.name, pad);
            limbName.setHighlight(bionic.limb.name);
            limbName.setHighlightColors(t);
            //---------Bionic
            int bionicInstalledI = 0;
            for (ba_bionicitemplugin b: bionic.bionicInstalled) {
                int sectionH = singleBionicInstalledNameH;
                int sectionW = bionicRowW;
                int sectionX = bionicRowX;
                int sectionSpacerY = singleBionicInstalledNameH * bionicInstalledI;
                TooltipMakerAPI bionicNameTooltip = bionicDisplayContainer.createTooltip("BIONIC_NAME"+bionicInstalledI, sectionW, sectionH, false, sectionX, sectionSpacerY);
                bionicNameTooltip.getPosition().inTL(sectionX, sectionSpacerY);
                //>name
                LabelAPI bionicName = bionicNameTooltip.addPara("(%s) %s", pad, h, !Objects.equals(b.namePrefix, "") ? b.namePrefix: " ", "" + b.getName());
                bionicName.getPosition().setSize(bionicNameW,sectionH);
//                bionicName.setHighlight(b.name, b.namePrefix);
                bionicName.setHighlightColors(Misc.getBasePlayerColor() ,b.displayColor);
                //>BRM
//                int brmX = (int) (bionicName.getPosition().getWidth());
                LabelAPI bionicBRM = bionicNameTooltip.addPara("" + Math.round(b.brmCost), pad);
                bionicBRM.getPosition().setSize(bionicBRMW,sectionH);
                bionicBRM.setHighlight("" + Math.round(b.brmCost));
                bionicBRM.setHighlightColors(Misc.getBrightPlayerColor());
                bionicBRM.getPosition().inTL(bionicBRMX + bionicBRMW/2, pad);
                //>Conscious
//                int consX = (int) (bionicBRM.getPosition().getWidth() + brmX);
                LabelAPI bionicConscious = bionicNameTooltip.addPara("" + Math.round(b.consciousnessCost * 100) + "%", pad);
                bionicConscious.getPosition().setSize(bionicConsciousW,sectionH);
                bionicConscious.setHighlight("" + Math.round(b.consciousnessCost * 100) + "%");
                bionicConscious.setHighlightColors(Misc.getNegativeHighlightColor());
                bionicConscious.getPosition().inTL(bionicConsciousX + bionicConsciousW/2, pad);
                if(ba_overclockmanager.isBionicOverclockable(b)) {
                    int overclockRowY = singleBionicInstalledNameH;
                    TooltipMakerAPI overclockTooltip = bionicDisplayContainer.createTooltip("BIONIC_OVERCLOCK_NAME", sectionW, sectionH, false, sectionX, overclockRowY);
                    overclockTooltip.getPosition().inTL(sectionX, overclockRowY);
                    //>name
                    overclockTooltip.setParaSmallInsignia();
                    LabelAPI overclockName = overclockTooltip.addPara("[ %s ]", pad, h, b.isOverClockApplied()? b.appliedOverclock.name: "--------");
                    overclockName.setHighlight("[",b.isOverClockApplied()? b.appliedOverclock.name: "--------", "]");
                    overclockName.setHighlightColors(special, b.isOverClockApplied()? h: g, special);
                    overclockName.getPosition().setSize(bionicNameW,sectionH);
                    overclockTooltip.setParaFontDefault();
                }

                bionicInstalledI++;
            }
            i++;
        }
        infoPersonBionicContainer.subComponentListMap.put("SUB_BIONIC_LIST", subComponentBionicList);
        if(isScroll) {
            infoPersonBionicContainer.mainPanel.addUIElement(infoPersonBionicTooltipContainer);
            if(infoPersonBionicTooltipContainer.getExternalScroller() != null) {
                infoPersonBionicTooltipContainer.getExternalScroller().setYOffset(currentScrollPositionBionicTable);
            }
        }
    }
    public void addButtonToList(ButtonAPI button, String buttonMapValue) {
        buttons.add(button);
        buttonMap.put(button, buttonMapValue);
    }
    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {
        ba_component previousTab2 = componentMap.get("MAIN_OVERCLOCk_CONTAINER");
//        if(previousTab2.getTooltip("INVENTORY_TOOLTIP") != null) {
//            ba_utils.drawBox(
//                    (int) previousTab2.getTooltip("INVENTORY_TOOLTIP").getPosition().getX(),
//                    (int) previousTab2.getTooltip("INVENTORY_TOOLTIP").getPosition().getY(),
//                    (int) previousTab2.getTooltip("INVENTORY_TOOLTIP").getPosition().getWidth(),
//                    (int) previousTab2.getTooltip("INVENTORY_TOOLTIP").getPosition().getHeight(),
//                    0.3f,
//                    Color.pink
//            );
//        }
        if(previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP") != null) {
            ba_utils.drawBox(
                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getX(),
                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getY(),
                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getWidth(),
                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getHeight(),
                    0.3f,
                    Color.blue
            );
        }
    }

    @Override
    public void advance(float amount) {
//handles button input processing
        //if pressing a button changes something in the diplay, call reset()
        boolean needsReset = false;
        for (ButtonAPI b : buttons)
        {
//            log.info("" + b + "--" + b.isHighlighted() + "-" + b.isChecked() + "-" + b.isEnabled());
            if (b.isChecked()) {
                b.setChecked(false);
                //Check if click change main page
                String s = buttonMap.get(b);
                String[] tokens = s.split(":");
                if(tokens[0].equals("hover_bionic_item")) {
                    if(ba_bionicmanager.bionicItemMap.get(tokens[1]) != null) {
                        this.currentSelectedBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
                        needsReset = true;
                        break;
                    }
                }
                if(tokens[0].equals("hover_bionic_table_limb")) {
                    this.currentSelectedLimb = ba_limbmanager.getLimb(tokens[1]);
                    needsReset = true;
                    break;
                }
            }
        }

        //pressing a button usually means something we are displaying has changed, so redraw the panel from scratch
        if (needsReset) {
            saveScrollPosition();
            refresh();
        };
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        boolean shouldRefresh = false;
        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;
            if(event.isMouseMoveEvent()) {
                for (ButtonAPI button: buttons) {
                    float buttonX = button.getPosition().getX();
                    float buttonY = button.getPosition().getY();
                    float buttonW = button.getPosition().getWidth();
                    float buttonH = button.getPosition().getHeight();
                    if(event.getX() >= buttonX && event.getX() < buttonX + buttonW && event.getY() >= buttonY && event.getY() < buttonY+buttonH) {
                        String s = buttonMap.get(button);
                        String[] tokens = s.split(":");
                        ba_component component = componentMap.get("WORKSHOP_INVENTORY_PANEL");
                        //hover bionic item in inventory
                        if(component != null && component.tooltipMap.get("WORKSHOP_INVENTORY_TOOLTIP") != null) {
                            if(tokens[0].equals("hover_bionic_item") && debounceplugin.isDebounceOver("WORKSHOP_INVENTORY_TOOLTIP", 0, component.tooltipMap.get("WORKSHOP_INVENTORY_TOOLTIP").getExternalScroller().getYOffset())) {
                                if(ba_bionicmanager.bionicItemMap.get(tokens[1]) != null && (this.currentHoveredBionic == null || !this.currentHoveredBionic.bionicId.equals(tokens[1]))) {
                                    this.currentHoveredBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
                                    shouldRefresh = true;
                                }
                            }
                        }
                    }
                }
            }
            //is ESC is pressed, close the custom UI panel and the blank IDP we used to create it
            if (event.isKeyDownEvent() && event.getEventValue() == Keyboard.KEY_ESCAPE) {
                event.consume();
                callbacks.dismissDialog();
                return;
            }
        }

        if(shouldRefresh) {
            saveScrollPosition();
            refresh();
        };
    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
//    class overclockUI {
//        HashMap<String, String> hashmap = new HashMap<>();
//        float scroll = 0;
//        commonUI commonui;
//        public overclockUI() {
//            commonui = new commonUI(hashmap, scroll);
//        }
//    }
//    class commonUI {
//        HashMap<String, String> hashmap;
//        float scroll;
//        public commonUI(HashMap<String, String> hashmap, float scroll) {
//            this.hashmap = hashmap; //this will pass down as reference lucky
//            this.scroll = scroll; //this wont pass down as reference but as a separate copy
//        }
//        public void useMethod() {
//
//        }
//    }
}

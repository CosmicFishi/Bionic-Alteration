package pigeonpun.bionicalteration.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.inventory.ba_inventoryhandler;
import pigeonpun.bionicalteration.overclock.ba_overclock;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;
import pigeonpun.bionicalteration.ui.bionic.ba_uiplugin;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.awt.*;
import java.util.*;
import java.util.List;

//import static pigeonpun.bionicalteration.ui.bionic.ba_uiplugin.currentScrollPositionBionicTable;
public class ba_uicommon implements CustomUIPanelPlugin {
    protected CustomVisualDialogDelegate.DialogCallbacks callbacks;
    protected InteractionDialogAPI dialog;
    protected HashMap<String, ba_component>  componentMap = new HashMap<>();
    protected List<ButtonAPI> buttons = new ArrayList<>();
    protected HashMap<ButtonAPI, String> buttonMap = new HashMap<>();
    protected ba_bionicitemplugin currentHoveredBionic;
    protected ba_limbmanager.ba_limb currentSelectedLimb;
    protected ba_bionicitemplugin currentSelectedBionic;
    protected PersonAPI currentPerson;
    protected float currentScrollPositionInventory = 0;
    protected float currentScrollPositionBionicTable = 0;
    protected float currentScrollPositionPersonList = 0;
    protected float currentScrollPositionBioformList = 0;
    public ba_bionicitemplugin currentRemovingBionic; //selected for removing
    public static ba_debounceplugin debounceplugin = new ba_debounceplugin();
    public List<CargoStackAPI> cargoBionic = new ArrayList<>();
    public static boolean isDisplayingOtherFleets = false;
    protected List<ba_officermanager.ba_bioformAugmentedData> currentBioformData = new ArrayList<>(); //use as a placeholder the actual bioform info from person to avoid overriding unintentionally person's bioform
    protected LinkedHashMap<String, String> bioformChangeList = new LinkedHashMap<>();
    protected List<String> bioformAddList = new ArrayList<>();
    protected List<String> bioformRemoveList = new ArrayList<>();
    public static float getInitDialogContainerPaddingX() {
        float containerPadding = 0;

        if(Global.getSettings().getScreenWidth() > getInitDialogContainerWidth()) {
            containerPadding = Global.getSettings().getScreenWidth() - getInitDialogContainerWidth();
        }

        return containerPadding;
    }
    public static float getInitDialogContainerPaddingY() {
        float containerPadding = 0;

        if(Global.getSettings().getScreenHeight() > getInitDialogContainerHeight()) {
            containerPadding = Global.getSettings().getScreenHeight() - getInitDialogContainerHeight();
        }

        return containerPadding;
    }
    public static float getInitDialogContainerWidth() {
        float width = 1200f;

        return width;
    }
    public static float getInitDialogContainerHeight() {
        float height = 700f;

        return height;
    }
    protected void init(CustomPanelAPI panel, CustomVisualDialogDelegate.DialogCallbacks callbacks, InteractionDialogAPI dialog) {
        currentScrollPositionInventory = 0;
        currentScrollPositionBionicTable = 0;
        currentScrollPositionPersonList = 0;
        currentScrollPositionBioformList = 0;
        debounceplugin.addToList("INVENTORY_TOOLTIP");
        debounceplugin.addToList("PERSON_LIST_TOOLTIP");
    }
    protected void refresh() {
        //reset button for checking press
        cargoBionic.clear();
        cargoBionic = ba_inventoryhandler.uncompressAllBionics().getStacksCopy();
//        CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
//        for(CargoStackAPI cargo: playerCargo.getStacksCopy()) {
//            if(cargo.isSpecialStack() && cargo.getSpecialItemSpecIfSpecial() != null && ba_bionicmanager.bionicItemMap.containsKey(cargo.getSpecialDataIfSpecial().getId())) {
//                cargoBionic.add(cargo);
//            }
//        }
        for (ButtonAPI b : buttons) {
            if (b.isChecked()) {
                b.setChecked(false);
            }
        }
//        log.info("refreshing");
        buttons.clear();
        buttonMap.clear();
        componentMap.clear();
    }
    public void displayInventoryWorkshop(
            ba_component creatorComponent,
            String creatorComponentTooltip,
            float inventoryW, float inventoryH,
            float inventoryX, float inventoryY
    ) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        //big container
        final float containerW = inventoryW - pad - pad / 2;
        int containerH = (int) (inventoryH - pad - pad);
        int containerX = (int) (inventoryX + pad);
        int containerY = (int) (inventoryY + pad);
        String inventoryTooltipKey = "INVENTORY_TOOLTIP";
        String inventoryPanelKey = "INVENTORY_PANEL";
        ba_component inventoryContainer = new ba_component(componentMap, creatorComponent.mainPanel, containerW, containerH, containerX, containerY, true, inventoryPanelKey);
        TooltipMakerAPI inventoryTooltipContainer = inventoryContainer.createTooltip(inventoryTooltipKey, containerW, containerH, true, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryPanelKey, inventoryContainer, containerX, containerY);

//        CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
//        java.util.List<CargoStackAPI> availableBionics = new ArrayList<>();
//        for(CargoStackAPI cargo: playerCargo.getStacksCopy()) {
//            if(cargo.isSpecialStack() && cargo.getSpecialItemSpecIfSpecial() != null && ba_bionicmanager.bionicItemMap.containsKey(cargo.getSpecialDataIfSpecial().getId())) {
//                availableBionics.add(cargo);
//            }
//        }
        List<ba_component> subComponentItemList = new ArrayList<>();
        if(cargoBionic.size() != 0) {
            int index = 0;
            int row = 0;
            int itemW = 100;
            int itemH = 100;
            int itemsPerRow = (int) Math.floor(containerW / itemW);
            int defaultRows = 1;
            int neededRows = defaultRows;
            if((float) cargoBionic.size() / itemsPerRow > defaultRows) {
                neededRows = (int) Math.ceil((float) cargoBionic.size() / itemsPerRow);
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
                    if(index < cargoBionic.size()) {
                        CargoStackAPI cargo = cargoBionic.get(index);
                        final ba_bionicitemplugin bionic = (ba_bionicitemplugin) cargo.getPlugin();
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
                        //---------hover
                        ButtonAPI areaChecker = rowTooltipContainer.addAreaCheckbox("", null,Misc.getBasePlayerColor(), Misc.getDarkPlayerColor().darker().darker().darker(), Misc.getBrightPlayerColor(), itemW, itemH, 0);
                        addButtonToList(areaChecker, "hover_bionic_item:"+bionic.getId()+":"+index);
                        areaChecker.getPosition().setLocation(0,0).inTL(itemX, itemY);
                        if(currentSelectedBionic != null) {
                            if(currentSelectedBionic.equals(bionic)) {
                                areaChecker.setHighlightBrightness(0.6f);
                                areaChecker.highlight();
                            }
                        }
//                        if(currentSelectedLimb != null) {
//                            if(ba_bionicmanager.checkIfBionicConflicted(bionic, currentPerson)) {
//                                areaChecker.setEnabled(false);
//                            }
//                            if(ba_limbmanager.isLimbInGroup(bionic.bionicLimbGroupId, currentSelectedLimb.limbId) && (currentSelectedBionic == null || !currentSelectedBionic.equals(bionic))) {
//                                areaChecker.setHighlightBrightness(0.25f);
//                                areaChecker.highlight();
//                            }
//                        }
                        //name
                        String name = ba_utils.getShortenBionicName(bionic.getName());
                        LabelAPI nameLabel = rowTooltipContainer.addPara(name, 0);
                        nameLabel.setOpacity(0.7f);
                        if(currentSelectedLimb != null) {
                            if(ba_limbmanager.isLimbInGroup(bionic.bionicLimbGroupId, currentSelectedLimb.limbId)) {
                                nameLabel.setOpacity(1);
                                nameLabel.setColor(Misc.getPositiveHighlightColor());
                            }
                        }
                        //---------quantity
                        LabelAPI quantityLabel = rowTooltipContainer.addPara(String.valueOf((int) quantity), Misc.getBrightPlayerColor(), 0);
                        quantityLabel.getPosition().inTL(itemX + itemW - quantityLabel.computeTextWidth(String.valueOf((int) quantity)) - pad / 2, itemY + pad / 2);
                        nameLabel.getPosition().inTL(itemX + 5, itemY + itemH - nameLabel.getPosition().getHeight() - 5);
                        quantityLabel.setOpacity(0.7f);
                        //hover thingy
                        personImageTooltip.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
                            @Override
                            public boolean isTooltipExpandable(Object tooltipParam) {
                                return false;
                            }

                            @Override
                            public float getTooltipWidth(Object tooltipParam) {
                                return Math.max(rowW * 0.8f, 500f);
                            }

                            @Override
                            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                                if(currentHoveredBionic == null) {
//                                    tooltip.addPara("Somehow the hover isn't registering the bionic ????? Im clueless LMAO. Try hovering again", Misc.getHighlightColor(),0);
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
            final boolean isWorkshopMode,
            boolean isScroll , float tableW, float tableH, float tableX, float tableY) {
        displayBionicTableWithKeyPreset(
                creatorComponent,
                creatorComponentTooltip, "",
                isWorkshopMode, isScroll,
                tableW, tableH,
                tableX, tableY
        );
    }
    /**
     * Use this for the preset keys inside the component
     * Note: You will have to set up custom input detection with this
     * @param creatorComponent
     * @param creatorComponentTooltip
     * @param isWorkshopMode
     * @param isScroll
     * @param tableW
     * @param tableH
     * @param tableX
     * @param tableY
     */
    protected void displayBionicTableWithKeyPreset(
            ba_component creatorComponent,
            String creatorComponentTooltip,
            String preset,
            final boolean isWorkshopMode,
            boolean isScroll , float tableW, float tableH, float tableX, float tableY) {
        displayBionicTableWithKeyPresetHighLight(
                creatorComponent,
                creatorComponentTooltip, preset,
                isWorkshopMode, isScroll,
                tableW, tableH,
                tableX, tableY,
                "",
                false
        );
    }

    /**
     * This one highlight LIMB if needed
     * @param creatorComponent
     * @param creatorComponentTooltip
     * @param isWorkshopMode
     * @param isScroll
     * @param tableW
     * @param tableH
     * @param tableX
     * @param tableY
     */
    protected void displayBionicTableWithKeyPresetHighLight(
            ba_component creatorComponent,
            String creatorComponentTooltip,
            String preset,
            final boolean isWorkshopMode,
            boolean isScroll , float tableW, float tableH, float tableX, float tableY, String highlightLimbGroupID, boolean isEdit) {
        final float pad = 10f;
        float opad = 10f;
        final Color h = Misc.getHighlightColor();
        final Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;
        String keyPreset = "";
        if(preset != "") {
            keyPreset = preset + "_";
        }

        String infoPersonBionicTooltipKey = "PERSON_INFO_BIONICS_TOOLTIP";
        String infoPersonBionicPanelKey = keyPreset+"PERSON_INFO_BIONICS_PANEL";
        ba_component infoPersonBionicContainer = new ba_component(componentMap, creatorComponent.mainPanel, tableW, tableH, tableX, tableY, !isScroll, infoPersonBionicPanelKey);
        TooltipMakerAPI infoPersonBionicTooltipContainer = infoPersonBionicContainer.createTooltip(infoPersonBionicTooltipKey, tableW, tableH, isScroll, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonBionicPanelKey, infoPersonBionicContainer, tableX, tableY);

        //table header
        String tableHeaderTooltipContainerKey = "BIONIC_TABLE_HEADER_TOOLTIP";
        String tableHeaderPanelContainerKey = keyPreset + "BIONIC_TABLE_HEADER_PANEL";
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

        UIComponentAPI borderHeader = tableHeaderDisplayContainerTooltip.createRect(Misc.getDarkPlayerColor(), 1);
        borderHeader.getPosition().setSize(tableHeaderW, tableHeaderH);
        borderHeader.getPosition().inTL(0,0);
        tableHeaderDisplayContainerTooltip.addComponent(borderHeader);
        //>Limb
        LabelAPI limbHeader = tableHeaderDisplayContainerTooltip.addPara("LIMB", 0, Misc.getBrightPlayerColor(), "LIMB");
        limbHeader.getPosition().setSize(limbW, tableHeaderH);
        limbHeader.getPosition().inTL(pad + 5, 0);
        limbHeader.setAlignment(Alignment.LMID);
        //>Bionic
        LabelAPI bionicNameHeader = tableHeaderDisplayContainerTooltip.addPara("BIONIC", 0, Misc.getBrightPlayerColor(), "BIONIC");
        bionicNameHeader.getPosition().setSize(bionicNameW, tableHeaderH);
        bionicNameHeader.getPosition().inTL(bionicNameX + 5, 0);
        bionicNameHeader.setAlignment(Alignment.LMID);
        //>BRM
        LabelAPI bionicBRMHeader = tableHeaderDisplayContainerTooltip.addPara("BRM", 0, Misc.getBrightPlayerColor(), "BRM");
        bionicBRMHeader.getPosition().setSize(bionicBRMW, tableHeaderH);
        bionicBRMHeader.getPosition().inTL(bionicBRMX + bionicNameX,0);
        bionicBRMHeader.setAlignment(Alignment.MID);
        //>Conscious
        LabelAPI bionicConsciousHeader = tableHeaderDisplayContainerTooltip.addPara(ba_consciousmanager.getDisplayConditionLabel(currentPerson).toUpperCase(), 0, Misc.getBrightPlayerColor(), ba_consciousmanager.getDisplayConditionLabel(currentPerson).toUpperCase());
        bionicConsciousHeader.getPosition().setSize(bionicConsciousW, tableHeaderH);
        bionicConsciousHeader.getPosition().inTL(bionicConsciousX + bionicNameX - pad,0);
        bionicConsciousHeader.setAlignment(Alignment.RMID);

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
        for(final ba_officermanager.ba_bionicAugmentedData augmentData: currentAnatomyList) {
            String bionicTooltipContainerKey = "BIONIC_TOOLTIP_CONTAINER";
            String bionicPanelContainerKey = keyPreset + "BIONIC_PANEL_CONTAINER_"+i;
            int singleBionicInstalledNameH = 40;
            int bionicH = singleBionicInstalledNameH;
            //add a extra line for the overclock
            if(augmentData.bionicInstalled != null && (ba_overclockmanager.isBionicOverclockable(augmentData.bionicInstalled) || isEdit)) {
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
            ButtonAPI areaChecker = personDisplayContainerTooltip.addAreaCheckbox("", null,Misc.getDarkPlayerColor().brighter(), Misc.getDarkPlayerColor().darker().darker(), Misc.getBrightPlayerColor(), bionicW, bionicH, 0);
            addButtonToList(areaChecker, "hover_bionic_table_limb:"+augmentData.limb.limbId + (augmentData.bionicInstalled != null ? ":"+augmentData.bionicInstalled.getId() : ""));
            areaChecker.getPosition().setLocation(0,0).inTL(0, 0);
            if(this.currentSelectedLimb != null) {
                if(this.currentSelectedLimb.limbId.equals(augmentData.limb.limbId)) {
                    areaChecker.highlight();
                }
            }
            if(isEdit) {
                areaChecker.setHighlightBrightness(0);
                areaChecker.setHighlightBounceDown(false);
                areaChecker.setEnabled(false);
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
                    if(isEdit && augmentData.bionicInstalled != null) {
                        tooltip.addSectionHeading("Removal", Misc.getNegativeHighlightColor(), Misc.getDarkPlayerColor().darker() ,Alignment.MID, 0);
                        if(augmentData.bionicInstalled != null && augmentData.bionicInstalled.isEffectAppliedAfterRemove) {
                            augmentData.bionicInstalled.getLongOnRemoveEffectDescription(tooltip);
                        } else {
                            String warnText = augmentData.bionicInstalled.isAllowedRemoveAfterInstall ? "No effect on remove" : "Can't be removed";
                            LabelAPI warnLabel = tooltip.addPara(warnText, pad);
                            warnLabel.setHighlight(warnText);
                            warnLabel.setHighlightColors(!augmentData.bionicInstalled.isAllowedRemoveAfterInstall? Misc.getNegativeHighlightColor(): Misc.getGrayColor().brighter());
                        }
                    }
                    tooltip.addSpacer(pad);
                    tooltip.addSectionHeading("Augmentation", Misc.getBrightPlayerColor(), Misc.getDarkPlayerColor().darker() ,Alignment.MID, 0);
                    tooltip.addPara(augmentData.limb.name + ": " + augmentData.limb.description, pad);
                    if(augmentData.bionicInstalled != null) {
                        ba_bionicitemplugin b = augmentData.bionicInstalled;
                        b.displayEffectDescription(tooltip, currentPerson, b, false);
                        //---------Overclock
                        if(ba_overclockmanager.isBionicOverclockable(b)) {
                            ba_overclock overclock = augmentData.appliedOverclock;
                            if(overclock != null) {
//                                LabelAPI overclockLabel = tooltip.addPara("%s %s: %s", pad, t, overclock.name, "[O]" , !overclock.description.equals("")? overclock.description: "No description for now...");
//                                overclockLabel.setHighlightColors(h, special,  Misc.getTextColor());
                                overclock.displayEffectDescription(tooltip, currentPerson, currentSelectedBionic, true);
                            } else {
                                LabelAPI overclockLabel = tooltip.addPara("%s %s", pad, t,"Overclock:", "None active");
                                overclockLabel.setHighlight("Overclock:", "None active");
                                overclockLabel.setHighlightColors(special, g);
                            }
                        }
                        if(isWorkshopMode) {
                            //---------Conflicts
                            StringBuilder conflictsList = new StringBuilder();
                            for (ba_bionicitemplugin augmentData: ba_bionicmanager.getListBionicConflicts(b)) {
                                conflictsList.append(augmentData.getName()).append(", ");
                            }
                            if(conflictsList.length() > 0) {
                                conflictsList.setLength(conflictsList.length() - 2);
                            } else {
                                conflictsList.append("None");
                            }
                            LabelAPI conflictListLabel = tooltip.addPara("%s %s", pad, t,"Conflicts:", conflictsList.toString());
                            conflictListLabel.setHighlight("Conflicts:", conflictsList.toString());
                            conflictListLabel.setHighlightColors(g.brighter().brighter(), conflictsList.toString().equals("None")? g : Misc.getNegativeHighlightColor());
                            if(!augmentData.bionicInstalled.isAllowedRemoveAfterInstall) {
                                LabelAPI removableLabel = tooltip.addPara("%s", pad, t,"[ UNREMOVEABLE ]");
                                removableLabel.setHighlightColors(bad);
                            }
                        }
                        if(expanded) {
//                                if(!isWorkshopMode) {
//                                    b.displayEffectDescription(tooltip, currentPerson, b);
////                                    LabelAPI expandedTooltip = tooltip.addPara("%s %s", pad, Misc.getBasePlayerColor(), "Effects:", effect);
////                                    expandedTooltip.setHighlight("Effects:", effect);
////                                    expandedTooltip.setHighlightColors(Misc.getGrayColor().brighter(), b != null ? Misc.getHighlightColor() :Misc.getGrayColor());
//                                } else {
//
//                                }
                            LabelAPI expandedTooltip = tooltip.addPara("%s %s", pad, Misc.getBasePlayerColor(), "Description:", b.getSpec().getDesc());
                            expandedTooltip.setHighlight("Description:", b.getSpec().getDesc());
                            expandedTooltip.setHighlightColors(Misc.getGrayColor().brighter(), t);
                        }
                        tooltip.addSpacer(pad);
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
            LabelAPI limbName = bionicLimbNameTooltip.addPara(augmentData.limb.name, pad);
            limbName.setHighlight(augmentData.limb.name);
            if(highlightLimbGroupID != "") {
                List<ba_limbmanager.ba_limb> limbList = ba_limbmanager.getListLimbFromGroup(highlightLimbGroupID);
                for (ba_limbmanager.ba_limb limb: limbList) {
                    if(limb.limbId.equals(augmentData.limb.limbId)) {
                        limbName.setHighlightColors(Misc.getPositiveHighlightColor());
                        break;
                    } else {
                        limbName.setHighlightColors(t);
                    }
                }
            } else {
                limbName.setHighlightColors(t);
            }
            limbName.getPosition().inTL(pad/2,12);

            //---------Bionic
            int bionicInstalledI = 0;
            if(augmentData.bionicInstalled != null) {
                ba_bionicitemplugin b = augmentData.bionicInstalled;
                int sectionH = singleBionicInstalledNameH;
                int sectionW = bionicRowW;
                int sectionX = bionicRowX;
                int sectionSpacerY = singleBionicInstalledNameH * bionicInstalledI;
                TooltipMakerAPI bionicNameTooltip = bionicDisplayContainer.createTooltip("BIONIC_NAME"+bionicInstalledI, sectionW, sectionH, false, sectionX, sectionSpacerY);
                bionicNameTooltip.getPosition().inTL(sectionX, sectionSpacerY);
                //>name
                LabelAPI bionicName = bionicNameTooltip.addPara("%s  -  %s", pad, g, "" + b.getName(), !Objects.equals(b.namePrefix, "") ? b.namePrefix: " ");
                bionicName.getPosition().setSize(bionicNameW,sectionH);
                bionicName.setHighlightColors(b.displayColor, Misc.getBasePlayerColor());
                bionicName.getPosition().inTL(0,12);
                //>BRM
//                int brmX = (int) (bionicName.getPosition().getWidth());
                LabelAPI bionicBRM = bionicNameTooltip.addPara("" + Math.round(b.brmCost), pad);
                bionicBRM.getPosition().setSize(bionicBRMW,sectionH);
                bionicBRM.setHighlight("" + Math.round(b.brmCost));
                bionicBRM.setHighlightColors(Misc.getBrightPlayerColor());
                bionicBRM.getPosition().inTL(bionicBRMX + bionicBRMW/2, 12);
                //>Conscious
//                int consX = (int) (bionicBRM.getPosition().getWidth() + brmX);
                LabelAPI bionicConscious = bionicNameTooltip.addPara("" + Math.round(b.consciousnessCost * 100) + "%", pad);
                bionicConscious.getPosition().setSize(bionicConsciousW,sectionH);
                bionicConscious.setHighlight("" + Math.round(b.consciousnessCost * 100) + "%");
                bionicConscious.setHighlightColors(Misc.getNegativeHighlightColor());
                bionicConscious.setAlignment(Alignment.TR);
                bionicConscious.getPosition().inTL(bionicConsciousX - pad, 12);
                if(b != null) {
                    ba_overclock overclock = augmentData.appliedOverclock;
                    int overclockRowY = singleBionicInstalledNameH;
                    if(ba_overclockmanager.isBionicOverclockable(b)) {
                        TooltipMakerAPI overclockTooltip = bionicDisplayContainer.createTooltip("BIONIC_OVERCLOCK_NAME", sectionW, sectionH, false, sectionX, overclockRowY);
                        overclockTooltip.getPosition().inTL(sectionX, overclockRowY);
                        LabelAPI overclockName = overclockTooltip.addPara("[ %s ]", pad, h, overclock != null? overclock.name: "--------");
                        overclockName.setHighlight("[",overclock != null? overclock.name: "--------", "]");
                        overclockName.setHighlightColors(special, overclock != null ? h: g, special);
                        overclockName.getPosition().setSize(bionicNameW,sectionH);
                        overclockName.getPosition().inTL(0, 12);
                    }
                    if (isEdit) {
                        TooltipMakerAPI btnTooltip = bionicDisplayContainer.createTooltip("BIONIC_EDIT_BTN", sectionW, sectionH, false, sectionX, overclockRowY);
                        btnTooltip.getPosition().inTL(bionicConsciousX + bionicConsciousW + opad*3, overclockRowY);
                        ButtonAPI editBtn = btnTooltip.addButton("Remove", null, Misc.getTextColor(), Misc.getNegativeHighlightColor().darker().darker(), bionicConsciousW, sectionH - pad, 0);
                        String btmMapValuePrefix = b.isEffectAppliedAfterRemove? "remove":"removeConfirm";
                        addButtonToList(editBtn, "bionic:" + btmMapValuePrefix +":" + b.bionicId + ":" + augmentData.limb.limbId);
                        if(!b.isAllowedRemoveAfterInstall) {
                            editBtn.setEnabled(false);
                        }
                        if(b.isEffectAppliedAfterRemove && this.currentRemovingBionic != null) {
                            editBtn.setEnabled(false);
                        }
                        if(b.isEffectAppliedAfterRemove) {
                            ButtonAPI editConfirmBtn = btnTooltip.addButton("Confirm", null, Misc.getTextColor(), Misc.getNegativeHighlightColor().darker(), bionicConsciousW, sectionH - pad, 0);
                            editConfirmBtn.getPosition().inTL(-bionicConsciousW-pad/2, 0);
                            addButtonToList(editConfirmBtn, "bionic:removeConfirm:"+b.bionicId + ":" + augmentData.limb.limbId);
                            editConfirmBtn.setEnabled(!b.isEffectAppliedAfterRemove || (this.currentRemovingBionic != null && this.currentRemovingBionic.bionicId.equals(b.bionicId)));
//                            editConfirmBtn.getPosition().inTL(editBtn.getPosition().getWidth() - editBtn.getPosition().getX() - pad, overclockRowY);
                        }
                    }
                    //>name
                }

                bionicInstalledI++;
            }
            i++;
        }
        if(i==0) { //empty || have no synthetic body || something went horribly wrong
            //table header
            String tableEmptyTooltipContainerKey = "BIONIC_TABLE_EMPTY_TOOLTIP";
            String tableEmptyPanelContainerKey = keyPreset + "BIONIC_TABLE_EMPTY_PANEL";
            int tableEmptyH = 240;
            int tableEmptyW = (int) (tableW - pad);
            //--------bionic container
            ba_component tableEmptyDisplayContainer = new ba_component(componentMap, infoPersonBionicContainer.mainPanel, tableEmptyW, tableEmptyH,0,0,false, tableEmptyPanelContainerKey);
            TooltipMakerAPI tableEmptyDisplayContainerTooltip = tableEmptyDisplayContainer.createTooltip(tableEmptyTooltipContainerKey, tableEmptyW, tableEmptyH, false, 0,0);
            tableEmptyDisplayContainerTooltip.setForceProcessInput(true);
            //attach to have the main tooltip scroll effect this component's panel
            infoPersonBionicContainer.attachSubPanel(infoPersonBionicTooltipKey, infoPersonBionicPanelKey, tableEmptyDisplayContainer);

            UIComponentAPI borderEmpty = tableEmptyDisplayContainerTooltip.createRect(Misc.getDarkPlayerColor().darker().darker(), 1);
            borderEmpty.getPosition().setSize(tableEmptyW, tableEmptyH);
            borderEmpty.getPosition().inTL(0,0);
            tableEmptyDisplayContainerTooltip.addComponent(borderEmpty);
            //>Limb
            LabelAPI emptyHeader = tableEmptyDisplayContainerTooltip.addPara("No Augmentation Information found", 0, Misc.getDarkPlayerColor(), "No Augmentation Information found");
            emptyHeader.getPosition().setSize(tableEmptyW, tableEmptyH);
            emptyHeader.getPosition().inTL(pad + 5, 0);
            emptyHeader.setAlignment(Alignment.MID);
        }
        infoPersonBionicContainer.subComponentListMap.put("SUB_BIONIC_LIST", subComponentBionicList);
        if(isScroll) {
            infoPersonBionicContainer.mainPanel.addUIElement(infoPersonBionicTooltipContainer);
            if(infoPersonBionicTooltipContainer.getExternalScroller() != null) {
                infoPersonBionicTooltipContainer.getExternalScroller().setYOffset(currentScrollPositionBionicTable);
            }
        }
    }
    protected void displayBioformTableWithKeyPreset(
            ba_component creatorComponent,
            String creatorComponentTooltip,
            String preset,
            boolean isScroll , float tableW, float tableH, float tableX, float tableY) {
        final float pad = 10f;
        float opad = 10f;
        final Color h = Misc.getHighlightColor();
        final Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;
        String keyPreset = "";
        if(preset != "") {
            keyPreset = preset + "_";
        }

        String infoPersonBionicTooltipKey = "PERSON_INFO_BIOFORM_TOOLTIP";
        String infoPersonBionicPanelKey = keyPreset+"PERSON_INFO_BIOFORM_PANEL";
        ba_component infoPersonBionicContainer = new ba_component(componentMap, creatorComponent.mainPanel, tableW, tableH, tableX, tableY, !isScroll, infoPersonBionicPanelKey);
        TooltipMakerAPI infoPersonBionicTooltipContainer = infoPersonBionicContainer.createTooltip(infoPersonBionicTooltipKey, tableW, tableH, isScroll, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonBionicPanelKey, infoPersonBionicContainer, tableX, tableY);

        if(this.currentPerson.isAICore()) {
            ba_officermanager.ba_aimemorydata aimemorydata = ba_officermanager.getAIMemData(this.currentPerson, Global.getSector().getCampaignUI().getCurrentInteractionDialog());
            //if AI person have the bioform data
            if(!aimemorydata.anatomy.isEmpty() && !aimemorydata.shell.equals(ba_variablemanager.BA_SYNTHETIC_BODY_HULLMOD)) {
                this.currentBioformData = aimemorydata.anatomy;
            }
            //if AI person have not set up bioform data
            if(this.currentBioformData == null || this.currentBioformData.isEmpty() ) {
                //todo: set a price for creating new bioform
                LabelAPI loreBioformLabel = infoPersonBionicTooltipContainer.addPara("//SCANNING// ...  Synthetic bioform is not detected in current unit ...", 0f, ba_variablemanager.BA_OVERFORM_COLOR, "");
                loreBioformLabel.getPosition().inTL(tableW/2 - loreBioformLabel.computeTextWidth("//SCANNING// ...  Synthetic bioform is not detected in current unit")/2, tableH/2 - pad*3);
                LabelAPI createBioformLabel = infoPersonBionicTooltipContainer.addPara("//%s", 0f, ba_variablemanager.BA_OVERFORM_COLOR, "CREATE NEW BIOFORM ?");
                createBioformLabel.getPosition().inTL(tableW/2 - createBioformLabel.computeTextWidth("//CREATE NEW BIOFORM ?")/2, tableH/2);
                //Create button
                int bioformBtnH = 40;
                int bioformBtnW = (int) (120);
                int bioformBtnX = (int) (tableW/2);
                int bioformBtnY = (int) (tableH/2 + pad*3);
                ButtonAPI bioformButton = infoPersonBionicTooltipContainer.addButton("Confirm", null, Misc.getTextColor(), Misc.getPositiveHighlightColor().darker().darker(), Alignment.MID, CutStyle.TL_BR,  bioformBtnW, bioformBtnH, 0);
                bioformButton.getPosition().inTL(bioformBtnX - bioformBtnW/2,bioformBtnY);
                bioformButton.setShortcut(Keyboard.KEY_G, true);
                addButtonToList(bioformButton, "bioform:createBaselineVariant");
            } else {
                //todo: display bioform stuffs
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
                //display the entire bioform list
                int i = 0;
                List<ba_component> subComponentBionicList = new ArrayList<>();
                for(final ba_officermanager.ba_bionicAugmentedData augmentData: this.currentBioformData) {
                    String bionicTooltipContainerKey = "BIONIC_TOOLTIP_CONTAINER";
                    String bionicPanelContainerKey = keyPreset + "BIONIC_PANEL_CONTAINER_"+i;
                    int singleBionicInstalledNameH = 40;
                    int bionicH = singleBionicInstalledNameH;
                    //add a extra line for the overclock
//                    if(augmentData.bionicInstalled != null && (ba_overclockmanager.isBionicOverclockable(augmentData.bionicInstalled) || isEdit)) {
//                        bionicH += singleBionicInstalledNameH;
//                    }
                    int bionicW = (int) (tableW - pad);
                    int bionicX = 0;
                    int bionicY = 0;
                    //--------bionic container
                    ba_component bionicDisplayContainer = new ba_component(componentMap, infoPersonBionicContainer.mainPanel, bionicW, bionicH,bionicX,bionicY,false, bionicPanelContainerKey);
                    TooltipMakerAPI personDisplayContainerTooltip = bionicDisplayContainer.createTooltip(bionicTooltipContainerKey, bionicW, bionicH, false, 0,0);
                    personDisplayContainerTooltip.setForceProcessInput(true);
                    //attach to have the main tooltip scroll effect this component's panel
                    infoPersonBionicContainer.attachSubPanel(infoPersonBionicTooltipKey, infoPersonBionicPanelKey, bionicDisplayContainer);
                    subComponentBionicList.add(bionicDisplayContainer);
                    //lines
                    int lineW = 30;
                    int spacerH = (int) (pad + pad/2);
                    if(!ba_limbmanager.isLimbCentralLimb(augmentData.limb)) {
                        UIComponentAPI borderSelected = personDisplayContainerTooltip.createRect(ba_variablemanager.BA_OVERFORM_COLOR.darker(), 1);
                        borderSelected.getPosition().setSize(1f, singleBionicInstalledNameH + spacerH);
                        borderSelected.getPosition().inTL(lineW/2, (i*(singleBionicInstalledNameH + spacerH)));
                        int lineStraightY = 0;
                        if(i == this.currentBioformData.size()-1) {
                            borderSelected.getPosition().setSize(1f, singleBionicInstalledNameH/2);
                        }
                        if(i == 0) {
                            borderSelected.getPosition().setSize(1f, singleBionicInstalledNameH/2+spacerH);
                            lineStraightY = singleBionicInstalledNameH/2;
                        }
                        personDisplayContainerTooltip.addCustomDoNotSetPosition(borderSelected).getPosition().inTL(lineW/2,lineStraightY);
                        UIComponentAPI border2 = personDisplayContainerTooltip.createRect(ba_variablemanager.BA_OVERFORM_COLOR.darker(), 1);
                        border2.getPosition().setSize(lineW/2 + pad/2, 1f);
//                        border2.getPosition().inTL(lineW/2, i*(singleBionicInstalledNameH + spacerH) + singleBionicInstalledNameH/2);
                        personDisplayContainerTooltip.addCustomDoNotSetPosition(border2).getPosition().inTL(lineW/2,singleBionicInstalledNameH/2);
                    } else {
                        UIComponentAPI borderSelected = personDisplayContainerTooltip.createRect(ba_variablemanager.BA_OVERFORM_COLOR.darker(), 1);
                        borderSelected.getPosition().setSize(1f, spacerH);
                        personDisplayContainerTooltip.addCustomDoNotSetPosition(borderSelected).getPosition().inTL(lineW/2, singleBionicInstalledNameH);
                        if(i == this.currentBioformData.size()-1) {
                            borderSelected.getPosition().setSize(0f, 0);
                            borderSelected.setOpacity(0);
                        }
                    }
                    //hover
                    int areaX = !ba_limbmanager.isLimbCentralLimb(augmentData.limb)? lineW: 0;
                    ButtonAPI areaChecker = personDisplayContainerTooltip.addAreaCheckbox("", null,!ba_limbmanager.isLimbCentralLimb(augmentData.limb)? Misc.getDarkPlayerColor().brighter(): ba_variablemanager.BA_OVERFORM_COLOR.darker(), !ba_limbmanager.isLimbCentralLimb(augmentData.limb)? Misc.getDarkPlayerColor(): ba_variablemanager.BA_OVERFORM_COLOR.darker(), Misc.getBrightPlayerColor(), bionicW - areaX, bionicH, 0);
                    addButtonToList(areaChecker, "bioform:remove:"+augmentData.limb.limbId + (augmentData.bionicInstalled != null ? ":"+augmentData.bionicInstalled.getId() : ""));
                    areaChecker.getPosition().setLocation(0,0).inTL(areaX, 0);
                    if(ba_limbmanager.isLimbCentralLimb(augmentData.limb)) {
                        areaChecker.setClickable(false);
                    }
                    if(!ba_limbmanager.isLimbCentralLimb(augmentData.limb) && this.bioformRemoveList.contains(augmentData.limb.limbId)) {
                        areaChecker.highlight();
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
                            tooltip.addSectionHeading("Augmentation", Misc.getBrightPlayerColor(), Misc.getDarkPlayerColor().darker() ,Alignment.MID, 0);
                            tooltip.addPara(augmentData.limb.name + ": " + augmentData.limb.description, pad);
                            if(augmentData.bionicInstalled != null) {
                                ba_bionicitemplugin b = augmentData.bionicInstalled;
                                b.displayEffectDescription(tooltip, currentPerson, b, false);
                                //---------Overclock
                                if(ba_overclockmanager.isBionicOverclockable(b)) {
                                    ba_overclock overclock = augmentData.appliedOverclock;
                                    if(overclock != null) {
//                                LabelAPI overclockLabel = tooltip.addPara("%s %s: %s", pad, t, overclock.name, "[O]" , !overclock.description.equals("")? overclock.description: "No description for now...");
//                                overclockLabel.setHighlightColors(h, special,  Misc.getTextColor());
                                        overclock.displayEffectDescription(tooltip, currentPerson, currentSelectedBionic, true);
                                    } else {
                                        LabelAPI overclockLabel = tooltip.addPara("%s %s", pad, t,"Overclock:", "None active");
                                        overclockLabel.setHighlight("Overclock:", "None active");
                                        overclockLabel.setHighlightColors(special, g);
                                    }
                                }
                                if(expanded) {
//                                if(!isWorkshopMode) {
//                                    b.displayEffectDescription(tooltip, currentPerson, b);
////                                    LabelAPI expandedTooltip = tooltip.addPara("%s %s", pad, Misc.getBasePlayerColor(), "Effects:", effect);
////                                    expandedTooltip.setHighlight("Effects:", effect);
////                                    expandedTooltip.setHighlightColors(Misc.getGrayColor().brighter(), b != null ? Misc.getHighlightColor() :Misc.getGrayColor());
//                                } else {
//
//                                }
                                    LabelAPI expandedTooltip = tooltip.addPara("%s %s", pad, Misc.getBasePlayerColor(), "Description:", b.getSpec().getDesc());
                                    expandedTooltip.setHighlight("Description:", b.getSpec().getDesc());
                                    expandedTooltip.setHighlightColors(Misc.getGrayColor().brighter(), t);
                                }
                                tooltip.addSpacer(pad);
                            } else {
                                tooltip.addPara("No bionic installed", pad, Misc.getGrayColor(), "No bionic installed");
                                tooltip.addSpacer(pad);
                            }
                        }
                    }, TooltipMakerAPI.TooltipLocation.BELOW);
                    //---------Limb Name
                    int nameH = bionicH;
                    int nameW = limbW - (!ba_limbmanager.isLimbCentralLimb(augmentData.limb)? lineW: 0);
                    int nameX = limbX + (!ba_limbmanager.isLimbCentralLimb(augmentData.limb)? lineW: 0);
                    TooltipMakerAPI bionicLimbNameTooltip = bionicDisplayContainer.createTooltip("BIONIC_LIMB_NAME", nameW, nameH, false, 0, 0);
                    bionicLimbNameTooltip.getPosition().inTL(nameX, 0);
                    String limbText = augmentData.limb.name;
                    if(!ba_limbmanager.isLimbCentralLimb(augmentData.limb) && this.bioformRemoveList.contains(augmentData.limb.limbId)) {
                        limbText += " (-)";
                    }
                    if(!ba_limbmanager.isLimbCentralLimb(augmentData.limb) && this.bioformAddList.contains(augmentData.limb.limbId)) {
                        limbText += " (+)";
                    }
                    LabelAPI limbName = bionicLimbNameTooltip.addPara(limbText, pad);
                    limbName.setHighlight(limbText);
                    limbName.setHighlightColors(t);
                    //todo: disable/enable the limb if selected
                    if(this.bioformAddList.contains(augmentData.limb.limbId)) {
                        limbName.setHighlightColors(Misc.getPositiveHighlightColor());
                    }
                    if(this.bioformRemoveList.contains(augmentData.limb.limbId)) {
                        limbName.setHighlightColors(Misc.getNegativeHighlightColor());
                    }
                    limbName.getPosition().inTL(pad/2,12);

                    //---------Bionic
                    int bionicInstalledI = 0;
                    if(augmentData.bionicInstalled != null) {
                        ba_bionicitemplugin b = augmentData.bionicInstalled;
                        int sectionH = singleBionicInstalledNameH;
                        int sectionW = bionicRowW;
                        int sectionX = bionicRowX;
                        int sectionSpacerY = singleBionicInstalledNameH * bionicInstalledI;
                        TooltipMakerAPI bionicNameTooltip = bionicDisplayContainer.createTooltip("BIONIC_NAME"+bionicInstalledI, sectionW, sectionH, false, sectionX, sectionSpacerY);
                        bionicNameTooltip.getPosition().inTL(sectionX, sectionSpacerY);
                        //>name
                        LabelAPI bionicName = bionicNameTooltip.addPara("%s  -  %s", pad, g, "" + b.getName(), !Objects.equals(b.namePrefix, "") ? b.namePrefix: " ");
                        bionicName.getPosition().setSize(bionicNameW,sectionH);
                        bionicName.setHighlightColors(b.displayColor, Misc.getBasePlayerColor());
                        bionicName.getPosition().inTL(0,12);
                        //>BRM
//                int brmX = (int) (bionicName.getPosition().getWidth());
                        LabelAPI bionicBRM = bionicNameTooltip.addPara("" + Math.round(b.brmCost), pad);
                        bionicBRM.getPosition().setSize(bionicBRMW,sectionH);
                        bionicBRM.setHighlight("" + Math.round(b.brmCost));
                        bionicBRM.setHighlightColors(Misc.getBrightPlayerColor());
                        bionicBRM.getPosition().inTL(bionicBRMX + bionicBRMW/2, 12);
                        //>Conscious
//                int consX = (int) (bionicBRM.getPosition().getWidth() + brmX);
                        LabelAPI bionicConscious = bionicNameTooltip.addPara("" + Math.round(b.consciousnessCost * 100) + "%", pad);
                        bionicConscious.getPosition().setSize(bionicConsciousW,sectionH);
                        bionicConscious.setHighlight("" + Math.round(b.consciousnessCost * 100) + "%");
                        bionicConscious.setHighlightColors(Misc.getNegativeHighlightColor());
                        bionicConscious.setAlignment(Alignment.TR);
                        bionicConscious.getPosition().inTL(bionicConsciousX - pad, 12);
                        if(b != null) {
                            ba_overclock overclock = augmentData.appliedOverclock;
                            int overclockRowY = singleBionicInstalledNameH;
                            if(ba_overclockmanager.isBionicOverclockable(b)) {
                                TooltipMakerAPI overclockTooltip = bionicDisplayContainer.createTooltip("BIONIC_OVERCLOCK_NAME", sectionW, sectionH, false, sectionX, overclockRowY);
                                overclockTooltip.getPosition().inTL(sectionX, overclockRowY);
                                LabelAPI overclockName = overclockTooltip.addPara("[ %s ]", pad, h, overclock != null? overclock.name: "--------");
                                overclockName.setHighlight("[",overclock != null? overclock.name: "--------", "]");
                                overclockName.setHighlightColors(special, overclock != null ? h: g, special);
                                overclockName.getPosition().setSize(bionicNameW,sectionH);
                                overclockName.getPosition().inTL(0, 12);
                            }
                            //>name
                        }
                    }
                    i++;
                    infoPersonBionicTooltipContainer.addSpacer(spacerH);
                }
            }
        }
        if(isScroll) {
            infoPersonBionicContainer.mainPanel.addUIElement(infoPersonBionicTooltipContainer);
            if(infoPersonBionicTooltipContainer.getExternalScroller() != null) {
                infoPersonBionicTooltipContainer.getExternalScroller().setYOffset(currentScrollPositionBioformList);
            }
        }
    }
    protected void displayPersonList(
            ba_component creatorComponent,
            String creatorComponentTooltip,
            boolean isDisplayingOtherFleets,
            float personListW, float personListH,
            float personListX, float personListY
    ) {
        displayPersonListWithKeyPreset(creatorComponent, creatorComponentTooltip, "", isDisplayingOtherFleets, personListW, personListH, personListX, personListY);
    }
    protected void displayPersonListWithKeyPreset(
            ba_component creatorComponent,
            String creatorComponentTooltip,
            String preset,
            boolean isDisplayingOtherFleets,
            float personListW, float personListH,
            float personListX, float personListY
    ) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();
        String keyPreset = "";
        if(preset != "") {
            keyPreset = preset + "_";
        }

        //overview personContainer
        String overviewPersonTooltipKey = keyPreset+"PERSON_LIST_TOOLTIP";
        String overviewPersonPanelKey = keyPreset+"PERSON_LIST_PANEL";
        ba_component overviewPersonContainer = new ba_component(componentMap, creatorComponent.mainPanel, personListW, personListH,personListX, personListY, true, overviewPersonPanelKey);
        TooltipMakerAPI overviewPersonTooltipContainer = overviewPersonContainer.createTooltip(overviewPersonTooltipKey, personListW, personListH, true, 0, 0);
        //important to set the container tooltip to have scroll enable if you want scroll
        //Next important is to have panel.addUI at the bottom of the code if you have scroll enabled, or the scroll wont work
        creatorComponent.attachSubPanel(creatorComponentTooltip,overviewPersonPanelKey, overviewPersonContainer);

        int i = 0;
        int imageH = 80;
        int imageW = 80;
        int ySpacer = 10;
        float personW = personListW - 10 * 2; //time 2 for the padding both left and right
        float personH = imageH + 20;
        List<ba_component> subComponentPersonList = new ArrayList<>();
        for (PersonAPI member: ba_officermanager.listPersons) {
            String defaultPersonTooltipContainerKey = "PERSON_TOOLTIP_CONTAINER";
            String defaultPersonPanelContainerKey = "PERSON_PANEL_CONTAINER_"+i;
            //add first spacer
            if(subComponentPersonList.size() == 0) {
                overviewPersonTooltipContainer.addSpacer(ySpacer);
            }
            //--------person container
//            ba_component personDisplayContainer = new ba_component(componentMap, overviewPersonContainer.mainPanel, personW, personH,0,0,false, defaultPersonPanelContainerKey);
//            TooltipMakerAPI personDisplayContainerTooltip = personDisplayContainer.createTooltip(defaultPersonTooltipContainerKey, personW, personH, false, 0,0);
//            personDisplayContainerTooltip.setForceProcessInput(true);
//            //attach to have the main tooltip scroll effect this component's panel
//            overviewPersonContainer.attachSubPanel(overviewPersonTooltipKey, defaultPersonPanelContainerKey,personDisplayContainer);
//            subComponentPersonList.add(personDisplayContainer);

            ba_component personDisplayer = displaySinglePersonFromList(
                    member,
                    overviewPersonContainer,
                    overviewPersonTooltipKey,
                    isDisplayingOtherFleets,
                    defaultPersonPanelContainerKey,
                    defaultPersonTooltipContainerKey,
                    imageW, imageH,
                    personW, personH,
                    0,0
                    );
            subComponentPersonList.add(personDisplayer);
            //Monthly Salary
            //Assign to ship/planet
            //--------Spacer because scroller dont like position offseting as spacing
            overviewPersonTooltipContainer.addSpacer(ySpacer);
            i++;
        }
        overviewPersonContainer.subComponentListMap.put("SUB_PERSON_LIST", subComponentPersonList);
        //do the adding late so the scroll work (thanks Lukas04)
        overviewPersonContainer.mainPanel.addUIElement(overviewPersonTooltipContainer);
        if(overviewPersonTooltipContainer.getExternalScroller() != null) {
            overviewPersonTooltipContainer.getExternalScroller().setYOffset(currentScrollPositionPersonList);
        }
    }
    protected void displaySinglePersonFromList(
            PersonAPI member,
            ba_component creatorComponent,
            String creatorComponentTooltip,
            boolean isDisplayingOtherFleets,
            float pW, float pH,
            float pX, float pY
    ) {
        float imageH = 80;
        float imageW = 80;
        String defaultSinglePersonPanelKey = "DEFAULT_PERSON_PANEL_CONTAINER";
        String defaultSinglePersonTooltipKey = "DEFAULT_PERSON_TOOLTIP_CONTAINER";
        displaySinglePersonFromList(
                member, creatorComponent, creatorComponentTooltip,
                isDisplayingOtherFleets,
                defaultSinglePersonPanelKey, defaultSinglePersonTooltipKey,
                imageW, imageH,
                pW, pH,
                pX, pY
        );
    }
    //todo: add disabling/only activate when clicking/string prefix
    protected ba_component displaySinglePersonFromList(
            PersonAPI member,
            ba_component creatorComponent,
            String creatorComponentTooltip,
            boolean isDisplayingOtherFleets,
            String componentPanelKey,
            String componentTooltipKey,
            float imageW, float imageH,
            float pW, float pH,
            float pX, float pY
    ) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();
        String spriteName = member.getPortraitSprite();

        ba_component personDisplayContainer = new ba_component(componentMap, creatorComponent.mainPanel, pW, pH, pX, pY,false, componentPanelKey);
        TooltipMakerAPI personDisplayContainerTooltip = personDisplayContainer.createTooltip(componentTooltipKey, pW, pH, false, 0,0);
        personDisplayContainerTooltip.setForceProcessInput(true);
        //attach to have the main tooltip scroll effect this component's panel
        //important that this doesn't set the location of the attaching component
        creatorComponent.attachSubPanel(creatorComponentTooltip, componentPanelKey,personDisplayContainer);
//        subComponentPersonList.add(personDisplayContainer);
        //hover
        ButtonAPI areaChecker = personDisplayContainerTooltip.addAreaCheckbox("", null,Color.red.darker(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), pW, pH, 0);
        addButtonToList(areaChecker, "hover_person:"+member.getId());
        areaChecker.getPosition().setLocation(0,0).inTL(0, 0);
        //--------image
        int imageX = (int) 0;
        TooltipMakerAPI personImageTooltip = personDisplayContainer.createTooltip("PERSON_IMAGE", imageW, imageH, false, 0, 0);
        personImageTooltip.getPosition().inTL(imageX, 0);
        personImageTooltip.addImage(spriteName, imageW, imageH, 0);
        personImageTooltip.getPosition().inTL(0, (pH - imageH ) / 2);
        //---------Name
        int nameH = 30;
        int nameW = (int) (pW - imageW - 30);
        int nameX = (int) (imageX + imageW + pad);
        TooltipMakerAPI personNameTooltip = personDisplayContainer.createTooltip("PERSON_NAME", nameW, nameH, false, 0, 0);
        personNameTooltip.getPosition().inTL(nameX, 0);
        LabelAPI name = personNameTooltip.addPara(member.getName().getFullName() + (member.isPlayer() ? " (" + "You" + ")": ""), pad);
        name.setHighlight(member.getName().getFullName());
        name.setHighlightColors(Misc.getBrightPlayerColor());
        //Personality
        //BRM (Bionic Rights Management)
        int brmH = 30;
        int brmW = 120;
        int brmX = (int) (nameX);
        int brmY = (int) (nameH);
        int currentBRM = ba_officermanager.getCurrentBRM(member);
        int limitBRM = ba_officermanager.getLimitBRM(member);
        TooltipMakerAPI personBRMTooltip = personDisplayContainer.createTooltip("PERSON_BRM", brmW, brmH, false, 0, 0);
        personBRMTooltip.getPosition().inTL(brmX, brmY);
        LabelAPI BRM;
        BRM = personBRMTooltip.addPara("BRM: " + currentBRM + " / " + limitBRM, pad);
        BRM.setHighlight("BRM: ", "" +currentBRM, "" +limitBRM);
        BRM.setHighlightColors(t,currentBRM > limitBRM ? bad: h,Misc.getBrightPlayerColor());
        if(bionicalterationplugin.isBRMCapDisable) {
            BRM.setText("BRM: " + currentBRM);
            BRM.setHighlight("BRM: ", "" +currentBRM);
            BRM.setHighlightColors(t, h);
        }
        //Level
        int levelH = brmH;
        int levelW = 100;
        int levelX = (int) (brmX + brmW);
        int levelY = brmY;
        TooltipMakerAPI personLevelTooltip = personDisplayContainer.createTooltip("PERSON_LEVEL", levelW, levelH, false, 0, 0);
        personLevelTooltip.getPosition().inTL(levelX, levelY);
        LabelAPI level = personLevelTooltip.addPara("Level: " + member.getStats().getLevel(), pad);
        level.setHighlight("Level: ","" + member.getStats().getLevel());
        level.setHighlightColors(g,h);
        //Profession: Captain/Administrator
        int profH = 20;
        int profW = 200;
        int profX = (int) (nameX);
        int profY = (int) (brmH + nameH);
        TooltipMakerAPI personProfTooltip = personDisplayContainer.createTooltip("PERSON_PROF", profW, profH, false, 9, 0);
        personProfTooltip.getPosition().inTL(profX, profY);
        String profString = ba_officermanager.getProfessionText(member, isDisplayingOtherFleets);
        LabelAPI prof = personProfTooltip.addPara("Profession: " + profString, pad);
        prof.setHighlight("Profession: ", profString);
        prof.setHighlightColors(g,h);

        return personDisplayContainer;
    }

    public void addButtonToList(ButtonAPI button, String buttonMapValue) {
        buttons.add(button);
        buttonMap.put(button, buttonMapValue);
    }
    public void saveScrollPosition() {
        //if the component have a preset, you will have to manually save position on the child class
        //these are for the case where the preset is empty string
        //Bionic table, preset = ""
        ba_component component = componentMap.get("PERSON_INFO_BIONICS_PANEL");
        if(component != null && component.tooltipMap.get("PERSON_INFO_BIONICS_TOOLTIP") != null) {
            if(component.tooltipMap.get("PERSON_INFO_BIONICS_TOOLTIP").getExternalScroller() != null) {
                currentScrollPositionBionicTable = component.tooltipMap.get("PERSON_INFO_BIONICS_TOOLTIP").getExternalScroller().getYOffset();
            }
        }
        //inventory, preset = ""
        ba_component component2 = componentMap.get("INVENTORY_PANEL");
        if(component2 != null && component2.tooltipMap.get("INVENTORY_TOOLTIP") != null) {
            if(component2.tooltipMap.get("INVENTORY_TOOLTIP").getExternalScroller() != null) {
                currentScrollPositionInventory = component2.tooltipMap.get("INVENTORY_TOOLTIP").getExternalScroller().getYOffset();
            }
        }
        //person list, preset = ""
        ba_component component3 = componentMap.get("PERSON_LIST_PANEL");
        if(component3 != null && component3.tooltipMap.get("PERSON_LIST_TOOLTIP") != null) {
            if(component3.tooltipMap.get("PERSON_LIST_TOOLTIP").getExternalScroller() != null) {
                currentScrollPositionPersonList = component3.tooltipMap.get("PERSON_LIST_TOOLTIP").getExternalScroller().getYOffset();
            }
        }
        //bioform list, preset = ""
        ba_component component4 = componentMap.get("PERSON_INFO_BIOFORM_PANEL");
        if(component4 != null && component4.tooltipMap.get("PERSON_INFO_BIOFORM_TOOLTIP") != null) {
            if(component4.tooltipMap.get("PERSON_INFO_BIOFORM_TOOLTIP").getExternalScroller() != null) {
                currentScrollPositionBioformList = component4.tooltipMap.get("PERSON_INFO_BIOFORM_TOOLTIP").getExternalScroller().getYOffset();
            }
        }
    }
    @Override
    public void positionChanged(PositionAPI position) {

    }
    protected int getCurrentLimbPartCount() {
        return this.currentBioformData.size() - this.bioformRemoveList.size();
    }
    protected int getCurrentLimbLimit() {
        return bionicalterationplugin.bioformMaxLimbCount;
    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {
//        ba_component previousTab2 = componentMap.get("MAIN_OVERCLOCk_CONTAINER");
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
//        if(previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP") != null) {
//            ba_utils.drawBox(
//                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getX(),
//                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getY(),
//                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getWidth(),
//                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getHeight(),
//                    0.3f,
//                    Color.blue
//            );
//        }
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
//                b.setChecked(false);
                //Check if click change main page
                String s = buttonMap.get(b);
                String[] tokens = s.split(":");
                if(tokens[0].equals("hover_bionic_item")) {
                    if(ba_bionicmanager.bionicItemMap.get(tokens[1]) != null) {
                        this.currentSelectedBionic = (ba_bionicitemplugin) cargoBionic.get(Integer.parseInt(tokens[2])).getPlugin();;
//                        this.currentSelectedBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
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
                        ba_component component = componentMap.get("INVENTORY_PANEL");
                        //hover bionic item in inventory
                        if(component != null && component.tooltipMap.get("INVENTORY_TOOLTIP") != null) {
                            if(tokens[0].equals("hover_bionic_item") && debounceplugin.isDebounceOver("INVENTORY_TOOLTIP", 0, component.tooltipMap.get("INVENTORY_TOOLTIP").getExternalScroller().getYOffset())) {
                                if(ba_bionicmanager.bionicItemMap.get(tokens[1]) != null && (this.currentHoveredBionic == null || !this.currentHoveredBionic.bionicId.equals(tokens[1]))) {
                                    this.currentHoveredBionic = (ba_bionicitemplugin) cargoBionic.get(Integer.parseInt(tokens[2])).getPlugin();
//                                    this.currentHoveredBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
//                                    shouldRefresh = true;
                                }
                            }
                        }
                        ba_component component1 = componentMap.get("PERSON_LIST_PANEL");
                        if(component1 != null && component1.tooltipMap.get("PERSON_LIST_TOOLTIP") != null) {
                            if(tokens[0].equals("hover_person") && debounceplugin.isDebounceOver("PERSON_LIST_TOOLTIP", 0, component1.tooltipMap.get("PERSON_LIST_TOOLTIP").getExternalScroller().getYOffset())) {
                                if(!this.currentPerson.getId().equals(tokens[1])) {
                                    for(PersonAPI person: ba_officermanager.listPersons) {
                                        if(tokens[1].equals(person.getId())) {
                                            this.currentPerson = person;
                                            shouldRefresh = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
}

package pigeonpun.bionicalteration.ui.bionic;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.AdminData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.inventory.ba_inventoryhandler;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;
import pigeonpun.bionicalteration.ui.ba_component;
import pigeonpun.bionicalteration.ui.ba_debounceplugin;
import pigeonpun.bionicalteration.ui.ba_uicommon;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author PigeonPun
 */
public class ba_uiplugin extends ba_uicommon {
    static Logger log = Global.getLogger(ba_uiplugin.class);
    protected CustomPanelAPI containerPanel; //Created panel from ba_deligate.java
    protected TooltipMakerAPI mainTooltip;
    int dW, dH, pW, pH;
    public static final float MAIN_CONTAINER_PADDING_X = ba_uicommon.getInitDialogContainerPaddingX();
    public static final float MAIN_CONTAINER_PADDING_Y = ba_uicommon.getInitDialogContainerPaddingY();
    public static final float MAIN_CONTAINER_WIDTH = ba_uicommon.getInitDialogContainerWidth();
    public static final float MAIN_CONTAINER_HEIGHT = ba_uicommon.getInitDialogContainerHeight();
    public static final String OVERVIEW = "OVERVIEW", WORKSHOP = "WORKSHOP", SHELL = "SHELL";
    public static final String WORKSHOP_EFFECT = "WORKSHOP_EFFECT", WORKSHOP_INV = "WORKSHOP_INV";
    public final String INSTALL_WORKSHOP="INSTALL", EDIT_WORKSHOP="EDIT";
    public String currentWorkShopMode = INSTALL_WORKSHOP; //determine what mode workshop is in
    public ba_bionicitemplugin currentRemovingBionic; //selected for removing
//    public ba_limbmanager.ba_limb currentHoveringLimb = null; //To highlight which effect on the effect list,
    // sadly not possible with how the bionic table currently implemented.
    // Bionic table hidden in certain UI resolution which cause the hovering being weird. it can still detect the button even tho its hidden
    HashMap<String, ba_component> tabMap = new HashMap<>();
    String currentTabId = OVERVIEW;
    String currentWorkshopEffectOrInvTab = WORKSHOP_INV;
    public static boolean isDisplayingOtherFleets = false;
//    public static float currentScrollPositionOverview = 0;
    public static ba_uiplugin createDefault() {
        return new ba_uiplugin();
    }
    @Override
    public void init(CustomPanelAPI panel, CustomVisualDialogDelegate.DialogCallbacks callbacks, InteractionDialogAPI dialog) {
        super.init(panel, callbacks, dialog);
        init(panel, callbacks, dialog, "", null);
    }

    /**
     * @param panel panel
     * @param callbacks callbacks
     * @param dialog dialog
     * @param moveToTabId tab id, get from uiPlugin class
     * @param personList Null for displaying the player fleet
     */
    public void init(CustomPanelAPI panel, CustomVisualDialogDelegate.DialogCallbacks callbacks, InteractionDialogAPI dialog, String moveToTabId, List<PersonAPI> personList) {
        super.init(panel, callbacks, dialog);
        if(personList != null) {
            isDisplayingOtherFleets = true;
        } else {
            isDisplayingOtherFleets = false;
        }
        ba_officermanager.refresh(personList);
        ba_inventoryhandler.compressAllBionics();
        this.callbacks = callbacks;
        this.containerPanel = panel;
        this.dialog = dialog;

        dW = Display.getWidth();
        dH = Display.getHeight();
        pW = (int) this.containerPanel.getPosition().getWidth();
        pH = (int) this.containerPanel.getPosition().getHeight();
        initialUICreation();
        //change the current tab id and "focus" on it
        focusContent(moveToTabId);
        currentScrollPositionPersonList = 0;
        currentScrollPositionBionicTable = 0;
        currentScrollPositionInventory = 0;
        debounceplugin.addToList("OVERVIEW_PERSON_LIST_TOOLTIP");
        debounceplugin.addToList("WORKSHOP_INVENTORY_TOOLTIP");
    }
    public void initialUICreation()
    {
        mainTooltip = this.containerPanel.createUIElement(this.containerPanel.getPosition().getWidth(), this.containerPanel.getPosition().getHeight(), false);
        mainTooltip.setForceProcessInput(true);
        containerPanel.addUIElement(mainTooltip).inTL(0,0);
        refresh();
        if(currentTabId.equals(OVERVIEW)) {
            ba_component component = componentMap.get("OVERVIEW_PERSON_LIST_PANEL");
            if(component != null && component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP") != null) {
                if(component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP").getExternalScroller() != null) {
                    component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP").getExternalScroller().setYOffset(0);
                }
            }
        }
    }
    @Override
    protected void refresh() {
        super.refresh();
//        log.info("refreshing");
        ba_component overviewComponent = tabMap.get(OVERVIEW);
        ba_component detailComponent = tabMap.get(WORKSHOP);
        if (overviewComponent != null) {
            containerPanel.removeComponent(overviewComponent.mainPanel);
        }
        if (detailComponent != null) {
            containerPanel.removeComponent(detailComponent.mainPanel);
        }
        getNewListPerson();
        //create smaller container for focus/unforcus
        displayOverview();
        displayWorkshop();
        focusContent("");
    }
    public void setCurrentPerson(PersonAPI focusingPerson) {
        for(PersonAPI person: ba_officermanager.listPersons) {
            if(focusingPerson.getId().equals(person.getId())) {
                this.currentPerson = person;
            }
        }
        refresh();
    }
    protected void displayOverview() {

        float pad = 5f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();
        //set default hover person
        if(ba_officermanager.listPersons.size() != 0 && this.currentPerson == null) {
            this.currentPerson = ba_officermanager.listPersons.get(0);
        }

        //big overview container
        String mainOverviewPanelKey = "MAIN_OVERVIEW_CONTAINER";
        String mainInfoTooltipKey = "MAIN_INFO_TOOLTIP";
        String mainPersonListTooltipKey = "MAIN_LIST_TOOLTIP";
        ba_component overviewContainer = new ba_component(componentMap, containerPanel, pW, pH, MAIN_CONTAINER_PADDING_X/2, MAIN_CONTAINER_PADDING_Y/2, true, mainOverviewPanelKey);
//        TooltipMakerAPI overviewTooltipContainer = overviewContainer.createTooltip(mainTooltipKey, pW, pH, false, 0, 0);
        tabMap.put(OVERVIEW, overviewContainer);
        overviewContainer.unfocusComponent(dW);

        float listPersonW = 0.3f * pW;
        float infoPersonW = ((1 - (listPersonW/pW)) * pW) - pad;
        TooltipMakerAPI overviewPersonListTooltipContainer = overviewContainer.createTooltip(mainPersonListTooltipKey, listPersonW, pH, false, 0, 0);
        TooltipMakerAPI overviewInfoTooltipContainer = overviewContainer.createTooltip(mainInfoTooltipKey, infoPersonW, pH, false, 0, 0);
        overviewInfoTooltipContainer.getPosition().inTL(listPersonW, 0);
        //overviewPerson
//        displayPersonList(overviewContainer, mainPersonListTooltipKey, listPersonW, pH);
        displayPersonListWithKeyPreset(overviewContainer, mainPersonListTooltipKey, "OVERVIEW", isDisplayingOtherFleets, listPersonW, pH, MAIN_CONTAINER_PADDING_X/2, MAIN_CONTAINER_PADDING_Y/2);
        displayPersonInfoList(overviewContainer, mainInfoTooltipKey, infoPersonW, pH, MAIN_CONTAINER_PADDING_X/2, MAIN_CONTAINER_PADDING_Y/2);
    }
    protected void displayPersonInfoList(ba_component creatorComponent, String creatorComponentTooltip, float personInfoW, float personInfoH, float personInfoX, float personInfoY) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

        String infoPersonTooltipKey = "OVERVIEW_PERSON_INFO_TOOLTIP";
        String infoPersonPanelKey = "OVERVIEW_PERSON_INFO_PANEL";
        ba_component infoPersonContainer = new ba_component(componentMap, creatorComponent.mainPanel, personInfoW, personInfoH, personInfoX, personInfoY, true, infoPersonPanelKey);
        TooltipMakerAPI infoPersonTooltipContainer = infoPersonContainer.createTooltip(infoPersonTooltipKey, personInfoW, personInfoH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonPanelKey,infoPersonContainer,0,0);
        //important to do this after you attach the sub panel
//        infoPersonContainer.mainPanel.getPosition().inTL(0,0);
        //--------header
//        float headerW = infoPersonTooltipContainer.getPosition().getWidth() - 3;
        float headerH = pad;
//        TooltipMakerAPI headerTooltip = infoPersonContainer.createTooltip("PERSON_INFO_HEADER", headerW, headerH, false, 0,0);
//        headerTooltip.getPosition().inTL(3, 0);
//        headerTooltip.addSectionHeading("DETAILS", Alignment.MID, 0);
//        UIComponentAPI line = headerTooltip.createRect(Misc.getDarkPlayerColor(), 1);
//        line.getPosition().setSize(1, personInfoH);
//        infoPersonContainer.mainPanel.addComponent(line).setLocation(0, 0).inTL(3,0);

        if(currentPerson != null) {
            int leftColumn = 230;
            int rightColumn = (int) (personInfoW - leftColumn);
            int row1H = 200;
            //--------Ship
            int shipX = (int) 35;
            int shipW = leftColumn;
            int shipH = row1H;
            int shipY = (int) (headerH + 20);
            TooltipMakerAPI personShipTooltip = infoPersonContainer.createTooltip("PERSON_INFO_STATS", shipW, shipH, false, 0, 0);
            personShipTooltip.getPosition().inTL(shipX,shipY);
            if(isDisplayingOtherFleets) {
                //displaying ship for the other fleet
                List<FleetMemberAPI> temp = new ArrayList<>();
                InteractionDialogPlugin plugin = dialog.getPlugin();
                if(plugin instanceof FleetInteractionDialogPluginImpl) {
                    FleetEncounterContext context = (FleetEncounterContext) plugin.getContext();
                    List<CampaignFleetAPI> fleets = context.getBattle().getBothSides();
                    FleetMemberAPI member = ba_officermanager.getFleetMemberFromFleet(currentPerson, fleets, false);
                    if(member != null) {
                        temp.add(member);
                    }
                    personShipTooltip.addShipList(1, 1, 150, Global.getSettings().getBasePlayerColor(), temp, 0);
                } else {
                    int imageX = (int) 0;
                    int imageY = (int) (0 + headerH);
                    int imageW = leftColumn;
                    int imageH = row1H;
                    String spriteName = currentPerson.getPortraitSprite();
                    TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("PERSON_INFO_IMAGE", imageW, imageH, false, 0, 0);
                    personImageTooltip.getPosition().inTL(imageX, imageY);
                    personImageTooltip.addImage(spriteName, imageW, imageH, 0);
                }
            } else {
                if(ba_officermanager.isCaptainOrAdmin(currentPerson, false).equals(ba_officermanager.ba_profession.CAPTAIN)) {
                    //display ship for the officer in player's fleet
                    List<FleetMemberAPI> temp = new ArrayList<>();
                    FleetMemberAPI member = ba_officermanager.getFleetMemberFromFleet(currentPerson, Collections.singletonList(Global.getSector().getPlayerFleet()), true);
                        if(member != null) {
                            temp.add(member);
                    } else {
                        //display the person pfp if idle
                        int imageX = (int) 0;
                        int imageY = (int) (0 + headerH);
                        int imageW = leftColumn;
                        int imageH = row1H;
                        String spriteName = currentPerson.getPortraitSprite();
                        TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("PERSON_INFO_IMAGE", imageW, imageH, false, 0, 0);
                        personImageTooltip.getPosition().inTL(imageX, imageY);
                        personImageTooltip.addImage(spriteName, imageW, imageH, 0);
                    }
                    personShipTooltip.addShipList(1, 1, 150, Global.getSettings().getBasePlayerColor(), temp, 0);
                } else {
                    AdminData selectedAdmin = null;
                    for (AdminData admin: Global.getSector().getCharacterData().getAdmins()) {
                        if(!admin.getPerson().isDefault() && !admin.getPerson().isAICore()) {
                            if(admin.getPerson().getId().equals(currentPerson.getId())) {
                                selectedAdmin = admin;
                                break;
                            }
                        }
                    }
                    if(selectedAdmin != null && selectedAdmin.getMarket() != null) {
                        //display planet
                        if(selectedAdmin.getMarket().getPlanetEntity() != null) {
                            personShipTooltip.showPlanetInfo(selectedAdmin.getMarket().getPlanetEntity(), 150,150,true,0);
                        } else {
                            //display whatever the market connected to

                            //display the person pfp if idle
                            int imageX = (int) 0;
                            int imageY = (int) (0 + headerH);
                            int imageW = leftColumn;
                            int imageH = row1H;
                            String spriteName = selectedAdmin.getMarket().getPrimaryEntity().getCustomEntitySpec().getSpriteName();;
                            TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("PERSON_INFO_IMAGE_ADMIN_ENTITY", imageW, imageH, false, 0, 0);
                            personImageTooltip.getPosition().inTL(imageX, imageY);
                            personImageTooltip.addImage(spriteName, imageW, imageH, 0);
                        }
                        //display the person pfp on top left
                        int imageW = 40;
                        int imageH = 40;
                        int imageX = (int) (leftColumn/2 - pad*4 - imageW);
                        int imageY = (int) (row1H/2 - pad - imageH);
                        String spriteName = currentPerson.getPortraitSprite();
                        TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("PERSON_INFO_IMAGE_ADMIN_PLANET", imageW, imageH, false, 0, 0);
                        personImageTooltip.getPosition().inTL(imageX, imageY);
                        personImageTooltip.addImage(spriteName, imageW, imageH, 0);
                        UIComponentAPI border = personImageTooltip.createRect(Misc.getDarkPlayerColor().brighter().brighter().brighter(), 1);
                        border.getPosition().setLocation(0,0).inTL(5,0);
                        border.getPosition().setSize(imageW, imageH);
                        personImageTooltip.addComponent(border);
                    } else {
                        //display the person pfp if idle
                        int imageX = (int) 0;
                        int imageY = (int) (0 + headerH);
                        int imageW = leftColumn;
                        int imageH = row1H;
                        String spriteName = currentPerson.getPortraitSprite();
                        TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("PERSON_INFO_IMAGE", imageW, imageH, false, 0, 0);
                        personImageTooltip.getPosition().inTL(imageX, imageY);
                        personImageTooltip.addImage(spriteName, imageW, imageH, 0);
                    }
                }
            }
            //--------Skill panels
            int skillX = (int) pad/2;
            int skillY = (int) (0 + headerH + row1H);
            int skillW = leftColumn;
            int skillH = (int) currentPerson.getStats().getSkillsCopy().size() * 100;
            TooltipMakerAPI personSkillTooltip = infoPersonContainer.createTooltip("PERSON_INFO_SKILLS", skillW, skillH, false, 0, 0);
            personSkillTooltip.getPosition().inTL(skillX,skillY);
            UIComponentAPI personSkills = personSkillTooltip.addSkillPanelOneColumn(currentPerson, 0);
            personSkills.getPosition().setSize(skillW, skillH);
            infoPersonContainer.mainPanel.addUIElement(personSkillTooltip);

            //--------Stats
            int statsX = (int) (leftColumn + pad + pad);
            int statsY = (int) (headerH + pad + pad/2);
            int statsW = (int) (rightColumn - pad);
            int statsH = (int) (row1H - pad);
            int statsSpacer = 15;
            TooltipMakerAPI personStatsTooltip = infoPersonContainer.createTooltip("PERSON_INFO_NAME", statsW, statsH, false, 0, 0);
            personStatsTooltip.getPosition().inTL(statsX,statsY);

            UIComponentAPI border = personStatsTooltip.createRect(Misc.getDarkPlayerColor(), 1);
            border.getPosition().setSize(statsW, statsH);
            border.getPosition().inTL(-pad-pad/2,-pad-pad/2);
            personStatsTooltip.addComponent(border);
            //>name
            LabelAPI nameLabel = personStatsTooltip.addPara(this.currentPerson.getName().getFullName() + (currentPerson.isPlayer() ? " (You)" : ""), 0, Misc.getBrightPlayerColor(), this.currentPerson.getName().getFullName());
            nameLabel.getPosition().setSize(200,20);
            nameLabel.getPosition().inTL(0,0);
            //>Level
            LabelAPI levelLabel = personStatsTooltip.addPara("Level: " + this.currentPerson.getStats().getLevel(), 0, Misc.getHighlightColor(), "" + this.currentPerson.getStats().getLevel());
            levelLabel.getPosition().setSize(100,20);
            levelLabel.getPosition().inTL(nameLabel.getPosition().getWidth() + pad, 0);
            //>personality
            LabelAPI personalityLabel = personStatsTooltip.addPara("Personality: " + this.currentPerson.getPersonalityAPI().getDisplayName(), 0, Misc.getHighlightColor(), "" + this.currentPerson.getPersonalityAPI().getDisplayName());
            personalityLabel.getPosition().setSize(200,20);
            personalityLabel.getPosition().inTL(0, nameLabel.getPosition().getHeight() + statsSpacer);
            //>Occupation
            String occupation = "Idle";
            if(this.dialog != null && this.dialog.getInteractionTarget() != null && this.dialog.getInteractionTarget() instanceof CampaignFleetAPI) {
                CampaignFleetAPI fleet = (CampaignFleetAPI) this.dialog.getInteractionTarget();
                if(fleet.getFleetData() != null && fleet.getFleetData().getMemberWithCaptain(this.currentPerson) != null) {
                    String shipName = fleet.getFleetData().getMemberWithCaptain(this.currentPerson).getShipName();
                    String shipClass = fleet.getFleetData().getMemberWithCaptain(this.currentPerson).getHullSpec().getNameWithDesignationWithDashClass();
                    occupation = "Piloting "+ shipName + " of " + shipClass;
                }
            }
            if(this.currentPerson.getFleet() != null || (this.currentPerson.isPlayer() && Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(this.currentPerson) != null)) {
                if(this.currentPerson.isPlayer()) {
                    String shipName = Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(this.currentPerson).getShipName();
                    String shipClass = Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(this.currentPerson).getHullSpec().getNameWithDesignationWithDashClass();
                    occupation = "Piloting "+ shipName + " of " + shipClass;
                } else if(this.currentPerson.getFleet().getFleetData().getMemberWithCaptain(this.currentPerson) != null) {
                    String shipName = this.currentPerson.getFleet().getFleetData().getMemberWithCaptain(this.currentPerson).getShipName();
                    String shipClass = this.currentPerson.getFleet().getFleetData().getMemberWithCaptain(this.currentPerson).getHullSpec().getNameWithDesignationWithDashClass();
                    occupation = "Piloting "+ shipName + " of " + shipClass;
                }
            } else if (this.currentPerson.getMarket() != null) {
                MarketAPI market = this.currentPerson.getMarket();
                if(market.getAdmin() == this.currentPerson) {
                    occupation = "Admin of " + market.getName();
                }
            }
            int occupationY = (int) (nameLabel.getPosition().getHeight() + statsSpacer + personalityLabel.getPosition().getHeight() + statsSpacer);
            LabelAPI occupationLabel = personStatsTooltip.addPara(String.valueOf("Currently: " + occupation), 0, Misc.getGrayColor(), "Currently: " + occupation);
            occupationLabel.getPosition().setSize(600,20);
            occupationLabel.getPosition().inTL(0, occupationY);
            //>BRM limit
            int limitBRMY = (int) (occupationY + occupationLabel.getPosition().getHeight() + statsSpacer);
            int limitBRM = (int) this.currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
            LabelAPI limitBRMLabel = personStatsTooltip.addPara(String.valueOf("BRM Limit: " + limitBRM), 0, Misc.getBrightPlayerColor(), "" + limitBRM);
            limitBRMLabel.getPosition().setSize(150,20);
            limitBRMLabel.getPosition().inTL(0, limitBRMY);
            if(bionicalterationplugin.isBRMCapDisable) {
                limitBRMLabel.setOpacity(0);
            }
            //>BRM available
            int currentBRM = (int) this.currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);
            int currentBRMY = (int) limitBRMY;
            int currentBRMX = (int) (limitBRMLabel.getPosition().getWidth());
            LabelAPI currentBRMLabel = personStatsTooltip.addPara(String.valueOf("BRM Using: " + currentBRM), 0, currentBRM > limitBRM ? bad: Misc.getHighlightColor(), "" + currentBRM);
            currentBRMLabel.getPosition().setSize(150,20);
            currentBRMLabel.getPosition().inTL(currentBRMX, currentBRMY);
            //>Consciousness
            float consciousness = this.currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
            int consciousnessY = (int) (limitBRMY + statsSpacer + limitBRMLabel.getPosition().getHeight());
            int consciousnessX = (int) (0);
            int consciousnessW = 150;
            String condition = ba_consciousmanager.getConsciousnessLevel(consciousness).getDisplayName() == null? "----": ba_consciousmanager.getConsciousnessLevel(consciousness).getDisplayName();
            int conditionY = (int) consciousnessY;
            int conditionX = (int) (consciousnessX + consciousnessW);
            //hover condition
            float hoverConsciousW = currentBRMLabel.computeTextWidth("Condition: "+ condition) + pad;
            float hoverConsciousH = currentBRMLabel.computeTextHeight("Condition: "+ condition) + pad;
            ButtonAPI consciousAreaChecker = personStatsTooltip.addAreaCheckbox("", null,Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), hoverConsciousW, hoverConsciousH, 0);
            addButtonToList(consciousAreaChecker, "hover_bionic_consciousness:"+ consciousness);
            consciousAreaChecker.getPosition().setLocation(0,0).inTL(conditionX - pad/2, conditionY - pad/2);
            //conscious label
            LabelAPI consciousnessLabel = personStatsTooltip.addPara(ba_consciousmanager.getDisplayConditionLabel(currentPerson) + ": " + Math.round(consciousness * 100) + "%", 0);
            consciousnessLabel.setHighlight("" + Math.round(consciousness * 100) + "%");
            consciousnessLabel.setHighlightColor(ba_consciousmanager.getConsciousnessColorByLevel(consciousness));
            consciousnessLabel.getPosition().setSize(consciousnessW,20);
            consciousnessLabel.getPosition().inTL(consciousnessX, consciousnessY);
            //>Conditions: tiled with conscious
            LabelAPI conditionLabel = personStatsTooltip.addPara("Condition: " + condition + "", 0);
            conditionLabel.setHighlight("" + condition);
            conditionLabel.setHighlightColor(ba_consciousmanager.getConsciousnessColorByLevel(consciousness));
            conditionLabel.getPosition().setSize(150,20);
            conditionLabel.getPosition().inTL(conditionX, conditionY);
            personStatsTooltip.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
                @Override
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return true;
                }

                @Override
                public float getTooltipWidth(Object tooltipParam) {
                    return 300;
                }

                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    ba_consciousmanager.displayConsciousEffects(tooltip, currentPerson, expanded);
                }
            }, consciousAreaChecker, TooltipMakerAPI.TooltipLocation.ABOVE);
            if(!isDisplayingOtherFleets) {
                //Button switch page
                float upgradeBtnH = 20 + statsSpacer + 20;
                float upgradeBtnW = 200;
                int upgradeX = (int) (personInfoW - upgradeBtnW - pad*2.5);
                int upgradeY = (int) (currentBRMY + headerH + pad + pad/2);
                TooltipMakerAPI personUpgradeTooltip = infoPersonContainer.createTooltip("PERSON_INFO_UPGRADE", statsW, statsH, false, 0, 0);
                personUpgradeTooltip.getPosition().setLocation(0,0);
                personUpgradeTooltip.getPosition().inTL(upgradeX,upgradeY);
                ButtonAPI upgradeButton = personUpgradeTooltip.addButton("Workshop", null, Misc.getTextColor(), Misc.getPositiveHighlightColor().darker().darker(), Alignment.MID, CutStyle.TOP, upgradeBtnW, upgradeBtnH, 0);
                addButtonToList(upgradeButton, "tab:" + WORKSHOP);
                upgradeButton.setShortcut(Keyboard.KEY_W, true);
                if(this.currentTabId.equals(WORKSHOP)) {
                    upgradeButton.setEnabled(false);
                }
            }
            //--------Bionic table
            int tableX = (int) (leftColumn);
            int tableY = (int) (row1H + headerH);
            int tableW = (int) (rightColumn);
            int tableH = (int) (personInfoH - row1H - headerH);
            displayBionicTableWithKeyPreset(infoPersonContainer, infoPersonTooltipKey, "OVERVIEW",false, true, tableW, tableH, tableX, tableY);
        }
        //do the adding late so the scroll work
//        infoPersonContainer.mainPanel.addUIElement(infoPersonTooltipContainer);
    }
    protected void displayWorkshop() {
        float pad = 5f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();
        //set default hover person - this shouldn't be triggered by default but somehow currentPerson is null, do it
        if(ba_officermanager.listPersons.size() != 0 && this.currentPerson == null) {
            this.currentPerson = ba_officermanager.listPersons.get(0);
        }
        //installing process
        /*
        Fist the player right click the item or enter the bionic UI
        - Selecting a npc by clicking on the npc in the npc list or the install bionic option on the top right will transfer to the install UI
        - Bionic will show up in the inventory on the bottom. Clicking on them will select them (an indicator text say selected bionic)
        - Clicking on a limb will select the limb (highlight the limb line on the table).
        - When the player hover over the install or remove button, description will show up guiding the player (If haven't select bionic or limb, will show information about it)
        - the menu on the right will show up all the bionic installed on the person and their effects
         */

        //big detail container
        String mainOverviewPanelKey = "MAIN_WORKSHOP_CONTAINER";
        String mainPersonInfoTooltipKey = "MAIN_PERSON_TOOLTIP";
        String mainInventoryTooltipKey = "MAIN_INVENTORY_TOOLTIP";
        String mainEffectsTooltipKey = "MAIN_EFFECTS_TOOLTIP";
        String btnTooltipKey = "MAIN_BTN_TOOLTIP";
        ba_component workshopContainer = new ba_component(componentMap, containerPanel, pW, pH, MAIN_CONTAINER_PADDING_X/2, MAIN_CONTAINER_PADDING_Y/2, true, mainOverviewPanelKey);
        tabMap.put(WORKSHOP, workshopContainer);
        workshopContainer.unfocusComponent(dW);

        //tooltip for scroll
        float effectListW = 315f;
        float effectListH = pH;
        float personInfoW = pW - effectListW;
        float personInfoH = 1f * pH - opad;
        //todo: INSTALL_WORKSHOP | EDIT_WORKSHOP - change bionic list to replace BRM + humanity to remove and confirm remove if have effects.
        TooltipMakerAPI personInfoTooltipContainer = workshopContainer.createTooltip(mainPersonInfoTooltipKey, personInfoW, personInfoH, false, 0, 0);
        personInfoTooltipContainer.getPosition().inTL(0,0);
        displayPersonInfoWorkshop(workshopContainer, mainPersonInfoTooltipKey, personInfoW, personInfoH, 0,0);

        float inventoryW = personInfoW;
        float inventoryH = (pH - (personInfoH));
//        TooltipMakerAPI inventoryTooltipContainer = workshopContainer.createTooltip(mainInventoryTooltipKey, inventoryW, inventoryH, false, 0, 0);
//        inventoryTooltipContainer.getPosition().inTL(0, personInfoH);
//        if(this.currentWorkShopMode.equals(INSTALL_WORKSHOP)) {
//            displayInventoryWorkshop(workshopContainer, mainInventoryTooltipKey, inventoryW, inventoryH, 0,0);
//        } else if (this.currentWorkShopMode.equals(EDIT_WORKSHOP)) {
//            displayRemoveBionicWorkshop(workshopContainer, mainInventoryTooltipKey, inventoryW, inventoryH, 0,0);
//        }

        //buttons
        TooltipMakerAPI btnTooltipContainer = workshopContainer.createTooltip(btnTooltipKey, effectListW, effectListH, false, 0, 0);
        btnTooltipContainer.getPosition().inTL(personInfoW, 0);
        float invEffectBtnH = 30;
        float invEffectBtnW = 100;
        int invBtnX = (int) (pad);
        int invBtnY = (int) (opad);
        int effectBtnX = (int) (invBtnX + invEffectBtnW);
        int effectBtnY = invBtnY;
        ButtonAPI invButton = btnTooltipContainer.addButton("Inventory", null, Misc.getTextColor(), this.currentWorkshopEffectOrInvTab.equals(WORKSHOP_INV)?Misc.getDarkPlayerColor():Misc.getDarkPlayerColor().darker().darker(), Alignment.MID, CutStyle.TOP, invEffectBtnW, invEffectBtnH, 0);
        addButtonToList(invButton, "workshop_tab:" + WORKSHOP_INV);
        invButton.getPosition().inTL(invBtnX, invBtnY);
        invButton.setShortcut(Keyboard.KEY_1, true);
        invButton.setButtonDisabledPressedSound("ui_button_pressed");
        invButton.setPerformActionWhenDisabled(true);
        if(this.currentWorkshopEffectOrInvTab.equals(WORKSHOP_INV)) {
            invButton.setHighlightBrightness(0);
            invButton.setFlashBrightness(0);
            invButton.setButtonPressedSound(null);
        }
        ButtonAPI effectButton = btnTooltipContainer.addButton("Effect", null, Misc.getTextColor(), this.currentWorkshopEffectOrInvTab.equals(WORKSHOP_EFFECT)?Misc.getDarkPlayerColor():Misc.getDarkPlayerColor().darker().darker(), Alignment.MID, CutStyle.TOP, invEffectBtnW, invEffectBtnH, 0);
        addButtonToList(effectButton, "workshop_tab:" + WORKSHOP_EFFECT);
        effectButton.getPosition().inTL(effectBtnX, effectBtnY);
        effectButton.setShortcut(Keyboard.KEY_2, true);
        effectButton.setButtonDisabledPressedSound("ui_button_pressed");
        effectButton.setPerformActionWhenDisabled(true);
        if(this.currentWorkshopEffectOrInvTab.equals(WORKSHOP_EFFECT)) {
            effectButton.setHighlightBrightness(0);
            effectButton.setFlashBrightness(0);
            effectButton.setButtonPressedSound(null);
        }
        //effect + inv
        TooltipMakerAPI effectListTooltipContainer = workshopContainer.createTooltip(mainEffectsTooltipKey, effectListW, effectListH, false, 0, invEffectBtnH);
        effectListTooltipContainer.getPosition().inTL(personInfoW, 0);
        if(Objects.equals(this.currentWorkshopEffectOrInvTab, WORKSHOP_INV)) {
            displayInventoryWorkshop(workshopContainer, mainEffectsTooltipKey, effectListW, effectListH-invEffectBtnH, -pad, invEffectBtnH);
        }
        if(Objects.equals(this.currentWorkshopEffectOrInvTab, WORKSHOP_EFFECT)) {
            displayEffectListWorkshop(workshopContainer, mainEffectsTooltipKey, effectListW, effectListH-invEffectBtnH, 0,invEffectBtnH);
        }
    }
    public void displayPersonInfoWorkshop(ba_component creatorComponent, String creatorComponentTooltip, final float personInfoW, float personInfoH, float personInfoX, float personInfoY) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String infoPersonTooltipKey = "WORKSHOP_PERSON_INFO_TOOLTIP";
        String infoPersonPanelKey = "WORKSHOP_PERSON_INFO_PANEL";
        ba_component infoPersonContainer = new ba_component(componentMap, creatorComponent.mainPanel, personInfoW, personInfoH, personInfoX, personInfoY, true, infoPersonPanelKey);
        TooltipMakerAPI infoPersonTooltipContainer = infoPersonContainer.createTooltip(infoPersonTooltipKey, personInfoW, personInfoH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonPanelKey,infoPersonContainer,0,0);

//        infoPersonTooltipContainer.addPara("person info" , 0);
        float infoLeftW = personInfoW * 0.2f;
        float infoRightW = personInfoW - infoLeftW;

        int upgradeBtnH = 40;
        int upgradeBtnW = (int) (infoLeftW - pad);
        int upgradeBtnX = (int) (0 + pad);
        int upgradeBtnY = (int) (0 + pad);
        ButtonAPI upgradeButton = infoPersonTooltipContainer.addButton("Exit", null, Misc.getTextColor(), Misc.getNegativeHighlightColor().darker().darker(), upgradeBtnW, upgradeBtnH, 0);
        upgradeButton.getPosition().inTL(upgradeBtnX,upgradeBtnY);
        upgradeButton.setShortcut(Keyboard.KEY_E, true);
        addButtonToList(upgradeButton, "tab:" + OVERVIEW);
        if(this.currentTabId.equals(OVERVIEW)) {
            upgradeButton.setEnabled(false);
        }

        //--------image
        int imageX = (int) (0 + pad);
        int imageY = (int) (upgradeBtnH + upgradeBtnY + pad);
        int imageW = (int) infoLeftW;
        int imageH = imageW;
        String spriteName = this.currentPerson.getPortraitSprite();
        TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("WORKSHOP_PERSON_IMAGE", imageW, imageH, false, 0, 0);
        personImageTooltip.getPosition().inTL(imageX, imageY);
        personImageTooltip.addImage(spriteName, imageW, imageH, 0);

        if(bionicalterationplugin.isDevmode) {
            infoPersonTooltipContainer.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
                @Override
                public boolean isTooltipExpandable(Object tooltipParam) {
                    return false;
                }

                @Override
                public float getTooltipWidth(Object tooltipParam) {
                    return 400;
                }

                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    tooltip.addSectionHeading("BRM MODIFY ID", Alignment.MID, 0);
                    HashMap<String, MutableStat.StatMod> brmIds = currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).getFlatBonuses();
                    tooltip.addPara(brmIds.keySet().toString(), pad);
                    tooltip.addSectionHeading("CONSCIOUSNESS MODIFY ID", Alignment.MID, pad);
                    HashMap<String, MutableStat.StatMod> consciousIds = currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).getFlatBonuses();
                    tooltip.addPara(consciousIds.keySet().toString(), pad);
                }
            }, personImageTooltip, TooltipMakerAPI.TooltipLocation.RIGHT);
        }
        //---------Name
        int nameH = 30;
        int nameW = (int) infoLeftW;
        int nameX = (int) (0 + pad);
        int nameY = (int) (imageH + imageY+ pad);
        LabelAPI name = infoPersonTooltipContainer.addPara(this.currentPerson.getName().getFullName() + (this.currentPerson.isPlayer() ? " (" + "You" + ")": ""), pad);
        name.getPosition().inTL(nameX, nameY);
        name.getPosition().setSize(nameW, nameH);
        name.setHighlight(this.currentPerson.getName().getFullName());
        name.setHighlightColors(Misc.getBrightPlayerColor());
        //BRM (Bionic Rights Management)
        int brmH = 30;
        int brmW = (int) infoLeftW;
        int brmX = (int) (0 + pad);
        int brmY = (int) (nameY + nameH);
        int currentBRM = (int) this.currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);;
        int limitBRM = (int) this.currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);;
        LabelAPI BRM = infoPersonTooltipContainer.addPara("BRM: " + currentBRM + " / " + limitBRM, pad);
        BRM.setHighlight("BRM: ", "" +currentBRM, "" +limitBRM);
        BRM.setHighlightColors(t,currentBRM > limitBRM ? bad: h,Misc.getBrightPlayerColor());
        BRM.getPosition().inTL(brmX, brmY);
        BRM.getPosition().setSize(brmW, brmH);
        if(bionicalterationplugin.isBRMCapDisable) {
            BRM.setText("BRM: " + currentBRM);
            BRM.setHighlight("BRM: ", "" +currentBRM);
            BRM.setHighlightColors(t, h);
        }
        //>Consciousness
        float consciousness = this.currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
        int consciousnessY = (int) (brmY + brmH);
        int consciousnessX = (int) (0 + pad);
        int consciousnessH = 30;
        int consciousnessW = (int) infoLeftW;
        int conditionY = (int) (consciousnessH + consciousnessY);
        int conditionX = (int) (0 + pad);
        int professionY = (int) (consciousnessH + conditionY);
        int professionX = (int) (0 + pad);
        String condition = ba_consciousmanager.getConsciousnessLevel(consciousness).getDisplayName() == null? "----": ba_consciousmanager.getConsciousnessLevel(consciousness).getDisplayName();
        //hover condition
        float hoverConsciousW = BRM.computeTextWidth("Condition: "+ condition) + pad;
        float hoverConsciousH = BRM.computeTextHeight("Condition: "+ condition) + pad;
        ButtonAPI consciousAreaChecker = infoPersonTooltipContainer.addAreaCheckbox("", null,Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), hoverConsciousW, hoverConsciousH, 0);
        addButtonToList(consciousAreaChecker, "hover_bionic_consciousness:"+ consciousness);
        consciousAreaChecker.getPosition().setLocation(0,0).inTL(conditionX - pad/2, conditionY - pad/2);
        //conscious label
        LabelAPI consciousnessLabel = infoPersonTooltipContainer.addPara(ba_consciousmanager.getDisplayConditionLabel(currentPerson) + ": " + Math.round(consciousness * 100) + "%", 0);
        consciousnessLabel.setHighlight("" + Math.round(consciousness * 100) + "%");
        consciousnessLabel.setHighlightColor(ba_consciousmanager.getConsciousnessColorByLevel(consciousness));
        consciousnessLabel.getPosition().setSize(consciousnessW,consciousnessH);
        consciousnessLabel.getPosition().inTL(consciousnessX, consciousnessY);
        //>Conditions: tiled with conscious
        LabelAPI conditionLabel = infoPersonTooltipContainer.addPara("Condition: " + condition + "", 0);
        conditionLabel.setHighlight("" + condition);
        conditionLabel.setHighlightColor(ba_consciousmanager.getConsciousnessColorByLevel(consciousness));
        conditionLabel.getPosition().setSize(150,30);
        conditionLabel.getPosition().inTL(conditionX, conditionY);
        infoPersonTooltipContainer.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return true;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 350;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                ba_consciousmanager.displayConsciousEffects(tooltip, currentPerson, expanded);
            }
        }, consciousAreaChecker, TooltipMakerAPI.TooltipLocation.ABOVE);
        //>professions: tiled with conscious
        LabelAPI professionLabel = infoPersonTooltipContainer.addPara("" + ba_officermanager.getProfessionText(this.currentPerson, isDisplayingOtherFleets) + "", 0);
        professionLabel.setHighlight("" + ba_officermanager.getProfessionText(this.currentPerson, isDisplayingOtherFleets));
        professionLabel.setHighlightColor(Misc.getHighlightColor());
        professionLabel.getPosition().setSize(150,30);
        professionLabel.getPosition().inTL(professionX, professionY);
        int btnH = 40;
        //--------upgrade button
        int installBtnH = btnH;
        int installBtnW = (int) (200 - pad);
        int installBtnX = (int) (personInfoW - installBtnW - pad);
        int installBtnY = (int) (personInfoH - installBtnH);
        ButtonAPI installButton = infoPersonTooltipContainer.addButton("Install", null, t, Color.green.darker().darker(), installBtnW, installBtnH, 0);
        installButton.getPosition().inTL(installBtnX,installBtnY);
        addButtonToList(installButton, "bionic:install");
        installButton.setEnabled(false);
        if(this.currentSelectedLimb != null && this.currentSelectedBionic != null && ba_officermanager.checkIfCanInstallBionic(this.currentSelectedBionic, this.currentSelectedLimb, this.currentPerson)) {
            installButton.setEnabled(true);
            installButton.flash(false);
        }
        infoPersonTooltipContainer.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return personInfoW * 1f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addSectionHeading("Info", Alignment.MID, 0);
                if(currentSelectedLimb == null) {
                    tooltip.addPara("Please select the modifying %s", pad, Misc.getBrightPlayerColor(), "limb");
                } else {
                    tooltip.addPara("Modifying %s selected", pad, Misc.getBrightPlayerColor(), "limb");
                }
                if(currentSelectedBionic == null) {
                    tooltip.addPara("Please select the installing %s", pad, Misc.getBrightPlayerColor(), "bionic");
                } else {
                    tooltip.addPara("Installing %s selected", pad, Misc.getBrightPlayerColor(), "bionic");
                }
                tooltip.addSectionHeading("Help", Alignment.MID, pad);
                tooltip.addPara("To install a bionic, follow the steps below:", Misc.getHighlightColor(),pad/2);
                tooltip.addPara("1. Select a %s. Click on the bionic table and select the desired limb.", pad/2, Misc.getHighlightColor(), "LIMB");
                tooltip.addPara("2. Select a %s. Click on bionic item in the inventory below if there are any bionic available from your fleet inventory.", pad/2, Misc.getHighlightColor(), "BIONIC");
                tooltip.addPara("3. Click the %s button.",pad/2, Misc.getHighlightColor(), "INSTALL");

                tooltip.addSectionHeading("Debug", Alignment.MID, pad);
                boolean isBionicInstallableOnLimb = false;
                boolean isBionicAlreadyInstalledOnLimb = false;
                boolean isBionicInstallableBaseOnPersonType = false;
                boolean isBrmExceed = false;
                boolean isConsciousnessReduceToZero = false;
                boolean isBionicConflicted = false;
                if(currentSelectedLimb != null && currentSelectedBionic != null) {
                    if(ba_officermanager.checkIfBionicLimbGroupContainSelected(currentSelectedBionic, currentSelectedLimb)) isBionicInstallableOnLimb = true;
                    if(ba_officermanager.checkIfBionicIsAlreadyInstalled(currentSelectedBionic, currentSelectedLimb, currentPerson)) isBionicAlreadyInstalledOnLimb = true;
                    if(ba_officermanager.checkIfBionicInstallableBaseOnPersonType(currentSelectedBionic, currentPerson)) isBionicInstallableBaseOnPersonType = true;
                    if(!ba_officermanager.checkIfCurrentBRMLowerThanLimitOnInstall(currentSelectedBionic, currentPerson)) isBrmExceed = true;
                    if(!ba_officermanager.checkIfConsciousnessReduceAboveZeroOnInstall(currentSelectedBionic, currentPerson)) isConsciousnessReduceToZero = true;
                    if(ba_bionicmanager.checkIfBionicConflicted(currentSelectedBionic, currentPerson)) isBionicConflicted = true;
                }
                tooltip.setParaFontVictor14();
                tooltip.addPara("Button is still disabled ? Hover on selected bionic / limb for more information.", pad);
                tooltip.setParaFontDefault();
                tooltip.addPara("Make sure that: ", pad);
                LabelAPI bionicInstallableLabel = tooltip.addPara("[ %s ] %s on selected limb.", pad/2, Misc.getHighlightColor(), isBionicInstallableOnLimb? "O": "X","Selected bionic can be installed");
                bionicInstallableLabel.setHighlightColors(isBionicInstallableOnLimb? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                LabelAPI bionicInstalledLabel = tooltip.addPara("[ %s ] %s bionic installable per limb limit. The current limit is %s", pad/2, Misc.getHighlightColor(), !isBionicAlreadyInstalledOnLimb? "O": "X", "Selected limb is not exceeding", "" +ba_variablemanager.BIONIC_INSTALL_PER_LIMB);
                bionicInstalledLabel.setHighlightColors(!isBionicAlreadyInstalledOnLimb? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                LabelAPI bionicPersonTypeLabel = tooltip.addPara("[ %s ] %s for the person profession (Officer/Admin). Note: Player can install both type", pad/2, Misc.getHighlightColor(), isBionicInstallableBaseOnPersonType? "O": "X", "Selected bionic have applying effect");
                bionicPersonTypeLabel.setHighlightColors(isBionicInstallableBaseOnPersonType? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                LabelAPI brmLabel = tooltip.addPara("[ %s ] %s the person BRM limit.", pad/2, Misc.getHighlightColor(), !isBrmExceed? "O": "X","Selected bionics BRM do not go past");
                brmLabel.setHighlightColors(!isBrmExceed? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                if(bionicalterationplugin.isBRMCapDisable) {
                    brmLabel.setText("[ R ] BRM Cap removed");
                    brmLabel.setHighlight("[ R ] BRM Cap removed");
                    brmLabel.setHighlightColors(Misc.getGrayColor());
                }
                LabelAPI consciousnessLabel = tooltip.addPara("[ %s ] %s the person's consciousness to lower or equal to %s.", pad/2, Misc.getHighlightColor(), !isConsciousnessReduceToZero? "O": "X","Selected bionics consciousness cost does not reduce", "0");
                consciousnessLabel.setHighlightColors(!isConsciousnessReduceToZero? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                LabelAPI conflictedLabel = tooltip.addPara("[ %s ] %s with other bionics installed on the person.", pad/2, Misc.getHighlightColor(), !isBionicConflicted? "O": "X", "Selected bionic is not conflicting");
                conflictedLabel.setHighlightColors(!isBionicConflicted? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
            }
        }, installButton, TooltipMakerAPI.TooltipLocation.ABOVE);
        //--------remove button
        //edit: enter edit mode, display list a list of bionic for a limb with remove button next to it
        int removeBtnH = btnH;
        int removeBtnW = (int) (160 - pad);
        int removeBtnX = (int) (installBtnX - pad - removeBtnW);
        int removeBtnY = (int) (installBtnY);
        ButtonAPI removeButton = infoPersonTooltipContainer.addButton(this.currentWorkShopMode.equals(this.INSTALL_WORKSHOP) ?"Removal": "Exit Removal", null, t, Color.yellow.darker().darker(), removeBtnW, removeBtnH, 0);
        removeButton.getPosition().inTL(removeBtnX,removeBtnY);
        removeButton.setShortcut(Keyboard.KEY_R, true);
        addButtonToList(removeButton, "bionic:edit");
//        removeButton.setEnabled(false);
//        if(this.currentSelectedLimb != null && ba_officermanager.checkIfCanEditLimb(this.currentSelectedLimb, this.currentPerson)) {
//            List<ba_bionicitemplugin> availableRemovingBionics = ba_bionicmanager.getListBionicInstalledOnLimb(this.currentSelectedLimb, this.currentPerson);
//            if(!availableRemovingBionics.isEmpty()) {
//                removeButton.setEnabled(true);
//                removeButton.flash(false);
//            }
//        }
        infoPersonTooltipContainer.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return personInfoW * 0.6f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addSectionHeading("Info", Alignment.MID, 0);
                if(currentSelectedLimb == null) {
                    tooltip.addPara("Please select the modifying %s", pad, Misc.getBrightPlayerColor(), "limb");
                } else {
                    tooltip.addPara("Modifying %s selected", pad, Misc.getBrightPlayerColor(), "limb");
                }
                tooltip.addSectionHeading("Help", Alignment.MID, pad);
                tooltip.addPara("To remove bionic(s) from a person, follow these steps:", pad);
                tooltip.addPara("1. Select the %s that installed the bionic", pad, Misc.getHighlightColor(), "LIMB");
                tooltip.addPara("2. Click the %s button", pad, Misc.getHighlightColor(), "EDIT");
                tooltip.addPara("3. Find the bionic you want to remove, if available, click the %s button", pad, Misc.getHighlightColor(), "REMOVE");
                tooltip.addPara("4. Confirm remove by clicking the %s button", pad, Misc.getHighlightColor(), "CONFIRM REMOVE");
                tooltip.addPara("The remove bionic will appear in your inventory. (Click the %s again to exist %s)", pad, Misc.getBasePlayerColor(),"Exit remove button", "Remove mode");
                tooltip.addPara("Note: Some bionics can NOT be removed, some have effects ON REMOVE and some once removed DO NOT RETURN the bionic item", pad);
//                tooltip.setParaFontVictor14();
//                tooltip.addPara("Button is disabled ?", pad);
//                tooltip.setParaFontDefault();
//                tooltip.addPara("- Make sure that selected limb have bionics installed. Hover on selected limb for this information", pad / 2);
            }
        }, removeButton, TooltipMakerAPI.TooltipLocation.ABOVE);
        //--------Bionic table
        int tableX = (int) (infoLeftW + pad  + pad);
        int tableY = (int) (0 + pad);
        int tableW = (int) (infoRightW - pad - pad);
        int tableH = (int) (personInfoH - pad - pad - btnH);
        String highlightLimbId = this.currentSelectedBionic != null? this.currentSelectedBionic.bionicLimbGroupId: "";
        if(currentWorkShopMode.equals(INSTALL_WORKSHOP)) {
            displayBionicTableWithKeyPresetHighLight(infoPersonContainer, infoPersonTooltipKey, "WORKSHOP",true, true, tableW, tableH, tableX, tableY, highlightLimbId, false);
        }
        if(currentWorkShopMode.equals(EDIT_WORKSHOP)) {
            displayBionicTableWithKeyPresetHighLight(infoPersonContainer, infoPersonTooltipKey, "WORKSHOP",true, true, tableW, tableH, tableX, tableY, highlightLimbId, true);
        }
        //--------selected
        int selectedH = btnH / 2;
        int selectedW = (int) (infoRightW - removeBtnW - installBtnW - pad);
        int selectedLimbX = (int) (tableX + pad);
        int selectedLimbY = (int) (removeBtnY);
        String limbName = this.currentSelectedLimb != null ? this.currentSelectedLimb.name : "None";
        LabelAPI selectedLimbLabel = infoPersonTooltipContainer.addPara("%s %s", 0, t, "Selected:", limbName);
        selectedLimbLabel.getPosition().inTL(selectedLimbX,selectedLimbY);
        selectedLimbLabel.getPosition().setSize(selectedW, selectedH);
        selectedLimbLabel.setHighlight("Selected:", limbName);
        selectedLimbLabel.setHighlightColors(Misc.getBrightPlayerColor(), this.currentSelectedLimb != null ? t :Misc.getGrayColor());
        int selectedBionicX = (int) (selectedLimbX);
        int selectedBionicY = (int) (removeBtnY + selectedH);
        String bionicName = this.currentSelectedBionic != null ? this.currentSelectedBionic.getName(): "None";
        LabelAPI selectedBionicLabel = infoPersonTooltipContainer.addPara("%s %s %s", 0, t,"Selected:",  bionicName, (this. currentSelectedBionic != null && !this.currentSelectedBionic.isAllowedRemoveAfterInstall)? "[ UNREMOVEABLE ]": "");
        selectedBionicLabel.getPosition().inTL(selectedBionicX,selectedBionicY);
        selectedBionicLabel.getPosition().setSize(selectedW, selectedH);
        selectedBionicLabel.setHighlight("Selected:", bionicName, "[ UNREMOVEABLE ]");
        selectedBionicLabel.setHighlightColors(Misc.getBrightPlayerColor(), this.currentSelectedBionic != null ? this.currentSelectedBionic.displayColor: Misc.getGrayColor(), bad);
    }
    public void displayEffectListWorkshop(ba_component creatorComponent, String creatorComponentTooltip, float effectListW, float effectListH, float effectListX, float effectListY) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

        String effectListTooltipKey = "WORKSHOP_EFFECT_LIST_TOOLTIP";
        String effectListPanelKey = "WORKSHOP_EFFECT_LIST_PANEL";
        ba_component effectListContainer = new ba_component(componentMap, creatorComponent.mainPanel, effectListW, effectListH, effectListX, effectListY, true, effectListPanelKey);
        TooltipMakerAPI effectListTooltipContainer = effectListContainer.createTooltip(effectListTooltipKey, effectListW, effectListH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, effectListPanelKey,effectListContainer,effectListX,effectListY);

        //border effect list
        int borderW = (int) (effectListW - pad - pad / 2);
        int borderH = (int) (effectListH * 1f - pad - pad);
        int borderX = (int) pad / 2;
        int borderY = (int) (pad);
        UIComponentAPI border = effectListTooltipContainer.createRect(Misc.getDarkPlayerColor(), 1);
        border.getPosition().setSize(borderW, borderH);
        effectListContainer.mainPanel.addComponent(border).setLocation(0,0).inTL(borderX, borderY);

        //sub container
        int subEffectW = (int) (borderW - pad - pad);
        int subEffectH = (int) (borderH - pad);
        int subEffectX = (int) (pad / 2 + pad);
        int subEffectY = (int) (pad + pad);
        String subEffectListTooltipKey = "WORKSHOP_SUB_EFFECT_LIST_TOOLTIP";
        String subEffectListPanelKey = "WORKSHOP_SUB_EFFECT_LIST_PANEL";
        ba_component subEffectListContainer = new ba_component(componentMap, effectListContainer.mainPanel, subEffectW, subEffectH, subEffectX, subEffectY, false, subEffectListPanelKey);
        TooltipMakerAPI subEffectListTooltipContainer = subEffectListContainer.createTooltip(subEffectListTooltipKey, subEffectW, subEffectH, true, 0,0);
        effectListContainer.attachSubPanel(effectListTooltipKey, subEffectListPanelKey,subEffectListContainer,0,0);

        int spacerY = 5;
//        //consciousness effect
//        float consciousness = this.currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
//        ba_conscious consciousLevel = ba_consciousmanager.getConsciousnessLevel(consciousness);
//        ba_consciousmanager.displayConsciousEffects(subEffectListTooltipContainer, currentPerson, true);
//        subEffectListTooltipContainer.addSpacer(spacerY);
        //bionic effects
        List<ba_officermanager.ba_bionicAugmentedData> currentAnatomyList = ba_officermanager.getBionicAnatomyList(this.currentPerson);
        int i = 0;
        for(ba_officermanager.ba_bionicAugmentedData bionicAugmentedDatas: currentAnatomyList) {
            if(bionicAugmentedDatas.bionicInstalled != null) {
                bionicAugmentedDatas.bionicInstalled.displayEffectDescription(subEffectListTooltipContainer, currentPerson, bionicAugmentedDatas.bionicInstalled, false);

                subEffectListTooltipContainer.addSpacer(spacerY);
                if(bionicAugmentedDatas.appliedOverclock != null) {
                    bionicAugmentedDatas.appliedOverclock.displayEffectDescription(subEffectListTooltipContainer, currentPerson, bionicAugmentedDatas.bionicInstalled, true);
                }
            }
            i++;
        }
        //do the adding late so the scroll work
        subEffectListContainer.mainPanel.addUIElement(subEffectListTooltipContainer).setLocation(0,0).inTL(subEffectX, subEffectY);
    }
    public void displayRemoveBionicWorkshop(ba_component creatorComponent, String creatorComponentTooltip, float removeW, float removeH, float removeX, float removeY) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        //big container
        final float containerW = removeW - pad - pad / 2;
        int containerH = (int) (removeH - pad - pad);
        int containerX = (int) pad;
        int containerY = (int) pad;
        String removeContainerTooltipKey = "WORKSHOP_REMOVE_CONTAINER_TOOLTIP";
        String removeContainerPanelKey = "WORKSHOP_REMOVE_CONTAINER_PANEL";
        ba_component removeContainer = new ba_component(componentMap, creatorComponent.mainPanel, containerW, containerH, containerX, containerY, true, removeContainerPanelKey);
        TooltipMakerAPI removeTooltipContainer = removeContainer.createTooltip(removeContainerTooltipKey, containerW, containerH, true, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, removeContainerPanelKey, removeContainer, containerX, containerY);

        List<ba_bionicitemplugin> availableBionics = ba_bionicmanager.getListBionicInstalledOnLimb(this.currentSelectedLimb, this.currentPerson);
        List<ba_component> subComponentItemList = new ArrayList<>();
        if(availableBionics.size() != 0) {
            int row = 0;
            int btnH = 30;
            for(final ba_bionicitemplugin bionic: availableBionics) {
                int rowX = 0;
                final int rowW = (int) containerW;
                int rowH = (int) (btnH * 2);
                int rowY = (int) ((row * rowH) + pad);
                String rowTooltipKey = "REMOVE_ROW_TOOLTIP";
                String rowPanelKey = "REMOVE_ROW_PANEL_"+ row;
                ba_component rowContainer = new ba_component(componentMap, removeContainer.mainPanel, rowW, rowH, rowX, rowY, false, rowPanelKey);
                TooltipMakerAPI rowTooltipContainer = rowContainer.createTooltip(rowTooltipKey, rowW, rowH, false, 0,0);
                removeContainer.attachSubPanel(removeContainerTooltipKey, rowPanelKey, rowContainer, rowX, rowY);
                subComponentItemList.add(rowContainer);

                //border effect list
                int borderW = (int) (containerW - pad * 2);
                int borderH = (int) (rowH - pad /2);
                int borderX = (int) pad;
                int borderY = (int) (0);
                UIComponentAPI border = rowTooltipContainer.createRect(Misc.getDarkPlayerColor(), 1);
                border.getPosition().setSize(borderW, borderH);
                rowContainer.mainPanel.addComponent(border).setLocation(0,0).inTL(borderX, borderY);

                int nameW = (int) (containerW * 0.3f - pad);
                int nameX = (int) (containerX + pad);
                int removeWarnW =  (int) (containerW * 0.4f);
                int removeWarnX = nameX + nameW;
                int removeBtnW = (int) (containerW * 0.1f);
                int removeBtnX = removeWarnX + removeWarnW;
                int removeConfirmBtnW = (int) (containerW * 0.2f - pad * 4);
                int removeConfirmBtnX = (int) (removeBtnX + removeBtnW + pad);

                int brmW = (int) (containerW * 0.3f - pad);
                int brmX = (int) nameX;
                int brmY = btnH;
                int consciousnessW = (int) (containerW * 0.40f);
                int consciousnessX = brmX + brmW;
                int consciousnessY = btnH;

                //>name
                LabelAPI bionicName = rowTooltipContainer.addPara("(%s) %s", pad, h, !Objects.equals(bionic.namePrefix, "") ? bionic.namePrefix: " ", "" + bionic.getName());
                bionicName.getPosition().setSize(nameW, btnH);
                bionicName.getPosition().inTL(nameX, pad);
                bionicName.setHighlightColors(Misc.getBasePlayerColor() ,bionic.displayColor);
                //>Remove warn
                String warnText = "No effect on remove";
                if(bionic != null && bionic.isEffectAppliedAfterRemove) {
                    if(bionic.getShortOnRemoveEffectDescription() != null && !bionic.getShortOnRemoveEffectDescription().equals("")) {
                        warnText = bionic.getShortOnRemoveEffectDescription();
                    } else {
                        warnText = "Has effects on remove!";
                    }
                }
                if(!bionic.isAllowedRemoveAfterInstall) {
                    warnText = "Can't be removed";
                }
                LabelAPI warnLabel = rowTooltipContainer.addPara(warnText, pad);
                warnLabel.getPosition().setSize(removeWarnW,btnH);
                warnLabel.setHighlight(warnText);
                warnLabel.setHighlightColors(!bionic.isAllowedRemoveAfterInstall || bionic.isEffectAppliedAfterRemove? Misc.getNegativeHighlightColor(): Misc.getGrayColor().brighter());
                warnLabel.getPosition().inTL(removeWarnX, pad);
                //>remove button
                if(bionic.isAllowedRemoveAfterInstall) {
                    if(bionic.isEffectAppliedAfterRemove) {
                        ButtonAPI removeButton = rowTooltipContainer.addButton("Remove", null, t, Color.red.darker().darker(), removeBtnW, btnH, 0);
                        removeButton.getPosition().inTL(removeBtnX,btnH/2 + borderY - 2);
                        removeButton.setEnabled(bionic.isAllowedRemoveAfterInstall && this.currentRemovingBionic == null);
                        addButtonToList(removeButton, "bionic:remove:" + bionic.bionicId);
                    }
                    //>remove button
                    ButtonAPI removeConfirmButton = rowTooltipContainer.addButton("Confirm remove", null, t, Color.red.darker().darker(), removeConfirmBtnW, btnH, 0);
                    removeConfirmButton.getPosition().inTL(removeConfirmBtnX,btnH/2 + borderY - 2);
                    addButtonToList(removeConfirmButton, "bionic:removeConfirm:"+bionic.bionicId);
                    removeConfirmButton.setEnabled(!bionic.isEffectAppliedAfterRemove || (this.currentRemovingBionic != null && this.currentRemovingBionic.bionicId.equals(bionic.bionicId)));
                    if(bionic.isEffectAppliedAfterRemove) {
                        rowTooltipContainer.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
                            @Override
                            public boolean isTooltipExpandable(Object tooltipParam) {
                                return false;
                            }

                            @Override
                            public float getTooltipWidth(Object tooltipParam) {
                                return 350;
                            }

                            @Override
                            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                                tooltip.addSectionHeading("On Remove", Alignment.MID, 0);
                                String warnText = "No effect on remove";
                                if(!bionic.isAllowedRemoveAfterInstall) {
                                    warnText = "Can't be removed";
                                    LabelAPI warnLabel = tooltip.addPara(warnText, pad);
                                    warnLabel.setHighlight(warnText);
                                    warnLabel.setHighlightColors(!bionic.isAllowedRemoveAfterInstall? Misc.getNegativeHighlightColor(): Misc.getGrayColor().brighter());
                                } else {
                                    if(bionic != null && bionic.isEffectAppliedAfterRemove) {
                                        bionic.getLongOnRemoveEffectDescription(tooltip);
                                    }
                                }

                            }
                        }, border, TooltipMakerAPI.TooltipLocation.RIGHT);
                    }
                }
                //>BRM
                LabelAPI bionicBRM = rowTooltipContainer.addPara("BRM: " + Math.round(bionic.brmCost), pad);
                bionicBRM.getPosition().setSize(brmW,btnH);
                bionicBRM.setHighlight("" + Math.round(bionic.brmCost));
                bionicBRM.setHighlightColors(Misc.getBrightPlayerColor());
                bionicBRM.getPosition().inTL(brmX, brmY);
                //>Conscious
                LabelAPI bionicConscious = rowTooltipContainer.addPara("Consciousness: " + Math.round(bionic.consciousnessCost * 100) + "%", pad);
                bionicConscious.getPosition().setSize(consciousnessW,rowH);
                bionicConscious.setHighlight("" + Math.round(bionic.consciousnessCost * 100) + "%");
                bionicConscious.setHighlightColors(Misc.getNegativeHighlightColor());
                bionicConscious.getPosition().inTL(consciousnessX, consciousnessY);

                row++;
            }
        } else {
            //Empty text
            int emptyX = (int) containerW / 2;
            int emptyY = (int) containerH / 2;
            String emptyText = "Currently no available bionic to remove.";
            LabelAPI emptyLabel = removeTooltipContainer.addPara(emptyText,Misc.getDarkPlayerColor(), 1);
            emptyLabel.getPosition().inTL(emptyX - (emptyLabel.computeTextWidth(emptyText) / 2), emptyY);
        }

        removeContainer.subComponentListMap.put("ROW_LIST", subComponentItemList);
        //scroll
        removeContainer.mainPanel.addUIElement(removeTooltipContainer).setLocation(0,0).inTL(0, 0);
        //border
        UIComponentAPI border = removeTooltipContainer.createRect(Misc.getDarkPlayerColor(), 1);
        border.getPosition().setSize(containerW, containerH);
        removeContainer.mainPanel.addComponent(border).setLocation(0,0).inTL(0, 0);
    }
    protected void installBionic() {
        if(this.currentSelectedBionic != null && this.currentSelectedLimb != null && ba_officermanager.checkIfCanInstallBionic(this.currentSelectedBionic, this.currentSelectedLimb, this.currentPerson)) {
            ba_officermanager.installBionic(this.currentSelectedBionic, this.currentSelectedLimb, this.currentPerson, true);
            this.currentSelectedLimb = null;
            this.currentSelectedBionic = null;
            this.currentRemovingBionic = null;
        }
    }
    protected void removeBionic() {
        boolean success = ba_officermanager.removeBionic(this.currentRemovingBionic, this.currentSelectedLimb, this.currentPerson);
        if (!success) {
            log.error("Can not remove " + this.currentRemovingBionic.getName() + " from person with tags: " + this.currentPerson.getTags().toString());
        }
//        this.currentSelectedLimb = null;
        this.currentSelectedBionic = null;
        this.currentRemovingBionic = null;
    }
    protected void focusContent(String focusTabId) {
        if(focusTabId == "") {
            //go to default if empty
            ba_component focusTab = tabMap.get(this.currentTabId);
            focusTab.mainPanel.getPosition().inTL(0, 0);
        } else {
            String previousTabId = this.currentTabId;
            this.currentTabId = focusTabId;
//            log.info(previousTabId);
//            log.info(currentTabId);

            //move the previous tab out
            ba_component previousTab = tabMap.get(previousTabId);
            previousTab.mainPanel.getPosition().inTL(dW,0);
            //move the focus tab in
            ba_component focusTab = tabMap.get(currentTabId);
            focusTab.mainPanel.getPosition().inTL(0, 0);
            //reset all the tab related variables
            if(this.currentHoveredBionic != null) this.currentHoveredBionic = null;
            if(this.currentSelectedBionic != null) this.currentSelectedBionic = null;
            if(this.currentSelectedLimb != null) this.currentSelectedLimb = null;
            if(this.currentRemovingBionic != null) this.currentRemovingBionic = null;
        }
        currentScrollPositionBionicTable = 0;
        currentScrollPositionInventory = 0;
        currentScrollPositionPersonList = 0;
    }
    public void getNewListPerson() {
        List<PersonAPI> tempList = new ArrayList<>((ba_officermanager.listPersons));
        ba_officermanager.refreshListPerson(tempList);
    }
    @Override
    public void saveScrollPosition() {
        super.saveScrollPosition();
        if(currentTabId.equals(OVERVIEW)) {
            ba_component component = componentMap.get("OVERVIEW_PERSON_LIST_PANEL");
            if(component != null && component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP") != null) {
                if(component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP").getExternalScroller() != null) {
                    currentScrollPositionPersonList = component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP").getExternalScroller().getYOffset();
                }
            }
        }
        if(currentTabId.equals(WORKSHOP)) {
            ba_component component = componentMap.get("WORKSHOP_PERSON_INFO_BIONICS_PANEL");
            if(component != null && component.tooltipMap.get("PERSON_INFO_BIONICS_TOOLTIP") != null) {
                if(component.tooltipMap.get("PERSON_INFO_BIONICS_TOOLTIP").getExternalScroller() != null) {
                    currentScrollPositionBionicTable = component.tooltipMap.get("PERSON_INFO_BIONICS_TOOLTIP").getExternalScroller().getYOffset();
                }
            }
        }
    }
    @Override
    public void positionChanged(PositionAPI position) {
        super.positionChanged(position);
    }

    @Override
    public void renderBelow(float alphaMult) {
        super.renderBelow(alphaMult);
    }

    @Override
    public void render(float alphaMult) {
        super.render(alphaMult);
//        ba_component previousTab2 = componentMap.get("WORKSHOP_BIONIC_PANEL_CONTAINER_0");
//        if(previousTab2.getTooltip("BIONIC_OVERCLOCK_NAME") != null) {
//            ba_utils.drawBox(
//                    (int) previousTab2.getTooltip("BIONIC_OVERCLOCK_NAME").getPosition().getX(),
//                    (int) previousTab2.getTooltip("BIONIC_OVERCLOCK_NAME").getPosition().getY(),
//                    (int) previousTab2.getTooltip("BIONIC_OVERCLOCK_NAME").getPosition().getWidth(),
//                    (int) previousTab2.getTooltip("BIONIC_OVERCLOCK_NAME").getPosition().getHeight(),
//                    0.3f,
//                    Color.pink
//            );
//        }
//        ba_component b = componentMap.get("INVENTORY_ROW_PANEL_1");
//        if(b!=null) {
//            ba_utils.drawBox(
//                    (int) b.mainPanel.getPosition().getX(),
//                    (int) b.mainPanel.getPosition().getY(),
//                    (int) b.mainPanel.getPosition().getWidth(),
//                    (int) b.mainPanel.getPosition().getHeight(),
//                    0.3f,
//                    Color.green
//            );
//        }
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
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
                if (tokens[0].equals("tab")) {
//                    log.info("clicked" + tokens[1]);
                    if(tokens[1].equals(OVERVIEW)) {
                        focusContent(OVERVIEW);
                        needsReset = true;
                        break;
                    }
                    if(tokens[1].equals(WORKSHOP)) {
                        focusContent(WORKSHOP);
                        needsReset = true;
                        break;
                    }
                }
                if(tokens[0].equals("hover_person") && !isDisplayingOtherFleets) {
                    if(!this.currentPerson.getId().equals(tokens[1])) {
                        for(PersonAPI person: ba_officermanager.listPersons) {
                            if(tokens[1].equals(person.getId())) {
                                this.currentPerson = person;
                            }
                        }
                    }
                    if(this.currentPerson != null) {
                        focusContent(WORKSHOP);
                        needsReset = true;
                        break;
                    }
                }
                if(tokens[0].equals("bionic")) {
                    if(tokens[1].equals("install")) {
                        installBionic();
                        needsReset = true;
                        break;
                    }
                    if(tokens[1].equals("edit")) {
                        if(this.currentWorkShopMode.equals(this.EDIT_WORKSHOP)) {
                            this.currentWorkShopMode = this.INSTALL_WORKSHOP;
                            this.currentSelectedLimb = null;
                        } else if(this.currentWorkShopMode.equals(this.INSTALL_WORKSHOP)) {
                            this.currentWorkShopMode = this.EDIT_WORKSHOP;
                        }
                        this.currentSelectedBionic = null;
                        this.currentRemovingBionic = null;
                        needsReset = true;
                        break;
                    }
                    if(tokens[1].equals("remove") && !tokens[2].isEmpty()) {
                        this.currentRemovingBionic = ba_bionicmanager.getBionic(tokens[2]);
                        needsReset = true;
                        break;
                    }
                    if(tokens[1].equals("removeConfirm") && !tokens[2].isEmpty()) {
                        if(ba_bionicmanager.getBionic(tokens[2].toString()) != null && !ba_bionicmanager.getBionic(tokens[2].toString()).isEffectAppliedAfterRemove) {
                            this.currentRemovingBionic = ba_bionicmanager.getBionic(tokens[2]);
                        }
                        if(tokens[2].equals(this.currentRemovingBionic.bionicId)) {
                            removeBionic();
                            List<ba_bionicitemplugin> availableRemovingBionics = ba_bionicmanager.getListBionicInstalledOnLimb(this.currentSelectedLimb, this.currentPerson);
                            if(availableRemovingBionics.isEmpty()) {
                                this.currentWorkShopMode = INSTALL_WORKSHOP;
                            }
                            needsReset = true;
                            break;
                        }
                    }
                }
                if (tokens[0].equals("workshop_tab")) {
//                    log.info("clicked" + tokens[1]);
                    if(tokens[1].equals(WORKSHOP_EFFECT) && this.currentWorkshopEffectOrInvTab.equals(WORKSHOP_INV)) {
                        this.currentWorkshopEffectOrInvTab = WORKSHOP_EFFECT;
                        needsReset = true;
                        break;
                    }
                    if(tokens[1].equals(WORKSHOP_INV) && this.currentWorkshopEffectOrInvTab.equals(WORKSHOP_EFFECT)) {
                        this.currentWorkshopEffectOrInvTab = WORKSHOP_INV;
                        needsReset = true;
                        break;
                    }
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
        super.processInput(events);
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
//                        log.info("hover " + s);
                        if(currentTabId.equals(OVERVIEW)) {
                            ba_component component = componentMap.get("OVERVIEW_PERSON_LIST_PANEL");
                            if(component != null && component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP") != null) {
                                if(tokens[0].equals("hover_person") && debounceplugin.isDebounceOver("OVERVIEW_PERSON_LIST_TOOLTIP", 0, component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP").getExternalScroller().getYOffset())) {
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
//                        if(currentTabId.equals(WORKSHOP)) {
//                            ba_component component = componentMap.get("INVENTORY_PANEL");
//                            //hover bionic item in inventory
//                            if(component != null && component.tooltipMap.get("INVENTORY_TOOLTIP") != null) {
//                                if(tokens[0].equals("hover_bionic_item") && debounceplugin.isDebounceOver("INVENTORY_TOOLTIP", 0, component.tooltipMap.get("INVENTORY_TOOLTIP").getExternalScroller().getYOffset())) {
//                                    if(ba_bionicmanager.bionicItemMap.get(tokens[1]) != null && (this.currentHoveredBionic == null || !this.currentHoveredBionic.bionicId.equals(tokens[1]))) {
//                                        this.currentHoveredBionic = (ba_bionicitemplugin) cargoBionic.get(Integer.parseInt(tokens[2])).getPlugin();
////                                    this.currentHoveredBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
//                                        shouldRefresh = true;
//                                    }
//                                }
//                            }
//                        }
                    }
                }
            }
            //is ESC is pressed, close the custom UI panel and the blank IDP we used to create it
            if (this.dialog != null && event.isKeyDownEvent() && event.getEventValue() == Keyboard.KEY_ESCAPE) {
                event.consume();
                callbacks.dismissDialog();
                if(!isDisplayingOtherFleets && (dialog.getInteractionTarget() == null || (dialog.getInteractionTarget() != null && !dialog.getInteractionTarget().getTags().contains("ba_overclock_station")))) {
                    dialog.dismiss();
                }
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
        super.buttonPressed(buttonId);
    }
}

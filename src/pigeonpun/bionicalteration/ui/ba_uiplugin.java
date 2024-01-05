package pigeonpun.bionicalteration.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.ui.trade.CargoItemStack;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.conscious.ba_conscious;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author PigeonPun
 */
public class ba_uiplugin implements CustomUIPanelPlugin {
    static Logger log = Global.getLogger(ba_uiplugin.class);
    protected CustomVisualDialogDelegate.DialogCallbacks callbacks;
    protected InteractionDialogAPI dialog;
    protected CustomPanelAPI containerPanel; //Created panel from ba_deligate.java
    protected TooltipMakerAPI mainTooltip;
    int dW, dH, pW, pH;
    public static final float MAIN_CONTAINER_PADDING = 150f;
    public static final float MAIN_CONTAINER_WIDTH = Global.getSettings().getScreenWidth() - MAIN_CONTAINER_PADDING;
    public static final float MAIN_CONTAINER_HEIGHT = Global.getSettings().getScreenHeight() - MAIN_CONTAINER_PADDING;
    List<ButtonAPI> buttons = new ArrayList<>();
    HashMap<ButtonAPI, String> buttonMap = new HashMap<>();
    HashMap<String, ba_component> componentMap = new HashMap<>();
    public static final String OVERVIEW = "OVERVIEW", WORKSHOP = "WORKSHOP";
    public final String INSTALL_WORKSHOP="INSTALL", EDIT_WORKSHOP="EDIT";
    public String currentWorkShopMode = INSTALL_WORKSHOP; //determine what mode workshop is in
    public PersonAPI currentPerson;
    public ba_bionicitemplugin currentHoveredBionic; //hovering in the inventory
    public ba_limbmanager.ba_limb currentSelectedLimb; //selected for installation/removal
    public ba_bionicitemplugin currentSelectedBionic; //selected for installation
    public ba_bionicitemplugin currentRemovingBionic; //selected for removing
    HashMap<String, ba_component> tabMap = new HashMap<>();
    String currentTabId = OVERVIEW;
    public static ba_uiplugin createDefault() {
        return new ba_uiplugin();
    }
    public void init(CustomPanelAPI panel, CustomVisualDialogDelegate.DialogCallbacks callbacks, InteractionDialogAPI dialog) {
        init(panel, callbacks, dialog, "");
    }

    /**
     * @param panel panel
     * @param callbacks callbacks
     * @param dialog dialog
     * @param moveToTabId tab id, get from uiPlugin class
     */
    public void init(CustomPanelAPI panel, CustomVisualDialogDelegate.DialogCallbacks callbacks, InteractionDialogAPI dialog, String moveToTabId) {
        this.callbacks = callbacks;
        this.containerPanel = panel;
        this.dialog = dialog;

        dW = Display.getWidth();
        dH = Display.getHeight();
        pW = (int) this.containerPanel.getPosition().getWidth();
        pH = (int) this.containerPanel.getPosition().getHeight();
        initialUICreation();
        ba_officermanager.refresh();
        //change the current tab id and "focus" on it
        focusContent(moveToTabId);
    }
    public void initialUICreation()
    {
        mainTooltip = this.containerPanel.createUIElement(this.containerPanel.getPosition().getWidth(), this.containerPanel.getPosition().getHeight(), false);
        mainTooltip.setForceProcessInput(true);
        containerPanel.addUIElement(mainTooltip).inTL(0,0);
        refresh();
    }
    protected void refresh() {
//        log.info("refreshing");
        ba_component overviewComponent = tabMap.get(OVERVIEW);
        ba_component detailComponent = tabMap.get(WORKSHOP);
        if (overviewComponent != null) {
            containerPanel.removeComponent(overviewComponent.mainPanel);
        }
        if (detailComponent != null) {
            containerPanel.removeComponent(detailComponent.mainPanel);
        }
        buttons.clear();
        buttonMap.clear();
        componentMap.clear();
        getNewListPerson();
        //create smaller container for focus/unforcus
        displayOverview();
        displayWorkshop();
        focusContent("");
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
        ba_component overviewContainer = new ba_component(containerPanel, pW, pH, MAIN_CONTAINER_PADDING/2, MAIN_CONTAINER_PADDING/2, true, mainOverviewPanelKey);
//        TooltipMakerAPI overviewTooltipContainer = overviewContainer.createTooltip(mainTooltipKey, pW, pH, false, 0, 0);
        tabMap.put(OVERVIEW, overviewContainer);
        overviewContainer.unfocusComponent();

        float listPersonW = 0.3f * pW;
        float infoPersonW = (1 - (listPersonW/pW)) * pW;
        TooltipMakerAPI overviewPersonListTooltipContainer = overviewContainer.createTooltip(mainPersonListTooltipKey, listPersonW, pH, false, 0, 0);
        TooltipMakerAPI overviewInfoTooltipContainer = overviewContainer.createTooltip(mainInfoTooltipKey, infoPersonW, pH, false, 0, 0);
        overviewInfoTooltipContainer.getPosition().inTL(listPersonW, 0);
        //overviewPerson
        displayPersonList(overviewContainer, mainPersonListTooltipKey, listPersonW, pH);
        displayPersonInfoList(overviewContainer, mainInfoTooltipKey, infoPersonW, pH, MAIN_CONTAINER_PADDING/2, MAIN_CONTAINER_PADDING/2);
    }
    protected void displayPersonList(ba_component creatorComponent, String creatorComponentTooltip, float personListW, float personListH) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

        //overview personContainer
        String overviewPersonTooltipKey = "OVERVIEW_PERSON_LIST_TOOLTIP";
        String overviewPersonPanelKey = "OVERVIEW_PERSON_LIST_PANEL";
        ba_component overviewPersonContainer = new ba_component(creatorComponent.mainPanel, personListW, personListH, MAIN_CONTAINER_PADDING/2, MAIN_CONTAINER_PADDING/2, true, overviewPersonPanelKey);
        TooltipMakerAPI overviewPersonTooltipContainer = overviewPersonContainer.createTooltip(overviewPersonTooltipKey, personListW, personListH, true, 0, 0);
        //important to set the container tooltip to have scroll enable if you want scroll
        //Next important is to have panel.addUI at the bottom of the code if you have scroll enabled, or the scroll wont work
        creatorComponent.attachSubPanel(creatorComponentTooltip,overviewPersonPanelKey, overviewPersonContainer);

        int i = 0;
        int xStart = 0;
        int yStart = 0;
        int imageH = 80;
        int imageW = 80;
        int ySpacer = 10;
        float personW = personListW - 10 * 2; //time 2 for the padding both left and right
        float personH = imageH + 20;
        List<ba_component> subComponentPersonList = new ArrayList<>();
        for (PersonAPI member: ba_officermanager.listPersons) {
            float currentStartX = xStart;
            float currentStartY = yStart;
            String spriteName = member.getPortraitSprite();
            String defaultPersonTooltipContainerKey = "PERSON_TOOLTIP_CONTAINER";
            String defaultPersonPanelContainerKey = "PERSON_PANEL_CONTAINER_"+i;
            //add first spacer
            if(subComponentPersonList.size() == 0) {
                overviewPersonTooltipContainer.addSpacer(ySpacer);
            }
            //--------person container
            ba_component personDisplayContainer = new ba_component(overviewPersonContainer.mainPanel, personW, personH,0,0,false, defaultPersonPanelContainerKey);
            TooltipMakerAPI personDisplayContainerTooltip = personDisplayContainer.createTooltip(defaultPersonTooltipContainerKey, personW, personH, false, 0,0);
            personDisplayContainerTooltip.setForceProcessInput(true);
                //attach to have the main tooltip scroll effect this component's panel
            overviewPersonContainer.attachSubPanel(overviewPersonTooltipKey, defaultPersonPanelContainerKey,personDisplayContainer);
            subComponentPersonList.add(personDisplayContainer);
                //border
//            UIComponentAPI border = personDisplayContainerTooltip.createRect(Color.red, 1);
//            border.getPosition().setSize(personW, personH);
//            personDisplayContainer.mainPanel.addComponent(border).setLocation(0,0).inTL(currentStartX, currentStartY);
            //hover
            ButtonAPI areaChecker = personDisplayContainerTooltip.addAreaCheckbox("", null,Color.red.darker(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), personW, personH, 0);
            addButtonToList(areaChecker, "hover:"+member.getId());
            areaChecker.getPosition().setLocation(0,0).inTL(currentStartX, currentStartY);
            //--------image
            int imageX = (int) currentStartX;
            TooltipMakerAPI personImageTooltip = personDisplayContainer.createTooltip("PERSON_IMAGE", imageW, imageH, false, 0, 0);
            personImageTooltip.getPosition().inTL(imageX, currentStartY);
            personImageTooltip.addImage(spriteName, imageW, imageH, 0);
            personImageTooltip.getPosition().inTL(0, (personH - imageH ) / 2);
            //---------Name
            int nameH = 30;
            int nameW = (int) (personListW - imageW - 30);
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
            int brmY = (int) (currentStartY + nameH);
            int currentBRM = (int) member.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);;
            int limitBRM = (int) member.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);;
            TooltipMakerAPI personBRMTooltip = personDisplayContainer.createTooltip("PERSON_BRM", brmW, brmH, false, 0, 0);
            personBRMTooltip.getPosition().inTL(brmX, brmY);
            LabelAPI BRM = personBRMTooltip.addPara("BRM: " + currentBRM + " / " + limitBRM, pad);
            BRM.setHighlight("BRM: ", "" +currentBRM, "" +limitBRM);
            BRM.setHighlightColors(t,currentBRM > limitBRM ? bad: h,Misc.getBrightPlayerColor());
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
            int profY = (int) (currentStartY + brmH + nameH);
            TooltipMakerAPI personProfTooltip = personDisplayContainer.createTooltip("PERSON_PROF", profW, profH, false, 9, 0);
            personProfTooltip.getPosition().inTL(profX, profY);
            String profString = ba_officermanager.getProfessionText(member);
            LabelAPI prof = personProfTooltip.addPara("Profession: " + profString, pad);
            prof.setHighlight("Profession: ", profString);
            prof.setHighlightColors(g,h);
            //Monthly Salary
            //Assign to ship/planet
            //--------Spacer because scroller dont like position offseting as spacing
            overviewPersonTooltipContainer.addSpacer(ySpacer);
            i++;
        }
        overviewPersonContainer.subComponentListMap.put("SUB_PERSON_LIST", subComponentPersonList);
        //do the adding late so the scroll work (thanks Lukas04)
        overviewPersonContainer.mainPanel.addUIElement(overviewPersonTooltipContainer);
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
        ba_component infoPersonContainer = new ba_component(creatorComponent.mainPanel, personInfoW, personInfoH, personInfoX, personInfoY, true, infoPersonPanelKey);
        TooltipMakerAPI infoPersonTooltipContainer = infoPersonContainer.createTooltip(infoPersonTooltipKey, personInfoW, personInfoH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonPanelKey,infoPersonContainer,0,0);
        //important to do this after you attach the sub panel
//        infoPersonContainer.mainPanel.getPosition().inTL(0,0);
        //--------header
        float headerW = infoPersonTooltipContainer.getPosition().getWidth();
        float headerH = 30f;
        TooltipMakerAPI headerTooltip = infoPersonContainer.createTooltip("PERSON_INFO_HEADER", headerW, headerH, false, 0,0);
        headerTooltip.getPosition().inTL(0, 0);
        headerTooltip.addSectionHeading("DETAILS", Alignment.MID, 0);
        if(currentPerson != null) {
            //--------image
            int imageX = (int) 0;
            int imageY = (int) (0 + headerH);
            int imageW = 200;
            int imageH = imageW;
            String spriteName = currentPerson.getPortraitSprite();
            TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("PERSON_INFO_IMAGE", imageW, imageH, false, 0, 0);
            personImageTooltip.getPosition().inTL(imageX, imageY);
            personImageTooltip.addImage(spriteName, imageW, imageH, 0);
            //--------Skill panels
            int skillX = (int) 0;
            int skillY = (int) (0 + headerH + imageH);
            int skillW = imageW;
            int skillH = (int) currentPerson.getStats().getSkillsCopy().size() * 100;
            TooltipMakerAPI personSkillTooltip = infoPersonContainer.createTooltip("PERSON_INFO_SKILLS", skillW, skillH, false, 0, 0);
            personSkillTooltip.getPosition().inTL(skillX,skillY);
            UIComponentAPI personSkills = personSkillTooltip.addSkillPanelOneColumn(currentPerson, 0);
            personSkills.getPosition().setSize(skillW, skillH);
            //--------Stats
            int statsX = (int) (imageW + pad);
            int statsY = imageY;
            int statsW = (int) (personInfoW - skillW - pad);
            int statsH = imageH;
            int statsSpacer = 20;
            TooltipMakerAPI personStatsTooltip = infoPersonContainer.createTooltip("PERSON_INFO_NAME", statsW, statsH, false, 0, 0);
            personStatsTooltip.getPosition().inTL(statsX,statsY);
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
            if(this.currentPerson.getFleet() != null || this.currentPerson.isPlayer()) {
                if(Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(this.currentPerson) != null) {
                    String shipName = Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(this.currentPerson).getShipName();
                    String shipClass = Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(this.currentPerson).getHullSpec().getNameWithDesignationWithDashClass();
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
            occupationLabel.getPosition().setSize(400,20);
            occupationLabel.getPosition().inTL(0, occupationY);
            //>BRM limit
            int limitBRMY = (int) (occupationY + occupationLabel.getPosition().getHeight() + statsSpacer);
            int limitBRM = (int) this.currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
            LabelAPI limitBRMLabel = personStatsTooltip.addPara(String.valueOf("BRM Limit: " + limitBRM), 0, Misc.getBrightPlayerColor(), "" + limitBRM);
            limitBRMLabel.getPosition().setSize(150,20);
            limitBRMLabel.getPosition().inTL(0, limitBRMY);
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
            LabelAPI consciousnessLabel = personStatsTooltip.addPara(ba_consciousmanager.getDisplayConsciousLabel(currentPerson) + ": " + Math.round(consciousness * 100) + "%", 0);
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
            //Button switch page
            float upgradeBtnH = 80;
            float upgradeBtnW = 200;
            int upgradeX = (int) (personInfoW - upgradeBtnW - pad - 5);
            int upgradeY = (int) headerH;
            TooltipMakerAPI personUpgradeTooltip = infoPersonContainer.createTooltip("PERSON_INFO_UPGRADE", statsW, statsH, false, 0, 0);
            personUpgradeTooltip.getPosition().inTL(upgradeX,upgradeY);
            ButtonAPI upgradeButton = personUpgradeTooltip.addButton("Upgrade/Change", null, upgradeBtnW, upgradeBtnH, 0);
            addButtonToList(upgradeButton, "tab:" + WORKSHOP);

            //--------Bionic table
            int tableX = (int) (skillX + skillW + pad);
            int tableY = (int) (imageY + imageH + pad);
            int tableW = (int) (personInfoW - skillW - pad);
            int tableH = (int) (personInfoH - imageH - headerH - pad);
            displayBionicTable(infoPersonContainer, infoPersonTooltipKey, "OVERVIEW",false, true, tableW, tableH, tableX, tableY);
        }
        //do the adding late so the scroll work
//        infoPersonContainer.mainPanel.addUIElement(infoPersonTooltipContainer);
    }
    protected void displayBionicTable(ba_component creatorComponent, String creatorComponentTooltip, String keyPrefix, final boolean isWorkshopMode, boolean isScroll , float tableW, float tableH, float tableX, float tableY) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        String prefix = keyPrefix + "_";

        String infoPersonBionicTooltipKey = "PERSON_INFO_BIONICS_TOOLTIP";
        String infoPersonBionicPanelKey = prefix + "PERSON_INFO_BIONICS_PANEL";
        ba_component infoPersonBionicContainer = new ba_component(creatorComponent.mainPanel, tableW, tableH, tableX, tableY, !isScroll, infoPersonBionicPanelKey);
        TooltipMakerAPI infoPersonBionicTooltipContainer = infoPersonBionicContainer.createTooltip(infoPersonBionicTooltipKey, tableW, tableH, isScroll, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonBionicPanelKey, infoPersonBionicContainer, tableX, tableY);

        //table header
        String tableHeaderTooltipContainerKey = "BIONIC_TABLE_HEADER_TOOLTIP";
        String tableHeaderPanelContainerKey = prefix + "BIONIC_TABLE_HEADER_PANEL";
        int tableHeaderH = 40;
        int tableHeaderW = (int) (tableW - pad);
        //--------bionic container
        ba_component tableHeaderDisplayContainer = new ba_component(infoPersonBionicContainer.mainPanel, tableHeaderW, tableHeaderH,0,0,false, tableHeaderPanelContainerKey);
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
        LabelAPI bionicConsciousHeader = tableHeaderDisplayContainerTooltip.addPara(ba_consciousmanager.getDisplayConsciousLabel(currentPerson).toUpperCase(), 0, Misc.getBrightPlayerColor(), ba_consciousmanager.getDisplayConsciousLabel(currentPerson).toUpperCase());
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
            final int bionicW = (int) (tableW - pad);
            //--------bionic container
            ba_component bionicDisplayContainer = new ba_component(infoPersonBionicContainer.mainPanel, bionicW, bionicH,0,0,false, bionicPanelContainerKey);
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
                int sectionH = bionicH;
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

                bionicInstalledI++;
            }
            i++;
        }
        infoPersonBionicContainer.subComponentListMap.put("SUB_BIONIC_LIST", subComponentBionicList);
        if(isScroll) {
            infoPersonBionicContainer.mainPanel.addUIElement(infoPersonBionicTooltipContainer);
        }
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
        ba_component workshopContainer = new ba_component(containerPanel, pW, pH, MAIN_CONTAINER_PADDING/2, MAIN_CONTAINER_PADDING/2, true, mainOverviewPanelKey);
        tabMap.put(WORKSHOP, workshopContainer);
        workshopContainer.unfocusComponent();

        //tooltip for scroll
        float personInfoW = 0.7f * pW;
        float personInfoH = 0.6f * pH;
        TooltipMakerAPI personInfoTooltipContainer = workshopContainer.createTooltip(mainPersonInfoTooltipKey, personInfoW, personInfoH, false, 0, 0);
        personInfoTooltipContainer.getPosition().inTL(0,0);
//        personInfoTooltipContainer.addPara("addd", 0);
        displayPersonInfoWorkshop(workshopContainer, mainPersonInfoTooltipKey, personInfoW, personInfoH, 0,0);

        float inventoryW = personInfoW;
        float inventoryH = (pH - (personInfoH));
        TooltipMakerAPI inventoryTooltipContainer = workshopContainer.createTooltip(mainInventoryTooltipKey, inventoryW, inventoryH, false, 0, 0);
        inventoryTooltipContainer.getPosition().inTL(0, personInfoH);
//        inventoryTooltipContainer.addPara("bbbb", 0);
        if(this.currentWorkShopMode.equals(INSTALL_WORKSHOP)) {
            displayInventoryWorkshop(workshopContainer, mainInventoryTooltipKey, inventoryW, inventoryH, 0,0);
        } else if (this.currentWorkShopMode.equals(EDIT_WORKSHOP)) {
            displayRemoveBionicWorkshop(workshopContainer, mainInventoryTooltipKey, inventoryW, inventoryH, 0,0);
        }

        float effectListW = (pW - personInfoW);
        float effectListH = pH;
        TooltipMakerAPI effectListTooltipContainer = workshopContainer.createTooltip(mainEffectsTooltipKey, effectListW, effectListH, false, 0, 0);
        effectListTooltipContainer.getPosition().inTL(personInfoW, 0);
//        effectListTooltipContainer.addPara("cccc", 0);
        displayEffectListWorkshop(workshopContainer, mainEffectsTooltipKey, effectListW, effectListH, 0,0);
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
        ba_component infoPersonContainer = new ba_component(creatorComponent.mainPanel, personInfoW, personInfoH, personInfoX, personInfoY, true, infoPersonPanelKey);
        TooltipMakerAPI infoPersonTooltipContainer = infoPersonContainer.createTooltip(infoPersonTooltipKey, personInfoW, personInfoH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonPanelKey,infoPersonContainer,0,0);

//        infoPersonTooltipContainer.addPara("person info" , 0);
        float infoLeftW = personInfoW * 0.2f;
        float infoRightW = personInfoW - infoLeftW;

        int upgradeBtnH = 40;
        int upgradeBtnW = (int) (infoLeftW - pad);
        int upgradeBtnX = (int) (0 + pad);
        int upgradeBtnY = (int) (0 + pad);
        ButtonAPI upgradeButton = infoPersonTooltipContainer.addButton("< Back", null, upgradeBtnW, upgradeBtnH, 0);
        upgradeButton.getPosition().inTL(upgradeBtnX,upgradeBtnY);
        addButtonToList(upgradeButton, "tab:" + OVERVIEW);

        //--------image
        int imageX = (int) (0 + pad);
        int imageY = (int) (upgradeBtnH + upgradeBtnY + pad);
        int imageW = (int) infoLeftW;
        int imageH = imageW;
        String spriteName = this.currentPerson.getPortraitSprite();
        TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("WORKSHOP_PERSON_IMAGE", imageW, imageH, false, 0, 0);
        personImageTooltip.getPosition().inTL(imageX, imageY);
        personImageTooltip.addImage(spriteName, imageW, imageH, 0);
        //todo: hide this in dev mode from settings.json
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
        //>Consciousness
        float consciousness = this.currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
        int consciousnessY = (int) (brmY + brmH);
        int consciousnessX = (int) (0 + pad);
        int consciousnessH = 30;
        int consciousnessW = (int) infoLeftW;
        int conditionY = (int) (consciousnessH + consciousnessY);
        int conditionX = (int) (0 + pad);
        String condition = ba_consciousmanager.getConsciousnessLevel(consciousness).getDisplayName() == null? "----": ba_consciousmanager.getConsciousnessLevel(consciousness).getDisplayName();
        //hover condition
        float hoverConsciousW = BRM.computeTextWidth("Condition: "+ condition) + pad;
        float hoverConsciousH = BRM.computeTextHeight("Condition: "+ condition) + pad;
        ButtonAPI consciousAreaChecker = infoPersonTooltipContainer.addAreaCheckbox("", null,Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), hoverConsciousW, hoverConsciousH, 0);
        addButtonToList(consciousAreaChecker, "hover_bionic_consciousness:"+ consciousness);
        consciousAreaChecker.getPosition().setLocation(0,0).inTL(conditionX - pad/2, conditionY - pad/2);
        //conscious label
        LabelAPI consciousnessLabel = infoPersonTooltipContainer.addPara(ba_consciousmanager.getDisplayConsciousLabel(currentPerson) + ": " + Math.round(consciousness * 100) + "%", 0);
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
                return 300;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                ba_consciousmanager.displayConsciousEffects(tooltip, currentPerson, expanded);
            }
        }, consciousAreaChecker, TooltipMakerAPI.TooltipLocation.ABOVE);
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
                boolean isBrmExceed = false;
                boolean isConsciousnessReduceToZero = false;
                boolean isBionicConflicted = false;
                if(currentSelectedLimb != null && currentSelectedBionic != null) {
                    if(currentSelectedLimb.limbGroupList.contains(currentSelectedBionic.bionicLimbGroupId)) isBionicInstallableOnLimb = true;
                    if(ba_bionicmanager.checkIfHaveBionicInstalled(currentSelectedBionic, currentPerson)) isBionicAlreadyInstalledOnLimb = true;
                    if(!ba_officermanager.checkIfCurrentBRMLowerThanLimitOnInstall(currentSelectedBionic, currentPerson)) isBrmExceed = true;
                    if(!ba_officermanager.checkIfConsciousnessReduceAboveZeroOnInstall(currentSelectedBionic, currentPerson)) isConsciousnessReduceToZero = true;
                    if(ba_bionicmanager.checkIfBionicConflicted(currentSelectedBionic, currentPerson)) isBionicConflicted = true;
                }
                tooltip.setParaFontVictor14();
                tooltip.addPara("Button is still disabled ? Hover on selected bionic/limb for more information.", pad);
                tooltip.setParaFontDefault();
                tooltip.addPara("Make sure that: ", pad);
                LabelAPI bionicInstallableLabel = tooltip.addPara("[ %s ] %s on selected limb.", pad/2, Misc.getHighlightColor(), isBionicInstallableOnLimb? "O": "X","Selected bionic can be installed");
                bionicInstallableLabel.setHighlightColors(isBionicInstallableOnLimb? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                LabelAPI bionicInstalledLabel = tooltip.addPara("[ %s ] %s on the selected bionic.", pad/2, Misc.getHighlightColor(), !isBionicAlreadyInstalledOnLimb? "O": "X", "Selected limb is not installed");
                bionicInstalledLabel.setHighlightColors(!isBionicAlreadyInstalledOnLimb? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                LabelAPI brmLabel = tooltip.addPara("[ %s ] %s the person BRM limit.", pad/2, Misc.getHighlightColor(), !isBrmExceed? "O": "X","Selected bionics BRM do not go past");
                brmLabel.setHighlightColors(!isBrmExceed? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                LabelAPI consciousnessLabel = tooltip.addPara("[ %s ] %s the person's consciousness to lower or equal to %s.", pad/2, Misc.getHighlightColor(), !isConsciousnessReduceToZero? "O": "X","Selected bionics consciousness cost does not reduce", "0");
                consciousnessLabel.setHighlightColors(!isConsciousnessReduceToZero? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                LabelAPI conflictedLabel = tooltip.addPara("[ %s ] %s with other bionics installed on the person.", pad/2, Misc.getHighlightColor(), !isBionicConflicted? "O": "X", "Selected bionic is not conflicting");
                conflictedLabel.setHighlightColors(!isBionicConflicted? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
            }
        }, installButton, TooltipMakerAPI.TooltipLocation.ABOVE);
        //--------remove button
        //edit: enter edit mode, display list a list of bionic for a limb with remove button next to it
        int removeBtnH = btnH;
        int removeBtnW = (int) (100 - pad);
        int removeBtnX = (int) (installBtnX - pad - removeBtnW);
        int removeBtnY = (int) (installBtnY);
        ButtonAPI removeButton = infoPersonTooltipContainer.addButton(this.currentWorkShopMode.equals(this.INSTALL_WORKSHOP) ?"Edit": "Exit edit", null, t, Color.yellow.darker().darker(), removeBtnW, removeBtnH, 0);
        removeButton.getPosition().inTL(removeBtnX,removeBtnY);
        addButtonToList(removeButton, "bionic:edit");
        removeButton.setEnabled(false);
        if(this.currentSelectedLimb != null && ba_officermanager.checkIfCanEditLimb(this.currentSelectedLimb, this.currentPerson)) {
            removeButton.setEnabled(true);
            removeButton.flash(false);
        }
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
                tooltip.addPara("3. Find the bionic you want to remove, click the %s button", pad, Misc.getHighlightColor(), "REMOVE");
                tooltip.addPara("4. Confirm remove by clicking the %s button", pad, Misc.getHighlightColor(), "CONFIRM REMOVE");
                tooltip.addPara("The remove bionic will appear in your inventory. (Click the %s again to exist %s)", pad, Misc.getBasePlayerColor(),"Edit button", "Edit mode");
                tooltip.addPara("Note: Some bionics can not be removed, some have effects on remove and some once removed do not return the bionic item", pad);
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
        displayBionicTable(infoPersonContainer, infoPersonTooltipKey, "WORKSHOP",true, true, tableW, tableH, tableX, tableY);
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
        LabelAPI selectedBionicLabel = infoPersonTooltipContainer.addPara("%s %s", 0, t,"Selected:",  bionicName);
        selectedBionicLabel.getPosition().inTL(selectedBionicX,selectedBionicY);
        selectedBionicLabel.getPosition().setSize(selectedW, selectedH);
        selectedBionicLabel.setHighlight("Selected:", bionicName);
        selectedBionicLabel.setHighlightColors(Misc.getBrightPlayerColor(), this.currentSelectedBionic != null ? this.currentSelectedBionic.displayColor: Misc.getGrayColor());
    }
    public void displayInventoryWorkshop(ba_component creatorComponent, String creatorComponentTooltip, float inventoryW, float inventoryH, float inventoryX, float inventoryY) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        //big container
        final float containerW = inventoryW - pad - pad / 2;
        int containerH = (int) (inventoryH - pad - pad);
        int containerX = (int) pad;
        int containerY = (int) pad;
        String inventoryTooltipKey = "WORKSHOP_INVENTORY_TOOLTIP";
        String inventoryPanelKey = "WORKSHOP_INVENTORY_PANEL";
        ba_component inventoryContainer = new ba_component(creatorComponent.mainPanel, containerW, containerH, containerX, containerY, true, inventoryPanelKey);
        TooltipMakerAPI inventoryTooltipContainer = inventoryContainer.createTooltip(inventoryTooltipKey, containerW, containerH, true, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryPanelKey, inventoryContainer, containerX, containerY);

        CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
        List<CargoStackAPI> availableBionics = new ArrayList<>();
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
            if(availableBionics.size() / itemsPerRow > defaultRows) {
                neededRows = Math.round((float) availableBionics.size() / itemsPerRow);
            }
            while(row < neededRows) {
                int rowX = 0;
                int rowY = (int) (row * itemH);
                final int rowW = (int) containerW;
                int rowH = (int) (itemH);
                String rowTooltipKey = "INVENTORY_ROW_TOOLTIP";
                String rowPanelKey = "INVENTORY_ROW_PANEL_"+ row;
                ba_component rowContainer = new ba_component(inventoryContainer.mainPanel, rowW, rowH, rowX, rowY, false, rowPanelKey);
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
                        if(this.currentSelectedBionic != null) {
                            if(currentSelectedBionic.equals(bionic)) {
                                areaChecker.highlight();
                            }
                        }
                        if(this.currentSelectedLimb != null) {
                            if(ba_bionicmanager.checkIfBionicConflicted(bionic, this.currentPerson)) {
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
                                //---------name
                                tooltip.setParaInsigniaLarge();
                                LabelAPI nameLabel = tooltip.addPara(currentHoveredBionic.getName(), Misc.getHighlightColor(),0);
                                tooltip.addSpacer(10);
                                tooltip.setParaFontDefault();
                                //---------design
                                LabelAPI designLabel = tooltip.addPara("%s %s",0,t, "Design by:",currentHoveredBionic.getDesignType());
                                designLabel.setHighlight("Design by:", currentHoveredBionic.getDesignType());
                                designLabel.setHighlightColors(g, t.darker());
                                //---------effect
                                currentHoveredBionic.effectScript.displayEffectDescription(tooltip, currentPerson, currentHoveredBionic, true);
//                                String effect = "No effects yet...";
//                                if(currentHoveredBionic.effectScript != null) {
//                                    effect = currentHoveredBionic.effectScript.getShortEffectDescription();
//                                }
//                                LabelAPI effectLabel = tooltip.addPara("%s %s", pad, Misc.getBasePlayerColor(), "Effects:", effect);
//                                effectLabel.setHighlight("Effects:", effect);
//                                effectLabel.setHighlightColors(g.brighter().brighter(), currentHoveredBionic.effectScript != null ? Misc.getBrightPlayerColor() :Misc.getGrayColor());
                                //---------BRM + conscious
                                LabelAPI brmConsciousLabel = tooltip.addPara("%s %s     %s %s", pad, Misc.getBasePlayerColor(), "BRM:", "" + Math.round(currentHoveredBionic.brmCost), "Conscious:", "" + Math.round(currentHoveredBionic.consciousnessCost * 100) + "%");
                                brmConsciousLabel.setHighlight("BRM:", "" + Math.round(currentHoveredBionic.brmCost), "Conscious:", "" + Math.round(currentHoveredBionic.consciousnessCost * 100) + "%");
                                brmConsciousLabel.setHighlightColors(g.brighter().brighter(), Color.red, g.brighter().brighter(), Color.red);
                                //---------limb list
                                StringBuilder limbNameList = new StringBuilder();
                                for (ba_limbmanager.ba_limb limb: ba_limbmanager.getListLimbFromGroup(currentHoveredBionic.bionicLimbGroupId)) {
                                    limbNameList.append(limb.name).append(", ");
                                }
                                if(limbNameList.length() > 0) limbNameList.setLength(limbNameList.length()-2);
                                LabelAPI limbListLabel = tooltip.addPara("%s %s", pad, t,"Install on:", limbNameList.toString());
                                limbListLabel.setHighlight("Install on:", limbNameList.toString());
                                limbListLabel.setHighlightColors(g.brighter().brighter(), Misc.getBrightPlayerColor());
                                //---------Conflicts
                                StringBuilder conflictsList = new StringBuilder();
                                for (ba_bionicitemplugin bionic: ba_bionicmanager.getListBionicConflicts(currentHoveredBionic)) {
                                    conflictsList.append(bionic.getName()).append(", ");
                                }
                                if(conflictsList.length() > 0) {
                                    conflictsList.setLength(conflictsList.length() - 2);
                                } else {
                                    conflictsList.append("None");
                                }
                                LabelAPI conflictListLabel = tooltip.addPara("%s %s", pad, t,"Conflicts:", conflictsList.toString());
                                conflictListLabel.setHighlight("Conflicts:", conflictsList.toString());
                                conflictListLabel.setHighlightColors(g.brighter().brighter(), conflictsList.toString().equals("None")? g: Misc.getNegativeHighlightColor());
                                //----------desc
                                String desc = currentHoveredBionic.getSpec().getDesc();
                                LabelAPI descLabel = tooltip.addPara("%s %s", pad, t, "Description:", desc);
                                descLabel.setHighlight("Description:", desc);
                                descLabel.setHighlightColors(g.brighter().brighter(), t);
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

        //border
        UIComponentAPI border = inventoryTooltipContainer.createRect(Misc.getDarkPlayerColor(), 1);
        border.getPosition().setSize(containerW, containerH);
        inventoryContainer.mainPanel.addComponent(border).setLocation(0,0).inTL(0, 0);
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
        ba_component effectListContainer = new ba_component(creatorComponent.mainPanel, effectListW, effectListH, effectListX, effectListY, true, effectListPanelKey);
        TooltipMakerAPI effectListTooltipContainer = effectListContainer.createTooltip(effectListTooltipKey, effectListW, effectListH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, effectListPanelKey,effectListContainer,0,0);

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
        ba_component subEffectListContainer = new ba_component(effectListContainer.mainPanel, subEffectW, subEffectH, subEffectX, subEffectY, false, subEffectListPanelKey);
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
            for (ba_bionicitemplugin bionic: bionicAugmentedDatas.bionicInstalled) {
//                String effect = "No effects yet...";
//                if(bionic.effectScript != null) {
//                    effect = bionic.effectScript.getShortEffectDescription();
//                }
//                LabelAPI expandedTooltip = subEffectListTooltipContainer.addPara("%s - %s", pad, Misc.getBasePlayerColor(), bionic.getName(), effect);
//                expandedTooltip.setHighlight(bionic.getName(), effect);
//                expandedTooltip.setHighlightColors(bionic.displayColor, bionic.effectScript != null ? t :Misc.getGrayColor());
//                expandedTooltip.getPosition().inTL(pad, expandedTooltip.getPosition().getHeight() * i);
                bionic.effectScript.displayEffectDescription(subEffectListTooltipContainer, currentPerson, bionic, false);

                subEffectListTooltipContainer.addSpacer(spacerY);
                i++;
            }
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
        ba_component removeContainer = new ba_component(creatorComponent.mainPanel, containerW, containerH, containerX, containerY, true, removeContainerPanelKey);
        TooltipMakerAPI removeTooltipContainer = removeContainer.createTooltip(removeContainerTooltipKey, containerW, containerH, true, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, removeContainerPanelKey, removeContainer, containerX, containerY);

        List<ba_bionicitemplugin> availableBionics = ba_bionicmanager.getListBionicInstalledOnLimb(this.currentSelectedLimb, this.currentPerson);
        List<ba_component> subComponentItemList = new ArrayList<>();
        if(availableBionics.size() != 0) {
            int row = 0;
            int btnH = 30;
            for(ba_bionicitemplugin bionic: availableBionics) {
                int rowX = 0;
                final int rowW = (int) containerW;
                int rowH = (int) (btnH * 2);
                int rowY = (int) ((row * rowH) + pad);
                String rowTooltipKey = "REMOVE_ROW_TOOLTIP";
                String rowPanelKey = "REMOVE_ROW_PANEL_"+ row;
                ba_component rowContainer = new ba_component(removeContainer.mainPanel, rowW, rowH, rowX, rowY, false, rowPanelKey);
                TooltipMakerAPI rowTooltipContainer = rowContainer.createTooltip(rowTooltipKey, rowW, rowH, false, 0,0);
                removeContainer.attachSubPanel(removeContainerTooltipKey, rowPanelKey, rowContainer, rowX, rowY);
                subComponentItemList.add(rowContainer);

                int nameW = (int) (containerW * 0.4f - pad);
                int nameX = (int) (containerX + pad);
                int removeWarnW =  (int) (containerW * 0.3f);
                int removeWarnX = nameX + nameW;
                int removeBtnW = (int) (containerW * 0.1f);
                int removeBtnX = removeWarnX + removeWarnW;
                int removeConfirmBtnW = (int) (containerW * 0.2f - pad - pad - pad);
                int removeConfirmBtnX = (int) (removeBtnX + removeBtnW + pad);

                int brmW = (int) (containerW * 0.4f - pad);
                int brmX = (int) nameX;
                int brmY = btnH;
                int consciousnessW = (int) (containerW * 0.30f);
                int consciousnessX = brmX + brmW;
                int consciousnessY = btnH;

                //>name
                LabelAPI bionicName = rowTooltipContainer.addPara("(%s) %s", pad, h, !Objects.equals(bionic.namePrefix, "") ? bionic.namePrefix: " ", "" + bionic.getName());
                bionicName.getPosition().setSize(nameW, btnH);
                bionicName.getPosition().inTL(nameX, pad);
                bionicName.setHighlightColors(Misc.getBasePlayerColor() ,bionic.displayColor);
                //>Remove warn
                String warnText = "No effect on remove";
                if(bionic.effectScript != null && bionic.effectScript.getShortOnRemoveEffectDescription() != null) {
                    if(!bionic.effectScript.getShortOnRemoveEffectDescription().equals("")) {
                        warnText = bionic.effectScript.getShortOnRemoveEffectDescription();
                    } else {
                        warnText = "No description yet...";
                    }
                    if(!bionic.isAllowedRemoveAfterInstall) {
                        warnText = "Can't be removed";
                    }
                }
                LabelAPI warnLabel = rowTooltipContainer.addPara(warnText, pad);
                warnLabel.getPosition().setSize(removeWarnW,btnH);
                warnLabel.setHighlight(warnText);
                warnLabel.setHighlightColors(bionic.effectScript != null && bionic.effectScript.getShortOnRemoveEffectDescription() != null? Misc.getNegativeHighlightColor(): Misc.getGrayColor().brighter());
                warnLabel.getPosition().inTL(removeWarnX, pad);
                //>remove button
                ButtonAPI removeButton = rowTooltipContainer.addButton("Remove", null, t, Color.yellow.darker().darker(), removeBtnW, btnH, 0);
                removeButton.getPosition().inTL(removeBtnX,pad);
                removeButton.setEnabled(bionic.isAllowedRemoveAfterInstall);
                addButtonToList(removeButton, "bionic:remove:" + bionic.bionicId);
                //>remove button
                ButtonAPI removeConfirmButton = rowTooltipContainer.addButton("Confirm remove", null, t, Color.red.darker().darker(), removeConfirmBtnW, btnH, 0);
                removeConfirmButton.getPosition().inTL(removeConfirmBtnX,pad);
                addButtonToList(removeConfirmButton, "bionic:removeConfirm:"+bionic.bionicId);
                removeConfirmButton.setEnabled(this.currentRemovingBionic != null && this.currentRemovingBionic.bionicId.equals(bionic.bionicId));
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
            ba_officermanager.installBionic(this.currentSelectedBionic, this.currentSelectedLimb, this.currentPerson);
            this.currentSelectedLimb = null;
            this.currentSelectedBionic = null;
            this.currentRemovingBionic = null;
        }
    }
    protected void removeBionic() {
        ba_officermanager.removeBionic(this.currentRemovingBionic, this.currentSelectedLimb, this.currentPerson);
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
    }
    public void getNewListPerson() {
        ba_officermanager.refreshListPerson();
    }
    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {
//        ba_component previousTab2 = componentMap.get("WORKSHOP_BIONIC_DETAIL_PANEL");
//        ba_utils.drawBox(
//                (int) previousTab2.getTooltip("WORKSHOP_BIONIC_DETAIL_TOOLTIP").getPosition().getX(),
//                (int) previousTab2.getTooltip("WORKSHOP_BIONIC_DETAIL_TOOLTIP").getPosition().getY(),
//                (int) previousTab2.getTooltip("WORKSHOP_BIONIC_DETAIL_TOOLTIP").getPosition().getWidth(),
//                (int) previousTab2.getTooltip("WORKSHOP_BIONIC_DETAIL_TOOLTIP").getPosition().getHeight(),
//                0.3f,
//                Color.pink
//        );
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
                if(tokens[0].equals("hover_bionic_item")) {
                    if(ba_bionicmanager.bionicItemMap.get(tokens[1]) != null) {
                        this.currentSelectedBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
                        needsReset = true;
                        break;
                    }
                }
                if(this.currentTabId.equals(WORKSHOP) && tokens[0].equals("hover_bionic_table_limb")) {
                    this.currentSelectedLimb = ba_limbmanager.getLimb(tokens[1]);
                    needsReset = true;
                    break;
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
                        if(tokens[2].equals(this.currentRemovingBionic.bionicId)) {
                            removeBionic();
                            needsReset = true;
                            break;
                        }
                    }
                }
            }
        }

        //pressing a button usually means something we are displaying has changed, so redraw the panel from scratch
        if (needsReset) refresh();
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        boolean shouldRefresh = false;
        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;
            if(event.isMouseMoveEvent()) {
                //hover check
                for (ButtonAPI button: buttons) {
                    float buttonX = button.getPosition().getX();
                    float buttonY = button.getPosition().getY();
                    float buttonW = button.getPosition().getWidth();
                    float buttonH = button.getPosition().getHeight();
                    if(event.getX() >= buttonX && event.getX() < buttonX + buttonW && event.getY() >= buttonY && event.getY() < buttonY+buttonH) {
                        String s = buttonMap.get(button);
                        String[] tokens = s.split(":");
//                        log.info("hover " + s);
                        if(tokens[0].equals("hover")) {
                            if(!this.currentPerson.getId().equals(tokens[1])) {
                                for(PersonAPI person: ba_officermanager.listPersons) {
                                    if(tokens[1].equals(person.getId())) {
                                        this.currentPerson = person;
                                        shouldRefresh = true;
                                    }
                                }
                            }
                        }
                        //hover bionic item in inventory
                        if(tokens[0].equals("hover_bionic_item")) {
                            if(ba_bionicmanager.bionicItemMap.get(tokens[1]) != null && (this.currentHoveredBionic == null || !this.currentHoveredBionic.bionicId.equals(tokens[1]))) {
                                this.currentHoveredBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
                                shouldRefresh = true;
                            }
                        }
                    }
                }
            }
            //is ESC is pressed, close the custom UI panel and the blank IDP we used to create it
            if (event.isKeyDownEvent() && event.getEventValue() == Keyboard.KEY_ESCAPE) {
                event.consume();
                callbacks.dismissDialog();
                dialog.dismiss();
                return;
            }
        }

        if(shouldRefresh) refresh();
    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
    public void addButtonToList(ButtonAPI button, String buttonMapValue) {
        buttons.add(button);
        buttonMap.put(button, buttonMapValue);
    }
    public class ba_component {
//        protected TooltipMakerAPI tooltip;
        protected CustomPanelAPI mainPanel;
        protected HashMap<String, TooltipMakerAPI> tooltipMap = new HashMap<>();
        protected HashMap<String, List<TooltipMakerAPI>> tooltipListMap = new HashMap<>();
        protected HashMap<String, ba_component> subComponentMap = new HashMap<>();
        protected HashMap<String, List<ba_component>> subComponentListMap = new HashMap<>();
        /**
         * Create component and attach the component's panel to the creator panel<br>
         * @param creatorPanel creator panel
         * @param width Width of this component
         * @param height Height of this component
         * @param x location
         * @param y location
         * @param addToCreatorPanel true if is container, false if is element inside a scrolling container
         * @param thisComponentMapKey this component map key, access from {@code componentMap}
         */
        public ba_component(CustomPanelAPI creatorPanel, float width, float height,float x, float y, boolean addToCreatorPanel,String thisComponentMapKey) {
            //create both panel and tooltip
            mainPanel = creatorPanel.createCustomPanel(width, height, null);
            mainPanel.getPosition().setLocation(0,0);
            mainPanel.getPosition().inTL(x, y);
            //add into the big panel
            if(addToCreatorPanel) {
                creatorPanel.addComponent(mainPanel);
            }
            //add into list to remove on reset
            if(componentMap.get(thisComponentMapKey) == null) {
                componentMap.put(thisComponentMapKey, this);
            } else {
                log.error("Component already exist!!!. Key: " + thisComponentMapKey);
            }
        }

        /**
         * @param key
         * @param width
         * @param height
         * @param hasScroller If true, tooltip won't be added to the panel. Meaning, you will have to manually add the tooltip to the panel later on so the scroll work
         * @param tooltipLocationX
         * @param tooltipLocationY
         * @return
         */
        protected TooltipMakerAPI createTooltip(String key, float width, float height, boolean hasScroller, float tooltipLocationX, float tooltipLocationY) {
            TooltipMakerAPI tooltip = this.mainPanel.createUIElement(width, height, hasScroller);
            tooltip.setForceProcessInput(true);
            if(!hasScroller) this.mainPanel.addUIElement(tooltip).setLocation(tooltipLocationX, tooltipLocationY);
            this.tooltipMap.put(key, tooltip);
            return tooltip;
        }
        protected TooltipMakerAPI getTooltip(String key) {
            TooltipMakerAPI tooltip = this.tooltipMap.get(key);
            if(tooltip == null) {
                log.error("Can not find tooltip of key " + key);
            }
            return tooltip;
        }
        /**
         * Use for attaching an existing component's panel into this component<br>
         * @param tooltipKeyAttachTo Creator component's tooltip key
         * @param otherComponent the attaching component
         * @param otherComponentMapKey the attaching component map key. So the creator component can access to the attaching component in {@code subComponentMap}
         */
        protected void attachSubPanel(String tooltipKeyAttachTo, String otherComponentMapKey, ba_component otherComponent) {
            TooltipMakerAPI tooltipAttachingTo = this.tooltipMap.get(tooltipKeyAttachTo);
            if(tooltipAttachingTo == null) {
                log.error("Can't find container tooltip of Id: " + tooltipAttachingTo + " for: " + otherComponentMapKey);
            }
            tooltipAttachingTo.addCustom(otherComponent.mainPanel, 0f);
            subComponentMap.put(otherComponentMapKey, otherComponent);
        }
        /**
         * Use for attaching an existing component's panel into this component<br>
         * Use this for when the Sub component is aligning with the parent component which we want to break and reset it to certain point
         * @param tooltipKeyAttachTo Creator component's tooltip key
         * @param otherComponent the attaching component
         * @param otherComponentMapKey the attaching component map key. So the creator component can access to the attaching component in {@code subComponentMap}
         * @param locationX X
         * @param locationY Y
         */
        protected void attachSubPanel(String tooltipKeyAttachTo, String otherComponentMapKey, ba_component otherComponent, float locationX, float locationY) {
            attachSubPanel(tooltipKeyAttachTo, otherComponentMapKey, otherComponent);
            //important to do this after you attach the sub panel
            otherComponent.mainPanel.getPosition().inTL(locationX,locationY);
        }
        public void unfocusComponent() {
            mainPanel.getPosition().inTL(dW, 0);
        }
    }
}

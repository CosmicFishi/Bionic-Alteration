package pigeonpun.bionicalteration.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
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
    public PersonAPI currentHoveredPerson;
    public ba_limbmanager.ba_limb currentSelectedLimb;
    public ba_bionicitemplugin currentSelectedBionic;
    HashMap<String, ba_component> tabMap = new HashMap<>();
    String currentTabId = WORKSHOP;
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
        if(ba_officermanager.listPersons.size() != 0 && this.currentHoveredPerson == null) {
            this.currentHoveredPerson = ba_officermanager.listPersons.get(0);
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
            String profString = "Idle";
            if(member.getFleet() != null || member.isPlayer()) {
                if(Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(member) != null) {
                    profString = "Captain";
                }
            } else if (member.getMarket() != null) {
                MarketAPI market = member.getMarket();
                if(market.getAdmin() == member) {
                    profString = "Admin";
                }
            }
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
        if(currentHoveredPerson != null) {
            //--------image
            int imageX = (int) 0;
            int imageY = (int) (0 + headerH);
            int imageW = 200;
            int imageH = imageW;
            String spriteName = currentHoveredPerson.getPortraitSprite();
            TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("PERSON_INFO_IMAGE", imageW, imageH, false, 0, 0);
            personImageTooltip.getPosition().inTL(imageX, imageY);
            personImageTooltip.addImage(spriteName, imageW, imageH, 0);
            //--------Skill panels
            int skillX = (int) 0;
            int skillY = (int) (0 + headerH + imageH);
            int skillW = imageW;
            int skillH = (int) currentHoveredPerson.getStats().getSkillsCopy().size() * 100;
            TooltipMakerAPI personSkillTooltip = infoPersonContainer.createTooltip("PERSON_INFO_SKILLS", skillW, skillH, false, 0, 0);
            personSkillTooltip.getPosition().inTL(skillX,skillY);
            UIComponentAPI personSkills = personSkillTooltip.addSkillPanelOneColumn(currentHoveredPerson, 0);
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
            LabelAPI nameLabel = personStatsTooltip.addPara(this.currentHoveredPerson.getName().getFullName() + (currentHoveredPerson.isPlayer() ? " (You)" : ""), 0, Misc.getBrightPlayerColor(), this.currentHoveredPerson.getName().getFullName());
            nameLabel.getPosition().setSize(200,20);
            nameLabel.getPosition().inTL(0,0);
            //>Level
            LabelAPI levelLabel = personStatsTooltip.addPara("Level: " + this.currentHoveredPerson.getStats().getLevel(), 0, Misc.getHighlightColor(), "" + this.currentHoveredPerson.getStats().getLevel());
            levelLabel.getPosition().setSize(100,20);
            levelLabel.getPosition().inTL(nameLabel.getPosition().getWidth() + pad, 0);
            //>personality
            LabelAPI personalityLabel = personStatsTooltip.addPara("Personality: " + this.currentHoveredPerson.getPersonalityAPI().getDisplayName(), 0, Misc.getHighlightColor(), "" + this.currentHoveredPerson.getPersonalityAPI().getDisplayName());
            personalityLabel.getPosition().setSize(200,20);
            personalityLabel.getPosition().inTL(0, nameLabel.getPosition().getHeight() + statsSpacer);
            //>Occupation
            String occupation = "Idle";
            if(this.currentHoveredPerson.getFleet() != null || this.currentHoveredPerson.isPlayer()) {
                if(Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(this.currentHoveredPerson) != null) {
                    String shipName = Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(this.currentHoveredPerson).getShipName();
                    String shipClass = Global.getSector().getPlayerFleet().getFleetData().getMemberWithCaptain(this.currentHoveredPerson).getHullSpec().getNameWithDesignationWithDashClass();
                    occupation = "Piloting "+ shipName + " of " + shipClass;
                }
            } else if (this.currentHoveredPerson.getMarket() != null) {
                MarketAPI market = this.currentHoveredPerson.getMarket();
                if(market.getAdmin() == this.currentHoveredPerson) {
                    occupation = "Admin of " + market.getName();
                }
            }
            int occupationY = (int) (nameLabel.getPosition().getHeight() + statsSpacer + personalityLabel.getPosition().getHeight() + statsSpacer);
            LabelAPI occupationLabel = personStatsTooltip.addPara(String.valueOf("Currently: " + occupation), 0, Misc.getGrayColor(), "Currently: " + occupation);
            occupationLabel.getPosition().setSize(400,20);
            occupationLabel.getPosition().inTL(0, occupationY);
            //>BRM limit
            int limitBRMY = (int) (occupationY + occupationLabel.getPosition().getHeight() + statsSpacer);
            int limitBRM = (int) this.currentHoveredPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
            LabelAPI limitBRMLabel = personStatsTooltip.addPara(String.valueOf("BRM Limit: " + limitBRM), 0, Misc.getBrightPlayerColor(), "" + limitBRM);
            limitBRMLabel.getPosition().setSize(150,20);
            limitBRMLabel.getPosition().inTL(0, limitBRMY);
            //>BRM available
            int currentBRM = (int) this.currentHoveredPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);
            int currentBRMY = (int) limitBRMY;
            int currentBRMX = (int) (limitBRMLabel.getPosition().getWidth());
            LabelAPI currentBRMLabel = personStatsTooltip.addPara(String.valueOf("BRM Using: " + currentBRM), 0, currentBRM > limitBRM ? bad: Misc.getHighlightColor(), "" + currentBRM);
            currentBRMLabel.getPosition().setSize(150,20);
            currentBRMLabel.getPosition().inTL(currentBRMX, currentBRMY);
            //>Consciousness
            float consciousness = this.currentHoveredPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
            int consciousnessY = (int) limitBRMY;
            int consciousnessX = (int) (currentBRMX + currentBRMLabel.getPosition().getWidth());
            LabelAPI consciousnessLabel = personStatsTooltip.addPara("Conscious: " + Math.round(consciousness * 100) + "%", 0);
            consciousnessLabel.setHighlight("" + Math.round(consciousness * 100) + "%");
            consciousnessLabel.setHighlightColor(ba_officermanager.getConsciousnessColorByLevel(consciousness));
            consciousnessLabel.getPosition().setSize(150,20);
            consciousnessLabel.getPosition().inTL(consciousnessX, consciousnessY);
            //>Conditions: tiled with conscious //todo: add in conscious related code
            String condition = "Fine";
            int conditionY = (int) (limitBRMY + statsSpacer + limitBRMLabel.getPosition().getHeight());
            int conditionX = (int) (0);
            LabelAPI conditionLabel = personStatsTooltip.addPara("Condition: " + condition + "", 0);
            conditionLabel.setHighlight("" + condition);
            conditionLabel.setHighlightColor(ba_officermanager.getConsciousnessColorByLevel(consciousness));
            conditionLabel.getPosition().setSize(150,20);
            conditionLabel.getPosition().inTL(conditionX, conditionY);
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
    //todo: make isWorkshopMode a thing (clicking on a line, remove/add etc)
    protected void displayBionicTable(ba_component creatorComponent, String creatorComponentTooltip, String keyPrefix, boolean isWorkshopMode, boolean isScroll ,float tableW, float tableH, float tableX, float tableY) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();
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
        LabelAPI bionicConsciousHeader = tableHeaderDisplayContainerTooltip.addPara("CONSCIOUS", 0, Misc.getBrightPlayerColor(), "CONSCIOUS");
        bionicConsciousHeader.getPosition().setSize(bionicConsciousW, tableHeaderH);
        bionicConsciousHeader.getPosition().inTL(bionicConsciousX + bionicNameX,0);
        bionicConsciousHeader.setAlignment(Alignment.MID);

        //rows
        int i = 0;
        List<ba_component> subComponentBionicList = new ArrayList<>();
        List<ba_officermanager.ba_bionicAugmentedData> currentAnatomyList = ba_officermanager.getBionicAnatomyList(this.currentHoveredPerson);
//        log.info(currentAnatomyList.size() + " - " + this.currentHoveredPerson.getTags());
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
            addButtonToList(areaChecker, "hover_bionic:"+bionic);
            areaChecker.getPosition().setLocation(0,0).inTL(0, 0);
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
                            LabelAPI descriptions = tooltip.addPara(b.getName() + ": " + b.getSpec().getDesc(), pad);
                            descriptions.setHighlight(b.getName());
                            descriptions.setHighlightColor(b.displayColor);
                            if(expanded) {
                                String effect = "No effects yet...";
                                if(b.effectScript != null) {
                                    effect = b.effectScript.getShortEffectDescription();
                                }
                                LabelAPI expandedTooltip = tooltip.addPara("%s %s", pad, Misc.getBasePlayerColor(), "Effects:", effect);
                                expandedTooltip.setHighlight("Effects:", effect);
                                expandedTooltip.setHighlightColors(Misc.getGrayColor().brighter(), b.effectScript != null ? Misc.getHighlightColor() :Misc.getGrayColor());
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
        //set default hover person - this shouldn't be triggered by default but somehow currentHoveredPerson is null, do it
        if(ba_officermanager.listPersons.size() != 0 && this.currentHoveredPerson == null) {
            this.currentHoveredPerson = ba_officermanager.listPersons.get(0);
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
        displayInventoryWorkshop(workshopContainer, mainInventoryTooltipKey, inventoryW, inventoryH, 0,0);

        float effectListW = (pW - personInfoW);
        float effectListH = pH;
        TooltipMakerAPI effectListTooltipContainer = workshopContainer.createTooltip(mainEffectsTooltipKey, effectListW, effectListH, false, 0, 0);
        effectListTooltipContainer.getPosition().inTL(personInfoW, effectListTooltipContainer.getPosition().getY());
//        effectListTooltipContainer.addPara("cccc", 0);
        displayEffectListWorkshop(workshopContainer, mainEffectsTooltipKey, effectListW, effectListH, 0,0);
    }
    public void displayPersonInfoWorkshop(ba_component creatorComponent, String creatorComponentTooltip, float personInfoW, float personInfoH, float personInfoX, float personInfoY) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

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
        String spriteName = this.currentHoveredPerson.getPortraitSprite();
        TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("WORKSHOP_PERSON_IMAGE", imageW, imageH, false, 0, 0);
        personImageTooltip.getPosition().inTL(imageX, imageY);
        personImageTooltip.addImage(spriteName, imageW, imageH, 0);
        //---------Name
        int nameH = 30;
        int nameW = (int) infoLeftW;
        int nameX = (int) (0 + pad);
        int nameY = (int) (imageH + imageY+ pad);
        LabelAPI name = infoPersonTooltipContainer.addPara(this.currentHoveredPerson.getName().getFullName() + (this.currentHoveredPerson.isPlayer() ? " (" + "You" + ")": ""), pad);
        name.getPosition().inTL(nameX, nameY);
        name.getPosition().setSize(nameW, nameH);
        name.setHighlight(this.currentHoveredPerson.getName().getFullName());
        name.setHighlightColors(Misc.getBrightPlayerColor());
        //BRM (Bionic Rights Management)
        int brmH = 30;
        int brmW = (int) infoLeftW;
        int brmX = (int) (0 + pad);
        int brmY = (int) (nameY + nameH);
        int currentBRM = (int) this.currentHoveredPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);;
        int limitBRM = (int) this.currentHoveredPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);;
        LabelAPI BRM = infoPersonTooltipContainer.addPara("BRM: " + currentBRM + " / " + limitBRM, pad);
        BRM.setHighlight("BRM: ", "" +currentBRM, "" +limitBRM);
        BRM.setHighlightColors(t,currentBRM > limitBRM ? bad: h,Misc.getBrightPlayerColor());
        BRM.getPosition().inTL(brmX, brmY);
        BRM.getPosition().setSize(brmW, brmH);
        //>Consciousness
        float consciousness = this.currentHoveredPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).computeEffective(0f);
        int consciousnessY = (int) (brmY + brmH);
        int consciousnessX = (int) (0 + pad);
        int consciousnessH = 30;
        int consciousnessW = (int) infoLeftW;
        LabelAPI consciousnessLabel = infoPersonTooltipContainer.addPara("Conscious: " + Math.round(consciousness * 100) + "%", 0);
        consciousnessLabel.setHighlight("" + Math.round(consciousness * 100) + "%");
        consciousnessLabel.setHighlightColor(ba_officermanager.getConsciousnessColorByLevel(consciousness));
        consciousnessLabel.getPosition().setSize(consciousnessW,consciousnessH);
        consciousnessLabel.getPosition().inTL(consciousnessX, consciousnessY);

        int btnH = 40;
        //--------Bionic table
        int tableX = (int) (infoLeftW + pad  + pad);
        int tableY = (int) (0 + pad);
        int tableW = (int) (infoRightW - pad - pad);
        int tableH = (int) (personInfoH - pad - btnH);
        displayBionicTable(infoPersonContainer, infoPersonTooltipKey, "WORKSHOP",true, true, tableW, tableH, tableX, tableY);

        //--------upgrade button
        //todo: add guide info on hover
        int installBtnH = btnH;
        int installBtnW = (int) (200 - pad);
        int installBtnX = (int) (personInfoW - installBtnW - pad);
        int installBtnY = (int) (personInfoH - installBtnH - pad);
        ButtonAPI installButton = infoPersonTooltipContainer.addButton("Install", null, installBtnW, installBtnH, 0);
        installButton.getPosition().inTL(installBtnX,installBtnY);
        addButtonToList(installButton, "bionic:install");
        //--------remove button
        //todo: add guide info on hover
        int removeBtnH = btnH;
        int removeBtnW = (int) (100 - pad);
        int removeBtnX = (int) (installBtnX - pad - removeBtnW);
        int removeBtnY = (int) (installBtnY);
        ButtonAPI removeButton = infoPersonTooltipContainer.addButton("Remove", null, removeBtnW, removeBtnH, 0);
        removeButton.getPosition().inTL(removeBtnX,removeBtnY);
        addButtonToList(removeButton, "bionic:remove");
        //--------selected
        //todo: add guide info on hover
        int selectedH = btnH / 2;
        int selectedW = (int) (infoRightW - removeBtnW - installBtnW - pad);
        int selectedLimbX = (int) (tableX);
        int selectedLimbY = (int) (tableH);
        String limbName = this.currentSelectedLimb != null ? this.currentSelectedLimb.name : "None";
        LabelAPI selectedLimbLabel = infoPersonTooltipContainer.addPara("%s %s", 0, t, "Selected:", limbName);
        selectedLimbLabel.getPosition().inTL(selectedLimbX,selectedLimbY);
        selectedLimbLabel.getPosition().setSize(selectedW, selectedH);
        selectedLimbLabel.setHighlight("Selected:", limbName);
        selectedLimbLabel.setHighlightColors(Misc.getBrightPlayerColor(), this.currentSelectedLimb != null ? t :Misc.getGrayColor());
        int selectedBionicX = (int) (tableX);
        int selectedBionicY = (int) (tableH + selectedH);
        String bionicName = this.currentSelectedBionic != null ? this.currentSelectedBionic.getName(): "None";
        LabelAPI selectedBionicLabel = infoPersonTooltipContainer.addPara("%s %s", 0, t,"Selected:",  bionicName);
        selectedBionicLabel.getPosition().inTL(selectedBionicX,selectedBionicY);
        selectedBionicLabel.getPosition().setSize(selectedW, selectedH);
        selectedBionicLabel.setHighlight("Selected:", bionicName);
        selectedBionicLabel.setHighlightColors(Misc.getBrightPlayerColor(), this.currentSelectedBionic != null ? this.currentSelectedBionic.displayColor: Misc.getGrayColor());
    }
    public void displayInventoryWorkshop(ba_component creatorComponent, String creatorComponentTooltip, float inventoryW, float inventoryH, float inventoryX, float inventoryY) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

        String inventoryTooltipKey = "WORKSHOP_INVENTORY_TOOLTIP";
        String inventoryPanelKey = "WORKSHOP_INVENTORY_PANEL";
        ba_component inventoryContainer = new ba_component(creatorComponent.mainPanel, inventoryW, inventoryH, inventoryX, inventoryY, true, inventoryPanelKey);
        TooltipMakerAPI inventoryTooltipContainer = inventoryContainer.createTooltip(inventoryTooltipKey, inventoryW, inventoryH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryPanelKey,inventoryContainer,0,0);

        inventoryTooltipContainer.addPara("inventory", 0);
    }
    public void displayEffectListWorkshop(ba_component creatorComponent, String creatorComponentTooltip, float effectListW, float effectListH, float effectListX, float effectListY) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

        String infoPersonTooltipKey = "WORKSHOP_EFFECT_LIST_TOOLTIP";
        String infoPersonPanelKey = "WORKSHOP_EFFECT_LIST_PANEL";
        ba_component effectListContainer = new ba_component(creatorComponent.mainPanel, effectListW, effectListH, effectListX, effectListY, true, infoPersonPanelKey);
        TooltipMakerAPI effectListTooltipContainer = effectListContainer.createTooltip(infoPersonTooltipKey, effectListW, effectListH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonPanelKey,effectListContainer,0,0);

        //border
        int borderW = (int) (effectListW - pad - pad);
        int borderH = (int) (effectListH - pad - pad);
        int borderX = (int) pad;
        int borderY = (int) pad / 2;
        UIComponentAPI border = effectListTooltipContainer.createRect(Misc.getDarkPlayerColor(), 1);
        border.getPosition().setSize(borderW, borderH);
        effectListContainer.mainPanel.addComponent(border).setLocation(0,0).inTL(borderX, borderY);
    }
    protected void focusContent(String focusTabId) {
        if(focusTabId == "") {
            //go to default if empty
            ba_component focusTab = tabMap.get(this.currentTabId);
            focusTab.mainPanel.getPosition().inTL(0, 0);
        } else {
            String previousTabId = this.currentTabId;
            this.currentTabId = focusTabId;
            log.info(previousTabId);
            log.info(currentTabId);

            //move the previous tab out
            ba_component previousTab = tabMap.get(previousTabId);
            previousTab.mainPanel.getPosition().inTL(dW,0);
            //move the focus tab in
            ba_component focusTab = tabMap.get(currentTabId);
            focusTab.mainPanel.getPosition().inTL(0, 0);
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
//        ba_component previousTab2 = componentMap.get("WORKSHOP_INVENTORY_PANEL");
//        ba_utils.drawBox(
//                (int) previousTab2.getTooltip("WORKSHOP_INVENTORY_TOOLTIP").getPosition().getX(),
//                (int) previousTab2.getTooltip("WORKSHOP_INVENTORY_TOOLTIP").getPosition().getY(),
//                (int) previousTab2.getTooltip("WORKSHOP_INVENTORY_TOOLTIP").getPosition().getWidth(),
//                (int) previousTab2.getTooltip("WORKSHOP_INVENTORY_TOOLTIP").getPosition().getHeight(),
//                0.3f,
//                Color.pink
//        );
//        ba_component previousTab3 = componentMap.get("OVERVIEW_PERSON_INFO_PANEL");
//        ba_utils.drawBox(
//                (int) previousTab3.getTooltip("OVERVIEW_PERSON_INFO_TOOLTIP").getPosition().getX(),
//                (int) previousTab3.getTooltip("OVERVIEW_PERSON_INFO_TOOLTIP").getPosition().getY(),
//                (int) previousTab3.getTooltip("OVERVIEW_PERSON_INFO_TOOLTIP").getPosition().getWidth(),
//                (int) previousTab3.getTooltip("OVERVIEW_PERSON_INFO_TOOLTIP").getPosition().getHeight(),
//                0.3f,
//                Color.pink
//        );

    }

    @Override
    public void advance(float amount) {
        //handles button input processing
        //if pressing a button changes something in the diplay, call reset()
//        boolean needsReset = false;
        for (ButtonAPI b : buttons)
        {
//            log.info("" + b + "--" + b.isHighlighted() + "-" + b.isChecked() + "-" + b.isEnabled());
            if (b.isChecked()) {
                b.setChecked(false);
                //Check if click change main page
                String s = buttonMap.get(b);
                String[] tokens = s.split(":");
                if (tokens[0].equals("tab")) {
                    log.info("clicked" + tokens[1]);
                    if(tokens[1].equals(OVERVIEW)) {
                        focusContent(OVERVIEW);
//                        needsReset = true;
                        break;
                    }
                    if(tokens[1].equals(WORKSHOP)) {
                        focusContent(WORKSHOP);
//                        needsReset = true;
                        break;
                    }
                }
            }
        }

        //pressing a button usually means something we are displaying has changed, so redraw the panel from scratch
//        if (needsReset) reset();
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
                            if(!this.currentHoveredPerson.getId().equals(tokens[1])) {
                                for(PersonAPI person: ba_officermanager.listPersons) {
                                    if(tokens[1].equals(person.getId())) {
                                        this.currentHoveredPerson = person;
                                        shouldRefresh = true;
                                    }
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

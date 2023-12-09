package pigeonpun.bionicalteration.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import pigeonpun.bionicalteration.utils.ba_utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//the container panel
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
    List<PersonAPI> listPersons = new ArrayList<>();
    public static final String OVERVIEW = "OVERVIEW", DETAILS = "DETAILS";
    public PersonAPI currentHoveredPerson;
    public enum KEYS {
        IMG,
        TITLE,
        PARA
    }
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
        //change the current tab id and "focus" on it
        focusContent(moveToTabId);
    }
    public void initialUICreation()
    {

        //clears the ui panel
//        if (mainTooltip != null)
//        {
//            containerPanel.removeComponent(mainTooltip);
//            buttons.clear();
//            buttonMap.clear();
////            listComponents.clear();
//            for (ba_component component: listComponents) {
//                component.tooltipMap.values().clear();
//                component.tooltipListMap.values().clear();
//            }
//            listComponents.clear();
//            log.info("reseting");
//        }

        mainTooltip = this.containerPanel.createUIElement(this.containerPanel.getPosition().getWidth(), this.containerPanel.getPosition().getHeight(), false);
        mainTooltip.setForceProcessInput(true);
        containerPanel.addUIElement(mainTooltip).inTL(0,0);

//        getNewListPerson();
        refresh();
    }
    protected void refresh() {
        log.info("refreshing");
        ba_component overviewComponent = tabMap.get(OVERVIEW);
        ba_component detailComponent = tabMap.get(DETAILS);
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
        displayDetails();
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
        if(this.listPersons.size() != 0 && this.currentHoveredPerson == null) {
            this.currentHoveredPerson = this.listPersons.get(0);
        }

        //big overview container
        String mainOverviewPanelKey = "MAIN_OVERVIEW_CONTAINER";
        String mainInfoTooltipKey = "MAIN_INFO_TOOLTIP";
        String mainPersonListTooltipKey = "MAIN_LIST_TOOLTIP";
        ba_component overviewContainer = new ba_component(containerPanel, pW, pH, MAIN_CONTAINER_PADDING/2, MAIN_CONTAINER_PADDING/2, true, mainOverviewPanelKey);
//        TooltipMakerAPI overviewTooltipContainer = overviewContainer.createTooltip(mainTooltipKey, pW, pH, false, 0, 0);
        tabMap.put(OVERVIEW, overviewContainer);
        overviewContainer.unfocusComponent();

        float listPersonW = 0.6f * pW;
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
        float personW = personListW - 10;
        float personH = imageH + 20;
        List<ba_component> subComponentPersonList = new ArrayList<>();
        for (PersonAPI member: this.listPersons) {
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
            int nameH = 20;
            int nameW = 150;
            int nameX = (int) (imageX + imageW + pad);
            TooltipMakerAPI personNameTooltip = personDisplayContainer.createTooltip("PERSON_NAME", nameW, nameH, false, 0, 0);
            personNameTooltip.getPosition().inTL(nameX, 0);
            LabelAPI name = personNameTooltip.addPara(member.getName().getFullName(), pad);
            name.setHighlight(member.getName().getFullName());
            name.setHighlightColors(t);
            //Level
            int levelH = 20;
            int levelW = 100;
            int levelX = (int) (nameX + nameW);
            TooltipMakerAPI personLevelTooltip = personDisplayContainer.createTooltip("PERSON_LEVEL", levelW, levelH, false, 0, 0);
            personLevelTooltip.getPosition().inTL(levelX, 0);
            LabelAPI level = personLevelTooltip.addPara("Level: " + member.getStats().getLevel(), pad);
            level.setHighlight("Level: ","" + member.getStats().getLevel());
            level.setHighlightColors(g,h);
            //Personality
            //BRM (Bionic Rights Management)
            int brmH = 20;
            int brmW = 100;
            int brmX = (int) (nameX);
            int brmY = (int) (currentStartY + nameH);
            TooltipMakerAPI personBRMTooltip = personDisplayContainer.createTooltip("PERSON_BRM", brmW, brmH, false, 0, 0);
            personBRMTooltip.getPosition().inTL(brmX, brmY);
            LabelAPI BRM = personBRMTooltip.addPara("BRM: " + 0, pad);
            BRM.setHighlight("BRM: ","" + 0);
            BRM.setHighlightColors(h,h);
            //Profession: Captain/Administrator
            int profH = 20;
            int profW = 100;
            int profX = (int) (levelX);
            int profY = (int) (currentStartY + levelH);
            TooltipMakerAPI personProfTooltip = personDisplayContainer.createTooltip("PERSON_PROF", profW, profH, false, 9, 0);
            personProfTooltip.getPosition().inTL(profX, profY);
            LabelAPI prof = personProfTooltip.addPara("Profession: " + "---", pad);
            prof.setHighlight("Profession: ","---");
            prof.setHighlightColors(g,h);
            //Monthly Salary
            //Assign to ship/planet
            //add new skill
//            member.getPerson().getFleetCommanderStats().sets
            //--------Spacer because scroller dont like position offseting as spacing
            overviewPersonTooltipContainer.addSpacer(ySpacer);
            i++;
        }
        overviewPersonContainer.subComponentListMap.put("SUB_PERSON_LIST", subComponentPersonList);
        //do the adding late so the scroll work (thanks Lukas04)
        overviewPersonContainer.mainPanel.addUIElement(overviewPersonTooltipContainer);
    }
    protected void displayPersonInfoList(ba_component creatorComponent, String creatorComponentTooltip, float personInfoW, float personInfoH, float personInfoX, float personInfoY) {
        float pad = 5f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();

        String infoPersonTooltipKey = "OVERVIEW_PERSON_INFO_TOOLTIP";
        String infoPersonPanelKey = "OVERVIEW_PERSON_INFO_PANEL";
        ba_component infoPersonContainer = new ba_component(creatorComponent.mainPanel, personInfoW, personInfoH, personInfoX, personInfoY, true, infoPersonPanelKey);
        TooltipMakerAPI infoPersonTooltipContainer = infoPersonContainer.createTooltip(infoPersonTooltipKey, personInfoW, personInfoH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonPanelKey,infoPersonContainer);
        //move to correct place
        infoPersonContainer.mainPanel.getPosition().inTL(0,0);
        //--------header
        float headerW = infoPersonTooltipContainer.getPosition().getWidth();
        float headerH = 30f;
        TooltipMakerAPI headerTooltip = infoPersonContainer.createTooltip("PERSON_INFO_HEADER", headerW, headerH, false, 0,0);
        headerTooltip.getPosition().inTL(0, 0);
        headerTooltip.addSectionHeading("DETAILS", Alignment.MID, 0);
        if(currentHoveredPerson != null) {
            //--------image
            int imageX = (int) personInfoX;
            int imageY = (int) (personInfoY + headerH);
            int imageW = 200;
            int imageH = imageW;
            String spriteName = currentHoveredPerson.getPortraitSprite();
            TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("PERSON_INFO_IMAGE", imageW, imageH, false, 0, 0);
            personImageTooltip.getPosition().inTL(imageX, imageY);
            personImageTooltip.addImage(spriteName, imageW, imageH, 0);
            //--------Skill panels
            int skillW = imageW;
            int skillH = (int) currentHoveredPerson.getStats().getSkillsCopy().size() * 100;
            TooltipMakerAPI personSkillTooltip = infoPersonContainer.createTooltip("PERSON_INFO_SKILLS", skillW, skillH, false, 0, 0);
            UIComponentAPI personSkills = personSkillTooltip.addSkillPanelOneColumn(currentHoveredPerson, 0);
            personSkillTooltip.getPosition().setSize(skillW, skillH);
            infoPersonContainer.mainPanel.addComponent(personSkillTooltip).setLocation(0,0).inTL(personInfoX, personInfoY);

        }


        //do the adding late so the scroll work
//        infoPersonContainer.mainPanel.addUIElement(infoPersonTooltipContainer);
    }
    protected void displayDetails() {
//        ba_component detailContainer = new ba_component(containerPanel, 100f, pH, false, 0, 0);
//        tabMap.put(DETAILS, detailContainer);
//        detailContainer.unfocusComponent();
//        detailContainer.tooltip.addPara("test details", 0);
//        ButtonAPI button = detailContainer.tooltip.addButton("switch overview", null,200f, 100f,0f);
//        buttons.add(button);
//        buttonMap.put(button, "tab:"+ OVERVIEW);
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
        this.listPersons.clear();
        List<PersonAPI> listP = new ArrayList<>();
        listP.add(Global.getSector().getPlayerPerson());
        List<OfficerDataAPI> listPlayerMember = Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy();
        for (OfficerDataAPI officer: listPlayerMember) {
            listP.add(officer.getPerson());
        }
        this.listPersons.addAll(listP);
    }
    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {
//        ba_component previousTab2 = tabMap.get(OVERVIEW);
//        ba_utils.drawBox(
//                (int) previousTab2.getTooltip("MAIN_LIST_TOOLTIP").getPosition().getX(),
//                (int) previousTab2.getTooltip("MAIN_LIST_TOOLTIP").getPosition().getY(),
//                (int) previousTab2.getTooltip("MAIN_LIST_TOOLTIP").getPosition().getWidth(),
//                (int) previousTab2.getTooltip("MAIN_LIST_TOOLTIP").getPosition().getHeight(),
//                0.3f,
//                Color.pink
//        );
//        ba_utils.drawBox(
//                (int) previousTab2.getTooltip("MAIN_INFO_TOOLTIP").getPosition().getX(),
//                (int) previousTab2.getTooltip("MAIN_INFO_TOOLTIP").getPosition().getY(),
//                (int) previousTab2.getTooltip("MAIN_INFO_TOOLTIP").getPosition().getWidth(),
//                (int) previousTab2.getTooltip("MAIN_INFO_TOOLTIP").getPosition().getHeight(),
//                0.3f,
//                Color.green
//        );
//        ba_component a = previousTab2.subComponentMap.get("OVERVIEW_PERSON_INFO_PANEL");
//        ba_utils.drawBox(
//                (int) a.getTooltip("OVERVIEW_PERSON_INFO_TOOLTIP").getPosition().getX(),
//                (int) a.getTooltip("OVERVIEW_PERSON_INFO_TOOLTIP").getPosition().getY(),
//                (int) a.getTooltip("OVERVIEW_PERSON_INFO_TOOLTIP").getPosition().getWidth(),
//                (int) a.getTooltip("OVERVIEW_PERSON_INFO_TOOLTIP").getPosition().getHeight(),
//                0.3f,
//                Color.green
//        );

//        List<ba_component> personList = previousTab2.subComponentListMap.get("SUB_PERSON_LIST");
//        for (ba_component personPanel: personList) {
//            ba_utils.drawBox(
//                    (int) personPanel.getTooltip("PERSON_TOOLTIP_CONTAINER").getPosition().getX(),
//                    (int) personPanel.getTooltip("PERSON_TOOLTIP_CONTAINER").getPosition().getY(),
//                    (int) personPanel.getTooltip("PERSON_TOOLTIP_CONTAINER").getPosition().getWidth(),
//                    (int) personPanel.getTooltip("PERSON_TOOLTIP_CONTAINER").getPosition().getHeight(),
//                    0.3f,
//                    Color.green
//            );
//            ba_utils.drawBox(
//                    (int) personPanel.getTooltip("PERSON_NAME").getPosition().getX(),
//                    (int) personPanel.getTooltip("PERSON_NAME").getPosition().getY(),
//                    (int) personPanel.getTooltip("PERSON_NAME").getPosition().getWidth(),
//                    (int) personPanel.getTooltip("PERSON_NAME").getPosition().getHeight(),
//                    0.3f,
//                    Color.blue
//            );
//            ba_utils.drawBox(
//                    (int) personPanel.getTooltip("PERSON_LEVEL").getPosition().getX(),
//                    (int) personPanel.getTooltip("PERSON_LEVEL").getPosition().getY(),
//                    (int) personPanel.getTooltip("PERSON_LEVEL").getPosition().getWidth(),
//                    (int) personPanel.getTooltip("PERSON_LEVEL").getPosition().getHeight(),
//                    0.3f,
//                    Color.blue
//            );
//        }
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
                    if(tokens[1].equals(DETAILS)) {
                        focusContent(DETAILS);
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
                                for(PersonAPI person: this.listPersons) {
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
        public void unfocusComponent() {
            mainPanel.getPosition().inTL(dW, 0);
        }
    }
}

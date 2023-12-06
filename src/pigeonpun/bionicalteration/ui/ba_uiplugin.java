package pigeonpun.bionicalteration.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.combat.P;
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
    List<ba_component> listComponents = new ArrayList<>();
    List<SpriteAPI> listImgs = new ArrayList<>();
    public static final String OVERVIEW = "OVERVIEW", DETAILS = "DETAILS";
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
        this.callbacks = callbacks;
        this.containerPanel = panel;
        this.dialog = dialog;

        dW = Display.getWidth();
        dH = Display.getHeight();
        pW = (int) this.containerPanel.getPosition().getWidth();
        pH = (int) this.containerPanel.getPosition().getHeight();

        initialUICreation();
        focusContent("");
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
        if (mainTooltip != null)
        {
            containerPanel.removeComponent(mainTooltip);
            buttons.clear();
            buttonMap.clear();
//            listComponents.clear();
            for (ba_component component: listComponents) {
                component.tooltipMap.values().clear();
                component.tooltipListMap.values().clear();
            }
            listComponents.clear();
            log.info("reseting");
        }

        //create a new TooltipMakerAPI covering the entire UI panel
        //I don't think scrolling panels work here, but I might be doing them wrong
        mainTooltip = this.containerPanel.createUIElement(this.containerPanel.getPosition().getWidth(), this.containerPanel.getPosition().getHeight(), false);
        mainTooltip.setForceProcessInput(true);
        containerPanel.addUIElement(mainTooltip).inTL(0,0);
        //create smaller container for focus/unforcus
        displayOverview();
        displayDetails();
    }
    protected void displayOverview() {
        String mainTooltipKey = "MAIN_TOOLTIP";
        ba_component overviewContainer = new ba_component(containerPanel, pW, pH, MAIN_CONTAINER_PADDING/2, MAIN_CONTAINER_PADDING/2);
        //important to set the container tooltip to have scroll enable if you want scroll
        //Next important is to have panel.addUI at the bottom of the code if you have scroll enabled, or the scroll wont work
        TooltipMakerAPI overviewTooltipContainer = overviewContainer.createTooltip(mainTooltipKey, pW, pH, true, 0, 0, false);
        tabMap.put(OVERVIEW, overviewContainer);
        overviewContainer.unfocusComponent();
//        overviewContainer.getTooltip(mainTooltipKey).addPara("test overview", 0);
//        ButtonAPI button = overviewContainer.getTooltip(mainTooltipKey).addButton("switch details", null,200f, 100f,0f);
//        buttons.add(button);
//        buttonMap.put(button, "tab:"+ DETAILS);
        int i = 0;
        int xStart = 0;
        int yStart = 10;
        int imageH = 100;
        int imageW = 100;
        int ySpacer = 10;
        List<ba_component> subComponentPersonList = new ArrayList<>();
        for (PersonAPI member: getListPerson()) {
            float personW = pW - 10;
            float personH = imageH;
            float currentStartX = xStart;
            float currentStartY = yStart+(ySpacer*i);
            String spriteName = member.getPortraitSprite();
            listImgs.add(Global.getSettings().getSprite(spriteName));
            String defaultPersonTooltipContainerKey = "PERSON_TOOLTIP_CONTAINER";
            //--------person container
            ba_component personDisplayContainer = new ba_component(overviewContainer.mainPanel, personW, personH,0,0,false);
            personDisplayContainer.mainPanel.getPosition().inTL(0,0);
            subComponentPersonList.add(personDisplayContainer);
                //attach to have the main tooltip scroll effect this component's panel
            overviewContainer.attachExistingPanel(mainTooltipKey, personDisplayContainer);
                //Default person container tooltip
            TooltipMakerAPI personDisplayContainerTooltip = personDisplayContainer.createTooltip(defaultPersonTooltipContainerKey, personW, personH, false, 0,0);
            personDisplayContainerTooltip.getPosition().inTL(currentStartX,currentStartY);
                //border
            UIComponentAPI border = personDisplayContainerTooltip.createRect(Color.red, 1);
            border.getPosition().setSize(personW, personH);
            personDisplayContainer.mainPanel.addComponent(border).setLocation(0,0).inTL(currentStartX, currentStartY);

            //---------Name
            TooltipMakerAPI personNameTooltip = personDisplayContainer.createTooltip("PERSON_NAME", pW - imageW - 100, 20f, false, imageW, 0);
            personNameTooltip.getPosition().inTL(currentStartX + imageW, currentStartY);
            personNameTooltip.addPara(member.getName().getFullName(), 5);
            //--------image
            TooltipMakerAPI personImageTooltip = personDisplayContainer.createTooltip("PERSON_IMAGE", imageW, imageH, false, 0, 0);
            personImageTooltip.getPosition().inTL(currentStartX, currentStartY);
            personImageTooltip.addImage(spriteName, imageW, imageH, 0);
            //--------Skill panels
//
            //add new skill
//            member.getPerson().getFleetCommanderStats().sets
            i++;
        }
        overviewContainer.subComponentListMap.put("SUB_PERSON_LIST", subComponentPersonList);

        //do the adding late so the scroll work (thanks Lukas04)
        overviewContainer.mainPanel.addUIElement(overviewTooltipContainer);
//        overviewContainer.subComponentListMap.put("SUB_IMG_LIST", subComponentImgList);
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
    public List<PersonAPI> getListPerson() {
        List<PersonAPI> listPerson = new ArrayList<>();
        List<OfficerDataAPI> listPlayerMember = Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy();
        for (OfficerDataAPI officer: listPlayerMember) {
            listPerson.add(officer.getPerson());
        }
        listPerson.add(Global.getSector().getPlayerPerson());
        return listPerson;
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
//                (int) previousTab2.getTooltip("MAIN_TOOLTIP").getPosition().getX(),
//                (int) previousTab2.getTooltip("MAIN_TOOLTIP").getPosition().getY(),
//                (int) previousTab2.getTooltip("MAIN_TOOLTIP").getPosition().getWidth(),
//                (int) previousTab2.getTooltip("MAIN_TOOLTIP").getPosition().getHeight(),
//                0.3f,
//                Color.pink
//        );
//
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
//        }
    }

    @Override
    public void advance(float amount) {
        //handles button input processing
        //if pressing a button changes something in the diplay, call reset()
//        boolean needsReset = false;
        for (ButtonAPI b : buttons)
        {
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
        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;
            //is ESC is pressed, close the custom UI panel and the blank IDP we used to create it
            if (event.isKeyDownEvent() && event.getEventValue() == Keyboard.KEY_ESCAPE) {
                event.consume();
                callbacks.dismissDialog();
                dialog.dismiss();
                return;
            }
        }
    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
    public class ba_component {
//        protected TooltipMakerAPI tooltip;
        protected CustomPanelAPI mainPanel;
        protected HashMap<String, TooltipMakerAPI> tooltipMap = new HashMap<>();
        protected HashMap<String, List<TooltipMakerAPI>> tooltipListMap = new HashMap<>();
        protected HashMap<String, List<ba_component>> subComponentListMap = new HashMap<>();

        /**
         * Do note that this doesn't create a panel attach to the creator panel's tooltip. Therefor, scrolling on creator's tooltip will not work on the newly created component. <br>
         * If you want to create one that get effected by the scroll, use {@code createAndAttachSubPanel}
         * @param creatorPanel creator panel
         * @param width Width of this component
         * @param height Height of this component
         */
        public ba_component(CustomPanelAPI creatorPanel, float width, float height, float x, float y) {
            //create both panel and tooltip
            mainPanel = creatorPanel.createCustomPanel(width, height, null);
            mainPanel.getPosition().setLocation(x,y).inTL(0, 0);
            //extremely important or nothing will render
            creatorPanel.addComponent(mainPanel);
            //add into list to remove on reset
            listComponents.add(this);
        }
        public ba_component(CustomPanelAPI creatorPanel, float width, float height,float x, float y, boolean addToCreatorPanel) {
            //create both panel and tooltip
            mainPanel = creatorPanel.createCustomPanel(width, height, null);
            mainPanel.getPosition().setLocation(0,0).setLocation(x, y);
            //add into the big panel
            if(addToCreatorPanel) {
                creatorPanel.addComponent(mainPanel);
            }
            //add into list to remove on reset
            listComponents.add(this);
        }
        protected TooltipMakerAPI createTooltip(String key, float width, float height, boolean hasScroller, float tooltipLocationX, float tooltipLocationY) {
            TooltipMakerAPI tooltip = createTooltip(key, width, height, hasScroller, tooltipLocationX, tooltipLocationY, true);
            return tooltip;
        }
        protected TooltipMakerAPI createTooltip(String key, float width, float height, boolean hasScroller, float tooltipLocationX, float tooltipLocationY, boolean isImmediatelyAddToPanel) {
            TooltipMakerAPI tooltip = this.mainPanel.createUIElement(width, height, hasScroller);
            tooltip.setForceProcessInput(true);
            if(isImmediatelyAddToPanel) this.mainPanel.addUIElement(tooltip).setLocation(tooltipLocationX, tooltipLocationY);
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
        protected CustomPanelAPI createAndAttachSubPanel(String tooltipKeyAttachTo, float width, float height, float panelX, float panelY) {
            CustomPanelAPI panel = this.mainPanel.createCustomPanel(width, height, null);
            TooltipMakerAPI tooltipAttachingTo = this.tooltipMap.get(tooltipKeyAttachTo);
            tooltipAttachingTo.addCustom(panel, 0f).getPosition().inTL(panelX, panelY);
            return panel;
        }
        protected void attachExistingPanel(String tooltipKeyAttachTo, CustomPanelAPI panel) {
            TooltipMakerAPI tooltipAttachingTo = this.tooltipMap.get(tooltipKeyAttachTo);
            tooltipAttachingTo.addCustom(panel, 0f);
        }

        /**
         * Use for attaching an existing component's panel into this one <br>
         * If
         * @param tooltipKeyAttachTo Tooltip key
         * @param otherComponent the attaching component
         */
        protected void attachExistingPanel(String tooltipKeyAttachTo, ba_component otherComponent) {
            TooltipMakerAPI tooltipAttachingTo = this.tooltipMap.get(tooltipKeyAttachTo);
            tooltipAttachingTo.addCustom(otherComponent.mainPanel, 0f);
        }
        public void unfocusComponent() {
            mainPanel.getPosition().inTL(dW, 0);
        }
    }
}

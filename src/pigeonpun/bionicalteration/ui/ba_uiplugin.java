package pigeonpun.bionicalteration.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
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
    protected CustomPanelAPI containerPanel;
    protected TooltipMakerAPI mainTooltip;
    int dW, dH, pW, pH;
    public static final float MAIN_CONTAINER_PADDING = 150f;
    public static final float MAIN_CONTAINER_WIDTH = Global.getSettings().getScreenWidth() - MAIN_CONTAINER_PADDING;
    public static final float MAIN_CONTAINER_HEIGHT = Global.getSettings().getScreenHeight() - MAIN_CONTAINER_PADDING;
    List<ButtonAPI> buttons = new ArrayList<>();
    HashMap<ButtonAPI, String> buttonMap = new HashMap<>();
    List<ba_component> listComponents = new ArrayList<>();
    public static final String OVERVIEW = "OVERVIEW", DETAILS = "DETAILS";
    HashMap<String, ba_component> tabMap = new HashMap<>();
    String currentTabId = OVERVIEW;
    public static final float sidePanelWidth = 0.3f;
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
                if(component.tooltip != null) {
                    component.panel.removeComponent(component.tooltip);
                }
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
        ba_component overviewContainer = new ba_component(containerPanel, 100f, pH, false, 0, 0);
        tabMap.put(OVERVIEW, overviewContainer);
        overviewContainer.unfocusComponent();
        overviewContainer.tooltip.addPara("test overview", 0);
        ButtonAPI button = overviewContainer.tooltip.addButton("switch details", null,200f, 100f,0f);
        buttons.add(button);
        buttonMap.put(button, "tab:"+ DETAILS);

        List<OfficerDataAPI> listPlayerMember = Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy();
        for (OfficerDataAPI member: listPlayerMember) {
            log.info(member.getPerson().isDefault());
        }
    }
    protected void displayDetails() {
        ba_component detailContainer = new ba_component(containerPanel, 100f, pH, false, 0, 0);
        tabMap.put(DETAILS, detailContainer);
        detailContainer.unfocusComponent();
        detailContainer.tooltip.addPara("test details", 0);
        ButtonAPI button = detailContainer.tooltip.addButton("switch overview", null,200f, 100f,0f);
        buttons.add(button);
        buttonMap.put(button, "tab:"+ OVERVIEW);
    }
    protected void focusContent(String focusTabId) {
        if(focusTabId == "") {
            //go to default if empty
            ba_component focusTab = tabMap.get(this.currentTabId);
            focusTab.panel.getPosition().inTL(0, 0);
        } else {
            String previousTabId = this.currentTabId;
            this.currentTabId = focusTabId;
            log.info(previousTabId);
            log.info(currentTabId);

            //move the previous tab out
            ba_component previousTab = tabMap.get(previousTabId);
            previousTab.panel.getPosition().inTL(dW,0);
            //move the focus tab in
            ba_component focusTab = tabMap.get(currentTabId);
            focusTab.panel.getPosition().inTL(0, 0);
        }
    }
    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {

    }

    @Override
    public void render(float alphaMult) {
        //GL bottom left is 0,0
        ba_component previousTab = tabMap.get(DETAILS);
        int sidePanelLocationX = (int) (previousTab.panel.getPosition().getX());
        int sidePanelLocationY = (int) (previousTab.panel.getPosition().getY());
        ba_utils.drawBox(
                sidePanelLocationX,sidePanelLocationY,
                pW,
                pH,
                0.3f,
                Color.red
        );

        ba_component previousTab2 = tabMap.get(OVERVIEW);
        int sidePanelLocationX2 = (int) (previousTab2.panel.getPosition().getX());
        int sidePanelLocationY2 = (int) (previousTab2.panel.getPosition().getY());
        ba_utils.drawBox(
                sidePanelLocationX2,sidePanelLocationY2,
                pW,
                pH,
                0.3f,
                Color.green
        );
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
        protected TooltipMakerAPI tooltip;
        protected CustomPanelAPI panel;
        public ba_component(CustomPanelAPI creatorPanel, float width, float height, boolean hasScroller, float tooltipLocationX, float tooltipLocationY) {
            //create both panel and tooltip
            panel = creatorPanel.createCustomPanel(width, height, null);
            tooltip = panel.createUIElement(width, height, hasScroller);
            //move the tooltip
            panel.addUIElement(tooltip).inTL(tooltipLocationX, tooltipLocationY);
            //add into the big panel
            creatorPanel.addComponent(panel);
            //add into list to remove on reset
            listComponents.add(this);
        }
        public void unfocusComponent() {
            panel.getPosition().inTL(dW, 0);
        }
    }
}

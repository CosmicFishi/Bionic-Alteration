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
    public void addButtonToList(ButtonAPI button, String buttonMapValue) {
        buttons.add(button);
        buttonMap.put(button, buttonMapValue);
    }
    public void saveScrollPosition() {

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
        //todo: these
    }
    public void displayOverclockList(ba_component creatorComponent, String creatorComponentTooltip, float listW, float listH) {

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
        if(previousTab2.getTooltip("INVENTORY_TOOLTIP") != null) {
            ba_utils.drawBox(
                    (int) previousTab2.getTooltip("INVENTORY_TOOLTIP").getPosition().getX(),
                    (int) previousTab2.getTooltip("INVENTORY_TOOLTIP").getPosition().getY(),
                    (int) previousTab2.getTooltip("INVENTORY_TOOLTIP").getPosition().getWidth(),
                    (int) previousTab2.getTooltip("INVENTORY_TOOLTIP").getPosition().getHeight(),
                    0.3f,
                    Color.pink
            );
        }
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
//                        log.info("hover " + s);
//                        if(currentTabId.equals(OVERVIEW)) {
//                            ba_component component = componentMap.get("OVERVIEW_PERSON_LIST_PANEL");
//                            if(component != null && component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP") != null) {
//                                if(tokens[0].equals("hover") && debounceplugin.isDebounceOver("OVERVIEW_PERSON_LIST_TOOLTIP", 0, component.tooltipMap.get("OVERVIEW_PERSON_LIST_TOOLTIP").getExternalScroller().getYOffset())) {
//                                    if(!this.currentPerson.getId().equals(tokens[1])) {
//                                        for(PersonAPI person: ba_officermanager.listPersons) {
//                                            if(tokens[1].equals(person.getId())) {
//                                                this.currentPerson = person;
//                                                shouldRefresh = true;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
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
}

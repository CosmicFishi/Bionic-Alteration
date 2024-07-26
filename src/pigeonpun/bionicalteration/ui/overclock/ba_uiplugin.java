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
public class ba_uiplugin extends ba_uicommon {
    Logger log = Global.getLogger(ba_debounceplugin.class);
    protected CustomPanelAPI containerPanel; //Created panel from ba_deligate.java
    protected TooltipMakerAPI mainTooltip;
    public static final float MAIN_CONTAINER_PADDING = 150f;
    public static final String OVERCLOCK = "OVERCLOCK_WORKSHOP";
    public static final float MAIN_CONTAINER_WIDTH = Global.getSettings().getScreenWidth() - MAIN_CONTAINER_PADDING;
    public static final float MAIN_CONTAINER_HEIGHT = Global.getSettings().getScreenHeight() - MAIN_CONTAINER_PADDING;
    int dW, dH, pW, pH;
    protected HashMap<String, ba_component> tabMap = new HashMap<>();
    private PersonAPI currentPerson;
    public ba_bionicitemplugin currentHoveredBionic; //hovering in the inventory
    public ba_limbmanager.ba_limb currentSelectedLimb; //selected for installation/removal
    public ba_bionicitemplugin currentSelectedBionic; //selected for installation

    /**
     * @param panel panel
     * @param callbacks callbacks
     * @param dialog dialog
     */
    @Override
    protected void init(CustomPanelAPI panel, CustomVisualDialogDelegate.DialogCallbacks callbacks, InteractionDialogAPI dialog) {
        super.init(panel, callbacks, dialog);
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
    @Override
    protected void refresh() {
        super.refresh();
//        log.info("refreshing");
        ba_component overviewComponent = tabMap.get(OVERCLOCK);
        if (overviewComponent != null) {
            containerPanel.removeComponent(overviewComponent.mainPanel);
        }
        //create smaller container for focus/unforcus
        displayOverclock();
    }
    @Override
    public void saveScrollPosition() {
        super.saveScrollPosition();
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
        displayInventoryWorkshop(creatorComponent, creatorComponentTooltip, invW, invH, 0, 0);
    }
    public void displayOverclockList(ba_component creatorComponent, String creatorComponentTooltip, float listW, float listH) {

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
        super.advance(amount);
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
        super.processInput(events);
        boolean shouldRefresh = false;
        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;
            if(event.isMouseMoveEvent()) {
//                for (ButtonAPI button: buttons) {
//                    float buttonX = button.getPosition().getX();
//                    float buttonY = button.getPosition().getY();
//                    float buttonW = button.getPosition().getWidth();
//                    float buttonH = button.getPosition().getHeight();
//                    if(event.getX() >= buttonX && event.getX() < buttonX + buttonW && event.getY() >= buttonY && event.getY() < buttonY+buttonH) {
//                        String s = buttonMap.get(button);
//                        String[] tokens = s.split(":");
//                        ba_component component = componentMap.get("WORKSHOP_INVENTORY_PANEL");
//                        //hover bionic item in inventory
//                        if(component != null && component.tooltipMap.get("WORKSHOP_INVENTORY_TOOLTIP") != null) {
//                            if(tokens[0].equals("hover_bionic_item") && debounceplugin.isDebounceOver("WORKSHOP_INVENTORY_TOOLTIP", 0, component.tooltipMap.get("WORKSHOP_INVENTORY_TOOLTIP").getExternalScroller().getYOffset())) {
//                                if(ba_bionicmanager.bionicItemMap.get(tokens[1]) != null && (this.currentHoveredBionic == null || !this.currentHoveredBionic.bionicId.equals(tokens[1]))) {
//                                    this.currentHoveredBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
//                                    shouldRefresh = true;
//                                }
//                            }
//                        }
//                    }
//                }
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

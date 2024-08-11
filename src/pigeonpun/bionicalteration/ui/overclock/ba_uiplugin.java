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
import pigeonpun.bionicalteration.overclock.ba_overclock;
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
    public static final String PERSON_LIST = "person_list", INVENTORY = "inventory";
    public String currentSelectOverclockLocation = PERSON_LIST;
    public ba_bionicitemplugin currentSelectOverclockBionic = null;
    public String currentSelectedOverclock = null;
    int dW, dH, pW, pH;
    protected HashMap<String, ba_component> tabMap = new HashMap<>();

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
        ba_officermanager.refresh(null);

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

        if(ba_officermanager.listPersons.size() != 0 && this.currentPerson == null) {
            currentPerson = ba_officermanager.listPersons.get(0);
        }

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
        displayOverclockList(overclockContainer, overclockListTooltipKey, listOverclockW, pH, 0, 0);
    }
    public void displayInventory(ba_component creatorComponent, String creatorComponentTooltip, float invW, float invH) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
//        displayInventoryWorkshop(creatorComponent, creatorComponentTooltip, invW, invH, 0, 0);

        //top
        float inventoryTopH = (30 * invH)/100;
        float inventoryMidH = (10 * invH)/100;
        float inventoryBtmH = invH - (inventoryTopH + inventoryMidH);
        float topW = invW;
        float topH = inventoryTopH;
        float topX = 0;
        float topY = 0;
        String inventoryTopTooltipKey = "INVENTORY_TOP_TOOLTIP";
        String inventoryTopPanelKey = "INVENTORY_TOP_PANEL";
        ba_component inventoryTopContainer = new ba_component(componentMap, creatorComponent.mainPanel, topW, topH, topX, topY, true, inventoryTopPanelKey);
        TooltipMakerAPI inventoryTopTooltipContainer = inventoryTopContainer.createTooltip(inventoryTopTooltipKey, topW, topH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryTopPanelKey, inventoryTopContainer, topX, topY);

//        UIComponentAPI borderTop = inventoryTopTooltipContainer.createRect(Color.red, 1);
//        borderTop.getPosition().setSize(topW, topH);
//        inventoryTopContainer.mainPanel.addComponent(borderTop).setLocation(0,0).inTL(0, 0);
        //display top
        displayInventoryTop(inventoryTopContainer, inventoryTopTooltipKey, topW, topH, 0, 0);

        //Mid
        float midW = invW;
        float midH = inventoryMidH;
        float midX = 0;
        float midY = topH;
        String inventoryMidTooltipKey = "INVENTORY_MID_TOOLTIP";
        String inventoryMidPanelKey = "INVENTORY_MID_PANEL";
        ba_component inventoryMidContainer = new ba_component(componentMap, creatorComponent.mainPanel, midW, midH, midX, midY, true, inventoryMidPanelKey);
        TooltipMakerAPI inventoryMidTooltipContainer = inventoryMidContainer.createTooltip(inventoryMidTooltipKey, midW, midH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryMidPanelKey, inventoryMidContainer, midX, midY);

//        UIComponentAPI borderMid = inventoryMidTooltipContainer.createRect(Color.yellow, 1);
//        borderMid.getPosition().setSize(midW, midH);
//        inventoryMidContainer.mainPanel.addComponent(borderMid).setLocation(0,0).inTL(0, 0);
        //display mid
        displayInventoryMid(inventoryMidContainer, inventoryMidTooltipContainer, midW, midH, 0, 0);

        //Btm
        float btmW = invW;
        float btmH = inventoryBtmH;
        float btmX = 0;
        float btmY = topH + midH;
        String inventoryBtmTooltipKey = "INVENTORY_BTM_TOOLTIP";
        String inventoryBtmPanelKey = "INVENTORY_BTM_PANEL";
        ba_component inventoryBtmContainer = new ba_component(componentMap, creatorComponent.mainPanel, btmW, btmH, btmX, btmY, true, inventoryBtmPanelKey);
        TooltipMakerAPI inventoryBtmTooltipContainer = inventoryBtmContainer.createTooltip(inventoryBtmTooltipKey, btmW, btmH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryBtmPanelKey, inventoryBtmContainer, btmX, btmY);

//        UIComponentAPI borderBtm = inventoryBtmTooltipContainer.createRect(Color.GREEN, 1);
//        borderBtm.getPosition().setSize(btmW, btmH);
//        inventoryBtmContainer.mainPanel.addComponent(borderBtm).setLocation(0,0).inTL(0, 0);
        //display btm
        displayInventoryBtm(inventoryBtmContainer, inventoryBtmTooltipKey, btmW, btmH, 0, 0);
    }
    public void displayInventoryTop(ba_component creatorComponent, String creatorComponentTooltip, float cW, float cH, float cX, float cY) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        //left
        float leftW = (cW * 35)/100;
        float leftH = cH;
        float leftX = cX;
        float leftY = cY;
        String inventoryLeftTooltipKey = "INVENTORY_TOP_LEFT_TOOLTIP";
        String inventoryLeftPanelKey = "INVENTORY_TOP_LEFT_PANEL";
        ba_component inventoryLeftContainer = new ba_component(componentMap, creatorComponent.mainPanel, leftW, leftH, leftX, leftY, true, inventoryLeftPanelKey);
        TooltipMakerAPI inventoryLeftTooltipContainer = inventoryLeftContainer.createTooltip(inventoryLeftTooltipKey, leftW, leftH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryLeftPanelKey, inventoryLeftContainer, leftX, leftY);

//        UIComponentAPI borderLeft = inventoryLeftTooltipContainer.createRect(Color.PINK, 1);
//        borderLeft.getPosition().setSize(leftW, leftH);
//        inventoryLeftContainer.mainPanel.addComponent(borderLeft).setLocation(0,0).inTL(0, 0);
        displaySelectedPersonInfo(inventoryLeftContainer, inventoryLeftTooltipKey, leftW, leftH, leftX, leftY);

//        //right
        float rightW = cW - leftW;
        float rightH = cH;
        float rightX = leftW;
        float rightY = cY;
        String inventoryRightTooltipKey = "INVENTORY_TOP_RIGHT_TOOLTIP";
        String inventoryRightPanelKey = "INVENTORY_TOP_RIGHT_PANEL";
        ba_component inventoryRightContainer = new ba_component(componentMap, creatorComponent.mainPanel, rightW, rightH, rightX, rightY, true, inventoryRightPanelKey);
        TooltipMakerAPI inventoryRightTooltipContainer = inventoryRightContainer.createTooltip(inventoryRightTooltipKey, rightW, rightH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryRightPanelKey, inventoryRightContainer, rightX, rightY);

        UIComponentAPI borderRight = inventoryRightTooltipContainer.createRect(Color.RED, 1);
        borderRight.getPosition().setSize(rightW, rightH);
        inventoryRightContainer.mainPanel.addComponent(borderRight).setLocation(0,0).inTL(0, 0);
    }
    private void displayInventoryMid(ba_component creatorComponent, TooltipMakerAPI creatorComponentTooltipMaker, float cW, float cH, float cX, float cY) {
        final float pad = 10f;
        float opad = 10f;
        final Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        UIComponentAPI borderTop = creatorComponentTooltipMaker.createRect(Misc.getDarkPlayerColor(), 1);
        borderTop.getPosition().setSize(cW, 1);
        creatorComponent.mainPanel.addComponent(borderTop).setLocation(0,0).inTL(0, 0);
        UIComponentAPI borderBtm = creatorComponentTooltipMaker.createRect(Misc.getDarkPlayerColor(), 1);
        borderBtm.getPosition().setSize(cW, 1);
        creatorComponent.mainPanel.addComponent(borderBtm).setLocation(0,0).inTL(0, cH);

        float switchW = 200;
        float switchH = 40;
        float switchX = pad;
        float switchY = cH/2 - switchH/2;
        Color invColor = Color.YELLOW.darker().darker();
        Color personColor = Misc.getGrayColor().darker().darker();
        ButtonAPI switchBtn = creatorComponentTooltipMaker.addButton(
                "Switch to - " + (Objects.equals(currentSelectOverclockLocation, INVENTORY) ? "Fleet": "Inventory"),
                null,
                Misc.getBrightPlayerColor(), Objects.equals(currentSelectOverclockLocation, INVENTORY) ? personColor: invColor,
                Alignment.MID, CutStyle.TOP,
                switchW, switchH, pad
                );
        addButtonToList(switchBtn, "switch:" + (Objects.equals(currentSelectOverclockLocation, INVENTORY) ? PERSON_LIST: INVENTORY));
        switchBtn.getPosition().setLocation(0,0).inTL(switchX, switchY);
        switchBtn.setShortcut(Keyboard.KEY_S, false);

        float questionW = 40;
        float questionH = 40;
        float questionX = switchW + switchX + pad;
        float questionY = switchY;
        ButtonAPI questionBtn = creatorComponentTooltipMaker.addButton(
                "?", null,
                Misc.getBrightPlayerColor(), Color.GREEN.darker().darker(),
                Alignment.MID, CutStyle.TL_BR,
                questionW, questionH, pad
        );
        addButtonToList(questionBtn, "question:");
        questionBtn.getPosition().setLocation(0,0).inTL(questionX, questionY);
        creatorComponentTooltipMaker.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return true;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                //todo: add a bit of lore about the instruction: like a tripad shows up, displaying a scientist explaining how it all works
                tooltip.setParaOrbitronLarge();
                LabelAPI helpPara = tooltip.addSectionHeading("Help", Alignment.MID, 0);
                tooltip.setParaFontDefault();
                LabelAPI step1Para = tooltip.addPara("- Select the %s from either %s or %s", pad , h, "overclocking bionic", "player fleet", "player inventory");
                step1Para.setHighlightColors(h,h,h);
                LabelAPI step2Para = tooltip.addPara("- Select what %s you want to put into your bionic", pad , h, "overclock");
                step2Para.setHighlightColors(h);
                LabelAPI step3Para = tooltip.addPara("- Check for the %s", pad , h, "overclock cost");
                step3Para.setHighlightColors(h);
                LabelAPI step4Para = tooltip.addPara("- If available, select the %s button", pad , h, "Overclock");
                step4Para.setHighlightColors(h);
            }
        }, questionBtn, TooltipMakerAPI.TooltipLocation.ABOVE);

        //overclock button
        float overclockW = 150;
        float overclockH = 40;
        float overclockX = cW - overclockW - pad;
        float overclockY = switchY;
        ButtonAPI overclockBtn = creatorComponentTooltipMaker.addButton(
                "Overclock", null,
                Misc.getBrightPlayerColor(), new Color(255, 117, 134).darker().darker(),
                Alignment.MID, CutStyle.ALL,
                overclockW, overclockH, pad
        );
        addButtonToList(overclockBtn, "overclock:");
        overclockBtn.getPosition().setLocation(0,0).inTL(overclockX, overclockY);
        overclockBtn.setEnabled(this.currentSelectOverclockBionic != null && this.currentSelectedOverclock != null);
        creatorComponentTooltipMaker.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return true;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.setParaOrbitronLarge();
                LabelAPI helpPara = tooltip.addSectionHeading("Info", Alignment.MID, 0);
                tooltip.setParaFontDefault();
                LabelAPI selectBPara = tooltip.addPara("- Current selected bionic: %s", pad , h, currentSelectOverclockBionic != null? currentSelectOverclockBionic.getName(): "None");
                selectBPara.setHighlightColors(currentSelectOverclockBionic != null? h : g);
                if(currentSelectOverclockLocation.equals(PERSON_LIST)) {
                    LabelAPI selectLPara = tooltip.addPara("- Current selected limb: %s", pad , h, currentSelectedLimb != null ? currentSelectedLimb.name: "None");
                    selectLPara.setHighlightColors(currentSelectedLimb != null ? h: g);
                }
                LabelAPI selectOPara = tooltip.addPara("- Current selected overclock: %s", pad , h,
                        (currentSelectedOverclock != null) ?
                                ba_overclockmanager.getOverclock(currentSelectedOverclock) != null ?
                                ba_overclockmanager.getOverclock(currentSelectedOverclock).name : "the overclock ID doesn't match any of the existing" :
                            "None");
                selectOPara.setHighlightColors(currentSelectedOverclock != null? h: g);
            }
        }, overclockBtn, TooltipMakerAPI.TooltipLocation.ABOVE);
    }
    public void displayInventoryBtm(ba_component creatorComponent, String creatorComponentTooltip, float cW, float cH, float cX, float cY) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        if(currentSelectOverclockLocation.equals(PERSON_LIST)) {
            //left
            float leftW = (cW * 35)/100;
            float leftH = cH;
            float leftX = cX;
            float leftY = cY;
            String inventoryLeftTooltipKey = "INVENTORY_BTM_LEFT_TOOLTIP";
            String inventoryLeftPanelKey = "INVENTORY_BTM_LEFT_PANEL";
            ba_component inventoryLeftContainer = new ba_component(componentMap, creatorComponent.mainPanel, leftW, leftH, leftX, leftY, true, inventoryLeftPanelKey);
            TooltipMakerAPI inventoryLeftTooltipContainer = inventoryLeftContainer.createTooltip(inventoryLeftTooltipKey, leftW, leftH, false, 0,0);
            creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryLeftPanelKey, inventoryLeftContainer, leftX, leftY);

//        UIComponentAPI borderLeft = inventoryLeftTooltipContainer.createRect(Color.white, 1);
//        borderLeft.getPosition().setSize(leftW, leftH);
//        inventoryLeftContainer.mainPanel.addComponent(borderLeft).setLocation(0,0).inTL(0, 0);
            displayPersonList(inventoryLeftContainer, inventoryLeftTooltipKey, false, leftW, leftH, 0, 0);

//        //right
            float rightW = cW - leftW;
            float rightH = cH;
            float rightX = leftW;
            float rightY = cY;
            String inventoryRightTooltipKey = "INVENTORY_BTM_RIGHT_TOOLTIP";
            String inventoryRightPanelKey = "INVENTORY_BTM_RIGHT_PANEL";
            ba_component inventoryRightContainer = new ba_component(componentMap, creatorComponent.mainPanel, rightW, rightH, rightX, rightY, true, inventoryRightPanelKey);
            TooltipMakerAPI inventoryRightTooltipContainer = inventoryRightContainer.createTooltip(inventoryRightTooltipKey, rightW, rightH, false, 0,0);
            creatorComponent.attachSubPanel(creatorComponentTooltip, inventoryRightPanelKey, inventoryRightContainer, rightX, rightY);

//        UIComponentAPI borderRight = inventoryRightTooltipContainer.createRect(Color.CYAN, 1);
//        borderRight.getPosition().setSize(rightW, rightH);
//        inventoryRightContainer.mainPanel.addComponent(borderRight).setLocation(0,0).inTL(0, 0);
            displayBionicTable(inventoryRightContainer, inventoryRightTooltipKey, true, true, rightW, rightH, 0, 0);
        }
        if(currentSelectOverclockLocation.equals(INVENTORY)) {
            displayInventoryWorkshop(creatorComponent, creatorComponentTooltip, cW, cH, cX, cY);
        }
    }
    public void displaySelectedPersonInfo(ba_component creatorComponent, String creatorComponentTooltip, float cW, float cH, float cX, float cY) {
        //todo: move the person info to somewhere better like center or something
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();

        String infoPersonTooltipKey = "OVERCLOCK_PERSON_INFO_TOOLTIP";
        String infoPersonPanelKey = "OVERCLOCK_PERSON_INFO_PANEL";
        ba_component infoPersonContainer = new ba_component(componentMap, creatorComponent.mainPanel, cW, cH, cX, cY, true, infoPersonPanelKey);
        TooltipMakerAPI infoPersonTooltipContainer = infoPersonContainer.createTooltip(infoPersonTooltipKey, cW, cH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, infoPersonPanelKey,infoPersonContainer,0,0);

        //Selected Person info
        int selectH = 30;
        int selectW = (int) cW;
        int selectX = 0;
        int selectY = 0;
        infoPersonTooltipContainer.setParaOrbitronLarge();
        LabelAPI select = infoPersonTooltipContainer.addPara("Selected Person Info", pad);
        select.setAlignment(Alignment.MID);
        select.getPosition().inTL(selectX, selectY);
        select.getPosition().setSize(selectW, selectH);
        infoPersonTooltipContainer.setParaFontDefault();

//        infoPersonTooltipContainer.addPara("person info" , 0);
        float infoLeftW = cW * 0.4f;
        float inforRightW = cW - infoLeftW;

        //--------image
        float imageX = (int) (0);
        float imageY = (int) (selectH + pad);
        float imageW = (int) infoLeftW;
        float imageH = imageW;
        String spriteName = this.currentPerson.getPortraitSprite();
        TooltipMakerAPI personImageTooltip = infoPersonContainer.createTooltip("OVERCLOCK_PERSON_IMAGE", imageW, imageH, false, 0, 0);
        personImageTooltip.getPosition().inTL(imageX, imageY);
        personImageTooltip.addImage(spriteName, imageW, imageH, 0);

        //todo: dev mode displaying overclock stuffs for debugging
//        if(bionicalterationplugin.isDevmode) {
//            infoPersonTooltipContainer.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
//                @Override
//                public boolean isTooltipExpandable(Object tooltipParam) {
//                    return false;
//                }
//
//                @Override
//                public float getTooltipWidth(Object tooltipParam) {
//                    return 400;
//                }
//
//                @Override
//                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
//                    tooltip.addSectionHeading("BRM MODIFY ID", Alignment.MID, 0);
//                    HashMap<String, MutableStat.StatMod> brmIds = currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).getFlatBonuses();
//                    tooltip.addPara(brmIds.keySet().toString(), pad);
//                    tooltip.addSectionHeading("CONSCIOUSNESS MODIFY ID", Alignment.MID, pad);
//                    HashMap<String, MutableStat.StatMod> consciousIds = currentPerson.getStats().getDynamic().getMod(ba_variablemanager.BA_CONSCIOUSNESS_STATS_KEY).getFlatBonuses();
//                    tooltip.addPara(consciousIds.keySet().toString(), pad);
//                }
//            }, personImageTooltip, TooltipMakerAPI.TooltipLocation.RIGHT);
//        }
        //---------Name
        int nameH = 30;
        int nameW = (int) inforRightW;
        int nameX = (int) (imageW + pad + pad);
        int nameY = (int) (selectH + pad);
        LabelAPI name = infoPersonTooltipContainer.addPara(this.currentPerson.getName().getFullName() + (this.currentPerson.isPlayer() ? " (" + "You" + ")": ""), pad);
        name.getPosition().inTL(nameX, nameY);
        name.getPosition().setSize(nameW, nameH);
        name.setHighlight(this.currentPerson.getName().getFullName());
        name.setHighlightColors(Misc.getBrightPlayerColor());
        //BRM (Bionic Rights Management)
        int brmH = 30;
        int brmW = (int) inforRightW;
        int brmX = (int) (imageW + pad + pad);
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
        int consciousnessX = (int) (imageW + pad + pad);
        int consciousnessH = 30;
        int consciousnessW = (int) inforRightW;
        int conditionY = (int) (consciousnessH + consciousnessY);
        int conditionX = (int) (imageW + pad + pad);
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
                return 300;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                ba_consciousmanager.displayConsciousEffects(tooltip, currentPerson, expanded);
            }
        }, consciousAreaChecker, TooltipMakerAPI.TooltipLocation.ABOVE);
    }
    public void displayOverclockList(ba_component creatorComponent, String creatorComponentTooltip, float listW, float listH, float listX, float listY) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        //overclock list
        float overclockListW = listW;
        float overclockListH = listH;
        float overclockListX = listX;
        float overclockListY = listY;
        String overclockListTooltipKey = "OVERCLOCK_LIST_TOOLTIP";
        String overclockListPanelKey = "OVERCLOCK_LIST_PANEL";
        ba_component overclockListContainer = new ba_component(componentMap, creatorComponent.mainPanel, overclockListW, overclockListH, overclockListX, overclockListY, true, overclockListPanelKey);
        TooltipMakerAPI overclockListTooltipContainer = overclockListContainer.createTooltip(overclockListTooltipKey, overclockListW, overclockListH, true, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, overclockListPanelKey, overclockListContainer, overclockListX, overclockListY);

        UIComponentAPI borderList = overclockListTooltipContainer.createRect(Misc.getDarkPlayerColor(), 1);
        borderList.getPosition().setSize(overclockListW, overclockListH);
        overclockListContainer.mainPanel.addComponent(borderList).setLocation(0,0).inTL(0, 0);
        if(currentSelectOverclockBionic == null || currentSelectOverclockBionic.overclockList.isEmpty()) {
            //display the empty overclock
            int emptyX = (int) overclockListW / 2;
            int emptyY = (int) overclockListH / 2;
            String emptyText = "Currently no available overclock.";
            LabelAPI emptyLabel = overclockListTooltipContainer.addPara(emptyText,Misc.getDarkPlayerColor(), 1);
            emptyLabel.getPosition().inTL(emptyX - (emptyLabel.computeTextWidth(emptyText) / 2), emptyY);
        } else {
            //todo: overclock list.
            //disable the item if the person already installed that overclock
            float itemW = listW;
            float itemH = 300;
            float itemX = 0;
            float itemY = 0;
            int i = 0;
            for(String overclockId: currentSelectOverclockBionic.overclockList) {
                if(ba_overclockmanager.getOverclock(overclockId) != null) {
                    displayOverclockItem(
                            overclockListContainer,
                            overclockListTooltipKey,
                            ba_overclockmanager.getOverclock(overclockId),
                            i,
                            itemW, itemH,
                            itemX, itemY
                    );
                    if(i != currentSelectOverclockBionic.overclockList.size() - 1) {
                        overclockListTooltipContainer.addSpacer(pad);
                    }
                    i++;
                } else {
                    log.error("Can't find overclock of string: " + overclockId + "to display for bionic: " + currentSelectOverclockBionic.getName());
                }
            }
        }
        //do the adding late so the scroll work (thanks Lukas04)
        overclockListContainer.mainPanel.addUIElement(overclockListTooltipContainer);
    }
    public void displayOverclockItem(ba_component creatorComponent, String creatorComponentTooltip, ba_overclock overclock, int index ,float itemW, float itemH, float itemX, float itemY) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        //item
        float itemContainerW = itemW;
        float itemContainerH = itemH;
        float itemContainerX = itemX;
        float itemContainerY = itemY;
        String itemContainerTooltipKey = "OVERCLOCK_ITEM_CONTAINER_TOOLTIP";
        String itemContainerPanelKey = "OVERCLOCK_ITEM_CONTAINER_PANEL_" + index;
        ba_component itemContainerContainer = new ba_component(componentMap, creatorComponent.mainPanel, itemContainerW, itemContainerH, itemContainerX, itemContainerY, false, itemContainerPanelKey);
        TooltipMakerAPI itemContainerTooltipContainer = itemContainerContainer.createTooltip(itemContainerTooltipKey, itemContainerW, itemContainerH, false, 0,0);
        creatorComponent.attachSubPanel(creatorComponentTooltip, itemContainerPanelKey, itemContainerContainer);

        UIComponentAPI borderList = itemContainerTooltipContainer.createRect(Misc.getDarkPlayerColor(), 1);
        borderList.getPosition().setSize(itemContainerW - pad, itemContainerH - pad);
        itemContainerContainer.mainPanel.addComponent(borderList).setLocation(0,0).inTL(0, pad);

        //hover
        ButtonAPI areaChecker = itemContainerTooltipContainer.addAreaCheckbox("", null, new Color(255, 117, 134).darker().darker(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), pW, pH, 0);
        addButtonToList(areaChecker, "overclock_hover:"+overclock.id);
        areaChecker.getPosition().setSize(borderList.getPosition().getWidth(), borderList.getPosition().getHeight());
        areaChecker.getPosition().setLocation(0,0).inTL(0, pad);
        //---------Name
        int nameH = 30;
        int nameW = (int) (borderList.getPosition().getWidth() - pad - pad);
        int nameX = (int) (pad);
        int nameY = (int) (pad + pad);
        itemContainerTooltipContainer.setParaOrbitronLarge();
        LabelAPI name = itemContainerTooltipContainer.addPara(overclock.name, Misc.getHighlightColor(), pad);
        name.getPosition().setSize(nameW, nameH);
        name.getPosition().inTL(nameX, nameY);
        itemContainerTooltipContainer.setParaFontDefault();

        //todo: wrap this in a container component to control the width
        overclock.displayEffectDescription(itemContainerTooltipContainer, this.currentPerson, this.currentSelectOverclockBionic);
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
//        ba_component previousTab2 = componentMap.get("OVERCLOCK_LIST_PANEL");
//        if(previousTab2 != null && previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP") != null) {
//            ba_utils.drawBox(
//                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getX(),
//                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getY(),
//                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getWidth(),
//                    (int) previousTab2.getTooltip("OVERCLOCK_LIST_TOOLTIP").getPosition().getHeight(),
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
                        this.currentSelectedBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
                        this.currentSelectOverclockBionic = ba_bionicmanager.bionicItemMap.get(tokens[1]);
                        needsReset = true;
                        break;
                    }
                }
                if(tokens[0].equals("hover_bionic_table_limb")) {
                    this.currentSelectedLimb = ba_limbmanager.getLimb(tokens[1]);
                    for(ba_officermanager.ba_bionicAugmentedData data: ba_officermanager.getBionicAnatomyList(this.currentPerson)) {
                        if(data.limb.limbId.equals(this.currentSelectedLimb.limbId)) {
//                            this.previousSelectOverclockLimbId = this.currentSelectedLimb.limbId;
                            if(data.bionicInstalled != null) {
                                this.currentSelectOverclockBionic = data.bionicInstalled;
                            } else {
                                this.currentSelectOverclockBionic = null;
                            }
                            break;
                        }
                    }
                    needsReset = true;
                    break;
                }
                if(tokens[0].equals("switch") && ((Objects.equals(tokens[1], PERSON_LIST)) || (Objects.equals(tokens[1], INVENTORY)))) {
                    this.currentSelectOverclockLocation = tokens[1];
                    this.currentSelectOverclockBionic = null;
                    this.currentSelectedLimb = null;
                    this.currentSelectedBionic = null;
                    needsReset = true;
                    break;
                }
                if(tokens[0].equals("overclock_hover")  && tokens[1] != null) {
                    if(ba_overclockmanager.getOverclock(tokens[1]) != null) {
                        this.currentSelectedOverclock = tokens[1];
                        needsReset = true;
                        break;
                    }
                }
                if(tokens[0].equals("overclock") && this.currentSelectOverclockBionic != null && this.currentSelectedOverclock != null) {
                    boolean success = false;
                    if(this.currentSelectedLimb != null) {
                        success = ba_officermanager.overclockBionic(this.currentSelectOverclockBionic, this.currentSelectedOverclock, this.currentSelectedLimb, this.currentPerson);
                    } else {
                        success = ba_officermanager.overclockBionic(this.currentSelectOverclockBionic, this.currentSelectedOverclock, null, null);
                    }
                    if(success) {
                        this.currentSelectOverclockBionic = null;
                        this.currentSelectedLimb = null;
                        this.currentSelectedBionic = null;
                    }
                    needsReset= true;
                    break;
                }
            }
        }
        super.advance(amount);
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
                        ba_component component1 = componentMap.get("PERSON_LIST_PANEL");
                        if(component1 != null && component1.tooltipMap.get("PERSON_LIST_TOOLTIP") != null) {
                            if(tokens[0].equals("hover_person")) {
                                if(!this.currentPerson.getId().equals(tokens[1])) {
                                    if(this.currentSelectedOverclock != null) this.currentSelectedOverclock = null;
                                    if(this.currentSelectOverclockBionic != null) this.currentSelectOverclockBionic = null;
                                    if(this.currentSelectedLimb != null) this.currentSelectedLimb = null;
                                    shouldRefresh = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            super.processInput(events);
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

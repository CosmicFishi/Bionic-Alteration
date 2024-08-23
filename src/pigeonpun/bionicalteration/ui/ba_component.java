package pigeonpun.bionicalteration.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ui.bionic.ba_uiplugin;

import java.util.HashMap;
import java.util.List;

public class ba_component {
    //        protected TooltipMakerAPI tooltip;
    Logger log = Global.getLogger(ba_component.class);
    public CustomPanelAPI mainPanel;
    public HashMap<String, TooltipMakerAPI> tooltipMap = new HashMap<>();
    public HashMap<String, List<TooltipMakerAPI>> tooltipListMap = new HashMap<>();
    public HashMap<String, ba_component> subComponentMap = new HashMap<>();
    public HashMap<String, List<ba_component>> subComponentListMap = new HashMap<>();

    /**
     * Create component and attach the component's panel to the creator panel<br>
     *
     * @param creatorPanel        creator panel
     * @param width               Width of this component
     * @param height              Height of this component
     * @param x                   location
     * @param y                   location
     * @param addToCreatorPanel   true if is container, false if is element inside a scrolling container
     * @param thisComponentMapKey this component map key, access from {@code componentMap}
     */
    public ba_component(HashMap<String, ba_component> componentMap, CustomPanelAPI creatorPanel, float width, float height, float x, float y, boolean addToCreatorPanel, String thisComponentMapKey) {
        //create both panel and tooltip
        mainPanel = creatorPanel.createCustomPanel(width, height, null);
        mainPanel.getPosition().setLocation(0, 0);
        mainPanel.getPosition().inTL(x, y);
        //add into the big panel
        if (addToCreatorPanel) {
            creatorPanel.addComponent(mainPanel);
        }
        //add into list to remove on reset
        if (componentMap.get(thisComponentMapKey) == null) {
            componentMap.put(thisComponentMapKey, this);
        } else {
            log.error("Component already exist!!!. Key: " + thisComponentMapKey);
        }
    }

    /**
     * @param key
     * @param width
     * @param height
     * @param hasScroller      If true, tooltip won't be added to the panel. Meaning, you will have to manually add the tooltip to the panel later on so the scroll work
     * @param tooltipLocationX
     * @param tooltipLocationY
     * @return
     */
    public TooltipMakerAPI createTooltip(String key, float width, float height, boolean hasScroller, float tooltipLocationX, float tooltipLocationY) {
        TooltipMakerAPI tooltip = this.mainPanel.createUIElement(width, height, hasScroller);
        tooltip.setForceProcessInput(true);
        if (!hasScroller) this.mainPanel.addUIElement(tooltip).setLocation(tooltipLocationX, tooltipLocationY);
        this.tooltipMap.put(key, tooltip);
        return tooltip;
    }

    public TooltipMakerAPI getTooltip(String key) {
        TooltipMakerAPI tooltip = this.tooltipMap.get(key);
        if (tooltip == null) {
            log.error("Can not find tooltip of key " + key);
        }
        return tooltip;
    }

    /**
     * Use for attaching an existing component's panel into this component<br>
     * This doesn't set the location for the attaching component => component list can stack on top of each other
     * @param tooltipKeyAttachTo   Creator component's tooltip key
     * @param otherComponent       the attaching component
     * @param otherComponentMapKey the attaching component map key. So the creator component can access to the attaching component in {@code subComponentMap}
     */
    public void attachSubPanel(String tooltipKeyAttachTo, String otherComponentMapKey, ba_component otherComponent) {
        TooltipMakerAPI tooltipAttachingTo = this.tooltipMap.get(tooltipKeyAttachTo);
        if (tooltipAttachingTo == null) {
            log.error("Can't find container tooltip of Id: " + tooltipAttachingTo + " for: " + otherComponentMapKey);
        }
        tooltipAttachingTo.addCustom(otherComponent.mainPanel, 0f);
        subComponentMap.put(otherComponentMapKey, otherComponent);
    }

    /**
     * Use for attaching an existing component's panel into this component<br>
     * Use this for when the Sub component is aligning with the parent component which we want to break and reset it to certain point
     *
     * @param tooltipKeyAttachTo   Creator component's tooltip key
     * @param otherComponent       the attaching component
     * @param otherComponentMapKey the attaching component map key. So the creator component can access to the attaching component in {@code subComponentMap}
     * @param locationX            X
     * @param locationY            Y
     */
    public void attachSubPanel(String tooltipKeyAttachTo, String otherComponentMapKey, ba_component otherComponent, float locationX, float locationY) {
        attachSubPanel(tooltipKeyAttachTo, otherComponentMapKey, otherComponent);
        //important to do this after you attach the sub panel
        otherComponent.mainPanel.getPosition().inTL(locationX, locationY);
    }

    public void unfocusComponent(float dW) {
        mainPanel.getPosition().inTL(dW, 0);
    }
}

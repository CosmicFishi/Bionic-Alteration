package pigeonpun.bionicalteration.lunalib;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import lunalib.lunaRefit.BaseRefitButton;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.ui.bionic.ba_uiplugin;

import java.util.List;

public class ba_bionic_refit_btn extends BaseRefitButton {
    public static final float MAIN_CONTAINER_PADDING = 150f;
    protected ba_uiplugin workshopPanel = null;
    protected CustomPanelAPI backgroundPanel = null;
    protected CustomPanelAPI containerPanel = null;
    @Override
    public String getButtonName(FleetMemberAPI member, ShipVariantAPI variant) {
        return "Bionic Workshop";
    }

    @Override
    public int getOrder(FleetMemberAPI member, ShipVariantAPI variant) {
        return 85;
    }

    @Override
    public String getIconName(FleetMemberAPI member, ShipVariantAPI variant) {
        return "graphics/icons/abilities/bionic_artifact.png";
    }

    @Override
    public boolean shouldShow(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
        return true;
    }

    @Override
    public boolean hasPanel(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
        return true;
    }

    @Override
    public float getPanelHeight(FleetMemberAPI member, ShipVariantAPI variant) {
        return Global.getSettings().getScreenHeight() - MAIN_CONTAINER_PADDING;
    }

    @Override
    public float getPanelWidth(FleetMemberAPI member, ShipVariantAPI variant) {
        return Global.getSettings().getScreenWidth() - MAIN_CONTAINER_PADDING;
    }

    @Override
    public void initPanel(CustomPanelAPI backgroundPanel, FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
        this.workshopPanel = new ba_uiplugin();
        workshopPanel.init(backgroundPanel, null, null, ba_uiplugin.WORKSHOP, null);
        workshopPanel.setCurrentPerson(member.getCaptain());
        containerPanel = backgroundPanel.createCustomPanel(Global.getSettings().getScreenWidth() - MAIN_CONTAINER_PADDING, Global.getSettings().getScreenWidth() - MAIN_CONTAINER_PADDING,
                workshopPanel);
        backgroundPanel.addComponent(containerPanel);
    }

    @Override
    public void advance(FleetMemberAPI member, ShipVariantAPI variant, Float amount, MarketAPI market) {

    }

    @Override
    public void onClick(FleetMemberAPI member, ShipVariantAPI variant, InputEventAPI event, MarketAPI market) {

    }

    @Override
    public void onPanelClose(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
        this.workshopPanel = null;
        if(member != null && member.getVariant() != null) {
            if(member.getCaptain() != null && !member.getCaptain().isDefault() && !member.getCaptain().isAICore()) {
                if(ba_bionicmanager.checkIfHaveBionicInstalled(member.getCaptain())) {
                    if(!member.getVariant().hasHullMod(ba_variablemanager.BA_BIONIC_INFO_HULLMOD)) {
                        member.getVariant().addPermaMod(ba_variablemanager.BA_BIONIC_INFO_HULLMOD);
                        ba_officermanager.refresh(null);
                        refreshVariant();
                    }
                }
            }
        }
    }
}

package pigeonpun.bionicalteration.lunalib;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import lunalib.lunaRefit.BaseRefitButton;
import pigeonpun.bionicalteration.ui.bionic.ba_uiplugin;

public class ba_bionic_refit_btn extends BaseRefitButton {
    public static final float MAIN_CONTAINER_PADDING = 150f;
    protected ba_uiplugin workshopPanel = null;
    protected CustomPanelAPI backgroundPanel = null;
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
        backgroundPanel.createCustomPanel(Global.getSettings().getScreenWidth() - MAIN_CONTAINER_PADDING, Global.getSettings().getScreenWidth() - MAIN_CONTAINER_PADDING,
                workshopPanel);
    }

    @Override
    public void advance(FleetMemberAPI member, ShipVariantAPI variant, Float amount, MarketAPI market) {
        if(this.workshopPanel != null) {
            workshopPanel.advance(amount);
        }
    }

    @Override
    public void onPanelClose(FleetMemberAPI member, ShipVariantAPI variant, MarketAPI market) {
        this.workshopPanel = null;
    }
}

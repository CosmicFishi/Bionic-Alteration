package pigeonpun.bionicalteration.ability;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ui.bionic.ba_interactiondialog;

import java.awt.*;

public class ba_bionicability extends BaseDurationAbility {
    static Logger log = Global.getLogger(ba_bionicability.class);
    @Override
    protected void activateImpl() {
        Global.getSector().getCampaignUI().showInteractionDialog(new ba_interactiondialog(), null);
    }

    @Override
    public float getActivationDays() {
        return 0f;
    }

    @Override
    public float getCooldownDays() {
        return 0f;
    }

    @Override
    public float getDeactivationDays() {
        return 0f;
    }

    @Override
    public float getDurationDays() {
        return 0f;
    }

    @Override
    protected void applyEffect(float amount, float level) {

    }

    @Override
    protected void deactivateImpl() {

    }

    @Override
    protected void cleanupImpl() {

    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        float pad = 10f;
        LabelAPI title = tooltip.addTitle("Bionic Alteration");

        tooltip.addPara("Allow bionic modification for offcer, player or administrator", pad);
    }
}

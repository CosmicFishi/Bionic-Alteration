package pigeonpun.bionicalteration.hullmod;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;

public class ba_syntheticBody extends BaseHullMod {
    @Override
    public boolean shouldAddDescriptionToTooltip(ShipAPI.HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addPara("A synthetic body infused with nanites, engineered using advanced Domain-era technology. Each limb is fully modular, capable of being detached and reattached without degradation, thanks to the sophistication of Domain innovations. The body is permanently synchronized and bound to a specific ship, making it non-transferable and irreplaceable. An AI core can seamlessly integrate, functioning as the nervous system to operate the body with precision and autonomy.", 10f);
        tooltip.addPara("Bionics can be installed directly into the frame, and due to the body being automatically granted the highest system clearance upon creation, Bionic Rights Management (BRM) restrictions are largely bypassed. However, while BRM limits are removed, the body’s stability remains susceptible to the systemic strain caused by excessive or unbalanced bionic usage.", 10f);
        tooltip.addSectionHeading("Body", Alignment.MID, 10f);
        if(ship != null && ship.getCaptain()!= null) {
            ba_officermanager.ba_aimemorydata aimemorydata = ba_officermanager.getAIMemData(ship.getCaptain(), Global.getSector().getCampaignUI().getCurrentInteractionDialog());
            //if AI person have the bioform data
            if(aimemorydata == null) return;
            tooltip.setParaFontVictor14();
            for(ba_officermanager.ba_bionicAugmentedData data: ba_limbmanager.sortBionicDataByLimbOrder(aimemorydata.anatomy)) {
                tooltip.addPara(ba_limbmanager.isLimbCentralLimb(data.limb)?"%s":"  |- %s", 10f, ba_limbmanager.isLimbCentralLimb(data.limb)? ba_variablemanager.BA_OVERFORM_COLOR :Misc.getTextColor(), data.limb.name);
            }
            tooltip.setParaFontDefault();
        }
    }
}

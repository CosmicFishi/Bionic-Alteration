package pigeonpun.bionicalteration.conscious.econ;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;

public class ba_mental_unit extends BaseIndustry {

    public void apply() {super.apply(true);}
    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    protected void applyIncomeAndUpkeep(float sizeOverride) {
        if(!bionicalterationplugin.isConsciousnessDisable) {
            int upkeep = (int) (getSpec().getUpkeep());
            getUpkeep().modifyFlatAlways("ba_consciousness", upkeep, "Flat value");
        }
    }

    @Override
    public MutableStat getUpkeep() {
        if(!bionicalterationplugin.isConsciousnessDisable) {
            int upkeep = (int) (getSpec().getUpkeep());
            if(market.getAdmin() != null && !market.getAdmin().isDefault() && !ba_bionicmanager.getListStringBionicInstalled(market.getAdmin()).isEmpty()) {
                upkeep = (int) ba_consciousmanager.getConsciousnessLevel(market.getAdmin()).getConsciousTreatmentFee();
                return new MutableStat(upkeep);
            }
        }
        return super.getUpkeep();
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}

package pigeonpun.bionicalteration.conditions;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;
import pigeonpun.bionicalteration.skills.ba_bionic_augmented;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class bioniccondition extends BaseMarketConditionPlugin {
    protected PersonAPI person;

    @Override
    public void init(MarketAPI market, MarketConditionAPI condition) {
        super.init(market, condition);
        if(market.getAdmin() != null) {
            this.person = market.getAdmin();
        }
    }

    @Override
    public void apply(String id) {
        if(person != null) {
            List<ba_officermanager.ba_bionicAugmentedData> listAnatomy = ba_officermanager.getBionicAnatomyList(person);
            for(ba_officermanager.ba_bionicAugmentedData anatomy: listAnatomy) {
                for(ba_bionicitemplugin bionic: anatomy.bionicInstalled) {
                    String applyId = id + bionic.bionicId + anatomy.limb;
                    if(bionic.effectScript != null && bionic.isApplyAdminEffect && !checkIfAlreadyAppliedBionicEffect(applyId)) {
                        bionic.effectScript.applyEffectAdminMarket(market, applyId, 0, bionic);
                    }
                }
            }
            ba_consciousmanager.resetBeforeApplyEffectAdminMarket(market, id);
            ba_consciousmanager.getConsciousnessLevel(person).applyEffectAdminMarket(market, id, 0);
        }
    }

    @Override
    public void unapply(String id) {
        if(person != null) {
            List<ba_officermanager.ba_bionicAugmentedData> listAnatomy = ba_officermanager.getBionicAnatomyList(person);
            for(ba_officermanager.ba_bionicAugmentedData anatomy: listAnatomy) {
                for(ba_bionicitemplugin bionic: anatomy.bionicInstalled) {
                    if(bionic.effectScript != null && bionic.isApplyAdminEffect) {
                        String applyId = id + bionic.bionicId + anatomy.limb;
                        bionic.effectScript.unapplyEffectAdminMarket(market, applyId);
                    }
                }
            }
            ba_consciousmanager.resetBeforeApplyEffectAdminMarket(market, id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        ba_bionic_augmented.displayBionicDescriptions(this.person, tooltip, 10);
    }
    protected boolean checkIfAlreadyAppliedBionicEffect(String applyingId) {
        if(market.getMemoryWithoutUpdate().get(ba_variablemanager.BA_MARKET_BIONIC_MEMORY_KEY) != null && market.getMemoryWithoutUpdate().get(ba_variablemanager.BA_MARKET_BIONIC_MEMORY_KEY) instanceof List) {
            List<String> listIds = (List<String>) market.getMemoryWithoutUpdate().get(ba_variablemanager.BA_MARKET_BIONIC_MEMORY_KEY);
            for(String str: listIds) {
                if (str.equals(applyingId)) {
                    return true;
                }
            }
        } else {
            List<String> listIds = new ArrayList<>();
            market.getMemoryWithoutUpdate().set(ba_variablemanager.BA_MARKET_BIONIC_MEMORY_KEY, listIds);
        }
        return false;
    }
}

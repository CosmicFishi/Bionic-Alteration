package pigeonpun.bionicalteration.bionic;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ba_bionicitemplugin implements SpecialItemPlugin {
    static Logger log = Global.getLogger(ba_bionicitemplugin.class);
    public String bionicId;
    public String bionicLimbGroupId;
    public String namePrefix;
    public Color displayColor;
    public float brmCost;
    public float consciousnessCost;
    public ba_bioniceffect effectScript;
//    public boolean isCaptainBionic;
    public boolean isApplyCaptainEffect;
    public boolean isApplyAdminEffect;
    public boolean isAICoreBionic;
    public boolean isAdvanceInCombat;
    public boolean isAdvanceInCampaign;
    public boolean isAllowedRemoveAfterInstall;
    public List<String> conflictedBionicIdList = new ArrayList<>();
    protected SpecialItemSpecAPI spec;
    protected CargoStackAPI stack;
    public HashMap<String, Object> customData = new HashMap<>();
    public ba_bionicitemplugin() {}
    public ba_bionicitemplugin(String bionicId, SpecialItemSpecAPI spec ,String bionicLimbGroupId, String namePrefix, Color displayColor, int brmCost,
                               float consciousnessCost, boolean isApplyCaptainEffect, boolean isApplyAdminEffect, boolean isAICoreBionic, ba_bioniceffect effectScript,
                               List<String> conflictedBionicIdList, boolean isAllowedRemoveAfterInstall) {
        this.bionicId = bionicId;
        this.spec = spec;
        this.bionicLimbGroupId = bionicLimbGroupId;
        this.namePrefix = namePrefix;
        this.displayColor = displayColor;
        this.brmCost = brmCost;
        this.consciousnessCost = consciousnessCost;
        this.isAICoreBionic = isAICoreBionic;
        this.isApplyAdminEffect = isApplyAdminEffect;
        this.isApplyCaptainEffect = isApplyCaptainEffect;
        this.effectScript = effectScript;
        if(conflictedBionicIdList != null) {
            this.conflictedBionicIdList = conflictedBionicIdList;
        }
        this.isAllowedRemoveAfterInstall = isAllowedRemoveAfterInstall;
    }
    public String getId() {
        return bionicId;
    }
    public void setId(String id) {
        this.bionicId = id;
        spec = Global.getSettings().getSpecialItemSpec(id);
        log.info(spec);
    }

    public void init(CargoStackAPI stack) {
        this.stack = stack;
    }

    @Override
    public String getName() {
        return spec.getName();
    }

    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
        if (spec != null) return (int) spec.getBasePrice();
        return 0;
    }

    @Override
    public boolean hasRightClickAction() {
        return true;
    }

    @Override
    public void performRightClickAction() {
        //todo: add open INSTALL UI
    }

    @Override
    public boolean shouldRemoveOnRightClickAction() {
        return false;
    }

    @Override
    public boolean isTooltipExpandable() {
        return false;
    }

    @Override
    public float getTooltipWidth() {
        return 450;
    }

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        createTooltip(tooltip, expanded, transferHandler, stackSource, false);
    }
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource, boolean useGray) {
        float opad = 10f;

        tooltip.addTitle(getName());

        String design = getDesignType();
        Misc.addDesignTypePara(tooltip, design, opad);

        if (!spec.getDesc().isEmpty()) {
            Color c = Misc.getTextColor();
            tooltip.addPara(spec.getDesc(), c, opad);
        }
        addCostLabel(tooltip, opad, transferHandler, stackSource);
    }
    protected void addCostLabel(TooltipMakerAPI tooltip, float pad, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        BaseSpecialItemPlugin.ItemCostLabelData data = getCostLabelData(stack, transferHandler, stackSource);

        LabelAPI label = tooltip.addPara(data.text, pad);
        if (data.highlights != null) {
            label.setHighlight(data.highlights.getText());
            label.setHighlightColors(data.highlights.getColors());
        }
    }
    protected BaseSpecialItemPlugin.ItemCostLabelData getCostLabelData(CargoStackAPI stack, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        String text = "";
        String highlight = null;
        Highlights highlights = null;
        Color highlightColor = Misc.getHighlightColor();
        SubmarketPlugin.TransferAction action = SubmarketPlugin.TransferAction.PLAYER_BUY;
        if (transferHandler != null && stackSource == transferHandler.getManifestOne()) {
            action = SubmarketPlugin.TransferAction.PLAYER_SELL;
        }


        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL && stack.getSpecialItemSpecIfSpecial() != null) {
            SpecialItemSpecAPI spec = stack.getSpecialItemSpecIfSpecial();
            if (spec.hasTag(Tags.MISSION_ITEM)) {
                text = "Can not remove item";
                highlight = text;
                highlights = new Highlights();
                highlights.append(text, Misc.getNegativeHighlightColor());

                BaseSpecialItemPlugin.ItemCostLabelData data = new BaseSpecialItemPlugin.ItemCostLabelData();
                data.text = text;// + ".";
                data.highlights = highlights;

                return data;
            }
        }



        if (transferHandler != null && transferHandler.getSubmarketTradedWith() != null &&
                transferHandler.getSubmarketTradedWith().isIllegalOnSubmarket(stack, action)) {
            highlightColor = Misc.getNegativeHighlightColor();
            //text = "Illegal to trade on the " + transferHandler.getSubmarketTradedWith().getNameOneLine() + " here";
            text = transferHandler.getSubmarketTradedWith().getPlugin().getIllegalTransferText(stack, action);
            highlight = text;
            highlights = transferHandler.getSubmarketTradedWith().getPlugin().getIllegalTransferTextHighlights(stack, SubmarketPlugin.TransferAction.PLAYER_BUY);
        } else {
            if (stackSource != null && transferHandler != null && !transferHandler.isNoCost()) {
                if (stackSource == transferHandler.getManifestOne()) {
                    int cost = (int)transferHandler.computeCurrentSingleItemSellCost(stack);
                    //text = "Sells for: " + Misc.getWithDGS(cost) + " credits per unit";
                    text = "Sells for: " + Misc.getDGSCredits(cost) + " per unit.";
                    highlight = "" + Misc.getDGSCredits(cost);
                } else {
                    int cost = (int)transferHandler.computeCurrentSingleItemBuyCost(stack);
                    //text = "Price: " + Misc.getWithDGS(cost) + " credits per unit";
                    text = "Price: " + Misc.getDGSCredits(cost) + " per unit.";
                    highlight = "" + Misc.getDGSCredits(cost);
                }
            } else {
                int cost = (int) stack.getBaseValuePerUnit();
                //float mult = Global.getSettings().getFloat("nonEconItemSellPriceMult");
                //cost *= mult;
                //text = "Base value: " + Misc.getWithDGS(cost) + " credits per unit";
                text = "Base value: " + Misc.getDGSCredits(cost) + " per unit.";
                highlight = "" + Misc.getDGSCredits(cost);
            }
        }

        if (highlights == null) {
            highlights = new Highlights();
            highlights.setText(highlight);
            highlights.setColors(highlightColor);
        }

        BaseSpecialItemPlugin.ItemCostLabelData data = new BaseSpecialItemPlugin.ItemCostLabelData();
        data.text = text;// + ".";
        data.highlights = highlights;

        return data;
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer) {
        //do render
        float cx = x + w/2f;
        float cy = y + h/2f;

        float blX = cx -25f;
        float blY = cy -14f;
        float tlX = cx -30f;
        float tlY = cy +16f;
        float trX = cx +24f;
        float trY = cy +22f;
        float brX = cx +30f;
        float brY = cy -6f;

        SpriteAPI sprite = Global.getSettings().getSprite(spec.getIconName());

        float mult = 1f;

        sprite.setAlphaMult(alphaMult * mult);
        sprite.setNormalBlend();
        sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);

        if (glowMult > 0) {
            sprite.setAlphaMult(alphaMult * glowMult * 0.5f * mult);
            sprite.setAdditiveBlend();
            sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
        }

        renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);
        //do effect render ?
        if(this.effectScript != null) {
            effectScript.renderExtraOnItem(x,y,w,h,alphaMult,glowMult,renderer);
        }
    }

    @Override
    public String resolveDropParamsToSpecificItemData(String params, Random random) throws JSONException {
        return "";
    }

    @Override
    public String getDesignType() {
        return spec.getManufacturer();
    }

    @Override
    public SpecialItemSpecAPI getSpec() {
        return spec;
    }
}

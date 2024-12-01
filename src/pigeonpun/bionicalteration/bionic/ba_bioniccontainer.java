package pigeonpun.bionicalteration.bionic;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.inventory.ba_inventoryhandler;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ba_bioniccontainer implements SpecialItemPlugin {
    protected SpecialItemSpecAPI spec;
    protected CargoStackAPI stack;
    protected String itemId;
    @Override
    public void setId(String id) {
        this.itemId = id;
        spec = Global.getSettings().getSpecialItemSpec(id);
    }

    @Override
    public void init(CargoStackAPI stack) {
        this.stack = stack;
    }

    @Override
    public String getName() {
        return spec.getName();
    }

    @Override
    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
        return 0;
    }

    @Override
    public boolean hasRightClickAction() {
        return false;
    }

    @Override
    public void performRightClickAction() {
        //todo: cycle thru each bionic tiers
    }

    @Override
    public boolean shouldRemoveOnRightClickAction() {
        return false;
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }

    @Override
    public float getTooltipWidth() {
        return 450;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        final float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        final Color t = Misc.getTextColor();
        final Color g = Misc.getGrayColor();
        final Color special = ba_variablemanager.BA_OVERCLOCK_COLOR;

        tooltip.setParaInsigniaLarge();
        LabelAPI nameLabel = tooltip.addPara(getName(), Misc.getHighlightColor(),0);
        tooltip.addSpacer(5);
        tooltip.setParaFontDefault();

        LabelAPI descriptionLabel = tooltip.addPara(spec.getDesc(), t,0);
        tooltip.addSpacer(5);

        LabelAPI guideLabel = tooltip.addPara("The full list of bionics can be view inside the Bionic Workshop UI (Bionic Alteration ABILITY).", g,0);
        tooltip.addSpacer(pad * 2);

        CargoAPI sortedCargo = ba_inventoryhandler.uncompressAllBionics();
        if(sortedCargo.isEmpty()) {
            LabelAPI emptyLabel = tooltip.addPara("Empty", g,0);
        }
        sortedCargo.sort();
        HashMap<String, CargoAPI> designType = new HashMap<>();
        for(CargoStackAPI stack: sortedCargo.getStacksCopy()) {
            if(stack.getPlugin() instanceof ba_bionicitemplugin) {
                if(!stack.getPlugin().getDesignType().equals("")) {
                    if(designType.get(stack.getPlugin().getDesignType()) == null) {
                        CargoAPI newCargo = Global.getFactory().createCargo(true);
                        newCargo.addFromStack(stack);
                        designType.put(stack.getPlugin().getDesignType(), newCargo);
                    } else {
                        designType.get(stack.getPlugin().getDesignType()).addFromStack(stack);
                    }
                } else {
                    if(designType.get("Other") == null) {
                        CargoAPI newCargo = Global.getFactory().createCargo(true);
                        newCargo.addFromStack(stack);
                        designType.put("Other", newCargo);
                    } else {
                        designType.get("Other").addFromStack(stack);
                    }
                }
            }
        }
        int maxDesign = 5;
        int designCount = 0;
        for(Map.Entry<String, CargoAPI> setCargo: designType.entrySet()) {
            int i = 1;
            int maxRowNotExpanded = 4;
            if(designCount <= maxDesign) {
                tooltip.beginGrid(getTooltipWidth()/3 - 5, 3);
                tooltip.addToGrid(0, 0, setCargo.getKey(), "");
                tooltip.setGridRowHeight(15);
                for(CargoStackAPI stack: setCargo.getValue().getStacksCopy()) {
                    if(stack.getPlugin() instanceof ba_bionicitemplugin && maxRowNotExpanded >= i) {
                        ba_bionicitemplugin bionic = (ba_bionicitemplugin) stack.getPlugin();
//                LabelAPI subName = tooltip.addPara("%s [ %s ] ( x%s )", 0, t, bionic.getName(),
//                        bionic.getAppliedOverclockOnItem() != null ? bionic.getAppliedOverclockOnItem().name: "---", "" + Math.round(stack.getSize()));
//                subName.setHighlightColors(bionic.displayColor, bionic.getAppliedOverclockOnItem() != null ? special : g, h);
                        tooltip.setGridLabelColor(bionic.displayColor);
                        tooltip.addToGrid(0, i, "  " + bionic.getName() , "", Misc.getTextColor());
                        tooltip.addToGrid(1, i, "", "x" + Math.round(stack.getSize()), t);
                        tooltip.setGridLabelColor(t);
                        tooltip.addToGrid(2, i, "", bionic.getAppliedOverclockOnItem() != null ? bionic.getAppliedOverclockOnItem().name: "[ - ]", bionic.getAppliedOverclockOnItem() != null ? special: g);
                        i++;
                    } else {
                        tooltip.addToGrid(0, i, "... and more", "", g);
                    }
                }
                tooltip.addGrid(0);
                tooltip.addSpacer(pad*2);
                designCount += 1;
            }
        }
        if(designCount > maxDesign) {
            tooltip.addPara(".. and much much more bionics, viewable in Bionic Workshop UI", g, 0);
        }
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer) {

    }

    @Override
    public String resolveDropParamsToSpecificItemData(String params, Random random) throws JSONException {
        return "";
    }

    @Override
    public String getDesignType() {
        return null;
    }

    @Override
    public SpecialItemSpecAPI getSpec() {
        return spec;
    }
}

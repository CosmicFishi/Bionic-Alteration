package pigeonpun.bionicalteration.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import pigeonpun.bionicalteration.ba_variablemanager;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

public class ba_bionicstationintel extends BaseIntelPlugin {
    protected SectorEntityToken station;

    public ba_bionicstationintel(SectorEntityToken station) {
        this.station = station;
    }
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        Color tc = getBulletColorForMode(mode);

        bullet(info);

        LabelAPI para = info.addPara("Status: %s", initPad, tc, isRepaired()? "Functional": "Nonfunctional");
        para.setHighlightColors(isRepaired()? h: Misc.getNegativeHighlightColor());
        initPad = 0f;

        unindent(info);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;
        addBulletPoints(info, ListInfoMode.IN_DESC);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);
        addBulletPoints(info, mode);
    }

    @Override
    protected String getName() {
        return station.getName();
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "ba_bionic_station");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(ba_variablemanager.BA_OVERCLOCK_STATION_TAG_NAME);
        return tags;
    }

    public boolean isRepaired() {
        if(station.getMemoryWithoutUpdate().contains("$usable") && station.getMemoryWithoutUpdate().get("$usable").equals("true")) {
            return true;
        }
        return false;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return station;
    }
}

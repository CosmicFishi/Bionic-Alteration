package pigeonpun.bionicalteration.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;
import pigeonpun.bionicalteration.conscious.ba_conscious;
import pigeonpun.bionicalteration.conscious.ba_consciousmanager;

import java.awt.*;
import java.util.List;

public class ba_aptitude_bionic {
    static Logger log = Global.getLogger(ba_aptitude_bionic.class);
    public static class Level0 implements DescriptionSkillEffect {

        @Override
        public Color getTextColor() {
            return null;
        }

        @Override
        public String getString() {
            return "Bionics";
        }

        @Override
        public String[] getHighlights() {
            return new String[0];
        }

        @Override
        public Color[] getHighlightColors() {
            return new Color[0];
        }
    }
}

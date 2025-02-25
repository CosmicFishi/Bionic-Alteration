package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.Nex_BlueprintSwap;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import exerelin.utilities.StringHelper;
import org.lwjgl.input.Keyboard;
import pigeonpun.bionicalteration.ba_officermanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.plugin.bionicalterationplugin;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ba_BRMManage extends PaginatedOptions {
    public static final String BRM_OFFICER_OPT_PREFIX = "ba_officer_selection_";
    public List<PersonAPI> personList;
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        updatePersonList();
        switch (arg) {
            case "displayOfficersList":
                setupDelegateDialog(dialog);
                displayOfficersList();
                showOptions();
                break;
            case "select":
                displayInfo(dialog.getTextPanel());
                break;
            case "upgrade":
                //todo: this, upgrade, remove credit from player, increase BRM Tier
                break;
        }
        return false;
    }
    public void updatePersonList() {
        personList = ba_officermanager.getListOfficerFromFleet(null, true);
    }
    /**
     * To be called only when paginated dialog options are required.
     * Otherwise we get nested dialogs that take multiple clicks of the exit option to actually exit.
     * @param dialog
     */
    protected void setupDelegateDialog(InteractionDialogAPI dialog)
    {
        originalPlugin = dialog.getPlugin();

        dialog.setPlugin(this);
        init(dialog);
    }
    @Override
    public void showOptions() {
        super.showOptions();
//        for (String optId : disabledOpts)
        //todo: set up dsisable option for officer with max BRm
//        {
//            dialog.getOptionPanel().setEnabled(optId, false);
//        }
        dialog.getOptionPanel().setShortcut("ba_BRMSwapMenuReturn", Keyboard.KEY_ESCAPE, false, false, false, false);
    }
    public void displayOfficersList() {
        //add officers into the list
        dialog.getOptionPanel().clearOptions();

        int index = 0;
        for (PersonAPI person : personList)
        {
            //todo: set up list officers
            addOption(person.getNameString(), BRM_OFFICER_OPT_PREFIX + index);
            index++;
        }

        addOptionAllPages(StringHelper.getString("back", true), "ba_BRMSwapMenuReturn");
    }
    public void displayInfo(TextPanelAPI text) {
        float pad = 10f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color t = Misc.getTextColor();
        Color g = Misc.getGrayColor();
        int index = Integer.parseInt(memoryMap.get(MemKeys.LOCAL).getString("$option").substring(BRM_OFFICER_OPT_PREFIX.length()));
        PersonAPI person = personList.get(index);
        ba_officermanager.ba_personmemorydata memoryData = ba_officermanager.getPersonMemoryData(person);
        text.setFontSmallInsignia();

        text.addImage(person.getPortraitSprite());
        LabelAPI name = text.addPara(person.getName().getFullName() + (person.isPlayer() ? " (" + "You" + ")": ""));
        name.setHighlight(person.getName().getFullName());
        name.setHighlightColors(Misc.getBrightPlayerColor());

        boolean isAdmin = ba_officermanager.isCaptainOrAdmin(person, false).equals(ba_officermanager.ba_profession.ADMIN);

        String profString = ba_officermanager.getProfessionText(person, false);
        LabelAPI prof = text.addPara("Profession: " + profString);
        prof.setHighlight("Profession: ", profString);
        prof.setHighlightColors(g,h);

        if(memoryData != null) {
            int brmTier = memoryData.BRMTier;
            LabelAPI BRMTier = text.addPara("BRM Tier: " + brmTier);
            BRMTier.setHighlight("BRM Tier: ", "" +brmTier);
            BRMTier.setHighlightColors(t, h);
        }

        int currentBRM = (int) person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_CURRENT_STATS_KEY).computeEffective(0f);
        int limitBRM = (int) person.getStats().getDynamic().getMod(ba_variablemanager.BA_BRM_LIMIT_STATS_KEY).computeEffective(0f);
        LabelAPI BRM = text.addPara("BRM: " + limitBRM);
        BRM.setHighlight("BRM: ", "" +limitBRM);
        BRM.setHighlightColors(t,h);
        text.addSkillPanel(person, isAdmin);

        text.setFontInsignia();
    }
}

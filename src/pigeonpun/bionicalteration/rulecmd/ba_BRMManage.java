package pigeonpun.bionicalteration.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.Nex_BlueprintSwap;
import com.fs.starfarer.api.impl.campaign.rulecmd.PaginatedOptions;
import com.fs.starfarer.api.util.Misc;
import exerelin.utilities.StringHelper;
import pigeonpun.bionicalteration.ba_officermanager;

import java.util.List;
import java.util.Map;

public class ba_BRMManage extends PaginatedOptions {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);
        switch (arg) {
            case "displayOfficersList":
                displayOfficersList();
                break;
            case "upgrade":
                break;
        }
        return false;
    }
    public void displayOfficersList() {
        //Switch to Paginated Options
        originalPlugin = dialog.getPlugin();

        dialog.setPlugin(this);
        init(dialog);

        //add officers into the list
        dialog.getOptionPanel().clearOptions();
        List<PersonAPI> personList = ba_officermanager.getListOfficerFromFleet(null, true);

        int index = 0;
        for (PersonAPI person : personList)
        {
            //todo: set up list officers
            index++;
        }

        addOptionAllPages(StringHelper.getString("back", true), "nex_blueprintSwapMenuReturn");
    }
}

package pigeonpun.bionicalteration.listeners;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;

public class ba_salvagelistener implements ShowLootListener {
    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        //todo: salvage spawning
    }
}

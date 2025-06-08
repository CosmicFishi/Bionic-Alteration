package pigeonpun.bionicalteration.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.listeners.ShipRecoveryListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.apache.log4j.Logger;

import java.util.List;

public class ba_shiprecoverylistener implements ShipRecoveryListener {
    Logger log = Global.getLogger(ba_shiprecoverylistener.class);
    @Override
    public void reportShipsRecovered(List<FleetMemberAPI> ships, InteractionDialogAPI dialog) {
        log.info("aaaaaaaaaaaaaaaaaaaaa");
    }
}

package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ba_marketmanager {
    static Logger log = Global.getLogger(ba_marketmanager.class);

    public static void onSaveLoad() {
        setUpAdminForMarkets();
    }
    public static void onNewGameAfterEconomyLoad() {
        setUpAdminForMarkets();
    }
    public static void setUpAdminForMarkets() {
        if(!Global.getSector().getMemoryWithoutUpdate().contains(ba_variablemanager.BA_MARKET_ADMIN_SET_UP)) {
            SectorAPI sector = Global.getSector();
            List<MarketAPI> markets = sector.getEconomy().getMarketsCopy();
            log.info("Setting up bionics for NPC admins");
            for(MarketAPI market: markets) {
                if(market.getAdmin() != null && !market.getAdmin().isDefault() && !market.getAdmin().isAICore()) {
                    ba_officermanager.setUpListOfficers(Arrays.asList(market.getAdmin()));
                    //log.info("Setting up: " + market.getName() + " for " + market.getFaction() + " | Person tags: " + market.getAdmin().getTags());
                }
            }
            Global.getSector().getMemoryWithoutUpdate().set(ba_variablemanager.BA_MARKET_ADMIN_SET_UP, true);
        }
    }
}

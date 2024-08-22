package pigeonpun.bionicalteration.inventory;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.characters.PersonAPI;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.ba_limbmanager;
import pigeonpun.bionicalteration.ba_variablemanager;
import pigeonpun.bionicalteration.bionic.ba_bioniccontainer;
import pigeonpun.bionicalteration.bionic.ba_bionicitemplugin;
import pigeonpun.bionicalteration.overclock.ba_overclock;
import pigeonpun.bionicalteration.overclock.ba_overclockmanager;

import java.util.ArrayList;
import java.util.List;

public class ba_inventoryhandler {
    static Logger log = Global.getLogger(ba_inventoryhandler.class);
    /**
     * Only add persistent data, not override bionic persistent data
     * @return
     */
    public static void compressAllBionics() {
        if(Global.getSector().getPersistentData().get(ba_variablemanager.BA_BIONIC_CONTAINER_PERSISTENT_KEY) == null) {
            Global.getSector().getPersistentData().put(ba_variablemanager.BA_BIONIC_CONTAINER_PERSISTENT_KEY, Global.getFactory().createCargo(true));
        }
        //check if has container
        boolean hasContainer = false;
        for(CargoStackAPI stack: Global.getSector().getPlayerFleet().getCargo().getStacksCopy()) {
            if(stack.isSpecialStack() && stack.getPlugin() instanceof ba_bioniccontainer) {
                hasContainer = true;
                break;
            }
        }
        if(!hasContainer) {
            SpecialItemData specialItem = new SpecialItemData("ba_bionic_container", null);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(specialItem, 1);
        }

        //when the player open inventory
        //when person open overclock/bionic/ripper UI
        CargoAPI cargoCopy = Global.getFactory().createCargo(true);
        cargoCopy.addAll(Global.getSector().getPlayerFleet().getCargo());
        for(CargoStackAPI stack: cargoCopy.getStacksCopy()) {
            if(stack.isSpecialStack() && stack.getPlugin() instanceof ba_bionicitemplugin) {
                ba_bionicitemplugin bionic = (ba_bionicitemplugin) stack.getPlugin();
//                boolean removed = true;
                CargoStackAPI stackCopyOver = stack;
                boolean removed = removeBionicFromPersonCargo(bionic);
                if(removed) addToContainer(stackCopyOver);
            }
        }
        log.info("Finish compress");
    }
    public static CargoAPI uncompressAllBionics() {
        CargoAPI cargo = Global.getFactory().createCargo(true);
        if(getGlobalData() != null) {
            for(CargoStackAPI stack: getGlobalData().getStacksCopy()) {
                if(stack.getPlugin() != null) {
                    cargo.addFromStack(stack);
                }
            }
//            cargo = getGlobalData();
        }
        return cargo;
    }
    public static boolean removeBionicFromPersonCargo(ba_bionicitemplugin bionic) {
        boolean success = false;
        ba_overclock overclock = ba_overclockmanager.getOverclockFromItem(bionic);
        SpecialItemData specialItem = new SpecialItemData(bionic.bionicId, null);
        if(overclock != null) {
            specialItem = new SpecialItemData(bionic.bionicId, overclock.id);
        }
        success = Global.getSector().getPlayerFleet().getCargo().removeItems(CargoAPI.CargoItemType.SPECIAL, specialItem, 1);
        return success;
    }
    protected static void overrideGlobalData(CargoAPI cargo) {
        Global.getSector().getPersistentData().put(ba_variablemanager.BA_BIONIC_CONTAINER_PERSISTENT_KEY, cargo);
    }
    public static CargoAPI getGlobalData() {
        return (CargoAPI) Global.getSector().getPersistentData().get(ba_variablemanager.BA_BIONIC_CONTAINER_PERSISTENT_KEY);
    }
    public static void removeFromContainer(CargoStackAPI stack) {
        CargoAPI cargoFromMemory = getGlobalData();
        cargoFromMemory.removeItems(stack.getType(), stack.getSpecialDataIfSpecial(), stack.getSize());
//        cargoFromMemory.removeStack(stack);
        overrideGlobalData(cargoFromMemory);
    }

    /**
     * ONLY use this for the bionic item
     * @param bionic
     * @return
     */
    public static boolean removeFromContainer(ba_bionicitemplugin bionic) {
        boolean success = false;
        CargoAPI cargoFromMemory = getGlobalData();
        ba_overclock overclock = ba_overclockmanager.getOverclockFromItem(bionic);
        SpecialItemData specialItem = new SpecialItemData(bionic.bionicId, null);
        if(overclock != null) {
            specialItem = new SpecialItemData(bionic.bionicId, overclock.id);
        }
        success = cargoFromMemory.removeItems(CargoAPI.CargoItemType.SPECIAL, specialItem, 1);
//        cargoFromMemory.removeStack(stack);
        overrideGlobalData(cargoFromMemory);
        return success;
//        return success;
    }
    public static void addToContainer(CargoStackAPI stack) {
        CargoAPI cargoFromMemory = getGlobalData();
        cargoFromMemory.addFromStack(stack);
        overrideGlobalData(cargoFromMemory);
    }

    /**
     * For when removing a bionic from the person on the bionic table
     * @param bionic
     * @param person
     * @param limb
     */
    public static void addToContainer(ba_bionicitemplugin bionic, PersonAPI person, ba_limbmanager.ba_limb limb) {
        CargoAPI cargoFromMemory = getGlobalData();
//        cargoFromMemory.addFromStack(stack);
        SpecialItemData specialItem = new SpecialItemData(bionic.bionicId, null);
        if(person != null && limb != null) {
            ba_overclock overclock = ba_overclockmanager.getOverclockFromPerson(person, limb);
            if(overclock != null) {
                //require special item data to do the overclock things
                specialItem =  new SpecialItemData(bionic.bionicId, overclock.id);
            }
        }
        cargoFromMemory.addSpecial(specialItem, 1);
        overrideGlobalData(cargoFromMemory);
    }
}

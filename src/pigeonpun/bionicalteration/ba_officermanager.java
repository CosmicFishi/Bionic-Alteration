package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import org.apache.log4j.Logger;
import pigeonpun.bionicalteration.bionic.ba_bionicmanager;

import java.util.*;

import static pigeonpun.bionicalteration.ba_variantmanager.getAnatomyVariantTag;

/**
 * Handle how many bionic available on an officer
 * @author PigeonPun
 */
public class ba_officermanager {
    static Logger log = Global.getLogger(ba_officermanager.class);
    public static List<ba_bionicAugmentedData> getBionicAnatomyList(PersonAPI person) {
        if(getAnatomyVariantTag(person.getTags()).isEmpty()) {
            String randomVariant = ba_variantmanager.getRandomVariant();
            person.addTag(randomVariant);
            List<String> randomBionics = ba_bionicmanager.getRandomBionic();
            for (String random: randomBionics) {
                person.addTag(random);
            }
        }

        List<ba_bionicAugmentedData> anatomyList = new ArrayList<>();
        HashMap<ba_limbmanager.ba_limb, List<ba_bionicmanager.ba_bionic>> bionicsInstalledList = ba_bionicmanager.getListBionicInstalled(person);
        List<String> personGenericVariant = getAnatomyVariantTag(person.getTags());
        //return list with full limb details
//        log.info(bionicsInstalledList.size());
        for (String pGV: personGenericVariant) {
            List<String> variantAnatomy = ba_variantmanager.variantList.get(pGV);
            for (String limbString: variantAnatomy) {
                ba_limbmanager.ba_limb limb = ba_limbmanager.getLimb(limbString);
                if(bionicsInstalledList.get(limb) != null) {
                    List<ba_bionicmanager.ba_bionic> bionicsInstalled = bionicsInstalledList.get(limb);
                    anatomyList.add(new ba_bionicAugmentedData(limb, bionicsInstalled));
                } else {
                    anatomyList.add(new ba_bionicAugmentedData(limb, new ArrayList<ba_bionicmanager.ba_bionic>()));
                }
            }
        }
        return anatomyList;
    }
    public static class ba_bionicAugmentedData {
        public ba_limbmanager.ba_limb limb;
        public List<ba_bionicmanager.ba_bionic> bionicInstalled;
        ba_bionicAugmentedData(ba_limbmanager.ba_limb limb, List<ba_bionicmanager.ba_bionic> bionic) {
            this.limb = limb;
            this.bionicInstalled = bionic;
        }
    }
}

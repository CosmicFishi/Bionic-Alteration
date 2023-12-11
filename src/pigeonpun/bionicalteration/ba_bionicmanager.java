package pigeonpun.bionicalteration;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Handle bionic list/loading
 * Install tag: <BIONIC_TAG>:<LIMB_TAB>. Ex: bionic_id:hand_right
 */
public class ba_bionicmanager {
    static Logger log = Global.getLogger(ba_bionicmanager.class);
    protected static HashMap<String, ba_bionic> bionicMap = new HashMap<>();
    public ba_bionicmanager() {
        loadBionic();
    }
    public static void onApplicationLoad() {
        loadBionic();
    }
    //todo: change to get csv
    public static void loadBionic() {
        bionicMap.put("1",new ba_bionic("1",new ArrayList<String>(Arrays.asList("hand_left", "hand_right")),"Your mom hand", "idk", Color.RED.darker()));
        bionicMap.put("2",new ba_bionic("2",new ArrayList<String>(Arrays.asList("hand_right", "hand_left")),"Hand in Hand?", "idk", Color.YELLOW));
        bionicMap.put("3",new ba_bionic("3",new ArrayList<String>(Arrays.asList("eye_left", "eye_right")),"Smoll eye", "idk", Color.magenta));
        bionicMap.put("4",new ba_bionic("4",new ArrayList<String>(Arrays.asList("eye_left", "eye_right")),"Big eye", "idk", Color.green));
        bionicMap.put("5",new ba_bionic("5",new ArrayList<String>(Arrays.asList("brain")),"Big brainnnnn", "idk", Color.CYAN));
        bionicMap.put("6",new ba_bionic("6",new ArrayList<String>(Arrays.asList("heart")),"My heart <3", "idk", Color.red));
        bionicMap.put("7",new ba_bionic("7",new ArrayList<String>(Arrays.asList("hand_right", "hand_left")),"Big MF hand", "idk", Color.red));
        bionicMap.put("8",new ba_bionic("8",new ArrayList<String>(Arrays.asList("heart")),"Broken heart </3", "idk", Color.red));
        bionicMap.put("9",new ba_bionic("9",new ArrayList<String>(Arrays.asList("eye_left", "eye_right")),"Pirate eye, ARRRRR", "idk", Color.red));
        bionicMap.put("10",new ba_bionic("10",new ArrayList<String>(Arrays.asList("brain")),"ANother fking brain to understand WTF did i code", "idk", Color.red));
        bionicMap.put("11",new ba_bionic("11",new ArrayList<String>(Arrays.asList("brain")),"Another another brain", "idk", Color.red));
    }
    public static ba_bionic getBionic(String id) {
        ba_bionic bionic = bionicMap.get(id);
        if(bionic == null) {
            log.error("Can not find bionic of id: "+ id);
        }
        return bionic;
    }
    public static List<String> getListStringBionicInstalled(PersonAPI person) {
        List<String> bionics = new ArrayList<>();
        if (!person.getTags().isEmpty()) {
            for (String tag: person.getTags()) {
                if(bionicMap.get(tag) != null) {
                    bionics.add(tag);
                }
            }
        }
        return bionics;
    }
    public static HashMap<ba_anatomymanager.ba_limb, List<ba_bionic>> getListBionicInstalled(PersonAPI person) {
        HashMap<ba_anatomymanager.ba_limb, List<ba_bionic>> bionicsInstalledList = new HashMap<>();
        if (!person.getTags().isEmpty()) {
            for (String tag: person.getTags()) {
                if(tag.contains(":")) {
                    String[] tokens = tag.split(":");
                    ba_bionic bionicInstalled = bionicMap.get(tokens[0]);
                    if(bionicInstalled == null) log.error("Can't find bionic of tag: " + tokens[0]);
                    ba_anatomymanager.ba_limb sectionInstalled = ba_anatomymanager.getLimb(tokens[1]);
                    if(sectionInstalled == null) log.error("Can't find section of tag: " + tokens[1]);
                    if(bionicsInstalledList.get(sectionInstalled) != null) {
                        bionicsInstalledList.get(sectionInstalled).add(bionicInstalled);
                    } else {
                        bionicsInstalledList.put(sectionInstalled, new ArrayList<ba_bionic>(Arrays.asList(bionicInstalled)));
                    }
                }
            }
        }
        return bionicsInstalledList;
    }
    public static List<String> getRandomBionic() {
        List<String> randomBionic = new ArrayList<>();
        WeightedRandomPicker<String> random = new WeightedRandomPicker<>();
        random.addAll(getListBionicKeys());
        int i = 0;
        int maxNumberOfRandomBionic = 6;
        while(i < maxNumberOfRandomBionic) {
            String picked = random.pick();
            random.remove(picked);
            ba_bionic bionic = getBionic(picked);
            WeightedRandomPicker<String> randomSectionPicker = new WeightedRandomPicker<>();
            randomSectionPicker.addAll(bionic.bionicSectionId);
            randomBionic.add(picked+":"+ randomSectionPicker.pick());
            i++;
        }
        return randomBionic;
    }
    public static List<String> getListBionicKeys() {
        return new ArrayList<>(bionicMap.keySet());
    }
    public static class ba_bionic {
        public String bionicId;
        public List<String> bionicSectionId;
        public String name;
        public String effect;
        public Color displayColor;
        public HashMap<String, Object> customData = new HashMap<>();
        public ba_bionic(String bionicId, List<String> bionicSectionId, String name, String effect, Color displayColor) {
            this.bionicId = bionicId;
            this.bionicSectionId = bionicSectionId;
            this.name = name;
            this.effect = effect;
            this.displayColor = displayColor;
        }
    }
}

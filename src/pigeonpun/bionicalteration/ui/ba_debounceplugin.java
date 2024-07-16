package pigeonpun.bionicalteration.ui;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ba_debounceplugin {
    Logger log = Global.getLogger(ba_debounceplugin.class);
    //the list of tooltip keeping track of the position to determine if debounce over or not
    public static HashMap<String, scrollPos> debounceList = new HashMap<>();
    public static float debounceMaxDur = 20f;
    public static float debounceTimer = 0;
    public void addToList(String tooltipId) {
        if(!debounceList.containsKey(tooltipId)) {
            debounceList.put(tooltipId, new scrollPos(0,0));
        }
    }
    public boolean isDebounceOver(String id, float currentX, float currentY) {
        if(debounceList.containsKey(id)) {
            float prevX = debounceList.get(id).prevX;
            float prevY = debounceList.get(id).prevY;
            if(prevX != currentX || prevY != currentY) {
                debounceList.get(id).prevX = currentX;
                debounceList.get(id).prevY = currentY;
                debounceTimer = 0;
            } else {
                if(debounceTimer < debounceMaxDur) {
                    debounceTimer += 1;
                }
            }
            return debounceTimer == debounceMaxDur;
        } else {
            log.error("Debouncing for a Id that isnt on the debounce list: " + id);
        }
        return false;
    }

    private class scrollPos {
        public float prevX;
        public float prevY;
        public scrollPos(float currentX, float currentY){
            this.prevX = currentX;
            this.prevY = currentY;
        }
    }
}

package pigeonpun.bionicalteration.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

public class ba_stringhelper {
    public static String getString(String category, String id, boolean ucFirst) {
        String str = "";
        if (id == null) return str;
        try {
            str = Global.getSettings().getString(category, id);
        }
        catch (Exception ex)
        {
            // could be a string not found
            //str = ex.toString();  // looks really silly
            Global.getLogger(ba_stringhelper.class).warn(ex);
            return "[INVALID]" + id;
        }
        if (ucFirst) str = Misc.ucFirst(str);
        return str;
    }

    public static String getString(String category, String id) {
        return getString(category, id, false);
    }

    public static String getString(String id, boolean ucFirst) {
        return getString("general", id, ucFirst);
    }

    public static String getString(String id) {
        return getString("general", id, false);
    }
}

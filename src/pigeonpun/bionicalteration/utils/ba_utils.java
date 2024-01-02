package pigeonpun.bionicalteration.utils;

import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ba_utils {
    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * this will draw a box at x,y with width w and heigh h
     * remember OpenGL Bottom-Left is 0,0
     */
    public static void drawBox(int x, int y, int w, int h, float alphaMult, Color color) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        Color color = new Color(241, 197, 4);
        GL11.glColor4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 0.3f * alphaMult);
        for (int i=0; i<4; i++) {
            GL11.glBegin(GL11.GL_LINE_LOOP);
            {
                GL11.glVertex2f(x, y);
                GL11.glVertex2f(x + w, y);
                GL11.glVertex2f(x + w, y + h);
                GL11.glVertex2f(x, y + h);
            }
        }
        GL11.glEnd();
        GL11.glPopMatrix();
    }
    public static void drawLine(int x, int y, float alphaMult) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Color color = new Color(241, 197, 4);
        GL11.glColor4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 0.3f * alphaMult);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glPopMatrix();
    }
    public static List<String> trimAndSplitString(String string) {
        List<String> listString = new ArrayList<>();
        for (String s: string.split(",")) {
            listString.add(s.trim());
        }
        return listString;
    }

    /**
     * Serenity Neural Enhancement => S.N.E.
     * @param fullBionicName
     * @return
     */
    public static String getShortenBionicName(String fullBionicName) {
        String[] listString = fullBionicName.split(" ");
        StringBuilder returnString = new StringBuilder();
        for(String s: listString) {
            returnString.append(s.toUpperCase().charAt(0)).append(".");
        }
        if(returnString.length() > 1) returnString.setLength(returnString.length() - 1);
        return returnString.toString();
    }
}

package pigeonpun.bionicalteration.utils;

import org.lwjgl.opengl.GL11;

import java.awt.*;

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
}

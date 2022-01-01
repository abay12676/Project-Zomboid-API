package dev.zomboid.api;

import lombok.experimental.UtilityClass;
import zombie.ui.TextManager;

/**
 * Provides access to the rendering functionality within the game.
 */
@UtilityClass
public class Rendering {

    /**
     * Draws some text over the game window.
     */
    public static void text(String text, float x, float y, float r, float g, float b, float a) {
        TextManager.instance.DrawString(x, y, text, r, g, b, a);
    }

}

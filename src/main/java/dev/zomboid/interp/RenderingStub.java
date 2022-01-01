package dev.zomboid.interp;

import dev.zomboid.ZomboidApi;
import dev.zomboid.api.Rendering;
import lombok.experimental.UtilityClass;
import zombie.core.Core;
import zombie.ui.UIManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.zomboid.ZomboidApi.core;

/**
 * Provides interpolation with the game's rendering code.
 */
@UtilityClass
public class RenderingStub {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Determines if the API is ready to be initialized.
     */
    private static boolean readyToInit() {
        return !UIManager.UI.isEmpty();
    }

    /**
     * Called before the game runs Core#EndFrameUI.
     *
     * @param self The object the method was called on.
     */
    public static void endFrameUi(Core self) {
        Rendering.text(ZomboidApi.DISPLAY_NAME, 15, 15, 1.0f, 0.0f, 0.0f, 1.0f);

        if (readyToInit()) {
            if (initialized.compareAndSet(false, true)) {
                core.initClient();
            }

            core.update();
        }
    }

}

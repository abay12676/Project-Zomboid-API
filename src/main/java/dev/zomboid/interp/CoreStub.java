package dev.zomboid.interp;

import static dev.zomboid.ZomboidApi.core;

/**
 * Provides interpolation with the game's core code.
 */
public class CoreStub {

    /**
     * Called before GameServer#main runs.
     */
    public static void serverMain() {
        core.initServer();
    }

}

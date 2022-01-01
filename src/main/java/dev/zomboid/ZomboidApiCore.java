package dev.zomboid;

import com.google.gson.Gson;
import dev.zomboid.cheat.CheatWindow;
import zombie.debug.DebugLog;
import zombie.ui.UIManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides core functionality for the API.
 */
public class ZomboidApiCore {

    /**
     * The active anti-cheat configuration. This can be modified by changing 'anticheat.json' in the
     * game's directory.
     */
    public AntiCheatCfg antiCheatCfg;

    /**
     * The anti-cheat instance to use for moderating the server, if the server is running.
     */
    public AntiCheat antiCheat;

    /**
     * The cheat window, which we use for testing our exploit protections.
     */
    private CheatWindow window;

    /**
     * Initializes the server functionality.
     */
    public void initServer() {
        antiCheatCfg = new AntiCheatCfg();
        antiCheat = new AntiCheat();

        Path cfgPath = Paths.get("anticheat.json");
        if (Files.exists(cfgPath)) {
            try {
                antiCheatCfg = new Gson().fromJson(Files.readString(cfgPath), AntiCheatCfg.class);
                antiCheat.setCfg(antiCheatCfg);
            } catch (IOException e) {
                DebugLog.log("Failed to parse anticheat configuration file 'anticheat.json'");
            }
        } else {
            DebugLog.log("Failed to find anticheat configuration file 'anticheat.json'");
        }
    }

    /**
     * Initializes the client functionality.
     */
    public void initClient() {
        window = new CheatWindow();
        window.ResizeToFitY = false;
    }

    /**
     * Updates the API core.
     */
    public void update() {
        if (!UIManager.UI.contains(window)) {
            UIManager.UI.add(window);
        }

        window.visible = true;
        window.setEnabled(true);
    }

}

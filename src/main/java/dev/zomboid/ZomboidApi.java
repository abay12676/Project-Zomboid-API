package dev.zomboid;

import com.google.gson.Gson;
import dev.zomboid.cheat.CheatWindow;
import lombok.experimental.UtilityClass;
import zombie.debug.DebugLog;
import zombie.network.PacketTypes;
import zombie.ui.UIManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ZomboidApi {


    /**
     * The display name on the UI.
     */
    public static final String DISPLAY_NAME = "Zomboid Java API";

    /**
     * The core instance for the API's functionality.
     */
    public static final ZomboidApiCore core = new ZomboidApiCore();

}

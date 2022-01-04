package dev.zomboid;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ZomboidApi {

    /**
     * The current version.
     */
    public static final String VERSION = "0.0.3";

    /**
     * The display name on the UI.
     */
    public static final String DISPLAY_NAME = "Zomboid Java API";

    /**
     * The core instance for the API's functionality.
     */
    public static final ZomboidApiCore core = new ZomboidApiCore();

}

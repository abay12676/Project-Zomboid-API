package dev.zomboid;

/**
 * Various actions that can be performed when an anti-cheat violation
 * has been detected.
 */
public enum AntiCheatAction {

    /**
     * Only sends a discord alert.
     */
    ALERT,
    /**
     * Disconnects ths user.
     */
    DISCONNECT,
    /**
     * Bans the user's steam id.
     */
    BAN_STEAM_ID,
    /**
     * Bans the user's ip address.
     */
    BAN_IP,
    /**
     * Bans the user's steam id, and ip address.
     */
    BAN_ALL,

}

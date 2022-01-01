package dev.zomboid;

import lombok.Data;
import zombie.network.PacketTypes;

import java.util.LinkedList;
import java.util.List;

@Data
public class AntiCheatCfg {

    /**
     * The link to the discord API to report to.
     */
    public String discordApi = "";

    /**
     * If true, the player ids in packets will be overwritten with the player id from the UDP connection
     * if a valid player id is not found in the UDP connection's player array.
     */
    private boolean enforcePacketOwnership = true;

    /**
     * If true, the sync perks packet will be validated to ensure no cheating is detected.
     */
    private boolean enforceSyncPerks = false;

    /**
     * If true, teleports will cause violations unless the player is a mod/admin.
     */
    private boolean enforceTeleport = true;

    /**
     * If true, extra info packets will cause violations unless the player is a mod/admin.
     */
    private boolean enforceExtraInfo = true;

    /**
     * If true, player death packets must be a player associated with the network connection or a violation will be caused.
     */
    private boolean enforcePlayerDeaths = true;

    /**
     * The difference in value that a perk change must hit in order to cause a violation.
     */
    private int enforceSyncThreshold = 10;

    /**
     * If true, additional pain packets will cause a violation.
     */
    private boolean enforceAdditionalPain = true;

    /**
     * If rate limiting is enabled.
     */
    private boolean rateLimiting = true;

    /**
     * A list of rate limits to apply.
     */
    private List<AntiCheatRateLimit> rateLimits = new LinkedList<>();

    public boolean isRateLimited(short type) {
        if (!rateLimiting) {
            return false;
        }

        for (AntiCheatRateLimit rl : rateLimits) {
            PacketTypes.PacketType t = PacketTypes.PacketType.valueOf(rl.getType());
            if (t.getId() == type) {
                return true;
            }
        }
        return false;
    }

    public long getRateLimit(short type) {
        for (AntiCheatRateLimit rl : rateLimits) {
            PacketTypes.PacketType t = PacketTypes.PacketType.valueOf(rl.getType());
            if (t.getId() == type) {
                return rl.getDelay();
            }
        }
        return 0;
    }
}

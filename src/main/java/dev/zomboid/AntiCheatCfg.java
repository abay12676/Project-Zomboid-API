package dev.zomboid;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import zombie.network.PacketTypes;

import java.util.LinkedList;
import java.util.List;

@Data
public class AntiCheatCfg {

    /**
     * The link to the discord API to report to.
     */
    @SerializedName("discordApi")
    public String discordApi = "";

    /**
     * The sync perks packet will be validated to ensure no cheating is detected.
     */
    @SerializedName("syncPerksRule")
    private AntiCheatSyncPerksRule syncPerksRule = new AntiCheatSyncPerksRule(false);

    /**
     * Teleports will cause violations unless the player is a mod/admin.
     */
    @SerializedName("teleportRule")
    private AntiCheatRule teleportRule = new AntiCheatRule(false);

    /**
     * Extra info packets will cause violations unless the player is a mod/admin.
     */
    @SerializedName("extraInfoRule")
    private AntiCheatRule extraInfoRule = new AntiCheatRule(true);

    /**
     * Player death packets must be a player associated with the network connection or a violation will be caused.
     */
    @SerializedName("playerDeathsRule")
    private AntiCheatRule playerDeathsRule = new AntiCheatRule(false);

    /**
     * Additional pain packets will cause a violation.
     */
    @SerializedName("additionalPainRule")
    private AntiCheatRule additionalPainRule = new AntiCheatRule(false);

    /**
     * Player clothing synchronization requests must be associated with the network connection or a violation
     * will be caused.
     */
    @SerializedName("syncClothingRule")
    private AntiCheatRule syncClothingRule = new AntiCheatRule(false);

    /**
     * Checks will be put in various places such as interacting with other players,
     * zombies, objects, etc. Being too far away when interacting will cause a violation.
     */
    @SerializedName("distanceRule")
    private AntiCheatDistanceRule distanceRule = new AntiCheatDistanceRule(false);

    /**
     * Checks will be put in various places in order to ensure that chat messages are
     * received with a valid username attached.
     */
    @SerializedName("chatRule")
    private AntiCheatRule chatRule = new AntiCheatRule(false);

    /**
     * A list of rate limits to apply.
     */
    @SerializedName("rateLimits")
    private List<AntiCheatRateLimit> rateLimits = new LinkedList<>();

    public boolean isRateLimited(short type) {
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

    @Override
    public String toString() {
        return "AntiCheatCfg{" +
                "syncPerksRule=" + syncPerksRule +
                "\n teleportRule=" + teleportRule +
                "\n extraInfoRule=" + extraInfoRule +
                "\n playerDeathsRule=" + playerDeathsRule +
                "\n additionalPainRule=" + additionalPainRule +
                "\n syncClothingRule=" + syncClothingRule +
                "\n distanceRule=" + distanceRule +
                "\n chatRule=" + chatRule +
                "\n rateLimits=" + rateLimits +
                '}';
    }
}

package dev.zomboid;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * A rate limit to be applied by the anti-cheat.
 */
@Data
public class AntiCheatRateLimit {

    /**
     * The name of the enum field that represents the packet
     * to rate limit. These can be found in {@link zombie.network.PacketTypes.PacketType}.
     */
    @SerializedName("type")
    private String type;

    /**
     * The number of milliseconds a player must wait in between packets.
     */
    @SerializedName("delay")
    private long delay;

}

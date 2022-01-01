package dev.zomboid;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AntiCheatRateLimit {

    @SerializedName("type")
    private String type;

    @SerializedName("delay")
    private long delay;

}

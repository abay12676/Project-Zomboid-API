package dev.zomboid;

import lombok.Data;

@Data
public class AntiCheatRateLimit {

    private String type;
    private long delay;

}

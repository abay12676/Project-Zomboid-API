package dev.zomboid;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Contains various configuration options for a base anti-cheat rule.
 */
@Data
public class AntiCheatRule {

    /**
     * If the rule is enabled.
     */
    @SerializedName("enabled")
    private boolean enabled;

    /**
     * The action to perform if the rule is violated.
     */
    @SerializedName("action")
    private AntiCheatAction action;

    public AntiCheatRule(boolean enabled) {
        this.enabled = enabled;
        this.action = AntiCheatAction.DISCONNECT;
    }

    public AntiCheatRule(boolean enabled, AntiCheatAction action) {
        this.enabled = enabled;
        this.action = action;
    }

    @Override
    public String toString() {
        return "AntiCheatRule{" +
                "enabled=" + enabled +
                ", action=" + action +
                '}';
    }
}

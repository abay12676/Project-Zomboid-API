package dev.zomboid;

import lombok.Getter;
import lombok.Setter;

/**
 * The distance rule. Used to enforce player interactions are within a certain distance.
 */
public class AntiCheatDistanceRule extends AntiCheatRule {

    /**
     * The distance the player must be in, in order for the interaction to be valid.
     */
    @Getter
    @Setter
    private float threshold = 10.f;

    public AntiCheatDistanceRule(boolean enabled) {
        super(enabled);
    }

    public AntiCheatDistanceRule(boolean enabled, AntiCheatAction action) {
        super(enabled, action);
    }

    @Override
    public String toString() {
        return "AntiCheatDistanceRule{" +
                "enabled=" + isEnabled() +
                ", action=" + getAction() +
                ", threshold=" + threshold +
                '}';
    }
}

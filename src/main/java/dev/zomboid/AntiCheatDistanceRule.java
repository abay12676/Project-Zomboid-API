package dev.zomboid;

import lombok.Getter;
import lombok.Setter;

public class AntiCheatDistanceRule extends AntiCheatRule {

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

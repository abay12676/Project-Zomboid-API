package dev.zomboid;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class AntiCheatSyncPerksRule extends AntiCheatRule {

    @Getter
    @Setter
    private int threshold = 10;

    public AntiCheatSyncPerksRule(boolean enabled) {
        super(enabled);
    }

    public AntiCheatSyncPerksRule(boolean enabled, AntiCheatAction action) {
        super(enabled, action);
    }

    @Override
    public String toString() {
        return "AntiCheatSyncPerksRule{" +
                "enabled=" + isEnabled() +
                ", action=" + getAction() +
                ", threshold=" + threshold +
                '}';
    }
}

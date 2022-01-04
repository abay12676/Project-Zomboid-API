package dev.zomboid;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * The synchronize perks rule. Used to enforce the player's perk values do not change too much at once.
 */
public class AntiCheatSyncPerksRule extends AntiCheatRule {

    /**
     * The maximum difference between the new value and the old value, in order for the sync to be valid.
     */
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

package dev.zomboid;

import lombok.Getter;

/**
 * A generic rate limiter.
 */
public class RateLimiter {

    /**
     * The required delay.
     */
    @Getter
    private final long delay;

    /**
     * The next allowed system time.
     */
    @Getter
    private long nextAllowedTime = 0;

    public RateLimiter(long delay) {
        this.delay = delay;
    }

    /**
     * Checks if a request is allowed.
     */
    public boolean check() {
        long t = System.currentTimeMillis();
        boolean allowed = t > nextAllowedTime;
        if (allowed) {
            nextAllowedTime = (t + delay);
        }
        return allowed;
    }
}

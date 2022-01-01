package dev.zomboid;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A simple annotation to let us know something has been injected into.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Injected {
}

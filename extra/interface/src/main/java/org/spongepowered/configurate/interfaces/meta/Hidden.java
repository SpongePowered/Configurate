package org.spongepowered.configurate.interfaces.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows you to read the config node as normal, but it only
 * writes the node when the value is not the default value. This is to ensure
 * that when a user manually adds the entry, it remains there (as long as it's
 * not the default value.)
 *
 * <p>Without a default value the annotated node will be read, but will never
 * be written even if the user explicitly added it to their config.</p>
 *
 * @since 4.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Hidden {
}

package org.spongepowered.configurate.interfaces.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates a field which makes the annotated method act as a simple getter /
 * setter without being handled as a config node.
 *
 * @since 4.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Field {
}

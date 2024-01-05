package org.spongepowered.configurate.interfaces.meta.defaults;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides a default value for the annotated method.
 * Because of annotation limits, there is an annotation for:
 * {@link DefaultBoolean booleans}, {@link DefaultDecimal decimals},
 * {@link DefaultNumeric numerics} and {@link DefaultString Strings}.
 *
 * @since 4.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface DefaultBoolean {

    /**
     * The default value for the annotated method.
     *
     * @return the default value for the annotated method.
     * @since 4.2.0
     */
    boolean value() default false;

}

package org.spongepowered.configurate.interfaces.meta.range;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation limits the values that a config node can have.
 * Because of annotation limits, there is an annotation for:
 * {@link DecimalRange decimals}, {@link NumericRange numerics} and
 * {@link StringRange String length}.
 *
 * <p>When the String is null, the range validation is skipped. Use
 * {@link org.spongepowered.configurate.objectmapping.meta.Required Required}
 * if null shouldn't be allowed.</p>
 *
 * @since 4.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface StringRange {
    /**
     * The minimal String length allowed (inclusive.)
     *
     * @return the minimal value allowed (inclusive.)
     * @since 4.2.0
     */
    int from();

    /**
     * The maximal String length allowed (inclusive.)
     *
     * @return the maximal value allowed (inclusive.)
     * @since 4.2.0
     */
    int to();

}

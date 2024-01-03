package org.spongepowered.configurate.interfaces.processor;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.FieldSpec;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultBoolean;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultDecimal;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultString;

final class DefaultAnnotations {

    private DefaultAnnotations() {}

    static void process(final ExecutableElement element, final TypeMirror nodeType, final FieldSpec.Builder fieldSpec) {
        final @Nullable DefaultBoolean defaultBoolean = element.getAnnotation(DefaultBoolean.class);
        final @Nullable DefaultDecimal defaultDecimal = element.getAnnotation(DefaultDecimal.class);
        final @Nullable DefaultNumeric defaultNumeric = element.getAnnotation(DefaultNumeric.class);
        final @Nullable DefaultString defaultString = element.getAnnotation(DefaultString.class);
        //noinspection ConstantValue not everything is nonnull by default
        final boolean hasDefault = defaultBoolean != null || defaultDecimal != null || defaultNumeric != null || defaultString != null;

        @Nullable Object defaultValue = null;
        boolean isString = false;
        //noinspection ConstantValue
        if (hasDefault) {
            if (MoreTypes.isTypeOf(Boolean.TYPE, nodeType)) {
                //noinspection ConstantValue
                if (defaultBoolean == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                defaultValue = defaultBoolean.value();
            }

            if (Utils.isDecimal(nodeType)) {
                //noinspection ConstantValue
                if (defaultDecimal == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                defaultValue = defaultDecimal.value() + (MoreTypes.isTypeOf(Float.TYPE, nodeType) ? "F" : "D");
            }

            if (Utils.isNumeric(nodeType)) {
                //noinspection ConstantValue
                if (defaultNumeric == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                defaultValue = defaultNumeric.value();
                if (MoreTypes.isTypeOf(Long.TYPE, nodeType)) {
                    defaultValue += "L";
                }
            }

            if (MoreTypes.isTypeOf(String.class, nodeType)) {
                //noinspection ConstantValue
                if (defaultString == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                defaultValue = defaultString.value();
                isString = true;
            }
        }

        if (defaultValue != null) {
            fieldSpec.initializer(isString ? "$S" : "$L", defaultValue);
        }
    }

}

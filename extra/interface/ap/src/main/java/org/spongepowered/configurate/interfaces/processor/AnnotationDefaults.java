package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.Utils.annotation;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultBoolean;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultDecimal;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultString;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class AnnotationDefaults implements AnnotationProcessor {

    static final AnnotationDefaults INSTANCE = new AnnotationDefaults();

    private AnnotationDefaults() {}

    @Override
    public Set<Class<? extends Annotation>> processes() {
        return new HashSet<>(Arrays.asList(DefaultBoolean.class, DefaultDecimal.class, DefaultNumeric.class, DefaultString.class));
    }

    @Override
    public void process(
            final ExecutableElement element,
            final TypeMirror nodeType,
            final FieldSpecBuilderTracker fieldSpec
    ) throws IllegalStateException {
        final @Nullable DefaultBoolean defaultBoolean = annotation(element, DefaultBoolean.class);
        final @Nullable DefaultDecimal defaultDecimal = annotation(element, DefaultDecimal.class);
        final @Nullable DefaultNumeric defaultNumeric = annotation(element, DefaultNumeric.class);
        final @Nullable DefaultString defaultString = annotation(element, DefaultString.class);
        final boolean hasDefault = defaultBoolean != null || defaultDecimal != null || defaultNumeric != null || defaultString != null;

        @Nullable Object defaultValue = null;
        boolean isString = false;
        if (hasDefault) {
            if (MoreTypes.isTypeOf(Boolean.TYPE, nodeType)) {
                if (defaultBoolean == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                defaultValue = defaultBoolean.value();
                buildAndAddAnnotation(fieldSpec, DefaultBoolean.class, defaultValue, false);

            } else if (Utils.isDecimal(nodeType)) {
                if (defaultDecimal == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                defaultValue = defaultDecimal.value() + (MoreTypes.isTypeOf(Float.TYPE, nodeType) ? "F" : "D");
                buildAndAddAnnotation(fieldSpec, DefaultDecimal.class, defaultValue, false);

            } else if (Utils.isNumeric(nodeType)) {
                if (defaultNumeric == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                defaultValue = defaultNumeric.value();
                if (MoreTypes.isTypeOf(Long.TYPE, nodeType)) {
                    defaultValue += "L";
                }
                buildAndAddAnnotation(fieldSpec, DefaultNumeric.class, defaultValue, false);

            } else if (MoreTypes.isTypeOf(String.class, nodeType)) {
                if (defaultString == null) {
                    throw new IllegalStateException("A default value of the incorrect type was provided for " + element);
                }
                defaultValue = defaultString.value();
                isString = true;
                buildAndAddAnnotation(fieldSpec, DefaultString.class, defaultValue, true);
            }
        }

        if (defaultValue != null) {
            fieldSpec.initializer(isString ? "$S" : "$L", defaultValue);
        }
    }

    private void buildAndAddAnnotation(
        final FieldSpecBuilderTracker fieldSpec,
        final Class<? extends Annotation> annotationClass,
        final Object value,
        final boolean isString
    ) {
        fieldSpec.addAnnotation(
            AnnotationSpec.builder(annotationClass)
                .addMember("value", CodeBlock.of(isString ? "$S" : "$L", value))
        );
    }

}

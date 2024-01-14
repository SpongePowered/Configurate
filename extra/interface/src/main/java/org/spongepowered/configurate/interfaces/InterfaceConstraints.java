package org.spongepowered.configurate.interfaces;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.interfaces.meta.Hidden;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultBoolean;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultDecimal;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultString;
import org.spongepowered.configurate.interfaces.meta.range.DecimalRange;
import org.spongepowered.configurate.interfaces.meta.range.NumericRange;
import org.spongepowered.configurate.interfaces.meta.range.StringRange;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Constraint;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.serialize.SerializationException;

final class InterfaceConstraints {

    private InterfaceConstraints() {
    }

    static ObjectMapper.Factory buildObjectMapperWithConstraints() {
        return ObjectMapper.factoryBuilder()
            .addConstraint(DecimalRange.class, Number.class, decimalRange())
            .addConstraint(NumericRange.class, Number.class, numericRange())
            .addConstraint(StringRange.class, String.class, stringRange())
            .addProcessor(Hidden.class, hiddenProcessor())
            .build();
    }

    private static Constraint.Factory<DecimalRange, Number> decimalRange() {
        return (data, type) -> number -> {
            // null requirement is part of @Required
            if (number == null) {
                return;
            }

            final double value = number.doubleValue();
            if (!(data.from() >= value && data.to() <= value)) {
                throw new SerializationException(String.format(
                    "'%s' is not in the allowed range of from: %s, to: %s!",
                    value, data.from(), data.to()
                ));
            }
        };
    }

    private static Constraint.Factory<NumericRange, Number> numericRange() {
        return (data, type) -> number -> {
            // null requirement is part of @Required
            if (number == null) {
                return;
            }

            final long value = number.longValue();
            if (!(data.from() >= value && data.to() <= value)) {
                throw new SerializationException(String.format(
                    "'%s' is not in the allowed range of from: %s, to: %s!",
                    value, data.from(), data.to()
                ));
            }
        };
    }

    private static Constraint.Factory<StringRange, String> stringRange() {
        return (data, type) -> string -> {
            // null requirement is part of @Required
            if (string == null) {
                return;
            }

            final int length = string.length();
            if (!(data.from() >= length && data.to() <= length)) {
                throw new SerializationException(String.format(
                    "'%s' is not in the allowed string length range of from: %s, to: %s!",
                    length, data.from(), data.to()
                ));
            }
        };
    }

    @SuppressWarnings("ConstantValue")
    private static Processor.AdvancedFactory<Hidden, Object> hiddenProcessor() {
        return (ignored, fieldType, element) -> {
            // prefetch everything we can
            final @Nullable DefaultBoolean defaultBoolean = element.getAnnotation(DefaultBoolean.class);
            final @Nullable DefaultDecimal defaultDecimal = element.getAnnotation(DefaultDecimal.class);
            final @Nullable DefaultNumeric defaultNumeric = element.getAnnotation(DefaultNumeric.class);
            final @Nullable DefaultString defaultString = element.getAnnotation(DefaultString.class);
            final boolean isBoolean = TypeUtils.isBoolean(fieldType);
            final boolean isDecimal = TypeUtils.isDecimal(fieldType);
            final boolean isNumeric = TypeUtils.isNumeric(fieldType);
            final boolean isString = String.class == fieldType;

            // unfortunately default methods cannot be supported in combination with Hidden in Configurate

            return (value, destination) -> {
                // hidden fields are only absent from the config if the value is the default value, so we check that below
                if (isBoolean && defaultBoolean != null) {
                    if (!value.equals(defaultBoolean.value())) {
                        return;
                    }
                } else if (isDecimal && defaultDecimal != null) {
                    if (((Number) value).doubleValue() != defaultDecimal.value()) {
                        return;
                    }
                } else if (isNumeric && defaultNumeric != null) {
                    if (((Number) value).longValue() != defaultNumeric.value()) {
                        return;
                    }
                } else if (isString && defaultString != null) {
                    if (!defaultString.value().equals(value)) {
                        return;
                    }
                }

                // as long as it uses the naming scheme-based resolver parent should be the object holding the field and
                // key the field type
                //noinspection DataFlowIssue
                destination.parent().removeChild(destination.key());
            };
        };
    }

}

package org.spongepowered.configurate.interfaces;

import org.spongepowered.configurate.interfaces.meta.Hidden;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultBoolean;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultDecimal;
import org.spongepowered.configurate.interfaces.meta.defaults.DefaultNumeric;
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

    private static Processor.AdvancedFactory<Hidden, Object> hiddenProcessor() {
        return (ignored, fieldType, element) -> (value, destination) -> {
            // hidden fields are only not part of the config if the value is the default value, so we check that below
            if (TypeUtils.isBoolean(fieldType) && element.isAnnotationPresent(DefaultBoolean.class)) {
                if (!value.equals(element.getAnnotation(DefaultBoolean.class).value())) {
                    return;
                }
            } else if (TypeUtils.isDecimal(fieldType) && element.isAnnotationPresent(DefaultDecimal.class)) {
                if (((Number) value).doubleValue() != element.getAnnotation(DefaultDecimal.class).value()) {
                    return;
                }
            } else if (TypeUtils.isNumeric(fieldType) && element.isAnnotationPresent(DefaultNumeric.class)) {
                if (((Number) value).longValue() != element.getAnnotation(DefaultNumeric.class).value()) {
                    return;
                }
            }

            // as long as it uses the naming scheme-based resolver parent should be the object holding the field and
            // key the field type
            //noinspection DataFlowIssue
            destination.parent().removeChild(destination.key());
        };
    }

}

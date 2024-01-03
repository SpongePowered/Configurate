package org.spongepowered.configurate.interfaces;

import org.spongepowered.configurate.interfaces.meta.range.DecimalRange;
import org.spongepowered.configurate.interfaces.meta.range.NumericRange;
import org.spongepowered.configurate.interfaces.meta.range.StringRange;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Constraint;
import org.spongepowered.configurate.serialize.SerializationException;

final class InterfaceConstraints {

    private InterfaceConstraints() {
    }

    static ObjectMapper.Factory buildObjectMapperWithConstraints() {
        return ObjectMapper.factoryBuilder()
            .addConstraint(DecimalRange.class, Number.class, decimalRange())
            .addConstraint(NumericRange.class, Number.class, numericRange())
            .addConstraint(StringRange.class, String.class, stringRange())
            .build();
    }

    private static Constraint.Factory<DecimalRange, Number> decimalRange() {
        return (data, type) -> number -> {
            // Null requirement is part of @Required
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
            // Null requirement is part of @Required
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
            // Null requirement is part of @Required
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

}

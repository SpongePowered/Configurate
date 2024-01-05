package org.spongepowered.configurate.interfaces;

import static io.leangen.geantyref.GenericTypeReflector.box;

import java.lang.reflect.Type;

final class TypeUtils {

    private TypeUtils() {}

    static boolean isNumeric(final Type type) {
        final Type boxed = box(type);
        return Byte.class.equals(boxed) || Character.class.equals(boxed) || Short.class.equals(boxed)
            || Integer.class.equals(boxed) || Long.class.equals(boxed);
    }

    static boolean isDecimal(final Type type) {
        final Type boxed = box(type);
        return Float.class.equals(boxed) || Double.class.equals(boxed);
    }

    static boolean isBoolean(final Type type) {
        return Boolean.class.equals(box(type));
    }

}

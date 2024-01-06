package org.spongepowered.configurate.interfaces;

import java.util.Arrays;

final class TypeUtils {

    private TypeUtils() {}

    static <T> Class<? extends T> configImplementationFor(final Class<T> interfaceClass) {
        try {
            final String implClassName = implClassNameFor(interfaceClass);

            //noinspection unchecked
            return (Class<? extends T>) Class.forName(implClassName);
        } catch (final ClassNotFoundException notFound) {
            throw new IllegalStateException("No implementation for " + interfaceClass.getCanonicalName(), notFound);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static <T> String implClassNameFor(final Class<T> interfaceClass) {
        final String packageName = interfaceClass.getPackage().getName();
        // include the package name dot as well
        final String classHierarchy = interfaceClass.getCanonicalName().substring(packageName.length() + 1);

        // every subclass and the class itself has 'Impl' behind it
        final String implClassName =
                Arrays.stream(classHierarchy.split("\\."))
                        .reduce("", (reduced, current) -> {
                            if (!reduced.isEmpty()) {
                                reduced += "$";
                            }
                            return reduced + current + "Impl";
                        });
        return packageName + "." + implClassName;
    }

}

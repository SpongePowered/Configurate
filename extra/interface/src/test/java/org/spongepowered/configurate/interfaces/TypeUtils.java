/*
 * Configurate
 * Copyright (C) zml and Configurate contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

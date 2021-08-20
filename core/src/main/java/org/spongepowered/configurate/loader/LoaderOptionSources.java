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
package org.spongepowered.configurate.loader;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Locale;

final class LoaderOptionSources {

    static final LoaderOptionSource ENVIRONMENT = new EnvironmentVariables("CONFIGURATE");
    static final LoaderOptionSource SYSTEM_PROPERTIES = new SystemProperties("configurate");

    private LoaderOptionSources() {
    }

    static final class EnvironmentVariables implements LoaderOptionSource {

        private static final char ENVIORNMENT_VAR_SEPARATOR = '_';

        private final String prefix;

        EnvironmentVariables(final String prefix) {
            this.prefix = prefix.toUpperCase(Locale.ROOT);
        }

        @Override
        public @Nullable String get(final String... path) {
            if (path.length == 0) {
                throw new IllegalArgumentException("A provided path must have at least one element");
            }

            final StringBuilder varName = new StringBuilder(this.prefix);
            for (final String element : path) {
                varName.append(ENVIORNMENT_VAR_SEPARATOR).append(element.toUpperCase(Locale.ROOT));
            }
            return System.getenv(varName.toString());
        }

    }

    static final class SystemProperties implements LoaderOptionSource {

        private static final char SYSTEM_PROPERTY_SEPARATOR = '.';

        private final String prefix;

        SystemProperties(final String prefix) {
            this.prefix = prefix;
        }

        @Override
        public @Nullable String get(final String... path) {
            if (path.length == 0) {
                throw new IllegalArgumentException("A provided path must have at least one element");
            }

            final StringBuilder varName = new StringBuilder(this.prefix);
            for (final String element : path) {
                varName.append(SYSTEM_PROPERTY_SEPARATOR).append(element);
            }
            return System.getProperty(varName.toString());
        }

    }

    static final class Node implements LoaderOptionSource {

        private final ConfigurationNode source;

        Node(final ConfigurationNode source) {
            this.source = source;
        }

        @Override
        public @Nullable String get(final String... path) {
            if (path.length == 0) {
                throw new IllegalArgumentException("A provided path must have at least one element");
            }

            return this.source.node((Object[]) path).getString();
        }

    }

    static final class Composite implements LoaderOptionSource {

        private final LoaderOptionSource[] sources;

        Composite(final LoaderOptionSource[] sources) {
            this.sources = sources;
        }

        @Override
        public @Nullable String get(final String... path) {
            if (path.length == 0) {
                throw new IllegalArgumentException("A provided path must have at least one element");
            }

            @Nullable String result;
            for (final LoaderOptionSource source : this.sources) {
                result = source.get(path);
                if (result != null) {
                    return result;
                }
            }

            return null;
        }

    }

}

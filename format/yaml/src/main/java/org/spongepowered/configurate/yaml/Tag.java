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
package org.spongepowered.configurate.yaml;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

class Tag {

    private final URI tagUri;
    private final Set<Class<?>> supportedTypes;

    Tag(final URI tagUri, final Set<? extends Class<?>> supportedTypes) {
        this.tagUri = tagUri;
        this.supportedTypes = UnmodifiableCollections.copyOf(supportedTypes);
    }

    public final URI tagUri() {
        return this.tagUri;
    }

    public final Set<Class<?>> supportedTypes() {
        return this.supportedTypes;
    }

    abstract static class Scalar<V> extends Tag {

        private final @Nullable Pattern pattern;
        private final @Nullable ScalarStyle preferredScalarStyle;

        // for unregistered tags on scalars
        static Scalar<String> ofUnknown(final URI tagUri) {
            return new Scalar<String>(tagUri, Collections.emptySet(), null, null) {
                @Override
                public String fromString(final String input) {
                    return input;
                }

                @Override
                public String toString(final String own) {
                    return own;
                }
            };
        }

        Scalar(final URI tagUri, final Set<Class<? extends V>> supportedTypes, final @Nullable Pattern pattern) {
            this(tagUri, supportedTypes, pattern, null);
        }

        Scalar(final URI tagUri, final Set<Class<? extends V>> supportedTypes, final @Nullable Pattern pattern, final @Nullable ScalarStyle preferredScalarStyle) {
            super(tagUri, supportedTypes);
            this.pattern = pattern;
            this.preferredScalarStyle = preferredScalarStyle;
        }

        /**
         * Pattern to use to detect this tag.
         *
         * <p>May be {@code null} if this tag cannot be used as an
         * implicit tag.</p>
         *
         * @return the detection pattern
         * @since 4.2.0
         */
        public final @Nullable Pattern pattern() {
            return this.pattern;
        }

        /**
         * Get the preferred scalar style to use for this type, when none is specifically used.
         *
         * @return the preferred scalar style
         * @since 4.2.0
         */
        public final @Nullable ScalarStyle preferredScalarStyle() {
            return this.preferredScalarStyle;
        }

        public abstract V fromString(String input) throws ParsingException;

        public abstract String toString(V own) throws ConfigurateException;

    }

    static class Mapping extends Tag {

        Mapping(final URI tagUri, final Set<Class<?>> supportedTypes) {
            super(tagUri, supportedTypes);
        }

    }

    static class Sequence extends Tag {

        Sequence(final URI tagUri, final Set<Class<?>> supportedTypes) {
            super(tagUri, supportedTypes);
        }

    }

    @Override
    public boolean equals(final @Nullable Object that) {
        // todo: ensure type of tag is equal
        return that instanceof Tag
            && ((Tag) that).tagUri().equals(this.tagUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tagUri);
    }

}

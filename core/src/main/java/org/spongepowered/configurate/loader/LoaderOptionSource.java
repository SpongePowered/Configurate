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

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.Scalars;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Arrays;

/**
 * A provider for {@link ConfigurationLoader} options.
 *
 * <p>This allows initializing a loader from sources like system properties,
 * environment variables, or an existing configuration node.</p>
 *
 * <p>To allow working with primitive value sources, only
 * scalar values are supported.</p>
 *
 * @since 4.2.0
 */
@SuppressWarnings("checkstyle:NoGetSetPrefix") // we need something to not overlap with primitives
public interface LoaderOptionSource {

    /**
     * Retrieve loader options from environment variables.
     *
     * <p>When reading environment variables, property keys will be converted to
     * upper case and joined by '{@code _}' (an underscore).</p>
     *
     * <p>This source will use a default prefix of {@code CONFIGURATE}.</p>
     *
     * @return a source retrieving options from environment variables
     * @since 4.2.0
     */
    static LoaderOptionSource environmentVariables() {
        return LoaderOptionSources.ENVIRONMENT;
    }

    /**
     * Retrieve loader options from environment variables.
     *
     * <p>When reading environment variables, property keys will be converted to
     * upper case and joined by '{@code _}' (an underscore).</p>
     *
     * @param prefix the prefix to prepend to option paths
     * @return a source retrieving options from environment variables
     * @since 4.2.0
     */
    static LoaderOptionSource environmentVariables(final String prefix) {
        return new LoaderOptionSources.EnvironmentVariables(requireNonNull(prefix, "prefix"));
    }

    /**
     * Retrieve loader options from system properties.
     *
     * <p>When reading system properties, property keys will be joined by
     * '{@code .}' (a dot).</p>
     *
     * <p>This source will use a default prefix of {@code configurate}.</p>
     *
     * @return a source retrieving options from system properties
     * @since 4.2.0
     */
    static LoaderOptionSource systemProperties() {
        return LoaderOptionSources.SYSTEM_PROPERTIES;
    }

    /**
     * Retrieve loader options from system properties.
     *
     * <p>When reading system properties, property keys will be joined by
     * '{@code .}' (a dot).</p>
     *
     * @param prefix the prefix to prepend to option paths
     * @return a source retrieving options from system properties
     * @since 4.2.0
     */
    static LoaderOptionSource systemProperties(final String prefix) {
        return new LoaderOptionSources.SystemProperties(requireNonNull(prefix, "prefix"));
    }

    /**
     * Create an option source that will read from an existing
     * configuration node.
     *
     * <p>Option paths will be converted directly to node paths.</p>
     *
     * @param node the node to read options from
     * @return a source retrieving options from the provided node
     * @since 4.2.0
     */
    static LoaderOptionSource node(final ConfigurationNode node) {
        return new LoaderOptionSources.Node(requireNonNull(node, "node"));
    }

    /**
     * Create an option source that will try the provided sources in order.
     *
     * <p>The first source with a present value will be used.</p>
     *
     * @param sources the option sources to delegate to
     * @return a new option source
     * @since 4.2.0
     */
    static LoaderOptionSource composite(final LoaderOptionSource... sources) {
        return new LoaderOptionSources.Composite(Arrays.copyOf(sources, sources.length));
    }

    /**
     * Get the value at the provided path.
     *
     * @param path the path for options.
     * @return a value, or {@code null} if none is present
     * @throws IllegalArgumentException if the provided path is an empty array
     * @since 4.2.0
     */
    @Nullable String get(String... path);

    /**
     * Get the value at the provided path.
     *
     * <p>If no value is present, {@code defaultValue} will be returned.</p>
     *
     * @param path the path for options.
     * @param defaultValue the value to return if none is present
     * @return a value, or {@code defaultValue} if none is present
     * @throws IllegalArgumentException if the provided path is an empty array
     * @since 4.2.0
     */
    default String getOr(final String defaultValue, final String... path) {
        final @Nullable String value = this.get(path);
        return value == null ? defaultValue : value;
    }

    /**
     * Get the value at the provided path as an enum constant.
     *
     * @param <T> enum type
     * @param enumClazz the enum type's class
     * @param path the path for options.
     * @return a value, or {@code null} if none is present or the provided value
     *     is not a member of the enum
     * @throws IllegalArgumentException if the provided path is an empty array
     * @since 4.2.0
     */
    @SuppressWarnings("unchecked")
    default <T extends Enum<T>> @Nullable T getEnum(final Class<T> enumClazz, final String... path) {
        final @Nullable String value = this.get(path);
        try {
            return value == null ? null : (T) Scalars.ENUM.deserialize(enumClazz, value);
        } catch (SerializationException e) {
            return null;
        }
    }

    /**
     * Get the value at the provided path as an enum constant.
     *
     * <p>If no value is present or the provided value is not a known enum
     * constant, {@code defaultValue} will be returned.</p>
     *
     * @param <T> enum type
     * @param enumClazz the enum type's class
     * @param path the path for options.
     * @param defaultValue the value to return if none is present
     * @return a value, or {@code defaultValue} if none is present
     * @throws IllegalArgumentException if the provided path is an empty array
     * @since 4.2.0
     */
    default <T extends Enum<T>> T getEnum(final Class<T> enumClazz, final T defaultValue, final String... path) {
        final @Nullable T value = this.getEnum(enumClazz, path);
        return value == null ? defaultValue : value;
    }

    // For primitives, a default is required since there is no null value

    /**
     * Get the value at the provided path as an integer.
     *
     * <p>If no value is present or the value is not a valid integer,
     * {@code defaultValue} will be returned.</p>
     *
     * @param path the path for options.
     * @param defaultValue the value to return if none is available
     * @return a value, or {@code defaultValue} if none is present
     * @throws IllegalArgumentException if the provided path is an empty array
     * @since 4.2.0
     */
    default int getInt(final int defaultValue, final String... path) {
        final @Nullable String value = this.get(path);
        if (value == null) {
            return defaultValue;
        }
        final @Nullable Integer attempt = Scalars.INTEGER.tryDeserialize(value);
        return attempt == null ? defaultValue : attempt;
    }

    /**
     * Get the value at the provided path as a double.
     *
     * <p>If no value is present or the value is not a valid double,
     * {@code defaultValue} will be returned.</p>
     *
     * @param path the path for options.
     * @param defaultValue the value to return if none is available
     * @return a value, or {@code defaultValue} if none is present
     * @throws IllegalArgumentException if the provided path is an empty array
     * @since 4.2.0
     */
    default double getDouble(final double defaultValue, final String... path) {
        final @Nullable String value = this.get(path);
        if (value == null) {
            return defaultValue;
        }
        final @Nullable Double attempt = Scalars.DOUBLE.tryDeserialize(value);
        return attempt == null ? defaultValue : attempt;
    }

    /**
     * Get the value at the provided path as a boolean.
     *
     * <p>If no value is present or the value is not a valid boolean,
     * {@code defaultValue} will be returned.</p>
     *
     * @param path the path for options.
     * @param defaultValue the value to return if none is available
     * @return a value, or {@code defaultValue} if none is present
     * @throws IllegalArgumentException if the provided path is an empty array
     * @since 4.2.0
     */
    default boolean getBoolean(final boolean defaultValue, final String... path) {
        final @Nullable String value = this.get(path);
        if (value == null) {
            return defaultValue;
        }
        final @Nullable Boolean attempt = Scalars.BOOLEAN.tryDeserialize(value);
        return attempt == null ? defaultValue : attempt;
    }

}

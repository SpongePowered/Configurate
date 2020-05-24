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
package org.spongepowered.configurate;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.DefaultObjectMapperFactory;
import org.spongepowered.configurate.objectmapping.ObjectMapperFactory;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.MapFactories;
import org.spongepowered.configurate.util.MapFactory;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This object is a holder for general configuration options.
 *
 * <p>This is meant to hold options that are used in configuring how the
 * configuration data structures are handled, rather than the serialization
 * configuration which is located in {@link ConfigurationLoader}s.</p>
 *
 * <p>This class is immutable.</p>
 */
public final class ConfigurationOptions {

    private static final ConfigurationOptions DEFAULTS = new ConfigurationOptions(MapFactories.insertionOrdered(), null,
        TypeSerializerCollection.defaults(), null, DefaultObjectMapperFactory.getInstance(), false);

    @NonNull private final MapFactory mapFactory;
    @Nullable private final String header;
    @NonNull private final TypeSerializerCollection serializers;
    @Nullable private final ImmutableSet<Class<?>> acceptedTypes;
    @NonNull private final ObjectMapperFactory objectMapperFactory;
    private final boolean shouldCopyDefaults;

    private ConfigurationOptions(final @NonNull MapFactory mapFactory, final @Nullable String header,
            final @NonNull TypeSerializerCollection serializers, final @Nullable Set<Class<?>> acceptedTypes,
            final @NonNull ObjectMapperFactory objectMapperFactory, final boolean shouldCopyDefaults) {
        this.mapFactory = mapFactory;
        this.header = header;
        this.serializers = serializers;
        this.acceptedTypes = acceptedTypes == null ? null : ImmutableSet.copyOf(acceptedTypes);
        this.objectMapperFactory = objectMapperFactory;
        this.shouldCopyDefaults = shouldCopyDefaults;
    }

    /**
     * Get the default set of options. This may be overridden by your chosen
     * configuration loader, so when building configurations it is recommended
     * to access {@code AbstractConfigurationLoader.Builder#getDefaultOptions()}
     * instead.
     *
     * @return the default options
     */
    @NonNull
    public static ConfigurationOptions defaults() {
        return DEFAULTS;
    }

    /**
     * Gets the {@link MapFactory} specified in these options.
     *
     * @return The map factory
     */
    @NonNull
    public MapFactory getMapFactory() {
        return this.mapFactory;
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified
     * {@link MapFactory} set, and all other settings copied from this instance.
     *
     * @param mapFactory The new factory to use to create a map
     * @return The new options object
     */
    @NonNull
    public ConfigurationOptions withMapFactory(final @NonNull MapFactory mapFactory) {
        requireNonNull(mapFactory, "mapFactory");
        if (this.mapFactory == mapFactory) {
            return this;
        }
        return new ConfigurationOptions(mapFactory, this.header, this.serializers, this.acceptedTypes, this.objectMapperFactory,
                this.shouldCopyDefaults);
    }

    /**
     * Gets the header specified in these options.
     *
     * @return The current header. Lines are split by \n,
     */
    @Nullable
    public String getHeader() {
        return this.header;
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified
     * header set, and all other settings copied from this instance.
     *
     * @param header The new header to use
     * @return The new options object
     */
    @NonNull
    public ConfigurationOptions withHeader(final @Nullable String header) {
        if (Objects.equals(this.header, header)) {
            return this;
        }
        return new ConfigurationOptions(this.mapFactory, header, this.serializers, this.acceptedTypes, this.objectMapperFactory,
                this.shouldCopyDefaults);
    }

    /**
     * Gets the {@link TypeSerializerCollection} specified in these options.
     *
     * @return The type serializers
     */
    @NonNull
    public TypeSerializerCollection getSerializers() {
        return this.serializers;
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified {@link TypeSerializerCollection}
     * set, and all other settings copied from this instance.
     *
     * @param serializers The serializers to use
     * @return The new options object
     */
    @NonNull
    public ConfigurationOptions withSerializers(final @NonNull TypeSerializerCollection serializers) {
        requireNonNull(serializers, "serializers");
        if (this.serializers == serializers) {
            return this;
        }
        return new ConfigurationOptions(this.mapFactory, this.header, serializers, this.acceptedTypes, this.objectMapperFactory,
                this.shouldCopyDefaults);
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with a new
     * {@link TypeSerializerCollection} created as a child of this options'
     * current collection. The provided function will be called with the builder
     * for this new collection to allow registering more type serializers.
     *
     * @param serializerBuilder accepts a builder for the collection that will
     *                          be used in the returned options object.
     * @return The new options object
     */
    public @NonNull ConfigurationOptions withSerializers(final @NonNull Consumer<TypeSerializerCollection.Builder> serializerBuilder) {
        requireNonNull(serializerBuilder, "serializerBuilder");
        final TypeSerializerCollection.Builder builder = this.serializers.childBuilder();
        serializerBuilder.accept(builder);
        return new ConfigurationOptions(this.mapFactory, this.header, builder.build(), this.acceptedTypes, this.objectMapperFactory,
                this.shouldCopyDefaults);
    }

    /**
     * Gets the {@link ObjectMapperFactory} specified in these options.
     *
     * @return The factory used to construct ObjectMapper instances
     */
    @NonNull
    public ObjectMapperFactory getObjectMapperFactory() {
        return this.objectMapperFactory;
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified {@link ObjectMapperFactory}
     * set, and all other settings copied from this instance.
     *
     * @param objectMapperFactory The factory to use to produce object mapper
     *                            instances. Must not be null
     * @return updated options object
     */
    @NonNull
    public ConfigurationOptions withObjectMapperFactory(final @NonNull ObjectMapperFactory objectMapperFactory) {
        requireNonNull(objectMapperFactory, "factory");
        if (this.objectMapperFactory == objectMapperFactory) {
            return this;
        }
        return new ConfigurationOptions(this.mapFactory, this.header, this.serializers, this.acceptedTypes, objectMapperFactory,
                this.shouldCopyDefaults);
    }

    /**
     * Gets whether objects of the provided type are natively accepted as values
     * for nodes with this as their options object.
     *
     * @param type The type to check
     * @return Whether the type is accepted
     */
    public boolean acceptsType(final @NonNull Class<?> type) {
        requireNonNull(type, "type");

        if (this.acceptedTypes == null) {
            return true;
        }
        if (this.acceptedTypes.contains(type)) {
            return true;
        }

        if (type.isPrimitive() && this.acceptedTypes.contains(Primitives.wrap(type))) {
            return true;
        }

        if (Primitives.isWrapperType(type) && this.acceptedTypes.contains(Primitives.unwrap(type))) {
            return true;
        }

        for (Class<?> clazz : this.acceptedTypes) {
            if (clazz.isAssignableFrom(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified native types
     * set, and all other settings copied from this instance.
     *
     * <p>Native types are format-dependent, and must be provided by a
     * configuration loader's {@link ConfigurationLoader#defaultOptions() default options}</p>
     *
     * <p>Null indicates that all types are accepted.</p>
     *
     * @param acceptedTypes The types that will be accepted to a call to {@link ConfigurationNode#setValue(Object)}
     * @return updated options object
     */
    @NonNull
    public ConfigurationOptions withNativeTypes(final @Nullable Set<Class<?>> acceptedTypes) {
        if (Objects.equals(this.acceptedTypes, acceptedTypes)) {
            return this;
        }
        return new ConfigurationOptions(this.mapFactory, this.header, this.serializers, acceptedTypes, this.objectMapperFactory,
                this.shouldCopyDefaults);
    }

    /**
     * Gets whether or not default parameters provided to {@link ConfigurationNode} getter methods
     * should be set to the node when used.
     *
     * @return Whether defaults should be copied into value
     */
    public boolean shouldCopyDefaults() {
        return this.shouldCopyDefaults;
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified 'copy defaults' setting
     * set, and all other settings copied from this instance.
     *
     * @see #shouldCopyDefaults() for information on what this method does
     * @param shouldCopyDefaults whether to copy defaults
     * @return updated options object
     */
    @NonNull
    public ConfigurationOptions withShouldCopyDefaults(final boolean shouldCopyDefaults) {
        if (this.shouldCopyDefaults == shouldCopyDefaults) {
            return this;
        }
        return new ConfigurationOptions(this.mapFactory, this.header, this.serializers, this.acceptedTypes, this.objectMapperFactory,
                shouldCopyDefaults);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ConfigurationOptions)) {
            return false;
        }

        final ConfigurationOptions that = (ConfigurationOptions) o;
        return Objects.equals(this.shouldCopyDefaults, that.shouldCopyDefaults)
                && Objects.equals(this.mapFactory, that.mapFactory)
                && Objects.equals(this.header, that.header)
                && Objects.equals(this.serializers, that.serializers)
                && Objects.equals(this.acceptedTypes, that.acceptedTypes)
                && Objects.equals(this.objectMapperFactory, that.objectMapperFactory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.mapFactory, this.header, this.serializers, this.acceptedTypes, this.objectMapperFactory, this.shouldCopyDefaults);
    }

    @Override
    public String toString() {
        return "ConfigurationOptions{"
                + "mapFactory=" + this.mapFactory
                + ", header='" + this.header + '\''
                + ", serializers=" + this.serializers
                + ", acceptedTypes=" + this.acceptedTypes
                + ", objectMapperFactory=" + this.objectMapperFactory
                + ", shouldCopyDefaults=" + this.shouldCopyDefaults
                + '}';
    }

}

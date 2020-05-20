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

import static java.util.Objects.requireNonNull;

/**
 * This object is a holder for general configuration options.
 *
 * <p>This is meant to hold options that are used in configuring how the configuration data
 * structures are handled, rather than the serialization configuration that is located in
 * {@link ConfigurationLoader}s.</p>
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

    private ConfigurationOptions(@NonNull MapFactory mapFactory, @Nullable String header, @NonNull TypeSerializerCollection serializers, @Nullable Set<Class<?>> acceptedTypes, @NonNull ObjectMapperFactory objectMapperFactory, boolean shouldCopyDefaults) {
        this.mapFactory = mapFactory;
        this.header = header;
        this.serializers = serializers;
        this.acceptedTypes = acceptedTypes == null ? null : ImmutableSet.copyOf(acceptedTypes);
        this.objectMapperFactory = objectMapperFactory;
        this.shouldCopyDefaults = shouldCopyDefaults;
    }

    /**
     * Get the default set of options. This may be overridden by your chosen configuration loader,
     * so when building configurations it is recommended to access
     * {@code AbstractConfigurationLoader.Builder#getDefaultOptions()} instead.
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
        return mapFactory;
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified {@link MapFactory}
     * set, and all other settings copied from this instance.
     *
     * @param mapFactory The new factory to use to create a map
     * @return The new options object
     */
    @NonNull
    public ConfigurationOptions withMapFactory(@NonNull MapFactory mapFactory) {
        requireNonNull(mapFactory, "mapFactory");
        if (this.mapFactory == mapFactory) {
            return this;
        }
        return new ConfigurationOptions(mapFactory, header, serializers, acceptedTypes, objectMapperFactory, shouldCopyDefaults);
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
     * Creates a new {@link ConfigurationOptions} instance, with the specified header
     * set, and all other settings copied from this instance.
     *
     * @param header The new header to use
     * @return The new options object
     */
    @NonNull
    public ConfigurationOptions withHeader(@Nullable String header) {
        if (Objects.equals(this.header, header)) {
            return this;
        }
        return new ConfigurationOptions(mapFactory, header, serializers, acceptedTypes, objectMapperFactory, shouldCopyDefaults);
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
    public ConfigurationOptions withSerializers(@NonNull TypeSerializerCollection serializers) {
        requireNonNull(serializers, "serializers");
        if (this.serializers == serializers) {
            return this;
        }
        return new ConfigurationOptions(mapFactory, header, serializers, acceptedTypes, objectMapperFactory, shouldCopyDefaults);
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with a new
     * {@link TypeSerializerCollection} created as a child of this options' current collection.
     * The provided function will be called with the builder for this new collection to allow
     * registering more type serializers.
     *
     * @param serializerBuilder accepts a builder for the collection that will be used in the
     *                          returned options object.
     * @return The new options object
     */
    public @NonNull ConfigurationOptions withSerializers(@NonNull Consumer<TypeSerializerCollection.Builder> serializerBuilder) {
        requireNonNull(serializerBuilder, "serializerBuilder");
        final TypeSerializerCollection.Builder builder = this.serializers.childBuilder();
        serializerBuilder.accept(builder);
        return new ConfigurationOptions(mapFactory, header, builder.build(), acceptedTypes, objectMapperFactory, shouldCopyDefaults);
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
     * @param objectMapperFactory The factory to use to produce object mapper instances. Must not be null
     * @return updated options object
     */
    @NonNull
    public ConfigurationOptions withObjectMapperFactory(@NonNull ObjectMapperFactory objectMapperFactory) {
        requireNonNull(objectMapperFactory, "factory");
        if (this.objectMapperFactory == objectMapperFactory) {
            return this;
        }
        return new ConfigurationOptions(mapFactory, header, serializers, acceptedTypes, objectMapperFactory, shouldCopyDefaults);
    }

    /**
     * Gets whether objects of the provided type are natively accepted as values for nodes with this as
     * their options object.
     *
     * @param type The type to check
     * @return Whether the type is accepted
     */
    public boolean acceptsType(@NonNull Class<?> type) {
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
     * Native types are format-dependent, and must be provided by a configuration loader's
     * {@link ConfigurationLoader#getDefaultOptions() default options}
     *
     * <p>Null indicates that all types are accepted.</p>
     *
     * @param acceptedTypes The types that will be accepted to a call to {@link ConfigurationNode#setValue(Object)}
     * @return updated options object
     */
    @NonNull
    public ConfigurationOptions withNativeTypes(@Nullable Set<Class<?>> acceptedTypes) {
        if (Objects.equals(this.acceptedTypes, acceptedTypes)) {
            return this;
        }
        return new ConfigurationOptions(mapFactory, header, serializers, acceptedTypes, objectMapperFactory, shouldCopyDefaults);
    }

    /**
     * Gets whether or not default parameters provided to {@link ConfigurationNode} getter methods
     * should be set to the node when used.
     *
     * @return Whether defaults should be copied into value
     */
    public boolean shouldCopyDefaults() {
        return shouldCopyDefaults;
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
    public ConfigurationOptions withShouldCopyDefaults(boolean shouldCopyDefaults) {
        if (this.shouldCopyDefaults == shouldCopyDefaults) {
            return this;
        }
        return new ConfigurationOptions(mapFactory, header, serializers, acceptedTypes, objectMapperFactory, shouldCopyDefaults);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationOptions)) return false;
        ConfigurationOptions that = (ConfigurationOptions) o;
        return Objects.equals(shouldCopyDefaults, that.shouldCopyDefaults) &&
                Objects.equals(mapFactory, that.mapFactory) &&
                Objects.equals(header, that.header) &&
                Objects.equals(serializers, that.serializers) &&
                Objects.equals(acceptedTypes, that.acceptedTypes) &&
                Objects.equals(objectMapperFactory, that.objectMapperFactory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapFactory, header, serializers, acceptedTypes, objectMapperFactory, shouldCopyDefaults);
    }

    @Override
    public String toString() {
        return "ConfigurationOptions{" +
                "mapFactory=" + mapFactory +
                ", header='" + header + '\'' +
                ", serializers=" + serializers +
                ", acceptedTypes=" + acceptedTypes +
                ", objectMapperFactory=" + objectMapperFactory +
                ", shouldCopyDefaults=" + shouldCopyDefaults +
                '}';
    }
}

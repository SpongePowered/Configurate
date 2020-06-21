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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.MapFactories;
import org.spongepowered.configurate.util.MapFactory;
import org.spongepowered.configurate.util.Typing;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.lang.reflect.Type;
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
        TypeSerializerCollection.defaults(), null, ObjectMapper.factory(), false);

    private final MapFactory mapFactory;
    private final @Nullable String header;
    private final TypeSerializerCollection serializers;
    private final @Nullable Set<Class<?>> acceptedTypes;
    private final ObjectMapper.Factory objectMapperFactory;
    private final boolean shouldCopyDefaults;

    private ConfigurationOptions(final MapFactory mapFactory, final @Nullable String header,
            final TypeSerializerCollection serializers, final @Nullable Set<Class<?>> acceptedTypes,
            final ObjectMapper.Factory objectMapperFactory, final boolean shouldCopyDefaults) {
        this.mapFactory = mapFactory;
        this.header = header;
        this.serializers = serializers;
        this.acceptedTypes = acceptedTypes == null ? null : UnmodifiableCollections.copyOf(acceptedTypes);
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
    public static ConfigurationOptions defaults() {
        return DEFAULTS;
    }

    /**
     * Gets the {@link MapFactory} specified in these options.
     *
     * @return The map factory
     */
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
    public ConfigurationOptions withMapFactory(final MapFactory mapFactory) {
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
    public @Nullable String getHeader() {
        return this.header;
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified
     * header set, and all other settings copied from this instance.
     *
     * @param header The new header to use
     * @return The new options object
     */
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
    public ConfigurationOptions withSerializers(final TypeSerializerCollection serializers) {
        requireNonNull(serializers, "serializers");
        if (this.serializers.equals(serializers)) {
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
    public ConfigurationOptions withSerializers(final Consumer<TypeSerializerCollection.Builder> serializerBuilder) {
        requireNonNull(serializerBuilder, "serializerBuilder");
        final TypeSerializerCollection.Builder builder = this.serializers.childBuilder();
        serializerBuilder.accept(builder);
        return new ConfigurationOptions(this.mapFactory, this.header, builder.build(), this.acceptedTypes, this.objectMapperFactory,
                this.shouldCopyDefaults);
    }

    /**
     * Gets the {@link ObjectMapper.Factory} specified in these options.
     *
     * @return The factory used to construct ObjectMapper instances
     */
    public ObjectMapper.Factory getObjectMapperFactory() {
        return this.objectMapperFactory;
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified {@link ObjectMapper.Factory}
     * set, and all other settings copied from this instance.
     *
     * @param objectMapperFactory The factory to use to produce object mapper
     *                            instances. Must not be null
     * @return updated options object
     */
    public ConfigurationOptions withObjectMapperFactory(final ObjectMapper.Factory objectMapperFactory) {
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
    public boolean acceptsType(final Class<?> type) {
        requireNonNull(type, "type");

        if (this.acceptedTypes == null) {
            return true;
        }
        if (this.acceptedTypes.contains(type)) {
            return true;
        }

        if (type.isPrimitive() && this.acceptedTypes.contains(Typing.box(type))) {
            return true;
        }

        final Type unboxed = Typing.unbox(type);
        if (unboxed != type && this.acceptedTypes.contains(unboxed)) {
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
    public ConfigurationOptions withShouldCopyDefaults(final boolean shouldCopyDefaults) {
        if (this.shouldCopyDefaults == shouldCopyDefaults) {
            return this;
        }
        return new ConfigurationOptions(this.mapFactory, this.header, this.serializers, this.acceptedTypes, this.objectMapperFactory,
                shouldCopyDefaults);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ConfigurationOptions)) {
            return false;
        }

        final ConfigurationOptions that = (ConfigurationOptions) other;
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

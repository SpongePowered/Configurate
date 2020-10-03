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

import com.google.auto.value.AutoValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.loader.ConfigurationLoader;
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
@AutoValue
public abstract class ConfigurationOptions {

    static class Lazy {

        // avoid initialization cycles

        static final ConfigurationOptions DEFAULTS = new AutoValue_ConfigurationOptions(MapFactories.insertionOrdered(), null,
                TypeSerializerCollection.defaults(), null, false, false);

    }

    ConfigurationOptions() {
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
        return Lazy.DEFAULTS;
    }

    /**
     * Gets the {@link MapFactory} specified in these options.
     *
     * @return The map factory
     */
    public abstract MapFactory getMapFactory();

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified
     * {@link MapFactory} set, and all other settings copied from this instance.
     *
     * @param mapFactory The new factory to use to create a map
     * @return The new options object
     */
    public ConfigurationOptions withMapFactory(final MapFactory mapFactory) {
        requireNonNull(mapFactory, "mapFactory");
        if (this.getMapFactory() == mapFactory) {
            return this;
        }
        return new AutoValue_ConfigurationOptions(mapFactory, getHeader(), getSerializers(), getNativeTypes(),
                shouldCopyDefaults(), isImplicitInitialization());
    }

    /**
     * Gets the header specified in these options.
     *
     * @return The current header. Lines are split by \n,
     */
    public abstract @Nullable String getHeader();

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified
     * header set, and all other settings copied from this instance.
     *
     * @param header The new header to use
     * @return The new options object
     */
    public ConfigurationOptions withHeader(final @Nullable String header) {
        if (Objects.equals(this.getHeader(), header)) {
            return this;
        }
        return new AutoValue_ConfigurationOptions(getMapFactory(), header, getSerializers(), getNativeTypes(),
                shouldCopyDefaults(), isImplicitInitialization());
    }

    /**
     * Gets the {@link TypeSerializerCollection} specified in these options.
     *
     * @return The type serializers
     */
    public abstract TypeSerializerCollection getSerializers();

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified {@link TypeSerializerCollection}
     * set, and all other settings copied from this instance.
     *
     * @param serializers The serializers to use
     * @return The new options object
     */
    public ConfigurationOptions withSerializers(final TypeSerializerCollection serializers) {
        requireNonNull(serializers, "serializers");
        if (this.getSerializers().equals(serializers)) {
            return this;
        }
        return new AutoValue_ConfigurationOptions(getMapFactory(), getHeader(), serializers, getNativeTypes(),
                shouldCopyDefaults(), isImplicitInitialization());
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
    public final ConfigurationOptions withSerializers(final Consumer<TypeSerializerCollection.Builder> serializerBuilder) {
        requireNonNull(serializerBuilder, "serializerBuilder");
        final TypeSerializerCollection.Builder builder = this.getSerializers().childBuilder();
        serializerBuilder.accept(builder);
        return withSerializers(builder.build());
    }

    @SuppressWarnings("AutoValueImmutableFields") // we don't use guava
    abstract @Nullable Set<Class<?>> getNativeTypes();

    /**
     * Gets whether objects of the provided type are natively accepted as values
     * for nodes with this as their options object.
     *
     * @param type The type to check
     * @return Whether the type is accepted
     */
    public final boolean acceptsType(final Class<?> type) {
        requireNonNull(type, "type");

        final @Nullable Set<Class<?>> nativeTypes = getNativeTypes();

        if (nativeTypes == null) {
            return true;
        }

        if (nativeTypes.contains(type)) {
            return true;
        }

        if (type.isPrimitive() && nativeTypes.contains(Typing.box(type))) {
            return true;
        }

        final Type unboxed = Typing.unbox(type);
        if (unboxed != type && nativeTypes.contains(unboxed)) {
            return true;
        }

        for (Class<?> clazz : nativeTypes) {
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
     * @param nativeTypes The types that will be accepted to a call to {@link ConfigurationNode#setValue(Object)}
     * @return updated options object
     */
    public ConfigurationOptions withNativeTypes(final @Nullable Set<Class<?>> nativeTypes) {
        if (Objects.equals(this.getNativeTypes(), nativeTypes)) {
            return this;
        }
        return new AutoValue_ConfigurationOptions(getMapFactory(), getHeader(), getSerializers(),
                nativeTypes == null ? null : UnmodifiableCollections.copyOf(nativeTypes), shouldCopyDefaults(), isImplicitInitialization());
    }

    /**
     * Gets whether or not default parameters provided to {@link ConfigurationNode} getter methods
     * should be set to the node when used.
     *
     * @return Whether defaults should be copied into value
     */
    public abstract boolean shouldCopyDefaults();

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified
     * 'copy defaults' setting set, and all other settings copied from
     * this instance.
     *
     * @see #shouldCopyDefaults() for information on what this method does
     * @param shouldCopyDefaults whether to copy defaults
     * @return updated options object
     */
    public ConfigurationOptions withShouldCopyDefaults(final boolean shouldCopyDefaults) {
        if (this.shouldCopyDefaults() == shouldCopyDefaults) {
            return this;
        }

        return new AutoValue_ConfigurationOptions(getMapFactory(), getHeader(), getSerializers(), getNativeTypes(),
                shouldCopyDefaults, isImplicitInitialization());
    }

    /**
     * Get whether values should be implicitly initialized.
     *
     * <p>When this is true, any value get operations will return an empty value
     * rather than null. This extends through to fields loaded into
     * object-mapped classes.</p>
     *
     * <p>This option is disabled by default</p>
     *
     * @return if implicit initialization is enabled.
     */
    public abstract boolean isImplicitInitialization();

    /**
     * Create a new {@link ConfigurationOptions} instance with the specified
     * implicit initialization setting.
     *
     * @param implicitInitialization whether to initialize implicitly
     * @return a new options object
     * @see #isImplicitInitialization() for more details
     */
    public ConfigurationOptions withImplicitInitialization(final boolean implicitInitialization) {
        if (this.isImplicitInitialization() == implicitInitialization) {
            return this;
        }

        return new AutoValue_ConfigurationOptions(getMapFactory(), getHeader(), getSerializers(), getNativeTypes(),
                shouldCopyDefaults(), implicitInitialization);
    }

}

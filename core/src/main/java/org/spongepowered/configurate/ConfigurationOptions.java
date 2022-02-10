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
import com.google.errorprone.annotations.CheckReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.MapFactories;
import org.spongepowered.configurate.util.MapFactory;
import org.spongepowered.configurate.util.Types;
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
 *
 * @since 4.0.0
 */
@AutoValue
@CheckReturnValue // No method has side effects, so any return value must be used.
public abstract class ConfigurationOptions {

    static class Lazy {

        // avoid initialization cycles

        static final ConfigurationOptions DEFAULTS = new AutoValue_ConfigurationOptions(MapFactories.insertionOrdered(), null,
                TypeSerializerCollection.defaults(), null, true, true);

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
     * @since 4.0.0
     */
    public static ConfigurationOptions defaults() {
        return Lazy.DEFAULTS;
    }

    /**
     * Gets the {@link MapFactory} specified in these options.
     *
     * @return the map factory
     * @since 4.0.0
     */
    public abstract MapFactory mapFactory();

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified
     * {@link MapFactory} set, and all other settings copied from this instance.
     *
     * @param mapFactory the new factory to use to create a map
     * @return the new options object
     * @since 4.0.0
     */
    public ConfigurationOptions mapFactory(final MapFactory mapFactory) {
        requireNonNull(mapFactory, "mapFactory");
        if (this.mapFactory() == mapFactory) {
            return this;
        }
        return new AutoValue_ConfigurationOptions(mapFactory, this.header(), this.serializers(), this.nativeTypes(),
                this.shouldCopyDefaults(), this.implicitInitialization());
    }

    /**
     * Gets the header specified in these options.
     *
     * @return the current header. Lines are split by \n, with no
     *         trailing newline
     * @since 4.0.0
     */
    public abstract @Nullable String header();

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified
     * header set, and all other settings copied from this instance.
     *
     * @param header the new header to use
     * @return the new options object
     * @since 4.0.0
     */
    public ConfigurationOptions header(final @Nullable String header) {
        if (Objects.equals(this.header(), header)) {
            return this;
        }
        return new AutoValue_ConfigurationOptions(this.mapFactory(), header, this.serializers(), this.nativeTypes(),
                this.shouldCopyDefaults(), this.implicitInitialization());
    }

    /**
     * Gets the {@link TypeSerializerCollection} specified in these options.
     *
     * @return the type serializers
     * @since 4.0.0
     */
    public abstract TypeSerializerCollection serializers();

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified {@link TypeSerializerCollection}
     * set, and all other settings copied from this instance.
     *
     * @param serializers the serializers to use
     * @return the new options object
     * @since 4.0.0
     */
    public ConfigurationOptions serializers(final TypeSerializerCollection serializers) {
        requireNonNull(serializers, "serializers");
        if (this.serializers().equals(serializers)) {
            return this;
        }
        return new AutoValue_ConfigurationOptions(this.mapFactory(), this.header(), serializers, this.nativeTypes(),
                this.shouldCopyDefaults(), this.implicitInitialization());
    }

    /**
     * Creates a new {@link ConfigurationOptions} instance, with a new
     * {@link TypeSerializerCollection} created as a child of this options'
     * current collection. The provided function will be called with the builder
     * for this new collection to allow registering more type serializers.
     *
     * @param serializerBuilder accepts a builder for the collection that will
     *                          be used in the returned options object.
     * @return the new options object
     * @since 4.0.0
     */
    public final ConfigurationOptions serializers(final Consumer<TypeSerializerCollection.Builder> serializerBuilder) {
        requireNonNull(serializerBuilder, "serializerBuilder");
        final TypeSerializerCollection.Builder builder = this.serializers().childBuilder();
        serializerBuilder.accept(builder);
        return this.serializers(builder.build());
    }

    @SuppressWarnings("AutoValueImmutableFields") // we don't use guava
    abstract @Nullable Set<Class<?>> nativeTypes();

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified native types
     * set, and all other settings copied from this instance.
     *
     * <p>Native types are format-dependent, and must be provided by a
     * configuration loader's {@link ConfigurationLoader#defaultOptions() default options}</p>
     *
     * <p>Null indicates that all types are accepted.</p>
     *
     * @param nativeTypes the types that will be accepted to a
     *                     call to {@link ConfigurationNode#set(Object)}
     * @return updated options object
     * @since 4.0.0
     */
    public ConfigurationOptions nativeTypes(final @Nullable Set<Class<?>> nativeTypes) {
        if (Objects.equals(this.nativeTypes(), nativeTypes)) {
            return this;
        }
        return new AutoValue_ConfigurationOptions(this.mapFactory(), this.header(), this.serializers(),
                nativeTypes == null ? null : UnmodifiableCollections.copyOf(nativeTypes), this.shouldCopyDefaults(), this.implicitInitialization());
    }

    /**
     * Gets whether objects of the provided type are natively accepted as values
     * for nodes with this as their options object.
     *
     * @param type the type to check
     * @return whether the type is accepted
     * @since 4.0.0
     */
    public final boolean acceptsType(final Class<?> type) {
        requireNonNull(type, "type");

        final @Nullable Set<Class<?>> nativeTypes = this.nativeTypes();

        if (nativeTypes == null) {
            return true;
        }

        if (nativeTypes.contains(type)) {
            return true;
        }

        if (type.isPrimitive() && nativeTypes.contains(Types.box(type))) {
            return true;
        }

        final Type unboxed = Types.unbox(type);
        if (unboxed != type && nativeTypes.contains(unboxed)) {
            return true;
        }

        for (final Class<?> clazz : nativeTypes) {
            if (clazz.isAssignableFrom(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets whether or not default parameters provided to {@link ConfigurationNode} getter methods
     * should be set to the node when used.
     *
     * @return whether defaults should be copied into value
     * @since 4.0.0
     */
    public abstract boolean shouldCopyDefaults();

    /**
     * Creates a new {@link ConfigurationOptions} instance, with the specified
     * 'copy defaults' setting set, and all other settings copied from
     * this instance.
     *
     * @param shouldCopyDefaults whether to copy defaults
     * @return updated options object
     * @see #shouldCopyDefaults() for information on what this method does
     * @since 4.0.0
     */
    public ConfigurationOptions shouldCopyDefaults(final boolean shouldCopyDefaults) {
        if (this.shouldCopyDefaults() == shouldCopyDefaults) {
            return this;
        }

        return new AutoValue_ConfigurationOptions(this.mapFactory(), this.header(), this.serializers(), this.nativeTypes(),
                shouldCopyDefaults, this.implicitInitialization());
    }

    /**
     * Get whether values should be implicitly initialized.
     *
     * <p>When this is true, any value get operations will return an empty value
     * rather than null. This extends through to fields loaded into
     * object-mapped classes.</p>
     *
     * <p>This option is enabled by default.</p>
     *
     * @return if implicit initialization is enabled.
     * @since 4.0.0
     */
    public abstract boolean implicitInitialization();

    /**
     * Create a new {@link ConfigurationOptions} instance with the specified
     * implicit initialization setting.
     *
     * @param implicitInitialization whether to initialize implicitly
     * @return a new options object
     * @see #implicitInitialization() for more details
     * @since 4.0.0
     */
    public ConfigurationOptions implicitInitialization(final boolean implicitInitialization) {
        if (this.implicitInitialization() == implicitInitialization) {
            return this;
        }

        return new AutoValue_ConfigurationOptions(this.mapFactory(), this.header(), this.serializers(), this.nativeTypes(),
                this.shouldCopyDefaults(), implicitInitialization);
    }

}

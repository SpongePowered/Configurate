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
package org.spongepowered.configurate.reference;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reactive.Publisher;
import org.spongepowered.configurate.reactive.TransactionalSubscriber;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

/**
 * An updating reference to a base configuration node.
 *
 * @param <N> the type of node to work with
 * @since 4.0.0
 */
public interface ConfigurationReference<N extends ConfigurationNode> extends AutoCloseable {

    /**
     * Create a new configuration reference that will only update when loaded.
     *
     * @param loader the loader to load and save from
     * @param <N> the type of node
     * @return the newly created reference, with an initial load performed
     * @throws ConfigurateException if the configuration contained fails to load
     * @since 4.0.0
     */
    static <N extends ScopedConfigurationNode<N>> ConfigurationReference<N>
            fixed(final ConfigurationLoader<? extends N> loader) throws ConfigurateException {
        final ConfigurationReference<N> ret = new ManualConfigurationReference<>(loader, ForkJoinPool.commonPool());
        ret.load();
        return ret;
    }

    /**
     * Create a new configuration reference that will automatically update when
     * triggered by the provided {@link WatchServiceListener}.
     *
     * @param loaderCreator a function that can create a {@link ConfigurationLoader}
     * @param file the file to load this configuration from
     * @param listener the watch service listener that will receive events
     * @param <T> the node type
     * @return the created reference
     * @throws ConfigurateException if the underlying loader fails to load
     *         a configuration
     * @see WatchServiceListener#listenToConfiguration(Function, Path)
     * @since 4.0.0
     */
    static <T extends ScopedConfigurationNode<T>> ConfigurationReference<T>
            watching(final Function<Path, ConfigurationLoader<? extends T>> loaderCreator, final Path file, final WatchServiceListener listener)
            throws ConfigurateException {
        final WatchingConfigurationReference<T> ret = new WatchingConfigurationReference<>(loaderCreator.apply(file), listener.taskExecutor);
        ret.load();
        ret.disposable(listener.listenToFile(file, ret));

        return ret;
    }

    /**
     * Reload a configuration using the provided loader.
     *
     * <p>If the load fails, this reference will continue pointing to old
     * configuration values.
     *
     * @throws ConfigurateException when an error occurs
     * @since 4.0.0
     */
    void load() throws ConfigurateException;

    /**
     * Save this configuration using the provided loader.
     *
     * @throws ConfigurateException when an error occurs in the underlying IO
     * @since 4.0.0
     */
    void save() throws ConfigurateException;

    /**
     * Update the configuration node pointed to by this reference, and save it
     * using the reference's loader.
     *
     * <p>Even if the loader fails to save this new node, the node pointed to by
     * this reference will be updated.
     *
     * @param newNode the new node to save
     * @throws ConfigurateException when an error occurs within the loader
     * @since 4.0.0
     */
    void save(ConfigurationNode newNode) throws ConfigurateException;

    /**
     * Save this configuration using the provided loader. Any errors will be
     * submitted to subscribers of the returned publisher.
     *
     * @return publisher providing an event when the save is complete
     * @since 4.0.0
     */
    Publisher<N> saveAsync();

    /**
     * Update this configuration using the provided function, returning a
     * {@link Publisher} which will complete with the result of the operation.
     * The update function will be called asynchronously, and will be saved
     * to this reference's loader when complete.
     *
     * @param updater update function
     * @return publisher providing an event when the update is complete
     * @since 4.0.0
     */
    Publisher<N> updateAsync(Function<N, ? extends N> updater);

    /**
     * Get the base node this reference refers to.
     *
     * @return the node
     * @since 4.0.0
     */
    N node();

    /**
     * Get the loader this reference uses to load and save its node.
     *
     * @return the loader
     * @since 4.0.0
     */
    ConfigurationLoader<? extends N> loader();

    /**
     * Get the node at the given path, relative to the root node.
     *
     * @param path the path, a series of path elements
     * @return a child node
     * @see ConfigurationNode#node(Object...)
     * @since 4.0.0
     */
    N get(Object... path);

    /**
     * Get the node at the given path, relative to the root node.
     *
     * @param path the path, a series of path elements
     * @return a child node
     * @see ConfigurationNode#node(Iterable)
     * @since 4.0.0
     */
    N get(Iterable<?> path);

    /**
     * Update the value of the node at the given path, using the root node as
     * a base.
     *
     * @param path the path to get the child at
     * @param value the value to set the child node to
     * @throws SerializationException when unable to write the provided value
     * @since 4.0.0
     */
    default void set(final Object[] path, final @Nullable Object value) throws SerializationException {
        this.node().node(path).set(value);
    }

    /**
     * Set the value of the node at {@code path} to the given value.
     *
     * <p>This uses the appropriate
     * {@link org.spongepowered.configurate.serialize.TypeSerializer} to
     * serialize the data if it's not directly supported by the
     * provided configuration.</p>
     *
     * @param path the path to set the value at
     * @param type the type of data to serialize
     * @param value the value to set
     * @param <T> the type parameter for the value
     * @throws IllegalArgumentException if a raw type is provided
     * @throws SerializationException if thrown by the serialization mechanism
     * @since 4.0.0
     */
    default <T> void set(final Object[] path, final Class<T> type, final @Nullable T value) throws SerializationException {
        this.node().node(path).set(type, value);
    }

    /**
     * Set the value of the node at {@code path} to the given value.
     *
     * <p>This uses the appropriate
     * {@link org.spongepowered.configurate.serialize.TypeSerializer} to
     * serialize the data if it's not directly supported by the
     * provided configuration.</p>
     *
     * @param path the path to set the value at
     * @param type the type of data to serialize
     * @param value the value to set
     * @param <T> the type parameter for the value
     * @throws SerializationException if thrown by the serialization mechanism
     * @since 4.0.0
     */
    default <T> void set(final Object[] path, final TypeToken<T> type, final @Nullable T value) throws SerializationException {
        this.node().node(path).set(type, value);
    }

    /**
     * Update the value of the node at the given path, using the root node as
     * a base.
     *
     * @param path the path to get the child at
     * @param value the value to set the child node to
     * @since 4.0.0
     */
    default void set(final NodePath path, final @Nullable Object value) throws SerializationException {
        this.node().node(path).set(value);
    }

    /**
     * Set the value of the node at {@code path} to the given value.
     *
     * <p>This uses the appropriate
     * {@link org.spongepowered.configurate.serialize.TypeSerializer} to
     * serialize the data if it's not directly supported by the
     * provided configuration.</p>
     *
     * @param path the path to set the value at
     * @param type the type of data to serialize
     * @param value the value to set
     * @param <T> the type parameter for the value
     * @throws SerializationException if thrown by the serialization mechanism
     * @since 4.0.0
     */
    default <T> void set(final NodePath path, final Class<T> type, final @Nullable T value) throws SerializationException {
        this.node().node(path).set(type, value);
    }

    /**
     * Set the value of the node at {@code path} to the given value.
     *
     * <p>This uses the appropriate
     * {@link org.spongepowered.configurate.serialize.TypeSerializer} to
     * serialize the data if it's not directly supported by the
     * provided configuration.</p>
     *
     * @param path the path to set the value at
     * @param type the type of data to serialize
     * @param value the value to set
     * @param <T> the type parameter for the value
     * @throws SerializationException if thrown by the serialization mechanism
     * @since 4.0.0
     */
    default <T> void set(final NodePath path, final TypeToken<T> type, final @Nullable T value) throws SerializationException {
        this.node().node(path).set(type, value);
    }

    default ValueReference<?, N> referenceTo(final Type type, final Object... path) throws SerializationException {
        return this.referenceTo(type, NodePath.of(path));
    }
    
    /**
     * Create a reference to the node at the provided path. The value will be
     * deserialized according to the provided TypeToken.
     *
     * <p>The returned reference will update with reloads of and changes to the
     * value of the provided configuration. Any serialization errors encountered
     * will be submitted to the {@link #errors()} stream.
     *
     * @param type the value's type
     * @param path the path from the root node to the node containing the value
     * @param <T>  the value type
     * @return a deserializing reference to the node at the given path
     * @throws SerializationException if a type serializer could not be found
     *         for the provided type
     * @since 4.0.0
     */
    default <T> ValueReference<T, N> referenceTo(final TypeToken<T> type, final Object... path) throws SerializationException {
        return this.referenceTo(type, NodePath.of(path));
    }

    /**
     * Create a reference to the node at the provided path. The value will be
     * deserialized according to type of the provided {@link Class}.
     *
     * <p>The returned reference will update with reloads of and changes to the
     * value of the provided configuration. Any serialization errors encountered
     * will be submitted to the {@link #errors()} stream.
     *
     * @param type the value's type
     * @param path the path from the root node to the node containing the value
     * @param <T>  the value type
     * @return a deserializing reference to the node at the given path
     * @throws SerializationException if a type serializer could not be found
     *         for the provided type
     * @since 4.0.0
     */
    default <T> ValueReference<T, N> referenceTo(final Class<T> type, final Object... path) throws SerializationException {
        return this.referenceTo(type, NodePath.of(path));
    }
    
    /**
     * Create a reference to the node at the provided path. The value will be
     * deserialized according to the provided {@link Type}.
     *
     * <p>The returned reference will update with reloads of and changes to the
     * value of the provided configuration. Any serialization errors encountered
     * will be submitted to the {@link #errors()} stream.
     *
     * @param type the value's type
     * @param path the path from the root node to the node containing the value
     * @return a deserializing reference to the node at the given path
     * @throws SerializationException if a type serializer could not be found
     *         for the provided type
     * @since TODO: version
     */
    default ValueReference<?, N> referenceTo(final Type type, final NodePath path) throws SerializationException {
        return this.referenceTo(type, path, null);
    }

    /**
     * Create a reference to the node at the provided path. The value will be
     * deserialized according to the provided {@link TypeToken}.
     *
     * <p>The returned reference will update with reloads of and changes to the
     * value of the provided configuration. Any serialization errors encountered
     * will be submitted to the {@link #errors()} stream.
     *
     * @param type the value's type
     * @param path the path from the root node to the node containing the value
     * @param <T> the value type
     * @return a deserializing reference to the node at the given path
     * @throws SerializationException if a type serializer could not be found
     *          for the provided type
     * @since 4.0.0
     */
    default <T> ValueReference<T, N> referenceTo(final TypeToken<T> type, final NodePath path) throws SerializationException {
        return this.referenceTo(type, path, null);
    }

    /**
     * Create a reference to the node at the provided path. The value will be
     * deserialized according to type of the provided {@link Class}.
     *
     * <p>The returned reference will update with reloads of and changes to the
     * value of the provided configuration. Any serialization errors encountered
     * will be submitted to the {@link #errors()} stream.
     *
     * @param type the value's type
     * @param path the path from the root node to the node containing the value
     * @param <T> the value type
     * @return a deserializing reference to the node at the given path
     * @throws SerializationException if a type serializer could not be found
     *          for the provided type
     * @since 4.0.0
     */
    default <T> ValueReference<T, N> referenceTo(final Class<T> type, final NodePath path) throws SerializationException {
        return this.referenceTo(type, path, null);
    }

    /**
     * Create a reference to the node at the provided path. The value will be
     * deserialized according to the provided {@link Type}.
     *
     * <p>The returned reference will update with reloads of and changes to the
     * value of the provided configuration. Any serialization errors encountered
     * will be submitted to the {@link #errors()} stream.
     *
     * @param type the value's type
     * @param path the path from the root node to the node containing the value
     * @return a deserializing reference to the node at the given path
     * @throws SerializationException if a type serializer could not be found
     *          for the provided type
     * @since TODO: version
     */
    ValueReference<?, N> referenceTo(Type type, NodePath path, @Nullable Object defaultValue) throws SerializationException;

    /**
     * Create a reference to the node at the provided path. The value will be
     * deserialized according to the provided {@link TypeToken}.
     *
     * <p>The returned reference will update with reloads of and changes to the
     * value of the provided configuration. Any serialization errors encountered
     * will be submitted to the {@link #errors()} stream.
     *
     * @param type the value's type.
     * @param path the path from the root node to the node containing the value
     * @param defaultValue the value to use when there is no data present
     *                     in the targeted node.
     * @param <T> the value type
     * @return a deserializing reference to the node at the given path
     * @throws SerializationException if a type serializer could not be found
     *         for the provided type
     * @since 4.0.0
     */
    <T> ValueReference<T, N> referenceTo(TypeToken<T> type, NodePath path, @Nullable T defaultValue) throws SerializationException;

    /**
     * Create a reference to the node at the provided path. The value will be
     * deserialized according to type of the provided {@link Class}.
     *
     * <p>The returned reference will update with reloads of and changes to the
     * value of the provided configuration. Any serialization errors encountered
     * will be submitted to the {@link #errors()} stream.
     *
     * @param type value's type
     * @param path path from the root node to the node containing the value
     * @param defaultValue value to use when there is no data present
     *                     in the targeted node.
     * @param <T> value type
     * @return a deserializing reference to the node at the given path
     * @throws SerializationException if a type serializer could not be found
     *          for the provided type
     * @since 4.0.0
     */
    <T> ValueReference<T, N> referenceTo(Class<T> type, NodePath path, @Nullable T defaultValue) throws SerializationException;

    /**
     * Access the {@link Publisher} that will broadcast update events, providing the newly created node. The returned
     * publisher will be transaction-aware, i.e. any {@link TransactionalSubscriber} attached will progress through
     * their phases appropriately
     *
     * @return the publisher
     * @since 4.0.0
     */
    Publisher<N> updates();

    /**
     * A stream that will receive errors that occur while loading or saving to
     * this reference.
     *
     * @return the publisher
     * @since 4.0.0
     */
    Publisher<Map.Entry<ErrorPhase, Throwable>> errors();

    /**
     * {@inheritDoc}
     */
    @Override
    void close();

    /**
     * Representing the phase where an error occurred.
     *
     * @since 4.0.0
     */
    enum ErrorPhase {
        /**
         * While loading a configuration node from an external location.
         *
         * @since 4.0.0
         */
        LOADING,
        /**
         * While saving a configuration node to an external location.
         *
         * @since 4.0.0
         */
        SAVING,
        /**
         * When no further detail is known.
         *
         * @since 4.0.0
         */
        UNKNOWN,
        /**
         * While deserializing a value.
         *
         * @since 4.0.0
         */
        VALUE;

    }

}

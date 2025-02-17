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

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reactive.Disposable;
import org.spongepowered.configurate.reactive.Subscriber;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

/**
 * A wrapper around NIO's {@link WatchService} that uses the provided watch key
 * to poll for changes, and calls listeners once an event occurs.
 *
 * <p>Some deduplication is performed because Windows can be fairly spammy with
 * its events, so one callback may receive multiple events at one time.</p>
 *
 * <p>Callback functions are {@link Subscriber Subscribers} that take the
 * {@link WatchEvent} as their parameter.</p>
 *
 * <p>Listening to a directory provides updates on the directory's immediate
 * children, but does not listen recursively.</p>
 *
 * @since 4.0.0
 */
public final class WatchServiceListener implements AutoCloseable {

    @SuppressWarnings("rawtypes") // IntelliJ says it's unnecessary, but the compiler shows warnings
    private static final WatchEvent.Kind<?>[] DEFAULT_WATCH_EVENTS = new WatchEvent.Kind[]{StandardWatchEventKinds.OVERFLOW,
        StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY};
    private static final int PARALLEL_THRESHOLD = 100;
    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new PrefixedNameThreadFactory("Configurate-WatchService", true);

    private final WatchService watchService;
    private volatile boolean open = true;
    private final Thread executor;
    final Executor taskExecutor;
    @SuppressWarnings("PMD.LooseCoupling") // we use implementation-specific API
    private final ConcurrentHashMap<Path, DirectoryListenerRegistration> activeListeners = new ConcurrentHashMap<>();
    private static final ThreadLocal<IOException> exceptionHolder = new ThreadLocal<>();

    /**
     * Returns a new builder for a WatchServiceListener to create a
     * customized listener.
     *
     * @return a new builder
     * @since 4.0.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new {@link WatchServiceListener} using a new cached thread pool
     * executor and the default filesystem.
     *
     * @return a new instance with default values
     * @throws IOException if a watch service cannot be created
     * @see #builder() for customization
     * @since 4.0.0
     */
    public static WatchServiceListener create() throws IOException {
        return new WatchServiceListener(DEFAULT_THREAD_FACTORY, FileSystems.getDefault(), ForkJoinPool.commonPool());
    }

    private WatchServiceListener(final ThreadFactory factory, final FileSystem fileSystem, final Executor taskExecutor) throws IOException {
        this.watchService = fileSystem.newWatchService();
        this.executor = factory.newThread(() -> {
            while (this.open) {
                final WatchKey key;
                try {
                    key = this.watchService.take();
                } catch (final InterruptedException e) {
                    this.open = false;
                    Thread.currentThread().interrupt();
                    break;
                } catch (final ClosedWatchServiceException e) {
                    break;
                }
                final Path watched = (Path) key.watchable();
                final DirectoryListenerRegistration registration = this.activeListeners.get(watched);
                if (registration != null) {
                    final Set<Object> seenContexts = new HashSet<>();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (!key.isValid()) {
                            break;
                        }

                        if (!seenContexts.add(event.context())) {
                            continue;
                        }

                        // Process listeners
                        registration.submit(event);
                        if (registration.closeIfEmpty()) {
                            key.cancel();
                            break;
                        }
                    }

                    // If the watch key is no longer valid, send all listeners a close event
                    if (!key.reset()) {
                        final DirectoryListenerRegistration oldListeners = this.activeListeners.remove(watched);
                        oldListeners.onClose();
                    }
                }
                try {
                    Thread.sleep(20);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        this.taskExecutor = taskExecutor;
        this.executor.start();
    }

    /**
     * Gets or creates a registration holder for a specific directory. This
     * handles registering with the watch service if necessary.
     *
     * @param directory the directory to listen to
     * @return a registration, created new if necessary.
     * @throws ConfigurateException if produced while registering the path with
     *          our WatchService
     */
    private DirectoryListenerRegistration registration(final Path directory) throws ConfigurateException {
        final @Nullable DirectoryListenerRegistration reg = this.activeListeners.computeIfAbsent(directory, dir -> {
            try {
                return new DirectoryListenerRegistration(dir.register(this.watchService, DEFAULT_WATCH_EVENTS), this.taskExecutor);
            } catch (final IOException ex) {
                exceptionHolder.set(ex);
                return null;
            }
        });

        if (reg == null) {
            throw new ConfigurateException("While adding listener for " + directory, exceptionHolder.get());
        }
        return reg;
    }

    /**
     * Listen for changes to a specific file or directory.
     *
     * @param file the path of the file or directory to listen for changes on.
     * @param callback a subscriber that will be notified when changes occur.
     * @return a {@link Disposable} that can be used to cancel this subscription
     * @throws ConfigurateException if a filesystem error occurs.
     * @throws IllegalArgumentException if the provided path is a directory.
     * @since 4.0.0
     */
    public Disposable listenToFile(Path file, final Subscriber<WatchEvent<?>> callback) throws ConfigurateException, IllegalArgumentException {
        file = file.toAbsolutePath();
        if (Files.isDirectory(file)) {
            throw new IllegalArgumentException("Path " + file + " must be a file");
        }

        final Path fileName = file.getFileName();
        return registration(file.getParent()).subscribe(fileName, callback);
    }

    /**
     * Listen to a directory. Callbacks will receive events both for the
     * directory and for its contents.
     *
     * @param directory the directory to listen to
     * @param callback a subscriber that will be notified when changes occur.
     * @return a {@link Disposable} that can be used to cancel this subscription
     * @throws ConfigurateException when an error occurs registering with the
     *                              underlying watch service.
     * @throws IllegalArgumentException if the provided path is not a directory
     * @since 4.0.0
     */
    public Disposable listenToDirectory(Path directory, final Subscriber<WatchEvent<?>> callback)
            throws ConfigurateException, IllegalArgumentException {
        directory = directory.toAbsolutePath();
        if (!(Files.isDirectory(directory) || !Files.exists(directory))) {
            throw new IllegalArgumentException("Path " + directory + " must be a directory");
        }

        return registration(directory).subscribe(callback);
    }

    /**
     * Create a new {@link ConfigurationReference} subscribed to FS updates.
     *
     * @param loaderFunc function that will create a new loader
     * @param path path to to for changes
     * @param <N> node type
     * @return new reference
     * @throws ConfigurateException if unable to complete an initial load of
     *      the configuration.
     * @since 4.0.0
     */
    public <N extends ScopedConfigurationNode<N>> ConfigurationReference<N>
        listenToConfiguration(final Function<Path, ConfigurationLoader<? extends N>> loaderFunc, final Path path) throws ConfigurateException {
        return ConfigurationReference.watching(loaderFunc, path, this);
    }

    @Override
    public void close() throws IOException {
        this.open = false;
        this.watchService.close();
        this.activeListeners.forEachValue(PARALLEL_THRESHOLD, DirectoryListenerRegistration::onClose);
        this.activeListeners.clear();
        try {
            this.executor.interrupt();
            this.executor.join();
        } catch (final InterruptedException e) {
            throw new IOException("Failed to await termination of executor thread!");
        }
    }

    /**
     * Set the parameters needed to create a {@link WatchServiceListener}. All params are optional and defaults will be
     * used if no values are specified.
     *
     * @since 4.0.0
     */
    public static final class Builder {

        private @Nullable ThreadFactory threadFactory;
        private @Nullable FileSystem fileSystem;
        private @Nullable Executor taskExecutor;

        private Builder() { }

        /**
         * Set the thread factory that will be used to create the polling thread
         * for the returned watch service.
         *
         * @param factory the thread factory to use to create the deamon thread
         * @return this builder
         * @since 4.0.0
         */
        public Builder threadFactory(final ThreadFactory factory) {
            this.threadFactory = requireNonNull(factory, "factory");
            return this;
        }

        /**
         * Set the executor that will be used to execute tasks queued based on
         * received events. By default, the
         * {@link ForkJoinPool#commonPool() common pool} is used.
         *
         * @param executor the executor to use
         * @return this builder
         * @since 4.0.0
         */
        public Builder taskExecutor(final Executor executor) {
            this.taskExecutor = requireNonNull(executor, "executor");
            return this;
        }

        /**
         * Set the filesystem expected to be used for paths. A separate
         * {@link WatchServiceListener} should be created to listen to events on
         * each different file system.
         *
         * @param system the file system to use.
         * @return this builder
         * @since 4.0.0
         */
        public Builder fileSystem(final FileSystem system) {
            this.fileSystem = system;
            return this;
        }

        /**
         * Create a new listener, using default values for any unset parameters.
         *
         * @return a newly created executor
         * @throws IOException if thrown by {@link WatchServiceListener}'s constructor
         * @since 4.0.0
         */
        public WatchServiceListener build() throws IOException {
            if (this.threadFactory == null) {
                this.threadFactory = DEFAULT_THREAD_FACTORY;
            }

            if (this.fileSystem == null) {
                this.fileSystem = FileSystems.getDefault();
            }

            if (this.taskExecutor == null) {
                this.taskExecutor = ForkJoinPool.commonPool();
            }

            return new WatchServiceListener(this.threadFactory, this.fileSystem, this.taskExecutor);
        }

    }

}

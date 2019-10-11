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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reactive.Disposable;
import org.spongepowered.configurate.reactive.Subscriber;

import java.io.IOException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

/**
 * A wrapper around NIO's {@link WatchService} that uses the provided event loop to poll for changes, and calls listeners once an event occurs.
 *
 * Some deduplication is performed because Windows can be fairly spammy with its events, so one callback may receive multiple events at one time.
 *
 * Callback functions take a
 */
public class WatchServiceListener implements AutoCloseable {
    @SuppressWarnings("rawtypes") // IntelliJ says it's unnecessary, but the compiler shows warnings
    private static final WatchEvent.Kind<?>[] DEFAULT_WATCH_EVENTS = new WatchEvent.Kind[] {StandardWatchEventKinds.OVERFLOW,
            StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY};
    private static final int PARALLEL_THRESHOLD = 100;
    private final ExecutorService executor;
    private final WatchService watchService;
    private final ConcurrentHashMap<Path, DirectoryListenerRegistration> activeListeners = new ConcurrentHashMap<>();
    private static final ThreadLocal<IOException> exceptionHolder = new ThreadLocal<>();

    /**
     * Create a new builder for a WatchServiceListener to create a customized listener
     * @return A builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new {@link WatchServiceListener} using a new cached thread pool executor and the default filesystem.
     *
     * @return A new instance with default values
     * @throws IOException If a watch service cannot be created
     * @see #builder() for customization
     */
    public static WatchServiceListener create() throws IOException {
        return new WatchServiceListener(ForkJoinPool.commonPool(), FileSystems.getDefault());
    }

    private WatchServiceListener(ExecutorService executor, FileSystem fileSystem) throws IOException {
        this.executor = executor;
        this.watchService = fileSystem.newWatchService();
        executor.submit(() -> {
            while (!executor.isShutdown()) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    break;
                }
                Path watched = (Path) key.watchable();
                DirectoryListenerRegistration registration = activeListeners.get(watched);
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
                        executor.submit(() -> {
                            registration.submit(event);
                            if (registration.closeIfEmpty()) {
                                key.cancel();
                            }
                        });
                    }

                    // If the watch key is no longer valid, send all listeners a close event
                    if (!key.reset()) {
                        DirectoryListenerRegistration oldListeners = activeListeners.remove(watched);
                        oldListeners.onClose();
                    }
                }
            }
        });
    }

    /**
     * Gets or creates a registration holder for a specific directory. This handles registering with the watch service if necessary.
     *
     * @param directory The directory to listen to
     * @return A registration, created new if necessary.
     * @throws IOException If produced while registering the path with our WatchService
     */
    private DirectoryListenerRegistration getRegistration(Path directory) throws IOException {
        @Nullable DirectoryListenerRegistration reg = activeListeners.computeIfAbsent(directory, dir -> {
            try {
                return new DirectoryListenerRegistration(dir.register(watchService, DEFAULT_WATCH_EVENTS));
            } catch (IOException ex) {
                exceptionHolder.set(ex);
                return null;
            }
        });

        if (reg == null) {
            throw exceptionHolder.get();
        }
        return reg;
    }

    /**
     * Listen for changes to a specific file or directory.
     *
     * @param file The path of the file or directory to listen for changes on.
     * @param callback A callback function that will be called when changes are made. If return value is false, we will stop monitoring for changes.
     * @return A {@link Disposable} that can be used to cancel this subscription
     * @throws IOException if a filesystem error occurs.
     * @throws IllegalArgumentException if the provided path is a directory.
     */
    public Disposable listenToFile(Path file, Subscriber<WatchEvent<?>> callback) throws IOException, IllegalArgumentException {
        if (Files.isDirectory(file)) {
            throw new IllegalArgumentException("Path " + file + " must be a file");
        }

        Path fileName = file.getFileName();
        return getRegistration(file.getParent()).subscribe(fileName, callback);
    }

    /**
     * Listen to a directory. Callbacks will receive events both for the directory and for its contents.
     *
     * @param directory The directory to listen to
     * @param callback A callback function that will be called when changes are made. If return value is false, we will stop monitoring for changes.
     * @return A {@link Disposable} that can be used to cancel this subscription
     * @throws IOException When an error occurs registering with the underlying watch service.
     * @throws IllegalArgumentException If the provided path is not a directory
     */
    public Disposable listenToDirectory(Path directory, Subscriber<WatchEvent<?>> callback) throws IOException, IllegalArgumentException {
        if (!(Files.isDirectory(directory) || !Files.exists(directory))) {
            throw new IllegalArgumentException("Path " + directory + " must be a directory");
        }

        return getRegistration(directory).subscribe(callback);
    }

    public <N extends ScopedConfigurationNode<N>> ConfigurationReference<N> listenToConfiguration(Function<Path, ConfigurationLoader<N>> loaderFunc, Path path) throws IOException {
        return ConfigurationReference.createWatching(loaderFunc, path, this);
    }

    @Override
    public void close() throws IOException {
        watchService.close();
        activeListeners.forEachValue(PARALLEL_THRESHOLD, DirectoryListenerRegistration::onClose);
        activeListeners.clear();
    }

    /**
     * Set the parameters needed to create a {@link WatchServiceListener}. All params are optional
     * and defaults will be used if no values are specified.
     */
    public static class Builder {
        private @Nullable ExecutorService executor;
        private @Nullable FileSystem fileSystem;

        private Builder() {

        }

        /**
         * Set the executor to be used for polling and callbacks.
         *
         * If an executor is provided, it will must be managed outside of the watch service. If no executor is set, one will be created by the watch service.
         *
         * @param executor The executor to be used for the created service
         * @return thus
         */
        public Builder setExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Set the filesystem expected to be used for paths. A separate {@link WatchServiceListener} should be
         * created to listen to events on a different file system.
         *
         * @param system The file system to use.
         * @return this
         */
        public Builder setFileSystem(FileSystem system) {
            this.fileSystem = system;
            return this;
        }

        /**
         * Create a new listener, using default values for any unset parameters.
         *
         * @return A newly created executor
         * @throws IOException if thrown by {@link WatchServiceListener}'s constructor
         */
        public WatchServiceListener build() throws IOException {
            if (executor == null) {
                executor = ForkJoinPool.commonPool();
                assert executor != null; // shush
            }

            if (fileSystem == null) {
                fileSystem = FileSystems.getDefault();
            }

            return new WatchServiceListener(executor, fileSystem);
        }
    }
}

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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;
import java.util.function.Function;

/**
 * A wrapper around NIO's {@link WatchService} that uses the provided event loop to poll for changes, and calls
 * listeners once an event occurs.
 * <p>
 * Some deduplication is performed because Windows can be fairly spammy with its events, so one callback may receive
 * multiple events at one time.
 * <p>
 * Callback functions are {@link Subscriber Subscribers} that take the {@link WatchEvent} as their parameter.
 *
 * Listening to a directory provides updates on the directory's immediate children, but does not
 */
public class WatchServiceListener implements AutoCloseable {
    @SuppressWarnings("rawtypes") // IntelliJ says it's unnecessary, but the compiler shows warnings
    private static final WatchEvent.Kind<?>[] DEFAULT_WATCH_EVENTS = new WatchEvent.Kind[]{StandardWatchEventKinds.OVERFLOW,
            StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY};
    private static final int PARALLEL_THRESHOLD = 100;
    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new PrefixedNameThreadFactory("Configurate-WatchService", true);

    private final WatchService watchService;
    private volatile boolean open = true;
    private final Thread executor;
    private final ConcurrentHashMap<Path, DirectoryListenerRegistration> activeListeners = new ConcurrentHashMap<>();
    private static final ThreadLocal<IOException> exceptionHolder = new ThreadLocal<>();

    /**
     * Create a new builder for a WatchServiceListener to create a customized listener
     *
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
        return new WatchServiceListener(DEFAULT_THREAD_FACTORY, FileSystems.getDefault());
    }

    private WatchServiceListener(ThreadFactory factory, FileSystem fileSystem) throws IOException {
        this.watchService = fileSystem.newWatchService();
        this.executor = factory.newThread(() -> {
            while (open) {
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
                        registration.submit(event);
                        if (registration.closeIfEmpty()) {
                            key.cancel();
                            break;
                        }
                    }

                    // If the watch key is no longer valid, send all listeners a close event
                    if (!key.reset()) {
                        DirectoryListenerRegistration oldListeners = activeListeners.remove(watched);
                        oldListeners.onClose();
                    }
                }
            }
        });
        this.executor.start();
    }

    /**
     * Gets or creates a registration holder for a specific directory. This handles registering with the watch service
     * if necessary.
     *
     * @param directory The directory to listen to
     * @return A registration, created new if necessary.
     * @throws IOException If produced while registering the path with our WatchService
     */
    private DirectoryListenerRegistration getRegistration(Path directory) throws IOException {
        @Nullable DirectoryListenerRegistration reg = activeListeners.computeIfAbsent(directory, dir -> {
            try {
                return new DirectoryListenerRegistration(dir.register(watchService, DEFAULT_WATCH_EVENTS), ForkJoinPool.commonPool());
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
     * @param file     The path of the file or directory to listen for changes on.
     * @param callback A callback function that will be called when changes are made. If return value is false, we will
     *                 stop monitoring for changes.
     * @return A {@link Disposable} that can be used to cancel this subscription
     * @throws IOException              if a filesystem error occurs.
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
     * @param callback  A callback function that will be called when changes are made. If return value is false, we will
     *                  stop monitoring for changes.
     * @return A {@link Disposable} that can be used to cancel this subscription
     * @throws IOException              When an error occurs registering with the underlying watch service.
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
        open = false;
        watchService.close();
        activeListeners.forEachValue(PARALLEL_THRESHOLD, DirectoryListenerRegistration::onClose);
        activeListeners.clear();
        try {
            this.executor.join();
        } catch (InterruptedException e) {
            throw new IOException("Failed to await termination of executor thread!");
        }
    }

    /**
     * Set the parameters needed to create a {@link WatchServiceListener}. All params are optional and defaults will be
     * used if no values are specified.
     */
    public static class Builder {
        private @Nullable ThreadFactory threadFactory;
        private @Nullable FileSystem fileSystem;

        private Builder() {

        }

        /**
         * Set the thread factory that will be used to create the polling thread for this watch service
         *
         * @param factory The thread factory to create the deamon thread
         * @return this
         */
        public Builder setThreadFactory(ThreadFactory factory) {
            this.threadFactory = factory;
            return this;
        }

        /**
         * Set the filesystem expected to be used for paths. A separate {@link WatchServiceListener} should be created
         * to listen to events on a different file system.
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
            if (threadFactory == null) {
                threadFactory = DEFAULT_THREAD_FACTORY;
            }

            if (fileSystem == null) {
                fileSystem = FileSystems.getDefault();
            }

            return new WatchServiceListener(threadFactory, fileSystem);
        }
    }
}

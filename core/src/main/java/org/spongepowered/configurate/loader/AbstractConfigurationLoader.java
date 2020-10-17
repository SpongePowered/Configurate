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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Base class for many stream-based configuration loaders. This class provides
 * conversion from a variety of input sources to {@link BufferedReader}
 * suppliers, providing a consistent API for loaders to read from and write to.
 *
 * <p>Either the source or sink may be null. If this is true, this loader may
 * not support either loading or saving. In this case, implementing classes are
 * expected to throw an {@link IOException} for the unsupported operation.</p>
 *
 * @param <N> the {@link ConfigurationNode} type produced by the loader
 */
public abstract class AbstractConfigurationLoader<N extends ScopedConfigurationNode<N>> implements ConfigurationLoader<N> {

    /**
     * The escape sequence used by Configurate to separate comment lines.
     */
    public static final String CONFIGURATE_LINE_SEPARATOR = "\n";

    /**
     * A pattern that will match line breaks in comments.
     */
    public static final Pattern CONFIGURATE_LINE_PATTERN = Pattern.compile(CONFIGURATE_LINE_SEPARATOR);

    /**
     * The line separator used by the system.
     *
     * @see System#lineSeparator()
     */
    protected static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();

    /**
     * The reader source for this loader.
     *
     * <p>Can be null (for loaders which don't support loading!)</p>
     */
    protected final @Nullable Callable<BufferedReader> source;

    /**
     * The writer sink for this loader.
     *
     * <p>Can be null (for loaders which don't support saving!)</p>
     */
    protected final @Nullable Callable<BufferedWriter> sink;

    /**
     * The comment handlers defined for this loader.
     */
    private final List<CommentHandler> commentHandlers;

    /**
     * The mode used to read/write configuration headers.
     */
    private final HeaderMode headerMode;

    /**
     * The default {@link ConfigurationOptions} used by this loader.
     */
    private final ConfigurationOptions defaultOptions;

    protected AbstractConfigurationLoader(final Builder<?, ?> builder, final CommentHandler[] commentHandlers) {
        this.source = builder.source();
        this.sink = builder.sink();
        this.headerMode = builder.headerMode();
        this.commentHandlers = UnmodifiableCollections.toList(commentHandlers);
        this.defaultOptions = builder.defaultOptions();
    }

    /**
     * Gets the primary {@link CommentHandler} used by this loader.
     *
     * @return the default comment handler
     */
    public CommentHandler defaultCommentHandler() {
        return this.commentHandlers.get(0);
    }

    @Override
    public ConfigurationReference<N> loadToReference() throws ConfigurateException {
        return ConfigurationReference.fixed(this);
    }

    @Override
    public N load(ConfigurationOptions options) throws ParsingException {
        if (this.source == null) {
            throw new ParsingException(-1, -1, "", "No source present to read from!", null);
        }
        try (BufferedReader reader = this.source.call()) {
            if (this.headerMode == HeaderMode.PRESERVE || this.headerMode == HeaderMode.NONE) {
                final @Nullable String comment = CommentHandlers.extractComment(reader, this.commentHandlers);
                if (comment != null && comment.length() > 0) {
                    options = options.header(comment);
                }
            }
            final N node = createNode(options);
            loadInternal(node, reader);
            return node;
        } catch (final ParsingException ex) {
            throw ex;
        } catch (final FileNotFoundException | NoSuchFileException e) {
            // Squash -- there's nothing to read
            return createNode(options);
        } catch (final IOException e) {
            throw new ParsingException(-1, -1, options.header(), null, e);
        } catch (final Exception e) {
            throw new ParsingException(-1, -1, options.header(), "Unknown error occurred while loading", e);
        }
    }

    protected abstract void loadInternal(N node, BufferedReader reader) throws ParsingException;

    @Override
    public void save(final ConfigurationNode node) throws ConfigurateException {
        if (this.sink == null) {
            throw new ConfigurateException(node, "No sink present to write to!");
        }
        try (Writer writer = this.sink.call()) {
            writeHeaderInternal(writer);
            if (this.headerMode != HeaderMode.NONE) {
                final @Nullable String header = node.options().header();
                if (header != null && !header.isEmpty()) {
                    final Iterator<String> lines = defaultCommentHandler().toComment(CONFIGURATE_LINE_PATTERN.splitAsStream(header)).iterator();
                    while (lines.hasNext()) {
                        writer.write(lines.next());
                        writer.write(SYSTEM_LINE_SEPARATOR);
                    }
                    writer.write(SYSTEM_LINE_SEPARATOR);
                }
            }
            saveInternal(node, writer);
        } catch (final ConfigurateException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new ConfigurateException(node, ex);
        }
    }

    protected void writeHeaderInternal(final Writer writer) throws IOException {}

    protected abstract void saveInternal(ConfigurationNode node, Writer writer) throws ConfigurateException;

    @Override
    public ConfigurationOptions defaultOptions() {
        return this.defaultOptions;
    }

    @Override
    public final boolean canLoad() {
        return this.source != null;
    }

    @Override
    public final boolean canSave() {
        return this.sink != null;
    }

    /**
     * An abstract builder implementation for {@link AbstractConfigurationLoader}s.
     *
     * @param <T> the builder's own type (for chaining using generic types)
     */
    public abstract static class Builder<T extends Builder<T, L>, L extends AbstractConfigurationLoader<?>> {
        protected HeaderMode headerMode = HeaderMode.PRESERVE;
        protected @Nullable Callable<BufferedReader> source;
        protected @Nullable Callable<BufferedWriter> sink;
        protected ConfigurationOptions defaultOptions = ConfigurationOptions.defaults();

        protected Builder() {}

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        /**
         * Sets the sink and source of the resultant loader to the given file.
         *
         * <p>The {@link #source() source} is defined using
         * {@link Files#newBufferedReader(Path)} with UTF-8 encoding.</p>
         *
         * <p>The {@link #sink() sink} is defined using {@link AtomicFiles} with UTF-8
         * encoding.</p>
         *
         * @param file the configuration file
         * @return this builder (for chaining)
         */
        public T file(final File file) {
            return path(requireNonNull(file, "file").toPath());
        }

        /**
         * Sets the sink and source of the resultant loader to the given path.
         *
         * <p>The {@link #source() source} is defined using
         * {@link Files#newBufferedReader(Path)} with UTF-8 encoding.</p>
         *
         * <p>The {@link #sink() sink} is defined using {@link AtomicFiles} with UTF-8
         * encoding.</p>
         *
         * @param path the path of the configuration file
         * @return this builder (for chaining)
         */
        public T path(final Path path) {
            final Path absPath = requireNonNull(path, "path").toAbsolutePath();
            this.source = () -> Files.newBufferedReader(absPath, StandardCharsets.UTF_8);
            this.sink = AtomicFiles.atomicWriterFactory(absPath, StandardCharsets.UTF_8);
            return self();
        }

        /**
         * Sets the source of the resultant loader to the given URL.
         *
         * @param url the URL of the source
         * @return this builder (for chaining)
         */
        public T url(final URL url) {
            requireNonNull(url, "url");
            this.source = () -> new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8));
            return self();
        }

        /**
         * Sets the source of the resultant loader.
         *
         * <p>The "source" is used by the loader to load the configuration.</p>
         *
         * @param source the source
         * @return this builder (for chaining)
         */
        public T source(final @Nullable Callable<BufferedReader> source) {
            this.source = source;
            return self();
        }

        /**
         * Gets the source to be used by the resultant loader.
         *
         * @return the source
         */
        public @Nullable Callable<BufferedReader> source() {
            return this.source;
        }

        /**
         * Sets the sink of the resultant loader.
         *
         * <p>The "sink" is used by the loader to save the configuration.</p>
         *
         * @param sink the sink
         * @return this builder (for chaining)
         */
        public T sink(final @Nullable Callable<BufferedWriter> sink) {
            this.sink = sink;
            return self();
        }

        /**
         * Gets the sink to be used by the resultant loader.
         *
         * @return the sink
         */
        public @Nullable Callable<BufferedWriter> sink() {
            return this.sink;
        }

        /**
         * Sets the header mode of the resultant loader.
         *
         * @param mode the header mode
         * @return this builder (for chaining)
         */
        public T headerMode(final HeaderMode mode) {
            this.headerMode = requireNonNull(mode, "mode");
            return self();
        }

        /**
         * Gets the header mode to be used by the resultant loader.
         *
         * @return the header mode
         */
        public HeaderMode headerMode() {
            return this.headerMode;
        }

        /**
         * Sets the default configuration options to be used by the
         * resultant loader.
         *
         * @param defaultOptions the options
         * @return this builder (for chaining)
         */
        public T defaultOptions(final ConfigurationOptions defaultOptions) {
            this.defaultOptions = requireNonNull(defaultOptions, "defaultOptions");
            return self();
        }

        /**
         * Sets the default configuration options to be used by the resultant
         * loader by providing a function which takes the current default
         * options and applies any desired changes.
         *
         * @param defaultOptions to transform the existing default options
         * @return this builder (for chaining)
         */
        public T defaultOptions(final UnaryOperator<ConfigurationOptions> defaultOptions) {
            this.defaultOptions = requireNonNull(defaultOptions.apply(this.defaultOptions), "defaultOptions (updated)");
            return self();
        }

        /**
         * Gets the default configuration options to be used by the resultant
         * loader.
         *
         * @return the options
         */
        public ConfigurationOptions defaultOptions() {
            return this.defaultOptions;
        }

        /**
         * Builds the loader.
         *
         * @return a new loader
         */
        public abstract L build();

    }

}

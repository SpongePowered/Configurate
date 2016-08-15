/**
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
package ninja.leaping.configurate.loader;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Base class for many stream-based configuration loaders. This class provides conversion from a variety of input
 * sources to CharSource/Sink objects, providing a consistent API for loaders to read from and write to.
 *
 * Either the source or sink may be null. If this is true, this loader may not support either loading or saving. In
 * this case, implementing classes are expected to throw an IOException.
 */
public abstract class AbstractConfigurationLoader<NodeType extends ConfigurationNode> implements ConfigurationLoader<NodeType> {
    public static final String CONFIGURATE_LINE_SEPARATOR = "\n";
    protected static final Splitter LINE_SPLITTER = Splitter.on(CONFIGURATE_LINE_SEPARATOR);
    protected static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();
    protected final Callable<BufferedReader> source;
    private final Callable<BufferedWriter> sink;
    private final CommentHandler[] commentHandlers;
    private final HeaderMode headerMode;
    private final ConfigurationOptions defaultOptions;

    protected static abstract class Builder<T extends Builder> {
        protected HeaderMode headerMode = HeaderMode.PRESERVE;
        protected Callable<BufferedReader> source;
        protected Callable<BufferedWriter> sink;
        protected ConfigurationOptions defaultOptions = ConfigurationOptions.defaults();

        protected Builder() {}

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        public T setFile(File file) {
            return setPath(file.toPath());
        }

        public T setPath(Path path) {
            Path absPath = Objects.requireNonNull(path, "path").toAbsolutePath();
            this.source = () -> Files.newBufferedReader(absPath, UTF_8);
            this.sink = AtomicFiles.createAtomicWriterFactory(absPath, UTF_8);
            return self();
        }

        public T setURL(URL url) {
            this.source = () -> new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), UTF_8));
            return self();
        }

        public T setSource(Callable<BufferedReader> source) {
            this.source = source;
            return self();
        }

        public T setSink(Callable<BufferedWriter> sink) {
            this.sink = sink;
            return self();
        }

        public Callable<BufferedReader> getSource() {
            return this.source;
        }

        public Callable<BufferedWriter> getSink() {
            return this.sink;
        }

        @Deprecated
        public T setPreservesHeader(boolean preservesHeader) {
            this.headerMode = preservesHeader ? HeaderMode.PRESERVE : HeaderMode.PRESET;
            return self();
        }

        @Deprecated
        public boolean preservesHeader() {
            return this.headerMode == HeaderMode.PRESERVE;
        }

        public T setHeaderMode(HeaderMode mode) {
            this.headerMode = mode;
            return self();
        }

        public HeaderMode getHeaderMode() {
            return this.headerMode;
        }

        public T setDefaultOptions(ConfigurationOptions defaultOptions) {
            this.defaultOptions = Objects.requireNonNull(defaultOptions, "defaultOptions");
            return self();
        }

        public ConfigurationOptions getDefaultOptions() {
            return this.defaultOptions;
        }

        public abstract AbstractConfigurationLoader build();

    }

    protected AbstractConfigurationLoader(Builder<?> builder, CommentHandler[] commentHandlers) {
        this.source = builder.getSource();
        this.sink = builder.getSink();
        this.headerMode = builder.getHeaderMode();
        this.defaultOptions = builder.getDefaultOptions();
        this.commentHandlers = commentHandlers;
    }

    public CommentHandler getDefaultCommentHandler() {
        return this.commentHandlers[0];
    }

    @Override
    public NodeType load(ConfigurationOptions options) throws IOException {
        if (!canLoad()) {
            throw new IOException("No source present to read from!");
        }
        try (BufferedReader reader = source.call()) {
            NodeType node;
            if (headerMode == HeaderMode.PRESERVE || headerMode == HeaderMode.NONE) {
                String comment = CommentHandlers.extractComment(reader, commentHandlers);
                if (comment != null && comment.length() > 0) {
                    options = options.setHeader(comment);
                }
            }
            node = createEmptyNode(options);
            loadInternal(node, reader);
            return node;
        } catch (FileNotFoundException | NoSuchFileException e) {
            // Squash -- there's nothing to read
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException(e);
            }
        }
        return createEmptyNode(options);
    }

    protected abstract void loadInternal(NodeType node, BufferedReader reader) throws IOException;

    @Override
    public void save(ConfigurationNode node) throws IOException {
        if (!canSave()) {
            throw new IOException("No sink present to write to!");
        }
        try (Writer writer = sink.call()) {
            if (headerMode != HeaderMode.NONE) {
                String header = node.getOptions().getHeader();
                if (header != null && !header.isEmpty()) {
                    for (String line : getDefaultCommentHandler().toComment(ImmutableList.copyOf(LINE_SPLITTER.split(header)))) {
                        writer.write(line);
                        writer.write(SYSTEM_LINE_SEPARATOR);
                    }
                    writer.write(SYSTEM_LINE_SEPARATOR);
                }
            }
            saveInternal(node, writer);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException(e);
            }
        }
    }

    protected abstract void saveInternal(ConfigurationNode node, Writer writer) throws IOException;

    /**
     * Get the default options that any new nodes will be created with if no options object is passed.
     *
     * @return The default options
     */
    @Override
    public ConfigurationOptions getDefaultOptions() {
        return this.defaultOptions;
    }

    public boolean canLoad() {
        return this.source != null;
    }

    public boolean canSave() {
        return this.sink != null;
    }

}

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
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Base class for many stream-based configuration loaders. This class provides conversion from a variety of input
 * sources to CharSource/Sink objects, providing a consistent API for loaders to read from and write to.
 *
 * Either the source or sink may be null. If this is true, this loader may not support either loading or saving. In
 * this case, implementing classes are expected to throw an IOException.
 */
public abstract class AbstractConfigurationLoader<NodeType extends ConfigurationNode> implements ConfigurationLoader<NodeType> {
    protected static final Splitter LINE_SPLITTER = Splitter.on('\n');
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final Charset UTF8_CHARSET = Charset.forName("utf-8");
    static {
        assert UTF8_CHARSET != null; // If it is, there is a serious problem w/ this user's jdk installation
    }
    protected final CharSource source;
    private final CharSink sink;
    private final CommentHandler[] commentHandlers;
    private final boolean preservesHeader;

    protected static abstract class Builder<T extends Builder> {
        protected boolean preserveHeader = true;
        protected CharSource source;
        protected CharSink sink;

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        public T setFile(File file) {
            this.source = Files.asCharSource(file, UTF8_CHARSET);
            this.sink = AtomicFiles.asCharSink(file, UTF8_CHARSET);
            return self();
        }

        public T setURL(URL url) {
            this.source = Resources.asCharSource(url, UTF8_CHARSET);
            return self();
        }

        public T setSource(CharSource source) {
            this.source = source;
            return self();
        }

        public T setSink(CharSink sink) {
            this.sink = sink;
            return self();
        }

        public T setPreservesHeader(boolean preservesHeader) {
            this.preserveHeader = preservesHeader;
            return self();
        }

        public abstract AbstractConfigurationLoader build();
    }

    protected AbstractConfigurationLoader(CharSource source, CharSink sink, CommentHandler[] commentHandlers, boolean
            preservesHeader) {
        this.source = source;
        this.sink = sink;
        this.commentHandlers = commentHandlers;
        this.preservesHeader = preservesHeader;
    }

    public CommentHandler getDefaultCommentHandler() {
        return this.commentHandlers[0];
    }

    @Override
    public NodeType load() throws IOException {
        return load(ConfigurationOptions.defaults());
    }

    @Override
    public NodeType load(ConfigurationOptions options) throws IOException {
        if (!canLoad()) {
            throw new IOException("No source present to read from!");
        }
        try (BufferedReader reader = source.openBufferedStream()) {
            NodeType node;
            if (preservesHeader) {
                String comment = CommentHandlers.extractComment(reader, commentHandlers);
                if (comment != null && comment.length() > 0) {
                    options = options.setHeader(comment);
                }
            }
            node = createEmptyNode(options);
            loadInternal(node, reader);
            return node;
        } catch (FileNotFoundException e) {
            // Squash -- there's nothing to read
        }
        return createEmptyNode(options);
    }

    protected abstract void loadInternal(NodeType node, BufferedReader reader) throws IOException;

    @Override
    public void save(ConfigurationNode node) throws IOException {
        if (!canSave()) {
            throw new IOException("No sink present to write to!");
        }
        try (Writer writer = sink.openBufferedStream()) {
            String header = node.getOptions().getHeader();
            if (header != null && !header.isEmpty()) {
                for (String line : getDefaultCommentHandler().toComment(ImmutableList.copyOf(LINE_SPLITTER.split(header)))) {
                    writer.write(line);
                    writer.write(LINE_SEPARATOR);
                }
                writer.write(LINE_SEPARATOR);
            }
            saveInternal(node, writer);
        }
    }

    protected abstract void saveInternal(ConfigurationNode node, Writer writer) throws IOException;

    public boolean canLoad() {
        return this.source != null;
    }

    public boolean canSave() {
        return this.sink != null;
    }

}

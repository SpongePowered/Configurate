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
package org.spongepowered.configurate.yaml;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationVisitor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.events.CommentEvent;
import org.yaml.snakeyaml.events.DocumentEndEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.ImplicitTuple;
import org.yaml.snakeyaml.events.MappingEndEvent;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceEndEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.events.StreamEndEvent;
import org.yaml.snakeyaml.events.StreamStartEvent;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

final class YamlVisitor implements ConfigurationVisitor<YamlVisitor.State, Void, ConfigurateException> {

    private static final Pattern COMMENT_SPLIT = Pattern.compile("\r?\n");
    private static final CommentEvent WHITESPACE = new CommentEvent(
        CommentType.BLANK_LINE,
        YamlConfigurationLoader.CONFIGURATE_LINE_SEPARATOR,
        null,
        null
    );
    private static final CommentEvent COMMENT_BLANK_LINE = new CommentEvent(CommentType.BLANK_LINE, "", null, null);
    static final StreamStartEvent STREAM_START = new StreamStartEvent(null, null);
    static final StreamEndEvent STREAM_END = new StreamEndEvent(null, null);
    static final DocumentEndEvent DOCUMENT_END = new DocumentEndEvent(null, null, false);
    private static final SequenceEndEvent SEQUENCE_END = new SequenceEndEvent(null, null);
    private static final MappingEndEvent MAPPING_END = new MappingEndEvent(null, null);

    private final boolean shouldPadComments;
    private final TagRepository tags;

    YamlVisitor(final boolean shouldPadComments, final TagRepository tags) {
        this.shouldPadComments = shouldPadComments;
        this.tags = tags;
    }

    @Override
    public State newState() throws ConfigurateException {
        throw new ConfigurateException("States cannot be created as a writer must be provided");
    }

    @Override
    public void beginVisit(final ConfigurationNode node, final State state) {
        state.mapKeyHolder = BasicConfigurationNode.root(node.options());
    }

    @Override
    public void enterNode(final ConfigurationNode node, final State state) throws ConfigurateException {
        if (node instanceof CommentedConfigurationNodeIntermediary<?> && state.options.isProcessComments()) {
            final @Nullable String comment = ((CommentedConfigurationNodeIntermediary<? extends @NonNull Object>) node).comment();
            if (comment != null) {
                if (this.shouldPadComments && node != state.start) {
                    if (!state.first) {
                        if (!node.parent().isList()) {
                            state.emit(WHITESPACE);
                        }
                    } else {
                        state.first = false;
                    }
                }
                for (final String line : COMMENT_SPLIT.split(comment, -1)) {
                    if (line.isEmpty()) {
                        state.emit(COMMENT_BLANK_LINE);
                    } else {
                        if (line.codePointAt(0) != '#') { // allow lines that are only the comment character, for box drawing
                            state.emit(new CommentEvent(CommentType.BLOCK, " " + line, null, null));
                        } else {
                            state.emit(new CommentEvent(CommentType.BLOCK, line, null, null));
                        }
                    }
                }
            }
        }

        if (node != state.start && node.key() != null /* implies node.parent() != null */ && node.parent().isMap()) { // emit key
            state.mapKeyHolder.raw(node.key());
            state.mapKeyHolder.visit(this, state);
        }

        // detect null value
        if (node.empty() && node.raw() == null) {
            state.emit(new ScalarEvent(
                this.anchor(node),
                null,
                new ImplicitTuple(true, false),
                "",
                null,
                null,
                DumperOptions.ScalarStyle.PLAIN
            ));
        }
    }

    @Override
    public void enterMappingNode(final ConfigurationNode node, final State state) throws ConfigurateException {
        final TagRepository.AnalyzedTag analysis = this.tags.analyze(node);
        state.first = true;
        state.emit(new MappingStartEvent(
            this.anchor(node),
            analysis.actual().tagUri().toString(),
            analysis.implicit(),
            null,
            null,
            NodeStyle.asSnakeYaml(state.determineStyle(node))
        ));
    }

    @Override
    public void enterListNode(final ConfigurationNode node, final State state) throws ConfigurateException {
        final TagRepository.AnalyzedTag analysis = this.tags.analyze(node);
        state.emit(new SequenceStartEvent(
            this.anchor(node),
            analysis.actual().tagUri().toString(),
            analysis.implicit(),
            null,
            null,
            NodeStyle.asSnakeYaml(state.determineStyle(node))
        ));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void enterScalarNode(final ConfigurationNode node, final State state) throws ConfigurateException {
        // determine
        final TagRepository.AnalyzedTag analysis = this.tags.analyze(node);
        final ImplicitTuple implicity = new ImplicitTuple(analysis.implicit(), analysis.resolved().equals(this.tags.stringTag()));
        final Tag actual = analysis.actual();
        if (!(actual instanceof Tag.Scalar<?>)) {
            throw new ConfigurateException(
                node,
                "Tag '" + actual.tagUri() + "' is required to be a scalar tag, but was actually a " + actual.getClass()
            );
        }

        state.emit(new ScalarEvent(
            this.anchor(node),
            actual.tagUri().toString(),
            implicity,
            ((Tag.Scalar<Object>) actual).toString(node.rawScalar()),
            null,
            null,
            // todo: support configuring default scalar style
            ScalarStyle.asSnakeYaml(
                node.hint(YamlConfigurationLoader.SCALAR_STYLE),
                implicity,
                ((Tag.Scalar<?>) actual).preferredScalarStyle()
            )
        ));
    }

    // TODO: emit alias events for enterReferenceNode

    @Override
    public void exitMappingNode(final ConfigurationNode node, final State state) throws ConfigurateException {
        state.first = false; // only true if empty map
        state.emit(MAPPING_END);
    }

    @Override
    public void exitListNode(final ConfigurationNode node, final State state) throws ConfigurateException {
        state.emit(SEQUENCE_END);
    }

    @Override
    public Void endVisit(final State state) {
        return null;
    }

    private @Nullable String anchor(final ConfigurationNode node) {
        return node.hint(YamlConfigurationLoader.ANCHOR_ID);
    }

    static final class State {
        private final Emitter emit;
        final DumperOptions options;
        @Nullable ConfigurationNode start;
        final @Nullable NodeStyle defaultStyle;
        ConfigurationNode mapKeyHolder;
        boolean first; // reset to true at the beginning of each mapping node

        State(final DumperOptions options, final Writer writer, final @Nullable NodeStyle defaultStyle) {
            this.emit = new Emitter(writer, options);
            this.options = options;
            this.defaultStyle = defaultStyle;
        }

        @Nullable NodeStyle determineStyle(final ConfigurationNode node) {
            // todo: some basic rules:
            // - if a node has any children with comments, convert it to block style
            // - when the default style is `AUTO` and `flowLevel` == 0,
            final @Nullable NodeStyle style = node.hint(YamlConfigurationLoader.NODE_STYLE);
            return style == null ? this.defaultStyle : style;
        }

        public void emit(final Event event) throws ConfigurateException {
            try {
                this.emit.emit(event);
            } catch (final YAMLException | IOException ex) {
                throw new ConfigurateException(ex);
            }
        }
    }

}

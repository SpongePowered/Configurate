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

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationNodeFactory;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.ParsingException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.CollectionStartEvent;
import org.yaml.snakeyaml.events.CommentEvent;
import org.yaml.snakeyaml.events.DocumentStartEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.NodeEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.scanner.ScannerImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * One combined object that handles parsing into an event stream and composing
 * the node graph.
 */
final class YamlParserComposer extends ParserImpl {

    private static final int INITIAL_STACK_SIZE = 16;
    private static final int FRAME_STACK_INCREMENT = 8;

    private @Nullable StringBuilder commentCollector;
    private final boolean processComments;
    private final boolean stripLeadingCommentWhitespace = true;
    final Map<String, ConfigurationNode> aliases = new HashMap<>();
    final TagRepository tags;
    final Map<String, String> declaredTags = new HashMap<>();

    private Frame[] frames = new Frame[INITIAL_STACK_SIZE];
    private int framePointer = -1;

    YamlParserComposer(final StreamReader reader, final TagRepository tags, final boolean enableComments) {
        super(new ScannerImpl(reader).setAcceptTabs(true));
        this.processComments = enableComments;
        this.tags = tags;
    }

    // "api" //

    public void singleDocumentStream(final ConfigurationNode node) throws ParsingException {
        requireEvent(Event.ID.StreamStart);
        document(node);
        requireEvent(Event.ID.StreamEnd);
    }

    public void document(final ConfigurationNode node) throws ParsingException {
        if (this.processComments && node instanceof CommentedConfigurationNodeIntermediary<@NonNull ?>) {
            // Only collect comments if we can handle them in the first place
            this.scanner().setEmitComments(true);
        }

        if (peekEvent().is(Event.ID.StreamEnd)) {
            return;
        }

        Frame active = this.pushFrame(DocumentStart.INSTANCE);
        active.node = node;
        try {
            // parser loop
            while (this.framePointer >= 0) {
                active = this.peekFrame();
                if (this.peekEvent() == null) {
                    throw new IllegalStateException("Still within composer state loop while out of events!\n"
                        + "    Active state is: " + this.peekFrame().state.getClass());
                }
                if (active.state.accept(active, this) == null) {
                    // todo: validate non-null returns here? does it matter?
                    this.popFrame();
                }
            }
        } catch (final MarkedYAMLException ex) {
            throw new ParsingException(
                active.node,
                ex.getProblemMark().getLine(),
                ex.getProblemMark().getColumn(),
                ex.getProblemMark().get_snippet(),
                ex.getProblem()
            );
        } catch (final ConfigurateException ex) {
            ex.initPath(active.node::path);
            throw ex;
        } finally {
            this.scanner().setEmitComments(false);
            this.aliases.clear();
            this.declaredTags.clear();
        }
    }

    ScannerImpl scanner() {
        return (ScannerImpl) this.scanner;
    }

    // events //

    void requireEvent(final Event.ID type) throws ParsingException {
        final Event next = peekEvent();
        if (!next.is(type)) {
            throw makeError(next.getStartMark(), "Expected next event of type" + type + " but was " + next.getEventId(), null);
        }
        this.getEvent();
    }

    @SuppressWarnings("unchecked")
    <T extends Event> T requireEvent(final Event.ID type, final Class<T> clazz) throws ParsingException {
        final Event next = peekEvent();
        if (!next.is(type)) {
            throw makeError(next.getStartMark(), "Expected next event of type " + type + " but was " + next.getEventId(), null);
        }
        if (!clazz.isInstance(next)) {
            throw makeError(next.getStartMark(), "Expected event of type " + clazz + " but got a " + next.getClass(), null);
        }

        return (T) this.getEvent();
    }

    URI tagUri(
        final String literalTag,
        final Mark startMark,
        final Frame head
    ) throws ParsingException {
        try {
            return new URI(literalTag);
        } catch (final URISyntaxException ex) {
            throw head.makeError(startMark, "Invalid tag URI " + literalTag, ex);
        }
    }

    // frame states //

    Frame pushFrame(final ComposerState state) {
        final int head = ++this.framePointer;
        if (head >= this.frames.length) {
            this.frames = Arrays.copyOf(this.frames, this.frames.length + FRAME_STACK_INCREMENT);
        }
        final Frame current;
        if (this.frames[head] == null) {
            current = this.frames[head] = new Frame();
        } else {
            current = this.frames[head];
        }

        if (head > 0) { // inherit from parent state
            current.init(state, this.frames[head - 1]);
        } else {
            current.init(state);
        }

        return current;
    }

    Frame swapState(final ComposerState state) {
        final Frame ret = this.peekFrame();
        ret.state = state;
        return ret;
    }

    void popFrame() {
        if (this.framePointer-- < 0) {
            throw new IllegalStateException("Tried to pop beyond bounds of the frame stack");
        }
        final Frame popped = this.frames[this.framePointer + 1];
        if (!popped.hasFlag(Frame.SAVE_NODE)) {
            popped.node = null; // don't hold references
        }
    }

    Frame peekFrame() {
        return peekFrame(0);
    }

    Frame peekFrame(final int depth) {
        if (depth < 0 || depth > this.framePointer) {
            throw new IllegalStateException("Tried to peek beyond bounds of state stack. requested depth: " + depth
                + ", actual depth: " + this.framePointer);
        }

        return this.frames[this.framePointer - depth];
    }

    /**
     * A frame in the state stack.
     *
     * <p>Contains the target node and current resolved tag (if any).</p>
     */
    static class Frame {

        static final int SUPPRESS_COMMENTS = 1; // whether to associate comment events with this node
        static final int SAVE_NODE = 1 << 1; // don't clear node when popping

        @MonotonicNonNull ComposerState state;

        /**
         * The resolved tag.
         *
         * <p>May be used by child states to perform their own
         * tag resolution.</p>
         */
        @Nullable Tag resolvedTag;
        ConfigurationNode node;
        int flags;

        void init(final ComposerState state, final Frame parent) {
            this.state = state;
            this.node = parent.node;
            this.flags = parent.flags;
            this.resolvedTag = null;
        }

        void init(final ComposerState state) {
            this.state = state;
            this.flags = 0;
            this.resolvedTag = null;
        }

        boolean hasFlag(final int flag) {
            return (this.flags & flag) != 0;
        }

        void addFlag(final int flag) {
            this.flags |= flag;
        }

        ParsingException makeError(
            final Mark mark,
            final @Nullable String message,
            final @Nullable Throwable error
        ) {
            return new ParsingException(this.node, mark.getLine(), mark.getColumn(), mark.get_snippet(), message, error);
        }
    }

    // comments

    void applyComments(final ConfigurationNode node) {
        if (!(node instanceof CommentedConfigurationNodeIntermediary<@NonNull?>)) {
            return; // no comments are even collected
        }

        if (this.commentCollector != null && this.commentCollector.length() > 0) {
            final StringBuilder collector = this.commentCollector;
            final CommentedConfigurationNodeIntermediary<@NonNull ?> commented = (CommentedConfigurationNodeIntermediary<@NonNull ?>) node;
            if (commented.comment() != null) {
                collector.insert(0, commented.comment());
                collector.insert(commented.comment().length(), '\n');
            }
            commented.comment(collector.toString());
            collector.delete(0, collector.length());
        }
    }

    @Nullable String popComment() {
        if (this.peekFrame().hasFlag(Frame.SUPPRESS_COMMENTS)) {
            return null;
        }

        final String ret;
        if (this.commentCollector != null && this.commentCollector.length() > 0) {
            final StringBuilder collector = this.commentCollector;
            ret = collector.toString();
            collector.delete(0, collector.length());
        } else {
            ret = null;
        }
        this.collectComments();
        return ret;
    }

    void applyComment(final @Nullable String comment, final ConfigurationNode node) {
        if (comment == null || !(node instanceof CommentedConfigurationNodeIntermediary<@NonNull ?>)) {
            return;
        }
        final CommentedConfigurationNodeIntermediary<@NonNull ?> commented = (CommentedConfigurationNodeIntermediary<@NonNull ?>) node;
        if (commented.comment() != null) {
            commented.comment(
                commented.comment()
                    + '\n'
                    + comment
            );
        } else {
            commented.comment(comment);
        }

    }

    void collectComments() {
        if (!this.processComments || !this.scanner().isEmitComments()) {
            return;
        }

        while (peekEvent().is(Event.ID.Comment)) {
            final CommentEvent event = (CommentEvent) getEvent();
            if (event.getCommentType() != CommentType.BLANK_LINE) {
                @Nullable StringBuilder commentCollector = this.commentCollector;
                if (commentCollector == null) {
                    this.commentCollector = commentCollector = new StringBuilder();
                }
                if (commentCollector.length() > 0) {
                    commentCollector.append(AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR);
                }
                if (this.stripLeadingCommentWhitespace && event.getValue().startsWith(" ")) {
                    commentCollector.append(event.getValue(), 1, event.getValue().length());
                } else {
                    commentCollector.append(event.getValue());
                }
            }
        }
    }

    public <N extends ConfigurationNode> Stream<N> stream(final ConfigurationNodeFactory<N> factory) throws ParsingException {
        requireEvent(Event.ID.StreamStart);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<N>() {
            @Override
            public boolean hasNext() {
                return !checkEvent(Event.ID.StreamEnd);
            }

            @Override
            public N next() {
                if (!hasNext()) {
                    throw new IndexOutOfBoundsException();
                }
                try {
                    final N node = factory.createNode();
                    document(node);
                    if (!hasNext()) {
                        requireEvent(Event.ID.StreamEnd);
                    }
                    return node;
                } catch (final ConfigurateException e) {
                    throw new RuntimeException(e); // TODO
                }
            }
        }, Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    static ParsingException makeError(
        final Mark mark,
        final @Nullable String message,
        final @Nullable Throwable error
    ) {
        return new ParsingException(mark.getLine(), mark.getColumn(), mark.get_snippet(), message, error);
    }

    /**
     * A phase in the composer state machine.
     *
     * <p>Each phase can manipulate the phase stack frames in response to one
     * or more events. The frames manipulate their own stack.</p>
     *
     * <p>Frames manage their own event consumption, and can swap to another
     * state within the same frame, or push/pop additional frames.</p>
     */
    interface ComposerState {

        /**
         * Perform one round of processing.
         *
         * @param head current state frame
         * @param self the state to work with
         * @return the next frame, or {@code null} to pop a frame. See {@link #pushFrame(ComposerState)} and {@link #swapState(ComposerState)}
         * @throws ParsingException to indicate an error
         * @see Frame#makeError(Mark, String, Throwable)
         */
        @Nullable Frame accept(Frame head, YamlParserComposer self) throws ParsingException;
    }

    /**
     * The first state.
     *
     * <p>Expects a {@link Event.ID#DocumentStart} event, and will push a frame
     * with Value state.</p>
     */
    static final class DocumentStart implements ComposerState {

        static final DocumentStart INSTANCE = new DocumentStart();

        private DocumentStart() {
        }

        @Override
        public Frame accept(final Frame head, final YamlParserComposer self) throws ParsingException {
            self.collectComments();
            final DocumentStartEvent ds = self.requireEvent(Event.ID.DocumentStart, DocumentStartEvent.class);
            if (ds.getTags() != null) {
                self.declaredTags.putAll(ds.getTags());
            }
            self.swapState(DocumentEnd.INSTANCE); // state to use after Value is complete
            if (self.peekEvent().is(Event.ID.DocumentEnd)) {
                return head;
            } else {
                return self.pushFrame(Value.INSTANCE);
            }
        }
    }

    /**
     * The final state.
     *
     * <p>Expects a {@link Event.ID#DocumentEnd} event, and will process any
     * trailing comments.</p>
     */
    static final class DocumentEnd implements ComposerState {

        static final DocumentEnd INSTANCE = new DocumentEnd();

        private DocumentEnd() {
        }

        @Override
        public @Nullable Frame accept(final Frame head, final YamlParserComposer self) throws ParsingException {
            self.requireEvent(Event.ID.DocumentEnd);
            return null;
        }
    }

    /**
     * Receives a value of unknown type, and figures out what to do with it.
     *
     * <p>This state performs pre-processing of values as well.</p>
     */
    static final class Value implements ComposerState {

        static final Value INSTANCE = new Value();

        private Value() {
        }

        @Override
        public Frame accept(final Frame head, final YamlParserComposer self) throws ParsingException {
            final Event peeked = self.peekEvent();
            // extract event metadata
            if (peeked instanceof NodeEvent && !(peeked instanceof AliasEvent)) {
                final String anchor = ((NodeEvent) peeked).getAnchor();
                if (anchor != null) {
                    head.node.hint(YamlConfigurationLoader.ANCHOR_ID, anchor);
                    self.aliases.put(anchor, head.node);
                }
                if (peeked instanceof CollectionStartEvent) {
                    head.node.hint(YamlConfigurationLoader.NODE_STYLE, NodeStyle.fromSnakeYaml(((CollectionStartEvent) peeked).getFlowStyle()));
                }
            }

            // then handle the value
            switch (peeked.getEventId()) {
                case Scalar:
                    return self.swapState(Scalar.INSTANCE);
                case MappingStart:
                    return self.swapState(MappingStart.INSTANCE);
                case SequenceStart:
                    return self.swapState(SequenceStart.INSTANCE);
                case Alias:
                    return self.swapState(Alias.INSTANCE);
                default:
                    throw head.makeError(peeked.getStartMark(), "Unexpected event type " + peeked.getEventId(), null);
            }
        }

    }

    static final class Scalar implements ComposerState {

        static final Scalar INSTANCE = new Scalar();

        private Scalar() {
        }

        @Override
        public @Nullable Frame accept(final Frame head, final YamlParserComposer self) throws ParsingException {
            final @Nullable String comments = self.popComment();
            // read scalar
            final ScalarEvent scalar = self.requireEvent(Event.ID.Scalar, ScalarEvent.class);
            head.node.hint(YamlConfigurationLoader.SCALAR_STYLE, ScalarStyle.fromSnakeYaml(scalar.getScalarStyle()));
            // resolve tag
            @Nullable Tag tag;
            if (scalar.getTag() != null) {
                // todo: handle ! tag
                final URI tagUri = self.tagUri(scalar.getTag(), scalar.getStartMark(), head);
                tag = self.tags.named(tagUri);
                if (tag == null) {
                    tag = Tag.Scalar.ofUnknown(tagUri);
                    head.node.raw(scalar.getValue()); // TODO: tags and value types
                } else if (!(tag instanceof Tag.Scalar<?>)) {
                    throw head.makeError(
                        scalar.getStartMark(),
                        "Declared tag for node was expected to handle a Scalar, but actually is a " + tag.getClass(),
                        null
                    );
                } else {
                    head.node.raw(((Tag.Scalar<?>) tag).fromString(scalar.getValue()));
                }
            } else {
                // Only perform implicit tag resolution for plain scalars
                tag = scalar.getScalarStyle() == DumperOptions.ScalarStyle.PLAIN ? self.tags.forInput(scalar.getValue()) : Yaml11Tags.STR;
                if (tag == null) {
                    // todo: maybe throw here?
                    tag = Yaml11Tags.STR;
                }
                head.node.raw(((Tag.Scalar<?>) tag).fromString(scalar.getValue()));
            }
            self.applyComment(comments, head.node);
            head.node.hint(YamlConfigurationLoader.TAG, tag);
            head.resolvedTag = tag;
            // pop state
            return null;
        }

    }

    static final class MappingStart implements ComposerState {

        static final MappingStart INSTANCE = new MappingStart();

        private MappingStart() {
        }

        @Override
        public Frame accept(final Frame head, final YamlParserComposer self) throws ParsingException {
            final MappingStartEvent event = self.requireEvent(Event.ID.MappingStart, MappingStartEvent.class);
            if (event.isFlow() || self.peekEvent().is(Event.ID.Comment)) {
                self.applyComments(head.node);
            }
            head.node.raw(Collections.emptyMap());
            return self.swapState(MappingKeyOrEnd.INSTANCE);
        }
    }

    static final class MappingKeyOrEnd implements ComposerState {

        static final MappingKeyOrEnd INSTANCE = new MappingKeyOrEnd();

        private MappingKeyOrEnd() {
        }

        @Override
        public @Nullable Frame accept(final Frame head, final YamlParserComposer self) {
            self.collectComments();
            if (self.peekEvent().is(Event.ID.MappingEnd)) {
                self.getEvent();
                return null;
            } else {
                // push state of MappingValue
                self.pushFrame(MappingValue.INSTANCE);
                // push destination node
                final Frame child = self.pushFrame(Value.INSTANCE); // compute key node
                child.addFlag(Frame.SUPPRESS_COMMENTS | Frame.SAVE_NODE);
                child.node = BasicConfigurationNode.root(head.node.options());
                return child;
            }
        }

    }

    static final class MappingValue implements ComposerState {

        static final MappingValue INSTANCE = new MappingValue();

        private MappingValue() {
        }

        @Override
        public Frame accept(final Frame head, final YamlParserComposer self) throws ParsingException {

            // get value from next state, somehow?
            // pop destination node
            // set as 'next target'
            final @Nullable ConfigurationNode keyHolder = self.frames[self.framePointer + 1].node; // todo: ugly
            if (keyHolder == null) {
                throw new IllegalStateException("null keyHolder");
            }
            final @Nullable Object key = keyHolder.raw();
            if (key == null) {
                throw head.makeError(self.scanner.peekToken().getStartMark(), "'null' is not permitted as a mapping key", null);
            }

            final ConfigurationNode child = head.node.node(key);
            if (!child.virtual()) {
                // duplicate keys are forbidden (3.2.1.3)
                // snakeyaml doesn't enforce this :(
                throw makeError(self.scanner.peekToken().getStartMark(), "Duplicate key '" + child.key() + "' encountered!", null);
            }
            head.node = child;
            self.applyComments(head.node);
            self.collectComments();
            return self.swapState(Value.INSTANCE);
        }

    }

    static final class SequenceStart implements ComposerState {

        static final SequenceStart INSTANCE = new SequenceStart();

        private SequenceStart() {
        }

        @Override
        public Frame accept(final Frame head, final YamlParserComposer self) throws ParsingException {
            final SequenceStartEvent event = self.requireEvent(Event.ID.SequenceStart, SequenceStartEvent.class);
            if (event.isFlow() || self.peekEvent().is(Event.ID.Comment)) {
                self.applyComments(head.node);
            }
            head.node.raw(Collections.emptyList());
            return self.swapState(SequenceEntryOrEnd.INSTANCE);
        }

    }

    static final class SequenceEntryOrEnd implements ComposerState {

        static final SequenceEntryOrEnd INSTANCE = new SequenceEntryOrEnd();

        private SequenceEntryOrEnd() {
        }

        @Override
        public @Nullable Frame accept(final Frame head, final YamlParserComposer self) {
            final @Nullable String comments = self.popComment();
            if (self.peekEvent().is(Event.ID.SequenceEnd)) {
                self.getEvent();
                return null;
            } else {
                // push destination node as 'next target'
                final Frame ret = self.pushFrame(Value.INSTANCE);
                ret.node = self.peekFrame().node.appendListNode();
                self.applyComment(comments, ret.node);
                return ret;
            }
        }

    }

    static final class Alias implements ComposerState {

        static final Alias INSTANCE = new Alias();

        private Alias() {
        }

        @Override
        public @Nullable Frame accept(final Frame head, final YamlParserComposer self) throws ParsingException {
            final AliasEvent event = self.requireEvent(Event.ID.Alias, AliasEvent.class);
            final ConfigurationNode target = self.aliases.get(event.getAnchor());
            if (target == null) {
                throw head.makeError(event.getStartMark(), "Unknown anchor '" + event.getAnchor() + "'", null);
            }
            head.node.from(target); // TODO: Reference node types
            head.node.hint(YamlConfigurationLoader.ANCHOR_ID, null); // don't duplicate alias
            return null;
        }

    }

}

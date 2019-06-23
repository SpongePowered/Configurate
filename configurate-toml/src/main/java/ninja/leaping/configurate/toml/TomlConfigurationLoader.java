package ninja.leaping.configurate.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.NewlineStyle;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.loader.CommentHandler;
import ninja.leaping.configurate.loader.CommentHandlers;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A loader for TOML-formatted configurations, using the Night-Config library for parsing and generation.
 */
public class TomlConfigurationLoader extends AbstractConfigurationLoader<CommentedConfigurationNode> {

    /**
     * Builds a {@link TomlConfigurationLoader}.
     */
    public static class Builder extends AbstractConfigurationLoader.Builder<Builder> {

        private boolean isLenientWithSeparators = false;
        private boolean isLenientWithBareKeys = false;
        @Nullable
        private String newline = null;
        @Nullable
        private String indent = null;
        @Nullable
        private Predicate<List<?>> indentArrayElements;
        @Nullable
        private Predicate<String> writeStringLiteral;
        @Nullable
        private Predicate<UnmodifiableConfig> writeTablesInline;

        private Builder() {}

        @NonNull
        @Override
        public TomlConfigurationLoader build() {
            return new TomlConfigurationLoader(this);
        }

        /**
         * Sets whether or not bare keys are allowed to have characters other
         * than {@code [A-Za-z0-9_-]}.
         *
         * @param isLenientWithBareKeys Whether to be lenient with bare keys
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setLenientWithBareKeys(boolean isLenientWithBareKeys) {
            this.isLenientWithBareKeys = isLenientWithBareKeys;
            return this;
        }

        /**
         * Sets whether or not {@code :} is allowed as a key-value separator
         * (in addition to {@code =}).
         *
         * @param isLenientWithSeparators Whether to be lenient with separators
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setLenientWithSeparators(boolean isLenientWithSeparators) {
            this.isLenientWithSeparators = isLenientWithSeparators;
            return this;
        }

        /**
         * Gets whether or not '{@code :}' is allowed as a key-value separator
         * (in addition to '{@code =}').
         *
         * @return This builder (for chaining)
         */
        public boolean isLenientWithSeparators() {
            return isLenientWithSeparators;
        }

        /**
         * Gets whether or not bare keys are allowed to have characters other
         * than {@code [A-Za-z0-9_-]}.
         *
         * @return Whether to be lenient with bare keys
         */
        public boolean isLenientWithBareKeys() {
            return isLenientWithBareKeys;
        }

        /**
         * Gets the {@link String} that will be used for new lines.
         *
         * @return The {@link String} that will be used for new lines
         */
        @Nullable
        public String getNewline() {
            return newline;
        }

        /**
         * Sets the {@link String} that will be used for new lines.
         *
         * @param newline The {@link String} that will be used for new lines
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setNewline(@Nullable String newline) {
            this.newline = newline;
            return this;
        }

        /**
         * Sets the {@link NewlineStyle} that will be used for new lines.
         *
         * @param style The {@link NewlineStyle} that will be used for new
         *              lines
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setNewline(@Nullable NewlineStyle style) {
            if (style != null) {
                this.newline = new String(style.chars);
            } else {
                this.newline = null;
            }
            return this;
        }

        /**
         * Gets the {@link String} that will be used for indentation.
         *
         * @return The {@link String} that will be used for indentation
         */
        @Nullable
        public String getIndent() {
            return indent;
        }

        /**
         * Sets the {@link String} that will be used for indentation.
         *
         * @param indent The {@link String} that will be used for indentation
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setIndent(@Nullable String indent) {
            this.indent = indent;
            return this;
        }

        /**
         * Sets the {@link IndentStyle} that will be used for indentation.
         *
         * @param style The {@link IndentStyle} that will be used for
         *              indentation
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setIndent(@Nullable IndentStyle style) {
            if (style != null) {
                this.indent = new String(style.chars);
            } else {
                this.indent = null;
            }
            return this;
        }

        /**
         * Gets the {@link Predicate} that will be used to check whether array
         * elements should be indented.
         *
         * @return The array indent checking {@link Predicate}
         */
        @Nullable
        public Predicate<List<?>> getIndentArrayElements() {
            return indentArrayElements;
        }

        /**
         * Sets the {@link Predicate} that will be used to check whether array
         * elements should be indented.
         *
         * @param indentArrayElements The array indent checking
         *                            {@link Predicate}
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setIndentArrayElements(@Nullable Predicate<List<?>> indentArrayElements) {
            this.indentArrayElements = indentArrayElements;
            return this;
        }

        /**
         * Sets whether array elements should be indented. Equivalent to
         * calling {@code setIndentArrayElements(l -> indentArrayElements)}.
         *
         * @param indentArrayElements Whether to indent array elements
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setIndentArrayElements(boolean indentArrayElements) {
            this.setIndentArrayElements(l -> indentArrayElements);
            return this;
        }

        /**
         * Gets the {@link Predicate} that will be used to check whether a
         * {@link String} should be written as a literal (single quotes,
         * unescaped).
         *
         * @return The literal checking predicate
         */
        @Nullable
        public Predicate<String> getWriteStringLiteral() {
            return writeStringLiteral;
        }

        /**
         * Sets the {@link Predicate} that will be used to check whether a
         * {@link String} should be written as a literal (single quotes,
         * unescaped).
         *
         * @param writeStringLiteral The literal checking predicate
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setWriteStringLiteral(@Nullable Predicate<String> writeStringLiteral) {
            this.writeStringLiteral = writeStringLiteral;
            return this;
        }

        /**
         * Sets whether or not strings should be written as literals (single
         * quotes, unescaped). Equivalent to calling
         * {@code setWriteStringLiteral(s -> writeStringLiteral)}.
         *
         * @param writeStringLiteral Whether to write strings as literals
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setWriteStringLiteral(boolean writeStringLiteral) {
            this.setWriteStringLiteral(s -> writeStringLiteral);
            return this;
        }

        /**
         * Gets the {@link Predicate} that will be used to check whether tables
         * should be written inline.
         *
         * @return The inline checking predicate
         */
        @Nullable
        public Predicate<UnmodifiableConfig> getWriteTablesInline() {
            return writeTablesInline;
        }

        /**
         * Sets the {@link Predicate} that will be used to check whether tables
         * should be writen inline.
         *
         * @param writeTablesInline The inline checking predicate
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setWriteTablesInline(@Nullable Predicate<UnmodifiableConfig> writeTablesInline) {
            this.writeTablesInline = writeTablesInline;
            return this;
        }

        /**
         * Sets whether tables should be written inline. Equivalent to calling
         * {@code setWriteTablesInline(n -> writeTablesInline)}.
         *
         * @param writeTablesInline Whether tables should be written inline
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setWriteTablesInline(boolean writeTablesInline) {
            this.setWriteTablesInline(n -> writeTablesInline);
            return this;
        }

    }

    @NonNull
    private TomlParser parser = new TomlParser();
    @NonNull
    private TomlWriter writer = new TomlWriter();

    private TomlConfigurationLoader(@NonNull Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.HASH });
        parser.setLenientWithBareKeys(builder.isLenientWithBareKeys);
        parser.setLenientWithSeparators(builder.isLenientWithSeparators);
        writer.setLenientWithBareKeys(builder.isLenientWithBareKeys);
        if (builder.newline != null) {
            writer.setNewline(builder.newline);
        }
        if (builder.indent != null) {
            writer.setIndent(builder.indent);
        }
        if (builder.indentArrayElements != null) {
            writer.setIndentArrayElementsPredicate(builder.indentArrayElements);
        }
        if (builder.writeTablesInline != null) {
            writer.setWriteTableInlinePredicate(builder.writeTablesInline);
        }
        if (builder.writeStringLiteral != null) {
            writer.setWriteStringLiteralPredicate(builder.writeStringLiteral);
        }
    }

    @Override
    protected void loadInternal(@NonNull CommentedConfigurationNode node, @NonNull BufferedReader reader) throws IOException {
        CommentedConfig config = parser.parse(reader);
        readTable(config, node);
    }

    private static void readTable(@NonNull CommentedConfig config, @NonNull CommentedConfigurationNode node) {
        for (Map.Entry<String, Object> entry : config.valueMap().entrySet()) {
            CommentedConfigurationNode subNode = node.getNode(entry.getKey());
            readValue(entry.getValue(), subNode);
            subNode.setComment(config.getComment(ImmutableList.of(entry.getKey())));
        }
    }

    private static void readValue(@Nullable Object value, @NonNull CommentedConfigurationNode node) {
        if (value == null) {
            node.setValue(null);
        } else if (value instanceof CommentedConfig) {
            readTable((CommentedConfig) value, node);
        } else if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            for (Object obj : list) {
                readValue(obj, node.getAppendedNode());
            }
        } else {
            node.setValue(value);
        }
    }

    @Override
    protected void saveInternal(@NonNull ConfigurationNode node, @NonNull Writer writer) throws IOException {
        CommentedConfig config = CommentedConfig.inMemory();
        if (!(node.hasMapChildren())) {
            throw new IllegalArgumentException("Node must have children");
        }
        writeTable(node, config);
        this.writer.write(config.unmodifiable(), writer);
    }

    @Nullable
    private static Object fromNode(@NonNull ConfigurationNode node) {
        if (node.hasListChildren()) {
            List<Object> list = new ArrayList<>();
            for (ConfigurationNode subNode : node.getChildrenList()) {
                list.add(fromNode(subNode));
            }
            return list;
        } else if (node.hasMapChildren()) {
            CommentedConfig config = CommentedConfig.inMemory();
            writeTable(node, config);
            return config;
        } else {
            return node.getValue();
        }
    }

    private static void writeTable(@NonNull ConfigurationNode node, @NonNull CommentedConfig config) {
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.getChildrenMap().entrySet()) {
            List<String> key = ImmutableList.of(entry.getKey().toString());
            config.set(key, fromNode(entry.getValue()));
            if (entry.getValue() instanceof CommentedConfigurationNode) {
                Optional<String> comment = ((CommentedConfigurationNode) entry.getValue()).getComment();
                if (comment.isPresent()) {
                    config.setComment(key, comment.get());
                } else {
                    config.removeComment(key);
                }
            }
        }
    }

    @NonNull
    @Override
    public CommentedConfigurationNode createEmptyNode(@NonNull ConfigurationOptions options) {
        options = options.setAcceptedTypes(ImmutableSet.of(Map.class, List.class, Double.class, Long.class,
                Integer.class, Boolean.class, String.class, Number.class, LocalDateTime.class,
                OffsetDateTime.class, LocalDate.class, LocalTime.class));
        return SimpleCommentedConfigurationNode.root(options);
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

}

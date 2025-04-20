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
package org.spongepowered.configurate.xml;

import net.kyori.option.Option;
import net.kyori.option.OptionSchema;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.AttributedConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.spongepowered.configurate.loader.ParsingException;
import org.spongepowered.configurate.util.UnmodifiableCollections;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;

/**
 * A loader for XML (Extensible Markup Language), using the native javax library
 * for parsing and generation.
 *
 * @since 4.0.0
 */
public final class XmlConfigurationLoader extends AbstractConfigurationLoader<AttributedConfigurationNode> {

    private static final Set<Class<?>> NATIVE_TYPES = UnmodifiableCollections.toSet(Double.class, Long.class,
            Integer.class, Boolean.class, String.class, Number.class);

    /**
     * The prefix of lines within the header.
     */
    private static final String HEADER_PREFIX = "~";

    private static final String ATTRIBUTE_TYPE = "configurate-type";

    /**
     * The user data used to store comments on nodes.
     */
    private static final String USER_DATA_COMMENT = "configurate-comment";

    /**
     * The property used to mark how many spaces should be used to indent.
     */
    private static final String INDENT_PROPERTY = "{http://xml.apache.org/xslt}indent-amount";

    private static final String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

    private static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

    private static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Creates a new {@link XmlConfigurationLoader} builder.
     *
     * @return a new builder
     * @since 4.0.0
     */
    public static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link XmlConfigurationLoader}.
     *
     * <p>This builder supports the following options:</p>
     * <ul>
     *     <li>{@link #DEFAULT_TAG_NAME}</li>
     *     <li>{@link #INDENT}</li>
     *     <li>{@link #WRITE_EXPLICIT_TYPE}</li>
     *     <li>{@link #RESOLVE_EXTERNAL_CONTENT}</li>
     *     <li>{@link #INCLUDE_XML_DECLARATION}</li>
     * </ul>
     *
     * @since 4.0.0
     */
    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, XmlConfigurationLoader> {

        private static final OptionSchema.Mutable UNSAFE_SCHEMA = OptionSchema.childSchema(AbstractConfigurationLoader.Builder.SCHEMA);
        /**
         * A schema of options available to configure the XML loader.
         *
         * @since 4.2.0
         */
        public static final OptionSchema SCHEMA = UNSAFE_SCHEMA.frozenView();
        /**
         * The default tag name the created loader should use.
         *
         * @see #defaultTagName(String)
         * @since 4.2.0
         */
        public static final Option<String> DEFAULT_TAG_NAME = UNSAFE_SCHEMA.stringOption("xml:default_tag_name", "element");

        /**
         * The size of indent (in spaces) the created loader should use.
         *
         * @see #indent(int)
         * @since 4.2.0
         */
        public static final Option<Integer> INDENT = UNSAFE_SCHEMA.intOption("xml:indent", 2);
        /**
         * Whether explicit node types should be written out (to disambiguate
         * between maps and lists).
         *
         * @see #writesExplicitType(boolean)
         * @since 4.2.0
         */
        public static final Option<Boolean> WRITE_EXPLICIT_TYPE = UNSAFE_SCHEMA.booleanOption("xml:writes_explicit_type", true);
        /**
         * Whether externally included content should be resolved in documents
         * read by the created loader.
         *
         * @see #resolvesExternalContent(boolean)
         * @since 4.2.0
         */
        public static final Option<Boolean> RESOLVE_EXTERNAL_CONTENT = UNSAFE_SCHEMA.booleanOption("xml:resolve_external_content", false);
        /**
         * Whether an XML declaration should be included as the first line of
         * emitted documents from the created loader.
         *
         * @see #includesXmlDeclaration(boolean)
         * @since 4.2.0
         */
        public static final Option<Boolean> INCLUDE_XML_DECLARATION = UNSAFE_SCHEMA.booleanOption("xml:include_xml_declaration", true);

        private @Nullable Schema schema;

        @Override
        protected OptionSchema optionSchema() {
            return SCHEMA;
        }

        /**
         * Sets the level of indentation the resultant loader should use.
         *
         * @param indent the indent level
         * @return this builder (for chaining)
         * @since 4.0.0
         */
        public @NonNull Builder indent(final int indent) {
            this.optionStateBuilder().value(INDENT, indent);
            return this;
        }

        /**
         * Gets the level of indentation to be used by the resultant loader.
         *
         * @return the indent level
         * @since 4.0.0
         */
        public int indent() {
            return this.optionState().value(INDENT);
        }

        /**
         * Sets the {@link Schema} the resultant loader should use.
         *
         * @param schema the schema
         * @return this builder (for chaining)
         * @since 4.0.0
         */
        public Builder schema(final @Nullable Schema schema) {
            this.schema = schema;
            return this;
        }

        /**
         * Gets the {@link Schema} to be used by the resultant loader.
         *
         * @return the schema
         * @since 4.0.0
         */
        public @Nullable Schema schema() {
            return this.schema;
        }

        /**
         * Sets the default tag name the resultant loader should use.
         *
         * @param defaultTagName the default tag name
         * @return this builder (for chaining)
         * @since 4.0.0
         */
        public Builder defaultTagName(final String defaultTagName) {
            this.optionStateBuilder().value(DEFAULT_TAG_NAME, defaultTagName);
            return this;
        }

        /**
         * Gets the default tag name to be used by the resultant loader.
         *
         * @return the default tag name
         * @since 4.0.0
         */
        public @NonNull String defaultTagName() {
            return this.optionState().value(DEFAULT_TAG_NAME);
        }

        /**
         * Sets if the resultant loader should write the explicit type of each
         * node when saving nodes.
         *
         * <p>This is necessary in some cases, as XML has no explicit definition
         * of an array or list. The loader is able to infer the type in some
         * cases, but this is inaccurate in some cases, for example lists with
         * only one element.</p>
         *
         * @param writeExplicitType if the loader should write explicit types
         * @return this builder (for chaining)
         * @since 4.0.0
         */
        public Builder writesExplicitType(final boolean writeExplicitType) {
            this.optionStateBuilder().value(WRITE_EXPLICIT_TYPE, writeExplicitType);
            return this;
        }

        /**
         * Gets if explicit type attributes should be written by the loader.
         *
         * <p>See the method doc at {@link #writesExplicitType(boolean)} for
         * a more detailed explanation.</p>
         *
         * @return the default tag name
         * @since 4.0.0
         */
        public boolean writesExplicitType() {
            return this.optionState().value(WRITE_EXPLICIT_TYPE);
        }

        /**
         * Sets if the resultant loader should include the XML declaration
         * header when saving.
         *
         * @param includeXmlDeclaration if the XML declaration should be
         *                              included
         * @return this builder (for chaining)
         * @since 4.0.0
         */
        public Builder includesXmlDeclaration(final boolean includeXmlDeclaration) {
            this.optionStateBuilder().value(INCLUDE_XML_DECLARATION, includeXmlDeclaration);
            return this;
        }

        /**
         * Gets if the resultant loader should include the XML declaration
         * header when saving.
         *
         * @return if the XML declaration should be included
         * @since 4.0.0
         */
        public boolean includesXmlDeclaration() {
            return this.optionState().value(INCLUDE_XML_DECLARATION);
        }

        /**
         * Sets whether external content should be resolved when loading data.
         *
         * <p>Resolving this content could result in network requests being
         * made, and will allow configuration files to access arbitrary URLs
         * This setting should only be enabled with caution.
         *
         * <p>Additionally, through use of features such as entity expansion and
         * XInclude, documents can be crafted that will grow exponentially
         * when parsed, requiring an amount of memory to store that may be
         * greater than what is available for the JVM.
         *
         * <p>By default, this is false.
         *
         * @param resolvesExternalContent whether to resolve external entities
         * @return this builder
         * @since 4.0.0
         */
        public Builder resolvesExternalContent(final boolean resolvesExternalContent) {
            this.optionStateBuilder().value(RESOLVE_EXTERNAL_CONTENT, resolvesExternalContent);
            return this;
        }

        /**
         * Get whether external content should be resolved.
         *
         * @return value, defaulting to false
         * @since 4.0.0
         */
        public boolean resolvesExternalContent() {
            return this.optionState().value(RESOLVE_EXTERNAL_CONTENT);
        }

        @Override
        public XmlConfigurationLoader build() {
            this.defaultOptions(o -> o.nativeTypes(NATIVE_TYPES));
            return new XmlConfigurationLoader(this);
        }
    }

    private final @Nullable Schema schema;
    private final String defaultTagName;
    private final int indent;
    private final boolean writeExplicitType;
    private final boolean includeXmlDeclaration;
    private final boolean resolvesExternalContent;

    private XmlConfigurationLoader(final Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.XML_STYLE});
        this.schema = builder.schema();
        this.defaultTagName = builder.defaultTagName();
        this.indent = builder.indent();
        this.writeExplicitType = builder.writesExplicitType();
        this.includeXmlDeclaration = builder.includesXmlDeclaration();
        this.resolvesExternalContent = builder.resolvesExternalContent();
    }

    private DocumentBuilder newDocumentBuilder() throws ConfigurateException {
        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        if (this.schema != null) {
            builderFactory.setSchema(this.schema);
        }
        if (!this.resolvesExternalContent) {
            // Settings based on https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
            try {
                builderFactory.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
                builderFactory.setFeature(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
                builderFactory.setFeature(FEATURE_LOAD_EXTERNAL_DTD, false);
            } catch (final ParserConfigurationException e) {
                throw new ConfigurateException(e);
            }
            builderFactory.setXIncludeAware(false);
            builderFactory.setExpandEntityReferences(false);
        }

        try {
            return builderFactory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new ConfigurateException(e);
        }
    }

    private Transformer newTransformer() throws ConfigurateException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        if (!this.resolvesExternalContent) {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        }
        try {
            final Transformer transformer = transformerFactory.newTransformer();

            // we write the header ourselves.
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            if (this.indent > 0) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(INDENT_PROPERTY, Integer.toString(this.indent));
            }
            return transformer;
        } catch (final TransformerConfigurationException e) {
            throw new ConfigurateException(e);
        }
    }

    @Override
    public @NonNull AttributedConfigurationNode load(@NonNull ConfigurationOptions options) throws ParsingException {
        if (this.source == null) {
            throw new ParsingException(-1, -1, "", "No source present to read from!", null);
        }
        try (BufferedReader reader = this.source.call()) {
            final DocumentBuilder documentBuilder = this.newDocumentBuilder();

            final Document document;
            try {
                document = documentBuilder.parse(new InputSource(reader));
            } catch (final SAXParseException ex) {
                throw new ParsingException(ex.getLineNumber(), ex.getColumnNumber(), "", ex.getMessage(), ex.getCause());
            } catch (final SAXException e) {
                throw new ParsingException(-1, -1, null, null, e);
            }

            final NodeList children = document.getChildNodes();
            for (int i = 0; i < children.getLength(); ++i) {
                final Node child = children.item(i);
                if (child.getNodeType() == Node.COMMENT_NODE) {
                    options = options.header(this.unwrapHeader(child.getTextContent().trim()));
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    final AttributedConfigurationNode node = this.createNode(options);
                    this.readElement(child, node);
                    return node;
                }
            }
            // empty document, fall through
        } catch (final FileNotFoundException | NoSuchFileException e) {
            // Squash -- there's nothing to read
        } catch (final ParsingException ex) {
            throw ex;
        } catch (final Exception e) {
            throw new ParsingException(-1, -1, "", null, e);
        }
        return this.createNode(options);
    }

    /**
     * Given a single comment node's comment, clear any prefix lines.
     *
     * @param headerContent the content of a header
     * @return a formatted header, with lines separated by {@link #CONFIGURATE_LINE_SEPARATOR}
     */
    private String unwrapHeader(final String headerContent) {
        if (headerContent.isEmpty()) {
            return headerContent;
        }
        // TODO: 4.0 may have changed behaviour here when moving away from Guava
        return CONFIGURATE_LINE_PATTERN.splitAsStream(headerContent)
                .map(line -> {
                    final String trimmedLine = line.trim();
                    if (trimmedLine.startsWith(HEADER_PREFIX)) {
                        line = line.substring(line.indexOf(HEADER_PREFIX) + 1);
                    }

                    if (line.length() > 0 && line.charAt(0) == ' ') {
                        line = line.substring(1);
                    }
                    return line;
                }).filter(line -> !line.isEmpty())
                .collect(Collectors.joining(CONFIGURATE_LINE_SEPARATOR));
    }

    @Override
    protected void loadInternal(final AttributedConfigurationNode node, final BufferedReader reader) {
        throw new UnsupportedOperationException("XMLConfigurationLoader provides custom loading logic to handle headers");
    }

    @SuppressWarnings("checkstyle:JavadocVariable") // false positive, private enum
    private enum NodeType {
        MAP, LIST
    }

    private void readElement(final Node from, final AttributedConfigurationNode to) {
        @Nullable NodeType type = null;

        // copy the name of the tag
        to.tagName(from.getNodeName());

        final String potentialComment = (String) from.getUserData(USER_DATA_COMMENT);
        if (potentialComment != null) {
            to.comment(potentialComment);
        }

        // copy attributes
        if (from.hasAttributes()) {
            final NamedNodeMap attributes = from.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                final Node attribute = attributes.item(i);
                final String key = attribute.getNodeName();
                final String value = attribute.getNodeValue();

                // read the type of the node
                if (key.equals(ATTRIBUTE_TYPE)) {
                    if (value.equals("map")) {
                        type = NodeType.MAP;
                    } else if (value.equals("list")) {
                        type = NodeType.LIST;
                    }

                    // don't add internal configurate attributes to the node
                    continue;
                }

                to.addAttribute(key, value);
            }
        }

        // read out the child nodes into a multimap
        final Map<String, Collection<Node>> children = new LinkedHashMap<>();
        if (from.hasChildNodes()) {
            final StringBuilder comment = new StringBuilder();
            final NodeList childNodes = from.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    children.computeIfAbsent(child.getNodeName(), $ -> new ArrayList<>()).add(child);
                    if (comment.length() > 0) {
                        child.setUserData(USER_DATA_COMMENT, comment.toString(), null);
                        comment.setLength(0);
                    }
                } else if (child.getNodeType() == Node.COMMENT_NODE) {
                    if (comment.length() > 0) {
                        comment.append('\n');
                    }

                    comment.append(child.getTextContent().trim());
                }
            }
        }

        // if there are no child nodes present, assume it's a scalar value
        if (children.isEmpty()) {
            to.raw(parseValue(from.getTextContent()));
            return;
        }

        // if type is null, we need to infer what type the element is
        if (type == null) {
            // if there are no duplicate keys, we can infer that it is a map
            // otherwise, assume it's a list
            type = NodeType.MAP;
            for (final Collection<Node> child : children.values()) {
                if (child.size() > 1) {
                    type = NodeType.LIST;
                    break;
                }
            }
        }

        if (type == NodeType.MAP) {
            to.raw(Collections.emptyMap());
        } else {
            to.raw(Collections.emptyList());
        }

        // read out the elements
        for (final Map.Entry<String, Collection<Node>> entry : children.entrySet()) {
            AttributedConfigurationNode child;
            if (type == NodeType.MAP) {
                child = to.node(entry.getKey());
                this.readElement(entry.getValue().iterator().next(), child);
            } else {
                for (final Node element : entry.getValue()) {
                    child = to.appendListNode();
                    this.readElement(element, child);
                }
            }
        }
    }

    @Override
    protected void writeHeaderInternal(final Writer writer) throws IOException {
        if (this.includeXmlDeclaration) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(SYSTEM_LINE_SEPARATOR);
        }
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) throws ConfigurateException {
        final DocumentBuilder documentBuilder = this.newDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        final @Nullable Node comment = this.createCommentNode(document, node);
        if (comment != null) {
            document.appendChild(comment);
        }

        document.appendChild(this.writeNode(document, node, null));

        final Transformer transformer = this.newTransformer();
        final DOMSource source = new DOMSource(document);
        try {
            transformer.transform(source, new StreamResult(writer));
        } catch (final TransformerException e) {
            throw new ConfigurateException(node, e);
        }
    }

    private void appendCommentIfNecessary(final Element parent, final ConfigurationNode node) {
        final @Nullable Node possibleComment = this.createCommentNode(parent.getOwnerDocument(), node);
        if (possibleComment != null) {
            parent.appendChild(possibleComment);
        }
    }

    private @Nullable Node createCommentNode(final Document doc, final ConfigurationNode node) {
        if (node instanceof CommentedConfigurationNodeIntermediary<?>) {
            final @Nullable String comment = ((CommentedConfigurationNodeIntermediary<?>) node).comment();
            if (comment != null) {
                return doc.createComment(" " + comment.trim() + " ");
            }
        }
        return null;
    }

    private Element writeNode(final Document document, final ConfigurationNode node, final @Nullable String forcedTag) {
        String tag = this.defaultTagName;
        Map<String, String> attributes = Collections.emptyMap();

        if (node instanceof AttributedConfigurationNode) {
            final AttributedConfigurationNode attributedNode = (AttributedConfigurationNode) node;
            tag = attributedNode.tagName();
            attributes = attributedNode.attributes();
        }

        final Element element = document.createElement(forcedTag == null ? tag : forcedTag);
        for (final Map.Entry<String, String> attribute : attributes.entrySet()) {
            element.setAttribute(attribute.getKey(), attribute.getValue());
        }

        if (node.isMap()) {
            for (final Map.Entry<Object, ? extends ConfigurationNode> child : node.childrenMap().entrySet()) {
                this.appendCommentIfNecessary(element, child.getValue());
                element.appendChild(this.writeNode(document, child.getValue(), child.getKey().toString()));
            }
        } else if (node.isList()) {
            if (this.writeExplicitType) {
                element.setAttribute(ATTRIBUTE_TYPE, "list");
            }
            for (final ConfigurationNode child : node.childrenList()) {
                this.appendCommentIfNecessary(element, child);
                element.appendChild(this.writeNode(document, child, null));
            }
        } else {
            element.appendChild(document.createTextNode(Objects.toString(node.rawScalar())));
        }

        return element;
    }

    @Override
    public AttributedConfigurationNode createNode(ConfigurationOptions options) {
        options = options.nativeTypes(NATIVE_TYPES);
        return AttributedConfigurationNode.root("root", options);
    }

    private static Object parseValue(final String value) {
        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        }

        try {
            final double doubleValue = Double.parseDouble(value);
            if (isInteger(doubleValue)) {
                final long longValue = Long.parseLong(value); // prevent losing precision
                final int intValue = (int) longValue;
                if (longValue == intValue) {
                    return intValue;
                } else {
                    return longValue;
                }
            }
            return doubleValue;
        } catch (final NumberFormatException e) {
            return value;
        }
    }

    private static boolean isInteger(final double value) {
        return !Double.isNaN(value) && Double.isFinite(value) && value == Math.rint(value);
    }

}

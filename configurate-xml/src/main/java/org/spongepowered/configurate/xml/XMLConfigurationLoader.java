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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.math.DoubleMath;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.attributed.AttributedConfigurationNode;
import org.spongepowered.configurate.attributed.SimpleAttributedConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.CommentHandler;
import org.spongepowered.configurate.loader.CommentHandlers;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * A loader for XML (Extensible Markup Language), using the native javax library for parsing and
 * generation.
 */
public class XMLConfigurationLoader extends AbstractConfigurationLoader<SimpleAttributedConfigurationNode> {

    /**
     * The property used to mark how many spaces should be used to indent.
     */
    private static final String INDENT_PROPERTY = "{http://xml.apache.org/xslt}indent-amount";

    /**
     * Creates a new {@link XMLConfigurationLoader} builder.
     *
     * @return A new builder
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds a {@link XMLConfigurationLoader}.
     */
    public static class Builder extends AbstractConfigurationLoader.Builder<Builder> {
        private Schema schema = null;
        private String defaultTagName = "element";
        private int indent = 2;
        private boolean writeExplicitType = true;
        private boolean includeXmlDeclaration = true;

        protected Builder() {
        }

        /**
         * Sets the level of indentation the resultant loader should use.
         *
         * @param indent The indent level
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setIndent(int indent) {
            this.indent = indent;
            return this;
        }

        /**
         * Gets the level of indentation to be used by the resultant loader.
         *
         * @return The indent level
         */
        public int getIndent() {
            return indent;
        }

        /**
         * Sets the {@link Schema} the resultant loader should use.
         *
         * @param schema The schema
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setSchema(@Nullable Schema schema) {
            this.schema = schema;
            return this;
        }

        /**
         * Gets the {@link Schema} to be used by the resultant loader.
         *
         * @return The schema
         */
        @Nullable
        public Schema getSchema() {
            return schema;
        }

        /**
         * Sets the default tag name the resultant loader should use.
         *
         * @param defaultTagName The default tag name
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setDefaultTagName(@NonNull String defaultTagName) {
            this.defaultTagName = defaultTagName;
            return this;
        }

        /**
         * Gets the default tag name to be used by the resultant loader.
         *
         * @return The default tag name
         */
        @NonNull
        public String getDefaultTagName() {
            return defaultTagName;
        }

        /**
         * Sets if the resultant loader should write the explicit type of each node
         * when saving nodes.
         *
         * <p>This is necessary in some cases, as XML has no explicit definition of an array or
         * list. The loader is able to infer the type in some cases, but this is inaccurate in some
         * cases, for example lists with only one element.</p>
         *
         * @param writeExplicitType If the loader should write explicit types
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setWriteExplicitType(boolean writeExplicitType) {
            this.writeExplicitType = writeExplicitType;
            return this;
        }

        /**
         * Gets if explicit type attributes should be written by the resultant loader.
         *
         * <p>See the method doc for {@link #setWriteExplicitType(boolean)} for a more detailed
         * explanation.</p>
         *
         * @return The default tag name
         */
        public boolean shouldWriteExplicitType() {
            return writeExplicitType;
        }

        /**
         * Sets if the resultant loader should include the XML declaration header when saving.
         *
         * @param includeXmlDeclaration If the XML declaration should be included
         * @return This builder (for chaining)
         */
        @NonNull
        public Builder setIncludeXmlDeclaration(boolean includeXmlDeclaration) {
            this.includeXmlDeclaration = includeXmlDeclaration;
            return this;
        }

        /**
         * Gets if the resultant loader should include the XML declaration header when saving.
         *
         * @return If the XML declaration should be included
         */
        public boolean shouldIncludeXmlDeclaration() {
            return includeXmlDeclaration;
        }

        @NonNull
        @Override
        public XMLConfigurationLoader build() {
            return new XMLConfigurationLoader(this);
        }
    }

    private final Schema schema;
    private final String defaultTagName;
    private final int indent;
    private final boolean writeExplicitType;
    private final boolean includeXmlDeclaration;

    private XMLConfigurationLoader(Builder builder) {
        super(builder, new CommentHandler[] {CommentHandlers.XML_STYLE});
        this.schema = builder.getSchema();
        this.defaultTagName = builder.getDefaultTagName();
        this.indent = builder.getIndent();
        this.writeExplicitType = builder.shouldWriteExplicitType();
        this.includeXmlDeclaration = builder.shouldIncludeXmlDeclaration();
    }

    private DocumentBuilder newDocumentBuilder() {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        if (schema != null) {
            builderFactory.setSchema(schema);
        }

        try {
            return builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private Transformer newTransformer() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();

            // we write the header ourselves.
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            if (indent > 0) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(INDENT_PROPERTY, Integer.toString(indent));
            }
            return transformer;
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadInternal(SimpleAttributedConfigurationNode node, BufferedReader reader) throws IOException {
        DocumentBuilder documentBuilder = newDocumentBuilder();

        Document document;
        try {
            document = documentBuilder.parse(new InputSource(reader));
        } catch (SAXException e) {
            throw new IOException(e);
        }

        Element root = document.getDocumentElement();
        readElement(root, node);
    }

    private enum NodeType {
        MAP, LIST
    }

    private void readElement(Node from, SimpleAttributedConfigurationNode to) {
        NodeType type = null;

        // copy the name of the tag
        to.setTagName(from.getNodeName());

        // copy attributes
        if (from.hasAttributes()) {
            NamedNodeMap attributes = from.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String key = attribute.getNodeName();
                String value = attribute.getNodeValue();

                // read the type of the node
                if (key.equals("configurate-type")) {
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
        Multimap<String, Node> children = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        if (from.hasChildNodes()) {
            NodeList childNodes = from.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    children.put(child.getNodeName(), child);
                }
            }
        }

        // if there are no child nodes present, assume it's a scalar value
        if (children.isEmpty()) {
            to.setValue(parseValue(from.getTextContent()));
            return;
        }

        // if type is null, we need to infer what type the element is
        if (type == null) {
            // if there are no duplicate keys, we can infer that it is a map
            // otherwise, assume it's a list
            if (children.keys().size() == children.keySet().size()) {
                type = NodeType.MAP;
            } else {
                type = NodeType.LIST;
            }
        }

        if (type == NodeType.MAP) {
            to.setValue(ImmutableMap.of());
        } else {
            to.setValue(ImmutableList.of());
        }

        // read out the elements
        for (Map.Entry<String, Node> entry : children.entries()) {
            SimpleAttributedConfigurationNode child;
            if (type == NodeType.MAP) {
                child = to.getNode(entry.getKey());
            } else {
                child = to.getAppendedNode();
            }

            readElement(entry.getValue(), child);
        }
    }

    @Override
    protected void writeHeaderInternal(Writer writer) throws IOException {
        if (includeXmlDeclaration) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(SYSTEM_LINE_SEPARATOR);
        }
    }

    @Override
    protected void saveInternal(ConfigurationNode<?> node, Writer writer) throws IOException {
        DocumentBuilder documentBuilder = newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        document.appendChild(writeNode(document, node, null));

        Transformer transformer = newTransformer();
        DOMSource source = new DOMSource(document);
        try {
            transformer.transform(source, new StreamResult(writer));
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    private Element writeNode(Document document, ConfigurationNode<?> node, String forcedTag) {
        String tag = defaultTagName;
        Map<String, String> attributes = ImmutableMap.of();

        if (node instanceof AttributedConfigurationNode) {
            AttributedConfigurationNode<?> attributedNode = ((AttributedConfigurationNode<?>) node);
            tag = attributedNode.getTagName();
            attributes = attributedNode.getAttributes();
        }

        Element element = document.createElement(forcedTag == null ? tag : forcedTag);
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            element.setAttribute(attribute.getKey(), attribute.getValue());
        }

        if (node.hasMapChildren()) {
            for (Map.Entry<Object, ? extends ConfigurationNode<?>> child : node.getChildrenMap().entrySet()) {
                element.appendChild(writeNode(document, child.getValue(), child.getKey().toString()));
            }
        } else if (node.hasListChildren()) {
            if (writeExplicitType) {
                element.setAttribute("configurate-type", "list");
            }
            for (ConfigurationNode<?> child : node.getChildrenList()) {
                element.appendChild(writeNode(document, child, null));
            }
        } else {
            element.appendChild(document.createTextNode(node.getValue().toString()));
        }

        return element;
    }

    @NonNull
    @Override
    public SimpleAttributedConfigurationNode createEmptyNode(@NonNull ConfigurationOptions options) {
        options = options.setAcceptedTypes(ImmutableSet.of(Double.class, Long.class,
                Integer.class, Boolean.class, String.class, Number.class));
        return SimpleAttributedConfigurationNode.root("root", options);
    }

    private static Object parseValue(String value) {
        if (value.equals("true") || value.equals("false")) {
            return Boolean.parseBoolean(value);
        }

        try {
            double doubleValue = Double.parseDouble(value);
            if (DoubleMath.isMathematicalInteger(doubleValue)) {
                long longValue = (long) doubleValue;
                int intValue = (int) longValue;
                if (longValue == intValue) {
                    return intValue;
                } else {
                    return longValue;
                }
            }
            return doubleValue;
        } catch (NumberFormatException e) {
            return value;
        }
    }
}

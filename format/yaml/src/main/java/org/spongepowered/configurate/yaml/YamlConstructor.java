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

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

class YamlConstructor extends Constructor {

    private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\R");

    @Nullable ConfigurationOptions options;

    YamlConstructor(final LoaderOptions loadingConfig) {
        super(loadingConfig);
    }

    @Override
    @EnsuresNonNull("options")
    public Object getSingleData(final Class<?> type) {
        if (this.options == null) {
            throw new IllegalStateException("options must be set before calling load!");
        }
        return super.getSingleData(type);
    }

    @Override
    protected Object constructObjectNoCheck(final Node yamlNode) {
        //noinspection DataFlowIssue guarenteed NonNull by getSingleData, which load(Reader) uses
        final CommentedConfigurationNode node = CommentedConfigurationNode.root(this.options);

        if (yamlNode.getNodeId() == NodeId.mapping) {
            // make sure to mark it as a map type, even if the map itself is empty
            node.raw(Collections.emptyMap());
            final MappingNode mapping = (MappingNode) yamlNode;
            if (mapping.getFlowStyle() != null) {
                node.hint(YamlConfigurationLoader.NODE_STYLE, NodeStyle.fromSnakeYaml(mapping.getFlowStyle()));
            }

            for (final NodeTuple tuple : mapping.getValue()) {
                final ConfigurationNode keyNode = (ConfigurationNode) this.constructObject(tuple.getKeyNode());
                final Node valueNode = tuple.getValueNode();

                // comments are on the key, not the value
                node.node(keyNode.raw())
                    .from((ConfigurationNode) constructObject(valueNode))
                    .comment(commentFor(tuple.getKeyNode().getBlockComments()));
            }

            return node.comment(commentFor(yamlNode.getBlockComments()));
        }

        final Object raw = super.constructObjectNoCheck(yamlNode);
        if (raw instanceof Collection<?>) {
            // make sure to mark it as a list type, even if the collection itself is empty
            node.raw(Collections.emptyList());
            if (((SequenceNode) yamlNode).getFlowStyle() != null) {
                node.hint(YamlConfigurationLoader.NODE_STYLE, NodeStyle.fromSnakeYaml(((SequenceNode) yamlNode).getFlowStyle()));
            }

            for (final Object value : (Collection<?>) raw) {
                node.appendListNode().from((ConfigurationNode) value);
            }
        } else {
            if (yamlNode instanceof ScalarNode) {
                node.hint(YamlConfigurationLoader.SCALAR_STYLE, ScalarStyle.fromSnakeYaml(((ScalarNode) yamlNode).getScalarStyle()));
            }
            node.raw(raw);
        }

        return node.comment(commentFor(yamlNode.getBlockComments()));
    }

    private static @Nullable String commentFor(final @Nullable List<CommentLine> commentLines) {
        if (commentLines == null || commentLines.isEmpty()) {
            return null;
        }

        final StringBuilder outputBuilder = new StringBuilder();
        for (final CommentLine line : commentLines) {
            if (outputBuilder.length() > 0) {
                outputBuilder.append(AbstractConfigurationLoader.CONFIGURATE_LINE_SEPARATOR);
            }
            final String lineStripped = removeLineBreaksForLine(line.getValue());
            if (!lineStripped.isEmpty() && lineStripped.charAt(0) == ' ') {
                outputBuilder.append(lineStripped, 1, lineStripped.length());
            } else {
                outputBuilder.append(lineStripped);
            }
        }
        return outputBuilder.toString();
    }

    private static String removeLineBreaksForLine(final String line) {
        return LINE_BREAK_PATTERN.matcher(line).replaceAll("");
    }

}

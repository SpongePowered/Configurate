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

import static org.spongepowered.configurate.loader.AbstractConfigurationLoader.CONFIGURATE_LINE_PATTERN;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.ConfigurationNode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class YamlRepresenter extends Representer {

    private static final CommentLine BLANK_LINE = new CommentLine(null, null, "", CommentType.BLANK_LINE);

    private final boolean padComments;

    YamlRepresenter(final boolean padComments, final DumperOptions options) {
        super(options);
        this.padComments = padComments;
        multiRepresenters.put(ConfigurationNode.class, new ConfigurationNodeRepresent());
        nullRepresenter = new EmptyNullRepresenter();
    }

    private final class ConfigurationNodeRepresent implements Represent {
        @Override
        public Node representData(final Object nodeObject) {
            final ConfigurationNode node = (ConfigurationNode) nodeObject;

            final Node yamlNode;
            if (node.isMap()) {
                final List<NodeTuple> children = new ArrayList<>();
                boolean first = true;
                for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.childrenMap().entrySet()) {
                    // SnakeYAML supports both key and value comments. Add the comments on the key
                    final Node value = represent(ent.getValue());
                    final Node key = represent(ent.getKey());
                    key.setBlockComments(value.getBlockComments());
                    value.setBlockComments(Collections.emptyList());
                    if (
                            !first
                            && !(node.parent() != null && node.parent().isList())
                            && key.getBlockComments() != null
                            && !key.getBlockComments().isEmpty()
                    ) {
                        key.getBlockComments().add(0, BLANK_LINE);
                    }
                    first = false;

                    children.add(new NodeTuple(key, value));
                }
                yamlNode = new MappingNode(Tag.MAP, children, this.flowStyle(node));
            } else if (node.isList()) {
                final List<Node> children = new ArrayList<>();
                for (ConfigurationNode ent : node.childrenList()) {
                    children.add(represent(ent));
                }
                yamlNode = new SequenceNode(Tag.SEQ, children, this.flowStyle(node));
            } else {
                final Node optionNode = represent(node.rawScalar());
                final org.spongepowered.configurate.yaml.@Nullable ScalarStyle requestedStyle
                    = node.ownHint(YamlConfigurationLoader.SCALAR_STYLE);
                if (optionNode instanceof ScalarNode && requestedStyle != null) {
                    final ScalarNode scalar = (ScalarNode) optionNode;
                    yamlNode = new ScalarNode(
                        scalar.getTag(),
                        scalar.getValue(),
                        scalar.getStartMark(),
                        scalar.getEndMark(),
                        org.spongepowered.configurate.yaml.ScalarStyle.asSnakeYaml(requestedStyle, scalar.getScalarStyle())
                    );
                } else {
                    yamlNode = optionNode;
                }

            }

            if (node instanceof CommentedConfigurationNodeIntermediary<?>) {
                final @Nullable String nodeComment = ((CommentedConfigurationNodeIntermediary<?>) node).comment();
                if (nodeComment != null) {
                    yamlNode.setBlockComments(
                        Arrays.stream(CONFIGURATE_LINE_PATTERN.split(nodeComment))
                            .map(this::commentLineFor)
                            .collect(Collectors.toList())
                    );
                }
            }

            return yamlNode;
        }

        private FlowStyle flowStyle(final ConfigurationNode node) {
            final @Nullable NodeStyle requested = node.ownHint(YamlConfigurationLoader.NODE_STYLE);
            return NodeStyle.asSnakeYaml(requested);
        }

        private CommentLine commentLineFor(final String comment) {
            if (comment.isEmpty()) {
                return BLANK_LINE;
            } else if (!YamlRepresenter.this.padComments || comment.charAt(0) == '#') {
                return new CommentLine(null, null, comment, CommentType.BLOCK);
            }
            // prepend a space before the comment:
            // before: #hello
            // after:  # hello
            return new CommentLine(null, null, " " + comment, CommentType.BLOCK);
        }
    }

    private static final class EmptyNullRepresenter implements Represent {
        @Override
        public Node representData(final Object data) {
            return new ScalarNode(Tag.NULL, "", null, null, ScalarStyle.PLAIN);
        }
    }

}

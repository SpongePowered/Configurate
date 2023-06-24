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
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class YamlConstructor extends Constructor {

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
        final Object raw = super.constructObjectNoCheck(yamlNode);
        //noinspection DataFlowIssue guarenteed NonNull by getSingleData, which load(Reader) uses
        final CommentedConfigurationNode node = CommentedConfigurationNode.root(this.options);

        // SnakeYAML uses a LinkedHashMap to preserve key order
        if (raw instanceof LinkedHashMap<?, ?>) {
            // make sure to mark it as a map type, even if the map itself is empty
            node.raw(Collections.emptyMap());

            // Map is always a MappingNode
            final List<NodeTuple> tuples = ((MappingNode) yamlNode).getValue();

            // comments are on the key, but SnakeYAML uses strings as key, so we have to be a bit creative
            final AtomicInteger index = new AtomicInteger();
            ((LinkedHashMap<?, ?>) raw).forEach((key, value) -> {
                node.node(key)
                    .from((ConfigurationNode) value)
                    .comment(commentFor(tuples.get(index.getAndIncrement()).getKeyNode().getBlockComments()));
            });
        } else if (raw instanceof Collection<?>) {
            // make sure to mark it as a list type, even if the collection itself is empty
            node.raw(Collections.emptyList());

            ((Collection<?>) raw).forEach(value -> {
                node.appendListNode().from((ConfigurationNode) value);
            });
        } else {
            node.raw(raw);
        }

        if (yamlNode.getBlockComments() != null) {
            node.comment(commentFor(yamlNode.getBlockComments()));
        }

        return node;
    }

    private static @Nullable String commentFor(final @Nullable List<CommentLine> commentLines) {
        if (commentLines == null || commentLines.isEmpty()) {
            return null;
        }
        return commentLines.stream()
            .map(input -> {
                final String lineStripped = input.getValue().replace("\r", "");
                if (!lineStripped.isEmpty() && lineStripped.charAt(0) == ' ') {
                    return lineStripped.substring(1);
                } else {
                    return lineStripped;
                }
            })
            .collect(Collectors.joining("\n"));
    }

}

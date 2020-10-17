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
package org.spongepowered.configurate.loader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;

import java.io.BufferedReader;
import java.io.Writer;

/**
 * This test configuration loader holds a single {@link ConfigurationNode},
 * {@code result}, that is updated when a node is saved and loaded when
 * necessary.
 */
public class TestConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

    private ConfigurationNode result;

    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, TestConfigurationLoader> {

        @NonNull
        @Override
        public TestConfigurationLoader build() {
            return new TestConfigurationLoader(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    protected TestConfigurationLoader(final Builder builder) {
        super(builder, CommentHandlers.values());
    }

    @Override
    protected void loadInternal(final BasicConfigurationNode node, final BufferedReader reader) {
        node.from(this.result);
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) {
        this.result.from(node);
    }

    public ConfigurationNode node() {
        return this.result;
    }

    public void node(final ConfigurationNode node) {
        this.result = node;
    }

    /**
     * Return an empty node of the most appropriate type for this loader
     *
     * @param options the options to use with this node. Must not be null (take a look at {@link ConfigurationOptions#defaults()})
     * @return the appropriate node type
     */
    @Override
    public @NonNull BasicConfigurationNode createNode(final @NonNull ConfigurationOptions options) {
        return BasicConfigurationNode.root(options);
    }

}

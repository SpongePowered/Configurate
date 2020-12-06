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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.stream.Collectors;

/**
 * This test configuration loader uses the literal content
 * of a node as a String, performing no parsing.
 */
public class TestConfigurationLoader extends AbstractConfigurationLoader<BasicConfigurationNode> {

    public static final class Builder extends AbstractConfigurationLoader.Builder<Builder, TestConfigurationLoader> {

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
        node.raw(reader.lines().collect(Collectors.joining("\n")));
    }

    @Override
    protected void saveInternal(final ConfigurationNode node, final Writer writer) throws ConfigurateException {
        try {
            final @Nullable String value = node.getString();
            if (value != null) {
                writer.write(value);
            }
        } catch (final IOException ex) {
            throw new ConfigurateException(ex);
        }
    }

    @Override
    public @NonNull BasicConfigurationNode createNode(final @NonNull ConfigurationOptions options) {
        return BasicConfigurationNode.root(options);
    }

}

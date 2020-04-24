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
package org.spongepowered.configurate.examples;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.hocon.HOCONConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.objectmapping.serialize.ConfigSerializable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * An example of how to use the ObjectMapper for a simple read-only configuration.
 *
 * Error handling is not considered in this example, but for a fully fledged application it will be essential.
 */
public class ObjectMapperExample {
    public static void main(String[] args) throws IOException, ObjectMappingException {
        final Path file = Paths.get(args[0]);
        final HOCONConfigurationLoader loader = HOCONConfigurationLoader.builder()
                .setPath(file) // or setUrl(), or setFile(), or setSource/Sink
                .build();

        final CommentedConfigurationNode node = loader.load(); // Load from file
        final MyConfiguration config = MyConfiguration.loadFrom(node); // Populate object

        // Do whatever actions with the configuration, then...
        config.setItemName("Steve");

        config.saveTo(node); // Update the backing node
        loader.save(node); // Write to the original file
    }
}

@ConfigSerializable
class MyConfiguration {
    private static final ObjectMapper<MyConfiguration> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.forClass(MyConfiguration.class); // We hold on to the instance of our ObjectMapper
        } catch (ObjectMappingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static <N extends ScopedConfigurationNode<N>> MyConfiguration loadFrom(N node) throws ObjectMappingException {
        return MAPPER.bindToNew().populate(node);
    }

    @Setting(value = "item-name") // The key for a setting is normally provided by the field name, but can be overridden
    private @Nullable String itemName;
    @Setting(comment = "Here is a comment to describe the purpose of this field")
    private Pattern filter = Pattern.compile("cars?"); // Set defaults by initializing the field

    // As long as custom classes are annotated with @ConfigSerializable, they can be nested as ordinary fields.
    private List<Section> sections = new ArrayList<>();

    public @Nullable String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = requireNonNull(itemName, "itemName");
    }

    public Pattern getFilter() {
        return this.filter;
    }

    public List<Section> getSections() {
        return this.sections;
    }

    public <N extends ScopedConfigurationNode<N>> void saveTo(N node) throws ObjectMappingException {
        MAPPER.bind(this).serialize(node);
    }
}

@ConfigSerializable
class Section {
    @Setting
    private String name;

    @Setting
    private UUID id;

    // the ObjectMapper resolves settings based on fields -- these methods are provided as a convenience
    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }
}

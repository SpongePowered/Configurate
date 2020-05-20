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

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Paths;

public final class Tutorial {

    private Tutorial() {}

    public static void main(final String[] args) throws ObjectMappingException {
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .setPath(Paths.get("myproject.conf")) // Set where we will load and save to
                .build();

        final CommentedConfigurationNode root;
        try {
            root = loader.load();
        } catch (final IOException e) {
            System.err.println("An error occurred while loading this configuration: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            System.exit(1);
            return;
        }

        final ConfigurationNode countNode = root.getNode("messages", "count");
        final ConfigurationNode moodNode = root.getNode("messages", "mood");

        final @Nullable String name = root.getNode("name").getString();
        final int count = countNode.getInt(Integer.MIN_VALUE);
        final @Nullable Mood mood = moodNode.getValue(Mood.TYPE);

        if (name == null || count == Integer.MIN_VALUE || mood == null) {
            System.err.println("Invalid configuration");
            System.exit(2);
            return;
        }

        System.out.println("Hello, " + name + "!");
        System.out.println("You have " + count + " " + mood + " messages!");
        System.out.println("Thanks for viewing your messages");

        // Update values
        countNode.setValue(0); // native type
        moodNode.setValue(Mood.TYPE, Mood.NEUTRAL); // serialized type

        root.getNode("accesses").act(n -> {
            n.setCommentIfAbsent("The times messages have been accessed, in milliseconds since the epoch");
            n.appendListNode().setValue(System.currentTimeMillis());
        });

        // And save the node back to the file
        try {
            loader.save(root);
        } catch (final IOException e) {
            System.err.println("Unable to save your messages configuration! Sorry! " + e.getMessage());
            System.exit(1);
        }

    }

    /**
     * A mood that a message may have.
     */
    enum Mood {

        HAPPY, SAD, CONFUSED, NEUTRAL;

        public static final TypeToken<Mood> TYPE = TypeToken.of(Mood.class); // Keep track of our generic type, to avoid reinitialization

    }

}


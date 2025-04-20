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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Paths;

public final class Tutorial {

    private Tutorial() {}

    public static void main(final String[] args) throws SerializationException {
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(Paths.get("myproject.conf")) // Set where we will load and save to
                .build();

        final CommentedConfigurationNode root;
        try {
            root = loader.load();
        } catch (final ConfigurateException e) {
            System.err.println("An error occurred while loading this configuration: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            System.exit(1);
            return;
        }

        final ConfigurationNode countNode = root.node("messages", "count");
        final ConfigurationNode moodNode = root.node("messages", "mood");

        final @Nullable String name = root.node("name").getString();
        final int count = countNode.getInt(Integer.MIN_VALUE);
        final @Nullable Mood mood = moodNode.get(Mood.class);

        if (name == null || count == Integer.MIN_VALUE || mood == null) {
            System.err.println("Invalid configuration");
            System.exit(2);
            return;
        }

        System.out.println("Hello, " + name + "!");
        System.out.println("You have " + count + " " + mood + " messages!");
        System.out.println("Thanks for viewing your messages");

        // Update values
        countNode.raw(0); // native type
        moodNode.set(Mood.class, Mood.NEUTRAL); // serialized type

        root.node("accesses").act(n -> {
            n.commentIfAbsent("The times messages have been accessed, in milliseconds since the epoch");
            n.appendListNode().set(System.currentTimeMillis());
        });

        // And save the node back to the file
        try {
            loader.save(root);
        } catch (final ConfigurateException e) {
            System.err.println("Unable to save your messages configuration! Sorry! " + e.getMessage());
            System.exit(1);
        }

    }

    /**
     * A mood that a message may have.
     */
    @SuppressWarnings("checkstyle:JavadocVariable")
    enum Mood {

        HAPPY, SAD, CONFUSED, NEUTRAL;

    }

}

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

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.reference.WatchServiceListener;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ValueReferences implements AutoCloseable {

    private final WatchServiceListener listener;
    private final ConfigurationReference<CommentedConfigurationNode> base;
    private final ValueReference<String, CommentedConfigurationNode> name;
    private final ValueReference<Integer, CommentedConfigurationNode> cookieCount;
    private final ValueReference<List<TestObject>, CommentedConfigurationNode> complex;

    @ConfigSerializable
    static class TestObject {
        String name;
        UUID id = UUID.randomUUID();

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof TestObject)) {
                return false;
            }

            final TestObject that = (TestObject) o;
            return this.name.equals(that.name)
                    && this.id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.id);
        }

        @Override
        public String toString() {
            return "TestObject{"
                    + "name='" + this.name + '\''
                    + ", id=" + this.id
                    + '}';
        }
    }

    public ValueReferences(final Path configFile) throws IOException, ConfigurateException {
        // Initialize the watch service and node
        this.listener = WatchServiceListener.create();
        this.base = this.listener.listenToConfiguration(file -> HoconConfigurationLoader.builder()
                .defaultOptions(opts -> opts.shouldCopyDefaults(true))
                .path(file)
                .build(), configFile);

        // Perform actions on successful and unsuccessful reloads
        this.base.updates().subscribe($ -> System.out.println("Configuration auto-reloaded"));
        this.base.errors().subscribe(err -> {
            final Throwable thr = err.getValue();
            System.out.println("Unable to " + err.getKey() + " the configuration: " + thr.getMessage());
            if (thr.getCause() != null) {
                System.out.println(thr.getCause().getMessage());
            }
        });

        // Get node references
        this.name = this.base.referenceTo(String.class, "name");
        this.name.subscribe(newName -> System.out.println("Reloaded, name is: " + newName));
        this.cookieCount = this.base.referenceTo(Integer.class, NodePath.path("cookie-count"), 5);
        this.complex = this.base.referenceTo(new TypeToken<List<TestObject>>() {}, "complex");

        // And then save now that values have been initialized
        this.base.save();
    }

    @SuppressWarnings("SystemConsoleNull")
    public void repl() {
        boolean running = true;
        if (System.console() == null) {
            System.err.println("Not at an interactive prompt");
            this.printData();
            return;
        }

        while (running) {
            final @Nullable String next = System.console().readLine(">");
            if (next == null) {
                break;
            }
            final String[] cmd = next.split(" ", -1);
            if (cmd.length == 0) {
                continue;
            }

            switch (cmd[0]) {
                case "stop":
                    running = false;
                    break;
                case "name":
                    if (cmd.length < 2) {
                        System.err.println("Not enough arguments, usage: name <new-name>");
                        break;
                    }
                    this.name.setAndSave(cmd[1]);
                    System.out.println("Name: " + this.name.get());
                    break;
                case "dump":
                    printData();
                    break;
                case "help":
                    System.out.println("Value reference tester\n"
                            + "Commands:\n\n"
                            + "stop: Exit the loop\n"
                            + "name <name>: Update the name in the config file\n"
                            + "dump: Dump all accessed data in the config file\n"
                            + "help: Show this message"
                    );
                    break;
                default:
                    System.err.println("Unknown command '" + next + "'");
                    System.err.println("help for help");
            }
        }
    }

    public void printData() {
        System.out.println("Name: " + this.name.get());
        System.out.println("Cookie count: " + this.cookieCount.get());
        System.out.println("Complex: ");
        if (this.complex.get().isEmpty()) {
            System.out.println("(empty)");
        } else {
            for (TestObject obj : this.complex.get()) {
                System.out.println("- " + obj);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.base.close();
        this.listener.close();
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: ./reference-example <file>");
            return;
        }
        final Path path = Paths.get(args[0]);
        try (ValueReferences engine = new ValueReferences(path)) {
            engine.repl();
        } catch (final IOException e) { // may be a ConfigurateException, or something else
            System.out.println("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

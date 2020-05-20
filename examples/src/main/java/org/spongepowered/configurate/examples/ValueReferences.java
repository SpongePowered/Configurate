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
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.Setting;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.reference.WatchServiceListener;
import org.spongepowered.configurate.serialize.ConfigSerializable;
import org.spongepowered.configurate.transformation.NodePath;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ValueReferences {
    private final WatchServiceListener listener;
    private final ConfigurationReference<CommentedConfigurationNode> base;
    private final ValueReference<String, CommentedConfigurationNode> name;
    private final ValueReference<Integer, CommentedConfigurationNode> cookieCount;
    private final ValueReference<List<TestObject>, CommentedConfigurationNode> complex;

    @ConfigSerializable
   static class TestObject {
        @Setting
        String name;
        @Setting
        UUID id = UUID.randomUUID();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestObject)) return false;
            TestObject that = (TestObject) o;
            return name.equals(that.name) &&
                    id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, id);
        }

        @Override
        public String toString() {
            return "TestObject{" +
                    "name='" + name + '\'' +
                    ", id=" + id +
                    '}';
        }
    }

   public ValueReferences(Path configFile) throws IOException, ObjectMappingException {
       this.listener = WatchServiceListener.create();
       this.base = listener.listenToConfiguration(file -> HoconConfigurationLoader.builder().setDefaultOptions(o -> o.withShouldCopyDefaults(true)).setPath(file).build(), configFile);
       this.base.updates().subscribe($ -> System.out.println("Configuration auto-reloaded"));
       this.base.errors().subscribe(err -> {
           Throwable t = err.getValue();
           System.out.println("Unable to " + err.getKey() + " the configuration: " + t.getMessage());
           if (t.getCause() != null) {
               System.out.println(t.getCause().getMessage());
           }
       });

       name = this.base.referenceTo(String.class, "name");
       this.name.subscribe(newName -> System.out.println("Reloaded, name is: " + newName));
       cookieCount = this.base.referenceTo(Integer.class, NodePath.path("cookie-count"), 5);
       this.complex = this.base.referenceTo(new TypeToken<List<TestObject>>() {}, "complex");
       this.base.save();
   }

   public void repl() {
        boolean running = true;
        if (System.console() == null) {
            System.err.println("Not at an interactive prompt");
            this.printData();
            return;
        }

        while (running) {
            String next = System.console().readLine(">");
            if (next == null) {
                break;
            }
            String[] cmd = next.split(" ");
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
                    name.setAndSave(cmd[1]);
                    System.out.println("Name: " + this.name.get());
                    break;
                case "dump":
                    printData();
                    break;
                case "help":
                    System.out.println("Value reference tester\n" +
                            "Commands:\n\n" +
                            "stop: Exit the loop\n" +
                            "name <name>: Update the name in the config file\n" +
                            "dump: Dump all accessed data in the config file\n" +
                            "help: Show this message"
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

   public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: ./reference-example <file>");
            return;
        }
        final Path path = Paths.get(args[0]);
       try {
           new ValueReferences(path).repl();
       } catch (IOException | ObjectMappingException e) {
           System.out.println("Error loading configuration: " + e.getMessage());
           e.printStackTrace();
       }
   }

}

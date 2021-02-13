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
package org.spongepowered.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.serialize.SerializationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

@SuppressWarnings("UnusedVariable") // set by object mapper
public class RecordDiscovererTest {

    @ConfigSerializable
    @SuppressWarnings("UnusedVariable")
    record TestRecord(String name, int testable) {
    }

    @Test
    void testDeserializeToRecord() throws SerializationException {
        final var node = BasicConfigurationNode.root(n -> {
            n.node("name").raw("Hello");
            n.node("testable").raw(13);
        });

        final var element = ObjectMapper.factory().get(TestRecord.class).load(node);

        assertEquals(new TestRecord("Hello", 13), element);
    }

    @Test
    void testSerializeFromRecord() throws SerializationException {
        final var record = new TestRecord("meow", 32);
        final var target = BasicConfigurationNode.root();

        ObjectMapper.factory().get(TestRecord.class).save(record, target);

        assertEquals("meow", target.node("name").raw());
        assertEquals(32, target.node("testable").raw());
    }

    @Test
    void testAnnotationsApplied() throws SerializationException, MalformedURLException {
        @ConfigSerializable
        @SuppressWarnings("UnusedVariable")
        record AnnotatedRecord(
            @Required TestRecord element,
            @Comment("The most url") URL fetchLoc
        ) {}

        final var record = new AnnotatedRecord(new TestRecord("nested", 0xFACE),
                new URL("https://spongepowered.org/"));

        final var target = CommentedConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(Set.of(String.class, Integer.class)));

        ObjectMapper.factory().get(AnnotatedRecord.class).save(record, target);

        assertEquals("nested", target.node("element", "name").raw());
        assertEquals(0xFACE, target.node("element", "testable").raw());
        assertEquals("https://spongepowered.org/", target.node("fetch-loc").raw());
        assertEquals("The most url", target.node("fetch-loc").comment());
    }

    @ConfigSerializable
    @SuppressWarnings("UnusedVariable")
    record Empty(@Nullable String value) {

        public Empty {
            if (value == null) {
                value = "<unknown>";
            }
        }

    }

    @ConfigSerializable
    @SuppressWarnings("UnusedVariable")
    record ImplicitlyFillable(Empty something, Set<String> somethingElse) {

        public ImplicitlyFillable {
            somethingElse = Set.copyOf(somethingElse);
        }

    }

    @Test
    void testImplicitDefaultsLoaded() throws SerializationException {
        final var filled =
                ObjectMapper.factory().get(ImplicitlyFillable.class)
                        .load(BasicConfigurationNode.root(ConfigurationOptions.defaults()
                                .implicitInitialization(true)));

        assertEquals(new Empty("<unknown>"), filled.something());
        assertEquals(Set.of(), filled.somethingElse());
    }

    @Test
    void testPrimitveDefaultsFilled() throws SerializationException {
        @ConfigSerializable
        record WithPrimitives(@Nullable String test, int helloWorld) {}

        final var withPrimitives = BasicConfigurationNode.root(n -> {
            n.node("test").set("Hi");
        }).get(WithPrimitives.class);

        assertEquals("Hi", withPrimitives.test());
        assertEquals(0, withPrimitives.helloWorld());
    }

}

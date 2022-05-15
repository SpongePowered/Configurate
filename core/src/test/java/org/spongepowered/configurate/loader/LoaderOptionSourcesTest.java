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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.UnmodifiableCollections;

class LoaderOptionSourcesTest {

    @Test
    void testReadSystemProperties() {
        final LoaderOptionSource systemProperties = LoaderOptionSource.systemProperties();
        assertNull(systemProperties.get("a"));
        assertNull(systemProperties.get("a", "b"));

        System.setProperty("a", "test");
        System.setProperty("a.b", "test2");

        assertNull(systemProperties.get("a"));
        assertNull(systemProperties.get("a", "b"));

        System.setProperty("configurate.a", "test");
        System.setProperty("configurate.a.b", "test2");

        assertEquals("test", systemProperties.get("a"));
        assertEquals("test2", systemProperties.get("a", "b"));
    }

    @Test
    void testReadSystemPropertiesCustomPrefix() {
        final LoaderOptionSource customProperties = LoaderOptionSource.systemProperties("test");

        // Unset
        assertNull(customProperties.get("a"));
        assertNull(customProperties.get("a", "b"));

        // Setting with no prefix does not read
        System.setProperty("a", "test");
        System.setProperty("a.b", "test2");

        assertNull(customProperties.get("a"));
        assertNull(customProperties.get("a", "b"));

        // Setting with default prefix does not read
        System.setProperty("configurate.a", "test");
        System.setProperty("configurate.a.b", "test2");

        assertNull(customProperties.get("a"));
        assertNull(customProperties.get("a", "b"));

        // Setting with our custom prefix *does* work
        System.setProperty("test.a", "test");
        System.setProperty("test.a.b", "test2");

        assertEquals("test", customProperties.get("a"));
        assertEquals("test2", customProperties.get("a", "b"));
    }

    @Test
    void testEmptyPathFailsSystemProperties() {
        assertThrows(IllegalArgumentException.class, () -> LoaderOptionSource.systemProperties().get());
    }

    @Test
    @Disabled // todo: no way to set
    void testReadEnvironmentVariable() {
        final LoaderOptionSource environmentVariables = LoaderOptionSource.environmentVariables();
        assertNull(environmentVariables.get("a"));
        assertNull(environmentVariables.get("a", "b"));

        System.getenv().put("a", "test");
        System.setProperty("a.b", "test2");

        assertNull(environmentVariables.get("a"));
        assertNull(environmentVariables.get("a", "b"));

        System.setProperty("configurate.a", "test");
        System.setProperty("configurate.a.b", "test2");

        assertEquals("test", environmentVariables.get("a"));
        assertEquals("test2", environmentVariables.get("a", "b"));
    }

    @Test
    @Disabled // todo: no way to set
    void testReadEnvironmentVariableCustomPrefix() {
        final LoaderOptionSource environmentVariables = LoaderOptionSource.environmentVariables("test");

        // Unset
        assertNull(environmentVariables.get("a"));
        assertNull(environmentVariables.get("a", "b"));

        // Setting with no prefix does not read
        System.setProperty("a", "test");
        System.setProperty("a.b", "test2");

        assertNull(environmentVariables.get("a"));
        assertNull(environmentVariables.get("a", "b"));

        // Setting with default prefix does not read
        System.setProperty("configurate.a", "test");
        System.setProperty("configurate.a.b", "test2");

        assertNull(environmentVariables.get("a"));
        assertNull(environmentVariables.get("a", "b"));

        // Setting with our custom prefix *does* work
        System.setProperty("test.a", "test");
        System.setProperty("test.a.b", "test2");

        assertEquals("test", environmentVariables.get("a"));
        assertEquals("test2", environmentVariables.get("a", "b"));
    }

    @Test
    void testEmptyPathFailsEnvironmentVariable() {
        assertThrows(IllegalArgumentException.class, () -> LoaderOptionSource.environmentVariables().get());
    }

    @Test
    void testReadNode() throws SerializationException {
        final ConfigurationNode node = BasicConfigurationNode.root(
            ConfigurationOptions.defaults().nativeTypes(UnmodifiableCollections.toSet(String.class))
        );
        final LoaderOptionSource nodeSource = LoaderOptionSource.node(node);

        assertNull(nodeSource.get("a"));
        node.node("a").set("test");
        assertEquals("test", nodeSource.get("a"));

        assertNull(nodeSource.get("b", "c"));
        node.node("b", "c").set("test2");
        assertEquals("test2", nodeSource.get("b", "c"));
    }

    @Test
    void testEmptyPathFailsNode() {
        assertThrows(IllegalArgumentException.class, () -> LoaderOptionSource.node(BasicConfigurationNode.root()).get());
    }

    @Test
    void testCompositeFoundInFirst() {
        final LoaderOptionSource node = LoaderOptionSource.node(BasicConfigurationNode.root());
        final LoaderOptionSource source = LoaderOptionSource.composite(
            LoaderOptionSource.systemProperties(),
            node
        );

        System.setProperty("configurate.composite.test", "hello");

        assertEquals("hello", source.get("composite", "test"));
    }

    @Test
    void testCompositeFoundInSecond() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        final LoaderOptionSource source = LoaderOptionSource.composite(
            LoaderOptionSource.systemProperties(),
            LoaderOptionSource.node(node)
        );

        node.node("composite", "test2").set("hello2");

        assertEquals("hello2", source.get("composite", "test2"));
    }

    @Test
    void testCompositeNotFound() {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        final LoaderOptionSource source = LoaderOptionSource.composite(
            LoaderOptionSource.systemProperties(),
            LoaderOptionSource.node(node)
        );

        assertNull(source.get("composite", "unknown"));
    }

}

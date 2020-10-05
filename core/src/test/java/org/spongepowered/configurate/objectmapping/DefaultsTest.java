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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
import java.util.List;

/**
 * Tests for application of defaults
 */
public class DefaultsTest {

    public static final ConfigurationOptions IMPLICIT_OPTS = ConfigurationOptions.defaults()
            .implicitInitialization(true);

    @ConfigSerializable
    static class ImplicitDefaultsOnly {
        List<String> myStrings;
        AnotherThing funTimes;
        int[] items;
    }

    @ConfigSerializable
    static class AnotherThing {

    }

    @Test
    void testFieldsInitialized() throws SerializationException {
        final ImplicitDefaultsOnly instance = ObjectMapper.factory().get(ImplicitDefaultsOnly.class).load(BasicConfigurationNode.root(IMPLICIT_OPTS));
        assertEquals(Collections.emptyList(), instance.myStrings);
        assertNotNull(instance.funTimes);
        assertNotNull(instance.items);
        assertEquals(0, instance.items.length);
    }

    @Test
    void testImplicitDefaultsSaved() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(IMPLICIT_OPTS.shouldCopyDefaults(true));
        node.get(ImplicitDefaultsOnly.class);

        assertPresentAndEmpty(node.node("my-strings"));
        assertPresentAndEmpty(node.node("fun-times"));
        assertPresentAndEmpty(node.node("items"));
    }

    private void assertPresentAndEmpty(final ConfigurationNode node) {
        assertFalse(node.virtual());
        assertTrue(node.empty());
    }

}

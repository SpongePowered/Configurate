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

import java.util.Collections;
import java.util.List;

/**
 * Tests for application of defaults
 */
public class DefaultsTest {

    public static final ConfigurationOptions IMPLICIT_OPTS = ConfigurationOptions.defaults()
            .withImplicitInitialization(true);

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
    void testFieldsInitialized() throws ObjectMappingException {
        final ImplicitDefaultsOnly instance = ObjectMapper.factory().get(ImplicitDefaultsOnly.class).load(BasicConfigurationNode.root(IMPLICIT_OPTS));
        assertEquals(Collections.emptyList(), instance.myStrings);
        assertNotNull(instance.funTimes);
        assertNotNull(instance.items);
        assertEquals(0, instance.items.length);
    }

    @Test
    void testImplicitDefaultsSaved() throws ObjectMappingException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(IMPLICIT_OPTS.withShouldCopyDefaults(true));
        node.getValue(ImplicitDefaultsOnly.class);

        assertPresentAndEmpty(node.getNode("my-strings"));
        assertPresentAndEmpty(node.getNode("fun-times"));
        assertPresentAndEmpty(node.getNode("items"));
    }

    private void assertPresentAndEmpty(final ConfigurationNode node) {
        assertFalse(node.isVirtual());
        assertTrue(node.isEmpty());
    }

}

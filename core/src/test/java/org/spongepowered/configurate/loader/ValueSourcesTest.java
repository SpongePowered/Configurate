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

import net.kyori.option.Option;
import net.kyori.option.OptionSchema;
import net.kyori.option.value.ValueSource;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.UnmodifiableCollections;

class ValueSourcesTest {

    @Test
    void testReadNode() throws SerializationException {
        final OptionSchema.Mutable schema = OptionSchema.emptySchema();
        final Option<String> a = schema.stringOption("a", null);
        final Option<String> b_c = schema.stringOption("b/c", null);

        final ConfigurationNode node = BasicConfigurationNode.root(
            ConfigurationOptions.defaults().nativeTypes(UnmodifiableCollections.toSet(String.class))
        );
        final ValueSource nodeSource = ValueSources.node(node);

        assertNull(nodeSource.value(a));
        node.node("a").set("test");
        assertEquals("test", nodeSource.value(a));

        assertNull(nodeSource.value(b_c));
        node.node("b", "c").set("test2");
        assertEquals("test2", nodeSource.value(b_c));
    }

}

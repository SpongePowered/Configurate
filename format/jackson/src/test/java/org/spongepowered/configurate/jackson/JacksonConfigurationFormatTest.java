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
package org.spongepowered.configurate.jackson;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationFormat;

class JacksonConfigurationFormatTest {

    @Test
    void testJacksonFormatPresent() {
        final @Nullable ConfigurationFormat format = ConfigurationFormat.forExtension("json");
        assertNotNull(format);
    }

    @Test
    void testLoadJackson() throws ConfigurateException {
        final @Nullable ConfigurationFormat format = ConfigurationFormat.forExtension("json");
        final ConfigurationNode node = format.create(this.getClass().getResource("simple.json")).load();
        assertTrue(node.node("test").getBoolean());
    }

}

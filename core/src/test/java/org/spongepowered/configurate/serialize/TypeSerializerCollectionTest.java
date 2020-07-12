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
package org.spongepowered.configurate.serialize;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.util.Arrays;
import java.util.List;

public class TypeSerializerCollectionTest {

    @Test
    public void testResolveWildcard() throws ObjectMappingException {
        final ConfigurationNode node = BasicConfigurationNode.root(ConfigurationOptions.defaults()
            .withSerializers(b -> b.register(TypeToken.of(Object.class), new PassthroughSerializer())), n -> {
                n.appendListNode().setValue("a string");
                n.appendListNode().setValue(14);
            });

        final @Nullable List<?> value = node.getValue(new TypeToken<List<?>>() {
        });
        assertEquals(Arrays.asList("a string", 14), value);
    }

}

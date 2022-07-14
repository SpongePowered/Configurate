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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

class ScopedConfigurationNodeTest {

    @Test
    void testSet() {
        final var node = BasicConfigurationNode.root();
        Assertions.assertThrows(
                SerializationException.class,
                () -> node.set(TestRecord.class, new TestRecord())
        );
    }

    @ConfigSerializable
    record TestRecord(
            @Setting NonRegisteredClass non
    ) {

        TestRecord() {
            this(new NonRegisteredClass());
        }
    }

    static final class NonRegisteredClass {

        @Setting
        private final String test;

        public NonRegisteredClass(String test) {
            this.test = test;
        }

        public NonRegisteredClass() {
            this("test");
        }
    }
}

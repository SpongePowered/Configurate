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
package org.spongepowered.configurate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.errorprone.annotations.Keep;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.serialize.SerializationException;

class ScopedConfigurationNodeTest {

    @Test
    void testSet() {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        assertThatThrownBy(() -> node.set(TestClass.class, new TestClass()))
            .isInstanceOf(SerializationException.class)
            .hasMessageContaining("No TypeSerializer found for field non");
    }

    @ConfigSerializable
    static final class TestClass {
        @Keep
        private NonRegisteredClass non = new NonRegisteredClass();
    }

    static final class NonRegisteredClass {

        @Keep
        private final String test;

        NonRegisteredClass() {
            this.test = "test";
        }

    }

}

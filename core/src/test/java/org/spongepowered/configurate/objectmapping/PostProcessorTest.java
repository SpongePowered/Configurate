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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.serialize.SerializationException;

class PostProcessorTest {

    @Test
    void testAnnotatedPostProcessor() throws SerializationException {
        final ConfigurationNode node = BasicConfigurationNode.root();
        final MethodTest result = node.require(MethodTest.class);

        assertTrue(result.postProcessCalled);
    }

    @ConfigSerializable
    static class MethodTest {

        private transient boolean postProcessCalled;

        @PostProcess
        void afterEvaluate() {
            this.postProcessCalled = true;
        }

    }

}

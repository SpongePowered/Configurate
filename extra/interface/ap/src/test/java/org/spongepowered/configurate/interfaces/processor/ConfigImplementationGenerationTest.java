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
package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.TestUtils.EXPECT_CONFIG_AND_MAPPING;
import static org.spongepowered.configurate.interfaces.processor.TestUtils.testCompilation;

import org.junit.experimental.runners.Enclosed;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
class ConfigImplementationGenerationTest {

    @Test
    void testBasicCompilation() {
        testCompilation("test/BasicConfig", EXPECT_CONFIG_AND_MAPPING);
    }

    @Test
    void testExtendedCompilation() {
        testCompilation("test/ExtendedConfig", EXPECT_CONFIG_AND_MAPPING);
    }

    @Test
    void testMultiLayerCompilation() {
        testCompilation("test/MultiLayerConfig", EXPECT_CONFIG_AND_MAPPING);
    }
}

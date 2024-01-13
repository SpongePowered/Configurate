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

import static org.spongepowered.configurate.interfaces.processor.TestUtils.testCompilation;

import org.junit.experimental.runners.Enclosed;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
class ConfigImplementationGenerationTest {

    @Test
    void testBasicCompilation() {
        testCompilation("structure/BasicConfig");
    }

    @Test
    void testExtendedCompilation() {
        testCompilation("structure/ExtendedConfig");
    }

    @Test
    void testMultiLayerCompilation() {
        testCompilation("structure/MultiLayerConfig");
    }

    @Test
    void testAnnotationOthersCompilation() {
        testCompilation("test/OtherAnnotations");
    }

}

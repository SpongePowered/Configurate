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
package org.spongepowered.configurate.yaml

import static org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test

/**
 * End-to-end tests using sample configurations sourced
 * from production projects.
 */
class IntegrationTests implements YamlTest {

    @Test
    void testEssentialsXDefault() {
        def input = this.class.getResourceAsStream("essx-example.yml").text
        def node = parseString(input)

        def serialized = dump(node)

        assertThat(serialized).isEqualTo(input)
    }

    @Test
    void testEssentialsXLegacy() {
        def input = this.class.getResourceAsStream("essx-legacy.yml").text
        def node = parseString(input)

        def serialized = dump(node)

        assertThat(serialized).isEqualTo(input)
    }

    @Test
    void testMobCleaner() {
        def input = this.class.getResourceAsStream("mobcleaner-example.yml").text
        def node = parseString(input)

        def serialized = dump(node)

        assertThat(serialized).isEqualTo(input)
    }
}

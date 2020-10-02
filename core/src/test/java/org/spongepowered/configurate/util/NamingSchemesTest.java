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
package org.spongepowered.configurate.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NamingSchemesTest {

    @Test
    public void testCamelCasePassthrough() {
        assertEquals("camelCase", NamingSchemes.CAMEL_CASE.coerce("camelCase"));
        assertEquals("camels", NamingSchemes.CAMEL_CASE.coerce("camels"));
    }

    @Test
    public void testSnakeCasePassthrough() {
        assertEquals("snake_case", NamingSchemes.SNAKE_CASE.coerce("snake_case"));
        assertEquals("snake", NamingSchemes.SNAKE_CASE.coerce("snake"));
    }

    @Test
    public void testDashSeparatedPassthrough() {
        assertEquals("dash-separated", NamingSchemes.LOWER_CASE_DASHED.coerce("dash-separated"));
        assertEquals("dash", NamingSchemes.LOWER_CASE_DASHED.coerce("dash"));
    }

    @Test
    public void testCamelToSnake() {
        assertEquals("chat_radius", NamingSchemes.SNAKE_CASE.coerce("chatRadius"));
        assertEquals("max_growth_area", NamingSchemes.SNAKE_CASE.coerce("maxGrowthArea"));
    }

    @Test
    public void testCamelToDashed() {
        assertEquals("quick-fix", NamingSchemes.LOWER_CASE_DASHED.coerce("quickFix"));
        assertEquals("lets-go-again", NamingSchemes.LOWER_CASE_DASHED.coerce("letsGoAgain"));
    }

    @Test
    public void testSnakeToCamel() {
        assertEquals("getDamageSource", NamingSchemes.CAMEL_CASE.coerce("get_damage_source"));
        assertEquals("setTarget", NamingSchemes.CAMEL_CASE.coerce("set_target"));
    }

    @Test
    public void testSnakeToDashed() {
        assertEquals("get-damage-source", NamingSchemes.LOWER_CASE_DASHED.coerce("get_damage_source"));
        assertEquals("set-target", NamingSchemes.LOWER_CASE_DASHED.coerce("set_target"));
    }

    @Test
    public void testDashedToCamel() {
        assertEquals("nextTarget", NamingSchemes.CAMEL_CASE.coerce("next-target"));
        assertEquals("defaultSpawnPoint", NamingSchemes.CAMEL_CASE.coerce("default-spawn-point"));
    }

    @Test
    public void testDashedToSnake() {
        assertEquals("next_target", NamingSchemes.SNAKE_CASE.coerce("next-target"));
        assertEquals("default_spawn_point", NamingSchemes.SNAKE_CASE.coerce("default-spawn-point"));
    }

    @Test
    public void testLeadingDelimiterPassesThrough() {
        assertEquals("-days", NamingSchemes.SNAKE_CASE.coerce("-days"));
        assertEquals("-days", NamingSchemes.LOWER_CASE_DASHED.coerce("-days"));
        assertEquals("-days", NamingSchemes.CAMEL_CASE.coerce("-days"));
        assertEquals("_days", NamingSchemes.SNAKE_CASE.coerce("_days"));
        assertEquals("_days", NamingSchemes.LOWER_CASE_DASHED.coerce("_days"));
        assertEquals("_days", NamingSchemes.CAMEL_CASE.coerce("_days"));
    }

    @Test
    public void testTrailingDelimiterPassesThrough() {
        assertEquals("days-", NamingSchemes.LOWER_CASE_DASHED.coerce("days-"));
        assertEquals("days-", NamingSchemes.SNAKE_CASE.coerce("days-"));
        assertEquals("days-", NamingSchemes.CAMEL_CASE.coerce("days-"));
        assertEquals("days_", NamingSchemes.LOWER_CASE_DASHED.coerce("days_"));
        assertEquals("days_", NamingSchemes.SNAKE_CASE.coerce("days_"));
        assertEquals("days_", NamingSchemes.CAMEL_CASE.coerce("days_"));
    }

    @Test
    public void testAccents() {
        assertEquals("usingÜmlauts", NamingSchemes.CAMEL_CASE.coerce("using-ümlauts"));
        assertEquals("with_àccents", NamingSchemes.SNAKE_CASE.coerce("with-àccents"));
        assertEquals("with_àccents", NamingSchemes.SNAKE_CASE.coerce("withÀccents"));
        assertEquals("usingCombiningU\u0308mlauts", NamingSchemes.CAMEL_CASE.coerce("using-combining-u\u0308mlauts"));
    }

    @Test
    public void testNonBmpCodePoints() {
        assertEquals("using𝔐ath", NamingSchemes.CAMEL_CASE.coerce("using-𝔐ath"));
        assertEquals("asdf-𐐴hjkl", NamingSchemes.LOWER_CASE_DASHED.coerce("asdf𐐌hjkl"));
    }

}

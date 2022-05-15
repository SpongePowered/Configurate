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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

class LoaderOptionsSourceTest {

    @Test
    void testDefaultHandled() {
        assertEquals("nothing", LoaderOptionSource.systemProperties().getOr("nothing", "unknown", "property"));
    }

    @Test
    void testBoolean() {
        // Values
        assertTrue(new TestSource("true").getBoolean(false, "x"));
        assertFalse(new TestSource("false").getBoolean(true, "x"));

        // Defaults
        assertFalse(new TestSource(null).getBoolean(false, "y"));
        assertTrue(new TestSource(null).getBoolean(true, "y"));
    }

    enum TestEnum {
        ONE,
        TWO;
    }

    @Test
    void testEnum() {
        assertEquals(TestEnum.ONE, new TestSource("one").getEnum(TestEnum.class, "x"));
        assertEquals(TestEnum.TWO, new TestSource(null).getEnum(TestEnum.class, TestEnum.TWO, "x"));
        assertEquals(TestEnum.TWO, new TestSource("nothing").getEnum(TestEnum.class, TestEnum.TWO, "x"));
    }

    @Test
    void testInt() {
        assertEquals(1, new TestSource("1").getInt(-1, "y"));
        assertEquals(65536, new TestSource("65536").getInt(-1, "y"));
        assertEquals(255, new TestSource("0xFF").getInt(-1, "y"));

        // Default from invalid + null
        assertEquals(123, new TestSource(null).getInt(123, "y"));
        assertEquals(123, new TestSource("i'm not valid").getInt(123, "y"));
    }

    @Test
    void testDouble() {
        assertEquals(-0.1, new TestSource("-0.1").getDouble(-1, "y"));
        assertEquals(2, new TestSource("2").getDouble(-1, "y"));
        assertEquals(13e5, new TestSource("13e5").getDouble(-1, "y"));

        // Default from invalid + null
        assertEquals(123.45, new TestSource(null).getDouble(123.45, "y"));
        assertEquals(123.45, new TestSource("i'm not valid").getDouble(123.45, "y"));
    }

    static class TestSource implements LoaderOptionSource {
        private final @Nullable String fixedValue;

        TestSource(final @Nullable String fixedValue) {
            this.fixedValue = fixedValue;
        }

        @Override
        public @Nullable String get(final String... path) {
            return this.fixedValue;
        }
    }

}

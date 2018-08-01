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
package ninja.leaping.configurate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TypesTest {

    @Test
    public void testAsString() throws Exception {
        final String actualString = "actually a string";
        final Integer number = 54;
        final List<Integer> list = Arrays.asList(4, 3, 8);
        assertEquals(actualString, Types.asString(actualString));
        assertEquals("54", Types.asString(number));
        assertEquals("[4, 3, 8]", Types.asString(list));
    }

    @Test
    public void testStrictAsString() throws Exception {
        final String actualString = "actually a string";
        final Integer number = 54;
        final List<Integer> list = Arrays.asList(4, 3, 8);
        assertEquals(actualString, Types.strictAsString(actualString));
        assertNull(Types.strictAsString(number));
        assertNull(Types.strictAsString(list));
    }

    @Test
    public void testAsFloat() throws Exception {
        final float actuallyFloat = 1.45f;
        final String floatString = "1.45";
        final int integer = 4;
        assertEquals((Float) actuallyFloat, Types.asFloat(actuallyFloat));
        assertEquals((Float) 1.45f, Types.asFloat(floatString));
        assertEquals((Float) 4f, Types.asFloat(integer));
    }

    @Test
    public void testStrictAsFloat() throws Exception {
        final float actuallyFloat = 1.45f;
        final String floatString = "1.45";
        final int integer = 4;
        assertEquals((Float) actuallyFloat, Types.strictAsFloat(actuallyFloat));
        assertNull(Types.strictAsFloat(floatString));
        assertEquals((Float) 4f, Types.strictAsFloat(integer));

    }

    @Test
    public void testAsDouble() throws Exception {
        final double actuallyDouble = 1.45f;
        final String doubleString = "1.45";
        final int integer = 4;
        assertEquals((Double) actuallyDouble, Types.asDouble(actuallyDouble));
        assertEquals((Double) 1.45d,  Types.asDouble(doubleString));
        assertEquals((Double) 4d, Types.asDouble(integer));

    }

    @Test
    public void testStrictAsDouble() throws Exception {
        final double actuallyDouble = 1.45f;
        final String doubleString = "1.45";
        final int integer = 4;
        assertEquals((Double) actuallyDouble, Types.strictAsDouble(actuallyDouble));
        assertNull(Types.strictAsDouble(doubleString));
        assertEquals((Double) 4d, Types.strictAsDouble(integer));

    }

    @Test
    public void testAsInt() throws Exception {
        final int actuallyInt = 4;
        final String doubleString = "42";
        final float integer = 4f;
        assertEquals((Integer) actuallyInt, Types.asInt(actuallyInt));
        assertEquals((Integer) 42,  Types.asInt(doubleString));
        assertEquals((Integer) 4, Types.asInt(integer));
    }

    @Test
    public void testStrictAsInt() throws Exception {
        final int actuallyInt = 4;
        final String doubleString = "42";
        final float integer = 4f;
        assertEquals((Integer) actuallyInt, Types.strictAsInt(actuallyInt));
        assertNull(Types.strictAsInt(doubleString));
        assertNull(Types.strictAsInt(integer));
    }

    @Test
    public void testAsLong() throws Exception {
        final long actuallyInt = 4934285231847238472L;
        final String doubleString = "424338492842";
        final double integer = 4f;
        assertEquals((Long) actuallyInt, Types.asLong(actuallyInt));
        assertEquals((Long) 424338492842L,  Types.asLong(doubleString));
        assertEquals((Long) 4L, Types.asLong(integer));
    }

    @Test
    public void testStrictAsLong() throws Exception {
        final long actuallyInt = 4934285231847238472L;
        final String doubleString = "424338492842";
        final double integer = 4f;
        assertEquals((Long) actuallyInt, Types.strictAsLong(actuallyInt));
        assertNull(Types.strictAsLong(doubleString));
        assertNull(Types.strictAsLong(integer));
    }

    @Test
    public void testAsBoolean() throws Exception {
        final boolean actual = true;
        final String[] trueEvaluating = new String[] {"true", "yes", "1", "t", "y"}, falseEvaluating = new String[]
                {"false", "no", "0", "f", "n"};
        assertEquals(actual, Types.asBoolean(actual));
        for (String val : trueEvaluating) {
            assertEquals(true, Types.asBoolean(val));
        }

        for (String val : falseEvaluating) {
            assertEquals(false, Types.asBoolean(val));
        }
    }

    @Test
    public void testStrictAsBoolean() throws Exception {
        final boolean actual = true;
        final String[] trueEvaluating = new String[] {"true", "yes", "1", "t", "y"}, falseEvaluating = new String[]
                {"false", "no", "0", "f", "n"};
        assertEquals(actual, Types.strictAsBoolean(actual));
        for (String val : trueEvaluating) {
            assertNull(Types.strictAsBoolean(val));
        }

        for (String val : falseEvaluating) {
            assertNull(Types.strictAsBoolean(val));
        }
    }
}
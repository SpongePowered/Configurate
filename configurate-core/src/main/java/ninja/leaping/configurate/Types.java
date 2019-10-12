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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Contains functions useful for performing configuration type conversions.
 *
 * <p>The naming scheme is as follows:</p>
 *
 * <ul>
 *     <li><code>as</code> methods attempt to convert the data passed to the appropriate type</li>
 *     <li><code>strictAs</code> methods will only return values if the input value is already of an appropriate type</li>
 * </ul>
 */
public final class Types {
    private Types() {}

    /**
     * Attempts to convert <code>value</code> to a {@link String}.
     *
     * <p>Returns null if <code>value</code> is null, and the {@link Object#toString()}
     * representation of <code>value</code> otherwise.</p>
     *
     * @param value The value
     * @return <code>value</code> as a {@link String}, or null
     * @see Object#toString()
     */
    @Nullable
    public static String asString(@Nullable Object value) {
        return value == null ? null : value.toString();
    }

    /**
     * Returns <code>value</code> if it is a {@link String}.
     *
     * @param value The value
     * @return <code>value</code> as a {@link String}, or null
     */
    @Nullable
    public static String strictAsString(@Nullable Object value) {
        return value instanceof String ? (String) value : null;
    }

    /**
     * Attempts to convert <code>value</code> to a {@link Float}.
     *
     * <p>Returns null if <code>value</code> is null.</p>
     *
     * <p>This method will attempt to cast <code>value</code> to {@link Float}, or
     * {@link Float#parseFloat(String) parse} the <code>value</code> if it is a {@link String}.</p>
     *
     * @param value The value
     * @return <code>value</code> as a {@link Float}, or null
     */
    @Nullable
    public static Float asFloat(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Integer) {
            return ((Number) value).floatValue();
        }

        try {
            return Float.parseFloat(value.toString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Returns <code>value</code> if it is a {@link Float}.
     *
     * @param value The value
     * @return <code>value</code> as a {@link Float}, or null
     */
    @Nullable
    public static Float strictAsFloat(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Float
                || value instanceof Integer) {
            return ((Number) value).floatValue();
        }

        return null;
    }

    /**
     * Attempts to convert <code>value</code> to a {@link Double}.
     *
     * <p>Returns null if <code>value</code> is null.</p>
     *
     * <p>This method will attempt to cast <code>value</code> to {@link Double}, or
     * {@link Double#parseDouble(String) parse} the <code>value</code> if it is a {@link String}.</p>
     *
     * @param value The value
     * @return <code>value</code> as a {@link Float}, or null
     */
    @Nullable
    public static Double asDouble(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer
                || value instanceof Long
                || value instanceof Float) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Returns <code>value</code> if it is a {@link Double}.
     *
     * @param value The value
     * @return <code>value</code> as a {@link Double}, or null
     */
    @Nullable
    public static Double strictAsDouble(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Double
                || value instanceof Float
                || value instanceof Integer
                || value instanceof Long) {
            return ((Number) value).doubleValue();
        }

        return null;
    }

    /**
     * Attempts to convert <code>value</code> to a {@link Integer}.
     *
     * <p>Returns null if <code>value</code> is null.</p>
     *
     * <p>This method will attempt to cast <code>value</code> to {@link Integer}, or
     * {@link Integer#parseInt(String) parse} the <code>value</code> if it is a {@link String}.</p>
     *
     * @param value The value
     * @return <code>value</code> as a {@link Float}, or null
     */
    @Nullable
    public static Integer asInt(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Float
            || value instanceof Double) {
            double val = ((Number) value).doubleValue();
            if (val == Math.floor(val)) {
                return (int) val;
            }
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Returns <code>value</code> if it is a {@link Integer}.
     *
     * @param value The value
     * @return <code>value</code> as a {@link Integer}, or null
     */
    @Nullable
    public static Integer strictAsInt(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        return value instanceof Integer ? (Integer) value : null;
    }

    /**
     * Attempts to convert <code>value</code> to a {@link Long}.
     *
     * <p>Returns null if <code>value</code> is null.</p>
     *
     * <p>This method will attempt to cast <code>value</code> to {@link Long}, or
     * {@link Long#parseLong(String) parse} the <code>value</code> if it is a {@link String}.</p>
     *
     * @param value The value
     * @return <code>value</code> as a {@link Float}, or null
     */
    @Nullable
    public static Long asLong(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Number) value).longValue();
        }

        if (value instanceof Float
                || value instanceof Double) {
            double val = ((Number) value).doubleValue();
            if (val == Math.floor(val)) {
                return (long) val;
            }
        }

        try {
            return Long.parseLong(value.toString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Returns <code>value</code> if it is a {@link Long}.
     *
     * @param value The value
     * @return <code>value</code> as a {@link Long}, or null
     */
    @Nullable
    public static Long strictAsLong(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Number) value).longValue();
        }

        return null;
    }

    /**
     * Attempts to convert <code>value</code> to a {@link Boolean}.
     *
     * <ul>
     *     <li>If <code>value</code> is a {@link Boolean}, casts and returns</li>
     *     <li>If <code>value</code> is a {@link Number}, returns true if value is not 0</li>
     *     <li>If <code>value.toString()</code> returns true, t, yes, y, or 1, returns true</li>
     *     <li>If <code>value.toString()</code> returns false, f, no, n, or 0, returns false</li>
     *     <li>Otherwise returns null</li>
     * </ul>
     *
     * @param value The value
     * @return <code>value</code> as a {@link Boolean}, or null
     */
    @Nullable
    public static Boolean asBoolean(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return !value.equals(0);
        }

        final String potential = value.toString();
        if (potential.equals("true")
                || potential.equals("t")
                || potential.equals("yes")
                || potential.equals("y")
                || potential.equals("1")) {
            return true;
        } else if (potential.equals("false")
                || potential.equals("f")
                || potential.equals("no")
                || potential.equals("n")
                || potential.equals("0")) {
            return false;
        }

        return null;
    }

    /**
     * Returns <code>value</code> if it is a {@link Boolean}.
     *
     * @param value The value
     * @return <code>value</code> as a {@link Boolean}, or null
     */
    @Nullable
    public static Boolean strictAsBoolean(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        return value instanceof Boolean ? (Boolean) value : null;
    }
}

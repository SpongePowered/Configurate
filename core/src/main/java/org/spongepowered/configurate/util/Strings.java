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

import static java.util.Objects.requireNonNull;

/**
 * Extra string utilities.
 */
public final class Strings {

    private Strings() {}

    /**
     * Create a new string with the contents of the provided string repeated
     * {@code times} times.
     *
     * <p>Available in {@link String} itself as of JDK 11.
     *
     * @param content Text to repeat
     * @param times amount to repeat
     * @return repeated string
     */
    public static String repeat(final String content, final int times) {
        requireNonNull(content, "content");

        switch (times) {
            case 0: return "";
            case 1: return content;
            default:
                final StringBuilder ret = new StringBuilder(content.length() * times);
                for (int i = 0; i < times; ++i) {
                    ret.append(content);
                }
                return ret.toString();
        }
    }

}

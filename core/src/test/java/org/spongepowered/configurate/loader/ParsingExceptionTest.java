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

import org.junit.jupiter.api.Test;

class ParsingExceptionTest {

    // column is 1-indexed, so column == context.length() points at the last char.
    // The caret must be rendered there too. Previously the guard was
    // `column < context.length()`, which dropped the caret for the last char (#625).
    @Test
    void caretRenderedForLastColumn() {
        final String context = "hello"; // length 5
        final String message = new ParsingException(1, context.length(), context, "err", null).getMessage();

        // The caret sits under the last character: (length - 1) leading spaces, then '^'.
        final int caretIndex = message.indexOf('^');
        assertTrue(caretIndex >= 0, "a caret should be rendered for the last column");
        int leadingSpaces = 0;
        for (int i = caretIndex - 1; i >= 0 && message.charAt(i) == ' '; i--) {
            leadingSpaces++;
        }
        assertEquals(context.length() - 1, leadingSpaces, "caret should be aligned under the last character");
    }

    @Test
    void caretNotRenderedBeyondContext() {
        final String context = "hello";
        final String message = new ParsingException(1, context.length() + 1, context, "err", null).getMessage();
        assertFalse(message.contains("^"), "caret should not be rendered past the context");
    }

}

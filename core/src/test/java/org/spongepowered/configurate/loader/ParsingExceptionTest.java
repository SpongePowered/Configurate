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

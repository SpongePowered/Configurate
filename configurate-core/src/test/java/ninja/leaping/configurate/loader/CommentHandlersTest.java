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
package ninja.leaping.configurate.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Joiner;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

public class CommentHandlersTest {

    @Test
    public void testExtractBlockCommentHeader() throws IOException {
        final String testDocument = "/*\n" +
                " * First header line\n" +
                " * more header\n" +
                " * even more header\n" +
                " */";

        Optional<String> head = CommentHandlers.SLASH_BLOCK.extractHeader(new BufferedReader(new StringReader(testDocument)));
        assertTrue(head.isPresent());
        assertEquals("First header line\n" +
                "more header\n" +
                "even more header", head.get());

        assertEquals(testDocument, Joiner.on('\n').join(CommentHandlers.SLASH_BLOCK.toComment(AbstractConfigurationLoader
                .LINE_SPLITTER
                .splitToList(head.get()))));

    }

    @Test
    public void testExtractSingleLineBlockComment() throws IOException {
        final String testDocument = "/* single line */\n";
        try (BufferedReader read = new BufferedReader(new StringReader(testDocument))) {
            Optional<String> head = CommentHandlers.SLASH_BLOCK.extractHeader(read);
            assertTrue(head.isPresent());
            assertEquals("single line", head.get());
        }
    }

    @Test
    public void testExtractLineCommentHeader() throws IOException {
        final String testDocument = "# First header line\n" +
                "# more header\n" +
                "# even more header";

        Optional<String> head = CommentHandlers.HASH.extractHeader(new BufferedReader(new StringReader(testDocument)));
        assertTrue(head.isPresent());
        assertEquals("First header line\n" +
                "more header\n" +
                "even more header", head.get());

    }
}

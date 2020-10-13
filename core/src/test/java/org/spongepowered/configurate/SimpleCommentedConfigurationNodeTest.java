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
package org.spongepowered.configurate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class SimpleCommentedConfigurationNodeTest {

    @Test
    void testCommentsTransferred() {
        final CommentedConfigurationNode subject = CommentedConfigurationNode.root();
        final CommentedConfigurationNode firstChild = subject.node("first");
        firstChild.raw("test value")
                .comment("Such comment. Very wow.");


        final CommentedConfigurationNode secondChild = subject.node("second");
        secondChild.raw("test value's evil twin");

        assertFalse(secondChild.virtual());

        secondChild.from(firstChild);
        assertEquals("test value", secondChild.raw());
        assertEquals("Such comment. Very wow.", secondChild.comment());
    }

    @Test
    void testNestedCommentsTransferred() {
        final CommentedConfigurationNode subject = CommentedConfigurationNode.root();
        final CommentedConfigurationNode firstChild = subject.node("first");
        final CommentedConfigurationNode firstChildChild = firstChild.node("child");
        firstChildChild.raw("test value")
                .comment("Such comment. Very wow.");


        final CommentedConfigurationNode secondChild = subject.node("second");
        secondChild.raw("test value's evil twin");

        assertFalse(secondChild.virtual());

        secondChild.from(firstChild);
        assertEquals("test value", secondChild.node("child").raw());
        assertEquals("Such comment. Very wow.", secondChild.node("child").comment());
    }

    @Test
    void testCommentsMerged() {
        final CommentedConfigurationNode source = CommentedConfigurationNode.root();
        final CommentedConfigurationNode target = CommentedConfigurationNode.root();

        source.node("no-value").raw("a").comment("yeah");
        source.node("existing-value-no-comment").raw("orig").comment("maybe");
        source.node("existing-value").raw("a").comment("yeah");
        source.node("no-parent", "child").raw("x").comment("always");
        target.node("existing-value-no-comment").raw("new");
        target.node("existing-value").raw("b").comment("nope");

        target.mergeFrom(source);
        assertEquals("yeah", target.node("no-value").comment());
        assertEquals("maybe", target.node("existing-value-no-comment").comment());
        assertEquals("new", target.node("existing-value-no-comment").getString());
        assertEquals("nope", target.node("existing-value").comment());
        assertEquals("always", target.node("no-parent", "child").comment());
    }

}

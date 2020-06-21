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
package org.spongepowered.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Processor;

import java.util.Locale;
import java.util.ResourceBundle;

public class ProcessorTest {

    // Comments

    static class TestComment {
        @Comment("An important option") String first;
        @Comment("Another important option!") String second;
    }

    @Test
    void testComment() throws ObjectMappingException {
        final ObjectMapper<TestComment> mapper = ObjectMapper.factory().get(TestComment.class);
        final TestComment object = new TestComment();
        object.first = "hello";
        object.second = "world";
        final CommentedConfigurationNode target = CommentedConfigurationNode.root();

        mapper.save(object, target);

        assertEquals("An important option", target.getNode("first").getComment());
        assertEquals("Another important option!", target.getNode("second").getComment());
    }

    static class TestCommentLocalized {
        @Comment("configurate.test.comment.one") int hello = 1;
        @Comment("Missing comment passthrough") int goodbye = 2;
    }

    // Localized comments

    @Test
    void testCommentLocalized() throws ObjectMappingException {
        final ResourceBundle bundle = ResourceBundle.getBundle("org.spongepowered.configurate.objectmapping.messages", new Locale("en", "US"));
        final ObjectMapper<TestCommentLocalized> mapper = ObjectMapper.factoryBuilder()
                .addProcessor(Comment.class, Processor.localizedComments(bundle))
                .build().get(TestCommentLocalized.class);

        final CommentedConfigurationNode target = CommentedConfigurationNode.root();
        mapper.save(new TestCommentLocalized(), target);

        assertEquals("First property", target.getNode("hello").getComment());
        assertEquals("Missing comment passthrough", target.getNode("goodbye").getComment());
    }

}

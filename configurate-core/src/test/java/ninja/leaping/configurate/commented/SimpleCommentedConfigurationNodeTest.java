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
package ninja.leaping.configurate.commented;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class SimpleCommentedConfigurationNodeTest {

    @Test
    public void testCommentsTransferred() {
        CommentedConfigurationNode subject = SimpleCommentedConfigurationNode.root();
        CommentedConfigurationNode firstChild = subject.getNode("first");
        firstChild.setValue("test value");
        firstChild.setComment("Such comment. Very wow.");


        CommentedConfigurationNode secondChild = subject.getNode("second");
        secondChild.setValue("test value's evil twin");

        assertFalse(secondChild.isVirtual());

        secondChild.setValue(firstChild);
        assertEquals("test value", secondChild.getValue());
        assertEquals("Such comment. Very wow.", secondChild.getComment().orElse(null));
    }

    @Test
    public void testNestedCommentsTransferred() {
        CommentedConfigurationNode subject = SimpleCommentedConfigurationNode.root();
        CommentedConfigurationNode firstChild = subject.getNode("first");
        CommentedConfigurationNode firstChildChild = firstChild.getNode("child");
        firstChildChild.setValue("test value");
        firstChildChild.setComment("Such comment. Very wow.");


        CommentedConfigurationNode secondChild = subject.getNode("second");
        secondChild.setValue("test value's evil twin");

        assertFalse(secondChild.isVirtual());

        secondChild.setValue(firstChild);
        assertEquals("test value", secondChild.getNode("child").getValue());
        assertEquals("Such comment. Very wow.", secondChild.getNode("child").getComment().orElse(null));
    }

    @Test
    public void testCommentsMerged() {
        CommentedConfigurationNode source = SimpleCommentedConfigurationNode.root();
        CommentedConfigurationNode target = SimpleCommentedConfigurationNode.root();

        source.getNode("no-value").setValue("a").setComment("yeah");
        source.getNode("existing-value-no-comment").setValue("orig").setComment("maybe");
        source.getNode("existing-value").setValue("a").setComment("yeah");
        source.getNode("no-parent", "child").setValue("x").setComment("always");
        target.getNode("existing-value-no-comment").setValue("new");
        target.getNode("existing-value").setValue("b").setComment("nope");

        target.mergeValuesFrom(source);
        assertEquals("yeah", target.getNode("no-value").getComment().orElse(null));
        assertEquals("maybe", target.getNode("existing-value-no-comment").getComment().orElse(null));
        assertEquals("new", target.getNode("existing-value-no-comment").getString());
        assertEquals("nope", target.getNode("existing-value").getComment().orElse(null));
        assertEquals("always", target.getNode("no-parent", "child").getComment().orElse(null));
    }
}

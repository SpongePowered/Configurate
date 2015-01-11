/**
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

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleCommentedConfigurationNodeTest {
    @Test
    public void testCommentsTransferred() {
        CommentedConfigurationNode subject = SimpleCommentedConfigurationNode.root();
        CommentedConfigurationNode firstChild = subject.getChild("first");
        firstChild.setValue("test value");
        firstChild.setComment("Such comment. Very wow.");

        CommentedConfigurationNode secondChild = subject.getChild("second");
        secondChild.setValue("test value's evil twin");

        assertFalse(secondChild.isVirtual());

        secondChild.setValue(firstChild);
        assertEquals("test value", secondChild.getValue());
        assertEquals("Such comment. Very wow.", secondChild.getComment().orNull());
    }
}

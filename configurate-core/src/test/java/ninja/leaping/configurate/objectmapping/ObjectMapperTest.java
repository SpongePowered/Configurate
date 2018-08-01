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
package ninja.leaping.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ObjectMapperTest {

    @ConfigSerializable
    private static class TestObject {
        @Setting("test-key") protected String stringVal;
    }

    @Test
    public void testCreateFromNode() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.forClass(TestObject.class);
        final ConfigurationNode source = SimpleConfigurationNode.root();
        source.getNode("test-key").setValue("some are born great, some achieve greatness, and some have greatness thrust upon them");

        final TestObject obj = mapper.bindToNew().populate(source);
        assertEquals("some are born great, some achieve greatness, and some have greatness thrust upon them", obj.stringVal);
    }

    @Test
    public void testNullsPreserved() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.forClass(TestObject.class);
        final TestObject obj = mapper.bindToNew().populate(SimpleConfigurationNode.root());
        assertNull(obj.stringVal);
    }

    @Test
    public void testLoadExistingObject() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.forClass(TestObject.class);
        final ConfigurationNode source = SimpleConfigurationNode.root();
        final TestObject instance = new TestObject();

        source.getNode("test-key").setValue("boom");

        mapper.bind(instance).populate(source);
        assertEquals("boom", instance.stringVal);
    }

    @Test
    public void testDefaultsApplied() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.forClass(TestObject.class);
        final ConfigurationNode source = SimpleConfigurationNode.root();
        final TestObject instance = new TestObject();

        instance.stringVal = "hi";
        mapper.bind(instance).populate(source);
        assertEquals("hi", source.getNode("test-key").getString());
    }

    @ConfigSerializable
    private static class CommentedObject {
        @Setting(value = "commented-key", comment = "You look nice today") private String color;
        @Setting("no-comment") private String politician;
    }

    @Test
    public void testCommentsApplied() throws ObjectMappingException {
        CommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();
        ObjectMapper<CommentedObject>.BoundInstance mapper = ObjectMapper.forClass(CommentedObject.class).bindToNew();
        CommentedObject obj = mapper.populate(node);
        obj.color = "fuchsia";
        obj.politician = "All of them";
        mapper.serialize(node);
        assertEquals("You look nice today", node.getNode("commented-key").getComment().orElse(null));
        assertEquals("fuchsia", node.getNode("commented-key").getString());
        assertFalse(node.getNode("no-comment").getComment().isPresent());
    }


    @ConfigSerializable
    private static class NonZeroArgConstructorObject {
        @Setting private long key;
        private final String value;

        protected NonZeroArgConstructorObject(String value) {
            this.value = value;
        }
    }

    @Test
    public void testNoArglessConstructor() throws ObjectMappingException {
        Assertions.assertTrue(Assertions.assertThrows(ObjectMappingException.class, () -> {
            ObjectMapper<NonZeroArgConstructorObject> mapper = ObjectMapper.forClass(NonZeroArgConstructorObject.class);
            assertFalse(mapper.canCreateInstances());
            mapper.bindToNew();
        }).getMessage().startsWith("No zero-arg constructor"));
    }

    @ConfigSerializable
    private static class TestObjectChild extends TestObject {
        @Setting("child-setting") private boolean childSetting;
    }

    @Test
    public void testSuperclassFieldsIncluded() throws ObjectMappingException {
        final ObjectMapper<TestObjectChild> mapper = ObjectMapper.forClass(TestObjectChild.class);
        ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("child-setting").setValue(true);
        node.getNode("test-key").setValue("Parents get populated too!");

        TestObjectChild instance = mapper.bindToNew().populate(node);
        assertEquals(true, instance.childSetting);
        assertEquals("Parents get populated too!", instance.stringVal);
    }

    @ConfigSerializable
    private static class FieldNameObject {
        @Setting private boolean loads;
    }

    @Test
    public void testKeyFromFieldName() throws ObjectMappingException {
        final ObjectMapper<FieldNameObject> mapper = ObjectMapper.forClass(FieldNameObject.class);
        final ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("loads").setValue(true);

        FieldNameObject obj = mapper.bindToNew().populate(node);
        assertTrue(obj.loads);
    }

    private static class ParentObject {
        @Setting(comment = "Comment on parent") private InnerObject inner = new InnerObject();
    }

    @ConfigSerializable
    private static class InnerObject {
        @Setting(comment = "Something") private String test = "Default value";
    }

    @Test
    public void testNestedObjectWithComments() throws ObjectMappingException {
        CommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();
        final ObjectMapper<ParentObject>.BoundInstance mapper = ObjectMapper.forObject(new ParentObject());
        mapper.populate(node);
        assertEquals("Comment on parent", node.getNode("inner").getComment().get());
        assertTrue(node.getNode("inner").hasMapChildren());
        assertEquals("Default value", node.getNode("inner", "test").getString());
        assertEquals("Something", node.getNode("inner", "test").getComment().get());
    }

    @ConfigSerializable
    private interface ParentInterface {
        String getTest();
    }

    private static class ChildObject implements ParentInterface {
        @Setting(comment = "Something") private String test = "Default value";

        @Override public String getTest() {
            return test;
        }
    }

    @ConfigSerializable
    private static class ContainingObject {
        @Setting ParentInterface inner = new ChildObject();
        @Setting List<ParentInterface> list = new ArrayList<>();
    }

    @Test
    public void testInterfaceSerialization() throws ObjectMappingException {
        CommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();

        final ChildObject childObject = new ChildObject();
        childObject.test = "Changed value";

        final ContainingObject containingObject = new ContainingObject();
        containingObject.list.add(childObject);
        containingObject.inner = childObject;

        final ObjectMapper<ContainingObject> mapper = ObjectMapper.forClass(ContainingObject.class);
        mapper.bind(containingObject).serialize(node);

        final ContainingObject newContainingObject = mapper.bindToNew().populate(node);

        // serialization
        assertEquals(1, node.getNode("list").getChildrenList().size());
        assertEquals("Changed value", node.getNode("inner").getNode("test").getString());
        assertEquals("Changed value", node.getNode("list").getChildrenList().get(0).getNode("test").getString());
        assertEquals("Something", node.getNode("inner").getNode("test").getComment().get());
        assertEquals("Something", node.getNode("list").getChildrenList().get(0).getNode("test").getComment().get());
        assertEquals(ChildObject.class.getName(), node.getNode("inner").getNode("__class__").getString());
        assertEquals(ChildObject.class.getName(), node.getNode("list").getChildrenList().get(0).getNode("__class__").getString());

        // deserialization
        assertEquals(1, newContainingObject.list.size());
        assertEquals("Changed value", newContainingObject.inner.getTest());
        assertEquals("Changed value", newContainingObject.list.get(0).getTest());
    }
}

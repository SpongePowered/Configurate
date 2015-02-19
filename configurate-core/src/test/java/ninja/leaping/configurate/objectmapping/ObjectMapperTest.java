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
package ninja.leaping.configurate.objectmapping;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ObjectMapperTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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
        assertEquals("You look nice today", node.getNode("commented-key").getComment().orNull());
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
        ObjectMapper<NonZeroArgConstructorObject> mapper = ObjectMapper.forClass(NonZeroArgConstructorObject.class);
        assertFalse(mapper.canCreateInstances());
        expectedException.expect(ObjectMappingException.class);
        expectedException.expectMessage("No zero-arg constructor");
        mapper.bindToNew();
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

    private static class MultiValueObject {
        @Setting private FieldNameObject child;
    }

    @Test
    public void testGetValueAtKey() throws ObjectMappingException {
        final ObjectMapper<MultiValueObject> mapper = ObjectMapper.forClass(MultiValueObject.class);
        final ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("child", "loads").setValue(true);
        final ObjectMapper<MultiValueObject>.BoundInstance bound = mapper.bindToNew();
        final MultiValueObject instance = bound.populate(node);
        assertEquals(true, instance.child.loads);
        Object loads = bound.getValue("child", "loads");
        assertEquals(true, loads);
        bound.setValue(false, "child", "loads");
        assertEquals(false, instance.child.loads);
    }
}

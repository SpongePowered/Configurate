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

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.*;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectMapperTest {

    @ConfigSerializable
    private static class TestObject {
        @Setting("test-key") protected String stringVal;
    }

    @Test
    public void testCreateFromNode() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.forClass(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root();
        source.getNode("test-key").setValue("some are born great, some achieve greatness, and some have greatness thrust upon them");

        final TestObject obj = mapper.bindToNew().populate(source);
        assertEquals("some are born great, some achieve greatness, and some have greatness thrust upon them", obj.stringVal);
    }

    @Test
    public void testNullsPreserved() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.forClass(TestObject.class);
        final TestObject obj = mapper.bindToNew().populate(BasicConfigurationNode.root());
        assertNull(obj.stringVal);
    }

    @Test
    public void testLoadExistingObject() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.forClass(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root();
        final TestObject instance = new TestObject();

        source.getNode("test-key").setValue("boom");

        mapper.bind(instance).populate(source);
        assertEquals("boom", instance.stringVal);
    }

    @Test
    public void testDefaultsApplied() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.forClass(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root();
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
        CommentedConfigurationNode node = CommentedConfigurationNode.root();
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
        Assertions.assertTrue(assertThrows(ObjectMappingException.class, () -> {
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
        BasicConfigurationNode node = BasicConfigurationNode.root();
        node.getNode("child-setting").setValue(true);
        node.getNode("test-key").setValue("Parents get populated too!");

        TestObjectChild instance = mapper.bindToNew().populate(node);
        assertTrue(instance.childSetting);
        assertEquals("Parents get populated too!", instance.stringVal);
    }

    @ConfigSerializable
    private static class FieldNameObject {
        @Setting private boolean loads;
    }

    @Test
    public void testKeyFromFieldName() throws ObjectMappingException {
        final ObjectMapper<FieldNameObject> mapper = ObjectMapper.forClass(FieldNameObject.class);
        final BasicConfigurationNode node = BasicConfigurationNode.root();
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
        CommentedConfigurationNode node = CommentedConfigurationNode.root();
        final ObjectMapper<ParentObject>.BoundInstance mapper = ObjectMapper.forObject(new ParentObject());
        mapper.populate(node);
        assertEquals("Comment on parent", node.getNode("inner").getComment().get());
        assertTrue(node.getNode("inner").isMap());
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
        CommentedConfigurationNode node = CommentedConfigurationNode.root();

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

    @ConfigSerializable
    static class GenericSerializable<V> {
        @Setting
        public List<V> elements;
    }

    static class ParentTypesResolved extends GenericSerializable<URL> {
        @Setting
        public String test = "hi";
    }

    @Test
    public void testGenericTypesResolved() throws ObjectMappingException {
        final TypeToken<GenericSerializable<String>> stringSerializable = new TypeToken<GenericSerializable<String>>() {};
        final TypeToken<GenericSerializable<Integer>> intSerializable = new TypeToken<GenericSerializable<Integer>>() {};

        ObjectMapper<GenericSerializable<String>> stringMapper = ObjectMapper.forType(stringSerializable);
        ObjectMapper<GenericSerializable<Integer>> intMapper = ObjectMapper.forType(intSerializable);

        BasicConfigurationNode stringNode = BasicConfigurationNode.root(p -> {
            p.getNode("elements").act(n -> {
                n.appendListNode().setValue("hello");
                n.appendListNode().setValue("world");
            });
        });
        BasicConfigurationNode intNode = BasicConfigurationNode.root(p -> {
            p.getNode("elements").act(n -> {
                n.appendListNode().setValue(1);
                n.appendListNode().setValue(1);
                n.appendListNode().setValue(2);
                n.appendListNode().setValue(3);
                n.appendListNode().setValue(5);
                n.appendListNode().setValue(8);
            });
        });

        GenericSerializable<String> stringObject = stringMapper.bindToNew().populate(stringNode);
        assertEquals(ImmutableList.of("hello", "world"), stringObject.elements);

        GenericSerializable<Integer> intObject = intMapper.bindToNew().populate(intNode);
        assertEquals(ImmutableList.of(1, 1, 2, 3, 5, 8), intObject.elements);
    }

    @Test
    public void testGenericsResolvedThroughSuperclass() throws ObjectMappingException, MalformedURLException {
        ObjectMapper<ParentTypesResolved> mapper = ObjectMapper.forClass(ParentTypesResolved.class);

        BasicConfigurationNode urlNode = BasicConfigurationNode.root(p -> {
            p.getNode("elements").act(n -> {
                n.appendListNode().setValue("https://spongepowered.org");
                n.appendListNode().setValue("https://yaml.org");
            });
            p.getNode("test").setValue("bye");
        });

        ParentTypesResolved resolved = mapper.bindToNew().populate(urlNode);
        assertEquals(ImmutableList.of(new URL("https://spongepowered.org"), new URL("https://yaml.org")), resolved.elements);
        assertEquals("bye", resolved.test);

    }

    @Test
    public void testDirectInterfacesProhibited() throws ObjectMappingException {
        assertThrows(ObjectMappingException.class, () -> ObjectMapper.forClass(ParentInterface.class));
    }
}

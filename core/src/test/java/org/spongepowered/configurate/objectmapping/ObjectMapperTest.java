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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.ConfigSerializable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnusedVariable") // test object mapper objects are not always read
public class ObjectMapperTest {

    @ConfigSerializable
    private static class TestObject {
        @Setting("test-key") protected String stringVal;
    }

    @Test
    public void testCreateFromNode() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root();
        source.getNode("test-key").setValue("some are born great, some achieve greatness, and some have greatness thrust upon them");

        final TestObject obj = mapper.load(source);
        assertEquals("some are born great, some achieve greatness, and some have greatness thrust upon them", obj.stringVal);
    }

    @Test
    public void testNullsPreserved() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final TestObject obj = mapper.load(BasicConfigurationNode.root());
        assertNull(obj.stringVal);
    }

    @Test
    public void testLoadExistingObject() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root();
        final TestObject instance = new TestObject();

        source.getNode("test-key").setValue("boom");
        assertTrue(mapper instanceof ObjectMapper.Mutable<?>);

        ((ObjectMapper.Mutable<TestObject>) mapper).load(instance, source);
        assertEquals("boom", instance.stringVal);
    }

    @Test
    public void testDefaultsNotAppiledUnlessCopyDefaults() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root();
        final TestObject instance = new TestObject();
        assertTrue(mapper instanceof ObjectMapper.Mutable<?>);

        instance.stringVal = "hi";
        ((ObjectMapper.Mutable<TestObject>) mapper).load(instance, source);
        assertTrue(source.getNode("test-key").isVirtual());
    }

    @Test
    public void testDefaultsApplied() throws ObjectMappingException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root(ConfigurationOptions.defaults().withShouldCopyDefaults(true));
        final TestObject instance = new TestObject();
        assertTrue(mapper instanceof ObjectMapper.Mutable<?>);

        instance.stringVal = "hi";
        ((ObjectMapper.Mutable<TestObject>) mapper).load(instance, source);
        assertEquals("hi", source.getNode("test-key").getString());
    }

    @ConfigSerializable
    private static class CommentedObject {
        @Setting("commented-key")
        @Comment("You look nice today")
        private String color;
        @Setting("no-comment") private String politician;
    }

    @Test
    public void testCommentsApplied() throws ObjectMappingException {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root();
        final ObjectMapper<CommentedObject> mapper = ObjectMapper.factory().get(CommentedObject.class);
        final CommentedObject obj = mapper.load(node);
        obj.color = "fuchsia";
        obj.politician = "All of them";
        mapper.save(obj, node);
        assertEquals("You look nice today", node.getNode("commented-key").getComment());
        assertEquals("fuchsia", node.getNode("commented-key").getString());
        assertNull(node.getNode("no-comment").getComment());
    }

    @ConfigSerializable
    private static class NonZeroArgConstructorObject {
        @Setting private long key;
        private final String value;

        protected NonZeroArgConstructorObject(final String value) {
            this.value = value;
        }
    }

    @Test
    public void testNoArglessConstructor() throws ObjectMappingException {
        Assertions.assertTrue(assertThrows(ObjectMappingException.class, () -> {
            final ObjectMapper<NonZeroArgConstructorObject> mapper = ObjectMapper.factory().get(NonZeroArgConstructorObject.class);
            assertFalse(mapper.canCreateInstances());
            mapper.load(BasicConfigurationNode.root());
        }).getMessage().startsWith("Unable to create instance"));
    }

    @ConfigSerializable
    private static class TestObjectChild extends TestObject {
        @Setting("child-setting") private boolean childSetting;
    }

    @Test
    public void testSuperclassFieldsIncluded() throws ObjectMappingException {
        final ObjectMapper<TestObjectChild> mapper = ObjectMapper.factory().get(TestObjectChild.class);
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.getNode("child-setting").setValue(true);
        node.getNode("test-key").setValue("Parents get populated too!");

        final TestObjectChild instance = mapper.load(node);
        assertTrue(instance.childSetting);
        assertEquals("Parents get populated too!", instance.stringVal);
    }

    @ConfigSerializable
    private static class FieldNameObject {
        @Setting private boolean loads;
    }

    @Test
    public void testKeyFromFieldName() throws ObjectMappingException {
        final ObjectMapper<FieldNameObject> mapper = ObjectMapper.factory().get(FieldNameObject.class);
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.getNode("loads").setValue(true);

        final FieldNameObject obj = mapper.load(node);
        assertTrue(obj.loads);
    }

    private static class ParentObject {
        @Comment("Comment on parent") private InnerObject inner = new InnerObject();
    }

    @ConfigSerializable
    private static class InnerObject {
        @Comment("Something") private String test = "Default value";
    }

    @Test
    public void testNestedObjectWithComments() throws ObjectMappingException {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root(ConfigurationOptions.defaults().withShouldCopyDefaults(true));
        final ObjectMapper<ParentObject> mapper = ObjectMapper.factory().get(ParentObject.class);
        mapper.load(node);
        assertEquals("Comment on parent", node.getNode("inner").getComment());
        assertTrue(node.getNode("inner").isMap());
        assertEquals("Default value", node.getNode("inner", "test").getString());
        assertEquals("Something", node.getNode("inner", "test").getComment());
    }

    @ConfigSerializable
    private interface ParentInterface {
        String getTest();
    }

    private static class ChildObject implements ParentInterface {
        @Comment("Something") private String test = "Default value";

        @Override public String getTest() {
            return this.test;
        }
    }

    @ConfigSerializable
    private static class ContainingObject {
        @Setting ParentInterface inner = new ChildObject();
        @Setting List<ParentInterface> list = new ArrayList<>();
    }

    @Test
    public void testInterfaceSerialization() throws ObjectMappingException {

        final ChildObject childObject = new ChildObject();
        childObject.test = "Changed value";

        final ContainingObject containingObject = new ContainingObject();
        containingObject.list.add(childObject);
        containingObject.inner = childObject;

        final CommentedConfigurationNode node = CommentedConfigurationNode.root();
        final ObjectMapper<ContainingObject> mapper = ObjectMapper.factory().get(ContainingObject.class);
        mapper.save(containingObject, node);

        final ContainingObject newContainingObject = mapper.load(node);

        // serialization
        assertEquals(1, node.getNode("list").getChildrenList().size());
        assertEquals("Changed value", node.getNode("inner").getNode("test").getString());
        assertEquals("Changed value", node.getNode("list").getChildrenList().get(0).getNode("test").getString());
        assertEquals("Something", node.getNode("inner").getNode("test").getComment());
        assertEquals("Something", node.getNode("list").getChildrenList().get(0).getNode("test").getComment());
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

        final ObjectMapper<GenericSerializable<String>> stringMapper = ObjectMapper.factory().get(stringSerializable);
        final ObjectMapper<GenericSerializable<Integer>> intMapper = ObjectMapper.factory().get(intSerializable);

        final BasicConfigurationNode stringNode = BasicConfigurationNode.root(p -> {
            p.getNode("elements").act(n -> {
                n.appendListNode().setValue("hello");
                n.appendListNode().setValue("world");
            });
        });
        final BasicConfigurationNode intNode = BasicConfigurationNode.root(p -> {
            p.getNode("elements").act(n -> {
                n.appendListNode().setValue(1);
                n.appendListNode().setValue(1);
                n.appendListNode().setValue(2);
                n.appendListNode().setValue(3);
                n.appendListNode().setValue(5);
                n.appendListNode().setValue(8);
            });
        });

        final GenericSerializable<String> stringObject = stringMapper.load(stringNode);
        assertEquals(Arrays.asList("hello", "world"), stringObject.elements);

        final GenericSerializable<Integer> intObject = intMapper.load(intNode);
        assertEquals(Arrays.asList(1, 1, 2, 3, 5, 8), intObject.elements);
    }

    @Test
    public void testGenericsResolvedThroughSuperclass() throws ObjectMappingException, MalformedURLException {
        final ObjectMapper<ParentTypesResolved> mapper = ObjectMapper.factory().get(ParentTypesResolved.class);

        final BasicConfigurationNode urlNode = BasicConfigurationNode.root(p -> {
            p.getNode("elements").act(n -> {
                n.appendListNode().setValue("https://spongepowered.org");
                n.appendListNode().setValue("https://yaml.org");
            });
            p.getNode("test").setValue("bye");
        });

        final ParentTypesResolved resolved = mapper.load(urlNode);
        assertEquals(Arrays.asList(new URL("https://spongepowered.org"), new URL("https://yaml.org")), resolved.elements);
        assertEquals("bye", resolved.test);

    }

    @Test
    public void testDirectInterfacesProhibited() {
        assertThrows(ObjectMappingException.class, () -> ObjectMapper.factory().get(ParentInterface.class));
    }

}

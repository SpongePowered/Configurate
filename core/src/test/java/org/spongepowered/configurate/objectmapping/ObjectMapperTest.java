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

import com.google.errorprone.annotations.Keep;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.UpperCase;
import org.spongepowered.configurate.serialize.UppercaseStringTypeSerializer;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnusedVariable") // test object mapper objects are not always read
class ObjectMapperTest {

    @ConfigSerializable
    private static class TestObject {
        @Setting("test-key") protected String stringVal;
    }

    @Test
    void testCreateFromNode() throws SerializationException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root();
        source.node("test-key").set("some are born great, some achieve greatness, and some have greatness thrust upon them");

        final TestObject obj = mapper.load(source);
        assertEquals("some are born great, some achieve greatness, and some have greatness thrust upon them", obj.stringVal);
    }

    @Test
    void testNullsPreserved() throws SerializationException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final TestObject obj = mapper.load(BasicConfigurationNode.root());
        assertNull(obj.stringVal);
    }

    @Test
    void testLoadExistingObject() throws SerializationException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root();
        final TestObject instance = new TestObject();

        source.node("test-key").set("boom");
        assertTrue(mapper instanceof ObjectMapper.Mutable<?>);

        ((ObjectMapper.Mutable<TestObject>) mapper).load(instance, source);
        assertEquals("boom", instance.stringVal);
    }

    @Test
    void testDefaultsNotAppliedUnlessCopyDefaults() throws SerializationException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root(ConfigurationOptions.defaults().shouldCopyDefaults(false));
        final TestObject instance = new TestObject();
        assertTrue(mapper instanceof ObjectMapper.Mutable<?>);

        instance.stringVal = "hi";
        ((ObjectMapper.Mutable<TestObject>) mapper).load(instance, source);
        assertTrue(source.node("test-key").virtual());
    }

    @Test
    void testDefaultsApplied() throws SerializationException {
        final ObjectMapper<TestObject> mapper = ObjectMapper.factory().get(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root(ConfigurationOptions.defaults());
        final TestObject instance = new TestObject();
        assertTrue(mapper instanceof ObjectMapper.Mutable<?>);

        instance.stringVal = "hi";
        ((ObjectMapper.Mutable<TestObject>) mapper).load(instance, source);
        assertEquals("hi", source.node("test-key").getString());
    }

    @ConfigSerializable
    private static final class CommentedObject {
        @Setting("commented-key")
        @Comment("You look nice today")
        private String color;
        @Setting("no-comment") private String politician;
    }

    @Test
    void testCommentsApplied() throws SerializationException {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root();
        final ObjectMapper<CommentedObject> mapper = ObjectMapper.factory().get(CommentedObject.class);
        final CommentedObject obj = mapper.load(node);
        obj.color = "fuchsia";
        obj.politician = "All of them";
        mapper.save(obj, node);
        assertEquals("You look nice today", node.node("commented-key").comment());
        assertEquals("fuchsia", node.node("commented-key").getString());
        assertNull(node.node("no-comment").comment());
    }

    @ConfigSerializable
    private static class NonZeroArgConstructorObject {
        @Setting private long key;
        private final String value;

        @Keep
        protected NonZeroArgConstructorObject(final String value) {
            this.value = value;
        }
    }

    @Test
    void testNoArglessConstructor() {
        Assertions.assertTrue(assertThrows(SerializationException.class, () -> {
            final ObjectMapper<NonZeroArgConstructorObject> mapper = ObjectMapper.factory().get(NonZeroArgConstructorObject.class);
            assertFalse(mapper.canCreateInstances());
            mapper.load(BasicConfigurationNode.root());
        }).rawMessage().startsWith("Objects must have a zero-argument"));
    }

    @ConfigSerializable
    private static final class TestObjectChild extends TestObject {
        @Setting("child-setting") private boolean childSetting;
    }

    @Test
    void testSuperclassFieldsIncluded() throws SerializationException {
        final ObjectMapper<TestObjectChild> mapper = ObjectMapper.factory().get(TestObjectChild.class);
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("child-setting").set(true);
        node.node("test-key").set("Parents get populated too!");

        final TestObjectChild instance = mapper.load(node);
        assertTrue(instance.childSetting);
        assertEquals("Parents get populated too!", instance.stringVal);
    }

    @ConfigSerializable
    private static final class FieldNameObject {
        @Setting private boolean loads;
    }

    @Test
    void testKeyFromFieldName() throws SerializationException {
        final ObjectMapper<FieldNameObject> mapper = ObjectMapper.factory().get(FieldNameObject.class);
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("loads").set(true);

        final FieldNameObject obj = mapper.load(node);
        assertTrue(obj.loads);
    }

    private static final class ParentObject {
        @Comment("Comment on parent") private InnerObject inner = new InnerObject();
    }

    @ConfigSerializable
    private static final class InnerObject {
        @Comment("Something") private String test = "Default value";
    }

    @Test
    void testNestedObjectWithComments() throws SerializationException {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root(ConfigurationOptions.defaults().shouldCopyDefaults(true));
        final ObjectMapper<ParentObject> mapper = ObjectMapper.factory().get(ParentObject.class);
        mapper.load(node);
        assertEquals("Comment on parent", node.node("inner").comment());
        assertTrue(node.node("inner").isMap());
        assertEquals("Default value", node.node("inner", "test").getString());
        assertEquals("Something", node.node("inner", "test").comment());
    }

    @ConfigSerializable
    @SuppressWarnings("PMD.ImplicitFunctionalInterface")
    private interface ParentInterface {
        String test();
    }

    private static final class ChildObject implements ParentInterface {
        @Comment("Something") private String test = "Default value";

        @Override public String test() {
            return this.test;
        }
    }

    @ConfigSerializable
    private static final class ContainingObject {
        @Setting ParentInterface inner = new ChildObject();
        @Setting List<ParentInterface> list = new ArrayList<>();
    }

    @Test
    void testInterfaceSerialization() throws SerializationException {

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
        assertEquals(1, node.node("list").childrenList().size());
        assertEquals("Changed value", node.node("inner").node("test").getString());
        assertEquals("Changed value", node.node("list").childrenList().get(0).node("test").getString());
        assertEquals("Something", node.node("inner").node("test").comment());
        assertEquals("Something", node.node("list").childrenList().get(0).node("test").comment());
        assertEquals(ChildObject.class.getName(), node.node("inner").node("__class__").getString());
        assertEquals(ChildObject.class.getName(), node.node("list").childrenList().get(0).node("__class__").getString());

        // deserialization
        assertEquals(1, newContainingObject.list.size());
        assertEquals("Changed value", newContainingObject.inner.test());
        assertEquals("Changed value", newContainingObject.list.get(0).test());
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
    void testGenericTypesResolved() throws SerializationException {
        final TypeToken<GenericSerializable<String>> stringSerializable = new TypeToken<GenericSerializable<String>>() {};
        final TypeToken<GenericSerializable<Integer>> intSerializable = new TypeToken<GenericSerializable<Integer>>() {};

        final ObjectMapper<GenericSerializable<String>> stringMapper = ObjectMapper.factory().get(stringSerializable);
        final ObjectMapper<GenericSerializable<Integer>> intMapper = ObjectMapper.factory().get(intSerializable);

        final BasicConfigurationNode stringNode = BasicConfigurationNode.root(p -> {
            p.node("elements").act(n -> {
                n.appendListNode().raw("hello");
                n.appendListNode().raw("world");
            });
        });
        final BasicConfigurationNode intNode = BasicConfigurationNode.root(p -> {
            p.node("elements").act(n -> {
                n.appendListNode().raw(1);
                n.appendListNode().raw(1);
                n.appendListNode().raw(2);
                n.appendListNode().raw(3);
                n.appendListNode().raw(5);
                n.appendListNode().raw(8);
            });
        });

        final GenericSerializable<String> stringObject = stringMapper.load(stringNode);
        assertEquals(Arrays.asList("hello", "world"), stringObject.elements);

        final GenericSerializable<Integer> intObject = intMapper.load(intNode);
        assertEquals(Arrays.asList(1, 1, 2, 3, 5, 8), intObject.elements);
    }

    @Test
    void testGenericsResolvedThroughSuperclass() throws SerializationException, MalformedURLException {
        final ObjectMapper<ParentTypesResolved> mapper = ObjectMapper.factory().get(ParentTypesResolved.class);

        final BasicConfigurationNode urlNode = BasicConfigurationNode.root(p -> {
            p.node("elements").act(n -> {
                n.appendListNode().raw("https://spongepowered.org");
                n.appendListNode().raw("https://yaml.org");
            });
            p.node("test").raw("bye");
        });

        final ParentTypesResolved resolved = mapper.load(urlNode);
        assertEquals(Arrays.asList(new URL("https://spongepowered.org"), new URL("https://yaml.org")), resolved.elements);
        assertEquals("bye", resolved.test);

    }

    @Test
    void testDirectInterfacesProhibited() {
        assertThrows(SerializationException.class, () -> ObjectMapper.factory().get(ParentInterface.class));
    }

    @ConfigSerializable
    static class HandleNonVirtualNulls {
        @Comment("Test")
        private @Nullable String hello;
    }

    @Test
    void testNullNonVirtualNodeHasNoValue() throws SerializationException {
        final CommentedConfigurationNode value = CommentedConfigurationNode.root(n -> {
            n.node("hello").comment("Hi friend!");
        });

        assertFalse(value.node("hello").virtual());
        assertTrue(value.node("hello").isNull());

        final HandleNonVirtualNulls deserialized = value.require(HandleNonVirtualNulls.class);
        assertNull(deserialized.hello);
    }

    @Test
    void testRequiresNewObjectCreation() throws SerializationException {
        // We register two object field discoverers, one that only matches constructors
        // with an int param, one which matches constructors with a boolean param.
        // Then we ensure that two test instances can be constructed

        final ObjectMapper.Factory factory = ObjectMapper.factoryBuilder()
            .addDiscoverer(FieldDiscoverer.instantiableObject(type -> {
                try {
                    final Constructor<?> constructor;
                    constructor = GenericTypeReflector.erase(type.getType()).getDeclaredConstructor(int.class);
                    constructor.setAccessible(true);
                    return () -> {
                        try {
                            return constructor.newInstance(42);
                        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                } catch (final NoSuchMethodException e) {
                    return null;
                }
            }))
            .addDiscoverer(FieldDiscoverer.instantiableObject(type -> {
                try {
                    final Constructor<?> constructor;
                    constructor = GenericTypeReflector.erase(type.getType()).getDeclaredConstructor(boolean.class);
                    constructor.setAccessible(true);
                    return () -> {
                        try {
                            return constructor.newInstance(true);
                        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                } catch (final NoSuchMethodException e) {
                    return null;
                }
            }))
            .build();

        assertTrue(factory.get(ConstructorTestA.class).canCreateInstances());
        assertTrue(factory.get(ConstructorTestB.class).canCreateInstances());

    }

    @ConfigSerializable
    static class ConstructorTestA {

        private final transient int one;

        ConstructorTestA(final int one) {
            this.one = one;
        }

    }

    @ConfigSerializable
    static class ConstructorTestB {

        private final transient boolean two;

        ConstructorTestB(final boolean two) {
            this.two = two;
        }

    }

    @ConfigSerializable
    static class TestAnnotatedTypes {
        @UpperCase String one;
        String two;
        @UpperCase.Field
        String three;
    }

    @Test
    void testAnnotatedTypes() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(
            ConfigurationOptions.defaults()
                .serializers(builder -> builder.registerAnnotated(UppercaseStringTypeSerializer::applicable, UppercaseStringTypeSerializer.INSTANCE))
                .nativeTypes(UnmodifiableCollections.toSet(String.class))
        );

        node.act(n -> {
            n.node("one").set("hello");
            n.node("two").set("world");
            n.node("three").set("three");
        });

        final TestAnnotatedTypes instance = node.require(TestAnnotatedTypes.class);

        assertEquals("HELLO", instance.one);
        assertEquals("world", instance.two);
        assertEquals("THREE", instance.three);
    }

}

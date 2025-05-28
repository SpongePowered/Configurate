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
package org.spongepowered.configurate.serialize;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.util.CheckedBiFunction;
import org.spongepowered.configurate.util.CheckedTriConsumer;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

/**
 * Base class for serializers that represents switch
 * between classes implementing one class or interface.
 *
 * @param <T> class or interface to switch under
 * @since 4.3.0
 */
public abstract class SwitchSerializer<T> implements TypeSerializer<T>{
    private final Class<T> base;
    private final Object switching;
    private final @Nullable Object settings;

    /**
     * Create a new SwitchSerializer that handles provided type.
     *
     * <p>{@code base} must not be a raw parameterized type.</p>
     *
     * @param base      type to switch under.
     * @param switching path to node that provides switching.
     * @param settings  path to node that setting implementing class.
     * @since 4.3.0
     */
    protected SwitchSerializer(final Class<T> base,final Object switching,final Object settings){
        if(switching.equals(settings))throw new IllegalArgumentException(
            "Switching and settings nodes are same"
        );
        if (base.getTypeParameters().length>0)throw new IllegalArgumentException(
            "Provided base type " + base + " has type parameters but was not provided as a TypeToken!"
        );

        this.base=base;
        this.switching=switching;
        this.settings=settings;
    }

    /**
     * Create a new SwitchSerializer that handles provided type
     * where settings node is the root node of object.
     *
     * <p>{@code base} must not be a raw parameterized type.</p>
     * <p>Be sure that switching node hasn't collisions with switch cases</p>
     *
     * @param base      type to switch under.
     * @param switching path to node that provides switching.
     * @since 4.3.0
     */
    protected SwitchSerializer(final Class<T> base,final Object switching){
        if (base.getTypeParameters().length>0)throw new IllegalArgumentException(
            "Provided base type " + base + " has type parameters but was not provided as a TypeToken!"
        );

        this.base=base;
        this.switching=switching;
        this.settings=null;
    }

    /**
     * Create a new SwitchSerializer that handles provided type.
     *
     * @param base      type to switch under.
     * @param switching path to node that provides switching.
     * @param settings  path to node that setting implementing class.
     * @since 4.3.0
     */
    @SuppressWarnings("unchecked")
    protected SwitchSerializer(final TypeToken<T> base,final Object switching,final Object settings){
        if(switching.equals(settings))throw new IllegalArgumentException(
            "Switching and settings nodes are same"
        );

        this.base=(Class<T>)GenericTypeReflector.box(base.getType());
        this.switching=switching;
        this.settings=settings;
    }

    /**
     * Create a new SwitchSerializer that handles provided type
     * where settings node is the root node of object.
     *
     * <p>Be sure that switching node hasn't collisions with switch cases</p>
     *
     * @param base      type to switch under.
     * @param switching path to node that provides switching.
     * @since 4.3.0
     */
    @SuppressWarnings("unchecked")
    protected SwitchSerializer(final TypeToken<T> base,final Object switching){
        this.base=(Class<T>)GenericTypeReflector.box(base.getType());
        this.switching=switching;
        this.settings=null;
    }

    /**
     * Get the base type of this serializer.
     *
     * @return the base type for this serializer.
     * @since 4.3.0
     */
    public final Class<T> base() {
        return this.base;
    }

    /**
     * Get path to switching node
     *
     * @return path to switching node
     * @since 4.3.0
     */
    public final Object switching(){
        return switching;
    }

    /**
     * Get path to settings node
     *
     * @return path to settings node or null if root is settings node
     * @since 4.3.0
     */
    public final @Nullable Object settings(){
        return settings;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void serialize(final Type type, @Nullable final T obj, final ConfigurationNode node) throws SerializationException {
        if(obj==null)return;
        final Class<? extends T> subclass=GenericTypeReflector.erase(obj.getClass()).asSubclass(base);

        if(subclass.equals(getClass()))throw new SerializationException(node,base,
            "Was passed an object which class is the base class of serializer, serialization was stoped to prevent infinite loop"
        );

        final ConfigurationNode switchingNode=node.node(switching);
        final TypeSerializer<T> switchCase=(TypeSerializer<T>)serialize(subclass,switchingNode);
        if(switchingNode.virtual())throw new SerializationException(node,base,
            "SwitchSerializer didn't define switching node"
        );

        final ConfigurationNode settingsNode=settings==null?node:node.node(settings);
        switchCase.serialize(type,obj,settingsNode);
    }
    @Override
    public final void serialize(final AnnotatedType type,@Nullable final T obj,final ConfigurationNode node) throws SerializationException {
        serialize(type.getType(),obj,node);
    }

    @Override
    public final T deserialize(final Type type,final ConfigurationNode node) throws SerializationException{
        final ConfigurationNode switchingNode=node.node(switching);
        if(switchingNode.virtual())throw new SerializationException(node,type,"Switching node isn't defined");

        final TypeSerializer<? extends T> switchCase=deserialize(switchingNode);

        final ConfigurationNode settingsNode=settings==null?node:node.node(settings);
        return switchCase.deserialize(type,settingsNode);
    }
    @Override
    public final T deserialize(final AnnotatedType type,final ConfigurationNode node) throws SerializationException {
        return deserialize(type.getType(),node);
    }

    /**
     * Specify switching node and compute {@link TypeSerializer}
     * for specified subclass.
     *
     * @param subclass class of object serializer currently store
     * @param switchingNode node for perform switching between subclasses
     * @return {@link TypeSerializer} for current case
     * @throws SerializationException if can't find case
     * @since 4.3.0
     */
    protected abstract TypeSerializer<? extends T> serialize(final Class<? extends T> subclass,final ConfigurationNode switchingNode)
        throws SerializationException;
    /**
     * Try to compute {@link TypeSerializer} from switching node.
     *
     * @param switchingNode node for perform switching between subclasses
     * @return {@link TypeSerializer} for current case
     * @throws SerializationException if can't find case
     * @since 4.3.0
     */
    protected abstract TypeSerializer<? extends T> deserialize(final ConfigurationNode switchingNode)
        throws SerializationException;

    /**
     * Represent base builder for switch serializers
     * that uses simple switching type.
     *
     * @param <V> simple switching type
     * @param <T> base class to switch under
     * @param <S> serializer type that builder builds
     * @param <B> builder class
     * @since 4.3.0
     */
    public static abstract class BaseBuilder<V,T,S extends SwitchSerializer<T>,B extends BaseBuilder<V,T,S,B>>{
        protected final Class<V> switchingClass;
        protected final Class<T> baseClass;
        protected final Object switchingNode;
        protected final @Nullable Object settingsNode;

        protected BaseBuilder(
            final Class<V> switchingClass,
            final Class<T> baseClass,
            final Object switchingNode,
            final Object settingsNode
        ){
            if(switchingNode.equals(settingsNode))throw new IllegalArgumentException(
                "Switching and settings nodes are same"
            );

            this.switchingClass=switchingClass;
            this.baseClass=baseClass;
            this.switchingNode=switchingNode;
            this.settingsNode=settingsNode;
        }
        protected BaseBuilder(
            final Class<V> switchingClass,
            final Class<T> baseClass,
            final Object switchingNode
        ){
            this.switchingClass=switchingClass;
            this.baseClass=baseClass;
            this.switchingNode=switchingNode;
            this.settingsNode=null;
        }

        /**
         * Define a new switch case using {@link TypeSerializer}.
         *
         * <p>{@code subclass} must not be a raw parameterized type.</p>
         *
         * @param switching switching object
         * @param subclass class that represent switch case
         * @param serializer serializer to compute object settings
         * @return this
         * @since 4.3.0
         */
        @SuppressWarnings("unchecked")
        public B define(final V switching, final Class<? extends T> subclass, final TypeSerializer<? extends T> serializer){
            if(subclass.getTypeParameters().length>0)throw new IllegalArgumentException(
                "Provided class "+ subclass.getName()+" has type parameters but was not provided as a TypeToken!"
            );
            if(subclass.equals(baseClass))throw new IllegalArgumentException(
                "Provided class is the base class"
            );

            storeDefinition(switching,subclass,serializer);

            return (B)this;
        }

        /**
         * Define a new switch case using {@link TypeSerializer}.
         *
         * @param switching switching object
         * @param subtoken token type that represent switch case
         * @param serializer serializer to compute object settings
         * @return this
         * @since 4.3.0
         */
        @SuppressWarnings("unchecked")
        public B define(final V switching, final TypeToken<? extends T> subtoken, final TypeSerializer<? extends T> serializer){
            final Class<? extends T> subclass=(Class<? extends T>)GenericTypeReflector.box(subtoken.getType());
            if(subclass.equals(baseClass))throw new IllegalArgumentException(
                "Provided class is the base class"
            );

            storeDefinition(switching,subclass,serializer);

            return (B)this;
        }

        /**
         * Define a new enum switch case using inline {@link TypeSerializer}.
         *
         * <p>{@code subclass} must not be a raw parameterized type.</p>
         *
         * @param switching switching object
         * @param subclass class that represent switch case
         * @param deserializer deserializer part of {@link TypeSerializer}
         * @param serializer serializer part of {@link TypeSerializer}
         * @return this
         * @since 4.3.0
         */
        @SuppressWarnings("unchecked")
        public <N extends T> B define(
            final V switching,
            final Class<N> subclass,
            final CheckedBiFunction<Type,ConfigurationNode,N,SerializationException> deserializer,
            final CheckedTriConsumer<Type,@Nullable N,ConfigurationNode,SerializationException> serializer
        ){
            if (subclass.getTypeParameters().length>0)throw new IllegalArgumentException(
                "Provided class "+ subclass.getName()+" has type parameters but was not provided as a TypeToken!"
            );
            if(subclass.equals(baseClass))throw new IllegalArgumentException(
                "Provided class is the base class"
            );

            storeDefinition(switching,subclass,buildSerializer(deserializer,serializer));

            return (B)this;
        }

        /**
         * Define a new enum switch case using inline {@link TypeSerializer}.
         *
         * @param switching switching object
         * @param subtoken type token that represent switch case
         * @param deserializer deserializer part of {@link TypeSerializer}
         * @param serializer serializer part of {@link TypeSerializer}
         * @return this
         * @since 4.3.0
         */
        @SuppressWarnings("unchecked")
        public <N extends T> B define(
            final V switching,
            final TypeToken<N> subtoken,
            final CheckedBiFunction<Type,ConfigurationNode,N,SerializationException> deserializer,
            final CheckedTriConsumer<Type,@Nullable N,ConfigurationNode,SerializationException> serializer
        ){
            final Class<? extends T> subclass=(Class<? extends T>)GenericTypeReflector.box(subtoken.getType());
            if(subclass.equals(baseClass))throw new IllegalArgumentException(
                "Provided class is the base class"
            );

            storeDefinition(switching,subclass,buildSerializer(deserializer,serializer));

            return (B)this;
        }

        protected final <N extends T> TypeSerializer<N> buildSerializer(
            final CheckedBiFunction<Type,ConfigurationNode,N,SerializationException> deserializer,
            final CheckedTriConsumer<Type,@Nullable N,ConfigurationNode,SerializationException> serializer
        ){
            return new TypeSerializer<N>(){
                @Override
                public N deserialize(final Type type,final ConfigurationNode node) throws SerializationException {
                    return deserializer.accept(type,node);
                }
                @Override
                public void serialize(final Type type,@Nullable final N obj,final ConfigurationNode node) throws SerializationException{
                    serializer.consume(type,obj,node);
                }
            };
        }

        /**
         * Performs cases store and duplication checks.
         *
         * @param switching switching object
         * @param subclass switched class
         * @param serializer serializer
         */
        protected abstract void storeDefinition(final V switching,final Class<? extends T> subclass,final TypeSerializer<? extends T> serializer);

        /**
         * Finish building.
         *
         * @return serializer
         */
        public abstract S build();
    }
}

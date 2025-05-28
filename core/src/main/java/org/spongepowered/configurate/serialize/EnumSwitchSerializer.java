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
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.util.*;

/**
 * Represent serializer switch uses enums.
 *
 * @param <E> switching enum
 * @param <T> class or interface to switch under
 * @since 4.3.0
 */
public final class EnumSwitchSerializer<E extends Enum<E>,T> extends SwitchSerializer<T>{
    private final Class<E> enumClass;
    private final Map<E,TypeSerializer<? extends T>> map;
    private final Map<Class<? extends T>,E> returnMap;

    private EnumSwitchSerializer(
        final Class<E> enumClass,
        final Class<T> baseClass,
        final Object switchingNode,
        final Object settingsNode,
        final Map<E,TypeSerializer<? extends T>> map,
        final Map<Class<? extends T>,E> returnMap
    ){
        super(TypeToken.get(baseClass),switchingNode,settingsNode);
        this.enumClass=enumClass;
        this.map=map;
        this.returnMap=returnMap;
    }
    private EnumSwitchSerializer(
        final Class<E> enumClass,
        final Class<T> baseClass,
        final Object switchingNode,
        final Map<E,TypeSerializer<? extends T>> map,
        final Map<Class<? extends T>,E> returnMap
    ){
        super(TypeToken.get(baseClass),switchingNode);
        this.enumClass=enumClass;
        this.map=map;
        this.returnMap=returnMap;
    }

    @Override
    protected TypeSerializer<? extends T> serialize(final Class<? extends T> subclass,final ConfigurationNode switchingNode) throws SerializationException {
        E val=returnMap.get(subclass);
        if(val==null)throw new SerializationException(switchingNode,base(),"Passed object type hasn't definition in enum switch: "+subclass.getName());

        switchingNode.set(enumClass,val);
        return map.get(val);
    }

    @Override
    protected TypeSerializer<? extends T> deserialize(final ConfigurationNode switchingNode) throws SerializationException {
        E val=switchingNode.require(enumClass);
        return map.get(val);
    }

    /**
     * Create a new EnumSwitchSerializer builder.
     *
     * <p>{@code baseClass} must not be a raw parameterized type.</p>
     *
     * @param enumClass class of switching enum
     * @param baseClass class to switch under
     * @param switchingNode path to node where enum will store
     * @param settingsNode path to node where object data will store
     * @return builder
     * @since 4.3.0
     */
    public static <E extends Enum<E>,T> Builder<E,T> builder(
        final Class<E> enumClass,
        final Class<T> baseClass,
        final Object switchingNode,
        final Object settingsNode
    ){
        if (baseClass.getTypeParameters().length>0)throw new IllegalArgumentException(
            "Provided class "+ baseClass.getName()+" has type parameters but was not provided as a TypeToken!"
        );

        if(enumClass.equals(baseClass))throw new IllegalArgumentException(
            "Switching enum and object class are same"
        );
        if(enumClass.isAssignableFrom(baseClass))throw new IllegalArgumentException(
            "Switching enum is subclass of object class"
        );
        if(switchingNode.equals(settingsNode))throw new IllegalArgumentException(
            "Switching and settings nodes are same"
        );

        return new Builder<>(enumClass, baseClass,switchingNode,settingsNode);
    }

    /**
     * Create a new EnumSwitchSerializer builder
     * where settings node is root.
     *
     * <p>{@code baseClass} must not be a raw parameterized type.</p>
     * <p>Be sure that switching node hasn't collisions with switch cases</p>
     *
     * @param enumClass class of switching enum
     * @param baseClass class to switch under
     * @param switchingNode path to node where enum will store
     * @return builder
     * @since 4.3.0
     */
    public static <E extends Enum<E>,T> Builder<E,T> builder(
        final Class<E> enumClass,
        final Class<T> baseClass,
        final Object switchingNode
    ){
        if (baseClass.getTypeParameters().length>0)throw new IllegalArgumentException(
            "Provided class "+ baseClass.getName()+" has type parameters but was not provided as a TypeToken!"
        );

        if(enumClass.equals(baseClass))throw new IllegalArgumentException(
            "Switching enum and object class are same"
        );
        if(enumClass.isAssignableFrom(baseClass))throw new IllegalArgumentException(
            "Switching enum is subclass of object class"
        );

        return new Builder<>(enumClass, baseClass,switchingNode);
    }

    /**
     * Create a new EnumSwitchSerializer builder.
     *
     *
     * @param enumClass class of switching enum
     * @param baseToken type token of class to switch under
     * @param switchingNode path to node where enum will store
     * @param settingsNode path to node where object data will store
     * @return builder
     * @since 4.3.0
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>,T> Builder<E,T> builder(
        final Class<E> enumClass,
        final TypeToken<T> baseToken,
        final Object switchingNode,
        final Object settingsNode
    ){
        final Class<T> baseClass =(Class<T>)GenericTypeReflector.box(baseToken.getType());

        if(enumClass.equals(baseClass))throw new IllegalArgumentException(
            "Switching enum and object class are same"
        );
        if(enumClass.isAssignableFrom(baseClass))throw new IllegalArgumentException(
            "Switching enum is subclass of object class"
        );
        if(switchingNode.equals(settingsNode))throw new IllegalArgumentException(
            "Switching and settings nodes are same"
        );

        return new Builder<>(enumClass, baseClass,switchingNode,settingsNode);
    }

    /**
     * Create a new EnumSwitchSerializer builder.
     *
     * <p>Be sure that switching node hasn't collisions with switch cases</p>
     *
     * @param enumClass class of switching enum
     * @param baseToken type token of class to switch under
     * @param switchingNode path to node where enum will store
     * @return builder
     * @since 4.3.0
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>,T> Builder<E,T> builder(
        final Class<E> enumClass,
        final TypeToken<T> baseToken,
        final Object switchingNode
    ){
        final Class<T> baseClass =(Class<T>)GenericTypeReflector.box(baseToken.getType());

        if(enumClass.equals(baseClass))throw new IllegalArgumentException(
            "Switching enum and object class are same"
        );
        if(enumClass.isAssignableFrom(baseClass))throw new IllegalArgumentException(
            "Switching enum is subclass of object class"
        );

        return new Builder<>(enumClass,baseClass,switchingNode);
    }

    /**
     * EnumSwitchSerializer builder.
     *
     * @since 4.3.0
     */
    public static final class Builder<E extends Enum<E>,T>
        extends SwitchSerializer.BaseBuilder<E,T,EnumSwitchSerializer<E,T>,Builder<E,T>>{
        private final Map<E,TypeSerializer<? extends T>> toSerializer;
        private final Map<Class<? extends T>,E> toEnum=new HashMap<>();

        private Builder(
            final Class<E> enumClass,
            final Class<T> baseClass,
            final Object switchingNode,
            final Object settingsNode
        ){
            super(enumClass,baseClass,switchingNode,settingsNode);
            toSerializer=new EnumMap<>(enumClass);
        }

        private Builder(
            final Class<E> enumClass,
            final Class<T> baseClass,
            final Object switchingNode
        ){
            super(enumClass,baseClass,switchingNode);
            toSerializer=new EnumMap<>(enumClass);
        }

        @Override
        protected void storeDefinition(final E switching, final Class<? extends T> subclass, final TypeSerializer<? extends T> serializer) {
            if(toSerializer.containsKey(switching))throw new IllegalArgumentException(
                "Provided enum already defined in other scope: "+switching.name()
            );
            if(toEnum.containsKey(subclass))throw new IllegalArgumentException(
                "Provided class already defined in other scope: "+subclass.getName()
            );

            toSerializer.put(switching,serializer);
            toEnum.put(subclass,switching);
        }

        @Override
        public EnumSwitchSerializer<E, T> build() {
            return settingsNode==null?
                new EnumSwitchSerializer<>(
                    switchingClass,
                    baseClass,
                    switchingNode,
                    UnmodifiableCollections.copyOf(toSerializer),
                    UnmodifiableCollections.copyOf(toEnum)
                ):new EnumSwitchSerializer<>(
                    switchingClass,
                    baseClass,
                    switchingNode,
                    settingsNode,
                    UnmodifiableCollections.copyOf(toSerializer),
                    UnmodifiableCollections.copyOf(toEnum)
                );
        }
    }
}

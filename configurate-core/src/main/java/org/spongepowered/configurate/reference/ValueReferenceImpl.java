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
package org.spongepowered.configurate.reference;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.configurate.reference.ConfigurationReference.ErrorPhase;
import org.spongepowered.configurate.transformation.NodePath;
import org.spongepowered.configurate.reactive.Disposable;
import org.spongepowered.configurate.reactive.Processor;
import org.spongepowered.configurate.reactive.Subscriber;

import java.io.IOException;

class ValueReferenceImpl<@Nullable T, N extends ScopedConfigurationNode<N>> implements ValueReference<T, N>, Subscriber<T> {
    // Information about the reference
    private final ManualConfigurationReference<N> root;
    private final NodePath path;
    private final TypeToken<T> type;
    private final TypeSerializer<T> serializer;
    private final Processor<N, T> updateListener;

    // State
    private @Nullable T deserializedValue;

    ValueReferenceImpl(ManualConfigurationReference<N> root, NodePath path, TypeToken<T> type) throws ObjectMappingException {
        this.root = root;
        this.path = path;
        this.type = type;
        serializer = root.getNode().getOptions().getSerializers().get(type);
        if (serializer == null) {
            throw new ObjectMappingException("Unsupported type" + type);
        }

        updateListener = root.updateListener.map(n -> {
            N node = n.getNode(path);
            try {
                return serializer.deserialize(type, node);
            } catch (ObjectMappingException e) {
                root.errorListener.submit(Maps.immutableEntry(ErrorPhase.LOADING, e));
                return null;
            }
        });
        updateListener.subscribe(this);
    }

    ValueReferenceImpl(ManualConfigurationReference<N> root, NodePath path, Class<T> type) throws ObjectMappingException {
        this(root, path, TypeToken.of(type));
    }

    @Override
    public @Nullable T get() {
        return deserializedValue;
    }

    @Override
    public boolean set(@Nullable T value) {
        try {
            serializer.serialize(type, value, getNode());
            updateListener.inject(value);
            return true;
        } catch (ObjectMappingException e) {
            root.errorListener.submit(Maps.immutableEntry(ErrorPhase.SAVING, e));
            return false;
        }
    }

    @Override
    public boolean setAndSave(@Nullable T value) {
        try {
            if (set(value)) {
                root.save();
                return true;
            }
        } catch (IOException e) {
            root.errorListener.submit(Maps.immutableEntry(ErrorPhase.SAVING, e));
        }
        return false;
    }

    @Override
    public N getNode() {
        return root.getNode().getNode(path);
    }

    @Override
    public void submit(T item) {
        deserializedValue = item;
    }

    @Override
    public void onError(Throwable e) {
        root.errorListener.submit(Maps.immutableEntry(ErrorPhase.UNKNOWN, e));
    }

    @Override
    public Disposable subscribe(final Subscriber<? super T> subscriber) {
        return updateListener.subscribe(subscriber);
    }

    @Override
    public boolean hasSubscribers() {
        return updateListener.hasSubscribers();
    }
}

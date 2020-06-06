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

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.configurate.reactive.Disposable;
import org.spongepowered.configurate.reactive.Publisher;
import org.spongepowered.configurate.reactive.Subscriber;
import org.spongepowered.configurate.reactive.TransactionFailedException;
import org.spongepowered.configurate.reference.ConfigurationReference.ErrorPhase;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.transformation.NodePath;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.function.Function;

class ValueReferenceImpl<@Nullable T, N extends ScopedConfigurationNode<N>> implements ValueReference<T, N>, Publisher<T> {

    // Information about the reference
    private final ManualConfigurationReference<N> root;
    private final NodePath path;
    private final TypeToken<T> type;
    private final TypeSerializer<T> serializer;
    private final Publisher.Cached<T> deserialized;

    ValueReferenceImpl(final ManualConfigurationReference<N> root, final NodePath path, final TypeToken<T> type,
                       final @Nullable T def) throws ObjectMappingException {
        this.root = root;
        this.path = path;
        this.type = type;
        this.serializer = root.getNode().getOptions().getSerializers().get(type);
        if (this.serializer == null) {
            throw new ObjectMappingException("Unsupported type" + type);
        }

        this.deserialized = root.updateListener.map(n -> {
            try {
                return deserializedValueFrom(n, def);
            } catch (final ObjectMappingException e) {
                root.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ErrorPhase.VALUE, e));
                throw new TransactionFailedException(e);
            }
        }).cache(deserializedValueFrom(root.getNode(), def));
    }

    ValueReferenceImpl(final ManualConfigurationReference<N> root, final NodePath path, final Class<T> type,
                       final @Nullable T def) throws ObjectMappingException {
        this(root, path, TypeToken.of(type), def);
    }

    private @Nullable T deserializedValueFrom(final N parent, final @Nullable T defaultVal) throws ObjectMappingException {
        final N node = parent.getNode(this.path);
        final @Nullable T possible = this.serializer.deserialize(this.type, node);
        if (possible != null) {
            return possible;
        } else if (defaultVal != null && node.getOptions().shouldCopyDefaults()) {
            this.serializer.serialize(this.type, defaultVal, node);
        }
        return defaultVal;
    }

    @Override
    public @Nullable T get() {
        return this.deserialized.get();
    }

    @Override
    public boolean set(final @Nullable T value) {
        try {
            this.serializer.serialize(this.type, value, getNode());
            this.deserialized.submit(value);
            return true;
        } catch (final ObjectMappingException e) {
            this.root.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ErrorPhase.SAVING, e));
            return false;
        }
    }

    @Override
    public boolean setAndSave(final @Nullable T value) {
        try {
            if (set(value)) {
                this.root.save();
                return true;
            }
        } catch (final IOException e) {
            this.root.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ErrorPhase.SAVING, e));
        }
        return false;
    }

    @Override
    public Publisher<Boolean> setAndSaveAsync(final @Nullable T value) {
        return Publisher.execute(() -> {
            this.serializer.serialize(this.type, value, getNode());
            this.deserialized.submit(value);
            this.root.save();
            return true;
        }, this.root.updates().getExecutor());
    }

    @Override
    public boolean update(final Function<@Nullable T, ? extends T> action) {
        try {
            return set(action.apply(get()));
        } catch (final Exception t) {
            this.root.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ErrorPhase.VALUE, t));
            return false;
        }
    }

    @Override
    public Publisher<Boolean> updateAsync(final Function<T, ? extends T> action) {
        return Publisher.execute(() -> {
            final @Nullable T orig = get();
            final T updated = action.apply(orig);
            this.serializer.serialize(this.type, updated, getNode());
            this.deserialized.submit(updated);
            this.root.save();
            return true;
        }, this.root.updates().getExecutor());
    }

    @Override
    public N getNode() {
        return this.root.getNode().getNode(this.path);
    }

    @Override
    public Disposable subscribe(final Subscriber<? super T> subscriber) {
        return this.deserialized.subscribe(subscriber);
    }

    @Override
    public boolean hasSubscribers() {
        return this.deserialized.hasSubscribers();
    }

    @Override
    public Executor getExecutor() {
        return this.deserialized.getExecutor();
    }

}

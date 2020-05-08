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
package ninja.leaping.configurate.reference;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.reactive.Disposable;
import ninja.leaping.configurate.reactive.Publisher;
import ninja.leaping.configurate.reactive.Subscriber;
import ninja.leaping.configurate.reactive.TransactionFailedException;
import ninja.leaping.configurate.reference.ConfigurationReference.ErrorPhase;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.function.Function;

class ValueReferenceImpl<@Nullable T> implements ValueReference<T>, Publisher<T> {
    // Information about the reference
    private final ManualConfigurationReference<?> root;
    private final Object[] path;
    private final TypeToken<T> type;
    private final TypeSerializer<T> serializer;
    private final Publisher.Cached<T> deserialized;

    ValueReferenceImpl(ManualConfigurationReference<?> root, Object[] path, TypeToken<T> type,
                       @Nullable T def) throws ObjectMappingException {
        this.root = root;
        this.path = Arrays.copyOf(path, path.length);
        this.type = type;
        serializer = root.getNode().getOptions().getSerializers().get(type);
        if (serializer == null) {
            throw new ObjectMappingException("Unsupported type" + type);
        }

        deserialized = root.updateListener.map(n -> {
            try {
                return deserializedValueFrom(n, def);
            } catch (ObjectMappingException e) {
                root.errorListener.submit(Maps.immutableEntry(ErrorPhase.VALUE, e));
                throw new TransactionFailedException(e);
            }
        }).cache(deserializedValueFrom(root.getNode(), def));
    }

    ValueReferenceImpl(ManualConfigurationReference<?> root, Object[] path, Class<T> type,
                       @Nullable T def) throws ObjectMappingException {
        this(root, path, TypeToken.of(type), def);
    }

    private @Nullable T deserializedValueFrom(ConfigurationNode parent, @Nullable T defaultVal) throws ObjectMappingException {
        ConfigurationNode node = parent.getNode(path);
        @Nullable T possible = serializer.deserialize(type, node);
        if (possible != null) {
            return possible;
        } else if (defaultVal != null && node.getOptions().shouldCopyDefaults()) {
            serializer.serialize(type, defaultVal, node);
        }
        return defaultVal;
    }

    @Override
    public @Nullable T get() {
        return deserialized.get();
    }

    @Override
    public boolean set(@Nullable T value) {
        try {
            serializer.serialize(type, value, getNode());
            deserialized.submit(value);
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
    public Publisher<Boolean> setAndSaveAsync(@Nullable T value) {
        return Publisher.execute(() -> {
            serializer.serialize(type, value, getNode());
            deserialized.submit(value);
            root.save();
            return true;
        }, root.updates().getExecutor());
    }

    @Override
    public boolean update(Function<@Nullable T, ? extends T> action) {
        try {
            return set(action.apply(get()));
        } catch (Throwable t) {
            root.errorListener.submit(Maps.immutableEntry(ErrorPhase.VALUE, t));
            return false;
        }
    }

    @Override
    public Publisher<Boolean> updateAsync(Function<T, ? extends T> action) {
        return Publisher.execute(() -> {
            @Nullable T orig = get();
            T updated = action.apply(orig);
            serializer.serialize(type, updated, getNode());
            deserialized.submit(updated);
            root.save();
            return true;
        }, root.updates().getExecutor());
    }

    @Override
    public ConfigurationNode getNode() {
        return root.getNode().getNode(path);
    }

    @Override
    public Disposable subscribe(final Subscriber<? super T> subscriber) {
        return deserialized.subscribe(subscriber);
    }

    @Override
    public boolean hasSubscribers() {
        return deserialized.hasSubscribers();
    }

    @Override
    public Executor getExecutor() {
        return this.deserialized.getExecutor();
    }
}

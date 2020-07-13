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
package org.spongepowered.configurate.extra.dfu.v2;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Implementation of DataFixerUpper's DynamicOps.
 *
 * <p>When possible, the first node's {@link ConfigurationNode#copy()}  method
 * will be used to create a new node to contain results. Otherwise, the provided
 * factory will be used. The default factory creates a
 * {@link CommentedConfigurationNode} with the {@link TypeSerializerCollection#defaults() default TypeSerializer collection},
 * but a custom factory may be provided.
 *
 */
public final class ConfigurateOps implements DynamicOps<ConfigurationNode> {

    private static final ConfigurateOps INSTANCE = new ConfigurateOps(CommentedConfigurationNode::root);

    private final Supplier<? extends ConfigurationNode> factory;

    /**
     * Get the shared instance of this class, which creates new nodes using
     * the default factory.
     *
     * @return The shared instance
     */
    public static DynamicOps<ConfigurationNode> getInstance() {
        return INSTANCE;
    }

    /**
     * Create a new instance of the ops, with a custom node factory.
     *
     * @param factory The factory function
     * @return A new ops instance
     */
    public static DynamicOps<ConfigurationNode> getWithNodeFactory(final Supplier<? extends ConfigurationNode> factory) {
        return new ConfigurateOps(factory);
    }

    /**
     * Wrap a ConfigurationNode in a {@link Dynamic} instance. The returned Dynamic will use the same type
     * serializer collection as the original node for its operations.
     *
     * @param node The node to wrap
     * @return a wrapped node
     */
    public static Dynamic<ConfigurationNode> wrap(final ConfigurationNode node) {
        if (node.getOptions().getSerializers().equals(TypeSerializerCollection.defaults())) {
            return new Dynamic<>(getInstance(), node);
        } else {
            final ConfigurationOptions opts = node.getOptions();
            return new Dynamic<>(getWithNodeFactory(() -> CommentedConfigurationNode.root(opts)), node);
        }
    }

    ConfigurateOps(final Supplier<? extends ConfigurationNode> factory) {
        this.factory = factory;
    }

    private static String unwrapKey(final ConfigurationNode node) {
        return requireNonNull(node.getString(), "Kep nodes must have a value!");
    }

    @Override
    public ConfigurationNode empty() {
        return this.factory.get();
    }

    @Override
    public Type<?> getType(final ConfigurationNode input) {
        if (input == null) {
            throw new NullPointerException("input is null");
        }

        if (input.isMap()) {
            return DSL.compoundList(DSL.remainderType(), DSL.remainderType());

        } else if (input.isList()) {
            return DSL.list(DSL.remainderType());
        } else {
            final @Nullable Object value = input.getValue();
            if (value == null) {
                return DSL.nilType();
            } else if (value instanceof String) {
                return DSL.string();
            } else if (value instanceof Boolean) {
                return DSL.bool();
            } else if (value instanceof Short) {
                return DSL.shortType();
            } else if (value instanceof Integer) {
                return DSL.intType();
            } else if (value instanceof Long) {
                return DSL.longType();
            } else if (value instanceof Float) {
                return DSL.floatType();
            } else if (value instanceof Double) {
                return DSL.doubleType();
            } else if (value instanceof Byte) {
                return DSL.byteType();
            } else {
                throw new IllegalArgumentException("Scalar value '" + input + "' has an unknown type: " + value.getClass().getName());
            }
        }
    }

    @Override
    public Optional<Number> getNumberValue(final ConfigurationNode input) {
        if (!(input.isMap() || input.isList())) {
            if (input.getValue() instanceof Number) {
                return Optional.of((Number) input.getValue());
            } else if (input.getValue() instanceof Boolean) {
                return Optional.of(input.getBoolean() ? 1 : 0);
            }
        }

        return Optional.empty();
    }

    @Override
    public ConfigurationNode createNumeric(final Number i) {
        return empty().setValue(i);
    }

    @Override
    public ConfigurationNode createBoolean(final boolean value) {
        return empty().setValue(value);
    }

    @Override
    public Optional<String> getStringValue(final ConfigurationNode input) {
        return Optional.ofNullable(input.getString());
    }

    @Override
    public ConfigurationNode createString(final String value) {
        return empty().setValue(value);
    }

    @Override
    public ConfigurationNode mergeInto(final ConfigurationNode input, final ConfigurationNode value) {
        if (input.isList()) {
            final ConfigurationNode ret = input.copy();
            ret.appendListNode().setValue(value);
            return ret;
        }
        return input;
    }

    @Override
    public ConfigurationNode mergeInto(final ConfigurationNode input, final ConfigurationNode key, final ConfigurationNode value) {
        return input.copy().getNode(unwrapKey(key)).setValue(value);
    }

    /**
     * Merge into a newly created node.
     *
     * @param first The primary node
     * @param second The second node, with values that will override those in
     *               the first node
     * @return A newly created node
     */
    @Override
    public ConfigurationNode merge(final ConfigurationNode first, final ConfigurationNode second) {
        return first.copy().mergeValuesFrom(second);

    }

    @Override
    public Optional<Map<ConfigurationNode, ConfigurationNode>> getMapValues(final ConfigurationNode input) {
        if (input.isMap()) {
            final ImmutableMap.Builder<ConfigurationNode, ConfigurationNode> builder = ImmutableMap.builder();
            for (final Map.Entry<Object, ? extends ConfigurationNode> entry : input.getChildrenMap().entrySet()) {
                builder.put(empty().setValue(entry.getKey()), entry.getValue().copy());
            }
            return Optional.of(builder.build());
        }

        return Optional.empty();
    }

    @Override
    public ConfigurationNode createMap(final Map<ConfigurationNode, ConfigurationNode> map) {
        final ConfigurationNode ret = empty();

        for (final Map.Entry<ConfigurationNode, ConfigurationNode> entry : map.entrySet()) {
            ret.getNode(unwrapKey(entry.getKey())).setValue(entry.getValue());
        }

        return ret;
    }

    @Override
    public Optional<Stream<ConfigurationNode>> getStream(final ConfigurationNode input) {
        if (input.isList()) {
            final Stream<ConfigurationNode> stream = input.getChildrenList().stream().map(it -> it);
            return Optional.of(stream);
        }

        return Optional.empty();
    }

    @Override
    public ConfigurationNode createList(final Stream<ConfigurationNode> input) {
        final ConfigurationNode ret = empty();
        input.forEach(it -> ret.appendListNode().setValue(it));
        return ret;
    }

    @Override
    public ConfigurationNode remove(final ConfigurationNode input, final String key) {
        if (input.isMap()) {
            final ConfigurationNode ret = input.copy();
            ret.getNode(key).setValue(null);
            return ret;
        }

        return input;
    }

    @Override
    public Optional<ConfigurationNode> get(final ConfigurationNode input, final String key) {
        final ConfigurationNode ret = input.getNode(key);
        return ret.isVirtual() ? Optional.empty() : Optional.of(ret);
    }

    @Override
    public Optional<ConfigurationNode> getGeneric(final ConfigurationNode input, final ConfigurationNode key) {
        final ConfigurationNode ret = input.getNode(unwrapKey(key));
        return ret.isVirtual() ? Optional.empty() : Optional.of(ret);
    }

    @Override
    public ConfigurationNode set(final ConfigurationNode input, final String key, final ConfigurationNode value) {
        final ConfigurationNode ret = input.copy();
        ret.getNode(key).setValue(value);
        return ret;
    }

    @Override
    public ConfigurationNode update(final ConfigurationNode input, final String key, final Function<ConfigurationNode, ConfigurationNode> function) {
        if (input.getNode(key).isVirtual()) {
            return input;
        }

        final ConfigurationNode ret = input.copy();

        final ConfigurationNode child = ret.getNode(key);
        child.setValue(function.apply(child));
        return ret;
    }

    @Override
    public ConfigurationNode updateGeneric(final ConfigurationNode input, final ConfigurationNode wrappedKey,
                                           final Function<ConfigurationNode, ConfigurationNode> function) {
        final Object key = unwrapKey(wrappedKey);
        if (input.getNode(key).isVirtual()) {
            return input;
        }

        final ConfigurationNode ret = input.copy();

        final ConfigurationNode child = ret.getNode(key);
        child.setValue(function.apply(child));
        return ret;
    }

    @Override
    public String toString() {
        return "Configurate";
    }

}

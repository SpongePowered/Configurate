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
package org.spongepowered.configurate.objectmapping.meta;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * A function to resolve nodes for a specific field.
 *
 * <p>A {@link Factory} is responsible for creating node resolvers for each
 * field in an object, and provides the context necessary for a resolver to
 * determine which node to navigate to.</p>
 *
 * @since 4.0.0
 */
@FunctionalInterface
public interface NodeResolver {

    /**
     * Indicates that a field should be explicitly skipped.
     *
     * @since 4.0.0
     */
    NodeResolver SKIP_FIELD = parent -> null;

    /**
     * Given a parent node, resolve an appropriate child.
     *
     * <p>The {@code parent} node is the node that the mapped object is being
     * deserialized from.</p>
     *
     * @param parent parent node
     * @return child node, or null if the node should not be deserialized.
     * @since 4.0.0
     */
    @Nullable ConfigurationNode resolve(ConfigurationNode parent);

    /**
     * Provides fields.
     *
     * @since 4.0.0
     */
    @FunctionalInterface
    interface Factory {

        /**
         * Create a function that resolves a child node from its parent.
         *
         * @param name field name
         * @param element annotations on the field
         * @return {@code null} to continue, {@link #SKIP_FIELD} to stop further
         *     processing and exclude this field from serialization, or a
         *     resolver for a node.
         * @since 4.0.0
         */
        @Nullable NodeResolver make(String name, AnnotatedElement element);
    }

    /**
     * Creates resolvers that provide the key of the containing node for values.
     *
     * @return key-based resolver
     * @since 4.0.0
     */
    static NodeResolver.Factory nodeKey() {
        return (name, element) -> {
            if (element.isAnnotationPresent(NodeKey.class)) {
                return node -> BasicConfigurationNode.root(node.options()).raw(node.key());
            }
            return null;
        };
    }

    /**
     * Creates resolvers that get the node at a key defined by {@link Setting}.
     *
     * @return a factory that will extract keys from a provided annotation
     * @since 4.0.0
     */
    static NodeResolver.Factory keyFromSetting() {
        return (name, element) -> {
            if (element.isAnnotationPresent(Setting.class)) {
                final String key = element.getAnnotation(Setting.class).value();
                if (!key.isEmpty()) {
                    return node -> node.node(key);
                }
            }
            return null;
        };
    }

    /**
     * A resolver that skips any field not annotated with {@code annotation}.
     *
     * @param annotation annotation to require
     * @return a new resolver
     * @since 4.0.0
     */
    static NodeResolver.Factory onlyWithAnnotation(final Class<? extends Annotation> annotation) {
        return (name, element) -> {
            if (!element.isAnnotationPresent(annotation)) {
                return NodeResolver.SKIP_FIELD;
            }
            return null;
        };
    }

    /**
     * A resolver that will skip any field not annotated with {@link Setting}.
     *
     * @return new resolver restricting fields
     * @since 4.0.0
     */
    static NodeResolver.Factory onlyWithSetting() {
        return onlyWithAnnotation(Setting.class);
    }

    /**
     * A resolver that uses the containing node of a field.
     *
     * <p>This can be used to combine multiple Java objects into one
     * configuration node.</p>
     *
     * @return new resolver using containing field value
     * @since 4.0.0
     */
    static NodeResolver.Factory nodeFromParent() {
        return (name, element) -> {
            final @Nullable Setting setting = element.getAnnotation(Setting.class);
            if (setting != null && setting.nodeFromParent()) {
                return node -> node;
            }
            return null;
        };
    }

}

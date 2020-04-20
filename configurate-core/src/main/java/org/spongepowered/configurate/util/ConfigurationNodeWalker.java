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
package org.spongepowered.configurate.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ScopedConfigurationNode;
import org.spongepowered.configurate.transformation.NodePath;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiConsumer;

/**
 * Represents a method for "walking" or traversing a {@link ConfigurationNode configuration}
 * structure.
 */
public abstract class ConfigurationNodeWalker {

    /**
     * A {@link ConfigurationNodeWalker} that implements a breadth-first traversal over
     * the configuration.
     *
     * <p>See <a href="https://en.wikipedia.org/wiki/Breadth-first_search">here</a> for more
     * info.
     */
    public static final ConfigurationNodeWalker BREADTH_FIRST = new ConfigurationNodeWalker() {
        @Override
        public <T extends ScopedConfigurationNode<T>> Iterator<VisitedNode<T>> walkWithPath(T start) {
            return new BreadthFirstIterator<>(start);
        }
    };

    /**
     * A {@link ConfigurationNodeWalker} that implements a depth-first pre-order traversal over
     * the configuration.
     *
     * <p>See <a href="https://en.wikipedia.org/wiki/Depth-first_search">here</a> for more
     * info.
     */
    public static final ConfigurationNodeWalker DEPTH_FIRST_PRE_ORDER = new ConfigurationNodeWalker() {
        @Override
        public <T extends @NonNull ScopedConfigurationNode<T>> Iterator<@NonNull VisitedNode<T>> walkWithPath(T start) {
            return new DepthFirstPreOrderIterator<>(start);
        }
    };

    /**
     * A {@link ConfigurationNodeWalker} that implements a depth-first post-order traversal over
     * the configuration.
     *
     * <p>See <a href="https://en.wikipedia.org/wiki/Depth-first_search">here</a> for more
     * info.
     */
    public static final ConfigurationNodeWalker DEPTH_FIRST_POST_ORDER = new ConfigurationNodeWalker() {
        @Override
        public <T extends @NonNull ScopedConfigurationNode<T>> Iterator<@NonNull VisitedNode<T>> walkWithPath(T start) {
            return new DepthFirstPostOrderIterator<>(start);
        }
    };

    /**
     * Returns an iterator which will iterate over all paths and nodes in the
     * configuration, in the order defined by the walker.
     *
     * @param start The node to start at
     * @param <T> The node type
     * @return An iterator of {@link VisitedNode}s
     */
    public abstract <T extends @NonNull ScopedConfigurationNode<T>> Iterator<@NonNull VisitedNode<T>> walkWithPath(T start);


    /**
     * Returns an iterator which will iterate over all nodes in the
     * configuration, in the order defined by the walker.
     *
     * @param start The node to start at
     * @param <T> The node type
     * @return An iterator of {@link ConfigurationNode}s
     */
    public <T extends ScopedConfigurationNode<T>> Iterator<T> walk(T start) {
        return Iterators.transform(walkWithPath(start), VisitedNode::getNode);
    }

    /**
     * Walks the configuration, and calls the {@code consumer} for each path and node
     * visited, in the order defined by the walker.
     *
     * @param start The node to start at
     * @param consumer The consumer to accept the visited nodes
     * @param <T> The node type
     */
    public <T extends ScopedConfigurationNode<T>> void walk(T start, BiConsumer<? super NodePath, ? super T> consumer) {
        Iterator<VisitedNode<T>> it = walkWithPath(start);
        while (it.hasNext()) {
            VisitedNode<T> next = it.next();
            consumer.accept(next.getPath(), next.getNode());
        }
    }

    /**
     * Encapsulates a given {@link ConfigurationNode node} visited during a
     * traversal.
     *
     * @param <T> The node type
     */
    public interface VisitedNode<T extends ConfigurationNode> {

        /**
         * Gets the node that was visited.
         *
         * @return The visited node
         */
        T getNode();

        /**
         * Gets the path of the node that was visited.
         *
         * <p>Equivalent to calling {@link ConfigurationNode#getPath()} - except
         * this method is likely to be more more efficient.</p>
         *
         * @return The path of the visited node
         */
        NodePath getPath();

    }


    private static <T extends ScopedConfigurationNode<T>> Iterator<VisitedNodeImpl<T>> getChildren(VisitedNodeImpl<T> from) {
        T node = from.getNode();
        switch (node.getValueType()) {
            case LIST: {
                return Iterators.transform(node.getChildrenList().iterator(), child -> {
                    Objects.requireNonNull(child);

                    return new VisitedNodeImpl<>(from.getPath().withAppendedChild(child.getKey()), child);
                });
            }
            case MAP: {
                return Iterators.transform(node.getChildrenMap().entrySet().iterator(), child -> {
                    Objects.requireNonNull(child);

                    return new VisitedNodeImpl<>(from.getPath().withAppendedChild(child.getKey()), child.getValue());
                });
            }
            default:
                return Collections.emptyIterator();
        }
    }

    private static final class BreadthFirstIterator<N extends ScopedConfigurationNode<N>> implements Iterator<VisitedNode<N>> {
        private final Queue<VisitedNodeImpl<N>> queue = new ArrayDeque<>();

        BreadthFirstIterator(N root) {
            this.queue.add(new VisitedNodeImpl<>(root.getPath(), root));
        }

        @Override
        public boolean hasNext() {
            return !this.queue.isEmpty();
        }

        @Override
        public VisitedNode<N> next() {
            VisitedNodeImpl<N> current = this.queue.remove();
            Iterators.addAll(this.queue, getChildren(current));
            return current;
        }
    }

    private static final class DepthFirstPreOrderIterator<N extends ScopedConfigurationNode<N>> implements Iterator<VisitedNode<N>> {
        private final Deque<Iterator<VisitedNodeImpl<N>>> stack = new ArrayDeque<>();

        DepthFirstPreOrderIterator(N root) {
            this.stack.push(Iterators.singletonIterator(new VisitedNodeImpl<>(root.getPath(), root)));
        }

        @Override
        public boolean hasNext() {
            return !this.stack.isEmpty();
        }

        @Override
        public VisitedNode<N> next() {
            Iterator<VisitedNodeImpl<N>> iterator = this.stack.getLast();
            VisitedNodeImpl<N> result = iterator.next();
            if (!iterator.hasNext()) {
                this.stack.removeLast();
            }
            Iterator<VisitedNodeImpl<N>> childIterator = getChildren(result);
            if (childIterator.hasNext()) {
                this.stack.addLast(childIterator);
            }
            return result;
        }
    }

    private static final class DepthFirstPostOrderIterator<N extends ScopedConfigurationNode<N>> extends AbstractIterator<VisitedNode<N>> {
        private final ArrayDeque<NodeAndChildren> stack = new ArrayDeque<>();

        DepthFirstPostOrderIterator(N root) {
            this.stack.addLast(new NodeAndChildren(null, Iterators.singletonIterator(new VisitedNodeImpl<>(root.getPath(), root))));
        }

        @Override
        protected VisitedNode<N> computeNext() {
            while (!this.stack.isEmpty()) {
                NodeAndChildren tail = this.stack.getLast();
                if (tail.children.hasNext()) {
                    VisitedNodeImpl<N> child = tail.children.next();
                    this.stack.addLast(new NodeAndChildren(child, getChildren(child)));
                } else {
                    this.stack.removeLast();
                    if (tail.node != null) {
                        return tail.node;
                    }
                }
            }
            return endOfData();
        }

        private final class NodeAndChildren {
            final VisitedNodeImpl<N> node;
            final Iterator<VisitedNodeImpl<N>> children;

            NodeAndChildren(VisitedNodeImpl<N> node, Iterator<VisitedNodeImpl<N>> children) {
                this.node = node;
                this.children = children;
            }
        }
    }

    private static final class VisitedNodeImpl<N extends ScopedConfigurationNode<N>> implements VisitedNode<N> {
        private final NodePath path;
        private final N node;

        VisitedNodeImpl(NodePath path, N node) {
            this.path = path;
            this.node = node;
        }

        public N getNode() {
            return this.node;
        }

        @Override
        public NodePath getPath() {
            return path;
        }
    }

}

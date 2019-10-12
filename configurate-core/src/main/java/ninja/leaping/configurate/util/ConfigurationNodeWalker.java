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
package ninja.leaping.configurate.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.transformation.NodePath;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayDeque;
import java.util.Arrays;
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
        @NonNull
        @Override
        public <T extends ConfigurationNode> Iterator<VisitedNode<T>> walkWithPath(@NonNull T start) {
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
        @NonNull
        @Override
        public <T extends ConfigurationNode> Iterator<VisitedNode<T>> walkWithPath(@NonNull T start) {
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
        @NonNull
        @Override
        public <T extends ConfigurationNode> Iterator<VisitedNode<T>> walkWithPath(@NonNull T start) {
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
    @NonNull
    public abstract <T extends ConfigurationNode> Iterator<VisitedNode<T>> walkWithPath(@NonNull T start);


    /**
     * Returns an iterator which will iterate over all nodes in the
     * configuration, in the order defined by the walker.
     *
     * @param start The node to start at
     * @param <T> The node type
     * @return An iterator of {@link ConfigurationNode}s
     */
    @NonNull
    public <T extends ConfigurationNode> Iterator<T> walk(@NonNull T start) {
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
    public <T extends ConfigurationNode> void walk(@NonNull T start, @NonNull BiConsumer<? super NodePath, ? super T> consumer) {
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
        @NonNull
        T getNode();

        /**
         * Gets the path of the node that was visited.
         *
         * <p>Equivalent to calling {@link ConfigurationNode#getPath()} - except
         * this method is likely to be more more efficient.</p>
         *
         * @return The path of the visited node
         */
        @NonNull
        NodePath getPath();

    }

    private static Object[] calculatePath(Object[] path, Object childKey) {
        if (path.length == 1 && path[0] == null) {
            return new Object[]{childKey};
        }

        Object[] childPath = Arrays.copyOf(path, path.length + 1);
        childPath[childPath.length - 1] = childKey;

        return childPath;
    }

    private static <T extends ConfigurationNode> Iterator<VisitedNodeImpl<T>> getChildren(VisitedNodeImpl<T> from) {
        T node = from.getNode();
        switch (node.getValueType()) {
            case LIST: {
                Object[] path = from.getRawPath();
                return Iterators.transform(node.getChildrenList().iterator(), child -> {
                    Objects.requireNonNull(child);

                    @SuppressWarnings("unchecked")
                    T castedChild = ((T) child);
                    Object[] childPath = calculatePath(path, child.getKey());

                    return new VisitedNodeImpl<>(childPath, castedChild);
                });
            }
            case MAP: {
                Object[] path = from.getRawPath();
                return Iterators.transform(node.getChildrenMap().entrySet().iterator(), child -> {
                    Objects.requireNonNull(child);

                    @SuppressWarnings("unchecked")
                    T castedChild = ((T) child.getValue());
                    Object[] childPath = calculatePath(path, child.getKey());

                    return new VisitedNodeImpl<>(childPath, castedChild);
                });
            }
            default:
                return Collections.emptyIterator();
        }
    }

    private static final class BreadthFirstIterator<N extends ConfigurationNode> implements Iterator<VisitedNode<N>> {
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

    private static final class DepthFirstPreOrderIterator<N extends ConfigurationNode> implements Iterator<VisitedNode<N>> {
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

    private static final class DepthFirstPostOrderIterator<N extends ConfigurationNode> extends AbstractIterator<VisitedNode<N>> {
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

    private static final class VisitedNodeImpl<T extends ConfigurationNode> implements VisitedNode<T>, NodePath {
        private final Object[] path;
        private final T node;

        VisitedNodeImpl(Object[] path, T node) {
            this.path = path;
            this.node = node;
        }

        Object[] getRawPath() {
            return this.path;
        }

        // implement VisitedNode

        @NonNull
        public T getNode() {
            return this.node;
        }

        @NonNull
        @Override
        public NodePath getPath() {
            return this;
        }

        // implement NodePath

        @Override
        public Object get(int i) {
            return this.path[i];
        }

        @Override
        public int size() {
            return this.path.length;
        }

        @Override
        public Object[] getArray() {
            return Arrays.copyOf(this.path, this.path.length);
        }

        @NonNull
        @Override
        public Iterator<Object> iterator() {
            return Iterators.forArray(this.path);
        }
    }

}

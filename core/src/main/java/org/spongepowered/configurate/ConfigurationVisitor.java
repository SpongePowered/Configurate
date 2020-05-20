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
package org.spongepowered.configurate;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.LinkedList;

/**
 * A visitor to traverse node hierarchies in a depth-first order
 * <p>
 * Instances of stateful implementations may be reusable by taking advantage of the state object. During each
 * visitation, a visitor will experience the node tree as a sequence of events, described by the following
 * pseudo-grammar:
 *
 * <pre>
 * mappingNode: enterMappingNode node* exitMappingNode
 * listNode: enterListNode node* exitListNode
 *
 * node: enterNode
 *      (mappingNode
 *       | listNode
 *       | enterScalarNode)
 *
 * visit: newState?
 *        beginVisit
 *        node*
 *        endVisit
 * </pre>
 * <p>
 * If the starting node has no value, no node events will be received. Otherwise, the first event received will be for
 * the starting node itself, and will continue from there.
 * <p>
 * The children to visit for list and mapping nodes will only be collected after both the {@code enterNode} and {@code
 * enter(List|Mapping)Node} methods have been executed for the node, and changes to the node values may be made to
 * control which nodes will be visited.
 * <p>
 * Any exceptions thrown within the visitor will result in the visitation ending immediately and the exception being
 * rethrown within the visit method
 * <p>
 * There are a few specializations of the visitor interface available: {@link Stateless} carries no state and can act as
 * a functional interface type, and {@link Safe} which throws no checked exceptions and therefore can be visited without
 * having to handle any exceptions.
 *
 * @param <N> The type of node to visit
 * @param <S> A state object that will be used for one visit
 * @param <T> The terminal value, that can be returned at the end of the visit
 * @param <E> exception type that may be thrown
 * @see ScopedConfigurationNode#visit(ConfigurationVisitor) to execute this configuration visitation
 */
public interface ConfigurationVisitor<N extends ConfigurationNode, S, T, E extends Exception> {

    /**
     * Begin the visitation using a newly created state object
     *
     * @param node The node to start visiting from
     * @return The terminal value
     * @throws E when thrown by implementation
     */
    default T visit(N node) throws E {
        S state = newState();
        return visit(node, state);
    }

    /**
     * Executes a depth-first visitation of the provided configuration node. It is recommended to directly access a
     * ScopedConfigurationNode where possible
     *
     * @param node  The node to begin from
     * @param state A state object to begin with
     * @return The terminal value
     * @throws E when thrown by implementation
     * @see ScopedConfigurationNode#visit(ConfigurationVisitor) for the recommended visitation method
     */
    @SuppressWarnings("unchecked")
    default T visit(N node, S state) throws E {
        if (node instanceof ScopedConfigurationNode) { // bleh
            return ((ScopedConfigurationNode<? extends N>) node).visit(this, state);
        }

        beginVisit(node, state);
        if (!node.isMap() && !node.isList() && node.isEmpty()) {
            LinkedList<Object> toVisit = new LinkedList<>(); // a list of N | VisitorNodeEnd
            toVisit.add(node);

            @Nullable Object active;
            while ((active = toVisit.pollFirst()) != null) {
                @Nullable N current = (N) VisitorNodeEnd.popFromVisitor(active, this, state);
                if (current == null) {
                    continue;
                }

                enterNode(current, state);
                if (current.isMap()) {
                    enterMappingNode(current, state);
                    toVisit.addFirst(new VisitorNodeEnd(current, true));
                    toVisit.addAll(0, current.getChildrenMap().values());
                } else if (current.isList()) {
                    enterListNode(current, state);
                    toVisit.addFirst(new VisitorNodeEnd(current, false));
                    toVisit.addAll(0, current.getChildrenList());
                } else {
                    enterScalarNode(current, state);
                }
            }
        }
        return endVisit(state);
    }

    /**
     * Called to provide a state object if a visit is initiated without one already existing
     *
     * @return A new state object to be passed through the rest of this visit
     * @throws E when thrown by implementation
     */
    S newState() throws E;

    /**
     * Called at the beginning of the visit with a state object created.
     *
     * @param node  the root node
     * @param state the state
     * @throws E when thrown by implementation
     */
    void beginVisit(N node, S state) throws E;

    /**
     * Called once per node, for every node
     *
     * @param node  The current node
     * @param state provided state
     * @throws E when thrown by implementation
     */
    void enterNode(N node, S state) throws E;

    /**
     * Called after {@link #enterNode(ConfigurationNode, Object)} for mapping nodes
     *
     * @param node  current node
     * @param state provided state
     * @throws E when thrown by implementation
     */
    void enterMappingNode(N node, S state) throws E;

    /**
     * Called after {@link #enterNode(ConfigurationNode, Object)} for list nodes
     *
     * @param node  current node
     * @param state provided state
     * @throws E when thrown by implementation
     */
    void enterListNode(N node, S state) throws E;

    /**
     * Called after {@link #enterNode(ConfigurationNode, Object)} for scalar nodes
     *
     * @param node  current node
     * @param state provided state
     * @throws E when thrown by implementation
     */
    void enterScalarNode(N node, S state) throws E;

    /**
     * Called for a list node after the node and any of its children have been visited
     *
     * @param node  The node that has been visited
     * @param state provided state
     * @throws E when thrown by implementation
     */
    void exitMappingNode(N node, S state) throws E;

    /**
     * Called for a list node after the node and any of its children have been visited
     *
     * @param node  The node that has been visited
     * @param state provided state
     * @throws E when thrown by implementation
     */
    void exitListNode(N node, S state) throws E;

    /**
     * Called after every node has been visited, to allow for cleanup and validation
     *
     * @param state provided state
     * @return a terminal value
     * @throws E when thrown by implementation
     */
    T endVisit(S state) throws E;

    /**
     * Stateless specialization of visitors, where both the state and terminal type are Void
     *
     * @param <N> The node type
     */
    @FunctionalInterface
    interface Stateless<N extends ConfigurationNode, E extends Exception> extends ConfigurationVisitor<N, Void, Void, E> {
        @Override
        default Void newState() {
            return null;
        }

        @Override
        default void beginVisit(N node, Void state) throws E {
            beginVisit(node);
        }

        @Override
        default void enterNode(N node, Void state) throws E {
            enterNode(node);
        }

        @Override
        default void enterMappingNode(N node, Void state) throws E {
            enterMappingNode(node);
        }

        @Override
        default void enterListNode(N node, Void state) throws E {
            enterListNode(node);
        }

        @Override
        default void enterScalarNode(N node, Void state) throws E {
            enterScalarNode(node);
        }

        @Override
        default void exitMappingNode(N node, Void state) throws E {
            exitMappingNode(node);
        }

        @Override
        default void exitListNode(N node, Void state) throws E {
            exitListNode(node);
        }

        @Override
        default Void endVisit(Void state) throws E {
            endVisit();
            return null;
        }

        /**
         * Called at the beginning of the visit with a state object created.
         *
         * @param node The root node
         * @throws E as required by implementation
         */
        default void beginVisit(N node) throws E {
        }

        /**
         * Called once per node, for every node
         *
         * @param node The current node
         * @throws E as required by implementation
         */
        void enterNode(N node) throws E;

        /**
         * Called after {@link #enterNode(ConfigurationNode, Object)} for mapping nodes
         *
         * @param node current node
         * @throws E when thrown by implementation
         */
        default void enterMappingNode(N node) throws E {
        }

        /**
         * Called after {@link #enterNode(ConfigurationNode, Object)} for list nodes
         *
         * @param node current node
         * @throws E when thrown by implementation
         */
        default void enterListNode(N node) throws E {
        }

        /**
         * Called after {@link #enterNode(ConfigurationNode, Object)} for scalar nodes
         *
         * @param node current node
         * @throws E when thrown by implementation
         */
        default void enterScalarNode(N node) throws E {
        }

        /**
         * Called for a mapping node after the node and any of its children have been visited
         *
         * @param node The node that has been visited
         * @throws E when thrown by implementation
         */
        default void exitMappingNode(N node) throws E {
        }

        /**
         * Called for a list node after the node and any of its children have been visited
         *
         * @param node The node that has been visited
         * @throws E when thrown by implementation
         */
        default void exitListNode(N node) throws E {
        }

        /**
         * Called after every node has been visited, to allow for cleanup and validation
         *
         * @throws E when thrown by implementation
         */
        default void endVisit() throws E {
        }
    }

    /**
     * A subinterface for visitors that do not throw any checked exceptions during their execution
     *
     * @param <N> node type
     * @param <S> state type
     * @param <T> terminal value type
     */
    interface Safe<N extends ConfigurationNode, S, T> extends ConfigurationVisitor<N, S, T, VisitorSafeNoopException> {
        @Override
        default T visit(N node) {
            return visit(node, newState());
        }

        @Override
        default T visit(N node, S state) {
            try {
                return ConfigurationVisitor.super.visit(node, state);
            } catch (VisitorSafeNoopException e) {
                throw new Error("Exception designed not to be thrown was thrown :(");
            }
        }

        @Override
        S newState();

        @Override
        void beginVisit(N node, S state);

        @Override
        void enterNode(N node, S state);

        @Override
        void enterMappingNode(N node, S state);

        @Override
        void enterListNode(N node, S state);

        @Override
        void enterScalarNode(N node, S state);

        @Override
        void exitMappingNode(N node, S state);

        @Override
        void exitListNode(N node, S state);

        @Override
        T endVisit(S state);
    }
}

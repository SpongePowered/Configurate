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

/**
 * A visitor to traverse node hierarchies in a depth-first order.
 *
 * <p>Instances of stateful implementations may be reusable by taking advantage
 * of the state object. During each visitation, a visitor will experience the
 * node tree as a sequence of events, described by the following
 * pseudo-grammar:</p>
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
 *
 * <p>If the starting node has no value, no node events will be received.
 * Otherwise, the first event received will be for the starting node itself, and
 * will continue from there.</p>
 *
 * <p>The children to visit for list and mapping nodes will only be collected
 * after both the {@code enterNode} and {@code enter(List|Mapping)Node} methods
 * have been executed for the node, and changes to the node values may be made
 * to control which nodes will be visited.</p>
 *
 * <p>Any exceptions thrown within the visitor will result in the visitation
 * ending immediately and the exception being rethrown within the visit
 * method.</p>
 *
 * <p>There are a few specializations of the visitor interface available:
 * {@link Stateless} carries no state and can act as a functional interface
 * type, and {@link Safe} which throws no checked exceptions and therefore can
 * be visited without having to handle any exceptions.</p>
 *
 * @param <S> a state object that will be used for one visit
 * @param <T> the terminal value, that can be returned at the end of the visit
 * @param <E> exception type that may be thrown
 * @see ScopedConfigurationNode#visit(ConfigurationVisitor) to execute this
 *      configuration visitation
 * @since 4.0.0
 */
public interface ConfigurationVisitor<S, T, E extends Exception> {

    /**
     * Called to provide a state object if a visit is initiated without one
     * already existing.
     *
     * @return a new state object to be passed through the rest of this visit
     * @throws E when thrown by implementation
     * @since 4.0.0
     */
    S newState() throws E;

    /**
     * Called at the beginning of the visit with a state object created.
     *
     * @param node the root node
     * @param state the state
     * @throws E when thrown by implementation
     * @since 4.0.0
     */
    void beginVisit(ConfigurationNode node, S state) throws E;

    /**
     * Called once per node, for every node.
     *
     * @param node the current node
     * @param state provided state
     * @throws E when thrown by implementation
     * @since 4.0.0
     */
    void enterNode(ConfigurationNode node, S state) throws E;

    /**
     * Called after {@link #enterNode(ConfigurationNode, Object)} for mapping
     * nodes.
     *
     * @param node current node
     * @param state provided state
     * @throws E when thrown by implementation
     * @since 4.0.0
     */
    void enterMappingNode(ConfigurationNode node, S state) throws E;

    /**
     * Called after {@link #enterNode(ConfigurationNode, Object)} for list nodes.
     *
     * @param node current node
     * @param state provided state
     * @throws E when thrown by implementation
     * @since 4.0.0
     */
    void enterListNode(ConfigurationNode node, S state) throws E;

    /**
     * Called after {@link #enterNode(ConfigurationNode, Object)} for scalar nodes.
     *
     * @param node current node
     * @param state provided state
     * @throws E when thrown by implementation
     * @since 4.0.0
     */
    void enterScalarNode(ConfigurationNode node, S state) throws E;

    /**
     * Called for a list node after the node and any of its children have
     * been visited.
     *
     * @param node the node that has been visited
     * @param state provided state
     * @throws E when thrown by implementation
     * @since 4.0.0
     */
    void exitMappingNode(ConfigurationNode node, S state) throws E;

    /**
     * Called for a list node after the node and any of its children have
     * been visited.
     *
     * @param node the node that has been visited
     * @param state provided state
     * @throws E when thrown by implementation
     * @since 4.0.0
     */
    void exitListNode(ConfigurationNode node, S state) throws E;

    /**
     * Called after every node has been visited, to allow for cleanup
     * and validation.
     *
     * @param state provided state
     * @return a terminal value
     * @throws E when thrown by implementation
     * @since 4.0.0
     */
    T endVisit(S state) throws E;

    /**
     * Stateless specialization of visitors, where both the state and terminal
     * type are Void.
     *
     * @since 4.0.0
     */
    @FunctionalInterface
    interface Stateless<E extends Exception> extends ConfigurationVisitor<Void, Void, E> {
        @Override
        default Void newState() {
            return null;
        }

        @Override
        default void beginVisit(final ConfigurationNode node, final Void state) throws E {
            beginVisit(node);
        }

        /**
         * Called at the beginning of the visit with a state object created.
         *
         * @param node the root node
         * @throws E as required by implementation
         * @since 4.0.0
         */
        default void beginVisit(final ConfigurationNode node) throws E {}

        @Override
        default void enterNode(final ConfigurationNode node, final Void state) throws E {
            enterNode(node);
        }

        /**
         * Called once per node, for every node.
         *
         * @param node the current node
         * @throws E as required by implementation
         * @since 4.0.0
         */
        void enterNode(ConfigurationNode node) throws E;

        @Override
        default void enterMappingNode(final ConfigurationNode node, final Void state) throws E {
            enterMappingNode(node);
        }

        /**
         * Called after {@link #enterNode(ConfigurationNode, Object)} for
         * mapping nodes.
         *
         * @param node current node
         * @throws E when thrown by implementation
         * @since 4.0.0
         */
        default void enterMappingNode(final ConfigurationNode node) throws E {}

        @Override
        default void enterListNode(final ConfigurationNode node, final Void state) throws E {
            enterListNode(node);
        }

        /**
         * Called after {@link #enterNode(ConfigurationNode, Object)} for list
         * nodes.
         *
         * @param node current node
         * @throws E when thrown by implementation
         * @since 4.0.0
         */
        default void enterListNode(final ConfigurationNode node) throws E {
        }

        @Override
        default void enterScalarNode(final ConfigurationNode node, final Void state) throws E {
            enterScalarNode(node);
        }

        /**
         * Called after {@link #enterNode(ConfigurationNode, Object)} for scalar
         * nodes.
         *
         * @param node current node
         * @throws E when thrown by implementation
         * @since 4.0.0
         */
        default void enterScalarNode(final ConfigurationNode node) throws E {
        }

        @Override
        default void exitMappingNode(final ConfigurationNode node, final Void state) throws E {
            exitMappingNode(node);
        }

        /**
         * Called for a mapping node after the node and any of its children have
         * been visited.
         *
         * @param node the node that has been visited
         * @throws E when thrown by implementation
         * @since 4.0.0
         */
        default void exitMappingNode(final ConfigurationNode node) throws E {}

        @Override
        default void exitListNode(final ConfigurationNode node, final Void state) throws E {
            exitListNode(node);
        }

        /**
         * Called for a list node after the node and any of its children have
         * been visited.
         *
         * @param node the node that has been visited
         * @throws E when thrown by implementation
         * @since 4.0.0
         */
        default void exitListNode(final ConfigurationNode node) throws E {}

        @Override
        default Void endVisit(final Void state) throws E {
            endVisit();
            return null;
        }

        /**
         * Called after every node has been visited, to allow for cleanup
         * and validation.
         *
         * @throws E when thrown by implementation
         * @since 4.0.0
         */
        default void endVisit() throws E {}
    }

    /**
     * A subinterface for visitors that do not throw any checked exceptions
     * during their execution.
     *
     * @param <S> state type
     * @param <T> terminal value type
     * @since 4.0.0
     */
    interface Safe<S, T> extends ConfigurationVisitor<S, T, VisitorSafeNoopException> {

        @Override
        S newState();

        @Override
        void beginVisit(ConfigurationNode node, S state);

        @Override
        void enterNode(ConfigurationNode node, S state);

        @Override
        void enterMappingNode(ConfigurationNode node, S state);

        @Override
        void enterListNode(ConfigurationNode node, S state);

        @Override
        void enterScalarNode(ConfigurationNode node, S state);

        @Override
        void exitMappingNode(ConfigurationNode node, S state);

        @Override
        void exitListNode(ConfigurationNode node, S state);

        @Override
        T endVisit(S state);

    }

}

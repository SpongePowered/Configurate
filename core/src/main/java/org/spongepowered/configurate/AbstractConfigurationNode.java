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

import static io.leangen.geantyref.GenericTypeReflector.erase;
import static io.leangen.geantyref.GenericTypeReflector.isMissingTypeParameters;
import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Simple implementation of {@link ConfigurationNode}.
 */
abstract class AbstractConfigurationNode<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>>
        implements ScopedConfigurationNode<N> {

    /**
     * The options determining the behaviour of this node.
     */
    private final ConfigurationOptions options;

    /**
     * If this node is attached to a wider configuration structure.
     */
    volatile boolean attached;

    /**
     * Path of this node.
     *
     * <p>Internally, may only be modified when an operation that adds or
     * removes a node at the same or higher level in the node tree</p>
     */
    volatile @Nullable Object key;

    /**
     * The parent of this node.
     */
    private @Nullable A parent;

    /**
     * The current value of this node.
     */
    volatile ConfigValue<N, A> value;

    /**
     * Storage for representation hints.
     */
    final Map<RepresentationHint<?>, Object> hints;

    protected AbstractConfigurationNode(final @Nullable Object key, final @Nullable A parent, final ConfigurationOptions options) {
        requireNonNull(options, "options");
        if ((key == null) != (parent == null)) {
            throw new IllegalArgumentException("A node's key and parent must share the same nullability status");
        }

        this.key = key;
        this.options = options;
        this.parent = parent;
        this.value = NullConfigValue.instance();
        this.hints = new ConcurrentHashMap<>();

        // if the parent is null, this node is a root node, and is therefore "attached"
        if (parent == null) {
            this.attached = true;
        }
    }

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    protected AbstractConfigurationNode(final @Nullable A parent, final A copyOf) {
        this.options = copyOf.options();
        this.attached = true; // copies are always attached
        this.key = copyOf.key;
        this.parent = parent;
        this.value = copyOf.value.copy(this.implSelf());
        this.hints = new ConcurrentHashMap<>(copyOf.hints);
    }

    /**
     * Handles the copying of applied defaults, if enabled.
     *
     * @param node destination node
     * @param type value type
     * @param defValue the default value
     * @param <V> the value type
     * @return the same value
     */
    static <V> V storeDefault(final ConfigurationNode node, final Type type, final V defValue) throws SerializationException {
        requireNonNull(defValue, "defValue");
        if (node.options().shouldCopyDefaults()) {
            node.set(type, defValue);
        }
        return defValue;
    }

    /**
     * Handles the copying of applied defaults, if enabled.
     *
     * @param node destination node
     * @param type value type
     * @param defValue the default value
     * @param <V> the value type
     * @return the same value
     */
    static <V> V storeDefault(final ConfigurationNode node, final AnnotatedType type, final V defValue) throws SerializationException {
        requireNonNull(defValue, "defValue");
        if (node.options().shouldCopyDefaults()) {
            node.set(type, defValue);
        }
        return defValue;
    }

    @Override
    public final @Nullable Object get(final AnnotatedType type) throws SerializationException {
        return this.get0(type, true);
    }

    @Override
    public final Object get(final AnnotatedType type, final Object def) throws SerializationException {
        final @Nullable Object value = this.get0(type, false);
        return value == null ? storeDefault(this, type, def) : value;
    }

    @Override
    public final Object get(final AnnotatedType type, final Supplier<?> defSupplier) throws SerializationException {
        final @Nullable Object value = this.get0(type, false);
        return value == null ? storeDefault(this, type, defSupplier.get()) : value;
    }

    @Override
    public final @Nullable Object get(final Type type) throws SerializationException {
        return this.get0(type, true);
    }

    @Override
    public final Object get(final Type type, final Object def) throws SerializationException {
        final @Nullable Object value = this.get0(type, false);
        return value == null ? storeDefault(this, type, def) : value;
    }

    @Override
    public final Object get(final Type type, final Supplier<?> defSupplier) throws SerializationException {
        final @Nullable Object value = this.get0(type, false);
        return value == null ? storeDefault(this, type, defSupplier.get()) : value;
    }

    final @Nullable Object get0(final Type type, final boolean doImplicitInit) throws SerializationException {
        requireNonNull(type, "type");
        if (isMissingTypeParameters(type)) {
            throw new SerializationException(this, type, "Raw types are not supported");
        }

        final @Nullable TypeSerializer<?> serial = this.options().serializers().get(type);
        if (this.value instanceof NullConfigValue) {
            if (serial != null && doImplicitInit && this.options().implicitInitialization()) {
                final @Nullable Object emptyValue = serial.emptyValue(type, this.options);
                if (emptyValue != null) {
                    return storeDefault(this, type, emptyValue);
                }
            }
            return null;
        }

        if (serial == null) {
            final @Nullable Object value = this.raw();
            final Class<?> erasure = erase(type);
            if (erasure.isInstance(value)) {
                return value;
            } else {
                return null;
            }
        }
        try {
            return serial.deserialize(type, this.self());
        } catch (final SerializationException ex) {
            ex.initPath(this::path);
            ex.initType(type);
            throw ex;
        }
    }

    final @Nullable Object get0(final AnnotatedType type, final boolean doImplicitInit) throws SerializationException {
        requireNonNull(type, "type");
        if (isMissingTypeParameters(type.getType())) {
            throw new SerializationException(this, type, "Raw types are not supported");
        }

        final @Nullable TypeSerializer<?> serial = this.options().serializers().get(type);
        if (this.value instanceof NullConfigValue) {
            if (serial != null && doImplicitInit && this.options().implicitInitialization()) {
                final @Nullable Object emptyValue = serial.emptyValue(type, this.options);
                if (emptyValue != null) {
                    return storeDefault(this, type, emptyValue);
                }
            }
            return null;
        }

        if (serial == null) {
            final @Nullable Object value = this.raw();
            final Class<?> erasure = erase(type.getType());
            if (erasure.isInstance(value)) {
                return value;
            } else {
                return null;
            }
        }
        try {
            return serial.deserialize(type, this);
        } catch (final SerializationException ex) {
            ex.initPath(this::path);
            ex.initType(type);
            throw ex;
        }
    }

    @Override
    public final N set(final @Nullable Object newValue) throws SerializationException {
        // if the value to be set is a configuration node already, unwrap and store the raw data
        if (newValue instanceof ConfigurationNode) {
            this.from((ConfigurationNode) newValue);
            return this.self();
        }

        // if the new value is null, handle detaching from this nodes parent
        if (newValue == null) {
            final @Nullable Object key = this.key;
            if (this.parent == null || key == null) {
                this.clear();
            } else {
                this.parent.removeChild(key);
            }
            return this.self();
        } else if (newValue instanceof Collection || newValue instanceof Map) {
            this.insertNewValue(newValue, false);
            return this.self();
        } else {
            return this.set(newValue.getClass(), newValue);
        }
    }

    @Override
    public N from(final ConfigurationNode that) {
        if (that == this) { // this would be a no-op whoop
            return this.self();
        }

        this.hints.clear();
        this.hints.putAll(that.ownHints());
        if (that.isList()) {
            // handle list
            this.attachIfNecessary();
            final ListConfigValue<N, A> newList = new ListConfigValue<>(this.implSelf());
            synchronized (that) {
                final List<? extends ConfigurationNode> children = that.childrenList();
                for (int i = 0; i < children.size(); i++) {
                    final A node = this.createNode(i);
                    node.attached = true;
                    node.from(children.get(i));
                    newList.putChild(i, node);
                }
            }
            this.value = newList;
        } else if (that.isMap()) {
            // handle map
            this.attachIfNecessary();
            final MapConfigValue<N, A> newMap = new MapConfigValue<>(this.implSelf());
            synchronized (that) {
                for (final Map.Entry<Object, ? extends ConfigurationNode> entry : that.childrenMap().entrySet()) {
                    final A node = this.createNode(entry.getKey());
                    node.attached = true;
                    node.from(entry.getValue());
                    newMap.putChild(entry.getKey(), node);
                }
            }
            this.value = newMap;
        } else {
            // handle scalar/null
            this.raw(that.raw());
        }

        return this.self();
    }

    /**
     * Handles the process of setting a new value for this node.
     *
     * @param newValue the new value
     * @param onlyIfNull if the insertion should only take place if the current
     *                    value is null
     */
    private void insertNewValue(final Object newValue, final boolean onlyIfNull) {
        if (newValue instanceof ConfigurationNode) {
            throw new IllegalArgumentException("Cannot set a node as the raw value of another node");
        }

        this.attachIfNecessary();

        synchronized (this) {
            final ConfigValue<N, A> oldValue;
            ConfigValue<N, A> value;
            oldValue = value = this.value;

            if (onlyIfNull && !(oldValue instanceof NullConfigValue)) {
                return;
            }

            // init new config value backing for the new value type if necessary
            if (newValue instanceof Collection) {
                if (!(value instanceof ListConfigValue)) {
                    value = new ListConfigValue<>(this.implSelf());
                }
            } else if (newValue instanceof Map) {
                if (!(value instanceof MapConfigValue)) {
                    value = new MapConfigValue<>(this.implSelf());
                }
            } else if (!(value instanceof ScalarConfigValue)) {
                value = new ScalarConfigValue<>(this.implSelf());
            }

            // insert the data into the config value
            value.set(newValue);
            this.value = value;
        }
    }

    @Override
    public N mergeFrom(final ConfigurationNode other) {
        // If we are empty, then just directly set our value from the source
        if ((this.virtual() || this.empty()) && !other.virtual()) {
            return this.from(other);
        }

        this.hints.putAll(other.ownHints());
        if (other.isMap()) {
            final ConfigValue<N, A> oldValue;
            ConfigValue<N, A> newValue;
            synchronized (this) {
                oldValue = newValue = this.value;

                // ensure the current type is applicable.
                if (!(oldValue instanceof MapConfigValue)) {
                    if (oldValue instanceof NullConfigValue) {
                        newValue = new MapConfigValue<>(this.implSelf());
                    } else {
                        return this.self();
                    }
                }

                // merge values from 'other'
                for (final Map.Entry<Object, ? extends ConfigurationNode> ent : other.childrenMap().entrySet()) {
                    final @Nullable A currentChild = newValue.child(ent.getKey());
                    // Never allow null values to overwrite non-null values
                    if (currentChild != null && currentChild.raw() != null && ent.getValue().raw() == null) {
                        continue;
                    }

                    // create a new child node for the value
                    final A newChild = this.createNode(ent.getKey());
                    newChild.attached = true;
                    newChild.from(ent.getValue());
                    // replace the existing value, if absent
                    final @Nullable A existing = newValue.putChildIfAbsent(ent.getKey(), newChild);
                    // if an existing value was present, attempt to merge the new value into it
                    if (existing != null) {
                        existing.mergeFrom(newChild);
                    }
                }
                this.value = newValue;
            }
        } else if (other.isList()) {
            if (this.virtual()) {
                this.from(other);
            }
        } else if (other.rawScalar() != null) {
            // otherwise, replace the value of this node, only if currently null
            this.insertNewValue(other.rawScalar(), true);
        }
        return this.self();
    }

    @Override
    public final @Nullable Object raw() {
        return this.value.get();
    }

    @Override
    public final N raw(final @Nullable Object newValue) {
        // if the new value is null, handle detaching from this nodes parent
        if (newValue == null) {
            final @Nullable Object key = this.key;
            if (this.parent == null || key == null) {
                this.clear();
            } else {
                this.parent.removeChild(key);
            }
        } else {
            this.insertNewValue(newValue, false);
        }

        return this.self();
    }

    @Override
    public final @Nullable Object rawScalar() {
        final ConfigValue<N, A> value = this.value;
        if (value instanceof ScalarConfigValue<?, ?>) {
            return value.get();
        } else {
            return null;
        }
    }

    @Override
    public final N node(final Object... path) {
        A pointer = this.implSelf();
        for (final Object el : path) {
            pointer = pointer.child(requireNonNull(el, () -> "element in path " + Arrays.toString(path)), false);
        }
        return pointer.self();
    }

    @Override
    public final N node(final Iterable<?> path) {
        A pointer = this.implSelf();
        for (final Object el : path) {
            pointer = pointer.child(requireNonNull(el, () -> "element in path " + path), false);
        }
        return pointer.self();
    }

    @Override
    public final boolean hasChild(final Object... path) {
        A pointer = this.implSelf();
        for (final Object el : path) {
            final @Nullable A child = pointer.value.child(requireNonNull(el, () -> "element in path " + Arrays.toString(path)));
            if (child == null) {
                return false;
            }
            pointer = child;
        }
        return true;
    }

    @Override
    public final boolean hasChild(final Iterable<?> path) {
        A pointer = this.implSelf();
        for (final Object el : path) {
            final @Nullable A child = pointer.value.child(requireNonNull(el, () -> "element in path " + path));
            if (child == null) {
                return false;
            }
            pointer = child;
        }
        return true;
    }

    @Override
    public final boolean virtual() {
        return !this.attached;
    }

    @Override
    public boolean isNull() {
        return this.value instanceof NullConfigValue;
    }

    @Override
    public final boolean isList() {
        return this.value instanceof ListConfigValue;
    }

    @Override
    public final boolean isMap() {
        return this.value instanceof MapConfigValue;
    }

    @Override
    public final List<N> childrenList() {
        final ConfigValue<N, A> value = this.value;
        return value instanceof ListConfigValue ? ((ListConfigValue<N, A>) value).unwrapped() : Collections.emptyList();
    }

    @Override
    public final Map<Object, N> childrenMap() {
        final ConfigValue<N, A> value = this.value;
        return value instanceof MapConfigValue ? ((MapConfigValue<N, A>) value).unwrapped() : Collections.emptyMap();
    }

    @Override
    public boolean empty() {
        return this.value.isEmpty();
    }

    /**
     * Gets a child node, relative to this.
     *
     * @param key the key
     * @param attach if the resultant node should be automatically attached
     * @return the child node
     */
    protected final A child(final Object key, final boolean attach) {
        @Nullable A child = this.value.child(key);

        // child doesn't currently exist
        if (child == null) {
            if (attach) {
                // attach ourselves first
                this.attachIfNecessary();
                // insert the child node into the value
                final @Nullable A existingChild = this.value.putChildIfAbsent(key, child = this.createNode(key));
                if (existingChild != null) {
                    child = existingChild;
                } else {
                    this.attachChild(child);
                }
            } else {
                // just create a new virtual (detached) node
                child = this.createNode(key);
            }
        }

        return child;
    }

    @Override
    public final boolean removeChild(final Object key) {
        return detachIfNonNull(this.value.putChild(key, null)) != null;
    }

    private static <N extends ScopedConfigurationNode<N>, T extends AbstractConfigurationNode<N, T>>
            @Nullable T detachIfNonNull(final @Nullable T node) {
        if (node != null) {
            node.attached = false;
            node.clear();
        }
        return node;
    }

    @Override
    public final N appendListNode() {
        // the appended node can have a key of -1
        // the "real" key will be determined when the node is inserted into a list config value
        return this.child(ListConfigValue.UNALLOCATED_IDX, false).self();
    }

    @Override
    public final @Nullable Object key() {
        return this.key;
    }

    @Override
    public final NodePath path() {
        N pointer = this.self();
        if (pointer.parent() == null) {
            return NodePath.path();
        }

        final Deque<@Nullable Object> pathElements = new ArrayDeque<>();
        do {
            pathElements.addFirst(pointer.key());
            pointer = requireNonNull(pointer.parent());
        } while (pointer.parent() != null);
        return NodePath.of(pathElements);
    }

    @Override
    public final @Nullable N parent() {
        final @Nullable A parent = this.parent;
        return parent == null ? null : parent.self();
    }

    @Override
    public final ConfigurationOptions options() {
        return this.options;
    }

    @Override
    public final N copy() {
        return this.copy(null).self();
    }

    protected abstract A copy(@Nullable A parent);

    /**
     * The same as {@link #parent()} - but ensuring that 'parent' is attached via
     * {@link #attachChildIfAbsent(AbstractConfigurationNode)}.
     *
     * @return the parent
     */
    final @Nullable A parentEnsureAttached() {
        @Nullable A parent = this.parent;
        if (parent != null && parent.virtual()) {
            parent = parent.parentEnsureAttached().attachChildIfAbsent(parent);
        }
        return this.parent = parent;
    }

    protected final void attachIfNecessary() {
        if (!this.attached) {
            final @Nullable A parent = this.parentEnsureAttached();
            if (parent != null) {
                parent.attachChild(this.implSelf());
            }
        }
    }

    protected final A attachChildIfAbsent(final A child) {
        return this.attachChild(child, true);
    }

    final void attachChild(final A child) {
        this.attachChild(child, false);
    }

    /**
     * Attaches a child to this node.
     *
     * @param child the child
     * @return the resultant value
     */
    private A attachChild(final A child, final boolean onlyIfAbsent) {
        // ensure this node is attached
        if (this.virtual()) {
            throw new IllegalStateException("This parent is not currently attached. This is an internal state violation.");
        }

        // ensure the child actually is a child
        if (!Objects.equals(child.parentEnsureAttached(), this)) {
            throw new IllegalStateException("Child " + child + " path is not a direct parent of me (" + this.path() + "), cannot attach");
        }

        // update the value
        final ConfigValue<N, A> oldValue;
        ConfigValue<N, A> newValue;
        synchronized (this) {
            newValue = oldValue = this.value;

            if (oldValue instanceof MapConfigValue) {
                if (child.key == ListConfigValue.UNALLOCATED_IDX) {
                    newValue = new ListConfigValue<>(this.implSelf());
                }
            } else {
                // if the existing value isn't a map, we need to update it's type
                if (ListConfigValue.likelyNewListKey(child.key) || ListConfigValue.likelyListKey(oldValue, child.key)) {
                    // if child.key is an integer, we can infer that the type of this node should be a list
                    if (oldValue instanceof NullConfigValue) {
                        // if the oldValue was null, we can just replace it with an empty list
                        newValue = new ListConfigValue<>(this.implSelf());
                    } else if (!(oldValue instanceof ListConfigValue)) {
                        // if the oldValue contained a value, we add it as the first element of the
                        // new list
                        newValue = new ListConfigValue<>(this.implSelf(), oldValue.get());
                    }
                } else {
                    // if child.key isn't an integer, assume map
                    newValue = new MapConfigValue<>(this.implSelf());
                }
            }

            /// now the value has been updated to an appropriate type, we can insert the value
            final @Nullable Object childKey = child.key;
            if (childKey == null) {
                throw new IllegalArgumentException("Cannot attach a child with null key");
            }

            if (onlyIfAbsent) {
                final @Nullable A oldChild = newValue.putChildIfAbsent(childKey, child);
                if (oldChild != null) {
                    return oldChild;
                }
            } else {
                detachIfNonNull(newValue.putChild(childKey, child));
            }
            this.value = newValue;
        }

        if (newValue != oldValue) {
            oldValue.clear();
        }
        child.attached = true;
        return child;
    }

    protected final void clear() {
        synchronized (this) {
            final ConfigValue<N, A> oldValue = this.value;
            this.value = NullConfigValue.instance();
            oldValue.clear();
        }
    }

    @Override
    public final <S, T, E extends Exception> T visit(final ConfigurationVisitor<S, T, E> visitor, final S state) throws E {
        return this.visitInternal(visitor, state);
    }

    @Override
    public final <S, T> T visit(final ConfigurationVisitor.Safe<S, T> visitor, final S state) {
        try {
            return this.visitInternal(visitor, state);
        } catch (final VisitorSafeNoopException ex) {
            // this exception should never be thrown, has a private constructor
            throw new AssertionError("Exception was thrown on a Safe visitor", ex);
        }
    }

    @SuppressWarnings({"JdkObsolete", "unchecked", "PMD.LooseCoupling"})
    private <S, T, E extends Exception> T visitInternal(final ConfigurationVisitor<S, T, E> visitor, final S state) throws E {
        visitor.beginVisit(this.self(), state);
        if (!(this.value instanceof NullConfigValue)) { // only visit if we have an actual value
            final LinkedList<Object> toVisit = new LinkedList<>();
            toVisit.add(this);

            @Nullable Object active;
            while ((active = toVisit.pollFirst()) != null) {
                // try to pop a node from the stack, or handle the node exit if applicable
                final @Nullable A current = VisitorNodeEnd.popFromVisitor(active, visitor, state);
                if (current == null) {
                    continue;
                }

                try {
                    visitor.enterNode(current.self(), state);
                    final ConfigValue<N, A> value = current.value;
                    if (value instanceof MapConfigValue) {
                        visitor.enterMappingNode(current.self(), state);
                        toVisit.addFirst(new VisitorNodeEnd(current, true));
                        toVisit.addAll(0, ((MapConfigValue<N, A>) value).values.values());
                    } else if (value instanceof ListConfigValue) {
                        visitor.enterListNode(current.self(), state);
                        toVisit.addFirst(new VisitorNodeEnd(current, false));
                        toVisit.addAll(0, ((ListConfigValue<N, A>) value).values);
                    } else if (value instanceof ScalarConfigValue) {
                        visitor.enterScalarNode(current.self(), state);
                    } else if (!(value instanceof NullConfigValue)) { // temporary workaround, no null values should appear in attached nodes
                        throw new IllegalStateException("Unknown value type " + value.getClass() + " at " + current.path());
                    }
                } catch (final Exception ex) {
                    // Assign an appropriate path to ConfigurateExceptions
                    if (ex instanceof ConfigurateException) {
                        ((ConfigurateException) ex).initPath(current::path);
                    }
                    throw (E) ex;
                }
            }
        }
        return visitor.endVisit(state);
    }

    @Override
    public final <V> N hint(final RepresentationHint<V> hint, final @Nullable V value) {
        if (value == null) {
            this.hints.remove(hint);
        } else {
            this.hints.put(hint, value);
        }

        return this.self();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <V> @Nullable V hint(final RepresentationHint<V> hint) {
        final Object value = this.hints.get(hint);
        if (value != null) {
            return (V) value;
        }
        final @Nullable A parent = this.parent;
        if (parent != null && hint.inheritable()) {
            return parent.hint(hint);
        } else {
            return hint.defaultValue();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <V> @Nullable V ownHint(final RepresentationHint<V> hint) {
        return (V) this.hints.get(hint);
    }

    @Override
    public final Map<RepresentationHint<?>, ?> ownHints() {
        return UnmodifiableCollections.copyOf(this.hints);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractConfigurationNode)) {
            return false;
        }

        final AbstractConfigurationNode<?, ?> that = (AbstractConfigurationNode<?, ?>) o;
        return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
    }

    @Override
    public String toString() {
        return "AbstractConfigurationNode{key=" + this.key + ", value=" + this.value + '}';
    }

    // Methods to be implemented for type-safety

    protected abstract A createNode(Object path);

    protected abstract A implSelf();

}

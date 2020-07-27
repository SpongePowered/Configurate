package org.spongepowered.configurate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;

class ReferenceConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> extends ConfigValue<N, A> {

    volatile ConfigurationNode target;

    protected ReferenceConfigValue(final @NonNull A holder) {
        super(holder);
    }

    @Override
    @Nullable Object getValue() {
        return this.target.getValue();
    }

    @Override
    void setValue(final @Nullable Object value) {
        this.target.setValue(value);
    }

    @Override
    @Nullable A putChild(final Object key, final @Nullable A value) {
        throw new IllegalStateException("Referenced configuration nodes should not be directly modified");
    }

    @Override
    @Nullable A putChildIfAbsent(final Object key, final @Nullable A value) {
        throw new IllegalStateException("Referenced configuration nodes should not be directly modified");
    }

    @Override
    @Nullable A getChild(final @Nullable Object key) {
        return null; // TODO
    }

    @Override
    Iterable<A> iterateChildren() {
        return Collections.emptyList();
    }

    @Override
    @NonNull ConfigValue<N, A> copy(final @NonNull A holder) {
        final ReferenceConfigValue<N, A> ret = new ReferenceConfigValue<>(holder);
        ret.target = this.target;
        return ret;
    }

    @Override
    boolean isEmpty() {
        return this.target.isEmpty();
    }

}

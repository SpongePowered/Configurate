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

import java.util.Objects;

/**
 * A way to know that we've reached the end of a node within a visitor,
 * plus associated utility methods.
 */
class VisitorNodeEnd {

    private final ConfigurationNode end;
    private final boolean isMap;

    VisitorNodeEnd(final ConfigurationNode end, final boolean isMap) {
        this.end = end;
        this.isMap = isMap;
    }

    ConfigurationNode getEnd() {
        return this.end;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof VisitorNodeEnd)) {
            return false;
        }

        final VisitorNodeEnd that = (VisitorNodeEnd) o;
        return getEnd().equals(that.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEnd());
    }

    @SuppressWarnings("unchecked")
    static <N extends ConfigurationNode, A extends AbstractConfigurationNode<?, A>, S, E extends Exception> @Nullable A
            popFromVisitor(final Object unknown, final ConfigurationVisitor<N, S, ?, E> visitor, final S state) throws E {
        if (unknown instanceof VisitorNodeEnd) {
            final N node = (N) ((VisitorNodeEnd) unknown).getEnd();
            if (((VisitorNodeEnd) unknown).isMap) {
                visitor.exitMappingNode(node, state);
            } else {
                visitor.exitListNode(node, state);
            }
            return null;
        } else if (unknown instanceof AbstractConfigurationNode) {
            return (A) unknown;
        } else {
            throw new IllegalStateException("Unknown value type " + unknown);
        }
    }

}

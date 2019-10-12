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
package org.spongepowered.configurate.transformation;

import org.spongepowered.configurate.ConfigurationNode;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of {@link ConfigurationTransformation}.
 *
 * <p>Transformations are executed from deepest in the configuration hierarchy outwards.</p>
 */
class SingleConfigurationTransformation<T extends ConfigurationNode<T>> extends ConfigurationTransformation<T> {
    private final MoveStrategy strategy;
    private final Map<Object[], TransformAction<? super T>> actions;

    /**
     * Thread local {@link ConfigurationTransformation.NodePath} instance - used so we don't have to create lots of NodePath
     * instances.
     *
     * As such, data within paths is only guaranteed to be the same during a run of
     * a transform function.
     */
    private final ThreadLocal<ConfigurationTransformation.NodePath> sharedPath = ThreadLocal.withInitial(ConfigurationTransformation.NodePath::new);

    SingleConfigurationTransformation(Map<Object[], TransformAction<? super T>> actions, MoveStrategy strategy) {
        this.actions = actions;
        this.strategy = strategy;
    }

    @Override
    public void apply(@NonNull T node) {
        for (Map.Entry<Object[], TransformAction<? super T>> ent : actions.entrySet()) {
            applySingleAction(node, ent.getKey(), 0, node, ent.getValue());
        }
    }

    private void applySingleAction(T start, Object[] path, int startIdx, T node, TransformAction<? super T> action) {
        for (int i = startIdx; i < path.length; ++i) {
            if (path[i] == WILDCARD_OBJECT) {
                if (node.hasListChildren()) {
                    List<T> children = node.getChildrenList();
                    for (int di = 0; di < children.size(); ++di) {
                        path[i] = di;
                        applySingleAction(start, path, i + 1, children.get(di), action);
                    }
                    path[i] = WILDCARD_OBJECT;
                } else if (node.hasMapChildren()) {
                    for (Map.Entry<Object, T> ent : node.getChildrenMap().entrySet()) {
                        path[i] = ent.getKey();
                        applySingleAction(start, path, i + 1, ent.getValue(), action);
                    }
                    path[i] = WILDCARD_OBJECT;
                } else {
                    // No children
                    return;
                }
                return;
            } else {
                node = node.getNode(path[i]);
                if (node.isVirtual()) {
                    return;
                }
            }
        }

        // apply action
        ConfigurationTransformation.NodePath nodePath = sharedPath.get();
        nodePath.arr = path;

        Object[] transformedPath = action.visitPath(nodePath, node);
        if (transformedPath != null && !Arrays.equals(path, transformedPath)) {
            this.strategy.move(node, start.getNode(transformedPath));
            node.setValue(null);
        }
    }
}

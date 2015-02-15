/**
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
package ninja.leaping.configurate.transformation;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility to perform bulk transformations of configuration data
 * Transformations are executed from deepest in the configuration hierarchy outwards
 */
class SingleConfigurationTransformation extends ConfigurationTransformation {
    private final Map<Object[], TransformAction> actions;
    private final ThreadLocal<NodePath> sharedPath = new ThreadLocal<NodePath>() {
        @Override
        protected NodePath initialValue() {
            return new NodePath();
        }
    };

    protected SingleConfigurationTransformation(Map<Object[], TransformAction> actions) {
        this.actions = actions;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void apply(ConfigurationNode node) {
        for (Map.Entry<Object[], TransformAction> ent : actions.entrySet()) {
            applySingleAction(node, ent.getKey(), 0, node, ent.getValue());
        }
    }

    protected void applySingleAction(ConfigurationNode start, Object[] path, int startIdx, ConfigurationNode node,
                                     TransformAction action) {
        for (int i = startIdx; i < path.length; ++i) {
            if (path[i] == WILDCARD_OBJECT) {
                if (node.hasListChildren()) {
                    List<? extends ConfigurationNode> children = node.getChildrenList();
                    for (int cI = 0; cI < children.size(); ++cI) {
                        path[i] = cI;
                        applySingleAction(start, path, i + 1, children.get(cI), action);
                    }
                    path[i] = WILDCARD_OBJECT;
                } else if (node.hasMapChildren()) {
                    for (Map.Entry<Object, ? extends ConfigurationNode> ent : node.getChildrenMap().entrySet()) {
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
        NodePath immutablePath = sharedPath.get();
        immutablePath.arr = path;
        Object[] transformedPath = action.visitPath(immutablePath, start);
        if (transformedPath != null && !Arrays.equals(path, transformedPath)) {
            start.getNode(transformedPath).setValue(node);
            node.setValue(null);
        }
    }
}

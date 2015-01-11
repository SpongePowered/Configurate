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

/**
 * Represents an action to be performed that transforms a node in the configuration tree
 */
public interface TransformAction {
    /**
     * Called at a certain path, with the node at that path
     * @param inputPath The path of the given node
     * @param valueAtPath The node at the input path. May be modified
     * @return A modified path, or null if the path is to stay the same
     */
    public Object[] visitPath(ConfigurationTransformation.NodePath inputPath, ConfigurationNode valueAtPath);
}

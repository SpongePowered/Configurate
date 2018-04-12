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
package ninja.leaping.configurate.transformation;

import ninja.leaping.configurate.ConfigurationNode;

/**
 * Strategy to use when moving a node from one path to another
 */
public enum MoveStrategy {

    /**
     * Moves nodes using {@link ConfigurationNode#mergeValuesFrom(ConfigurationNode)}.
     */
    MERGE {
        @Override
        public void move(ConfigurationNode source, ConfigurationNode target) {
            target.mergeValuesFrom(source);
        }
    },

    /**
     * Moves nodes using {@link ConfigurationNode#setValue(Object)}.
     */
    OVERWRITE {
        @Override
        public void move(ConfigurationNode source, ConfigurationNode target) {
            target.setValue(source);
        }
    };

    /**
     * Moves <code>source</code> to <code>target</code>.
     *
     * @param source The source node
     * @param target The target node
     */
    public abstract void move(ConfigurationNode source, ConfigurationNode target);
}

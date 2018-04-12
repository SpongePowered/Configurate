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
package ninja.leaping.configurate;

import java.util.Iterator;

abstract class ConfigValue {
    protected final SimpleConfigurationNode holder;

    protected ConfigValue(SimpleConfigurationNode holder) {
        this.holder = holder;
    }

    abstract Object getValue();
    abstract void setValue(Object value);

    /**
     * put a child, or null to remove value at key
     *
     * @param key the key
     * @param value the node to put at key
     * @return existing node at key, if present
     */
    abstract SimpleConfigurationNode putChild(Object key, SimpleConfigurationNode value);
    abstract SimpleConfigurationNode putChildIfAbsent(Object key, SimpleConfigurationNode value);

    /**
     * gets the currently present child.
     * returns null if no child is present
     *
     * @param key the key to get child at
     * @return the child if any
     */
    abstract SimpleConfigurationNode getChild(Object key);
    abstract Iterable<SimpleConfigurationNode> iterateChildren();

    void clear() {
        for (Iterator<SimpleConfigurationNode> it = iterateChildren().iterator(); it.hasNext();) {
            SimpleConfigurationNode node = it.next();
            node.attached = false;
            it.remove();
            if (node.getParentAttached().equals(holder)) {
                node.clear();
            }
        }
    }

}

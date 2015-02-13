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
package ninja.leaping.configurate;


import java.util.Collections;

class NullConfigValue extends ConfigValue {
    NullConfigValue(SimpleConfigurationNode holder) {
        super(holder);
    }
    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void setValue(Object value) {
    }

    @Override
    SimpleConfigurationNode putChild(Object key, SimpleConfigurationNode value) {
        return null;
    }

    @Override
    SimpleConfigurationNode putChildIfAbsent(Object key, SimpleConfigurationNode value) {
        return null;
    }

    @Override
    public SimpleConfigurationNode getChild(Object key) {
        return null;
    }

    @Override
    public Iterable<SimpleConfigurationNode> iterateChildren() {
        return Collections.emptySet();
    }

    @Override
    public void clear() {

    }
}

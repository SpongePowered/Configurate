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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.Collections;

class ScalarConfigValue extends ConfigValue {
    private volatile Object value;

    ScalarConfigValue(SimpleConfigurationNode holder) {
        super(holder);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        Preconditions.checkNotNull(value);

        if (!holder.getOptions().acceptsType(value.getClass())) {
            throw new IllegalArgumentException("Configuration does not accept objects of type " + value
                    .getClass());
        }

        this.value = value;
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
       this.value = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScalarConfigValue that = (ScalarConfigValue) o;
        return Objects.equal(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}

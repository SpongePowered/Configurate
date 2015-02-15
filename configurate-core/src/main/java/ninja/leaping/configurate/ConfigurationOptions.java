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

import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.util.Comparator;

/**
 * This object is a holder for general configuration options. This is meant to hold options
 * that are used in configuring how the configuration data structures are handled, rather than the serialization configuration that is located in {@link ConfigurationLoader}s
 */
public class ConfigurationOptions {
    private final Comparator<Object> nodeKeySort;

    private ConfigurationOptions(Comparator<Object> nodeKeySort) {
        this.nodeKeySort = nodeKeySort;
    }

    /**
     * Create a new options object with defaults set
     *
     * @return A new default options object
     */
    public static ConfigurationOptions defaults() {
        return new ConfigurationOptions(null);
    }

    /**
     * Get the key comparator currently being used for this configuration
     *
     * @return The active key comparator
     */
    public Comparator<Object> getKeyComparator() {
        return nodeKeySort;
    }

    /**
     * Return a new options object with the provided option set.
     *
     * @param keyComparator The new comparator to be used. Set to null to use natural ordering.
     * @return The new options object
     */
    public ConfigurationOptions setKeyComparator(Comparator<Object> keyComparator) {
        return new ConfigurationOptions(keyComparator);
    }
}

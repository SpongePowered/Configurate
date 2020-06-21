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
package org.spongepowered.configurate.objectmapping.meta;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helpers for built-in localized processors and constraints.
 */
final class Localization {

    private Localization() {
    }

    /**
     * Get {@code key} from the provided bundle, passing the key through if
     * not found.
     *
     * @param bundle bundle to look in
     * @param key key to find
     * @return localized key, or input
     */
    static String key(final ResourceBundle bundle, final String key) {
        try {
            return bundle.getString(key);
        } catch (final MissingResourceException ex) {
            return key;
        }
    }

}

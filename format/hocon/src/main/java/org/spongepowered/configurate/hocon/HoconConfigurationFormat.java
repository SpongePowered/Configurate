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
package org.spongepowered.configurate.hocon;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationFormat;
import org.spongepowered.configurate.loader.ConfigurationFormat;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.util.Set;

/**
 * A {@link ConfigurationFormat} for the HOCON configuration loader.
 *
 * <p>This format should not be used directly, but instead accessed
 * through methods on {@link ConfigurationFormat}.</p>
 *
 * @since 4.2.0
 */
public final class HoconConfigurationFormat extends AbstractConfigurationFormat<
    CommentedConfigurationNode,
    HoconConfigurationLoader,
    HoconConfigurationLoader.Builder
    > {

    private static final Set<String> SUPPORTED_EXTENSIONS = UnmodifiableCollections.toSet("conf");

    /**
     * For use by service loader only.
     *
     * @since 4.2.0
     */
    public HoconConfigurationFormat() {
        super("hocon", HoconConfigurationLoader::builder, SUPPORTED_EXTENSIONS);
    }

}

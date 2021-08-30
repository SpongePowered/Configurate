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
package org.spongepowered.configurate.loader;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

final class ConfigurationFormats {

    /**
     * Set this property to discover service implementations on Configurate's
     * classloader rather than the context classloader.
     *
     * <p>Some platforms Configurate is commonly deployed on don't appropriately
     * set the context classloader to one that allows plugins that might
     * distribute Configurate themselves to see Configurate. To work around
     * this, we have an option to forcibly perform service discovery on the same
     * classloader that Configurate has been loaded on.</p>
     *
     * <p>Default: {@code false}</p>
     */
    private static final boolean USE_OWN_CLASSLOADER;

    // Try to auto-detect platforms where our own classloader is needed
    private static final String[] OWN_CLASSLOADER_SENTINEL_CLASSES = {
        "org.bukkit.Bukkit"
    };

    static {
        final String property = System.getProperty("configurate.services.discoverOnOwnClassloader");
        boolean useOwnLoader;
        if (property != null) {
            useOwnLoader = Boolean.parseBoolean(property);
        } else {
            useOwnLoader = false;
            for (final String clazz : OWN_CLASSLOADER_SENTINEL_CLASSES) {
                try {
                    Class.forName(clazz);
                    useOwnLoader = true;
                    break;
                } catch (final ClassNotFoundException ex) {
                    // unknown, continue
                }
            }
        }

        USE_OWN_CLASSLOADER = useOwnLoader;
    }

    static final Map<String, Holder> BY_EXTENSION;
    static final Set<Holder> FORMATS;

    static {
        final Map<String, Holder> byExtension = new HashMap<>();
        final Set<Holder> formats = new HashSet<>();

        final ServiceLoader<ConfigurationFormat> loader;
        if (USE_OWN_CLASSLOADER) {
            loader = ServiceLoader.load(ConfigurationFormat.class, ConfigurationFormats.class.getClassLoader());
        } else {
            loader = ServiceLoader.load(ConfigurationFormat.class);
        }
        for (final Iterator<ConfigurationFormat> it = loader.iterator(); it.hasNext();) {
            Holder holder;
            try {
                holder = new Holder(it.next());
            } catch (final ServiceConfigurationError ex) {
                holder = new Holder(ex);
            }

            formats.add(holder);
            if (holder.successful()) {
                final @Nullable ConfigurationFormat format = holder.format;
                if (format == null) {
                    continue;
                }
                final Set<String> supportedExtensions = requireNonNull(
                    format.supportedExtensions(),
                    () -> "The supported extensions field was improperly null on " + format
                );
                for (final String ext : supportedExtensions) {
                    byExtension.put(ext, holder);
                }
            }
        }

        BY_EXTENSION = UnmodifiableCollections.copyOf(byExtension);
        FORMATS = UnmodifiableCollections.copyOf(formats);

    }

    static final class UnwrappedFormats {

        static final Set<ConfigurationFormat> UNWRAPPED_FORMATS;

        static {
            final Set<ConfigurationFormat> formats = new HashSet<>(ConfigurationFormats.FORMATS.size());
            for (final Holder format : ConfigurationFormats.FORMATS) {
                formats.add(format.get());
            }
            UNWRAPPED_FORMATS = UnmodifiableCollections.copyOf(formats);
        }

        private UnwrappedFormats() {
        }

    }

    private ConfigurationFormats() {
    }

    static Set<ConfigurationFormat> unwrappedFormats() {
        return UnwrappedFormats.UNWRAPPED_FORMATS;
    }

    static final class Holder {
        private final @Nullable ConfigurationFormat format;
        private final @Nullable ServiceConfigurationError error;

        Holder(final ConfigurationFormat format) {
            this.format = format;
            this.error = null;
        }

        Holder(final ServiceConfigurationError error) {
            this.format = null;
            this.error = error;
        }

        ConfigurationFormat get() {
            if (this.error != null) {
                throw this.error;
            }
            assert this.format != null; // either-or, by constructor
            return this.format;
        }

        boolean successful() {
            return this.format != null;
        }
    }

}

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
package org.spongepowered.configurate.objectmapping.guice;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import org.spongepowered.configurate.objectmapping.FieldDiscoverer;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A factory for {@link ObjectMapper}s that will inherit the injector from
 * wherever it is provided.
 *
 * <p>This class is intended to be constructed through Guice
 * dependency injection.
 */
@Singleton
public final class GuiceObjectMapperProvider {

    private final ObjectMapper.Factory factory;

    @Inject
    GuiceObjectMapperProvider(final Injector baseInjector) {
        this.factory = ObjectMapper.factoryBuilder()
                .addDiscoverer(ofInjectedPojo(baseInjector))
                .build();
    }

    /**
     * Get the default factory, with guice support added.
     *
     * @return the default guice factory
     */
    public ObjectMapper.Factory get() {
        return this.factory;
    }

    /**
     * Create a field discoverer that uses the provided injector.
     *
     * @param injector injector to create instances with
     * @return new discoverer
     */
    public static FieldDiscoverer<?> ofInjectedPojo(final Injector injector) {
        return FieldDiscoverer.ofPojo(type -> {
            try {
                final Provider<?> prov = injector.getProvider(Key.get(type.getType()));
                return prov::get;
            } catch (final ConfigurationException ex) {
                throw new ObjectMappingException("Cannot create instances", ex);
            }
        });
    }

    @Override
    public String toString() {
        return "GuiceObjectMapperFactory{}";
    }

}

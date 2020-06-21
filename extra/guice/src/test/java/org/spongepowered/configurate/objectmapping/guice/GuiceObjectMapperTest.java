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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

/**
 * Created by zml on 7/5/15.
 */
public class GuiceObjectMapperTest {

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).toInstance("test value");
        }
    }

    private static final class ConfigClass {
        @Inject
        private ConfigClass(final String msg) {
            assertEquals("test value", msg);
        }
    }

    @Test
    public void testCreateGuiceObjectMapper() throws ObjectMappingException {
        final Injector injector = Guice.createInjector(new TestModule());
        final GuiceObjectMapperProvider factory = injector.getInstance(GuiceObjectMapperProvider.class);
        final ObjectMapper<ConfigClass> mapper = factory.get().get(ConfigClass.class);
        assertTrue(mapper.canCreateInstances());
        assertNotNull(mapper.load(BasicConfigurationNode.root()));
    }

}

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
package org.spongepowered.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.NodeKey;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.objectmapping.meta.Setting;

public class NodeResolverTest {

    // node key

    static class TestNodeKey {
        @NodeKey String ownKey;
        String own;
    }

    @Test
    void testNodeKey() throws ObjectMappingException {
        final ObjectMapper<TestNodeKey> mapper = ObjectMapper.factory().get(TestNodeKey.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root().getNode("test");
        source.getNode("own").setValue("yeet");

        final TestNodeKey object = mapper.load(source);

        assertEquals("test", object.ownKey);
        assertEquals("yeet", object.own);
    }

    // key from setting

    static class TestSettingKey {
        @Setting("something") String notSomething;
    }

    @Test
    void testSettingKey() throws ObjectMappingException {
        final ObjectMapper<TestSettingKey> mapper = ObjectMapper.factory().get(TestSettingKey.class);

        final BasicConfigurationNode source = BasicConfigurationNode.root(n -> {
            n.getNode("something").setValue("blah");
        });

        final TestSettingKey object = mapper.load(source);

        assertEquals("blah", object.notSomething);
    }

    // only with annotation (setting.class in this case)

    static class TestOnlyWithAnnotation {
        @Setting String marked;
        String notProcessed;
    }

    @Test
    void testOnlyWithAnnotation() throws ObjectMappingException {
        final ObjectMapper<TestOnlyWithAnnotation> mapper = ObjectMapper.factoryBuilder()
                .addNodeResolver(NodeResolver.onlyWithAnnotation(Setting.class))
                .build().get(TestOnlyWithAnnotation.class);

        final BasicConfigurationNode source = BasicConfigurationNode.root(n -> {
            n.getNode("marked").setValue("something");
            n.getNode("not-processed").setValue("ignored");
        });

        final TestOnlyWithAnnotation object = mapper.load(source);

        assertEquals("something", object.marked);
        assertNull(object.notProcessed);
    }

}

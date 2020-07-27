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
package org.spongepowered.configurate.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Basic sanity checks for the loader
 */
public class YamlConfigurationLoaderTest {

    @Test
    public void testSimpleLoading() throws IOException, ObjectMappingException {
        final URL url = getClass().getResource("/example.yml");
        final ConfigurationLoader<BasicConfigurationNode> loader = YamlConfigurationLoader.builder()
                .setUrl(url).build();
        final ConfigurationNode node = loader.load();
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        assertEquals("dog park", node.getNode("other", "location").getValue());


        final List<Map<String, List<String>>> fooList = new ArrayList<>(node.getNode("foo")
            .getList(new TypeToken<Map<String, List<String>>>() {}));
        assertEquals(0, fooList.get(0).get("bar").size());
    }

    @Test
    public void testReadWithTabs() throws IOException {
        final ConfigurationNode expected = BasicConfigurationNode.root(n -> {
            n.getNode("document").act(d -> {
                d.getNode("we").setValue("support tabs");
                d.getNode("and").setValue("literal tabs\tin strings");
                d.getNode("with").act(w -> {
                    w.appendListNode().setValue("more levels");
                    w.appendListNode().setValue("of indentation");
                });
            });
        });

        final URL url = getClass().getResource("/tab-example.yml");
        final ConfigurationLoader<BasicConfigurationNode> loader = YamlConfigurationLoader.builder()
                .setUrl(url).build();
        final ConfigurationNode node = loader.load();
        assertEquals(expected, node);
    }

}

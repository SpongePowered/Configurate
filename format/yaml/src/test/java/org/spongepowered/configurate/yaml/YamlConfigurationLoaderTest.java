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
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Basic sanity checks for the loader.
 */
public class YamlConfigurationLoaderTest {

    @Test
    void testSimpleLoading() throws ConfigurateException {
        final URL url = getClass().getResource("/example.yml");
        final ConfigurationLoader<CommentedConfigurationNode> loader = YamlConfigurationLoader.builder()
                .url(url).build();
        final ConfigurationNode node = loader.load();
        assertEquals("unicorn", node.node("test", "op-level").raw());
        assertEquals("dragon", node.node("other", "op-level").raw());
        assertEquals("dog park", node.node("other", "location").raw());


        final List<Map<String, List<String>>> fooList = new ArrayList<>(node.node("foo")
            .getList(new TypeToken<Map<String, List<String>>>() {}));
        assertEquals(0, fooList.get(0).get("bar").size());
    }

    @Test
    void testReadWithTabs() throws ConfigurateException {
        final ConfigurationNode expected = CommentedConfigurationNode.root(n -> {
            n.node("document").act(d -> {
                d.node("we").raw("support tabs");
                d.node("and").raw("literal tabs\tin strings");
                d.node("with").act(w -> {
                    w.appendListNode().raw("more levels");
                    w.appendListNode().raw("of indentation");
                });
            });
        });

        final URL url = getClass().getResource("/tab-example.yml");
        final ConfigurationLoader<CommentedConfigurationNode> loader = YamlConfigurationLoader.builder()
                .url(url).build();
        final ConfigurationNode node = loader.load();
        assertEquals(expected, node);
    }

}

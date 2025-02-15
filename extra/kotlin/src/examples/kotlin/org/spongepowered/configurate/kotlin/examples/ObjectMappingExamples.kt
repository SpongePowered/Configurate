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
package org.spongepowered.configurate.kotlin.examples

import java.nio.file.Path
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.loader.ConfigurationLoader
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

fun createLoader(source: Path): ConfigurationLoader<*> {
    // Create a new yaml loader for the target that uses options customized to
    // support the Kotlin object mapper.
    return YamlConfigurationLoader.builder()
        .path(source)
        .defaultOptions {
            it.serializers { s ->
                s.registerAnnotatedObjects(
                    ObjectMapper.factoryBuilder().addDiscoverer(dataClassFieldDiscoverer()).build()
                )
            }
        }
        .build()
}

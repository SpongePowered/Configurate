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
package org.spongepowered.configurate.kotlin

import org.spongepowered.configurate.AttributedConfigurationNode
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions

// Factory methods //

/** Create a new basic configuration node, optionally providing options. */
fun node(
    options: ConfigurationOptions = ConfigurationOptions.defaults(),
    init: BasicConfigurationNode.() -> Unit,
): BasicConfigurationNode = BasicConfigurationNode.root<RuntimeException>(options, init)

/** Create a commented configuration node. */
fun commented(
    options: ConfigurationOptions = ConfigurationOptions.defaults(),
    init: CommentedConfigurationNode.() -> Unit,
): CommentedConfigurationNode = CommentedConfigurationNode.root<RuntimeException>(options, init)

/**
 * Create a new attributed configuration node, with all [attributes] applied, the provided options,
 * and running [init] to initialize the node.
 */
fun attributed(
    nodeName: String = "root",
    vararg attributes: Pair<String, String>,
    options: ConfigurationOptions = ConfigurationOptions.defaults(),
    init: AttributedConfigurationNode.() -> Unit,
): AttributedConfigurationNode {
    val node = AttributedConfigurationNode.root(nodeName, options)
    node.attributes(mapOf(*attributes))
    node.init()
    return node
}

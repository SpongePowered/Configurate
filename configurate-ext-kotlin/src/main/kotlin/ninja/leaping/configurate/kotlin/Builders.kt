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
package ninja.leaping.configurate.kotlin

import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.attributed.AttributedConfigurationNode
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader

// Factory methods

/**
 * Create a new basic configuration node, optionally providing options
 */
fun node(options: ConfigurationOptions = ConfigurationOptions.defaults(), init: ConfigurationNode.() -> Unit) =
        ConfigurationNode.root(options, init)

/**
 * Create a commented configuration node
 */
fun commented(options: ConfigurationOptions = ConfigurationOptions.defaults(), init: CommentedConfigurationNode.() ->
Unit) =
        CommentedConfigurationNode.root(options, init)

fun attributed(nodeName: String = "root", vararg attributes: Pair<String, String>, options: ConfigurationOptions =
        ConfigurationOptions.defaults(), init:
               AttributedConfigurationNode.() ->
               Unit): AttributedConfigurationNode {
    val node = AttributedConfigurationNode.root(nodeName, options)
    node.attributes = mapOf(*attributes)
    node.init()
    return node
}

/**
 * Given a loader, create and configure an empty node. Options may be provided, but otherwise the loader's defaults
 * will be used.
 */
fun <T : ConfigurationNode> ConfigurationLoader<T>.node(options: ConfigurationOptions = this.defaultOptions, init: T.
() -> Unit): T {
    val ret = createEmptyNode(options)
    ret.init()
    return ret
}

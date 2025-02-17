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
package org.spongepowered.configurate.kotlin.extensions

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationNodeFactory
import org.spongepowered.configurate.ConfigurationOptions

/** Use the receiver factory to create a new node. */
operator fun <N : ConfigurationNode> ConfigurationNodeFactory<N>.invoke(
    options: ConfigurationOptions = defaultOptions()
): N = createNode(options)

/**
 * Given a node factory, create and configure an empty node. [Options][options] may be provided, but
 * otherwise the factory's defaults will be used.
 */
fun <T : ConfigurationNode> ConfigurationNodeFactory<T>.node(
    options: ConfigurationOptions = this.defaultOptions(),
    init: T.() -> Unit,
): T {
    val ret = createNode(options)
    ret.init()
    return ret
}

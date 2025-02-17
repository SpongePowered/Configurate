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

import java.net.URL
import java.util.UUID
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.AttributedConfigurationNode
import org.spongepowered.configurate.ScopedConfigurationNode

class BuildersTest {
    @Test
    fun testBuildAttributed() {
        attributed(
            "ServerPack",
            "version" to "1.15.2",
            "url" to "https://permissionsex.stellardrift.ca",
        ) {
            this.node("a").set("Hello")
            this.node("Game, over").set(5)
            child("Haa") { set("nope") }
            child("list", "parameter" to "test") {
                comment("I'm a list!")
                this += "hello"
                this += "world"
            }
        }
    }

    @Test
    fun testBuildCommented() {
        commented {
            child("a", value = "b") { comment("The most important thing") }
            this.node("cow").set("potato")
            set(UUID.randomUUID())
        }
    }

    @Test
    fun testBuildBasic() {
        node {
            this.node("test").set("hello")
            this.node("value").set(URL::class.java, URL("https://spongepowered.org"))
        }
    }
}

// -- Incubating API -- these aren't ready to go into public API

// Internal
private val NO_VALUE: Any = Any()

// Creating children

fun <N : ScopedConfigurationNode<N>> N.child(
    vararg path: Any,
    value: Any? = NO_VALUE,
    init: N.() -> Unit = {},
) {
    val node = this.node(*path)
    if (value != NO_VALUE) {
        node.set(value)
    }
    node.init()
}

/**
 * For attributed nodes only, allow creating a direct child with its attributes.
 *
 * This function returns the child
 */
fun AttributedConfigurationNode.child(
    key: String,
    value: Any? = NO_VALUE,
    vararg attributes: Pair<String, String>,
    init: AttributedConfigurationNode.() -> Unit,
): AttributedConfigurationNode {
    val node = this.node(key)
    tagName(key)
    if (value != NO_VALUE) {
        node.set(value)
    }
    if (!attributes.isEmpty()) {
        node.attributes(mapOf(*attributes))
    }
    node.init()
    return node
}

operator fun <N : ScopedConfigurationNode<N>> N.plusAssign(value: Any?) {
    appendListNode().set(value)
}

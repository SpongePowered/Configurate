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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.objectmapping.ObjectMappingException
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Matches

class ObjectMappingTest {

    data class SimpleTest(val denyMessage: String, val match: String)

    @Test
    fun `deserialize to data class`() {
        val source = node {
            this["deny-message"] = "you're not allowed to do that!"
            this["match"] = "[uo]w[uo]"
        }
        val deserialized = objectMapper<SimpleTest>().load(source)

        println(deserialized)

        assertEquals(SimpleTest("you're not allowed to do that!", "[uo]w[uo]"), deserialized)
    }

    @Test
    fun `serialize from data class`() {
        val node = BasicConfigurationNode.root()
        val source = SimpleTest("goodbye", "i'm leaving")

        objectMapper<SimpleTest>().save(source, node)

        assertEquals("goodbye", node["deny-message"].value)
        assertEquals("i'm leaving", node["match"].value)
    }

    data class AnnotatedTest(
        @Comment("sad") val name: String,
        @Matches("[A-Z]") val attributes: String
    )

    @Test
    fun `annotations on data class entries`() {
        val node = CommentedConfigurationNode.root()
        val data = AnnotatedTest("purr", "SHOUTING")

        val mapper = objectMapper<AnnotatedTest>()
        mapper.save(data, node)

        assertEquals("purr", node["name"].value)
        assertEquals("sad", node["name"].comment)
        assertEquals("SHOUTING", node["attributes"].value)

        assertThrows<ObjectMappingException> {
            mapper.load(
                node {
                    this["name"] = "meow"
                    this["attributes"] = "quiet" // does not match regex
                }
            )
        }
    }
}

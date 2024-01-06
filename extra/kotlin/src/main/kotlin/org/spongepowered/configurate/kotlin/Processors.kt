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

import java.lang.reflect.Type
import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Processor

/**
 * Apply comments from [Comment] annotation on save. This will first call the [String.trimIndent]
 * method on the strings, allowing multi-line Kotlin strings in [Comment] annotations without
 * worrying about indentation.
 *
 * @return a new processor factory
 * @since 4.2.0
 */
fun kotlinCommentsProcessor(): Processor.Factory<Comment, Any?> {
    return Processor.Factory { data: Comment, _: Type? ->
        Processor { _: Any?, destination: ConfigurationNode? ->
            if (destination is CommentedConfigurationNodeIntermediary<*>) {
                val comment = data.value.trimIndent()
                if (data.override) {
                    destination.comment(comment)
                } else {
                    destination.commentIfAbsent(comment)
                }
            }
        }
    }
}

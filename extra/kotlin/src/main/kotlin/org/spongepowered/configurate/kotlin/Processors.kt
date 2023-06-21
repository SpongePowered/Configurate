package org.spongepowered.configurate.kotlin

import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Processor
import java.lang.reflect.Type

/**
 * Apply comments from [Comment] annotation on save. This will first call the [String.trimIndent] method on the strings,
 * allowing multi-line Kotlin strings in [Comment] annotations without worrying about indentation.
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

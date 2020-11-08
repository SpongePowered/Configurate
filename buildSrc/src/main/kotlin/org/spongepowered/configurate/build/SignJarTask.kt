package org.spongepowered.configurate.build

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.withGroovyBuilder

/**
 * A task that uses the Ant jar signing task to sign a jar
 */
@CacheableTask
abstract class SignJarTask : Jar() {

    @get:Input
    abstract val alias: Property<String>

    @get:Input
    abstract val storePassword: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    abstract val keyStore: RegularFileProperty

    @get:Input
    abstract val strict: Property<Boolean>

    init {
        strict.convention(false)
    }

    override fun copy() {
        super.copy()
        if (this.didWork) {
            ant.withGroovyBuilder {
                "signjar"(
                    "jar" to archiveFile.get().asFile,
                    "alias" to alias.get(),
                    "storepass" to storePassword.get(),
                    "keystore" to keyStore.get().asFile,
                    "verbose" to logger.isInfoEnabled,
                    "strict" to strict.get()
                )
            }
        }
    }
}

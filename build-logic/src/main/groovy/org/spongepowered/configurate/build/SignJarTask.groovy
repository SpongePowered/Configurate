package org.spongepowered.configurate.build

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.jvm.tasks.Jar

/**
 * A task that uses the Ant jar signing task to sign a jar
 */
@CacheableTask
abstract class SignJarTask extends Jar {

    @Input
    abstract Property<String> getAlias()

    @Input
    abstract Property<String> getStorePassword()

    @InputFile
    @PathSensitive(PathSensitivity.ABSOLUTE)
    abstract RegularFileProperty getKeyStore()

    @Input
    abstract Property<Boolean> getStrict()

    SignJarTask() {
        strict.convention(false)
    }

    @Override
    void copy() {
        super.copy()
        if (this.didWork) {
            ant.signjar(
                jar: archiveFile.get().asFile,
                alias: alias.get(),
                storepass: storePassword.get(),
                keystore: keyStore.get().asFile,
                verbose: logger.infoEnabled,
                strict: strict.get()
            )
        }
    }

}

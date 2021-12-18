package org.spongepowered.configurate.build

import org.gradle.api.model.ObjectFactory
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property

class ConfigurateExtension {
    final Property<String> autoValueVersion
    private final DependencyHandler dh
    
    ConfigurateExtension(final ObjectFactory objects, final DependencyHandler dh) {
        this.autoValueVersion = objects.property(String)
        this.dh = dh
    }
    
    def useAutoValue() {
        this.dh.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, "com.google.auto.value:auto-value-annotations:${autoValueVersion.get()}")
        this.dh.add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, "com.google.auto.value:auto-value:${autoValueVersion.get()}")
    }
}

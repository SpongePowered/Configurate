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
package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.Utils.isNestedConfig;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.interfaces.Constants;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Generates an implementation for a given interface based config,
 * which then can be read by Configurate.
 *
 * @since 4.2.0
 */
@AutoService(Processor.class)
public final class ConfigImplementationGeneratorProcessor extends AbstractProcessor {

    private final Properties mappings = new Properties();
    Types typeUtils;
    private Filer filer;
    private Messager messager;

    @Override
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.typeUtils = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ConfigSerializable.class.getCanonicalName());
    }

    @Override
    public boolean process(final Set<? extends TypeElement> ignored, final RoundEnvironment env) {
        if (env.processingOver()) {
            if (!env.errorRaised()) {
                writeMappings();
            }
            return false;
        }

        for (final Element element : env.getElementsAnnotatedWith(ConfigSerializable.class)) {
            if (element.getKind() != ElementKind.INTERFACE) {
                continue;
            }
            final TypeElement typeElement = (TypeElement) element;

            // nested classes are handled in their containing interfaces
            if (isNestedConfig(typeElement)) {
                continue;
            }

            try {
                processInterface(typeElement);
            } catch (final IOException exception) {
                printError(exception.getMessage());
            }
        }

        return false;
    }

    /**
     * Generate a class for the given interface.
     */
    private void processInterface(final TypeElement type) throws IOException {
        final ClassName className = ClassName.get(type);

        final TypeSpec.@Nullable Builder generated = new ConfigImplementationGenerator(this, type).generate();
        if (generated == null) {
            return;
        }

        JavaFile.builder(className.packageName(), generated.build())
            .build()
            .writeTo(this.filer);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private void writeMappings() {
        final FileObject resource;
        try {
            resource = this.filer.createResource(StandardLocation.CLASS_OUTPUT, "", Constants.MAPPING_FILE);
            try (Writer writer = resource.openWriter()) {
                this.mappings.store(writer, null);
            }
        } catch (final IOException exception) {
            throw new RuntimeException("Failed to write interface mappings!", exception);
        }
    }

    Properties generatedClasses() {
        return this.mappings;
    }

    void printError(final String message, final Object... arguments) {
        this.messager.printMessage(Kind.ERROR, String.format(Locale.ROOT, message, arguments));
    }

}

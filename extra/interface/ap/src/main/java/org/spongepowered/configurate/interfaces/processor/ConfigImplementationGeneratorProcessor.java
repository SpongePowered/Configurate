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

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.spongepowered.configurate.interfaces.Constants;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
class ConfigImplementationGeneratorProcessor extends AbstractProcessor {

    private final Properties mappings = new Properties();
    private Types typeUtils;
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
                processInterface(typeElement, this.mappings);
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return false;
    }

    /**
     * Generate a class for the given interface and
     * returns the name of the generated class.
     */
    private void processInterface(final TypeElement type, final Properties generatedClasses) throws IOException {
        final ClassName className = ClassName.get(type);
        final TypeSpec spec = generateImplementation(type, generatedClasses).build();

        JavaFile.builder(className.packageName(), spec)
            .build()
            .writeTo(this.filer);
    }

    private TypeSpec.Builder generateImplementation(final TypeElement type, final Properties generatedClasses) {
        final ClassName className = ClassName.get(type);

        info("Generating implementation for %s", type);

        final TypeSpec.Builder spec = TypeSpec
            .classBuilder(className.simpleName() + "Impl")
            .addSuperinterface(className)
            .addModifiers(Modifier.FINAL)
            .addAnnotation(ConfigSerializable.class)
            .addJavadoc("Automatically generated implementation of the config");

        final TypeSpecBuilderTracker tracker = new TypeSpecBuilderTracker();
        gatherElementSpec(tracker, type, generatedClasses);
        tracker.writeTo(spec);

        final String qualifiedName = className.reflectionName();
        generatedClasses.put(qualifiedName, qualifiedName + "Impl");
        info("Generated implementation for %s", type);

        return spec;
    }

    private void gatherElementSpec(
        final TypeSpecBuilderTracker spec,
        final TypeElement type,
        final Properties generatedClasses
    ) {
        // first handle own elements

        for (final Element enclosedElement : type.getEnclosedElements()) {
            final ElementKind kind = enclosedElement.getKind();

            if (kind == ElementKind.INTERFACE && hasAnnotation(enclosedElement, ConfigSerializable.class)) {
                spec.add(
                    enclosedElement.getSimpleName().toString(),
                    generateImplementation((TypeElement) enclosedElement, generatedClasses)
                        .addModifiers(Modifier.STATIC)
                );
                continue;
            }
            if (kind != ElementKind.METHOD) {
                continue;
            }

            final ExecutableElement element = (ExecutableElement) enclosedElement;

            final boolean excluded = hasAnnotation(element, Exclude.class);
            if (element.isDefault()) {
                if (excluded) {
                    // no need to handle them
                    continue;
                }
                info("Overriding implementation for %s as it's not excluded", element);
            } else if (excluded) {
                throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    "Cannot make config due to method %s, which is an excluded method that has no implementation!",
                    element
                ));
            }

            final List<? extends VariableElement> parameters = element.getParameters();
            if (parameters.size() > 1) {
                throw new IllegalStateException("Setters cannot have more than one parameter! Method: " + element);
            }

            final String simpleName = element.getSimpleName().toString();
            TypeMirror nodeType = element.getReturnType();

            if (parameters.size() == 1) {
                final VariableElement parameter = parameters.get(0);
                // setter
                spec.add(
                    simpleName + "#" + parameter.getSimpleName().toString(),
                    MethodSpec.overriding(element)
                        .addStatement(
                            "this.$N = $N",
                            element.getSimpleName(),
                            parameter.getSimpleName()
                        )
                );
                nodeType = parameter.asType();
            } else {
                // getter
                spec.add(
                    simpleName,
                    MethodSpec.overriding(element)
                        .addStatement("return $N", element.getSimpleName())
                );
            }

            spec.add(simpleName, FieldSpec.builder(TypeName.get(nodeType), simpleName, Modifier.PRIVATE));
        }

        // then handle parent elements
        for (final TypeMirror parent : type.getInterfaces()) {
            gatherElementSpec(spec, (TypeElement) this.typeUtils.asElement(parent), generatedClasses);
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private void writeMappings() {
        final FileObject resource;
        try {
            resource = this.filer.createResource(StandardLocation.SOURCE_OUTPUT, "", Constants.MAPPING_FILE);
            try (Writer writer = resource.openWriter()) {
                this.mappings.store(writer, null);
            }
        } catch (final IOException exception) {
            throw new RuntimeException("Failed to write interface mappings!", exception);
        }
    }

    private boolean hasAnnotation(final AnnotatedConstruct element, final Class<? extends Annotation> annotation) {
        //noinspection ConstantValue not everything is nonnull by default
        return element.getAnnotation(annotation) != null;
    }

    private boolean isNestedConfig(final TypeElement type) {
        if (!type.getNestingKind().isNested()) {
            return false;
        }

        Element current = type;
        while (current.getKind() == ElementKind.INTERFACE && hasAnnotation(current, ConfigSerializable.class)) {
            current = current.getEnclosingElement();
        }
        return current.getKind() == ElementKind.PACKAGE;
    }

    private void info(final String message, final Object... arguments) {
        this.messager.printMessage(Kind.NOTE, String.format(Locale.ROOT, message, arguments));
    }

}

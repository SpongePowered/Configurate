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

import static org.spongepowered.configurate.interfaces.processor.Utils.hasAnnotation;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.interfaces.meta.Field;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

class ConfigImplementationGenerator {

    private final ConfigImplementationGeneratorProcessor processor;
    private final TypeElement source;

    ConfigImplementationGenerator(
        final ConfigImplementationGeneratorProcessor processor,
        final TypeElement source
    ) {
        this.processor = processor;
        this.source = source;
    }

    /**
     * Returns the generated class or null if something went wrong during
     * generation.
     */
    public TypeSpec.@Nullable Builder generate() {
        final ClassName className = ClassName.get(this.source);

        final TypeSpec.Builder spec = TypeSpec
            .classBuilder(className.simpleName() + "Impl")
            .addSuperinterface(className)
            .addModifiers(Modifier.FINAL)
            .addAnnotation(ConfigSerializable.class)
            .addJavadoc("Automatically generated implementation of the config");

        final TypeSpecBuilderTracker tracker = new TypeSpecBuilderTracker();
        if (!gatherElementSpec(tracker, this.source)) {
            return null;
        }
        tracker.writeTo(spec);

        final String qualifiedName = className.reflectionName();
        final String qualifiedImplName = qualifiedName.replace("$", "Impl$") + "Impl";
        this.processor.generatedClasses().put(qualifiedName, qualifiedImplName);

        return spec;
    }

    /**
     * Returns true if successful, otherwise false.
     */
    private boolean gatherElementSpec(
        final TypeSpecBuilderTracker spec,
        final TypeElement type
    ) {
        return gatherElementSpec(spec, type, new HashSet<>());
    }

    /**
     * Returns true if successful, otherwise false.
     *
     * @param excludedElements a set of all elements a superclass has annotated with {@link Exclude}.
     */
    private boolean gatherElementSpec(
        final TypeSpecBuilderTracker spec,
        final TypeElement type,
        final Set<Name> excludedElements
    ) {
        // first handle own elements
        // If this interface is noted as ConfigSerializable, then its element order should override previous configs
        final boolean hasConfigSerializable = hasAnnotation(type, ConfigSerializable.class);

        for (final Element enclosedElement : type.getEnclosedElements()) {
            final ElementKind kind = enclosedElement.getKind();

            if (kind == ElementKind.INTERFACE && hasAnnotation(enclosedElement, ConfigSerializable.class)) {
                final TypeSpec.@Nullable Builder generated =
                        new ConfigImplementationGenerator(this.processor, (TypeElement) enclosedElement)
                                .generate();

                // if something went wrong in the child class, the parent can't complete normally either
                if (generated == null) {
                    return false;
                }

                spec.add(enclosedElement.getSimpleName().toString(), generated.addModifiers(Modifier.STATIC));
                continue;
            }

            if (kind != ElementKind.METHOD) {
                continue;
            }

            final ExecutableElement element = (ExecutableElement) enclosedElement;

            if (hasAnnotation(element, PostProcess.class)) {
                // A postprocess annotated method is not a config node
                continue;
            }

            if (excludedElements.contains(element.getSimpleName())) {
                continue;
            }
            final boolean excluded = hasAnnotation(element, Exclude.class);
            if (excluded) {
                if (element.isDefault()) {
                    // Do not add setters to the exclusion list as they will not be serialized anyway.
                    if (element.getParameters().isEmpty()) {
                        excludedElements.add(element.getSimpleName());
                    }
                    continue;
                }
                this.processor.printError(
                        "Cannot make config due to method %s, which is an excluded method that has no implementation!",
                        element
                );
                return false;
            }

            // all methods are either setters or getters past this point

            final List<? extends VariableElement> parameters = element.getParameters();
            if (parameters.size() > 1) {
                this.processor.printError("Setters cannot have more than one parameter! Method: " + element);
                return false;
            }

            final String simpleName = element.getSimpleName().toString();
            TypeMirror nodeType = element.getReturnType();

            if (parameters.size() == 1) {
                // setter
                final VariableElement parameter = parameters.get(0);
                final boolean success = handleSetter(element, simpleName, parameter, nodeType, spec);
                if (!success) {
                    return false;
                }
                nodeType = parameter.asType();
            } else {
                handleGetter(element, simpleName, nodeType, spec);
            }

            final FieldSpec.Builder fieldSpec = FieldSpec.builder(TypeName.get(nodeType), simpleName, Modifier.PRIVATE);

            final boolean isField = hasAnnotation(element, Field.class);
            if (isField) {
                fieldSpec.addModifiers(Modifier.TRANSIENT);
            }

            // set a default value for config subsections
            final TypeElement nodeTypeElement = Utils.toBoxedTypeElement(nodeType, this.processor.typeUtils);
            if (!isField && !element.isDefault() && hasAnnotation(nodeTypeElement, ConfigSerializable.class)) {
                ClassName configClass = ClassName.get(nodeTypeElement);
                if (nodeTypeElement.getKind().isInterface()) {
                    // first find the generated class for given type
                    String implName = this.processor.generatedClasses().getProperty(configClass.reflectionName());
                    if (implName == null) {
                        this.processor.printError("Could not determine an implementation type for method " + element.getSimpleName());
                        return false;
                    }
                    // make it canonical and replace superinterface type with source interface type if present
                    implName = implName.replace('$', '.').replace(type.getQualifiedName(), this.source.getQualifiedName());
                    configClass = ClassName.bestGuess(implName);
                }
                fieldSpec.initializer("new $T()", configClass);
            }

            //todo add tests for hidden in both ap and interfaces and defaults in interfaces
            AnnotationProcessorHandler.handle(this.source, element, nodeType, fieldSpec);

            // If this is a getter and ConfigSerializable, then it should define where in the config
            // this element should go.
            spec.add(simpleName, fieldSpec, hasConfigSerializable && element.getParameters().isEmpty());
        }

        // then handle parent elements
        for (final TypeMirror parent : type.getInterfaces()) {
            gatherElementSpec(spec, (TypeElement) this.processor.typeUtils.asElement(parent), excludedElements);
        }
        return true;
    }

    private boolean handleSetter(
            final ExecutableElement element,
            final String simpleName,
            final VariableElement parameter,
            final TypeMirror returnType,
            final TypeSpecBuilderTracker spec
    ) {
        final MethodSpec.Builder method = MethodSpec.overriding(element);

        // we have two main branches of setters, default non-void setters and non-default any setters
        if (element.isDefault()) {
            if (MoreTypes.isTypeOf(Void.TYPE, returnType)) {
                this.processor.printError("A default setter cannot have void as return type. Method: " + element);
                return false;
            }

            method.addStatement(
                    "this.$N = $T.super.$L($N)",
                    simpleName,
                    element.getEnclosingElement(),
                    simpleName,
                    parameter.getSimpleName()
            );
        } else {
            method.addStatement("this.$N = $N", simpleName, parameter.getSimpleName());
        }

        // if it's not void
        if (!MoreTypes.isTypeOf(Void.TYPE, returnType)) {
            // the return type can be a parent type of parameter, but it has to be assignable
            if (!this.processor.typeUtils.isAssignable(parameter.asType(), returnType)) {
                this.processor.printError(
                        "Cannot create a setter with return type %s for argument type %s. Method: %s",
                        returnType,
                        parameter.asType(),
                        element
                );
                return false;
            }
            method.addStatement("return this.$N", simpleName);
        }

        spec.add(simpleName + "#" + parameter.getSimpleName(), method);
        return true;
    }

    private void handleGetter(
            final ExecutableElement element,
            final String simpleName,
            final TypeMirror nodeType,
            final TypeSpecBuilderTracker spec
    ) {
        // voids aren't valid
        if (MoreTypes.isTypeOf(Void.TYPE, nodeType)) {
            this.processor.printError(
                    "Cannot create a getter with return type void for method %s, did you forget to @Exclude this method?",
                    element
            );
        }

        spec.add(
                simpleName,
                MethodSpec.overriding(element)
                        .addStatement("return $N", element.getSimpleName())
        );
    }

}

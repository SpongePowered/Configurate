package org.spongepowered.configurate.interfaces.processor;

import static org.spongepowered.configurate.interfaces.processor.Utils.hasAnnotation;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.Locale;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

class ConfigImplementationGenerator {

    private final ConfigImplementationGeneratorProcessor processor;
    private final TypeElement source;

    ConfigImplementationGenerator(
        final ConfigImplementationGeneratorProcessor processor,
        final TypeElement configInterfaceType
    ) {
        this.processor = processor;
        this.source = configInterfaceType;
    }

    public TypeSpec.Builder generate() {
        final ClassName className = ClassName.get(this.source);

        this.processor.info("Generating implementation for %s", this.source);

        final TypeSpec.Builder spec = TypeSpec
            .classBuilder(className.simpleName() + "Impl")
            .addSuperinterface(className)
            .addModifiers(Modifier.FINAL)
            .addAnnotation(ConfigSerializable.class)
            .addJavadoc("Automatically generated implementation of the config");

        final TypeSpecBuilderTracker tracker = new TypeSpecBuilderTracker();
        gatherElementSpec(tracker, this.source);
        tracker.writeTo(spec);

        final String qualifiedName = className.reflectionName();
        this.processor.generatedClasses().put(qualifiedName, qualifiedName + "Impl");
        this.processor.info("Generated implementation for %s", this.source);

        return spec;
    }

    private void gatherElementSpec(
        final TypeSpecBuilderTracker spec,
        final TypeElement type
    ) {
        // first handle own elements

        for (final Element enclosedElement : type.getEnclosedElements()) {
            final ElementKind kind = enclosedElement.getKind();

            if (kind == ElementKind.INTERFACE && hasAnnotation(enclosedElement, ConfigSerializable.class)) {
                spec.add(
                    enclosedElement.getSimpleName().toString(),
                    new ConfigImplementationGenerator(this.processor, (TypeElement) enclosedElement)
                        .generate()
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
                this.processor.info("Overriding implementation for %s as it's not excluded", element);
            } else if (excluded) {
                throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    "Cannot make config due to method %s, which is a method excluded method that has no implementation!",
                    element
                ));
            }

            // all methods are either setters or getters past this point

            final List<? extends VariableElement> parameters = element.getParameters();
            if (parameters.size() > 1) {
                throw new IllegalStateException("Setters cannot have more than one parameter! Method: " + element);
            }

            final String simpleName = element.getSimpleName().toString();
            TypeMirror nodeType = element.getReturnType();

            if (parameters.size() == 1) {
                // setter
                final VariableElement parameter = parameters.get(0);

                final MethodSpec.Builder method = MethodSpec.overriding(element)
                    .addStatement(
                        "this.$N = $N",
                        element.getSimpleName(),
                        parameter.getSimpleName()
                    );

                // if it's not void
                if (!MoreTypes.isTypeOf(Void.TYPE, nodeType)) {
                    // the return type can be a parent type of parameter, but it has to be assignable
                    if (!this.processor.typeUtils.isAssignable(parameter.asType(), nodeType)) {
                        throw new IllegalStateException(String.format(
                            "Cannot create a setter with return type %s for argument type %s. Method: %s",
                            nodeType,
                            parameter.asType(),
                            element
                        ));
                    }
                    method.addStatement("return this.$N", element.getSimpleName());
                }

                spec.add(simpleName + "#" + parameter.getSimpleName().toString(), method);
                nodeType = parameter.asType();
            } else {
                // getter
                spec.add(
                    simpleName,
                    MethodSpec.overriding(element)
                        .addStatement("return $N", element.getSimpleName())
                );
            }

            final FieldSpec.Builder fieldSpec = FieldSpec.builder(TypeName.get(nodeType), simpleName, Modifier.PRIVATE);

            //todo add tests for hidden in both ap and interfaces and defaults in interfaces
            AnnotationDefaults.process(element, nodeType, fieldSpec);
            AnnotationHidden.process(element, fieldSpec);

            spec.add(simpleName, fieldSpec);
        }

        // then handle parent elements
        for (final TypeMirror parent : type.getInterfaces()) {
            gatherElementSpec(spec, (TypeElement) this.processor.typeUtils.asElement(parent));
        }
    }

}

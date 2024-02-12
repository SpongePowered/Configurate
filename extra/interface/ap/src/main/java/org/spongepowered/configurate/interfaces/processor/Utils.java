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

import com.google.auto.common.MoreTypes;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.lang.annotation.Annotation;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

final class Utils {

    private Utils() {}

    static boolean hasAnnotation(final AnnotatedConstruct element, final Class<? extends Annotation> annotation) {
        return annotation(element, annotation) != null;
    }

    /**
     * The same as {@link AnnotatedConstruct#getAnnotation(Class)} except that
     * you don't have to suppress the ConstantValue warning everywhere.
     */
    @SuppressWarnings("DataFlowIssue")
    static <T extends Annotation> @Nullable T annotation(final AnnotatedConstruct construct, final Class<T> annotation) {
        return construct.getAnnotation(annotation);
    }

    static boolean isNestedConfig(final TypeElement type) {
        if (!type.getNestingKind().isNested()) {
            return false;
        }

        Element current = type;
        while (current.getKind() == ElementKind.INTERFACE && hasAnnotation(current, ConfigSerializable.class)) {
            current = current.getEnclosingElement();
        }
        return current.getKind() == ElementKind.PACKAGE;
    }

    static boolean isDecimal(final TypeMirror typeMirror) {
        return MoreTypes.isTypeOf(Float.TYPE, typeMirror) || MoreTypes.isTypeOf(Double.TYPE, typeMirror);
    }

    static boolean isNumeric(final TypeMirror typeMirror) {
        return MoreTypes.isTypeOf(Byte.TYPE, typeMirror) || MoreTypes.isTypeOf(Character.TYPE, typeMirror)
            || MoreTypes.isTypeOf(Short.TYPE, typeMirror) || MoreTypes.isTypeOf(Integer.TYPE, typeMirror)
            || MoreTypes.isTypeOf(Long.TYPE, typeMirror);
    }

    public static TypeElement toBoxedTypeElement(final TypeMirror mirror, final Types typeUtils) {
        if (mirror.getKind().isPrimitive()) {
            return typeUtils.boxedClass(MoreTypes.asPrimitiveType(mirror));
        }
        return MoreTypes.asTypeElement(mirror);
    }

}

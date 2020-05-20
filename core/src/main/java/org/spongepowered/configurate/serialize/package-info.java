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
/**
 * Type serializer mechanisms and the standard type serializers
 * shipped with Configurate.
 *
 * <p>Type serializers are registered and queried using
 * {@link org.spongepowered.configurate.serialize.TypeSerializerCollection}. For
 * scalar serializers (those that take a scalar configuration value only), the
 * {@link org.spongepowered.configurate.serialize.ScalarSerializer} class
 * provides many common behaviours. Core scalar serializers are included in
 * the {@link org.spongepowered.configurate.serialize.Scalars} interface.
 */
@DefaultQualifier(NonNull.class)
package org.spongepowered.configurate.serialize;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

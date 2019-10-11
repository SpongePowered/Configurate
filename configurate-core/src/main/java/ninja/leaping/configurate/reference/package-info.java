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
 * File watcher and infrastructure to support dynamically updating configurations
 * <p>
 * There are three core components of this package:
 *
 * <ul>
 *     <li>The {@link ninja.leaping.configurate.reference.WatchServiceListener} provides a
 *     reactive interface to listen to filesystem changes</li>
 *     <li>{@link ninja.leaping.configurate.reference.ConfigurationReference ConfigurationReferences}
 *     can either be manually updated, or attached to a watch service, and hold a loader plus
 *     a loaded node, to pass around a reference to a root node while allowing for reloads.</li>
 *     <li>{@link ninja.leaping.configurate.reference.ValueReference Value references} can
 *     be retrieved from configuration references, and provide an always up-to-date reference to
 *     a deserialized value within a configuration node</li>
 * </ul>
 * <p>
 * These components are designed to work together, but the watch listener can be used
 * independently from the other components for any situation where file watching is necessary.
 */
@DefaultQualifier(value = NonNull.class)
package ninja.leaping.configurate.reference;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

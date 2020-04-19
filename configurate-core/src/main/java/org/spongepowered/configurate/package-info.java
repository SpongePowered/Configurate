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
 * Core node data structures and supports
 *
 * At the core of Configurate are nodes.
 * We have 3 variants to work with:
 * <ul>
 *     <li><code>BasicConfigurationNode</code> for a standard node</li>
 *     <li>{@link org.spongepowered.configurate.CommentedConfigurationNode} for configurations that can have attached comments</li>
 *     <li>{@link org.spongepowered.configurate.AttributedConfigurationNode} for configuration structures that have attributes on each node, like XML</li>
 * </ul>
 */
@DefaultQualifier(NonNull.class)
package org.spongepowered.configurate;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

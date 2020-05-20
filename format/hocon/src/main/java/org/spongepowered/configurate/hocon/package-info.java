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
 * Configuration loader for the <a href="https://github.com/lightbend/config/">HOCON</a> library, plus supports
 * <p>
 * Due to limitations in the library currently used, this loader does not respect nodes' map factories. This means that
 * ordering in configurations will not be preserved.
 */
package org.spongepowered.configurate.hocon;

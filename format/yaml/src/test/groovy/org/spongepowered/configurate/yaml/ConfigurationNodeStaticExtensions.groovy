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
package org.spongepowered.configurate.yaml

import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.CommentedConfigurationNode

class ConfigurationNodeStaticExtensions {

    static BasicConfigurationNode root(
        final BasicConfigurationNode unused,
        final @DelegatesTo(value = BasicConfigurationNode, strategy = Closure.DELEGATE_FIRST) Closure action
    ) {
        def root = BasicConfigurationNode.root()
        action.setDelegate(root)
        action.resolveStrategy = Closure.DELEGATE_FIRST
        action.call(root)
        return root
    }

    static CommentedConfigurationNode root(
        final CommentedConfigurationNode unused,
        final @DelegatesTo(value = CommentedConfigurationNode, strategy = Closure.DELEGATE_FIRST) Closure action
    ) {
        def root = CommentedConfigurationNode.root()
        action.setDelegate(root)
        action.resolveStrategy = Closure.DELEGATE_FIRST
        action.call(root)
        return root
    }

}

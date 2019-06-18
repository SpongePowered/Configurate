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
package org.spongepowered.configurate.transformation;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.SortedMap;

/**
 * Implements a number of child {@link ConfigurationTransformation}s which are only applied if required,
 * according to the configurations current version.
 */
class VersionedTransformation extends ConfigurationTransformation {
    private final Object[] versionPath;
    private final SortedMap<Integer, ConfigurationTransformation> versionTransformations;

    VersionedTransformation(Object[] versionPath, SortedMap<Integer, ConfigurationTransformation> versionTransformations) {
        this.versionPath = versionPath;
        this.versionTransformations = versionTransformations;
    }

    @Override
    public void apply(@NonNull ConfigurationNode node) {
        ConfigurationNode versionNode = node.getNode(versionPath);
        int currentVersion = versionNode.getInt(-1);
        for (SortedMap.Entry<Integer, ConfigurationTransformation> entry : versionTransformations.entrySet()) {
            if (entry.getKey() <= currentVersion) {
                continue;
            }
            entry.getValue().apply(node);
            currentVersion = entry.getKey();
        }
        versionNode.setValue(currentVersion);
    }
}
